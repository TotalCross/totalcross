<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image scroll prefetch execution state

- branch: `perf/image-scroll-prefetch`
- parent_head: `702139cedfad946cde133f8bfaeb4ec4ad239897`
- active_milestone: 4 — final benchmark and closeout
- active_slice: customer benchmark harness committed; final matrix and closeout evidence pending
- last_commit: `4d2d40fc3`
- active_paths: `.agent/state/image-scroll-prefetch.md`,
  `.agent/evidence/image-scroll-prefetch.md`,
  `scripts/run-image-scroll-real-workload-benchmark.py`,
  `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/`,
  focused image/ScrollContainer smoke fixtures
- next_action: run the final 16-process benchmark matrix, focused final smokes,
  and commit the evidence/report
- focused_validation: baseline runner passed 24 records across eight fresh
  profiles using exactly 663 JPEGs; focused SDK tests, macOS tcvm build,
  ImagePreparation macOS smoke, benchmark harness tests, and one representative
  prefetch-enabled real-workload process passed
- deferred_validation: the caller shell does not export `TC_IMAGE_CORPUS`; both
  baseline and final runs use the verified local corpus path explicitly; final
  matrix and closeout native smoke remain pending
- decisions_active: `PREFETCH_ALL`, explicit asynchronous API, one decode
  worker, detached worker candidates, UI-thread adoption, no eviction
- blockers: none; `TC_IMAGE_CORPUS` is available as
  `/Users/flsobral/Downloads/win32` but is not exported by default
- deliberate_out_of_scope: pre-existing untracked files and generated/local
  artifacts in the shared worktree are preserved and are not plan changes
- resume_command: `cd /Users/flsobral/repos/totalcross-image-scroll-raster-fast-path &&
  git switch perf/image-scroll-prefetch && sed -n '1,220p'
  .agent/state/image-scroll-prefetch.md`
