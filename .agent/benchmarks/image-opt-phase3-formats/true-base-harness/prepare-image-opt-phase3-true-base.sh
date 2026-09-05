#!/usr/bin/env bash
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

set -eu

BASE_SHA=86bfeafe388ce866236c3ae58eecb144664895e2
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

BASE_WORKTREE="$BASE_WORKTREE" HARNESS_FILES="$HARNESS_FILES" SOURCE_COMMIT="$SOURCE_COMMIT" python3 - <<'PY'
import hashlib
import os
from pathlib import Path

root = Path(os.environ["BASE_WORKTREE"])
files = [Path(value) for value in os.environ["HARNESS_FILES"].splitlines() if value]
files.extend(sorted(Path("TotalCrossSDK/src/smokeTest/resources/image-opt-phase3").glob("*")))
digest = hashlib.sha256()
for relative_path in sorted(files):
    digest.update(str(relative_path).encode("utf-8"))
    digest.update(b"\0")
    digest.update((root / relative_path).read_bytes())
    digest.update(b"\0")
print("source_revision=" + os.environ["SOURCE_COMMIT"])
print("base_revision=" + os.environ.get("BASE_SHA", "86bfeafe388ce866236c3ae58eecb144664895e2"))
print("adapter_digest=" + digest.hexdigest())
PY
