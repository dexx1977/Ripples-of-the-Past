"""Compares every @Inject handler signature in src/main with the target method.

Mixin validates these at runtime only, so a handler that does not match its target
silently disables the injection (and hides the ones after it in the same class).
The target methods are read from the mapped Forge jar and the SRG file; members
that carry different names in the two namespaces are reported as "not found" and
have to be checked against a runtime log.

Usage: python3 migration/tools/check_mixin_handlers.py
"""
import re, pathlib, subprocess

JAR = subprocess.check_output(["bash", "-c", "ls build/fg_cache/net/minecraftforge/forge/*_mapped_official_*/*.jar | head -1"]).decode().strip()
CACHE = {}

def javap(cls):
    if cls in CACHE: return CACHE[cls]
    path = cls.replace(".", "/") + ".class"
    r = subprocess.run(["unzip", "-p", JAR, path], capture_output=True)
    if r.returncode != 0:
        CACHE[cls] = None; return None
    tmp = pathlib.Path("/tmp/javap_tmp2.class"); tmp.write_bytes(r.stdout)
    txt = subprocess.run(["javap", "-p", str(tmp)], capture_output=True).stdout.decode()
    CACHE[cls] = txt
    return txt

def methods_of(cls):
    txt = javap(cls)
    if not txt: return {}
    out = {}
    for line in txt.splitlines():
        line = line.strip()
        m = re.match(r"(?:public|protected|private|abstract|final|static|\s)+[\w.<>\[\], ?]+ (\w+)\((.*?)\);", line)
        if m:
            out.setdefault(m.group(1), []).append([p.strip().split(" ")[-1] for p in m.group(2).split(",") if p.strip()])
    return out

def find_super(cls):
    txt = javap(cls)
    if not txt: return None
    m = re.search(r"class \S+ extends ([\w.$]+)", txt)
    return m.group(1) if m else None

def lookup(cls, name, depth=0):
    """find the method in the class or its superclasses"""
    c = cls
    while c and depth < 12:
        ms = methods_of(c)
        if name in ms: return c, ms[name]
        c = find_super(c); depth += 1
    return None, None

def simple(t):
    t = t.strip().replace("...", "[]")
    return t.split(".")[-1]

def source_type(t):
    """strip the parameter name from a source parameter declaration"""
    t = t.strip().replace("...", "[]")
    if " " in t and not t.endswith(">"):
        t = t.rsplit(" ", 1)[0]
    else:
        # e.g. 'List<ItemStack> items' -> drop the trailing name
        m = re.match(r"(.+?>)\s+\w+$", t)
        if m: t = m.group(1)
    return simple(t)

def typevar(x):
    return len(x) == 1 and x.isupper()

problems, checked = [], 0
for p in sorted(pathlib.Path("src/main/java/com/github/standobyte/jojo/mixin").rglob("*.java")):
    src = p.read_text(encoding="utf-8")
    imports = {f.rsplit(".", 1)[1]: f for f in re.findall(r'^import\s+([\w.]+);', src, re.M)}
    for m in re.finditer(r'@Mixin\(\s*(?:value\s*=\s*)?([\w.$]+)\.class', src):
        target = imports.get(m.group(1), m.group(1) if "." in m.group(1) else None)
        if target is None: continue
        for inj in re.finditer(r'@Inject\(method\s*=\s*"([^"()]+)(?:\([^"]*\))?".*?\)\s*\n(?:\s*@\w+(?:\([^)]*\))?\s*\n)*\s*(?:public|protected|private)\s+(?:static\s+)?[\w.<>\[\], ?]+\s+(\w+)\(([^)]*)\)', src, re.S):
            name, handler, params_raw = inj.group(1), inj.group(2), inj.group(3)
            params = [source_type(x) for x in params_raw.split(",") if x.strip()]
            ci = next((i for i, x in enumerate(params) if "CallbackInfo" in x), None)
            if ci is None:
                problems.append((p, handler, name, params, "no CallbackInfo parameter")); continue
            prefix = params[:ci]                       # the target's parameters
            owner, tmethods = lookup(target, name)
            if tmethods is None:
                problems.append((p, handler, name, prefix, "target method not found")); continue
            checked += 1
            def matches(tm):
                tm = [simple(x) for x in tm]
                return len(tm) == len(prefix) and all(a == b or typevar(b) for a, b in zip(prefix, tm))
            if not any(matches(tm) for tm in tmethods):
                problems.append((p, handler, name, prefix, [[simple(x) for x in tm] for tm in tmethods]))

print("checked @Inject handlers:", checked)
print("issues (%d):" % len(problems))
for f, h, n, got, want in problems:
    print("\n %s :: %s -> %s" % (str(f).split("/jojo/")[-1], h, n))
    print("   handler (target part):", got)
    print("   target signature     :", want)
