#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Focused matrix tests for the distributed image-scroll benchmark runner."""

from collections import Counter
import importlib.util
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


def main():
    require(RUNNER.DEFAULT_MATRIX_PROCESS_COUNT == 50,
            "default matrix process count is not 50")
    require(RUNNER.DEFAULT_EXPECTED_PROCESS_COUNT == 50,
            "default expected process count is not 50")
    assert_results_state_diagnostics()
    assert_physical_target_baseline()
    assert_validation_failure_continuation()
    assert_fatal_execution_stops()

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
