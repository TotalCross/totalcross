#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Run the distributed image-decode benchmark in fresh native processes."""

import argparse
import csv
import json
import math
from pathlib import Path
import signal
import subprocess
import sys
import time


EXPECTED_IMAGES = 663
ROUNDS = 3
SEED = 73001
EXPECTED_PROCESSES = 90
PROCESS_TIMEOUT_SECONDS = 180
SCREEN_SPEC = "-1,-1,540,960"
VARIANTS = (
    "imag", "lossless", "decode-baseline", "decode-fast",
    "aggresive-480", "aggresive-540",
)
LIBRARIES = {
    "imag": "DecodeImagLib.tcz",
    "lossless": "DecodeLosslessLib.tcz",
    "decode-baseline": "DecodeBaselineLib.tcz",
    "decode-fast": "DecodeFastLib.tcz",
    "aggresive-480": "DecodeAggresive480Lib.tcz",
    "aggresive-540": "DecodeAggresive540Lib.tcz",
}
SCENARIOS = (
    ("filesystem", "full", "sequential"),
    ("tcz", "full", "sequential"),
    ("tcz", "full", "random"),
    ("filesystem", "half", "sequential"),
    ("tcz", "half", "sequential"),
)
PLAN_FIELDS = (
    "process_id", "variant", "source", "order", "mode", "round", "order_file",
)
IMAGE_FIELDS = (
    "variant", "source", "order", "mode", "round", "image_id",
    "encoded_bytes", "format", "input_width", "input_height",
    "output_width", "output_height", "acquire_ns", "decode_ns",
    "total_ns", "status", "targeted_jpeg_decode_count",
    "targeted_jpeg_decode_denominator",
)
FNV_MASK = 0xFFFFFFFFFFFFFFFF


class BenchmarkFailure(RuntimeError):
    pass


def require(condition, message):
    if not condition:
        raise BenchmarkFailure(message)


def load_manifest(bundle):
    path = bundle / "manifest.json"
    require(path.is_file(), f"manifest not found: {path}")
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        raise BenchmarkFailure(f"invalid manifest: {path}") from error
    require(manifest.get("decodeImageCount") == EXPECTED_IMAGES,
            "manifest decode image count is not 663")
    require(manifest.get("decodeExpectedProcessCount") == EXPECTED_PROCESSES,
            "manifest decode process count is not 90")
    require(tuple(manifest.get("corpusVariants", ())) == VARIANTS,
            "manifest decode corpus variants differ")
    require(manifest.get("decodeLibraries") == LIBRARIES,
            "manifest decode library mapping differs")
    return manifest


def image_paths(folder):
    return sorted(
        path for path in folder.rglob("*")
        if path.is_file() and path.suffix.lower() == ".jpg"
    )


def validate_bundle(bundle, manifest):
    corpus = bundle / "corpus"
    require(corpus.is_dir(), f"corpus directory not found: {corpus}")
    dirs = tuple(sorted(path.name for path in corpus.iterdir() if path.is_dir()))
    require(dirs == tuple(sorted(VARIANTS)), "bundle must contain six decode variants")
    ids_by_variant = {}
    for variant in VARIANTS:
        root = corpus / variant
        files = sorted(path for path in root.rglob("*") if path.is_file())
        images = image_paths(root)
        require(len(files) == EXPECTED_IMAGES and len(images) == EXPECTED_IMAGES,
                f"{variant} must contain exactly 663 benchmark images")
        ids_by_variant[variant] = [path.relative_to(root).as_posix() for path in images]
    baseline_ids = ids_by_variant["imag"]
    for variant in VARIANTS[1:]:
        require(ids_by_variant[variant] == baseline_ids,
                f"{variant} relative paths differ from imag")

    for variant, library_name in LIBRARIES.items():
        library = bundle / library_name
        require(library.is_file() and library.stat().st_size > 0,
                f"decode library is missing: {library_name}")
    executable_name = manifest.get("decodeExecutable")
    require(isinstance(executable_name, str) and executable_name,
            "manifest decode executable is missing")
    executable = bundle / executable_name
    require(executable.is_file(), f"decode executable not found: {executable_name}")
    application_tcz = manifest.get("decodeApplicationTcz")
    require(application_tcz == "ImageDecodeBenchmarkApp.tcz"
            and (bundle / application_tcz).is_file(),
            "decode application TCZ is missing")
    return ids_by_variant, executable


