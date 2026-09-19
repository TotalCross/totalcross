<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — device-space writePixels policy

## 2026-09-18 — bootstrap

- Revision: `8ab1c6b28a6238c2e2aea53afcd6103bfc46d45b`.
- Branch: `perf/image-decode-distributed-benchmark`.
- Plan 1 closure and the prepared Plan 2 specification were verified; Plan 2
  specification commit: `9594720fa`.
- The fixed device-space mapping, explicit clip/source subset, regular counter,
  native test, and 24-process benchmark requirements are preserved in the
  active plan.
- No implementation, SDK/native build, smoke, package, or focused benchmark
  execution has run for Plan 2.
- Next milestone: implement the shared device-space planner and regular-path
  accounting in `skia_image_backing.cpp`.
