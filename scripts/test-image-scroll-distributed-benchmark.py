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


def assert_archive_execution_state_complete(output):
    archives = sorted(output.glob("totalcross-image-benchmark-results-*.zip"))
    require(archives, "completed phase did not create a results ZIP")
    with RUNNER.zipfile.ZipFile(archives[-1]) as source:
        require("results/execution-state.json" in source.namelist(),
                "results ZIP omitted execution-state.json")
        state = json.loads(source.read("results/execution-state.json"))
    require(state.get("status") != "RUNNING",
            "results ZIP captured execution state while RUNNING")
    require(state.get("status") in ("PASS", "PASS_WITH_VALIDATION_FAILURES"),
            "results ZIP did not capture a final successful execution status")
    require(state.get("resultsState") == "COMPLETE",
            "results ZIP did not capture COMPLETE results state")


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
                    bundle / "corpus", list(range(RUNNER.EXPECTED_JPEGS)),
                    "test-digest", bundle / "benchmark-app"
                )), \
                mock.patch.object(RUNNER, "self_test", side_effect=fake_self_test), \
                mock.patch.object(RUNNER, "run_scroll_profile"):
            RUNNER.run_phase(bundle, "full", "full")

        state = json.loads((output / RUNNER.RESULTS_STATE_FILE).read_text())
        require(state["status"] == "PASS",
                "clean full execution did not complete successfully")
        summary = json.loads((output / "default-execution-summary.json").read_text())
        require(summary["status"] == "PASS",
                "clean full execution did not write a PASS summary")
        assert_archive_execution_state_complete(output)

    with tempfile.TemporaryDirectory(prefix="image-scroll-auto-self-test-test-") as temp:
        bundle = Path(temp) / "bundle"
        bundle.mkdir()
        output = bundle / "results"
        calls = []

        def fake_self_test(_bundle, test_manifest, test_output):
            calls.append("self-test")
            (test_output / "self-test.json").write_text(json.dumps({
                "fixture": RUNNER.FIXTURE, "status": "PASS",
                "datasetFileCount": RUNNER.EXPECTED_JPEGS,
                "sourceCommit": test_manifest["sourceCommit"],
            }))
            RUNNER.write_execution_state(test_output, "SELF_TEST_PASS", test_manifest)
            return test_output, [], "test-digest"

        with mock.patch.object(RUNNER, "load_manifest", return_value=manifest), \
                mock.patch.object(RUNNER, "validate_bundle", return_value=(
                    bundle / "corpus", list(range(RUNNER.EXPECTED_JPEGS)),
                    "test-digest", bundle / "benchmark-app"
                )), \
                mock.patch.object(RUNNER, "self_test", side_effect=fake_self_test), \
                mock.patch.object(RUNNER, "run_scroll_profile") as run_profile:
            RUNNER.run_phase(bundle, "matrix", "prefetch-diagnostics")
        require(calls == ["self-test"],
                "clean non-decode phase did not automatically run self-test")
        require(run_profile.call_count == 1,
                "clean non-decode phase did not continue to its requested profile")
        assert_archive_execution_state_complete(output)

    with tempfile.TemporaryDirectory(prefix="image-scroll-valid-self-test-resume-") as temp:
        bundle = Path(temp) / "bundle"
        output = bundle / "results"
        bundle.mkdir()
        output.mkdir()
        marker = {
            "fixture": RUNNER.FIXTURE, "status": "PASS",
            "datasetFileCount": RUNNER.EXPECTED_JPEGS,
            "sourceCommit": manifest["sourceCommit"],
            "runtimeSha256": manifest["runtimeSha256"],
        }
        (output / "self-test.json").write_text(json.dumps(marker))
        RUNNER.write_execution_state(output, "SELF_TEST_PASS", manifest)
        with mock.patch.object(RUNNER, "load_manifest", return_value=manifest), \
                mock.patch.object(RUNNER, "validate_bundle", return_value=(
                    bundle / "corpus", list(range(RUNNER.EXPECTED_JPEGS)),
                    "test-digest", bundle / "benchmark-app"
                )), \
                mock.patch.object(RUNNER, "self_test") as self_test:
            RUNNER.run_phase(bundle, "self-test", "full")
        self_test.assert_not_called()

    with tempfile.TemporaryDirectory(prefix="image-scroll-partial-results-test-") as temp:
        bundle = Path(temp) / "bundle"
        output = bundle / "results"
        output.mkdir(parents=True)
        sentinel = output / "partial.txt"
        sentinel.write_text("preserve me")
        with mock.patch.object(RUNNER, "validate_bundle"):
            try:
                RUNNER.preflight(bundle, manifest, output, "prefetch-thread-diagnostics")
            except RUNNER.FatalBenchmarkFailure as error:
                require("partial/invalid" in str(error),
                        "partial results failure did not identify the state")
            else:
                raise AssertionError("partial results state was overwritten")
        require(sentinel.read_text() == "preserve me"
                and not (output / "self-test.json").exists(),
                "partial results preflight changed existing data")


def assert_zip_failure_marks_incomplete():
    manifest = {
        "sourceCommit": "test-source",
        "sdkSourceAttestation": "test-source",
        "runtimeSha256": "0" * 64,
        "includeDecodeAssets": False,
    }
    for phase, profile in (("matrix", "prefetch-diagnostics"), ("full", "full")):
        with tempfile.TemporaryDirectory(prefix="image-scroll-zip-failure-test-") as temp:
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
                RUNNER.write_execution_state(test_output, "SELF_TEST_PASS", test_manifest)
                return test_output, [], "test-digest"

            with mock.patch.object(RUNNER, "load_manifest", return_value=manifest), \
                    mock.patch.object(RUNNER, "validate_bundle", return_value=(
                        bundle / "corpus", list(range(RUNNER.EXPECTED_JPEGS)),
                        "test-digest", bundle / "benchmark-app"
                    )), \
                    mock.patch.object(RUNNER, "self_test", side_effect=fake_self_test), \
                    mock.patch.object(RUNNER, "run_scroll_profile"), \
                    mock.patch.object(RUNNER, "write_zip", side_effect=OSError("zip failed")):
                try:
                    RUNNER.run_phase(bundle, phase, profile)
                except OSError as error:
                    require(str(error) == "zip failed",
                            "ZIP failure was replaced by a different error")
                else:
                    raise AssertionError("ZIP creation failure was ignored")

            state = json.loads((output / RUNNER.RESULTS_STATE_FILE).read_text())
            require(state.get("status") == "INCOMPLETE",
                    f"{phase} ZIP failure did not leave execution state INCOMPLETE")


