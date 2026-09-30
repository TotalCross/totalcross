<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image reconstruction integration and stacking audit

Baseline: freshly fetched `origin/master` at
`7d50c12c0675fbef13e74c54e59b704c04c08050` (2026-09-30). This audit uses the
completed C/D/E/G/H/I documents from their analysis branches, cross-checks P1's
public contract against J, then checks their assumptions against that tree.

| Evidence | Branch / snapshot |
|---|---|
| C rendering map | `analysis/image-rendering-reconstruction-map` / `a58a8f47` |
| D diagnostics inventory | `analysis/runtime-diagnostics-inventory` / `1a060380` |
| E tooling audit | `analysis/performance-tooling-audit` / `02d9e2e7` |
| G raster specification | `analysis/image-raster-reconstruction-spec` / `9c025a22` |
| H prefetch specification | `analysis/image-prefetch-reconstruction-spec` / `f1485b62` |
| I scroll/pacing specification | `analysis/scroll-pacing-reconstruction-spec` / `6101098a` |
| J configuration migration (P1 API cross-check) | `analysis/image-runtime-config-spec` / `3d771b66` |
The numbered PRs below are future owners; historical branch ancestry is evidence
only.

## 1. Verified current-master baseline

| Assumption / component | Current owner and status on `origin/master` |
|---|---|
| `ImagePipeline` and deferred operations | Present, internal: `TotalCrossSDK/src/main/java/totalcross/ui/image/ImagePipeline.java`; `Image.java` builds deferred scale/color/frame/crop operations and caches draw/materialized results. This is an existing substrate, not a class P2/P5 should blindly recreate. |
| `EncodedImageSource` | Present, internal: `.../totalcross/ui/image/EncodedImageSource.java`; captures bytes/path/stream and metadata. JPEG factories already capture this source, but currently materialize before returning. |
| `ImageBacking` / `NativeImageBacking` | Present, internal: `.../ImageBacking.java`, `.../NativeImageBacking.java`; native handle, snapshot, readback, scale and mutation operations exist. Native backing accounting and failure hooks are test-named but currently registered in the normal native method catalogs; future work must move or gate them as test support. |
| `ImageDrawPlan` / `ImageDrawingBridge` | Present: package-private `ImageDrawPlan.java`; `ImageDrawingBridge.java` is `public` but `@hidden`, deprecated and documented unsupported/internal. Do not expand the application API through this bridge. |
| Deferred draw/native Skia support | Present in `Image.java`, `totalcross/ui/gfx/Graphics.java`, `TotalCrossVM/src/nm/ui/image_Image.c`, `image_NativeImageBacking.c`, and `nm/ui/skia/skia_image_{backing,geometry,color}.cpp` plus their internal headers. `Graphics` already has private `drawGeometryNative` and `copyGeometryNative` plan bridges. |
| `ScrollContainer`, `ClippedContainer`, `ImageControl`, `Flick` | Present at `TotalCrossSDK/src/main/java/totalcross/ui/{ScrollContainer,ClippedContainer,ImageControl,Flick}.java`. They provide ordinary scrolling, clipping, image painting and TimerEvent flick behavior. They do not provide async preparation, raster reuse, or P11 timing aggregates. |
| `System.nanoTime()` | Present as `java.lang.System.nanoTime()`, compatibility source `jdkcompat/lang/System4D.java`, native implementation `TotalCrossVM/src/nm/lang/System.c`, and normal native registration. Use it; do not add another clock API. |
| Semaphore V1 | Present: `jdkcompat/util/concurrent/Semaphore4D.java` and existing native sync implementation/registrations. P9 should use the supported `java.util.concurrent.Semaphore` surface; do not duplicate Semaphore core or its stress suite. |
| Standard streams | `System.out` and `System.err` are backed by `totalcross.sys.VmStandardOutputStream` and `TotalCrossVM/src/nm/sys/VmStandardOutputStream.c`. No VM-owned `VmStandardInputStream` bridge was found; P11/P12 only need output/error. |
| Runtime configuration / diagnostics foundations | `RuntimeConfiguration` and `RuntimeDiagnostics` are absent from this master tree. The `feat/runtime-configuration-api` remote ref and separate `feat/runtime-diagnostics` work are prerequisites, not current-master APIs. Merge both foundations before P1; do not copy their feature branches into this audit branch. |
| Image-specific configuration and metrics | No typed image configuration, `ImageOptimizationSettings`, `RenderingOptimizations`, `SCROLL_RASTER_REUSE`, `ImagePreparation`, feature metric groups, or diagnostic group gates exist in current master. No public integer optimization mask is present. |
| JPEG factory behavior | `Image.getJpegBestFit` and `getJpegScaled` retain public Java signatures but call `materializeCanonicalChecked()`. Both are also registered as public native replacements in `TotalCrossVM/src/nm/NativeMethods.txt` and implemented in `nm/ui/image_Image.c`. P5's deferred behavior is therefore still missing on deployed targets unless those replacements are removed or changed to preserve a deferred source. |
| Raster variants / clip fast path / scroll reuse / prefetch | The generic draw plan and two destination-scale cache slots exist. They are not the P3 one-slot physical/target-color variant, P6's visible-physical-subrect three-way path, P7's framebuffer row reuse, or P8's queue. No `ImagePreparation`, `ScrollContainer.prepareForDisplay`, or `scrollRasterRegion` product path is present. |

