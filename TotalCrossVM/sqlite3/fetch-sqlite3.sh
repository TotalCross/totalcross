#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: fetch-sqlite3.sh [options]

Options:
  --platform PLATFORM      Target platform: linux, windows, android, ios
  --arch ARCH              Target architecture, e.g. x86_64, armv7l, aarch64
  --variant VARIANT        Artifact variant, default: plain
  --release-tag TAG        GitHub release tag, default: sqlite-3.32.3
  --github-repo OWNER/REPO GitHub repository, default: TotalCross/totalcross-sqlite3-build
  --github-token-env NAME  Environment variable containing a GitHub token,
                           default: SQLITE3_GITHUB_TOKEN, then GITHUB_TOKEN
  --dest DIR               Destination directory, default: TotalCrossVM/sqlite3/local
EOF
}

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/.." && pwd)"

platform=""
arch=""
variant="plain"
release_tag="sqlite-3.32.3"
github_repo="TotalCross/totalcross-sqlite3-build"
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
    --variant)
      variant="${2:-}"
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
elif [ -n "${SQLITE3_GITHUB_TOKEN:-}" ]; then
  github_token="${SQLITE3_GITHUB_TOKEN}"
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

  echo "Downloading SQLite3 artifact ${candidate}"

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
    ruby -rjson -e '
      release = JSON.parse(File.read(ARGV.fetch(0)))
      asset_name = ARGV.fetch(1)
      asset = release.fetch("assets", []).find { |item| item["name"] == asset_name }
      puts asset["id"] if asset
    ' "${release_json}" "${candidate}"
  )"

  if [ -z "${asset_id}" ]; then
    return 1
  fi

  github_curl \
    -H "Accept: application/octet-stream" \
    -o "${archive_path}" \
    "https://api.github.com/repos/${github_repo}/releases/assets/${asset_id}"
}

asset_name=""
archive=""
candidate_assets=()
if [ "${variant}" != "plain" ]; then
  candidate_assets+=("sqlite3-${variant}-${platform}-${arch}.tar.gz")
else
  candidate_assets+=("sqlite3-${platform}-${arch}.tar.gz")
fi

for candidate in "${candidate_assets[@]}"; do
  archive="${tmp_dir}/${candidate}"
  if download_release_asset "${candidate}" "${archive}"; then
    asset_name="${candidate}"
    break
  fi
done

if [ -z "${asset_name}" ]; then
  echo "Unable to download a SQLite3 artifact for ${variant}/${platform}/${arch}" >&2
  exit 1
fi

resolved_variant="plain"
if [ "${asset_name}" = "sqlite3-${variant}-${platform}-${arch}.tar.gz" ]; then
  resolved_variant="${variant}"
fi

echo "SQLite3 variant requested: ${variant}"
echo "SQLite3 variant resolved: ${resolved_variant}"
echo "SQLite3 artifact: ${asset_name}"

tar -tzf "${archive}" >/dev/null
tar -xzf "${archive}" -C "${tmp_dir}"

include_header="$(find "${tmp_dir}" -path "*/include/sqlite3.h" -type f | head -n 1)"
if [ -z "${include_header}" ]; then
  echo "Unable to find include/sqlite3.h in ${asset_name}" >&2
  exit 1
fi

artifact_root="$(cd "$(dirname "${include_header}")/.." && pwd)"
if ! find "${artifact_root}/lib" -type f \( -name "libsqlite3.a" -o -name "sqlite3.lib" \) | grep -q .; then
  echo "Unable to find sqlite3 static library under ${artifact_root}/lib" >&2
  exit 1
fi

rm -rf "${dest}"
mkdir -p "${dest}"
cp -a "${artifact_root}/." "${dest}/"

echo "Installed SQLite3 ${resolved_variant}/${platform}/${arch} into ${dest}"
