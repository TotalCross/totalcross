#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Focused matrix tests for the distributed image-scroll benchmark runner."""

from collections import Counter
import csv
import contextlib
import importlib.util
import io
from itertools import product
import json
from pathlib import Path
import tempfile
from unittest import mock
from types import SimpleNamespace


RUNNER_PATH = Path(__file__).with_name("run-image-scroll-distributed-benchmark.py")
SPEC = importlib.util.spec_from_file_location("image_scroll_runner", RUNNER_PATH)
RUNNER = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(RUNNER)


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def reuse_keys(plan):
    return [
        (run, mask, prefetch, accounting, rendering_reuse)
        for _, _, run, mask, prefetch, accounting, rendering_reuse in plan
    ]


def assert_reuse_plan(name, output):
    profile = RUNNER.profile_config(name)
    plan = RUNNER.write_reuse_suite_plan(output, profile)
    keys = reuse_keys(plan)
    expected_per_round = list(product(
        profile["masks"], profile["prefetch"], profile["accounting"],
        profile["rendering_reuse"],
    ))
    counts = Counter((mask, prefetch, accounting, rendering_reuse)
                     for _, mask, prefetch, accounting, rendering_reuse in keys)
    require(len(plan) == profile["expected_processes"],
            f"{name} process count differs")
    require(len(set(keys)) == len(keys), f"{name} plan contains duplicates")
    require(set(counts) == set(expected_per_round),
            f"{name} combinations differ")
    require(all(count == profile["rounds"] for count in counts.values()),
            f"{name} combinations do not repeat once per round")
    return plan


def assert_results_state_diagnostics():
    manifest = {
        "sourceCommit": "test-source",
        "sdkSourceAttestation": "test-source",
        "runtimeSha256": "0" * 64,
    }
    with tempfile.TemporaryDirectory(prefix="image-scroll-results-state-test-") as temp:
        root = Path(temp)
        output = root / "results"
        state, detail = RUNNER.inspect_results_state(output)
        require(state == "CLEAN_START" and "does not exist" in detail,
                "missing results directory was not a clean start")

        output.mkdir()
        RUNNER.probe_results_directory(output)
        require(not list(output.glob(".runner-results-probe-*")),
                "results probe file was not deleted")
        (output / "self-test.json").write_text(json.dumps({
            "fixture": RUNNER.FIXTURE, "status": "PASS",
        }))
        RUNNER.write_execution_state(output, "SELF_TEST_PASS", manifest)
        state, detail = RUNNER.inspect_results_state(output)
        require(state == "VALID_RESUME" and "SELF_TEST_PASS" in detail,
                "complete self-test was not a valid resume state")

        RUNNER.write_execution_state(output, "RUNNING", manifest)
        state, detail = RUNNER.inspect_results_state(output)
        require(state == "PARTIAL_INVALID" and "not resumable" in detail,
                "active execution was treated as a valid resume")

        (output / RUNNER.RESULTS_STATE_FILE).unlink()
        state, detail = RUNNER.inspect_results_state(output)
        require(state == "PARTIAL_INVALID" and "lacks both" in detail,
                "partial results were treated as a successful self-test")

        probe = root / "probe-file"
        probe.write_text("not a directory")
        with mock.patch.object(RUNNER, "probe_results_directory",
                               side_effect=RUNNER.FatalBenchmarkFailure("access denied")):
            try:
                RUNNER.probe_results_directory(probe)
            except RUNNER.FatalBenchmarkFailure as error:
                require(str(error) == "access denied",
                        "results access failure was not preserved")
            else:
                raise AssertionError("results access failure did not stop execution")


