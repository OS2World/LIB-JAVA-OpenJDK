"""Compare the OS/2 OpenJDK 6 port tree against pristine jdk6-b27 and classify files."""
import hashlib
import os
import sys
import zipfile

BASE = r"C:/Temporal/1.- OS2/Projects/Other/openjdk17-odin"
PORT = r"C:/Temporal/1.- OS2/Projects/LIBs/LIB-JAVA-OpenJDK/openjdk"
ZIP = BASE + "/jdk6-upstream/jdk6-b27.zip"
OUT = BASE + "/os2-delta"


def norm(data):
    return data.replace(b"\r\n", b"\n")


def h(data):
    return hashlib.sha1(norm(data)).hexdigest()


up = {}
with zipfile.ZipFile(ZIP) as z:
    for info in z.infolist():
        if info.is_dir():
            continue
        rel = info.filename.split("/", 1)[1]
        up[rel] = h(z.read(info))

port = {}
for root, dirs, files in os.walk(PORT):
    dirs[:] = [d for d in dirs if d not in (".git", ".hg", ".svn")]
    for f in files:
        p = os.path.join(root, f)
        rel = os.path.relpath(p, PORT).replace(os.sep, "/")
        with open(p, "rb") as fh:
            port[rel] = h(fh.read())

added = sorted(k for k in port if k not in up)
removed = sorted(k for k in up if k not in port)
changed = sorted(k for k in port if k in up and port[k] != up[k])
same = len(port) - len(added) - len(changed)

os.makedirs(OUT, exist_ok=True)
for name, lst in (("added", added), ("removed", removed), ("changed", changed)):
    with open(OUT + "/" + name + ".txt", "w") as fh:
        fh.write("\n".join(lst) + "\n")

print("upstream files:", len(up), "port files:", len(port))
print("identical:", same, "changed:", len(changed), "added:", len(added), "removed-in-port:", len(removed))


def top(lst, depth):
    c = {}
    for k in lst:
        key = "/".join(k.split("/")[:depth])
        c[key] = c.get(key, 0) + 1
    return sorted(c.items(), key=lambda x: -x[1])


for name, lst in (("CHANGED", changed), ("ADDED", added)):
    print("---", name, "by directory (depth 3)")
    for k, v in top(lst, 3)[:25]:
        print("%5d %s" % (v, k))
sys.stdout.flush()
