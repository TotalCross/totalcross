<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image/rendering reconstruction audit

## Audit basis and verdict

The reconstruction branch starts at `origin/master` commit `5a44f503bf6fa1bec350f1218f4d501a70fc4812`. The audit maps the requested image/rendering/scroll behavior to merged production work and the branch corrections below.

**Verdict:** no required production behavior in the requested chain is classified `STILL MISSING`. The production candidate restores the intended copy, opaque-write and admission behavior while preserving the bounded cache and native variant policy.

## Feature map

| Required behavior | Classification | Evidence in merged base or this branch |
| --- | --- | --- |
| Logical UI scaling prerequisites for image destination sizing | `MERGED` | Surface content scale and image destination-scale handling: `4a33a6e92c`, `67f72de438`, `133dc7047b`, `dbcefa9d35`. This audit does not claim the separate broader logical-UI-scaling project is complete. |
| Native `Image` backing and resource ownership | `MERGED` | Backing abstraction, native Skia backing, and raster backing ownership: `f087c4f4eb`, `69380896fb`, `922083ef23`. |
| Lazy decode and deferred image operations | `MERGED` | Encoded-source foundation, safe deferred materialization, native draw plans, and execution: `63bd6bcd88`, `fa9d0ac3f5`, `de94251f0d`, `3579c7e7c2`. |
| Adaptive JPEG decode behavior | `MERGED` | Adaptive encoded-image decode and JPEG request policy: `3998ee1bd8`, `793cad23ef`. |
| Zero-copy/native decode, opacity metadata, row readback, and direct color materialization | `MERGED` | Bounded raster-core paths: `b3bf92034f`; typed policy paths retain conservative fallback. |
| Opaque `writePixels` in the NativeImageBacking draw path | `MERGED + CORRECTED HERE` | Raster-core implementation: `b3bf92034f`. `f45b0a1f9` reconnects it through deferred geometry and ordinary draws with proof-based eligibility and Skia fallback. |
| Physical identity drawing | `MERGED` | Exact physical-identity raster path: `a21601d35a`; direct physical-copy validation/fix: `d5c66f7bc4`. |
| Target-color and physical raster variants | `MERGED` | Variant implementation and bounded shared slot: `013350af97`, `d27351800b`; both retain second-observation admission. |
| Compact image storage | `MERGED` | Immutable compact decode and mutation/variant preservation: `df0e8bc6f0`, `021e18d58e`. |
| Lazy JPEG factories | `MERGED` | Deployed factories route through the lazy pipeline: `7a908990b9`; request decode policy: `793cad23ef`. |
| `copyRect` final-raster reuse and deferred draw plans | `MERGED + CORRECTED HERE` | Existing reuse and direct-copy work: `1496ef5bc4`, `f6eca87ee5`, `d5c66f7bc4`. `3be3a1eaf` returns physical-copy misses to Java for normal resolution and copying. |
| Generic/transient versus persistent final-raster admission | `MERGED + CORRECTED HERE` | `9896ec9ad` keeps generic resolution on second-observation admission and adds internal immediate admission. `c7bf28d09` and `2f81accfb` tie that mode to persistent `ImageControl` ownership and last-owner cleanup. |
| Scroll framebuffer raster reuse | `MERGED` | Bounded move, vertical scroll reuse, and damage/fallback recovery: `69cdacb931`, `4534058eae`, `cd43530883`. Unsupported backends conservatively repaint. |
| Explicit async display preparation | `MERGED` | Request capture, worker/adoption, and UI-thread lifecycle: `1bc652c2d0`; preparation does not change normal scroll admission. |
| Semaphore-backed prefetch worker | `MERGED` | Worker policy and execution: `96c9fcf96`, `dda302b85c`. |
| Static PNG preparation | `MERGED` | `3f64be3a5a`, with lifecycle and deployed validation in adjacent test commits. |
| Pacing/`nanoTime` measurement support | `MERGED` | Native monotonic mapping and macOS smoke: `af54bc9cbd`, `eb77521ac3`. Timing is benchmark/diagnostic evidence; no additional production pacing policy is required here. |
| Typed image runtime configuration | `MERGED` | Typed options, deployment metadata and image policy persistence: `fab56343f4`, `fb2ffc3add`, `9156bce301`. |
| Diagnostics/testing/tooling separation | `MERGED`; probe outputs are `BENCHMARK/DIAGNOSTIC ONLY` | Generic snapshot API: `9343d9e58b`; counters do not select production paths. |
| Raw optimization masks, obsolete switches and duplicate experimental benchmark scripts | `INTENTIONALLY DROPPED` | They are not required runtime behavior; typed runtime policy and the single-slot cache are current contracts. |

No required production behavior is classified `STILL MISSING`.

