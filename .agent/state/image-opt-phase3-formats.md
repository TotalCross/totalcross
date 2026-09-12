<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image optimization phase 3 state

Updated: 2026-09-08T02:27:33-03:00
Branch: `perf/image-opt-phase3-formats`
Phase-2 parent SHA: `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`
Plans: `.agent/plans/exec-plan-image-opt-phase3-milestone9a.md`,
`.agent/plans/exec-plan-image-opt-phase3-milestone9b.md`

## Active milestone

Milestone 9B — authoritative matrices, platform closeout, and final
integration are complete. `PHASE3_RUNTIME_CANDIDATE` is
`d2f8195b9faf828f4ee04154c93a93e1136c5bd4`; only documentation closeout
remains before the final Phase-3 tip is frozen.

The previous Milestone-8 rebase and benchmark handoff are historical and remain
unchanged. No authoritative Milestone-9 samples have been captured.

## Progress

- [x] Verified branch, frozen Phase-2 remote SHA, merge-base, expected tip, and
      scoped diff safety for Milestone 9A.
- [x] Read and activated the split Milestone-9A/9B plans.
- [x] Correct compact final-buffer accounting and ARGB4444 opacity semantics.
- [x] Freeze explicit final-stack harness and exact-base adapter.
- [x] Add cross-feature, observer, writePixels, and adaptive-JPEG correctness.
- [x] Run focused build/smoke validation and hand off to Plan 9B.
- [x] Correct combined target-color dispatch and complete the corrective 9A
      gate without capturing authoritative samples.
- [x] Capture authoritative Matrix A and Matrix B S1/S2/S3 evidence with the
      frozen exact-base adapter and complete the required 200-sample RSS gate.
- [x] Validate the exact runtime candidate with the existing GitHub matrix and
      physical Android GPU compact-format smoke.
- [x] Reconcile the final Phase-3 state, evidence, history, and editorial
      handoff; leave Phase 4 unstarted.

## Exact-base rebase handoff

On 2026-09-07, the complete Phase-3 sequence was rebased from old HEAD
`9d5c6133318f97baeb88d382cb7837c3f585f122` onto the frozen Phase-2 tip
`6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`, which was verified as
`origin/perf/image-opt-phase2-raster`. The rebased Phase-3 implementation HEAD
is `15ab7d72e28f860f67106796bdd4cd7329075e56`; all 21 Phase-3 commits retain
their original order and subjects. Conflicts were limited to the SDK build
registration and the Java/native compact-backing integration; finalized
Phase-2 raster architecture was retained and only compact-format changes were
replayed.

Post-rebase focused `totalcross.ui.image.*` tests, SDK `dist -x test`, the
Release macOS software-Skia CMake/Ninja build, and
`runImageCompactFormatsSmokeMacOS` passed. No Phase-3 benchmarks were rerun.

## Last completed slice

The pre-Milestone-9 rebase handoff is committed through
`604d7bb3bdba05e70699b648c59e093fb1c330e3`. Milestone 9A runtime corrections
are `e9955ee6a75ab3d4153bd1a87ded1c955723bfab` and the last runtime correction
is `d5f40104119d56c64a633388ba17bb8673f128f3`. The frozen harness commit is
`fe3fe963a40e839f3a5c513f39b9c9bc46683937`; the adapter source-revision fix
is `c95c0cf2674c73a046fe2f78c7310394f0a9970f`; the final compact-smoke test
correction is `a6e23f73011c5457b8d6e9cbc69be9bcaed1428b`.
The previous adapted harness revision is
`a6e23f73011c5457b8d6e9cbc69be9bcaed1428b`, with deterministic adapter digest
`1fb32deccbb0c1d79c041e8f25af6d7bd1a3e6da4536fa3c4ddfb521aa08a158`.
The corrected final-workload overlay used for the authoritative true-base RSS
recheck has digest
`edd9a79ebb30d081681b141a245a8525788b74df4a55712113ecf7a9c2335d7d`.
RGB565 S1 is captured under `.agent/benchmarks/image-opt-phase3-formats/rgb565/s1/`
on exact pre-rebase runtime `86bfeafe388ce866236c3ae58eecb144664895e2`.
Compact backing implementation checkpoint from the prior documented rebase
is historical (`5dbadf3bb7ea44ee14472ba25ab2b27b2fb3a2b9`); final benchmark-control corrections
are `9443ca28e55c3f69859602097b0db8a815e6cd6e`,
`2f3d692d4ba2c015c46422dc2e1a6ca7e6d30ed7`, and
`91b277c10ef5ad7ed622d556a01c6a56ff4ec910`. The corrective runtime commit is
`a212f76e64c5415af8d6a3843e87ce82030db94e` and the correctness matrix commit is
`4e52067003b899566de67f01a9f50000326cbd98`.

