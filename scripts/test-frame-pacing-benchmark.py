#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Contract tests for frame pacing matrices, summaries, failures, and evidence."""

import csv
import importlib.util
import json
from pathlib import Path
import re
import sys
import tempfile
from types import SimpleNamespace
from unittest import mock

import frame_pacing_contract as CONTRACT


RUNNER_PATH = Path(__file__).with_name("run-frame-pacing-benchmark.py")
WINDOWS_RUNNER_PATH = Path(__file__).with_name("run-frame-pacing-benchmark-windows.ps1")
WINDOWS_HELPER_PATH = Path(__file__).with_name("frame-pacing-benchmark-windows-functions.ps1")
PACKAGER_PATH = Path(__file__).with_name("package-image-scroll-benchmark.sh")
README_PATH = Path(__file__).with_name("README-image-benchmarks.md")
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
        "timerDeadlineMode": config.timer_deadline_mode,
        "eventLoopMode": config.event_loop_mode,
        "threadYieldMode": config.thread_yield_mode,
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
        4: ["timer-60-nano-relative", "timer-60-nano-absolute",
            "update-nano-relative", "update-nano-absolute"],
        5: ["poll-legacy-yield", "wait-legacy-yield", "wait-native-yield"],
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
    require(len(RUNNER.STAGES[4]) * RUNNER.ROUNDS == 12,
            "stage 4 evidence row count differs")
    require(len(RUNNER.STAGES[5]) * RUNNER.ROUNDS == 9,
            "stage 5 evidence row count differs")
    require(sum(len(configs) for configs in RUNNER.STAGES.values())
            * RUNNER.ROUNDS == 48,
            "complete frame-pacing matrix is not 48 processes")


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


def test_process_environment_sets_all_diagnostic_modes():
    config = RUNNER.STAGES[5][2]
    with mock.patch.dict("os.environ", {
            "TC_TIMER_DEADLINE_MODE": "relative",
            "TC_EVENT_LOOP_MODE": "poll",
            "TC_THREAD_YIELD_MODE": "legacy",
    }):
        environment = RUNNER.process_environment(config)
    require(environment["TC_TIMER_DEADLINE_MODE"] == "absolute",
            "timer deadline mode leaked from the parent environment")
    require(environment["TC_EVENT_LOOP_MODE"] == "wait",
            "event loop mode leaked from the parent environment")
    require(environment["TC_THREAD_YIELD_MODE"] == "native",
            "thread yield mode leaked from the parent environment")


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


def test_stage_two_evidence_fits_canonical_size_limit():
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
    })
    for field in (
        "callbackDeltaP50Ns", "callbackDeltaP95Ns", "callbackDeltaP99Ns",
        "callbackDeltaMaxNs", "callbackAbsoluteLatenessP50Ns",
        "callbackAbsoluteLatenessP95Ns", "callbackAbsoluteLatenessP99Ns",
        "callbackAbsoluteLatenessMaxNs", "callbackDeltaErrorP50Ns",
        "callbackDeltaErrorP95Ns", "callbackDeltaErrorP99Ns",
        "callbackDeltaErrorMaxNs",
    ):
        summary[field] = 1_000_000
    manifest = {"sourceCommit": "test-source"}
    rows = [
        RUNNER.make_row(2, config, sample, manifest, "runtime", summary, 1)
        for sample in range(1, 10)
    ]
    with tempfile.TemporaryDirectory(prefix="frame-pacing-stage-two-size-") as temp:
        root = Path(temp)
        csv_path = root / "stage.csv"
        json_path = root / "stage.json"
        RUNNER.write_evidence(csv_path, json_path, 2, 3, manifest,
                              "runtime", {"status": "PASS"}, rows)
        require(csv_path.stat().st_size < 20 * 1024
                and json_path.stat().st_size < 20 * 1024,
                "stage 2 canonical evidence exceeds 20 KiB")

        csv_path.write_text("previous csv\n", encoding="utf-8")
        json_path.write_text("previous json\n", encoding="utf-8")
        oversized_manifest = {"sourceCommit": "x" * (24 * 1024)}
        oversized_rows = [
            RUNNER.make_row(2, config, sample, oversized_manifest,
                            "runtime", summary, 1)
            for sample in range(1, 10)
        ]
        try:
            RUNNER.write_evidence(csv_path, json_path, 2, 3, oversized_manifest,
                                  "runtime", {"status": "PASS"}, oversized_rows)
        except RUNNER.BenchmarkFailure as error:
            require("20 KiB" in str(error),
                    "oversize evidence failure did not identify the limit")
            require(csv_path.read_text(encoding="utf-8") == "previous csv\n"
                    and json_path.read_text(encoding="utf-8") == "previous json\n",
                    "oversize evidence replaced the previous canonical files")
        else:
            raise AssertionError("oversize evidence was accepted")


