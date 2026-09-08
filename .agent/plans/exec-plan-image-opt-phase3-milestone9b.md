<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Phase 3 Milestone 9B — Authoritative benchmark and platform freeze

This ExecPlan follows `AGENTS.md`, `.agents/skills/logical-commits/SKILL.md`,
and the ExecPlan contract from `TotalCross/totalcross-depot-tools/.agent/PLANS.md`.
It is the second of two sequential Milestone-9 plans. Do not execute it until
`exec-plan-image-opt-phase3-milestone9a.md` is complete and state records `GO`.

## Purpose / Big Picture

Close Image Optimization Phase 3 on its actual frozen Phase-2 base. Produce
fresh authoritative macOS S1/S2/S3 evidence for Phase 3 alone and for the full
Phase-2+Phase-3 stack, execute the existing GitHub platform build matrix,
validate compact formats on a physical Android GPU runtime built from the exact
candidate SHA, then freeze the final Phase-3 handoff for Phase 4.

Plan 9B must not introduce new optimizations. Runtime edits are allowed only to
fix a correctness or confirmed disabled-path regression found by this plan; any
such edit invalidates affected benchmark evidence and must be followed by the
required rerun.

## Working Set and Resume Protocol

Branch:

    perf/image-opt-phase3-formats

Frozen Phase 2:

    6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee

Read first on every resume:

    .agent/state/image-opt-phase3-formats.md

The state must show Plan 9A complete, the last runtime correction SHA, frozen
harness commit SHA, adapter digest, and no unresolved correctness blocker.
Then read only the active section of this plan.

Supporting paths:

    .agent/plans/exec-plan-image-opt-phase3-milestone9a.md
    .agent/plans/exec-plan-image-opt-phase3-milestone9b.md
    .agent/evidence/image-opt-phase3-formats.jsonl
    .agent/archive/image-opt-phase3-formats-history.md
    .agent/reports/image-opt-phase3-formats-editorial.md
    .agent/design/image-optimization-benchmark-protocol.md
    .agent/benchmarks/image-opt-phase3-formats/milestone9/

Do not reread historical Phase-3 benchmark directories during normal resume.
Historical per-feature evidence is immutable provenance, not input to the new
measurement decisions.

Every new file must remain <=20 KiB or approximately 600 lines. Split new files
before the limit. Do not refactor existing files merely to shrink them. Raw CSV
samples over 20 KiB must be deterministically gzip-compressed. Commit all direct
plan artifacts; do not commit ordinary build outputs, APKs, binaries, full CI
logs, complete local build logs, or raw logcat.

## Progress

- [ ] Verify Plan 9A handoff and frozen runtime/harness state.
- [ ] Capture authoritative Matrix A on macOS software Skia.
- [ ] Capture authoritative Matrix B with complete Phase-2 final stack.
- [ ] Apply 60->200 and matched-memory escalation where triggered.
- [ ] Commit benchmark samples/reports and identify runtime candidate SHA.
- [ ] Run exact-SHA GitHub build matrix.
- [ ] Run exact-SHA physical Android GPU compact-format smoke.
- [ ] Reconcile state/evidence/archive/editorial and freeze Phase 3.

## Current Architecture and Scope

Final Phase 2 is exactly:

    PHASE2_FINAL = {0,1,2,3,4,13,14,15}

Phase 3 storage is exactly:

    PHASE3_STORAGE = {5,6,7}

IDs 8-12 stay disabled. Every scenario must reset settings, explicitly disable
all IDs `0..FEATURE_COUNT-1`, then enable only its exact set. Never rely on
`DEFAULT`. Test accounting, if required for counters, must be configured
identically across compared scenarios and outside timed work.

MacOS Release software Skia is the authoritative performance environment:

    -DCMAKE_BUILD_TYPE=Release
    -DTC_GRAPHICS_SOFTWARE=ON
    -DTC_RENDERER_SKIA=ON
    -DTC_WINDOWING_SDL=ON

Use the same machine/build/backend/fixtures/workload/sample regime inside each
comparison. Use 3 complete warmups and 60 measured samples. External RSS sample
interval is 50 ms. Full hashing and quality analysis remain outside timing.

Frozen protocol escalation:

- elapsed CV >5%, or S2 near/beyond +5% median elapsed or peak RSS -> rerun
  affected comparison with 200 samples;
- persistent >5% peak RSS after 200 -> matched `vmmap -summary`, `ps` RSS, and
  physical-footprint captures at equivalent checkpoints, normally samples 100
  and 150;
