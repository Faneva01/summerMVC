#!/bin/sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
SRC_DIR="$ROOT_DIR/src"
LIB_DIR="$ROOT_DIR/lib"
BUILD_DIR=${SUMMERMVC_BUILD_DIR:-"$ROOT_DIR/target/framework"}
CLASS_DIR="$BUILD_DIR/classes"
JAR_DIR="$BUILD_DIR/jar"
DEST_DIR=${SUMMERMVC_LIB_DIR:-"$ROOT_DIR/test_summerMVC/lib"}

echo "Nettoyage du build summerMVC..."
rm -rf "$BUILD_DIR"
mkdir -p "$CLASS_DIR" "$JAR_DIR" "$DEST_DIR"

echo "Compilation du framework..."
find "$SRC_DIR" -name '*.java' -print > "$BUILD_DIR/sources.list"
javac -cp "$LIB_DIR/*" -d "$CLASS_DIR" @"$BUILD_DIR/sources.list"

echo "Création du JAR..."
jar cf "$JAR_DIR/summermvc.jar" -C "$CLASS_DIR" .
cp -f "$JAR_DIR/summermvc.jar" "$DEST_DIR/summermvc.jar"

echo "JAR créé : $JAR_DIR/summermvc.jar"
echo "JAR copié : $DEST_DIR/summermvc.jar"
