#!/bin/bash

PROJECT_NAME="summermvc"

SRC_DIR="src"
BUILD_DIR="build"
CLASS_DIR="$BUILD_DIR/classes"
JAR_DIR="$BUILD_DIR/jar"

SERVLET_API="lib/servlet-api.jar"

echo "Nettoyage..."
rm -rf "$BUILD_DIR"

mkdir -p "$CLASS_DIR"
mkdir -p "$JAR_DIR"

echo "Compilation..."

javac \
    -cp "$SERVLET_API" \
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

echo ""
echo "Terminé."
echo "JAR généré :"
echo "$JAR_DIR/$PROJECT_NAME.jar"