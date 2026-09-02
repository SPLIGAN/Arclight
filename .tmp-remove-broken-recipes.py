#!/usr/bin/env python3
"""Remove recipe/advancement JSON that references items not registered in the NeoForge port."""
from pathlib import Path
import shutil

ROOT = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1/src/main/resources/data/techreborn")
# Exact files known to fail at smoke
REMOVE = [
    "recipe/crafting_table/ingot/industrial_alloy_ingot.json",
    "recipe/crafting_table/parts/uranium_dust.json",
    "recipe/crafting_table/uu_matter/ore/uranium_ore.json",
    "recipe/crafting_table/uu_matter/ore/deepslate_uranium_ore.json",
    "recipe/crafting_table/machine/nuclear_reactor.json",
    "recipe/crafting_table/machine/reactor_chamber.json",
    "recipe/crafting_table/parts/reactor_plating.json",
    "advancement/recipes/crafting_table/ingot/industrial_alloy_ingot.json",
    "advancement/recipes/crafting_table/machine/reactor_chamber.json",
    "advancement/recipes/crafting_table/machine/nuclear_reactor.json",
    "advancement/recipes/crafting_table/parts/reactor_plating.json",
    "advancement/recipes/crafting_table/parts/uranium_dust.json",
    "advancement/recipes/crafting_table/uu_matter/ore/uranium_ore.json",
    "advancement/recipes/crafting_table/uu_matter/ore/deepslate_uranium_ore.json",
]
removed = 0
for rel in REMOVE:
    p = ROOT / rel
    if p.exists():
        p.unlink()
        removed += 1
        print("removed", rel)
print(f"removed={removed}")

# Also scan for any remaining references to missing item ids in recipe json and delete those files
MISSING = (
    "techreborn:industrial_alloy_plate",
    "techreborn:industrial_alloy_ingot",
    "techreborn:uranium_ore",
    "techreborn:deepslate_uranium_ore",
    "techreborn:uranium_dust",
    "techreborn:uranium_238_dust",
    "techreborn:uranium_235_dust",
    "techreborn:uranium_235_small_dust",
    "techreborn:raw_uranium_storage_block",
)
extra = 0
for p in ROOT.rglob("*.json"):
    text = p.read_text(encoding="utf-8")
    if any(m in text for m in MISSING):
        # keep fuel rods etc that only mention uranium_fuel which exists
        if "uranium_fuel" in text and "uranium_ore" not in text and "uranium_dust" not in text and "uranium_238" not in text and "uranium_235" not in text and "industrial_alloy" not in text:
            continue
        if any(m in text for m in MISSING):
            print("also", p.relative_to(ROOT))
            p.unlink()
            extra += 1
print(f"extra={extra}")
