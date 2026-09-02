#!/usr/bin/env python3
import json
import zipfile
from collections import Counter

z = zipfile.ZipFile("/tmp/tr602/TechReborn-6.0.2.jar")
keys = Counter()
fabricish = []
for n in z.namelist():
    if not (n.startswith("data/techreborn/recipe/") and n.endswith(".json")):
        continue
    parts = n.split("/")
    kind = parts[3] if len(parts) > 3 else "?"
    if kind in ("crafting_table", "smelting", "blasting"):
        continue
    data = json.loads(z.read(n))
    s = json.dumps(data)
    if "fabric:" in s or '"type": "reborncore:fluid' in s:
        fabricish.append(n)
    for k in data:
        keys[k] += 1

print("top-level keys:", keys.most_common(20))
print("fabricish count", len(fabricish))
print("samples", fabricish[:20])

for n in z.namelist():
    if "/industrial_grinder/" in n and n.endswith(".json"):
        print("industrial_grinder sample", n)
        print(z.read(n).decode()[:600])
        break

for n in z.namelist():
    if not n.endswith(".json") or "/chemical_reactor/" not in n:
        continue
    raw = z.read(n).decode()
    if "fluid" in raw.lower():
        print("chem with fluid", n)
        print(raw[:500])
        break

# blasting count - should we copy?
blast = [n for n in z.namelist() if "/recipe/blasting/" in n]
print("blasting recipes", len(blast))
