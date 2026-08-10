# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Validate and optionally repair headers using provenance rules."""
from __future__ import annotations

import datetime as dt
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PROVENANCE_DIR = ROOT / "legal" / "copyright-provenance"
if str(PROVENANCE_DIR) not in sys.path:
    sys.path.insert(0, str(PROVENANCE_DIR))

from provenance import (  # noqa: E402
    SPDX,
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
)

from .changes import ChangeSet

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


def should_check(path: str, exists: bool) -> bool:
    if path in EXCLUDED_PATHS:
        return False
    if path in ALWAYS_CHECK_PATHS:
        return exists
    item = Path(path)
    if item.suffix not in CHECK_EXTENSIONS or not exists:
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
        expected = rule.notices
        if require_snapshot and java_fingerprint(text) != rule.fingerprint:
            reasons.append("provenance snapshot mismatch for " + ", ".join(rule.audit_ids))
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


def validate_file(root: Path, status: str, path: str, text: str,
                  rule: Rule | None, fix: bool,
                  require_snapshot: bool = False) -> tuple[list[str], bool]:
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
            (root / path).write_text(updated, encoding="utf-8")
            changed = True
        reasons, _, _ = validate_text(path, status, updated, rule, require_snapshot)
    return reasons, changed


def run(root: Path, changes: ChangeSet, *, fix: bool = False,
        audit_ids: set[str] | None = None,
        require_snapshots: bool = False) -> int:
    rules = load_rules(root, audit_ids)
    failures: list[tuple[str, list[str]]] = []
    checked = fixed = 0
    seen: set[str] = set()
    for change in changes.files:
        path = change.current_path
        if path is None or path in seen:
            continue
        # Preserve historical header-validation behavior for revision modes,
        # while staged pre-commit validation must inspect the index exactly.
        staged_snapshot = changes.mode == "staged" and not fix
        content = (changes.target.read(root, path) if staged_snapshot
                   else ((root / path).read_bytes() if (root / path).is_file() else None))
        if not should_check(path, content is not None):
            continue
        seen.add(path)
        checked += 1
        text = content.decode("utf-8")
        reasons, changed = validate_file(
            root, change.status, path, text, rules.get(path), fix, require_snapshots,
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
    verb = "fixed" if fix else "validated"
    print(f"Copyright headers {verb}: {checked} file(s), {fixed} changed.")
    return 0
