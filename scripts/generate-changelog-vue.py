#!/usr/bin/env python3
"""
Generate a Markdown changelog in a Vue-like format.

Usage:
    python3 scripts/generate-changelog-vue.py <start-commit> <end-commit>

The script reads commits in the range <start-commit>..<end-commit>, parses titles
that follow the repository convention:

    <type>(<scope>[,<platform>][,<arch>]): short description

and renders sections similar to the Vue release notes style.
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
    "sdk",
    "fix",
    "perf",
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

TYPE_TITLES = {
    "sdk": "Features",
    "fix": "Bug Fixes",
    "perf": "Performance Improvements",
    "runtime": "Runtime",
    "vm": "VM",
    "compiler": "Compiler",
    "tools": "Tools",
    "build": "Build",
    "refactor": "Refactors",
    "test": "Tests",
    "doc": "Documentation",
    "chore": "Chores",
}


def run_git(*args: str) -> str:
    return subprocess.check_output(["git", *args], text=True).strip()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate a Vue-like Markdown changelog from a commit range."
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
    grouped: OrderedDict[str, list[tuple[str, str, str]]] = OrderedDict(
        (commit_type, []) for commit_type in TYPE_ORDER
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
        target_type = commit_type
        if scope == "build" and commit_type in {"fix", "sdk"}:
            target_type = "build"
        elif scope == "deploy" and commit_type in {"fix", "sdk"}:
            target_type = "tools"

        grouped[target_type].append((commit_hash[:7], qualifiers, description))

    grouped = OrderedDict(
        (commit_type, entries) for commit_type, entries in grouped.items() if entries
    )
    return grouped, ignored


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
        return "\n".join(lines) + "\n"

    for commit_type, entries in grouped.items():
        lines.append(f"### {TYPE_TITLES[commit_type]}")
        lines.append("")
        for short_hash, qualifiers, description in entries:
            lines.append(f"- **{qualifiers}:** {description} (`{short_hash}`)")
        lines.append("")

    if ignored:
        lines.append("### Ignored")
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
