<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Runtime diagnostics inventory

## Design constraints

This is a migration inventory, not an API implementation. Keep metric IDs
internal and defer public type names until repository naming is settled.
Observations must not select image formats, rendering paths, worker modes, or
frame schedules. V1 is counters, gauges, timers, snapshots, and deltas; tracing
and event timelines are out of scope.

The production build may retain rare failure counters. Other metrics with
meaningful collection cost belong to separately gated groups and must compile
out when diagnostic support is absent. In a diagnostic-capable build, check the
group gate at the call site before clock reads, arithmetic done only for
metrics, allocations, synchronization, registry lookup, or Java/native calls.
An off group must not add helper calls or branches to per-pixel paths. The
current image accounting helpers check a runtime boolean before field-name
`strcmp`s, but native draw paths still call the helper; this is not zero-cost
compile-time removal.

Use one conceptual Java snapshot surface. Java-owned values are read directly
in Java; only native-owned values cross the bridge. A small generic native
surface, conceptually `readMetric(metricId)`,
`readMetrics(metricIds[], values[])`, and `resetMetrics(groupMask)`, can read
internal IDs and reset selected counter groups. The existing
`metricForTest(int)` bridge is benchmark plumbing, not a
stable protocol: its IDs mix backing diagnostics with renderer and SDL
environment values. Do not reuse those numbers or publish them. Snapshot calls
must be side-effect free and must never influence runtime decisions.

Source coverage: the named image/raster, prefetch, and semaphore refs are
ancestors of `feat/frame-pacing-scheduling-diagnostics`; that branch was audited
through its local tip and newer tracking tip. The Windows packaging branch is a
descendant and was checked separately; its runtime-source delta is limited to
the smoke benchmark. The tracking tip adds benchmark packaging/environment
capture, not new production diagnostics. The later native multiplexer is in
`f02f9dcff`; `c56afb40c` only shortens a specialized frame-metric native symbol.

## Domain summary

- **IMAGE**: decode outcomes and routes, JPEG decode latency, materialization,
  and aggregate native raster fast-path effectiveness.
- **RENDERING**: software scroll-raster reuse outcomes, bounded fallback
  counters, write-pixel/direct-copy outcomes, and screen update counts.
- **PREFETCH**: request outcomes, in-flight work, and coarse preparation,
  decode-worker, and UI-dispatch latency.
- **SCHEDULING**: optional frame interval, callback lateness, and work time.
- **THREADING**: no production waiter probe survives; deterministic semaphore
  waiter confirmation is test-only.
- **MEMORY**: native backing count and live/peak byte gauges; detailed
  format/scratch accounting remains diagnostic-build support.

## Metric inventory

Classification is exactly one of `PRODUCTION_SAFE`, `RUNTIME_OPTIONAL`,
`COMPILE_TIME_ONLY`, `TEST_ONLY`, or `DROP`. Costs below describe the historical
collection path; treatment describes the proposed destination.

