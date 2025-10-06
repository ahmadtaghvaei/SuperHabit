#!/bin/bash
set -e

GRADLE_WRAPPER="./gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER" ]; then
  echo "Gradle wrapper JAR not found at $GRADLE_WRAPPER"
  exit 1
fi

java -jar "$GRADLE_WRAPPER" "$@"
