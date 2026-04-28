#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
MANIFEST_PATH="$ROOT_DIR/skia/artifacts.json"
BASE_URL="${SKIA_ARTIFACT_BASE_URL:-}"
GITHUB_REPO="${SKIA_GITHUB_REPO:-}"
RELEASE_TAG="${SKIA_RELEASE_TAG:-}"
PLATFORM=""
ARCH=""
SOURCE=""
INSTALL_DEV_BUNDLE=0

usage() {
  cat <<EOF
Usage: $(basename "$0") [options]

Installs a Skia artifact outside Git tracking, either from a local file or from
an artifact base URL or a GitHub Release.

Options:
  --platform <name>   Target platform: ios, macos, linux, android.
  --arch <name>       Target arch/ABI: universal, arm64, x86_64, aarch64,
                      armv7l, arm64-v8a, armeabi-v7a.
  --source <path|url> Install from a specific local file or URL.
  --base-url <url>    Base URL used with the manifest artifact_name.
  --github-repo <r>   GitHub repo in owner/name format.
  --release-tag <t>   GitHub release tag.
  --manifest <file>   Manifest file. Default: $MANIFEST_PATH
  --install-dev       Install the shared dev bundle declared in the manifest.
  --print-target      Print the resolved output path and exit.
  -h, --help          Show this help.

Examples:
  $(basename "$0")
  $(basename "$0") --platform macos --arch arm64 --source /tmp/libskia.a
  $(basename "$0") --platform linux --arch x86_64 --base-url https://artifacts.example.com/skia/m87
  $(basename "$0") --platform ios --arch universal
  $(basename "$0") --github-repo TotalCross/totalcross-skia-build --release-tag skia-158dc9d7-r1
EOF
}

die() {
  echo "error: $*" >&2
  exit 1
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "missing required command: $1"
}

SKIA_DOWNLOAD_RETRIES="${SKIA_DOWNLOAD_RETRIES:-5}"
SKIA_DOWNLOAD_RETRY_DELAY="${SKIA_DOWNLOAD_RETRY_DELAY:-2}"

should_retry_download() {
  local status="$1"
  [[ "$status" == "000" || "$status" == "408" || "$status" == "429" || "$status" == "500" || "$status" == "502" || "$status" == "503" || "$status" == "504" ]]
}

download_to_file() {
  local url="$1"
  local out="$2"
  local attempt=1
  local max_attempts="$SKIA_DOWNLOAD_RETRIES"
  local delay="$SKIA_DOWNLOAD_RETRY_DELAY"
  local status
  local curl_exit

  require_cmd curl

  while true; do
    status=$(curl -w "%{http_code}" -fsSL -o "$out" "$url") && return 0
    curl_exit=$?

    if [[ $attempt -ge $max_attempts ]] || ! should_retry_download "${status:-000}"; then
      rm -f "$out"
      echo "error: failed to download '$url' after $attempt attempt(s) (curl exit $curl_exit, http ${status:-000})" >&2
      return "$curl_exit"
    fi

    rm -f "$out"
    echo "warning: transient download failure for '$url' (attempt $attempt/$max_attempts, curl exit $curl_exit, http ${status:-000}); retrying in ${delay}s" >&2
    sleep "$delay"
    attempt=$((attempt + 1))
    delay=$((delay * 2))
  done
}

verify_sha256() {
  local file_path="$1"
  local expected_sha="$2"
  local label="$3"

  if [[ -n "$expected_sha" ]]; then
    local actual_sha
    actual_sha=$(shasum -a 256 "$file_path" | awk '{print $1}')
    [[ "$actual_sha" == "$expected_sha" ]] || die "checksum mismatch for ${label}: expected ${expected_sha}, got ${actual_sha}"
  else
    echo "warning: no sha256 configured for ${label} in ${MANIFEST_PATH}" >&2
  fi
}

