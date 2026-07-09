#!/usr/bin/env bash
#
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEST_DIR="${TOTALCROSS_DEPOT_TOOLS_DIR:-${ROOT_DIR}/totalcross-depot-tools}"
REPO_URL="${TOTALCROSS_DEPOT_TOOLS_REPO:-https://github.com/TotalCross/totalcross-depot-tools.git}"
REF="${TOTALCROSS_DEPOT_TOOLS_REF:-}"
REF_FILE="${ROOT_DIR}/totalcross-depot-tools.ref"

canonical_repo_url() {
  local url="$1"

  case "${url}" in
    git@github.com:*)
      url="https://github.com/${url#git@github.com:}"
      ;;
    ssh://git@github.com/*)
      url="https://github.com/${url#ssh://git@github.com/}"
      ;;
  esac

  url="${url%.git}"
  printf '%s\n' "${url}"
}

if [ -z "${REF}" ]; then
  if [ -f "${REF_FILE}" ]; then
    line=""
    while IFS= read -r line || [ -n "${line}" ]; do
      line="${line%%#*}"
      line="${line#"${line%%[![:space:]]*}"}"
      line="${line%"${line##*[![:space:]]}"}"
      if [ -n "${line}" ]; then
        REF="${line}"
        break
      fi
    done < "${REF_FILE}"

    if [ -z "${REF}" ]; then
      echo "Warning: no totalcross-depot-tools ref found in ${REF_FILE}; falling back to the repository default branch" >&2
    fi
  else
    echo "Warning: ${REF_FILE} not found; falling back to the repository default branch" >&2
  fi
fi

if [ ! -e "${DEST_DIR}" ]; then
  mkdir -p "$(dirname "${DEST_DIR}")"
  git clone "${REPO_URL}" "${DEST_DIR}"
elif [ ! -d "${DEST_DIR}/.git" ]; then
  tmp_dir="$(mktemp -d "${DEST_DIR}.tmp.XXXXXX")"
  trap 'rm -rf "${tmp_dir}"' EXIT

  echo "Warning: ${DEST_DIR} exists without Git metadata; restoring it from ${REPO_URL}" >&2
  git clone "${REPO_URL}" "${tmp_dir}"
  cp -a "${tmp_dir}/." "${DEST_DIR}/"
fi

actual_repo_url="$(git -C "${DEST_DIR}" remote get-url origin 2>/dev/null || true)"
if [ -z "${actual_repo_url}" ]; then
  echo "Error: ${DEST_DIR} has no origin remote; expected ${REPO_URL}" >&2
  exit 1
fi

if [ "$(canonical_repo_url "${actual_repo_url}")" != "$(canonical_repo_url "${REPO_URL}")" ]; then
  echo "Error: ${DEST_DIR} origin is ${actual_repo_url}; expected ${REPO_URL}" >&2
  exit 1
fi

git -C "${DEST_DIR}" fetch --tags origin

if [ -n "${REF}" ]; then
  git -C "${DEST_DIR}" -c advice.detachedHead=false checkout --force --detach "${REF}"
else
  default_branch="$(git -C "${DEST_DIR}" symbolic-ref --short refs/remotes/origin/HEAD 2>/dev/null || true)"
  default_branch="${default_branch#origin/}"
  if [ -n "${default_branch}" ]; then
    git -C "${DEST_DIR}" checkout --force "${default_branch}"
    git -C "${DEST_DIR}" pull --ff-only origin "${default_branch}"
  else
    git -C "${DEST_DIR}" pull --ff-only
  fi
fi

echo "${DEST_DIR}"
