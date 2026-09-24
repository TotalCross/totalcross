<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore Windows validation package

Run `run-semaphore-windows-tests.cmd` from this folder on Windows. The runner
uses Windows PowerShell 5.1 and built-in .NET/PowerShell commands; it does not
require Python, Java, Git, or network access. It starts the correctness app and
then the 20,000-handoff stress app, with a 60-second timeout for correctness
and 120 seconds for stress. Standard output, standard error, and each test's
`DebugConsole.txt` output are saved separately under `results/`. Before each
test, the runner removes a stale package-root `DebugConsole.txt`, then copies
the file produced by that test to its own result log. Required PASS markers are
checked across all three output streams.

The PowerShell 5.1 runner acquires the process handle before its timed wait,
performs a final parameterless `WaitForExit()`, and then reads `ExitCode`. It
returns non-zero for package hash failures, timeouts, non-zero process exits, or
missing PASS markers.

The correctness smoke uses Semaphore readiness and completion handshakes. It
does not observe or claim that a thread has entered `WaitForSingleObject`
before a release. The package tests functional behavior, release consumption,
and deadlock or lost-wakeup failures without sleep, polling, or latency
measurement.

`provenance.json` identifies workflow run `36040721060`, its exact source SHA,
the artifact IDs and digests, the Windows launcher template hash used during
deployment, and SHA-256 hashes for package files. The replacement package also
records the earlier Windows app PASS markers and leaves `testExecution` pending
until this corrected runner writes `results-summary.txt` and updated results.
