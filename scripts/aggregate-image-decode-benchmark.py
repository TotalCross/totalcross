#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Aggregate image-decode per-image records without converting ns to ms."""

import argparse
import csv
import json
import math
from pathlib import Path
import sys


VARIANTS = (
    "imag", "lossless", "decode-baseline", "decode-fast",
    "aggresive-480", "aggresive-540",
)
SCENARIOS = (
    ("filesystem", "full", "sequential"),
    ("tcz", "full", "sequential"),
    ("tcz", "full", "random"),
    ("filesystem", "half", "sequential"),
    ("tcz", "half", "sequential"),
)
ROUNDS = 3
IMAGE_FIELDS = (
    "variant", "source", "order", "mode", "round", "image_id",
    "encoded_bytes", "format", "input_width", "input_height",
    "output_width", "output_height", "acquire_ns", "decode_ns",
    "total_ns", "status", "targeted_jpeg_decode_count",
    "targeted_jpeg_decode_denominator",
)
PROCESS_FIELDS = (
    "process_id", "variant", "source", "order", "mode", "round",
    "image_count", "failed_count", "encoded_bytes_total", "acquire_ns_total",
    "acquire_ns_median", "decode_ns_total", "decode_ns_median",
    "decode_ns_p50", "decode_ns_p90", "decode_ns_p95", "decode_ns_p99",
    "total_ns_total", "total_ns_median", "decoded_pixels",
    "decode_throughput_mp_s", "targeted_jpeg_decode_count",
    "targeted_jpeg_decode_denominator",
)
SUMMARY_FIELDS = (
    "variant", "source", "order", "mode", "round_count", "image_count",
    "variant_encoded_bytes_total", "encoded_size_delta_vs_imag_pct",
    "tcz_library_bytes", "acquire_ns_total", "acquire_ns_median",
    "decode_ns_total", "decode_ns_p50", "decode_ns_p90", "decode_ns_p95",
    "decode_ns_p99", "total_ns_total", "total_ns_median", "decoded_pixels",
    "decode_throughput_mp_s", "decode_p50_speedup_vs_imag",
    "targeted_jpeg_decode_count", "targeted_jpeg_decode_denominator",
)


class AggregationFailure(RuntimeError):
    pass


def require(condition, message):
    if not condition:
        raise AggregationFailure(message)


def percentile(values, fraction):
    ordered = sorted(values)
    require(ordered, "cannot calculate percentile from no values")
    return ordered[max(0, math.ceil(fraction * len(ordered)) - 1)]


def median_ns(values):
    ordered = sorted(values)
    require(ordered, "cannot calculate median from no values")
    middle = len(ordered) // 2
    if len(ordered) % 2:
        return ordered[middle]
    return (ordered[middle - 1] + ordered[middle]) // 2


def throughput_mp_s(rows):
    pixels = sum(int(row["output_width"]) * int(row["output_height"]) for row in rows)
    decode_ns = sum(int(row["decode_ns"]) for row in rows)
    if decode_ns <= 0:
        return ""
    return f"{pixels * 1000 / decode_ns:.6f}"


def integer(row, field):
    try:
        value = int(row[field])
    except (KeyError, TypeError, ValueError) as error:
        raise AggregationFailure(f"invalid integer {field} in image row") from error
    require(value >= 0, f"negative {field} in image row")
    return value


def load_plan(results):
    path = results / "decode-plan.csv"
    require(path.is_file(), f"decode plan is missing: {path}")
    with path.open(newline="", encoding="utf-8") as source:
        rows = list(csv.DictReader(source))
    require(len(rows) == 90, f"decode plan has {len(rows)} processes, expected 90")
    require(len({row["process_id"] for row in rows}) == 90,
            "decode plan contains duplicate process IDs")
    counts = {}
    for row in rows:
        key = (row["variant"], row["source"], row["mode"], row["order"])
        counts[key] = counts.get(key, 0) + 1
    expected = {
        (variant, source, mode, order)
        for variant in VARIANTS
        for source, mode, order in SCENARIOS
    }
    require(set(counts) == expected and all(count == ROUNDS for count in counts.values()),
            "decode plan scenarios or round counts differ")
    return rows


def load_process_rows(results, plan_row):
    path = results / "raw" / f"{plan_row['process_id']}.csv"
    require(path.is_file(), f"per-image CSV is missing: {path}")
    with path.open(newline="", encoding="utf-8") as source:
        reader = csv.DictReader(source)
        require(tuple(reader.fieldnames or ()) == IMAGE_FIELDS,
                f"unexpected per-image fields in {path}")
        rows = list(reader)
    require(len(rows) == 663,
            f"{plan_row['process_id']} has {len(rows)} rows, expected 663")
    expected_ids = (results / plan_row["order_file"]).read_text(encoding="utf-8").splitlines()
    require([row["image_id"] for row in rows] == expected_ids,
            f"image order differs in {plan_row['process_id']}")
    for row in rows:
        require(all(row[field] == plan_row[field] for field in (
            "variant", "source", "order", "mode", "round"
        )), f"process labels differ in {path}")
        for field in (
            "encoded_bytes", "input_width", "input_height", "output_width",
            "output_height", "acquire_ns", "decode_ns", "total_ns",
            "targeted_jpeg_decode_count", "targeted_jpeg_decode_denominator",
        ):
            integer(row, field)
        require(row["status"] == "ok", f"failed image row in {path}: {row['image_id']}")
        require(row["format"] not in ("", "unknown"),
                f"unknown image format in {path}: {row['image_id']}")
    return rows


