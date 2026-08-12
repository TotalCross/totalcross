#!/usr/bin/env python3
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import os
from pathlib import Path
import re
import subprocess
import sys


TITLE_FORMAT = re.compile(
    r"^(fix|feat|refactor|perf|style|test|docs|build|ci|chore|revert)"
    r"(!\([A-Za-z0-9_-]+(,[a-z0-9_-]+)?\)|"
    r"\([A-Za-z0-9_-]+(,[a-z0-9_-]+)?\)!?): [a-z0-9 ].*$"
)
TITLE_CAPITALIZATION = re.compile(r"^[^A-Z]")
TITLE_WORD_COUNT = re.compile(r"^[^ ]+([ \t]+[^ ]+){2,}$", re.S)
TITLE_LENGTH = re.compile(r"^([^\n]{1,80})(\n.*)?$", re.S)
BLANK_LINE = re.compile(r"^[^\n]+(\n\n.+)?$", re.S)
MARKDOWN_LINK = re.compile(r"\[([^\]]*)\]\([^)]+\)")


def run(*args):
    return subprocess.check_output(args, text=True).strip()


def commit_range():
    event_name = os.environ["EVENT_NAME"]
    if event_name == "pull_request":
        head = os.environ["PR_HEAD_SHA"]
        base = run("git", "merge-base", os.environ["PR_BASE_SHA"], head)
        return base, head

    before = os.environ["PUSH_BEFORE"]
    after = os.environ["PUSH_AFTER"]
    if before == "0000000000000000000000000000000000000000":
        return "", after
    return before, after


def list_commits(base, head):
    if base:
        output = run("git", "rev-list", f"{base}..{head}")
    else:
        output = run("git", "rev-list", head)
    return [line for line in output.splitlines() if line]


def read_message(commit):
    return subprocess.check_output(
        ["git", "show", "-s", "--format=%B", commit],
        text=True,
    ).rstrip("\n")


def body_line_length(line):
    return len(MARKDOWN_LINK.sub(r"\1", line))


def validate(commit, message):
    title = message.split("\n", 1)[0]
    failures = []
    warnings = []

    if not TITLE_FORMAT.match(title):
        failures.append("Invalid commit title format.")
    if not TITLE_CAPITALIZATION.match(title):
        failures.append("Commit title must not start with an uppercase letter.")
    if not TITLE_WORD_COUNT.match(title):
        failures.append("Commit title must contain at least 3 words.")
    if not TITLE_LENGTH.match(message):
        failures.append("Commit title must be 80 characters or less.")
    if title.endswith("."):
        failures.append("Commit title must not end with a period.")
    if not BLANK_LINE.match(message):
        failures.append(
            "If a commit body is present, it must be separated from the title by a blank line."
        )
    if any(body_line_length(line) > 80 for line in message.split("\n")[1:]):
        warnings.append(
            "Commit body lines should be 80 characters or less, "
            "excluding Markdown link URLs."
        )

    return title, failures, warnings


def validate_messages(entries):
    invalid = []
    warned = []
    for commit, message in entries:
        title, failures, warnings = validate(commit, message)
        if warnings:
            warned.append((commit, title, warnings))
        if failures:
            invalid.append((commit, title, failures))

    if warned:
        print(f"Commit message warnings for {len(warned)} commit(s).\n")
        for commit, title, warnings in warned:
            print(f"- {commit} {title}")
            for warning in warnings:
                print(f"    - {warning}")
                print(f"::warning title={commit[:10]} {title}::{warning}")
            print()

    if invalid:
        print(f"Commit message validation failed for {len(invalid)} commit(s).\n")
        print("Commits outside the expected format:")
        for commit, title, failures in invalid:
            print(f"- {commit} {title}")
            for failure in failures:
                print(f"    - {failure}")
                print(f"::error title={commit[:10]} {title}::{failure}")
            print()
        return 1

    print(f"Validated {len(entries)} commit message(s).")
    return 0


def parse_args():
    parser = argparse.ArgumentParser(description="Validate TotalCross commit messages.")
    source = parser.add_mutually_exclusive_group()
    source.add_argument(
        "--commit",
        action="append",
        metavar="SHA",
        help="validate one commit; may be repeated",
    )
    source.add_argument(
        "--message-file",
        type=Path,
        help="validate one message from a local text file",
    )
    return parser.parse_args()


def main():
    args = parse_args()
    if args.commit:
        print(f"Validating {len(args.commit)} explicitly selected commit(s).")
        entries = [(commit, read_message(commit)) for commit in args.commit]
        return validate_messages(entries)

    if args.message_file:
        print(f"Validating commit message file: {args.message_file}")
        message = args.message_file.read_text(encoding="UTF-8").rstrip("\n")
        return validate_messages([("message-file", message)])

    event_name = os.environ["EVENT_NAME"]
    if event_name == "workflow_dispatch":
        head = os.environ["PUSH_AFTER"]
        commits = [head]
        range_label = head
    else:
        base, head = commit_range()
        commits = list_commits(base, head)
        range_label = f"{base}..{head}" if base else head

    print(f"Validating commit messages in range: {range_label}")
    print(f"Event: {event_name}")

    if not commits:
        print("No commits to validate.")
        return 0

    entries = [(commit, read_message(commit)) for commit in reversed(commits)]
    return validate_messages(entries)


if __name__ == "__main__":
    sys.exit(main())
