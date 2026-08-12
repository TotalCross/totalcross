#!/usr/bin/env bash
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
depot_dir="${TOTALCROSS_DEPOT_TOOLS_DIR:-${repo_root}/TotalCrossVM/deps/totalcross-depot-tools}"
dry_run=0
prepared_index="${PREPARED_NATIVE_DEPENDENCIES_INDEX:-${RUNNER_TEMP:-/tmp}/prepared-native-dependencies-index.jsonl}"

if [ "${1:-}" = --dry-run ] && [ "$#" -eq 1 ]; then
  dry_run=1
elif [ "$#" -ne 0 ]; then
  echo "Usage: prepare-native-dependencies.sh [--dry-run]" >&2
  exit 2
fi

[ -f "$depot_dir/deps.yml" ] || {
  echo "Native dependency metadata is missing from ${depot_dir}" >&2
  exit 1
}

if [ -z "${TOTALCROSS_DEPOT_FETCH_CACHE_DIR:-}" ]; then
  if [ -n "${RUNNER_TEMP:-}" ]; then
    TOTALCROSS_DEPOT_FETCH_CACHE_DIR="${RUNNER_TEMP}/totalcross-depot-fetch-session"
  else
    TOTALCROSS_DEPOT_FETCH_CACHE_DIR="$(mktemp -d)"
  fi
fi
export TOTALCROSS_DEPOT_FETCH_CACHE_DIR
if [ "$dry_run" -eq 0 ]; then
  : > "$prepared_index"
fi

read_release_pin() {
  local dependency="$1"
  awk -v dependency="$dependency" '
    $0 == "  " dependency ":" { found = 1; next }
    found && /^  [^ ]+:/ { exit }
    found && $1 == "release:" { print $2; exit }
  ' "$depot_dir/deps.yml"
}

run_command() {
  if [ "$dry_run" -eq 1 ]; then
    printf 'FETCH'
    printf ' %q' "$@"
    printf '\n'
  else
    "$@"
  fi
}

record_prepared_marker() {
  local dependency="$1"
  local platform="$2"
  local arch="$3"
  local repository="$4"
  local release_tag="$5"
  [ "$dry_run" -eq 0 ] || return 0
  python3 - "$depot_dir" "$prepared_index" "$dependency" "$platform" "$arch" "$repository" "$release_tag" <<'PY'
import hashlib
import json
import pathlib
import sys

depot = pathlib.Path(sys.argv[1])
index = pathlib.Path(sys.argv[2])
dependency, platform, arch, repository, release_tag = sys.argv[3:]
if dependency == "sqlite3":
    namespace = "%s-%s" % (release_tag, hashlib.sha256(repository.encode()).hexdigest()[:12])
    marker = depot / dependency / "local" / namespace / platform / arch / ".totalcross-artifact.json"
elif dependency == "skia-shared":
    marker = depot / "skia" / "local" / ".totalcross-artifact.json"
elif dependency == "skia":
    artifacts = json.loads((depot / "skia" / "artifacts.json").read_text())
    target = artifacts["artifacts"]["%s-%s" % (platform, arch)]["target_path"]
    marker = depot / "skia" / pathlib.Path(target).parent / ".totalcross-artifact.json"
else:
    marker = depot / dependency / "local" / platform / arch / ".totalcross-artifact.json"
if not marker.is_file():
    raise SystemExit("prepared dependency marker is missing: %s" % marker)
payload = json.loads(marker.read_text())
if payload.get("repository") != repository or payload.get("release_tag") != release_tag:
    raise SystemExit("prepared dependency marker identity mismatch: %s" % marker)
payload["marker_path"] = str(marker.relative_to(depot))
with index.open("a") as handle:
    handle.write(json.dumps(payload, sort_keys=True, separators=(",", ":")) + "\n")
PY
}

fetch_dep() {
  local dependency="$1"
  local platform="$2"
  local arch="$3"
  local token_env="$4"
  local release_tag_var="$5"
  local github_repo_var="$6"
  shift 6
  local release_tag="${!release_tag_var:-}"
  local github_repo="${!github_repo_var:-TotalCross/totalcross-depot-tools}"
  local args

  [ -n "$release_tag" ] || release_tag="$(read_release_pin "$dependency")"
  [ -n "$release_tag" ] || {
    echo "No release pin found for ${dependency}" >&2
    exit 1
  }
  args=(
    "$depot_dir/$dependency/fetch.sh"
    --platform "$platform"
    --arch "$arch"
    --github-token-env "$token_env"
    --release-tag "$release_tag"
    --github-repo "$github_repo"
  )
  args+=("$@")
  run_command bash "${args[@]}"
  record_prepared_marker "$dependency" "$platform" "$arch" "$github_repo" "$release_tag"
}

