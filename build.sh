#!/bin/bash

# ---------------- CONFIG ----------------
# On utilise le répertoire courant comme base
APP_DIR=$(pwd)
WAR_NAME="location"
BUILD_DIR="$APP_DIR/build"

# Structure source selon ton premier exemple
SRC_JAVA="$APP_DIR/src/java"
WEBAPP_DIR="$APP_DIR/src/main/webapp"
LIB_DIR="$APP_DIR/lib"

# Chemin vers Tomcat (à adapter selon ton OS)
SERVLET_API="/opt/tomcat/lib/servlet-api.jar"
TOMCAT_WEBAPPS="/opt/tomcat/webapps"

# --------------- RESET BUILD -----------
echo "Nettoyage du répertoire build..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/WEB-INF/classes"
mkdir -p "$BUILD_DIR/WEB-INF/lib"

# ---------------- COMPILATION -----------
# Initialisation du Classpath avec la Servlet API
CP="$SERVLET_API"

# Ajout dynamique de tous les JARs du dossier lib
if [ -d "$LIB_DIR" ]; then
    for jar in "$LIB_DIR"/*.jar; do
        if [ -f "$jar" ]; then
            CP="$CP:$jar"
        fi
    done
fi

echo "Compilation des fichiers Java..."
# On liste les fichiers .java et on compile
find "$SRC_JAVA" -name "*.java" > "$BUILD_DIR/sources.txt"
javac -parameters -cp "$CP" -d "$BUILD_DIR/WEB-INF/classes" @"$BUILD_DIR/sources.txt"

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation"
    rm "$BUILD_DIR/sources.txt"
    exit 1
fi
rm "$BUILD_DIR/sources.txt"

# -------------- RESSOURCES WEB ---------
echo "Copie des ressources web (JSP, CSS, etc.)..."
if [ -d "$WEBAPP_DIR" ]; then
    # On copie tout le contenu du dossier webapp vers la racine du build
    cp -r "$WEBAPP_DIR"/. "$BUILD_DIR/"
fi

# ----------------- LIBS -----------------
echo "Copie des bibliothèques JAR..."
if [ -d "$LIB_DIR" ]; then
    cp -r "$LIB_DIR"/* "$BUILD_DIR/WEB-INF/lib/" 2>/dev/null
fi

# ----------------- WAR ------------------
echo "Création du fichier WAR..."
cd "$BUILD_DIR" || exit 1
# On crée le war à la racine du projet, pas à l'intérieur du dossier build
jar cf "../$WAR_NAME.war" *

echo "✅ Terminé ! Le fichier WAR a été créé : $APP_DIR/$WAR_NAME.war"

# ---------------- DEPLOY ----------------
# Optionnel : décommenter pour déployer automatiquement
# echo "Déploiement vers Tomcat..."
# cp "../$WAR_NAME.war" "$TOMCAT_WEBAPPS/"