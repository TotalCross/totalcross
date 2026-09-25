#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Run fresh-process frame-pacing samples and atomically write stage evidence."""

import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import time

from frame_pacing_contract import (
    BenchmarkFailure, EXPECTED_IMAGES, EXPECTED_JPEG_PAYLOADS, EXPECTED_MASK,
    EXPECTED_PNG_PAYLOADS, FIXTURE, FNV_OFFSET, FNV_PRIME, PROCESS_TIMEOUT_SECONDS,
    ROUNDS, SCREEN_SPEC, STAGES, require,
)
from frame_pacing_results import (
    make_row, read_completion_record, read_frames, read_json, validate_preflight,
    validate_summary, write_evidence,
)


def sha256_file(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def image_paths(corpus):
    return sorted(path for path in corpus.rglob("*")
                  if path.is_file() and path.suffix.lower() in (".jpg", ".jpeg"))


def dataset_hash(corpus, paths):
    value = FNV_OFFSET
    for path in paths:
        relative = path.relative_to(corpus).as_posix().encode("utf-8")
        for byte in relative + b"\0":
            value = ((value ^ byte) * FNV_PRIME) & 0xFFFFFFFFFFFFFFFF
        with path.open("rb") as source:
            for chunk in iter(lambda: source.read(16384), b""):
                for byte in chunk:
                    value = ((value ^ byte) * FNV_PRIME) & 0xFFFFFFFFFFFFFFFF
    return f"{value:016x}"


def validate_bundle(bundle):
    bundle = Path(bundle).resolve()
    manifest_path = bundle / "manifest.json"
    require(manifest_path.is_file(), f"bundle manifest not found: {manifest_path}")
    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise BenchmarkFailure(f"cannot read bundle manifest: {error}") from error
    require(manifest.get("target") == "macos-arm64",
            "frame-pacing Part 1 requires a macos-arm64 bundle")
    source_commit = manifest.get("sourceCommit")
    require(isinstance(source_commit, str) and len(source_commit) >= 7
            and all(character in "0123456789abcdef" for character in source_commit.lower()),
            "manifest source commit is missing or invalid")
    executable_name = manifest.get("executable")
    runtime_name = manifest.get("runtime")
    require(isinstance(executable_name, str) and executable_name,
            "manifest executable is missing")
    require(isinstance(runtime_name, str) and runtime_name,
            "manifest runtime is missing")
    executable = bundle / executable_name
    runtime = bundle / runtime_name
    require(executable.is_file(), f"bundle executable not found: {executable}")
    require(runtime.is_file(), f"bundle runtime not found: {runtime}")
    runtime_identity = sha256_file(runtime)
    require(runtime_identity == manifest.get("runtimeSha256"),
            "bundle runtime SHA-256 differs from manifest")
    corpus = bundle / "corpus" / "imag"
    paths = image_paths(corpus)
    require(len(paths) == EXPECTED_IMAGES,
            f"bundle corpus contains {len(paths)} images; expected {EXPECTED_IMAGES}")
    jpeg_payloads = 0
    png_payloads = 0
    for path in paths:
        with path.open("rb") as source:
            magic = source.read(8)
        if magic.startswith(b"\xff\xd8\xff"):
            jpeg_payloads += 1
        elif magic.startswith(b"\x89PNG\r\n\x1a\n"):
            png_payloads += 1
    require(jpeg_payloads == EXPECTED_JPEG_PAYLOADS
            and png_payloads == EXPECTED_PNG_PAYLOADS,
            f"bundle corpus payloads are {jpeg_payloads} JPEG/{png_payloads} PNG")
    digest = dataset_hash(corpus, paths)
    require(digest == manifest.get("datasetHash"),
            "bundle corpus hash differs from manifest")
    return bundle, manifest, executable, digest, runtime_identity


def process_command(executable, bundle, output_dir, run, dataset_digest,
                    config, accounting):
    relative_output_dir = Path(output_dir).resolve().relative_to(
        Path(bundle).resolve()
    )
    return [
        str(executable), "/scr", SCREEN_SPEC, "-p", ".", "--app-root=.",
        "--mode=benchmark", "--corpus=corpus/imag",
        f"--output={relative_output_dir.as_posix()}",
        f"--image-optimization={EXPECTED_MASK}", "--prefetch=on",
        f"--accounting={accounting}", "--passes=1", f"--run={run}",
        f"--dataset-hash={dataset_digest}", "--prefetch-thread-mode=worker-semaphore",
        "--prefetch-worker-sleep-ms=0", "--duration=3000", *config.app_arguments(),
    ]


def run_process(bundle, executable, manifest, dataset_digest, runtime_identity,
                config, stage, sample, accounting, work_root, preflight=False):
    output_dir = (bundle / f"frame-pacing-results-{work_root.name}" / "outputs"
                  / f"{sample:02d}-{config.name}")
    output_dir.mkdir(parents=True, exist_ok=True)
    log_dir = work_root / "logs"
    log_dir.mkdir(parents=True, exist_ok=True)
    log_path = log_dir / f"{sample:02d}-{config.name}.log"
    command = process_command(executable, bundle, output_dir, sample, dataset_digest,
                              config, accounting)
    started_ns = time.monotonic_ns()
    try:
        with log_path.open("w", encoding="utf-8") as log:
            completed = subprocess.run(
                command, cwd=bundle, stdout=log, stderr=subprocess.STDOUT,
                check=False, timeout=PROCESS_TIMEOUT_SECONDS,
            )
    except subprocess.TimeoutExpired as error:
        raise BenchmarkFailure(
            f"{config.name} sample {sample} timed out; log={log_path}"
        ) from error
    except OSError as error:
        raise BenchmarkFailure(
            f"{config.name} sample {sample} failed to launch: {error}; log={log_path}"
        ) from error
    process_wall_ns = time.monotonic_ns() - started_ns
    if completed.returncode != 0:
        tail = "\n".join(log_path.read_text(encoding="utf-8", errors="replace")
                           .splitlines()[-40:])
        raise BenchmarkFailure(
            f"{config.name} sample {sample} exited {completed.returncode}; "
            f"log={log_path}\n{tail}"
        )
    summaries = sorted(output_dir.glob("runs/**/summary.json"))
    require(len(summaries) == 1,
            f"{config.name} sample {sample} produced {len(summaries)} run summaries")
    summary = read_json(summaries[0], "run summary")
    validate_summary(summary, stage, config, accounting, FIXTURE)
    completion = read_completion_record(log_path, FIXTURE)
    frame_rows = read_frames(summaries[0].parent / "frames.csv")
    if preflight:
        validate_preflight(completion, frame_rows)
    row = make_row(stage, config, sample, manifest, runtime_identity, summary,
                   process_wall_ns)
    return row, {
        "configuration": config.name,
        "summaryPath": str(summaries[0]),
        "frameRows": len(frame_rows),
        "prefetchRequestCount": int(completion["prefetch_request_count"]),
        "prefetchReadyCount": int(completion["prefetch_ready_count"]),
        "prefetchFailedCount": int(completion["prefetch_failed_count"]),
        "prefetchNotPrefetchableCount": int(completion["prefetch_not_prefetchable_count"]),
        "scrollJpegDecodes": sum(int(frame["scroll_jpeg_decode_count"])
                                  for frame in frame_rows),
        "scrollImageMaterializations": sum(
            int(frame["scroll_image_materializations"]) for frame in frame_rows
        ),
        "scrollNativeGeometryMaterializations": sum(
            int(frame["scroll_native_geometry_materializations"]) for frame in frame_rows
        ),
    }


def run_stage(args):
    require(args.stage in STAGES,
            f"stage {args.stage} is not implemented in Part 1 (available: 1, 2, 3)")
    require(args.rounds == ROUNDS,
            f"frame-pacing measurements require exactly {ROUNDS} rounds")
    bundle, manifest, executable, dataset_digest, runtime_identity = validate_bundle(args.bundle)
    configs = STAGES[args.stage]
    work_root = Path(tempfile.mkdtemp(prefix=f"frame-pacing-stage-{args.stage}-"))
    try:
        _, preflight = run_process(
            bundle, executable, manifest, dataset_digest, runtime_identity,
            configs[0], args.stage, 0, "on", work_root, preflight=True,
        )
        rows = []
        sample = 1
        for config in configs:
            for _ in range(args.rounds):
                row, _ = run_process(
                    bundle, executable, manifest, dataset_digest, runtime_identity,
                    config, args.stage, sample, "off", work_root,
                )
                rows.append(row)
                sample += 1
        write_evidence(args.evidence_csv, args.evidence_json, args.stage, args.rounds,
                       manifest, runtime_identity, preflight, rows)
    except BenchmarkFailure as error:
        raise BenchmarkFailure(f"{error}; executionDir={work_root}") from error
    print(f"status=PASS stage={args.stage} samples={len(rows)} preflight=PASS")
    print(f"csv={Path(args.evidence_csv).resolve()}")
    print(f"json={Path(args.evidence_json).resolve()}")
    print(f"executionDir={work_root}")


def parse_args(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--stage", type=int, choices=range(1, 6), required=True)
    parser.add_argument("--rounds", type=int, default=ROUNDS)
    parser.add_argument("--bundle", type=Path, required=True)
    parser.add_argument("--evidence-csv", type=Path, required=True)
    parser.add_argument("--evidence-json", type=Path, required=True)
    return parser.parse_args(argv)


def main(argv=None):
    try:
        run_stage(parse_args(argv))
    except BenchmarkFailure as error:
        print(f"frame-pacing benchmark failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