def test_stage_three_evidence_fits_canonical_size_limit():
    configs = RUNNER.STAGES[3]
    manifest = {"sourceCommit": "test-source"}
    rows = []
    for config in configs:
        summary = stage_one_summary(config)
        summary.pop("syntheticPacingProfile")
        summary.pop("syntheticPacingIntervalNs")
        for field in CONTRACT.PACING_SUMMARY_FIELDS:
            summary.pop(field)
        summary.update({
            "driver": config.driver,
            "timerFps": config.timer_fps if config.timer_fps is not None else 0,
            "clock": config.clock,
            "timerDeadlinePolicy": config.timer_deadline_policy,
            "eventLoopPolicy": config.event_loop_policy,
            "yieldPolicy": config.yield_policy,
            "expectedCallbackIntervalNs": config.expected_callback_interval_ns,
            "callbackCount": 181,
        })
        for field in (
            "callbackDeltaP50Ns", "callbackDeltaP95Ns", "callbackDeltaP99Ns",
            "callbackDeltaMaxNs", "callbackAbsoluteLatenessP50Ns",
            "callbackAbsoluteLatenessP95Ns", "callbackAbsoluteLatenessP99Ns",
            "callbackAbsoluteLatenessMaxNs", "callbackDeltaErrorP50Ns",
            "callbackDeltaErrorP95Ns", "callbackDeltaErrorP99Ns",
            "callbackDeltaErrorMaxNs",
        ):
            summary[field] = 1_000_000
        for sample in range(1, 4):
            rows.append(RUNNER.make_row(
                3, config, sample, manifest, "runtime", summary, 1
            ))
    with tempfile.TemporaryDirectory(prefix="frame-pacing-stage-three-size-") as temp:
        root = Path(temp)
        csv_path = root / "stage.csv"
        json_path = root / "stage.json"
        RUNNER.write_evidence(csv_path, json_path, 3, 3, manifest,
                              "runtime", {"status": "PASS"}, rows)
        require(csv_path.stat().st_size < 20 * 1024
                and json_path.stat().st_size < 20 * 1024,
                "stage 3 canonical evidence exceeds 20 KiB")
        payload = json.loads(json_path.read_text(encoding="utf-8"))
        require(len(payload["rows"]) == 12,
                "stage 3 JSON evidence row count differs")
        require(all("syntheticPacingProfile" not in row for row in payload["rows"]),
                "null-only fields were retained in compact JSON rows")


def test_stage_four_five_modes_and_evidence_size():
    manifest = {"sourceCommit": "test-source"}
    for stage in (4, 5):
        rows = []
        for config in RUNNER.STAGES[stage]:
            summary = stage_one_summary(config)
            summary.pop("syntheticPacingProfile")
            summary.pop("syntheticPacingIntervalNs")
            for field in CONTRACT.PACING_SUMMARY_FIELDS:
                summary.pop(field)
            summary.update({
                "driver": config.driver,
                "timerFps": config.timer_fps if config.timer_fps is not None else 0,
                "clock": config.clock,
                "timerDeadlinePolicy": config.timer_deadline_policy,
                "eventLoopPolicy": config.event_loop_policy,
                "yieldPolicy": config.yield_policy,
                "expectedCallbackIntervalNs": config.expected_callback_interval_ns,
                "callbackCount": 181,
            })
            for field in (
                "callbackDeltaP50Ns", "callbackDeltaP95Ns", "callbackDeltaP99Ns",
                "callbackDeltaMaxNs", "callbackAbsoluteLatenessP50Ns",
                "callbackAbsoluteLatenessP95Ns", "callbackAbsoluteLatenessP99Ns",
                "callbackAbsoluteLatenessMaxNs", "callbackDeltaErrorP50Ns",
                "callbackDeltaErrorP95Ns", "callbackDeltaErrorP99Ns",
                "callbackDeltaErrorMaxNs",
            ):
                summary[field] = 1_000_000
            RUNNER.validate_summary(summary, stage, config, "off", RUNNER.FIXTURE)
            for sample in range(1, 4):
                rows.append(RUNNER.make_row(
                    stage, config, sample, manifest, "runtime", summary, 1
                ))
        with tempfile.TemporaryDirectory(
                prefix=f"frame-pacing-stage-{stage}-size-") as temp:
            root = Path(temp)
            csv_path = root / "stage.csv"
            json_path = root / "stage.json"
            RUNNER.write_evidence(csv_path, json_path, stage, 3, manifest,
                                  "runtime", {"status": "PASS"}, rows)
            require(csv_path.stat().st_size < 20 * 1024
                    and json_path.stat().st_size < 20 * 1024,
                    f"stage {stage} canonical evidence exceeds 20 KiB")