Current-master presence means “integrate with and verify this owner,” not “the
full future PR contract is already satisfied.” In particular, the two scale
cache slots in `ImagePipeline` are not a physical color-variant cache.

## 2. Assumptions corrected from the reconstruction specs

- **P2/P5:** C placed P5 after P2 and P4. H's inspected baseline shows that P5
  can be built on the already-present `ImagePipeline` and `EncodedImageSource`;
  compact formats and P2 backing work are not semantic prerequisites. P2/P5
  can be implemented in parallel after P1 under a shared ownership contract.
  Their edits to `Image.java`, `ImagePipeline.java`, and `EncodedImageSource.java`
  still need a deliberate rebase/merge order.
- **P5 deployed path:** keeping the Java body alone will not make JPEG factories
  lazy while the current public native replacements remain registered. Keep
  both public descriptors, but remove those direct factory replacements or make
  them construct the same deferred policy-bearing source. Reuse existing source
  capture/decode machinery; do not add another public factory.
- **P7/P6:** C stacked P7 after P6. The I row-move/repaint path uses existing
  `ScrollContainer` and screen-surface operations and has no call dependency on
  P6's image subrect planner. P7 needs P1's default-off effective option, but
  can be developed independently of P6. `Graphics.java` and native registration
  overlap make merge order useful, not a hard architecture dependency.
- **P10/P9:** P10's static PNG candidate uses P8's existing queue/adoption
  contract; the Semaphore worker is an alternate queue scheduler. P9 is not a
  semantic prerequisite for PNG, though P9 should merge first if both change
  `ImagePreparation.java` so P10 lands on the final queue lifecycle.
- **P11/P10:** C's P10 integrated-workload base is optional for P11's Flick
  advancement and aggregate timers. P11 can proceed on `System.nanoTime()` and
  F's diagnostics contract; P10 is only needed for a combined PNG-prefetch
  pacing workload.
- **P12:** C proposed a reusable tooling PR, while E concluded
  `DO NOT CREATE STANDALONE TOOLING PR`; K explicitly applies E. P12 is canceled
  as an independent branch. Keep only focused runners with their owning feature
  PRs; do not create a post-chain tooling dump.
- **Prefetch request identity:** H lists an optimization mask in the captured
  request. Replace it with the immutable effective decode-policy/configuration
  identity or only the typed effective fields that can change the candidate.
  Keep exact `Image`, pipeline and source references (the identity hash is only
  a compact key), scale bits and decode target data. Never key on a public/raw
  mask or path string.
- **Configuration vs diagnostics:** P1 resolves requested/effective behavior
  once at startup. RuntimeDiagnostics only observes. Group gates precede clocks,
  allocation, synchronization added for metrics, registry lookup, helper calls
  and native crossings. An unavailable backend/format changes effective state;
  it does not get “fixed” by a diagnostic setting.
- **Worker terminology:** “legacy per-entry threads” and “one process-scoped
  Semaphore worker” are internal scheduler implementations. Keep legacy as the
  product default; worker forcing, poll/sleep comparison and wake counters stay
  test-only. No worker selector becomes application API.
- **Reuse terminology:** historical `SCROLL_RASTER_REUSE` is an internal typed
  requested/effective option, default false. It is not a public integer bit.
- **Generation and lifetime:** distinguish immutable source identity, backing
  mutation generation, decoded-source/cache generation, and ScrollContainer
  batch generation. A real mutation advances backing generation once; a no-op,
  failed write, or clipped-out operation changes none. Background candidates
  remain detached until UI adoption; every stale/failure path releases an
  unadopted candidate exactly once and leaves transiently failed sources retryable.

## 3. P1–P12 dependency and ownership table