def thread_artifacts(mode, sleep_ms, mask, elapsed_ns):
    summary = {
        "prefetchThreadMode": mode,
        "prefetchWorkerSleepMs": sleep_ms,
        "prefetch": "on",
        "accounting": "on",
        "imageCount": RUNNER.EXPECTED_JPEGS,
        "prefetchRequestCount": RUNNER.EXPECTED_JPEGS,
        "prefetchReadyCount": RUNNER.EXPECTED_JPEGS - 3,
        "prefetchFailedCount": 0,
        "prefetchNotPrefetchableCount": 3,
        "requestedMask": mask,
        "effectiveMask": mask,
        "prefetchElapsedNs": elapsed_ns,
        "uiBuildElapsedNs": 1,
    }
    for json_name, _ in RUNNER.PREFETCH_PHASE_SUMMARY_FIELDS:
        summary[json_name] = 1
    decoded_entries = RUNNER.EXPECTED_JPEGS - 3
    thread_count = decoded_entries if mode == "legacy" else 1
    poll_count = 2 if mode == "worker-poll" else 0
    sem_wake_count = decoded_entries - 1 if mode == "worker-semaphore" else 0
    phases = {
        json_name: 0 for json_name, _ in RUNNER.PREFETCH_DIAGNOSTIC_COUNTER_FIELDS
    }
    phases.update({
        json_name: None for json_name, _ in RUNNER.PREFETCH_DIAGNOSTIC_DERIVED_FIELDS
    })
    phases["geometryUnaccountedNs"] = 0
    thread_counts = {
        "preparationEntryCount": decoded_entries,
        "preparationEntryTotalNs": 10,
        "threadCreateCount": thread_count,
        "threadObjectCreateNs": 2,
        "threadStartCount": thread_count,
        "threadStartCallNs": 3,
        "threadStartLatencyNs": 4,
        "decodeEntryCount": decoded_entries,
        "decodeWorkerNs": 5,
        "uiDispatchCount": decoded_entries,
        "uiDispatchWaitNs": 6,
        "uiWaitNs": 6,
        "adoptNs": 7,
        "finishPreparationNs": 8,
        "finishBookkeepingNs": 9,
        "workerPollCount": poll_count,
        "workerSleepRequestedNs": poll_count * sleep_ms * 1_000_000,
        "workerIdleElapsedNs": 11 if poll_count else 0,
        "workerSemaphoreReleaseCount": sem_wake_count,
        "workerSemaphoreAcquireCount": sem_wake_count,
        "workerSemaphoreWakeCount": sem_wake_count,
        "workerSemaphoreOutstandingWakeCount": 0,
    }
    phases.update({"prefetchThreadMode": mode, "prefetchWorkerSleepMs": sleep_ms})
    phases.update(thread_counts)
    summary.update({
        "workerSemaphoreReleaseCount": sem_wake_count,
        "workerSemaphoreAcquireCount": sem_wake_count,
        "workerSemaphoreWakeCount": sem_wake_count,
        "workerSemaphoreOutstandingWakeCount": 0,
    })
    return summary, {"accountingEnabled": True, "prefetchPhases": phases}


