#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Run the official image-scroll benchmark bundle without local runtime injection."""

import argparse
import csv
import hashlib
import json
from pathlib import Path
import signal
import subprocess
import sys
import time
import zipfile


EXPECTED_JPEGS = 663
SCREEN_SPEC = "-1,-1,540,960"
SCREEN_ARGUMENT = "/scr " + SCREEN_SPEC
MASKS = (
    0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048,
    4096, 8192, 16384, 32768, 32799, 40991, 49183, 57375,
)
PREFETCH_PROFILES = ("off", "on")
ROUNDS = 5
SEED = 73001
EXPECTED_PROCESSES = len(MASKS) * len(PREFETCH_PROFILES) * ROUNDS
FIXTURE = "ImageScrollRealWorkloadBenchmarkApp"
FNV_OFFSET = 0xCBF29CE484222325
FNV_PRIME = 0x100000001B3


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
    require(manifest.get("benchmark") == "image-scroll", "unexpected benchmark manifest")
    require(manifest.get("datasetFileCount") == EXPECTED_JPEGS,
            "manifest datasetFileCount is not 663")
    require(tuple(manifest.get("masks", ())) == MASKS, "manifest mask matrix differs")
    require(tuple(manifest.get("prefetchProfiles", ())) == PREFETCH_PROFILES,
            "manifest prefetch matrix differs")
    require(manifest.get("rounds") == ROUNDS, "manifest rounds differs")
    require(manifest.get("seed") == SEED, "manifest seed differs")
    require(manifest.get("expectedProcessCount") == EXPECTED_PROCESSES,
            "manifest expectedProcessCount differs")
    require(manifest.get("screenArgument") == SCREEN_ARGUMENT,
            "manifest screen argument differs")
    return manifest


def jpeg_paths(corpus):
    return sorted(
        path for path in corpus.rglob("*")
        if path.is_file() and path.suffix.lower() in (".jpg", ".jpeg")
    )


def dataset_hash(corpus, paths):
    value = FNV_OFFSET
    for path in paths:
        relative = path.relative_to(corpus).as_posix().encode("utf-8")
        for byte in relative + b"\0":
            value = ((value ^ byte) * FNV_PRIME) & 0xFFFFFFFFFFFFFFFF
        with path.open("rb") as source:
            while True:
                chunk = source.read(16384)
                if not chunk:
                    break
                for byte in chunk:
                    value = ((value ^ byte) * FNV_PRIME) & 0xFFFFFFFFFFFFFFFF
    return f"{value:016x}"


