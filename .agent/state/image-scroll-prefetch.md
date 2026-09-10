<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image scroll prefetch execution state

- branch: `perf/image-scroll-prefetch`
- parent_head: `702139cedfad946cde133f8bfaeb4ec4ad239897`
- active_milestone: complete — final benchmark and closeout
- active_slice: final matrix, native smoke, evidence, and editorial report committed
- last_commit: `38446c9e1`
- active_paths: `.agent/state/image-scroll-prefetch.md`,
  `.agent/evidence/image-scroll-prefetch.md`,
  `scripts/run-image-scroll-real-workload-benchmark.py`,
  `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/`,
  focused image/ScrollContainer smoke fixtures
- next_action: recheck the remote head and push `perf/image-scroll-prefetch`
- focused_validation: baseline runner passed 24 records across eight fresh
  profiles; focused SDK tests, Release SDK distribution, macOS tcvm build,
  ImagePreparation macOS smoke, benchmark harness validation, and the final
  16-process/48-record matrix all passed
- deferred_validation: Android, Linux, Windows, and iOS builds were not run;
  the ExecPlan restricts local validation to SDK and macOS
- decisions_active: `PREFETCH_ALL`, explicit asynchronous API, one decode
  worker, detached worker candidates, UI-thread adoption, no eviction
- blockers: none; `TC_IMAGE_CORPUS` is available as
  `/Users/flsobral/Downloads/win32` but is not exported by default
- deliberate_out_of_scope: pre-existing untracked files and generated/local
  artifacts in the shared worktree are preserved and are not plan changes
- resume_command: `cd /Users/flsobral/repos/totalcross-image-scroll-raster-fast-path &&
  git switch perf/image-scroll-prefetch && sed -n '1,220p'
  .agent/state/image-scroll-prefetch.md`
