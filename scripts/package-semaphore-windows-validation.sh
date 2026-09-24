#!/usr/bin/env bash
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <workflow-sdk-artifact.zip> <workflow-windows-artifact.zip> <output-folder>" >&2
  exit 2
fi

repo_root=$(cd "$(dirname "$0")/.." && pwd)
sdk_artifact_arg=$1
windows_artifact_arg=$2
output_arg=$3
output_parent_arg=$(dirname "$output_arg")
mkdir -p "$output_parent_arg"
output_parent=$(cd "$output_parent_arg" && pwd)
output_name=$(basename "$output_arg")
output_dir="$output_parent/$output_name"
zip_path="$output_dir.zip"

workflow_run_id=36040721060
runtime_source_sha=0badac435cc2c6af31de4bb0adad9ed58e6cd0c5
windows_artifact_id=10826514166
windows_artifact_sha=749e41bb3fc136dc8021ef037cdbecd6e898be471be5221ce37e4ec3f23fb81e
sdk_artifact_id=10826538938
sdk_artifact_sha=ee867104a02f80241480543ad99bf1f70edae2cdbd843af902be60f93b37cbbe
expected_launcher_sha=28acbad889979d8c4ee656aaa352747eae8092f83edc76fecbcaacf094fddaee
expected_tcvm_sha=b063aa213d028a6674a4d42ae1018a8edda98b6dce9cd51292b37ad653a7cc8b

sha256_file() {
  if command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$1" | awk '{print $1}'
  else
    sha256sum "$1" | awk '{print $1}'
  fi
}

for artifact in "$sdk_artifact_arg" "$windows_artifact_arg"; do
  if [ ! -f "$artifact" ]; then
    echo "Workflow artifact ZIP is missing: $artifact" >&2
    exit 1
  fi
done
sdk_artifact_zip=$(cd "$(dirname "$sdk_artifact_arg")" && pwd)/$(basename "$sdk_artifact_arg")
windows_artifact_zip=$(cd "$(dirname "$windows_artifact_arg")" && pwd)/$(basename "$windows_artifact_arg")
if [ "$(sha256_file "$sdk_artifact_zip")" != "$sdk_artifact_sha" ] ||
    [ "$(sha256_file "$windows_artifact_zip")" != "$windows_artifact_sha" ]; then
  echo 'Workflow artifact ZIP hashes do not match run 36040721060.' >&2
  exit 1
fi

if [ -e "$output_dir" ] || [ -e "$zip_path" ]; then
  echo "Output already exists; choose a new destination: $output_dir" >&2
  exit 2
fi

if ! command -v javac >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1 ||
    ! command -v jar >/dev/null 2>&1 || ! command -v zip >/dev/null 2>&1 ||
    ! command -v unzip >/dev/null 2>&1 || ! command -v tar >/dev/null 2>&1; then
  echo 'Package preparation requires JDK 17, unzip, tar, and zip.' >&2
  exit 1
fi

smoke_dir="$repo_root/TotalCrossSDK/src/smokeTest/java/totalcross/util/concurrent"
correctness_source="$smoke_dir/SemaphoreSmokeApp.java"
stress_source="$smoke_dir/SemaphoreStressSmokeApp.java"
for source in "$correctness_source" "$stress_source"; do
  if [ ! -f "$source" ]; then
    echo "Smoke source is missing: $source" >&2
    exit 1
  fi
done

smoke_source_sha=$(git -C "$repo_root" rev-parse HEAD)
if ! git -C "$repo_root" diff --quiet HEAD -- \
  TotalCrossSDK/src/smokeTest/java/totalcross/util/concurrent/SemaphoreSmokeApp.java \
  TotalCrossSDK/src/smokeTest/java/totalcross/util/concurrent/SemaphoreStressSmokeApp.java \
  scripts/semaphore-windows-validation \
  scripts/package-semaphore-windows-validation.sh; then
  echo 'Commit the semaphore smoke and runner sources before packaging.' >&2
  exit 1
fi

stage=$(mktemp -d "${TMPDIR:-/tmp}/semaphore-windows-package.XXXXXX")
cleanup_stage() {
  local status=$?
  if [ "$status" -eq 0 ]; then
    rm -rf "$stage"
  else
    echo "Preparation files retained for debugging: $stage" >&2
  fi
}
trap cleanup_stage EXIT
package_dir="$stage/$output_name"
mkdir -p "$stage/windows" "$stage/sdk"
unzip -q "$windows_artifact_zip" -d "$stage/windows"
unzip -p "$sdk_artifact_zip" sdk-build.tar.gz | tar -xz -C "$stage/sdk"
sdk_root="$stage/sdk/build/TotalCross"
windows_runtime="$stage/windows"

for required in \
  "$sdk_root/dist/totalcross-sdk.jar" \
  "$sdk_root/dist/vm/tc.base.lang.tcz" \
  "$sdk_root/etc/images/empty_icon.ico" \
  "$windows_runtime/Launcher.exe" \
  "$windows_runtime/tcvm.dll"; do
  if [ ! -f "$required" ]; then
    echo "Required workflow artifact file is missing: $required" >&2
    exit 1
  fi
done

actual_launcher_sha=$(sha256_file "$windows_runtime/Launcher.exe")
actual_tcvm_sha=$(sha256_file "$windows_runtime/tcvm.dll")
if [ "$actual_launcher_sha" != "$expected_launcher_sha" ] ||
    [ "$actual_tcvm_sha" != "$expected_tcvm_sha" ]; then
  echo 'Windows runtime file hashes do not match workflow run 36040721060.' >&2
  exit 1
fi

deploy_sdk="$stage/deploy-sdk"
mkdir -p "$package_dir" \
  "$stage/classes/correctness" "$stage/classes/stress" \
  "$deploy_sdk"

