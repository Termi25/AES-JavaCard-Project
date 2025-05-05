#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SRC_DIR="$ROOT_DIR/main"
JAR_DIR="$ROOT_DIR/jars"

CP="$ROOT_DIR:$JAR_DIR/*"

echo "Compiling Host.java..."
javac -cp "$CP" -d "$ROOT_DIR"  \
      "$SRC_DIR"/AESApplet.java \
      "$SRC_DIR"/Host.java

echo "Running Host..."
cd "$ROOT_DIR"
java -cp "$CP" main.Host "$@"