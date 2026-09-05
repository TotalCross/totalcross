#!/usr/bin/env bash
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

set -eu

BASE_SHA=9545c18207fab74d81340b24825c5a82ddbda7fd
BASE_WORKTREE=${1:?base worktree path is required}
SOURCE_ROOT=${2:?source repository path is required}
SOURCE_REVISION=${3:?source harness revision is required}
FIXTURE_ROOT=${4:?fixture directory is required}
LAUNCHER=${5:?macOS launcher path is required}

SOURCE_COMMIT=$(git -C "$SOURCE_ROOT" rev-parse --verify "${SOURCE_REVISION}^{commit}")
BASE_COMMIT=$(git -C "$BASE_WORKTREE" rev-parse --verify HEAD)
if [ "$BASE_COMMIT" != "$BASE_SHA" ]; then
  echo "base worktree is $BASE_COMMIT; expected $BASE_SHA" >&2
  exit 1
fi

HARNESS_FILES="TotalCrossSDK/build.gradle
scripts/run-image-optimization-benchmark.py
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterBenchmarkSupport.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterDecodeBenchmarkApp.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterOpacityBenchmarkApp.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterOpaqueDrawBenchmarkApp.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterReadbackBenchmarkApp.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterCombinedBenchmarkApp.java"

while IFS= read -r relative_path; do
  [ -n "$relative_path" ] || continue
  git -C "$SOURCE_ROOT" archive "$SOURCE_COMMIT" -- "$relative_path" \
    | tar -x -C "$BASE_WORKTREE"
done <<EOF
$HARNESS_FILES
EOF

mkdir -p "$BASE_WORKTREE/TotalCrossSDK/src/main/resources/images"
cp "$FIXTURE_ROOT/lenna.png" "$BASE_WORKTREE/TotalCrossSDK/src/main/resources/images/lenna.png"
cp "$FIXTURE_ROOT/lenna_full.png" \
  "$BASE_WORKTREE/TotalCrossSDK/src/main/resources/images/lenna_full.png"
mkdir -p "$BASE_WORKTREE/TotalCrossSDK/etc/launchers/macos"
cp "$LAUNCHER" "$BASE_WORKTREE/TotalCrossSDK/etc/launchers/macos/Launcher"

BASE_WORKTREE="$BASE_WORKTREE" HARNESS_FILES="$HARNESS_FILES" SOURCE_COMMIT="$SOURCE_COMMIT" python3 - <<'PY'
import os
from pathlib import Path

root = Path(os.environ["BASE_WORKTREE"])
files = [Path(value) for value in os.environ["HARNESS_FILES"].splitlines() if value]
support = root / files[2]
support_text = support.read_text(encoding="utf-8")
shim_marker = "  static void require(boolean condition, String message) {"
shims = """  static long zeroCopyDecodeCountForTest() { return 0; }
  static long copiedDecodeCountForTest() { return 0; }
  static long decodeCopiedBytesForTest() { return 0; }
  static long decodeFinalBufferBytesForTest() { return 0; }
  static long opacityKnownFromSourceForTest() { return 0; }
  static long opacityDeterminedDuringDecodeForTest() { return 0; }
  static long opacityFallbackScansForTest() { return 0; }
  static long opacityFallbackPixelsForTest() { return 0; }
  static long writePixelsAttemptsForTest() { return 0; }
  static long writePixelsHitsForTest() { return 0; }
  static long writePixelsFallbacksForTest() { return 0; }
  static long writePixelsCopiedBytesForTest() { return 0; }
  static long rowReadbackCountForTest() { return 0; }
  static long fullReadbackCountForTest() { return 0; }
  static long rowScratchPeakBytesForTest() { return 0; }
  static long fullScratchBytesForTest() { return 0; }
  static long directColorMaterializationCountForTest() { return 0; }

"""
if support_text.count(shim_marker) != 1:
    raise SystemExit("benchmark support insertion marker is not unique")
support.write_text(support_text.replace(shim_marker, shims + shim_marker), encoding="utf-8")

image_methods = (
    "zeroCopyDecodeCountForTest", "copiedDecodeCountForTest",
    "decodeCopiedBytesForTest", "decodeFinalBufferBytesForTest",
    "opacityKnownFromSourceForTest", "opacityDeterminedDuringDecodeForTest",
    "opacityFallbackScansForTest", "opacityFallbackPixelsForTest",
    "rowReadbackCountForTest", "fullReadbackCountForTest",
    "rowScratchPeakBytesForTest", "fullScratchBytesForTest",
    "directColorMaterializationCountForTest",
)
backing_methods = (
    "writePixelsAttemptsForTest", "writePixelsHitsForTest",
    "writePixelsFallbacksForTest", "writePixelsCopiedBytesForTest",
)
for relative_path in files[3:]:
    path = root / relative_path
    text = path.read_text(encoding="utf-8")
    for method in image_methods:
        text = text.replace("Image." + method + "()",
                            "ImageRasterBenchmarkSupport." + method + "()")
    for method in backing_methods:
        text = text.replace("NativeImageBacking." + method + "()",
                            "ImageRasterBenchmarkSupport." + method + "()")
    if relative_path.name == "ImageRasterOpacityBenchmarkApp.java":
        old = """        int opacity = image.backing instanceof NativeImageBacking
            ? ((NativeImageBacking) image.backing).opacityForTest()
            : NativeImageBacking.OPACITY_UNKNOWN;"""
        if text.count(old) != 1:
            raise SystemExit("opacity compatibility expression is not unique")
        text = text.replace(old, "        int opacity = 0;")
    path.write_text(text, encoding="utf-8")

import hashlib
digest = hashlib.sha256()
for relative_path in sorted(files):
    digest.update(str(relative_path).encode("utf-8"))
    digest.update(b"\0")
    digest.update((root / relative_path).read_bytes())
    digest.update(b"\0")
print("source_revision=" + os.environ.get("SOURCE_COMMIT", "unknown"))
print("adapter_digest=" + digest.hexdigest())
PY
