@echo off
setlocal EnableExtensions EnableDelayedExpansion

set PRODUCT_NAME="${package.full.name}"
set VERSION="${package.version}"
set VENDOR="${package.vendor}"
set COPYRIGHT="${package.copyright}"
set LIBRARY_PATH="$ROOTDIR/app/lib/native"

set app[0]=lecturePresenter
set app[1]=lectureEditor
set app[2]=lecturePlayer

set class[0]=org.lecturestudio.presenter.swing.PresenterApplication
set class[1]=org.lecturestudio.editor.javafx.EditorFxApplication
set class[2]=org.lecturestudio.player.javafx.PlayerFxApplication

set icon[0]=${project.parent.parent.basedir}/lect-presenter-swing/src/main/resources/gfx/app-icon/128.ico
set icon[1]=${project.parent.parent.basedir}/lect-editor-fx/src/main/resources/gfx/app-icon/128.ico
set icon[2]=${project.parent.parent.basedir}/lect-player-fx/src/main/resources/gfx/app-icon/128.ico

set jar[0]=lect-presenter-swing.jar
set jar[1]=lect-editor-fx.jar
set jar[2]=lect-player-fx.jar

set MANIFEST_FILE="app.manifest"
set MANIFEST_VERSION_VAR="${assembly.manifest.version}"
set MANIFEST_VERSION="${package.version}.0"

:: Retrieve Windows SDK directory.
for /f "tokens=2*" %%a in ('REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Microsoft\Microsoft SDKs\Windows\v10.0" /v InstallationFolder') do set "WindowsSdkDir=%%~b"
for /f "tokens=2*" %%a in ('REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Microsoft\Microsoft SDKs\Windows\v10.0" /v ProductVersion') do set "ProductVersion=%%~b"

:: mt.exe is available in the Microsoft Windows Software Development Kit (SDK).
set MT_BIN="%WindowsSdkDir%\bin\%ProductVersion%.0\x64\mt"

:: Start with modules not discovered with jdeps.
set MODULES="java.security.jgss,jdk.zipfs"

:: Retrieve modules.
for /l %%n in (0,1,2) do (
	echo Get !app[%%n]! modules
	for /F %%i in ('jdeps ^
					--class-path "${package.input.dir}\lib\*" ^
					--multi-release 15 ^
					--ignore-missing-deps ^
					--print-module-deps ^
					-R -q ^
					"${package.input.dir}\!jar[%%n]!"') do (
		echo "%%i"
		if [!MODULES!] == [] (
			set "MODULES=%%i"
		) else (
			set "MODULES=!MODULES!,%%i"
		)
	)
)

:: Create the Runtime.
echo Create Runtime

jlink ^
	--no-header-files --no-man-pages ^
	--compress=1 ^
	--strip-debug ^
	--strip-native-commands ^
	--add-modules="%MODULES%" ^
	--output "%PRODUCT_NAME%\runtime"

for /l %%n in (0,1,2) do (
	echo Packaging !app[%%n]!

	set app_path="%PRODUCT_NAME%\!app[%%n]!"
	set exe_path=!app_path!.exe

	REM Create the self-contained Java application package.
	jpackage ^
		--type app-image ^
		--input "${package.input.dir}" ^
		--runtime-image "%PRODUCT_NAME%\runtime" ^
		--dest "%PRODUCT_NAME%" ^
		--java-options -Xmx2048m ^
		--java-options -Djava.library.path=%LIBRARY_PATH% ^
		--app-version %VERSION% ^
		--name !app[%%n]! ^
		--main-jar !jar[%%n]! ^
		--main-class !class[%%n]! ^
		--icon !icon[%%n]! ^
		--vendor %VENDOR% ^
		--copyright %COPYRIGHT%

	REM Remove Runtime since there is a shared one.
	rmdir /Q/S "!app_path!\runtime"

	REM Copy all files of the generated application package to the common bundle folder.
	robocopy !app_path! %PRODUCT_NAME%\ /NFL /NDL /NJH /NJS /nc /ns /np /MOV /E

	REM Remove read-only file attribute
	attrib -R !exe_path!
	REM Add Windows assembly manifest to the generated application .exe to set DPI awareness.
	%MT_BIN% -nologo -manifest %MANIFEST_FILE% -outputresource:!exe_path!;#1
	REM Set read-only file attribute again
	attrib +R !exe_path!

	REM Remove the individual application package.
	del /F/Q/S !app_path! > NUL
	rmdir /Q/S !app_path!
)

:: Remove unnecessary resources from the final bundle package.
del /F/Q/S "%PRODUCT_NAME%\api-ms-win-*.dll" > NUL
del /F/Q/S "%PRODUCT_NAME%\msvcp140.dll" > NUL
del /F/Q/S "%PRODUCT_NAME%\ucrtbase.dll" > NUL
del /F/Q/S "%PRODUCT_NAME%\vcruntime140.dll" > NUL
del /F/Q/S "%PRODUCT_NAME%\*.ico" > NUL
del /F/Q/S "%PRODUCT_NAME%\.jpackage.xml" > NUL

endlocal