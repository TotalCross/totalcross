# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Parse process artifacts, validate pacing rows, and write canonical evidence."""

import csv
import json
import os
from pathlib import Path
import tempfile

from frame_pacing_contract import (
    BenchmarkFailure, CSV_FIELDS, EXPECTED_IMAGES, EXPECTED_MASK,
    FRAME_FIELDS, PACING_SUMMARY_FIELDS, STAGES, SUMMARY_FIELDS, require,
)


def read_json(path, description):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise BenchmarkFailure(f"cannot read {description} {path}: {error}") from error


def read_frames(path):
    try:
        with path.open(newline="", encoding="utf-8") as source:
            reader = csv.DictReader(source)
            require(reader.fieldnames is not None, f"frame CSV is empty: {path}")
            missing = [field for field in FRAME_FIELDS if field not in reader.fieldnames]
            require(not missing, f"frame CSV lacks required columns: {', '.join(missing)}")
            rows = list(reader)
    except OSError as error:
        raise BenchmarkFailure(f"cannot read frame CSV {path}: {error}") from error
    require(rows, f"frame CSV has no frame rows: {path}")
    return rows


def read_completion_record(path, fixture):
    prefix = f"fixture={fixture},record=summary,"
    try:
        lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
    except OSError as error:
        raise BenchmarkFailure(f"cannot read process log {path}: {error}") from error
    records = [line for line in lines if line.startswith(prefix)]
    require(len(records) == 1, f"process log has {len(records)} completion records: {path}")
    record = {}
    for part in records[0].split(","):
        if "=" in part:
            key, value = part.split("=", 1)
            record[key] = value
    require(record.get("overallPass") == "true",
            f"benchmark completion record did not pass: {path}")
    return record


def validate_summary(summary, stage, config, accounting, fixture):
    require(summary.get("fixture") == fixture, "benchmark summary fixture mismatch")
    require(summary.get("status") == "PASS", "benchmark summary did not pass")
    require(summary.get("imageCount") == EXPECTED_IMAGES,
            "benchmark summary image count is not 663")
    require(summary.get("prefetch") == "on", "benchmark summary prefetch is not on")
    require(summary.get("prefetchThreadMode") == "worker-semaphore"
            and summary.get("prefetchWorkerSleepMs") == 0,
            "benchmark summary prefetch strategy is not worker-semaphore")
    require(summary.get("accounting") == accounting,
            f"benchmark summary accounting is not {accounting}")
    require(summary.get("requestedMask") == EXPECTED_MASK
            and summary.get("effectiveMask") == EXPECTED_MASK,
            "benchmark summary optimization mask is not 6")
    for field in SUMMARY_FIELDS:
        value = summary.get(field)
        require(isinstance(value, int) and value >= 0,
                f"benchmark summary field {field} is missing or invalid")
    require(summary["frameCount"] > 1 and summary["durationNs"] > 0,
            "benchmark summary has no measured frames or duration")
    for field in ("framesOver16_67Count", "framesOver20Count", "framesOver25Count",
                  "framesOver33_3Count", "framesOver50Count", "framesOver100Count"):
        require(summary[field] <= summary["frameCount"],
                f"benchmark summary field {field} exceeds frameCount")
    if stage == 1:
        expected_profile = config.synthetic_pacing_profile
        expected_interval = (16_000_000 if expected_profile == "synthetic-current-16ms"
                             else 16_666_667)
        require(summary.get("syntheticPacingProfile") == expected_profile,
                "synthetic pacing profile differs from configuration")
        require(summary.get("syntheticPacingIntervalNs") == expected_interval,
                "synthetic pacing interval differs from configuration")
        for field in PACING_SUMMARY_FIELDS:
            value = summary.get(field)
            require(isinstance(value, int) and value >= 0,
                    f"benchmark summary field {field} is missing or invalid")
        require(summary["sleepRequestCount"] > 0
                and summary["totalRequestedSleepNs"] > 0,
                "synthetic pacer did not record sleep requests")
    else:
        expected_timer_fps = config.timer_fps if config.timer_fps is not None else 0
        require(summary.get("driver") == config.driver,
                "Flick driver differs from configuration")
        require(summary.get("timerFps") == expected_timer_fps,
                "Flick timer fps differs from configuration")
        require(summary.get("clock") == config.clock,
                "Flick clock differs from configuration")
        require(summary.get("timerDeadlinePolicy") == config.timer_deadline_policy,
                "timer deadline policy differs from configuration")
        require(summary.get("eventLoopPolicy") == config.event_loop_policy,
                "event loop policy differs from configuration")
        require(summary.get("yieldPolicy") == config.yield_policy,
                "event loop yield policy differs from configuration")
        require(summary.get("expectedCallbackIntervalNs")
                == config.expected_callback_interval_ns,
                "expected callback interval differs from configuration")
        callback_fields = (
            "callbackCount", "callbackDeltaP50Ns", "callbackDeltaP95Ns",
            "callbackDeltaP99Ns", "callbackDeltaMaxNs",
            "callbackAbsoluteLatenessP50Ns", "callbackAbsoluteLatenessP95Ns",
            "callbackAbsoluteLatenessP99Ns", "callbackAbsoluteLatenessMaxNs",
            "callbackDeltaErrorP50Ns", "callbackDeltaErrorP95Ns",
            "callbackDeltaErrorP99Ns", "callbackDeltaErrorMaxNs",
        )
        for field in callback_fields:
            require(isinstance(summary.get(field), int) and summary[field] >= 0,
                    f"benchmark summary field {field} is missing or invalid")
        require(summary.get("callbackCount", 0) > 1,
                "Flick driver did not record enough callbacks")


