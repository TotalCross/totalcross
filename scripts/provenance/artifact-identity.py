#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import json
from pathlib import Path
import struct
import sys
import tarfile
import zipfile
import zlib

sys.dont_write_bytecode = True

from artifact_identity import (
    artifact_identity,
    validate_artifact_set,
    validate_compatibility,
    validate_downloads,
    validate_identity,
)
from compatibility_contract import read_compatibility_contract


def main():
    parser = argparse.ArgumentParser(
        description="Read or validate embedded TotalCross artifact build identities."
    )
    parser.add_argument("artifacts", nargs="*", type=Path)
    parser.add_argument(
        "--artifact-download",
        action="append",
        default=[],
        metavar="NAME=OUTCOME=ROOT",
        help="validate each successfully downloaded package artifact",
    )
    parser.add_argument("--repository", help="expected normalized repository identity")
    parser.add_argument("--source-tree", help="expected Git source tree object ID")
    parser.add_argument(
        "--compatibility-only",
        action="store_true",
        help="allow different Build IDs when compatibility epochs align",
    )
    parser.add_argument(
        "--compatibility-contract",
        type=Path,
        help="require applicable artifact epochs to match this registry",
    )
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()

    if bool(args.repository) != bool(args.source_tree):
        parser.error("--repository and --source-tree must be used together")
    if args.artifacts and args.artifact_download:
        parser.error("use either direct artifacts or --artifact-download, not both")
    if args.compatibility_only and (args.repository or args.artifact_download):
        parser.error("--compatibility-only requires direct artifacts without repository checks")
    if not args.artifacts and not args.artifact_download:
        parser.error("provide artifacts or --artifact-download entries")

    try:
        contract = (
            read_compatibility_contract(args.compatibility_contract)
            if args.compatibility_contract
            else None
        )
        if args.artifact_download:
            if not args.repository or not args.source_tree:
                parser.error("--artifact-download requires --repository and --source-tree")
            rows = validate_downloads(
                args.artifact_download,
                args.repository,
                args.source_tree,
            )
            compatibility = validate_compatibility(rows)
        else:
            rows = [artifact_identity(path) for path in args.artifacts]
            if args.repository:
                for row in rows:
                    validate_identity(row, args.repository, args.source_tree)
                compatibility = validate_compatibility(rows)
            else:
                compatibility = validate_artifact_set(
                    rows, allow_mixed_builds=args.compatibility_only
                )
        if contract is not None:
            for key in ("tczFormat", "converterAbi", "runtimeAbi"):
                actual = compatibility.get(key)
                expected = str(contract[key])
                if actual is not None and actual != expected:
                    raise ValueError(
                        f"compatibility {key} mismatch: expected {expected}, got {actual}"
                    )
    except (
        OSError,
        ValueError,
        EOFError,
        KeyError,
        IndexError,
        struct.error,
        tarfile.TarError,
        zipfile.BadZipFile,
        zlib.error,
    ) as error:
        print(f"artifact identity validation failed: {error}", file=sys.stderr)
        return 1

    if args.json:
        print(json.dumps(rows, sort_keys=True))
    else:
        for row in rows:
            print(
                f"{row['path']}: {row['kind']} build={row.get('buildId') or 'missing'} "
                f"tree={row.get('sourceTree') or 'missing'}"
            )
        if args.repository:
            print(f"validated embedded build identity for {len(rows)} payloads")
        elif args.compatibility_only:
            print(
                "compatible TotalCross artifacts: "
                f"runtimeAbi={compatibility['runtimeAbi']} "
                f"converterAbi={compatibility['converterAbi'] or 'n/a'} "
                f"tczFormat={compatibility['tczFormat'] or 'n/a'}"
            )
        else:
            print(f"coherent TotalCross build: {rows[0]['buildId']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
