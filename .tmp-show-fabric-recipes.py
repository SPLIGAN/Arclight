#!/usr/bin/env python3
import json
import zipfile

z = zipfile.ZipFile("/tmp/tr602/TechReborn-6.0.2.jar")
for n in z.namelist():
    if not (n.startswith("data/techreborn/recipe/") and n.endswith(".json")):
        continue
    raw = z.read(n).decode()
    if "fabric:load_conditions" in raw or "fabric:" in raw:
        print("====", n)
        print(raw)
        print()
