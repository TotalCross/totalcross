#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Validate and optionally repair headers using conservative provenance rules."""
from __future__ import annotations

import argparse
import datetime as dt
import os
import sys
from pathlib import Path

sys.dont_write_bytecode = True

ROOT = Path(__file__).resolve().parents[1]
PROVENANCE_DIR = ROOT / "legal" / "copyright-provenance"
if str(PROVENANCE_DIR) not in sys.path:
    sys.path.insert(0, str(PROVENANCE_DIR))

from provenance import (  # noqa: E402
    SPDX,
    ProvenanceError,
    Rule,
    DAVE_SLAUGHTER,
    SUPERWABA,
    TOTALCROSS,
    WABASOFT_PREFIX,
    canonical_inherited,
    creation_notices,
    git,
    java_fingerprint,
    load_rules,
    notice_mismatches,
    parse_notices,
    replace_header,
    repo_root,
)

ZERO_SHA = "0000000000000000000000000000000000000000"
CHECK_EXTENSIONS = {
    ".java", ".gradle", ".kt", ".c", ".h", ".cpp", ".cc", ".hpp",
    ".sh", ".md", ".html", ".yml", ".yaml", ".rb", ".py",
}
EXCLUDED_PREFIXES = (
    "TotalCrossVM/deps/",
    "build/",
    "legal/copyright-provenance/audits/",
    "TotalCrossSDK/src/main/java/totalcross/util/regex/",
    "TotalCrossSDK/src/main/java/totalcross/db/sqlite/",
)
EXCLUDED_PATHS = {".agent/PLANS.md"}
ALWAYS_CHECK_PATHS = {
    "TotalCrossSDK/src/main/java/totalcross/db/sqlite/SQLiteUtil.java",
    "TotalCrossSDK/src/main/java/totalcross/db/sqlite/ui/DBListBox.java",
}


def parse_name_status(output: str) -> list[tuple[str, str]]:
    entries: list[tuple[str, str]] = []
    for line in output.splitlines():
        if line:
            parts = line.split("\t")
            entries.append((parts[0], parts[-1]))
    return entries


def diff_name_status(*args: str) -> list[tuple[str, str]]:
    return parse_name_status(git([
        "diff", "-M", "--name-status", "--diff-filter=ACMRT", *args,
    ]))


def diff_tree_name_status(commit: str) -> list[tuple[str, str]]:
    return parse_name_status(git([
        "diff-tree", "-M", "--no-commit-id", "--name-status",
        "--diff-filter=ACMRT", "-r", commit,
    ]))


def list_files_from_environment() -> list[tuple[str, str]]:
    event = os.environ.get("EVENT_NAME") or os.environ.get("GITHUB_EVENT_NAME", "")
    if event == "pull_request" and os.environ.get("PR_BASE_SHA") and os.environ.get("PR_HEAD_SHA"):
        base_sha = os.environ["PR_BASE_SHA"]
        head_sha = os.environ["PR_HEAD_SHA"]
        base = git(["merge-base", base_sha, head_sha], False).strip() or base_sha
        return diff_name_status(base, head_sha)
    if os.environ.get("PUSH_AFTER"):
        after = os.environ["PUSH_AFTER"]
        before = os.environ.get("PUSH_BEFORE", "")
        return diff_name_status(before, after) if before and before != ZERO_SHA else diff_tree_name_status(after)
    staged = diff_name_status("--cached")
    return staged or diff_name_status()


def list_files(args: argparse.Namespace) -> list[tuple[str, str]]:
    if args.files is not None:
        return [("M", path) for path in args.files]
    if args.commit:
        return diff_tree_name_status(args.commit)
    if args.base and args.head:
        return diff_name_status(args.base, args.head)
    return list_files_from_environment()


def should_check(path: str, root: Path) -> bool:
    if path in EXCLUDED_PATHS:
        return False
    if path in ALWAYS_CHECK_PATHS:
        return True
    item = Path(path)
    if item.suffix not in CHECK_EXTENSIONS or not (root / item).is_file():
        return False
    if any(path.startswith(prefix) for prefix in EXCLUDED_PREFIXES):
        return False
    return "/build/" not in path and "/.gradle/" not in path and not path.endswith(".orig")


def creation_year(path: str) -> int:
    output = git([
        "log", "--follow", "--diff-filter=A", "--format=%ad",
        "--date=format:%Y", "--", path,
    ], False)
    years = [line.strip() for line in output.splitlines() if line.strip()]
    return int(years[-1]) if years else dt.date.today().year


