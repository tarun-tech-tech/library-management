@echo off
setlocal
set "BASE_DIR=%~dp0"
set "MAVEN_VERSION=3.9.11"
set "MAVEN_DIR=%BASE_DIR%.mvn\wrapper\apache-maven-%MAVEN_VERSION%"
set "MAVEN_ARCHIVE=%BASE_DIR%.mvn\wrapper\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

if exist "%MAVEN_DIR%\bin\mvn.cmd" goto run
if not exist "%BASE_DIR%.mvn\wrapper" mkdir "%BASE_DIR%.mvn\wrapper"
if not exist "%MAVEN_ARCHIVE%" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ARCHIVE%'"
    if errorlevel 1 exit /b %errorlevel%
)
set "TEMP_DIR=%BASE_DIR%.mvn\wrapper\.extract-%RANDOM%"
mkdir "%TEMP_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%MAVEN_ARCHIVE%' -DestinationPath '%TEMP_DIR%' -Force"
if errorlevel 1 exit /b %errorlevel%
if exist "%MAVEN_DIR%" rmdir /s /q "%MAVEN_DIR%"
move "%TEMP_DIR%\apache-maven-%MAVEN_VERSION%" "%MAVEN_DIR%" >nul
rmdir /s /q "%TEMP_DIR%"

:run
call "%MAVEN_DIR%\bin\mvn.cmd" %*
exit /b %errorlevel%
