<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Semaphore v1 execution state

- Part/milestone/slice: Part 1 / Milestone 1 closed. Windows validation
  follow-up passed; Part 2 has not started and is outside the current goal.
- Branch: `feat/semaphore-v1`; current worktree; no history rewrite.
- Validated implementation revision: `ca7d77d88880ac5c6c666bd2b67721c2bead7063`.
- Last functional commits: `cc07c0097` (signed; commit-message body validation
  failure preserved), `d4873f56d` (signed CI/package workflow), and
  `ca7d77d88` (signed converter guard test fix; message validation passed).
- Windows hosted validation: run `36053759681`, source SHA above, passed.
  The diagnostics-off production and diagnostics-on Windows runtime builds,
  static production-symbol checks, SDK build, app packaging, and Windows
  PowerShell 5.1 execution all passed.
- Windows results: correctness 7 checks; stress 4 producers / 4 consumers,
  20,000 expected, produced, and acquired; latency 20 warm-ups plus 200
  samples, 200 blocked waiters confirmed, all 220 handshakes confirmed, and
  three-waiter preflight passed. min 7,900 ns, p50 12,400 ns, nearest-rank p95
  13,900 ns, max 35,500 ns, mean 12,973 ns. This is descriptive for the
  hosted Windows Server 2022 environment only.
- Windows result artifact: `semaphore-windows-diagnostic-results`; downloaded
  to `/tmp/semaphore-windows-results-36053759681`. The runner verified all
  package files; an independent check matched all 16 manifest hashes.
- Earlier run `36053130369` built both Windows runtimes but failed the SDK
  source-inventory test because its parser did not recognize the platform-
  qualified diagnostics guard. The helper was fixed, the focused converter
  test passed locally, and run `36053759681` passed the full SDK build.
- Earlier user-provided corrected-runner result for run `36040721060` remains
  recorded as package files verified, correctness and stress passed, exit code
  0, no timeouts. That diagnostics-off run did not prove wait entry.
- Local macOS validation also passed with diagnostics omitted and enabled,
  113 Ninja steps each, plus correctness, 4+4/20,000 stress, and 20+200 latency
  smokes with 220 confirmed handshakes. See the evidence index for exact logs
  and metrics.
- Durable closure record: `.agent/plans/semaphore-v1-part-1-core.md`,
  `.agent/state/semaphore-v1.md`, `.agent/evidence/semaphore-v1.jsonl`, and
  `.agent/reports/semaphore-v1-editorial.md`; they are committed together in
  the signed documentation closure commit. Resolve its revision with `git log`.
- Blockers: none.
- Deferred: Android, Linux, and iOS native execution; effective interruption
  semantics for deployed `Semaphore.acquire()`; no `ImagePreparation`
  performance claim.
- Preserve unrelated dirty paths: `totalcross.code-workspace`,
  `.agent/benchmarks/image-scroll-prefetch/final-definitive-pass/`,
  `.agent/benchmarks/image-scroll-prefetch/final-fixed/`,
  `.agent/benchmarks/scroll-raster-reuse-poc/m2/`,
  `.agent/plans/image-optimization-mask-01-diagnose.md`,
  `.agent/plans/image-optimization-mask-02-fix.md`,
  `.agent/plans/image-scroll-prefetch-reduction.md`,
  `.agent/plans/prefetch-thread-diagnostics-execplan.md`,
  `.agent/state/image-scroll-prefetch-reduction.md`,
  `ImageScrollRasterFastPathBenchmarkApp.log`,
  `ImageScrollRealWorkloadBenchmarkApp.log`, `TotalCrossSDK/IOSDateFixture.tcz`,
  `TotalCrossSDK/ImageScrollRealWorkloadBenchmarkApp.log`,
  `TotalCrossSDK/etc/launchers/`, `TotalCrossVM/xcode/generated/`, and
  `scripts/__pycache__/`.
- Next: no active Part 1 work remains. Do not start Part 2 as part of this
  goal. Future continuation begins at
  `.agent/plans/semaphore-v1-part-2-stress.md`.
- Resume: read this state first, then read the active sections of Part 2 only
  when that work is requested.