## Active paths

- `.agent/plans/exec-plan-image-opt-phase3-milestone9a.md`
- `.agent/plans/exec-plan-image-opt-phase3-milestone9b.md`
- `.agent/state/image-opt-phase3-formats.md`
- `.agent/evidence/image-opt-phase3-formats.jsonl`
- `.agent/archive/image-opt-phase3-formats-history.md`
- `.agent/reports/image-opt-phase3-formats-editorial.md`
- `.agent/benchmarks/image-opt-phase3-formats/`

Milestone-9A runtime/test paths are:

- `TotalCrossVM/third_party/jpeg/JpegLoader.c`
- `TotalCrossVM/third_party/png/PngLoader.c`
- `TotalCrossVM/src/nm/ui/skia/skia_image_backing.cpp`
- existing image benchmark/smoke support under
  `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/`
- `.agent/benchmarks/image-opt-phase3-formats/milestone9/true-base-harness/`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsSmokeApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsBenchmarkApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsBenchmarkSupport.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsFinalStackSmokeApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsAdaptiveJpegSmokeApp.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageCompactFormatsNativeHooks.java`
- `TotalCrossSDK/src/smokeTest/java/totalcross/ui/image/ImageRasterPhysicalVariantSmokeApp.java`
- `TotalCrossSDK/build.gradle`

## S1/S2/S3 status

- Exact pre-item S1 runtime SHA (historical pre-rebase evidence):
  `86bfeafe388ce866236c3ae58eecb144664895e2`.
- Exact true-base dylib SHA-256: `32926d24c475ca3b6f04134ce4d6556c37d926862d6877d122cdec517213c4ca`.
- Rebased Phase-3 implementation HEAD: `15ab7d72e28f860f67106796bdd4cd7329075e56`.
- Milestone-9A validation dylib SHA-256:
  `6bb984f08349990e169f67d8fc8dc234afc93c19ad347a8c5caaf6acfe231de4`.
- Final dylib SHA-256: `2864d0ee3ace6d52729bcaccad727902088327caa2bafe0769e66cbc2c0a9caa`.
- Harness digest: `9a0bc2a348b197f597a11f10b2ca9d5787ab7b0f2937c70a643e4fc6f09018ae`.
- Prior documented implementation checkpoint (historical):
  `5dbadf3bb7ea44ee14472ba25ab2b27b2fb3a2b9`.
- RGB565: exact S1 plus 60-sample S2/S3 complete; S2 median 63 ms vs 62 ms,
  S3 selects RGB565 and uses 1,048,576 compact bytes.
- GRAY8: exact-base S1 and matched final-harness S1 preserved; S2/S3 complete,
  S3 selects GRAY8, uses one-byte storage, and has zero temporary RGBA decode
  bytes.
- ARGB4444: exact-base S1 and matched final-harness S1 preserved; S2/S3
  complete, S3 model error is 0 and black/white composite max error is 16.
- Promotion matrix: matched-control S1/S2/S3 complete; S3 selects all three
  compact formats and promotes each exactly once per repetition.
- Combined matrices: exact-base and matched-control S1s are preserved; final
  60-sample matrices after the row-conversion correction report disabled-stack
  S1/S2/S3 medians 263/263/184 ms and full-stack medians 160/161/162 ms.
  Both S3 matrices use `RGB565|RGB565|GRAY8|GRAY8|ARGB4444`, zero compact-source
  promotions, and zero temporary RGBA decode bytes. Full-stack S3 improved from
  the previous 169 ms median to 162 ms.
- Milestone-9A dry runs used the final workload names
  `milestone9-isolated` and `milestone9-full-stack`. Both ran 3 samples with
  identical fixture hashes and samples above 30 ms; the final full-stack run
  was 163/163/164 ms and the isolated run was 185/186/186 ms. These are
  harness-freeze checks only, not authoritative Milestone-9 measurements.
  Isolated log: `TotalCrossSDK/agent-logs/20260908-001230-runImageCompactFormatsBenchmarkMacOS-full.log`;
  full-stack log: `TotalCrossSDK/agent-logs/20260908-001251-runImageCompactFormatsBenchmarkMacOS-full.log`.

## Corrective 200-sample RSS gate

- ARGB4444: 127088 -> 139456 KiB (+9.7%); required sample-100/150 `vmmap -summary`
  and `ps` captures are under `argb4444/rss-200/diagnostics/`. S2 peak physical
  footprint was 81.3M vs S1 84.4M; the signal is recorded as unconfirmed
  allocator-residency variation.
- Promotion: 153216 -> 153744 KiB (+0.3%).
- Combined-disabled: 147904 -> 151504 KiB (+2.4%).
- Combined-enabled full stack: 157568 -> 152496 KiB (-3.2%).

The authoritative ARGB4444 three-pair recheck is under
`argb4444/rss-200-corrective-3pairs-matched-harness/` and used order
S1->S2, S2->S1, S1->S2. S2 peak-RSS deltas were -0.7%, +7.1%, and -4.3%;
matched S2 peak physical-footprint deltas were -4.9%, -2.6%, and +2.0%.
Checkpoint physical/private writable residency changed with run order. The
frozen rule rejects a reproducible disabled-path regression; runtime source was
not changed.

## Corrective Milestone-9A closeout

- Final branch tip after corrective test/harness work:
  `ffe681fe811a840d4ba6db2e2b696cf24ff0aeba`.
- Last runtime correction:
  `8b455f2ad fix(image): preserve combined target-color coverage`.
- Active Phase-3 Release macOS software-Skia dylib SHA-256:
  `e62560c60b3cac312206ceae4e3eff4f3d95cfc92edb6621cd9327d82a4353f6`.
- Final test/harness commit:
  `ffe681fe811a840d4ba6db2e2b696cf24ff0aeba`.
- Exact-base adapter source revision:
  `ffe681fe811a840d4ba6db2e2b696cf24ff0aeba`.
- Exact-base native runtime revision:
  `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`.
- Exact-base native runtime SHA-256:
  `aee430fd3846476ebaaeb6769b51039d9164999a05907d3dddec81b115fdae8f`.
- Exact-base adapter digest:
  `f2292c73893fb1568c3ba5e56803f4fb0b37d3ff5b7f193f9a992a3e91472f42`.
- Exact-base flags were Release, software graphics, Skia renderer, and SDL
  windowing. The adapter used a detached physical depot-tools checkout and
  did not accept or copy the active Phase-3 dylib.
- Exact-base 3-sample `milestone9-full-stack` dry run passed with stable input
  hashes, stable output hash `0000915C00006078`, and elapsed samples
  `163/161/162` ms. Quality/hash work remained outside the timed batch.
- Compact BGRA target-color/physical smoke passed with
  `lookups=3,misses=2,materializations=1,hits=1,bytes=160000` and pixel hash
  `00005CB800005F7C`. The RGB565-target control passed with 80,000 bytes.
- Legacy combined BGRA smoke passed its original target-color fallback
  invariant and physical counters `3/2/1/1/160000`, pixel hash
  `000000D600000165`.
- Normal lazy adaptive-JPEG smoke passed RGB565 and GRAY8 at denominators
  `2/4/8`, with source logical dimensions retained, cached compact backing
  identity reused, one targeted decode, zero temporary RGBA bytes, zero
  promotion, and independent full-decode pixel parity.
- No authoritative Milestone-9 S1/S2/S3 samples were captured.

## Milestone-9B final closeout

- Frozen Phase-2 base: `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`.
- Last 9A runtime correction: `8b455f2ad`. No 9B runtime fix was required.
- Final Android-aware smoke harness: `d2f8195b9faf828f4ee04154c93a93e1136c5bd4`.
- Exact-base adapter digest: `f2292c73893fb1568c3ba5e56803f4fb0b37d3ff5b7f193f9a992a3e91472f42`.
- `PHASE3_RUNTIME_CANDIDATE`:
  `d2f8195b9faf828f4ee04154c93a93e1136c5bd4`.

Matrix A (`.agent/benchmarks/image-opt-phase3-formats/milestone9/isolated/`)
passed 60 samples for S1/S2/S3 with medians `266/264/184` ms and peak RSS
`150912/152128/139840` KiB. S2 deltas were `-0.75%` elapsed and `+0.81%`
RSS. S3 selected `RGB565|RGB565|GRAY8|GRAY8|ARGB4444`, with model max error
1, black/white composite max error 16, zero promotions, and zero temporary
RGBA decode bytes.

Matrix B (`.agent/benchmarks/image-opt-phase3-formats/milestone9/full-stack/`)
passed 60 samples with medians `162/159/163` ms and peak RSS
`141792/155472/155840` KiB. The first S2 delta was `-1.85%` elapsed and
`+9.65%` RSS, so the frozen 200-sample escalation ran under
`full-stack/escalation-200/`: medians `162/161` ms, peak RSS
`141408/153568` KiB, deltas `-0.62%` elapsed and `+8.60%` RSS. Matched
`vmmap -summary`/`ps` checkpoints at samples 100 and 150 are under
`full-stack/matched-diagnostics/`; physical and writable residency did not
reproduce monotonic private growth, so the signal is unconfirmed
allocator/residency variation and no runtime change was made.

The existing PR workflow passed at exact candidate head in run
`34190415679`:
`https://github.com/TotalCross/totalcross/actions/runs/34190415679`.
SDK, macOS ARM64, Android, iOS archive, Windows, Windows native/legacy, Linux
amd64, and native Linux ARM64 all passed; the intentionally disabled Linux
arm32 cross job was skipped.

