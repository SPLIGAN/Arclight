from pathlib import Path

src_script = Path(r"/mnt/c/Users/SPLIGAN/github/Arclight/.tmp-fix-and-build.sh")
# also restore and build inline to avoid nested shell issues
src = Path("/mnt/c/Users/SPLIGAN/github/Arclight")
dst = Path.home() / "arclight-build"

gradlew = (src / "gradlew").read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n")
assert b"dirname" in gradlew and b"warn" in gradlew
(dst / "gradlew").write_bytes(gradlew)
(dst / "gradlew").chmod(0o755)
print("gradlew restored", len(gradlew))
