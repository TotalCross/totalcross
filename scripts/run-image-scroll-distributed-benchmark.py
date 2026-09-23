#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

"""Run the official image-scroll benchmark bundle without local runtime injection."""

import argparse
import csv
import hashlib
import json
import math
import os
import platform
from pathlib import Path
import signal
import subprocess
import sys
import time
import zipfile


EXPECTED_JPEGS = 663
SCROLL_CORPUS_VARIANTS = ("imag",)
DECODE_CORPUS_VARIANTS = (
    "imag", "lossless", "decode-baseline", "decode-fast",
    "aggresive-480", "aggresive-540",
)
DECODE_LIBRARIES = {
    "imag": "DecodeImagLib.tcz",
    "lossless": "DecodeLosslessLib.tcz",
    "decode-baseline": "DecodeBaselineLib.tcz",
    "decode-fast": "DecodeFastLib.tcz",
    "aggresive-480": "DecodeAggresive480Lib.tcz",
    "aggresive-540": "DecodeAggresive540Lib.tcz",
}
SCREEN_SPEC = "-1,-1,540,960"
SCREEN_ARGUMENT = "/scr " + SCREEN_SPEC
LOGICAL_TARGET = {"width": 540, "height": 960}
PHYSICAL_TARGET_FIELDS = (
    "skiaSurfaceWidth", "skiaSurfaceHeight", "skiaSurfaceRowBytes",
    "skiaSurfacePixelBytes", "skiaSurfaceColorType", "skiaSurfaceAlphaType",
    "skiaSurfaceColorClassification", "rendererBackend",
)
ENVIRONMENT_BASELINE_FIELDS = (
    "packageTarget", "hostOs", "hostOsVersion", "hostArchitecture",
    "endianness", "totalCrossPlatform", "cpuModel", "refreshRate",
    "sdlDrawableWidth", "sdlDrawableHeight", "surfaceScaleX", "surfaceScaleY",
)
ENVIRONMENT_INTEGER_FIELDS = frozenset({
    "refreshRate", "sdlDrawableWidth", "sdlDrawableHeight",
})
ENVIRONMENT_FLOAT_FIELDS = frozenset({"surfaceScaleX", "surfaceScaleY"})
MASKS = (
    0, 6, 8, 16, 32, 8192, 16384, 32768, 32795, 32799,
)
CONTROLLED_PAIRS = (
    (0, 32), (0, 8192), (32, 8224), (8192, 8224),
    (32795, 32799), (32827, 32831),
)
DEFAULT_EFFECTIVE_MASK = 32799
PREFETCH_PROFILES = ("on",)
ACCOUNTING_PROFILES = ("on", "off")
ROUNDS = 3
SEED = 73001
EXPLORATORY_PASS_COUNT = 3
REDUCED_PROCESS_COUNT = len(MASKS)
PREFETCH_DIAGNOSTIC_MASKS = (0, 4, 6, 38, 32799)
PREFETCH_DIAGNOSTIC_PROCESS_COUNT = len(PREFETCH_DIAGNOSTIC_MASKS)
CORRECTNESS_PROCESS_COUNT = 2
PERFORMANCE_PROCESS_COUNT = 6
RELEASE_PROCESS_COUNT = 6
RELEASE_CANDIDATE_PROCESS_COUNT = 6
RELEASE_CANDIDATE_MASK = 32795
DEFAULT_MATRIX_PROCESS_COUNT = (
    REDUCED_PROCESS_COUNT + CORRECTNESS_PROCESS_COUNT
    + PERFORMANCE_PROCESS_COUNT + RELEASE_PROCESS_COUNT
    + RELEASE_CANDIDATE_PROCESS_COUNT
)
DEFAULT_EXPECTED_PROCESS_COUNT = DEFAULT_MATRIX_PROCESS_COUNT
PROFILES = {
    "reduced-image-optimizations": {
        "masks": MASKS,
        "prefetch": PREFETCH_PROFILES,
        "accounting": ("off",),
        "rounds": 1,
        "expected_processes": REDUCED_PROCESS_COUNT,
        "passes": EXPLORATORY_PASS_COUNT,
        "exploratory": True,
        "workload_images": EXPECTED_JPEGS,
    },
    "scroll-raster-correctness": {
        "masks": (0,),
        "prefetch": ("on",),
        "accounting": ("on",),
        "rendering_reuse": ("off", "on"),
        "rounds": 1,
        "expected_processes": CORRECTNESS_PROCESS_COUNT,
        "passes": 2,
        "app_profile": "scroll-raster-reuse-poc",
        "workload_images": 120,
    },
    "scroll-raster-performance": {
        "masks": (0,),
        "prefetch": ("on",),
        "accounting": ("off",),
        "rendering_reuse": ("off", "on"),
        "rounds": ROUNDS,
        "expected_processes": PERFORMANCE_PROCESS_COUNT,
        "passes": 2,
        "app_profile": "scroll-raster-reuse-poc",
        "workload_images": 120,
    },
    "release-default-scroll": {
        "masks": (None,),
        "prefetch": ("on",),
        "accounting": ("off",),
        "rendering_reuse": ("off", "on"),
        "rounds": ROUNDS,
        "expected_processes": RELEASE_PROCESS_COUNT,
        "passes": 2,
        "default_effective_mask": DEFAULT_EFFECTIVE_MASK,
        "app_profile": "release-default-scroll",
        "workload_images": EXPECTED_JPEGS,
    },
    "release-candidate-scroll": {
        "masks": (RELEASE_CANDIDATE_MASK,),
        "prefetch": ("on",),
        "accounting": ("off",),
        "rendering_reuse": ("off", "on"),
        "rounds": ROUNDS,
        "expected_processes": RELEASE_CANDIDATE_PROCESS_COUNT,
        "passes": 2,
        "app_profile": "release-candidate-scroll",
        "workload_images": EXPECTED_JPEGS,
    },
    "prefetch-diagnostics": {
        "masks": PREFETCH_DIAGNOSTIC_MASKS,
        "prefetch": ("on",),
        "accounting": ("on",),
        "rounds": 1,
        "expected_processes": PREFETCH_DIAGNOSTIC_PROCESS_COUNT,
        "passes": 1,
        "diagnostic": True,
        "app_profile": "prefetch-diagnostics",
        "workload_images": EXPECTED_JPEGS,
    },
}
PROCESS_TIMEOUT_SECONDS = 180
FIXTURE = "ImageScrollRealWorkloadBenchmarkApp"
FNV_OFFSET = 0xCBF29CE484222325
FNV_PRIME = 0x100000001B3
TEMPORAL_SUMMARY_FIELDS = (
    "uiBuildElapsedNs", "prefetchElapsedNs", "durationNs",
    "frameTimeP50Ns", "frameTimeP90Ns", "frameTimeP95Ns", "frameTimeP99Ns",
    "frameTimeMaxNs", "workTimeP50Ns", "workTimeP95Ns", "workTimeP99Ns",
    "workTimeMaxNs", "paintTimeP50Ns", "paintTimeP95Ns", "paintTimeP99Ns",
    "paintTimeMaxNs", "largestStallNs",
)
FRAME_THRESHOLD_COUNT_FIELDS = (
    "framesOver16_67Count", "framesOver33_3Count",
    "framesOver50Count", "framesOver100Count",
)
FRAME_FIELDS = (
    "frame_index", "elapsed_ns", "frame_time_ns", "scroll_value",
    "scroll_work_ns", "paint_work_ns", "work_time_ns",
    "jpeg_decode_count", "jpeg_decode_ns", "jpeg_full_count", "jpeg_full_ns",
    "jpeg_half_count", "jpeg_half_ns", "jpeg_quarter_count", "jpeg_quarter_ns",
    "jpeg_eighth_count", "jpeg_eighth_ns", "jpeg_other_count", "jpeg_other_ns",
    "diagnostics_available", "visible_control_first", "visible_control_last",
    "visible_control_count", "visible_control_path_hash", "write_pixels_attempts",
    "write_pixels_hits", "write_pixels_fallbacks", "write_pixels_copied_bytes",
    "write_pixels_regular_attempts", "write_pixels_regular_hits",
    "write_pixels_regular_fallbacks", "write_pixels_regular_copied_bytes",
    "write_pixels_full_hits", "write_pixels_clipped_hits",
    "write_pixels_full_copied_bytes", "write_pixels_clipped_copied_bytes",
    "write_pixels_last_width", "write_pixels_last_height", "write_pixels_last_format",
    "write_pixels_total_ns", "write_pixels_preparation_ns", "write_pixels_copy_ns",
    "write_pixels_rgb565_conversion_ns",
    "image_materializations", "native_geometry_materializations", "target_color_attempts",
    "target_color_hits", "target_color_materializations", "target_color_fallbacks",
    "target_color_converted_bytes", "physical_variant_lookups", "physical_variant_hits",
    "physical_variant_misses", "physical_variant_materializations",
    "physical_variant_evictions", "physical_variant_bytes", "backing_live_bytes",
    "backing_peak_bytes", "rgba8888_bytes", "rgb565_bytes", "gray8_bytes", "argb4444_bytes",
    "scroll_jpeg_decode_count", "scroll_jpeg_decode_ns", "scroll_image_materializations",
    "scroll_native_geometry_materializations", "scroll_write_pixels_attempts",
    "scroll_write_pixels_hits", "scroll_write_pixels_fallbacks", "scroll_write_pixels_copied_bytes",
    "scroll_write_pixels_total_ns", "scroll_write_pixels_preparation_ns",
    "scroll_write_pixels_copy_ns", "scroll_write_pixels_rgb565_conversion_ns",
    "scroll_target_color_attempts", "scroll_target_color_hits",
    "scroll_physical_variant_lookups", "scroll_physical_variant_hits",
    "scroll_physical_variant_misses", "paint_jpeg_decode_count", "paint_jpeg_decode_ns",
    "paint_image_materializations", "paint_native_geometry_materializations",
    "paint_write_pixels_attempts", "paint_write_pixels_hits", "paint_write_pixels_fallbacks",
    "paint_write_pixels_copied_bytes", "paint_write_pixels_total_ns",
    "paint_write_pixels_preparation_ns", "paint_write_pixels_copy_ns",
    "paint_write_pixels_rgb565_conversion_ns", "paint_target_color_attempts",
    "paint_target_color_hits",
    "paint_physical_variant_lookups", "paint_physical_variant_hits",
    "paint_physical_variant_misses",
)
WRITE_PIXELS_TIMING_FIELDS = (
    "write_pixels_total_ns", "write_pixels_preparation_ns",
    "write_pixels_copy_ns", "write_pixels_rgb565_conversion_ns",
)
DIAGNOSTIC_SUMMARY_FIELDS = (
    "jpeg_decode_count", "jpeg_decode_ns", "jpeg_full_count", "jpeg_half_count",
    "jpeg_quarter_count", "jpeg_eighth_count", "write_pixels_attempts",
    "write_pixels_hits", "write_pixels_fallbacks", "write_pixels_device_1to1_candidates",
    "write_pixels_device_1to1_known_opaque_candidates", "write_pixels_reject_matrix",
    "write_pixels_reject_save_count", "write_pixels_reject_size_mismatch",
    "write_pixels_regular_attempts", "write_pixels_regular_hits",
    "write_pixels_regular_fallbacks", "write_pixels_regular_copied_bytes",
    "write_pixels_regular_clipped_hits",
)
PREFETCH_PHASE_SUMMARY_FIELDS = (
    ("corpusFileEnumerationElapsedNs", "corpus_file_enumeration_ns"),
    ("imageLoadElapsedNs", "image_load_ns"),
    ("imageScaleElapsedNs", "image_scale_ns"),
    ("imageControlAttachElapsedNs", "image_control_attach_ns"),
)
PREFETCH_DIAGNOSTIC_COUNTER_FIELDS = (
    ("imageMaterializations", "prefetch_image_materializations"),
    ("nativeGeometryMaterializations", "prefetch_native_geometry_materializations"),
    ("targetColorMaterializations", "prefetch_target_color_materializations"),
    ("targetColorConvertedBytes", "prefetch_target_color_converted_bytes"),
    ("physicalVariantStores", "prefetch_physical_variant_stores"),
    ("physicalVariantBytes", "prefetch_physical_variant_bytes"),
)
SAVE_COUNT_BUCKETS = ("0", "1", "2", "3", "4", "5OrMore")
MAPPING_SUBREASONS = (
    "CompileGeometry", "MatrixInversion", "RootToDevice", "DestinationAxis",
    "DestinationFractional", "SourceMapping", "ValidRoot", "UnsupportedTransform",
    "ExplicitClip", "VisibleMapping",
)
POLICY_DIAGNOSTIC_COUNTERS = (
    "physicalIdentityRejectCanvas", "physicalIdentityRejectSurface",
    "physicalIdentityRejectClip",
    "physicalIdentityRejectMapping", "physicalIdentityRejectBacking",
    "physicalIdentityRejectExecution", "targetColorUniqueSources",
    "targetColorFallbacks", "targetColorConvertedBytes",
    "targetColorUniqueFullKeys", "targetColorUniqueNoDestinationKeys",
    "targetColorUniqueIntrinsicKeys", "targetColorAcquisitionSources",
    "targetColorPendingReplacements",
    "targetColorRejectCanvas", "targetColorRejectSurface", "targetColorRejectClip",
    "targetColorRejectMapping", "targetColorRejectBacking",
    "targetColorRejectExecution", "physicalVariantUniqueFullKeys",
    "physicalVariantUniqueNoSurfaceSizeKeys", "physicalVariantPendingReplacements",
    "physicalVariantUniqueSources",
    "physicalVariantEvictions",
    "physicalVariantRejectCanvas", "physicalVariantRejectSurface",
    "physicalVariantRejectClip",
    "physicalVariantRejectMapping", "physicalVariantRejectBacking",
    "physicalVariantRejectExecution", "sharedSlotTargetToPhysical",
    "sharedSlotPhysicalToTarget", "sharedPendingTargetToPhysical",
    "sharedPendingPhysicalToTarget",
)
POLICY_DIAGNOSTIC_COUNTERS += tuple(
    f"{feature}SaveCount{bucket}"
    for feature in ("targetColor", "physicalVariant")
    for bucket in SAVE_COUNT_BUCKETS
)
POLICY_DIAGNOSTIC_COUNTERS += tuple(
    f"physicalIdentitySaveCount{bucket}" for bucket in SAVE_COUNT_BUCKETS
)
POLICY_DIAGNOSTIC_COUNTERS += tuple(
    f"{feature}Mapping{subreason}"
    for feature in ("targetColor", "physicalVariant", "physicalIdentity")
    for subreason in MAPPING_SUBREASONS
)
MEMORY_FIELDS = (
    "checkpoint", "elapsed_ns", "current_resident_bytes", "peak_resident_bytes",
    "private_bytes", "phys_footprint_bytes",
)
TIMELINE_FIELDS = ("event", "elapsed_ns", "value")


class BenchmarkFailure(RuntimeError):
    pass


class FatalBenchmarkFailure(BenchmarkFailure):
    """An infrastructure or execution failure that must stop the suite."""


def benchmark_pass_names(pass_count):
    if pass_count == 1:
        return ("cold",)
    if pass_count == 3:
        return ("cold-forward", "warm-reverse", "warm-forward")
    raise BenchmarkFailure(f"unsupported non-reuse pass count: {pass_count}")


RESULTS_STATE_FILE = "execution-state.json"
VALIDATION_FAILURES_FILE = "validation-failures.jsonl"
PHYSICAL_TARGET_BASELINE_FILE = "physical-target-baseline.json"
VALID_RESUME_STATUSES = frozenset({
    "SELF_TEST_PASS", "PASS", "PASS_WITH_VALIDATION_FAILURES",
})


def require(condition, message):
    if not condition:
        raise BenchmarkFailure(message)


def manifest_package_target(manifest):
    return manifest.get("packageTarget") or manifest.get("target")


def describe_results_os_error(operation, path, error):
    details = []
    winerror = getattr(error, "winerror", None)
    if winerror is not None:
        details.append(f"Windows error {winerror}")
    errno_value = getattr(error, "errno", None)
    if errno_value is not None:
        details.append(f"errno {errno_value}")
    text = getattr(error, "strerror", None) or str(error)
    if text:
        details.append(text)
    detail = ", ".join(details) if details else "unknown OS error"
    return (
        f"cannot {operation} results path {path}: {detail}. "
        "Check that the bundle is local and that the current user can "
        "create, read, write, and delete files in results."
    )


def write_json_file(path, payload, description):
    try:
        path.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n",
                        encoding="utf-8")
    except OSError as error:
        raise FatalBenchmarkFailure(
            describe_results_os_error(f"write {description}", path, error)
        ) from error


def read_json_file(path, description):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        raise BenchmarkFailure(f"invalid {description}: {path}") from error


def write_execution_state(output, status, manifest, **fields):
    state = {
        "schemaVersion": 1,
        "fixture": FIXTURE,
        "status": status,
        "packageTarget": manifest_package_target(manifest),
        "sourceCommit": manifest.get("sourceCommit"),
        "sdkSourceAttestation": manifest.get("sdkSourceAttestation"),
        "runtimeSha256": manifest.get("runtimeSha256"),
        "expectedProcessCount": DEFAULT_EXPECTED_PROCESS_COUNT,
        **fields,
    }
    write_json_file(output / RESULTS_STATE_FILE, state, "execution state")


