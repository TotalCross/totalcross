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

sha256_file() {
   if command -v sha256sum >/dev/null 2>&1; then
      sha256sum "$1" | awk '{print $1}'
   else
      shasum -a 256 "$1" | awk '{print $1}'
   fi
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

sdk_zip=$(cd "$(dirname "$sdk_zip")" && pwd)/$(basename "$sdk_zip")
corpus_dir=$(cd "$corpus_dir" && pwd)
mkdir -p "$output_dir"
output_dir=$(cd "$output_dir" && pwd)

case "$(uname -s)" in
   Darwin|Linux) : ;;
   *) echo "Unsupported packaging host: $(uname -s)" >&2; exit 2 ;;
esac

corpus_count=$(find "$corpus_dir" -type f \( -iname '*.jpg' -o -iname '*.jpeg' \) -print | wc -l | tr -d ' ')
[ "$corpus_count" -eq 663 ] || {
   echo "Expected exactly 663 JPEG-named corpus files, found $corpus_count" >&2
   exit 2
}

work_dir=$(mktemp -d "${TMPDIR:-/tmp}/totalcross-image-scroll-package.XXXXXX")
cleanup() {
   rm -rf "$work_dir"
}
trap cleanup EXIT

# Extraction must precede compilation so the source is always compiled against
# the SDK shipped by the caller, never against a repository-generated SDK.
(cd "$work_dir" && jar xf "$sdk_zip")
sdk_root="$work_dir/TotalCross"
if [ ! -d "$sdk_root" ]; then
   nested_sdk_zip=$(find "$work_dir" -maxdepth 1 -type f -name '*.zip' -print -quit)
   [ -n "$nested_sdk_zip" ] || {
      echo "SDK ZIP did not contain a TotalCross directory" >&2
      exit 1
   }
   nested_root="$work_dir/nested-sdk"
   mkdir -p "$nested_root"
   (cd "$nested_root" && jar xf "$nested_sdk_zip")
   sdk_root="$nested_root/TotalCross"
fi
sdk_jar="$sdk_root/dist/totalcross-sdk.jar"
[ -f "$sdk_jar" ] || {
   echo "Official ZIP must contain TotalCross/dist/totalcross-sdk.jar" >&2
   exit 1
}
sdk_compile_sha256=$(sha256_file "$sdk_jar")

benchmark_source="$repo_dir/TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.java"
support_source="$repo_dir/TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterBenchmarkSupport.java"
compiled_dir="$work_dir/benchmark-classes"
benchmark_build_log="$work_dir/benchmark-javac.log"
mkdir -p "$compiled_dir"
if ! javac -source 17 -target 17 -encoding UTF-8 -cp "$sdk_jar" -d "$compiled_dir" \
      "$benchmark_source" "$support_source" > "$benchmark_build_log" 2>&1; then
   tail -100 "$benchmark_build_log" >&2
   echo "Benchmark JAR compilation failed; full log: $benchmark_build_log" >&2
   exit 1
fi
benchmark_jar="$work_dir/ImageScrollRealWorkloadBenchmarkApp.jar"
(
   cd "$compiled_dir"
   jar -cf "$benchmark_jar" \
      totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp*.class \
      totalcross/ui/image/ImageRasterBenchmarkSupport*.class
)
jar tf "$benchmark_jar" | grep -Fqx \
   'totalcross/ui/image/ImageScrollRealWorkloadBenchmarkApp.class' || {
   echo "Benchmark JAR does not contain ImageScrollRealWorkloadBenchmarkApp" >&2
   exit 1
}

chime_resource_dir="$work_dir/chime-resource"
mkdir -p "$chime_resource_dir"
chime_resource_jar="$sdk_jar"
if ! jar tf "$chime_resource_jar" | grep -Fqx 'totalcross/res/mp3/chime.mp3'; then
   chime_resource_jar=$(find "$sdk_root/dist" -maxdepth 1 -type f \
      -name 'totalcross-sdk-*-sources.jar' -print -quit)
fi
[ -f "$chime_resource_jar" ] || {
   echo "Official SDK resource JAR not found: totalcross/res/mp3/chime.mp3" >&2
   exit 1
}
(cd "$chime_resource_dir" && jar xf "$chime_resource_jar" totalcross/res/mp3/chime.mp3)
chime_resource="$chime_resource_dir/totalcross/res/mp3/chime.mp3"
[ -f "$chime_resource" ] || {
   echo "Official SDK resource not found: totalcross/res/mp3/chime.mp3" >&2
   exit 1
}
chime_sha256=$(sha256_file "$chime_resource")

dataset_hash=$(python3 - "$corpus_dir" <<'PY'
import pathlib
import sys

corpus = pathlib.Path(sys.argv[1]).resolve()
value = 0xCBF29CE484222325
prime = 0x100000001B3
paths = sorted(
    path for path in corpus.rglob("*")
    if path.is_file() and path.suffix.lower() in (".jpg", ".jpeg")
)
for path in paths:
    relative = path.relative_to(corpus).as_posix().encode("utf-8")
    for byte in relative + b"\0":
        value = ((value ^ byte) * prime) & 0xFFFFFFFFFFFFFFFF
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(16384), b""):
            for byte in chunk:
                value = ((value ^ byte) * prime) & 0xFFFFFFFFFFFFFFFF