| PR | Hard prerequisites and branch base | Files / expected changes | API, ABI, tests and tools | Must not import from historical branches |
|---|---|---|---|---|
| **P1 `feat/image-runtime-config`** | B `feat/runtime-configuration-api` and F `feat/runtime-diagnostics` must both be merged to master first. | Public `ImageStorageProfile` plus image startup/config adapter and option mapping; only existing image owners that consume settings. | Use B's typed selector contract and F's generic diagnostics gate. Add only public `ImageStorageProfile.STANDARD` / `COMPACT`: the high-level memory-versus-fidelity request. Concrete format choice and effective policy stay internal; no raw mask API or diagnostic IDs. Test profile defaults, requested/effective behavior, selectors, immutability and no hot-path selector lookup. | Integer masks as persistence/public API; inert IDs 8–12; diagnostic-accounting bits; guessed B/F class names or metric IDs. |
| **P2 `perf/image-raster-core`** | P1; base on P1. F is available for optional aggregates. | `Image.java`, `ImageBacking.java`, `NativeImageBacking.java`, decode status/ownership, `image_Image.c`, `image_NativeImageBacking.c`, Skia backing/color/geometry, decoder glue and native registrations as needed. | Internal behavior only. Reconcile rather than duplicate current backing/draw-plan code. Keep parity, ownership/release, retry, opacity/mutation, writePixels, bounded readback, `APPLY_COLOR2` alpha, mutable-target and test-only failure cases. Focused SDK/native smoke; no generic matrix. | Budget/pressure/GPU-discard/mmap placeholders; production failure hooks; new public counters or masks. |
| **P3 `perf/image-raster-variants`** | P2; base on P2. P1 owns option defaults. | `Image.java`, `ImagePipeline.java`, `NativeImageBacking.java`, `ImageDrawPlan.java`, Skia geometry/backing/color, private native test registration if retained. | Internal one-slot exact physical/target-color variant. Test identity-before-lookup, generation/alias invalidation, replacement/eviction, equivalent-entry retention and software fallback. Bench measurements remain feature-owned. | A second Java cache, GPU variants, approximate geometry, default-on IDs 13/14, metric IDs. |
| **P4 `feat/image-compact-formats`** | P3 (P2 transitively); base on P3. | Add internal format enums (`ImageBackingFormat.h`, `ImageDecodeFormat.h`); update backing/readback, JPEG/PNG decoder dispatch and Skia color/draw paths. | Consume P1's `ImageStorageProfile.COMPACT`; do not expose RGB565/GRAY8/ARGB4444 choices or add a per-format native entry point. Keep canonical reads, transactional promotion, compact writePixels, quality/parity and allocation-retry tests. Native format probes are test-only. | Public format IDs, mmap, unsupported platform claims, per-format production metric APIs. |
| **P5 `feat/image-lazy-jpeg`** | Current master encoded-source/pipeline; recommended base P1 for common API state, but no hard P2/P4 dependency. F is needed only for any optional failure aggregate. | `Image.java`, `ImagePipeline.java`, `EncodedImageSource.java`, new internal `ImageDecodePolicy.java`; adjust current JPEG factory native replacement path. | Preserve `Image.getJpegBestFit` / `getJpegScaled` descriptors and eager argument/path/header errors. Tests cover payload laziness, no reread, deterministic error caching, transient retry and deployed factory dispatch. Existing decode native methods are reused. | New public lazy switch/policy, duplicate file loader, new public factory descriptor, malformed-payload fallback. |
| **P6 `perf/image-scroll-raster-fast-path`** | P3; base on P3. P4 is optional and must have conservative format fallback; P5 is only an optional deferred-source fixture. | `Graphics.java`, `Image.java`, `ImageDrawPlan.java`, hidden bridge, `GraphicsPrimitivesSkia_c.h`, Skia geometry/draw color and existing graphics native registrations. | No new public setting. Reuse `drawGeometryNative` / `copyGeometryNative`; preserve generic draw/copyRect fallback. Test unhandled / handled-no-op / handled-mutated, clipping, generation and cache side effects, GLES fallback and copyRect parity. | New cache, async decode, public repaint/diagnostic hooks, P5 policy copied from a shared historical branch. |
| **P7 `perf/ui-scroll-raster-reuse`** | P1; F for optional counters/timers. P6 is not a hard prerequisite. Base on P1/F; rebase after P6 if both touch Graphics. | `ScrollContainer.java`, `ClippedContainer.java`, `totalcross/ui/gfx/Graphics.java`, Skia screen-surface implementation and the native registry only for the row-move operation. | Internal option defaults off. Keep vertical software BGRA8888/RGB565 path, exact physical edges/stride, repaint-state preservation and full recovery after post-move failure. Native hashes stay test-only. A focused deployed smoke is correctness evidence, not a broad speedup claim. | Horizontal/GPU reuse, partial presentation, default promotion, raw benchmark profiles or last-event APIs. |
| **P8 `feat/image-async-prefetch`** | P1 and P5; base on P1+P5. | New `ImagePreparation.java`; `Control`, `Container`, `ImageControl`, `ScrollContainer`, `Image.java`, `ImagePipeline.java`, `EncodedImageSource.java`, hidden drawing bridge. | Preserve the one supported `ScrollContainer.prepareForDisplay(Runnable)` entry point. Traversal/adoption hooks stay internal; audit API artifact filtering for public cross-package bridges. Tests own traversal, DRAW_READY/COPY_READY, identity, staleness, release/retry and batch completion. Java-only; no native redesign. | Virtual-list prediction, unbounded eviction/cache, worker pool, raw optimization mask in request identity, worker mode as public API. |
| **P9 `feat/image-prefetch-worker`** | P8 and Semaphore V1 already in current master; base on P8. | `ImagePreparation.java`, worker/lifecycle tests and any focused SDK task. | One serialized process worker using Semaphore wakeups; default remains legacy. Test lost-wakeup/coalescing, FIFO, queue drain, shutdown/start failure. No native ABI or generic Semaphore edits. | Semaphore V1 implementation/stress suite, production poll/sleep/wake metrics, broad default change or public worker selector. |
| **P10 `feat/image-png-prefetch`** | P8 hard; P9 is recommended merge/base order, not a PNG architecture dependency. Base on P8, then rebase onto P9 if both edit the coordinator. | `ImagePreparation.java`, encoded PNG eligibility and Java-result candidate/adoption; Gradle smoke/test tasks. | Static one-frame PNG only, same queue and ownership path; no new public or VM API. Test PNG selection/readiness, stale/failure cleanup, retry and JPEG-counter separation; one deployed smoke. | Animated PNG, new native decode entry points, six-process comparison profiles, raw packages/results. |
| **P11 `perf/frame-pacing-diagnostics`** | F for scheduling metric gate; `System.nanoTime()` and output streams already exist on master. P10 is optional workload integration. | `Flick.java`, internal scheduling aggregates/test adapters, focused SDK tests and feature-owned runner if needed. | Preserve TimerEvent 40 fps, millisecond animation clock, relative deadlines, SDL polling and legacy yield. No public pacing API or new native method. Test shared advancement, zero-delta plateau and disabled diagnostic path. | Clock/deadline/SDL/yield production selectors, per-frame allocation/observer, claims of a pacing winner. |
| **P12 `perf/image-rendering-benchmarks`** | **Do not create as a standalone PR.** E's tooling audit and K's instruction override C's initial P12 proposal. | No P12-owned files. Add only a focused runner/workload to P6/P7/P8/P9/P10/P11 when that owner needs it. | No API/ABI, archive, package or result data. The 663-image corpus remains external and manifest-identified. | Generic tooling layer, raw logs/CSVs/ZIPs/TCZs, old hard-coded matrix and numeric-mask selectors. |


