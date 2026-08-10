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
