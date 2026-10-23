#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Summarize raw Xcode archive diagnostics without affecting build status."""

import argparse
from collections import Counter
import os
from pathlib import Path
import re

COMPILE = re.compile(r"^CompileC\s")
TARGET = re.compile(r"\(in target '([^']+)' from project '([^']+)'\)")


def summarize(content):
    lines = content.splitlines()
    groups = Counter()
    for line in lines:
        if line.strip() == "Build Timing Summary":
            break
        if not COMPILE.match(line):
            continue
        match = TARGET.search(line)
        if not match:
            groups["Unattributed"] += 1
        elif match.group(2) == "Pods":
            groups["CocoaPods"] += 1
        elif match.group(2) == "tcvm":
            groups["TCVM"] += 1
        else:
            groups["Application/other"] += 1

    timing = []
    for index, line in enumerate(lines):
        if line.strip() == "Build Timing Summary":
            for row in lines[index:index + 90]:
                if row.startswith("** ") and row.endswith(" **"):
                    break
                timing.append(row.rstrip())
            break

    report = ["## iOS Xcode archive diagnostics", "",
              "Compilation counts from the raw build log (not elapsed time):", "",
              "| Component | CompileC operations |", "| --- | ---: |"]
    for group in ("TCVM", "CocoaPods", "Application/other", "Unattributed"):
        report.append(f"| {group} | {groups[group]} |")
    report.append(f"| **Total** | **{sum(groups.values())}** |")
    report.extend(["", "### Xcode Build Timing Summary", ""])
    if timing:
        report.extend(["```text", *timing, "```"])
    else:
        report.append("Not found (for example, when archive fails before timing is emitted).")
    report.append("")
    return "\n".join(report)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--log", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    raw = args.log.read_text(encoding="utf-8", errors="replace") if args.log.is_file() else ""
    report = summarize(raw)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(report, encoding="utf-8")
    summary = os.getenv("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as handle:
            handle.write(report)
    print(report)


if __name__ == "__main__":
    main()
