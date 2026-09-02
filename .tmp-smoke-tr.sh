#!/bin/bash
set -euo pipefail
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
ARC="$HOME/arclight-build"
RUN="$ARC/bootstrap/run_prod/neoforge"
echo "eula=true" > "$RUN/eula.txt"
ls -lah "$RUN/mods/"
cd "$ARC"
# restore gradlew if needed
python3 - <<'PY'
from pathlib import Path
src = Path('/mnt/c/Users/SPLIGAN/github/Arclight/gradlew')
dst = Path('gradlew')
data = src.read_bytes().replace(b'\r\n', b'\n').replace(b'\r', b'\n')
assert b'dirname' in data
dst.write_bytes(data)
dst.chmod(0o755)
PY
LOG=/tmp/arclight-smoke.log
rm -f "$LOG"
stdbuf -oL -eL bash ./gradlew :bootstrap:runProdNeoforge --no-daemon 2>&1 | stdbuf -oL -eL tee "$LOG" &
PID=$!
DEADLINE=$((SECONDS + 600))
while (( SECONDS < DEADLINE )); do
  if ! kill -0 "$PID" 2>/dev/null; then echo "gradle exited"; break; fi
  if grep -Eq 'Done \(|For help, type|Game crashed|InvalidModuleDescriptorException|FAILED TO BIND' "$LOG" 2>/dev/null; then
    echo "=== stop condition matched ==="
    break
  fi
  sleep 5
done
pkill -P "$PID" 2>/dev/null || true
kill "$PID" 2>/dev/null || true
sleep 2
pkill -f 'runProdNeoforge' 2>/dev/null || true
pkill -f 'arclight-neoforge' 2>/dev/null || true
echo "=== KEY LINES ==="
grep -Eiw 'techreborn|reborncore|arclight|Done \(|InvalidModule|Fabric API|ERROR|For help' "$LOG" | head -150 || true
echo "=== TAIL ==="
tail -40 "$LOG"
