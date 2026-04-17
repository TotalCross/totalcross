#!/usr/bin/env python3
"""
Generate a Markdown changelog grouped by primary scope.

Usage:
    python3 scripts/generate-changelog-by-scope.py <start-commit> <end-commit>

The script reads commits in the range <start-commit>..<end-commit>, parses titles
that follow the repository convention:

    <type>(<scope>[,<platform>][,<arch>]): short description

and groups them by primary scope. Commits that do not match the format are ignored.
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from collections import OrderedDict


TITLE_PATTERN = re.compile(
    r"^(?P<type>vm|runtime|sdk|compiler|tools|build|perf|fix|refactor|test|doc|chore)"
    r"\((?P<qualifiers>[a-z0-9_-]+(?:,[a-z0-9_-]+){0,2})\): "
    r"(?P<description>[a-z0-9 ].*)$"
)

TYPE_ORDER = [
    "fix",
    "perf",
    "sdk",
    "runtime",
    "vm",
    "compiler",
    "tools",
    "build",
    "refactor",
    "test",
    "doc",
    "chore",
]


def run_git(*args: str) -> str:
    return subprocess.check_output(["git", *args], text=True).strip()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate a Markdown changelog grouped by scope."
    )
    parser.add_argument("start_commit", help="Commit that marks the start of the range.")
    parser.add_argument("end_commit", help="Commit that marks the end of the range.")
    return parser.parse_args()


def read_commits(start_commit: str, end_commit: str) -> list[tuple[str, str]]:
    output = run_git("log", "--reverse", "--format=%H%x09%s", f"{start_commit}..{end_commit}")
    if not output:
        return []

    commits = []
    for line in output.splitlines():
        commit_hash, subject = line.split("\t", 1)
        commits.append((commit_hash, subject))
    return commits


def group_commits(
    commits: list[tuple[str, str]],
) -> tuple[OrderedDict[str, list[tuple[str, str, str]]], list[tuple[str, str]]]:
    grouped: OrderedDict[str, list[tuple[str, str, str]]] = OrderedDict()
    ignored: list[tuple[str, str]] = []

    for commit_hash, subject in commits:
        match = TITLE_PATTERN.match(subject)
        if not match:
            ignored.append((commit_hash, subject))
            continue

        commit_type = match.group("type")
        qualifiers = match.group("qualifiers")
        scope = qualifiers.split(",", 1)[0]
        description = match.group("description")

        if scope not in grouped:
            grouped[scope] = []
        grouped[scope].append((commit_type, qualifiers, description))

    return grouped, ignored


def sort_scope_entries(entries: list[tuple[str, str, str]]) -> list[tuple[str, str, str]]:
    type_index = {commit_type: index for index, commit_type in enumerate(TYPE_ORDER)}
    return sorted(entries, key=lambda item: (type_index.get(item[0], len(TYPE_ORDER)), item[1], item[2]))


def format_scope_title(scope: str) -> str:
    return scope.upper() if scope in {"ui", "vm"} else scope.capitalize()


def render_markdown(
    start_commit: str,
    end_commit: str,
    grouped: OrderedDict[str, list[tuple[str, str, str]]],
    ignored: list[tuple[str, str]],
) -> str:
    lines: list[str] = [
        "# Changelog",
        "",
        f"Range: `{start_commit}..{end_commit}`",
        "",
    ]

    if not grouped:
        lines.append("No matching commits found in the selected range.")
        if ignored:
            lines.extend(["", "Ignored commits:", ""])
            for commit_hash, subject in ignored:
                lines.append(f"- `{commit_hash[:10]}` {subject}")
        return "\n".join(lines)

    for scope, entries in grouped.items():
        lines.append(f"## {format_scope_title(scope)}")
        lines.append("")
        for commit_type, qualifiers, description in sort_scope_entries(entries):
            lines.append(f"- `{commit_type}` `{qualifiers}` {description}")
        lines.append("")

    if ignored:
        lines.append("## Ignored")
        lines.append("")
        lines.append("These commits were skipped because their titles do not match the project format.")
        lines.append("")
        for commit_hash, subject in ignored:
            lines.append(f"- `{commit_hash[:10]}` {subject}")
        lines.append("")

    return "\n".join(lines).rstrip() + "\n"


def main() -> int:
    args = parse_args()

    try:
        commits = read_commits(args.start_commit, args.end_commit)
    except subprocess.CalledProcessError as exc:
        sys.stderr.write(exc.output)
        sys.stderr.write("Failed to read commit range.\n")
        return exc.returncode or 1

    grouped, ignored = group_commits(commits)
    sys.stdout.write(
        render_markdown(args.start_commit, args.end_commit, grouped, ignored)
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
