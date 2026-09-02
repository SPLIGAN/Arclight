#!/usr/bin/env python3
"""Download Fabric TechReborn 6.0.2 and list/extract machine recipe JSON."""
from __future__ import annotations

import json
import os
import sys
import urllib.request
import zipfile
from pathlib import Path

OUT = Path("/tmp/tr602")
OUT.mkdir(parents=True, exist_ok=True)


def fetch(url: str, dest: Path) -> None:
    print(f"GET {url}", flush=True)
    req = urllib.request.Request(url, headers={"User-Agent": "tr-port/1.0"})
    with urllib.request.urlopen(req, timeout=120) as resp:
        dest.write_bytes(resp.read())
    print(f"wrote {dest} ({dest.stat().st_size} bytes)", flush=True)


def main() -> int:
    # Modrinth versions for 26.1.2 fabric
    api = (
        "https://api.modrinth.com/v2/project/techreborn/version"
        "?game_versions=%5B%2226.1.2%22%5D&loaders=%5B%22fabric%22%5D"
    )
    versions_path = OUT / "versions.json"
    try:
        fetch(api, versions_path)
        versions = json.loads(versions_path.read_text())
    except Exception as e:
        print("modrinth failed:", e, flush=True)
        versions = []

    jar_url = None
    for v in versions:
        ver = v.get("version_number", "")
        print("version:", ver, v.get("name"), flush=True)
        if ver.startswith("6.0.2") or ver == "6.0.2":
            for f in v.get("files", []):
                if f.get("filename", "").endswith(".jar") and not f.get(
                    "filename", ""
                ).endswith("-sources.jar"):
                    jar_url = f.get("url")
                    break
        if jar_url:
            break

    if not jar_url and versions:
        # fallback: first file of first version matching 6.0
        for v in versions:
            if "6.0.2" in str(v.get("version_number", "")):
                files = v.get("files") or []
                if files:
                    jar_url = files[0].get("url")
                    break

    if not jar_url:
        # GitHub release
        gh = "https://api.github.com/repos/TechReborn/TechReborn/releases/tags/6.0.2"
        try:
            fetch(gh, OUT / "gh.json")
            rel = json.loads((OUT / "gh.json").read_text())
            for a in rel.get("assets", []):
                name = a.get("name", "")
                print("asset:", name, a.get("browser_download_url"), flush=True)
                if name.endswith(".jar") and "sources" not in name.lower():
                    jar_url = a["browser_download_url"]
                    break
        except Exception as e:
            print("github failed:", e, flush=True)

    if not jar_url:
        print("ERROR: no jar url found", flush=True)
        return 1

    jar = OUT / "TechReborn-6.0.2.jar"
    if not jar.exists() or jar.stat().st_size < 1000:
        fetch(jar_url, jar)

    # List recipe dirs inside jar
    with zipfile.ZipFile(jar) as z:
        recipe_entries = [
            n
            for n in z.namelist()
            if "data/techreborn/recipe/" in n and n.endswith(".json")
        ]
        dirs: dict[str, int] = {}
        for n in recipe_entries:
            # data/techreborn/recipe/<type>/...
            parts = n.split("/")
            try:
                idx = parts.index("recipe")
                kind = parts[idx + 1] if idx + 1 < len(parts) else "?"
            except ValueError:
                kind = "?"
            dirs[kind] = dirs.get(kind, 0) + 1
        print("recipe counts by type:", flush=True)
        for k in sorted(dirs):
            print(f"  {k}: {dirs[k]}", flush=True)
        print(f"total recipe json: {len(recipe_entries)}", flush=True)

        # Extract all techreborn recipe json (except crafting_table/smelting if we want only machine)
        extract_root = OUT / "extracted"
        if extract_root.exists():
            import shutil

            shutil.rmtree(extract_root)
        extract_root.mkdir(parents=True)

        machine_kinds = {
            k
            for k in dirs
            if k
            not in {
                "crafting_table",
                "smelting",
                "blasting",
                "smoking",
                "campfire_cooking",
                "stonecutting",
                "smithing",
            }
        }
        extracted = 0
        for n in recipe_entries:
            parts = n.split("/")
            try:
                idx = parts.index("recipe")
                kind = parts[idx + 1]
            except (ValueError, IndexError):
                continue
            if kind not in machine_kinds:
                continue
            dest = extract_root / n
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(z.read(n))
            extracted += 1
        print(f"extracted machine recipes: {extracted} -> {extract_root}", flush=True)

        # Sample one machine recipe for format check
        for n in recipe_entries:
            if "/grinder/" in n or "/compressor/" in n:
                sample = json.loads(z.read(n))
                print("sample", n, json.dumps(sample, indent=2)[:800], flush=True)
                break

    return 0


if __name__ == "__main__":
    sys.exit(main())