| Historical metric/hook | Domain | Kind | Classification | Hotness | Cost | Future treatment |
|---|---|---|---|---|---|---|
| Native image/JPEG decode failures (`jpegNativeDecodeFailureCountForTest`) | IMAGE | COUNTER COUNT | PRODUCTION_SAFE | Per-image error path | Primitive increment; currently behind image-accounting gate | Keep a process counter; increment only on failure, with no message formatting, clock, or native read call. |
| Full/targeted, zero-copy/copied decode counts and copied/final bytes (`Image.java`) | IMAGE | COUNTER COUNT, BYTES | RUNTIME_OPTIONAL | Per-image | Branch + increments/adds; native accounting helper on some paths | Keep aggregate route and byte totals in a decode group; do not expose request dimensions as metric labels. |
| JPEG decode totals and full/half/quarter/eighth/other duration and requested-policy counts | IMAGE | COUNTER COUNT; TIMER NANOSECONDS | RUNTIME_OPTIONAL | Per-image decode | Clock reads around native decode plus increments; Java/native accounting | Keep total latency and a small bounded route/scale count; enter the group before either clock read. |
| Materialization, direct geometry/color materialization, readback counts/bytes | IMAGE | COUNTER COUNT, BYTES, PIXELS | RUNTIME_OPTIONAL | Per-image / backing conversion | Branch + increments/adds; some native helper calls | Keep coarse materialization and readback totals. Preserve source/output pixels only if computed without another scan. |
| Opacity provenance (`intrinsic`, `from source`, `during decode`) | IMAGE | COUNTER COUNT | DROP | Per-image | Branch + increment | Duplicates decode route and opacity state; not a durable support signal. |
| Opacity fallback scan count/pixels | IMAGE | COUNTER COUNT, PIXELS | RUNTIME_OPTIONAL | Per-image fallback | Branch + add after a scan; current native helper does a runtime gate first | Keep a scan count and optional pixels scanned; gate before helper/counter work. |
| Image/pipeline lifetime, draw-plan allocations/cache hits/capability arrays, presentation-only plan recreation | IMAGE | COUNTER COUNT | DROP | Startup/control path, per-image/draw | Primitive increments behind a global test-accounting flag | Retain in focused tests/benchmarks; these implementation-specific counts are too granular for the shared support surface. |
| Decode dimensions, denominator, optimization-mask observation, forced decode/draw masks | IMAGE | GAUGE PIXELS; COUNTER COUNT | TEST_ONLY | Per-image/test | Field reads; native probe crossing for mask observation | Keep in converter/smoke tests; never report test-selected configuration as runtime telemetry. |
| Native backing records live/peak and bytes live/peak | MEMORY | GAUGE ENTRIES, BYTES | RUNTIME_OPTIONAL | Backing create/release | Increment/decrement and peak update per allocation/free | Keep direct gauges. Do not reset live values; read a snapshot. Avoid format lookup or heap traversal to calculate them. |
| Per-format backing bytes/peaks, compact decode/readback scratch peaks, temporary RGBA bytes, promotion counters/bytes | MEMORY | GAUGE/COUNTER BYTES, COUNT | COMPILE_TIME_ONLY | Allocation, decode, readback | Runtime accounting branch and per-operation updates | Keep for opt-in native investigation; compile storage and update sites out of normal builds. |
| Write-pixel/direct-copy attempts, hits, fallbacks, copied bytes; physical-identity and target-color/variant lookup, hit, miss, eviction, materialization, converted bytes | RENDERING | COUNTER COUNT, BYTES | RUNTIME_OPTIONAL | Per-draw; per-backing cache event | Branch + primitive increment/add in native raster paths | Keep aggregate attempts/hits/fallbacks and useful copied/materialized bytes. No per-object registry or result-dependent behavior. |
| Write-pixel rejection causes (invalid target/source, alpha, matrix, save count, rect/size/fraction/bounds, opacity/pixels, write failure); packed physical-identity rejection lanes; mapping/save-count subreasons; shared-slot transition and unique-key counts | RENDERING | COUNTER COUNT | COMPILE_TIME_ONLY | Per-draw attempt | Branches, array buckets, mapping/reason updates; current getters decode packed values | Keep only in diagnostic native builds. The detailed cardinality is not suitable for V1; total fallback remains optional. |
| Screen update/present call counts | RENDERING | COUNTER COUNT | RUNTIME_OPTIONAL | Per-frame | Primitive increment in native screen path | Keep aggregate counts if a backend issue needs them; gate at the native call site. |
| Scroll raster reuse attempts/hits/fallbacks, viewport/reused/dirty pixels, moved bytes; decision/move/dirty-paint and screen-update totals | RENDERING | COUNTER COUNT, PIXELS, BYTES; TIMER NANOSECONDS | RUNTIME_OPTIONAL | Per-scroll / per-frame | Helper branches and increments; up to several `nanoTime` reads per eligible scroll when enabled | Keep aggregate outcomes, pixels/bytes and coarse phase timers. Test the group before any timer or helper call. |
| Scroll reuse fallback reason (12 bounded causes: backend, z-order, pending repaint, offscreen/transparent content, geometry/delta, overlap/scrollbar, native move/recovery, horizontal move) | RENDERING | COUNTER COUNT | RUNTIME_OPTIONAL | Per fallback | Branch + indexed increment; string conversion is separate | Use one simple counter per stable cause; no structured label registry is needed for these 12. Keep the total fallback counter too. |
| Last scroll result/delta/size/reason; `rasterReuseLastMetricsForTest()`; viewport hash and geometry string | RENDERING | GAUGE/COUNTER; PIXELS | TEST_ONLY | Per scroll/read | Last-state writes; `long[]` and string allocation; hash scans viewport pixels | Retain only for deterministic assertions and benchmark triage. Never put last-event state in counter deltas. |
| Write-pixel per-frame reset/metrics and last width/height/format (`writePixelsFrameMetric`) | RENDERING | COUNTER; TIMER NANOSECONDS | TEST_ONLY | Per draw/frame | Native timer reads and resets; one native query per requested kind in legacy harnesses | Keep in benchmark support only. Per-frame reset races with concurrent draws and obscures process-counter semantics. |
| Renderer target type/size/row bytes, backend code, SDL display values from `metricForTest` | RENDERING | GAUGE BYTES/PIXELS/COUNT | DROP | Benchmark setup | Generic native crossing and numeric switch; SDL query | Keep as benchmark environment metadata outside RuntimeDiagnostics. |
| Prefetch failed outcome | PREFETCH | COUNTER COUNT | PRODUCTION_SAFE | Per failed entry | Increment while existing queue lock is held | Keep as a failure-only count; do not add another lock or retain exception strings. |
| Prefetch requests/ready/not-prefetchable, pending/active entries and worker-running state | PREFETCH | COUNTER COUNT; GAUGE ENTRIES | RUNTIME_OPTIONAL | Per request/completion; worker state | Branch + increment under existing `LOCK`; gauge read currently synchronizes | Keep bounded totals and direct state gauges; no queue traversal. Skip extra reads/updates when the group is off. |
| Total preparation, decode-worker and UI-dispatch wait duration; thread create/start, adopt, finish-preparation/bookkeeping timers | PREFETCH | COUNTER COUNT; TIMER NANOSECONDS | RUNTIME_OPTIONAL | Per entry/callback | Clock reads and counter updates under `LOCK` | Keep coarse preparation/decode/UI-wait timers. Treat thread-start and fine bookkeeping phases as compile-time profiling detail. |
| Worker poll/sleep requested vs elapsed, semaphore release/acquire/wake counts and outstanding wake count | PREFETCH | COUNTER COUNT; TIMER NANOSECONDS | TEST_ONLY | Worker loop / blocking primitive | Clock reads and synchronized updates; a poll measurement runs each idle loop | Keep only in poll-vs-semaphore benchmark tests; it measures implementation choice, not field health. |
| Flick callback interval/lateness/delta error, scroll/paint/work duration | SCHEDULING | TIMER NANOSECONDS; COUNTER COUNT | RUNTIME_OPTIONAL | Per-frame | Two or more clock reads per frame; smoke recorder also allocates/stores a `Frame` | Keep aggregated frame timing under a scheduling group; no observer object or per-frame allocation in production. |
| Flick forced driver/clock, synthetic motion/start offset, injected driver host/listener and frame position | SCHEDULING | GAUGE/COUNTER | TEST_ONLY | Startup and per-frame test | Test setup, callbacks, optional timestamps | Keep in smoke tests. Do not expose as mutable runtime diagnostics controls. |
| `TC_THREAD_YIELD_MODE` native-vs-legacy mode | THREADING | — | TEST_ONLY | Blocking/yield path | Environment lookup once; changes actual scheduling | Keep only as benchmark/test setup if still needed, compile-gated; it is not an observation metric. |
| Semaphore waiter confirmation (`SemaphoreTestDiagnostics.awaitWaiters`) and diagnostic waiter state | THREADING | GAUGE ENTRIES | TEST_ONLY | Blocking primitive | Native crossing, lock/condition wait; Windows also creates per-waiter events and traverses waiter records | Keep test-only behind `TC_ENABLE_SEMAPHORE_TEST_DIAGNOSTICS`; no production waiter metric. |

