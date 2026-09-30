#!/usr/bin/env python3
"""Summarise javac errors from a build log so porting work can be ordered by system.

Usage: analyze_errors.py <log> [--symbols] [--files] [--samples N]
"""
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

HEADER = re.compile(r'^(?P<file>\S+\.java):(?P<line>\d+): error: (?P<msg>.*)$')
SYMBOL = re.compile(r'^\s*symbol:\s+(?:class|variable|method)\s+(?P<sym>\S+)')
LOCATION = re.compile(r'^\s*location:\s+(?P<loc>.+)$')


def main():
    log = Path(sys.argv[1])
    show_symbols = '--symbols' in sys.argv
    show_files = '--files' in sys.argv
    samples = 0
    if '--samples' in sys.argv:
        samples = int(sys.argv[sys.argv.index('--samples') + 1])

    errors = []
    current = None
    for line in log.read_text(errors='replace').splitlines():
        header = HEADER.match(line)
        if header:
            current = dict(header.groupdict(), symbol=None, location=None, sample=None)
            errors.append(current)
            continue
        if current is None:
            continue
        symbol = SYMBOL.match(line)
        if symbol:
            current['symbol'] = symbol.group('sym')
            continue
        location = LOCATION.match(line)
        if location:
            current['location'] = location.group('loc').strip()
            continue
        if samples and current['sample'] is None and line.strip():
            current['sample'] = line.strip()

    print(f'{len(errors)} errors parsed from {log}')
    if not errors:
        return

    by_msg = Counter()
    for err in errors:
        msg = err['msg']
        msg = re.sub(r"'[^']*'", "'…'", msg)
        by_msg[msg] += 1
    print('\n== by message ==')
    for msg, count in by_msg.most_common(25):
        print(f'{count:6d}  {msg}')

    if show_symbols:
        print('\n== by missing symbol ==')
        symbols = Counter(err['symbol'] for err in errors if err['symbol'])
        for sym, count in symbols.most_common(60):
            print(f'{count:6d}  {sym}')

    if show_files:
        print('\n== by file (top 40) ==')
        files = Counter(err['file'] for err in errors)
        for name, count in files.most_common(40):
            print(f'{count:6d}  {name}')

    print('\n== by package ==')
    packages = Counter()
    for err in errors:
        parts = Path(err['file']).parts
        try:
            index = parts.index('jojo')
        except ValueError:
            packages['<other>'] += 1
            continue
        packages['/'.join(parts[index + 1:-1]) or '<root>'] += 1
    for name, count in packages.most_common(30):
        print(f'{count:6d}  {name}')

    if samples:
        print('\n== samples ==')
        for err in errors[:samples]:
            print(f"{err['file']}:{err['line']}: {err['msg']}")
            if err['symbol']:
                print(f"    symbol: {err['symbol']}")
            if err['sample']:
                print(f"    {err['sample']}")


if __name__ == '__main__':
    main()
