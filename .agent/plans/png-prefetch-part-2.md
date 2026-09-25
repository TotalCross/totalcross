<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# PNG prefetch — Part 2: macOS measurement and Windows package

This ExecPlan follows `AGENTS.md` and
`totalcross-depot-tools/.agent/PLANS.md`. It is Part 2 of 2 and starts only
after Part 1 has completed its build/smoke gate.

## Purpose / Big Picture

Measure the completed static PNG prefetch implementation on the exact
663-image customer corpus on macOS, record the results, and produce a Windows
x64 package containing the same six-process benchmark plus a pure Windows
PowerShell 5.1 runner that requires no Python.

This plan does not change implementation architecture unless measurement exposes
a functional defect. It must not broaden into a general scroll/frame-pacing
optimization effort.

## Working Set and Resume Protocol

Repository:

    TotalCross/totalcross

Branch:

    feat/png-prefetch

Immutable ancestor:

    86d470c64b7dfc0ab6ac24314f32f0d630ff1990

Read first:

    .agent/state/png-prefetch.md

Then read only this plan's active milestone and inspect the active paths listed
in state.

Supporting files:

    .agent/evidence/png-prefetch.md
    .agent/evidence/png-prefetch-macos.csv
    .agent/reports/png-prefetch-editorial.md

Read prior pre-PNG baseline only when comparison is needed:

    .agent/evidence/image-scroll-prefetch.md

Use only its `2026-09-24 | semaphore comparison` entries plus the trusted
Windows runtime provenance recorded there.

Do not reread Part 1 unless state reports an unresolved implementation question.

## Fixed Delivered Architecture

Part 1 must already have established:

- static PNG is prefetchable;
- format detection uses encoded content, not extension;
- PNG denominator is 1;
- PNG uses the Java-result candidate route;
- deployed full decode uses the existing native PNG decoder;
- JPEG detached-native behavior is unchanged;
- multi-frame PNG remains not-prefetchable;
- `legacy` remains production default;
- no `TotalCrossVM` source was changed;
- Windows bundle packaging includes
  `run-prefetch-thread-benchmark-windows.ps1`.

If any item is false, return to Part 1 rather than changing architecture here.

## Artifact and Commit Policy

Commit compact result/evidence/report artifacts and all source/script changes.
Keep each new repository-authored file below 20 KB / approximately 600 lines.

Do not commit raw benchmark trees, build logs, extracted SDKs, compiled binaries,
or generated ZIP archives. Record package/result paths and hashes in evidence.

The required Windows ZIP is a generated deliverable and remains untracked
because it is a binary archive above the new-file limit.

Follow `.agents/skills/logical-commits/SKILL.md`. Do not amend/rewrite history.
Do not push unless explicitly requested.

## Progress

- [x] Milestone 4A: package and run fresh macOS six-process benchmark.
- [x] Milestone 4B: record/compare macOS results.
- [x] Milestone 4C: create and validate Windows x64 package.
- [ ] Finalization: editorial report, state/evidence reconciliation, commit
      checks.

Milestones 4A/4B passed on the exact 663-image corpus with six fresh macOS
processes. Compact per-row timing, frame, lifecycle, and cold-scroll counters
are in `.agent/evidence/png-prefetch-macos.csv`; interpretation and baseline
comparison are in `.agent/evidence/png-prefetch.md`. Each configuration ran
once; performance findings are descriptive.

Milestone 4C passed: the Windows x64 archive contains the current benchmark app
and PowerShell runner with the trusted Windows runtime, and its manifest,
dataset, no-external-tool contract, and ZIP contents validated. The initial SDK
staging ZIP lacked `Launcher.exe`; the trusted Windows launcher was added to the
temporary package input. No Windows build or package execution was performed.
The ZIP remains outside Git; hashes and paths are recorded in the evidence
index. Next, complete the editorial report and final state/commit checks.

## Milestone 4A — Fresh macOS benchmark

Require a clean Part 1 checkpoint and:

    git diff --quiet \
      86d470c64b7dfc0ab6ac24314f32f0d630ff1990..HEAD -- TotalCrossVM

The command must succeed.

Use the exact customer corpus. Do not synthesize another corpus. If the corpus
is unavailable, stop and record the blocker.

Use the SDK artifact produced by Part 1 and package only macOS-arm64:

    scripts/package-image-scroll-benchmark.sh \
      --sdk-zip <current-sdk-zip> \
      --corpus <customer-corpus-root> \
      --output <fresh-task-output> \
      --target macos-arm64 \
      --source-commit "$(git rev-parse HEAD)"

