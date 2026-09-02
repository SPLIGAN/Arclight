#!/usr/bin/env python3
"""Import machine recipes into Windows TR tree using pure byte writes (avoid WSL metadata)."""
from __future__ import annotations

import json
import zipfile
from pathlib import Path

CANDIDATE_JARS = [
    Path("/mnt/c/Users/SPLIGAN/github/Arclight/.tmp/TechReborn-6.0.2.jar"),
    Path("/tmp/tr602/TechReborn-6.0.2.jar"),
    Path(r"C:\Users\SPLIGAN\github\Arclight\.tmp\TechReborn-6.0.2.jar"),
]
DEST_CANDIDATES = [
    Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/src/main/resources"),
    Path(r"C:\Users\SPLIGAN\github\TechRebornForNeoForge1.21.1\src\main\resources"),
]
DEST = next((p for p in DEST_CANDIDATES if p.parent.is_dir()), DEST_CANDIDATES[0])
SKIP = {"crafting_table", "smelting"}


def convert_conditions(obj: dict) -> dict:
    fabric = obj.pop("fabric:load_conditions", None)
    if not fabric:
        return obj
    neoforge = []
    for cond in fabric:
        ctype = cond.get("condition")
        if ctype == "fabric:all_mods_loaded":
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
        elif ctype == "fabric:tags_populated":
            tags = cond.get("values") or []
            registry = cond.get("registry", "minecraft:item")
            for t in tags:
                tag = t if ":" in str(t) else f"c:{t}"
                empty = {"type": "neoforge:tag_empty", "tag": tag, "registry": registry}
                neoforge.append({"type": "neoforge:not", "value": empty})
        else:
            print("unknown", ctype)
    if neoforge:
        obj["neoforge:conditions"] = (
            neoforge if len(neoforge) == 1 else [{"type": "neoforge:and", "values": neoforge}]
        )
    return obj


def find_jar() -> Path:
    for p in CANDIDATE_JARS:
        if p.exists():
            return p
    # copy from WSL via reading in this process if UNC works
    raise SystemExit("jar not found; copy to Arclight/.tmp first")


def main() -> None:
    jar = find_jar()
    print("jar", jar)
    copied = 0
    with zipfile.ZipFile(jar) as z:
        for n in z.namelist():
            if not n.startswith("data/techreborn/recipe/") or not n.endswith(".json"):
                continue
            kind = n.split("/")[3]
            if kind in SKIP:
                continue
            data = json.loads(z.read(n))
            if "fabric:load_conditions" in data:
                data = convert_conditions(data)
                payload = (json.dumps(data, indent=2, ensure_ascii=False) + "\n").encode()
            else:
                payload = z.read(n)
            dest = DEST / n
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(payload)
            copied += 1
    print("copied", copied)
    kinds = sorted(p.name for p in (DEST / "data/techreborn/recipe").iterdir() if p.is_dir())
    print("kinds", kinds)


if __name__ == "__main__":
    main()
