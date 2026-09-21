<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Scroll raster reuse POC evidence

## 2026-09-21 — M0 bootstrap

- revision: `c57c985a9c5e2030ee72a989b5b5917d932103dd`
- branch: `perf/scroll-raster-reuse-poc`
- base: `perf/writepixels-tail-diagnosis`
- status: verified
- command: `git switch -c perf/scroll-raster-reuse-poc perf/writepixels-tail-diagnosis`
- result: target branch created from the exact required base; no build run.
- scope: unrelated local artifacts preserved and excluded from the bootstrap
  commit.

## 2026-09-21 — M0 documentation checkpoint

- revision: `b91ab518ead60beba1002f60edb94f9d389086ef`
- status: committed
- paths: plan, state, and initial evidence only
- validation: focused copyright-header validation and staged diff check passed;
  the local commit-message checker reported one body line over 80 characters.
- limitation: no history rewrite; later commits use wrapped body lines.