print(f"{value:016x}")
PY
)

deploy_classpath="$sdk_jar:$sdk_root/dist/libs/*"
sdk_deploy_sha256=$(sha256_file "$sdk_jar")
[ "$sdk_compile_sha256" = "$sdk_deploy_sha256" ] || {
   echo "SDK JAR changed between compile and deploy" >&2
   exit 1
}
runner_source="$repo_dir/scripts/run-image-scroll-distributed-benchmark.py"
[ -f "$runner_source" ] || { echo "Official distributed runner not found: $runner_source" >&2; exit 1; }

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
   local executable_name=$4
   local runtime_name=$5
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
   cp -R "$install_dir"/. "$bundle_dir/"
   local image_path relative_path destination
   while IFS= read -r -d '' image_path; do
      relative_path="${image_path#"$corpus_dir/"}"
      destination="$bundle_dir/corpus/$relative_path"
      mkdir -p "$(dirname "$destination")"
      cp "$image_path" "$destination"
   done < <(find "$corpus_dir" -type f \( -iname '*.jpg' -o -iname '*.jpeg' \) -print0)

   cp "$chime_resource" "$bundle_dir/chime.mp3"
   cmp -s "$chime_resource" "$bundle_dir/chime.mp3" || {
      echo "Bundle chime resource differs from the official SDK resource" >&2
      exit 1
   }

   local bundle_file_count bundle_image_count
   bundle_file_count=$(find "$bundle_dir/corpus" -type f -print | wc -l | tr -d ' ')
   bundle_image_count=$(find "$bundle_dir/corpus" -type f \( -iname '*.jpg' -o -iname '*.jpeg' \) -print | wc -l | tr -d ' ')
   [ "$bundle_file_count" -eq 663 ] && [ "$bundle_image_count" -eq 663 ] || {
      echo "Bundle corpus must contain exactly 663 JPEG files and no extras; found $bundle_file_count files and $bundle_image_count JPEGs" >&2
      exit 1
   }
   [ -f "$bundle_dir/$executable_name" ] || {
      echo "Deployed executable not found in bundle: $executable_name" >&2
      exit 1
   }
   [ -f "$bundle_dir/$runtime_name" ] || {
      echo "Deployed native runtime not found in bundle: $runtime_name" >&2
      exit 1
   }

   cp "$runner_source" "$bundle_dir/run-benchmark.py"
   chmod +x "$bundle_dir/run-benchmark.py"
   cat > "$bundle_dir/manifest.json" <<EOF
{
  "schemaVersion": 1,
  "benchmark": "image-scroll",
  "target": "$target",
  "screenArgument": "/scr -1,-1,540,960",
  "executable": "$executable_name",
  "runtime": "$runtime_name",
  "runner": "run-benchmark.py",
  "chime": "chime.mp3",
  "datasetFileCount": 663,
  "datasetHash": "$dataset_hash",
  "columns": 3,
  "masks": [0,1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,16384,32768,32799,40991,49183,57375],
  "prefetchProfiles": ["off","on"],
  "rounds": 3,
  "seed": 73001,
  "expectedProcessCount": 126,
  "sdkJar": "dist/totalcross-sdk.jar",
  "sdkJarSha256": "$sdk_compile_sha256",
  "sdkJarSha256Compile": "$sdk_compile_sha256",
  "sdkJarSha256Deploy": "$sdk_deploy_sha256",
  "chimeSha256": "$chime_sha256"
}
EOF
   local archive
   case "$target" in
      windows-x64|macos-arm64)
         archive="$output_dir/image-scroll-benchmark-$target.zip"
         (cd "$output_dir" && zip -q -r "$(basename "$archive")" "$(basename "$bundle_dir")")
         ;;
      linux-*)
         archive="$output_dir/image-scroll-benchmark-$target.tar.gz"
         (cd "$output_dir" && tar -czf "$(basename "$archive")" "$(basename "$bundle_dir")")
         ;;
   esac
   echo "created $archive"
   echo "sdk_jar_sha256_compile=$sdk_compile_sha256"
   echo "sdk_jar_sha256_deploy=$sdk_deploy_sha256"
}

for target in "${targets[@]}"; do
   target_supported "$target" || { echo "Unsupported target: $target" >&2; exit 2; }
   case "$target" in
      windows-x64) deploy_target "$target" -win32 win32 ImageScrollRealWorkloadBenchmarkApp.exe tcvm.dll ;;
      macos-arm64) deploy_target "$target" -macos macos ImageScrollRealWorkloadBenchmarkApp libtcvm.dylib ;;
      linux-x64) deploy_target "$target" -linux linux ImageScrollRealWorkloadBenchmarkApp libtcvm.so ;;
      linux-arm64) deploy_target "$target" -linux_arm linux_arm64 ImageScrollRealWorkloadBenchmarkApp libtcvm.so ;;
      linux-armv7) deploy_target "$target" -linux_arm linux ImageScrollRealWorkloadBenchmarkApp libtcvm.so ;;
   esac
done
