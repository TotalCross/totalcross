#!/bin/bash
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

set -euo pipefail

usage() {
   echo "Usage: $0 --sdk-zip <TotalCross-version.zip> --corpus <dir> --output <dir> [--target <target>|--all]" >&2
   echo "Targets: windows-x64 macos-arm64 linux-x64 linux-arm64 linux-armv7" >&2
}

repo_dir=$(cd "$(dirname "$0")/.." && pwd)
sdk_zip=""
corpus_dir="${TC_IMAGE_CORPUS:-}"
output_dir=""
targets=()

while [ "$#" -gt 0 ]; do
   case "$1" in
      --sdk-zip)
         [ "$#" -ge 2 ] || { usage; exit 2; }
         sdk_zip=$2
         shift 2
         ;;
      --corpus)
         [ "$#" -ge 2 ] || { usage; exit 2; }
         corpus_dir=$2
         shift 2
         ;;
      --output)
         [ "$#" -ge 2 ] || { usage; exit 2; }
         output_dir=$2
         shift 2
         ;;
      --target)
         [ "$#" -ge 2 ] || { usage; exit 2; }
         targets+=("$2")
         shift 2
         ;;
      --all)
         targets=(windows-x64 macos-arm64 linux-x64 linux-arm64 linux-armv7)
         shift
         ;;
      -h|--help)
         usage
         exit 0
         ;;
      *)
         usage
         exit 2
         ;;
   esac
done

[ -n "$sdk_zip" ] || { usage; echo "Missing --sdk-zip" >&2; exit 2; }
[ -f "$sdk_zip" ] || { echo "SDK ZIP not found: $sdk_zip" >&2; exit 2; }
[ -n "$corpus_dir" ] || { usage; echo "Missing --corpus or TC_IMAGE_CORPUS" >&2; exit 2; }
[ -d "$corpus_dir" ] || { echo "Corpus directory not found: $corpus_dir" >&2; exit 2; }
[ -n "$output_dir" ] || { usage; echo "Missing --output" >&2; exit 2; }
[ "${#targets[@]}" -gt 0 ] || targets=(windows-x64 macos-arm64 linux-x64 linux-arm64 linux-armv7)

case "$(uname -s)" in
   Darwin|Linux) : ;;
   *) echo "Unsupported packaging host: $(uname -s)" >&2; exit 2 ;;
esac

corpus_count=$(find "$corpus_dir" -type f \( -iname '*.jpg' -o -iname '*.jpeg' \) -print | wc -l | tr -d ' ')
[ "$corpus_count" -eq 663 ] || {
   echo "Expected exactly 663 JPEG-named corpus files, found $corpus_count" >&2
   exit 2
}

benchmark_gradle="$repo_dir/TotalCrossSDK/gradlew-agent"
benchmark_jar="$repo_dir/TotalCrossSDK/build/image-scroll-real-workload-benchmark/classes/ImageScrollRealWorkloadBenchmarkApp.jar"
if [ ! -f "$benchmark_jar" ]; then
   "$benchmark_gradle" -p "$repo_dir/TotalCrossSDK" jarImageScrollRealWorkloadBenchmark --no-daemon --console=plain
fi
[ -f "$benchmark_jar" ] || { echo "Benchmark JAR not found: $benchmark_jar" >&2; exit 1; }

work_dir=$(mktemp -d "${TMPDIR:-/tmp}/totalcross-image-scroll-package.XXXXXX")
cleanup() {
   rm -rf "$work_dir"
}
trap cleanup EXIT

mkdir -p "$output_dir"
(cd "$work_dir" && jar xf "$sdk_zip")
sdk_root="$work_dir/TotalCross"
sdk_jar="$sdk_root/dist/totalcross-sdk.jar"
[ -f "$sdk_jar" ] || {
   sdk_jar=$(find "$sdk_root/dist" -maxdepth 1 -type f -name 'totalcross-sdk-*.jar' -print -quit)
}
[ -f "$sdk_jar" ] || { echo "Packaged SDK JAR not found in $sdk_root" >&2; exit 1; }

