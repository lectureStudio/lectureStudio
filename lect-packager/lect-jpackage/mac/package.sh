#!/bin/bash

PRODUCT_NAME="${package.full.name}"
VERSION="${package.version}"
VENDOR="${package.vendor}"
COPYRIGHT="${package.copyright}"
DEV_ID_APP=$APPLE_DEV_ID_APP
DEV_ID_INSTALLER=$APPLE_DEV_ID_INSTALLER
DEV_TEAM_ID=$APPLE_DEV_TEAM_ID
DEV_USER=$APPLE_DEV_USER
DEV_PW=$APPLE_DEV_PW
DEV_CERT_PATH=$APPLE_CERTIFICATE_PATH
DEV_CERT_PW=$APPLE_CERTIFICATE_PW

createKeyChain() {
	echo "Preparing keychain for signing"

	keychain_name="lectureStudio"
	keychain="${keychain_name}-db"
	keychain_pw=`head /dev/urandom | base64 | head -c 50`

	# Delete existing keychain if it exists
	security delete-keychain "${keychain}" 2>/dev/null || true

	# Create new empty keychain.
	security create-keychain -p "${keychain_pw}" "${keychain}"

	# Add keychain to user's keychain search list.
	security list-keychains -d user -s "${keychain}" $(security list-keychains -d user | tr -d '"')

	# Remove re-lock timeout on keychain.
	security set-keychain-settings "${keychain}"

	# Import the certificates.
	security import "$DEV_CERT_PATH" -k "${keychain}" -P "$DEV_CERT_PW" -T "/usr/bin/codesign" -T "/usr/bin/productbuild"

	# Set to access this identity from command line with tools shipped by apple.
	security set-key-partition-list -S apple-tool:,apple: -s -k "$keychain_pw" -t private "${keychain}" 1> /dev/null

	# Set default keychain to temporary keychain.
	security default-keychain -d user -s "${keychain}"

	# Unlock keychain.
	security unlock-keychain -p "${keychain_pw}" "${keychain}"

	# Prove we added the code signing identity to the temp keychain.
	security find-identity -v -p codesigning -s "${keychain}"

	echo "Keychain ${keychain} created and configured successfully"
}

signFile() { # $1: file path
	filepath=${1?:"Need a file path."}

	plutil -convert xml1 entitlements.plist

	codesign -s "$DEV_ID_APP" \
		--timestamp \
		--entitlements entitlements.plist \
		-o runtime --deep -vvv -f "$filepath"
}