- confirmed >5% post-disabled median elapsed or live/private/physical memory
  regression blocks closure;
- classify allocator/residency noise unconfirmed only when matched captures do
  not reproduce the live/private/physical growth. Preserve both signals.

Do not create a hosted benchmark workflow in this plan. Windows/Linux hosted
S1/S2/S3 performance remains `NOT AVAILABLE`; normal platform build success is
still required.

## Plan of Work

### Milestone 9.4 — Authoritative macOS benchmark closeout

Before any capture run:

    git status --short
    git rev-parse HEAD
    git merge-base HEAD 6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee
    git diff --check

Require clean planned state, exact merge-base, and the Plan-9A harness digest
recorded in state. If runtime/harness changed since the recorded 9A freeze,
stop and revalidate/re-freeze before measuring.

Use the exact-base adapter committed by 9A for S1. It may shim only Phase-3-only
storage APIs/counters; real Phase-2 IDs `0,1,2,3,4,13,14,15` must execute on S1.

#### Matrix A — Phase 3 isolated

Use workload `milestone9-isolated` and identical fixture bytes/operations.

S1/pre:

    runtime = 6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee
    all Phase-2 features OFF
    Phase-3 storage unavailable/inert through adapter

S2/post-disabled:

    runtime = final 9A Phase-3 runtime
    all feature IDs explicitly OFF

S3/post-enabled:

    runtime = same S2 runtime
    only STORAGE_RGB565, STORAGE_GRAY8, STORAGE_ARGB4444 ON

Store direct artifacts under:

    .agent/benchmarks/image-opt-phase3-formats/milestone9/isolated/

Acceptance:

- fixture/input hashes identical in S1/S2/S3;
- exact full-output parity S1 vs S2;
- S2 has no confirmed >5% median/RSS regression;
- S3 selects intended RGB565/GRAY8/ARGB4444 sources;
- S3 promotion count is zero;
- S3 temporary full-RGBA decode bytes are zero;
- S3 storage is exactly 2/1/2 BPP for RGB565/GRAY8/ARGB4444;
- independent quality oracles pass;
- no minimum S3 speedup is required. Report timing/memory honestly.

#### Matrix B — full Phase 2 + Phase 3

Use workload `milestone9-full-stack` and the same fixture family and timed work.

S1/pre:

    runtime = 6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee
    enable exactly PHASE2_FINAL={0,1,2,3,4,13,14,15}
    Phase-3 storage unavailable/inert through adapter

S2/post-disabled:

    runtime = same final 9A Phase-3 runtime
    enable exactly PHASE2_FINAL
    disable PHASE3_STORAGE and IDs 8-12

S3/post-enabled:

    runtime = same S2 runtime
    enable exactly PHASE2_FINAL + PHASE3_STORAGE
    keep IDs 8-12 disabled

Store direct artifacts under:

    .agent/benchmarks/image-opt-phase3-formats/milestone9/full-stack/

Acceptance:

- identical fixture/input hashes across all scenarios;
- exact full-output parity S1 vs S2;
- S2 has no confirmed >5% median/RSS regression;
- S3 selects all intended compact formats;
- S3 source promotion count is zero and full RGBA staging is zero;
- S3 quality oracles pass;
- Phase-2 counters exercised by the workload remain semantically valid;
- no minimum S3 speedup is required.

Apply 60->200/matched-memory escalation independently to Matrix A/B when the
frozen triggers fire. Do not exceed 200 samples without a new documented reason.

For every benchmark report record: exact scenario SHAs, adapter digest, machine,
macOS/CPU/RAM, build flags/commands, sample counts, workload/batch count,
median/p95, mean/stddev when useful, CV, peak RSS, relevant counters/backing
bytes, S2-vs-S1 and S3-vs-S1/S2 deltas, quality/correctness, escalation reason,
and limitations.

If a confirmed regression requires runtime code change, stop measurement,
commit a focused fix + regression test, rerun focused validation, regenerate all
affected S1/S2/S3 evidence, and update state. Never mix pre-fix and post-fix
scenarios in one comparison.

Commit compact samples/reports/evidence after the matrices are accepted:

    test(image): record phase 3 final-stack benchmarks

Do not commit dylibs, executables, build directories, ordinary build logs, or
raw monitoring output not required as direct benchmark evidence.

After this commit run:

    git rev-parse HEAD

