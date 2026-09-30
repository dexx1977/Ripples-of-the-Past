#!/usr/bin/env python3
"""Look up 1.20.1 SRG member names (the names used at runtime and in AT files).

Forge 1.20.1 runs mods against official class names plus SRG member names, e.g.
`net/minecraft/world/entity/Entity` with the field `f_19853_`. Access
transformers, @Shadow members and reflection strings must use those names.

The index is built from the mapping ForgeGradle generates for this project
(build/createMcpToSrg/output.tsrg), which is itself derived from the official
Mojang mappings and MCP config pinned in build.gradle/gradle.properties.

  srg.py field  net.minecraft.world.entity.Entity level
  srg.py method net.minecraft.world.entity.Entity level
  srg.py list   net.minecraft.client.model.geom.ModelPart
  srg.py find   f_19853_
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TSRG = ROOT / 'build/createMcpToSrg/output.tsrg'
CACHE = ROOT / '.porting/srg-index.json'


def build():
    classes = {}
    current = None
    for line in TSRG.read_text().splitlines():
        if not line.strip() or line.startswith('tsrg2'):
            continue
        if not line.startswith('\t'):
            parts = line.split()
            current = parts[0].replace('/', '.')
            classes[current] = {'fields': {}, 'methods': {}, 'raw': []}
            continue
        parts = line.split()
        if current is None or not parts:
            continue
        if len(parts) == 2:  # field: name srg
            classes[current]['fields'][parts[0]] = parts[1]
        elif len(parts) == 3:  # method: name descriptor srg
            classes[current]['methods'][f'{parts[0]}{parts[1]}'] = parts[2]
        classes[current]['raw'].append(line.strip())
    return classes


def load():
    if CACHE.exists():
        return json.loads(CACHE.read_text())
    if not TSRG.exists():
        sys.exit(f'mapping file not found: {TSRG} (run ./gradlew compileJava once)')
    data = build()
    CACHE.parent.mkdir(exist_ok=True)
    CACHE.write_text(json.dumps(data))
    return data


def main():
    data = load()
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    command = sys.argv[1]

    if command == 'field' and len(sys.argv) == 4:
        owner, name = sys.argv[2], sys.argv[3]
        print(data.get(owner, {}).get('fields', {}).get(name, 'NOT FOUND'))
    elif command == 'method' and len(sys.argv) >= 4:
        owner, name = sys.argv[2], sys.argv[3]
        matches = {k: v for k, v in data.get(owner, {}).get('methods', {}).items()
                   if k.split('(')[0] == name}
        for key, value in matches.items():
            print(f'{value}\t{key}')
        if not matches:
            print('NOT FOUND')
    elif command == 'list' and len(sys.argv) == 3:
        for line in data.get(sys.argv[2], {}).get('raw', []):
            print(line)
    elif command == 'find' and len(sys.argv) == 3:
        needle = sys.argv[2]
        for owner, members in data.items():
            for kind in ('fields', 'methods'):
                for key, value in members[kind].items():
                    if value == needle or needle in key:
                        print(f'{owner}\t{key}\t{value}')
    else:
        sys.exit(__doc__)


if __name__ == '__main__':
    main()
