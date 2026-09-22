#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Analyze the focused writePixels tail benchmark outputs."""

import argparse
import csv
import json
import math
from pathlib import Path
from statistics import mean


MASKS = (32795, 32799, 32827, 32831)
PAIRS = ((32795, 32799), (32827, 32831))
PREFETCH = ("off", "on")
PASS_NAMES = ("cold-forward", "warm-reverse", "warm-forward")
OUTLIER_THRESHOLDS = (
    ("P95", "workTimeP95Ns"),
    ("P99", "workTimeP99Ns"),
    ("MAX", "workTimeMaxNs"),
)
CSV_FIELDS = (
    "kind", "pass", "pair", "prefetch", "run", "accounting",
    "control_mask", "enabled_mask", "control_frame_count", "enabled_frame_count",
    "control_work_p50_ns", "enabled_work_p50_ns", "work_p50_delta_ns",
    "work_p50_delta_pct", "control_work_p95_ns", "enabled_work_p95_ns",
    "work_p95_delta_ns", "work_p95_delta_pct", "control_work_p99_ns",
    "enabled_work_p99_ns", "work_p99_delta_ns", "work_p99_delta_pct",
    "control_work_max_ns", "enabled_work_max_ns", "work_max_delta_ns",
    "work_max_delta_pct", "control_write_pixels_attempts",
    "enabled_write_pixels_attempts", "control_write_pixels_hits",
    "enabled_write_pixels_hits", "control_write_pixels_copied_bytes",
    "enabled_write_pixels_copied_bytes", "control_image_materializations",
    "enabled_image_materializations", "control_native_geometry_materializations",
    "enabled_native_geometry_materializations",
    "control_write_pixels_total_ns", "enabled_write_pixels_total_ns",
    "control_write_pixels_preparation_ns", "enabled_write_pixels_preparation_ns",
    "control_write_pixels_copy_ns", "enabled_write_pixels_copy_ns",
    "control_write_pixels_rgb565_conversion_ns",
    "enabled_write_pixels_rgb565_conversion_ns",
)
OUTLIER_FIELDS = (
    "kind", "pair", "prefetch", "run", "pass", "threshold", "raw_frames",
    "raw_summary", "frame_index", "scroll_value", "visible_control_path_hash",
    "work_time_ns", "scroll_work_ns", "paint_work_ns", "write_pixels_attempts",
    "write_pixels_hits", "write_pixels_fallbacks", "write_pixels_copied_bytes",
    "write_pixels_full_hits", "write_pixels_clipped_hits",
    "write_pixels_full_copied_bytes", "write_pixels_clipped_copied_bytes",
    "write_pixels_last_width", "write_pixels_last_height", "write_pixels_last_format",
    "write_pixels_total_ns", "write_pixels_preparation_ns", "write_pixels_copy_ns",
    "write_pixels_rgb565_conversion_ns", "write_pixels_preparation_work_fraction",
    "write_pixels_copy_work_fraction", "write_pixels_rgb565_work_fraction",
    "jpeg_decode_work_fraction", "remaining_paint_work_ns",
    "remaining_paint_work_fraction",
    "image_materializations", "native_geometry_materializations",
    "jpeg_decode_count", "jpeg_decode_ns", "target_color_attempts",
    "target_color_hits", "physical_variant_lookups", "physical_variant_hits",
    "physical_variant_misses", "backing_live_bytes", "backing_peak_bytes",
    "rgba8888_bytes", "rgb565_bytes", "control_match_method", "control_frame_index",
    "control_scroll_value", "control_visible_control_path_hash",
    "control_work_time_ns", "paired_work_delta_ns", "identity_equal",
    "write_pixels_active", "large_copy", "materialization_or_decode",
    "paint_dominant",
)


def integer(row, key):
    return int(row[key])


def percent(control, enabled):
    if control == 0:
        return ""
    return round((enabled - control) * 100.0 / control, 6)


def percentile(values, percent_value):
    ordered = sorted(values)
    if not ordered:
        return 0
    if percent_value <= 0:
        return ordered[0]
    if percent_value >= 100:
        return ordered[-1]
    index = math.ceil(len(ordered) * percent_value / 100.0) - 1
    return ordered[max(0, min(len(ordered) - 1, index))]


def correlation(left, right):
    if len(left) < 2 or len(left) != len(right):
        return ""
    left_mean = mean(left)
    right_mean = mean(right)
    numerator = sum((a - left_mean) * (b - right_mean)
                    for a, b in zip(left, right))
    denominator = math.sqrt(
        sum((a - left_mean) ** 2 for a in left)
        * sum((b - right_mean) ** 2 for b in right)
    )
    return "" if denominator == 0 else round(numerator / denominator, 6)


