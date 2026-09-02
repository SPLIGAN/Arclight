#!/bin/bash
set -euo pipefail
JAR=$(find /home/spligan/.gradle/caches -name 'neoforge-26.1.2.73-sources.jar' 2>/dev/null | head -1)
echo "JAR=$JAR"
unzip -l "$JAR" | grep -i GatherData | head -30
TMP=/tmp/nf-src
rm -rf "$TMP"
mkdir -p "$TMP"
cd "$TMP"
unzip -q "$JAR" 'net/neoforged/neoforge/data/event/GatherDataEvent*.java' 2>/dev/null || unzip -q "$JAR" '**/GatherDataEvent*.java'
find . -name 'GatherDataEvent*.java' 
for f in $(find . -name 'GatherDataEvent*.java'); do
  echo "==== $f ===="
  head -n 120 "$f"
done
