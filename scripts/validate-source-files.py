#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Run independent repository source-file policy checks."""
from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

sys.dont_write_bytecode = True

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT / "scripts") not in sys.path:
    sys.path.insert(0, str(ROOT / "scripts"))
if str(ROOT / "legal" / "copyright-provenance") not in sys.path:
    sys.path.insert(0, str(ROOT / "legal" / "copyright-provenance"))

from provenance import ProvenanceError, repo_root  # noqa: E402
from source_validation import changes, headers  # noqa: E402


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Validate repository source files.")
    parser.add_argument("base", nargs="?", help="base commit")
    parser.add_argument("head", nargs="?", help="head commit")
    parser.add_argument("--commit", help="compare one commit with its first parent")
    parser.add_argument("--files", nargs="*", help="limit validation to explicit files")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--staged", action="store_true", help="compare HEAD with the Git index")
    mode.add_argument("--working-tree", action="store_true", help="compare HEAD with the working tree")
    parser.add_argument("--check", action="append", choices=("headers",),
                        help="run only the selected check")
    parser.add_argument("--fix", action="store_true", help="repair header mismatches")
    parser.add_argument("--audit-id", action="append", default=[],
                        help="restrict to one active provenance audit")
    parser.add_argument(
        "--require-provenance-snapshots", action="store_true",
        help="require audited code fingerprints; intended for audit activation",
    )
    args = parser.parse_args()
    if bool(args.base) != bool(args.head):
        parser.error("base and head must be provided together")
    selected_modes = sum(bool(value) for value in (
        args.base, args.commit, args.staged, args.working_tree,
    ))
    if selected_modes > 1:
        parser.error("choose only one comparison mode")
    if args.require_provenance_snapshots and not args.audit_id:
        parser.error("--require-provenance-snapshots requires --audit-id")
    return args


def main() -> int:
    args = parse_args()
    try:
        root = repo_root()
        os.chdir(root)
        change_set = changes.discover_changes(
            root, base=args.base, head=args.head, commit=args.commit,
            files=args.files, staged=args.staged, working_tree=args.working_tree,
        )
        return headers.run(
            root, change_set, fix=args.fix,
            audit_ids=set(args.audit_id) if args.audit_id else None,
            require_snapshots=args.require_provenance_snapshots,
        )
    except (changes.ChangeDiscoveryError, ProvenanceError) as exc:
        print(f"Source validation configuration error: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