def run_directory(root, mask, prefetch, accounting, run, pass_name=None):
    directory = root / "runs" / (
        f"mask-{mask}-prefetch-{prefetch}-accounting-{accounting}-run-{run}"
    )
    if pass_name is not None:
        directory = directory / "passes" / pass_name
    if not directory.is_dir():
        raise RuntimeError(f"missing raw run directory: {directory}")
    return directory


def display_path(path):
    resolved = path.resolve()
    marker = "/results/"
    resolved_text = resolved.as_posix()
    if marker in resolved_text:
        return "results/" + resolved_text.split(marker, 1)[1]
    try:
        return str(resolved.relative_to(Path.cwd().resolve()))
    except ValueError:
        return str(resolved)


def load_cell(root, kind, mask, prefetch, accounting, run, pass_name=None):
    directory = run_directory(root, mask, prefetch, accounting, run, pass_name)
    frames_path = directory / "frames.csv"
    summary_path = directory / "summary.json"
    with frames_path.open(newline="", encoding="utf-8") as source:
        frames = list(csv.DictReader(source))
    summary = json.loads(summary_path.read_text(encoding="utf-8"))
    return {
        "root": root,
        "kind": kind,
        "mask": mask,
        "prefetch": prefetch,
        "accounting": accounting,
        "run": run,
        "pass": pass_name or "cold",
        "directory": directory,
        "frames": frames,
        "summary": summary,
    }


def build_cells(root, kind, masks, accounting, runs, reuse):
    cells = {}
    passes = PASS_NAMES if reuse else (None,)
    for mask in masks:
        for prefetch in PREFETCH:
            for run in runs:
                for pass_name in passes:
                    cell = load_cell(root, kind, mask, prefetch, accounting, run,
                                     pass_name)
                    cells[(mask, prefetch, run, cell["pass"])] = cell
    return cells


def sum_frame_field(cell, field):
    if not cell["frames"] or field not in cell["frames"][0]:
        return ""
    values = [integer(row, field) for row in cell["frames"]]
    if not values or values[0] < 0:
        return ""
    return sum(values)


def summary_value(cell, field):
    return int(cell["summary"][field])


def optional_integer(row, key):
    return int(row[key]) if key in row else None


def timing_sum(cell, field):
    return sum_frame_field(cell, field)


def pairwise_rows(cells, kind, accounting, masks, runs, pass_names):
    rows = []
    pairs = PAIRS if kind in ("diagnostic", "reuse") else (PAIRS[0],)
    for control_mask, enabled_mask in pairs:
        for prefetch in PREFETCH:
            for run in runs:
                for pass_name in pass_names:
                    control = cells[(control_mask, prefetch, run, pass_name)]
                    enabled = cells[(enabled_mask, prefetch, run, pass_name)]
                    values = {}
                    for suffix, field in (("p50", "workTimeP50Ns"),
                                          ("p95", "workTimeP95Ns"),
                                          ("p99", "workTimeP99Ns"),
                                          ("max", "workTimeMaxNs")):
                        control_value = summary_value(control, field)
                        enabled_value = summary_value(enabled, field)
                        values[f"control_work_{suffix}_ns"] = control_value
                        values[f"enabled_work_{suffix}_ns"] = enabled_value
                        values[f"work_{suffix}_delta_ns"] = enabled_value - control_value
                        values[f"work_{suffix}_delta_pct"] = percent(
                            control_value, enabled_value)
                    rows.append({
                        "kind": kind,
                        "pass": pass_name,
                        "pair": f"{control_mask}->{enabled_mask}",
                        "prefetch": prefetch,
                        "run": run,
                        "accounting": accounting,
                        "control_mask": control_mask,
                        "enabled_mask": enabled_mask,
                        "control_frame_count": len(control["frames"]),
                        "enabled_frame_count": len(enabled["frames"]),
                        **values,
                        "control_write_pixels_attempts": sum_frame_field(
                            control, "write_pixels_attempts"),
                        "enabled_write_pixels_attempts": sum_frame_field(
                            enabled, "write_pixels_attempts"),
                        "control_write_pixels_hits": sum_frame_field(
                            control, "write_pixels_hits"),
                        "enabled_write_pixels_hits": sum_frame_field(
                            enabled, "write_pixels_hits"),
                        "control_write_pixels_copied_bytes": sum_frame_field(
                            control, "write_pixels_copied_bytes"),
                        "enabled_write_pixels_copied_bytes": sum_frame_field(
                            enabled, "write_pixels_copied_bytes"),
                        "control_image_materializations": sum_frame_field(
                            control, "image_materializations"),
                        "enabled_image_materializations": sum_frame_field(
                            enabled, "image_materializations"),
                        "control_native_geometry_materializations": sum_frame_field(
                            control, "native_geometry_materializations"),
                        "enabled_native_geometry_materializations": sum_frame_field(
                            enabled, "native_geometry_materializations"),
                        "control_write_pixels_total_ns": timing_sum(
                            control, "write_pixels_total_ns"),
                        "enabled_write_pixels_total_ns": timing_sum(
                            enabled, "write_pixels_total_ns"),
                        "control_write_pixels_preparation_ns": timing_sum(
                            control, "write_pixels_preparation_ns"),
                        "enabled_write_pixels_preparation_ns": timing_sum(
                            enabled, "write_pixels_preparation_ns"),
                        "control_write_pixels_copy_ns": timing_sum(
                            control, "write_pixels_copy_ns"),
                        "enabled_write_pixels_copy_ns": timing_sum(
                            enabled, "write_pixels_copy_ns"),
                        "control_write_pixels_rgb565_conversion_ns": timing_sum(
                            control, "write_pixels_rgb565_conversion_ns"),
                        "enabled_write_pixels_rgb565_conversion_ns": timing_sum(
                            enabled, "write_pixels_rgb565_conversion_ns"),
                    })
    return rows


