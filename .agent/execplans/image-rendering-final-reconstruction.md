<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Complete the image/rendering reconstruction

This ExecPlan follows `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Deliver the final production correction on `fix/image-rendering-final-reconstruction` so image copies route back to Java after a physical-copy miss, eligible opaque 1:1 draws use the existing typed `opaqueWritePixels` policy, and only persistent UI-owned images use immediate final-raster admission. Prove the ownership lifecycle and path behavior, validate the macOS candidate, prepare the Windows evidence runner, and audit the historical feature chain before claiming closure. Do not merge.

## Working Set and Resume Protocol

- `.agent/state/image-rendering-final-reconstruction.md` is the first read when resuming. It records the active milestone, last commit, paths, validation, deferrals, and next command.
- `.agent/evidence/image-rendering-final-reconstruction.md` is append-only and holds compact command results, artifact paths, and hashes. Search it only for the milestone being resumed.
- `.agent/reports/image-rendering-reconstruction-final.md` is the factual final feature-to-commit audit and handoff; synthesize it after production validation.
- Existing `.agent/reports/image-scroll-raster-fast-path.md`, `image-raster-core.md`, `image-raster-variants.md`, `image-compact-storage.md`, `image-lazy-jpeg.md`, `image-async-prefetch.md`, `image-prefetch-worker.md`, `image-png-prefetch.md`, `scroll-raster-reuse.md`, `image-runtime-config.md`, `image-runtime-options.md`, `runtime-diagnostics.md`, `system-nanotime.md`, and `semaphore-v1.md` are source maps for the final history audit. Read targeted sections only after implementation; the pasted task's P12 results are established evidence and must not be rerun.
- Benchmark scripts and candidate output live in the sibling `totalcross-performance-lab` checkout. Its existing branch is `perf/image-rendering-benchmarks`; preserve its history and only change it if runner compatibility requires a minimal separately committed fix.
- The clean implementation checkout is `/Users/flsobral/repos/totalcross-image-rendering-final-reconstruction`; the original dirty checkout `/Users/flsobral/repos/totalcross-image-scroll-raster-fast-path` is intentionally outside the work set.

Resume by reading state, checking the active branch and scoped diff, then inspecting only the current milestone's paths. Do not replay P12 probes or historical commit chains.

## Progress

- [x] (2026-10-03) Fetched `origin`; remote master remained `5a44f503bf6fa1bec350f1218f4d501a70fc4812`, which contains the requested base. Created a clean worktree and branch from that exact SHA; left the dirty checkout untouched.
- [x] (2026-10-03) Corrected `copyRect(GfxSurface)` so only a successful direct physical copy handles the native probe; misses return to Java. Focused SDK and native Skia surface tests passed; commit `3be3a1eaf`.
- [x] (2026-10-03) Reconnected the typed `opaqueWritePixels` policy through deferred geometry and NativeImageBacking draws. Native writes require proven opacity, compatible RGBA/BGRA raster pixmaps, exact integral device sizing, safe rectangular clipping, non-overlapping storage, and a successful write; rejected writes use the existing draw fallback. Added attempt/hit/fallback diagnostics and focused parity cases; commit `f45b0a1f9`.
- [x] Reconnect typed opaque 1:1 writePixels path and focused coverage; commit independently (`f45b0a1f9`).
- [x] (2026-10-03) Added package-private `SECOND_OBSERVATION`/`IMMEDIATE` final-raster admission. Generic resolution still waits for the second observation and immediate mode keeps the one exact-scale slot; commit `9896ec9ad`.
- [x] (2026-10-03) Added shared persistent ownership to attached `ImageControl`s, including replacement, reparent, detach, ScrollContainer bag/removal, last-owner cleanup, mutation transfer, and lazy weak-owner cleanup without changing `Image`'s native field layout; commit `c7bf28d09`.
- [x] (2026-10-03) Completed M5 integration: the 317-test SDK regression selection passed (20 skipped), SDK distribution passed, diagnostics remained OFF, Skia surface assertions passed, eight deployed Skia image smokes passed, and the legacy macOS native scroll smoke passed with primitive verification. Follow-up commit `2f81accfb` replaced an unsupported weak-reference queue constructor and split registry methods so `tc.Deploy` succeeds.
- [x] (2026-10-04) Measured the production candidate with the static-warm draw-path probe and 10-round deterministic scroll run. Static path counters show 18/18 cached-final hits and zero generic/smooth draws. The existing generic-admission regression confirms first-use does not populate the final-raster slot. The diagnostics-off runtime does not expose repeated-scroll lifecycle counters, materialization time, writePixels counters, or process memory; these remain explicitly unmeasured. See the M6 evidence record.
- [ ] Prepare the PowerShell-only Windows x64 package after macOS validation; no local Windows build.
- [ ] Audit required production features against merged PRs/commits, write the final report, commit and push the branch, and provide PR-ready summary.

## Current Architecture and Scope

`Graphics.copyImageRect(Image, ...)` now routes a deferred miss back to Java after a physical-only probe. Generic `Image.resolveForDrawing(scale)` keeps second-observation admission; a persistent drawing request can admit its first successful materialization into the same single exact-scale slot. `ImageControl` registers the distinct images it retains while attached, and the `Container` parent-change hook follows add/remove/reparent while ScrollContainer removal covers both its clipped bag and floating children. A weak owner registry clears pipeline ownership after a collected control is noticed at a later final-raster access. Preparation/adoption and native `TARGET_COLOR`/`PHYSICAL` variants remain separate and unchanged. The typed opaque-write policy continues through JavaSE `Graphics.setRGB`, native `NativeImageBacking`, and deferred-plan drawing.

Only image/rendering/scroll reconstruction code and required tests, benchmark-lab compatibility, package artifacts, and final audit documents are in scope. No public API or raw optimization mask is intended.

## Plan of Work

1. **Copy routing.** Probe an exact cached final raster first. On miss, request a deferred plan and run only a physical/direct-copy native attempt. A miss or ineligible physical variant returns to Java, which resolves through the existing path and copies the final raster. Preserve JavaSE behavior and geometry for ordinary draw and materialization. Add counter/path and pixel-parity tests. Validate focused tests and `git diff --check`, then commit.
2. **Typed opaque write.** Reconnect the current NativeImageBacking draw route to the existing typed option. Use a conservative eligibility predicate for software/Raster Skia, valid backing/format/source rectangle, proven opacity, compatible alpha semantics, integral destination, exact device 1:1 size, safe clipping, readable pixels, and successful target write. Fall back to canvas drawing for every failed condition. Cover hit, clipping, all specified fallback classes, and parity. Commit separately.
3. **Admission modes.** Add a package-private typed choice with `SECOND_OBSERVATION` for generic `resolveForDrawing` and `IMMEDIATE` only for a deterministic persistent UI owner. Keep one cache slot and exact scale/generation keys. Keep async preparation immediate and leave target-color/physical second-use untouched. Add isolated tests and commit.
4. **Persistent UI ownership.** After tracing `Control`/`Container` attach, remove, and lifecycle hooks, implement the smallest deterministic retained-owner contract. Multiple controls sharing an `Image` must be reference-safe. Image replacement, transfer, detach/removal, and disposal release exactly their ownership. Persistent rendering chooses immediate admission only while the owner is registered. Mutations and scale changes retain established invalidation. Test first-paint admission, later hits, generic transient behavior, replacement, detach, sharing, last-owner release, mutation, and key changes. Commit independently.
5. **Integration.** Run the focused Image/ImagePipeline, scale, mutation, ImageControl lifecycle, copyRect, compact-storage, lazy-JPEG, variants, async/PNG preparation, scroll reuse, runtime-configuration, artifact/API-boundary, and native Skia backing/surface tests. Run the SDK and only macOS `tcvm` and Launcher builds/smokes that are available and directly exercise the changed path. Diagnostics OFF remains baseline. Do not build Windows, Android, Linux, iOS, or WinCE. Commit any logically separate regression coverage.
6. **Candidate benchmark.** Use existing production-candidate support from benchmark commit `5b4902108c73842db54287f2b8d0dcec76a6573c` on `perf/image-rendering-benchmarks`; preserve the later `264b9fba8581f08f3c0e4f31529831b7270d2c95` history. Measure only static warm paint, deterministic repeated scroll, and a controlled transient/one-shot case. Check counters and memory where exposed; record diagnostics-off fields the runtime does not expose as unavailable. Do not rerun named P12 probes or broad matrices. Keep raw data out of the TotalCross commit and index it once.
7. **Windows package.** After macOS validation passes, use the existing package workflow to produce the PowerShell-only x64 runner/package for this production candidate. Record TotalCross commit, hashes, configuration, invocation, and expected evidence. Mark Windows results pending unless execution evidence exists.
8. **Historical audit and delivery.** Map every requested behavior to the responsible merged PR/commit or this branch and classify it using the requested labels. Explain superseded draft PRs #457–#462 without closing them. Any unexplained `STILL MISSING` production behavior blocks declaring reconstruction complete. Commit the report, push the branch, do not merge, and prepare a concise PR-ready summary.

## Decision Log

- Decision: Use a clean worktree based on the fetched `origin/master`, leaving dirty sibling checkouts untouched. Rationale: the requested starting-state cleanliness condition cannot be met safely in the original dirty checkout. Date: 2026-10-03.
- Decision: Generic resolution remains second-observation; only an explicit live persistent UI owner may request immediate final-raster admission. Rationale: established P12 evidence shows persistent scrolling gets no retained-cache benefit from second-use while one-shot traversal would retain an additional 272,220,336 bytes under universal immediate admission. Date: 2026-10-03.
- Decision: Preserve second-observation admission for `TARGET_COLOR` and `PHYSICAL`. Rationale: the task explicitly separates native speculative variants from final-raster policy. Date: 2026-10-03.
- Decision: Track owner controls through weak references and keep the materialized raster on its existing pipeline slot. Rationale: the `Image` Java fields have hard-coded native offsets, shared images need reference-safe counts, and collected owners should not pin a final raster. Date: 2026-10-03.
- Decision: Cache unknown-opacity proofs only for the backing generation that was scanned and report typed write attempts, hits, and fallbacks separately. Rationale: mutations invalidate the proof, and diagnostics distinguish rejected/failed writes from ordinary drawing. Date: 2026-10-03.

## Validation and Acceptance

- Level 2 for each functional commit: focused affected tests, pixel parity where applicable, and `git diff --check`.
- Level 3 for final image-operation integration: relevant focused suite families, SDK build, macOS native `tcvm` and Launcher validation/smokes, plus candidate path counters. Use quiet wrappers and task-specific logs; do not run `clean` by default.
- Candidate measurements must include the requested static warm, repeated-scroll, and controlled transient behavior. A timing-only result is insufficient. Report available path counts, workload/paint/materialization time, >100 ms stalls, and live/peak derived raster bytes; identify unsupported fields rather than infer them.
- Final acceptance additionally requires the audit to contain no unexplained `STILL MISSING`, one final-raster slot per pipeline, explicit owner cleanup, unchanged native speculative admission, Windows package evidence or a clearly pending Windows execution, clean diff checks, ordered logical commits, and pushed HEAD.
- Do not run disallowed platform builds or the completed P12 causal probes. Record any unavailable macOS dependency/smoke or Windows execution as a limitation rather than implying success.

## Risks and Open Questions

- Persistent ownership hooks must follow actual attachment and disposal semantics; scroll visibility or timing must not determine admission.
- `Container` may support reparenting and `ScrollContainer` may cull without detaching; ownership must follow actual persistent attachment semantics, not visibility heuristics.
- Image control ownership may outlive a particular draw through shared images; release must be reference-safe and avoid eagerly clearing a cache still owned elsewhere.
- Benchmark candidate support, macOS native dependencies, and Windows package availability are environmental and must be verified from repository scripts before invoking expensive work.
- The final feature audit must use existing reconstruction reports and merged PR history rather than replaying old branches.

## Idempotence and Recovery

The requested branch is isolated in a dedicated worktree. Never reset or clean dirty sibling worktrees. Keep native/generated build output, benchmark samples, and Windows packages out of Git unless the established package workflow intentionally tracks a small validation artifact. Each milestone has a separate commit so it can be reviewed or resumed independently. Before pushing, fetch and inspect branch divergence; never force-push or merge.

## Outcomes & Retrospective

Pending implementation. P12 findings are accepted input, not work to repeat. See the state and evidence index for the latest completed slice and validation.
