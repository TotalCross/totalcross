#!/usr/bin/env bash
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 LAUNCHER AXTLS_PROVIDER_SMOKE_TCZ PRE-MIGRATION|POST-MIGRATION" >&2
  exit 2
fi

launcher="$1"
smoke_tcz="$2"
phase="$3"
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
results_dir="${script_dir}/results"
launcher_dir="$(cd "$(dirname "${launcher}")" && pwd)"
launcher_name="$(basename "${launcher}")"
app_name="AxTLSProviderSmoke"
app_result_file="${launcher_dir}/axtls-provider-smoke-result.txt"
result_file="${results_dir}/${phase}-provider-smoke.log"
temporary_dir="$(mktemp -d)"
tls12_pid=""
tls13_pid=""
tls12_ecdsa_pid=""
launcher_pid=""

cleanup() {
  [ -z "${tls12_pid}" ] || kill "${tls12_pid}" 2>/dev/null || true
  [ -z "${tls13_pid}" ] || kill "${tls13_pid}" 2>/dev/null || true
  [ -z "${tls12_ecdsa_pid}" ] || kill "${tls12_ecdsa_pid}" 2>/dev/null || true
  [ -z "${launcher_pid}" ] || kill "${launcher_pid}" 2>/dev/null || true
  rm -f "${launcher_dir}/${app_name}.tcz" "${app_result_file}"
  rm -rf "${temporary_dir}"
}
trap cleanup EXIT

mkdir -p "${results_dir}"
[ -x "${launcher}" ] || { echo "Launcher is not executable: ${launcher}" >&2; exit 2; }
[ -f "${smoke_tcz}" ] || { echo "Smoke application not found: ${smoke_tcz}" >&2; exit 2; }

openssl req -x509 -newkey rsa:2048 -nodes -days 1 \
  -subj '/CN=localhost' \
  -keyout "${temporary_dir}/key.pem" \
  -out "${temporary_dir}/cert.pem" >/dev/null 2>&1
openssl ecparam -name prime256v1 -genkey -noout -out "${temporary_dir}/ecdsa-key.pem"
openssl req -x509 -new -days 1 -subj '/CN=localhost' \
  -key "${temporary_dir}/ecdsa-key.pem" \
  -out "${temporary_dir}/ecdsa-cert.pem" >/dev/null 2>&1

tls12_port="$((18000 + ($$ % 1000)))"
tls13_port="$((19000 + ($$ % 1000)))"
tls12_ecdsa_port="$((20000 + ($$ % 1000)))"
openssl s_server -accept "${tls12_port}" -rev -tls1_2 -cipher AES128-SHA \
  -cert "${temporary_dir}/cert.pem" -key "${temporary_dir}/key.pem" \
  >"${temporary_dir}/tls12-server.log" 2>&1 &
tls12_pid="$!"
openssl s_server -accept "${tls13_port}" -rev -tls1_3 \
  -cert "${temporary_dir}/cert.pem" -key "${temporary_dir}/key.pem" \
  >"${temporary_dir}/tls13-server.log" 2>&1 &
tls13_pid="$!"
openssl s_server -accept "${tls12_ecdsa_port}" -rev -tls1_2 -cipher ECDHE-ECDSA-AES128-SHA \
  -cert "${temporary_dir}/ecdsa-cert.pem" -key "${temporary_dir}/ecdsa-key.pem" \
  >"${temporary_dir}/tls12-ecdsa-server.log" 2>&1 &
tls12_ecdsa_pid="$!"

# Do not probe with a TCP client here: openssl s_server treats a probe EOF as
# a client failure and may stop before the TotalCross app connects.
sleep 0.5
if ! kill -0 "${tls12_pid}" 2>/dev/null || ! kill -0 "${tls13_pid}" 2>/dev/null || ! kill -0 "${tls12_ecdsa_pid}" 2>/dev/null; then
  cat "${temporary_dir}/tls12-server.log" "${temporary_dir}/tls13-server.log" "${temporary_dir}/tls12-ecdsa-server.log" >&2
  exit 1
fi

cp "${smoke_tcz}" "${launcher_dir}/${app_name}.tcz"
rm -f "${app_result_file}"
(
  cd "${launcher_dir}"
  "./${launcher_name}" "${app_name}" "${tls12_port}" "${tls13_port}" "${tls12_ecdsa_port}"
) >"${result_file}" 2>&1 &
launcher_pid="$!"

for _ in $(seq 1 90); do
  if ! kill -0 "${launcher_pid}" 2>/dev/null; then
    break
  fi
  sleep 1
done

if kill -0 "${launcher_pid}" 2>/dev/null; then
  echo "[FAIL] Launcher timed out after 90 seconds" >>"${result_file}"
  kill "${launcher_pid}" 2>/dev/null || true
  wait "${launcher_pid}" 2>/dev/null || true
  launcher_pid=""
  cat "${result_file}"
  exit 124
fi

set +e
wait "${launcher_pid}"
status="$?"
set -e
launcher_pid=""

if [ -f "${app_result_file}" ]; then
  cat "${app_result_file}" >>"${result_file}"
fi
cat "${result_file}"
[ "${status}" -eq 0 ] || exit "${status}"
rg -F '[PASS] base TLSv1.2' "${result_file}" >/dev/null
rg -F '[PASS] base TLSv1.3 rejected' "${result_file}" >/dev/null
rg -F '[PASS] base TLSv1.2 ECDSA rejected' "${result_file}" >/dev/null
rg -F '[PASS] default TLSv1.2' "${result_file}" >/dev/null
rg -F '[PASS] default TLSv1.3 unsupported (FFFF8880)' "${result_file}" >/dev/null
rg -F '[PASS] default TLSv1.2 ECDSA' "${result_file}" >/dev/null
rg -F '[PASS] provider matrix complete' "${result_file}" >/dev/null

echo "Provider smoke results: ${result_file}"
