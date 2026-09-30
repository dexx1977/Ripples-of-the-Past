#!/usr/bin/env python3
"""Apply audited class-name relocations, without changing APIs or string literals.

The mapping joins MCP 1.16.5's obfuscated->SRG class table with Mojang's 1.16.5
names and only retains names present in Mojang's 1.20.1 client mapping. This is
a namespace preparation step, NOT an API/behavior migration. Removed types,
member changes, mixin descriptors, reflection strings and access transformers
must be ported separately after reviewing their purpose.
"""
import argparse
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
IMPORT = re.compile(r'^import ([\w.]+);', re.M)
# Comments and literals must not have gameplay data, NBT keys or text rewritten.
TOKEN = re.compile(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|\b[A-Za-z_$][\w$]*\b')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    names = json.loads((ROOT / 'migration/class-renames.json').read_text())
    edits = []
    for path in sorted((ROOT / 'src/main/java').rglob('*.java')):
        source = path.read_text()
        imports = IMPORT.findall(source)
        renames = {old.rsplit('.', 1)[-1]: names[old].rsplit('.', 1)[-1]
                   for old in imports if old in names}
        if not renames:
            continue
        # Never silently introduce an ambiguous simple name.
        targets = {}
        for old in imports:
            full = names.get(old, old)
            simple = full.rsplit('.', 1)[-1]
            if simple in targets and full != targets[simple]:
                raise ValueError(f'{path}: conflicting imports {targets[simple]} / {full}')
            targets[simple] = full
        # Keep import declarations out of identifier processing: package segments
        # may happen to match a class simple name.
        body = IMPORT.sub(lambda m: f'/*PORT_IMPORT_{imports.index(m[1])}*/', source)
        body = TOKEN.sub(lambda m: renames.get(m[0], m[0]), body)
        for index, old in enumerate(imports):
            body = body.replace(f'/*PORT_IMPORT_{index}*/', f'import {names.get(old, old)};')
        if body != source:
            edits.append((path, body))
    print(f'{len(edits)} source files have verified class-name changes')
    if args.apply:
        for path, body in edits:
            path.write_text(body)


if __name__ == '__main__':
    main()
