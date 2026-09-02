#!/usr/bin/env python3
from pathlib import Path
import re
from collections import Counter

log = Path(sorted(Path("/tmp").glob("arclight-smoke-*.log"), key=lambda p: p.stat().st_mtime)[-1])
text = log.read_text(encoding="utf-8", errors="replace")
print("log", log.name)

# Unknown item keys
items = Counter()
for m in re.finditer(r"Unknown registry key in ResourceKey\[minecraft:root / minecraft:item\]: ([^\s;]+)", text):
    items[m.group(1)] += 1
print("unique unknown items", len(items))
for k, v in items.most_common(40):
    print(f"  {v:4d} {k}")

# Error categories
parse_errors = [ln for ln in text.splitlines() if "Couldn't parse data file" in ln]
print("parse errors", len(parse_errors))
cats = Counter()
for ln in parse_errors:
    if "Unknown registry key" in ln and "/ minecraft:item" in ln:
        cats["unknown_item"] += 1
    elif "No key type in MapLike" in ln:
        cats["ingredient_format"] += 1
    elif "Not a string: {\"fluid\"" in ln or "fluid" in ln and "Failed to parse either" in ln:
        cats["fluid_format"] += 1
    else:
        cats["other"] += 1
print("categories", cats)

for needle in ["uranium", "industrial_alloy", "Done (", "TechReborn setup"]:
    print(needle, text.lower().count(needle.lower()) if needle.islower() or "_" in needle else len(re.findall(re.escape(needle), text)))

# Check if uranium unknown
print("uranium unknown?", any("uranium" in k for k in items))
print("industrial_alloy unknown?", any("industrial_alloy" in k for k in items))

# Sample one ingredient_format and one fluid
for ln in parse_errors:
    if "No key type" in ln:
        print("INGREDIENT SAMPLE:", ln[:400])
        break
for ln in parse_errors:
    if "industrial_grinder" in ln and "fluid" in ln:
        print("FLUID SAMPLE:", ln[:400])
        break
