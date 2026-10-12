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
    validate_downloads,
    validate_identity,
)


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
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()

    if bool(args.repository) != bool(args.source_tree):
        parser.error("--repository and --source-tree must be used together")
    if args.artifacts and args.artifact_download:
        parser.error("use either direct artifacts or --artifact-download, not both")
    if not args.artifacts and not args.artifact_download:
        parser.error("provide artifacts or --artifact-download entries")

    try:
        if args.artifact_download:
            if not args.repository or not args.source_tree:
                parser.error("--artifact-download requires --repository and --source-tree")
            rows = validate_downloads(
                args.artifact_download,
                args.repository,
                args.source_tree,
            )
        else:
            rows = [artifact_identity(path) for path in args.artifacts]
            if args.repository:
                for row in rows:
                    validate_identity(row, args.repository, args.source_tree)
            else:
                missing = [
                    row["path"]
                    for row in rows
                    if not row.get("buildId")
                    or not row.get("sourceTree")
                    or row.get("identityVersion") != "1"
                ]
                if missing:
                    raise ValueError(
                        "missing or incomplete embedded build identity: "
                        + ", ".join(missing)
                    )
                build_ids = {row["buildId"] for row in rows}
                trees = {row.get("sourceTree") for row in rows}
                if len(build_ids) != 1:
                    raise ValueError("mixed TotalCross Build IDs detected")
                if len(trees) != 1:
                    raise ValueError("mixed TotalCross source trees detected")
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
        else:
            print(f"coherent TotalCross build: {rows[0]['buildId']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
