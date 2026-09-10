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
