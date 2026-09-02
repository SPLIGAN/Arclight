#!/bin/bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
TR_WIN=/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1
TR=$HOME/tr-neoforge-build
test -d "$TR"
# sync Java sources needed for setId fix
rsync -a --delete "$TR_WIN/src/main/java/techreborn/blocks/" "$TR/src/main/java/techreborn/blocks/"
rsync -a \
  "$TR_WIN/src/main/java/techreborn/init/TRBlockSettings.java" \
  "$TR_WIN/src/main/java/techreborn/init/TRContent.java" \
  "$TR_WIN/src/main/java/techreborn/init/ModFluids.java" \
  "$TR/src/main/java/techreborn/init/"
rsync -a "$TR_WIN/src/main/java/techreborn/events/ModRegistry.java" "$TR/src/main/java/techreborn/events/"
rm -f "$TR/src/main/resources/META-INF/services/me.shedaniel.rei.api.client.plugins.REIClientPlugin" || true
# good gradlew from Arclight tree (linux path already good in arclight-build)
cp "$HOME/arclight-build/gradlew" "$TR/gradlew"
chmod +x "$TR/gradlew"
# ensure wrapper jar exists
ls -la "$TR/gradle/wrapper/"
cd "$TR"
echo "=== compileJava ==="
bash ./gradlew compileJava --stacktrace --no-daemon 2>&1 | tee /tmp/tr-compile.log | tail -n 150
