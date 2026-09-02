from pathlib import Path

src = Path("/mnt/c/Users/SPLIGAN/github/Arclight/.tmp-build-collect.sh")
dst = Path("/tmp/build-collect.sh")
data = src.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n")
dst.write_bytes(data)
dst.chmod(0o755)
print("wrote", dst, "bytes", len(data))