### RuntimeConfiguration and RuntimeDiagnostics ownership

| PR | Configuration dependency | Diagnostics dependency |
|---|---|---|
| P1 | Consumes B's generic typed selector contract; owns `ImageStorageProfile`, requested defaults, requested/effective resolution and the immutable image startup policy. | Consumes F's group gate. Diagnostics never supplies defaults or changes effective options. |
| P2 | Consumes P1 raster-core policy. | F IMAGE/RENDERING/MEMORY groups for coarse route, bytes and backing gauges; detailed probes remain test/compile-only. |
| P3 | Consumes P1 physical-identity and variant policy; IDs 13/14 stay off. | F RENDERING aggregates only; keys, rejection causes and slot transitions stay test/compile-only. |
| P4 | Consumes P1 `ImageStorageProfile`; format-specific choice remains best-effort and internal. | F IMAGE/MEMORY aggregates; per-format/scratch accounting stays compile-time-only. |
| P5 | No per-factory option; preserves factory behavior under P1's immutable image state. | Only a failure-only counter or F IMAGE aggregate if justified; no decode-policy metric controls decoding. |
| P6 | Consumes P1/P3 effective raster policy; P6 adds no selector. | F RENDERING group for coarse hit/fallback totals; per-call rejection detail stays test-only. |
| P7 | Consumes P1's internal default-off scroll reuse policy. | F RENDERING group for totals, bounded causes and gated phase timers. |
| P8 | Captures the P1 effective decode policy identity needed for request deduplication. | F PREFETCH group for coarse request/state/timing metrics; failure-only count may stay production-safe. |
| P9 | P1 rollout state may select the internal worker, but legacy per-entry threads remain the default. | F PREFETCH aggregates only; poll/sleep/wake comparisons and waiter probes stay test-only. |
| P10 | Uses P1's effective format policy where it affects candidate output; adds no public option. | F PREFETCH/IMAGE aggregates; PNG readiness does not increment native JPEG decode counters. |
| P11 | No pacing policy option; existing production driver/default remains authoritative. | F SCHEDULING group, gated before every clock read and allocation. |
| P12 | Canceled. | Canceled. |


## 4. File ownership and conflict matrix

Overlap classes use K's terms: `safe parallel`, `stack required`,
`cherry-pick/fold candidate`, `conflict-prone but independent`, and
`same-owner feature and should not be split`. A consumer edge is hard only where
noted; shared files alone call for an ordered rebase/merge. Test hooks and late
corrections are folded into their owning feature, not a separate repair PR.

