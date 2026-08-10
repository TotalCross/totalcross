# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Enforce the forward-only byte-size ratchet for maintained source files."""
from __future__ import annotations

import dataclasses
import enum
import sys
from pathlib import PurePosixPath

from .changes import ChangeSet, ChangedFile

SOURCE_FILE_LIMIT = 20_480
LEGACY_GROWTH_TOLERANCE = 500

SOURCE_SUFFIXES = {
    ".java", ".kt", ".kts", ".gradle", ".groovy",
    ".c", ".cc", ".cpp", ".cxx", ".h", ".hh", ".hpp", ".hxx",
    ".m", ".mm", ".py", ".rb", ".sh",
    ".js", ".jsx", ".ts", ".tsx",
    ".asm", ".s", ".cmake", ".mk", ".bat", ".cmd", ".swift",
    ".proto", ".rc", ".pch",
}
SOURCE_NAMES = {
    "CMakeLists.txt", "Gemfile", "GNUmakefile", "Makefile", "Podfile",
    "Rakefile", "gradlew-agent",
}
EXCLUDED_PREFIXES = (
    "TotalCrossVM/deps/",
    "TotalCrossVM/third_party/",
    "legal/copyright-provenance/audits/",
    "TotalCrossSDK/src/main/java/totalcross/util/regex/",
    "TotalCrossSDK/src/main/java/totalcross/db/sqlite/",
    "TotalCrossSDK/src/main/java/totalcross/sql/sqlite4j/",
)
EXCLUDED_COMPONENTS = {
    ".cxx", ".gradle", "CMakeFiles", "_deps", "build", "dist", "gen",
    "generated", "node_modules", "out", "target", "third_party", "vendor",
}


class Outcome(enum.Enum):
    PASS = "pass"
    WARNING = "warning"
    FAIL = "fail"


@dataclasses.dataclass(frozen=True)
class Decision:
    outcome: Outcome
    reason: str


@dataclasses.dataclass(frozen=True)
class Finding:
    path: str
    base_size: int | None
    current_size: int
    decision: Decision

    @property
    def delta(self) -> int | None:
        return None if self.base_size is None else self.current_size - self.base_size


def evaluate(base_size: int | None, current_size: int) -> Decision:
    """Evaluate sizes without depending on Git or filesystem state."""
    if base_size is None:
        if current_size <= SOURCE_FILE_LIMIT:
            return Decision(Outcome.PASS, "new source is within the limit")
        return Decision(Outcome.FAIL, "new source exceeds the 20 KiB limit")
    if base_size <= SOURCE_FILE_LIMIT:
        if current_size <= SOURCE_FILE_LIMIT:
            return Decision(Outcome.PASS, "existing source remains within the limit")
        return Decision(
            Outcome.FAIL,
            "source at or below the limit in the base may not cross 20 KiB",
        )
    growth = current_size - base_size
    if growth <= 0:
        return Decision(Outcome.PASS, "oversized legacy source did not grow")
    if growth <= LEGACY_GROWTH_TOLERANCE:
        return Decision(Outcome.WARNING, "oversized legacy source grew within tolerance")
    return Decision(Outcome.FAIL, "oversized legacy source exceeded growth tolerance")


def should_check(path: str) -> bool:
    """Return whether a path is manually maintained executable/build source."""
    normalized = path.replace("\\", "/")
    while normalized.startswith("./"):
        normalized = normalized[2:]
    if any(normalized.startswith(prefix) for prefix in EXCLUDED_PREFIXES):
        return False
    item = PurePosixPath(normalized)
    if any(part in EXCLUDED_COMPONENTS for part in item.parts[:-1]):
        return False
    if normalized.endswith(".orig"):
        return False
    return item.name in SOURCE_NAMES or item.suffix.lower() in SOURCE_SUFFIXES


def _base_path(change: ChangedFile) -> str | None:
    if change.status.startswith("R"):
        return change.old_path
    if change.status.startswith(("A", "C")):
        return None
    return change.old_path or change.new_path


def collect(change_set: ChangeSet, root) -> list[Finding]:
    findings: list[Finding] = []
    seen: set[str] = set()
    for change in change_set.files:
        path = change.current_path
        if path is None or path in seen or not should_check(path):
            continue
        current = change_set.target.read(root, path)
        if current is None:
            continue
        base_path = _base_path(change)
        base = change_set.base.read(root, base_path) if base_path is not None else None
        findings.append(Finding(path, None if base is None else len(base), len(current),
                                evaluate(None if base is None else len(base), len(current))))
        seen.add(path)
    return findings


def _size(value: int | None) -> str:
    return "new file" if value is None else f"{value:,} bytes"


def _print_finding(finding: Finding, *, warning: bool) -> None:
    stream = sys.stderr
    label = "WARNING: oversized source grew within tolerance" if warning else "Source file size violation"
    print(f"{label}: {finding.path}", file=stream)
    print(f"  base:       {_size(finding.base_size)}", file=stream)
    print(f"  current:    {finding.current_size:,} bytes", file=stream)
    if finding.delta is not None:
        print(f"  delta:      {finding.delta:+,} bytes", file=stream)
    if finding.base_size is not None and finding.base_size > SOURCE_FILE_LIMIT:
        print(f"  tolerance:  {LEGACY_GROWTH_TOLERANCE:,} bytes", file=stream)
    else:
        print(f"  limit:      {SOURCE_FILE_LIMIT:,} bytes", file=stream)
    print(f"  reason:     {finding.decision.reason}", file=stream)


def run(root, change_set: ChangeSet) -> int:
    findings = collect(change_set, root)
    warnings = [item for item in findings if item.decision.outcome is Outcome.WARNING]
    failures = [item for item in findings if item.decision.outcome is Outcome.FAIL]
    for finding in warnings:
        _print_finding(finding, warning=True)
    if failures:
        print(f"Source file size validation failed for {len(failures)} file(s):", file=sys.stderr)
        for finding in failures:
            _print_finding(finding, warning=False)
        print("Split or extract new responsibility before committing.", file=sys.stderr)
        return 1
    print(f"Source sizes validated: {len(findings)} file(s), {len(warnings)} warning(s).")
    return 0
