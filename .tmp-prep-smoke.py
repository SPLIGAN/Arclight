from pathlib import Path
src = Path("/mnt/c/Users/SPLIGAN/github/Arclight/.tmp-smoke-tr.sh")
dst = Path("/tmp/smoke-tr.sh")
dst.write_bytes(src.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n"))
dst.chmod(0o755)
print("ok")