def random_order(ids, round_number):
    values = list(ids)
    state = (SEED + 0x9E3779B97F4A7C15 * (round_number + 1)) & FNV_MASK
    for index in range(len(values) - 1, 0, -1):
        state = (state * 6364136223846793005 + 1442695040888963407) & FNV_MASK
        swap = (state >> 1) % (index + 1)
        values[index], values[swap] = values[swap], values[index]
    return values


def write_orders(results, ids):
    directory = results / "orders"
    directory.mkdir(parents=True, exist_ok=True)
    sequential = list(ids)
    randomized = {}
    for round_number in range(1, ROUNDS + 1):
        order = random_order(ids, round_number - 1)
        require(order != sequential, f"random order equals sequential in round {round_number}")
        require(order not in randomized.values(), "random orders repeat across rounds")
        randomized[round_number] = order
        path = directory / f"round-{round_number}-random.txt"
        path.write_text("\n".join(order) + "\n", encoding="utf-8")
    sequential_path = directory / "sequential.txt"
    sequential_path.write_text("\n".join(sequential) + "\n", encoding="utf-8")
    return {
        (round_number, "sequential"): sequential_path
        for round_number in range(1, ROUNDS + 1)
    } | {
        (round_number, "random"): directory / f"round-{round_number}-random.txt"
        for round_number in range(1, ROUNDS + 1)
    }


def process_id(variant, source, order, mode, round_number):
    return f"{variant}--{source}--{order}--{mode}--round-{round_number}"


def write_plan(results, ids):
    order_files = write_orders(results, ids)
    plan = []
    for variant in VARIANTS:
        for source, mode, order in SCENARIOS:
            for round_number in range(1, ROUNDS + 1):
                plan.append({
                    "process_id": process_id(variant, source, order, mode, round_number),
                    "variant": variant,
                    "source": source,
                    "order": order,
                    "mode": mode,
                    "round": round_number,
                    "order_file": order_files[(round_number, order)].relative_to(results).as_posix(),
                })
    require(len(plan) == EXPECTED_PROCESSES, "decode plan does not contain 90 processes")
    keys = [(row["variant"], row["source"], row["order"], row["mode"], row["round"])
            for row in plan]
    require(len(set(keys)) == EXPECTED_PROCESSES, "decode plan contains duplicate scenarios")
    counts = {}
    for row in plan:
        key = (row["variant"], row["source"], row["order"], row["mode"])
        counts[key] = counts.get(key, 0) + 1
    expected = {
        (variant, source, order, mode)
        for variant in VARIANTS
        for source, mode, order in SCENARIOS
    }
    require(set(counts) == expected and all(count == ROUNDS for count in counts.values()),
            "decode plan scenarios or round counts differ")
    path = results / "decode-plan.csv"
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=PLAN_FIELDS, lineterminator="\n")
        writer.writeheader()
        writer.writerows(plan)
    print("decode plan passed,processes=90,scenarios=30,rounds_per_scenario=3")
    return plan


def parse_csv_row(line):
    try:
        return next(csv.reader([line[len("DECODE_ROW,"):]]))
    except (csv.Error, StopIteration) as error:
        raise BenchmarkFailure("invalid DECODE_ROW record") from error


def validate_process_rows(job, rows, expected_ids):
    require(len(rows) == EXPECTED_IMAGES,
            f"{job['process_id']} returned {len(rows)} image rows, expected 663")
    require([row["image_id"] for row in rows] == expected_ids,
            f"{job['process_id']} returned a different image order")
    for row in rows:
        require(all(row[field] == str(job[field]) for field in (
            "variant", "source", "order", "mode", "round"
        )), f"{job['process_id']} returned mismatched labels")
        require(row["status"] == "ok", f"image failed: {row['image_id']}")
        require(row["format"] not in ("", "unknown"),
                f"unrecognized image format: {row['image_id']}")
        for field in (
            "encoded_bytes", "input_width", "input_height", "output_width",
            "output_height", "acquire_ns", "decode_ns", "total_ns",
            "targeted_jpeg_decode_count", "targeted_jpeg_decode_denominator",
        ):
            try:
                value = int(row[field])
            except (KeyError, ValueError) as error:
                raise BenchmarkFailure(f"invalid {field} in {job['process_id']}") from error
            require(value >= 0, f"negative {field} in {job['process_id']}")
        require(int(row["encoded_bytes"]) > 0 and int(row["input_width"]) > 0
                and int(row["input_height"]) > 0 and int(row["output_width"]) > 0
                and int(row["output_height"]) > 0,
                f"empty image result in {job['process_id']}")
        require(int(row["targeted_jpeg_decode_denominator"])
                == (1 if row["format"] == "jpeg" else 0),
                f"targeted JPEG denominator differs for {row['image_id']}")
        if job["mode"] == "full":
            require(row["input_width"] == row["output_width"]
                    and row["input_height"] == row["output_height"],
                    f"full decode changed dimensions for {row['image_id']}")


