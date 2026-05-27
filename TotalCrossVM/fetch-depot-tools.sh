#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEST_DIR="${TOTALCROSS_DEPOT_TOOLS_DIR:-${ROOT_DIR}/deps/totalcross-depot-tools}"
REPO_URL="${TOTALCROSS_DEPOT_TOOLS_REPO:-https://github.com/TotalCross/totalcross-depot-tools.git}"
REF="${TOTALCROSS_DEPOT_TOOLS_REF:-}"

if [ -f "${DEST_DIR}/deps.yml" ]; then
  echo "${DEST_DIR}"
  exit 0
fi

if [ ! -e "${DEST_DIR}" ]; then
  rm -rf "${DEST_DIR}"
  mkdir -p "$(dirname "${DEST_DIR}")"
  git clone "${REPO_URL}" "${DEST_DIR}"
elif [ ! -d "${DEST_DIR}/.git" ]; then
  tmp_dir="$(mktemp -d "${DEST_DIR}.tmp.XXXXXX")"
  trap 'rm -rf "${tmp_dir}"' EXIT

  git clone "${REPO_URL}" "${tmp_dir}"
  cp -a "${tmp_dir}/." "${DEST_DIR}/"
fi

git -C "${DEST_DIR}" fetch --tags origin

if [ -n "${REF}" ]; then
  git -C "${DEST_DIR}" checkout --detach "${REF}"
else
  git -C "${DEST_DIR}" pull --ff-only
fi

echo "${DEST_DIR}"
