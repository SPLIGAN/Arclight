#!/bin/bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

TR_WIN=/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1
TR_BUILD=$HOME/tr-neoforge-build

# restore wrapper from Windows source
cp "$TR_WIN/gradlew" "$TR_BUILD/gradlew"
python3 - <<'PY'
from pathlib import Path
p = Path.home() / 'tr-neoforge-build' / 'gradlew'
data = p.read_bytes().replace(b'\r\n', b'\n').replace(b'\r', b'\n')
assert b'dirname' in data and b'warn' in data
p.write_bytes(data)
p.chmod(0o755)
print('gradlew restored', len(data))
PY

# remove orphan REI service
rm -f "$TR_BUILD/src/main/resources/META-INF/services/me.shedaniel.rei.api.client.plugins.REIClientPlugin"
rm -f "$TR_WIN/src/main/resources/META-INF/services/me.shedaniel.rei.api.client.plugins.REIClientPlugin"
# sync services dir
if [[ -d "$TR_WIN/src/main/resources/META-INF/services" ]]; then
  mkdir -p "$TR_BUILD/src/main/resources/META-INF/services"
  rsync -a --delete "$TR_WIN/src/main/resources/META-INF/services/" "$TR_BUILD/src/main/resources/META-INF/services/"
else
  rm -rf "$TR_BUILD/src/main/resources/META-INF/services"
fi

cd "$TR_BUILD"
echo "=== rebuild techreborn ==="
bash ./gradlew build --stacktrace --no-daemon
mkdir -p "$TR_WIN/build/libs"
cp -f build/libs/techreborn-6.0.2+local.jar "$TR_WIN/build/libs/"
echo "=== services in jar ==="
jar tf build/libs/techreborn-6.0.2+local.jar | grep 'META-INF/services' || echo 'no services entries (ok)'
