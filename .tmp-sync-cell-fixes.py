#!/usr/bin/env python3
"""Sync Windows TR NeoForge sources into WSL build tree."""
from pathlib import Path
import shutil

WIN = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1")
WSL = Path("/home/spligan/tr-neoforge-build")

RELS = [
    "RebornCore/src/main/java/reborncore/common/crafting/SizedIngredient.java",
    "RebornCore/src/main/java/reborncore/common/fluid/container/FluidInstance.java",
    "src/main/java/techreborn/items/CellItem.java",
    "src/main/java/techreborn/items/DynamicCellItem.java",
    "src/main/java/techreborn/init/TRContent.java",
    "src/main/java/techreborn/init/TRDispenserBehavior.java",
    "src/main/java/techreborn/init/TRCauldronBehavior.java",
    "src/main/java/techreborn/events/ModRegistry.java",
    "src/main/java/techreborn/blocks/storage/fluid/TankUnitBlock.java",
    "src/client/java/techreborn/client/events/StackToolTipHandler.java",
]

for rel in RELS:
    src = WIN / rel
    dst = WSL / rel
    if not src.exists():
        print("MISSING", rel)
        continue
    dst.parent.mkdir(parents=True, exist_ok=True)
    dst.write_bytes(src.read_bytes())
    print("synced", rel)

# cell item models
src_items = WIN / "src/main/resources/assets/techreborn/items"
dst_items = WSL / "src/main/resources/assets/techreborn/items"
dst_items.mkdir(parents=True, exist_ok=True)
n = 0
for p in src_items.glob("*cell*.json"):
    (dst_items / p.name).write_bytes(p.read_bytes())
    n += 1
print("synced cell models", n)
