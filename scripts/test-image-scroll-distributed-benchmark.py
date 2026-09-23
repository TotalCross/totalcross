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


def main():
    require(RUNNER.DEFAULT_MATRIX_PROCESS_COUNT == 50,
            "default matrix process count is not 50")
    require(RUNNER.DEFAULT_EXPECTED_PROCESS_COUNT == 50,
            "default expected process count is not 50")
    assert_results_state_diagnostics()

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
