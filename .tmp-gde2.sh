#!/bin/bash
set -euo pipefail
JAR=/home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.1.2.73/5596011a67b8716d928cf7897d140680b0626606/neoforge-26.1.2.73-sources.jar
TMP=/tmp/nf-src
rm -rf "$TMP"
mkdir -p "$TMP"
cd "$TMP"
jar xf "$JAR" net/neoforged/neoforge/data/event/GatherDataEvent.java 2>/dev/null || true
find . -name 'GatherDataEvent*.java' -print
# try listing
jar tf "$JAR" | grep -i GatherData | head -30
# extract all matching
jar tf "$JAR" | grep GatherDataEvent | while read -r p; do jar xf "$JAR" "$p"; done
for f in $(find . -name 'GatherDataEvent*.java'); do
  echo "==== $f ===="
  sed -n '1,160p' "$f"
done
# also DatapackBuiltinEntriesProvider usage examples if any
jar tf "$JAR" | grep -i DatapackBuiltin | head -10