Record that exact SHA as `PHASE3_RUNTIME_CANDIDATE` unless a later runtime/test
change occurs. The final docs-only closeout commit will have a different tip.

### Milestone 9.5 — Platform closeout

Goal: validate the exact runtime candidate on the repository build matrix and a
physical Android production-GPU runtime.

#### GitHub build matrix

Use the existing PR workflow. Do not add benchmark CI infrastructure.

If remote CI requires a push and credentials are already configured, use only:

    git push origin perf/image-opt-phase3-formats

Never force-push. Immediately verify the remote branch SHA equals
`PHASE3_RUNTIME_CANDIDATE`; do not associate a workflow with the candidate if it
does not.

Require the existing relevant jobs to succeed for the candidate, including:

- SDK;
- macOS ARM64;
- Android;
- iOS device archive;
- Windows;
- Windows native/legacy when present;
- Linux amd64;
- native Linux ARM64;
- any other currently required matrix job.

Build failure is a blocker. Hosted build duration is not performance evidence.
Record workflow run ID/URL, candidate SHA, job summary, and any limitation in
compact evidence; do not commit full CI logs.

If credentials cannot start/observe the exact-SHA CI run, record
`BLOCKED: remote CI unavailable` and do not freeze Phase 3.

#### Android physical GPU smoke

Use a physical Android device through `adb`. Reuse the branch-matching
provenance/deployment approach documented in:

    .agent/evidence/image-opt-phase2-raster-closeout-android.md

Read that file once. Build/deploy the Milestone-9 compact smoke from
`PHASE3_RUNTIME_CANDIDATE` using the normal production Android GPU/OpenGL ES
configuration. Never define `TC_GRAPHICS_SOFTWARE` for Android.

The smoke must render RGB565, GRAY8, and ARGB4444 sources through an actual
`MainWindow`/screen draw, not only an offscreen `Image`. It may perform explicit
observer readbacks after draw for verification, but must distinguish those
counters from draw-only behavior.

Require:

- RGB565 selected for opaque color source;
- GRAY8 selected for structural grayscale source;
- ARGB4444 selected for alpha-bearing source;
- direct compact decode count >0 and expected compact bytes;
- temporary full RGBA decode bytes =0;
- draw-only source promotion count =0;
- visible/screen draw completes and observer hashes/quality pass;
- raster writePixels attempts/hits =0;
- raster target-color attempts/materializations/hits =0;
- raster physical-variant lookups/materializations/hits =0;
- raster physical-identity attempts/hits =0;
- draw-only execution does not CPU-readback/materialize merely to attempt a
  software-raster fast path.

Explicit observer row-readback counters are allowed and must be identified as
such. No Android timing or RSS claim is required.

Record device model, Android version, renderer/GLES information, candidate SHA,
APK SHA-256 and `libtcvm.so` SHA-256 when available, concise result fields, and
limitations. Remove the diagnostic app afterward. Do not commit APKs, native
binaries, raw logcat, or full deployment logs.

Any compact-selection/output failure or nonzero software-raster fast-path
activity on Android GPU is a blocker.

### Milestone 9.6 — Freeze Phase 3 and hand off Phase 4

Update existing support files only:

    .agent/state/image-opt-phase3-formats.md
    .agent/evidence/image-opt-phase3-formats.jsonl
    .agent/archive/image-opt-phase3-formats-history.md
    .agent/reports/image-opt-phase3-formats-editorial.md

State must record:

- frozen Phase-2 SHA;
- last runtime correction SHA from 9A plus any 9B fix;
- final harness SHA and adapter digest;
- `PHASE3_RUNTIME_CANDIDATE`;
- Matrix A/B artifact paths and concise median/RSS/quality deltas;
- every 60->200/matched-memory escalation and outcome;
- exact GitHub run ID/URL and candidate SHA;
- Android device/runtime provenance and GPU result;
- hosted Windows/Linux S1/S2/S3 performance as `NOT AVAILABLE`, not failed;
- remaining limitations;
- exact next action: Phase 4 must start from final Phase-3 branch tip.

Update the existing editorial report using the sections required by
`.agent/PLANS.md`, but add only the Milestone-9/final-closeout delta. Do not copy
all old benchmark tables into every support file.

Final direct-artifact commit:

    docs(image): close phase 3 final integration

This commit must change only state/evidence/archive/editorial/direct closeout
artifacts. Do not rerun the full platform matrix solely because this docs-only
commit follows the validated runtime candidate.

