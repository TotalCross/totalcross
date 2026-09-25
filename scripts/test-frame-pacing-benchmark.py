#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Contract tests for frame pacing matrices, summaries, failures, and evidence."""

import csv
import importlib.util
import json
from pathlib import Path
import sys
import tempfile
from types import SimpleNamespace
from unittest import mock

import frame_pacing_contract as CONTRACT


RUNNER_PATH = Path(__file__).with_name("run-frame-pacing-benchmark.py")
SPEC = importlib.util.spec_from_file_location("frame_pacing_runner", RUNNER_PATH)
RUNNER = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
sys.modules[SPEC.name] = RUNNER
SPEC.loader.exec_module(RUNNER)


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def stage_one_summary(config, accounting="off"):
    summary = {
        "fixture": RUNNER.FIXTURE,
        "status": "PASS",
        "imageCount": RUNNER.EXPECTED_IMAGES,
        "prefetch": "on",
        "prefetchThreadMode": "worker-semaphore",
        "prefetchWorkerSleepMs": 0,
        "accounting": accounting,
        "requestedMask": RUNNER.EXPECTED_MASK,
        "effectiveMask": RUNNER.EXPECTED_MASK,
        "syntheticPacingProfile": config.synthetic_pacing_profile,
        "syntheticPacingIntervalNs": (
            16_000_000 if config.synthetic_pacing_profile == "synthetic-current-16ms"
            else 16_666_667
        ),
        "frameCount": 181,
        "durationNs": 3_002_000_000,
        "prefetchRequestCount": 663,
        "prefetchReadyCount": 663,
        "prefetchFailedCount": 0,
        "prefetchNotPrefetchableCount": 0,
    }
    for field in CONTRACT.SUMMARY_FIELDS:
        summary.setdefault(field, 0)
    summary.update({
        "frameTimeP50Ns": 16_000_000,
        "frameTimeP95Ns": 17_000_000,
        "frameTimeP99Ns": 18_000_000,
        "frameTimeMaxNs": 20_000_000,
        "workTimeP50Ns": 1_000_000,
        "workTimeP95Ns": 1_500_000,
        "workTimeP99Ns": 2_000_000,
        "workTimeMaxNs": 3_000_000,
        "paintTimeP50Ns": 800_000,
        "paintTimeP95Ns": 1_000_000,
        "paintTimeP99Ns": 1_200_000,
        "paintTimeMaxNs": 2_000_000,
        "sleepRequestCount": 180,
        "totalRequestedSleepNs": 2_900_000_000,
        "totalActualSleepNs": 3_000_000_000,
        "sleepOvershootP50Ns": 100_000,
        "sleepOvershootP95Ns": 200_000,
        "sleepOvershootP99Ns": 300_000,
        "sleepOvershootMaxNs": 400_000,
        "deadlineErrorP50Ns": 200_000,
        "deadlineErrorP95Ns": 400_000,
        "deadlineErrorP99Ns": 500_000,
        "deadlineErrorMaxNs": 600_000,
    })
    return summary


def test_exact_stage_matrices():
    expected = {
        1: ["synthetic-current-16ms", "synthetic-60hz"],
        2: ["timer-40-millis", "timer-60-millis", "update-millis"],
        3: ["timer-60-millis", "timer-60-nano", "update-millis", "update-nano"],
    }
    for stage, names in expected.items():
        require([config.name for config in RUNNER.STAGES[stage]] == names,
                f"stage {stage} matrix differs")
    require(len(RUNNER.STAGES[1]) * RUNNER.ROUNDS == 6,
            "stage 1 evidence row count differs")
    require(len(RUNNER.STAGES[2]) * RUNNER.ROUNDS == 9,
            "stage 2 evidence row count differs")
    require(len(RUNNER.STAGES[3]) * RUNNER.ROUNDS == 12,
            "stage 3 evidence row count differs")


