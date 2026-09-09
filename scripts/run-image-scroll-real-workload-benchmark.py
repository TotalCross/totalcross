#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Build, deploy, and run the real customer image-scroll workload."""

import argparse
import csv
import hashlib
import os
from pathlib import Path
import platform
import shutil
import subprocess
import sys
import tempfile


EXPECTED_JPEGS = 663
RESOLUTIONS = ((480, 720, "480x720x24"), (540, 960, "540x960x24"))
PROFILES = ("disabled", "enabled")
FIXTURE = "ImageScrollRealWorkloadBenchmarkApp"


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
            [str(gradle), "dist", "-x", "test", "--no-daemon", "--console=plain"],
            repo / "TotalCrossSDK", output_dir / "sdk-dist.log",
        )
        run_command(
            [str(gradle), "jarImageScrollRealWorkloadBenchmark", "--no-daemon", "--console=plain"],
            repo / "TotalCrossSDK", output_dir / "fixture-jar.log",
        )
        run_command(
            [str(gradle), "deployImageScrollRealWorkloadBenchmarkMacOS", "--no-daemon", "--console=plain"],
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
        for profile in PROFILES:
            run_name = f"{width}x{height}-{profile}"
            log_path = output_dir / f"{run_name}.log"
            command = [
                str(executable),
                "/scr", f"-2,-2,{width},{height}",
                f"--image-dir={image_dir}",
                f"--variant-cache={profile}",
            ]
            run_command(command, install_dir, log_path)
            records = parse_pass_records(log_path)
            if len(records) != 3:
                raise RuntimeError(f"{run_name} recorded {len(records)} passes; see {log_path}")
            for record in records:
                if record.get("resolution") != f"{width}x{height}":
                    raise RuntimeError(f"{run_name} used unexpected resolution; see {log_path}")
                if record.get("variant_cache_profile") != profile:
                    raise RuntimeError(f"{run_name} used unexpected cache profile; see {log_path}")
                if record.get("image_count") != str(EXPECTED_JPEGS):
                    raise RuntimeError(f"{run_name} used an unexpected corpus; see {log_path}")
                record["screen_spec"] = screen_spec
                record["run"] = run_name
                all_records.append(record)

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
            f"frame_p95_ms={record['frame_time_p95_ms']} "
            f"targeted_jpeg_decodes={record['targeted_jpeg_decodes']} "
            f"full_jpeg_decodes={record['full_jpeg_decodes']} "
            f"smooth_resample_draws={record['smooth_resample_draws']}"
        )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except (OSError, RuntimeError, ValueError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
