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


def staged_entries():
    output = run_git(["diff", "--cached", "--name-status", "--diff-filter=ACMRT"])
    entries = []
    for line in output.splitlines():
        if not line:
            continue
        parts = line.split("\t")
        status = parts[0]
        path = parts[-1]
        entries.append((status, path))
    return entries


def should_check(path):
    if path in EXCLUDED_PATHS:
        return False
    path_obj = Path(path)
    if path_obj.suffix not in CHECK_EXTENSIONS:
        return False
    if any(path.startswith(prefix) for prefix in EXCLUDED_PREFIXES):
        return False
    if "/build/" in path or "/.gradle/" in path or path.endswith(".orig"):
        return False
    return True


def staged_content(path):
    return run_git(["show", f":{path}"])


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


def line_header(entries, token):
    lines = [f"{token} Copyright (C) {years} {owner}" for years, owner in entries]
    lines.append(f"{token}")
    lines.append(f"{token} {SPDX}")
    return "\n".join(lines) + "\n\n"


def html_header(entries):
    lines = ["<!--"]
    lines.extend(f"Copyright (C) {years} {owner}" for years, owner in entries)
    lines.append("")
    lines.append(SPDX)
    lines.append("-->")
    return "\n".join(lines) + "\n"


def desired_entries_from_creation(year, current_year):
    if 2000 <= year <= 2013:
        return [
            (format_year_range(year, 2013), SUPERWABA),
            ("2014-2021", TOTALCROSS),
            (format_year_range(2022, current_year), AMALGAM),
        ]
    if 2014 <= year <= 2021:
        return [
            (format_year_range(year, 2021), TOTALCROSS),
            (format_year_range(2022, current_year), AMALGAM),
        ]
    return [(format_year_range(year, current_year), AMALGAM)]


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


def desired_entries_from_existing(entries, current_year):
    if not entries:
        return []

    owners = {owner for _, _, owner in entries}
    if AMALGAM in owners and owners <= {AMALGAM}:
        start = min(start for start, _, owner in entries if owner == AMALGAM)
        return [(format_year_range(start, current_year), AMALGAM)]

    desired = []
    for start, end, owner in entries:
        if owner == TOTALCROSS:
            end = 2021
        elif owner == SUPERWABA and end > 2013:
            end = 2013
        elif owner == AMALGAM:
            end = current_year
        desired.append((format_year_range(start, end), owner))

    if TOTALCROSS in owners and AMALGAM not in owners:
        desired.append((format_year_range(2022, current_year), AMALGAM))

    if AMALGAM not in owners and TOTALCROSS not in owners:
        desired.append((str(current_year), AMALGAM))

    return desired


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


def top_header_extent(body, path):
    lines = body.splitlines(keepends=True)
    if not lines:
        return 0, []

    suffix = Path(path).suffix
    if suffix in {".md", ".html"} and lines[0].lstrip().startswith("<!--"):
        header = []
        for i, line in enumerate(lines):
            header.append(line)
            if "-->" in line:
                if i + 1 < len(lines) and lines[i + 1].strip() == "":
                    return i + 2, header
                return i + 1, header
        return 0, []

    token = line_comment_token(path)
    if token is None:
        return 0, []

    if not lines[0].lstrip().startswith(token):
        return 0, []

    header = []
    for i, line in enumerate(lines):
        stripped = line.lstrip()
        if stripped.startswith(token) or stripped.strip() == "":
            header.append(line)
            if SPDX in line:
                if i + 1 < len(lines) and lines[i + 1].strip() == "":
                    return i + 2, header
                return i + 1, header
            continue
        break
    return 0, []


def make_header(path, entries):
    token = line_comment_token(path)
    if token is None:
        return html_header(entries)
    return line_header(entries, token)


def corrected_content(path, status, content):
    current_year = datetime.date.today().year
    prefix, body = split_prefix(content, path)
    extent, header_lines = top_header_extent(body, path)
    existing_entries = parse_copyrights(header_lines)

    if status.startswith("A"):
        entries = [(str(current_year), AMALGAM)]
    elif existing_entries:
        entries = desired_entries_from_existing(existing_entries, current_year)
    else:
        entries = desired_entries_from_creation(creation_year(path, status), current_year)

    header = make_header(path, entries)
    if extent:
        remaining = "".join(body.splitlines(keepends=True)[extent:])
    else:
        remaining = body
    return prefix + header + remaining.lstrip("\n")


def unstaged_changes(path):
    result = subprocess.run(
        ["git", "diff", "--quiet", "--", path],
        check=False,
    )
    return result.returncode != 0


def validate_or_fix(fix):
    failures = []
    fixed = []
    fixed_unstaged = []
    checked = 0

    for status, path in staged_entries():
        if not should_check(path):
            continue
        checked += 1
        content = staged_content(path)
        corrected = corrected_content(path, status, content)
        if content == corrected:
            continue
        if not fix:
            failures.append(path)
            continue
        if unstaged_changes(path):
            worktree_content = Path(path).read_text()
            worktree_corrected = corrected_content(path, status, worktree_content)
            if worktree_content != worktree_corrected:
                Path(path).write_text(worktree_corrected)
            fixed_unstaged.append(path)
            continue
        Path(path).write_text(corrected)
        run_git(["add", "--", path])
        fixed.append(path)

    if failures:
        for path in failures:
            print(f"copyright header needs correction: {path}", file=sys.stderr)
        return 1

    if fix:
        print(f"Copyright header correction completed: {len(fixed) + len(fixed_unstaged)} fixed, {checked} checked.")
        if fixed_unstaged:
            print("Files corrected but not automatically added to the index:")
            for path in fixed_unstaged:
                print(f"  {path}")
    else:
        print(f"Copyright header validation passed for {checked} staged file(s).")
    return 0


def main():
    parser = argparse.ArgumentParser(description="Validate or fix copyright headers in staged files.")
    parser.add_argument("--fix", action="store_true", help="fix staged files and re-stage them")
    args = parser.parse_args()
    return validate_or_fix(args.fix)


if __name__ == "__main__":
    sys.exit(main())