def test_windows_runner_matrix_and_process_contract():
    runner = WINDOWS_RUNNER_PATH.read_text(encoding="utf-8")
    helper = WINDOWS_HELPER_PATH.read_text(encoding="utf-8")
    combined = runner + "\n" + helper
    for path in (WINDOWS_RUNNER_PATH, WINDOWS_HELPER_PATH):
        require(path.stat().st_size < 20 * 1024,
                f"{path.name} exceeds the 20 KiB limit")
        require(len(path.read_text(encoding="utf-8").splitlines()) < 600,
                f"{path.name} exceeds the 600-line limit")

    require(runner.index("Set-StrictMode -Version Latest")
            < runner.index("$ErrorActionPreference = 'Stop'"),
            "strict mode is not initialized before runner behavior")
    metadata_initializers = (
        "$datasetHash = $null", "$runtimeSha256 = $null", "$sourceCommit = $null",
        "$completedProcesses = 0", "$completedPreflightProcesses = 0",
        "$expectedMeasuredProcesses = 48", "$rowsByStage = @{}",
    )
    writer_position = runner.index("function Write-RunMetadata")
    for initializer in metadata_initializers:
        require(runner.index(initializer) < writer_position,
                f"metadata variable is not initialized before the writer: {initializer}")
    require(writer_position < runner.index("Write-RunMetadata 'RUNNING'"),
            "metadata writer is called before it is defined")

    csv_block = re.search(r"\$csvFields = @\((.*?)\n\)", runner, re.DOTALL)
    require(csv_block is not None, "Windows runner is missing its CSV schema")
    windows_csv_fields = tuple(re.findall(r"'([^']+)'", csv_block.group(1)))
    require(windows_csv_fields == CONTRACT.CSV_FIELDS,
            "Windows runner CSV schema differs from the shared benchmark contract")

    actual = [(int(stage), name) for stage, name in re.findall(
        r"@\{Stage=(\d); Name='([^']+)'", runner)]
    expected = [(stage, config.name) for stage, configs in CONTRACT.STAGES.items()
                for config in configs]
    require(actual == expected, "Windows PowerShell matrix differs from the 48-process contract")
    require("$expectedMeasuredProcesses = 48" in runner,
            "Windows runner does not require exactly 48 measured processes")
    require("$sample -le 3" in runner and "for ($stage = 1; $stage -le 5; $stage++)" in runner,
            "Windows runner does not execute three samples for all five stages")
    require("System.Collections.Generic.List[object]" not in combined,
            "Windows runner must use ordinary PowerShell arrays")

    forbidden = re.compile(
        r"(?im)^\s*(?:&\s*)?(?:python(?:3)?|py|java|git|gh|curl|wget|"
        r"Invoke-WebRequest|Invoke-RestMethod|Install-Module|winget)\b"
    )
    require(forbidden.search(combined) is None,
            "Windows runner invokes a forbidden external tool or network command")
    require("SetEnvironmentVariable($name, [string]$environmentValues[$name], 'Process')" in runner
            and "SetEnvironmentVariable($name, $previousEnvironment[$name], 'Process')" in runner,
            "Windows runner does not set and restore every diagnostic process mode")

    launch = runner.index("Start-Process -FilePath $executablePath")
    handle = runner.index("$null = $process.Handle", launch)
    timed_wait = runner.index("$process.WaitForExit($timeoutMilliseconds)", handle)
    final_wait = runner.index("$process.WaitForExit()", timed_wait)
    require(launch < handle < timed_wait < final_wait
            and "-PassThru" in runner[launch:timed_wait],
            "Windows launch does not follow the handle and wait contract")
    require("$manifest.runtimeSha256" in runner
            and "Get-DatasetDigest $corpusPath" in runner
            and "Corpus magic counts must be 660 JPEG and 3 PNG" in helper,
            "Windows runner does not validate runtime and dataset identities")
    require("$hexStyle = [System.Globalization.NumberStyles]::HexNumber" in helper
            and "[UInt32]::Parse('84222325', $hexStyle)" in helper
            and "[UInt32]::Parse('cbf29ce4', $hexStyle)" in helper
            and "[UInt32]0x84222325" not in helper
            and "[UInt32]0xcbf29ce4" not in helper,
            "Windows FNV seeds are not parsed as unsigned hexadecimal values")
    require("$manifest.framePacingRunnerCompanion -ne 'frame-pacing-benchmark-windows-functions.ps1'" in runner,
            "Windows runner does not validate the packaged companion identity")

    failure_start = runner.index("} catch {\n    $failureMessage")
    failure_path = runner[failure_start:]
    for required in ("Write-RunMetadata 'FAILED'", "Write-StageSummaries",
                     "Compress-Archive -Path $evidenceRoot"):
        require(required in failure_path,
                f"failure path is missing {required}")
    require('"stage-$stage-summary.csv"' in runner,
            "Windows results archive omits stage summary generation")
    for artifact in ("execution-metadata.json",
                     "runner-copies", "manifest.json", "frame-pacing-windows-results-"):
        require(artifact in runner, f"Windows results archive omits {artifact}")


