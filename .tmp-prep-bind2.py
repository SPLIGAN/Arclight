from pathlib import Path
p = Path("/mnt/c/Users/SPLIGAN/github/Arclight/.tmp-find-bind2.sh")
Path("/tmp/find-bind2.sh").write_bytes(p.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n"))
print("ok")
