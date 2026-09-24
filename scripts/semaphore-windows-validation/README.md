<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore Windows validation package

Run `run-semaphore-windows-tests.cmd` from this folder on Windows. The runner
uses Windows PowerShell 5.1 and built-in .NET/PowerShell commands; it does not
require Python, Java, Git, or network access. It starts the correctness app and
then the 20,000-handoff stress app, with a 60-second timeout for correctness
and 120 seconds for stress. A package whose provenance sets
`validationMode` to `windows-diagnostic` also runs the blocked-wake latency
app with a 180-second timeout. Standard output, standard error, and each test's
`DebugConsole.txt` output are saved separately under `results/`. Before each
test, the runner removes a stale package-root `DebugConsole.txt`, then copies
the file produced by that test to its own result log. Required PASS markers are
checked across all three output streams.

The PowerShell 5.1 runner acquires the process handle before its timed wait,
performs a final parameterless `WaitForExit()`, and then reads `ExitCode`. It
returns non-zero for package hash failures, timeouts, non-zero process exits, or
missing PASS markers.

The diagnostics-on latency app first confirms three concurrent waiters, then
runs 20 warm-ups and 200 measured samples. Each release timestamp is recorded
only after native waiter-entry confirmation, and the latency result reports
min, p50, nearest-rank p95, max, mean, and the confirmed waiter counts. It
reports blocked-waiter wake latency only when all 220 sample confirmations
succeed. No absolute latency threshold is used.

`provenance.json` identifies the workflow run, source SHA, runtime diagnostic
configuration, artifact digests, Windows launcher template, and SHA-256 hashes
for package files. The runner writes `results-summary.txt` and the final
execution details back into `provenance.json`.