| High-contention file / subsystem | Expected owners | Overlap classification and merge note |
|---|---|---|
| `Image.java` | P1, P2, P3, P4, P5, P6, P8, P10 | `stack required`: P1→P2 and P2→P3→P4. `conflict-prone but independent`: P2/P5 can be implemented in parallel; merge P2 then P5. P3/P5 share pipeline/decode-generation state; merge P5 before P3's final key review. P6 stacks on P3. P8/P10 are one preparation owner chain. |
| `ImagePipeline.java` | P2, P3, P5, P6, P8 | `stack required`: P2/P3; P5's immutable decode policy is independent but should merge before P3 finalizes source/decode key semantics. P6 consumes P3 plans; P8 consumes exact pipeline identity. `conflict-prone but independent`, not a reason to split features. |
| `EncodedImageSource.java` | P2, P5, P8, P10 | P2 owns storage/cache lifetime; P5 adds policy/error cache; P8/P10 consume immutable identity. `stack required`: P8 on P5; `cherry-pick/fold candidate`: source ownership fixes stay in P2/P5. |
| `NativeImageBacking.java` | P2, P3, P4, P6 | `stack required`: backing owner chain P2→P3→P4; P6 consumes its stable physical-generation contract. One internal bridge is preferable to per-format/per-optimization methods. |
| `ImageDrawPlan.java` | P2, P3, P6 | P2's draw semantics → P3 cache/identity → P6 clip-aware plan. `stack required`. |
| `ImageDrawingBridge.java` | P2, P3, P6, P8 | P2/P3/P6 reuse the existing hidden bridge; P8 adds a preparation handoff. `conflict-prone but independent`: keep additions internal and verify the SDK API artifact; cross-package visibility needs review. |
| `Graphics.java` (`totalcross/ui/gfx`) | P6, P7 | Independent algorithms but same Java owner and native registration. P6 uses existing draw/copy plan bridges; P7 adds at most one narrow row-move operation. `conflict-prone but independent`: rebase P7 after P6 for merge hygiene, not because P7 calls P6. |
| `ScrollContainer.java` | P7, P8 | P7 changes scroll/repaint lifecycle; P8 adds preparation batches/invalidation. `conflict-prone but independent`: merge P7 first, then attach P8 hooks to the final scroll lifecycle. |
| `ClippedContainer.java` | P7 | `same-owner feature and should not be split`: P7 dirty-strip painting and fallback stay with ScrollContainer repaint recovery. |
| `ImageControl.java`, `Control.java`, `Container.java` | P8 | `same-owner feature and should not be split`: traversal, image capture and invalidation hooks stay in P8; safe parallel with P7 outside ScrollContainer. |
| `Flick.java` | P11 | `safe parallel`: P11 owns shared callback advancement and fixed-field aggregate gating; no P7/P8 production edit expected. |
| Native method registries (`nm/NativeMethods.txt`, prototypes, `NativeMethods.h`, `init/nativeProcAddressesTC.c`) | P2, P3, P4, P5, P6, P7 | `conflict-prone but independent`: methods remain feature-owned. Generate/update as one atomic feature change. P5 must resolve existing public JPEG replacement entries; P7's screen move is the only likely new SDK/native ABI. Test-only entries must not ship. |
| `nm/ui/image_Image.c` / `nm/ui/image_NativeImageBacking.c` | P2, P4, P5; P2/P3/P4 respectively | `stack required`: P2/P4 decoder/backing ownership. P5 factory replacement removal/reroute is independent Java policy but touches image registration. Keep P5 out of the pixel decoder implementation. |
| Skia backing / geometry / color | P2, P3, P4, P6 | `stack required`: P2 backing semantics → P3 variant slot → P4 format helpers; P6 consumes P3 physical paths. `safe parallel` edits are possible in isolated functions, but merge through that stack. `cherry-pick/fold candidate`: P3 equivalent-entry and P6 fallback fixes fold into their owners. |
| JPEG loader | P2, P4; P5 consumes existing decoder | P5 should preserve the current decoder entry points; P2/P4 own pixel buffer/storage changes. No separate lazy-JPEG loader path. |
| PNG loader | P2, P4; P10 Java queue only | `safe parallel`: P4 owns decoder format dispatch; P10 must not alter VM PNG decode. |
| VM startup/configuration and diagnostic groups | B, F, P1; later feature metrics owned by P2–P11 | `safe parallel`: B and F are independent foundation branches from master. `stack required`: P1 on both. Feature groups/IDs remain internal and must use F's generic bridge. |
| `TotalCrossSDK/build.gradle` smoke/task wiring | P2–P11, each with owned tests | `conflict-prone but independent`: same build file causes mechanical conflicts; tests are independent. Add only the focused task for the owner and avoid centralizing them in canceled P12. |

## 5. Native ABI ownership