## Native bridge migration

Historical native declarations live in `NativeMethods.txt`, generated
prototypes/registrations, `image_NativeImageBacking.c`, and Skia internals.
`NativeImageBacking.metricForTest(int)` / `tuiNIB_metricForTest_i` calls
`skia_benchmark_native_metric(int32)`, whose lower IDs expose target/backend/SDL
metadata and whose `>=100` range forwards to
`skia_image_backing_diagnostic_metric`. That native switch includes rejection
arrays, mapping subreasons, save-count buckets, unique-key sizes, geometry phase
timers, and pixel totals. This is a useful consolidation precedent, but all of
it is explicitly test/benchmark accounting.

The remaining dedicated getter families (`backing*`, `writePixels*`,
`physicalIdentity*`, `targetColor*`, `variant*`, `compactDecode*`, `promotion*`,
`screenUpdate/Present*`, and rejection getters) should be removed from the
production ABI. Map only the surviving native production metrics above to
generic internal IDs and batched reads. Leave detailed reason getters in
compile-time test support or eliminate them. `writePixelsFrameResetTest` and
`writePixelsFrameMetric(int)` stay test-only; `c56afb40c` renamed the latter to
fit the native signature limit, it did not generalize the bridge.

`Image.setDiagnosticAccountingTestNative`, native accounting reset/clear,
`createEmptyForTestNative`, `nativeOptimizationMaskObservedForTestNative`,
`mutateForTestNative`, and native failure-injection methods remain test-only.
`SemaphoreTestDiagnostics.awaitWaiters` remains a smoke-test native entry point.
`System.nanoTime()` is a general clock service, not a diagnostics bridge. Flick
and prefetch timers can use it only after their Java group gate.