install_linux_build_manifests() {
  local metadata_info

  metadata_info=$(
    python3 - "$MANIFEST_PATH" <<'PY'
import json
import pathlib
import sys

manifest = json.loads(pathlib.Path(sys.argv[1]).read_text())
entries = manifest.get("metadata", {}).get("linux-build-manifests", {})
for arch, info in sorted(entries.items()):
    print(arch)
    print(info["artifact_name"])
    print(info["target_path"])
    print(info.get("sha256", ""))
PY
  )

  [[ -n "$metadata_info" ]] || return 0

  while IFS= read -r manifest_arch; do
    [[ -n "$manifest_arch" ]] || break
    IFS= read -r manifest_name || break
    IFS= read -r manifest_target_rel || break
    IFS= read -r manifest_sha || break

    local manifest_target="$ROOT_DIR/$manifest_target_rel"
    local manifest_tmp
    manifest_tmp=$(mktemp /tmp/skia-linux-manifest.XXXXXX)

    if [[ -n "$BASE_URL" ]]; then
      download_to_file "${BASE_URL%/}/${manifest_name}" "$manifest_tmp"
    elif [[ -n "$GITHUB_REPO" && -n "$RELEASE_TAG" ]]; then
      download_to_file "https://github.com/${GITHUB_REPO}/releases/download/${RELEASE_TAG}/${manifest_name}" "$manifest_tmp"
    else
      rm -f "$manifest_tmp"
      die "linux build manifest installation requires a GitHub release or --base-url"
    fi

    verify_sha256 "$manifest_tmp" "$manifest_sha" "linux-build-manifest-${manifest_arch}"
    mkdir -p "$(dirname "$manifest_target")"
    cp "$manifest_tmp" "$manifest_target"
    rm -f "$manifest_tmp"

    echo "Installed Linux build manifest for ${manifest_arch} at:"
    echo "  $manifest_target"
  done <<< "$metadata_info"
}

normalize_platform() {
  case "$1" in
    iOS|ios) echo "ios" ;;
    Darwin|darwin|macos|mac|osx) echo "macos" ;;
    Linux|linux) echo "linux" ;;
    Android|android) echo "android" ;;
    *) die "unsupported platform: $1" ;;
  esac
}

normalize_arch() {
  case "$1" in
    universal) echo "universal" ;;
    arm64|aarch64) echo "arm64" ;;
    x86_64|amd64) echo "x86_64" ;;
    armv7l|armv7) echo "armv7l" ;;
    arm64-v8a) echo "arm64-v8a" ;;
    armeabi-v7a) echo "armeabi-v7a" ;;
    *) die "unsupported architecture/ABI: $1" ;;
  esac
}

detect_platform() {
  normalize_platform "$(uname -s)"
}

detect_arch() {
  normalize_arch "$(uname -m)"
}