deploy_classpath="$sdk_jar"
for dependency in "$sdk_root"/dist/libs/*.jar; do
   [ -f "$dependency" ] || continue
   deploy_classpath="$deploy_classpath:$dependency"
done

target_supported() {
   case "$1" in
      windows-x64|macos-arm64|linux-x64|linux-arm64|linux-armv7) return 0 ;;
      *) return 1 ;;
   esac
}

deploy_target() {
   local target=$1
   local deploy_platform=$2
   local install_name=$3
   local deploy_dir="$work_dir/deploy-$target"
   local bundle_dir="$output_dir/image-scroll-benchmark-$target"
   mkdir -p "$deploy_dir"
   cp "$benchmark_jar" "$deploy_dir/ImageScrollRealWorkloadBenchmarkApp.jar"
   (
      cd "$deploy_dir"
      TOTALCROSS3_HOME="$sdk_root" java -cp "$deploy_classpath" tc.Deploy \
         ImageScrollRealWorkloadBenchmarkApp.jar "$deploy_platform" > "$work_dir/deploy-$target.log" 2>&1
   ) || {
      tail -80 "$work_dir/deploy-$target.log" >&2
      echo "tc.Deploy failed for $target; full log: $work_dir/deploy-$target.log" >&2
      exit 1
   }
   local install_dir="$deploy_dir/install/$install_name"
   [ -d "$install_dir" ] || { echo "Deployment output not found: $install_dir" >&2; exit 1; }
   rm -rf "$bundle_dir"
   mkdir -p "$bundle_dir/corpus"
   cp -R "$install_dir/." "$bundle_dir/"
   cp -R "$corpus_dir/." "$bundle_dir/corpus/"
   cat > "$bundle_dir/manifest.json" <<EOF
{
  "schemaVersion": 1,
  "benchmark": "image-scroll",
  "target": "$target",
  "datasetFileCount": 663,
  "datasetHash": "computed-by-run-all",
  "columns": 3,
  "masks": [0,1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,16384,32768,32799,40991,49183,57375],
  "expectedProcessCount": 210
}
EOF
   case "$target" in
      windows-x64)
         cat > "$bundle_dir/run-all.bat" <<'EOF'
@echo off
setlocal
cd /d "%~dp0"
ImageScrollRealWorkloadBenchmarkApp.exe --mode=run-all --corpus=corpus --output=results
exit /b %ERRORLEVEL%
EOF
         ;;
      macos-arm64)
         cat > "$bundle_dir/run-all.command" <<'EOF'
#!/bin/sh
set -eu
cd "$(dirname "$0")"
exec ./ImageScrollRealWorkloadBenchmarkApp --mode=run-all --corpus=corpus --output=results
EOF
         chmod +x "$bundle_dir/run-all.command"
         ;;
      linux-*)
         cat > "$bundle_dir/run-all.sh" <<'EOF'
#!/bin/sh
set -eu
cd "$(dirname "$0")"
exec ./ImageScrollRealWorkloadBenchmarkApp --mode=run-all --corpus=corpus --output=results
EOF
         chmod +x "$bundle_dir/run-all.sh"
         ;;
   esac
   local archive
   case "$target" in
      windows-x64|macos-arm64)
         archive="$output_dir/image-scroll-benchmark-$target.zip"
         (cd "$output_dir" && jar -cf "$(basename "$archive")" "$(basename "$bundle_dir")")
         ;;
      linux-*)
         archive="$output_dir/image-scroll-benchmark-$target.tar.gz"
         (cd "$output_dir" && tar -czf "$(basename "$archive")" "$(basename "$bundle_dir")")
         ;;
   esac
   echo "created $archive"
}

for target in "${targets[@]}"; do
   target_supported "$target" || { echo "Unsupported target: $target" >&2; exit 2; }
   case "$target" in
      windows-x64) deploy_target "$target" -win32 win32 ;;
      macos-arm64) deploy_target "$target" -macos macos ;;
      linux-x64) deploy_target "$target" -linux linux ;;
      linux-arm64) deploy_target "$target" -linux_arm linux_arm64 ;;
      linux-armv7) deploy_target "$target" -linux_arm linux_arm ;;
   esac
done