Physical Android provenance: Xiaomi `2312DRA50G`, Android 13,
MIUI `V14.0.9.0.TNRMIXM`, Qualcomm Adreno (TM) 710, OpenGL ES 3.2,
driver `V@0615.73`. The candidate APK SHA-256 was
`b1a55bb8faaed5eb702dad94ba82b2dfe66466803bce8d02086d12fd2a1ca20c`, and
its arm64 `libtcvm.so` SHA-256 was
`60c8d16fd2582ef1be4cf51fccb65fbdd815032f2a345fb9a10b86003fa80923`.
The smoke reported compact direct decodes `5`, compact bytes `2097152`,
observer hashes stable, quality `max=32/rmse=8.056096554759305`, visible
screen draw complete, and all raster/promotion/temp-RGBA counters zero. The
diagnostic package was removed after the run.

Hosted Windows/Linux S1/S2/S3 performance is `NOT AVAILABLE`, not failed.
Remaining limitations are that S3 has no speedup requirement, live compact
byte counters are GC-sensitive, Android supplies correctness/GPU evidence but
no timing or RSS claim, and the final docs tip will differ from the runtime
candidate. Phase 4 must start from that final Phase-3 branch tip; it has not
started.

## Validation

Milestone-9 bootstrap checks passed: branch and SHA identity match the plan,
merge-base is exact, and scoped `git diff --check` is clean. The prior
post-rebase native rebuild, SDK distribution, compact smoke, decode/promotion
failure-retry checks, parity checks, matrices, RSS gate, and focused SDK tests
remain historical evidence from the pre-9A handoff.

