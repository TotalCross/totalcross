#!/usr/bin/env python3
"""
Generate a Markdown changelog from commit messages in the current project format.

Usage:
    python3 scripts/generate-changelog.py <start-commit> <end-commit>

The script reads commits in the range <start-commit>..<end-commit>, parses titles
that follow the repository convention:

    <type>(<scope>[,<platform>]): short description

and groups them by commit type and primary scope. Commits that do not match the
format are ignored.
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from collections import OrderedDict


TITLE_PATTERN = re.compile(
    r"^(?P<type>fix|feat|refactor|perf|style|test|docs|build|ci|chore|revert)"
    r"(?P<leading_breaking>!)?"
    r"\((?P<qualifiers>[a-z0-9_-]+(?:,[a-z0-9_-]+)?)\)"
    r"(?P<trailing_breaking>!)?: "
    r"(?P<description>[a-z0-9 ].*)$"
)

TYPE_ORDER = [
    "feat",
    "fix",
    "perf",
    "refactor",
    "style",
    "test",
    "docs",
    "build",
    "ci",
    "chore",
    "revert",
]

TYPE_TITLES = {
    "feat": "Features",
    "fix": "Fixes",
    "perf": "Performance",
    "refactor": "Refactors",
    "style": "Style",
    "test": "Tests",
    "docs": "Documentation",
    "build": "Build",
    "ci": "CI",
    "chore": "Chores",
    "revert": "Reverts",
}


def run_git(*args: str) -> str:
    return subprocess.check_output(["git", *args], text=True).strip()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate a Markdown changelog from a commit range."
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
) -> tuple[OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]], list[tuple[str, str]]]:
    grouped: OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]] = OrderedDict(
        (commit_type, OrderedDict()) for commit_type in TYPE_ORDER
    )
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
        is_breaking = bool(match.group("leading_breaking") or match.group("trailing_breaking"))
        if is_breaking:
            description = f"{description} [breaking]"
        target_type = commit_type

        if scope not in grouped[target_type]:
            grouped[target_type][scope] = []
        grouped[target_type][scope].append((qualifiers, description))

    grouped = OrderedDict(
        (commit_type, entries) for commit_type, entries in grouped.items() if entries
    )
    return grouped, ignored


def render_markdown(
    start_commit: str,
    end_commit: str,
    grouped: OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]],
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
            lines.extend(
                [
                    "",
                    "Ignored commits:",
                    "",
                ]
            )
            for commit_hash, subject in ignored:
                lines.append(f"- `{commit_hash[:10]}` {subject}")
        return "\n".join(lines)

    for commit_type, scopes in grouped.items():
        lines.append(f"## {TYPE_TITLES[commit_type]}")
        lines.append("")
        for scope, entries in scopes.items():
            for qualifiers, description in entries:
                lines.append(f"- `{qualifiers}` {description}")
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
