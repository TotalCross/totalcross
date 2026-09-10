<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Image scroll prefetch execution state

- branch: `perf/image-scroll-prefetch`
- parent_head: `702139cedfad946cde133f8bfaeb4ec4ad239897`
- active_milestone: 1 — reusable asynchronous image preparation
- active_slice: reusable asynchronous image preparation implemented; focused SDK/native validation passed; ScrollContainer traversal pending
- last_commit: `09cdb5da9`
- active_paths: `.agent/state/image-scroll-prefetch.md`,
  `.agent/evidence/image-scroll-prefetch.md`,
  `scripts/run-image-scroll-real-workload-benchmark.py`,
  `TotalCrossSDK/src/main/java/totalcross/ui/image/`,
  `TotalCrossSDK/src/main/java/totalcross/ui/`,
  focused image/ScrollContainer smoke fixtures
- next_action: add the package-private display-preparation traversal and
  ScrollContainer PREFETCH_ALL batch completion
- focused_validation: parent local and remote heads matched; target branch was
  created from the parent and pushed; baseline runner passed 24 records across
  eight fresh profiles using exactly 663 JPEGs; focused SDK tests and the
  macOS tcvm build passed for asynchronous image preparation
- deferred_validation: the caller shell does not export `TC_IMAGE_CORPUS`; the
  baseline used the verified local corpus path explicitly; native smoke remains
  deferred until the related implementation milestone
- decisions_active: `PREFETCH_ALL`, explicit asynchronous API, one decode
  worker, detached worker candidates, UI-thread adoption, no eviction
- blockers: none; `TC_IMAGE_CORPUS` is available as
  `/Users/flsobral/Downloads/win32` but is not exported by default
- deliberate_out_of_scope: pre-existing untracked files and generated/local
  artifacts in the shared worktree are preserved and are not plan changes
- resume_command: `cd /Users/flsobral/repos/totalcross-image-scroll-raster-fast-path &&
  git switch perf/image-scroll-prefetch && sed -n '1,220p'
  .agent/state/image-scroll-prefetch.md`
