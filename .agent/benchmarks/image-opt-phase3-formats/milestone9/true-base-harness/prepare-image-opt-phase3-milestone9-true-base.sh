#!/usr/bin/env bash
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

set -eu

BASE_SHA=6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee
BASE_WORKTREE=${1:?base worktree path is required}
SOURCE_ROOT=${2:?source repository path is required}
SOURCE_REVISION=${3:?source harness revision is required}
FIXTURE_ROOT=${4:?fixture directory is required}
LAUNCHER=${5:?macOS launcher path is required}
NATIVE_RUNTIME=${6:?Release macOS native runtime path is required}

SOURCE_COMMIT=$(git -C "$SOURCE_ROOT" rev-parse --verify "${SOURCE_REVISION}^{commit}")
BASE_COMMIT=$(git -C "$BASE_WORKTREE" rev-parse --verify HEAD)
if [ "$BASE_COMMIT" != "$BASE_SHA" ]; then
  echo "base worktree is $BASE_COMMIT; expected $BASE_SHA" >&2
  exit 1
fi

HARNESS_FILES="TotalCrossSDK/build.gradle
scripts/run-image-optimization-benchmark.py
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterBenchmarkSupport.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsBenchmarkSupport.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsNativeHooks.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsBenchmarkApp.java
TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsSmokeApp.java"

while IFS= read -r relative_path; do
  [ -n "$relative_path" ] || continue
  git -C "$SOURCE_ROOT" archive "$SOURCE_COMMIT" -- "$relative_path" \
    | tar -x -C "$BASE_WORKTREE"
done <<EOF
$HARNESS_FILES
EOF

mkdir -p "$BASE_WORKTREE/TotalCrossSDK/src/smokeTest/resources/image-opt-phase3"
cp "$FIXTURE_ROOT"/* "$BASE_WORKTREE/TotalCrossSDK/src/smokeTest/resources/image-opt-phase3/"
mkdir -p "$BASE_WORKTREE/TotalCrossSDK/etc/launchers/macos"
cp "$LAUNCHER" "$BASE_WORKTREE/TotalCrossSDK/etc/launchers/macos/Launcher"
mkdir -p "$BASE_WORKTREE/TotalCrossSDK/dist/vm/macos"
cp "$NATIVE_RUNTIME" "$BASE_WORKTREE/TotalCrossSDK/dist/vm/macos/libtcvm.dylib"

BASE_WORKTREE="$BASE_WORKTREE" HARNESS_FILES="$HARNESS_FILES" SOURCE_COMMIT="$SOURCE_COMMIT" python3 - <<'PY'
import hashlib
import os
from pathlib import Path

root = Path(os.environ["BASE_WORKTREE"])
files = [Path(value) for value in os.environ["HARNESS_FILES"].splitlines() if value]

hook = root / "TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsNativeHooks.java"
hook.write_text("""// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

/** Phase-3-only compatibility shims for the exact Phase-2 runtime. */
final class ImageCompactFormatsNativeHooks {
  private ImageCompactFormatsNativeHooks() {
  }

  static Image materialize(Image image) {
    image.getPixels();
    return image;
  }

  static String format(Image image) {
    return image.hasNativeBackingForSmoke()
        ? ImageCompactFormatsBenchmarkSupport.RGBA8888 : "JAVA_RASTER";
  }

  static boolean formatProbeAvailable() {
    return false;
  }

  static boolean metricProbeAvailable(String method) {
    return false;
  }

  static long metric(String method) {
    return 0;
  }

  static void invokeStaticRequired(String className, String method) {
    throw new IllegalArgumentException("Unsupported Phase-3 test hook "
        + className + "." + method);
  }
}
""", encoding="utf-8")
files[4] = hook.relative_to(root)

smoke = root / "TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsSmokeApp.java"
smoke_text = smoke.read_text(encoding="utf-8")
smoke_text = smoke_text.replace("Image.decodeFinalBufferBytesForTest()", "0")
smoke.write_text(smoke_text, encoding="utf-8")

files.extend(sorted(Path("TotalCrossSDK/src/smokeTest/resources/image-opt-phase3").glob("*")))
digest = hashlib.sha256()
for relative_path in sorted(set(files)):
    digest.update(str(relative_path).encode("utf-8"))
    digest.update(b"\0")
    digest.update((root / relative_path).read_bytes())
    digest.update(b"\0")
print("source_revision=" + os.environ.get("SOURCE_COMMIT", "unknown"))
print("base_revision=6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee")
print("adapter_digest=" + digest.hexdigest())
PY
