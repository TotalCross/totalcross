<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image scroll prefetch evidence index

- 2026-09-10 | milestone 0 | branch | pass | parent local and remote head
  `702139cedfad946cde133f8bfaeb4ec4ad239897` matched; created and pushed
  `perf/image-scroll-prefetch` | no benchmark artifact
- 2026-09-10 | milestone 0 | baseline benchmark | pass | exact 663-JPEG corpus,
  two resolutions, four feature-13/14 profiles, three passes each; runtime
  `efef5fb8b062df88054daa7c2e4aeeff98b1b1dd1c2ea49e00aad48ce188a61a` | raw
  logs/results and summary in `.agent/benchmarks/image-scroll-prefetch/baseline/`
- 2026-09-10 | milestone 0 | commit-message check | fail | `09cdb5da9` was
  committed and not rewritten; the validator reported one body line over 80
  columns; future commits will use the required wrapped format
- 2026-09-10 | milestone 1 | focused validation | pass | Image preparation SDK
  tests passed; macOS `tcvm` Release build passed; `git diff --check` passed
- 2026-09-10 | milestone 2 | focused validation | pass | traversal and existing
  ScrollContainer tests passed; ImagePreparation macOS smoke passed with
  detached adoption, UI completion, responsive timer, deferred plan, and one
  targeted decode before first draw
- 2026-09-10 | milestone 3 | benchmark harness | pass | disabled/all-prefetch
  profiles added to the 663-JPEG customer workload; focused SDK tests and one
  representative prefetch-enabled process passed
- 2026-09-10 | milestone 4 | final benchmark and smoke | pass | 16 fresh
  processes, 48 pass records, exact 663-JPEG corpus, and final
  `ImagePreparation` macOS smoke passed; raw logs/results/summary are in
  `.agent/benchmarks/image-scroll-prefetch/final/`, editorial report is in
  `.agent/reports/image-scroll-prefetch-editorial.md`, runtime SHA-256 is
  `88e379320ab6c292830588d4291f73d24b142ed02ea4a67e2a35c68fb540a48d`
- 2026-09-10 | milestone 5 | corrected implementation | pass | `COPY_READY`
  now materializes/caches the final raster before copyRect scrolling, promotes
  joined work from `DRAW_READY`, removes terminal in-flight entries, releases
  detached handles exactly once, invalidates ancestor batches, and captures
  the native optimization mask per request; implementation commits are
  `c57f948e5`, `2879d6920`, `c7de9d72e`, `8adfb169c`, `decf5398f`,
  `db10fecb7`, `53bcc7d41`, and `203a8013f`
- 2026-09-10 | milestone 5 | corrected focused validation | pass | focused
  SDK tests, Release SDK `dist -x test`, benchmark macOS redeploy, and the
  exact ImagePreparation macOS smoke passed; smoke reported detached adoption,
  retry after injected adoption failure, UI completion, captured mask, and no
  extra decode during copyRect
- 2026-09-10 | milestone 5 | historical corrected final benchmark | pass | 16 fresh
  macOS processes and 48 records in
  `.agent/benchmarks/image-scroll-prefetch/final-fixed/`; all prefetch cold
  records were 663 requests/660 ready/0 failed/3 not-prefetchable, cold
  targeted decodes were 0, cold final/native materializations were at most 3,
  warm decodes/materializations were 0, live/peak backing after prefetch was
  `282405544--356793048`/`283405544--357793048`, after cold/warm was
  `295619032--370331016`, target-color and physical-variant bytes were zero,
  and the strict runner gates passed;
  runtime SHA-256 is
  `6ad937b552e9305a39b17c363b655fc50f93d977181cd3435a4fdd770797d72c`
- 2026-09-10 | milestone 6 | TCVM-compatible lock | pass | replaced the
  preparation coordinator's `Object` lock with `totalcross.util.concurrent.Lock`
  without changing synchronized sections or the one-active-candidate
  continuation architecture; commit `39998c5e5`; focused ImagePreparation,
  shared-source, ScrollContainer, and traversal tests passed
- 2026-09-10 | milestone 6 | authoritative final benchmark | pass | 16 fresh
  macOS processes and 48 pass records in
  `.agent/benchmarks/image-scroll-prefetch/final-definitive-pass/`; every
  prefetch run reported 663 requests/660 READY/0 FAILED/3 NOT_PREFETCHABLE,
  cold targeted JPEG decodes were 0, cold final/native materializations were
  at most 3, cold p95 was at most 5 ms, frames >=34 ms were at most 2, warm
  and warm2 JPEG decodes/materializations were 0 with p95 at most 5 ms, and
  target-color converted bytes and physical-variant bytes were 0; accounting
  reset after UI construction; shared-source regressions passed; runtime
  SHA-256 is `6ad937b552e9305a39b17c363b655fc50f93d977181cd3435a4fdd770797d72c`