def test_summary_contract():
    config = RUNNER.STAGES[1][0]
    summary = stage_one_summary(config)
    RUNNER.validate_summary(summary, 1, config, "off", RUNNER.FIXTURE)
    require(set(CONTRACT.SUMMARY_FIELDS).issubset(summary),
            "required timing fields are not represented")
    del summary["framesOver25Count"]
    try:
        RUNNER.validate_summary(summary, 1, config, "off", RUNNER.FIXTURE)
    except RUNNER.BenchmarkFailure as error:
        require("framesOver25Count" in str(error),
                "missing field failure did not identify the field")
    else:
        raise AssertionError("missing required summary field was accepted")


def test_flick_summary_matches_configuration():
    config = RUNNER.STAGES[2][0]
    summary = stage_one_summary(config)
    summary.update({
        "driver": config.driver,
        "timerFps": config.timer_fps,
        "clock": config.clock,
        "timerDeadlinePolicy": config.timer_deadline_policy,
        "eventLoopPolicy": config.event_loop_policy,
        "yieldPolicy": config.yield_policy,
        "expectedCallbackIntervalNs": config.expected_callback_interval_ns,
        "callbackCount": 181,
        "callbackDeltaP50Ns": 16_000_000,
        "callbackDeltaP95Ns": 17_000_000,
        "callbackDeltaP99Ns": 18_000_000,
        "callbackDeltaMaxNs": 20_000_000,
        "callbackAbsoluteLatenessP50Ns": 0,
        "callbackAbsoluteLatenessP95Ns": 1_000_000,
        "callbackAbsoluteLatenessP99Ns": 2_000_000,
        "callbackAbsoluteLatenessMaxNs": 3_000_000,
        "callbackDeltaErrorP50Ns": 1_000_000,
        "callbackDeltaErrorP95Ns": 2_000_000,
        "callbackDeltaErrorP99Ns": 3_000_000,
        "callbackDeltaErrorMaxNs": 4_000_000,
    })
    RUNNER.validate_summary(summary, 2, config, "off", RUNNER.FIXTURE)
    summary["clock"] = "nano"
    try:
        RUNNER.validate_summary(summary, 2, config, "off", RUNNER.FIXTURE)
    except RUNNER.BenchmarkFailure as error:
        require("clock differs" in str(error),
                "wrong clock failure did not identify the mismatch")
    else:
        raise AssertionError("a mismatched Flick clock was accepted")


def test_preflight_contract():
    completion = {
        "prefetch_request_count": "663",
        "prefetch_ready_count": "663",
        "prefetch_failed_count": "0",
        "prefetch_not_prefetchable_count": "0",
    }
    frames = [{field: "0" for field in CONTRACT.FRAME_FIELDS}]
    RUNNER.validate_preflight(completion, frames)
    frames[0]["scroll_image_materializations"] = "1"
    try:
        RUNNER.validate_preflight(completion, frames)
    except RUNNER.BenchmarkFailure as error:
        require("scroll_image_materializations" in str(error),
                "preflight failure did not identify the counter")
    else:
        raise AssertionError("scroll-time materialization was accepted")


def test_process_output_path_is_bundle_relative():
    with tempfile.TemporaryDirectory(prefix="frame-pacing-output-test-") as temp:
        bundle = Path(temp) / "bundle"
        bundle.mkdir()
        output_dir = bundle / "pacing-test" / "00"
        command = RUNNER.process_command(
            bundle / "app", bundle, output_dir, 1, RUNNER.STAGES[1][0], "off"
        )
        output_arguments = [argument for argument in command
                            if argument.startswith("--output=")]
        require(output_arguments == [
            "--output=pacing-test/00"
        ], "macOS launcher output path is not bundle-relative")
        for configs in RUNNER.STAGES.values():
            for config in configs:
                command = RUNNER.process_command(
                    bundle / "app", bundle,
                    bundle / "pacing-abcdefgh" / "99", 99, config, "off",
                )
                app_arguments = command[5:]
                require(len(" ".join(app_arguments)) <= 255,
                        f"{config.name} exceeds the native app command-line limit")
                require(app_arguments[-1] == config.app_arguments()[-1],
                        f"{config.name} pacing option is not retained last")