def assert_clean_full_and_resume_preflight():
    manifest = {
        "sourceCommit": "test-source",
        "sdkSourceAttestation": "test-source",
        "runtimeSha256": "0" * 64,
        "includeDecodeAssets": False,
    }
    with tempfile.TemporaryDirectory(prefix="image-scroll-clean-start-test-") as temp:
        bundle = Path(temp) / "bundle"
        output = bundle / "results"
        bundle.mkdir()

        def fake_self_test(_bundle, test_manifest, test_output):
            (test_output / "self-test.json").write_text(json.dumps({
                "fixture": RUNNER.FIXTURE,
                "status": "PASS",
                "datasetFileCount": RUNNER.EXPECTED_JPEGS,
                "sourceCommit": test_manifest["sourceCommit"],
            }))
            RUNNER.write_execution_state(
                test_output, "SELF_TEST_PASS", test_manifest,
            )
            return test_output, [], "test-digest"

        with mock.patch.object(RUNNER, "load_manifest", return_value=manifest), \
                mock.patch.object(RUNNER, "validate_bundle", return_value=(
                    bundle / "corpus", [], "test-digest", bundle / "benchmark-app"
                )), \
                mock.patch.object(RUNNER, "self_test", side_effect=fake_self_test), \
                mock.patch.object(RUNNER, "run_scroll_profile"), \
                mock.patch.object(RUNNER, "write_zip"):
            RUNNER.run_phase(bundle, "full", "full")

        state = json.loads((output / RUNNER.RESULTS_STATE_FILE).read_text())
        require(state["status"] == "PASS",
                "clean full execution did not complete successfully")
        summary = json.loads((output / "default-execution-summary.json").read_text())
        require(summary["status"] == "PASS",
                "clean full execution did not write a PASS summary")

    with tempfile.TemporaryDirectory(prefix="image-scroll-resume-preflight-test-") as temp:
        bundle = Path(temp) / "bundle"
        bundle.mkdir()
        output = bundle / "results"
        with mock.patch.object(RUNNER, "validate_bundle"):
            try:
                RUNNER.preflight(bundle, manifest, output, "matrix")
            except RUNNER.FatalBenchmarkFailure as error:
                require("run --phase self-test" in str(error),
                        "clean resume failure did not explain the required self-test")
            else:
                raise AssertionError("clean resume phase was accepted without self-test")


def physical_environment(width, height, pixel_bytes=4, renderer="software"):
    classification = "BGRA8888" if pixel_bytes == 4 else "RGB565"
    return {
        "expectedLogicalWidth": 540,
        "expectedLogicalHeight": 960,
        "effectiveLogicalWidth": 540,
        "effectiveLogicalHeight": 960,
        "skiaSurfaceWidth": width,
        "skiaSurfaceHeight": height,
        "skiaSurfaceRowBytes": width * pixel_bytes,
        "skiaSurfacePixelBytes": pixel_bytes,
        "skiaSurfaceColorType": 1 if pixel_bytes == 4 else 2,
        "skiaSurfaceAlphaType": 1,
        "skiaSurfaceColorClassification": classification,
        "rendererBackend": renderer,
    }


def assert_physical_target_baseline():
    one_x = physical_environment(540, 960)
    two_x = physical_environment(1080, 1920)
    first = RUNNER.validate_physical_target(one_x, None, "1x environment")
    second = RUNNER.validate_physical_target(two_x, None, "2x environment")
    require(first["skiaSurfaceWidth"] == 540 and second["skiaSurfaceWidth"] == 1080,
            "1x/2x physical targets were not accepted")
    require(first["skiaSurfaceHeight"] == 960 and second["skiaSurfaceHeight"] == 1920,
            "1x/2x physical target heights were not accepted")
    try:
        RUNNER.validate_physical_target(two_x, first, "changed environment")
    except RUNNER.BenchmarkFailure as error:
        require("skiaSurfaceWidth" in str(error),
                "physical target change did not name the differing field")
    else:
        raise AssertionError("physical target change was accepted")

    with tempfile.TemporaryDirectory(prefix="image-scroll-physical-target-test-") as temp:
        output = Path(temp)
        captured = RUNNER.capture_physical_target_baseline(
            output, one_x, {"profile": "test", "run": 1}
        )
        require(captured == first, "physical target baseline was not captured")
        require(RUNNER.load_physical_target_baseline(output) == first,
                "physical target baseline was not persisted")


