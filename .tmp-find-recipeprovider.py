#!/usr/bin/env python3
import zipfile
from pathlib import Path

# Find RecipeProvider source
candidates = list(Path("/home/spligan/.gradle/caches").rglob("*sources*.jar"))
for jar in candidates:
    try:
        z = zipfile.ZipFile(jar)
    except Exception:
        continue
    names = z.namelist()
    if "net/minecraft/data/recipes/RecipeProvider.java" in names:
        print("FOUND", jar)
        text = z.read("net/minecraft/data/recipes/RecipeProvider.java").decode()
        print(text[:3500])
        break
else:
    print("not found in sources jars, try class via cfr or find neoform")
    for p in Path("/home/spligan/.gradle/caches").rglob("RecipeProvider.java"):
        print(p)
        print(p.read_text()[:3000])
        break
