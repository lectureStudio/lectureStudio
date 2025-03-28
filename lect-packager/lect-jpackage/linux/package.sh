#!/bin/bash

# lecturePresenter is the main launcher and lectureEditor is the secondary
# launcher which parameters are defined in the separate properties file.
# Both applications are packaged together and share one runtime.

JAVA_VERSION=${package.java.version}

PRODUCT_NAME="${package.full.name}"
PRESENTER_NAME="${package.presenter.name}"
PRESENTER_CLASS="${package.presenter.class}"
PRESENTER_JAR="${package.presenter.jar}"
PRESENTER_ICON="${package.presenter.icon}"
EDITOR_NAME="${package.editor.name}"
EDITOR_JAR="${package.editor.jar}"
VERSION="${package.version}"
VENDOR="${package.vendor}"
DESCRIPTION="${package.description}"
ABOUT_URL="${package.about.url}"
LICENSE="${package.license}"
LICENSE_TYPE="${package.license.type}"
COPYRIGHT="${package.copyright}"
INPUT_DIR="${package.input.dir}"
OUTPUT_DIR="${package.output.dir}"

LIBRARY_PATH=\$APPDIR/lib/native

COMMON_PARAMS=(--java-options -Xmx4096m \
	--java-options -Djava.library.path="$LIBRARY_PATH" \
	--java-options -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager \
	--description "$DESCRIPTION" \
	--app-version "$VERSION" \
	--vendor "$VENDOR" \
	--copyright "$COPYRIGHT" \
	--name "$PRESENTER_NAME" \
	--main-class "$PRESENTER_CLASS" \
	--main-jar "$PRESENTER_JAR" \
	--icon "$PRESENTER_ICON" \
	--add-launcher lectureEditor=resources/lectureEditor.properties)

LINUX_PARAMS=("${COMMON_PARAMS[@]}" \
	--input "$INPUT_DIR" \
	--dest "$OUTPUT_DIR" \
	--about-url "$ABOUT_URL" \
	--file-associations "resources/lectureEditor-File-Association.properties" \
	--runtime-image "runtime" \
	--linux-package-name "lecturestudio" \
	--linux-menu-group "Education" \
	--linux-app-category "education" \
	--linux-app-release "linux" \
	--linux-shortcut \
	--license-file "$LICENSE" \
	--resource-dir resources)

mkdir "$PRODUCT_NAME"

# Start with modules not discovered with jdeps.
MODULES="jdk.localedata,java.security.jgss,java.security.sasl,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.zipfs"

# Retrieve the required modules by the applications.

# $1: Application name
# $2: Application jar file
getAppModules() {
	app=${1?:"Need an application."}
	appJar=${2?:"Need an application jar file."}

	echo "Get $app Modules"
	
	modules=$(jdeps \
		--class-path "$INPUT_DIR/lib/*" \
		--multi-release $JAVA_VERSION \
		--ignore-missing-deps \
		--print-module-deps \
		-R -q \
		"$INPUT_DIR/$appJar")

	echo "$modules"

	if [ -z "$MODULES" ]
	then
		MODULES="$modules"
	else
		MODULES="$MODULES,$modules"
	fi
}

getAppModules $PRESENTER_NAME $PRESENTER_JAR
getAppModules $EDITOR_NAME $EDITOR_JAR

# Create runtime with modules required by the applications.
echo "Create Runtime"

jlink --no-header-files --no-man-pages \
	--strip-debug \
	--strip-native-commands \
	--include-locales=de,en \
	--add-modules="$MODULES" \
	--output "runtime"

# Create the self-contained Java application package. Used only for the ZIP archive.
echo "Create Application Image"

jpackage "${COMMON_PARAMS[@]}" \
	--type app-image \
	--input "$INPUT_DIR" \
	--runtime-image "runtime"

# Copy all files of the generated application package to the common bundle folder.
cp -npR "$PRESENTER_NAME"/* "$PRODUCT_NAME/"

# Remove the individual application package.
rm -Rf "$PRESENTER_NAME"

# Create a manually installable ZIP package.
chmod +x "$PRODUCT_NAME"/lib/app/lib/native/ffmpeg

echo "Create ZIP Archive"

zip -r -q "$OUTPUT_DIR/$PRODUCT_NAME".zip "$PRODUCT_NAME"

# Create installable DEB package.
echo "Create DEB Package"

jpackage "${LINUX_PARAMS[@]}" \
	--type deb \
	--temp temp_deb

# Create installable RPM package.
echo "Create RPM Package"

jpackage "${LINUX_PARAMS[@]}" \
	--type rpm \
	--linux-rpm-license-type "$LICENSE_TYPE" \
	--temp temp_rpm