def assert_validation_failure_continuation():
    manifest = {
        "executable": "benchmark-app",
        "includeDecodeAssets": False,
    }
    with tempfile.TemporaryDirectory(prefix="image-scroll-validation-test-") as temp:
        root = Path(temp)
        bundle = root / "bundle"
        output = bundle / "results"
        output.mkdir(parents=True)
        (bundle / "benchmark-app").write_text("fake executable")
        environment = physical_environment(540, 960)
        environment.update({
            "datasetFileCount": 663,
            "datasetHash": "test-digest",
        })
        (output / "environment.json").write_text(json.dumps(environment))
        completed = [SimpleNamespace(returncode=0), SimpleNamespace(returncode=0)]
        with mock.patch.object(RUNNER.subprocess, "run", side_effect=completed), \
                mock.patch.object(
                    RUNNER, "validate_run_artifacts",
                    side_effect=[RUNNER.BenchmarkFailure("bad viewport hash"), {}],
                ):
            first = RUNNER.run_process(
                bundle, manifest, output, "test-digest", 0, "on", "off", 1,
                "test-first-matrix-1", profile_name="test-profile",
            )
            second = RUNNER.run_process(
                bundle, manifest, output, "test-digest", 0, "on", "off", 2,
                "test-second-matrix-2", profile_name="test-profile",
            )
        require(first == "VALIDATION_FAILED", "validation failure did not return a status")
        require(second == "PASS", "later process did not continue after validation failure")
        failures = RUNNER.load_validation_failures(output)
        require(len(failures) == 1, "validation failure artifact has wrong count")
        failure = next(iter(failures.values()))
        require(failure["fatal"] is False
                and failure["status"] == "VALIDATION_FAILED",
                "validation failure was not classified as non-fatal")
        require(failure["configuration"]["profile"] == "test-profile"
                and failure["messages"] == ["bad viewport hash"],
                "validation failure configuration/message was not preserved")
        require(Path(failure["logPath"]).is_file(),
                "validation failure log path was not preserved")


def assert_fatal_execution_stops():
    manifest = {"executable": "benchmark-app"}
    with tempfile.TemporaryDirectory(prefix="image-scroll-fatal-test-") as temp:
        root = Path(temp)
        bundle = root / "bundle"
        output = bundle / "results"
        output.mkdir(parents=True)
        (bundle / "benchmark-app").write_text("fake executable")
        with mock.patch.object(
            RUNNER.subprocess, "run", return_value=SimpleNamespace(returncode=17)
        ) as process:
            try:
                RUNNER.run_matrix(
                    bundle, manifest, output, "test-digest", (0,), ("on",),
                    ("off",), 2, 2, profile_name="fatal-profile",
                )
            except RUNNER.BenchmarkFailure as error:
                require("exited with code 17" in str(error),
                        "fatal exit error did not preserve the exit code")
            else:
                raise AssertionError("fatal execution failure was swallowed")
            require(process.call_count == 1,
                    "matrix continued after a fatal execution failure")
            failures = RUNNER.load_validation_failures(output)
            require(any(record["fatal"]
                        and record["classification"] == "FATAL_EXECUTION"
                        for record in failures.values()),
                    "fatal execution was not recorded in the failure artifact")


