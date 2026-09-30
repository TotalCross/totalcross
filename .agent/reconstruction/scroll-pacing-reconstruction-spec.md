<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse and frame pacing reconstruction

This analysis-only specification reconstructs P7 `perf/ui-scroll-raster-reuse`
and P11 `perf/frame-pacing-diagnostics` from their frozen implementation,
test, benchmark, and evidence histories. It is based on `origin/master`
`7d50c12c0675fbef13e74c54e59b704c04c08050` (2026-09-30). `System.nanoTime()`
and first-class standard streams already exist on that base and are treated as
available APIs.

The implementation boundary is deliberately small: P7 owns conservative,
opt-in software framebuffer reuse; P11 owns shared Flick advancement and
optional aggregate diagnostics. Detailed samples, injected clocks/drivers,
experimental scheduling choices, runners, and result files stay outside
production APIs. No default or pacing policy change follows from the historical
measurements.

## 1. P7 final product architecture

### Feature and native primitive

Keep `RenderingOptimizations.SCROLL_RASTER_REUSE` as an explicit opt-in bit
whose default mask is zero. The bit affects only vertical `ScrollContainer`
movement on a software raster target. Do not reuse `Settings.optimizeScroll`,
add GPU behavior, create per-control surfaces, or change SDL's full-frame
upload/presentation path.

Expose one native operation through `totalcross.ui.gfx.Graphics`:

```text
scrollRasterRegion(x, y, width, height, deltaY)
```

All arguments are physical pixels; `width` is a pixel count, not a byte count.
The software Skia implementation validates the complete rectangle, target,
pixel storage, format, stride, and `0 < abs(deltaY) < height` before mutation.
It moves only retained rows with overlap-safe row copies, iterating from the
safe end for the movement direction. It touches exactly `physicalWidth *
bytesPerPixel` bytes per row, honors Skia `rowBytes` without copying padding,
notifies Skia that pixels changed, and never presents the screen. Supported
pixel formats are BGRA8888 (4 bytes/pixel) and RGB565 (2 bytes/pixel); an
unknown format or invalid stride fails closed.

The deterministic raster-region hash is a test fixture only. It must not be a
production API or part of `Graphics` runtime behavior.

### Scroll and repaint sequence

For each enabled, nonzero scroll request:

1. Capture repaint state before moving the content and calculate the actual
   vertical delta after scrollbar clamping. The fast path uses this actual
   delta, never the requested delta.
2. Prove every eligibility condition in section 2. Convert the viewport's left,
   top, right, and bottom edges independently to physical integers using the
   main-window content scale. Require exact integral edges and delta; derive
   `physicalWidth = right - left` and `physicalHeight = bottom - top` from those
   converted edges. Never treat logical width as physical width.
3. Move the content bag through its normal logical path, then move the old
   framebuffer viewport by `-physicalDeltaY`.
4. Repaint only the newly exposed strip at the opposite edge. Fill inherited,
   viewport, and bag backgrounds in normal paint order under a strip clip, then
   paint only intersecting children. Keep ordinary `paintChildren()` behavior
   unchanged; use the bounded dirty-child path and its vertical search when
   available.
5. Repaint a visible, opaque, non-overlay scrollbar only when it does not
   overlap the reusable viewport. Present once using the existing
   `safeUpdateScreen()` path; SDL still uploads the full bitmap.

Pixel accounting is defined from physical geometry:

```text
viewportPixels = physicalWidth * physicalHeight
dirtyPixels    = physicalWidth * abs(physicalDeltaY)
reusedPixels   = viewportPixels - dirtyPixels
movedBytes     = reusedPixels * targetBytesPerPixel
```

Use checked wide arithmetic for products. The dirty logical strip must map to
the same physical strip used by the native move, including scaled windows and
fractional content scales; if its edges cannot be represented exactly, fall
back.

### Repaint-state ownership and recovery

