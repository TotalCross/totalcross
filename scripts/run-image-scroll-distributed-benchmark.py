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
from pathlib import Path
import signal
import subprocess
import sys
import time
import zipfile


EXPECTED_JPEGS = 663
EXPECTED_TARGET_WIDTH = 1080
EXPECTED_TARGET_HEIGHT = 1920
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
MASKS = (
    0, 6, 8, 16, 32, 8192, 16384, 32768, 32795, 32799,
)
DEFAULT_EFFECTIVE_MASK = 32799
PREFETCH_PROFILES = ("on",)
ACCOUNTING_PROFILES = ("on", "off")
ROUNDS = 3
SEED = 73001
REDUCED_PROCESS_COUNT = len(MASKS) * ROUNDS
CORRECTNESS_PROCESS_COUNT = 2
PERFORMANCE_PROCESS_COUNT = 6
RELEASE_PROCESS_COUNT = 6
DEFAULT_MATRIX_PROCESS_COUNT = (
    REDUCED_PROCESS_COUNT + CORRECTNESS_PROCESS_COUNT
    + PERFORMANCE_PROCESS_COUNT + RELEASE_PROCESS_COUNT
)
DEFAULT_EXPECTED_PROCESS_COUNT = DEFAULT_MATRIX_PROCESS_COUNT
PROFILES = {
    "reduced-image-optimizations": {
        "masks": MASKS,
        "prefetch": PREFETCH_PROFILES,
        "accounting": ("off",),
        "rounds": ROUNDS,
        "expected_processes": REDUCED_PROCESS_COUNT,
        "passes": 1,
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


def require(condition, message):
    if not condition:
        raise BenchmarkFailure(message)


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
    require(manifest.get("selfTestPhaseCount") == 1,
            "manifest selfTestPhaseCount differs")
    require(manifest.get("includeDecodeAssets") in (False, True),
            "manifest includeDecodeAssets must be boolean")
    expected_corpus_variants = (DECODE_CORPUS_VARIANTS
                                if manifest["includeDecodeAssets"]
                                else SCROLL_CORPUS_VARIANTS)
    require(tuple(manifest.get("corpusVariants", ())) == expected_corpus_variants,
            "manifest corpus variants differ")
    expected_profiles = {
        "reduced-image-optimizations": {
            "masks": list(MASKS), "prefetch": ["on"], "accounting": ["off"],
            "renderingReuse": [], "rounds": ROUNDS, "processCount": REDUCED_PROCESS_COUNT,
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
    }
    require(manifest.get("profiles") == expected_profiles,
            "manifest profile matrix differs")
    source_commit = manifest.get("sourceCommit")
    require(isinstance(source_commit, str) and len(source_commit) >= 7,
            "manifest sourceCommit is missing")
    runtime_sha = manifest.get("runtimeSha256")
    require(isinstance(runtime_sha, str) and len(runtime_sha) == 64,
            "manifest runtimeSha256 is missing")
    if manifest.get("target") == "windows-x64":
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
    if manifest.get("target") == "windows-x64":
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
    output.mkdir(parents=True, exist_ok=True)
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
        "tcvmSha256": manifest.get("tcvmSha256"),
        "executable": str(executable.relative_to(bundle)),
        "runtime": manifest["runtime"],
    }
    (output / "self-test.json").write_text(
        json.dumps(marker, indent=2, sort_keys=True) + "\n", encoding="utf-8"
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
                                 require_structural_diagnostics, pass_count):
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
    expected_passes = ("cold-forward", "warm-reverse", "warm-forward")
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
    target_width = environment.get("skiaSurfaceWidth")
    target_height = environment.get("skiaSurfaceHeight")
    target_row_bytes = environment.get("skiaSurfaceRowBytes")
    target_color_type = environment.get("skiaSurfaceColorType")
    target_alpha_type = environment.get("skiaSurfaceAlphaType")
    n32_color_type = environment.get("kN32SkColorType")
    target_class = environment.get("skiaSurfaceColorClassification")
    require(isinstance(target_width, int) and target_width > 0
            and isinstance(target_height, int) and target_height > 0
            and isinstance(target_row_bytes, int) and target_row_bytes > 0
            and isinstance(target_color_type, int) and target_color_type >= 0
            and isinstance(target_alpha_type, int) and target_alpha_type >= 0
            and isinstance(n32_color_type, int) and n32_color_type >= 0,
            f"{output}/environment.json lacks native target metrics")
    require(target_class in ("BGRA8888", "RGB565", "OTHER"),
            f"{output}/environment.json lacks target-color classification")
    require(environment.get("rendererBackend") in ("software", "gpu"),
            f"{output}/environment.json lacks renderer backend")
    require(target_width == EXPECTED_TARGET_WIDTH and target_height == EXPECTED_TARGET_HEIGHT,
            f"{output}/environment.json has unexpected physical target size")
    minimum_row_bytes = {"BGRA8888": target_width * 4,
                         "RGB565": target_width * 2,
                         "OTHER": target_width}[target_class]
    require(target_row_bytes >= minimum_row_bytes,
            f"{output}/environment.json has an inconsistent target pitch")

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
    require(value >= minimum, f"{description} {field} is below {minimum}")
    return value


def percentile(values, fraction):
    require(values, "cannot calculate a percentile from an empty sequence")
    ordered = sorted(values)
    index = max(0, min(len(ordered) - 1, math.ceil(fraction * len(ordered)) - 1))
    return ordered[index]


def validate_scroll_reuse_artifacts(output, log_path, manifest, profile, mask,
                                    prefetch, accounting, rendering_reuse, run,
                                    expected_image_count):
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
    environment = json.loads(environment_path.read_text(encoding="utf-8"))
    require(environment.get("datasetFileCount") == expected_image_count
            and environment.get("datasetHash") == manifest.get("datasetHash"),
            f"{environment_path} has an unexpected scroll corpus identity")
    require(environment.get("effectiveLogicalWidth") == 540
            and environment.get("effectiveLogicalHeight") == 960,
            f"{environment_path} is not 540x960")
    target_pixel_bytes = environment.get("skiaSurfacePixelBytes")
    require(isinstance(target_pixel_bytes, int) and target_pixel_bytes in (2, 4),
            f"{environment_path} lacks a supported target pixel width")
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
            "pass", "actual_delta", "movement", "measured", "work_time_ns",
            "screen_update_ns", "row_paints", "image_paints",
        }
        require(required <= set(reader.fieldnames or ()),
                f"{frames_path} lacks scroll reuse frame fields")
        for row_data in reader:
            pass_name = row_data.get("pass")
            require(pass_name in expected_passes, f"{frames_path} has an unexpected pass")
            frame_passes.add(pass_name)
            for field in ("actual_delta", "movement", "measured", "work_time_ns",
                          "screen_update_ns", "row_paints", "image_paints"):
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
        raise BenchmarkFailure(f"{label} timed out; log={log_path}")
    if completed.returncode:
        print(f"{label} failed,exit_code={completed.returncode},log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise BenchmarkFailure(f"{label} failed with exit code {completed.returncode}")
    rows, hashes = validate_scroll_reuse_artifacts(
        output, log_path, manifest, profile, mask, prefetch, accounting, rendering_reuse, run,
        expected_image_count,
    )
    print(f"{label} passed,artifacts={expected_reuse_run_dir(output, profile, mask, prefetch, accounting, rendering_reuse, run)}")
    return rows, hashes


def aggregate_scroll_reuse(output, manifest, profile, plan, rows_by_key, hashes_by_key):
    by_pair = {}
    for _, _, run, mask, prefetch, accounting, rendering_reuse in plan:
        by_pair.setdefault((run, mask, prefetch, accounting), {})[rendering_reuse] = (
            rows_by_key[(run, mask, prefetch, accounting, rendering_reuse)],
            hashes_by_key[(run, mask, prefetch, accounting, rendering_reuse)],
        )
    summary_rows = []
    hash_rows = []
    for (run, mask, prefetch, accounting), modes in by_pair.items():
        require(set(modes) == {"off", "on"},
                f"{profile['name']} run {run} lacks an off/on pair")
        off_rows, off_hashes = modes["off"]
        on_rows, on_hashes = modes["on"]
        require(off_hashes == on_hashes,
                f"{profile['name']} run {run} viewport hashes differ between reuse modes")
        for rendering_reuse, rows in (("off", off_rows), ("on", on_rows)):
            summary_rows.extend(rows)
            for (pass_name, waypoint_index), hashes in sorted(
                    (off_hashes if rendering_reuse == "off" else on_hashes).items()):
                hash_rows.append({
                    "profile": profile["name"], "run": run, "mask": mask_token(mask),
                    "prefetch": prefetch, "accounting": accounting,
                    "rendering_reuse": rendering_reuse, "pass": pass_name,
                    "waypoint_index": waypoint_index, "hash": hashes[0],
                    "top_hash": hashes[1], "bottom_hash": hashes[2],
                })
    summary_fields = list(summary_rows[0]) if summary_rows else []
    summary_path = output / f"scroll-raster-summary-{profile['name']}.csv"
    with summary_path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=summary_fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(summary_rows)
    hash_path = output / f"scroll-raster-waypoint-hashes-{profile['name']}.csv"
    with hash_path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=list(hash_rows[0]), lineterminator="\n")
        writer.writeheader()
        writer.writerows(hash_rows)
    require(len(summary_rows) == profile["expected_processes"] * 2,
            f"{profile['name']} summary row count differs")
    print(f"scroll reuse aggregation passed,profile={profile['name']},"
          f"rows={len(summary_rows)},summary={summary_path},hashes={hash_path}")
    return summary_path, hash_path


def run_scroll_reuse_matrix(bundle, manifest, output, corpus_digest, profile):
    plan = write_reuse_suite_plan(output, profile)
    rows_by_key = {}
    hashes_by_key = {}
    for _, _, run, mask, prefetch, accounting, rendering_reuse in plan:
        app_profile = profile.get("app_profile", profile["name"])
        rows, hashes = run_scroll_reuse_process(
            bundle, manifest, output, corpus_digest, app_profile, mask, prefetch,
            accounting, rendering_reuse, run, profile["workload_images"]
        )
        key = (run, mask, prefetch, accounting, rendering_reuse)
        rows_by_key[key] = rows
        hashes_by_key[key] = hashes
    return aggregate_scroll_reuse(output, manifest, profile, plan, rows_by_key, hashes_by_key)


def validate_run_artifacts(output, log_path, mask, prefetch, accounting, run, dataset_digest,
                           require_policy_diagnostics=False,
                           require_structural_diagnostics=False, passes=1):
    if passes != 1:
        return validate_reuse_run_artifacts(
            output, log_path, mask, prefetch, accounting, run, dataset_digest,
            require_policy_diagnostics, require_structural_diagnostics, passes,
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
    target_width = environment.get("skiaSurfaceWidth")
    target_height = environment.get("skiaSurfaceHeight")
    target_row_bytes = environment.get("skiaSurfaceRowBytes")
    target_color_type = environment.get("skiaSurfaceColorType")
    target_alpha_type = environment.get("skiaSurfaceAlphaType")
    n32_color_type = environment.get("kN32SkColorType")
    require(isinstance(target_width, int) and target_width > 0
            and isinstance(target_height, int) and target_height > 0
            and isinstance(target_row_bytes, int) and target_row_bytes > 0
            and isinstance(target_color_type, int) and target_color_type >= 0
            and isinstance(target_alpha_type, int) and target_alpha_type >= 0
            and isinstance(n32_color_type, int) and n32_color_type >= 0,
            f"{output}/environment.json lacks native target metrics")
    require(environment.get("skiaSurfaceColorClassification") in (
        "BGRA8888", "RGB565", "OTHER",
    ), f"{output}/environment.json lacks target-color classification")
    require(environment.get("rendererBackend") in ("software", "gpu"),
            f"{output}/environment.json lacks renderer backend")
    require(target_width == EXPECTED_TARGET_WIDTH
            and target_height == EXPECTED_TARGET_HEIGHT,
            f"{output}/environment.json has unexpected physical target size")
    minimum_row_bytes = {
        "BGRA8888": target_width * 4,
        "RGB565": target_width * 2,
        "OTHER": target_width,
    }[environment["skiaSurfaceColorClassification"]]
    require(target_row_bytes >= minimum_row_bytes,
            f"{output}/environment.json has an inconsistent target pitch")
    validate_diagnostic_counters(counters, run_dir, accounting, require_policy_diagnostics)
    if require_structural_diagnostics:
        require(accounting == "on", f"{run_dir} structural diagnostics require accounting")
        validate_structural_diagnostics(counters, run_dir, mask)
    return run_summary


def run_process(bundle, manifest, output, corpus_digest, mask, prefetch, accounting, run, label,
                require_policy_diagnostics=False, require_structural_diagnostics=False,
                passes=1):
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
        print(f"{label} failed: {reason}; log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise BenchmarkFailure(f"{label} failed: {reason}")
    if completed.returncode:
        if completed.returncode < 0:
            signal_name = signal.Signals(-completed.returncode).name
            reason = f"terminated by {signal_name}"
        else:
            reason = f"exited with code {completed.returncode}"
        print(f"{label} failed: {reason}; log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise BenchmarkFailure(f"{label} failed: {reason}")
    try:
        summary = validate_run_artifacts(
            output, log_path, mask, prefetch, accounting, run, corpus_digest,
            require_policy_diagnostics, require_structural_diagnostics, passes
        )
    except BenchmarkFailure as error:
        print(f"{label} failed validation; log={log_path}", file=sys.stderr)
        print(tail(log_path), file=sys.stderr)
        raise
    print(f"{label} passed,exit_code=0,resolution=540x960,"
          f"requested_mask={mask},effective_mask={mask},prefetch={prefetch},"
          f"accounting={accounting},"
          f"artifacts={expected_run_dir(output, mask, prefetch, accounting, run)}")
    return summary


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
               passes=1):
    plan = write_suite_plan(output, masks, prefetch_profiles, accounting_profiles,
                            rounds, expected_processes)
    completed = 0
    for _, _, run, mask, prefetch, accounting in plan:
        run_process(
            bundle, manifest, output, corpus_digest, mask, prefetch, accounting, run,
            f"matrix-{run}-{mask}-{prefetch}-{accounting}",
            require_policy_diagnostics, require_structural_diagnostics,
            passes,
        )
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
    for control, enabled in CONTROLLED_PAIRS:
        require(control in masks and enabled in masks,
                f"focused profile lacks controlled pair {control}->{enabled}")
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
    for control, enabled in CONTROLLED_PAIRS:
        for prefetch in PREFETCH_PROFILES:
            pair_records = []
            for run in range(1, rounds + 1):
                control_record = by_key.get((control, prefetch, run))
                enabled_record = by_key.get((enabled, prefetch, run))
                require(control_record is not None and enabled_record is not None,
                        f"missing controlled pair {control}->{enabled},"
                        f" prefetch={prefetch},run={run}")
                work_delta = (enabled_record["work_time_p50_ns"]
                              - control_record["work_time_p50_ns"])
                pair_records.append(work_delta)
            directions = {direction(delta) for delta in pair_records}
            variance_status = ("INCONCLUSIVE_VARIANCE" if len(directions) > 1
                               else "CONSISTENT_DIRECTION")
            for run in range(1, rounds + 1):
                control_record = by_key[(control, prefetch, run)]
                enabled_record = by_key[(enabled, prefetch, run)]
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
              require_structural_diagnostics=False):
    records = []
    for _, order, run, mask, prefetch, accounting in plan:
        path = expected_run_dir(output, mask, prefetch, accounting, run) / "summary.json"
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
            "prefetch": prefetch,
            "accounting": accounting,
            "mask": mask,
            "status": summary["status"],
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
            **diagnostics,
        })
    require(len(records) == expected_processes,
            f"aggregation did not find {expected_processes} summaries")
    rows = []
    for prefetch in prefetch_profiles:
        for accounting in accounting_profiles:
            baseline = [record for record in records
                        if record["prefetch"] == prefetch
                        and record["accounting"] == accounting
                        and record["mask"] == 0]
            if 0 in masks:
                require(len(baseline) == rounds,
                        f"missing mask zero baseline for {prefetch}/{accounting}")
                baseline_p50 = sum(record["frame_p50_ns"] for record in baseline) // len(baseline)
                baseline_p95 = sum(record["frame_p95_ns"] for record in baseline) // len(baseline)
            for record in records:
                if record["prefetch"] != prefetch or record["accounting"] != accounting:
                    continue
                row = dict(record)
                row["baseline_scope"] = "same-machine-same-prefetch-mask0"
                row["baseline_mask0_p50_ns"] = baseline_p50 if 0 in masks else None
                row["delta_p50_ns"] = (record["frame_p50_ns"] - baseline_p50
                                        if 0 in masks else None)
                row["baseline_mask0_p95_ns"] = baseline_p95 if 0 in masks else None
                row["delta_p95_ns"] = (record["frame_p95_ns"] - baseline_p95
                                        if 0 in masks else None)
                rows.append(row)
    fields = [
        "order", "run", "prefetch", "accounting", "mask", "status", "frame_count",
        "frame_p50_ns",
        "frame_p90_ns", "frame_p95_ns", "frame_p99_ns", "frame_max_ns",
        "work_time_p50_ns", "work_time_p95_ns", "work_time_p99_ns", "work_time_max_ns",
        "paint_time_p50_ns", "paint_time_p95_ns", "paint_time_p99_ns",
        "paint_time_max_ns",
        "frames_over_16_67_count", "frames_over_33_3_count", "frames_over_50_count",
        "frames_over_100_count", "largest_stall_ns", "largest_consecutive_over_33_3",
        "prefetch_elapsed_ns", "memory_peak_resident_bytes",
    ] + list(DIAGNOSTIC_SUMMARY_FIELDS) + [
        "baseline_scope",
        "baseline_mask0_p50_ns", "delta_p50_ns", "baseline_mask0_p95_ns", "delta_p95_ns",
    ]
    path = output / "summary.csv"
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(sorted(rows, key=lambda row: (row["run"], row["prefetch"], row["mask"])))
    if 0 in masks:
        for prefetch in prefetch_profiles:
            for accounting in accounting_profiles:
                require(sum(1 for row in rows if row["mask"] == 0
                            and row["prefetch"] == prefetch
                            and row["accounting"] == accounting) == rounds,
                        f"aggregated {prefetch}/{accounting} baselines are incomplete")
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
    require(pairs, "reuse profile lacks a controlled pair")
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
    for control, enabled in pairs:
        for prefetch in prefetch_profiles:
            for pass_name in pass_names:
                deltas = []
                for run in range(1, rounds + 1):
                    control_row = by_key.get((control, prefetch, run, pass_name))
                    enabled_row = by_key.get((enabled, prefetch, run, pass_name))
                    require(control_row is not None and enabled_row is not None,
                            f"missing reuse pair {control}->{enabled},"
                            f" prefetch={prefetch},run={run},pass={pass_name}")
                    deltas.append(enabled_row["work_time_p50_ns"]
                                   - control_row["work_time_p50_ns"])
                directions = {direction(delta) for delta in deltas}
                variance_status = ("INCONCLUSIVE_VARIANCE" if len(directions) > 1
                                   else "CONSISTENT_DIRECTION")
                for run in range(1, rounds + 1):
                    control_row = by_key[(control, prefetch, run, pass_name)]
                    enabled_row = by_key[(enabled, prefetch, run, pass_name)]
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