## Branch corrections

The implementation commits remain separate and ordered:

1. `3be3a1eaf` — return physical-copy misses to Java.
2. `f45b0a1f9` — restore typed opaque raster writes.
3. `9896ec9ad` — separate second-observation and immediate final-raster admission.
4. `c7bf28d09` — retain final rasters for attached controls with shared-owner cleanup.
5. `2f81accfb` — keep the owner registry deploy-compatible.
6. `bdd27273ead29bab320cba43b9ebad27be9b87ad` — integration validation and production candidate.
7. `7ad76e94a` — independent production-candidate benchmark evidence.
8. `docs(image): finalize rendering reconstruction audit` — final audit and documentation cleanup.

The final-raster slot remains one per pipeline. Generic/transient resolution waits for the second observation; a consumer already registered through the persistent UI ownership lifecycle can admit its first successful materialization. Native `TARGET_COLOR` and `PHYSICAL` variants retain their existing second-observation policy. Shared `ImageControl`s retain the image until the last attached control releases it.

## Candidate benchmark

All production-candidate benchmark evidence is tied exactly to TotalCross commit `bdd27273ead29bab320cba43b9ebad27be9b87ad`. The later benchmark-evidence and final documentation commits do not change runtime behavior.

The static warm 540x960 logical / 1080x1920 drawable / scale-2 run recorded 18 cached-final hits, zero misses, and no generic-geometry or smooth-resample draws. `paintTree` took 5.509 ms and cumulative image paint took 4.984 ms.

The repeated-scroll candidate run passed with 3 warmups and 10 measured runs. Across 9,720 route frame intervals, none exceeded 100 ms. Cold-forward p50/p95/max was 70.456/73.900/98.178 ms; warm-reverse was 16.348/16.743/30.483 ms; warm-forward was 16.313/16.627/47.995 ms.

The diagnostics-off candidate cannot report repeated-scroll admissions/materializations, writePixels counts, materialization time, or live/peak derived-raster memory. These values were not inferred. The generic-admission regression confirms first-use leaves the final-raster slot empty and second observation admits one entry; this is cache-state evidence, not an RSS measurement. Exact commands, package hashes, dataset identity and raw result paths are in `.agent/evidence/image-rendering-final-reconstruction.md`.

## Validation and known limits

The focused integration selection passed 317 SDK tests (20 skipped), SDK distribution, diagnostics-off Skia surface assertions, eight deployed Skia image smokes, and the legacy macOS scroll-reuse smoke with `nativePrimitive=true`.

PR #488 Merge Flow run [37172383967](https://github.com/TotalCross/totalcross/actions/runs/37172383967) completed successfully for the pre-cleanup branch head `d83ea1d1c98bab4fe04f715eddd4be2cb29707c4`. The `windows` and `windows-native-legacy` builds passed; the run produced a non-expired `windows` artifact. The SDK, macOS ARM64, iOS, Android, Linux ARM32v7, Linux ARM64 and Linux AMD64 jobs also passed; `linux-arm32v7-cross` was skipped. This proves Windows build validation passed. The dedicated Windows performance benchmark was not executed, so Windows performance remains unmeasured.

The history cleanup preserves that run as pre-cleanup CI evidence; the force-pushed branch starts a fresh CI run. P12 investigations and production benchmarks were not rerun during cleanup.

## Superseded draft PRs

GitHub reports all six legacy image drafts as open and draft. They were not closed or modified. Their work is superseded by the merged production feature commits above and the final corrections in this branch.

| Draft PR | Current title | Superseding production work |
| --- | --- | --- |
| [#457](https://github.com/TotalCross/totalcross/pull/457) | Perf/image opt phase1 controls | Typed image runtime configuration and bounded raster-core path. |
| [#458](https://github.com/TotalCross/totalcross/pull/458) | Perf/image opt phase2 raster | Raster core, variants and typed `writePixels` correction (`f45b0a1f9`). |
| [#459](https://github.com/TotalCross/totalcross/pull/459) | Perf/image opt phase3 formats | Compact storage and bounded variant implementation. |
| [#460](https://github.com/TotalCross/totalcross/pull/460) | Perf/image jpeg factories lazy | Lazy JPEG factories and adaptive decode policy. |
| [#461](https://github.com/TotalCross/totalcross/pull/461) | Perf/image scroll raster fast path | Merged copy/scroll reuse plus physical-miss correction (`3be3a1eaf`). |
| [#462](https://github.com/TotalCross/totalcross/pull/462) | Perf/image scroll prefetch | Merged explicit preparation, Semaphore worker and PNG preparation. |

PR #488 is pending review/merge. Windows build validation is complete; the dedicated Windows performance benchmark remains unexecuted and is not evidence of missing production behavior.
