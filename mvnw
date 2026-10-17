#!/bin/sh

set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_VERSION=3.9.11
MAVEN_DIR="$BASE_DIR/.mvn/wrapper/apache-maven-$MAVEN_VERSION"
MAVEN_ARCHIVE="$BASE_DIR/.mvn/wrapper/apache-maven-$MAVEN_VERSION-bin.zip"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"

if [ ! -x "$MAVEN_DIR/bin/mvn" ]; then
    mkdir -p "$BASE_DIR/.mvn/wrapper"
    if [ ! -f "$MAVEN_ARCHIVE" ]; then
        if command -v curl >/dev/null 2>&1; then
            curl --fail --location --retry 3 --output "$MAVEN_ARCHIVE" "$MAVEN_URL"
        elif command -v wget >/dev/null 2>&1; then
            wget --output-document="$MAVEN_ARCHIVE" "$MAVEN_URL"
        else
            echo "mvnw requires curl or wget to download Maven $MAVEN_VERSION" >&2
            exit 1
        fi
    fi
    TEMP_DIR="$BASE_DIR/.mvn/wrapper/.extract-$$"
    rm -rf "$TEMP_DIR"
    mkdir -p "$TEMP_DIR"
    if command -v unzip >/dev/null 2>&1; then
        unzip -q "$MAVEN_ARCHIVE" -d "$TEMP_DIR"
    else
        JAVA_BIN="${JAVA_HOME:-}/bin/jar"
        if [ ! -x "$JAVA_BIN" ]; then JAVA_BIN=jar; fi
        "$JAVA_BIN" --extract --file "$MAVEN_ARCHIVE" --dir "$TEMP_DIR"
    fi
    rm -rf "$MAVEN_DIR"
    mv "$TEMP_DIR/apache-maven-$MAVEN_VERSION" "$MAVEN_DIR"
    rm -rf "$TEMP_DIR"
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