Milestone-9A focused validation passed:

- `./gradlew-agent test --tests 'totalcross.ui.image.*' --no-daemon --console=plain`
  passed; full log `TotalCrossSDK/agent-logs/20260908-005617-test-full.log`.
- `./gradlew-agent dist -x test --console=plain` passed; full log
  `TotalCrossSDK/agent-logs/20260908-005625-dist-full.log`.
- `ninja -C build/image-opt-phase1-corrective-macos` passed for the Release
  macOS software-Skia build; log
  `/tmp/image-opt-phase3-9a-final-release-ninja.log`.
- `runImageCompactFormatsSmokeMacOS` passed with all retry/parity/opacity checks;
  full log
  `TotalCrossSDK/agent-logs/20260908-005645-runImageCompactFormatsSmokeMacOS-full.log`.
- `runImageCompactFormatsFinalStackSmokeMacOS` passed identity, variant,
  target-color composition, invalidation, observers, writePixels, and adaptive
  JPEG; full log
  `TotalCrossSDK/agent-logs/20260908-005655-runImageCompactFormatsFinalStackSmokeMacOS-full.log`.
- `runImageRasterPhysicalVariantSmokeMacOS --case=compact-bgra-combined`
  passed; full log
  `TotalCrossSDK/agent-logs/20260908-005705-runImageRasterPhysicalVariantSmokeMacOS-full.log`.