An already-pending `Window.needsPaint` means unrelated or earlier work exists:
reject reuse and retain the normal full repaint. The fast path may suppress only
the synchronous invalidation caused by moving its own bag, and only when no
repaint was pending before the scroll. Never clear a repaint flag raised by
another control or by strip painting. A hit may omit the scroll-generated full
repaint only after the strip and scrollbar repaint both succeed.

A rejected eligibility check or native move that made no mutation follows the
existing full-repaint path. If anything fails after framebuffer pixels moved,
mark the window fully dirty and complete a full normal repaint before the next
presentation. Count that outcome as `postMoveRecovery`; do not present a
partially repaired frame or leave the old scroll raster visible.

## 2. P7 eligibility and fallback state machine

The first failed check is the sole primary reason for that request. One bounded
reason is recorded per fallback. Attempts/hits/fallback totals cover nonzero
vertical candidates, so `attempts = hits + fallbacks`. Count nonzero horizontal
requests separately and report their `unsupportedHorizontal` cause; do not
fold them into the vertical totals. Disabled requests do not enter counters.

| Order | Check | Result when it fails |
| --- | --- | --- |
| 1 | Request is vertical-only | `unsupportedHorizontal` |
| 2 | Software raster backend and supported pixel storage are active | `unsupportedBackend` |
| 3 | Scroll container is topmost | `notTopmost` |
| 4 | No repaint was pending before the scroll | `pendingRepaint` |
| 5 | No legacy `offscreen` / `offscreen0` screenshot surface is active on the relevant containers | `legacyOffscreen` |
| 6 | Content and inherited backgrounds can be repainted without transparent-parent reconstruction | `transparentContent` |
| 7 | No visible control painted above the viewport intersects it | `overlappingControl` |
| 8 | Scrollbar is opaque and does not overlay the viewport | `overlayScrollbar` |
| 9 | Physical viewport edges and actual delta are exact, in-bounds integers | `nonIntegralPhysicalGeometry` |
| 10 | `0 < abs(physicalDeltaY) < physicalHeight` | `deltaOutOfRange` |
| 11 | Native row movement accepts the validated target and format | `nativeMoveFailure` |
| 12 | Strip, scrollbar, or presentation work fails after mutation | `postMoveRecovery` plus full repaint |

`unsupportedBackend`, `notTopmost`, `pendingRepaint`, `legacyOffscreen`,
`transparentContent`, `nonIntegralPhysicalGeometry`, `deltaOutOfRange`,
`overlappingControl`, `overlayScrollbar`, `nativeMoveFailure`,
`postMoveRecovery`, and `unsupportedHorizontal` are the stable bounded reason
set. Keep their semantic meanings stable if later assigned diagnostic IDs.

## 3. P7 diagnostics boundary

| Component | Classification | Contract |
| --- | --- | --- |
| Optional totals for attempts, hits, fallbacks, and the bounded reason set | `RUNTIME_DIAGNOSTIC` | May be exposed through RuntimeDiagnostics when enabled. No per-event strings, arrays, observers, or clock reads when disabled. |
| Optional aggregate decision, move, and dirty-paint time | `RUNTIME_DIAGNOSTIC` | Only aggregate nanoseconds; gate every `System.nanoTime()` read on the diagnostic setting. |
| Viewport hashes, geometry strings, requested/actual last-event values, last-result arrays, and frame-local `writePixels` values | `TEST_ONLY` | Keep in test fixtures or benchmark adapters; do not ship as stable RuntimeDiagnostics state. |
| Compact correctness profiles and reproducible benchmark summaries | `BENCHMARK_TOOL` | Use them to prove actual hits and compare matched paths; never expose them as SDK APIs. |

Runtime diagnostics are not required for reuse to work. When disabled, reuse
does not allocate per frame, dispatch observers, build geometry strings, hash
pixels, or read the diagnostic clock. Counters must distinguish a real fast-path
hit from a candidate attempt, including in diagnostics-off benchmark runs; the
benchmark can collect local outcomes without adding product fields.

## 4. P11 final diagnostic and test architecture

### Production path

