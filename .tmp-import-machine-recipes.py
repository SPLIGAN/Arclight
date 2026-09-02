#!/usr/bin/env python3
"""Copy machine (+ blasting) recipes from Fabric TR 6.0.2 jar into NeoForge resources."""
from __future__ import annotations

import json
import shutil
import zipfile
from pathlib import Path

JAR = Path("/tmp/tr602/TechReborn-6.0.2.jar")
# Prefer WSL mirror if present, else Windows mount
CANDIDATES = [
    Path("/home/spligan/tr-neoforge-build"),
    Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1"),
]
ROOT = next((p for p in CANDIDATES if (p / "src/main/resources").is_dir()), None)
if ROOT is None:
    raise SystemExit("no TR project root found")

DEST = ROOT / "src/main/resources"
print("DEST", DEST)

SKIP_KINDS = {"crafting_table", "smelting"}  # already present in tree
INCLUDE_BLASTING = True

# fabric:load_conditions -> neoforge:conditions mapping for known patterns
def convert_conditions(obj: dict) -> dict:
    """Return recipe dict with NeoForge conditions; drop fabric key."""
    fabric = obj.pop("fabric:load_conditions", None)
    if not fabric:
        return obj
    neoforge = []
    for cond in fabric:
        ctype = cond.get("condition")
        if ctype == "fabric:all_mods_loaded":
            # NeoForge: mod_loaded for each? Use AND of mod_loaded
            mods = cond.get("values") or []
            if len(mods) == 1:
                neoforge.append({"type": "neoforge:mod_loaded", "modid": mods[0]})
            elif mods:
                neoforge.append(
                    {
                        "type": "neoforge:and",
                        "values": [
                            {"type": "neoforge:mod_loaded", "modid": m} for m in mods
                        ],
                    }
                )
        elif ctype == "fabric:any_mods_loaded":
            mods = cond.get("values") or []
            if len(mods) == 1:
                neoforge.append({"type": "neoforge:mod_loaded", "modid": mods[0]})
            elif mods:
                neoforge.append(
                    {
                        "type": "neoforge:or",
                        "values": [
                            {"type": "neoforge:mod_loaded", "modid": m} for m in mods
                        ],
                    }
                )
        elif ctype == "fabric:tags_populated":
            # NeoForge: not(tag_empty)
            tags = cond.get("values") or []
            registry = cond.get("registry", "minecraft:item")
            for t in tags:
                tag = t if ":" in str(t) else f"c:{t}"
                empty = {"type": "neoforge:tag_empty", "tag": tag}
                # Prefer including registry when provided (NeoForge accepts it)
                if registry:
                    empty["registry"] = registry
                neoforge.append({"type": "neoforge:not", "value": empty})
        else:
            print("WARN unknown fabric condition", ctype, "in recipe; stripping")
    if neoforge:
        if len(neoforge) == 1:
            obj["neoforge:conditions"] = neoforge
        else:
            obj["neoforge:conditions"] = [{"type": "neoforge:and", "values": neoforge}]
    return obj


copied = 0
skipped = 0
converted = 0
with zipfile.ZipFile(JAR) as z:
    for n in z.namelist():
        if not n.startswith("data/techreborn/recipe/") or not n.endswith(".json"):
            continue
        parts = n.split("/")
        kind = parts[3]
        if kind in SKIP_KINDS:
            skipped += 1
            continue
        if kind == "blasting" and not INCLUDE_BLASTING:
            skipped += 1
            continue
        raw = z.read(n)
        data = json.loads(raw)
        if "fabric:load_conditions" in data or "fabric:" in json.dumps(data):
            data = convert_conditions(data)
            converted += 1
            payload = (json.dumps(data, indent=2, ensure_ascii=False) + "\n").encode()
        else:
            payload = raw
        dest = DEST / n
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(payload)
        copied += 1

print(f"copied={copied} skipped={skipped} converted_conditions={converted}")

# Also sync to Windows tree if we wrote to WSL mirror
win = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1")
if ROOT != win and win.is_dir():
    # rsync recipe dirs (except crafting_table/smelting)
    src_recipe = DEST / "data/techreborn/recipe"
    dst_recipe = win / "src/main/resources/data/techreborn/recipe"
    for kind_dir in src_recipe.iterdir():
        if not kind_dir.is_dir():
            continue
        if kind_dir.name in SKIP_KINDS:
            continue
        target = dst_recipe / kind_dir.name
        if target.exists():
            shutil.rmtree(target)
        shutil.copytree(kind_dir, target)
        print("synced", kind_dir.name, "-> windows")
