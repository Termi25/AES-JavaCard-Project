@echo off

set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
set "SRC_DIR=%ROOT_DIR%\main"
set "CLASS_DIR=%ROOT_DIR%\classes"
set "JCDK_DIR=%ROOT_DIR%\jcdk"      REM ← adjust if SDK sits elsewhere

echo Compiling AESApplet.java...
if not exist "%CLASS_DIR%" mkdir "%CLASS_DIR%"
set "JCP=%JCDK_DIR%\lib\*;%ROOT_DIR%"

javac -g -source 1.7 -target 1.7 ^
      -classpath "%JCP%" ^
      -encoding utf8 ^
      -d "%CLASS_DIR%" ^
      "%SRC_DIR%\AESApplet.java"

echo Running Java-Card converter...
set "PKG_AID=0xa0:0x00:0x00:0x00:0x62:0x12:0x35"
set "APP_AID=0xa0:0x00:0x00:0x00:0x62:0x12:0x34"

pushd "%JCDK_DIR%\bin" >nul
converter.bat ^
  -classdir "%CLASS_DIR%" ^
  -applet %APP_AID% AESApplet main ^
  %PKG_AID% 1.0 ^
  -out CAP ^
  -verbose
popd >nul