Refactor `Flick` so TimerEvent and UpdateListener callbacks enter one shared
advancement routine. The routine owns elapsed-time sampling, scroll amount,
scroll execution, and completion/stop semantics. Preserve current event
registration and unregister exactly the active driver. Keep the production
defaults: TimerEvent at 40 fps, millisecond animation time, relative timer
deadlines, SDL event polling, and legacy `Sleep(1)` behavior. Do not add a new
pacing policy or default.

Use the existing `System.nanoTime()` only as a monotonic source for elapsed
diagnostic and benchmark durations. Do not interpret it as wall-clock time or
replace the established animation clock. First-class `System.out` and
`System.err` are already available for benchmark summaries and failures; no new
runtime stream abstraction is needed.

### Optional aggregate diagnostics

Scheduling diagnostics may be runtime-optional. When enabled, collect bounded
aggregates for callback count, callback-start interval, callback work time, and
lateness where that callback has a defined nominal deadline. A useful minimal
shape is count, total, and maximum for each duration plus late-callback count and
total/max lateness. Keep percentile distributions in the benchmark only.

When diagnostics are disabled, the callback path performs no diagnostic
`nanoTime()` reads, per-frame allocation, sample append, or observer dispatch.
When enabled, update fixed aggregate fields in place. Measurement must not
change which driver runs, how deadlines are scheduled, how long a callback
waits, or the amount of Flick motion.

### Test-only pacing adapters and named workload shapes

Driver and clock injection is package-private/test-only. It must not become a
public runtime option, system property, or product configuration. Each test
configuration names its driver, clock, interval, and scheduling experiment;
the benchmark records that name with the result.

| Workload | Shape to retain | Classification |
| --- | --- | --- |
| Synthetic current pacing | Monotonic `System.nanoTime()` loop at 16 ms; keep separate from application callback timing | `BENCHMARK_TOOL` |
| Synthetic 60 Hz pacing | Monotonic loop at 16,666,667 ns | `BENCHMARK_TOOL` |
| TimerEvent 40 fps | Real Flick callback path at the existing 25 ms cadence | `BENCHMARK_TOOL` |
| TimerEvent 60 fps | Real Flick callback path at 16,666,667 ns cadence | `BENCHMARK_TOOL` |
| UpdateListener Flick | Same real scroll workload driven by update callbacks | `BENCHMARK_TOOL` |
| Animation clock comparison | Millisecond baseline versus injected nano clock; identify the selected clock per run | `TEST_ONLY` |
| Relative/absolute timer comparison | Diagnostic probe per driver; absolute mode remains test-only | `TEST_ONLY` |
| SDL polling/wait and legacy/native yield comparisons | Isolated native experiment modes, not production choices | `TEST_ONLY` |

For Flick rows, preserve the same corpus, prefetch readiness, viewport,
animation distance, and reporting contract across configurations. Samples report
callback interval, deadline lateness where defined, callback work time, and
scroll completion independently. Do not conflate synthetic sleep timing with
application callback timing.

## 5. Production vs diagnostics vs benchmark separation

Every component below has exactly one destination classification.

| Historical component | Classification | Destination rule |
| --- | --- | --- |
| Conservative software framebuffer movement and opt-in `SCROLL_RASTER_REUSE` | `PRODUCT` | Keep behind the default-off mask with the full fallback path. |
| Shared Flick frame advancement | `PRODUCT` | Keep as the one implementation used by existing callbacks; preserve defaults. |
| Existing `System.nanoTime()` and standard streams on master | `PRODUCT` | Use as existing facilities; add no parallel clock or stream API. |
| Scroll attempt/hit/fallback totals and stable causes | `RUNTIME_DIAGNOSTIC` | Optional bounded aggregates only. |
| P11 callback interval/lateness/work aggregates | `RUNTIME_DIAGNOSTIC` | Optional bounded aggregates only. |
| Raster viewport hash, geometry text, last-event arrays, frame-local writePixels counters | `TEST_ONLY` | Keep out of product-facing diagnostic state. |
| Flick driver/clock injection, forced selectors, detailed frame samples | `TEST_ONLY` | Package-private or test-source fixtures; no public options. |
| Absolute-deadline, SDL wait, and native-yield experiments | `TEST_ONLY` | Isolated comparison code only; do not promote their scheduling policy. |
| 16 ms/60 Hz synthetic loops, 40/60 fps and UpdateListener profiles, named matrix runner | `BENCHMARK_TOOL` | Retain workload intent and reproducibility contracts; rebuild runner. |
| Raw logs, per-process outputs, result ZIPs, temporary packages | `DROP` | Not source or product artifacts; retain only in the external evidence/artifact store when needed. |

