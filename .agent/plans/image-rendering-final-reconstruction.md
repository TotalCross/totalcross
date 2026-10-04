<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Image/rendering final reconstruction — completed plan record

## Purpose

Complete the production reconstruction on `fix/image-rendering-final-reconstruction`: return physical-copy misses to Java resolution, restore the typed opaque `writePixels` path with conservative fallback, and admit a final raster immediately only for a persistently owned UI image. The branch also records candidate measurements and audits the historical feature chain before PR review.

## Completed architecture and decisions

- `Graphics.copyRect(Image, ...)` consumes a native physical-copy probe only on success. A miss returns to Java for normal image resolution and copying.
- Opaque raster writes require proven opacity, compatible software pixels, exact integral device 1:1 sizing, safe clipping and non-overlapping storage. Any failed or ineligible write uses the existing draw fallback.
- Each pipeline retains at most one exact final materialized representation. Generic or transient resolution uses second-observation admission. Drawing for a consumer already registered through the persistent UI ownership lifecycle may admit its first successful materialization immediately.
- `ImageControl` ownership follows attachment, image replacement, reparenting, removal and collected controls. Shared owners retain the pipeline raster until the last owner leaves. Native `TARGET_COLOR` and `PHYSICAL` variants keep their existing second-observation policy; no fields were added to `Image`.

## Completed milestones

The implementation and integration history remains in separate commits:

1. `3be3a1eaf` — return physical-copy misses to Java.
2. `f45b0a1f9` — restore typed opaque raster writes.
3. `9896ec9ad` — separate final-raster admission modes.
4. `c7bf28d09` — retain final rasters for attached controls.
5. `2f81accfb` — keep the owner registry deploy compatible.
6. `bdd27273ead29bab320cba43b9ebad27be9b87ad` — integration validation and the exact production candidate used for benchmarking.
7. `7ad76e94a` — independent production-candidate benchmark evidence.

The historical feature audit and this cleanup are recorded in one final documentation/audit commit. The audit maps required behaviors to merged work or the branch corrections and finds no required production behavior classified `STILL MISSING`.

## Validation and limits

The focused integration selection passed 317 SDK tests (20 skipped), SDK distribution, Skia surface assertions, eight deployed image smokes and the macOS scroll-reuse smoke with `nativePrimitive=true`. The production benchmark is tied exactly to candidate `bdd27273ead29bab320cba43b9ebad27be9b87ad`; its hashes, commands, results and unsupported diagnostics are indexed in `.agent/evidence/image-rendering-final-reconstruction.md`.

GitHub Actions Merge Flow run `37172383967` completed successfully for the pre-cleanup branch head. Windows and `windows-native-legacy` builds passed, and the run produced a Windows artifact. This is Windows build validation; the dedicated Windows performance benchmark was not executed. The final report records the distinction and the run evidence.

P12 investigations, production benchmarks, and platform benchmark matrices are accepted historical evidence and were not repeated during this documentation cleanup. The diagnostics-off candidate cannot report repeated-scroll admission/materialization counters, writePixels counts, materialization time or derived-raster memory; these values remain unmeasured.

## Outcome and retrospective

The branch is implementation-complete and awaiting PR #488 review/merge. The feature audit found no missing required production behavior. The candidate benchmark SHA remains unchanged and reachable; later history cleanup is limited to documentation and comments. Detailed feature mapping is in `.agent/reports/image-rendering-reconstruction-final.md`, and exact measurement provenance remains in the evidence index.