def validate_preflight(completion, frame_rows):
    expected = {
        "prefetch_request_count": EXPECTED_IMAGES,
        "prefetch_ready_count": EXPECTED_IMAGES,
        "prefetch_failed_count": 0,
        "prefetch_not_prefetchable_count": 0,
    }
    for field, wanted in expected.items():
        try:
            actual = int(completion.get(field, ""))
        except ValueError as error:
            raise BenchmarkFailure(f"preflight {field} is missing or invalid") from error
        require(actual == wanted,
                f"preflight {field} was {actual}; expected {wanted}")
    for field in FRAME_FIELDS:
        try:
            total = sum(int(row[field]) for row in frame_rows)
        except (TypeError, ValueError) as error:
            raise BenchmarkFailure(f"preflight frame field {field} is invalid") from error
        require(total == 0, f"preflight scroll-time {field} was {total}; expected zero")


def make_row(stage, config, sample, manifest, runtime_identity, summary, process_wall_ns):
    row = {
        "stage": stage,
        "configuration": config.name,
        "sample": sample,
        "sourceCommit": manifest.get("sourceCommit"),
        "runtimeIdentity": runtime_identity,
        "frameCount": summary["frameCount"],
        "callbackCount": summary.get("callbackCount", 0),
        "frameIntervalP50Ns": summary["frameTimeP50Ns"],
        "frameIntervalP95Ns": summary["frameTimeP95Ns"],
        "frameIntervalP99Ns": summary["frameTimeP99Ns"],
        "frameIntervalMaxNs": summary["frameTimeMaxNs"],
        "activeWorkP50Ns": summary["workTimeP50Ns"],
        "activeWorkP95Ns": summary["workTimeP95Ns"],
        "activeWorkP99Ns": summary["workTimeP99Ns"],
        "activeWorkMaxNs": summary["workTimeMaxNs"],
        "paintP50Ns": summary["paintTimeP50Ns"],
        "paintP95Ns": summary["paintTimeP95Ns"],
        "paintP99Ns": summary["paintTimeP99Ns"],
        "paintMaxNs": summary["paintTimeMaxNs"],
        "measuredWallDurationNs": summary["durationNs"],
        "processWallNs": process_wall_ns,
        "prefetchThreadMode": summary.get("prefetchThreadMode"),
        "prefetchWorkerSleepMs": summary.get("prefetchWorkerSleepMs"),
        "syntheticPacingProfile": summary.get("syntheticPacingProfile"),
        "syntheticPacingIntervalNs": summary.get("syntheticPacingIntervalNs"),
    }
    for key in (
        "framesOver16_67Count", "framesOver20Count", "framesOver25Count",
        "framesOver33_3Count", "framesOver50Count", "framesOver100Count",
        "sleepRequestCount", "totalRequestedSleepNs", "totalActualSleepNs",
        "sleepOvershootP50Ns", "sleepOvershootP95Ns", "sleepOvershootP99Ns",
        "sleepOvershootMaxNs", "deadlineErrorP50Ns", "deadlineErrorP95Ns",
        "deadlineErrorP99Ns", "deadlineErrorMaxNs",
    ):
        row[key] = summary.get(key)
    callback_names = (
        "callbackDeltaP50Ns", "callbackDeltaP95Ns", "callbackDeltaP99Ns",
        "callbackDeltaMaxNs", "callbackAbsoluteLatenessP50Ns",
        "callbackAbsoluteLatenessP95Ns", "callbackAbsoluteLatenessP99Ns",
        "callbackAbsoluteLatenessMaxNs", "callbackDeltaErrorP50Ns",
        "callbackDeltaErrorP95Ns", "callbackDeltaErrorP99Ns",
        "callbackDeltaErrorMaxNs",
    )
    for name in callback_names:
        row[name] = summary.get(name)
    row.update(config.metadata())
    return {field: row.get(field) for field in CSV_FIELDS}


def write_evidence(csv_path, json_path, stage, rounds, manifest, runtime_identity,
                   preflight, rows):
    expected_rows = len(STAGES[stage]) * rounds
    require(len(rows) == expected_rows,
            f"evidence has {len(rows)} rows; expected {expected_rows}")
    csv_path = Path(csv_path)
    json_path = Path(json_path)
    csv_path.parent.mkdir(parents=True, exist_ok=True)
    json_path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "schemaVersion": 1,
        "stage": stage,
        "platform": "macos-arm64",
        "sourceCommit": manifest.get("sourceCommit"),
        "runtimeIdentity": runtime_identity,
        "rounds": rounds,
        "processCount": expected_rows,
        "preflight": preflight,
        "rows": rows,
    }
    temp_paths = []
    try:
        with tempfile.NamedTemporaryFile("w", encoding="utf-8", newline="",
                                         dir=csv_path.parent, delete=False) as target:
            temp_paths.append(Path(target.name))
            writer = csv.DictWriter(target, fieldnames=CSV_FIELDS, lineterminator="\n")
            writer.writeheader()
            writer.writerows(rows)
        with tempfile.NamedTemporaryFile("w", encoding="utf-8", newline="",
                                         dir=json_path.parent, delete=False) as target:
            temp_paths.append(Path(target.name))
            json.dump(payload, target, indent=2, sort_keys=True)
            target.write("\n")
        os.replace(temp_paths[0], csv_path)
        os.replace(temp_paths[1], json_path)
    finally:
        for path in temp_paths:
            if path.exists():
                path.unlink()
