# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

set -euo pipefail

ZERO_SHA=0000000000000000000000000000000000000000

usage() {
  cat <<'EOF'
Usage:
  bash scripts/validate-copyright-headers.sh
  bash scripts/validate-copyright-headers.sh <base> <head>
  bash scripts/validate-copyright-headers.sh <commit>
  bash scripts/validate-copyright-headers.sh --files <path>...

Without arguments, CI ranges are inferred from GitHub Actions environment
variables. Outside CI, staged files are checked; if nothing is staged, modified
worktree files are checked.
EOF
}

list_files_from_environment() {
  event_name="${EVENT_NAME:-${GITHUB_EVENT_NAME:-}}"

  if [ "$event_name" = "pull_request" ] && [ -n "${PR_BASE_SHA:-}" ] && [ -n "${PR_HEAD_SHA:-}" ]; then
    base="$(git merge-base "$PR_BASE_SHA" "$PR_HEAD_SHA" 2>/dev/null || printf '%s' "$PR_BASE_SHA")"
    git diff --name-only --diff-filter=ACMRT "$base" "$PR_HEAD_SHA"
    return
  fi

  if [ -n "${PUSH_AFTER:-}" ]; then
    if [ -n "${PUSH_BEFORE:-}" ] && [ "$PUSH_BEFORE" != "$ZERO_SHA" ]; then
      git diff --name-only --diff-filter=ACMRT "$PUSH_BEFORE" "$PUSH_AFTER"
    else
      git diff-tree --no-commit-id --name-only --diff-filter=ACMRT -r "$PUSH_AFTER"
    fi
    return
  fi

  staged_files="$(git diff --cached --name-only --diff-filter=ACMRT)"
  if [ -n "$staged_files" ]; then
    printf '%s\n' "$staged_files"
  else
    git diff --name-only --diff-filter=ACMRT
  fi
}

list_files() {
  if [ "${1:-}" = "--help" ]; then
    usage
    exit 0
  fi

  if [ "${1:-}" = "--files" ]; then
    shift
    printf '%s\n' "$@"
    return
  fi

  case "$#" in
    0)
      list_files_from_environment
      ;;
    1)
      git diff-tree --no-commit-id --name-only --diff-filter=ACMRT -r "$1"
      ;;
    2)
      git diff --name-only --diff-filter=ACMRT "$1" "$2"
      ;;
    *)
      usage >&2
      exit 2
      ;;
  esac
}

should_check_file() {
  path="$1"

  [ -f "$path" ] || return 1

  case "$path" in
    TotalCrossVM/deps/*|TotalCrossVM/**/local/*|build/*|*/build/*|*/.gradle/*|*.orig)
      return 1
      ;;
  esac

  case "$path" in
    *.java|*.gradle|*.kt|*.c|*.h|*.cpp|*.cc|*.hpp|*.sh|*.md|*.html|*.yml|*.yaml|*.rb|*.py)
      return 0
      ;;
  esac

  return 1
}

validate_file() {
  path="$1"
  header="$(sed -n '1,20p' "$path")"

  if ! printf '%s\n' "$header" | grep -Fq 'Copyright (C)'; then
    printf '%s: missing copyright header\n' "$path" >&2
    return 1
  fi

  if ! printf '%s\n' "$header" | grep -Fq 'SPDX-License-Identifier: LGPL-2.1-only'; then
    printf '%s: missing SPDX header\n' "$path" >&2
    return 1
  fi
}

failures=0
checked=0

while IFS= read -r path; do
  [ -n "$path" ] || continue
  if should_check_file "$path"; then
    checked=$((checked + 1))
    if ! validate_file "$path"; then
      failures=$((failures + 1))
    fi
  fi
done <<EOF
$(list_files "$@")
EOF

if [ "$failures" -gt 0 ]; then
  printf 'Copyright header validation failed for %s file(s).\n' "$failures" >&2
  exit 1
fi

printf 'Copyright header validation passed for %s file(s).\n' "$checked"
