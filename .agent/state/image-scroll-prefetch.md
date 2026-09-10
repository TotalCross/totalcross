<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image scroll prefetch execution state

- branch: `perf/image-scroll-prefetch`
- parent_head: `702139cedfad946cde133f8bfaeb4ec4ad239897`
- active_milestone: complete — corrected COPY_READY implementation and closeout
- active_slice: permanent readiness/parity tests and metric audit committed and pushed
- last_commit: `5b01e4195`
- active_paths: `.agent/state/image-scroll-prefetch.md`,
  `.agent/evidence/image-scroll-prefetch.md`,
  `scripts/run-image-scroll-real-workload-benchmark.py`,
  `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/`,
  focused image/ScrollContainer smoke fixtures
- next_action: none; local and `origin/perf/image-scroll-prefetch` both point to
  `5b01e4195`
- focused_validation: focused Java regressions including parity/fallback passed;
  Release SDK dist passed;
  Release macOS tcvm build passed; corrected 16-process/48-record matrix
  passed; ImagePreparation macOS smoke passed with adoption failure/retry,
  copyRect no-extra-decode, and captured detached optimization mask
- deferred_validation: Android, Linux, Windows, and iOS builds were not run;
  the ExecPlan restricts local validation to SDK and macOS
- decisions_active: `PREFETCH_ALL`, explicit asynchronous API, one bounded
  decode worker, DRAW_READY/COPY_READY requirements, detached worker
  candidates, UI-thread adoption, per-request optimization mask, no general
  eviction; release unreferenced COPY_READY intermediates
- blockers: none; `TC_IMAGE_CORPUS` is available as
  `/Users/flsobral/Downloads/win32` but is not exported by default
- deliberate_out_of_scope: pre-existing untracked files and generated/local
  artifacts in the shared worktree are preserved and are not plan changes
- resume_command: `cd /Users/flsobral/repos/totalcross-image-scroll-raster-fast-path &&
  git switch perf/image-scroll-prefetch && sed -n '1,220p'
  .agent/state/image-scroll-prefetch.md`
