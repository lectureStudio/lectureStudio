#!/bin/bash

createKeyChain() {
	echo "Preparing keychain for signing"

	keychain=lectureStudio
	keychain_pw=`head /dev/urandom | base64 | head -c 50`

	# Create new empty keychain.
	security create-keychain -p "${keychain_pw}" "${keychain}"

	# Add keychain to user's keychain search list.
	security list-keychains -d user -s "${keychain}" $(security list-keychains -d user | tr -d '"')

	# Remove re-lock timeout on keychain.
	security set-keychain-settings "${keychain}"

	# Import the certificates.
	security import "${package.dev.cert}" -k "${keychain}" -P "${package.dev.cert.password}" -T "/usr/bin/codesign" -T "/usr/bin/productbuild"

	# Set to access this identity from command line with tools shipped by apple.
	security set-key-partition-list -S apple-tool:,apple: -s -k "$keychain_pw" -t private ${keychain} 1> /dev/null

	# Set default keychain to temporary keychain.
	security default-keychain -d user -s ${keychain}

	# Unlock keychain.
	security unlock-keychain -p ${keychain_pw} ${keychain}

	# Prove we added the code signing identity to the temp keychain.
	security find-identity -v -p codesigning
}

signFile() { # $1: file path
	filepath=${1?:"Need a file path."}

	codesign -s "$DEV_ID_APP" \
		--timestamp \
		--entitlements entitlements.plist \
		-o runtime --deep -vvv -f "$filepath"
}

signJarFiles() { # $1: dir path
	dirpath=${1?:"Need a directory path."}

	mkdir signed-libs

	for jarFile in $dirpath
	do
		jar tf "$jarFile" | grep -E "dylib|jnilib" | while read -r lib ; do
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
	status=$(xcrun altool --notarization-info "$requestUUID" \
							--username "${package.dev.username}" \
							--password "${package.dev.password}" 2>&1 \
			| awk -F ': ' '/Status:/ { print $2; }' )
	echo "$status"
}

notarizeFile() { # $1: path to file to notarize, $2: identifier
	filepath=${1:?"Need a file path."}
	identifier=${2:?"Need an identifier."}

	# Upload the app to the Notarization Service.
	echo "Uploading $filepath for notarization."
	requestUUID=$(xcrun altool --notarize-app \
								--primary-bundle-id "$identifier" \
								--username "${package.dev.username}" \
								--password "${package.dev.password}" \
								--file "$filepath" 2>&1 \
				| awk '/RequestUUID/ { print $NF; }')

	echo "Notarization Request-UUID: $requestUUID"

	if [[ $requestUUID == "" ]]; then
		echo "Could not upload for notarization."
		exit 1
	fi

	# Wait for status to be not "in progress" any more.
	request_status="in progress"
	while [[ "$request_status" == "in progress" ]]; do
		echo -n "waiting... "
		sleep 10
		request_status=$(requestStatus "$requestUUID")
		echo "$request_status"
	done

	# Print status information.
	xcrun altool --notarization-info "$requestUUID" \
				--username "${package.dev.username}" \
				--password "${package.dev.password}"
	echo

	if [[ $request_status != "success" ]]; then
		echo "Could not notarize $filepath"
		exit 1
	fi
}

PRODUCT_NAME="${package.full.name}"
VERSION="${package.version}"
VENDOR="${package.vendor}"
COPYRIGHT="${package.copyright}"
DEV_ID_APP="${package.dev.id.app}"
DEV_ID_INSTALLER="${package.dev.id.installer}"
LIBRARY_PATH=\$ROOTDIR/app/lib/native

app[0]=lecturePresenter
app[1]=lectureEditor
app[2]=lecturePlayer

class[0]=org.lecturestudio.presenter.swing.PresenterApplication
class[1]=org.lecturestudio.editor.javafx.EditorFxApplication
class[2]=org.lecturestudio.player.javafx.PlayerFxApplication

icon[0]=${project.parent.parent.basedir}/lect-presenter-swing/src/main/resources/gfx/app-icon/128.icns
icon[1]=${project.parent.parent.basedir}/lect-editor-fx/src/main/resources/gfx/app-icon/128.icns
icon[2]=${project.parent.parent.basedir}/lect-player-fx/src/main/resources/gfx/app-icon/128.icns

jar[0]=lect-presenter-swing.jar
jar[1]=lect-editor-fx.jar
jar[2]=lect-player-fx.jar

assoc[0]=
assoc[1]="--file-associations rec-file.association"
assoc[2]=

mkdir $PRODUCT_NAME

# Start with modules not discovered with jdeps.
MODULES="jdk.localedata,java.security.jgss,jdk.zipfs"

# Retrieve modules.
for value in {0..2}
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
	--compress=1 \
	--strip-debug \
	--strip-native-commands \
	--include-locales=de,en \
	--add-modules="$MODULES" \
	--output "$PRODUCT_NAME/runtime/Contents/Home"

for value in {0..2}
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
		--java-options -Xmx2048m \
		--java-options -Djava.library.path="$LIBRARY_PATH" \
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
	plutil -insert "NSMicrophoneUsageDescription" \
		-string "This app requires access to your microphone." \
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

	# Sign all dylib/s.
	signDir "$PRODUCT_NAME/runtime" "*.dylib"
	signDir "$PRODUCT_NAME/app/lib/native" "*.dylib"
	signJarFiles "$PRODUCT_NAME/app/lib/*.jar"

	# Sign all apps.
	for value in {0..2}
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

	# Clean up temp keychain we created.
	security delete-keychain "${keychain}"

	# Notarizing the Installer Package.
	notarizeFile "${package.output.dir}/$PRODUCT_NAME".pkg "org.lecturestudio"

	# Staple the ticket to the Installer Package.
	xcrun stapler staple "${package.output.dir}/$PRODUCT_NAME".pkg
fi