Do not use `--all`.

Extract/use the fresh bundle and run:

    python3 run-benchmark.py --phase prefetch-thread-diagnostics

The matrix must remain exactly:

    legacy           mask 6
    legacy           mask 38
    worker-poll      mask 6, sleep 1 ms
    worker-poll      mask 38, sleep 1 ms
    worker-semaphore mask 6
    worker-semaphore mask 38

One process per configuration. Do not add rounds in this plan.

Require all six processes to complete and all six rows to validate.

For every row require:

    image_count=663
    prefetch_request_count=663
    prefetch_ready_count=663
    prefetch_failed_count=0
    prefetch_not_prefetchable_count=0

Also require:

- same dataset hash across runs;
- no missing/duplicate preparation;
- preparation/decode/UI-dispatch lifecycle reconciliation;
- legacy per-entry thread behavior unchanged;
- poll worker uses one persistent worker and nonzero polling;
- semaphore worker uses one persistent worker and zero polling;
- semaphore release/acquire/work-wake counts balance;
- semaphore outstanding wake count is zero.

Do not rerun merely because one strategy is slower. Performance has no pass/fail
threshold.

## Milestone 4B — Record and compare macOS results

Record for each mask/strategy:

- process wall time;
- prefetch elapsed time;
- preparation-entry total;
- decode-worker time;
- UI wait;
- finishPreparation time;
- thread-start count/call/latency;
- poll count/requested sleep/measured idle;
- semaphore release/acquire/wake/outstanding;
- frame p50/p95/p99/max;
- frames over 16.67 ms;
- frames over 33.3 ms;
- frames over 50 ms;
- cold-scroll decode/materialization counters relevant to the former three
  unsupported images.

Copy the compact six-row CSV to:

    .agent/evidence/png-prefetch-macos.csv

only if it remains below 20 KB. Do not copy the entire raw result tree.

Append one concise evidence entry containing:

- source SHA;
- SDK JAR hash;
- runtime hash;
- corpus dataset hash;
- 660 JPEG / 3 PNG content counts;
- six-process PASS count;
- 663/663/0/0 outcome;
- compact performance comparison;
- raw result/log paths;
- limitations: single run per configuration.

Compare descriptively to the pre-PNG macOS benchmark already recorded in
`.agent/evidence/image-scroll-prefetch.md`.

Explicitly answer in evidence:

1. Did the three previously unsupported requests become READY?
2. Did cold-scroll decode/materialization activity associated with those three
   images disappear?
3. Did frame outliers above 33.3/50 ms materially change?
4. Did worker-semaphore scheduling remain balanced?

Do not claim causality unless counters support it. If 663/663/0/0 passes but
stalls remain, record functional PNG support as successful and stall removal as
unconfirmed.

Commit the compact result/evidence checkpoint.

Suggested commit:

    docs(benchmark): record png prefetch results

## Milestone 4C — Windows package without Windows build

No Windows build is allowed.

First prove no task native delta:

    git diff --quiet \
      86d470c64b7dfc0ab6ac24314f32f0d630ff1990..HEAD -- TotalCrossVM

If it fails, do not create a misleading Windows package. Return to Part 1 and
correct the plan violation.

Use the trusted Windows runtime provenance recorded by the prior semaphore
benchmark in:

    .agent/evidence/image-scroll-prefetch.md

Reuse that runtime because this feature is SDK-only. Record its source revision
and SHA-256 once in current evidence.

Create a current SDK/package input containing:

- current SDK JAR/source changes;
- trusted unchanged Windows runtime;
- current benchmark app;
- current PowerShell runner.

Package only:

    --target windows-x64

Pass the trusted runtime source revision through:

    --sdk-source-commit <trusted-runtime-source-sha>

and current branch HEAD through:

    --source-commit "$(git rev-parse HEAD)"

The packaging script must verify the trusted runtime revision is an ancestor of
current source and that deployed `tcvm.dll` equals the SDK ZIP runtime.

The Windows ZIP must contain at bundle root:

- deployed benchmark executable/TCZ/runtime;
- `manifest.json`;
- corpus/imag with 663 files;
- `run-prefetch-thread-benchmark-windows.ps1`;
- any existing runner files normally included by packaging.

The PowerShell runner must be the primary no-Python execution path.

Validate package contents and manifest:

    target=windows-x64
    datasetFileCount=663
    contentFormatCounts.jpeg=660
    contentFormatCounts.png=3
    prefetchThreadDiagnosticProcessCount=6