| PR / Java owner | Existing or expected native boundary | Classification and disposition |
|---|---|---|
| P2 — `totalcross.ui.image.Image` | Existing private `decodeEncodedSource`, `decodeEncodedSourceTargeted`, and `decodeEncodedSourceTiered`; registrations map to `tuiI_decodeEncodedSource*`. | Production decode operations. Preserve signatures unless ownership semantics require a private change; do not add a method per optimization. |
| P2 — `totalcross.ui.image.NativeImageBacking` | Existing private create/read/scale/snapshot/make-mutable/release operations (`createEmptyNative`, `createFromArgbPixelsNative`, `readPixelsNative`, `readRgbaRowNative`, `scaleNative`, `snapshotNative`, `makeMutableNative`, `releaseNativeHandle`, `materializeGeometryNative`). | Production backing operations. Reconcile existing bridge before adding one. Current `backing*TestNative`, accounting reset and snapshot-failure methods are test-only and must not be in normal production registration. |
| P3 — `NativeImageBacking` / `Graphics` | Variant lookup/materialization belongs in Skia backing/geometry internals and the existing draw-plan bridge. | No new production native method per cache/variant. Any synthetic raster factory or variant probe is test-only. |
| P4 — `NativeImageBacking` / decoders | Internal format enums/dispatch in `ImageBackingFormat.h`, `ImageDecodeFormat.h`, `image_Image.c`, `image_NativeImageBacking.c`, JPEG/PNG loaders and Skia code. | No runtime Java native entry point per format. `currentFormatNative()` / `tuiNIB_currentFormatNative`, if retained, is a test-only probe with synchronized descriptor/prototype/address registration. |
| P5 — `Image` | Current public `getJpegBestFit` / `getJpegScaled` entries are `tuiI_getJpegBestFit_sii` and `tuiI_getJpegScaled_sii`; current Java methods are native-replacement candidates. | Preserve public Java descriptors but stop the direct eager native replacement from bypassing lazy construction, or make those implementations create the same policy-bearing deferred source. Later materialization reuses P2's existing decode natives. No new public/native factory. |
| P6 — `totalcross.ui.gfx.Graphics` | Existing private `drawGeometryNative(Object,...)`, `copyGeometryNative(Object,...)`, and `copyImageRectNative(...)`; source side uses `NativeImageBacking.materializeGeometryNative(ImageDrawPlan)`. | Reuse the generic plan/copy bridge. Keep `unhandled`/`handled-no-op`/`handled-mutated` internal; do not add per-rejection native calls. |
| P7 — `totalcross.ui.gfx.Graphics` | Proposed private `scrollRasterRegion(int x,int y,int width,int height,int deltaY)` / `tugG_scrollRasterRegion_iiiii`, backed by one Skia surface helper. | One product operation for overlap-safe row movement, exact physical width/stride and no presentation. `copyRectNative` currently calls generic `drawSurface`; reuse it only if native tests prove the full row-move contract. Raster hash/last-result helpers stay C++ test-only. |
| P8–P10 — `Image` / `ImagePreparation` | Existing JPEG decoder/backing operations can populate a detached candidate; Java owns queue and UI adoption. | No new VM method is required by H; P9 uses existing Semaphore V1; P10 adds no native method or VM redesign. |
| P11 — `RuntimeDiagnostics` / `Flick` | Reuse `System.nanoTime()` and F's single generic internal metric read/batch-read/reset bridge. | No new native clock, metric-specific method or public numeric ID. Forced clocks/drivers remain test-only. |
| P12 | None. | Canceled. |

Any JNI change must update `TotalCrossVM/src/nm/NativeMethods.txt`,
`NativeMethodsPrototypes.txt`, `NativeMethods.h`, and
`init/nativeProcAddressesTC.c` together. D's dedicated `backing*`,
`writePixels*`, `physicalIdentity*`, `targetColor*`, `variant*`,
`compactDecode*`, `promotion*`, screen-update/present, rejection and
`metricForTest(int)` getters are not a production diagnostics API. F's generic
batch bridge owns runtime metric transport; detailed values remain test-only or
compile-time-only. `ImageDrawingBridge` and any new ScrollContainer preparation
hook must also pass SDK public-artifact filtering.

## 6. Cumulative public API audit

| After | Stable application API | Internal SDK API | Test-only / native internal ABI |
|---|---|---|---|
| B + F foundations | B's generic typed `RuntimeConfiguration`; F's generic snapshot surface (`RuntimeDiagnostics` / snapshot contract). | Selectors, descriptors, groups and numeric IDs remain private. | Generic native batch bridge; synthetic metric hooks test-only. |
| P1 | No integer mask. Use B's typed configuration surface and add public `ImageStorageProfile.STANDARD` (default) / best-effort `COMPACT`, the app-visible memory/fidelity preference. Concrete formats and cache choices are not public. | Immutable requested/effective image policy and any one-way legacy adapter, derived once at startup. | No feature metric IDs, masks or bridge entry points. |
| P2–P7 | No new application API; preserve existing `Image`/`Graphics` behavior and descriptors. | Backings, plans, caches, format choice, fast paths and scroll reuse. P7 option is internal/default-off. | Native backing operations private; probes/fault hooks absent from production API/ABI. |
| P8 | Keep the single historical `ScrollContainer.prepareForDisplay(Runnable)` entry point required by H. | Control/Container traversal hooks, `ImagePreparation`, request states and bridge methods. | No public worker selector, source identity, counters or hooks. Check hidden bridge and callback types in the shipped API artifact. |
| P9–P11 | No additional application API. Existing `Flick` behavior/defaults stay. | Worker scheduling and Flick test drivers are internal. | Metric identifiers private to F; forced worker/clock/deadline/SDL/yield controls test-only. |
| P12 | No addition; no P12 branch. | Feature-owned runners only. | No benchmark selector, archive format, test hook or result ABI. |

