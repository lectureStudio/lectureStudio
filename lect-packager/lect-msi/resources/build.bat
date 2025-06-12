@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd "%~dp0"
mkdir build

cd MSI

wix build -arch x64 ^
    Package.wxs ^
    Folders.wxs ^
    AppComponents.wxs ^
    Shortcuts.wxs ^
    lang\Package.en-us.wxl ^
    -culture en-US ^
    -out ${package.msi.file}

wix msi validate ${package.msi.file}

cd ..\Bundle

wix build -arch x64 ^
    -ext WixToolset.Bal.wixext ^
    -ext WixToolset.UI.wixext ^
    -culture de-DE ^
    -culture en-US ^
    Bundle.wxs ^
    -out ${package.output.dir}\${package.full.name}.exe