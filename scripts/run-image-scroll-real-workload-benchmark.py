#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Build, deploy, and run the real customer image-scroll workload."""

import argparse
import csv
import hashlib
from pathlib import Path
import platform
import shutil
import subprocess
import sys
import tempfile


EXPECTED_JPEGS = 663
RESOLUTIONS = ((480, 720, "480x720x24"), (540, 960, "540x960x24"))
PROFILES = ("disabled", "enabled")
PREFETCH_PROFILES = ("disabled", "all")
FIXTURE = "ImageScrollRealWorkloadBenchmarkApp"
MAX_PREFETCH_COLD_P95_MS = 17
MAX_PREFETCH_COLD_SLOW_FRAMES = 3
MAX_PREFETCH_COLD_MATERIALIZATIONS = 3


def jpeg_paths(directory):
    return sorted(
        path for path in Path(directory).rglob("*")
        if path.is_file() and path.suffix.lower() in (".jpg", ".jpeg")
    )


def run_command(command, cwd, log_path):
    log_path.parent.mkdir(parents=True, exist_ok=True)
    with log_path.open("w", encoding="utf-8") as log:
        result = subprocess.run(
            command, cwd=cwd, stdout=log, stderr=subprocess.STDOUT, text=True, check=False
        )
    if result.returncode:
        tail = log_path.read_text(encoding="utf-8", errors="replace").splitlines()[-40:]
        print(f"failed command ({result.returncode}): {' '.join(command)}", file=sys.stderr)
        print("\n".join(tail), file=sys.stderr)
        print(f"full log: {log_path}", file=sys.stderr)
        raise RuntimeError("benchmark command failed")


def parse_pass_records(log_path):
    records = []
    for line in log_path.read_text(encoding="utf-8", errors="replace").splitlines():
        if not line.startswith("fixture=" + FIXTURE + ",record=pass"):
            continue
        record = {}
        for field in line.split(","):
            key, separator, value = field.partition("=")
            if separator:
                record[key] = value
        records.append(record)
    return records


def parse_summary(log_path):
    summaries = []
    for line in log_path.read_text(encoding="utf-8", errors="replace").splitlines():
        if not line.startswith("fixture=" + FIXTURE + ",record=summary"):
            continue
        record = {}
        for field in line.split(","):
            key, separator, value = field.partition("=")
            if separator:
                record[key] = value
        summaries.append(record)
    return summaries


def integer(record, key, run_name):
    try:
        return int(record[key])
    except (KeyError, TypeError, ValueError) as error:
        raise RuntimeError(f"{run_name} has invalid {key}; see benchmark output") from error


def require_record(record, key, expected, run_name):
    if record.get(key) != str(expected):
        raise RuntimeError(f"{run_name} {key}={record.get(key)!r}, expected {expected}")