def assert_prefetch_thread_diagnostics():
    profile = RUNNER.profile_config("prefetch-thread-diagnostics")
    require(profile["masks"] == (6, 38)
            and profile["thread_configurations"] == (("legacy", 0),
                                                       ("worker-poll", 1),
                                                       ("worker-semaphore", 0)),
            "prefetch thread diagnostic configuration differs")
    require(profile["app_profile"] == "pft",
            "prefetch thread public phase does not map to its short app profile")
    with tempfile.TemporaryDirectory(prefix="image-scroll-prefetch-thread-test-") as temp:
        output = Path(temp)
        plan = RUNNER.write_prefetch_thread_suite_plan(output, profile)
        expected = [
            (mode, sleep_ms, mask)
            for mode, sleep_ms in profile["thread_configurations"]
            for mask in profile["masks"]
        ]
        require([(mode, sleep_ms, mask) for _, _, mode, sleep_ms, mask, _, _ in plan]
                == expected and len(plan) == 6,
                "prefetch thread suite plan is not the exact six-process matrix")
        run_dirs = [RUNNER.expected_prefetch_thread_run_dir(
            output, mode, sleep_ms, mask, run
        ) for _, run, mode, sleep_ms, mask, _, _ in plan]
        require(len(set(run_dirs)) == 6,
                "prefetch thread run directories are not unique")

        bundle = output / "bundle"
        bundle.mkdir()
        commands_by_target = {}

        def fake_subprocess(command, **_kwargs):
            commands_by_target[current_target[0]] = command
            return SimpleNamespace(returncode=0)

        current_target = [None]
        with mock.patch.object(RUNNER.subprocess, "run", side_effect=fake_subprocess), \
                mock.patch.object(RUNNER, "enrich_environment_metadata", return_value={}), \
                mock.patch.object(RUNNER, "capture_physical_target_baseline", return_value={}), \
                mock.patch.object(RUNNER, "validate_run_artifacts", return_value={}), \
                mock.patch.object(RUNNER, "read_json_file", return_value={}), \
                mock.patch.object(RUNNER, "validate_prefetch_thread_run_artifacts"):
            for target, executable_name in (
                    ("windows-x64", "ImageScrollRealWorkloadBenchmarkApp.exe"),
                    ("macos-arm64", "ImageScrollRealWorkloadBenchmarkApp")):
                (bundle / executable_name).touch()
                for thread_mode, sleep_ms in profile["thread_configurations"]:
                    current_target[0] = (target, thread_mode)
                    RUNNER.run_process(
                        bundle, {"target": target, "executable": executable_name}, output,
                        "0123456789abcdef", 38, "on", "on", 1,
                        f"thread-{thread_mode}-{target}",
                        profile_name="prefetch-thread-diagnostics",
                        prefetch_thread_mode=thread_mode,
                        prefetch_worker_sleep_ms=sleep_ms,
                        profile_run_dir=RUNNER.expected_prefetch_thread_run_dir(
                            output, thread_mode, sleep_ms, 38, 1
                        ),
                        app_profile=profile["app_profile"],
                    )

        def desktop_application_payload(command):
            # The desktop startup parser consumes these VM-level options before
            # copying the remaining application arguments into commandLine[256].
            payload = []
            skip_next = False
            for argument in command[1:]:
                if skip_next:
                    skip_next = False
                elif argument in ("/scr", "-p"):
                    skip_next = True
                else:
                    payload.append(argument)
            return " ".join(payload)

        require(set(commands_by_target) == {
                    (target, mode)
                    for target in ("windows-x64", "macos-arm64")
                    for mode, _ in profile["thread_configurations"]
                }, "runner command regression omitted a target or strategy")
        for (target, mode), command in commands_by_target.items():
            sleep_ms = dict(profile["thread_configurations"])[mode]
            require(command[-1] == "--profile=pft"
                    and f"--prefetch-thread-mode={mode}" in command
                    and f"--prefetch-worker-sleep-ms={sleep_ms}" in command,
                    f"{target}/{mode} runner omitted prefetch-thread app arguments")
            payload = desktop_application_payload(command)
            legacy_profile_command = list(command)
            legacy_profile_command[-1] = "--profile=prefetch-thread-diagnostics"
            legacy_payload = desktop_application_payload(legacy_profile_command)
            require(len(legacy_payload) >= 255,
                    f"{target} regression command no longer reaches the VM limit")
            require(len(payload) <= 247,
                    f"{target}/{mode} application payload lacks headroom below the VM limit: "
                    f"{len(payload)} characters")
        require(RUNNER.configuration_key(
                    "prefetch-thread-diagnostics", 1, 6, "on", "on",
                    prefetch_thread_mode="worker-poll", prefetch_worker_sleep_ms=1,
                ) != RUNNER.configuration_key(
                    "prefetch-thread-diagnostics", 1, 6, "on", "on",
                    prefetch_thread_mode="worker-semaphore", prefetch_worker_sleep_ms=0,
                ), "thread strategy and sleep do not distinguish run keys")

        elapsed_by = {("legacy", 0): 1000, ("worker-poll", 1): 1100,
                      ("worker-semaphore", 0): 1200}
        for _, run, mode, sleep_ms, mask, _, _ in plan:
            run_dir = RUNNER.expected_prefetch_thread_run_dir(
                output, mode, sleep_ms, mask, run
            )
            run_dir.mkdir(parents=True, exist_ok=True)
            summary, counters = thread_artifacts(
                mode, sleep_ms, mask, elapsed_by[(mode, sleep_ms)]
            )
            (run_dir / "summary.json").write_text(json.dumps(summary))
            (run_dir / "counters.json").write_text(json.dumps(counters))
            (run_dir / "runner-process.json").write_text(json.dumps({
                "processWallNs": 10_000 + run,
                "exitCode": 0,
            }))
        csv_path = RUNNER.aggregate_prefetch_thread_diagnostics(output, plan)
        with csv_path.open(newline="", encoding="utf-8") as source:
            csv_rows = list(csv.DictReader(source))
        require(len(csv_rows) == 6
                and all(row["status"] == "PASS" for row in csv_rows),
                "prefetch thread aggregate did not retain six validated rows")
        require({"process_wall_ns", "prefetch_elapsed_ns", "prefetch_jpeg_decode_count",
                 "prefetch_geometry_draw_ns",
                 "preparation_entry_count", "thread_start_latency_ns",
                 "worker_sleep_requested_ns", "ui_wait_ns",
                 "worker_semaphore_release_count",
                 "worker_semaphore_acquire_count",
                 "worker_semaphore_wake_count"}.issubset(csv_rows[0]),
                "prefetch thread CSV omitted existing or preparation metrics")
        aggregate = json.loads((output / "prefetch-thread-diagnostics-summary.json").read_text())
        require(aggregate["processCount"] == 6 and len(aggregate["rows"]) == 6,
                "prefetch thread JSON summary process count differs")
        require([item["mask"] for item in aggregate["comparisons"]] == [6, 38]
                and all([strategy["strategy"] for strategy in item["strategies"]]
                        == ["legacy", "worker-poll", "worker-semaphore"]
                        for item in aggregate["comparisons"]),
                "prefetch thread strategy comparisons differ")
        require(all(strategy["process_wall_ns"] > 0
                    for item in aggregate["comparisons"]
                    for strategy in item["strategies"]),
                "prefetch thread comparison omitted process wall time")


def assert_results_zip_contract():
    with tempfile.TemporaryDirectory(prefix="image-scroll-results-zip-test-") as temp:
        bundle = Path(temp) / "bundle"
        output = bundle / "results"
        nested = output / "runs" / "one"
        nested.mkdir(parents=True)
        bundle.mkdir(exist_ok=True)
        (nested / "summary.json").write_text("{}")
        (output / "totalcross-image-benchmark-results-old.zip").write_bytes(b"zip")
        (bundle / "manifest.json").write_text("{}")
        (bundle / "benchmark-app").write_text("app")
        (bundle / "DebugConsole.txt").write_text("debug")
        archive = RUNNER.write_zip(bundle, output)
        with RUNNER.zipfile.ZipFile(archive) as source:
            require(set(source.namelist()) == {"results/runs/one/summary.json",
                                              "DebugConsole.txt"},
                    "results ZIP contains files outside the contract")
        archive.unlink()
        (bundle / "DebugConsole.txt").unlink()
        archive = RUNNER.write_zip(bundle, output)
        with RUNNER.zipfile.ZipFile(archive) as source:
            require(set(source.namelist()) == {"results/runs/one/summary.json"},
                    "results ZIP without DebugConsole has unexpected files")