## Test-only support

- Allocation/decode/adoption fault injection: decoded raster and frame-buffer
  allocation failures, native materialization and zero-copy failures, native
  snapshot/promotion/adoption failures, and native backing mutation.
- Synthetic backing/image factories; forced optimization masks and native mask
  probes; forced source/format/scale choices used only by benchmark workloads.
- Semaphore waiter-entry confirmation, artificial UI-inline/before-adoption
  hooks, worker-mode/sleep overrides, and worker shutdown/reset controls.
- Synthetic Flick clocks/drivers/hosts/motion/start offsets and frame observers.
- Scroll raster viewport hashes, geometry strings, last-result arrays, and
  detailed per-frame write-pixel measurements.
- High-detail native rejection/mapping/save-count buckets and geometry phase
  instrumentation. Keep the production JPEG decode-policy factories separate;
  they are behavior APIs, not diagnostic hooks.

## Snapshot/reset semantics

Cumulative counters and cumulative timers can be subtracted when both snapshots
use the same metric set and process epoch. Return signed deltas for byte/count
gauges only when callers explicitly want net change. A current live count/byte
gauge is not resettable; a peak gauge is compared as an absolute observation,
because `after - before` does not describe peak growth reliably. Resetting a
peak may establish a new baseline only if its semantics are documented.

Do not reset live backing gauges: historical
`skia_image_backing_clear_accounting_counters_for_test()` zeroes live counts and
bytes even if backings still exist. Per-scroll/per-frame `last*` fields are
samples, not monotonic counters. Reset only selected cumulative groups at a
quiescent benchmark boundary; production snapshots/deltas should not require a
global reset. Test counter reset APIs remain in compile-time test support.

Snapshot construction must use direct fields/IDs, not scan a metric registry.
Disabled snapshots must take the cheapest gate before allocating arrays or
crossing native code. Java metrics remain readable through the same conceptual
snapshot without routing through C; native reads batch requested IDs. Reject or
clearly mark deltas across different metric sets/epochs.

## Compile-time gating

Add a build-time diagnostics option for all optional metric storage and update
sites. When it is off, those fields, helper calls, native registrations, clocks,
and bridge code should be absent. `PRODUCTION_SAFE` failure counters are the
only proposed default-build observation. In a diagnostics-capable build,
`RUNTIME_OPTIONAL` groups still need a first-operation runtime gate. All
`TEST_ONLY` fault hooks, synthetic factories, benchmark forcing, detailed
rejection arrays, frame samples, and semaphore waiter support remain in
test/smoke configurations. The existing semaphore macro is a pattern; the
image accounting flag and `DIAGNOSTIC_ACCOUNTING` optimization-mask bit are not
compile-time boundaries and should not define the future public configuration.

## Runtime-disabled cost

- **Image/decode**: gate before counters, byte/pixel accounting, timer reads,
  native accounting helpers, or requested-route work done only for metrics.
  Never call the current native field-name accounting helper from a hot path
  when the group is off.
- **Raster/draw/scroll**: gate before each metric helper and every timer. No
  per-draw metric lookup, fallback-name formatting, pixel scan, synchronization,
  or native query when disabled. Per-pixel paths must have no diagnostics branch
  when diagnostics support is not compiled.
- **Frame scheduling**: no `nanoTime`, observer dispatch, or sample allocation
  before the scheduling gate. Aggregate only after it is enabled.
- **Prefetch/worker**: no metric clock reads, additional `LOCK` acquisition,
  poll accounting, or wrapper allocation when disabled; the prefetcher's normal
  synchronization remains runtime behavior and is not a metric.
- **Snapshot/native**: the disabled gate precedes metric-ID resolution, array
  allocation, registry access, and `readMetric(s)` crossing. Java-owned metrics
  need no native bridge.

## Migration order

1. Define internal metric metadata, build-time exclusion, group gates, and
   snapshot/delta/reset semantics. Implement the generic native read/batch/reset
   boundary before any feature-specific instrumentation; leave IDs private.
2. Add rare failure counters and direct memory gauges, then coarse prefetch
   outcome/state and aggregate timing groups.
3. Add scheduling aggregates and rendering scroll-reuse totals/reasons, with
   runtime-disabled path checks before clocks or helper calls.
4. Add image decode/materialization and native raster fast-path aggregates.
   Keep detailed format, rejection, mapping, scratch, and per-frame accounting
   in diagnostic/test builds.
5. Move benchmark harnesses to snapshots/deltas, retain deterministic hooks in
   test-only source sets, and remove superseded per-metric native declarations.
