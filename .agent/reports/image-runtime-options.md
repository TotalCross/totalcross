<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Production Image runtime options

## Summary

Image storage, raster variant behavior, scroll raster reuse, and image
preparation worker selection now use typed `@ImageRuntimeRule` properties.
Deployment metadata, startup resolution, the simulator, production consumers,
and the runtime configuration report share the same resolved policy. Existing
storage-only source rules and version-1 deployed metadata remain compatible.

## Public API

`RuntimeFeatureState` provides `DEFAULT`, `ENABLED`, and `DISABLED`.
`ImagePrefetchWorkerMode` provides `DEFAULT`,
`LEGACY_PER_ENTRY_THREAD`, and `SEMAPHORE_PROCESS_WORKER`.
`ImageStorageProfile.DEFAULT` is an annotation sentinel; resolved storage is
always `STANDARD` or `COMPACT`.

`ImageRuntimeRule` accepts optional `storage`, `targetColorConversion`,
`physicalVariantCache`, `scrollRasterReuse`, and `prefetchWorker` properties.
An all-default rule is rejected. Old storage-only rules remain valid.

## Defaults

| Setting | No matching assignment |
| --- | --- |
| Storage | `STANDARD` requested and effective |
| Target color conversion | Disabled |
| Physical variant cache | Disabled |
| Scroll raster reuse | Disabled |
| Automatic preparation | Disabled and not configurable |
| Prefetch worker | `LEGACY_PER_ENTRY_THREAD` |
| Stable raster core | All six established defaults enabled |

Compact storage remains opt-in. If compact backing is unavailable, the policy
reports `STANDARD` as effective and gives the downgrade reason.

## Rule composition

Each property is resolved independently through the shared runtime selector
resolver. A more-specific assignment overrides a less-specific assignment for
that property. Equally specific rules can assign different properties, and
same-value duplicate assignments are accepted. `DEFAULT` is ignored as an
assignment.

## Conflict behavior

Two equally specific rules that assign different values to the same property
fail startup with a deterministic configuration conflict naming the property,
rules, and values. This preserves the existing selector specificity rules
without introducing Image-specific precedence.

## Metadata compatibility

Metadata version 2 encodes a five-property presence mask and stable value tags.
It writes only explicit assignments; it does not serialize enum ordinals or
`DEFAULT`. Decode rejects unsupported versions, unknown presence bits or tags,
empty rules, truncated input, and trailing bytes.

Version 1 remains readable as a storage-only rule. Its newer properties resolve
to their no-assignment defaults. Deployment pruning removes rules whose
selectors no longer target the deployment, retains all assigned properties on
surviving rules, and leaves the application on defaults when every rule is
pruned.

## Deployment and simulator behavior

The converter, deployed startup, and simulator share the typed rule value and
property resolver. The simulator waits for the graphics backend to finalize
before resolving rules. Tests cover deployed/simulator parity, sparse version 2,
version 1 decoding, malformed metadata, and pruning.

## Resolved policy

`RuntimeConfigurationReport` describes requested and effective storage, a
reason (`none` when there is no downgrade), the six enabled raster-core
defaults, target color conversion, physical variant caching, scroll reuse,
automatic preparation, prefetch worker, and matched rules. It is a
human-readable diagnostic snapshot, not a stable machine protocol.

Feature eligibility remains with its consumer. An enabled request does not
promise that every draw or scroll takes an optimized path; ineligible work
uses the existing safe fallback.

## Production integration

- P3 target color conversion and physical variant caching read the resolved
  policy. Separate deployed fixtures exercise each path: the physical-cache
  fixture disables target conversion so that conversion cannot satisfy the
  draw before the physical-cache path is considered.
- P4 STANDARD and COMPACT production backing paths retain their pixel and
  storage checks.
- P7 uses the annotation-resolved policy without test policy injection for its
  first vertical scroll and confirms row reuse. The deployed smoke uses the
  legacy software renderer at unit scale so the native row-move primitive is
  eligible.
- P8/P9 exercise explicit preparation, the configured semaphore worker, worker
  lifecycle, stale results, retries, callbacks, and draw reuse. The default and
  explicit legacy worker path remains covered.
- P10 static PNG preparation passes with both the semaphore worker and the
  legacy worker.
- P3, P4, P5, P6, P7, P8, P9, and P10 macOS regression smokes pass.

## Artifact boundary

Internal `ImageRuntimeOptions` implementation classes are excluded from the
public API jar and aggregate SDK jar. `artifactContentTest` verifies the
boundary.

## Diagnostics

The stable raster-core defaults are policy values, not new runtime toggles.
Diagnostics ON and OFF produce equivalent option resolution. No diagnostics
domain, counter ID, or native ABI was added. Diagnostic accounting and P11
scheduling experiments remain outside production configuration.

## Validation

- Full SDK tests passed with diagnostics OFF and ON.
- Focused parser, metadata, startup, selector bridge, simulator, and deployment
  tests passed.
- `artifactContentTest`, `compileSmokeTestJava`, and `dist -x test` passed.
- macOS ARM64 Release `tcvm` and `Launcher` builds passed with the regular
  software/Skia/SDL configuration. A software/legacy/SDL build was also used
  for P7 because that native renderer supplies the row-move primitive.
- Configured metadata, default-policy, storage, raster, preparation, worker,
  and PNG deployed smokes passed. One long sequential run ended with a
  transient SIGBUS during P10; isolated semaphore and legacy P10 reruns both
  passed all checks.
- The focused copyright-header validator and `git diff --check` passed.

## Historical option mapping

| Historical option family | Production treatment |
| --- | --- |
| Zero-copy decode, opacity metadata, opaque pixel writes, row readback, direct color materialization, physical identity folding | Retain their stable enabled defaults; no public toggle is needed |
| RGB565, gray8, and ARGB4444 storage selection | Map to `storage=STANDARD` or `storage=COMPACT`; the existing compact selector chooses the eligible backing format |
| Raster target color conversion | Map to `targetColorConversion` |
| Raster physical variant cache | Map to `physicalVariantCache` |
| Scroll raster reuse | Map to `scrollRasterReuse` |
| Legacy per-entry and semaphore process workers | Map to `prefetchWorker` |
| Cache byte budget, memory-pressure eviction, GPU CPU-backing discard, mmap storage | Leave out of production configuration: the historical switches had no production consumer; the memory-pressure hook was a no-op |
| Diagnostic accounting | Keep behind the diagnostics/test gate; it is not an application runtime option |

Raw image and rendering masks are not part of the new API.

## Known limitations

COMPACT may resolve effectively to STANDARD when native compact backing is not
available. Scroll reuse still applies only where its renderer, surface, scale,
viewport, and damage checks permit a safe native row move; an enabled policy
can therefore fall back on other configurations. The local P7 run used the
legacy software renderer and unit scale to exercise the eligible production
path.

## P12 handoff

P12 benchmarks should select named typed options on their application entry
class and record `RuntimeConfigurationReport`. Workloads should include the
resolved policy in their run evidence. Do not use raw masks or test-only policy
injection to select benchmark behavior.
