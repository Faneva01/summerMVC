#!/bin/bash

PROJECT_NAME="summermvc"

SRC_DIR="src"
BUILD_DIR="build"
CLASS_DIR="$BUILD_DIR/classes"
JAR_DIR="$BUILD_DIR/jar"

LIB_DIR="lib"


DEST_DIR="/home/faneva/L2/S4/Web_dynamique/framework/summerMVC/test_summerMVC/lib"


echo "Nettoyage..."
rm -rf "$BUILD_DIR"


mkdir -p "$CLASS_DIR"
mkdir -p "$JAR_DIR"


echo "Compilation..."


javac \
    -cp "$LIB_DIR/*" \
    -d "$CLASS_DIR" \
    $(find "$SRC_DIR" -name "*.java")



if [ $? -ne 0 ]; then

    echo "Erreur de compilation"
    exit 1

fi



echo "Création du JAR..."


jar cf \
    "$JAR_DIR/$PROJECT_NAME.jar" \
    -C "$CLASS_DIR" .



echo "Déploiement du JAR..."


mkdir -p "$DEST_DIR"


cp -f \
"$JAR_DIR/$PROJECT_NAME.jar" \
"$DEST_DIR/"



if [ $? -eq 0 ]; then

    echo "JAR copié avec succès dans : $DEST_DIR"

else

    echo "Erreur lors de la copie du JAR"
    exit 1

fi



echo ""
echo "Terminé."
echo "JAR généré et déployé :"
echo "$DEST_DIR/$PROJECT_NAME.jar"