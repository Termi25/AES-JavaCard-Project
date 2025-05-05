#!/usr/bin/env bash
set -e

JDK8_HOME="/usr/lib/jvm/java-8-openjdk-amd64" # Adjust this path to your JDK8 installation

SRC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/main"
CLASS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/classes"
JC_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/jcdk"

mkdir -p "$CLASS_DIR"

echo "Compiling AESApplet.java → $CLASS_DIR"
"$JDK8_HOME/bin/javac" \
        -g \
        -proc:none \
        -source 1.7 -target 1.7 \
        -classpath "$JC_HOME/lib/*" \
        -encoding utf8 \
        -d "$CLASS_DIR" \
        "$SRC_DIR/AESApplet.java"

echo "Running Java-Card converter …"
chmod +x "$JC_HOME/bin/converter.sh"      # just in case
"$JC_HOME/bin/converter.sh" \
        -classdir "$CLASS_DIR" \
        -applet 0xa0:0x00:0x00:0x00:0x62:0x12:0x34 AESApplet main \
        0xa0:0x00:0x00:0x00:0x62:0x12:0x35 1.0 \
        -out CAP \
        -verbose