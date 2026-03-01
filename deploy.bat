@echo off
setlocal

set "APP_DIR=%~dp0"
set "WAR_PATH=%APP_DIR%target\location.war"

if "%TOMCAT_WEBAPPS_DIR%"=="" (
    set "TOMCAT_WEBAPPS_DIR=C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps"
)

echo [1/3] Build Maven (WAR)...
cd /d "%APP_DIR%"
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERREUR] Echec du build Maven.
    exit /b 1
)

if not exist "%WAR_PATH%" (
    echo [ERREUR] WAR introuvable: %WAR_PATH%
    exit /b 1
)

if not exist "%TOMCAT_WEBAPPS_DIR%" (
    echo [ERREUR] Dossier Tomcat webapps introuvable: %TOMCAT_WEBAPPS_DIR%
    echo Definissez la variable TOMCAT_WEBAPPS_DIR puis relancez.
    exit /b 1
)

echo [2/3] Copie du WAR vers Tomcat...
copy /Y "%WAR_PATH%" "%TOMCAT_WEBAPPS_DIR%\location.war" >nul
if errorlevel 1 (
    echo [ERREUR] Copie vers Tomcat echouee.
    exit /b 1
)

echo [3/3] Termine
echo WAR deploye: %TOMCAT_WEBAPPS_DIR%\location.war
echo Redemarrez Tomcat si necessaire.
endlocal