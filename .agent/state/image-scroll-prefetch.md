<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image scroll prefetch execution state

- branch: `perf/image-scroll-prefetch`
- parent_head: `702139cedfad946cde133f8bfaeb4ec4ad239897`
- active_milestone: complete — TCVM-compatible asynchronous prefetch closeout
- active_slice: Deferred already-decoded adoption, focused SDK/macOS validation,
  corrected plan, and authoritative final evidence complete
- last_commit: `9237982ce`
- active_paths: `.agent/state/image-scroll-prefetch.md`,
  `.agent/evidence/image-scroll-prefetch.md`,
  `scripts/run-image-scroll-real-workload-benchmark.py`,
  `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/`,
  focused image/ScrollContainer smoke fixtures
- next_action: none; local and `origin/perf/image-scroll-prefetch` heads are
  synchronized after final validation and documentation closeout
- focused_validation: focused Java regressions including deferred
  alreadyDecoded adoption, queued continuation iteration, shared DRAW/COPY
  source lifecycles, ScrollContainer, and traversal passed; Release SDK dist
  passed; Release macOS tcvm build was reused; ImagePreparation macOS smoke
  passed with adoption failure/retry, copyRect no-extra-decode, and captured
  detached optimization mask
- deferred_validation: the 663-JPEG matrix was not rerun because this fix only
  changes alreadyDecoded scheduling; Android, Linux, Windows, and iOS builds
  were not run because the ExecPlan restricts local validation to SDK and macOS
- decisions_active: `PREFETCH_ALL`, explicit asynchronous continuation API,
  always-queued alreadyDecoded adoption when MainWindow exists, one
  lock-protected active slot, short-lived decode threads, DRAW_READY/COPY_READY
  requirements, detached candidates, UI-thread adoption, per-request
  optimization mask, immutable source identity for final variants, no general
  eviction, and no explicit release of source backings still held by sibling
  plans or variants
- blockers: none; `TC_IMAGE_CORPUS` is available as
  `/Users/flsobral/Downloads/win32` but is not exported by default
- deliberate_out_of_scope: pre-existing untracked files and generated/local
  artifacts in the shared worktree are preserved and are not plan changes
- resume_command: `cd /Users/flsobral/repos/totalcross-image-scroll-raster-fast-path &&
  git switch perf/image-scroll-prefetch && sed -n '1,220p'
  .agent/state/image-scroll-prefetch.md`
