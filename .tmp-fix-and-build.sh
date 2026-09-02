#!/bin/bash
set -euo pipefail
SRC=/mnt/c/Users/SPLIGAN/github/Arclight
DST=$HOME/arclight-build
cp "$SRC/gradlew" "$DST/gradlew"
python3 - <<'PY'
from pathlib import Path
p = Path.home() / 'arclight-build' / 'gradlew'
data = p.read_bytes().replace(b'\r\n', b'\n').replace(b'\r', b'\n')
# sanity: dirname must remain
assert b'dirname' in data, 'gradlew corrupted (dirname missing)'
assert b'warn' in data, 'gradlew corrupted (warn missing)'
p.write_bytes(data)
print('gradlew ok', len(data))
PY
chmod +x "$DST/gradlew"
# also fix other shell scripts that may have been tr-damaged
find "$DST" -name '*.sh' -o -name 'gradlew' | head
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
cd "$DST"
bash ./gradlew :buildSrc:compileJava --stacktrace
