from pathlib import Path
src = Path("/mnt/c/Users/SPLIGAN/github/Arclight/.tmp-build-and-smoke.sh")
dst = Path("/tmp/build-and-smoke.sh")
dst.write_bytes(src.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n"))
dst.chmod(0o755)
print("ok")
