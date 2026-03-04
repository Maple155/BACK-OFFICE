#!/bin/bash

set -e

APP_DIR=$(cd "$(dirname "$0")" && pwd)
WAR_PATH="$APP_DIR/target/location.war"
TOMCAT_WEBAPPS_DIR="${TOMCAT_WEBAPPS_DIR:-/Users/faniry/ITU/utils/apache-tomcat-10.1.49/webapps}"

echo "[1/3] Build Maven (WAR)..."
cd "$APP_DIR"
mvn clean package -DskipTests

if [ ! -f "$WAR_PATH" ]; then
    echo "[ERREUR] WAR introuvable: $WAR_PATH"
    exit 1
fi

if [ ! -d "$TOMCAT_WEBAPPS_DIR" ]; then
    echo "[ERREUR] Dossier Tomcat webapps introuvable: $TOMCAT_WEBAPPS_DIR"
    echo "Définissez TOMCAT_WEBAPPS_DIR puis relancez."
    exit 1
fi

echo "[2/3] Copie du WAR vers Tomcat..."
cp "$WAR_PATH" "$TOMCAT_WEBAPPS_DIR/location.war"

echo "[3/3] Terminé"
echo "WAR déployé: $TOMCAT_WEBAPPS_DIR/location.war"
echo "Redémarrez Tomcat si nécessaire."