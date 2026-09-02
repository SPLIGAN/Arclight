#!/usr/bin/env python3
"""Sync critical TR sources Windows->WSL and ensure machine recipes on both trees."""
from __future__ import annotations

import os
import shutil
import zipfile
from pathlib import Path

WIN = Path("/mnt/c/Users/SPLIGAN/github/TechRebornForNeoForge1.21.1")
WSL = Path("/home/spligan/tr-neoforge-build")
JAR = Path("/tmp/tr602/TechReborn-6.0.2.jar")

# Paths that must match Windows (item registration work)
SYNC_REL = [
    "src/main/java/techreborn/init/TRContent.java",
    "src/main/java/techreborn/world/OreDistribution.java",
    "src/main/java/techreborn/config/TechRebornConfig.java",
    "src/main/resources/data/minecraft/tags/block/needs_iron_tool.json",
    "src/main/resources/data/c/tags/block/ores/uranium.json",
]


def copy_file(src: Path, dst: Path) -> None:
    dst.parent.mkdir(parents=True, exist_ok=True)
    data = src.read_bytes()
    # write via temp then replace to avoid partials
    tmp = dst.with_suffix(dst.suffix + ".tmpwrite")
    tmp.write_bytes(data)
    tmp.replace(dst)
    print(f"synced {src.relative_to(WIN)} -> WSL")


def main() -> None:
    for rel in SYNC_REL:
        src = WIN / rel
        dst = WSL / rel
        if not src.exists():
            print("MISSING on Windows:", rel)
            continue
        copy_file(src, dst)

    # Also sync restored crafting recipes that reference uranium/industrial_alloy
    craft_patterns = [
        "src/main/resources/data/techreborn/recipe/crafting_table",
    ]
    # Copy any crafting json that mentions uranium or industrial_alloy from WIN to WSL
    win_craft = WIN / "src/main/resources/data/techreborn/recipe/crafting_table"
    wsl_craft = WSL / "src/main/resources/data/techreborn/recipe/crafting_table"
    if win_craft.is_dir():
        count = 0
        for p in win_craft.rglob("*.json"):
            text = p.read_text(encoding="utf-8", errors="ignore")
            if "uranium" in text or "industrial_alloy" in text:
                rel = p.relative_to(win_craft)
                dest = wsl_craft / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(p.read_bytes())
                count += 1
        print(f"synced {count} crafting recipes mentioning uranium/industrial_alloy")

    # Confirm machine recipe dirs on WSL
    recipe = WSL / "src/main/resources/data/techreborn/recipe"
    kinds = sorted(d.name for d in recipe.iterdir() if d.is_dir())
    print("WSL recipe kinds:", kinds)
    total = sum(1 for _ in recipe.rglob("*.json"))
    print("WSL recipe json count:", total)

    # Spot-check TRContent uranium on WSL
    trc = (WSL / "src/main/java/techreborn/init/TRContent.java").read_text(encoding="utf-8")
    for needle in ["URANIUM(OreDistribution", "DEEPSLATE_URANIUM", "RAW_URANIUM", "INDUSTRIAL_ALLOY"]:
        print(needle, "->", needle in trc)


if __name__ == "__main__":
    main()