def test_failure_handling_does_not_launch_or_accept_failed_process():
    config = RUNNER.STAGES[1][0]
    with tempfile.TemporaryDirectory(prefix="frame-pacing-failure-test-") as temp:
        root = Path(temp)
        bundle = root / "bundle"
        bundle.mkdir()
        work = root / "work"
        def failed_process(_command, **kwargs):
            kwargs["stdout"].write("native process failed\n")
            return SimpleNamespace(returncode=7)
        with mock.patch.object(RUNNER.subprocess, "run", side_effect=failed_process):
            try:
                RUNNER.run_process(bundle, bundle / "app", {}, "runtime",
                                   config, 1, 1, "off", work)
            except RUNNER.BenchmarkFailure as error:
                require("exited 7" in str(error), "exit status was lost")
                require("native process failed" in str(error),
                        "failure log context was lost")
            else:
                raise AssertionError("nonzero native exit was accepted")

        with mock.patch.object(RUNNER.subprocess, "run",
                               side_effect=RUNNER.subprocess.TimeoutExpired("app", 180)):
            try:
                RUNNER.run_process(bundle, bundle / "app", {}, "runtime",
                                   config, 1, 1, "off", work)
            except RUNNER.BenchmarkFailure as error:
                require("timed out" in str(error), "timeout failure was lost")
            else:
                raise AssertionError("timed out native process was accepted")


def test_evidence_row_count_and_safe_failure():
    config = RUNNER.STAGES[1][0]
    summary = stage_one_summary(config)
    manifest = {"sourceCommit": "test-source"}
    rows = [
        RUNNER.make_row(1, config, sample, manifest, "runtime", summary, 1)
        for sample in range(1, 7)
    ]
    with tempfile.TemporaryDirectory(prefix="frame-pacing-evidence-test-") as temp:
        root = Path(temp)
        csv_path = root / "stage.csv"
        json_path = root / "stage.json"
        RUNNER.write_evidence(csv_path, json_path, 1, 3, manifest,
                              "runtime", {"status": "PASS"}, rows)
        with csv_path.open(newline="", encoding="utf-8") as source:
            output_rows = list(csv.DictReader(source))
        payload = json.loads(json_path.read_text(encoding="utf-8"))
        require(len(output_rows) == 6 and len(payload["rows"]) == 6,
                "canonical evidence row count differs")
        require(csv_path.stat().st_size < 20 * 1024
                and json_path.stat().st_size < 20 * 1024,
                "canonical stage evidence exceeds 20 KiB")
        csv_path.write_text("previous evidence\n", encoding="utf-8")
        try:
            RUNNER.write_evidence(csv_path, json_path, 1, 3, manifest,
                                  "runtime", {"status": "PASS"}, rows[:-1])
        except RUNNER.BenchmarkFailure:
            require(csv_path.read_text(encoding="utf-8") == "previous evidence\n",
                    "failed evidence validation overwrote the previous CSV")
        else:
            raise AssertionError("wrong evidence row count was accepted")


def main():
    require(RUNNER_PATH.stat().st_size < 20 * 1024,
            "frame-pacing runner exceeds the plan's 20 KiB limit")
    tests = (
        test_exact_stage_matrices,
        test_summary_contract,
        test_flick_summary_matches_configuration,
        test_preflight_contract,
        test_process_output_path_is_bundle_relative,
        test_failure_handling_does_not_launch_or_accept_failed_process,
        test_evidence_row_count_and_safe_failure,
    )
    for test in tests:
        test()
    print(f"frame-pacing benchmark contract tests passed: {len(tests)}")


if __name__ == "__main__":
    main()