def summarize_process(plan_row, rows):
    decode = [integer(row, "decode_ns") for row in rows]
    acquire = [integer(row, "acquire_ns") for row in rows]
    total = [integer(row, "total_ns") for row in rows]
    pixels = sum(integer(row, "output_width") * integer(row, "output_height") for row in rows)
    return {
        "process_id": plan_row["process_id"],
        "variant": plan_row["variant"],
        "source": plan_row["source"],
        "order": plan_row["order"],
        "mode": plan_row["mode"],
        "round": plan_row["round"],
        "image_count": len(rows),
        "failed_count": sum(row["status"] != "ok" for row in rows),
        "encoded_bytes_total": sum(integer(row, "encoded_bytes") for row in rows),
        "acquire_ns_total": sum(acquire),
        "acquire_ns_median": median_ns(acquire),
        "decode_ns_total": sum(decode),
        "decode_ns_median": median_ns(decode),
        "decode_ns_p50": percentile(decode, 0.50),
        "decode_ns_p90": percentile(decode, 0.90),
        "decode_ns_p95": percentile(decode, 0.95),
        "decode_ns_p99": percentile(decode, 0.99),
        "total_ns_total": sum(total),
        "total_ns_median": median_ns(total),
        "decoded_pixels": pixels,
        "decode_throughput_mp_s": throughput_mp_s(rows),
        "targeted_jpeg_decode_count": sum(
            integer(row, "targeted_jpeg_decode_count") for row in rows
        ),
        "targeted_jpeg_decode_denominator": sum(
            integer(row, "targeted_jpeg_decode_denominator") for row in rows
        ),
    }


def write_csv(path, fields, rows):
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def aggregate(bundle):
    results = bundle / "results" / "decode"
    plan = load_plan(results)
    all_rows = []
    process_rows = []
    variant_sizes = {variant: {} for variant in VARIANTS}
    for plan_row in plan:
        rows = load_process_rows(results, plan_row)
        all_rows.extend(rows)
        process_rows.append(summarize_process(plan_row, rows))
        image_sizes = variant_sizes[plan_row["variant"]]
        for row in rows:
            image_id = row["image_id"]
            size = integer(row, "encoded_bytes")
            require(image_sizes.setdefault(image_id, size) == size,
                    f"encoded size changed within {plan_row['variant']}: {image_id}")

    require(len(all_rows) == 90 * 663, "decode detail row total differs from 59,670")
    require(all(len(sizes) == 663 for sizes in variant_sizes.values()),
            "encoded size table is incomplete")
    variant_totals = {
        variant: sum(sizes.values()) for variant, sizes in variant_sizes.items()
    }
    manifest = json.loads((bundle / "manifest.json").read_text(encoding="utf-8"))
    libraries = manifest["decodeLibraries"]
    library_sizes = {
        variant: (bundle / name).stat().st_size for variant, name in libraries.items()
    }

    groups = {}
    for row in all_rows:
        key = (row["variant"], row["source"], row["order"], row["mode"])
        groups.setdefault(key, []).append(row)
    base_size = variant_totals["imag"]
    summaries = []
    for variant in VARIANTS:
        for source, mode, order in SCENARIOS:
            rows = groups[(variant, source, order, mode)]
            decode = [integer(row, "decode_ns") for row in rows]
            acquire = [integer(row, "acquire_ns") for row in rows]
            total = [integer(row, "total_ns") for row in rows]
            baseline = groups[("imag", source, order, mode)]
            baseline_p50 = percentile([integer(row, "decode_ns") for row in baseline], 0.50)
            this_p50 = percentile(decode, 0.50)
            pixels = sum(integer(row, "output_width") * integer(row, "output_height")
                         for row in rows)
            summaries.append({
                "variant": variant,
                "source": source,
                "order": order,
                "mode": mode,
                "round_count": ROUNDS,
                "image_count": len(rows),
                "variant_encoded_bytes_total": variant_totals[variant],
                "encoded_size_delta_vs_imag_pct": f"{(variant_totals[variant] / base_size - 1) * 100:.6f}",
                "tcz_library_bytes": library_sizes[variant],
                "acquire_ns_total": sum(acquire),
                "acquire_ns_median": median_ns(acquire),
                "decode_ns_total": sum(decode),
                "decode_ns_p50": this_p50,
                "decode_ns_p90": percentile(decode, 0.90),
                "decode_ns_p95": percentile(decode, 0.95),
                "decode_ns_p99": percentile(decode, 0.99),
                "total_ns_total": sum(total),
                "total_ns_median": median_ns(total),
                "decoded_pixels": pixels,
                "decode_throughput_mp_s": throughput_mp_s(rows),
                "decode_p50_speedup_vs_imag": f"{baseline_p50 / this_p50:.6f}"
                    if this_p50 > 0 else "",
                "targeted_jpeg_decode_count": sum(
                    integer(row, "targeted_jpeg_decode_count") for row in rows
                ),
                "targeted_jpeg_decode_denominator": sum(
                    integer(row, "targeted_jpeg_decode_denominator") for row in rows
                ),
            })

    write_csv(results / "process-summary.csv", PROCESS_FIELDS, process_rows)
    write_csv(results / "decode-detailed.csv", IMAGE_FIELDS, all_rows)
    write_csv(results / "decode-summary.csv", SUMMARY_FIELDS, summaries)
    print(
        "decode aggregation passed,processes=90,image_rows=59670,"
        f"summary_rows={len(summaries)},process_summary={results / 'process-summary.csv'},"
        f"summary={results / 'decode-summary.csv'}"
    )
    return process_rows, summaries


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bundle", type=Path, default=Path.cwd())
    args = parser.parse_args(argv[1:])
    bundle = args.bundle.expanduser().resolve()
    require(bundle.is_dir(), f"bundle directory not found: {bundle}")
    aggregate(bundle)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except (AggregationFailure, OSError, ValueError, KeyError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