def match_control(enabled_row, control_frames):
    enabled_index = integer(enabled_row, "frame_index")
    enabled_scroll = integer(enabled_row, "scroll_value")
    exact = [row for row in control_frames
             if integer(row, "frame_index") == enabled_index
             and integer(row, "scroll_value") == enabled_scroll]
    if exact:
        return exact[0], "exact_frame_position"
    match = min(
        control_frames,
        key=lambda row: (abs(integer(row, "scroll_value") - enabled_scroll),
                         abs(integer(row, "frame_index") - enabled_index)),
    )
    return match, "nearest_scroll_position"


def outlier_rows(enabled_cell, control_cell, pair, copy_threshold):
    rows = []
    for label, summary_field in OUTLIER_THRESHOLDS:
        threshold = summary_value(enabled_cell, summary_field)
        for frame in enabled_cell["frames"]:
            if integer(frame, "work_time_ns") != threshold:
                continue
            control_frame, match_method = match_control(frame, control_cell["frames"])
            copy_bytes = integer(frame, "write_pixels_copied_bytes")
            work_time = integer(frame, "work_time_ns")
            paint_work = integer(frame, "paint_work_ns")
            write_pixels_total = optional_integer(frame, "write_pixels_total_ns")
            write_pixels_preparation = optional_integer(frame, "write_pixels_preparation_ns")
            write_pixels_copy = optional_integer(frame, "write_pixels_copy_ns")
            rgb565_conversion = optional_integer(frame, "write_pixels_rgb565_conversion_ns")
            decode_ns = integer(frame, "scroll_jpeg_decode_ns") + integer(
                frame, "paint_jpeg_decode_ns")
            paint_write_pixels_total = optional_integer(frame, "paint_write_pixels_total_ns")
            remaining_paint = (max(0, paint_work - paint_write_pixels_total)
                               if paint_write_pixels_total is not None else None)
            rows.append({
                "kind": enabled_cell["kind"],
                "pair": pair,
                "prefetch": enabled_cell["prefetch"],
                "run": enabled_cell["run"],
                "pass": enabled_cell["pass"],
                "threshold": label,
                "raw_frames": display_path(enabled_cell["directory"] / "frames.csv"),
                "raw_summary": display_path(enabled_cell["directory"] / "summary.json"),
                "frame_index": frame["frame_index"],
                "scroll_value": frame["scroll_value"],
                "visible_control_path_hash": frame["visible_control_path_hash"],
                **{field: frame[field] for field in (
                    "work_time_ns", "scroll_work_ns", "paint_work_ns",
                    "write_pixels_attempts", "write_pixels_hits",
                    "write_pixels_fallbacks", "write_pixels_copied_bytes",
                    "write_pixels_full_hits", "write_pixels_clipped_hits",
                    "write_pixels_full_copied_bytes",
                    "write_pixels_clipped_copied_bytes", "write_pixels_last_width",
                    "write_pixels_last_height", "write_pixels_last_format",
                    "image_materializations", "native_geometry_materializations",
                    "jpeg_decode_count", "jpeg_decode_ns", "target_color_attempts",
                    "target_color_hits", "physical_variant_lookups",
                    "physical_variant_hits", "physical_variant_misses",
                    "backing_live_bytes", "backing_peak_bytes", "rgba8888_bytes",
                    "rgb565_bytes",
                )},
                "control_match_method": match_method,
                "control_frame_index": control_frame["frame_index"],
                "control_scroll_value": control_frame["scroll_value"],
                "control_visible_control_path_hash": control_frame[
                    "visible_control_path_hash"],
                "control_work_time_ns": control_frame["work_time_ns"],
                "paired_work_delta_ns": work_time
                - integer(control_frame, "work_time_ns"),
                "identity_equal": int(
                    frame["visible_control_path_hash"]
                    == control_frame["visible_control_path_hash"]
                ),
                "write_pixels_active": int(integer(frame, "write_pixels_hits") > 0),
                "large_copy": int(copy_bytes >= copy_threshold),
                "materialization_or_decode": int(
                    integer(frame, "image_materializations") > 0
                    or integer(frame, "native_geometry_materializations") > 0
                    or integer(frame, "jpeg_decode_count") > 0
                ),
                "paint_dominant": int(
                    integer(frame, "paint_work_ns") > integer(frame, "scroll_work_ns")
                ),
                "write_pixels_total_ns": write_pixels_total,
                "write_pixels_preparation_ns": write_pixels_preparation,
                "write_pixels_copy_ns": write_pixels_copy,
                "write_pixels_rgb565_conversion_ns": rgb565_conversion,
                "write_pixels_preparation_work_fraction": round(
                    write_pixels_preparation / work_time, 6)
                if write_pixels_preparation is not None and work_time else "",
                "write_pixels_copy_work_fraction": round(
                    write_pixels_copy / work_time, 6)
                if write_pixels_copy is not None and work_time else "",
                "write_pixels_rgb565_work_fraction": round(
                    rgb565_conversion / work_time, 6)
                if rgb565_conversion is not None and work_time else "",
                "jpeg_decode_work_fraction": round(
                    decode_ns / work_time, 6) if work_time else "",
                "remaining_paint_work_ns": remaining_paint,
                "remaining_paint_work_fraction": round(
                    remaining_paint / work_time, 6)
                if remaining_paint is not None and work_time else "",
            })
    return rows


