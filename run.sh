#!/bin/bash
cd "$(dirname "$0")" || exit 1

if ! javac -version >/dev/null 2>&1; then
  echo "Java JDK not found. Install it with:  brew install --cask temurin"
  exit 1
fi

mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -cp out univoice.CampusTracker "$@"
