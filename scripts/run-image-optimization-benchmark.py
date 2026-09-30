#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Run a deployed macOS image benchmark and collect compact timing/RSS data."""

import argparse
import csv
import os
import platform
from pathlib import Path
import select
import subprocess
import sys
import time


def rss_kb(pid):
    result = subprocess.run(
        ["ps", "-o", "rss=", "-p", str(pid)],
        check=False,
        capture_output=True,
        text=True,
    )
    values = result.stdout.strip().split()
    return int(values[0]) if result.returncode == 0 and values else None


def parse_sample(line):
    if not line.startswith("sample="):
        return None
    values = {}
    for field in line.strip().split(","):
        key, separator, value = field.partition("=")
        if not separator:
            continue
        try:
            values[key] = int(value)
        except ValueError:
            values[key] = value
    return values if "sample" in values else None


def host_memory_bytes():
    result = subprocess.run(
        ["sysctl", "-n", "hw.memsize"],
        check=False,
        capture_output=True,
        text=True,
    )
    return result.stdout.strip() if result.returncode == 0 else "unknown"


def repository_root():
    return Path(__file__).resolve().parent.parent


def resolve_revision(requested):
    revision = requested or "HEAD"
    result = subprocess.run(
        ["git", "-C", str(repository_root()), "rev-parse", "--verify", f"{revision}^{{commit}}"],
        check=False,
        capture_output=True,
        text=True,
    )
    resolved = result.stdout.strip()
    if result.returncode != 0 or not resolved:
        raise RuntimeError(f"could not resolve benchmark revision: {revision}")
    return resolved


def write_summary(path, executable, scenario, expected_samples, records, peak_rss, exit_code, revision):
    lines = [
        f"scenario={scenario}",
        f"revision={revision}",
        f"executable={os.path.abspath(executable)}",
        f"expected_samples={expected_samples}",
        f"recorded_samples={len(records)}",
        f"peak_rss_kb={peak_rss}",
        f"exit_code={exit_code}",
        f"host={platform.system()} {platform.release()}",
        f"macos={platform.mac_ver()[0] or 'unknown'}",
        f"cpu={platform.machine() or 'unknown'}",
        f"ram_bytes={host_memory_bytes()}",
        "correctness=process_exit_and_sample_count",
    ]
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    with open(path, "w", encoding="utf-8") as output:
        output.write("\n".join(lines) + "\n")


def run(args):
    executable = os.path.abspath(args.executable)
    if not os.path.isfile(executable):
        raise RuntimeError(f"benchmark executable not found: {executable}")
    revision = resolve_revision(args.revision)
    process = subprocess.Popen(
        [executable, f"--scenario={args.scenario}", f"--samples={args.samples}"],
        cwd=os.path.dirname(executable),
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,
    )
    start = time.monotonic()
    records = []
    rss_samples = []
    os.makedirs(os.path.dirname(os.path.abspath(args.log)), exist_ok=True)
    with open(args.log, "w", encoding="utf-8") as log:
        while True:
            now = time.monotonic()
            if now - (start + (len(rss_samples) * args.rss_interval)) >= 0:
                rss = rss_kb(process.pid)
                if rss is not None:
                    rss_samples.append(rss)

            ready = []
            if process.stdout is not None:
                ready, _, _ = select.select([process.stdout], [], [], 0.02)
            if ready:
                line = process.stdout.readline()
                if line:
                    log.write(line)
                    log.flush()
                    record = parse_sample(line)
                    if record is not None:
                        records.append(record)
            if process.poll() is not None:
                if process.stdout is not None:
                    for line in process.stdout:
                        log.write(line)
                        record = parse_sample(line)
                        if record is not None:
                            records.append(record)
                break

    exit_code = process.returncode
    if exit_code != 0:
        raise RuntimeError(f"benchmark exited with code {exit_code}; see {args.log}")
    if len(records) != args.samples:
        raise RuntimeError(
            f"benchmark recorded {len(records)} of {args.samples} samples; see {args.log}"
        )
    peak_rss = max(rss_samples, default=0)
    fields = sorted({key for record in records for key in record})
    fields.append("rss_peak_kb")
    os.makedirs(os.path.dirname(os.path.abspath(args.output)), exist_ok=True)
    with open(args.output, "w", newline="", encoding="utf-8") as output:
        writer = csv.DictWriter(
            output, fieldnames=fields, extrasaction="ignore", lineterminator="\n"
        )
        writer.writeheader()
        for record in records:
            record["rss_peak_kb"] = peak_rss
            writer.writerow(record)
    write_summary(args.summary, executable, args.scenario, args.samples, records, peak_rss, exit_code, revision)
    print(
        f"scenario={args.scenario} samples={len(records)} peak_rss_kb={peak_rss} "
        f"output={args.output} summary={args.summary}"
    )


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("executable")
    parser.add_argument("--scenario", required=True)
    parser.add_argument("--samples", type=int, default=60)
    parser.add_argument("--output", required=True)
    parser.add_argument("--log", required=True)
    parser.add_argument("--summary", required=True)
    parser.add_argument("--revision", help="commit to record; validated in the repository root")
    parser.add_argument("--rss-interval", type=float, default=0.05)
    args = parser.parse_args(argv[1:])
    if args.samples <= 0 or args.samples > 200:
        parser.error("--samples must be between 1 and 200")
    if args.rss_interval <= 0:
        parser.error("--rss-interval must be positive")
    try:
        run(args)
    except (OSError, RuntimeError, ValueError) as error:
        print(str(error), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