def assert_aggregation_and_final_status():
    manifest = {
        "sourceCommit": "test-source",
        "sdkSourceAttestation": "test-source",
        "runtimeSha256": "0" * 64,
        "tcvmSha256": "0" * 64,
        "includeDecodeAssets": False,
    }
    with tempfile.TemporaryDirectory(prefix="image-scroll-aggregate-test-") as temp:
        output = Path(temp)
        plan = RUNNER.write_suite_plan(output, (0,), ("on",), ("off",), 2, 2)
        log_path = output / "logs" / "failed.log"
        log_path.parent.mkdir()
        log_path.write_text("post-run validation failed")
        RUNNER.append_validation_failure(
            output, "test-profile", 1, 0, "on", "off", None,
            RUNNER.BenchmarkFailure("viewport hash mismatch"), log_path,
            [output / "runs" / "failed"],
        )
        valid_dir = RUNNER.expected_run_dir(output, 0, "on", "off", 2)
        valid_dir.mkdir(parents=True)
        valid_summary = {
            "status": "PASS", "requestedMask": 0, "effectiveMask": 0,
            "frameCount": 2, "memoryPeakResidentBytes": 1,
            "largestConsecutiveOver33_3": 0,
        }
        for field in RUNNER.TEMPORAL_SUMMARY_FIELDS:
            valid_summary[field] = 1
        for field in RUNNER.FRAME_THRESHOLD_COUNT_FIELDS:
            valid_summary[field] = 0
        (valid_dir / "summary.json").write_text(json.dumps(valid_summary))
        (valid_dir / "counters.json").write_text("{}")
        with mock.patch.object(RUNNER, "validate_diagnostic_counters", return_value={}):
            summary_path = RUNNER.aggregate(
                output, plan, (0,), ("on",), ("off",), 2, 2,
                "test-profile",
            )
        with summary_path.open(newline="", encoding="utf-8") as source:
            rows = list(csv.DictReader(source))
        require(len(rows) == 2, "aggregate omitted a validation-failed process")
        failed = next(row for row in rows if row["status"] == "VALIDATION_FAILED")
        require(failed["comparison_status"] == "INCOMPLETE_VALIDATION"
                and "viewport hash mismatch" in failed["validation_error"],
                "aggregate did not mark validation failure as incomplete")

        pairwise_records = [
            {"mask": 0, "prefetch": "on", "run": 1, "status": "VALIDATION_FAILED"},
            {"mask": 32, "prefetch": "on", "run": 1, "status": "PASS",
             "work_time_p50_ns": 20, "work_time_p95_ns": 30,
             "paint_time_p50_ns": 10, "paint_time_p95_ns": 15},
        ]
        pairwise_path = RUNNER.write_pairwise_comparison(
            output, pairwise_records, (0, 32), 1
        )
        with pairwise_path.open(newline="", encoding="utf-8") as source:
            pairwise_rows = list(csv.DictReader(source))
        incomplete = next(row for row in pairwise_rows if row["pair"] == "0->32")
        require(incomplete["variance_status"] == "INCOMPLETE_VALIDATION",
                "pairwise comparison did not mark an invalid side")

        reuse_profile = dict(
            RUNNER.profile_config("scroll-raster-performance"),
            expected_processes=2,
        )
        reuse_plan = [
            (0, 0, 1, 0, "on", "off", "off"),
            (0, 1, 1, 0, "on", "off", "on"),
        ]
        reuse_log = output / "logs" / "reuse-failed.log"
        for rendering_reuse in ("off", "on"):
            RUNNER.append_validation_failure(
                output, "scroll-raster-reuse-poc", 1, 0, "on", "off",
                rendering_reuse,
                RUNNER.BenchmarkFailure("reuse viewport mismatch"), reuse_log,
                [output / "runs" / f"reuse-{rendering_reuse}"],
            )
        reuse_summary, reuse_hashes = RUNNER.aggregate_scroll_reuse(
            output, manifest, reuse_profile, reuse_plan,
            {(1, 0, "on", "off", mode): None for mode in ("off", "on")},
            {(1, 0, "on", "off", mode): None for mode in ("off", "on")},
        )
        with reuse_summary.open(newline="", encoding="utf-8") as source:
            reuse_rows = list(csv.DictReader(source))
        require(len(reuse_rows) == 4
                and all(row["status"] == "VALIDATION_FAILED" for row in reuse_rows),
                "reuse aggregation did not preserve invalid runs")
        require(reuse_hashes.is_file(), "reuse hash artifact was not written")

        tracker = RUNNER.new_execution_tracker()
        tracker.update({
            "plannedProcessCount": 4, "launchedProcessCount": 4,
            "completedProcessCount": 4, "validProcessCount": 1,
            "validationFailedProcessCount": 3,
        })
        RUNNER.write_default_execution_summary(
            output, manifest, status="PASS_WITH_VALIDATION_FAILURES", tracker=tracker
        )
        final = json.loads((output / "default-execution-summary.json").read_text())
        require(final["status"] == "PASS_WITH_VALIDATION_FAILURES"
                and final["validationFailedProcessCount"] == 3
                and len(final["validationFailures"]) == 3,
                "final summary did not preserve validation-failure status")