- `runImageRasterPhysicalVariantSmokeMacOS --case=combined` passed the original
  combined BGRA invariant; full log
  `TotalCrossSDK/agent-logs/20260908-005714-runImageRasterPhysicalVariantSmokeMacOS-full.log`.
- `runImageRasterPhysicalVariantSmokeMacOS --case=compact-combined` passed the
  RGB565-target control; full log
  `TotalCrossSDK/agent-logs/20260908-005740-runImageRasterPhysicalVariantSmokeMacOS-full.log`.
- `runImageCompactFormatsAdaptiveJpegSmokeMacOS` passed the real lazy pipeline;
  full log
  `TotalCrossSDK/agent-logs/20260908-005725-runImageCompactFormatsAdaptiveJpegSmokeMacOS-full.log`.
- The exact-base adapter used frozen SHA
  `6d1c95f77fcb9c74d19b4e9393dba7c82cd37aee`; preparation output is in
  `/tmp/image-opt-phase3-9a-final-adapter.log`, exact-base build logs are under
  `/private/tmp/image-opt-phase3-m9-exact-base-build-final/`, and the 3-sample
  full run log is
  `/private/tmp/image-opt-phase3-m9-base-final/TotalCrossSDK/agent-logs/20260908-005555-runImageCompactFormatsBenchmarkMacOS-full.log`.
- Final source headers and `git diff --check` passed. No authoritative
  Milestone-9 samples were captured before the harness freeze.

Milestone-9B validation passed:

- Fresh exact-base adapter/build and current Release software-Skia/SDL native
  build; native hashes are preserved in the evidence index.
- Active and exact-base SDK distributions, six 3-sample dry runs, six 60-sample
  captures, Matrix B 200-sample escalation, and matched `vmmap`/`ps` diagnostics.
- Artifact validator: `2` matrices, `6` scenarios, `2` escalation runs, and
  `23` JSONL evidence records parsed successfully.
- Updated macOS final-stack screen-draw smoke passed.
- Android release dependency fetch/build, APK deployment, physical GPU smoke,
  and diagnostic-app removal passed without defining `TC_GRAPHICS_SOFTWARE`.
- Existing GitHub PR workflow run `34190415679` passed at the exact candidate
  SHA. Focused headers and `git diff --check` passed.

## Deferred validation

Hosted Windows/Linux performance remains unavailable by plan; Android GPU
correctness and the existing iOS/Windows/Linux build jobs are complete for this
milestone. No hosted benchmark performance workflow was added.

## Decisions still active

- Compact formats are internal, opt-in, and disabled by default.
- Precedence is GRAY8 > RGB565 > ARGB4444 > RGBA8888.
- Compact formats are source-only; mutable/full-precision barriers promote
  transactionally to RGBA8888.
- The Phase-2 true-base adapter and final candidate provenance are recorded in
  the evidence index; the final docs tip is intentionally separate.
- Unrelated generated/untracked files remain untouched.

## Blockers and deliberate out-of-scope files

No Phase-3 blocker. Existing unrelated untracked paths are deliberately out of
scope: `TotalCrossVM/deps/wince-deps/` and `TotalCrossVM/xcode/generated/`.

## Next exact command

Create the final docs-only closeout commit
`docs(image): close phase 3 final integration`, then record its SHA as
`PHASE3_FINAL_TIP`. Phase 4 must branch or rebase from that tip while retaining
`d2f8195b9faf828f4ee04154c93a93e1136c5bd4` as runtime provenance.

## Successor lazy-JPEG milestone handoff

The successor branch `perf/image-jpeg-factories-lazy` retains the Phase-3 tip
`224682b15a244201718701ad24be8f9d54b3fa71` and candidate
`d2f8195b9faf828f4ee04154c93a93e1136c5bd4`. Its runtime candidate is
`4ce4c74d2d6310ff0eb603546c81da28ebf1bc9f`, with lazy JPEG tests and smokes
recorded in the successor evidence. Phase 4 is explicitly NO-GO until the final
candidate has the hosted cross-platform matrix and an installable Android GPU
smoke.
