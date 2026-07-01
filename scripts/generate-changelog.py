#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""
Generate a Markdown changelog from commit messages.

Usage:
    python3 scripts/generate-changelog.py <start-commit> <end-commit>

The script reads commits in the range <start-commit>..<end-commit>, parses titles
that follow the repository convention:

    <type>(<scope>[,<platform>]): short description

and renders release-note sections grouped by type, scope, and platform.
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from collections import OrderedDict
from datetime import date
from pathlib import Path


NO_PLATFORM = ""
CHANGELOG_OMITTED_TYPES = {"ci", "chore"}
CHANGELOG_OMITTED_SCOPES = {
    "docs": {"agents"},
}
ROOT_DIR = Path(__file__).resolve().parents[1]
CHANGELOG_PATH = ROOT_DIR / "CHANGELOG.md"
SDK_BUILD_GRADLE = ROOT_DIR / "TotalCrossSDK" / "build.gradle"
VERSION_HEADER = re.compile(r"^## \[\d+\.\d+\.\d+\] - \d{4}-\d{2}-\d{2}$", re.M)
SDK_VERSION = re.compile(r"^version\s*=\s*['\"](?P<version>\d+\.\d+\.\d+)['\"]", re.M)

TITLE_PATTERN = re.compile(
    r"^(?P<type>fix|feat|refactor|perf|style|test|docs|build|ci|chore|revert)"
    r"(?P<leading_breaking>!)?"
    r"\((?P<qualifiers>[A-Za-z0-9_-]+(?:,[a-z0-9_-]+)?)\)"
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
    "fix": "Bug Fixes",
    "perf": "Performance Improvements",
    "refactor": "Refactors",
    "style": "Styles",
    "test": "Tests",
    "docs": "Documentation",
    "build": "Build",
    "ci": "Continuous Integration",
    "chore": "Chores",
    "revert": "Reverts",
}


def is_release_commit(scope: str, description: str) -> bool:
    description = description.lower()
    return scope == "release" or description.startswith("release ") or description.startswith(
        "bump version"
    )


def is_changelog_omitted_scope(commit_type: str, scope: str) -> bool:
    return scope in CHANGELOG_OMITTED_SCOPES.get(commit_type, set())


def run_git(*args: str) -> str:
    return subprocess.check_output(["git", *args], text=True).strip()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate a Markdown changelog from a commit range."
    )
    parser.add_argument("start_commit", help="Commit that marks the start of the range.")
    parser.add_argument("end_commit", help="Commit that marks the end of the range.")
    parser.add_argument(
        "--update-changelog",
        action="store_true",
        help="insert the generated section into CHANGELOG.md before the first version entry",
    )
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
) -> tuple[
    OrderedDict[str, OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]]],
    list[tuple[str, str]],
]:
    grouped: OrderedDict[
        str,
        OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]],
    ] = OrderedDict(
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
        qualifier_parts = qualifiers.split(",", 1)
        scope = qualifier_parts[0]
        platform = qualifier_parts[1] if len(qualifier_parts) > 1 else NO_PLATFORM
        description = match.group("description")
        if is_release_commit(scope, description) or is_changelog_omitted_scope(commit_type, scope):
            continue
        is_breaking = bool(match.group("leading_breaking") or match.group("trailing_breaking"))
        if is_breaking:
            description = f"{description} [breaking]"

        if scope not in grouped[commit_type]:
            grouped[commit_type][scope] = OrderedDict()
        if platform not in grouped[commit_type][scope]:
            grouped[commit_type][scope][platform] = []
        grouped[commit_type][scope][platform].append((commit_hash[:7], description))

    grouped = OrderedDict(
        (commit_type, scopes) for commit_type, scopes in grouped.items() if scopes
    )
    return grouped, ignored


def render_markdown(
    start_commit: str,
    end_commit: str,
    grouped: OrderedDict[str, OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]]],
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

    for commit_type, scopes in grouped.items():
        lines.append(f"### {TYPE_TITLES[commit_type]}")
        lines.append("")
        for scope, platforms in scopes.items():
            lines.append(f"- **{scope}:**")
            for platform, entries in platforms.items():
                if platform == NO_PLATFORM:
                    for short_hash, description in entries:
                        lines.append(f"  - {description} (`{short_hash}`)")
                else:
                    lines.append(f"  - **{platform}:**")
                    for short_hash, description in entries:
                        lines.append(f"    - {description} (`{short_hash}`)")
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


def read_sdk_version() -> str:
    content = SDK_BUILD_GRADLE.read_text()
    match = SDK_VERSION.search(content)
    if not match:
        raise ValueError(f"could not find SDK version in {SDK_BUILD_GRADLE}")
    return match.group("version")


def filter_changelog_groups(
    grouped: OrderedDict[str, OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]]],
) -> OrderedDict[str, OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]]]:
    return OrderedDict(
        (commit_type, scopes)
        for commit_type, scopes in grouped.items()
        if commit_type not in CHANGELOG_OMITTED_TYPES
    )


def render_release_section(
    version: str,
    release_date: date,
    grouped: OrderedDict[str, OrderedDict[str, OrderedDict[str, list[tuple[str, str]]]]],
    ignored: list[tuple[str, str]],
) -> str:
    lines = render_markdown("", "", grouped, ignored).splitlines()
    body = "\n".join(lines[4:]).strip()
    if not body:
        body = "No matching commits found."
    return f"## [{version}] - {release_date.isoformat()}\n\n{body}\n"


def update_changelog(section: str) -> None:
    content = CHANGELOG_PATH.read_text()
    match = VERSION_HEADER.search(content)
    if not match:
        raise ValueError(
            f"could not find a version header like '## [7.2.0] - 2026-06-08' in {CHANGELOG_PATH}"
        )

    before = content[: match.start()].rstrip()
    after = content[match.start() :].lstrip()
    CHANGELOG_PATH.write_text(f"{before}\n\n{section.rstrip()}\n\n{after}")


def main() -> int:
    args = parse_args()

    try:
        commits = read_commits(args.start_commit, args.end_commit)
    except subprocess.CalledProcessError as exc:
        sys.stderr.write(exc.output)
        sys.stderr.write("Failed to read commit range.\n")
        return exc.returncode or 1

    grouped, ignored = group_commits(commits)
    if args.update_changelog:
        version = read_sdk_version()
        section = render_release_section(
            version, date.today(), filter_changelog_groups(grouped), ignored
        )
        update_changelog(section)
        print(f"Inserted changelog section for {version} into {CHANGELOG_PATH}.")
        return 0

    sys.stdout.write(
        render_markdown(args.start_commit, args.end_commit, grouped, ignored)
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
