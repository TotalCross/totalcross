#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import hashlib
import json
from pathlib import Path
import sys


def parse_args():
    parser = argparse.ArgumentParser(description="Validate TotalCross build provenance.")
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--repository", required=True)
    parser.add_argument("--source-commit", required=True)
    parser.add_argument("--source-tree", required=True)
    parser.add_argument("--require-clean-source", action="store_true")
    parser.add_argument("--require-build-identity", action="store_true")
    return parser.parse_args()


def main():
    args = parse_args()
    try:
        value = json.loads(args.manifest.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        print(f"invalid provenance manifest {args.manifest}: {error}", file=sys.stderr)
        return 1

    mismatches = []

    expected_top_level = {
        "schemaVersion": 1,
        "repository": args.repository,
    }
    mismatches.extend(
        f"{key}: expected {expected_value!r}, got {value.get(key)!r}"
        for key, expected_value in expected_top_level.items()
        if value.get(key) != expected_value
    )

    source = value.get("source")
    if not isinstance(source, dict):
        mismatches.append(f"source: expected object, got {source!r}")
    else:
        expected_source = {
            "commit": args.source_commit,
            "tree": args.source_tree,
        }
        mismatches.extend(
            f"source.{key}: expected {expected_value!r}, got {source.get(key)!r}"
            for key, expected_value in expected_source.items()
            if source.get(key) != expected_value
        )
        if args.require_clean_source and source.get("dirty") is not False:
            mismatches.append(
                f"source.dirty: expected False, got {source.get('dirty')!r}"
            )

    if args.require_build_identity and isinstance(source, dict):
        canonical = json.dumps(
            {
                "identityVersion": 1,
                "repository": args.repository,
                "sourceTree": args.source_tree,
            },
            sort_keys=True,
            separators=(",", ":"),
        ).encode("utf-8")
        expected_build_id = hashlib.sha256(canonical).hexdigest()
        build = value.get("build")
        if not isinstance(build, dict):
            mismatches.append(f"build: expected object, got {build!r}")
        else:
            if build.get("identityVersion") != 1:
                mismatches.append(
                    "build.identityVersion: expected 1, "
                    f"got {build.get('identityVersion')!r}"
                )
            if build.get("id") != expected_build_id:
                mismatches.append(
                    f"build.id: expected {expected_build_id!r}, got {build.get('id')!r}"
                )

    if mismatches:
        print(f"build provenance mismatch in {args.manifest}", file=sys.stderr)
        for mismatch in mismatches:
            print("  " + mismatch, file=sys.stderr)
        return 1

    print(f"validated build provenance: {args.manifest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