## 7. Test and smoke ownership

| Owner | Unit / SDK integration | Native C/C++ and deployed smoke | Benchmark / external evidence |
|---|---|---|---|
| P2 | Decode parity, ownership transfer/release, retry, opacity, aliases, writePixels guards, bounded readback, color alpha, mutable target. | Skia backing/readback tests; focused deployed macOS image smoke where native ownership differs from JavaSE. | Only a focused feature-owned measurement if it exercises the changed hot path. |
| P3 | Identity precedence, BGRA8888/RGB565 conversion, translucent fallback, one-slot replacement/eviction and generation tests. | Native geometry/color tests and supported software-target smoke. | Variant hit/materialization measures stay with P3 tests/runner; no general matrix. |
| P4 | JPEG/PNG selection, GRAY8/ARGB4444 cases, compact writes, promotion/failure retry, canonical readback and quality bounds. | Decoder/Skia format tests, combined P2/P3 tests and supported deployed target smoke. | Synthetic format fixture; never force formats into the 663-image customer list. |
| P5 | Factory signature and eager error boundary, deferred payload, no path reread, deterministic error cache, transient retry. | SDK ABI/deployed test that proves Java lazy construction runs despite former native replacements. | No standalone decode matrix. |
| P6 | Full/partial/no clip, corners, 1x/2x, three-way result, unchanged no-op state, one mutation generation, copyRect fallback parity. | `skia_surface_test.cpp`/geometry tests and deployed macOS smoke; GLES/non-software fallback. | Small synthetic fixture; customer harness only if external and needed. |
| P7 | Eligibility reasons, clamped actual delta, pending repaint ownership, opaque fill order, post-move full recovery. | `TotalCrossVM/src/nm/ui/skia/skia_surface_test.cpp` overlap/bounds/BGRA8888/RGB565 tests plus SDK scroll smoke. | Mac/Windows runs are environment-specific evidence; Windows manifest records RDP/local, refresh, DPI, drawable size, SDL format/backend. No broad speedup gate. |
| P8 | Traversal, DRAW_READY/COPY_READY, dedupe, source/pipeline identity, stale batch, callbacks once, candidate release/retry. | Deployed image preparation smoke for native JPEG ownership/adoption. | Optional cold/warm use of an externally supplied manifest-identified 663-image corpus. |
| P9 | Semaphore wake coalescing, FIFO/one active item, legacy default, start/stop/failure and queue drain. | No new native suite; Semaphore V1 tests remain in its foundation owner. | Drop poll-vs-semaphore timing profiles from product history. |
| P10 | Static PNG readiness, Java-result adoption, animated PNG rejection, stale/failure cleanup, retry, JPEG counter separation. | One deployed PNG prefetch smoke; no VM/format ABI change. | Do not copy six-process matrices or generated Windows packages. |
| P11 | Shared Flick advancement, timer/update parity, zero-delta plateau, clock gate and aggregate semantics. | No native scheduling-policy change; macOS smoke only verifies unchanged driver behavior. | Keep named 40/60 fps, UpdateListener and 16 ms/16.667 ms workload shapes with feature-owned tooling. Three samples on one Mac and no Windows P11 execution prove no winner. |
| P12 | None. | None. | Canceled; all useful workloads and runners remain with P6/P7/P8/P9/P10/P11 owners. |

The 663-image corpus remains external. Keep its three-column/221-row workload
shape and manifest validation only where an owner needs it; do not check in its
customer images, per-process logs, CSV/JSON results, packages, or hashes for a
single run. Keep compact conclusions and environment conditions in feature
reports. Do not treat a fresh process as a cold host file cache.

## 8. Safe parallel implementation pairs

| Pair | Decision and shared files | Rebase/merge cost and order |
|---|---|---|
| **P2 vs P5** | Safe to implement in parallel after P1. P5's hard substrate is already on master; it does not require compact formats. Shared: `Image.java`, `ImagePipeline.java`, `EncodedImageSource.java`, plus JPEG factory registrations. Ownership/backing and lazy policy are architecturally separable. | Medium-high file conflict. Merge P2 first to settle backing/source lifetime, then P5; review P5's policy identity before P3 finalizes variant keys. |
| **P4 vs P6** | Safe to implement in parallel once P3 lands, not immediately after P1. Shared: `Image.java`, `NativeImageBacking.java`, `image_NativeImageBacking.c`, Skia backing/geometry/color and native registries. Format storage and clipped physical copy are independent; P6 can conservatively reject compact sources. | Medium-high rebase. Merge P4 first so P6 explicitly tests compact-enabled fallback and does not reinterpret storage. P6 still must work with P4 disabled. |
| **P7 vs P8** | Safe after their own gates: P7 needs P1/F; P8 needs P1/P5. Shared mainly `ScrollContainer.java`; P7 owns `ClippedContainer`/screen movement, P8 owns traversal, `ImageControl` and preparation queue. No semantic call dependency. | Medium rebase. Merge P7 first, then P8 adds batch invalidation/preparation hooks against the final scroll/repaint lifecycle. P7 can start before P6; rebase it after P6 if both touch `Graphics.java`. |
| **P10 vs P11** | Safe in parallel. P10 needs P8 and preferably the P9 final queue. P11 needs F and master `nanoTime`; it does not need PNG prefetch. Shared source is limited to central metric registration or an optional end-to-end workload. | Low if metric domains are separate, medium if F has one registration file. Merge P10 first when P11 uses the mixed-format prepared workload; otherwise either order. |