PRINT_TARGET=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --platform)
      [[ $# -ge 2 ]] || die "--platform requires a value"
      PLATFORM=$(normalize_platform "$2")
      shift 2
      ;;
    --arch)
      [[ $# -ge 2 ]] || die "--arch requires a value"
      ARCH=$(normalize_arch "$2")
      shift 2
      ;;
    --source)
      [[ $# -ge 2 ]] || die "--source requires a value"
      SOURCE="$2"
      shift 2
      ;;
    --base-url)
      [[ $# -ge 2 ]] || die "--base-url requires a value"
      BASE_URL="$2"
      shift 2
      ;;
    --github-repo)
      [[ $# -ge 2 ]] || die "--github-repo requires a value"
      GITHUB_REPO="$2"
      shift 2
      ;;
    --release-tag)
      [[ $# -ge 2 ]] || die "--release-tag requires a value"
      RELEASE_TAG="$2"
      shift 2
      ;;
    --manifest)
      [[ $# -ge 2 ]] || die "--manifest requires a value"
      MANIFEST_PATH="$2"
      shift 2
      ;;
    --install-dev)
      INSTALL_DEV_BUNDLE=1
      shift
      ;;
    --print-target)
      PRINT_TARGET=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      die "unknown argument: $1"
      ;;
  esac
done

require_cmd python3

[[ -n "$PLATFORM" ]] || PLATFORM=$(detect_platform)
[[ -n "$ARCH" ]] || ARCH=$(detect_arch)

if [[ "$PLATFORM" == "linux" && "$ARCH" == "arm64" ]]; then
  ARCH="aarch64"
fi

ARTIFACT_KEY="${PLATFORM}-${ARCH}"

ARTIFACT_INFO=$(
  python3 - "$MANIFEST_PATH" "$ARTIFACT_KEY" <<'PY'
import json
import pathlib
import sys

manifest_path = pathlib.Path(sys.argv[1])
artifact_key = sys.argv[2]

manifest = json.loads(manifest_path.read_text())
artifact = manifest["artifacts"].get(artifact_key)
if artifact is None:
    sys.exit(2)

print(artifact["artifact_name"])
print(artifact["target_path"])
print(artifact.get("sha256", ""))
defaults = manifest.get("defaults", {})
source = defaults.get("source", {})
print(source.get("type", ""))
print(source.get("repo", ""))
print(source.get("tag", ""))
dev_bundle = defaults.get("dev_bundle", {})
print(dev_bundle.get("artifact_name", ""))
print(dev_bundle.get("sha256", ""))
PY
) || die "artifact '${ARTIFACT_KEY}' not found in manifest ${MANIFEST_PATH}"

ARTIFACT_NAME=$(printf '%s\n' "$ARTIFACT_INFO" | sed -n '1p')
TARGET_PATH_REL=$(printf '%s\n' "$ARTIFACT_INFO" | sed -n '2p')
EXPECTED_SHA=$(printf '%s\n' "$ARTIFACT_INFO" | sed -n '3p')
DEFAULT_SOURCE_TYPE=$(printf '%s\n' "$ARTIFACT_INFO" | sed -n '4p')
DEFAULT_GITHUB_REPO=$(printf '%s\n' "$ARTIFACT_INFO" | sed -n '5p')
DEFAULT_RELEASE_TAG=$(printf '%s\n' "$ARTIFACT_INFO" | sed -n '6p')
DEV_BUNDLE_NAME=$(printf '%s\n' "$ARTIFACT_INFO" | sed -n '7p')
DEV_BUNDLE_SHA=$(printf '%s\n' "$ARTIFACT_INFO" | sed -n '8p')
TARGET_PATH="$ROOT_DIR/$TARGET_PATH_REL"

if [[ -z "$GITHUB_REPO" ]]; then
  GITHUB_REPO="$DEFAULT_GITHUB_REPO"
fi

if [[ -z "$RELEASE_TAG" ]]; then
  RELEASE_TAG="$DEFAULT_RELEASE_TAG"
fi

if [[ $PRINT_TARGET -eq 1 ]]; then
  printf '%s\n' "$TARGET_PATH"
  exit 0
fi

mkdir -p "$(dirname "$TARGET_PATH")"

TMP_FILE=$(mktemp /tmp/skia-artifact.XXXXXX)
TMP_DEV_FILE=""
TMP_DEV_DIR=""
cleanup() {
  rm -f "$TMP_FILE"
  if [[ -n "$TMP_DEV_FILE" ]]; then
    rm -f "$TMP_DEV_FILE"
  fi
  if [[ -n "$TMP_DEV_DIR" ]]; then
    rm -rf "$TMP_DEV_DIR"
  fi
  return 0
}
trap cleanup EXIT

if [[ -n "$SOURCE" ]]; then
  if [[ -f "$SOURCE" ]]; then
    cp "$SOURCE" "$TMP_FILE"
  else
    download_to_file "$SOURCE" "$TMP_FILE"
  fi
elif [[ -n "$BASE_URL" ]]; then
  download_to_file "${BASE_URL%/}/${ARTIFACT_NAME}" "$TMP_FILE"
elif [[ "$DEFAULT_SOURCE_TYPE" == "github_release" || -n "$GITHUB_REPO" || -n "$RELEASE_TAG" ]]; then
  [[ -n "$GITHUB_REPO" ]] || die "missing GitHub repo. Use --github-repo or set defaults.source.repo in the manifest"
  [[ -n "$RELEASE_TAG" ]] || die "missing release tag. Use --release-tag or set defaults.source.tag in the manifest"
  download_to_file "https://github.com/${GITHUB_REPO}/releases/download/${RELEASE_TAG}/${ARTIFACT_NAME}" "$TMP_FILE"
else
  die "either --source, --base-url/SKIA_ARTIFACT_BASE_URL, or a GitHub release default is required"
fi

verify_sha256 "$TMP_FILE" "$EXPECTED_SHA" "$ARTIFACT_KEY"

cp "$TMP_FILE" "$TARGET_PATH"
echo "Installed ${ARTIFACT_KEY} artifact at:"
echo "  $TARGET_PATH"

if [[ $INSTALL_DEV_BUNDLE -eq 1 ]]; then
  [[ -n "$DEV_BUNDLE_NAME" ]] || die "no defaults.dev_bundle.artifact_name configured in ${MANIFEST_PATH}"
  TMP_DEV_FILE=$(mktemp /tmp/skia-dev-bundle.XXXXXX.tar.gz)
  TMP_DEV_DIR=$(mktemp -d /tmp/skia-dev-bundle.XXXXXX)

  if [[ -n "$BASE_URL" ]]; then
    download_to_file "${BASE_URL%/}/${DEV_BUNDLE_NAME}" "$TMP_DEV_FILE"
  elif [[ -n "$GITHUB_REPO" && -n "$RELEASE_TAG" ]]; then
    download_to_file "https://github.com/${GITHUB_REPO}/releases/download/${RELEASE_TAG}/${DEV_BUNDLE_NAME}" "$TMP_DEV_FILE"
  else
    die "--install-dev requires a GitHub release or --base-url"
  fi

  verify_sha256 "$TMP_DEV_FILE" "$DEV_BUNDLE_SHA" "dev-bundle"
  tar -xzf "$TMP_DEV_FILE" -C "$TMP_DEV_DIR"
  rsync -a "$TMP_DEV_DIR/modules/skia/" "$ROOT_DIR/skia/local/"
  install_linux_build_manifests

  echo "Installed Skia dev bundle at:"
  echo "  $ROOT_DIR/skia/local"
fi