def physical_environment(width, height, pixel_bytes=4, renderer="software"):
    classification = "BGRA8888" if pixel_bytes == 4 else "RGB565"
    return {
        "packageTarget": "windows-x64",
        "hostOs": "Windows",
        "hostOsVersion": "11.0",
        "hostArchitecture": "AMD64",
        "endianness": "little",
        "totalCrossPlatform": "win32",
        "cpuModel": None,
        "refreshRate": 60,
        "sdlDrawableWidth": width,
        "sdlDrawableHeight": height,
        "surfaceScaleX": width / 540,
        "surfaceScaleY": height / 960,
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
        manifest = {"target": "windows-x64"}
        captured = RUNNER.capture_physical_target_baseline(
            output, one_x, {"profile": "test", "run": 1}, manifest
        )
        require(captured == first, "physical target baseline was not captured")
        require(RUNNER.load_physical_target_baseline(output, manifest) == first,
                "physical target baseline was not persisted")


def assert_environment_metadata():
    with mock.patch.object(RUNNER.platform, "system", return_value="Windows"), \
            mock.patch.object(RUNNER.platform, "release", return_value="11.0"), \
            mock.patch.object(RUNNER.platform, "machine", return_value="AMD64"), \
            mock.patch.object(RUNNER, "host_cpu_model", return_value="Intel CPU"):
        windows = RUNNER.collect_host_environment_metadata({"target": "windows-x64"})
    require(windows == {
        "packageTarget": "windows-x64", "hostOs": "Windows",
        "hostOsVersion": "11.0", "hostArchitecture": "AMD64",
        "endianness": windows["endianness"], "cpuModel": "Intel CPU",
    }, "Windows host metadata was not collected separately from the package target")

    with mock.patch.object(RUNNER.platform, "system", return_value="Darwin"), \
            mock.patch.object(RUNNER.platform, "release", return_value="24.5.0"), \
            mock.patch.object(RUNNER.platform, "machine", return_value="arm64"), \
            mock.patch.object(RUNNER, "host_cpu_model", return_value="Apple CPU"):
        macos = RUNNER.collect_host_environment_metadata({"packageTarget": "macos-arm64"})
    require(macos["packageTarget"] == "macos-arm64"
            and macos["hostOs"] == "Darwin"
            and macos["hostArchitecture"] == "arm64"
            and macos["cpuModel"] == "Apple CPU",
            "macOS host metadata was not collected separately from the package target")

    one_x = physical_environment(540, 960)
    two_x = physical_environment(1080, 1920)
    two_x.update({"sdlDrawableWidth": 540, "sdlDrawableHeight": 960})
    manifest = {"target": "windows-x64"}
    first = RUNNER.environment_baseline_from_environment(
        one_x, manifest, "1x environment"
    )
    second = RUNNER.environment_baseline_from_environment(
        two_x, manifest, "2x environment"
    )
    require(first["surfaceScaleX"] == 1.0 and first["surfaceScaleY"] == 1.0,
            "1x surface scale was not derived from the Skia surface")
    require(second["surfaceScaleX"] == 2.0 and second["surfaceScaleY"] == 2.0,
            "2x surface scale was not derived from the Skia surface")
    require(second["sdlDrawableWidth"] == 540
            and second["sdlDrawableHeight"] == 960
            and second["sdlDrawableWidth"] != two_x["skiaSurfaceWidth"],
            "SDL drawable dimensions were copied from the Skia surface")

    unavailable = dict(one_x)
    unavailable.update({
        "cpuModel": None, "refreshRate": None,
        "sdlDrawableWidth": None, "sdlDrawableHeight": None,
    })
    accepted = RUNNER.environment_baseline_from_environment(
        unavailable, manifest, "unavailable optional environment"
    )
    require(accepted["cpuModel"] is None and accepted["refreshRate"] is None
            and accepted["sdlDrawableWidth"] is None,
            "unavailable optional environment fields were not preserved")

    with tempfile.TemporaryDirectory(prefix="image-scroll-environment-baseline-test-") as temp:
        output = Path(temp)
        RUNNER.capture_physical_target_baseline(
            output, one_x, {"profile": "test", "run": 1}, manifest
        )
        changed = dict(one_x)
        changed["refreshRate"] = 75
        try:
            RUNNER.capture_physical_target_baseline(
                output, changed, {"profile": "test", "run": 2}, manifest
            )
        except RUNNER.BenchmarkFailure as error:
            require("refreshRate" in str(error),
                    "environment baseline mismatch did not name refreshRate")
        else:
            raise AssertionError("environment baseline accepted a changed refresh rate")


def assert_validation_failure_continuation():
    manifest = {
        "executable": "benchmark-app",
        "includeDecodeAssets": False,
        "target": "windows-x64",
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
                mock.patch.object(RUNNER, "host_cpu_model", return_value=None), \
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


def assert_reuse_hash_mismatch_is_non_fatal():
    manifest = {
        "sourceCommit": "test-source",
        "sdkSourceAttestation": "test-source",
        "runtimeSha256": "0" * 64,
        "includeDecodeAssets": False,
    }
    profile = {
        "name": "test-reuse",
        "app_profile": "test-reuse",
        "expected_processes": 2,
    }
    plan = [
        (0, 0, 1, 0, "on", "off", "off"),
        (0, 1, 1, 0, "on", "off", "on"),
    ]
    rows_by_key = {}
    hashes_by_key = {}
    for rendering_reuse in ("off", "on"):
        key = (1, 0, "on", "off", rendering_reuse)
        rows_by_key[key] = [
            {
                "profile": "test-reuse", "run": 1, "mask": "0",
                "rendering_reuse": rendering_reuse, "pass": pass_name,
                "status": "PASS", "validation_status": "VALID",
                "validation_error": "", "comparison_status": "VALID",
            }
            for pass_name in ("cold", "warm")
        ]
        hash_value = "hash-off" if rendering_reuse == "off" else "hash-on"
        hashes_by_key[key] = {
            (pass_name, 0): (hash_value, f"top-{hash_value}", f"bottom-{hash_value}")
            for pass_name in ("cold", "warm")
        }

    with tempfile.TemporaryDirectory(prefix="image-scroll-reuse-mismatch-test-") as temp:
        output = Path(temp)
        summary_path, hash_path = RUNNER.aggregate_scroll_reuse(
            output, manifest, profile, plan, rows_by_key, hashes_by_key
        )
        failures = RUNNER.load_validation_failures(output)
        comparison_failures = [
            record for record in failures.values()
            if record["configuration"].get("renderingReuse") == "off-on-comparison"
        ]
        require(len(comparison_failures) == 1,
                "hash mismatch did not produce one comparison failure")
        failure = comparison_failures[0]
        require(not failure["fatal"] and failure["status"] == "VALIDATION_FAILED",
                "hash mismatch was not recorded as non-fatal")
        require(failure["configuration"]["run"] == 1
                and "viewport hashes differ" in failure["messages"][0],
                "hash mismatch failure lost its run or exact message")
        require(len(failure["artifactPaths"]) == 4,
                "hash mismatch failure did not preserve comparison artifacts")

        with summary_path.open(newline="", encoding="utf-8") as source:
            summary_rows = list(csv.DictReader(source))
        require(len(summary_rows) == 4
                and all(row["status"] == "VALIDATION_FAILED"
                        and row["comparison_status"] == "INCOMPLETE_VALIDATION"
                        for row in summary_rows),
                "hash mismatch did not invalidate affected aggregate rows")
        with hash_path.open(newline="", encoding="utf-8") as source:
            hash_rows = list(csv.DictReader(source))
        require(hash_rows and all(row["status"] == "VALIDATION_FAILED"
                                  and row["comparison_status"] == "INCOMPLETE_VALIDATION"
                                  for row in hash_rows),
                "hash mismatch did not invalidate waypoint comparison rows")

        tracker = RUNNER.new_execution_tracker()
        tracker.update({
            "plannedProcessCount": 2, "launchedProcessCount": 2,
            "completedProcessCount": 2, "validProcessCount": 2,
            "validationFailedProcessCount": 0,
        })
        stream = io.StringIO()
        with contextlib.redirect_stdout(stream):
            RUNNER.write_default_execution_summary(
                output, manifest, status="PASS_WITH_VALIDATION_FAILURES",
                tracker=tracker,
            )
        final = json.loads((output / "default-execution-summary.json").read_text())
        require(final["validationFailedProcessCount"] == 0
                and final["validationFailureCount"] == 1,
                "comparison-only failure counts were conflated with failed processes")
        require("validation_failed_processes=0,validation_failures=1" in stream.getvalue(),
                "comparison-only failure diagnostics used misleading counts")


def assert_final_status_output():
    manifest = {
        "sourceCommit": "test-source",
        "sdkSourceAttestation": "test-source",
        "runtimeSha256": "0" * 64,
        "includeDecodeAssets": False,
    }
    for status in ("PASS", "PASS_WITH_VALIDATION_FAILURES", "INCOMPLETE"):
        with tempfile.TemporaryDirectory(prefix="image-scroll-status-output-test-") as temp:
            output = Path(temp)
            stream = io.StringIO()
            with contextlib.redirect_stdout(stream):
                RUNNER.write_default_execution_summary(
                    output, manifest, status=status, fatal_error=(
                        "process failed" if status == "INCOMPLETE" else None
                    )
                )
            require(f"default execution status={status}," in stream.getvalue(),
                    f"console output did not report {status}")


def assert_exploratory_plan_and_execution():
    profile = RUNNER.profile_config("reduced-image-optimizations")
    require(profile["masks"] == (4, 5, 6, 7, 38, 8198, 16390, 24582,
                                  32774, 57350, 32799),
            "exploratory mask matrix differs")
    require(profile["passes"] == 3 and profile["rounds"] == 1,
            "exploratory profile does not use one process with three passes")
    require(RUNNER.CONTROLLED_PAIRS == (
                (4, 6), (5, 7), (6, 38), (6, 8198),
                (6, 16390), (6, 32774), (6, 57350), (6, 32799),
            ), "controlled comparisons do not center on mask 6")
    diagnostic = RUNNER.profile_config("prefetch-diagnostics")
    require(diagnostic["masks"] == (0, 4, 6, 38, 32799)
            and diagnostic["accounting"] == ("on",)
            and diagnostic["expected_processes"] == 5,
            "prefetch diagnostic profile configuration differs")
    with tempfile.TemporaryDirectory(prefix="image-scroll-diagnostic-plan-test-") as temp:
        diagnostic_plan = RUNNER.write_suite_plan(
            Path(temp), diagnostic["masks"], diagnostic["prefetch"],
            diagnostic["accounting"], diagnostic["rounds"],
            diagnostic["expected_processes"],
        )
        require(len(diagnostic_plan) == 5
                and {entry[3] for entry in diagnostic_plan}
                == set(diagnostic["masks"]),
                "diagnostic profile does not isolate its five masks")
        phase_summary = {
            "uiBuildElapsedNs": 100,
            "prefetchElapsedNs": 200,
            **{json_name: 1 for json_name, _ in RUNNER.PREFETCH_PHASE_SUMMARY_FIELDS},
        }
        phase_fields = RUNNER.prefetch_phase_summary_fields(
            phase_summary, "diagnostic summary", required=True
        )
        require(phase_fields["image_load_ns"] == 1,
                "diagnostic phase timing was not mapped")
        prefetch_phase_values = {
            json_name: 1
            for json_name, _ in RUNNER.PREFETCH_DIAGNOSTIC_COUNTER_FIELDS
        }
        prefetch_phase_values.update({
            "jpegDecodeCount": 5,
            "jpegDecodeNs": 50,
            "jpegFullCount": 1,
            "jpegFullNs": 10,
            "jpegHalfCount": 1,
            "jpegHalfNs": 10,
            "jpegQuarterCount": 1,
            "jpegQuarterNs": 10,
            "jpegEighthCount": 1,
            "jpegEighthNs": 10,
            "jpegOtherCount": 1,
            "jpegOtherNs": 10,
            "geometryMaterializationTotalNs": 7,
            "geometrySourceSnapshotNs": 1,
            "geometrySurfaceAllocationNs": 1,
            "geometryCompileNs": 1,
            "geometryDrawNs": 1,
            "geometrySnapshotNs": 1,
            "geometryRegisterNs": 1,
            "geometryRgba8888Count": 1,
            "geometryRgba8888DrawNs": 1,
            "geometryRgb565Count": 0,
            "geometryRgb565DrawNs": 0,
            "geometryGray8Count": 0,
            "geometryGray8DrawNs": 0,
            "geometryArgb4444Count": 0,
            "geometryArgb4444DrawNs": 0,
            "geometrySourcePixels": 1,
            "geometryOutputPixels": 1,
            "geometryUnaccountedNs": 1,
            "geometryDrawNsPerMaterialization": 1.0,
            "geometryDrawNsPerSourceMegapixel": 1000000.0,
            "geometryDrawNsPerOutputMegapixel": 1000000.0,
        })
        counter_payload = {"prefetchPhases": prefetch_phase_values}
        counter_fields = RUNNER.validate_prefetch_diagnostic_counters(
            counter_payload, "diagnostic run"
        )
        require(counter_fields["prefetch_jpeg_decode_ns"] == 50
                and counter_fields["prefetch_target_color_converted_bytes"] == 1
                and counter_fields["prefetch_geometry_draw_ns"] == 1
                and counter_fields["prefetch_geometry_unaccounted_ns"] == 1
                and counter_fields["prefetch_geometry_draw_ns_per_materialization"] == 1.0,
                "diagnostic JPEG, native conversion, or geometry counter was not mapped")
        invalid_counter_payload = {
            "prefetchPhases": dict(prefetch_phase_values, jpegDecodeNs=49)
        }
        try:
            RUNNER.validate_prefetch_diagnostic_counters(
                invalid_counter_payload, "inconsistent diagnostic run"
            )
        except RUNNER.BenchmarkFailure as error:
            require("decode ns" in str(error),
                    "JPEG timing inconsistency did not identify decode ns")
        else:
            raise AssertionError("inconsistent JPEG timing was accepted")
    with tempfile.TemporaryDirectory(prefix="image-scroll-exploratory-plan-test-") as temp:
        output = Path(temp)
        plan = RUNNER.write_suite_plan(
            output, profile["masks"], profile["prefetch"], profile["accounting"],
            profile["rounds"], profile["expected_processes"],
        )
        plan_masks = [mask for _, _, _, mask, _, _ in plan]
        require(len(plan) == len(RUNNER.MASKS) and set(plan_masks) == set(RUNNER.MASKS),
                "exploratory plan does not contain one entry per mask")
        require(Counter(plan_masks) == Counter(RUNNER.MASKS),
                "exploratory plan repeats or omits a mask")
        with mock.patch.object(RUNNER, "run_process", return_value="PASS") as process:
            RUNNER.run_matrix(
                Path(temp) / "bundle", {}, output, "digest", profile["masks"],
                profile["prefetch"], profile["accounting"], profile["rounds"],
                profile["expected_processes"], passes=profile["passes"],
                profile_name=profile["name"],
            )
        require(process.call_count == len(RUNNER.MASKS),
                "exploratory masks were not isolated into one process each")
        calls_by_mask = {call.args[4]: call for call in process.call_args_list}
        require(set(calls_by_mask) == set(RUNNER.MASKS),
                "exploratory process calls do not cover the mask matrix")
        require(all(call.args[5:7] == ("on", "off") for call in process.call_args_list),
                "exploratory process configuration changed")
        require(all(call.args[11] == 3 for call in process.call_args_list),
                "exploratory process did not request three measured passes")
        require(len({call.args[8] for call in process.call_args_list}) == len(RUNNER.MASKS),
                "exploratory process labels are not isolated")


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

        multipass_output = output / "multipass"
        multipass_output.mkdir()
        multipass_plan = RUNNER.write_suite_plan(
            multipass_output, (0,), ("on",), ("off",), 1, 1
        )
        multipass_base = RUNNER.expected_run_dir(
            multipass_output, 0, "on", "off", 1
        )
        pass_names = ("cold-forward", "warm-reverse", "warm-forward")
        for pass_name in pass_names:
            pass_dir = multipass_base / "passes" / pass_name
            pass_dir.mkdir(parents=True)
            multipass_summary = {
                "status": "PASS", "requestedMask": 0, "effectiveMask": 0,
                "prefetch": "on", "accounting": "off", "pass": pass_name,
                "frameCount": 2, "memoryPeakResidentBytes": 1,
                "largestConsecutiveOver33_3": 0,
            }
            for field in RUNNER.TEMPORAL_SUMMARY_FIELDS:
                multipass_summary[field] = 1
            for field in RUNNER.FRAME_THRESHOLD_COUNT_FIELDS:
                multipass_summary[field] = 0
            (pass_dir / "summary.json").write_text(json.dumps(multipass_summary))
            (pass_dir / "counters.json").write_text("{}")
        with mock.patch.object(RUNNER, "validate_diagnostic_counters", return_value={}):
            multipass_summary_path = RUNNER.aggregate(
                multipass_output, multipass_plan, (0,), ("on",), ("off",),
                1, 1, "test-profile", pass_count=3,
            )
        with multipass_summary_path.open(newline="", encoding="utf-8") as source:
            multipass_rows = list(csv.DictReader(source))
        require(len(multipass_rows) == 3
                and [row["pass"] for row in multipass_rows] == list(pass_names)
                and all(row["pass_index"] for row in multipass_rows),
                "three-pass aggregation did not preserve independent pass rows")

        diagnostic_output = output / "prefetch-diagnostic"
        diagnostic_output.mkdir()
        diagnostic_profile = RUNNER.profile_config("prefetch-diagnostics")
        diagnostic_plan = RUNNER.write_suite_plan(
            diagnostic_output, diagnostic_profile["masks"],
            diagnostic_profile["prefetch"], diagnostic_profile["accounting"],
            diagnostic_profile["rounds"], diagnostic_profile["expected_processes"],
        )
        diagnostic_phase_values = {
            json_name: 1
            for json_name, _ in RUNNER.PREFETCH_DIAGNOSTIC_COUNTER_FIELDS
        }
        diagnostic_phase_values.update({
            "jpegDecodeCount": 5,
            "jpegDecodeNs": 50,
            "jpegFullCount": 1,
            "jpegFullNs": 10,
            "jpegHalfCount": 1,
            "jpegHalfNs": 10,
            "jpegQuarterCount": 1,
            "jpegQuarterNs": 10,
            "jpegEighthCount": 1,
            "jpegEighthNs": 10,
            "jpegOtherCount": 1,
            "jpegOtherNs": 10,
            "geometryMaterializationTotalNs": 7,
            "geometrySourceSnapshotNs": 1,
            "geometrySurfaceAllocationNs": 1,
            "geometryCompileNs": 1,
            "geometryDrawNs": 1,
            "geometrySnapshotNs": 1,
            "geometryRegisterNs": 1,
            "geometryRgba8888Count": 1,
            "geometryRgba8888DrawNs": 1,
            "geometryRgb565Count": 0,
            "geometryRgb565DrawNs": 0,
            "geometryGray8Count": 0,
            "geometryGray8DrawNs": 0,
            "geometryArgb4444Count": 0,
            "geometryArgb4444DrawNs": 0,
            "geometrySourcePixels": 1,
            "geometryOutputPixels": 1,
            "geometryUnaccountedNs": 1,
            "geometryDrawNsPerMaterialization": 1.0,
            "geometryDrawNsPerSourceMegapixel": 1000000.0,
            "geometryDrawNsPerOutputMegapixel": 1000000.0,
        })
        for _, _, run, mask, prefetch, accounting in diagnostic_plan:
            run_dir = RUNNER.expected_run_dir(
                diagnostic_output, mask, prefetch, accounting, run
            )
            run_dir.mkdir(parents=True)
            diagnostic_summary = {
                "status": "PASS", "requestedMask": mask, "effectiveMask": mask,
                "prefetch": prefetch, "accounting": accounting,
                "frameCount": 2, "memoryPeakResidentBytes": 1,
                "largestConsecutiveOver33_3": 0,
                "uiBuildElapsedNs": 10, "prefetchElapsedNs": 20,
            }
            for field in RUNNER.PREFETCH_PHASE_SUMMARY_FIELDS:
                diagnostic_summary[field[0]] = 1
            for field in RUNNER.TEMPORAL_SUMMARY_FIELDS:
                diagnostic_summary[field] = 1
            for field in RUNNER.FRAME_THRESHOLD_COUNT_FIELDS:
                diagnostic_summary[field] = 0
            (run_dir / "summary.json").write_text(
                json.dumps(diagnostic_summary)
            )
            (run_dir / "counters.json").write_text(json.dumps({
                "prefetchPhases": diagnostic_phase_values
            }))
        with mock.patch.object(RUNNER, "validate_diagnostic_counters", return_value={}):
            diagnostic_summary_path = RUNNER.aggregate(
                diagnostic_output, diagnostic_plan,
                diagnostic_profile["masks"], diagnostic_profile["prefetch"],
                diagnostic_profile["accounting"], diagnostic_profile["rounds"],
                diagnostic_profile["expected_processes"], "prefetch-diagnostics",
            )
        require(diagnostic_summary_path.name == "prefetch-diagnostics.csv",
                "diagnostic aggregation used the normal summary path")
        with diagnostic_summary_path.open(newline="", encoding="utf-8") as source:
            diagnostic_rows = list(csv.DictReader(source))
        require(len(diagnostic_rows) == 5
                and all(row["image_load_ns"] == "1"
                        and row["prefetch_jpeg_decode_ns"] == "50"
                        and row["prefetch_target_color_converted_bytes"] == "1"
                        and row["prefetch_geometry_draw_ns"] == "1"
                        and row["prefetch_geometry_unaccounted_ns"] == "1"
                        and row["prefetch_geometry_draw_ns_per_output_megapixel"] == "1000000.0"
                        for row in diagnostic_rows),
                "diagnostic aggregation omitted phase, conversion, or geometry metrics")

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


def assert_exploratory_comparison_aggregation():
    profile = RUNNER.profile_config("reduced-image-optimizations")
    pass_names = RUNNER.benchmark_pass_names(profile["passes"])
    with tempfile.TemporaryDirectory(
            prefix="image-scroll-exploratory-comparison-test-") as temp:
        output = Path(temp)
        plan = RUNNER.write_suite_plan(
            output, profile["masks"], profile["prefetch"], profile["accounting"],
            profile["rounds"], profile["expected_processes"],
        )
        failure_log = output / "logs" / "mask-4-validation.log"
        failure_log.parent.mkdir()
        failure_log.write_text("viewport hash mismatch")
        RUNNER.append_validation_failure(
            output, profile["name"], 1, 4, "on", "off", None,
            RUNNER.BenchmarkFailure("viewport hash mismatch"), failure_log,
            [output / "runs" / "mask-4-failed"],
        )
        for _, _, run, mask, prefetch, accounting in plan:
            if mask == 4:
                continue
            base = RUNNER.expected_run_dir(output, mask, prefetch, accounting, run)
            for pass_index, pass_name in enumerate(pass_names, start=1):
                pass_dir = base / "passes" / pass_name
                pass_dir.mkdir(parents=True)
                measurement = pass_index * 1000 + mask * pass_index
                summary = {
                    "status": "PASS", "requestedMask": mask,
                    "effectiveMask": mask, "prefetch": prefetch,
                    "accounting": accounting, "pass": pass_name,
                    "frameCount": 2, "memoryPeakResidentBytes": 1,
                    "largestConsecutiveOver33_3": 0,
                }
                for field in RUNNER.TEMPORAL_SUMMARY_FIELDS:
                    summary[field] = measurement
                for field in RUNNER.FRAME_THRESHOLD_COUNT_FIELDS:
                    summary[field] = 0
                (pass_dir / "summary.json").write_text(json.dumps(summary))
                (pass_dir / "counters.json").write_text("{}")
        with mock.patch.object(RUNNER, "validate_diagnostic_counters", return_value={}):
            summary_path = RUNNER.aggregate(
                output, plan, profile["masks"], profile["prefetch"],
                profile["accounting"], profile["rounds"],
                profile["expected_processes"], profile["name"],
                pass_count=profile["passes"],
            )
        comparison_path = output / "exploratory-mask-comparison.csv"
        require(summary_path.is_file() and comparison_path.is_file(),
                "exploratory aggregation did not create its artifacts")
        with summary_path.open(newline="", encoding="utf-8") as source:
            summary_rows = list(csv.DictReader(source))
        failed_summary_rows = [
            row for row in summary_rows
            if row["mask"] == "4" and row["status"] == "VALIDATION_FAILED"
        ]
        require(len(summary_rows) == 33 and len(failed_summary_rows) == 3
                and all(row["comparison_status"] == "INCOMPLETE_VALIDATION"
                        for row in failed_summary_rows),
                "exploratory aggregation did not preserve failed pass rows")
        with comparison_path.open(newline="", encoding="utf-8") as source:
            comparison_rows = list(csv.DictReader(source))
        expected_combinations = {
            (f"{control}->{enabled}", pass_name)
            for control, enabled in RUNNER.CONTROLLED_PAIRS
            for pass_name in pass_names
        }
        actual_combinations = {
            (row["pair"], row["pass"]) for row in comparison_rows
        }
        require(len(comparison_rows) == 24
                and {row["prefetch"] for row in comparison_rows} == {"on"}
                and {row["run"] for row in comparison_rows} == {"1"}
                and actual_combinations == expected_combinations,
                "exploratory comparison rows do not cover all pair/pass combinations")
        failed_comparisons = [
            row for row in comparison_rows if row["pair"] == "4->6"
        ]
        require(len(failed_comparisons) == 3
                and all(row["variance_status"] == "INCOMPLETE_VALIDATION"
                        for row in failed_comparisons),
                "comparison validation failure was not preserved per pass")
        pass_deltas = [
            int(row["work_p50_delta_ns"])
            for row in comparison_rows if row["pair"] == "5->7"
        ]
        require(pass_deltas == [2, 4, 6],
                "comparison keys did not preserve independent pass samples")


def main():
    require(RUNNER.DEFAULT_MATRIX_PROCESS_COUNT == 31,
            "default matrix process count is not 31")
    require(RUNNER.DEFAULT_EXPECTED_PROCESS_COUNT == 31,
            "default expected process count is not 31")
    assert_results_state_diagnostics()
    assert_clean_full_and_resume_preflight()
    assert_zip_failure_marks_incomplete()
    assert_prefetch_thread_diagnostics()
    assert_results_zip_contract()
    assert_physical_target_baseline()
    assert_environment_metadata()
    assert_validation_failure_continuation()
    assert_fatal_execution_stops()
    assert_reuse_hash_mismatch_is_non_fatal()
    assert_aggregation_and_final_status()
    assert_exploratory_comparison_aggregation()
    assert_final_status_output()
    assert_exploratory_plan_and_execution()

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
            output, RUNNER.MASKS, ("on",), ("off",), 1,
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
        require(len(reduced_plan) == 11, "reduced plan process count differs")
        require(len(set(reduced_keys)) == len(reduced_keys),
                "reduced plan contains duplicates")
        require(reduced_counts == Counter(
            (mask, "on", "off")
            for mask in RUNNER.MASKS
        ),
                "reduced plan combinations differ")

        names = (
            "scroll-raster-correctness", "scroll-raster-performance",
            "release-default-scroll", "release-candidate-scroll",
        )
        reuse_plans = {name: assert_reuse_plan(name, output) for name in names}
        total = len(reduced_plan) + sum(len(plan) for plan in reuse_plans.values())
        require(total == 31, f"default plan contains {total} processes")

        manifest = {
            "sourceCommit": "test",
            "sdkSourceAttestation": "test",
            "runtimeSha256": "0" * 64,
            "tcvmSha256": "0" * 64,
            "includeDecodeAssets": False,
        }
        RUNNER.write_default_execution_summary(output, manifest)
        summary = json.loads((output / "default-execution-summary.json").read_text())
        require(summary["expectedProcessCount"] == 31,
                "default summary process count is not 31")
        require(summary["profileProcessCounts"]["reduced-image-optimizations"] == 11,
                "exploratory summary process count differs")
        require(summary["profilePassCounts"]["reduced-image-optimizations"] == 3,
                "exploratory summary pass count differs")
        require(summary["expectedMeasuredPassCount"] == 73,
                "default summary measured-pass count differs")
        require(summary["profileProcessCounts"]["release-default-scroll"] == 6,
                "real default summary count differs")
        require(summary["profileProcessCounts"]["release-candidate-scroll"] == 6,
                "release candidate summary count differs")

    package_script = Path(__file__).with_name("package-image-scroll-benchmark.sh").read_text()
    require('"matrixProcessCount": 31' in package_script,
            "package manifest matrix count is not 31")
    require('"expectedProcessCount": 31' in package_script,
            "package manifest expected count is not 31")
    require('"exploratoryPassCount": 3' in package_script,
            "package manifest exploratory pass count is not three")
    require('"prefetchDiagnosticProcessCount": 5' in package_script,
            "package manifest diagnostic process count is not five")
    require('"prefetchThreadDiagnosticMasks": [6,38]' in package_script,
            "package manifest prefetch thread masks differ")
    require('"prefetchThreadDiagnosticProcessCount": 6' in package_script,
            "package manifest prefetch thread process count differs")
    require('[["legacy",0],["worker-poll",1],["worker-semaphore",0]]'
            in package_script,
            "package manifest prefetch thread strategy matrix differs")
    require('"passes":3,"processCount":11' in package_script,
            "package manifest exploratory process count is not eleven")
    require('"masks":[4,5,6,7,38,8198,16390,24582,32774,57350,32799]' in package_script,
            "package manifest exploratory masks differ")
    require('"prefetch-diagnostics": {"masks":[0,4,6,38,32799]' in package_script,
            "package manifest lacks prefetch diagnostic profile")
    require('"prefetch-thread-diagnostics": {"masks":[6,38]' in package_script,
            "package manifest lacks prefetch thread diagnostic profile")
    require('"appProfile":"pft"' in package_script,
            "package manifest prefetch thread app profile differs")
    require('"release-candidate-scroll": {"masks":[32795]' in package_script,
            "package manifest lacks release candidate profile")

    print("image-scroll matrix tests passed,processes=31,exploratory_passes=3,unique_combinations=true")


if __name__ == "__main__":
    main()