def test_windows_package_manifest_and_documentation_contract():
    runner = WINDOWS_RUNNER_PATH.read_text(encoding="utf-8")
    helper = WINDOWS_HELPER_PATH.read_text(encoding="utf-8")
    package = PACKAGER_PATH.read_text(encoding="utf-8")
    readme = README_PATH.read_text(encoding="utf-8")
    for field in (
        '"framePacingRunner"', '"framePacingRunnerCompanion"',
        '"framePacingSchemaVersion": 1',
        '"framePacingMeasuredProcessCount": 48',
        '"framePacingStages": [1, 2, 3, 4, 5]',
    ):
        require(field in package, f"Windows package manifest is missing {field}")
    require("frame-pacing-benchmark-windows-functions.ps1" in runner
            and "frame-pacing-benchmark-windows-functions.ps1" in package,
            "Windows package omits the PowerShell companion")
    command = "powershell -ExecutionPolicy Bypass -File .\\run-frame-pacing-benchmark-windows.ps1"
    require(readme.count(command) == 1,
            "README must document the Windows frame-pacing runner once")
    for name in ("TC_TIMER_DEADLINE_MODE", "TC_EVENT_LOOP_MODE", "TC_THREAD_YIELD_MODE"):
        require(name not in readme,
                f"README documents internal diagnostic selector {name}")


def main():
    require(RUNNER_PATH.stat().st_size < 20 * 1024,
            "frame-pacing runner exceeds the plan's 20 KiB limit")
    tests = (
        test_exact_stage_matrices,
        test_summary_contract,
        test_flick_summary_matches_configuration,
        test_preflight_contract,
        test_process_output_path_is_bundle_relative,
        test_process_environment_sets_all_diagnostic_modes,
        test_failure_handling_does_not_launch_or_accept_failed_process,
        test_evidence_row_count_and_safe_failure,
        test_stage_two_evidence_fits_canonical_size_limit,
        test_stage_three_evidence_fits_canonical_size_limit,
        test_stage_four_five_modes_and_evidence_size,
        test_windows_runner_matrix_and_process_contract,
        test_windows_package_manifest_and_documentation_contract,
    )
    for test in tests:
        test()
    print(f"frame-pacing benchmark contract tests passed: {len(tests)}")


if __name__ == "__main__":
    main()
