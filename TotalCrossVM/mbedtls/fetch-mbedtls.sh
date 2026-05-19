#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: fetch-mbedtls.sh [options]

Options:
  --platform PLATFORM      Target platform: linux, windows, android, ios
  --arch ARCH              Target architecture, e.g. x86_64, armv7l, aarch64
  --release-tag TAG        GitHub release tag, default: mbedtls-3.5.2
  --github-repo OWNER/REPO GitHub repository, default: TotalCross/totalcross-mbedtls-build
  --github-token-env NAME  Environment variable containing a GitHub token,
                           default: MBEDTLS_GITHUB_TOKEN, then GITHUB_TOKEN
  --dest DIR               Destination directory, default: TotalCrossVM/mbedtls/local
EOF
}

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

platform=""
arch=""
release_tag="mbedtls-3.5.2"
github_repo="TotalCross/totalcross-mbedtls-build"
github_token_env=""
dest="${script_dir}/local"

while [ "$#" -gt 0 ]; do
  case "$1" in
    --platform)
      platform="${2:-}"
      shift 2
      ;;
    --arch)
      arch="${2:-}"
      shift 2
      ;;
    --release-tag)
      release_tag="${2:-}"
      shift 2
      ;;
    --github-repo)
      github_repo="${2:-}"
      shift 2
      ;;
    --github-token-env)
      github_token_env="${2:-}"
      shift 2
      ;;
    --dest)
      dest="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if [ -z "$platform" ] || [ -z "$arch" ]; then
  usage >&2
  exit 2
fi

case "$platform" in
  linux)
    case "$arch" in
      amd64) arch="x86_64" ;;
      arm64) arch="aarch64" ;;
      arm|armv7) arch="armv7l" ;;
    esac
    ;;
  macos|ios)
    case "$arch" in
      aarch64) arch="arm64" ;;
      amd64) arch="x86_64" ;;
    esac
    ;;
esac

tmp_dir="$(mktemp -d)"
trap 'rm -rf "${tmp_dir}"' EXIT

github_token=""
if [ -n "${github_token_env}" ]; then
  github_token="${!github_token_env:-}"
elif [ -n "${MBEDTLS_GITHUB_TOKEN:-}" ]; then
  github_token="${MBEDTLS_GITHUB_TOKEN}"
elif [ -n "${GITHUB_TOKEN:-}" ]; then
  github_token="${GITHUB_TOKEN}"
fi

github_curl() {
  if [ -n "${github_token}" ]; then
    curl -fsSL \
      -H "Authorization: Bearer ${github_token}" \
      -H "X-GitHub-Api-Version: 2022-11-28" \
      "$@"
  else
    curl -fsSL "$@"
  fi
}

download_release_asset() {
  local candidate="$1"
  local archive_path="$2"
  local download_url="https://github.com/${github_repo}/releases/download/${release_tag}/${candidate}"

  echo "Downloading mbedTLS artifact ${candidate} from ${github_repo}@${release_tag}"

  if github_curl -o "${archive_path}" "${download_url}"; then
    return 0
  fi

  if [ -z "${github_token}" ]; then
    return 1
  fi

  local release_json="${tmp_dir}/release.json"
  local asset_id=""
  github_curl \
    -o "${release_json}" \
    "https://api.github.com/repos/${github_repo}/releases/tags/${release_tag}"

  asset_id="$(
    awk -v asset_name="${candidate}" '
      /"id":/ && id == "" {
        line = $0
        sub(/.*"id": */, "", line)
        sub(/,.*/, "", line)
        id = line
      }
      /"name":/ {
        line = $0
        sub(/.*"name": "/, "", line)
        sub(/".*/, "", line)
        if (line == asset_name) {
          print id
          exit
        }
        id = ""
      }
    ' "${release_json}"
  )"

  if [ -z "${asset_id}" ]; then
    return 1
  fi

  github_curl \
    -H "Accept: application/octet-stream" \
    -o "${archive_path}" \
    "https://api.github.com/repos/${github_repo}/releases/assets/${asset_id}"
}

asset_name="mbedtls-${platform}-${arch}.tar.gz"
archive="${tmp_dir}/${asset_name}"

if ! download_release_asset "${asset_name}" "${archive}"; then
  echo "Unable to download an mbedTLS artifact for ${platform}/${arch}" >&2
  exit 1
fi

tar -tzf "${archive}" >/dev/null
tar -xzf "${archive}" -C "${tmp_dir}"

include_header="$(find "${tmp_dir}" -path "*/include/mbedtls/ssl.h" -type f | head -n 1)"
if [ -z "${include_header}" ]; then
  echo "Unable to find include/mbedtls/ssl.h in ${asset_name}" >&2
  exit 1
fi

artifact_root="$(cd "$(dirname "${include_header}")/../.." && pwd)"
for library_name in mbedtls mbedx509 mbedcrypto; do
  if ! find "${artifact_root}/lib" -type f \( -name "lib${library_name}.a" -o -name "${library_name}.lib" \) | grep -q .; then
    echo "Unable to find ${library_name} static library under ${artifact_root}/lib" >&2
    exit 1
  fi
done

rm -rf "${dest}"
mkdir -p "${dest}"
cp -a "${artifact_root}/." "${dest}/"

echo "Installed mbedTLS ${platform}/${arch} into ${dest}"
