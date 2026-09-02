#!/usr/bin/env python3
"""Rewrite Fabric fluid_container ingredients to NeoForge neoforge:ingredient_type form."""
from pathlib import Path
import json

ROOT = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/src/main/resources/data/techreborn/recipe")
files = list(ROOT.rglob("*.json"))
changed = 0

def rewrite_ingredient(obj):
    if not isinstance(obj, dict):
        return obj, False
    if obj.get("fabric:type") in ("reborncore:fluid_container", "reborncore:fluid_container"):
        new = {"neoforge:ingredient_type": "reborncore:fluid_container", "fluid": obj["fluid"]}
        if "amount" in obj:
            new["amount"] = obj["amount"]
        return new, True
    out = {}
    did = False
    for k, v in obj.items():
        nv, d = walk(v)
        out[k] = nv
        did = did or d
    return out, did

def walk(v):
    if isinstance(v, dict):
        return rewrite_ingredient(v)
    if isinstance(v, list):
        out = []
        did = False
        for i in v:
            ni, d = walk(i)
            out.append(ni)
            did = did or d
        return out, did
    return v, False

for path in files:
    text = path.read_text(encoding="utf-8")
    data = json.loads(text)
    new_data, did = walk(data)
    if did:
        path.write_text(json.dumps(new_data, indent=4) + "\n", encoding="utf-8")
        changed += 1
        print(path.relative_to(ROOT))

print(f"changed={changed}")