def run_scroll_profile(bundle, manifest, output, corpus_digest, profile_name):
    profile = profile_config(profile_name)
    if profile.get("rendering_reuse"):
        return run_scroll_reuse_matrix(bundle, manifest, output, corpus_digest, profile)
    plan = run_matrix(
        bundle, manifest, output, corpus_digest, profile["masks"], profile["prefetch"],
        profile["accounting"], profile["rounds"], profile["expected_processes"],
        passes=profile.get("passes", 1),
    )
    return aggregate(
        output, plan, profile["masks"], profile["prefetch"], profile["accounting"],
        profile["rounds"], profile["expected_processes"], profile_name,
    )


def write_default_execution_summary(output, manifest):
    summary = {
        "status": "PASS",
        "sourceCommit": manifest["sourceCommit"],
        "sdkSourceAttestation": manifest.get("sdkSourceAttestation"),
        "runtimeSha256": manifest["runtimeSha256"],
        "tcvmSha256": manifest.get("tcvmSha256"),
        "selfTestProcessCount": 1,
        "profileProcessCounts": {
            name: profile_config(name)["expected_processes"]
            for name in (
                "reduced-image-optimizations", "scroll-raster-correctness",
                "scroll-raster-performance", "release-default-scroll",
            )
        },
        "profileWorkloadImages": {
            name: profile_config(name)["workload_images"]
            for name in (
                "reduced-image-optimizations", "scroll-raster-correctness",
                "scroll-raster-performance", "release-default-scroll",
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
    print(f"default execution passed,processes={summary['expectedProcessCount']},summary={path}")


def run_phase(bundle, phase, profile_name):
    if phase in ("decode-self-test", "decode-smokes", "decode-full"):
        decode_phase = phase.removeprefix("decode-")
        run_decode_phase(bundle, decode_phase)
        return
    manifest = load_manifest(bundle)
    output = bundle / "results"
    if phase in ("self-test", "full"):
        _, _, corpus_digest = self_test(bundle, manifest, output)
    else:
        _, images, corpus_digest, _ = validate_bundle(bundle, manifest)
        require((output / "self-test.json").is_file(),
                "self-test must pass before this phase")
        require(len(images) == EXPECTED_JPEGS, "corpus changed after self-test")
    if phase == "self-test":
        return
    if phase == "full":
        for name in (
            "reduced-image-optimizations", "scroll-raster-correctness",
            "scroll-raster-performance", "release-default-scroll",
        ):
            run_scroll_profile(bundle, manifest, output, corpus_digest, name)
        write_default_execution_summary(output, manifest)
        if manifest["includeDecodeAssets"]:
            run_decode_phase(bundle, "full")
        write_zip(bundle, output)
        return
    if phase == "smokes":
        profile_name = "reduced-image-optimizations"
    elif phase == "matrix":
        require(profile_name != "full", "matrix phase requires a named scroll profile")
    elif phase != profile_name:
        require(phase in PROFILES, f"unsupported scroll phase: {phase}")
        profile_name = phase
    run_scroll_profile(bundle, manifest, output, corpus_digest, profile_name)
    write_zip(bundle, output)


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
            "release-default-scroll", "decode-self-test", "decode-smokes", "decode-full",
        ), default="full",
        help="run one fail-fast phase; full runs the 44-process non-decode suite",
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
