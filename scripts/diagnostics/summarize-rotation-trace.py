# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Summarize TotalCross Android rotation trace lines as compact JSON or CSV."""

import argparse
import csv
import json
import math
import re
import sys
from collections import Counter, defaultdict


TRACE_RE = re.compile(
    r"ROTATION_TRACE\s+generation=(?P<generation>-?\d+)\s+"
    r"stage=(?P<stage>[^\s]+)\s+ts_ns=(?P<ts_ns>\d+)\s+"
    r"width=(?P<width>-?\d+)\s+height=(?P<height>-?\d+)"
)

REQUIRED_STAGES = (
    "surface_callback_accepted",
    "resize_runnable_scheduled",
    "resize_runnable_started",
    "native_init_size_entered",
    "native_init_size_returned",
    "native_window_changed",
    "destroy_egl",
    "init_gles",
    "init_skia",
    "screen_changed_handled",
    "screen_change_entered",
    "screen_change_returned",
    "graphics_create_screen_surface",
    "repaint_active_windows",
    "first_egl_swap_buffers",
)


def percentile(values, fraction):
    if not values:
        return None
    ordered = sorted(values)
    index = max(0, min(len(ordered) - 1, math.ceil(fraction * len(ordered)) - 1))
    return ordered[index]


def read_traces(stream):
    generations = defaultdict(list)
    for line in stream:
        match = TRACE_RE.search(line)
        if match:
            record = match.groupdict()
            record["generation"] = int(record["generation"])
            record["ts_ns"] = int(record["ts_ns"])
            record["width"] = int(record["width"])
            record["height"] = int(record["height"])
            generations[record["generation"]].append(record)
    return generations


def summarize(generations):
    rows = []
    for generation in sorted(generations):
        records = generations[generation]
        stages = Counter(record["stage"] for record in records)
        first_ts = min(record["ts_ns"] for record in records)
        last_ts = max(record["ts_ns"] for record in records)
        first_frame = next(
            (record["ts_ns"] for record in records if record["stage"] == "first_egl_swap_buffers"),
            None,
        )
        rows.append(
            {
                "generation": generation,
                "width": records[-1]["width"],
                "height": records[-1]["height"],
                "duration_ms": round((last_ts - first_ts) / 1_000_000, 3),
                "first_frame_ms": None if first_frame is None else round((first_frame - first_ts) / 1_000_000, 3),
                "scheduled": stages["resize_runnable_scheduled"],
                "completed": int(first_frame is not None),
                "stale_tasks": stages["resize_runnable_stale"],
                "missing_stages": [stage for stage in REQUIRED_STAGES if not stages[stage]],
                "stage_counts": dict(sorted(stages.items())),
            }
        )

    first_frames = [row["first_frame_ms"] for row in rows if row["first_frame_ms"] is not None]
    return {
        "generations": rows,
        "aggregate": {
            "generations": len(rows),
            "completed": sum(row["completed"] for row in rows),
            "scheduled": sum(row["scheduled"] for row in rows),
            "stale_tasks": sum(row["stale_tasks"] for row in rows),
            "missing_stage_warnings": sum(bool(row["missing_stages"]) for row in rows),
            "first_frame_p50_ms": percentile(first_frames, 0.50),
            "first_frame_p95_ms": percentile(first_frames, 0.95),
        },
    }


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("trace", nargs="?", default="-", help="logcat file, or - for stdin")
    parser.add_argument("--format", choices=("json", "csv"), default="json")
    args = parser.parse_args(argv)

    stream = sys.stdin if args.trace == "-" else open(args.trace, encoding="utf-8")
    try:
        result = summarize(read_traces(stream))
    finally:
        if stream is not sys.stdin:
            stream.close()

    if args.format == "json":
        json.dump(result, sys.stdout, indent=2, sort_keys=True)
        sys.stdout.write("\n")
    else:
        fields = (
            "generation", "width", "height", "duration_ms", "first_frame_ms",
            "scheduled", "completed", "stale_tasks", "missing_stages",
        )
        writer = csv.DictWriter(sys.stdout, fieldnames=fields)
        writer.writeheader()
        for row in result["generations"]:
            output = {field: row[field] for field in fields}
            output["missing_stages"] = ";".join(row["missing_stages"])
            writer.writerow(output)


if __name__ == "__main__":
    main(sys.argv[1:])
