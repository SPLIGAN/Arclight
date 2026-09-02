from pathlib import Path
src = Path("/mnt/c/Users/SPLIGAN/github/Arclight/.tmp-compile-tr.sh")
dst = Path("/tmp/compile-tr.sh")
dst.write_bytes(src.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n"))
dst.chmod(0o755)
print("ok")
