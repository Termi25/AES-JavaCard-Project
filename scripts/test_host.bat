@echo off

pushd "%~dp0.."

set "CP=%CD%;%CD%\jars\*"

if not exist "main\Host.class" (
    echo Compiling Host.java...
    javac -encoding utf8 -cp "%CP%" -d . "main\Host.java"
) else (
    for %%F in ("main\Host.java") do set "SRC_TS=%%~tF"
    for %%F in ("main\Host.class") do set "CLS_TS=%%~tF"
    if "%SRC_TS%" GTR "%CLS_TS%" (
        echo Re-compiling Host.java...
        javac -encoding utf8 -cp "%CP%" -d . "main\Host.java"
    )
)

echo Running Host...
java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp "%CP%" main.Host

popd