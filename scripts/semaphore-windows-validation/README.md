<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore Windows validation package

Run `run-semaphore-windows-tests.cmd` from this folder on Windows. The runner
uses Windows PowerShell 5.1 and built-in .NET/PowerShell commands; it does not
require Python, Java, Git, or network access. It starts the correctness app and
then the 20,000-handoff stress app, with a 60-second timeout for correctness
and 120 seconds for stress. Standard output and error are saved under `results/`.

The correctness smoke uses Semaphore readiness and completion handshakes. It
does not observe or claim that a thread has entered `WaitForSingleObject`
before a release. The package tests functional behavior, release consumption,
and deadlock or lost-wakeup failures without sleep, polling, or latency
measurement.

`provenance.json` identifies workflow run `36040721060`, its exact source SHA,
the artifact IDs and digests, the Windows launcher template hash used during
deployment, and SHA-256 hashes for package files. Windows execution is pending
until the runner writes `results-summary.txt` and `testExecution` results.