Other parallelism follows the same rule: B and F can start from master in
parallel; P1 waits for both. P7 can start after P1 while P2–P6 proceed. P3
stacks on P2; P4 and P6 form a safe implementation wave after P3. P8 stacks on
P5; P9 can be developed with P10 after P8 if the worker/candidate seam is
frozen, then merge P9 before P10. P11 does not gate runtime image PRs.

## 9. Terminology to use in implementation

| Avoid | Use in the reconstructed work |
|---|---|
| Optimization mask / bit value | Typed requested option plus immutable effective startup policy. Historical IDs are migration evidence only. |
| Mask in a prefetch key | Exact image/pipeline/source identity and only decode-relevant effective policy identity. |
| `SCROLL_RASTER_REUSE` mask | Internal `scrollRasterReuse` rollout option; requested/effective boolean, default false. |
| Worker mode selector | Internal scheduler choice: legacy per-entry threads (default) or serialized Semaphore worker; test forcing stays test-only. |
| “Diagnostics enabled” as feature setting | Separate RuntimeDiagnostics group gate. It observes decisions and never changes them. |
| One generation for every kind of freshness | `backingMutationGeneration`, `decodedSourceGeneration`, immutable source/pipeline identity and `preparationBatchGeneration`, each with its own owner. |
| Candidate is image-owned before adoption | Detached candidate wrapper owns it until a current UI-thread adoption transfers it once; stale/failure cleanup releases it once. |
| A screen move succeeded because native call returned | `handled-mutated` only after a complete write; `handled-no-op` changes no target state; `unhandled` leaves generic fallback untouched. |
| Requested option equals active backend behavior | Report requested and effective state separately; unsupported backend/format yields a conservative effective fallback. |

## 10. P12 final boundary

**Decision: DO NOT CREATE STANDALONE TOOLING PR.** This resolves the direct
conflict between C's draft P12 and E's completed audit, as K requires. There is
no P12 branch base or merge step. P6/P7 own clipped-draw and scroll-reuse
correctness workloads; P8/P9/P10 own preparation, worker and static-PNG
smokes; P11 owns Flick/pacing fixtures. Each runner is rebuilt only when its
feature needs it, with named configuration, RuntimeDiagnostics snapshots,
source/runtime hashes and host metadata. Keep the 663-image corpus external.
Do not add a generic six-process runner, standalone corpus packager, copied
PowerShell matrix, raw result archive, or tooling-only closeout after P1–P11.

## 11. Recommended implementation waves and merge order

```text
origin/master
  ├─ B feat/runtime-configuration-api ─┐
  └─ F feat/runtime-diagnostics ───────┴─> P1 feat/image-runtime-config
                                           ├─> P2 perf/image-raster-core ─> P3 perf/image-raster-variants
                                           │                               ├─> P4 feat/image-compact-formats
                                           │                               └─> P6 perf/image-scroll-raster-fast-path
                                           ├─> P5 feat/image-lazy-jpeg ────────────────> P8 feat/image-async-prefetch
                                           │                                               └─> P9 feat/image-prefetch-worker
                                           │                                                       └─> P10 feat/image-png-prefetch
                                           └─> P7 perf/ui-scroll-raster-reuse (no P6 hard edge)
F + master nanoTime/streams ───────────────────> P11 perf/frame-pacing-diagnostics (parallel with P10)
P12 perf/image-rendering-benchmarks: canceled by E/K; tools stay feature-owned
```

Recommended merge order where contracts overlap: B and F in either order; P1;
P2; P5; P3; P4; P6; P7; P8; P9; P10; P11. P7 can be implemented earlier
from P1 and rebased after P6. P4/P6 and P10/P11 may merge in either order
unless their optional integrated workload is included. P9 before P10 minimizes
`ImagePreparation.java` churn. Every PR branch starts from the latest merge
base named above; no implementation branch starts from a historical feature
branch.

## 12. Genuine blockers and unresolved questions

No image architecture question blocks assignment. The two real gates are that
B's typed RuntimeConfiguration contract and F's generic RuntimeDiagnostics
foundation must be merged before P1; both are absent from the audited master.
F owns final private metric descriptors/IDs. P1's only justified image-specific
public value is `ImageStorageProfile.STANDARD` / `COMPACT`; concrete formats,
cache choices, scroll reuse, worker selection, and diagnostic controls remain
internal or test-only.
