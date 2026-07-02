# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import datetime
import os
import re
import subprocess
import sys
from pathlib import Path


AMALGAM = "Amalgam Solucoes em TI Ltda"
TOTALCROSS = "TotalCross Global Mobile Platform Ltda"
SUPERWABA = "SuperWaba Ltda"
SPDX = "SPDX-License-Identifier: LGPL-2.1-only"
ZERO_SHA = "0000000000000000000000000000000000000000"

CHECK_EXTENSIONS = {
    ".java",
    ".gradle",
    ".kt",
    ".c",
    ".h",
    ".cpp",
    ".cc",
    ".hpp",
    ".sh",
    ".md",
    ".html",
    ".yml",
    ".yaml",
    ".rb",
    ".py",
}

EXCLUDED_PREFIXES = (
    "TotalCrossVM/deps/",
    "build/",
)

EXCLUDED_PATHS = {
    ".agent/PLANS.md",
}

COPYRIGHT_RE = re.compile(r"Copyright \(C\) ([0-9]{4})(?:-([0-9]{4}))? (.+)$")


def run_git(args, check=True):
    result = subprocess.run(
        ["git"] + args,
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    if check and result.returncode != 0:
        raise RuntimeError(result.stderr.strip())
    return result.stdout


def list_files_from_environment():
    event_name = os.environ.get("EVENT_NAME") or os.environ.get("GITHUB_EVENT_NAME", "")

    if event_name == "pull_request" and os.environ.get("PR_BASE_SHA") and os.environ.get("PR_HEAD_SHA"):
        base_sha = os.environ["PR_BASE_SHA"]
        head_sha = os.environ["PR_HEAD_SHA"]
        base = run_git(["merge-base", base_sha, head_sha], check=False).strip() or base_sha
        return diff_name_status(base, head_sha)

    if os.environ.get("PUSH_AFTER"):
        after = os.environ["PUSH_AFTER"]
        before = os.environ.get("PUSH_BEFORE", "")
        if before and before != ZERO_SHA:
            return diff_name_status(before, after)
        return diff_tree_name_status(after)

    staged = diff_name_status("--cached")
    if staged:
        return staged
    return diff_name_status()


def diff_name_status(*args):
    output = run_git(["diff", "--name-status", "--diff-filter=ACMRT"] + list(args))
    return parse_name_status(output)


def diff_tree_name_status(commit):
    output = run_git(["diff-tree", "--no-commit-id", "--name-status", "--diff-filter=ACMRT", "-r", commit])
    return parse_name_status(output)


def parse_name_status(output):
    entries = []
    for line in output.splitlines():
        if not line:
            continue
        parts = line.split("\t")
        entries.append((parts[0], parts[-1]))
    return entries


def list_files(args):
    if args.files is not None:
        return [("M", path) for path in args.files]
    if args.commit:
        return diff_tree_name_status(args.commit)
    if args.base and args.head:
        return diff_name_status(args.base, args.head)
    return list_files_from_environment()


def should_check(path):
    if path in EXCLUDED_PATHS:
        return False
    path_obj = Path(path)
    if path_obj.suffix not in CHECK_EXTENSIONS:
        return False
    if not path_obj.is_file():
        return False
    if any(path.startswith(prefix) for prefix in EXCLUDED_PREFIXES):
        return False
    if "/build/" in path or "/.gradle/" in path or path.endswith(".orig"):
        return False
    return True


def creation_year(path, status):
    if status.startswith("A"):
        return datetime.date.today().year
    output = run_git(
        ["log", "--follow", "--diff-filter=A", "--format=%ad", "--date=format:%Y", "--", path],
        check=False,
    )
    years = [line.strip() for line in output.splitlines() if line.strip()]
    if years:
        return int(years[-1])
    return datetime.date.today().year


def line_comment_token(path):
    suffix = Path(path).suffix
    if suffix in {".md", ".html"}:
        return None
    if suffix in {".sh", ".py", ".rb", ".yml", ".yaml"}:
        return "#"
    return "//"


def format_year_range(start, end):
    return str(start) if start == end else f"{start}-{end}"


def desired_entries_from_creation(year, current_year):
    if 2000 <= year <= 2013:
        return [
            (year, 2013, SUPERWABA),
            (2014, 2021, TOTALCROSS),
            (2022, current_year, AMALGAM),
        ]
    if 2014 <= year <= 2021:
        return [
            (year, 2021, TOTALCROSS),
            (2022, current_year, AMALGAM),
        ]
    return [(year, current_year, AMALGAM)]


def split_prefix(content, path):
    lines = content.splitlines(keepends=True)
    prefix = []
    index = 0

    if lines and lines[0].startswith("#!"):
        prefix.append(lines[0])
        index = 1

    if Path(path).suffix in {".yml", ".yaml"} and index < len(lines) and lines[index].strip() == "---":
        prefix.append(lines[index])
        index += 1

    while index < len(lines) and lines[index].strip() == "":
        prefix.append(lines[index])
        index += 1

    return "".join(prefix), "".join(lines[index:])


def top_header_lines(body, path):
    lines = body.splitlines()
    if not lines:
        return []

    suffix = Path(path).suffix
    if suffix in {".md", ".html"} and lines[0].lstrip().startswith("<!--"):
        header = []
        for line in lines:
            header.append(line)
            if "-->" in line:
                return header
        return header

    token = line_comment_token(path)
    if token is None or not lines[0].lstrip().startswith(token):
        return []

    header = []
    for line in lines:
        stripped = line.lstrip()
        if stripped.startswith(token) or stripped.strip() == "":
            header.append(line)
            if SPDX in line:
                return header
            continue
        break
    return header


def parse_copyrights(header_lines):
    entries = []
    for line in header_lines:
        stripped = line.strip()
        stripped = stripped.lstrip("/#").strip()
        match = COPYRIGHT_RE.search(stripped)
        if match:
            start, end, owner = match.groups()
            entries.append((int(start), int(end or start), owner.strip().rstrip(".")))
    return entries


def expected_entries(path, status):
    return desired_entries_from_creation(creation_year(path, status), datetime.date.today().year)


def find_owner(entries, owner):
    return [(start, end) for start, end, entry_owner in entries if entry_owner == owner]


def explain_mismatch(path, status, actual, expected, has_spdx):
    current_year = datetime.date.today().year
    year = creation_year(path, status)
    reasons = []
    if not actual:
        reasons.append("missing copyright header")
    if not has_spdx:
        reasons.append(f"missing {SPDX}")

    if not actual:
        expected_text = ", ".join(f"{format_year_range(start, end)} {owner}" for start, end, owner in expected)
        reasons.append(f"expected [{expected_text}] based on creation year {year}")
        return reasons

    if status.startswith("A"):
        expected_new = [(current_year, current_year, AMALGAM)]
        if actual != expected_new:
            reasons.append(
                f"new files must use [{current_year} {AMALGAM}], found "
                f"[{', '.join(f'{format_year_range(start, end)} {owner}' for start, end, owner in actual)}]"
            )
        return reasons

    for start, end in find_owner(actual, SUPERWABA):
        if end != 2013:
            reasons.append(f"{SUPERWABA} copyright must end in 2013, found {format_year_range(start, end)}")

    totalcross_entries = find_owner(actual, TOTALCROSS)
    for start, end in totalcross_entries:
        if end != 2021:
            reasons.append(f"{TOTALCROSS} copyright must end in 2021, found {format_year_range(start, end)}")
        if 2014 <= year <= 2021 and len(actual) == 2 and start != year:
            reasons.append(f"{TOTALCROSS} copyright must start in creation year {year}, found {start}")

    amalgam_entries = find_owner(actual, AMALGAM)
    if totalcross_entries:
        if (2022, current_year) not in amalgam_entries:
            reasons.append(f"missing {AMALGAM} copyright range {format_year_range(2022, current_year)}")
    elif year >= 2022:
        if not any(start == year and end == current_year for start, end in amalgam_entries):
            reasons.append(f"{AMALGAM} copyright must be {format_year_range(year, current_year)}")
    elif find_owner(actual, SUPERWABA):
        reasons.append(f"missing {TOTALCROSS} copyright range 2014-2021")
        reasons.append(f"missing {AMALGAM} copyright range {format_year_range(2022, current_year)}")
    else:
        expected_text = ", ".join(f"{format_year_range(start, end)} {owner}" for start, end, owner in expected)
        reasons.append(f"expected [{expected_text}] based on creation year {year}")

    return reasons


def validate_file(status, path):
    content = Path(path).read_text()
    _, body = split_prefix(content, path)
    header_lines = top_header_lines(body, path)
    actual = parse_copyrights(header_lines)
    expected = expected_entries(path, status)
    has_spdx = any(SPDX in line for line in header_lines)
    return explain_mismatch(path, status, actual, expected, has_spdx)


def main():
    parser = argparse.ArgumentParser(description="Validate copyright headers in changed files.")
    parser.add_argument("base", nargs="?", help="base commit for changed-file validation")
    parser.add_argument("head", nargs="?", help="head commit for changed-file validation")
    parser.add_argument("--commit", help="validate files changed by one commit")
    parser.add_argument("--files", nargs="*", help="validate explicit files")
    args = parser.parse_args()

    if bool(args.base) != bool(args.head):
        parser.error("base and head must be provided together")

    failures = []
    checked = 0
    for status, path in list_files(args):
        if not should_check(path):
            continue
        checked += 1
        reasons = validate_file(status, path)
        if reasons:
            failures.append((path, reasons))

    if failures:
        print(f"Copyright header validation failed for {len(failures)} file(s):", file=sys.stderr)
        for path, reasons in failures:
            print(f"- {path}", file=sys.stderr)
            for reason in reasons:
                print(f"  - {reason}", file=sys.stderr)
        return 1

    print(f"Copyright header validation passed for {checked} file(s).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
