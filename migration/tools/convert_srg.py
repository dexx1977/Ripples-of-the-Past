#!/usr/bin/env python3
"""Rewrite 1.16.5 SRG member names in the sources to their 1.20.1 equivalents.

Reflection helpers and mixins refer to vanilla members by SRG name (`field_71445_n`,
`func_225602_a_`). Those ids change between versions, so a port has to translate
them. This does it from mapping data only, never by guessing:

  1.16.5: config/joined.tsrg (obf <-> SRG) joined with the official 1.16.5 client
          mappings (obf <-> official name, including method signatures),
  1.20.1: the mapping ForgeGradle generates for this project
          (build/createMcpToSrg/output.tsrg, official name <-> SRG),
  plus migration/class-renames.json (verified official 1.16.5 -> 1.20.1 classes).

Methods are matched by descriptor (with class names translated the same way) so
overloads keep their own SRG name. Anything that cannot be resolved is reported
instead of being rewritten.
"""
import json
import re
import sys
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'
MCP_1165 = ROOT / '.porting/downloads/mcp-1.16.5.zip'
CLIENT_1165 = ROOT / '.porting/downloads/client-1.16.5.txt'
TSRG_1201 = ROOT / 'build/createMcpToSrg/output.tsrg'
RENAMES = ROOT / 'migration/class-renames.json'
CACHE = ROOT / '.porting/srg-conversion.json'

SRG_PATTERN = re.compile(r'\b((?:field|func|f|m)_\d+_[a-zA-Z0-9_]*)\b')


def official_1165():
    """obf class -> official name, and (obf class, obf member) -> official member."""
    classes = {}
    members = {}
    current = None
    for line in CLIENT_1165.read_text().splitlines():
        if not line.startswith(' ') and ' -> ' in line:
            name, obf = line.split(' -> ')[0], line.split(' -> ')[1][:-1]
            current = obf
            classes[obf] = name
            members.setdefault(obf, {})
            continue
        stripped = line.strip()
        if not stripped or current is None or ' -> ' not in stripped:
            continue
        left, obf_member = stripped.rsplit(' -> ', 1)
        obf_member = obf_member.rstrip(':')
        # fields: "<start>:<end>:<type> <name>", methods: "<start>:<end>:<ret> <name>(<args>)"
        body = left.split(':', 2)[-1].strip()
        if '(' in body:
            name = body[:body.index('(')]
            args = body[body.index('(') + 1:body.rindex(')')]
            ret = body[:body.index(' ')] if ' ' in body[:body.index('(')] else 'void'
            members[current][(obf_member, 'method')] = ('method', name, [a for a in args.split(',') if a], ret)
        else:
            name = body.split(' ')[-1]
            members[current][(obf_member, 'field')] = ('field', name, None, None)
    return classes, members


def srg_1165():
    """(srg member) -> (obf class, obf member)."""
    result = {}
    with ZipFile(MCP_1165) as z:
        current = None
        for line in z.read('config/joined.tsrg').decode().splitlines():
            if not line.strip():
                continue
            if not line.startswith('\t'):
                parts = line.split()
                current = parts[0]
                continue
            parts = line.split()
            if len(parts) >= 2 and parts[1].startswith(('field_', 'func_', 'f_', 'm_')):
                result[parts[1]] = (current, parts[0])
    return result


def index_1201():
    """official class -> {'field': {name: srg}, 'method': {name+desc: srg}}."""
    classes = {}
    current = None
    for line in TSRG_1201.read_text().splitlines():
        if not line.strip() or line.startswith('tsrg2'):
            continue
        if not line.startswith('\t'):
            current = line.split()[0].replace('/', '.')
            classes[current] = {'field': {}, 'method': {}}
            continue
        parts = line.split()
        if current is None:
            continue
        if len(parts) == 2:
            classes[current]['field'][parts[0]] = parts[1]
        elif len(parts) == 3:
            classes[current]['method'][parts[0] + parts[1]] = parts[2]
    return classes


def translate_descriptor(desc, renames):
    def repl(match):
        name = match.group(1)
        return 'L' + renames.get(name.replace('/', '.'), name.replace('/', '.')).replace('.', '/') + ';'
    return re.sub(r'L([\w/$]+);', repl, desc)


def build():
    if CACHE.exists():
        return json.loads(CACHE.read_text())
    official, members = official_1165()
    srg = srg_1165()
    index = index_1201()
    renames = json.loads(RENAMES.read_text())

    converted = {}
    unresolved = {}
    for srg_name, (obf_class, obf_member) in srg.items():
        kind_wanted = 'field' if srg_name.startswith('field_') else 'method'
        info = members.get(obf_class, {}).get((obf_member, kind_wanted))
        if info is None:
            unresolved[srg_name] = 'no official 1.16.5 name'
            continue
        official_class = official.get(obf_class)
        if official_class is None:
            unresolved[srg_name] = 'no official 1.16.5 class'
            continue
        new_class = renames.get(official_class, official_class)
        target = index.get(new_class)
        if target is None:
            unresolved[srg_name] = f'class {new_class} missing in 1.20.1'
            continue
        kind = info[0]
        name = info[1]
        if kind == 'field':
            new_srg = target['field'].get(name)
            if new_srg is None:
                unresolved[srg_name] = f'field {new_class}.{name} missing in 1.20.1'
                continue
        else:
            args, ret = info[2], info[3]
            desc = '(' + ''.join(translate_descriptor(a, renames) for a in args) + ')' + translate_descriptor(
                'L' + ret.replace('.', '/') + ';' if ret not in ('void', 'int', 'float', 'double', 'boolean', 'long', 'byte', 'short', 'char') else ret, renames)
            if ret in ('void', 'int', 'float', 'double', 'boolean', 'long', 'byte', 'short', 'char'):
                desc = '(' + ''.join(translate_descriptor(a, renames) for a in args) + ')' + {'void': 'V', 'int': 'I', 'float': 'F', 'double': 'D', 'boolean': 'Z', 'long': 'J', 'byte': 'B', 'short': 'S', 'char': 'C'}[ret]
            new_srg = target['method'].get(name + desc)
            if new_srg is None:
                unresolved[srg_name] = f'method {new_class}.{name}{desc} missing in 1.20.1'
                continue
        if new_srg != srg_name:
            converted[srg_name] = new_srg
    data = {'converted': converted, 'unresolved': unresolved}
    CACHE.parent.mkdir(exist_ok=True)
    CACHE.write_text(json.dumps(data, indent=1))
    return data


def main():
    data = build()
    converted, unresolved = data['converted'], data['unresolved']
    used = set()
    for path in SRC.rglob('*.java'):
        text = path.read_text()
        found = set(SRG_PATTERN.findall(text))
        if not found:
            continue
        used |= found
        new_text = text
        for old in sorted(found, key=len, reverse=True):
            if old in converted:
                new_text = new_text.replace(old, converted[old])
        if new_text != text:
            path.write_text(new_text)

    changed = sorted(used & set(converted))
    missing = sorted(used & set(unresolved))
    print(f'{len(changed)} SRG names rewritten in the sources')
    for old in changed[:20]:
        print(f'   {old} -> {converted[old]}')
    if missing:
        print(f'{len(missing)} SRG names in the sources could not be resolved:')
        for old in missing:
            print(f'   {old}: {unresolved[old]}')
    if '--list' in sys.argv:
        for old, new in sorted(converted.items()):
            print(f'{old}\t{new}')


if __name__ == '__main__':
    main()
