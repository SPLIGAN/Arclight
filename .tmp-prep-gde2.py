from pathlib import Path
p = Path("/mnt/c/Users/SPLIGAN/github/Arclight/.tmp-gde2.sh")
Path("/tmp/gde2.sh").write_bytes(p.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n"))
print("ok")
