#!/bin/bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
cd "$HOME/arclight-build"
python3 - <<'PY'
from pathlib import Path
p = Path('gradlew')
data = p.read_bytes().replace(b'\r\n', b'\n').replace(b'\r', b'\n')
assert b'dirname' in data
p.write_bytes(data)
PY
echo "=== starting build collect ==="
# unbuffered live log
stdbuf -oL -eL bash ./gradlew build collect --stacktrace --no-daemon 2>&1 | stdbuf -oL -eL tee /tmp/arclight-build.log