After commit:

    PHASE3_FINAL_TIP=$(git rev-parse HEAD)

Do not create another commit merely to write `PHASE3_FINAL_TIP` back into a
tracked file, which would make the recorded SHA stale again. Report both values
in the final executor response:

    PHASE3_RUNTIME_CANDIDATE = source/test SHA validated by CI and Android
    PHASE3_FINAL_TIP         = final docs/evidence branch tip

Phase 4 must branch/rebase from `PHASE3_FINAL_TIP`; runtime provenance remains
`PHASE3_RUNTIME_CANDIDATE`.

Final lightweight checks:

    git status --short
    git diff --check
    git log --oneline --decorate -12
    git merge-base HEAD 6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee

Working tree must be clean except for pre-existing explicitly documented
unrelated paths.

## Surprises & Discoveries

Record only discoveries that change remaining execution. Do not restate
historical Milestone 1-8 detail.

Expected starting facts from 9A:

- runtime correctness defects are fixed;
- explicit `PHASE2_FINAL`/`PHASE3_STORAGE` harness is frozen;
- exact Phase-2 adapter digest is recorded;
- cross-feature/adaptive-JPEG correctness is green.

If any is false, return to 9A rather than patching around it in benchmarks.

## Decision Log

- macOS software Skia is authoritative for performance and small-regression
  decisions.
- Matrix A measures Phase-3 isolated disabled/enabled behavior on final base.
- Matrix B measures integration with real final Phase-2 IDs 0-4,13-15.
- S3 has no required speedup; correctness, compact bytes, zero staging, and zero
  unintended promotion are required.
- GitHub hosted builds validate portability; no hosted performance workflow is
  created in this closeout.
- Android validates production GPU semantics, not software-raster timing.
- Final runtime candidate and final docs tip are intentionally separate SHAs.

## Validation and Acceptance

Mark Phase 3 `FINAL / GO FOR PHASE 4` only if:

- Matrix A S2 has no confirmed >5% median/RSS regression;
- Matrix B S2 has no confirmed >5% median/RSS regression;
- both S3 matrices pass compact selection, actual byte counts, quality, zero
  full-RGBA staging, and zero source promotion;
- all triggered 200-sample/memory diagnostics are resolved under the frozen
  rule;
- existing GitHub platform matrix is green for `PHASE3_RUNTIME_CANDIDATE`;
- physical Android compact GPU smoke passes and all raster-only counters stay
  zero;
- all direct benchmark/evidence/report artifacts are committed and normal build
  artifacts are not;
- final `git diff --check` passes and tree is clean.

Correctness/hash/quality mismatch, stale/duplicate variant evidence, compact
promotion during ordinary source use, nonzero raster-only GPU activity, or a
confirmed >5% S2 regression is a blocker.

## Risks and Open Questions

No architectural choices are delegated to the executor.

Fixed responses:

- runtime fix after any authoritative capture -> invalidate/rerun affected
  comparison;
- Android runtime not built from candidate SHA -> block, do not substitute;
- GitHub CI unavailable -> block final freeze, preserve local benchmark evidence;
- >5% 200-sample RSS -> matched diagnostics, no new threshold;
- new direct artifact exceeds 20 KiB/~600 lines -> split or deterministic gzip
  raw samples; do not shrink existing source files.

## Idempotence and Recovery

Use scenario-specific directories. Never overwrite historical pre-Milestone-9
evidence. For an incomplete uncommitted rerun, remove only artifacts created by
that attempt or move them to `superseded-<reason>/` before the accepted capture.

Use temporary worktrees for exact Phase-2 S1. Never reset the active branch over
local work. Preserve unrelated untracked paths named by existing state.

Follow logical-commits. Do not amend/squash earlier phase history. Normal
fast-forward push is allowed only to obtain required CI; never force-push. Do
not merge PRs, tag, release, or alter branch protection.

## Outcomes & Retrospective

At completion summarize only: Matrix A/B measured results, escalations, exact
runtime candidate, GitHub result, Android GPU result, final tip, limitations,
and explicit `GO/NO-GO` for Phase 4. Keep detailed resolved history in existing
archive and factual handoff in the editorial report.

## Revision Note

2026-09-08: Plan 9B is the measurement/platform half of Milestone 9. It follows
Plan 9A's frozen correctness/harness boundary and exists separately to keep each
new ExecPlan below 20 KiB/~600 lines while remaining resumable.