def ordinary_expected(path: str, status: str, actual: list) -> tuple[list, bool]:
    year = creation_year(path)
    current_year = dt.date.today().year
    if status.startswith("A") and year == current_year:
        return creation_notices(current_year), True
    owners = {notice.owner for notice in actual}
    historical = (
        DAVE_SLAUGHTER in owners or SUPERWABA in owners or TOTALCROSS in owners
        or any(owner.startswith(WABASOFT_PREFIX) for owner in owners)
    )
    return (canonical_inherited(actual) if historical else creation_notices(year)), False


def validate_text(path: str, status: str, text: str, rule: Rule | None,
                  require_snapshot: bool = False) -> tuple[list[str], list, bool]:
    actual = parse_notices(text)
    reasons: list[str] = []
    strict_owners = False
    if rule is not None:
        # An approved provenance audit permanently establishes the minimum
        # historical notices required by this path. Code changes after the
        # audited snapshot do not invalidate that obligation; reducing notices
        # requires a separate, explicitly approved removal audit.
        expected = rule.notices
        if require_snapshot and java_fingerprint(text) != rule.fingerprint:
            reasons.append(
                "provenance snapshot mismatch for " + ", ".join(rule.audit_ids)
            )
    else:
        expected, strict_owners = ordinary_expected(path, status, actual)

    if not actual:
        reasons.append("missing copyright header")
    reasons.extend(notice_mismatches(actual, expected))
    if strict_owners:
        expected_owners = {notice.owner for notice in expected}
        unexpected = sorted({notice.owner for notice in actual} - expected_owners)
        if unexpected:
            reasons.append("new file has unexpected copyright holders: " + ", ".join(unexpected))
    if SPDX not in "\n".join(text.splitlines()[:120]):
        reasons.append(f"missing {SPDX}")
    return reasons, expected, strict_owners


def validate_file(root: Path, status: str, path: str, rule: Rule | None,
                  fix: bool, require_snapshot: bool = False) -> tuple[list[str], bool]:
    file_path = root / path
    text = file_path.read_text(encoding="utf-8")
    reasons, expected, strict_owners = validate_text(
        path, status, text, rule, require_snapshot
    )
    snapshot_mismatch = any(
        reason.startswith("provenance snapshot mismatch") for reason in reasons
    )
    changed = False
    if fix and reasons and not snapshot_mismatch:
        updated = replace_header(text, path, expected, preserve_extras=not strict_owners)
        if updated != text:
            file_path.write_text(updated, encoding="utf-8")
            changed = True
        reasons, _, _ = validate_text(
            path, status, updated, rule, require_snapshot
        )
    return reasons, changed


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate or repair copyright headers.")
    parser.add_argument("base", nargs="?", help="base commit")
    parser.add_argument("head", nargs="?", help="head commit")
    parser.add_argument("--commit", help="validate files changed by one commit")
    parser.add_argument("--files", nargs="*", help="validate explicit files")
    parser.add_argument("--fix", action="store_true", help="repair header mismatches")
    parser.add_argument("--audit-id", action="append", default=[], help="restrict to one active audit")
    parser.add_argument(
        "--require-provenance-snapshots", action="store_true",
        help="require audited code fingerprints; intended for audit activation",
    )
    args = parser.parse_args()
    if bool(args.base) != bool(args.head):
        parser.error("base and head must be provided together")
    if args.require_provenance_snapshots and not args.audit_id:
        parser.error("--require-provenance-snapshots requires --audit-id")

    try:
        root = repo_root()
        os.chdir(root)
        rules = load_rules(root, set(args.audit_id) if args.audit_id else None)
        failures: list[tuple[str, list[str]]] = []
        checked = fixed = 0
        seen: set[str] = set()
        for status, path in list_files(args):
            if path in seen or not should_check(path, root):
                continue
            seen.add(path)
            checked += 1
            reasons, changed = validate_file(
                root, status, path, rules.get(path), args.fix,
                args.require_provenance_snapshots,
            )
            fixed += int(changed)
            if changed:
                print(f"Fixed copyright header: {path}")
            if reasons:
                failures.append((path, reasons))
        if failures:
            print(f"Copyright header validation failed for {len(failures)} file(s):", file=sys.stderr)
            for path, reasons in failures:
                print(f"- {path}", file=sys.stderr)
                for reason in reasons:
                    print(f"  - {reason}", file=sys.stderr)
            return 1
        verb = "fixed" if args.fix else "validated"
        print(f"Copyright headers {verb}: {checked} file(s), {fixed} changed.")
        return 0
    except ProvenanceError as exc:
        print(f"Copyright header configuration error: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