signJarFiles() { # $1: dir path
	dirpath=${1?:"Need a directory path."}

	mkdir -p signed-libs

	for jarFile in "$dirpath"/*.jar
	do
		# List files in JAR and filter for native libraries/executables
		# Exclude directories by ensuring the line doesn't end with '/'
		jar tf "$jarFile" | grep -v '/$' | grep -E '\.(dylib|jnilib)$|/(ffmpeg|ffprobe)$' | while read -r lib ; do
			echo "Signing native library in JAR: $lib"
			unzip -qq "$jarFile" "$lib" -d signed-libs
			signFile "signed-libs/$lib"
			jar uf "$jarFile" -C "signed-libs" "$lib"
			rm "signed-libs/$lib"
		done
	done
}

signDir() { # $1: dir path, $2: extension
	dirpath=${1?:"Need a directory path."}
	extension=${2:-"*"}

	find "$dirpath" -type f -name "$extension" -print0 | \
		while IFS= read -r -d '' file; do signFile "$file"; done
}

# Sign all dylibs and app bundles.
requestStatus() { # $1: requestUUID
	requestUUID=${1?:"Need a request UUID."}

	status=$(set -x; xcrun notarytool log \
          --apple-id "$APPLE_DEV_USER" \
          --team-id "$APPLE_DEV_TEAM_ID" \
          --password "$APPLE_DEV_PW" \
          "$requestUUID" 2>&1)
	echo "$status"
}

notarizeFile() { # $1: path to file to notarize
	filepath=${1:?"Need a file path."}

	echo "Uploading $filepath for notarization..."

	# Submit for notarization and capture the submission ID
	submit_response=$(xcrun notarytool submit "$filepath" \
		--apple-id "$DEV_USER" \
		--team-id "$DEV_TEAM_ID" \
		--password "$DEV_PW" \
		--wait \
		--timeout 600s \
		--output-format json 2>&1)

	echo "Notarization response: $submit_response"

	# Extract the submission ID from the response
	submit_uuid=$(echo "$submit_response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

	if [[ -z "$submit_uuid" ]]; then
		echo "ERROR: Could not extract submission UUID from notarization response"
		echo "Full response: $submit_response"
		exit 1
	fi

	echo "Submission UUID: $submit_uuid"

	# Check the notarization status
	echo "Checking notarization status..."
	status_response=$(xcrun notarytool info "$submit_uuid" \
		--apple-id "$DEV_USER" \
		--team-id "$DEV_TEAM_ID" \
		--password "$DEV_PW" \
		--output-format json 2>&1)

	echo "Status response: $status_response"

	# Extract status from response
	status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

	echo "Notarization status: $status"

	if [[ "$status" != "Accepted" ]]; then
		echo "ERROR: Notarization failed with status: $status"

		# Get detailed log for debugging
		echo "Fetching notarization log for debugging..."
		xcrun notarytool log "$submit_uuid" \
			--apple-id "$DEV_USER" \
			--team-id "$DEV_TEAM_ID" \
			--password "$DEV_PW"

		exit 1
	fi

	echo "Notarization successful!"
}

LIBRARY_PATH=\$ROOTDIR/app/lib/native

app[0]=lecturePresenter
app[1]=lectureEditor

class[0]=org.lecturestudio.presenter.swing.PresenterApplication
class[1]=org.lecturestudio.editor.javafx.EditorFxApplication

icon[0]=${project.parent.parent.basedir}/lect-presenter-swing/src/main/resources/gfx/app-icon/128.icns
icon[1]=${project.parent.parent.basedir}/lect-editor-fx/src/main/resources/gfx/app-icon/128.icns

jar[0]=lect-presenter-swing.jar
jar[1]=lect-editor-fx.jar

assoc[0]=
assoc[1]="--file-associations rec-file.association"

mkdir $PRODUCT_NAME

# Start with modules not discovered with jdeps.
MODULES="jdk.localedata,java.security.jgss,java.security.sasl,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.zipfs"

# Retrieve modules.
for value in {0..1}
do
	echo "Get ${app[$value]} modules"

	modules=$(/usr/libexec/java_home -v 15 --exec jdeps \
		--class-path "${package.input.dir}/lib/*" \
		--multi-release 15 \
		--ignore-missing-deps \
		--print-module-deps \
		-R -q \
		"${package.input.dir}/${jar[$value]}")

	echo "$modules"

	if [ -z "$MODULES" ]
	then
		MODULES="$modules"
	else
		MODULES="$MODULES,$modules"
	fi
done

# Create the Runtime.
echo "Create Runtime"

/usr/libexec/java_home -v 15 --exec jlink \
	--no-header-files --no-man-pages \
	--strip-debug \
	--strip-native-commands \
	--include-locales=de,en \
	--add-modules="$MODULES" \
	--output "$PRODUCT_NAME/runtime/Contents/Home"

for value in {0..1}
do
	app_name=${app[$value]}
	app_path=$PRODUCT_NAME/$app_name

	echo "Packaging $app_name";

	# Create the self-contained Java application package.
	/usr/libexec/java_home -v 15 --exec jpackage \
		--type app-image \
		--input "${package.input.dir}" \
		--runtime-image "$PRODUCT_NAME/runtime/Contents/Home" \
		--dest "$PRODUCT_NAME" \
		--java-options -Xmx4096m \
		--java-options -Djava.library.path="$LIBRARY_PATH" \
		--java-options -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager \
		--app-version $VERSION \
		--name $app_name \
		--main-jar ${jar[$value]} \
		--main-class ${class[$value]} \
		--icon "${icon[$value]}" \
		--vendor "$VENDOR" \
		--copyright "$COPYRIGHT" \
		${assoc[$value]}

	# Patch Info.plist.
	app_plist="$app_path.app/Contents/Info.plist"

	plutil -replace "LSApplicationCategoryType" \
		-string "public.app-category.education" \
		"$app_plist"

	# Add privacy descriptions for camera and microphone.
	plutil -insert "NSCameraUsageDescription" \
		-string "This app requires access to your camera." \
		"$app_plist"
	plutil -insert "NSScreenCaptureUsageDescription" \
		-string "This app needs screen recording permission to capture and share your screen." \
		"$app_plist"

	# Remove Runtime since there is a shared one.
	rm -R "$app_path.app/Contents/runtime"

	# Move apps to the common bundle folder.
	app_dir="$PRODUCT_NAME/app"

	if [ -d "$app_dir" ]; then
		rm -R "$app_path.app/Contents/app/lib"
		find "$app_path.app/Contents/app/" -name "*.jar" -type f -delete
	else
		mkdir "$app_dir"
		mv "$app_path.app/Contents/app/lib" "$app_dir"
		find "$app_path.app/Contents/app" -type f -name "*.jar" -print0 | \
			while IFS= read -r -d '' file; do mv "$file" "$app_dir"; done
	fi

	# Patch runtime and library paths.
	app_cfg=$app_path.app/Contents/app/$app_name.cfg

	sed -i '' 's/$ROOTDIR\//\$ROOTDIR\/..\//g' "$app_cfg"
	sed -i '' 's/$APPDIR\//\$ROOTDIR\/..\/app\//g' "$app_cfg"
	sed -i "" -e $'1 a\\\n'"app.runtime=\$ROOTDIR/../runtime" "$app_cfg"
done

# Make FFmpeg executable.
chmod +x $PRODUCT_NAME/app/lib/native/ffmpeg

if [ -z "$DEV_ID_APP" ]
then
	echo "Won't sign app."

	zip -r -q ${package.output.dir}/$PRODUCT_NAME.zip $PRODUCT_NAME
else
	createKeyChain

	signFile "$PRODUCT_NAME/app/lib/native/ffmpeg"

	signFile "$PRODUCT_NAME/runtime/Contents/Home/lib/jspawnhelper"

	# Sign all dylib/s.
	signDir "$PRODUCT_NAME/runtime" "*.dylib"
	signDir "$PRODUCT_NAME/app/lib/native" "*.dylib"
	signJarFiles "$PRODUCT_NAME/app/lib/"

	# Sign all apps.
	for value in {0..1}
	do
		app_path=$PRODUCT_NAME/${app[$value]}

		signDir "$app_path.app/Contents/MacOS"
		signFile "$app_path.app"
	done

	# Build the Installer Package.
	pkgbuild --analyze --root $PRODUCT_NAME apps.plist
	# Telling pkgbuild to not re-locate.
	plutil -replace BundleIsRelocatable -bool NO apps.plist

	pkgbuild --root $PRODUCT_NAME \
		--component-plist apps.plist \
		--identifier "org.lecturestudio" \
		--version "$VERSION" \
		--install-location "Applications/lectureStudio/" \
		"$PRODUCT_NAME".pkg

	productbuild --distribution distribution.xml \
		--resources . \
		--package-path "$PRODUCT_NAME".pkg \
		--keychain ${keychain} \
		--sign "$DEV_ID_INSTALLER" \
		"${package.output.dir}/$PRODUCT_NAME".pkg

	# Verify the package signature
	echo "Verifying package signature..."
	pkgutil --check-signature "${package.output.dir}/$PRODUCT_NAME".pkg
	if [ $? -ne 0 ]; then
		echo "ERROR: Package signature verification failed"
		exit 1
	fi

	# Clean up temp keychain we created.
	echo "Cleaning up temporary keychain: ${keychain}"
	security delete-keychain "${keychain}"

	# Notarizing the Installer Package.
	echo "Starting notarization process..."
	notarizeFile "${package.output.dir}/$PRODUCT_NAME".pkg

	# Staple the ticket to the Installer Package.
	echo "Stapling notarization ticket..."
	xcrun stapler staple "${package.output.dir}/$PRODUCT_NAME".pkg

	# Verify stapling was successful
	echo "Verifying stapling..."
	xcrun stapler validate "${package.output.dir}/$PRODUCT_NAME".pkg
	if [ $? -eq 0 ]; then
		echo "SUCCESS: Package is properly signed, notarized, and stapled!"
	else
		echo "ERROR: Stapling verification failed"
		exit 1
	fi

	echo "Package creation complete: ${package.output.dir}/$PRODUCT_NAME.pkg"
fi