def parse_process_summary(output, job):
    records = [line for line in output.splitlines() if line.startswith("DECODE_PROCESS,")]
    require(len(records) == 1, f"{job['process_id']} did not emit one process summary")
    values = {}
    for field in records[0].split(",")[1:]:
        key, separator, value = field.partition("=")
        if separator:
            values[key] = value
    require(values.get("overallPass") == "true" and values.get("processed") == "663",
            f"{job['process_id']} process summary did not pass")
    require(values.get("constructor") == "Image-byte-array",
            "decode constructor marker missing")
    require(all(values.get(field) == str(job[field]) for field in (
        "variant", "source", "order", "mode", "round"
    )), f"{job['process_id']} process summary labels differ")
    return values


def tail(path, count=30):
    try:
        return "\n".join(path.read_text(encoding="utf-8", errors="replace").splitlines()[-count:])
    except OSError:
        return ""


def run_process(bundle, results, executable, job, output_root):
    log_dir = output_root / "logs"
    csv_dir = output_root / "raw"
    log_dir.mkdir(parents=True, exist_ok=True)
    csv_dir.mkdir(parents=True, exist_ok=True)
    log_path = log_dir / f"{job['process_id']}.log"
    csv_path = csv_dir / f"{job['process_id']}.csv"
    order_file = results / job["order_file"]
    command = [
        str(executable), "/scr", SCREEN_SPEC, "-p", str(bundle),
        f"--app-root={bundle}", f"--variant={job['variant']}",
        f"--source={job['source']}", f"--order={job['order']}",
        f"--decode-mode={job['mode']}", f"--round={job['round']}",
        f"--order-file=results/decode/{order_file.relative_to(results).as_posix()}",
    ]
    started = time.monotonic()
    try:
        completed = subprocess.run(
            command, cwd=bundle, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            text=True, encoding="utf-8", errors="replace", check=False,
            timeout=PROCESS_TIMEOUT_SECONDS,
        )
    except subprocess.TimeoutExpired as error:
        output = error.stdout or ""
        if isinstance(output, bytes):
            output = output.decode("utf-8", errors="replace")
        log_path.write_text(output + f"\nbenchmark_timeout_seconds={PROCESS_TIMEOUT_SECONDS}\n",
                            encoding="utf-8")
        raise BenchmarkFailure(
            f"{job['process_id']} timed out; log={log_path}\n{tail(log_path)}"
        ) from error
    duration = time.monotonic() - started
    log_path.write_text(completed.stdout, encoding="utf-8")
    if completed.returncode:
        reason = (f"terminated by {signal.Signals(-completed.returncode).name}"
                  if completed.returncode < 0 else f"exited with code {completed.returncode}")
        raise BenchmarkFailure(
            f"{job['process_id']} {reason}; log={log_path}\n{tail(log_path)}"
        )

    values = []
    for line in completed.stdout.splitlines():
        if line.startswith("DECODE_ROW,"):
            fields = parse_csv_row(line)
            require(len(fields) == len(IMAGE_FIELDS),
                    f"{job['process_id']} emitted an incomplete image row")
            values.append(fields)
    rows = [dict(zip(IMAGE_FIELDS, fields)) for fields in values]
    expected_ids = (results / job["order_file"]).read_text(encoding="utf-8").splitlines()
    validate_process_rows(job, rows, expected_ids)
    summary = parse_process_summary(completed.stdout, job)
    require(int(summary["targeted_jpeg_decode_count"]) == sum(
        int(row["targeted_jpeg_decode_count"]) for row in rows
    ), f"{job['process_id']} targeted decode counts differ")
    require(int(summary["targeted_jpeg_decode_denominator"]) == sum(
        int(row["targeted_jpeg_decode_denominator"]) for row in rows
    ), f"{job['process_id']} targeted decode denominator differs")
    with csv_path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=IMAGE_FIELDS, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    return duration, csv_path, log_path


