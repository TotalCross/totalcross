#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import json
from pathlib import Path
import sys


def parse_args():
    parser = argparse.ArgumentParser(description="Validate TotalCross CI build provenance.")
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--repository", required=True)
    parser.add_argument("--source-commit", required=True)
    return parser.parse_args()


def main():
    args = parse_args()
    try:
        value = json.loads(args.manifest.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        print(f"invalid provenance manifest {args.manifest}: {error}", file=sys.stderr)
        return 1

    expected = {
        "schemaVersion": 1,
        "repository": args.repository,
        "sourceCommit": args.source_commit,
    }
    mismatches = [
        f"{key}: expected {expected_value!r}, got {value.get(key)!r}"
        for key, expected_value in expected.items()
        if value.get(key) != expected_value
    ]
    if mismatches:
        print(f"build provenance mismatch in {args.manifest}", file=sys.stderr)
        for mismatch in mismatches:
            print("  " + mismatch, file=sys.stderr)
        return 1

    print(f"validated build provenance: {args.manifest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
