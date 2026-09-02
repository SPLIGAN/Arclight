from pathlib import Path
p = Path("/mnt/c/Users/SPLIGAN/github/Arclight/.tmp-gde.sh")
Path("/tmp/gde.sh").write_bytes(p.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n"))
print("ok")