## 6. Historical commit migration table

`KEEP` means the behavior or test remains at the stated destination;
`FOLD` means a correction is part of the owning final design and supersedes an
earlier version; `MOVE` means retain only in the test or benchmark boundary;
`DROP` means do not reconstruct the artifact or public option. Owners are the
final P7/P11 groups named by this specification, regardless of historical
branch names.

| Commit/group | Final owner | Action | Final files/functions | Surviving proof or destination |
| --- | --- | --- | --- | --- |
| `90b1fd4b8` native screen scroll primitive | P7 `perf/ui-scroll-raster-reuse` | `KEEP` | `Graphics.scrollRasterRegion`; `tugG_scrollRasterRegion_iiiii`; `skia_scroll_raster_region` in `Graphics.java`, `gfx_Graphics.c`, and `skia.cpp` | Keep native overlap, direction, bounds, BGRA8888, and RGB565 tests in `skia_surface_test.cpp`. Keep `hashRasterRegion` test-only. |
| `83934e79f` SDK raster reuse | P7 | `KEEP` | `RenderingOptimizations.SCROLL_RASTER_REUSE`; `ScrollContainer` eligibility/attempt path; `ClippedContainer.paintDirtyChildren` | Keep matched OFF/ON waypoint correctness and reason coverage in the smoke-test fixture. Default remains off. |
| `4c0812185` repaint recovery/background completion | P7 | `FOLD` | Exposed-strip paint order in `ScrollContainer` and dirty child painting in `ClippedContainer` | Supersedes the first SDK pass: inherited opaque backgrounds must be filled before repainting exposed children. Retain equal-color parent and full-recovery cases. |
| `94b12e5a5` physical raster byte width | P7 | `FOLD` | Main-window physical pixel width and target bytes-per-pixel mapping through `Graphics` and native Skia helpers | Supersedes pixel-as-byte accounting. Use physical edge differences and actual target stride; test BGRA8888 and RGB565. |
| `87fb35684` repaint-state preservation | P7 | `FOLD` | `ScrollContainer` capture/restore of repaint ownership; sibling-overlap proof in `Control` | Supersedes unconditional clearing: pending unrelated repaint rejects reuse and remains pending. Retain overlap and repaint-state regressions. |
| `77f75b251` diagnostic timer gating | P7 | `FOLD` | Conditional `System.nanoTime()` reads in reuse decision, move, and dirty-paint sections | Retain a diagnostics-off check proving zero diagnostic timer reads without losing local benchmark hit measurements. |
| `62487c6a3` correctness profile and final M3 evidence group (`8d086003f`, with `ba70700d4` measurement corrections) | P7 | `MOVE` | `ImageScrollRasterFastPathBenchmarkApp` / `ImageScrollRealWorkloadBenchmarkApp` test harness and compact external evidence | Keep exact waypoint hashes, endpoint filtering, diagnostics-off gate, and zero post-move recovery proof as tests/benchmark contracts. |
| Windows scroll reuse runner/package (`75b0fb97a`, `35b7a0139`, `1e8d7c03f`, `f5dad132c`) | P7 | `MOVE` | Named Windows workload profile, package contract, PowerShell adapter, environment manifest | Keep only as benchmark tooling. Carry the successful preflight/parser fixes; add RDP, refresh, scaling, and backend metadata. Do not use RDP as a stable gate. |
| `6ead9cd9d` shared Flick advancement | P11 `perf/frame-pacing-diagnostics` | `KEEP` | One `Flick` advancement method called by TimerEvent and UpdateListener | Keep equivalence, start/stop, and active-registration tests; preserve current driver/default. |
| `90bdb5f41` injected internal drivers; `697d460ec` Flick profiles | P11 | `MOVE` | `FlickBenchmarkSupport` and test-only driver host; named benchmark configurations | Keep artificial drivers in test scope. Do not expose driver selection to applications. |
| `fab38b8e0` synthetic pacer and 16 ms / 60 Hz fixtures | P11 | `MOVE` | Synthetic pacing workload in the benchmark app and its contract tests | Keep the two exact intervals and distinguish pacer sleep from app callback work. Rebuild around named configuration. |
| `fb9dda02b` forced animation-clock selector | P11 | `MOVE` | Clock injection in test/benchmark fixture only | Preserve millisecond production default. Nano timing uses existing `System.nanoTime()` and is not a product selector. |
| `feddea120`, `2beb02475` relative/absolute deadline probe | P11 | `MOVE` | Deterministic scheduling experiment and benchmark comparison | Keep relative production scheduling. Do not ship environment-driven deadline selection. |
| `703045cf9` SDL event-driven wait; `0ee9086f7` native yield experiment | P11 | `MOVE` | Isolated native test/benchmark modes | Keep polling and legacy yield defaults. Keep wake/deadline tests only if their experimental implementation remains under test ownership. |
| Flick plateau / zero-delta corrections (`593bea07b`, `4c32898d7`, `71da965d4`, `85ff9a7b4`, `92f5e94e6`) | P11 | `FOLD` | Flick benchmark fixture and `ScrollContainer.scrollContent` call contract | Keep rounded-zero plateau coverage and skip `scrollContent(0, 0)`; it is treated as an end condition. Preserve failed-frame evidence before a run aborts. |
| Bounded evidence writer (`b7b7e13fb`) and Windows seed fix (`0ba5eec57`) | P11 | `FOLD` | Rebuilt benchmark result writer and PowerShell contract tests | Keep omission of null-only fields, size-before-replace checks, and explicit hexadecimal `UInt32` parsing. |
| Windows P11 package/runner (`8a0cdc9bd`) and matrix closeout (`135b77f20`, `400b986db`) | P11 | `MOVE` | Benchmark package integration and named runner configuration | Static package checks remain benchmark tooling. Historical P11 report records no Windows benchmark execution; do not label it Windows runtime evidence. |
| Raw run logs, full per-frame files, and temporary SDK/result packages | P7/P11 | `DROP` | External artifact storage only | Keep compact summaries/indexes only when they support an explicit reproducibility need; never commit raw outputs as product files. |

