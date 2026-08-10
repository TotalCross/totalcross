<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Harden TCM/J2TC Boundary — Milestone History

## Milestone 0 — frozen baseline

Completed 2026-08-10 at source revision
`441c5785dd88a6aaf8c028c2a390c27d113ad0d6`.

The reusable `captureTcmBoundaryBaseline` task converts the aggregate modern
Java smoke workload in-process, discards three warmups, records ten samples,
summarizes collected metadata, and preserves the emitted baseline v1 sidecar in
ignored build output. Its test harness resets only public/test-reflected
per-deploy converter tables between identical samples; production code is not
instrumented.

Both modes collected identical metadata, demonstrating that default-off was
only an emission policy at baseline. The full counts, timing samples, fixture
hash, command logs, and scope limitation are in the evidence index.

## Milestone 1 — disabled capture and linear origins

Completed 2026-08-10 after focused validation.

`J2TC.process()` now chooses one conversion-scoped capture implementation. AOT
uses the real collector; `NONE` uses a singleton no-op implementation that
allocates no class, method, bytecode-site, call-site, origin, or synthetic
metadata. Normal `Class.forName` deploy discovery remains independent. The same
semantic capability flows into class parsing, so disabled conversion skips each
StackMap attribute by its declared length while explicit metadata parsing keeps
the existing diagnostics and frames.

The collector indexes AOT source sites by Java PC, scans final lowered
instructions once to compute slot and call ranges, then walks source sites once
to preserve ordered and empty origins. Existing branch-promotion tag propagation
is covered by a regression fixture, and dynamic lowering ranges remain valid.

The fixed workload produced zero disabled metadata, unchanged AOT counts, an
unchanged v1 fixture hash, and byte-identical `NONE`/AOT TCZ output. A noisy
first timing pass was followed by one bounded repeat; both medians were within
5% of baseline. Details and logs are in the evidence index.

## Milestone 2 — stable wire and safe publication

Completed 2026-08-10 after focused compatibility and failure validation.

Native, invoke, and synthetic enums now carry their documented permanent v1
codes, and the reader searches those codes with precise unknown-value errors.
No enum ordinal controls wire bytes. The preserved aggregate v1 fixture decoded
through the production reader, and new output retained its exact SHA-256.

`TcmArtifacts` now owns sidecar naming, ordered artifact construction, bounded
16 KiB streaming SHA-256, and manifest validation for both writer and reader.
`TcmPublisher` owns temporary-file publication and exposes a package-private
filesystem seam. Tests prove ordinary replacement, atomic-not-supported
fallback, previous-sidecar preservation on injected pre-replacement failure,
and owned-temporary cleanup. Documentation deliberately makes no fsync or crash
durability claim.

## Milestone 3 — canonical converter semantics

Completed 2026-08-10 after focused validation on the required semantic paths.

`GlobalConstantPool.javaType2TCType` is now the single descriptor-lowering entry
point used by production conversion and metadata. It preserves the established
primitive, object, array, 4D-name, and scalar-float-to-TC-double behavior while
keeping source descriptors intact in metadata.

`MethodDeclarationResolver` now serves both device-call validation and TCM
capture. It consults active parsed program classes first, then guarded
TotalCross/jdkcompat device classes and the explicit Properties-to-Hashtable
mapping. It never loads a host `java.*` class. Bytecode symbolic owners remain
unchanged, constructors never search mapped superclasses, and unresolved facts
remain unresolved. Focused tests passed on JDK 17; installed JDK 11 could not run
the Java-17 Gradle/project bytecode, so no cross-runtime result is claimed.
