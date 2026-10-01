<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image Scroll Raster Fast Path — Evidence Index

## Milestone 1: plan-aware `copyRect`

- `./gradlew-agent test --tests totalcross.ui.gfx.GraphicsDeferredImageTest` — passed.
- `./gradlew-agent dist -x test` — passed.
- macOS ARM64 Release CMake build for `tcvm` and `Launcher` — passed. The local configure used the supported legacy QR-code backend because the default QRCodeGen asset returned 404, and selected the installed SQLite `r2` release tag for discovery.
- `runImageScrollRasterFastPathSmokeMacOS` — passed. Three plan attempts were handled, zero fell back; source-subrect/destination parity, translated partial clipping, empty-intersection no-mutation, and deferred-source preservation all passed.
- `python3 scripts/validate-copyright-headers.sh --files …` — passed for the changed first-party sources.
- `git diff --check` — passed.

## Deferred

Milestones 2–4 validation remains open: cache validity and artifact boundaries; physical direct-copy correctness and fallback coverage; diagnostics-on tests; warm microbenchmark; optional 663-image workload; final artifact/distribution checks; and Merge Flow.