def copy_stats(cells, masks):
    rows = []
    for mask in masks:
        for prefetch in PREFETCH:
            frames = []
            for run in range(1, 4):
                frames += cells[(mask, prefetch, run, "cold")]["frames"]
            copies = [integer(row, "write_pixels_copied_bytes") for row in frames]
            works = [integer(row, "work_time_ns") for row in frames]
            positive = [value for value in copies if value > 0]
            positive_work = [work for work, copy in zip(works, copies) if copy > 0]
            rows.append({
                "mask": mask,
                "prefetch": prefetch,
                "frames": len(frames),
                "copy_frames": sum(value > 0 for value in copies),
                "copy_bytes_total": sum(copies),
                "copy_bytes_p50": percentile(positive, 50),
                "copy_bytes_p95": percentile(positive, 95),
                "copy_bytes_max": max(positive, default=0),
                "work_p50_copy_frames": percentile(positive_work, 50),
                "work_p95_copy_frames": percentile(positive_work, 95),
                "copy_work_correlation": correlation(copies, works),
                "full_hits": sum(integer(row, "write_pixels_full_hits") for row in frames),
                "clipped_hits": sum(integer(row, "write_pixels_clipped_hits")
                                    for row in frames),
                "formats": ",".join(sorted({row["write_pixels_last_format"]
                                             for row in frames})),
                "rgb565_peak_bytes": max(
                    (integer(row, "rgb565_bytes") for row in frames), default=0),
                "rgba8888_peak_bytes": max(
                    (integer(row, "rgba8888_bytes") for row in frames), default=0),
            })
    return rows