cp -R "$sdk_root/etc" "$deploy_sdk/etc"
cp -R "$sdk_root/dist" "$deploy_sdk/dist"
mkdir -p "$deploy_sdk/etc/launchers/win32" "$deploy_sdk/dist/vm/win32"
cp "$windows_runtime/Launcher.exe" "$deploy_sdk/etc/launchers/win32/Launcher.exe"
cp "$windows_runtime/tcvm.dll" "$deploy_sdk/dist/vm/win32/tcvm.dll"

javac --release 17 -classpath "$sdk_root/dist/totalcross-sdk.jar" \
  -d "$stage/classes/correctness" "$correctness_source"
javac --release 17 -classpath "$sdk_root/dist/totalcross-sdk.jar" \
  -d "$stage/classes/stress" "$stress_source"
jar cf "$stage/SemaphoreSmokeApp.jar" -C "$stage/classes/correctness" .
jar cf "$stage/SemaphoreStressSmokeApp.jar" -C "$stage/classes/stress" .

classpath="$sdk_root/dist/totalcross-sdk.jar:$sdk_root/dist/libs/*"
for app in SemaphoreSmokeApp SemaphoreStressSmokeApp; do
  app_log="$stage/$app-deploy.log"
  if ! (cd "$deploy_sdk" && \
      java -cp "$classpath" tc.Deploy "$stage/$app.jar" -win32) >"$app_log" 2>&1; then
    echo "Windows deployment failed for $app; log: $app_log" >&2
    tail -60 "$app_log" >&2
    exit 1
  fi
  if [ ! -f "$deploy_sdk/win32/$app.exe" ]; then
    echo "Deployment did not produce $app.exe; log: $app_log" >&2
    tail -60 "$app_log" >&2
    exit 1
  fi
done

while IFS= read -r file; do
  name=$(basename "$file")
  destination="$package_dir/$name"
  if [ -e "$destination" ]; then
    if [ "$(sha256_file "$destination")" != "$(sha256_file "$file")" ]; then
      echo "Deployments produced conflicting files named $name." >&2
      exit 1
    fi
  else
    cp "$file" "$destination"
  fi
done < <(find "$deploy_sdk/win32" -maxdepth 1 -type f -print | LC_ALL=C sort)

for file in \
  "$package_dir/SemaphoreSmokeApp.exe" \
  "$package_dir/SemaphoreStressSmokeApp.exe" \
  "$package_dir/tcvm.dll"; do
  if [ ! -f "$file" ]; then
    echo "Final package file is missing: $file" >&2
    exit 1
  fi
done
if [ "$(sha256_file "$package_dir/tcvm.dll")" != "$expected_tcvm_sha" ]; then
  echo 'Packaged tcvm.dll does not match the workflow Windows artifact.' >&2
  exit 1
fi

cp "$repo_root/scripts/semaphore-windows-validation/run-semaphore-windows-tests.cmd" "$package_dir/"
cp "$repo_root/scripts/semaphore-windows-validation/run-semaphore-windows-tests.ps1" "$package_dir/"
cp "$repo_root/scripts/semaphore-windows-validation/README.md" "$package_dir/"

file_records=''
while IFS= read -r file; do
  name=$(basename "$file")
  hash=$(sha256_file "$file")
  if [ -n "$file_records" ]; then file_records="$file_records,"; fi
  file_records="$file_records{\"path\":\"$name\",\"sha256\":\"$hash\"}"
done < <(find "$package_dir" -maxdepth 1 -type f -print | LC_ALL=C sort)

runtime_records=''
while IFS= read -r file; do
  name=$(basename "$file")
  case "$name" in
    *.dll) origin='windows artifact' ;;
    SemaphoreSmokeApp.tcz|SemaphoreStressSmokeApp.tcz) origin='deployed smoke app' ;;
    *.tcz) origin='sdk-build artifact' ;;
    *) continue ;;
  esac
  hash=$(sha256_file "$file")
  if [ -n "$runtime_records" ]; then runtime_records="$runtime_records,"; fi
  runtime_records="$runtime_records{\"file\":\"$name\",\"sha256\":\"$hash\",\"origin\":\"$origin\"}"
done < <(find "$package_dir" -maxdepth 1 -type f -print | LC_ALL=C sort)

cat > "$package_dir/provenance.json" <<EOF
{
  "workflow": {
    "repository": "TotalCross/totalcross",
    "runId": $workflow_run_id,
    "sourceSha": "$runtime_source_sha",
    "status": "success"
  },
  "smokeSourceSha": "$smoke_source_sha",
  "artifacts": {
    "windows": {
      "id": $windows_artifact_id,
      "name": "windows",
      "sha256": "$windows_artifact_sha"
    },
    "sdkBuild": {
      "id": $sdk_artifact_id,
      "name": "sdk-build",
      "sha256": "$sdk_artifact_sha"
    }
  },
  "deploymentTemplate": {
    "file": "Launcher.exe",
    "sha256": "$actual_launcher_sha",
    "artifactId": $windows_artifact_id
  },
  "runtimeFiles": [$runtime_records],
  "files": [$file_records],
  "testExecution": {"status": "pending Windows execution"}
}
EOF

mkdir -p "$output_parent"
mv "$package_dir" "$output_dir"
(cd "$output_parent" && zip -qr "$zip_path" "$output_name")
printf 'status=package-prepared\nfolder=%s\nzip=%s\nworkflowRun=%s\nruntimeSourceSha=%s\nsmokeSourceSha=%s\n' \
  "$output_dir" "$zip_path" "$workflow_run_id" "$runtime_source_sha" "$smoke_source_sha"
