@echo off
@REM ================================
@REM === Configuration des variables
@REM ================================
set BUILD_DIR=build
set SRC_DIR=src\java
set WEBAPP_DIR=src\main\webapp
set LIB_DIR=lib

@REM set SERVLET_API_JAR=C:\Program Files\Apache Software Foundation\apache-tomcat-10.1.28\lib\servlet-api.jar
@REM set TOMCAT_WEBAPPS_DIR=C:\Program Files\Apache Software Foundation\apache-tomcat-10.1.28\webapps

set SERVLET_API_JAR=C:\Program Files\Apache Software Foundation\Tomcat 10.1\lib\servlet-api.jar
set TOMCAT_WEBAPPS_DIR=C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps

set WAR_NAME=location.war

@REM ================================
@REM === Etape 0 - Nettoyage du dossier build
@REM ================================
if exist %BUILD_DIR% (
    echo Le dossier %BUILD_DIR% existe deja. Suppression...
    rmdir /s /q %BUILD_DIR%
)

@REM ================================
@REM === Etape 1 - Creation du dossier build
@REM ================================
echo Creation du dossier %BUILD_DIR%...
mkdir %BUILD_DIR%
mkdir %BUILD_DIR%\WEB-INF
mkdir %BUILD_DIR%\WEB-INF\classes

@REM ================================
@REM === Etape 2 - Compilation des fichiers Java
@REM ================================
echo Compilation des fichiers Java...

@REM :: Lister tous les fichiers .java
dir /b /s %SRC_DIR%\*.java > sources.txt

@REM :: Construire le classpath avec servlet-api.jar + tous les jars du dossier lib
set "CLASSPATH=%SERVLET_API_JAR%;%LIB_DIR%\*;%SRC_DIR%"

@REM :: Compiler (IMPORTANT: utiliser des guillemets autour de -cp)
javac -parameters -cp "%CLASSPATH%" -d %BUILD_DIR%\WEB-INF\classes @sources.txt
if errorlevel 1 (
    echo [ERREUR] Erreur lors de la compilation. Verifiez vos fichiers Java.
    del sources.txt
    pause
    exit /b
)

del sources.txt

@REM ================================
@REM === Etape 3 - Copier les fichiers webapp
@REM ================================
echo Copie des fichiers du dossier %WEBAPP_DIR% vers %BUILD_DIR%...
xcopy /e /i "%WEBAPP_DIR%" "%BUILD_DIR%" >nul

@REM ================================
@REM === Etape 4 - Creation du fichier WAR
@REM ================================
echo Creation du fichier .war...
cd %BUILD_DIR%
jar -cvf %WAR_NAME% * >nul
cd ..

@REM ================================
@REM === Etape 5 - Deploiement dans Tomcat
@REM ================================
echo Deploiement du fichier .war vers Tomcat...
copy %BUILD_DIR%\%WAR_NAME% "%TOMCAT_WEBAPPS_DIR%" >nul

@REM ================================
@REM === Etape 6 - Fin
@REM ================================
echo.
echo ======================================
echo Deploiement termine avec succes !
echo Le fichier .war a ete copie dans :
echo %TOMCAT_WEBAPPS_DIR%\%WAR_NAME%
echo ======================================
echo.
echo Vous pouvez maintenant redemarrer Tomcat pour tester votre application.
pause