def sha256_file(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def executable_path(bundle, manifest):
    name = manifest.get("executable")
    require(isinstance(name, str) and name, "manifest executable is missing")
    executable = bundle / name
    require(executable.is_file(), f"bundle executable not found: {executable}")
    return executable


def validate_bundle(bundle, manifest):
    corpus = bundle / "corpus"
    require(corpus.is_dir(), f"bundle corpus not found: {corpus}")
    all_files = sorted(path for path in corpus.rglob("*") if path.is_file())
    images = jpeg_paths(corpus)
    require(len(all_files) == EXPECTED_JPEGS and len(images) == EXPECTED_JPEGS,
            "bundle corpus must contain exactly 663 JPEG files and no extra files")
    require(dataset_hash(corpus, images) == manifest.get("datasetHash"),
            "bundle dataset hash differs from manifest")
    chime = bundle / "device" / "chime.mp3"
    require(chime.is_file() and chime.stat().st_size > 0,
            "official device/chime.mp3 is missing")
    require(sha256_file(chime) == manifest.get("chimeSha256"),
            "bundle chime differs from manifest")
    executable = executable_path(bundle, manifest)
    runtime = manifest.get("runtime")
    require(isinstance(runtime, str) and (bundle / runtime).is_file(),
            "bundle native runtime is missing")
    compile_hash = manifest.get("sdkJarSha256Compile")
    deploy_hash = manifest.get("sdkJarSha256Deploy")
    require(isinstance(compile_hash, str) and len(compile_hash) == 64,
            "compile SDK SHA-256 is missing")
    require(isinstance(deploy_hash, str) and len(deploy_hash) == 64,
            "deploy SDK SHA-256 is missing")
    require(compile_hash == deploy_hash == manifest.get("sdkJarSha256"),
            "compile/deploy SDK SHA-256 values differ")
    return corpus, images, dataset_hash(corpus, images), executable


def self_test(bundle, manifest, output):
    corpus, images, corpus_digest, executable = validate_bundle(bundle, manifest)
    output.mkdir(parents=True, exist_ok=True)
    marker = {
        "fixture": FIXTURE,
        "status": "PASS",
        "screenArgument": SCREEN_ARGUMENT,
        "datasetFileCount": len(images),
        "datasetHash": corpus_digest,
        "sdkJarSha256Compile": manifest["sdkJarSha256Compile"],
        "sdkJarSha256Deploy": manifest["sdkJarSha256Deploy"],
        "executable": str(executable.relative_to(bundle)),
        "runtime": manifest["runtime"],
    }
    (output / "self-test.json").write_text(
        json.dumps(marker, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    print("self-test passed,screen=540x960,corpus_jpegs=663,"
          f"sdk_jar_sha256={manifest['sdkJarSha256Compile']}")
    return corpus, images, corpus_digest


def parse_record(line):
    record = {}
    for field in line.split(","):
        key, separator, value = field.partition("=")
        if separator:
            record[key] = value
    return record


def tail(path, count=60):
    return "\n".join(path.read_text(encoding="utf-8", errors="replace").splitlines()[-count:])


def expected_run_dir(output, mask, prefetch, run):
    return output / "runs" / f"mask-{mask}-prefetch-{prefetch}-run-{run}"


def validate_run_artifacts(output, log_path, mask, prefetch, run, dataset_digest):
    lines = log_path.read_text(encoding="utf-8", errors="replace").splitlines()
    pass_records = [
        parse_record(line) for line in lines
        if line.startswith(f"fixture={FIXTURE},record=pass")
    ]
    summary_records = [
        parse_record(line) for line in lines
        if line.startswith(f"fixture={FIXTURE},record=summary")
    ]
    require(len(pass_records) == 1, f"{log_path.name} must contain one pass record")
    require(len(summary_records) == 1, f"{log_path.name} must contain one summary record")
    record = pass_records[0]
    summary_record = summary_records[0]
    expected = {
        "resolution": "540x960",
        "requested_mask": str(mask),
        "effective_mask": str(mask),
        "prefetch_profile": prefetch,
        "image_count": str(EXPECTED_JPEGS),
        "pass": "cold",
    }
    for key, value in expected.items():
        require(record.get(key) == value,
                f"{log_path.name} {key}={record.get(key)!r}, expected {value!r}")
    require(summary_record.get("overallPass") == "true",
            f"{log_path.name} did not report overallPass=true")
    require(summary_record.get("resolution") == "540x960",
            f"{log_path.name} summary resolution is not 540x960")
    require(summary_record.get("requested_mask") == str(mask)
            and summary_record.get("effective_mask") == str(mask),
            f"{log_path.name} summary mask mismatch")
    require(summary_record.get("prefetch_profile") == prefetch,
            f"{log_path.name} summary prefetch mismatch")
    if prefetch == "off":
        for key in ("prefetch_request_count", "prefetch_ready_count",
                    "prefetch_failed_count", "prefetch_not_prefetchable_count"):
            require(summary_record.get(key) == "0",
                    f"{log_path.name} {key} must be zero")
    else:
        require(summary_record.get("prefetch_request_count") == str(EXPECTED_JPEGS),
                f"{log_path.name} prefetch request count is not 663")
        require(summary_record.get("prefetch_ready_count") == str(EXPECTED_JPEGS - 3),
                f"{log_path.name} prefetch ready count is not 660")
        require(summary_record.get("prefetch_failed_count") == "0",
                f"{log_path.name} prefetch failed count is not zero")
        require(summary_record.get("prefetch_not_prefetchable_count") == "3",
                f"{log_path.name} prefetch not-prefetchable count is not 3")

    run_dir = expected_run_dir(output, mask, prefetch, run)
    for name in ("summary.json", "frames.csv", "counters.json", "memory.csv", "timeline.csv"):
        require((run_dir / name).is_file(), f"{run_dir / name} is missing")
    try:
        run_summary = json.loads((run_dir / "summary.json").read_text(encoding="utf-8"))
        environment = json.loads((output / "environment.json").read_text(encoding="utf-8"))
        counters = json.loads((run_dir / "counters.json").read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        raise BenchmarkFailure(f"invalid JSON artifact for {log_path.name}") from error
    require(run_summary.get("status") == "PASS", f"{run_dir}/summary.json is not PASS")
    require(run_summary.get("requestedMask") == mask
            and run_summary.get("effectiveMask") == mask,
            f"{run_dir}/summary.json mask mismatch")
    require(run_summary.get("prefetch") == prefetch,
            f"{run_dir}/summary.json prefetch mismatch")
    require(run_summary.get("imageCount") == EXPECTED_JPEGS,
            f"{run_dir}/summary.json image count mismatch")
    require(run_summary.get("frameCount", 0) > 1,
            f"{run_dir}/summary.json has no measured frames")
    require(environment.get("expectedLogicalWidth") == 540
            and environment.get("expectedLogicalHeight") == 960
            and environment.get("effectiveLogicalWidth") == 540
            and environment.get("effectiveLogicalHeight") == 960,
            f"{output}/environment.json is not 540x960")
    require(environment.get("datasetFileCount") == EXPECTED_JPEGS
            and environment.get("datasetHash") == dataset_digest,
            f"{output}/environment.json dataset mismatch")
    require(isinstance(counters, dict) and "features" in counters,
            f"{run_dir}/counters.json is invalid")
    return run_summary


def run_process(bundle, manifest, output, corpus_digest, mask, prefetch, run, label):
    executable = executable_path(bundle, manifest)
    logs = output / "logs"
    logs.mkdir(parents=True, exist_ok=True)
    log_path = logs / f"{label}.log"
    command = [
        str(executable),
        "/scr", SCREEN_SPEC,
        "--mode=benchmark",
        "--corpus=corpus",
        "--output=results",
        f"--image-optimization={mask}",
        f"--prefetch={prefetch}",
        f"--run={run}",
        f"--dataset-hash={corpus_digest}",
    ]
    with log_path.open("w", encoding="utf-8") as log:
        completed = subprocess.run(
            command, cwd=bundle, stdout=log, stderr=subprocess.STDOUT, text=True,
            check=False
        )
    if completed.returncode:
        if completed.returncode < 0:
            signal_name = signal.Signals(-completed.returncode).name
            reason = f"terminated by {signal_name}"
        else:
            reason = f"exited with code {completed.returncode}"
        print(f"{label} failed: {reason}; log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise BenchmarkFailure(f"{label} failed: {reason}")
    try:
        summary = validate_run_artifacts(
            output, log_path, mask, prefetch, run, corpus_digest
        )
    except BenchmarkFailure as error:
        print(f"{label} failed validation; log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise
    print(f"{label} passed,exit_code=0,resolution=540x960,"
          f"requested_mask={mask},effective_mask={mask},prefetch={prefetch},"
          f"artifacts={expected_run_dir(output, mask, prefetch, run)}")
    return summary


def lcg_permutation(round_number):
    combinations = list(range(len(MASKS) * len(PREFETCH_PROFILES)))
    state = (SEED + 0x9E3779B97F4A7C15 * (round_number + 1)) & 0xFFFFFFFFFFFFFFFF
    for index in range(len(combinations) - 1, 0, -1):
        state = (state * 6364136223846793005 + 1442695040888963407) & 0xFFFFFFFFFFFFFFFF
        swap = (state >> 1) % (index + 1)
        combinations[index], combinations[swap] = combinations[swap], combinations[index]
    return tuple(
        (MASKS[combination // 2], PREFETCH_PROFILES[combination % 2])
        for combination in combinations
    )


def write_suite_plan(output):
    lines = ["round\torder\trun\tmask\tprefetch"]
    planned = []
    order = 0
    for round_number in range(ROUNDS):
        for mask, prefetch in lcg_permutation(round_number):
            run = round_number + 1
            lines.append(f"{run}\t{order}\t{run}\t{mask}\t{prefetch}")
            planned.append((round_number, order, run, mask, prefetch))
            order += 1
    require(len(planned) == EXPECTED_PROCESSES, "suite plan does not contain 210 processes")
    (output / "suite-plan.tsv").write_text("\n".join(lines) + "\n", encoding="utf-8")
    return planned


def require_smokes_completed(output, corpus_digest):
    marker = output / "self-test.json"
    require(marker.is_file(), "self-test marker is missing; refusing to run matrix")
    for mask, prefetch in ((0, "off"), (0, "on"), (32799, "off"), (32799, "on")):
        log_path = output / "logs" / f"smoke-{mask}-{prefetch}.log"
        require(log_path.is_file(), f"smoke log is missing: {log_path}")
        validate_run_artifacts(output, log_path, mask, prefetch, 0, corpus_digest)


def run_matrix(bundle, manifest, output, corpus_digest):
    plan = write_suite_plan(output)
    completed = 0
    for _, _, run, mask, prefetch in plan:
        run_process(
            bundle, manifest, output, corpus_digest, mask, prefetch, run,
            f"matrix-{run}-{mask}-{prefetch}",
        )
        completed += 1
        print(f"matrix progress={completed}/{EXPECTED_PROCESSES}")
    require(completed == EXPECTED_PROCESSES, "matrix did not complete 210 processes")
    return plan


def aggregate(output, plan):
    records = []
    for _, order, run, mask, prefetch in plan:
        path = expected_run_dir(output, mask, prefetch, run) / "summary.json"
        try:
            summary = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, ValueError) as error:
            raise BenchmarkFailure(f"invalid matrix summary: {path}") from error
        require(summary.get("status") == "PASS", f"invalid matrix status: {path}")
        require(summary.get("requestedMask") == mask
                and summary.get("effectiveMask") == mask,
                f"matrix mask mismatch: {path}")
        records.append({
            "order": order,
            "run": run,
            "prefetch": prefetch,
            "mask": mask,
            "status": summary["status"],
            "frame_count": summary["frameCount"],
            "frame_p50_ms": summary["frameTimeP50Ms"],
            "frame_p90_ms": summary["frameTimeP90Ms"],
            "frame_p95_ms": summary["frameTimeP95Ms"],
            "frame_p99_ms": summary["frameTimeP99Ms"],
            "frame_max_ms": summary["frameTimeMaxMs"],
            "frames_over_16_67_ms": summary["framesOver16_67Ms"],
            "frames_over_33_3_ms": summary["framesOver33_3Ms"],
            "frames_over_50_ms": summary["framesOver50Ms"],
            "frames_over_100_ms": summary["framesOver100Ms"],
            "largest_stall_ms": summary["largestStallMs"],
            "largest_consecutive_over_33_3": summary["largestConsecutiveOver33_3"],
            "prefetch_elapsed_ms": summary["prefetchElapsedMs"],
            "memory_peak_resident_bytes": summary["memoryPeakResidentBytes"],
        })
    require(len(records) == EXPECTED_PROCESSES, "aggregation did not find 210 summaries")
    rows = []
    for prefetch in PREFETCH_PROFILES:
        baseline = [record for record in records
                    if record["prefetch"] == prefetch and record["mask"] == 0]
        require(len(baseline) == ROUNDS, f"missing mask zero baseline for {prefetch}")
        baseline_p50 = sum(record["frame_p50_ms"] for record in baseline) // len(baseline)
        baseline_p95 = sum(record["frame_p95_ms"] for record in baseline) // len(baseline)
        for record in records:
            if record["prefetch"] != prefetch:
                continue
            row = dict(record)
            row["baseline_scope"] = "same-machine-same-prefetch-mask0"
            row["baseline_mask0_p50_ms"] = baseline_p50
            row["delta_p50_ms"] = record["frame_p50_ms"] - baseline_p50
            row["baseline_mask0_p95_ms"] = baseline_p95
            row["delta_p95_ms"] = record["frame_p95_ms"] - baseline_p95
            rows.append(row)
    fields = [
        "run", "prefetch", "mask", "status", "frame_count", "frame_p50_ms",
        "frame_p90_ms", "frame_p95_ms", "frame_p99_ms", "frame_max_ms",
        "frames_over_16_67_ms", "frames_over_33_3_ms", "frames_over_50_ms",
        "frames_over_100_ms", "largest_stall_ms", "largest_consecutive_over_33_3",
        "prefetch_elapsed_ms", "memory_peak_resident_bytes", "baseline_scope",
        "baseline_mask0_p50_ms", "delta_p50_ms", "baseline_mask0_p95_ms", "delta_p95_ms",
    ]
    path = output / "summary.csv"
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(sorted(rows, key=lambda row: (row["run"], row["prefetch"], row["mask"])))
    require(sum(1 for row in rows if row["mask"] == 0 and row["prefetch"] == "off") == ROUNDS,
            "aggregated off baselines are incomplete")
    require(sum(1 for row in rows if row["mask"] == 0 and row["prefetch"] == "on") == ROUNDS,
            "aggregated on baselines are incomplete")
    print(f"aggregation passed,summary={path},rows={len(rows)}")
    return path


def write_zip(bundle, output):
    archive = output / f"totalcross-image-benchmark-results-{int(time.time() * 1000)}.zip"
    with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED) as destination:
        for path in sorted(bundle.rglob("*")):
            if (path.is_file() and path != archive
                    and not path.name.startswith("totalcross-image-benchmark-results-")):
                destination.write(path, path.relative_to(bundle).as_posix())
    print(f"final ZIP={archive}")
    return archive


def run_phase(bundle, phase):
    manifest = load_manifest(bundle)
    output = bundle / "results"
    if phase in ("self-test", "full"):
        _, _, corpus_digest = self_test(bundle, manifest, output)
    else:
        _, images, corpus_digest, _ = validate_bundle(bundle, manifest)
        require((output / "self-test.json").is_file(),
                "self-test must pass before this phase")
        require(len(images) == EXPECTED_JPEGS, "corpus changed after self-test")
    if phase == "self-test":
        return
    if phase in ("smokes", "full"):
        for mask, prefetch in ((0, "off"), (0, "on"), (32799, "off"), (32799, "on")):
            run_process(
                bundle, manifest, output, corpus_digest, mask, prefetch, 0,
                f"smoke-{mask}-{prefetch}",
            )
        if phase == "smokes":
            return
    require_smokes_completed(output, corpus_digest)
    plan = run_matrix(bundle, manifest, output, corpus_digest)
    aggregate(output, plan)
    write_zip(bundle, output)


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--bundle", type=Path, default=Path.cwd(),
        help="bundle directory (defaults to the current directory)",
    )
    parser.add_argument(
        "--phase", choices=("self-test", "smokes", "matrix", "full"), default="full",
        help="run only the requested fail-fast phase; full runs everything",
    )
    args = parser.parse_args(argv[1:])
    bundle = args.bundle.expanduser().resolve()
    require(bundle.is_dir(), f"bundle directory not found: {bundle}")
    run_phase(bundle, args.phase)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except (BenchmarkFailure, OSError, ValueError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