common_targets=(
  'linux x86_64'
  'linux aarch64'
  'linux armv7l'
  'windows x86'
  'macos arm64'
  'android arm64-v8a'
  'ios arm64'
)

for target in "${common_targets[@]}"; do
  read -r platform arch <<< "$target"
  fetch_dep zlib-ng "$platform" "$arch" ZLIB_NG_GITHUB_TOKEN ZLIB_NG_RELEASE_TAG ZLIB_NG_GITHUB_REPO
  fetch_dep minizip-ng "$platform" "$arch" MINIZIP_NG_GITHUB_TOKEN MINIZIP_NG_RELEASE_TAG MINIZIP_NG_GITHUB_REPO
  fetch_dep libpng "$platform" "$arch" LIBPNG_GITHUB_TOKEN LIBPNG_RELEASE_TAG LIBPNG_GITHUB_REPO
  fetch_dep libjpeg-turbo "$platform" "$arch" LIBJPEG_TURBO_GITHUB_TOKEN LIBJPEG_TURBO_RELEASE_TAG LIBJPEG_TURBO_GITHUB_REPO
  fetch_dep sqlite3 "$platform" "$arch" SQLITE3_GITHUB_TOKEN SQLITE3_RELEASE_TAG SQLITE3_GITHUB_REPO
  fetch_dep mbedtls "$platform" "$arch" MBEDTLS_GITHUB_TOKEN MBEDTLS_RELEASE_TAG MBEDTLS_GITHUB_REPO
  fetch_dep axtls "$platform" "$arch" AXTLS_GITHUB_TOKEN AXTLS_RELEASE_TAG AXTLS_GITHUB_REPO
  fetch_dep qrcodegen "$platform" "$arch" QRCODEGEN_GITHUB_TOKEN QRCODEGEN_RELEASE_TAG QRCODEGEN_GITHUB_REPO
  fetch_dep skia "$platform" "$arch" SKIA_GITHUB_TOKEN SKIA_RELEASE_TAG SKIA_GITHUB_REPO
done

skia_shared_args=("$depot_dir/skia/fetch.sh" --install-shared --github-token-env SKIA_GITHUB_TOKEN)
[ -z "${SKIA_RELEASE_TAG:-}" ] || skia_shared_args+=(--release-tag "$SKIA_RELEASE_TAG")
[ -z "${SKIA_GITHUB_REPO:-}" ] || skia_shared_args+=(--github-repo "$SKIA_GITHUB_REPO")
run_command bash "${skia_shared_args[@]}"
if [ "$dry_run" -eq 0 ]; then
  skia_shared_release="${SKIA_RELEASE_TAG:-$(read_release_pin skia)}"
  skia_shared_repo="${SKIA_GITHUB_REPO:-TotalCross/totalcross-depot-tools}"
  record_prepared_marker skia-shared '' '' "$skia_shared_repo" "$skia_shared_release"
fi

fetch_dep vcruntime windows x86 VCRUNTIME_GITHUB_TOKEN VCRUNTIME_RELEASE_TAG VCRUNTIME_GITHUB_REPO
if [ "$dry_run" -eq 0 ]; then
  vcruntime_runtime_dir="$depot_dir/vcruntime/local/windows/x86"
  printf '%s\0%s\0' \
    "${VCRUNTIME_RELEASE_TAG:-$(read_release_pin vcruntime)}" \
    "${VCRUNTIME_GITHUB_REPO:-TotalCross/totalcross-depot-tools}" \
    | sha256sum \
    | cut -d ' ' -f 1 \
    > "$vcruntime_runtime_dir/totalcross-runtime-inputs.sha256"
fi
fetch_dep minizip-ng windows x64 MINIZIP_NG_GITHUB_TOKEN MINIZIP_NG_RELEASE_TAG MINIZIP_NG_GITHUB_REPO
fetch_dep minizip-ng windows arm64 MINIZIP_NG_GITHUB_TOKEN MINIZIP_NG_RELEASE_TAG MINIZIP_NG_GITHUB_REPO
fetch_dep minizip-ng ios-simulator arm64 MINIZIP_NG_GITHUB_TOKEN MINIZIP_NG_RELEASE_TAG MINIZIP_NG_GITHUB_REPO
fetch_dep axtls windows x64 AXTLS_GITHUB_TOKEN AXTLS_RELEASE_TAG AXTLS_GITHUB_REPO
fetch_dep axtls windows arm64 AXTLS_GITHUB_TOKEN AXTLS_RELEASE_TAG AXTLS_GITHUB_REPO
fetch_dep axtls ios-simulator arm64 AXTLS_GITHUB_TOKEN AXTLS_RELEASE_TAG AXTLS_GITHUB_REPO

echo "Prepared native dependencies sequentially in ${depot_dir}"