def self_test(bundle, manifest):
    ids_by_variant, executable = validate_bundle(bundle, manifest)
    require(len(ids_by_variant["imag"]) == EXPECTED_IMAGES,
            "imag does not contain 663 image IDs")
    results = bundle / "results" / "decode"
    results.mkdir(parents=True, exist_ok=True)
    plan = write_plan(results, ids_by_variant["imag"])
    marker = {
        "status": "PASS",
        "variantCount": len(VARIANTS),
        "imagesPerVariant": EXPECTED_IMAGES,
        "plannedProcesses": len(plan),
        "roundsPerScenario": ROUNDS,
        "decodeExecutable": executable.name,
        "libraryNames": list(LIBRARIES.values()),
        "seed": SEED,
    }
    (results / "self-test.json").write_text(
        json.dumps(marker, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    print("decode self-test passed,variants=6,images_per_variant=663,processes=90")
    return manifest, results, executable, plan


def run_smokes(bundle, manifest, results, executable, plan):
    requested = {
        ("imag", "filesystem", "full", "sequential"),
        ("imag", "tcz", "full", "sequential"),
        ("imag", "tcz", "full", "random"),
        ("decode-fast", "filesystem", "half", "sequential"),
        ("decode-fast", "tcz", "half", "sequential"),
    }
    selected = [
        row for row in plan
        if (row["variant"], row["source"], row["mode"], row["order"]) in requested
        and row["round"] == 1
    ]
    require(len(selected) == 5, "decode smoke plan does not contain five scenarios")
    results_list = []
    smoke_root = results / "smokes"
    for job in selected:
        duration, csv_path, log_path = run_process(
            bundle, results, executable, job, smoke_root,
        )
        results_list.append({
            "process_id": job["process_id"],
            "variant": job["variant"],
            "source": job["source"],
            "order": job["order"],
            "mode": job["mode"],
            "round": job["round"],
            "image_count": EXPECTED_IMAGES,
            "duration_seconds": f"{duration:.3f}",
            "image_csv": str(csv_path.relative_to(bundle)),
            "process_log": str(log_path.relative_to(bundle)),
            "status": "PASS",
        })
        print(f"{job['process_id']} passed,images=663,"
              f"image_csv={csv_path.relative_to(bundle)},log={log_path.relative_to(bundle)}")
    summary_path = results / "smoke-summary.csv"
    with summary_path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=tuple(results_list[0]),
                                lineterminator="\n")
        writer.writeheader()
        writer.writerows(results_list)
    print(f"decode smokes passed,processes={len(results_list)},summary={summary_path}")


def run_matrix(bundle, results, executable, plan):
    marker = results / "self-test.json"
    require(marker.is_file(), "decode self-test must pass before the matrix")
    elapsed_process_seconds = 0.0
    for index, job in enumerate(plan, start=1):
        duration, _, _ = run_process(
            bundle, results, executable, job, results,
        )
        elapsed_process_seconds += duration
        if index >= 3:
            average = elapsed_process_seconds / index
            remaining = (len(plan) - index) * average
            print(
                f"decode progress={index}/{len(plan)},"
                f"average_process_seconds={average:.3f},"
                f"estimated_remaining_seconds={remaining:.1f}"
            )
    print("decode matrix passed,processes=90,images_per_process=663")


def run_aggregation(bundle):
    aggregator = bundle / "aggregate-image-decode-benchmark.py"
    require(aggregator.is_file(), f"decode aggregator is missing: {aggregator}")
    completed = subprocess.run(
        [sys.executable, str(aggregator), "--bundle", str(bundle)],
        cwd=bundle, check=False,
    )
    require(completed.returncode == 0,
            f"decode aggregation failed with exit code {completed.returncode}")


def run_phase(bundle, phase):
    manifest = load_manifest(bundle)
    if phase in ("self-test", "smokes", "full"):
        manifest, results, executable, plan = self_test(bundle, manifest)
    else:
        ids_by_variant, executable = validate_bundle(bundle, manifest)
        results = bundle / "results" / "decode"
        require((results / "self-test.json").is_file(),
                "decode self-test must pass before this phase")
        plan = write_plan(results, ids_by_variant["imag"])
    if phase == "self-test":
        return
    if phase == "smokes":
        run_smokes(bundle, manifest, results, executable, plan)
        return
    if phase == "matrix":
        run_matrix(bundle, results, executable, plan)
        return
    if phase == "aggregate":
        run_aggregation(bundle)
        return
    if phase == "full":
        run_matrix(bundle, results, executable, plan)
        run_aggregation(bundle)
        return
    raise BenchmarkFailure(f"unsupported decode phase: {phase}")


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bundle", type=Path, default=Path.cwd())
    parser.add_argument(
        "--phase", choices=("self-test", "smokes", "matrix", "aggregate", "full"),
        default="self-test",
    )
    args = parser.parse_args(argv[1:])
    bundle = args.bundle.expanduser().resolve()
    require(bundle.is_dir(), f"bundle directory not found: {bundle}")
    run_phase(bundle, args.phase)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except (BenchmarkFailure, OSError, ValueError, KeyError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
