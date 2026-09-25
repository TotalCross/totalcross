<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Windows scroll raster reuse benchmark package

This ExecPlan follows `.agent/PLANS.md` and `AGENTS.md`.

## Purpose / Big Picture

Deliver a small Windows x64 ZIP that compares `SCROLL_RASTER_REUSE` OFF and ON
with exactly three measured processes per mode, using the existing image-scroll
app and all 663 customer images. A PowerShell 5.1 command runs correctness
preflights, the six measured processes, and writes compact results with package
and host provenance. Windows execution is not part of local validation.

## Working Set and Resume Protocol

- `.agent/state/scroll-raster-reuse-windows-package.md` is the first read when
  continuing; it records the current slice and next action.
- `.agent/evidence/scroll-raster-reuse-windows-package.json` is the canonical
  package provenance and compact validation index; update at package creation.
- `.agent/plans/scroll-raster-reuse-poc-plan.md` and
  `.agent/state/scroll-raster-reuse-poc.md` contain the existing benchmark
  profile, raster diagnostics, and prior correctness decisions; read only the
  relevant sections when changing that app contract.
- `.agent/reports/scroll-raster-reuse-windows-package.md` is the final handoff.
- Source changes are isolated in worktree
  `/tmp/totalcross-scroll-raster-reuse-windows`, based on remote branch
  `feat/frame-pacing-scheduling-diagnostics` at
  `1c6306b0861c589b8c6abe5f494c5c623db346f9`. The original worktree has
  unrelated uncommitted Windows frame-pacing recovery edits; preserve them.

## Progress

- [x] Confirm local and remote target branch HEADs match the requested SHA.
- [x] Read the existing raster-reuse state and implementation contract.
- [x] Add a 663-image benchmark profile and exact SDL pixel-format evidence.
- [x] Add focused Windows runner, preflight, six-process matrix, summaries, and
  Windows-specific package mode to the existing packager.
- [x] Add focused contract checks and one-command README instructions.
- [ ] Run required focused validation, commit signed logical slices, push only
  the requested branch, and dispatch `package.yml` for the final SHA.
- [ ] Package from the successful full SDK ZIP artifact, record provenance,
  and confirm no local Windows build or benchmark execution occurred.

## Current Architecture and Scope

`ImageScrollRealWorkloadBenchmarkApp` already has a two-pass raster-reuse
profile, waypoint hashes, local OFF/ON hit accounting with diagnostics disabled,
screen-update timing, and logical-resolution validation. Its existing POC uses
120 images; release profiles use 663 but impose unrelated optimization masks.
Add a dedicated 663-image profile so the only measured difference is
`--rendering-reuse=off|on`; both cells keep ImageOptimizations mask 6.

The app reports drawable, scale, backend, and refresh metrics. SDL's selected
texture format is not currently exposed, so the SDL runtime will emit it only
when the benchmark-specific environment flag is set. The Windows runner records
that actual format with each process result and uses the returned drawable
metrics without requiring any fixed physical size.

The existing `scripts/package-image-scroll-benchmark.sh` remains the packaging
entry point. Add a dedicated Windows raster-reuse mode that reuses its SDK,
corpus, deployment, and runtime-identity checks while shipping only the app,
runtime, corpus, manifest, README, runner, and small helper.

## Plan of Work

1. Extend the app with a Windows raster-reuse profile that selects all 663
   images, three columns, logical size 540x960, mask 6, and two identical cold
   and warm passes. Preserve the existing 120-image POC behavior. Allow zero
   hits on hosts where valid physical geometry causes conservative fallbacks;
   record hit/fallback metrics instead of rejecting the host.
2. Add benchmark-gated SDL startup evidence for the selected SDL pixel format.
   Do not add timers or per-frame instrumentation to native hot paths.
3. Implement the pure PowerShell 5.1 runner. Run the minimum two OFF/ON
   correctness preflights, compare waypoint hashes, then OFF x3 followed by ON
   x3. Keep measured rendering/accounting diagnostics off and restore process
   environment variables after every invocation. Persist failure evidence and
   a result ZIP on every failure.
4. Aggregate six per-process, per-pass distributions and OFF-to-ON deltas for
   frame interval, active work, paint, screen update, and frame thresholds
   including 22.22 ms. Record reuse accounting and actual host metrics.
5. Extend the existing packager with a dedicated six-process Windows package
   mode and one-command README. Add focused contract checks for matrix
   isolation, physical-resolution tolerance, forbidden commands, failure ZIP,
   and manifest counts.
6. Validate, make signed logical commits, push only
   `feat/frame-pacing-scheduling-diagnostics`, verify the exact remote SHA,
   dispatch and verify `package.yml`, download only the successful full SDK
   artifact for that SHA, package Windows x64, and record compact provenance.

## Decision Log

- Use two correctness preflights: one per reuse mode is needed to compare OFF
  and ON viewport hashes while holding each process configuration fixed.
- Measured process count is six; preflight processes are separate and excluded
  from timing aggregates.
- Keep diagnostics/accounting enabled only in preflight. The existing local
  benchmark path supplies reuse counts in measured runs without enabling
  rendering diagnostics.
- Accept any actual physical drawable returned by Windows; require only the
  logical 540x960 benchmark size.
- Build the package from the action's full SDK ZIP and exact `tcvm.dll`; never
  use a locally built Windows runtime.

## Validation and Acceptance

Before publication run the focused validators required by the request:

- `python3 scripts/test-frame-pacing-benchmark.py`
- `python3 scripts/test-image-scroll-distributed-benchmark.py`
- `bash -n scripts/package-image-scroll-benchmark.sh`
- `git diff --check`
- focused copyright-header validation for every changed first-party file
- the applicable SDK/macOS native checks for changed code, without any local
  Windows build or Windows benchmark execution

Package acceptance: manifest says six measured processes; the runner matrix is
OFF 1-3 then ON 1-3; two preflights validate 663/660/3, prefetch, no
scroll-time decode/materialization/geometry work, final displacement, and
matching hashes; measured config differs only in reuse; output ZIP is compact;
each process records frame interval, active work, paint, screen-update and
counts above 16.67, 20, 22.22, 25, 33.3 and 50 ms, with separate cold/warm
summaries; aggregate deltas include the specified 22.22, 33.3 and 50 ms counts;
runtime SHA matches the SDK artifact; provenance names the exact source SHA,
successful workflow and full SDK artifact; `windowsExecuted` is false.

## Risks and Open Questions

- DPI scaling may make individual scroll deltas ineligible for reuse. Preserve
  correctness, report actual fallback reasons and host geometry, and do not
  fail solely because the machine uses a different physical drawable.
- The current SDK fixture previously lacked the Windows launcher. The exact
  successful `package.yml` full SDK artifact must supply both launcher and
  runtime before packaging.
- Keep every new tracked file below 20 KiB and about 600 lines; split compact
  helpers or evidence when necessary.

## Idempotence and Recovery

Use a new output directory for packaging; packaging may replace its own bundle
directory. Keep SDK ZIPs and package ZIPs under `/tmp` and never commit them.
If workflow dispatch fails, preserve its run ID and logs, fix source, commit a
new exact SHA, and dispatch only that SHA. Do not rewrite signed history.

## Outcomes & Retrospective

Pending implementation and action-built package. Do not claim Windows runtime
execution; the operator-facing ZIP is prepared for later execution.

## Revision Note

- 2026-09-25: Created for the six-process, full-corpus Windows benchmark package
  after confirming the target branch HEAD and the prior 120-image POC contract.
