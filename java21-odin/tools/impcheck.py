"""Static check: which Win32 imports of a PE tree are missing from Odin's exports.

usage: impcheck.py <dir with PEs> [--odin <odin src dir>]
Forwarders in api-ms-win-*.dll are resolved to their real targets first.
"""
import os
import re
import sys

import pefile

ODIN = r"C:/Temporal/1.- OS2/Projects/Other/odin32/src"
root = sys.argv[1]

# --- Odin exports from the .def files -----------------------------------
exports = {}  # dllname(lower) -> set(names)
for d in os.listdir(ODIN):
    if not os.path.isdir(os.path.join(ODIN, d)):
        continue
    for f in os.listdir(os.path.join(ODIN, d)):
        if not f.lower().endswith(".def"):
            continue
        if f.lower().endswith("dbg.def") or f.lower().endswith("_dbg.def"):
            continue
        p = os.path.join(ODIN, d, f)
        try:
            txt = open(p, encoding="latin-1").read()
        except OSError:
            continue
        m = re.search(r"^\s*LIBRARY\s+(\S+)", txt, re.M | re.I)
        if not m:
            continue
        lib = m.group(1).lower()
        sect = re.split(r"^\s*EXPORTS\s*$", txt, flags=re.M | re.I)
        if len(sect) < 2:
            continue
        names = exports.setdefault(lib, set())
        for line in sect[1].splitlines():
            line = line.split(";", 1)[0].strip()
            if not line:
                continue
            mm = re.match(r"^([A-Za-z_@?$][\w@?$.]*)", line)
            if mm:
                names.add(mm.group(1))
            mm = re.match(r"^([\w@?$.]+)\s*=\s*\S+", line)
            if mm:
                names.add(mm.group(1))

# Odin maps some DLL names onto others (ws2_32 -> ws2_32, ntdll lives in kernel32 ...)
alias = {"ntdll": ["ntdll", "kernel32"]}

# --- scan the PE tree ---------------------------------------------------
pes = {}
for dp, dn, fn in os.walk(root):
    for f in fn:
        if f.lower().endswith((".dll", ".exe")):
            pes[f.lower()] = os.path.join(dp, f)


def forwards(path):
    pe = pefile.PE(path, fast_load=True)
    pe.parse_data_directories([pefile.DIRECTORY_ENTRY["IMAGE_DIRECTORY_ENTRY_EXPORT"]])
    out = {}
    if hasattr(pe, "DIRECTORY_ENTRY_EXPORT"):
        for s in pe.DIRECTORY_ENTRY_EXPORT.symbols:
            if s.name and s.forwarder:
                out[s.name.decode()] = s.forwarder.decode()
    return out


fwd_cache = {}


def resolve(dll, name):
    """follow api-ms-win-* forwarders; returns (dll, name)"""
    for _ in range(4):
        low = dll.lower()
        if low.endswith(".dll"):
            low = low[:-4]
        if low.startswith("api-ms-win") or low.startswith("ext-ms-win"):
            key = low + ".dll"
            if key in pes:
                if key not in fwd_cache:
                    fwd_cache[key] = forwards(pes[key])
                t = fwd_cache[key].get(name)
                if t:
                    dll, name = t.split(".", 1)
                    continue
            return (low, name)
        return (low, name)
    return (dll.lower(), name)


missing = {}  # (dll,name) -> set of users
total = 0
skip = {"api-ms-win-crt-private-l1-1-0"}
for key, path in sorted(pes.items()):
    if key.startswith("api-ms-win") or key.startswith("ext-ms"):
        continue
    try:
        pe = pefile.PE(path, fast_load=True)
        pe.parse_data_directories([pefile.DIRECTORY_ENTRY["IMAGE_DIRECTORY_ENTRY_IMPORT"]])
    except Exception as e:
        print("cannot parse", key, e)
        continue
    for ent in getattr(pe, "DIRECTORY_ENTRY_IMPORT", []):
        dll = ent.dll.decode()
        for imp in ent.imports:
            if not imp.name:
                continue
            name = imp.name.decode()
            rd, rn = resolve(dll, name)
            total += 1
            ok = False
            cand = alias.get(rd, [rd])
            if rd in pes or (rd + ".dll") in pes:
                continue  # provided by a DLL shipped in the JDK itself
            for c in cand:
                if rn in exports.get(c, ()):
                    ok = True
            if not ok:
                missing.setdefault((rd, rn), set()).add(key)

print("imports checked:", total, "missing:", len(missing))
by = {}
for (d, n), users in missing.items():
    by.setdefault(d, []).append((n, users))
for d in sorted(by):
    print("\n== %s: %d missing (odin knows: %s)" % (d, len(by[d]), "yes" if d in exports else "NO SUCH DLL"))
    for n, users in sorted(by[d]):
        print("  %-40s %s" % (n, ",".join(sorted(users))[:60]))
