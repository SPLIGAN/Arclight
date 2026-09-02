#!/usr/bin/env bash
set -euo pipefail
cd ~/tr-neoforge-build
echo "=== compile ==="
bash ./gradlew :RebornCore:compileJava compileJava compileClientJava --stacktrace --no-daemon
echo "=== build ==="
bash ./gradlew build --stacktrace --no-daemon
JAR=$(ls -1 build/libs/techreborn-*.jar | grep -v sources | grep -v javadoc | head -1)
echo "JAR=$JAR"
mkdir -p ~/arclight-build/bootstrap/run_prod/neoforge/mods
cp -f "$JAR" ~/arclight-build/bootstrap/run_prod/neoforge/mods/
echo eula=true > ~/arclight-build/bootstrap/run_prod/neoforge/eula.txt
echo "=== smoke ==="
cd ~/arclight-build
pkill -f runProdNeoforge 2>/dev/null || true
sleep 2
LOG=/tmp/arclight-smoke-$(date +%Y%m%d-%H%M%S).log
set +e
timeout 240s ./gradlew :bootstrap:runProdNeoforge --no-daemon 2>&1 | tee "$LOG"
RC=${PIPESTATUS[0]}
set -e
echo "smoke exit=$RC log=$LOG"
python3 - "$LOG" <<'PY'
from pathlib import Path
import re, sys
text = Path(sys.argv[1]).read_text(encoding="utf-8", errors="replace")
print("Done", "Done (" in text)
print("setup", "TechReborn setup done" in text)
parse = sum(1 for l in text.splitlines() if "Couldn't parse data file" in l)
print("parse_errors", parse)
unk = sorted(set(re.findall(r"Unknown registry key in ResourceKey\[minecraft:root / minecraft:item\]: ([^\s;]+)", text)))
print("unknown_items", len(unk))
for u in unk[:40]:
    print(" ", u)
print("SMOKE_OK" if ("Done (" in text and parse < 50) else "SMOKE_FAIL")
PY
