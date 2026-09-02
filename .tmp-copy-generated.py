#!/usr/bin/env python3
import shutil
from pathlib import Path
src = Path.home() / "tr-neoforge-build/src/main/generated"
dst = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/src/main/generated")
if dst.exists():
    shutil.rmtree(dst, ignore_errors=True)
count = 0
for p in src.rglob("*"):
    if ".cache" in p.parts:
        continue
    rel = p.relative_to(src)
    target = dst / rel
    if p.is_dir():
        target.mkdir(parents=True, exist_ok=True)
    else:
        target.parent.mkdir(parents=True, exist_ok=True)
        with open(p, "rb") as f:
            data = f.read()
        with open(target, "wb") as f:
            f.write(data)
        count += 1
print(f"copied {count} files")
print("placed:", len(list((dst / "data/techreborn/worldgen/placed_feature").glob("*.json"))))
print("configured:", len(list((dst / "data/techreborn/worldgen/configured_feature").glob("*.json"))))
print("damage:", len(list((dst / "data/techreborn/damage_type").glob("*.json"))))
