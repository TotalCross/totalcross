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