def inspect_results_state(output, manifest=None):
    if not output.exists():
        return "CLEAN_START", "results directory does not exist"
    if not output.is_dir():
        return "PARTIAL_INVALID", "results path exists but is not a directory"
    state_path = output / RESULTS_STATE_FILE
    marker_path = output / "self-test.json"
    if not state_path.is_file() or not marker_path.is_file():
        return (
            "PARTIAL_INVALID",
            "results lacks both a valid execution-state.json and self-test.json",
        )
    try:
        state = json.loads(state_path.read_text(encoding="utf-8"))
        marker = json.loads(marker_path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        return "PARTIAL_INVALID", f"results state or self-test is not valid JSON: {error}"
    if state.get("fixture") != FIXTURE:
        return "PARTIAL_INVALID", "execution-state.json belongs to another fixture"
    if marker.get("fixture") != FIXTURE or marker.get("status") != "PASS":
        return "PARTIAL_INVALID", "self-test.json is not a passing fixture marker"
    status = state.get("status")
    if status not in VALID_RESUME_STATUSES:
        return "PARTIAL_INVALID", f"execution state is {status!r}, not resumable"
    if manifest is not None:
        if state.get("sourceCommit") != manifest.get("sourceCommit"):
            return "PARTIAL_INVALID", "execution state source provenance differs from bundle"
        if marker.get("sourceCommit") != manifest.get("sourceCommit"):
            return "PARTIAL_INVALID", "self-test source provenance differs from bundle"
        if marker.get("datasetFileCount") != EXPECTED_JPEGS:
            return "PARTIAL_INVALID", "self-test dataset count differs from bundle"
        if marker.get("sdkJarSha256Compile") != manifest.get("sdkJarSha256Compile"):
            return "PARTIAL_INVALID", "self-test SDK compile hash differs from bundle"
        if marker.get("runtimeSha256") != manifest.get("runtimeSha256"):
            return "PARTIAL_INVALID", "self-test runtime hash differs from bundle"
    return "VALID_RESUME", f"execution state is {status}"


def probe_results_directory(output):
    probe = output / f".runner-results-probe-{os.getpid()}-{time.time_ns()}"
    payload = "image-scroll-runner-results-probe\n"
    try:
        with probe.open("x", encoding="utf-8") as destination:
            destination.write(payload)
        if probe.read_text(encoding="utf-8") != payload:
            raise OSError("probe contents changed during read")
        probe.unlink()
    except OSError as error:
        try:
            if probe.exists():
                probe.unlink()
        except OSError:
            pass
        raise FatalBenchmarkFailure(
            describe_results_os_error("create/read/write/delete", probe, error)
        ) from error


def physical_target_from_environment(environment, description):
    target = {}
    for field in PHYSICAL_TARGET_FIELDS:
        value = environment.get(field)
        if field in ("skiaSurfaceColorClassification", "rendererBackend"):
            require(isinstance(value, str) and value,
                    f"{description} lacks {field}")
        else:
            require(type(value) is int and value >= 0,
                    f"{description} has invalid {field}")
        target[field] = value
    require(target["skiaSurfaceWidth"] > 0 and target["skiaSurfaceHeight"] > 0,
            f"{description} has an invalid physical target size")
    require(target["skiaSurfaceRowBytes"] > 0 and target["skiaSurfacePixelBytes"] > 0,
            f"{description} has invalid physical target storage metrics")
    require(target["skiaSurfaceColorClassification"] in (
        "BGRA8888", "RGB565", "OTHER",
    ), f"{description} lacks target-color classification")
    require(target["rendererBackend"] in ("software", "gpu"),
            f"{description} lacks renderer backend")
    minimum_row_bytes = {
        "BGRA8888": target["skiaSurfaceWidth"] * 4,
        "RGB565": target["skiaSurfaceWidth"] * 2,
        "OTHER": target["skiaSurfaceWidth"],
    }[target["skiaSurfaceColorClassification"]]
    require(target["skiaSurfaceRowBytes"] >= minimum_row_bytes,
            f"{description} has an inconsistent target pitch")
    return target


def _optional_text(value):
    if not isinstance(value, str):
        return None
    value = value.strip()
    return value or None


def host_cpu_model():
    candidates = []
    for getter in (platform.processor, lambda: platform.uname().processor):
        try:
            candidates.append(getter())
        except Exception:
            pass
    try:
        host_os = platform.system()
    except Exception:
        host_os = None
    if host_os == "Windows":
        candidates.insert(0, os.environ.get("PROCESSOR_IDENTIFIER"))
    if host_os == "Linux":
        try:
            for line in Path("/proc/cpuinfo").read_text(
                    encoding="utf-8", errors="replace").splitlines():
                key, separator, value = line.partition(":")
                if separator and key.strip().lower() in ("model name", "hardware"):
                    candidates.insert(0, value)
                    break
        except OSError:
            pass
    for candidate in candidates:
        candidate = _optional_text(candidate)
        if candidate and candidate.lower() not in ("unknown", "unavailable"):
            return candidate
    return None


def collect_host_environment_metadata(manifest):
    try:
        host_os = platform.system()
    except Exception:
        host_os = None
    try:
        host_os_version = platform.release()
    except Exception:
        host_os_version = None
    try:
        host_architecture = platform.machine()
    except Exception:
        host_architecture = None
    return {
        "packageTarget": manifest_package_target(manifest),
        "hostOs": _optional_text(host_os),
        "hostOsVersion": _optional_text(host_os_version),
        "hostArchitecture": _optional_text(host_architecture),
        "endianness": sys.byteorder,
        "cpuModel": host_cpu_model(),
    }


def validate_environment_baseline_values(values, target, description,
                                         package_target=None):
    require(isinstance(values, dict), f"{description} is missing")
    for field in ENVIRONMENT_BASELINE_FIELDS:
        require(field in values, f"{description} lacks {field}")
    if package_target is not None:
        require(values["packageTarget"] == package_target,
                f"{description} packageTarget differs from the bundle")
    for field in ("packageTarget", "hostOs", "totalCrossPlatform"):
        require(isinstance(values[field], str) and values[field],
                f"{description} lacks {field}")
    for field in ("hostOsVersion", "hostArchitecture", "endianness", "cpuModel"):
        require(values[field] is None or (
            isinstance(values[field], str) and bool(values[field])
        ), f"{description} has invalid {field}")
    for field in ENVIRONMENT_INTEGER_FIELDS:
        value = values[field]
        require(value is None or (type(value) is int and value > 0),
                f"{description} has invalid {field}")
    for field in ENVIRONMENT_FLOAT_FIELDS:
        value = values[field]
        require(type(value) in (int, float) and math.isfinite(value) and value > 0,
                f"{description} has invalid {field}")
    require(math.isclose(
        values["surfaceScaleX"], target["skiaSurfaceWidth"] / LOGICAL_TARGET["width"],
        rel_tol=0.0, abs_tol=1e-12,
    ), f"{description} surfaceScaleX is not derived from the Skia surface")
    require(math.isclose(
        values["surfaceScaleY"], target["skiaSurfaceHeight"] / LOGICAL_TARGET["height"],
        rel_tol=0.0, abs_tol=1e-12,
    ), f"{description} surfaceScaleY is not derived from the Skia surface")
    return dict(values)


def environment_baseline_from_environment(environment, manifest, description):
    target = physical_target_from_environment(environment, description)
    values = {
        field: environment.get(field) for field in ENVIRONMENT_BASELINE_FIELDS
    }
    return validate_environment_baseline_values(
        values, target, description, manifest_package_target(manifest)
    )


def enrich_environment_metadata(output, manifest):
    path = output / "environment.json"
    environment = read_json_file(path, "benchmark environment")
    require(isinstance(environment, dict), f"{path} must contain an object")
    environment.update(collect_host_environment_metadata(manifest))
    write_json_file(path, environment, "benchmark environment metadata")
    return environment


def validate_physical_target(environment, baseline, description, manifest=None):
    require(environment.get("expectedLogicalWidth") == LOGICAL_TARGET["width"]
            and environment.get("expectedLogicalHeight") == LOGICAL_TARGET["height"]
            and environment.get("effectiveLogicalWidth") == LOGICAL_TARGET["width"]
            and environment.get("effectiveLogicalHeight") == LOGICAL_TARGET["height"],
            f"{description} is not 540x960")
    target = physical_target_from_environment(environment, description)
    if manifest is not None:
        environment_baseline_from_environment(environment, manifest, description)
    if baseline is not None:
        for field in PHYSICAL_TARGET_FIELDS:
            require(target[field] == baseline[field],
                    f"{description} {field} differs from the execution baseline: "
                    f"{target[field]!r} != {baseline[field]!r}")
    return target


def load_physical_target_baseline(output, manifest=None):
    path = output / PHYSICAL_TARGET_BASELINE_FILE
    if not path.is_file():
        return None
    payload = read_json_file(path, "physical target baseline")
    require(payload.get("fixture") == FIXTURE,
            f"{path} belongs to another fixture")
    require(payload.get("logicalTarget") == LOGICAL_TARGET,
            f"{path} logical target differs from 540x960")
    target = payload.get("physicalTarget")
    require(isinstance(target, dict), f"{path} physical target is missing")
    target = physical_target_from_environment(target, str(path))
    environment_baseline = payload.get("environmentBaseline")
    require(isinstance(environment_baseline, dict),
            f"{path} environment baseline is missing")
    validate_environment_baseline_values(
        environment_baseline, target, f"{path} environment baseline",
        manifest_package_target(manifest) if manifest is not None else None,
    )
    return target


def load_environment_baseline(output, manifest=None):
    path = output / PHYSICAL_TARGET_BASELINE_FILE
    if not path.is_file():
        return None
    payload = read_json_file(path, "physical target baseline")
    target = physical_target_from_environment(
        payload.get("physicalTarget", {}), str(path)
    )
    values = payload.get("environmentBaseline")
    return validate_environment_baseline_values(
        values, target, f"{path} environment baseline",
        manifest_package_target(manifest) if manifest is not None else None,
    )


def capture_physical_target_baseline(output, environment, configuration, manifest=None):
    baseline = load_physical_target_baseline(output, manifest)
    try:
        target = physical_target_from_environment(
            environment, str(output / "environment.json")
        )
    except BenchmarkFailure:
        return baseline
    environment_baseline = None
    if manifest is not None:
        environment_baseline = environment_baseline_from_environment(
            environment, manifest, str(output / "environment.json")
        )
    if baseline is None:
        payload = {
            "schemaVersion": 1,
            "fixture": FIXTURE,
            "logicalTarget": dict(LOGICAL_TARGET),
            "physicalTarget": target,
            "environmentBaseline": environment_baseline,
            "firstProcess": configuration,
        }
        write_json_file(output / PHYSICAL_TARGET_BASELINE_FILE, payload,
                        "physical target baseline")
        return target
    for field in PHYSICAL_TARGET_FIELDS:
        require(target[field] == baseline[field],
                f"{output / 'environment.json'} {field} differs from the "
                f"execution baseline: {target[field]!r} != {baseline[field]!r}")
    if manifest is not None:
        baseline_payload = read_json_file(
            output / PHYSICAL_TARGET_BASELINE_FILE, "physical target baseline"
        )
        baseline_environment = baseline_payload.get("environmentBaseline")
        for field in ENVIRONMENT_BASELINE_FIELDS:
            require(environment_baseline[field] == baseline_environment[field],
                    f"{output / 'environment.json'} {field} differs from the "
                    f"execution baseline: {environment_baseline[field]!r} != "
                    f"{baseline_environment[field]!r}")
    return baseline


def physical_summary_fields(target):
    fields = {
        "logical_width": LOGICAL_TARGET["width"],
        "logical_height": LOGICAL_TARGET["height"],
    }
    if target is None:
        fields.update({
            "physical_width": None, "physical_height": None,
            "physical_row_bytes": None, "physical_pixel_bytes": None,
            "physical_color_type": None, "physical_alpha_type": None,
            "physical_color_classification": None, "renderer_backend": None,
        })
    else:
        fields.update({
            "physical_width": target["skiaSurfaceWidth"],
            "physical_height": target["skiaSurfaceHeight"],
            "physical_row_bytes": target["skiaSurfaceRowBytes"],
            "physical_pixel_bytes": target["skiaSurfacePixelBytes"],
            "physical_color_type": target["skiaSurfaceColorType"],
            "physical_alpha_type": target["skiaSurfaceAlphaType"],
            "physical_color_classification": target["skiaSurfaceColorClassification"],
            "renderer_backend": target["rendererBackend"],
        })
    return fields


def configuration_key(profile, run, mask, prefetch, accounting, rendering_reuse=None):
    return json.dumps(
        [profile, run, mask, prefetch, accounting, rendering_reuse],
        separators=(",", ":"),
    )


def append_failure_record(output, profile, run, mask, prefetch, accounting,
                          rendering_reuse, error, log_path, artifact_paths,
                          status, classification, fatal, scope="process"):
    configuration = {
        "profile": profile,
        "run": run,
        "mask": mask,
        "prefetch": prefetch,
        "accounting": accounting,
        "renderingReuse": rendering_reuse,
    }
    record = {
        "status": status,
        "classification": classification,
        "fatal": fatal,
        "scope": scope,
        "configurationKey": configuration_key(
            profile, run, mask, prefetch, accounting, rendering_reuse
        ),
        "configuration": configuration,
        "messages": [str(error)],
        "logPath": str(log_path),
        "artifactPaths": [str(path) for path in artifact_paths],
    }
    path = output / VALIDATION_FAILURES_FILE
    try:
        with path.open("a", encoding="utf-8") as destination:
            destination.write(json.dumps(record, sort_keys=True) + "\n")
    except OSError as write_error:
        raise FatalBenchmarkFailure(
            describe_results_os_error("preserve validation failure in", path, write_error)
        ) from write_error
    return record


def append_validation_failure(output, profile, run, mask, prefetch, accounting,
                              rendering_reuse, error, log_path, artifact_paths,
                              scope="process"):
    record = append_failure_record(
        output, profile, run, mask, prefetch, accounting, rendering_reuse,
        error, log_path, artifact_paths, "VALIDATION_FAILED",
        "NON_FATAL_VALIDATION", False, scope,
    )
    print(
        f"WARNING {profile} run={run} mask={mask} reuse={rendering_reuse} "
        f"VALIDATION_FAILED; log={log_path}; error={error}",
        file=sys.stderr,
    )
    return record


def append_execution_failure(output, profile, run, mask, prefetch, accounting,
                             rendering_reuse, error, log_path, artifact_paths):
    record = append_failure_record(
        output, profile, run, mask, prefetch, accounting, rendering_reuse,
        error, log_path, artifact_paths, "INCOMPLETE", "FATAL_EXECUTION", True,
    )
    print(
        f"ERROR {profile} run={run} mask={mask} reuse={rendering_reuse} "
        f"FATAL_EXECUTION; log={log_path}; error={error}",
        file=sys.stderr,
    )
    return record


def load_validation_failures(output):
    path = output / VALIDATION_FAILURES_FILE
    if not path.is_file():
        return {}
    failures = {}
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except OSError as error:
        raise FatalBenchmarkFailure(
            describe_results_os_error("read", path, error)
        ) from error
    for line_number, line in enumerate(lines, start=1):
        if not line.strip():
            continue
        try:
            record = json.loads(line)
        except ValueError as error:
            raise FatalBenchmarkFailure(
                f"invalid validation failure record at {path}:{line_number}"
            ) from error
        key = record.get("configurationKey")
        require(isinstance(key, str) and key,
                f"validation failure record at {path}:{line_number} lacks configurationKey")
        failures[key] = record
    return failures


def new_execution_tracker():
    return {
        "plannedProcessCount": 0,
        "launchedProcessCount": 0,
        "completedProcessCount": 0,
        "validProcessCount": 0,
        "validationFailedProcessCount": 0,
    }


def track_process_start(tracker):
    if tracker is not None:
        tracker["launchedProcessCount"] += 1


def track_process_complete(tracker, status):
    if tracker is None:
        return
    tracker["completedProcessCount"] += 1
    if status == "VALIDATION_FAILED":
        tracker["validationFailedProcessCount"] += 1
    else:
        tracker["validProcessCount"] += 1


def validation_failure_message(record):
    return "; ".join(record.get("messages", ())) or "validation failed"


def preflight(bundle, manifest, output, phase):
    validate_bundle(bundle, manifest)
    state, detail = inspect_results_state(output, manifest)
    if state == "PARTIAL_INVALID":
        raise FatalBenchmarkFailure(
            f"results state is partial/invalid for {bundle}: {detail}; "
            "preserve the directory for review and start with a fresh bundle "
            "or restore a complete self-test state"
        )
    if phase not in ("self-test", "full") and state == "CLEAN_START":
        raise FatalBenchmarkFailure(
            f"results state is clean for {output}; run --phase self-test "
            "before resuming benchmark processes"
        )
    try:
        output.mkdir(parents=True, exist_ok=True)
    except OSError as error:
        raise FatalBenchmarkFailure(
            describe_results_os_error("create", output, error)
        ) from error
    probe_results_directory(output)
    if (output / PHYSICAL_TARGET_BASELINE_FILE).is_file():
        try:
            load_physical_target_baseline(output, manifest)
        except BenchmarkFailure as error:
            raise FatalBenchmarkFailure(
                f"results state is partial/invalid: {error}; "
                "preserve the directory for review and start with a fresh "
                "bundle or restore a complete baseline"
            ) from error
    if state == "CLEAN_START":
        write_execution_state(output, "PREFLIGHT_PASS", manifest,
                              resultsState="CLEAN_START")
    print(f"results preflight passed,state={state.lower()},phase={phase},path={output}")
    return state


def profile_config(name):
    try:
        profile = PROFILES[name]
    except KeyError as error:
        raise BenchmarkFailure(f"unknown benchmark profile: {name}") from error
    return dict(profile, name=name)


def load_manifest(bundle):
    path = bundle / "manifest.json"
    require(path.is_file(), f"manifest not found: {path}")
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        raise BenchmarkFailure(f"invalid manifest: {path}") from error
    require(manifest.get("benchmark") == "image-scroll", "unexpected benchmark manifest")
    require(manifest.get("datasetFileCount") == EXPECTED_JPEGS,
            "manifest datasetFileCount is not 663")
    require(tuple(manifest.get("masks", ())) == MASKS, "manifest mask matrix differs")
    require(tuple(manifest.get("prefetchProfiles", ())) == PREFETCH_PROFILES,
            "manifest prefetch matrix differs")
    require(manifest.get("rounds") == ROUNDS, "manifest rounds differs")
    require(manifest.get("seed") == SEED, "manifest seed differs")
    require(manifest.get("expectedProcessCount") == DEFAULT_EXPECTED_PROCESS_COUNT,
            "manifest expectedProcessCount differs")
    require(manifest.get("matrixProcessCount") == DEFAULT_MATRIX_PROCESS_COUNT,
            "manifest matrixProcessCount differs")
    require(manifest.get("exploratoryPassCount") == EXPLORATORY_PASS_COUNT,
            "manifest exploratoryPassCount differs")
    require(tuple(manifest.get("prefetchDiagnosticMasks", ()))
            == PREFETCH_DIAGNOSTIC_MASKS,
            "manifest prefetch diagnostic masks differ")
    require(manifest.get("prefetchDiagnosticProcessCount")
            == PREFETCH_DIAGNOSTIC_PROCESS_COUNT,
            "manifest prefetch diagnostic process count differs")
    require(manifest.get("selfTestPhaseCount") == 1,
            "manifest selfTestPhaseCount differs")
    require(manifest.get("includeDecodeAssets") in (False, True),
            "manifest includeDecodeAssets must be boolean")
    package_target = manifest_package_target(manifest)
    require(isinstance(package_target, str) and package_target,
            "manifest package target is missing")
    expected_corpus_variants = (DECODE_CORPUS_VARIANTS
                                if manifest["includeDecodeAssets"]
                                else SCROLL_CORPUS_VARIANTS)
    require(tuple(manifest.get("corpusVariants", ())) == expected_corpus_variants,
            "manifest corpus variants differ")
    expected_profiles = {
        "reduced-image-optimizations": {
            "masks": list(MASKS), "prefetch": ["on"], "accounting": ["off"],
            "renderingReuse": [], "rounds": 1, "processCount": REDUCED_PROCESS_COUNT,
            "passes": EXPLORATORY_PASS_COUNT,
            "workloadImages": EXPECTED_JPEGS,
        },
        "scroll-raster-correctness": {
            "masks": [0], "prefetch": ["on"], "accounting": ["on"],
            "renderingReuse": ["off", "on"], "rounds": 1,
            "processCount": CORRECTNESS_PROCESS_COUNT,
            "workloadImages": 120,
        },
        "scroll-raster-performance": {
            "masks": [0], "prefetch": ["on"], "accounting": ["off"],
            "renderingReuse": ["off", "on"], "rounds": ROUNDS,
            "processCount": PERFORMANCE_PROCESS_COUNT,
            "workloadImages": 120,
        },
        "release-default-scroll": {
            "masks": ["default"], "prefetch": ["on"], "accounting": ["off"],
            "renderingReuse": ["off", "on"], "rounds": ROUNDS,
            "processCount": RELEASE_PROCESS_COUNT,
            "defaultEffectiveMask": DEFAULT_EFFECTIVE_MASK,
            "workloadImages": EXPECTED_JPEGS,
        },
        "release-candidate-scroll": {
            "masks": [RELEASE_CANDIDATE_MASK], "prefetch": ["on"], "accounting": ["off"],
            "renderingReuse": ["off", "on"], "rounds": ROUNDS,
            "processCount": RELEASE_CANDIDATE_PROCESS_COUNT,
            "workloadImages": EXPECTED_JPEGS,
        },
        "prefetch-diagnostics": {
            "masks": list(PREFETCH_DIAGNOSTIC_MASKS), "prefetch": ["on"],
            "accounting": ["on"], "renderingReuse": [], "rounds": 1,
            "processCount": PREFETCH_DIAGNOSTIC_PROCESS_COUNT, "passes": 1,
            "workloadImages": EXPECTED_JPEGS,
        },
    }
    require(manifest.get("profiles") == expected_profiles,
            "manifest profile matrix differs")
    source_commit = manifest.get("sourceCommit")
    require(isinstance(source_commit, str) and len(source_commit) >= 7,
            "manifest sourceCommit is missing")
    runtime_sha = manifest.get("runtimeSha256")
    require(isinstance(runtime_sha, str) and len(runtime_sha) == 64,
            "manifest runtimeSha256 is missing")
    if package_target == "windows-x64":
        require(isinstance(manifest.get("tcvmSha256"), str)
                and len(manifest["tcvmSha256"]) == 64,
                "Windows manifest tcvmSha256 is missing")
        require(manifest.get("sdkSourceAttestation") == source_commit,
                "Windows manifest SDK source attestation differs from benchmark source")
    require(manifest.get("screenArgument") == SCREEN_ARGUMENT,
            "manifest screen argument differs")
    return manifest


def jpeg_paths(corpus):
    return sorted(
        path for path in corpus.rglob("*")
        if path.is_file() and path.suffix.lower() in (".jpg", ".jpeg")
    )


def dataset_hash(corpus, paths):
    value = FNV_OFFSET
    for path in paths:
        relative = path.relative_to(corpus).as_posix().encode("utf-8")
        for byte in relative + b"\0":
            value = ((value ^ byte) * FNV_PRIME) & 0xFFFFFFFFFFFFFFFF
        with path.open("rb") as source:
            while True:
                chunk = source.read(16384)
                if not chunk:
                    break
                for byte in chunk:
                    value = ((value ^ byte) * FNV_PRIME) & 0xFFFFFFFFFFFFFFFF
    return f"{value:016x}"


def sha256_file(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def executable_path(bundle, manifest):
    name = manifest.get("executable")
    require(isinstance(name, str) and name, "manifest executable is missing")
    executable = bundle / name
    require(executable.is_file(), f"bundle executable not found: {executable}")
    return executable


def validate_bundle(bundle, manifest):
    corpus = bundle / "corpus"
    require(corpus.is_dir(), f"bundle corpus not found: {corpus}")
    expected_variants = (DECODE_CORPUS_VARIANTS if manifest["includeDecodeAssets"]
                         else SCROLL_CORPUS_VARIANTS)
    require(tuple(sorted(path.name for path in corpus.iterdir() if path.is_dir()))
            == tuple(sorted(expected_variants)),
            "bundle corpus variants differ from manifest")
    images = jpeg_paths(corpus / "imag")
    require(len(images) == EXPECTED_JPEGS,
            "bundle corpus imag must contain exactly 663 JPEG files")
    base_names = [path.relative_to(corpus / "imag").as_posix() for path in images]
    require(dataset_hash(corpus / "imag", images) == manifest.get("datasetHash"),
            "bundle dataset hash differs from manifest")
    decode_assets = (
        *(bundle / name for name in DECODE_LIBRARIES.values()),
        bundle / "ImageDecodeBenchmarkApp.tcz",
        bundle / "run-image-decode-benchmark.py",
        bundle / "aggregate-image-decode-benchmark.py",
    )
    if manifest["includeDecodeAssets"]:
        require(tuple(sorted(path.name for path in corpus.iterdir() if path.is_dir()))
                == tuple(sorted(DECODE_CORPUS_VARIANTS)),
                "decode-enabled bundle must contain six corpus variants")
        for variant in DECODE_CORPUS_VARIANTS:
            variant_root = corpus / variant
            variant_files = sorted(path for path in variant_root.rglob("*") if path.is_file())
            variant_images = jpeg_paths(variant_root)
            names = [path.relative_to(variant_root).as_posix() for path in variant_images]
            require(len(variant_files) == EXPECTED_JPEGS
                    and len(variant_images) == EXPECTED_JPEGS,
                    f"bundle corpus {variant} must contain exactly 663 files")
            require(names == base_names, f"bundle corpus {variant} names differ from imag")
        for variant, library_name in DECODE_LIBRARIES.items():
            library = bundle / library_name
            require(library.is_file() and library.stat().st_size > 0,
                    f"decode library is missing: {library_name}")
        decode_executable = manifest.get("decodeExecutable")
        require(isinstance(decode_executable, str) and decode_executable
                and (bundle / decode_executable).is_file(),
                "decode benchmark executable is missing")
        decode_tcz = manifest.get("decodeApplicationTcz")
        require(decode_tcz == "ImageDecodeBenchmarkApp.tcz"
                and (bundle / decode_tcz).is_file(),
                "decode application TCZ is missing")
        decode_runner = manifest.get("decodeRunner")
        require(decode_runner == "run-image-decode-benchmark.py"
                and (bundle / decode_runner).is_file(),
                "decode benchmark runner is missing")
        decode_aggregator = manifest.get("decodeAggregator")
        require(decode_aggregator == "aggregate-image-decode-benchmark.py"
                and (bundle / decode_aggregator).is_file(),
                "decode benchmark aggregator is missing")
    else:
        require(not any(path.exists() for path in decode_assets),
                "decode assets are present in a package without includeDecodeAssets")
    require(not (bundle / "device").exists(),
            "bundle must not contain a physical device directory")
    require(manifest.get("chime") == "chime.mp3",
            "manifest chime path must be the application root")
    chime = bundle / "chime.mp3"
    require(chime.is_file() and chime.stat().st_size > 0,
            "official root chime.mp3 is missing")
    require(sha256_file(chime) == manifest.get("chimeSha256"),
            "bundle chime differs from manifest")
    executable = executable_path(bundle, manifest)
    runtime = manifest.get("runtime")
    require(isinstance(runtime, str) and (bundle / runtime).is_file(),
            "bundle native runtime is missing")
    runtime_sha = sha256_file(bundle / runtime)
    require(runtime_sha == manifest["runtimeSha256"],
            "bundle native runtime differs from manifest")
    if manifest_package_target(manifest) == "windows-x64":
        require(runtime == "tcvm.dll" and runtime_sha == manifest["tcvmSha256"],
                "Windows bundle tcvm.dll provenance differs from manifest")
    compile_hash = manifest.get("sdkJarSha256Compile")
    deploy_hash = manifest.get("sdkJarSha256Deploy")
    require(isinstance(compile_hash, str) and len(compile_hash) == 64,
            "compile SDK SHA-256 is missing")
    require(isinstance(deploy_hash, str) and len(deploy_hash) == 64,
            "deploy SDK SHA-256 is missing")
    require(compile_hash == deploy_hash == manifest.get("sdkJarSha256"),
            "compile/deploy SDK SHA-256 values differ")
    return corpus, images, dataset_hash(corpus / "imag", images), executable


def self_test(bundle, manifest, output):
    corpus, images, corpus_digest, executable = validate_bundle(bundle, manifest)
    marker = {
        "fixture": FIXTURE,
        "status": "PASS",
        "screenArgument": SCREEN_ARGUMENT,
        "datasetFileCount": len(images),
        "datasetHash": corpus_digest,
        "sdkJarSha256Compile": manifest["sdkJarSha256Compile"],
        "sdkJarSha256Deploy": manifest["sdkJarSha256Deploy"],
        "sourceCommit": manifest["sourceCommit"],
        "sdkSourceAttestation": manifest.get("sdkSourceAttestation"),
        "runtimeSha256": manifest["runtimeSha256"],
        "packageTarget": manifest_package_target(manifest),
        "tcvmSha256": manifest.get("tcvmSha256"),
        "executable": str(executable.relative_to(bundle)),
        "runtime": manifest["runtime"],
    }
    write_json_file(output / "self-test.json", marker, "self-test marker")
    write_execution_state(
        output, "SELF_TEST_PASS", manifest,
        resultsState="VALID_RESUME", datasetFileCount=len(images),
        datasetHash=corpus_digest,
    )
    print("self-test passed,screen=540x960,corpus_jpegs=663,"
          f"sdk_jar_sha256={manifest['sdkJarSha256Compile']}")
    return corpus, images, corpus_digest


def parse_record(line):
    record = {}
    for field in line.split(","):
        key, separator, value = field.partition("=")
        if separator:
            record[key] = value
    return record


def require_nonnegative_ns(value, description):
    require(type(value) is int and value >= 0,
            f"{description} must be a non-negative integer in ns")


def require_record_ns(record, key, description):
    value = record.get(key)
    try:
        value = int(value)
    except (TypeError, ValueError) as error:
        raise BenchmarkFailure(f"{description} must be an integer in ns") from error
    require_nonnegative_ns(value, description)


def require_nonnegative_count(value, description):
    require(type(value) is int and value >= 0,
            f"{description} must be a non-negative integer count")


def require_record_count(record, key, description):
    value = record.get(key)
    try:
        value = int(value)
    except (TypeError, ValueError) as error:
        raise BenchmarkFailure(f"{description} must be an integer count") from error
    require_nonnegative_count(value, description)


def counter_value(mapping, key, description):
    value = mapping.get(key)
    require_nonnegative_count(value, description)
    return value


def validate_jpeg_counter_section(section, description):
    require(isinstance(section, dict), f"{description} jpeg section is invalid")
    count = counter_value(section, "count", f"{description} jpeg count")
    decode_ns = counter_value(section, "ns", f"{description} jpeg ns")
    bucket_counts = []
    bucket_ns = []
    for bucket in ("full", "half", "quarter", "eighth", "other"):
        values = section.get(bucket)
        require(isinstance(values, dict), f"{description} {bucket} jpeg section is invalid")
        bucket_counts.append(counter_value(values, "count", f"{description} {bucket} count"))
        bucket_ns.append(counter_value(values, "ns", f"{description} {bucket} ns"))
    requested = section.get("requested")
    require(isinstance(requested, dict), f"{description} requested jpeg section is invalid")
    requested_count = sum(
        counter_value(requested, mode, f"{description} requested {mode} count")
        for mode in ("full", "target", "explicitRatio", "bestFit")
    )
    failures = counter_value(section, "failures", f"{description} jpeg failures")
    require(count == sum(bucket_counts), f"{description} jpeg denominator count mismatch")
    require(decode_ns == sum(bucket_ns), f"{description} jpeg denominator ns mismatch")
    require(count == requested_count, f"{description} jpeg requested mode count mismatch")
    return count, decode_ns, bucket_counts


def validate_diagnostic_counters(counters, run_dir, accounting,
                                 require_policy_diagnostics=False):
    require(isinstance(counters, dict), f"{run_dir}/counters.json is invalid")
    if accounting == "off":
        require(counters.get("accountingEnabled") is False
                and counters.get("diagnosticsAvailable") is False,
                f"{run_dir}/counters.json does not mark diagnostics unavailable")
        return {field: None for field in DIAGNOSTIC_SUMMARY_FIELDS}
    require(counters.get("accountingEnabled") is True
            and counters.get("diagnosticsAvailable") is True,
            f"{run_dir}/counters.json does not mark diagnostics available")
    jpeg_decode = counters.get("jpegDecode")
    require(isinstance(jpeg_decode, dict), f"{run_dir}/counters.json lacks jpegDecode")
    require(set(("prefetch", "scroll")) <= set(jpeg_decode),
            f"{run_dir}/counters.json lacks distinct JPEG phases")
    scroll = jpeg_decode["scroll"]
    validate_jpeg_counter_section(jpeg_decode["prefetch"], f"{run_dir} prefetch")
    scroll_count, scroll_ns, bucket_counts = validate_jpeg_counter_section(
        scroll, f"{run_dir} scroll"
    )
    required_counters = {
        "writePixelsAttempts": "writePixels attempts",
        "writePixelsHits": "writePixels hits",
        "writePixelsFallbacks": "writePixels fallbacks",
        "writePixelsDeviceOneToOneCandidates": "writePixels candidates",
        "writePixelsDeviceOneToOneKnownOpaqueCandidates": "writePixels known-opaque candidates",
        "writePixelsRejectMatrix": "writePixels matrix rejects",
        "writePixelsRejectSaveCount": "writePixels save-count rejects",
        "writePixelsRejectSizeMismatch": "writePixels size-mismatch rejects",
        "writePixelsRegularAttempts": "regular writePixels attempts",
        "writePixelsRegularHits": "regular writePixels hits",
        "writePixelsRegularFallbacks": "regular writePixels fallbacks",
        "writePixelsRegularCopiedBytes": "regular writePixels copied bytes",
        "writePixelsRegularClippedHits": "regular writePixels clipped hits",
    }
    values = {
        key: counter_value(counters, key, f"{run_dir} {description}")
        for key, description in required_counters.items()
    }
    require(values["writePixelsAttempts"] == values["writePixelsHits"]
            + values["writePixelsFallbacks"],
            f"{run_dir} writePixels attempts do not equal hits plus fallbacks")
    require(values["writePixelsRegularAttempts"] == values["writePixelsRegularHits"]
            + values["writePixelsRegularFallbacks"],
            f"{run_dir} regular writePixels attempts do not equal hits plus fallbacks")
    require(values["writePixelsRegularClippedHits"] <= values["writePixelsRegularHits"],
            f"{run_dir} regular writePixels clipped hits exceed hits")
    require(values["writePixelsDeviceOneToOneCandidates"] <= values["writePixelsAttempts"],
            f"{run_dir} writePixels candidates exceed attempts")
    require(values["writePixelsDeviceOneToOneKnownOpaqueCandidates"]
            <= values["writePixelsDeviceOneToOneCandidates"],
            f"{run_dir} writePixels known-opaque candidates exceed candidates")
    if require_policy_diagnostics:
        policy_values = {
            key: counter_value(counters, key, f"{run_dir} {key}")
            for key in POLICY_DIAGNOSTIC_COUNTERS
        }
        require(
            policy_values["targetColorUniqueFullKeys"]
            >= policy_values["targetColorAcquisitionSources"],
            f"{run_dir} target scoped full keys are fewer than acquisition sources",
        )
        require(
            policy_values["targetColorUniqueNoDestinationKeys"]
            >= policy_values["targetColorAcquisitionSources"],
            f"{run_dir} target scoped no-destination keys are fewer than acquisition sources",
        )
        require(
            policy_values["targetColorUniqueIntrinsicKeys"]
            >= policy_values["targetColorAcquisitionSources"],
            f"{run_dir} target scoped intrinsic keys are fewer than acquisition sources",
        )
        require(
            policy_values["physicalVariantUniqueFullKeys"]
            >= policy_values["physicalVariantUniqueSources"],
            f"{run_dir} physical scoped full keys are fewer than observed sources",
        )
        require(
            policy_values["physicalVariantUniqueNoSurfaceSizeKeys"]
            >= policy_values["physicalVariantUniqueSources"],
            f"{run_dir} physical scoped no-surface-size keys are fewer than observed sources",
        )
    features = counters.get("features")
    require(isinstance(features, dict), f"{run_dir}/counters.json lacks features")
    for name in (
        "RASTER_OPAQUE_WRITE_PIXELS", "RASTER_TARGET_COLORTYPE_CONVERSION",
        "RASTER_PHYSICAL_VARIANT_CACHE", "RASTER_PHYSICAL_IDENTITY_FOLDING",
    ):
        feature = features.get(name)
        require(isinstance(feature, dict), f"{run_dir} feature {name} is invalid")
        counter_value(feature, "hits", f"{run_dir} feature {name} hits")
        require(feature.get("status") in (
            "DISABLED", "NOT_REACHED", "ATTEMPTED_NO_HIT", "EXERCISED",
        ), f"{run_dir} feature {name} status is invalid")
    return {
        "jpeg_decode_count": scroll_count,
        "jpeg_decode_ns": scroll_ns,
        "jpeg_full_count": bucket_counts[0],
        "jpeg_half_count": bucket_counts[1],
        "jpeg_quarter_count": bucket_counts[2],
        "jpeg_eighth_count": bucket_counts[3],
        "write_pixels_attempts": values["writePixelsAttempts"],
        "write_pixels_hits": values["writePixelsHits"],
        "write_pixels_fallbacks": values["writePixelsFallbacks"],
        "write_pixels_device_1to1_candidates": values["writePixelsDeviceOneToOneCandidates"],
        "write_pixels_device_1to1_known_opaque_candidates": values[
            "writePixelsDeviceOneToOneKnownOpaqueCandidates"
        ],
        "write_pixels_reject_matrix": values["writePixelsRejectMatrix"],
        "write_pixels_reject_save_count": values["writePixelsRejectSaveCount"],
        "write_pixels_reject_size_mismatch": values["writePixelsRejectSizeMismatch"],
        "write_pixels_regular_attempts": values["writePixelsRegularAttempts"],
        "write_pixels_regular_hits": values["writePixelsRegularHits"],
        "write_pixels_regular_fallbacks": values["writePixelsRegularFallbacks"],
        "write_pixels_regular_copied_bytes": values["writePixelsRegularCopiedBytes"],
        "write_pixels_regular_clipped_hits": values["writePixelsRegularClippedHits"],
    }


def prefetch_phase_summary_fields(summary, path, required=False):
    fields = {}
    for json_name, csv_name in PREFETCH_PHASE_SUMMARY_FIELDS:
        value = summary.get(json_name)
        if required:
            require_nonnegative_ns(value, f"{path} {json_name}")
        elif value is not None:
            require_nonnegative_ns(value, f"{path} {json_name}")
        fields[csv_name] = value
    if required:
        require_nonnegative_ns(summary.get("uiBuildElapsedNs"), f"{path} uiBuildElapsedNs")
        require_nonnegative_ns(summary.get("prefetchElapsedNs"), f"{path} prefetchElapsedNs")
    return fields


def validate_prefetch_diagnostic_counters(counters, run_dir):
    phases = counters.get("prefetchPhases")
    require(isinstance(phases, dict), f"{run_dir}/counters.json lacks prefetchPhases")
    values = {}
    for json_name, csv_name in PREFETCH_DIAGNOSTIC_COUNTER_FIELDS:
        values[csv_name] = counter_value(
            phases, json_name, f"{run_dir} prefetch phase {json_name}"
        )
    return values


def validate_structural_diagnostics(counters, run_dir, mask, allow_inactive=False):
    """Require every approved M3 path to expose activity or a measured reject."""
    path_specs = (
        (8192, "targetColorAttempts", (
            "targetColorRejectCanvas", "targetColorRejectSurface", "targetColorRejectClip",
            "targetColorRejectMapping", "targetColorRejectBacking",
            "targetColorRejectExecution",
        )),
        (16384, "physicalVariantLookups", (
            "physicalVariantRejectCanvas", "physicalVariantRejectSurface",
            "physicalVariantRejectClip", "physicalVariantRejectMapping",
            "physicalVariantRejectBacking", "physicalVariantRejectExecution",
        )),
        (32768, "physicalIdentityAttempts", (
            "physicalIdentityRejectCanvas", "physicalIdentityRejectSurface",
            "physicalIdentityRejectClip", "physicalIdentityRejectMapping",
            "physicalIdentityRejectBacking", "physicalIdentityRejectExecution",
        )),
    )
    exercised = set()
    for bit, attempts_key, reject_keys in path_specs:
        attempts = counter_value(counters, attempts_key, f"{run_dir} {attempts_key}")
        measured_rejects = sum(
            counter_value(counters, key, f"{run_dir} {key}") for key in reject_keys
        )
        if mask & bit:
            if attempts == 0 and allow_inactive:
                require(measured_rejects == 0,
                        f"{run_dir} mask {mask} has unaccounted inactive {attempts_key}")
                continue
            require(attempts > 0,
                    f"{run_dir} mask {mask} did not exercise {attempts_key}")
            exercised.add(bit)
            if bit in (8192, 32768):
                mapping_root_to_device = counter_value(
                    counters, f"{attempts_key[:-8]}MappingRootToDevice",
                    f"{run_dir} root-to-device mapping rejects for {attempts_key}",
                )
                require(mapping_root_to_device < attempts,
                        f"{run_dir} mask {mask} remains 100% root-to-device rejects for {attempts_key}")
            if bit == 8192:
                acquisition_sources = counter_value(
                    counters, "targetColorAcquisitionSources",
                    f"{run_dir} target-color acquisition sources",
                )
                require(acquisition_sources > 0,
                        f"{run_dir} mask {mask} did not acquire a target-color source")
        else:
            require(attempts == 0 and measured_rejects == 0,
                    f"{run_dir} mask {mask} exercised disabled raster path {attempts_key}")

    write_attempts = counter_value(counters, "writePixelsAttempts",
                                   f"{run_dir} writePixels attempts")
    write_hits = counter_value(counters, "writePixelsHits", f"{run_dir} writePixels hits")
    write_fallbacks = counter_value(counters, "writePixelsFallbacks",
                                    f"{run_dir} writePixels fallbacks")
    require(write_attempts == write_hits + write_fallbacks,
            f"{run_dir} writePixels accounting regressed")
    return exercised


def validate_temporal_artifacts(run_dir, run_summary, pass_record, summary_record):
    for field in TEMPORAL_SUMMARY_FIELDS:
        require_nonnegative_ns(run_summary.get(field), f"{run_dir}/summary.json {field}")
    for field in FRAME_THRESHOLD_COUNT_FIELDS:
        require_nonnegative_count(run_summary.get(field), f"{run_dir}/summary.json {field}")
    for field in (
        "ui_build_elapsed_ns", "prefetch_elapsed_ns", "elapsed_total_ns",
        "frame_time_min_ns", "frame_time_p90_ns", "frame_time_p50_ns",
        "frame_time_p95_ns", "frame_time_p99_ns", "frame_time_max_ns",
        "work_time_p50_ns", "work_time_p95_ns", "work_time_p99_ns",
        "work_time_max_ns", "paint_time_p50_ns", "paint_time_p95_ns",
        "paint_time_p99_ns", "paint_time_max_ns", "largest_stall_ns",
    ):
        require_record_ns(pass_record, field, f"{field} in pass record")
    for field in (
        "frames_over_16_67_count", "frames_over_33_3_count",
        "frames_over_50_count", "frames_over_100_count",
    ):
        require_record_count(pass_record, field, f"{field} in pass record")
    for field in ("ui_build_elapsed_ns", "prefetch_elapsed_ns"):
        require_record_ns(summary_record, field, f"{field} in summary record")

    frames_path = run_dir / "frames.csv"
    positions = []
    with frames_path.open(newline="", encoding="utf-8") as source:
        reader = csv.reader(source)
        require(tuple(next(reader, ())) == FRAME_FIELDS,
                f"{frames_path} must use canonical ns fields")
        for row in reader:
            require(len(row) == len(FRAME_FIELDS), f"invalid row in {frames_path}")
            values = dict(zip(FRAME_FIELDS, row))
            require_nonnegative_ns(int(row[1]), f"{frames_path} elapsed_ns")
            require_nonnegative_ns(int(row[2]), f"{frames_path} frame_time_ns")
            require_nonnegative_ns(int(row[4]), f"{frames_path} scroll_work_ns")
            require_nonnegative_ns(int(row[5]), f"{frames_path} paint_work_ns")
            require_nonnegative_ns(int(row[6]), f"{frames_path} work_time_ns")
            positions.append(int(row[3]))
            require(int(row[6]) >= int(row[4]),
                    f"{frames_path} work_time_ns is less than scroll_work_ns")
            require(int(row[6]) >= int(row[5]),
                    f"{frames_path} work_time_ns is less than paint_work_ns")
            for index in (7, 9, 11, 13, 15, 17):
                require_nonnegative_count(int(row[index]), f"{frames_path} {FRAME_FIELDS[index]}")
            for index in (8, 10, 12, 14, 16, 18):
                require_nonnegative_ns(int(row[index]), f"{frames_path} {FRAME_FIELDS[index]}")
            diagnostics_available = int(values["diagnostics_available"])
            require(diagnostics_available in (0, 1),
                    f"{frames_path} diagnostics_available must be 0 or 1")
            for name, value in values.items():
                if name in FRAME_FIELDS[:19] or "jpeg_decode" in name or name in (
                            "diagnostics_available", "visible_control_first",
                            "visible_control_last", "visible_control_count",
                            "visible_control_path_hash"):
                    continue
                numeric = int(value)
                if diagnostics_available:
                    require(numeric >= 0 or name in (
                        "write_pixels_last_width", "write_pixels_last_height",
                        "write_pixels_last_format"),
                        f"{frames_path} {name} must be nonnegative or an empty-copy marker")
                else:
                    require(numeric == -1,
                            f"{frames_path} {name} must be unavailable when accounting is off")
            if diagnostics_available:
                require(int(values["write_pixels_attempts"]) == int(values["write_pixels_hits"])
                        + int(values["write_pixels_fallbacks"]),
                        f"{frames_path} writePixels attempts do not reconcile")
                require(int(values["write_pixels_hits"]) == int(values["write_pixels_full_hits"])
                        + int(values["write_pixels_clipped_hits"]),
                        f"{frames_path} writePixels hit extents do not reconcile")
                require(int(values["write_pixels_regular_attempts"])
                        == int(values["write_pixels_regular_hits"])
                        + int(values["write_pixels_regular_fallbacks"]),
                        f"{frames_path} regular writePixels attempts do not reconcile")
                require(int(values["write_pixels_total_ns"])
                        >= int(values["write_pixels_preparation_ns"]),
                        f"{frames_path} writePixels total time is less than preparation time")
                require(int(values["write_pixels_total_ns"])
                        >= int(values["write_pixels_copy_ns"]),
                        f"{frames_path} writePixels total time is less than copy time")
                require(int(values["write_pixels_preparation_ns"])
                        >= int(values["write_pixels_rgb565_conversion_ns"]),
                        f"{frames_path} RGB565 conversion exceeds preparation time")
                for name in (
                    "write_pixels_attempts", "write_pixels_hits", "write_pixels_fallbacks",
                    "write_pixels_copied_bytes",
                    "image_materializations",
                    "native_geometry_materializations", "target_color_attempts",
                    "target_color_hits", "physical_variant_lookups", "physical_variant_hits",
                    "physical_variant_misses",
                ) + WRITE_PIXELS_TIMING_FIELDS:
                    scroll_name = "scroll_" + name
                    paint_name = "paint_" + name
                    if scroll_name in values and paint_name in values:
                        require(int(values[name]) == int(values[scroll_name])
                                + int(values[paint_name]),
                                f"{frames_path} {name} does not equal segment sum")
    validate_scroll_trajectory(frames_path, positions, pass_record)

    for path, expected_fields in (
        (run_dir / "memory.csv", MEMORY_FIELDS),
        (run_dir / "timeline.csv", TIMELINE_FIELDS),
    ):
        with path.open(newline="", encoding="utf-8") as source:
            header = tuple(next(csv.reader(source), ()))
        require(header == expected_fields, f"{path} must use canonical ns fields")


def validate_scroll_trajectory(frames_path, positions, pass_record):
    require(len(positions) > 1,
            f"{frames_path} must contain multiple measured frames")
    direction = pass_record.get("direction")
    require(direction in ("top-to-bottom", "bottom-to-top"),
            f"{frames_path} has an invalid traversal direction")
    expected_start = int(pass_record["scroll_start"])
    expected_end = int(pass_record["scroll_end"])
    expected_distance = int(pass_record["scroll_distance"])
    require(expected_distance == abs(expected_end - expected_start),
            f"{frames_path} pass record has an inconsistent scroll distance")
    require(positions[0] == expected_start,
            f"{frames_path} does not start at the recorded endpoint")
    require(positions[-1] == expected_end,
            f"{frames_path} does not end at the recorded endpoint")
    if direction == "top-to-bottom":
        require(all(previous <= current
                    for previous, current in zip(positions, positions[1:])),
                f"{frames_path} forward traversal is not non-decreasing")
    else:
        require(all(previous >= current
                    for previous, current in zip(positions, positions[1:])),
                f"{frames_path} reverse traversal is not non-increasing")
    require(max(positions) - min(positions) == expected_distance,
            f"{frames_path} does not cover the full scrollbar range")


def validate_reuse_run_artifacts(output, log_path, mask, prefetch, accounting, run,
                                 dataset_digest, require_policy_diagnostics,
                                 require_structural_diagnostics, pass_count,
                                 physical_baseline=None):
    lines = log_path.read_text(encoding="utf-8", errors="replace").splitlines()
    pass_records = [
        parse_record(line) for line in lines
        if line.startswith(f"fixture={FIXTURE},record=pass")
    ]
    summary_records = [
        parse_record(line) for line in lines
        if line.startswith(f"fixture={FIXTURE},record=summary")
    ]
    require(len(pass_records) == pass_count,
            f"{log_path.name} must contain {pass_count} pass records")
    require(len(summary_records) == 1, f"{log_path.name} must contain one summary record")
    expected_passes = benchmark_pass_names(pass_count)
    pass_records.sort(key=lambda record: int(record.get("pass_index", "-1")))
    summary_record = summary_records[0]
    structural_activity = set()
    for index, record in enumerate(pass_records):
        expected = {
            "resolution": "540x960",
            "requested_mask": str(mask),
            "effective_mask": str(mask),
            "prefetch_profile": prefetch,
            "accounting": accounting,
            "image_count": str(EXPECTED_JPEGS),
            "pass": expected_passes[index],
            "pass_index": str(index + 1),
            "pass_count": str(pass_count),
        }
        for key, value in expected.items():
            require(record.get(key) == value,
                    f"{log_path.name} pass {index + 1} {key}={record.get(key)!r},"
                    f" expected {value!r}")
    require(summary_record.get("overallPass") == "true",
            f"{log_path.name} did not report overallPass=true")
    require(summary_record.get("resolution") == "540x960"
            and summary_record.get("requested_mask") == str(mask)
            and summary_record.get("effective_mask") == str(mask)
            and summary_record.get("prefetch_profile") == prefetch
            and summary_record.get("passes") == str(pass_count),
            f"{log_path.name} summary configuration mismatch")
    if accounting == "off":
        for key in ("prefetch_request_count", "prefetch_ready_count",
                    "prefetch_failed_count", "prefetch_not_prefetchable_count"):
            require(summary_record.get(key) == "0",
                    f"{log_path.name} {key} must be zero when accounting is off")
    elif prefetch == "off":
        for key in ("prefetch_request_count", "prefetch_ready_count",
                    "prefetch_failed_count", "prefetch_not_prefetchable_count"):
            require(summary_record.get(key) == "0",
                    f"{log_path.name} {key} must be zero")
    else:
        require(summary_record.get("prefetch_request_count") == str(EXPECTED_JPEGS),
                f"{log_path.name} prefetch request count is not 663")
        require(summary_record.get("prefetch_ready_count") == str(EXPECTED_JPEGS - 3),
                f"{log_path.name} prefetch ready count is not 660")
        require(summary_record.get("prefetch_failed_count") == "0",
                f"{log_path.name} prefetch failed count is not zero")
        require(summary_record.get("prefetch_not_prefetchable_count") == "3",
                f"{log_path.name} prefetch not-prefetchable count is not 3")

    run_dir = expected_run_dir(output, mask, prefetch, accounting, run)
    environment = json.loads((output / "environment.json").read_text(encoding="utf-8"))
    require(environment.get("datasetFileCount") == EXPECTED_JPEGS
            and environment.get("datasetHash") == dataset_digest,
            f"{output}/environment.json dataset mismatch")
    require(environment.get("expectedLogicalWidth") == 540
            and environment.get("expectedLogicalHeight") == 960
            and environment.get("effectiveLogicalWidth") == 540
            and environment.get("effectiveLogicalHeight") == 960,
            f"{output}/environment.json is not 540x960")
    validate_physical_target(
        environment, physical_baseline, f"{output}/environment.json"
    )

    for index, record in enumerate(pass_records):
        pass_dir = run_dir / "passes" / record["pass"]
        for name in ("summary.json", "frames.csv", "counters.json", "memory.csv", "timeline.csv"):
            require((pass_dir / name).is_file(), f"{pass_dir / name} is missing")
        run_summary = json.loads((pass_dir / "summary.json").read_text(encoding="utf-8"))
        counters = json.loads((pass_dir / "counters.json").read_text(encoding="utf-8"))
        require(run_summary.get("status") == "PASS", f"{pass_dir}/summary.json is not PASS")
        require(run_summary.get("requestedMask") == mask
                and run_summary.get("effectiveMask") == mask
                and run_summary.get("prefetch") == prefetch
                and run_summary.get("accounting") == accounting
                and run_summary.get("imageCount") == EXPECTED_JPEGS
                and run_summary.get("pass") == record["pass"],
                f"{pass_dir}/summary.json configuration mismatch")
        require(run_summary.get("frameCount", 0) > 1,
                f"{pass_dir}/summary.json has no measured frames")
        validate_temporal_artifacts(pass_dir, run_summary, record, summary_record)
        validate_diagnostic_counters(counters, pass_dir, accounting,
                                     require_policy_diagnostics)
        if require_structural_diagnostics:
            require(accounting == "on",
                    f"{pass_dir} structural diagnostics require accounting")
            structural_activity.update(
                validate_structural_diagnostics(
                    counters, pass_dir, mask, allow_inactive=index > 0,
                )
            )

    if require_structural_diagnostics:
        for bit, attempts_key in (
            (8192, "targetColorAttempts"),
            (16384, "physicalVariantLookups"),
            (32768, "physicalIdentityAttempts"),
        ):
            if mask & bit:
                require(bit in structural_activity,
                        f"{run_dir} reuse passes never exercised {attempts_key}")

    for name in ("summary.json", "frames.csv", "counters.json", "memory.csv", "timeline.csv"):
        require((run_dir / name).is_file(), f"{run_dir / name} is missing")
    return json.loads((run_dir / "summary.json").read_text(encoding="utf-8"))


def tail(path, count=60):
    return "\n".join(path.read_text(encoding="utf-8", errors="replace").splitlines()[-count:])


def expected_run_dir(output, mask, prefetch, accounting, run):
    return output / "runs" / (
        f"mask-{mask}-prefetch-{prefetch}-accounting-{accounting}-run-{run}"
    )


def mask_token(mask):
    return "default" if mask is None else str(mask)


def expected_reuse_run_dir(output, profile, mask, prefetch, accounting,
                           rendering_reuse, run):
    return output / "runs" / (
        f"{profile}-mask-{mask_token(mask)}-prefetch-{prefetch}-accounting-{accounting}"
        f"-reuse-{rendering_reuse}-run-{run}"
    )


def integer_field(row, field, description, minimum=0):
    try:
        value = int(row[field])
    except (KeyError, TypeError, ValueError) as error:
        raise BenchmarkFailure(f"{description} {field} is not an integer") from error
    if minimum is not None:
        require(value >= minimum, f"{description} {field} is below {minimum}")
    return value


def percentile(values, fraction):
    require(values, "cannot calculate a percentile from an empty sequence")
    ordered = sorted(values)
    index = max(0, min(len(ordered) - 1, math.ceil(fraction * len(ordered)) - 1))
    return ordered[index]


def validate_scroll_reuse_artifacts(output, log_path, manifest, profile, mask,
                                    prefetch, accounting, rendering_reuse, run,
                                    expected_image_count, physical_baseline=None):
    lines = log_path.read_text(encoding="utf-8", errors="replace").splitlines()
    pass_records = [
        parse_record(line) for line in lines
        if line.startswith(f"fixture={FIXTURE},record=scroll-raster-reuse")
    ]
    summary_records = [
        parse_record(line) for line in lines
        if line.startswith(f"fixture={FIXTURE},record=summary")
    ]
    require(len(pass_records) == 2, f"{log_path.name} must contain cold and warm pass records")
    require(len(summary_records) == 1, f"{log_path.name} must contain one summary record")
    pass_records.sort(key=lambda record: int(record.get("pass_index", "-1")))
    expected_mask = DEFAULT_EFFECTIVE_MASK if mask is None else mask
    expected_passes = ("cold", "warm")
    summary = summary_records[0]
    require(summary.get("overallPass") == "true", f"{log_path.name} did not pass")
    for key, value in {
        "profile": profile,
        "prefetch_profile": prefetch,
        "accounting": accounting,
        "requested_mask": str(expected_mask),
        "effective_mask": str(expected_mask),
        "passes": "2",
        "image_count": str(expected_image_count),
    }.items():
        require(summary.get(key) == value,
                f"{log_path.name} summary {key}={summary.get(key)!r}, expected {value!r}")
    require(profile != "release-default-scroll" or expected_mask == DEFAULT_EFFECTIVE_MASK,
            f"{log_path.name} release profile did not use effective mask 32799")
    require(profile != "release-candidate-scroll" or expected_mask == RELEASE_CANDIDATE_MASK,
            f"{log_path.name} release candidate did not use effective mask 32795")
    if profile == "release-default-scroll":
        for key in ("native_draw_mask", "native_decode_mask",
                    "observed_draw_mask", "observed_decode_mask"):
            require(summary.get(key) == str(DEFAULT_EFFECTIVE_MASK),
                    f"{log_path.name} release profile {key} is not 32799")

    run_dir = expected_reuse_run_dir(
        output, profile, mask, prefetch, accounting, rendering_reuse, run
    )
    environment_path = output / "environment.json"
    require(environment_path.is_file(), f"{environment_path} is missing")
    environment = read_json_file(environment_path, "benchmark environment")
    require(environment.get("datasetFileCount") == expected_image_count
            and environment.get("datasetHash") == manifest.get("datasetHash"),
            f"{environment_path} has an unexpected scroll corpus identity")
    physical_target = validate_physical_target(
        environment, physical_baseline, str(environment_path)
    )
    target_pixel_bytes = physical_target["skiaSurfacePixelBytes"]
    require(target_pixel_bytes in (2, 4),
            f"{environment_path} has an unsupported target pixel width")
    for name in ("scroll_raster_reuse_frames.csv", "scroll_raster_reuse_waypoints.csv",
                 "memory.csv", "timeline.csv"):
        require((run_dir / name).is_file(), f"{run_dir / name} is missing")
    memory_path = run_dir / "memory.csv"
    with memory_path.open(newline="", encoding="utf-8") as source:
        memory_reader = csv.DictReader(source)
        require({"checkpoint", "peak_resident_bytes"} <= set(memory_reader.fieldnames or ()),
                f"{memory_path} lacks memory fields")
        memory_rows = list(memory_reader)
    require(memory_rows, f"{memory_path} has no memory checkpoint")
    memory_peak = memory_rows[-1].get("peak_resident_bytes", "unavailable")
    if memory_peak != "unavailable":
        try:
            require(int(memory_peak) >= 0, f"{memory_path} has a negative memory peak")
        except ValueError as error:
            raise BenchmarkFailure(f"{memory_path} memory peak is invalid") from error

    frames_path = run_dir / "scroll_raster_reuse_frames.csv"
    movement_values = {
        pass_name: {name: [] for name in ("work_time_ns", "screen_update_ns")}
        for pass_name in expected_passes
    }
    paint_totals = {
        pass_name: {"row_paints": 0, "image_paints": 0}
        for pass_name in expected_passes
    }
    frame_passes = set()
    with frames_path.open(newline="", encoding="utf-8") as source:
        reader = csv.DictReader(source)
        required = {
            "pass", "requested_delta", "actual_delta", "movement", "measured", "work_time_ns",
            "screen_update_ns", "row_paints", "image_paints",
        }
        require(required <= set(reader.fieldnames or ()),
                f"{frames_path} lacks scroll reuse frame fields")
        for row_data in reader:
            pass_name = row_data.get("pass")
            require(pass_name in expected_passes, f"{frames_path} has an unexpected pass")
            frame_passes.add(pass_name)
            for field in ("requested_delta", "actual_delta"):
                integer_field(row_data, field, str(frames_path), minimum=None)
            for field in ("movement", "measured", "work_time_ns", "screen_update_ns",
                          "row_paints", "image_paints"):
                integer_field(row_data, field, str(frames_path))
            if row_data["movement"] == "1" and row_data["measured"] == "1":
                movement_values[pass_name]["work_time_ns"].append(int(row_data["work_time_ns"]))
                movement_values[pass_name]["screen_update_ns"].append(int(row_data["screen_update_ns"]))
                paint_totals[pass_name]["row_paints"] += int(row_data["row_paints"])
                paint_totals[pass_name]["image_paints"] += int(row_data["image_paints"])
    require(frame_passes == set(expected_passes), f"{frames_path} lacks a cold or warm pass")
    require(all(movement_values[pass_name]["work_time_ns"] for pass_name in expected_passes),
            f"{frames_path} has no measured movement frames")

    waypoint_hashes = {}
    waypoint_path = run_dir / "scroll_raster_reuse_waypoints.csv"
    with waypoint_path.open(newline="", encoding="utf-8") as source:
        reader = csv.DictReader(source)
        required = {"pass", "waypoint_index", "hash", "top_hash", "bottom_hash"}
        require(required <= set(reader.fieldnames or ()),
                f"{waypoint_path} lacks viewport hash fields")
        for row_data in reader:
            pass_name = row_data.get("pass")
            require(pass_name in expected_passes, f"{waypoint_path} has an unexpected pass")
            index = integer_field(row_data, "waypoint_index", str(waypoint_path))
            require(0 <= index <= 4, f"{waypoint_path} has an invalid waypoint index")
            hashes = tuple(row_data.get(field, "") for field in ("hash", "top_hash", "bottom_hash"))
            require(all(len(value) == 16 for value in hashes),
                    f"{waypoint_path} has an invalid viewport hash")
            waypoint_hashes[(pass_name, index)] = hashes
    require(len(waypoint_hashes) == 10,
            f"{waypoint_path} must contain five waypoints for cold and warm")

    records_by_pass = {record.get("pass"): record for record in pass_records}
    rows = []
    for pass_name in expected_passes:
        record = records_by_pass.get(pass_name)
        require(record is not None, f"{log_path.name} lacks {pass_name} reuse metrics")
        for key, value in {
            "profile": profile,
            "rendering_reuse": rendering_reuse,
            "image_count": str(expected_image_count),
        }.items():
            require(record.get(key) == value,
                    f"{log_path.name} {pass_name} {key}={record.get(key)!r}, expected {value!r}")
        attempts = integer_field(record, "attempts", log_path.name)
        hits = integer_field(record, "hits", log_path.name)
        fallbacks = integer_field(record, "fallbacks", log_path.name)
        recoveries = integer_field(record, "post_move_recoveries", log_path.name)
        viewport_pixels = integer_field(record, "viewport_pixels", log_path.name)
        reused_pixels = integer_field(record, "reused_pixels", log_path.name)
        dirty_pixels = integer_field(record, "dirty_pixels", log_path.name)
        moved_bytes = integer_field(record, "moved_bytes", log_path.name)
        movement_frames = integer_field(record, "movement_frames", log_path.name)
        require(movement_frames == len(movement_values[pass_name]["work_time_ns"]),
                f"{log_path.name} {pass_name} movement-frame count differs")
        if rendering_reuse == "on":
            require(attempts == hits + fallbacks,
                    f"{log_path.name} {pass_name} attempts do not reconcile")
            require(hits > 0, f"{log_path.name} {pass_name} has no reuse hits")
        else:
            require(attempts == hits == fallbacks == 0,
                    f"{log_path.name} {pass_name} disabled reuse accounting is nonzero")
        require(recoveries == 0, f"{log_path.name} {pass_name} recorded recovery")
        require(moved_bytes == reused_pixels * target_pixel_bytes,
                f"{log_path.name} {pass_name} moved-byte accounting differs")
        rows.append({
            "profile": profile,
            "run": run,
            "mask": mask_token(mask),
            "effective_mask": expected_mask,
            "prefetch": prefetch,
            "accounting": accounting,
            "rendering_reuse": rendering_reuse,
            "pass": pass_name,
            "status": "PASS",
            "validation_status": "VALID",
            "validation_error": "",
            "comparison_status": "VALID",
            "movement_frames": movement_frames,
            "movement_p50_ns": percentile(movement_values[pass_name]["work_time_ns"], 0.50),
            "movement_p95_ns": percentile(movement_values[pass_name]["work_time_ns"], 0.95),
            "movement_p99_ns": percentile(movement_values[pass_name]["work_time_ns"], 0.99),
            "movement_max_ns": percentile(movement_values[pass_name]["work_time_ns"], 1.0),
            "screen_p50_ns": percentile(movement_values[pass_name]["screen_update_ns"], 0.50),
            "screen_p95_ns": percentile(movement_values[pass_name]["screen_update_ns"], 0.95),
            "screen_p99_ns": percentile(movement_values[pass_name]["screen_update_ns"], 0.99),
            "screen_max_ns": percentile(movement_values[pass_name]["screen_update_ns"], 1.0),
            "row_paints": paint_totals[pass_name]["row_paints"],
            "image_paints": paint_totals[pass_name]["image_paints"],
            "attempts": attempts,
            "hits": hits,
            "fallbacks": fallbacks,
            "recoveries": recoveries,
            "hit_rate": round(hits / attempts, 9) if attempts else 0.0,
            "viewport_pixels": viewport_pixels,
            "reused_pixels": reused_pixels,
            "dirty_pixels": dirty_pixels,
            "moved_bytes": moved_bytes,
            "reuse_coverage": round(reused_pixels / viewport_pixels, 9)
            if viewport_pixels else 0.0,
            "target_pixel_bytes": target_pixel_bytes,
            "memory_peak_resident_bytes": memory_peak,
            **physical_summary_fields(physical_target),
        })
    return rows, waypoint_hashes


def write_reuse_suite_plan(output, profile):
    combinations = [
        (mask, prefetch, accounting, rendering_reuse)
        for mask in profile["masks"]
        for prefetch in profile["prefetch"]
        for accounting in profile["accounting"]
        for rendering_reuse in profile["rendering_reuse"]
    ]
    planned = []
    order = 0
    for round_number in range(profile["rounds"]):
        ordered = list(combinations)
        state = (SEED + 0x9E3779B97F4A7C15 * (round_number + 1)) & 0xFFFFFFFFFFFFFFFF
        for index in range(len(ordered) - 1, 0, -1):
            state = (state * 6364136223846793005 + 1442695040888963407) & 0xFFFFFFFFFFFFFFFF
            swap = (state >> 1) % (index + 1)
            ordered[index], ordered[swap] = ordered[swap], ordered[index]
        for mask, prefetch, accounting, rendering_reuse in ordered:
            run = round_number + 1
            planned.append((round_number, order, run, mask, prefetch, accounting, rendering_reuse))
            order += 1
    require(len(planned) == profile["expected_processes"],
            f"reuse suite plan does not contain {profile['expected_processes']} processes")
    lines = ["round\torder\trun\tmask\tprefetch\taccounting\trendering_reuse"]
    lines.extend(
        f"{round_number + 1}\t{order_number}\t{run}\t{mask_token(mask)}\t{prefetch}"
        f"\t{accounting}\t{rendering_reuse}"
        for round_number, order_number, run, mask, prefetch, accounting, rendering_reuse
        in planned
    )
    (output / f"{profile['name']}-suite-plan.tsv").write_text(
        "\n".join(lines) + "\n", encoding="utf-8"
    )
    return planned


def run_scroll_reuse_process(bundle, manifest, output, corpus_digest, profile,
                             mask, prefetch, accounting, rendering_reuse, run,
                             expected_image_count):
    executable = executable_path(bundle, manifest)
    logs = output / "logs"
    logs.mkdir(parents=True, exist_ok=True)
    label = (f"{profile}-run-{run}-mask-{mask_token(mask)}-prefetch-{prefetch}"
             f"-accounting-{accounting}-reuse-{rendering_reuse}")
    log_path = logs / f"{label}.log"
    command = [
        str(executable), "/scr", SCREEN_SPEC, "-p", ".", "--app-root=.",
        "--mode=benchmark", "--corpus=corpus/imag", "--output=results",
        f"--profile={profile}", f"--rendering-reuse={rendering_reuse}",
        f"--prefetch={prefetch}", f"--accounting={accounting}", "--passes=2",
        f"--run={run}", f"--dataset-hash={corpus_digest}",
    ]
    if mask is not None:
        command.append(f"--image-optimization={mask}")
    try:
        with log_path.open("w", encoding="utf-8") as log:
            completed = subprocess.run(
                command, cwd=bundle, stdout=log, stderr=subprocess.STDOUT, text=True,
                check=False, timeout=PROCESS_TIMEOUT_SECONDS
            )
    except subprocess.TimeoutExpired:
        with log_path.open("a", encoding="utf-8") as log:
            log.write(f"\nbenchmark_timeout_seconds={PROCESS_TIMEOUT_SECONDS}\n")
        reason = f"timed out after {PROCESS_TIMEOUT_SECONDS} seconds"
        append_execution_failure(
            output, profile, run, mask, prefetch, accounting, rendering_reuse,
            reason, log_path,
            [expected_reuse_run_dir(
                output, profile, mask, prefetch, accounting, rendering_reuse, run
            )],
        )
        raise BenchmarkFailure(f"{label} timed out; log={log_path}")
    except OSError as error:
        reason = f"process launch failed: {error}"
        append_execution_failure(
            output, profile, run, mask, prefetch, accounting, rendering_reuse,
            reason, log_path,
            [expected_reuse_run_dir(
                output, profile, mask, prefetch, accounting, rendering_reuse, run
            )],
        )
        raise BenchmarkFailure(f"{label} failed: {reason}") from error
    if completed.returncode:
        reason = f"exited with code {completed.returncode}"
        append_execution_failure(
            output, profile, run, mask, prefetch, accounting, rendering_reuse,
            reason, log_path,
            [expected_reuse_run_dir(
                output, profile, mask, prefetch, accounting, rendering_reuse, run
            )],
        )
        print(f"{label} failed,exit_code={completed.returncode},log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise BenchmarkFailure(f"{label} failed with exit code {completed.returncode}")
    try:
        environment = enrich_environment_metadata(output, manifest)
        physical_baseline = capture_physical_target_baseline(
            output, environment, {
                "profile": profile, "run": run, "mask": mask,
                "prefetch": prefetch, "accounting": accounting,
                "renderingReuse": rendering_reuse,
            }, manifest,
        )
        rows, hashes = validate_scroll_reuse_artifacts(
            output, log_path, manifest, profile, mask, prefetch, accounting,
            rendering_reuse, run, expected_image_count, physical_baseline,
        )
    except FatalBenchmarkFailure:
        raise
    except BenchmarkFailure as error:
        append_validation_failure(
            output, profile, run, mask, prefetch, accounting, rendering_reuse,
            error, log_path,
            [expected_reuse_run_dir(
                output, profile, mask, prefetch, accounting, rendering_reuse, run
            )],
        )
        return None, None, "VALIDATION_FAILED"
    print(f"{label} passed,physical_target={physical_baseline},"
          f"artifacts={expected_reuse_run_dir(output, profile, mask, prefetch, accounting, rendering_reuse, run)}")
    return rows, hashes, "PASS"


def invalid_reuse_summary_row(profile, run, mask, prefetch, accounting,
                              rendering_reuse, pass_name, failure, physical_target):
    fields = (
        "profile", "run", "mask", "effective_mask", "prefetch", "accounting",
        "rendering_reuse", "pass", "status", "validation_status", "validation_error",
        "comparison_status",
        "movement_frames", "movement_p50_ns", "movement_p95_ns", "movement_p99_ns",
        "movement_max_ns", "screen_p50_ns", "screen_p95_ns", "screen_p99_ns",
        "screen_max_ns", "row_paints", "image_paints", "attempts", "hits", "fallbacks",
        "recoveries", "hit_rate", "viewport_pixels", "reused_pixels", "dirty_pixels",
        "moved_bytes", "reuse_coverage", "target_pixel_bytes",
        "memory_peak_resident_bytes", "logical_width", "logical_height",
        "physical_width", "physical_height", "physical_row_bytes",
        "physical_pixel_bytes", "physical_color_type", "physical_alpha_type",
        "physical_color_classification", "renderer_backend",
    )
    row = {field: None for field in fields}
    row.update({
        "profile": profile,
        "run": run,
        "mask": mask_token(mask),
        "effective_mask": DEFAULT_EFFECTIVE_MASK if mask is None else mask,
        "prefetch": prefetch,
        "accounting": accounting,
        "rendering_reuse": rendering_reuse,
        "pass": pass_name,
        "status": "VALIDATION_FAILED",
        "validation_status": "VALIDATION_FAILED",
        "validation_error": validation_failure_message(failure),
        "comparison_status": "INCOMPLETE_VALIDATION",
        **physical_summary_fields(physical_target),
    })
    return row


def aggregate_scroll_reuse(output, manifest, profile, plan, rows_by_key, hashes_by_key):
    failures = load_validation_failures(output)
    physical_target = load_physical_target_baseline(output)
    app_profile = profile.get("app_profile", profile["name"])
    by_pair = {}
    for _, _, run, mask, prefetch, accounting, rendering_reuse in plan:
        by_pair.setdefault((run, mask, prefetch, accounting), {})[rendering_reuse] = (
            rows_by_key.get((run, mask, prefetch, accounting, rendering_reuse)),
            hashes_by_key.get((run, mask, prefetch, accounting, rendering_reuse)),
        )
    summary_rows = []
    hash_rows = []
    for (run, mask, prefetch, accounting), modes in by_pair.items():
        require(set(modes) == {"off", "on"},
                f"{profile['name']} run {run} lacks an off/on pair")
        off_rows, off_hashes = modes["off"]
        on_rows, on_hashes = modes["on"]
        pair_valid = off_rows is not None and on_rows is not None
        comparison_error = None
        if pair_valid and off_hashes != on_hashes:
            comparison_error = BenchmarkFailure(
                f"{profile['name']} run {run} viewport hashes differ between reuse "
                f"modes: off={off_hashes!r}, on={on_hashes!r}"
            )
            comparison_key = configuration_key(
                profile["name"], run, mask, prefetch, accounting,
                "off-on-comparison",
            )
            if comparison_key not in failures:
                off_run_dir = expected_reuse_run_dir(
                    output, app_profile, mask, prefetch, accounting, "off", run
                )
                on_run_dir = expected_reuse_run_dir(
                    output, app_profile, mask, prefetch, accounting, "on", run
                )
                off_log = output / "logs" / (
                    f"{app_profile}-run-{run}-mask-{mask_token(mask)}-prefetch-"
                    f"{prefetch}-accounting-{accounting}-reuse-off.log"
                )
                record = append_validation_failure(
                    output, profile["name"], run, mask, prefetch, accounting,
                    "off-on-comparison", comparison_error, off_log,
                    [off_run_dir, on_run_dir, off_log, off_log.with_name(
                        off_log.name.replace("reuse-off", "reuse-on")
                    )], scope="comparison",
                )
                failures[comparison_key] = record
        for rendering_reuse, rows in (("off", off_rows), ("on", on_rows)):
            if rows is None:
                failure = failures.get(
                    configuration_key(
                        app_profile, run, mask, prefetch, accounting, rendering_reuse
                    )
                )
                require(failure is not None,
                        f"missing validation failure for {profile['name']} run {run} "
                        f"reuse={rendering_reuse}")
                summary_rows.extend(
                    invalid_reuse_summary_row(
                        profile["name"], run, mask, prefetch, accounting,
                        rendering_reuse, pass_name, failure, physical_target
                    )
                    for pass_name in ("cold", "warm")
                )
                continue
            if comparison_error is not None:
                for row in rows:
                    row.update({
                        "status": "VALIDATION_FAILED",
                        "validation_status": "VALIDATION_FAILED",
                        "validation_error": str(comparison_error),
                        "comparison_status": "INCOMPLETE_VALIDATION",
                    })
            elif not pair_valid:
                for row in rows:
                    row["comparison_status"] = "INCOMPLETE_VALIDATION"
            summary_rows.extend(rows)
            for (pass_name, waypoint_index), hashes in sorted(
                    (off_hashes if rendering_reuse == "off" else on_hashes).items()):
                hash_rows.append({
                    "profile": profile["name"], "run": run, "mask": mask_token(mask),
                    "prefetch": prefetch, "accounting": accounting,
                    "rendering_reuse": rendering_reuse, "pass": pass_name,
                    "status": ("VALIDATION_FAILED" if comparison_error is not None
                               else "PASS"),
                    "validation_status": ("VALIDATION_FAILED"
                                           if comparison_error is not None else "VALID"),
                    "validation_error": (str(comparison_error)
                                          if comparison_error is not None else ""),
                    "comparison_status": ("INCOMPLETE_VALIDATION"
                                           if comparison_error is not None or not pair_valid
                                           else "VALID"),
                    "waypoint_index": waypoint_index,
                    "hash": hashes[0], "top_hash": hashes[1], "bottom_hash": hashes[2],
                })
    summary_fields = list(summary_rows[0]) if summary_rows else []
    summary_path = output / f"scroll-raster-summary-{profile['name']}.csv"
    with summary_path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=summary_fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(summary_rows)
    hash_path = output / f"scroll-raster-waypoint-hashes-{profile['name']}.csv"
    with hash_path.open("w", newline="", encoding="utf-8") as destination:
        hash_fields = [
            "profile", "run", "mask", "prefetch", "accounting", "rendering_reuse",
            "pass", "status", "validation_status", "validation_error",
            "comparison_status", "waypoint_index",
            "hash", "top_hash", "bottom_hash",
        ]
        writer = csv.DictWriter(destination, fieldnames=hash_fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(hash_rows)
    require(len(summary_rows) == profile["expected_processes"] * 2,
            f"{profile['name']} summary row count differs")
    print(f"scroll reuse aggregation passed,profile={profile['name']},"
          f"rows={len(summary_rows)},summary={summary_path},hashes={hash_path}")
    return summary_path, hash_path


def run_scroll_reuse_matrix(bundle, manifest, output, corpus_digest, profile, tracker=None):
    plan = write_reuse_suite_plan(output, profile)
    if tracker is not None:
        tracker["plannedProcessCount"] += len(plan)
    rows_by_key = {}
    hashes_by_key = {}
    for _, _, run, mask, prefetch, accounting, rendering_reuse in plan:
        app_profile = profile.get("app_profile", profile["name"])
        track_process_start(tracker)
        rows, hashes, _status = run_scroll_reuse_process(
            bundle, manifest, output, corpus_digest, app_profile, mask, prefetch,
            accounting, rendering_reuse, run, profile["workload_images"]
        )
        track_process_complete(tracker, _status)
        key = (run, mask, prefetch, accounting, rendering_reuse)
        rows_by_key[key] = rows
        hashes_by_key[key] = hashes
    return aggregate_scroll_reuse(output, manifest, profile, plan, rows_by_key, hashes_by_key)


def validate_run_artifacts(output, log_path, mask, prefetch, accounting, run, dataset_digest,
                           require_policy_diagnostics=False,
                           require_structural_diagnostics=False, passes=1,
                           physical_baseline=None):
    if passes != 1:
        return validate_reuse_run_artifacts(
            output, log_path, mask, prefetch, accounting, run, dataset_digest,
            require_policy_diagnostics, require_structural_diagnostics, passes,
            physical_baseline,
        )
    lines = log_path.read_text(encoding="utf-8", errors="replace").splitlines()
    pass_records = [
        parse_record(line) for line in lines
        if line.startswith(f"fixture={FIXTURE},record=pass")
    ]
    summary_records = [
        parse_record(line) for line in lines
        if line.startswith(f"fixture={FIXTURE},record=summary")
    ]
    require(len(pass_records) == 1, f"{log_path.name} must contain one pass record")
    require(len(summary_records) == 1, f"{log_path.name} must contain one summary record")
    record = pass_records[0]
    summary_record = summary_records[0]
    expected = {
        "resolution": "540x960",
        "requested_mask": str(mask),
        "effective_mask": str(mask),
        "prefetch_profile": prefetch,
        "accounting": accounting,
        "image_count": str(EXPECTED_JPEGS),
        "pass": "cold",
    }
    for key, value in expected.items():
        require(record.get(key) == value,
                f"{log_path.name} {key}={record.get(key)!r}, expected {value!r}")
    require(summary_record.get("overallPass") == "true",
            f"{log_path.name} did not report overallPass=true")
    require(summary_record.get("resolution") == "540x960",
            f"{log_path.name} summary resolution is not 540x960")
    require(summary_record.get("requested_mask") == str(mask)
            and summary_record.get("effective_mask") == str(mask),
            f"{log_path.name} summary mask mismatch")
    require(summary_record.get("prefetch_profile") == prefetch,
            f"{log_path.name} summary prefetch mismatch")
    if accounting == "off":
        for key in ("prefetch_request_count", "prefetch_ready_count",
                    "prefetch_failed_count", "prefetch_not_prefetchable_count"):
            require(summary_record.get(key) == "0",
                    f"{log_path.name} {key} must be zero when accounting is off")
    elif prefetch == "off":
        for key in ("prefetch_request_count", "prefetch_ready_count",
                    "prefetch_failed_count", "prefetch_not_prefetchable_count"):
            require(summary_record.get(key) == "0",
                    f"{log_path.name} {key} must be zero")
    else:
        require(summary_record.get("prefetch_request_count") == str(EXPECTED_JPEGS),
                f"{log_path.name} prefetch request count is not 663")
        require(summary_record.get("prefetch_ready_count") == str(EXPECTED_JPEGS - 3),
                f"{log_path.name} prefetch ready count is not 660")
        require(summary_record.get("prefetch_failed_count") == "0",
                f"{log_path.name} prefetch failed count is not zero")
        require(summary_record.get("prefetch_not_prefetchable_count") == "3",
                f"{log_path.name} prefetch not-prefetchable count is not 3")

    run_dir = expected_run_dir(output, mask, prefetch, accounting, run)
    for name in ("summary.json", "frames.csv", "counters.json", "memory.csv", "timeline.csv"):
        require((run_dir / name).is_file(), f"{run_dir / name} is missing")
    try:
        run_summary = json.loads((run_dir / "summary.json").read_text(encoding="utf-8"))
        environment = json.loads((output / "environment.json").read_text(encoding="utf-8"))
        counters = json.loads((run_dir / "counters.json").read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        raise BenchmarkFailure(f"invalid JSON artifact for {log_path.name}") from error
    require(run_summary.get("status") == "PASS", f"{run_dir}/summary.json is not PASS")
    require(run_summary.get("requestedMask") == mask
            and run_summary.get("effectiveMask") == mask,
            f"{run_dir}/summary.json mask mismatch")
    require(run_summary.get("prefetch") == prefetch,
            f"{run_dir}/summary.json prefetch mismatch")
    require(run_summary.get("accounting") == accounting,
            f"{run_dir}/summary.json accounting mismatch")
    require(run_summary.get("imageCount") == EXPECTED_JPEGS,
            f"{run_dir}/summary.json image count mismatch")
    require(run_summary.get("frameCount", 0) > 1,
            f"{run_dir}/summary.json has no measured frames")
    validate_temporal_artifacts(run_dir, run_summary, record, summary_record)
    require(environment.get("expectedLogicalWidth") == 540
            and environment.get("expectedLogicalHeight") == 960
            and environment.get("effectiveLogicalWidth") == 540
            and environment.get("effectiveLogicalHeight") == 960,
            f"{output}/environment.json is not 540x960")
    require(environment.get("datasetFileCount") == EXPECTED_JPEGS
            and environment.get("datasetHash") == dataset_digest,
            f"{output}/environment.json dataset mismatch")
    physical_target = validate_physical_target(
        environment, physical_baseline, f"{output}/environment.json"
    )
    validate_diagnostic_counters(counters, run_dir, accounting, require_policy_diagnostics)
    if require_structural_diagnostics:
        require(accounting == "on", f"{run_dir} structural diagnostics require accounting")
        validate_structural_diagnostics(counters, run_dir, mask)
    return run_summary


def run_process(bundle, manifest, output, corpus_digest, mask, prefetch, accounting, run, label,
                require_policy_diagnostics=False, require_structural_diagnostics=False,
                passes=1, profile_name="matrix"):
    executable = executable_path(bundle, manifest)
    logs = output / "logs"
    logs.mkdir(parents=True, exist_ok=True)
    log_path = logs / f"{label}.log"
    command = [
        str(executable),
        "/scr", SCREEN_SPEC,
        # The macOS launcher resolves the application root relative to cwd;
        # using the absolute bundle path trips its path handling and raises
        # SIGTRAP before the benchmark can emit diagnostics.
        "-p", ".",
        "--app-root=.",
        "--mode=benchmark",
        "--corpus=corpus/imag",
        "--output=results",
        f"--image-optimization={mask}",
        f"--prefetch={prefetch}",
        f"--accounting={accounting}",
        f"--passes={passes}",
        f"--run={run}",
        f"--dataset-hash={corpus_digest}",
    ]
    try:
        with log_path.open("w", encoding="utf-8") as log:
            completed = subprocess.run(
                command, cwd=bundle, stdout=log, stderr=subprocess.STDOUT, text=True,
                check=False, timeout=PROCESS_TIMEOUT_SECONDS
            )
    except subprocess.TimeoutExpired:
        with log_path.open("a", encoding="utf-8") as log:
            log.write(
                f"\nbenchmark_timeout_seconds={PROCESS_TIMEOUT_SECONDS}\n"
            )
        reason = f"timed out after {PROCESS_TIMEOUT_SECONDS} seconds"
        append_execution_failure(
            output, profile_name, run, mask, prefetch, accounting, None,
            reason, log_path,
            [expected_run_dir(output, mask, prefetch, accounting, run)],
        )
        print(f"{label} failed: {reason}; log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise BenchmarkFailure(f"{label} failed: {reason}")
    except OSError as error:
        reason = f"process launch failed: {error}"
        append_execution_failure(
            output, profile_name, run, mask, prefetch, accounting, None,
            reason, log_path,
            [expected_run_dir(output, mask, prefetch, accounting, run)],
        )
        raise BenchmarkFailure(f"{label} failed: {reason}") from error
    if completed.returncode:
        if completed.returncode < 0:
            signal_name = signal.Signals(-completed.returncode).name
            reason = f"terminated by {signal_name}"
        else:
            reason = f"exited with code {completed.returncode}"
        append_execution_failure(
            output, profile_name, run, mask, prefetch, accounting, None,
            reason, log_path,
            [expected_run_dir(output, mask, prefetch, accounting, run)],
        )
        print(f"{label} failed: {reason}; log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise BenchmarkFailure(f"{label} failed: {reason}")
    try:
        environment = enrich_environment_metadata(output, manifest)
        physical_baseline = capture_physical_target_baseline(
            output, environment, {
                "label": label, "run": run, "mask": mask,
                "prefetch": prefetch, "accounting": accounting,
            }, manifest,
        )
        summary = validate_run_artifacts(
            output, log_path, mask, prefetch, accounting, run, corpus_digest,
            require_policy_diagnostics, require_structural_diagnostics, passes,
            physical_baseline,
        )
        if profile_name == "prefetch-diagnostics":
            run_dir = expected_run_dir(output, mask, prefetch, accounting, run)
            prefetch_phase_summary_fields(
                summary, run_dir / "summary.json", required=True
            )
            validate_prefetch_diagnostic_counters(
                read_json_file(run_dir / "counters.json", "diagnostic counters"),
                run_dir,
            )
    except FatalBenchmarkFailure:
        raise
    except BenchmarkFailure as error:
        append_validation_failure(
            output, profile_name, run, mask, prefetch, accounting,
            None, error, log_path,
            [expected_run_dir(output, mask, prefetch, accounting, run)],
        )
        return "VALIDATION_FAILED"
    print(f"{label} passed,exit_code=0,resolution=540x960,"
          f"requested_mask={mask},effective_mask={mask},prefetch={prefetch},"
          f"accounting={accounting},"
          f"physical_target={physical_baseline},"
          f"artifacts={expected_run_dir(output, mask, prefetch, accounting, run)}")
    return "PASS"


def lcg_permutation(round_number, masks, prefetch_profiles, accounting_profiles):
    combinations = list(
        range(len(masks) * len(prefetch_profiles) * len(accounting_profiles))
    )
    state = (SEED + 0x9E3779B97F4A7C15 * (round_number + 1)) & 0xFFFFFFFFFFFFFFFF
    for index in range(len(combinations) - 1, 0, -1):
        state = (state * 6364136223846793005 + 1442695040888963407) & 0xFFFFFFFFFFFFFFFF
        swap = (state >> 1) % (index + 1)
        combinations[index], combinations[swap] = combinations[swap], combinations[index]
    combinations_per_mask = len(prefetch_profiles) * len(accounting_profiles)
    return tuple(
        (
            masks[combination // combinations_per_mask],
            prefetch_profiles[(combination % combinations_per_mask)
                              // len(accounting_profiles)],
            accounting_profiles[combination % len(accounting_profiles)],
        )
        for combination in combinations
    )


def write_suite_plan(output, masks, prefetch_profiles, accounting_profiles, rounds,
                     expected_processes):
    lines = ["round\torder\trun\tmask\tprefetch\taccounting"]
    planned = []
    order = 0
    for round_number in range(rounds):
        for mask, prefetch, accounting in lcg_permutation(
                round_number, masks, prefetch_profiles, accounting_profiles):
            run = round_number + 1
            lines.append(f"{run}\t{order}\t{run}\t{mask}\t{prefetch}\t{accounting}")
            planned.append((round_number, order, run, mask, prefetch, accounting))
            order += 1
    require(len(planned) == expected_processes,
            f"suite plan does not contain {expected_processes} processes")
    combination_counts = {}
    for _, _, _, mask, prefetch, accounting in planned:
        key = (mask, prefetch, accounting)
        combination_counts[key] = combination_counts.get(key, 0) + 1
    require(set(combination_counts) == {
        (mask, prefetch, accounting)
        for mask in masks for prefetch in prefetch_profiles
        for accounting in accounting_profiles
    }, "suite plan combinations differ")
    require(all(count == rounds for count in combination_counts.values()),
            f"suite plan does not contain {rounds} runs per combination")
    require(len({(run, mask, prefetch, accounting)
                 for _, _, run, mask, prefetch, accounting in planned})
            == expected_processes, "suite plan contains duplicate runs")
    (output / "suite-plan.tsv").write_text("\n".join(lines) + "\n", encoding="utf-8")
    return planned


def require_smokes_completed(output, corpus_digest):
    marker = output / "self-test.json"
    require(marker.is_file(), "self-test marker is missing; refusing to run matrix")
    for mask, prefetch in (
        (0, "off"), (0, "on"), (4, "off"), (4, "on"),
        (32799, "off"), (32799, "on"),
    ):
        log_path = output / "logs" / f"smoke-{mask}-{prefetch}-accounting-on.log"
        require(log_path.is_file(), f"smoke log is missing: {log_path}")
        validate_run_artifacts(output, log_path, mask, prefetch, "on", 0, corpus_digest)


def run_matrix(bundle, manifest, output, corpus_digest, masks, prefetch_profiles,
               accounting_profiles, rounds, expected_processes,
               require_policy_diagnostics=False, require_structural_diagnostics=False,
               passes=1, profile_name="matrix", tracker=None):
    plan = write_suite_plan(output, masks, prefetch_profiles, accounting_profiles,
                            rounds, expected_processes)
    if tracker is not None:
        tracker["plannedProcessCount"] += len(plan)
    completed = 0
    for _, _, run, mask, prefetch, accounting in plan:
        track_process_start(tracker)
        status = run_process(
            bundle, manifest, output, corpus_digest, mask, prefetch, accounting, run,
            f"matrix-{run}-{mask}-{prefetch}-{accounting}",
            require_policy_diagnostics, require_structural_diagnostics,
            passes,
            profile_name,
        )
        track_process_complete(tracker, status)
        completed += 1
        print(f"matrix progress={completed}/{expected_processes}")
    require(completed == expected_processes,
            f"matrix did not complete {expected_processes} processes")
    return plan


def delta_percent(control, enabled):
    if control == 0:
        return None
    return round((enabled - control) * 100.0 / control, 6)


def direction(delta):
    if delta < 0:
        return "improvement"
    if delta > 0:
        return "regression"
    return "no-change"


def write_pairwise_comparison(output, records, masks, rounds):
    by_key = {(record["mask"], record["prefetch"], record["run"]): record
              for record in records}
    fields = [
        "pair", "control_mask", "enabled_mask", "prefetch", "run", "variance_status",
        "control_work_p50_ns", "enabled_work_p50_ns", "work_p50_delta_ns",
        "work_p50_delta_pct", "control_work_p95_ns", "enabled_work_p95_ns",
        "work_p95_delta_ns", "work_p95_delta_pct", "control_paint_p50_ns",
        "enabled_paint_p50_ns", "paint_p50_delta_ns", "paint_p50_delta_pct",
        "control_paint_p95_ns", "enabled_paint_p95_ns", "paint_p95_delta_ns",
        "paint_p95_delta_pct",
    ]
    rows = []
    def empty_row(control, enabled, prefetch, run, status):
        row = {field: None for field in fields}
        row.update({
            "pair": f"{control}->{enabled}", "control_mask": control,
            "enabled_mask": enabled, "prefetch": prefetch, "run": run,
            "variance_status": status,
        })
        return row

    for control, enabled in CONTROLLED_PAIRS:
        if control not in masks or enabled not in masks:
            for prefetch in PREFETCH_PROFILES:
                for run in range(1, rounds + 1):
                    rows.append(empty_row(
                        control, enabled, prefetch, run, "NOT_PLANNED"
                    ))
            continue
        for prefetch in PREFETCH_PROFILES:
            pair_records = []
            pair_validity = []
            for run in range(1, rounds + 1):
                control_record = by_key.get((control, prefetch, run))
                enabled_record = by_key.get((enabled, prefetch, run))
                valid = (control_record is not None and enabled_record is not None
                         and control_record.get("status") == "PASS"
                         and enabled_record.get("status") == "PASS")
                pair_validity.append(valid)
                if not valid:
                    pair_records.append(None)
                    continue
                work_delta = (enabled_record["work_time_p50_ns"]
                              - control_record["work_time_p50_ns"])
                pair_records.append(work_delta)
            directions = {direction(delta) for delta in pair_records if delta is not None}
            if not all(pair_validity):
                variance_status = "INCOMPLETE_VALIDATION"
            else:
                variance_status = ("INCONCLUSIVE_VARIANCE" if len(directions) > 1
                                   else "CONSISTENT_DIRECTION")
            for run in range(1, rounds + 1):
                control_record = by_key.get((control, prefetch, run))
                enabled_record = by_key.get((enabled, prefetch, run))
                if not pair_validity[run - 1]:
                    rows.append(empty_row(
                        control, enabled, prefetch, run, variance_status
                    ))
                    continue
                work_p50_delta = (enabled_record["work_time_p50_ns"]
                                  - control_record["work_time_p50_ns"])
                work_p95_delta = (enabled_record["work_time_p95_ns"]
                                  - control_record["work_time_p95_ns"])
                paint_p50_delta = (enabled_record["paint_time_p50_ns"]
                                   - control_record["paint_time_p50_ns"])
                paint_p95_delta = (enabled_record["paint_time_p95_ns"]
                                   - control_record["paint_time_p95_ns"])
                rows.append({
                    "pair": f"{control}->{enabled}",
                    "control_mask": control,
                    "enabled_mask": enabled,
                    "prefetch": prefetch,
                    "run": run,
                    "variance_status": variance_status,
                    "control_work_p50_ns": control_record["work_time_p50_ns"],
                    "enabled_work_p50_ns": enabled_record["work_time_p50_ns"],
                    "work_p50_delta_ns": work_p50_delta,
                    "work_p50_delta_pct": delta_percent(
                        control_record["work_time_p50_ns"],
                        enabled_record["work_time_p50_ns"],
                    ),
                    "control_work_p95_ns": control_record["work_time_p95_ns"],
                    "enabled_work_p95_ns": enabled_record["work_time_p95_ns"],
                    "work_p95_delta_ns": work_p95_delta,
                    "work_p95_delta_pct": delta_percent(
                        control_record["work_time_p95_ns"],
                        enabled_record["work_time_p95_ns"],
                    ),
                    "control_paint_p50_ns": control_record["paint_time_p50_ns"],
                    "enabled_paint_p50_ns": enabled_record["paint_time_p50_ns"],
                    "paint_p50_delta_ns": paint_p50_delta,
                    "paint_p50_delta_pct": delta_percent(
                        control_record["paint_time_p50_ns"],
                        enabled_record["paint_time_p50_ns"],
                    ),
                    "control_paint_p95_ns": control_record["paint_time_p95_ns"],
                    "enabled_paint_p95_ns": enabled_record["paint_time_p95_ns"],
                    "paint_p95_delta_ns": paint_p95_delta,
                    "paint_p95_delta_pct": delta_percent(
                        control_record["paint_time_p95_ns"],
                        enabled_record["paint_time_p95_ns"],
                    ),
                })
    path = output / "write-pixels-policy-comparison.csv"
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    require(len(rows) == len(CONTROLLED_PAIRS) * len(PREFETCH_PROFILES) * rounds,
            "pairwise comparison row count differs")
    print(f"pairwise comparison passed,path={path},rows={len(rows)}")
    return path


def aggregate(output, plan, masks, prefetch_profiles, accounting_profiles, rounds,
              expected_processes, profile_name, require_policy_diagnostics=False,
              require_structural_diagnostics=False, pass_count=1):
    pass_names = benchmark_pass_names(pass_count)
    records = []
    physical_target = load_physical_target_baseline(output)
    physical_fields = physical_summary_fields(physical_target)
    failures = load_validation_failures(output)
    for _, order, run, mask, prefetch, accounting in plan:
        failure = failures.get(
            configuration_key(profile_name, run, mask, prefetch, accounting)
        )
        for pass_index, pass_name in enumerate(pass_names, start=1):
            base = expected_run_dir(output, mask, prefetch, accounting, run)
            if failure is not None:
                records.append({
                    "order": order,
                    "run": run,
                    "pass_index": pass_index,
                    "pass": pass_name,
                    "prefetch": prefetch,
                    "accounting": accounting,
                    "mask": mask,
                    "status": "VALIDATION_FAILED",
                    "validation_status": "VALIDATION_FAILED",
                    "validation_error": validation_failure_message(failure),
                    **physical_fields,
                })
                continue
            path = (base / "summary.json" if pass_count == 1
                    else base / "passes" / pass_name / "summary.json")
            try:
                summary = json.loads(path.read_text(encoding="utf-8"))
            except (OSError, ValueError) as error:
                raise BenchmarkFailure(f"invalid matrix summary: {path}") from error
            require(summary.get("status") == "PASS", f"invalid matrix status: {path}")
            require(summary.get("requestedMask") == mask
                    and summary.get("effectiveMask") == mask,
                    f"matrix mask mismatch: {path}")
            counters_path = path.parent / "counters.json"
            try:
                counters = json.loads(counters_path.read_text(encoding="utf-8"))
            except (OSError, ValueError) as error:
                raise BenchmarkFailure(f"invalid matrix counters: {counters_path}") from error
            diagnostics = validate_diagnostic_counters(
                counters, path.parent, accounting,
                require_policy_diagnostics
            )
            phase_fields = prefetch_phase_summary_fields(
                summary, path, required=profile_name == "prefetch-diagnostics"
            )
            prefetch_counter_fields = (
                validate_prefetch_diagnostic_counters(counters, path.parent)
                if profile_name == "prefetch-diagnostics" else {}
            )
            if require_structural_diagnostics:
                require(accounting == "on",
                        f"{path.parent} structural diagnostics require accounting")
                validate_structural_diagnostics(counters, path.parent, mask)
            for field in TEMPORAL_SUMMARY_FIELDS:
                require_nonnegative_ns(summary.get(field), f"{path} {field}")
            for field in FRAME_THRESHOLD_COUNT_FIELDS:
                require_nonnegative_count(summary.get(field), f"{path} {field}")
            records.append({
                "order": order,
                "run": run,
                "pass_index": pass_index,
                "pass": pass_name,
                "prefetch": prefetch,
                "accounting": accounting,
                "mask": mask,
                "status": summary["status"],
                "validation_status": "VALID",
                "validation_error": "",
                "frame_count": summary["frameCount"],
                "frame_p50_ns": summary["frameTimeP50Ns"],
                "frame_p90_ns": summary["frameTimeP90Ns"],
                "frame_p95_ns": summary["frameTimeP95Ns"],
                "frame_p99_ns": summary["frameTimeP99Ns"],
                "frame_max_ns": summary["frameTimeMaxNs"],
                "work_time_p50_ns": summary["workTimeP50Ns"],
                "work_time_p95_ns": summary["workTimeP95Ns"],
                "work_time_p99_ns": summary["workTimeP99Ns"],
                "work_time_max_ns": summary["workTimeMaxNs"],
                "paint_time_p50_ns": summary["paintTimeP50Ns"],
                "paint_time_p95_ns": summary["paintTimeP95Ns"],
                "paint_time_p99_ns": summary["paintTimeP99Ns"],
                "paint_time_max_ns": summary["paintTimeMaxNs"],
                "frames_over_16_67_count": summary["framesOver16_67Count"],
                "frames_over_33_3_count": summary["framesOver33_3Count"],
                "frames_over_50_count": summary["framesOver50Count"],
                "frames_over_100_count": summary["framesOver100Count"],
                "largest_stall_ns": summary["largestStallNs"],
                "largest_consecutive_over_33_3": summary["largestConsecutiveOver33_3"],
                "prefetch_elapsed_ns": summary["prefetchElapsedNs"],
                "memory_peak_resident_bytes": summary["memoryPeakResidentBytes"],
                **physical_fields,
                **diagnostics,
                **phase_fields,
                **prefetch_counter_fields,
            })
    require(len(records) == expected_processes * pass_count,
            f"aggregation did not find {expected_processes * pass_count} summaries")
    rows = []
    for prefetch in prefetch_profiles:
        for accounting in accounting_profiles:
            baselines = {}
            if 0 in masks:
                for pass_name in pass_names:
                    baseline = [record for record in records
                                if record["prefetch"] == prefetch
                                and record["accounting"] == accounting
                                and record["mask"] == 0
                                and record["pass"] == pass_name
                                and record["status"] == "PASS"]
                    available = len(baseline) == rounds
                    baselines[pass_name] = (
                        available,
                        sum(record["frame_p50_ns"] for record in baseline) // len(baseline)
                        if available else None,
                        sum(record["frame_p95_ns"] for record in baseline) // len(baseline)
                        if available else None,
                    )
            for record in records:
                if record["prefetch"] != prefetch or record["accounting"] != accounting:
                    continue
                row = dict(record)
                row["baseline_scope"] = "same-machine-same-prefetch-mask0"
                baseline_available, baseline_p50, baseline_p95 = baselines.get(
                    record["pass"], (False, None, None)
                )
                valid_comparison = (record["status"] == "PASS"
                                    and (0 not in masks or baseline_available))
                row["comparison_status"] = (
                    "VALID" if valid_comparison else "INCOMPLETE_VALIDATION"
                )
                row["baseline_mask0_p50_ns"] = (
                    baseline_p50 if 0 in masks and baseline_available else None
                )
                row["delta_p50_ns"] = (
                    record["frame_p50_ns"] - baseline_p50
                    if valid_comparison and 0 in masks else None
                )
                row["baseline_mask0_p95_ns"] = (
                    baseline_p95 if 0 in masks and baseline_available else None
                )
                row["delta_p95_ns"] = (
                    record["frame_p95_ns"] - baseline_p95
                    if valid_comparison and 0 in masks else None
                )
                rows.append(row)
    fields = [
        "order", "run", "pass_index", "pass", "prefetch", "accounting", "mask", "status",
        "validation_status", "validation_error", "comparison_status", "frame_count",
        "frame_p50_ns",
        "frame_p90_ns", "frame_p95_ns", "frame_p99_ns", "frame_max_ns",
        "work_time_p50_ns", "work_time_p95_ns", "work_time_p99_ns", "work_time_max_ns",
        "paint_time_p50_ns", "paint_time_p95_ns", "paint_time_p99_ns",
        "paint_time_max_ns",
        "frames_over_16_67_count", "frames_over_33_3_count", "frames_over_50_count",
        "frames_over_100_count", "largest_stall_ns", "largest_consecutive_over_33_3",
        "prefetch_elapsed_ns", "memory_peak_resident_bytes",
        *(csv_name for _, csv_name in PREFETCH_PHASE_SUMMARY_FIELDS),
        "logical_width", "logical_height", "physical_width", "physical_height",
        "physical_row_bytes", "physical_pixel_bytes", "physical_color_type",
        "physical_alpha_type", "physical_color_classification", "renderer_backend",
    ] + list(DIAGNOSTIC_SUMMARY_FIELDS) + [
        csv_name for _, csv_name in PREFETCH_DIAGNOSTIC_COUNTER_FIELDS
    ] + [
        "baseline_scope",
        "baseline_mask0_p50_ns", "delta_p50_ns", "baseline_mask0_p95_ns", "delta_p95_ns",
    ]
    path = output / (
        "prefetch-diagnostics.csv" if profile_name == "prefetch-diagnostics"
        else "summary.csv"
    )
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(sorted(rows, key=lambda row: (
            row["run"], row["pass_index"], row["prefetch"], row["mask"]
        )))
    print(f"aggregation passed,summary={path},rows={len(rows)}")
    if profile_name == "write-pixels-policy":
        write_pairwise_comparison(output, records, masks, rounds)
    return path


def write_reuse_pairwise_comparison(output, rows, masks, prefetch_profiles, rounds):
    candidate_pairs = (
        (0, 32), (0, 8192), (32, 8224), (8192, 8224),
        (32795, 32799), (32827, 32831),
    )
    pairs = tuple((control, enabled) for control, enabled in candidate_pairs
                  if control in masks and enabled in masks)
    pass_names = ("cold-forward", "warm-reverse", "warm-forward")
    by_key = {(row["mask"], row["prefetch"], row["run"], row["pass"]): row
              for row in rows}
    fields = [
        "pair", "prefetch", "run", "pass", "variance_status",
        "control_work_p50_ns", "enabled_work_p50_ns", "work_p50_delta_ns",
        "work_p50_delta_pct", "control_work_p95_ns", "enabled_work_p95_ns",
        "work_p95_delta_ns", "work_p95_delta_pct", "control_paint_p50_ns",
        "enabled_paint_p50_ns", "paint_p50_delta_ns", "paint_p50_delta_pct",
        "control_paint_p95_ns", "enabled_paint_p95_ns", "paint_p95_delta_ns",
        "paint_p95_delta_pct",
    ]
    output_rows = []
    def empty_row(control, enabled, prefetch, run, pass_name, status):
        row = {field: None for field in fields}
        row.update({
            "pair": f"{control}->{enabled}", "prefetch": prefetch,
            "run": run, "pass": pass_name, "variance_status": status,
        })
        return row

    for control, enabled in pairs:
        for prefetch in prefetch_profiles:
            for pass_name in pass_names:
                deltas = []
                for run in range(1, rounds + 1):
                    control_row = by_key.get((control, prefetch, run, pass_name))
                    enabled_row = by_key.get((enabled, prefetch, run, pass_name))
                    valid = (control_row is not None and enabled_row is not None
                             and control_row.get("status") == "PASS"
                             and enabled_row.get("status") == "PASS")
                    if not valid:
                        deltas.append(None)
                        continue
                    deltas.append(enabled_row["work_time_p50_ns"]
                                   - control_row["work_time_p50_ns"])
                directions = {direction(delta) for delta in deltas if delta is not None}
                variance_status = (
                    "INCOMPLETE_VALIDATION" if any(delta is None for delta in deltas)
                    else "INCONCLUSIVE_VARIANCE" if len(directions) > 1
                    else "CONSISTENT_DIRECTION"
                )
                for run in range(1, rounds + 1):
                    control_row = by_key.get((control, prefetch, run, pass_name))
                    enabled_row = by_key.get((enabled, prefetch, run, pass_name))
                    if (control_row is None or enabled_row is None
                            or control_row.get("status") != "PASS"
                            or enabled_row.get("status") != "PASS"):
                        output_rows.append(empty_row(
                            control, enabled, prefetch, run, pass_name, variance_status
                        ))
                        continue
                    work_p50_delta = (enabled_row["work_time_p50_ns"]
                                      - control_row["work_time_p50_ns"])
                    work_p95_delta = (enabled_row["work_time_p95_ns"]
                                      - control_row["work_time_p95_ns"])
                    paint_p50_delta = (enabled_row["paint_time_p50_ns"]
                                       - control_row["paint_time_p50_ns"])
                    paint_p95_delta = (enabled_row["paint_time_p95_ns"]
                                       - control_row["paint_time_p95_ns"])
                    output_rows.append({
                        "pair": f"{control}->{enabled}",
                        "prefetch": prefetch,
                        "run": run,
                        "pass": pass_name,
                        "variance_status": variance_status,
                        "control_work_p50_ns": control_row["work_time_p50_ns"],
                        "enabled_work_p50_ns": enabled_row["work_time_p50_ns"],
                        "work_p50_delta_ns": work_p50_delta,
                        "work_p50_delta_pct": delta_percent(
                            control_row["work_time_p50_ns"], enabled_row["work_time_p50_ns"]),
                        "control_work_p95_ns": control_row["work_time_p95_ns"],
                        "enabled_work_p95_ns": enabled_row["work_time_p95_ns"],
                        "work_p95_delta_ns": work_p95_delta,
                        "work_p95_delta_pct": delta_percent(
                            control_row["work_time_p95_ns"], enabled_row["work_time_p95_ns"]),
                        "control_paint_p50_ns": control_row["paint_time_p50_ns"],
                        "enabled_paint_p50_ns": enabled_row["paint_time_p50_ns"],
                        "paint_p50_delta_ns": paint_p50_delta,
                        "paint_p50_delta_pct": delta_percent(
                            control_row["paint_time_p50_ns"], enabled_row["paint_time_p50_ns"]),
                        "control_paint_p95_ns": control_row["paint_time_p95_ns"],
                        "enabled_paint_p95_ns": enabled_row["paint_time_p95_ns"],
                        "paint_p95_delta_ns": paint_p95_delta,
                        "paint_p95_delta_pct": delta_percent(
                            control_row["paint_time_p95_ns"], enabled_row["paint_time_p95_ns"]),
                    })
    path = output / "m4-reuse-pairwise.csv"
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(output_rows)
    print(f"reuse pairwise comparison passed,path={path},rows={len(output_rows)}")
    return path


def aggregate_reuse(output, plan, masks, prefetch_profiles, accounting_profiles, rounds,
                    expected_processes, profile_name, require_policy_diagnostics,
                    require_structural_diagnostics, pass_count):
    counter_fields = (
        "imageMaterializations", "nativeGeometryMaterializations", "backingLiveBytes",
        "backingPeakBytes", "targetColorAttempts", "targetColorHits",
        "targetColorFallbacks", "targetColorMaterializations", "targetColorConvertedBytes",
        "targetColorUniqueSources", "targetColorUniqueFullKeys",
        "targetColorUniqueNoDestinationKeys", "targetColorUniqueIntrinsicKeys",
        "targetColorAcquisitionSources", "targetColorPendingReplacements",
        "physicalVariantLookups", "physicalVariantHits",
        "physicalVariantMisses", "physicalVariantStores", "physicalVariantEvictions",
        "physicalVariantBytes", "physicalVariantUniqueSources",
        "physicalVariantUniqueFullKeys", "physicalVariantUniqueNoSurfaceSizeKeys",
        "physicalVariantPendingReplacements", "sharedSlotTargetToPhysical",
        "sharedSlotPhysicalToTarget", "sharedPendingTargetToPhysical",
        "sharedPendingPhysicalToTarget", "physicalIdentityAttempts",
        "physicalIdentityHits", "physicalIdentityFallbacks",
    )
    pass_names = ("cold-forward", "warm-reverse", "warm-forward")
    environment = json.loads((output / "environment.json").read_text(encoding="utf-8"))
    rows = []
    for _, order, run, mask, prefetch, accounting in plan:
        base = expected_run_dir(output, mask, prefetch, accounting, run)
        structural_activity = set()
        for pass_index, pass_name in enumerate(pass_names, start=1):
            pass_dir = base / "passes" / pass_name
            summary_path = pass_dir / "summary.json"
            counters_path = pass_dir / "counters.json"
            summary = json.loads(summary_path.read_text(encoding="utf-8"))
            counters = json.loads(counters_path.read_text(encoding="utf-8"))
            require(summary.get("status") == "PASS", f"invalid reuse status: {summary_path}")
            require(summary.get("pass") == pass_name
                    and summary.get("requestedMask") == mask
                    and summary.get("effectiveMask") == mask
                    and summary.get("prefetch") == prefetch
                    and summary.get("accounting") == accounting,
                    f"invalid reuse configuration: {summary_path}")
            diagnostics = validate_diagnostic_counters(
                counters, pass_dir, accounting, require_policy_diagnostics)
            if require_structural_diagnostics:
                structural_activity.update(
                    validate_structural_diagnostics(
                        counters, pass_dir, mask, allow_inactive=pass_index > 1,
                    )
                )
            for field in TEMPORAL_SUMMARY_FIELDS:
                require_nonnegative_ns(summary.get(field), f"{summary_path} {field}")
            for field in FRAME_THRESHOLD_COUNT_FIELDS:
                require_nonnegative_count(summary.get(field), f"{summary_path} {field}")
            row = {
                "order": order,
                "run": run,
                "pass_index": pass_index,
                "pass": pass_name,
                "prefetch": prefetch,
                "accounting": accounting,
                "mask": mask,
                "status": summary["status"],
                "frame_count": summary["frameCount"],
                "frame_p50_ns": summary["frameTimeP50Ns"],
                "frame_p95_ns": summary["frameTimeP95Ns"],
                "frame_p99_ns": summary["frameTimeP99Ns"],
                "frame_max_ns": summary["frameTimeMaxNs"],
                "work_time_p50_ns": summary["workTimeP50Ns"],
                "work_time_p95_ns": summary["workTimeP95Ns"],
                "work_time_p99_ns": summary["workTimeP99Ns"],
                "work_time_max_ns": summary["workTimeMaxNs"],
                "paint_time_p50_ns": summary["paintTimeP50Ns"],
                "paint_time_p95_ns": summary["paintTimeP95Ns"],
                "paint_time_p99_ns": summary["paintTimeP99Ns"],
                "paint_time_max_ns": summary["paintTimeMaxNs"],
                "target_color_class": environment.get("skiaSurfaceColorClassification"),
                "raster_bytes": counters.get("accounting", {}).get("raster", {}).get("bytes"),
                **diagnostics,
            }
            for field in counter_fields:
                row[field] = counters.get(field)
            rows.append(row)
        if require_structural_diagnostics:
            for bit, attempts_key in (
                (8192, "targetColorAttempts"),
                (16384, "physicalVariantLookups"),
                (32768, "physicalIdentityAttempts"),
            ):
                if mask & bit:
                    require(bit in structural_activity,
                            f"{base} reuse passes never exercised {attempts_key}")
    require(len(rows) == expected_processes * pass_count,
            f"reuse aggregation did not find {expected_processes * pass_count} pass summaries")
    fields = [
        "order", "run", "pass_index", "pass", "prefetch", "accounting", "mask", "status",
        "frame_count", "frame_p50_ns", "frame_p95_ns", "frame_p99_ns", "frame_max_ns",
        "work_time_p50_ns", "work_time_p95_ns", "work_time_p99_ns", "work_time_max_ns",
        "paint_time_p50_ns", "paint_time_p95_ns", "paint_time_p99_ns", "paint_time_max_ns",
        "target_color_class", "raster_bytes",
        "logical_width", "logical_height", "physical_width", "physical_height",
        "physical_row_bytes", "physical_pixel_bytes", "physical_color_type",
        "physical_alpha_type", "physical_color_classification", "renderer_backend",
    ] + list(DIAGNOSTIC_SUMMARY_FIELDS) + list(counter_fields)
    path = output / "m4-reuse-passes.csv"
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(sorted(rows, key=lambda row: (row["run"], row["pass_index"],
                                                       row["prefetch"], row["mask"])))
    write_reuse_pairwise_comparison(output, rows, masks, prefetch_profiles, rounds)
    print(f"reuse aggregation passed,summary={path},rows={len(rows)}")
    return path


def write_zip(bundle, output):
    archive = output / f"totalcross-image-benchmark-results-{time.time_ns()}.zip"
    with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED) as destination:
        for path in sorted(bundle.rglob("*")):
            if (path.is_file() and path != archive
                    and not path.name.startswith("totalcross-image-benchmark-results-")):
                destination.write(path, path.relative_to(bundle).as_posix())
    print(f"final ZIP={archive}")
    return archive


def run_decode_phase(bundle, phase):
    manifest = load_manifest(bundle)
    require(manifest.get("includeDecodeAssets") is True,
            "decode phase requested but this package has no decode assets; "
            "rebuild it with --include-decode")
    script = bundle / "run-image-decode-benchmark.py"
    require(script.is_file(), f"decode phase requested but runner is missing: {script}")
    completed = subprocess.run(
        [sys.executable, str(script), "--bundle", str(bundle), "--phase", phase],
        cwd=bundle, check=False,
    )
    require(completed.returncode == 0,
            f"decode {phase} phase failed with exit code {completed.returncode}")


def run_scroll_profile(bundle, manifest, output, corpus_digest, profile_name, tracker=None):
    profile = profile_config(profile_name)
    if profile.get("rendering_reuse"):
        return run_scroll_reuse_matrix(
            bundle, manifest, output, corpus_digest, profile, tracker
        )
    plan = run_matrix(
        bundle, manifest, output, corpus_digest, profile["masks"], profile["prefetch"],
        profile["accounting"], profile["rounds"], profile["expected_processes"],
        passes=profile.get("passes", 1), profile_name=profile_name, tracker=tracker,
    )
    return aggregate(
        output, plan, profile["masks"], profile["prefetch"], profile["accounting"],
        profile["rounds"], profile["expected_processes"], profile_name,
        pass_count=profile.get("passes", 1),
    )


def write_default_execution_summary(output, manifest, status="PASS", tracker=None,
                                    fatal_error=None):
    physical_target = load_physical_target_baseline(output, manifest)
    environment_baseline = load_environment_baseline(output, manifest)
    failures = list(load_validation_failures(output).values())
    process_failures = [
        record for record in failures if record.get("scope", "process") == "process"
    ]
    if tracker is None:
        tracker = new_execution_tracker()
        tracker.update({
            "plannedProcessCount": DEFAULT_EXPECTED_PROCESS_COUNT,
            "launchedProcessCount": DEFAULT_EXPECTED_PROCESS_COUNT,
            "completedProcessCount": DEFAULT_EXPECTED_PROCESS_COUNT,
            "validProcessCount": DEFAULT_EXPECTED_PROCESS_COUNT - len(process_failures),
            "validationFailedProcessCount": len(process_failures),
        })
    summary = {
        "status": status,
        "sourceCommit": manifest["sourceCommit"],
        "sdkSourceAttestation": manifest.get("sdkSourceAttestation"),
        "runtimeSha256": manifest["runtimeSha256"],
        "tcvmSha256": manifest.get("tcvmSha256"),
        "logicalTarget": dict(LOGICAL_TARGET),
        "physicalTarget": physical_target,
        "environmentCharacteristics": environment_baseline,
        "plannedProcessCount": tracker["plannedProcessCount"],
        "launchedProcessCount": tracker["launchedProcessCount"],
        "completedProcessCount": tracker["completedProcessCount"],
        "validProcessCount": tracker["validProcessCount"],
        "validationFailedProcessCount": tracker["validationFailedProcessCount"],
        "validationFailureCount": len(failures),
        "validationFailures": failures,
        "fatalFailure": fatal_error,
        "selfTestProcessCount": 1,
        "profileProcessCounts": {
            name: profile_config(name)["expected_processes"]
            for name in (
                "reduced-image-optimizations", "scroll-raster-correctness",
                "scroll-raster-performance", "release-default-scroll",
                "release-candidate-scroll",
            )
        },
        "profilePassCounts": {
            name: profile_config(name).get("passes", 1)
            for name in (
                "reduced-image-optimizations", "scroll-raster-correctness",
                "scroll-raster-performance", "release-default-scroll",
                "release-candidate-scroll",
            )
        },
        "expectedMeasuredPassCount": (
            REDUCED_PROCESS_COUNT * EXPLORATORY_PASS_COUNT
            + (CORRECTNESS_PROCESS_COUNT + PERFORMANCE_PROCESS_COUNT
               + RELEASE_PROCESS_COUNT + RELEASE_CANDIDATE_PROCESS_COUNT) * 2
        ),
        "diagnosticProfile": {
            "name": "prefetch-diagnostics",
            "processCount": PREFETCH_DIAGNOSTIC_PROCESS_COUNT,
            "masks": list(PREFETCH_DIAGNOSTIC_MASKS),
            "accounting": "on",
        },
        "profileWorkloadImages": {
            name: profile_config(name)["workload_images"]
            for name in (
                "reduced-image-optimizations", "scroll-raster-correctness",
                "scroll-raster-performance", "release-default-scroll",
                "release-candidate-scroll",
            )
        },
        "expectedProcessCount": DEFAULT_EXPECTED_PROCESS_COUNT,
        "decodeIncluded": manifest["includeDecodeAssets"],
    }
    require(sum(summary["profileProcessCounts"].values())
            == summary["expectedProcessCount"],
            "default execution process count does not reconcile")
    path = output / "default-execution-summary.json"
    path.write_text(json.dumps(summary, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(
        f"default execution status={status},"
        f"processes={summary['expectedProcessCount']},"
        f"completed={summary['completedProcessCount']},"
        f"validation_failed_processes={summary['validationFailedProcessCount']},"
        f"validation_failures={summary['validationFailureCount']},"
        f"summary={path}"
    )


def run_phase(bundle, phase, profile_name):
    if phase in ("decode-self-test", "decode-smokes", "decode-full"):
        decode_phase = phase.removeprefix("decode-")
        run_decode_phase(bundle, decode_phase)
        return
    manifest = load_manifest(bundle)
    output = bundle / "results"
    preflight(bundle, manifest, output, phase)
    if phase in ("self-test", "full"):
        _, _, corpus_digest = self_test(bundle, manifest, output)
    else:
        _, images, corpus_digest, _ = validate_bundle(bundle, manifest)
        require((output / "self-test.json").is_file(),
                "self-test must pass before this phase")
        require(len(images) == EXPECTED_JPEGS, "corpus changed after self-test")
    if phase == "self-test":
        return
    write_execution_state(output, "RUNNING", manifest, resultsState="RUNNING")
    tracker = new_execution_tracker()
    try:
        if phase == "full":
            for name in (
                "reduced-image-optimizations", "scroll-raster-correctness",
                "scroll-raster-performance", "release-default-scroll",
                "release-candidate-scroll",
            ):
                run_scroll_profile(bundle, manifest, output, corpus_digest, name, tracker)
            if manifest["includeDecodeAssets"]:
                run_decode_phase(bundle, "full")
            failures = load_validation_failures(output)
            status = ("PASS_WITH_VALIDATION_FAILURES" if failures
                      else "PASS")
            write_default_execution_summary(
                output, manifest, status=status, tracker=tracker
            )
            write_zip(bundle, output)
            write_execution_state(output, status, manifest,
                                  resultsState="COMPLETE", **tracker)
            return
        if phase == "smokes":
            profile_name = "reduced-image-optimizations"
        elif phase == "matrix":
            require(profile_name != "full", "matrix phase requires a named scroll profile")
        elif phase != profile_name:
            require(phase in PROFILES, f"unsupported scroll phase: {phase}")
            profile_name = phase
        run_scroll_profile(bundle, manifest, output, corpus_digest, profile_name, tracker)
        write_zip(bundle, output)
        failures = load_validation_failures(output)
        status = "PASS_WITH_VALIDATION_FAILURES" if failures else "PASS"
        write_execution_state(output, status, manifest,
                              resultsState="COMPLETE", **tracker)
    except (BenchmarkFailure, OSError) as error:
        if phase == "full":
            try:
                write_default_execution_summary(
                    output, manifest, status="INCOMPLETE", tracker=tracker,
                    fatal_error=str(error),
                )
                write_execution_state(output, "INCOMPLETE", manifest,
                                      resultsState="INCOMPLETE",
                                      fatalFailure=str(error), **tracker)
            except (BenchmarkFailure, OSError) as state_error:
                print(f"could not preserve fatal execution status: {state_error}",
                      file=sys.stderr)
        else:
            try:
                write_execution_state(output, "INCOMPLETE", manifest,
                                      resultsState="INCOMPLETE",
                                      fatalFailure=str(error))
            except (BenchmarkFailure, OSError) as state_error:
                print(f"could not preserve fatal execution status: {state_error}",
                      file=sys.stderr)
        raise


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--bundle", type=Path, default=Path.cwd(),
        help="bundle directory (defaults to the current directory)",
    )
    parser.add_argument(
        "--phase", choices=(
            "self-test", "smokes", "matrix", "full", "reduced-image-optimizations",
            "scroll-raster-correctness", "scroll-raster-performance",
            "release-default-scroll", "release-candidate-scroll", "prefetch-diagnostics",
            "decode-self-test",
            "decode-smokes", "decode-full",
        ), default="full",
        help="run one fail-fast phase; full runs the 30-process non-decode suite",
    )
    parser.add_argument(
        "--profile", choices=("full",) + tuple(PROFILES), default="full",
        help="scroll matrix profile (default: full)",
    )
    args = parser.parse_args(argv[1:])
    bundle = args.bundle.expanduser().resolve()
    require(bundle.is_dir(), f"bundle directory not found: {bundle}")
    run_phase(bundle, args.phase, args.profile)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv))
    except (BenchmarkFailure, OSError, ValueError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