def main():
    require(RUNNER.DEFAULT_MATRIX_PROCESS_COUNT == 50,
            "default matrix process count is not 50")
    require(RUNNER.DEFAULT_EXPECTED_PROCESS_COUNT == 50,
            "default expected process count is not 50")
    assert_results_state_diagnostics()
    assert_clean_full_and_resume_preflight()
    assert_physical_target_baseline()
    assert_validation_failure_continuation()
    assert_fatal_execution_stops()
    assert_aggregation_and_final_status()

    default = RUNNER.profile_config("release-default-scroll")
    candidate = RUNNER.profile_config("release-candidate-scroll")
    require(default["masks"] == (None,), "real default is not an omitted mask")
    require(candidate["masks"] == (32795,), "release candidate mask is not 32795")
    require(default["name"] != candidate["name"],
            "release profiles are not distinct")
    require(default["app_profile"] == default["name"],
            "real default filenames and app profile differ")
    require(candidate["app_profile"] == candidate["name"],
            "release candidate filenames and app profile differ")

    with tempfile.TemporaryDirectory(prefix="image-scroll-matrix-test-") as temp:
        output = Path(temp)
        reduced_plan = RUNNER.write_suite_plan(
            output, RUNNER.MASKS, ("on",), ("off",), RUNNER.ROUNDS,
            RUNNER.REDUCED_PROCESS_COUNT,
        )
        reduced_keys = [
            (run, mask, prefetch, accounting)
            for _, _, run, mask, prefetch, accounting in reduced_plan
        ]
        reduced_counts = Counter(
            (mask, prefetch, accounting)
            for _, mask, prefetch, accounting in reduced_keys
        )
        require(len(reduced_plan) == 30, "reduced plan process count differs")
        require(len(set(reduced_keys)) == len(reduced_keys),
                "reduced plan contains duplicates")
        require(reduced_counts == Counter(
            (mask, "on", "off")
            for mask in RUNNER.MASKS for _ in range(RUNNER.ROUNDS)
        ),
                "reduced plan combinations differ")

        names = (
            "scroll-raster-correctness", "scroll-raster-performance",
            "release-default-scroll", "release-candidate-scroll",
        )
        reuse_plans = {name: assert_reuse_plan(name, output) for name in names}
        total = len(reduced_plan) + sum(len(plan) for plan in reuse_plans.values())
        require(total == 50, f"default plan contains {total} processes")

        manifest = {
            "sourceCommit": "test",
            "sdkSourceAttestation": "test",
            "runtimeSha256": "0" * 64,
            "tcvmSha256": "0" * 64,
            "includeDecodeAssets": False,
        }
        RUNNER.write_default_execution_summary(output, manifest)
        summary = json.loads((output / "default-execution-summary.json").read_text())
        require(summary["expectedProcessCount"] == 50,
                "default summary process count is not 50")
        require(summary["profileProcessCounts"]["release-default-scroll"] == 6,
                "real default summary count differs")
        require(summary["profileProcessCounts"]["release-candidate-scroll"] == 6,
                "release candidate summary count differs")

    package_script = Path(__file__).with_name("package-image-scroll-benchmark.sh").read_text()
    require('"matrixProcessCount": 50' in package_script,
            "package manifest matrix count is not 50")
    require('"expectedProcessCount": 50' in package_script,
            "package manifest expected count is not 50")
    require('"release-candidate-scroll": {"masks":[32795]' in package_script,
            "package manifest lacks release candidate profile")

    print("image-scroll matrix tests passed,processes=50,unique_combinations=true")


if __name__ == "__main__":
    main()
