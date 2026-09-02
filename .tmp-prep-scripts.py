from pathlib import Path
import subprocess
import sys

for name in [".tmp-kill-gradle.sh", ".tmp-build-collect.sh"]:
    src = Path("/mnt/c/Users/SPLIGAN/github/Arclight") / name
    dst = Path("/tmp") / name.removeprefix(".tmp-")
    data = src.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n")
    dst.write_bytes(data)
    dst.chmod(0o755)
    print("wrote", dst)