Verify the PowerShell file contains no invocation of:

    python
    py
    java
    git
    curl
    Invoke-WebRequest
    Invoke-RestMethod

Allow ordinary PowerShell/.NET APIs such as `Start-Process`, `Get-FileHash`,
`ConvertFrom-Json`, `Export-Csv`, and `Compress-Archive`.

The operator flow after extraction must be exactly:

    .\run-prefetch-thread-benchmark-windows.ps1

The script must itself execute:

    legacy 6
    legacy 38
    worker-poll 6
    worker-poll 38
    worker-semaphore 6
    worker-semaphore 38

and validate 663/663/0/0.

Do not execute the Windows package on macOS. Do not invent Windows values.

Record:

- package path;
- package SHA-256;
- source SHA;
- SDK JAR SHA-256;
- runtime SHA-256/source revision;
- PowerShell runner SHA-256;
- manifest format counts.

The ZIP remains untracked.

## Finalization

Create:

    .agent/reports/png-prefetch-editorial.md

Keep it below 20 KB and use these headings:

- Editorial Summary
- Original Plan versus Actual Outcome
- What Changed
- Decisions and Trade-offs
- Unexpected Problems and Discoveries
- Validation and Measurable Results
- Useful Evidence and Examples
- Limitations, Remaining Work, and Open Questions
- Possible Article Angles
- Suggested Narrative
- Claims Requiring Human Review

Reconcile `.agent/state/png-prefetch.md` to completed status and point to compact
evidence rather than duplicating full tables.

Run final focused checks:

    git status --short
    git diff --check
    python3 scripts/validate-copyright-headers.sh --files <task source files>

Verify new-file size limits:

    wc -c \
      .agent/plans/png-prefetch-part-1.md \
      .agent/plans/png-prefetch-part-2.md \
      .agent/state/png-prefetch.md \
      .agent/evidence/png-prefetch.md \
      .agent/reports/png-prefetch-editorial.md \
      scripts/run-prefetch-thread-benchmark-windows.ps1

If the macOS CSV exists, include it in the check.

Verify all task commits are signed and validate each message using
`.agents/skills/logical-commits/SKILL.md`.

Suggested final commit:

    docs(plan): close png prefetch rollout

Do not amend prior commits and do not push unless explicitly requested.

## Validation and Acceptance

The complete two-part plan is accepted only when:

- implementation remains descended from the immutable base;
- static PNG is prefetched at denominator 1;
- JPEG behavior is unchanged;
- no task source change exists under `TotalCrossVM`;
- focused SDK tests pass;
- SDK distribution passes;
- macOS native smoke passes;
- macOS six-process benchmark passes;
- every macOS row reports 663 requests, 663 ready, 0 failed, 0 unsupported;
- macOS measurements and limitations are committed in compact evidence;
- Windows package is produced without a Windows build;
- Windows package contains the pure PowerShell runner;
- the runner needs no Python/Java/Git/network dependency;
- package provenance proves trusted runtime reuse;
- all durable task files are committed and signed;
- all new repository-authored files remain below the requested size limit.

No FPS, wall-time, or percentage improvement threshold is an acceptance
criterion.

## Risks and Open Questions

There are no architectural choices left.

Measurement risks:

- single-run variance;
- remaining frame pacing unrelated to PNG;
- generic materialization counters that may include work unrelated to the three
  PNGs.

If evidence cannot attribute a remaining stall to PNG, say so rather than
guessing.

If Windows execution later exposes a PNG decode failure, treat it as new
platform evidence. This plan only packages Windows; it does not authorize a
Windows build or historical workaround.

## Idempotence and Recovery

Use fresh macOS benchmark/package output directories.

Do not merge partial benchmark samples with later runs.

If packaging fails, delete/recreate only the task-specific generated bundle
directory. Do not clear caches or unrelated build outputs.

Do not rerun a completed macOS benchmark for documentation-only edits.

After each logical commit rewrite `.agent/state/png-prefetch.md` with the last
commit, validation completed, artifact paths/hashes, remaining work, and next
command.

## Outcomes & Retrospective

At completion state factually:

- whether all three PNG payloads became READY;
- whether the former three-image cold-path signature disappeared on macOS;
- measured macOS strategy comparison;
- Windows package path/hash and no-Python contract;
- remaining limitations and deferred Windows measurements.

## Revision Note

Initial Part 2 revision, 2026-09-24.