## 7. Tests to retain, rebuild, and drop

**Retain for P7:** native row-copy tests for both directions, overlap,
invalid bounds, both supported formats, and stride; SDK behavior tests for
matched viewport hashes, exact physical geometry, vertical eligibility,
stable fallback causes, pending repaint preservation, overlay rejection,
zero-delta rejection, exposed-strip child culling, and post-move full recovery.
The reusable native test location is `TotalCrossVM/src/nm/ui/skia/skia_surface_test.cpp`.

**Retain for P11:** shared Flick advancement equivalence, only-one-active-driver
registration, correct unregister on stop, exact 16,000,000 ns and 16,666,667 ns
synthetic intervals, TimerEvent 40/60 and UpdateListener workload completion,
rounded-zero plateau handling, and evidence-writer size/replace atomicity.
Historical fixtures live in `FlickDriverTest`, `SyntheticPacingTest`, and
`scripts/test-frame-pacing-benchmark.py`; move or rebuild them under the final
test/benchmark ownership rather than making them shipping runtime switches.

**Rebuild:** contract tests around named configurations, environment metadata,
bounded diagnostic schemas, and a common parser. A run must keep callback
interval, callback work, and deadline error as separate measurements. The
runner should refuse to publish canonical evidence until all configured fresh
processes and preflights pass.

**Drop from product:** public or environment-selectable driver/clock/deadline,
SDL wait, and native yield options; per-frame production result arrays; raw logs
and package artifacts. Narrow deterministic tests for an experimental helper
may remain only while that helper is still explicitly test-owned.