def write_csv(path, fields, rows):
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--diagnostic-root", type=Path)
    parser.add_argument("--reuse-root", type=Path)
    parser.add_argument("--timing-root", type=Path)
    parser.add_argument("--direct-root", type=Path)
    parser.add_argument("--direct-reuse-root", type=Path)
    parser.add_argument("--control-root", type=Path)
    parser.add_argument("--sdk-zip", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    sdk_zip = args.sdk_zip.expanduser().resolve()
    output = args.output.expanduser().resolve()
    output.mkdir(parents=True, exist_ok=True)

    diagnostic = reuse = timing = timing_reuse = None
    if args.diagnostic_root and args.reuse_root and args.timing_root:
        diagnostic_root = args.diagnostic_root.expanduser().resolve()
        reuse_root = args.reuse_root.expanduser().resolve()
        timing_root = args.timing_root.expanduser().resolve()
        diagnostic = build_cells(diagnostic_root, "diagnostic", MASKS, "on",
                                 range(1, 4), False)
        reuse = build_cells(reuse_root, "reuse", MASKS, "on", range(1, 3), True)
        timing = build_cells(timing_root, "timing", (32795, 32799), "off",
                             range(1, 4), False)
        timing_reuse = build_cells(timing_root, "timing-reuse", (32795, 32799),
                                   "off", (1,), True)
    direct = None
    direct_reuse = None
    control_cells = None
    if args.direct_root and args.direct_reuse_root and args.control_root:
        direct_root = args.direct_root.expanduser().resolve()
        direct_reuse_root = args.direct_reuse_root.expanduser().resolve()
        control_root = args.control_root.expanduser().resolve()
        direct = build_cells(direct_root, "direct-timing", (32795, 32799),
                             "on", range(1, 3), False)
        direct_reuse = build_cells(direct_reuse_root, "direct-timing-reuse",
                                   (32795, 32799), "on", (1,), True)
        control_cells = build_cells(control_root, "direct-timing-control",
                                    (32795, 32799), "off", (1,), False)
    pair_rows = []
    if diagnostic is not None:
        pair_rows += pairwise_rows(diagnostic, "diagnostic", "on", MASKS,
                                   range(1, 4), ("cold",))
        pair_rows += pairwise_rows(reuse, "reuse", "on", MASKS, range(1, 3), PASS_NAMES)
        pair_rows += pairwise_rows(timing, "timing", "off", (32795, 32799),
                                   range(1, 4), ("cold",))
        pair_rows += pairwise_rows(timing_reuse, "timing-reuse", "off",
                                   (32795, 32799), (1,), PASS_NAMES)
    if direct is not None:
        pair_rows += pairwise_rows(direct, "direct-timing", "on",
                                   (32795, 32799), range(1, 3), ("cold",))
        pair_rows += pairwise_rows(direct_reuse, "direct-timing-reuse", "on",
                                   (32795, 32799), (1,), PASS_NAMES)
    write_csv(output / "pairwise.csv", CSV_FIELDS, pair_rows)

    outlier_rows_all = []
    historical_cells = () if diagnostic is None else (
        (diagnostic, "diagnostic", range(1, 4)),
        (reuse, "reuse", range(1, 3)),
    )
    for cells, kind, runs in historical_cells:
        for control_mask, enabled_mask in PAIRS:
            for prefetch in PREFETCH:
                for run in runs:
                    pass_names = ("cold",) if kind == "diagnostic" else PASS_NAMES
                    for pass_name in pass_names:
                        enabled = cells[(enabled_mask, prefetch, run, pass_name)]
                        control = cells[(control_mask, prefetch, run, pass_name)]
                        positive = [integer(row, "write_pixels_copied_bytes")
                                    for row in enabled["frames"]
                                    if integer(row, "write_pixels_copied_bytes") > 0]
                        threshold = percentile(positive, 95) if positive else 0
                        outlier_rows_all += outlier_rows(
                            enabled, control, f"{control_mask}->{enabled_mask}", threshold)
    if direct is not None:
        for cells, kind, runs in ((direct, "direct-timing", range(1, 3)),
                                  (direct_reuse, "direct-timing-reuse", (1,))):
            for prefetch in PREFETCH:
                for run in runs:
                    pass_names = ("cold",) if kind == "direct-timing" else PASS_NAMES
                    for pass_name in pass_names:
                        enabled = cells[(32799, prefetch, run, pass_name)]
                        disabled = cells[(32795, prefetch, run, pass_name)]
                        positive = [integer(row, "write_pixels_copied_bytes")
                                    for row in enabled["frames"]
                                    if integer(row, "write_pixels_copied_bytes") > 0]
                        threshold = percentile(positive, 95) if positive else 0
                        outlier_rows_all += outlier_rows(
                            enabled, disabled, "32795->32799", threshold)
    write_csv(output / "outliers.csv", OUTLIER_FIELDS, outlier_rows_all)

    scaling = copy_stats(diagnostic, (32799, 32831)) if diagnostic is not None else []
    direct_status = "disabled"
    if direct is not None:
        direct_status = (f"enabled,direct_cells={len(direct)},"
                         f"direct_reuse_cells={len(direct_reuse)},"
                         f"control_cells={len(control_cells)}")
    print(f"analysis passed,output={output},pairwise_rows={len(pair_rows)},"
          f"outlier_rows={len(outlier_rows_all)},direct_timing={direct_status}")


if __name__ == "__main__":
    main()
