#!/usr/bin/env python3
"""Apply the audited relocations from migration/type-renames.json.

Rules of the road for this port:
  * Only relocations whose 1.20.1 target was confirmed against the mapped Forge
    artifact or the FML jars (see migration/tools/api.sh, migration/tools/srg.py)
    are applied here. Nothing is guessed.
  * Anything listed under "needsManualReview" is never touched: its behaviour
    changed and must be rewritten by hand with the original purpose in mind.
  * String literals and comments are masked out before matching, so NBT keys,
    packet ids, translation keys and resource paths are preserved byte for byte.
"""
import json
import re
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'
TABLE = json.loads((ROOT / 'migration/type-renames.json').read_text())

IMPORT = re.compile(r'^import (static )?([\w.]+(?:\.\*)?);', re.M)
TOKEN = re.compile(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|\b[A-Za-z_$][\w$]*\b')
MASKABLE = re.compile(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'')
SIMPLE = re.compile(r'[^.]')

stats = Counter()


def simple(fqn):
    return fqn.split('.')[-1] if '$' not in fqn else fqn.split('.')[-1].replace('$', '.')


def mask(source):
    """Blank out comments and literals, keeping offsets so spans stay valid."""
    return MASKABLE.sub(lambda m: re.sub(r'[^\n]', ' ', m.group(0)), source)


def apply_spans(source, mask_text, rules):
    """Replace every match of rules (from -> to) found in masked code."""
    edits = []
    for rule in rules:
        pattern = re.escape(rule['from'])
        for match in re.finditer(pattern, mask_text):
            start, end = match.span()
            # Require token boundaries so `BlockPos.Mutable` never matches
            # inside a longer identifier.
            before = mask_text[start - 1] if start else ''
            after = mask_text[end] if end < len(mask_text) else ''
            # Reject matches that are only part of a longer identifier. A dot is a
            # legal neighbour in both directions: `entity.isOnGround()` and
            # `Explosion.Mode.DESTROY` must be rewritten, while
            # `BlockPos.MutableBlockPos` must not.
            if before and (before.isalnum() or before in '_$'):
                continue
            if not rule.get('partial') and after and (after.isalnum() or after in '_$'):
                continue
            edits.append((start, end, rule['to'], rule.get('addImport')))
    edits.sort(key=lambda e: e[0], reverse=True)
    last = None
    applied = []
    for start, end, replacement, add_import in edits:
        if last is not None and end > last:
            continue
        source = source[:start] + replacement + source[end:]
        last = start
        applied.append((replacement, add_import))
    for replacement, add_import in applied:
        stats[f'member {replacement}'] += 1
    return source, {imp for _, imp in applied if imp}


def main():
    apply = '--apply' in sys.argv
    type_map = {k: v for k, v in TABLE['types'].items() if not k.startswith('_')}
    text_map = {k: v for k, v in TABLE['textComponents'].items() if not k.startswith('_')}
    factories = dict(TABLE['factories'])
    simple_renames = {k: v for k, v in TABLE['simpleRenames'].items() if not k.startswith('_')}
    member_rules = [r for r in TABLE['memberRenames'] if 'from' in r]

    simple_type = {simple(k): v for k, v in type_map.items()}
    simple_text = {simple(k): v for k, v in text_map.items()}
    renames = {}
    renames.update({simple(k): simple(v) for k, v in type_map.items()})
    renames.update({simple(k): simple(v) for k, v in text_map.items()})
    renames.update(simple_renames)

    changed = []

    for path in sorted(SRC.rglob('*.java')):
        source = path.read_text()
        original = source
        imports = IMPORT.findall(source)
        plain = [imp for static, imp in imports if not static]
        wildcards = {imp[:-2] for imp in plain if imp.endswith('.*')}

        # 1. member-level rewrites, matched on masked code for precision.
        source, extra_imports = apply_spans(source, mask(source), member_rules)

        used_simple = set(TOKEN.findall(IMPORT.sub('', source)))
        touched = {s for s in list(simple_type) + list(simple_text) + list(simple_renames) if s in used_simple}
        old_fqns = [fqn for fqn in list(type_map) + list(text_map) if simple(fqn) in touched]

        markers = []

        def stash(match):
            markers.append(match.group(0))
            return f'/*PORT_IMPORT_{len(markers) - 1}*/'

        body = IMPORT.sub(stash, source)

        holder = 'net.minecraft.network.chat.Component'
        if any(simple(i) == 'Component' and i != holder for i in plain):
            pass
        else:
            holder = 'Component'

        for old_simple, new_fqn in simple_text.items():
            factory = factories.get(old_simple)
            if not factory or old_simple not in touched:
                continue
            body, n = re.subn(r'\bnew\s+' + re.escape(old_simple) + r'\s*\(',
                              f'{holder}.{factory}(', body)
            stats[f'new {old_simple}( -> {simple(new_fqn)}.{factory}('] += n
            if old_simple == 'StringTextComponent':
                body, n = re.subn(r'\b' + old_simple + r'\s*\.\s*EMPTY\b', f'{holder}.empty()', body)
                stats['StringTextComponent.EMPTY -> Component.empty()'] += n

        body = TOKEN.sub(lambda m: renames.get(m.group(0), m.group(0)) if m.group(0) in touched
                         else m.group(0), body)

        # 2. import relocation, longest (most specific) key first.
        for index, (static, imp) in enumerate(imports):
            key = f'/*PORT_IMPORT_{index}*/'
            new_imp = imp
            if not static:
                for candidate in sorted(type_map, key=len, reverse=True):
                    if imp == candidate:
                        new_imp = type_map[candidate]
                        break
                else:
                    if imp in text_map:
                        new_imp = text_map[imp]
            body = body.replace(key, ('import static ' if static else 'import ') + new_imp + ';')

        # 3. make sure every rewritten reference can resolve, and drop duplicates.
        needed = {}
        for old_simple, new_fqn in list(simple_type.items()) + list(simple_text.items()):
            if old_simple in touched and new_fqn.rsplit('.', 1)[0] not in wildcards:
                needed[simple(new_fqn)] = new_fqn
        for imp in extra_imports:
            if imp.rsplit('.', 1)[0] not in wildcards:
                needed[simple(imp)] = imp

        lines = body.split('\n')
        existing = set()
        result = []
        insert_at = None
        for line in lines:
            match = IMPORT.match(line)
            if match:
                stripped = line.strip()
                if stripped in existing:
                    continue
                existing.add(stripped)
                insert_at = len(result) + 1
            result.append(line)
        if insert_at is None:
            insert_at = 0
        for new_simple, new_fqn in sorted(needed.items()):
            if new_simple in used_simple and re.search(r'\b' + re.escape(new_simple) + r'\b', body):
                statement = f'import {new_fqn};'
                if statement not in existing:
                    result.insert(insert_at, statement)
                    existing.add(statement)
                    stats[f'added import {new_fqn}'] += 1
                    insert_at += 1
        body = '\n'.join(result)

        if body != original:
            stats['files rewritten'] += 1
            changed.append((path, body))

    for rule, count in sorted(stats.items()):
        print(f'{count:6d}  {rule}')
    print(f'{len(changed)} files would change')
    if apply:
        for path, body in changed:
            path.write_text(body)
        print('applied')
    else:
        print('dry run - pass --apply to write')


if __name__ == '__main__':
    main()