## 8. Benchmark and tooling disposition

Do not create a standalone tooling PR. Preserve the workload shapes and
correctness gates, then rebuild the runner around named configurations and the
optional aggregate diagnostics. Keep the runner/package integration with the
owning benchmark work so its schema and SDK source attestation move together.

Each Windows result must state whether it ran over RDP or a local console,
monitor refresh rate, effective display scaling/DPI, logical and drawable
physical dimensions, SDL pixel format, and software/backend identity. Record
these fields in the manifest and result summary. RDP is an environmental
condition, not a stable performance regression gate. Keep raw logs, per-frame
samples, ZIPs, and temporary packages in the external artifact store; checked-in
files should be limited to source tests and compact, intentionally retained
evidence indexes.

## 9. Evidence interpretation and limits

P7's final macOS correctness run matched all waypoint hashes and recorded 71/71
local hits per enabled pass, no fallback, and no post-move recovery. Paint
counts fell by about 70%; median work improved 30.2% cold and 33.9% warm. The
same run showed substantially worse ON work and screen-update P95/P99/MAX
tails, attributed to unchanged full-frame presentation. Its recorded
classification is `PRESENTATION-BOUND`.

The later Windows run passed two correctness preflights and six measured
processes, matched ten waypoints, and recorded 142/142 reuse hits with no
fallback per ON sample. Active-work P95 improved about 40% cold and 37% warm,
while frame-interval and screen-update P95 moved in mixed directions. This
supports a workload-specific reduction in active work; it does not prove a
uniform end-to-end speedup. The Windows data is useful only with its display,
RDP, scaling, refresh, and backend context recorded.

P11 completed 48 macOS ARM64 processes across five stages, with three processes
per configuration. Results show descriptive callback/frame interval and
lateness ranges for synthetic pacing, TimerEvent 40/60, UpdateListener, clock,
deadline, SDL wait, and yield comparisons. Three samples per row are not
statistical proof. The Windows runner/package passed static and packaging
checks, but the P11 report records no Windows benchmark execution. Neither
platform's observations justify a clock, deadline, wait, yield, driver, or
scroll-reuse default change, nor a broad performance winner or energy claim.

Canonical historical summaries are in `.agent/benchmarks/scroll-raster-reuse-poc/final/`,
`.agent/reports/scroll-raster-reuse-windows-package.md`, and
`.agent/reports/frame-pacing-scheduling-editorial.md` on the historical
branches. Those paths identify evidence provenance; this reconstruction does
not copy the raw data into the product tree.

## 10. Ordered implementation checklist

1. Start from current master and verify `System.nanoTime()` and standard stream
   support; add no duplicate API.
2. Land the P7 default-off mask and native physical row-move contract, with
   native hash helpers isolated to tests.
3. Implement the P7 eligibility state machine, exact edge conversion, bpp/stride
   byte accounting, dirty-strip repaint, and repaint-state ownership.
4. Add post-move full-repaint recovery and prove every fallback leaves the
   baseline full-repaint path intact.
5. Gate bounded P7 and P11 aggregate diagnostics; verify the disabled path has
   no diagnostic clock reads, frame allocations, or observer dispatch.
6. Refactor Flick callbacks through one advancement routine without changing
   timer, clock, event-loop, or yield defaults.
7. Keep forced clocks/drivers and scheduling experiments in test-only adapters;
   rebuild synthetic/Flick workload matrices as named benchmark configurations.
8. Rebuild evidence writers and Windows manifests with RDP, refresh, scaling,
   and backend fields; keep raw output outside Git and reject incomplete runs.
9. Validate correctness and fallback behavior first, then compare matched
   workloads. Report platform-specific active work and presentation tails
   separately; do not promote defaults from these historical samples.

## 11. Genuine unresolved questions

No architectural question blocks reconstruction. The future
RuntimeDiagnostics surface owner must choose public metric names/IDs for the
bounded aggregates; the fallback semantics and disabled-path requirements in
this specification do not depend on that naming decision.
