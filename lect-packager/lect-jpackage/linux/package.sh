#!/bin/bash

PRODUCT_NAME="${package.full.name}"
VERSION="${package.version}"
VENDOR="${package.vendor}"
COPYRIGHT="${package.copyright}"
LIBRARY_PATH=\$ROOTDIR/lib/app/lib/native

app[0]=lecturePresenter
app[1]=lectureEditor

class[0]=org.lecturestudio.presenter.swing.PresenterApplication
class[1]=org.lecturestudio.editor.javafx.EditorFxApplication

icon[0]=${project.parent.parent.basedir}/lect-presenter-swing/src/main/resources/gfx/app-icon/128.png
icon[1]=${project.parent.parent.basedir}/lect-editor-fx/src/main/resources/gfx/app-icon/128.png

jar[0]=lect-presenter-swing.jar
jar[1]=lect-editor-fx.jar

mkdir "$PRODUCT_NAME"

# Start with modules not discovered with jdeps.
MODULES="jdk.localedata,java.security.jgss,java.security.sasl,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.zipfs"

# Retrieve modules.
for value in {0..1}
do
	echo "Get ${app[$value]} modules"

	modules=$(jdeps \
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

jlink \
	--no-header-files --no-man-pages \
	--compress=1 \
	--strip-debug \
	--strip-native-commands \
	--include-locales=de,en \
	--add-modules="$MODULES" \
	--output "runtime"

for value in {0..1}
do
	app_name=${app[$value]}

	echo "Packaging $app_name";

	if [ -n "${icon[$value]}" ]; then
		app_icon="--icon ${icon[$value]}"
	fi

	# Create the self-contained Java application package.
	jpackage \
		--type app-image \
		--input "${package.input.dir}" \
		--runtime-image "runtime" \
		--java-options -Xmx2048m \
		--java-options -Djava.library.path="$LIBRARY_PATH" \
		--app-version "$VERSION" \
		--name $app_name \
		--main-jar ${jar[$value]} \
		--main-class ${class[$value]} \
		--vendor "$VENDOR" \
		--copyright "$COPYRIGHT" \
		${app_icon}

	# Copy all files of the generated application package to the common bundle folder.
	cp -npR $app_name/* "$PRODUCT_NAME/"

	# Remove the individual application package.
	rm -Rf $app_name

	unset app_icon
done

chmod +x "$PRODUCT_NAME"/lib/app/lib/native/ffmpeg

zip -r -q "${package.output.dir}/$PRODUCT_NAME".zip "$PRODUCT_NAME"