def validate_prefetch_record(record, run_name):
    pass_name = record.get("pass")
    if pass_name == "cold":
        require_record(record, "prefetch_request_count", EXPECTED_JPEGS, run_name)
        require_record(record, "prefetch_ready_count", EXPECTED_JPEGS - 3, run_name)
        require_record(record, "prefetch_failed_count", 0, run_name)
        require_record(record, "prefetch_not_prefetchable_count", 3, run_name)
        require_record(record, "targeted_jpeg_decodes", 0, run_name)
        require_record(record, "full_jpeg_decodes", 3, run_name)
        if integer(record, "frame_time_p95_ms", run_name) > MAX_PREFETCH_COLD_P95_MS:
            raise RuntimeError(f"{run_name} cold p95 exceeds {MAX_PREFETCH_COLD_P95_MS}ms")
        if integer(record, "frames_ge_34_ms", run_name) > MAX_PREFETCH_COLD_SLOW_FRAMES:
            raise RuntimeError(f"{run_name} cold frames >=34ms exceed {MAX_PREFETCH_COLD_SLOW_FRAMES}")
        if integer(record, "image_materializations", run_name) > MAX_PREFETCH_COLD_MATERIALIZATIONS:
            raise RuntimeError(f"{run_name} cold final materializations exceed {MAX_PREFETCH_COLD_MATERIALIZATIONS}")
        if integer(record, "native_geometry_materializations", run_name) > MAX_PREFETCH_COLD_MATERIALIZATIONS:
            raise RuntimeError(f"{run_name} cold native geometry materializations exceed {MAX_PREFETCH_COLD_MATERIALIZATIONS}")
    else:
        require_record(record, "targeted_jpeg_decodes", 0, run_name)
        require_record(record, "full_jpeg_decodes", 0, run_name)
        require_record(record, "image_materializations", 0, run_name)
        require_record(record, "native_geometry_materializations", 0, run_name)


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--image-dir", required=True, type=Path)
    parser.add_argument(
        "--tcvm-dylib", type=Path, default=Path("build-image-scroll-raster/libtcvm.dylib")
    )
    parser.add_argument("--output-dir", type=Path)
    parser.add_argument("--skip-build", action="store_true")
    args = parser.parse_args(argv[1:])

    if platform.system() != "Darwin":
        parser.error("the native macOS runner requires macOS")
    image_dir = args.image_dir.expanduser().resolve()
    if not image_dir.is_dir():
        parser.error(f"image directory not found: {image_dir}")
    images = jpeg_paths(image_dir)
    if len(images) != EXPECTED_JPEGS:
        parser.error(f"expected exactly {EXPECTED_JPEGS} JPEGs, found {len(images)} in {image_dir}")

    repo = Path(__file__).resolve().parent.parent
    dylib = (repo / args.tcvm_dylib).resolve() if not args.tcvm_dylib.is_absolute() else args.tcvm_dylib
    if not dylib.is_file():
        parser.error(f"native runtime not found: {dylib}")
    output_dir = args.output_dir.resolve() if args.output_dir else Path(
        tempfile.mkdtemp(prefix="totalcross-image-scroll-real-workload-")
    )
    output_dir.mkdir(parents=True, exist_ok=True)
    gradle = repo / "TotalCrossSDK/gradlew-agent"
    if not gradle.is_file():
        parser.error(f"Gradle wrapper not found: {gradle}")

    if not args.skip_build:
        run_command(
            ["bash", str(gradle), "dist", "-x", "test", "--no-daemon", "--console=plain"],
            repo / "TotalCrossSDK", output_dir / "sdk-dist.log",
        )
        run_command(
            ["bash", str(gradle), "jarImageScrollRealWorkloadBenchmark", "--no-daemon", "--console=plain"],
            repo / "TotalCrossSDK", output_dir / "fixture-jar.log",
        )
        run_command(
            ["bash", str(gradle), "deployImageScrollRealWorkloadBenchmarkMacOS", "--no-daemon", "--console=plain"],
            repo / "TotalCrossSDK", output_dir / "fixture-deploy.log",
        )

    install_dir = repo / "TotalCrossSDK/build/image-scroll-real-workload-benchmark/classes/install/macos"
    executable = install_dir / FIXTURE
    if not executable.is_file():
        raise RuntimeError(f"deployed benchmark executable not found: {executable}")
    deployed_dylib = install_dir / "libtcvm.dylib"
    shutil.copy2(dylib, deployed_dylib)
    source_hash = hashlib.sha256(dylib.read_bytes()).hexdigest()
    deployed_hash = hashlib.sha256(deployed_dylib.read_bytes()).hexdigest()
    if source_hash != deployed_hash:
        raise RuntimeError("deployed native runtime hash differs")

    all_records = []
    for width, height, screen_spec in RESOLUTIONS:
        for target_color in PROFILES:
            for variant_cache in PROFILES:
                for prefetch in PREFETCH_PROFILES:
                    run_name = (f"{width}x{height}-prefetch-{prefetch}-target-"
                                f"{target_color}-variant-{variant_cache}")
                    log_path = output_dir / f"{run_name}.log"
                    # The simulator notation `/scr WIDTHxHEIGHTx24` maps to the
                    # native desktop launcher's `/scr x,y,width,height` form.
                    command = [
                        str(executable),
                        "/scr", f"-2,-2,{width},{height}",
                        f"--image-dir={image_dir}",
                        f"--target-color={target_color}",
                        f"--variant-cache={variant_cache}",
                        f"--prefetch={prefetch}",
                    ]
                    run_command(command, install_dir, log_path)
                    records = parse_pass_records(log_path)
                    if len(records) != 3:
                        raise RuntimeError(f"{run_name} recorded {len(records)} passes; see {log_path}")
                    summaries = parse_summary(log_path)
                    if len(summaries) != 1 or summaries[0].get("overallPass") != "true":
                        raise RuntimeError(f"{run_name} did not report overallPass=true; see {log_path}")
                    for record in records:
                        if record.get("resolution") != f"{width}x{height}":
                            raise RuntimeError(f"{run_name} used unexpected resolution; see {log_path}")
                        if record.get("target_color_profile") != target_color:
                            raise RuntimeError(f"{run_name} used unexpected target-color profile; see {log_path}")
                        if record.get("variant_cache_profile") != variant_cache:
                            raise RuntimeError(f"{run_name} used unexpected variant-cache profile; see {log_path}")
                        if record.get("prefetch_profile") != prefetch:
                            raise RuntimeError(f"{run_name} used unexpected prefetch profile; see {log_path}")
                        if record.get("image_count") != str(EXPECTED_JPEGS):
                            raise RuntimeError(f"{run_name} used an unexpected corpus; see {log_path}")
                        if prefetch == "all":
                            validate_prefetch_record(record, run_name)
                        else:
                            require_record(record, "prefetch_request_count", 0, run_name)
                            require_record(record, "prefetch_ready_count", 0, run_name)
                            require_record(record, "prefetch_failed_count", 0, run_name)
                            require_record(record, "prefetch_not_prefetchable_count", 0, run_name)
                        record["screen_spec"] = screen_spec
                        record["run"] = run_name
                        all_records.append(record)

    by_key = {(record["resolution"], record["target_color_profile"],
               record["variant_cache_profile"], record["prefetch_profile"], record["pass"]): record
              for record in all_records}
    for record in all_records:
        if record["prefetch_profile"] != "all" or record["pass"] not in ("warm", "warm2"):
            continue
        baseline = by_key[(record["resolution"], record["target_color_profile"],
                          record["variant_cache_profile"], "disabled", record["pass"])]
        prefetch_p95 = integer(record, "frame_time_p95_ms", record["run"])
        baseline_p95 = integer(baseline, "frame_time_p95_ms", record["run"])
        if prefetch_p95 * 10 > baseline_p95 * 11:
            raise RuntimeError(f"{record['run']} warm p95 exceeds disabled baseline by more than 10%")

    results_path = output_dir / "results.csv"
    fields = sorted({key for record in all_records for key in record})
    with results_path.open("w", newline="", encoding="utf-8") as output:
        writer = csv.DictWriter(output, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(all_records)
    print(f"corpus_jpegs={len(images)}")
    print(f"native_runtime_sha256={deployed_hash}")
    print(f"results={results_path}")
    for record in all_records:
        print(
            f"run={record['run']} pass={record['pass']} frames={record['frames']} "
            f"elapsed_total_ms={record['elapsed_total_ms']} "
            f"prefetch_elapsed_ms={record['prefetch_elapsed_ms']} "
            f"prefetch_request_count={record['prefetch_request_count']} "
            f"prefetch_ready_count={record['prefetch_ready_count']} "
            f"frame_p95_ms={record['frame_time_p95_ms']} "
            f"targeted_jpeg_decodes={record['targeted_jpeg_decodes']} "
            f"full_jpeg_decodes={record['full_jpeg_decodes']} "
            f"image_materializations={record['image_materializations']} "
            f"native_geometry_materializations={record['native_geometry_materializations']} "
            f"prefetch_native_geometry_materializations={record['prefetch_native_geometry_materializations']} "
            f"draw_plan_cache_hits={record['draw_plan_cache_hits']} "
            f"write_pixels_hits={record['write_pixels_hits']} "
            f"smooth_resample_draws={record['smooth_resample_draws']} "
            f"target_color_converted_bytes={record['target_color_converted_bytes']} "
            f"physical_variant_bytes={record['physical_variant_bytes']} "
            f"physical_identity_reject_mapping={record['physical_identity_reject_mapping']} "
            f"physical_identity_reject_backing={record['physical_identity_reject_backing']}"
        )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except (OSError, RuntimeError, ValueError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
