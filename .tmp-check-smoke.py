#!/usr/bin/env python3
from pathlib import Path
import re
import glob

logs = sorted(glob.glob("/tmp/arclight-smoke-*.log"), key=lambda p: Path(p).stat().st_mtime, reverse=True)
print("logs:", logs[:3])
if not logs:
    print("NO_SMOKE_LOG")
else:
    log = Path(logs[0])
    text = log.read_text(encoding="utf-8", errors="replace")
    print("log", log, "size", len(text))
    for pat in [
        r"Done \(",
        r"TechReborn setup",
        r"Unknown registry",
        r"Failed to parse recipe",
        r"SMOKE",
        r"techreborn",
        r"reborncore",
    ]:
        hits = [ln for ln in text.splitlines() if re.search(pat, ln, re.I)]
        print(f"=== {pat} count={len(hits)} ===")
        for ln in hits[-20:]:
            print(ln[:300])

mods = Path("/home/spligan/arclight-build/bootstrap/run_prod/neoforge/mods")
if mods.is_dir():
    print("mods:")
    for p in sorted(mods.iterdir()):
        print(" ", p.name, p.stat().st_size)

libs = Path("/home/spligan/tr-neoforge-build/build/libs")
if libs.is_dir():
    print("libs:")
    for p in sorted(libs.iterdir()):
        print(" ", p.name, p.stat().st_size)
