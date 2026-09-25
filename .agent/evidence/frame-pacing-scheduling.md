<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Frame pacing and scheduling evidence index

Append compact records here. Canonical stage rows belong in the stage CSV/JSON
files; raw process output and temporary run directories stay outside Git.

## Records

- 2026-09-25: Activation started on
  `feat/frame-pacing-scheduling-diagnostics`; both `HEAD` and
  `feat/png-prefetch` resolved to `d7a9ec93faf9f2da0724611de115649176d0b033`.
  Existing local paths are inventoried in
  `.agent/state/frame-pacing-scheduling.md` and excluded from staging.
- 2026-09-25: Signed activation commit `de7ad8077` created and signature
  verified. Header validation and staged diff checks passed. The prescribed
  commit-message check found a body line longer than 80 characters; history was
  not rewritten per plan policy. The corpus root was resolved from the existing
  evidence record `.agent/evidence/image-scroll-diagnostics-macos-02-benchmark.md`
  and validated at 663 files (660 JPEG, 3 PNG).
- 2026-09-25: Stage 1 correctness preflight attempt exited with signal `-5`
  before emitting app output. The retained log was empty at
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-1-l46l2s78/logs/00-synthetic-current-16ms.log`;
  the complete execution directory remains at
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/frame-pacing-stage-1-l46l2s78`.
  No canonical evidence files were written. Diagnosis found the runner passed an
  absolute output path although the established macOS runner uses a path
  relative to the bundle working directory. The runner now uses a unique,
  bundle-relative output directory; rerun pending.
- 2026-09-25: Signed correction commit `992ec1ccc` adds the relative output
  argument and contract test; all six frame-pacing contract tests and focused
  header/diff checks pass. The signature is valid. The post-commit message
  validator found one body line over 80 characters; history was not rewritten.
- 2026-09-25: The bundle-relative retry also exited `-5` because its output
  directory began with a dot. A direct invocation with `run=0` and a normal
  bundle-relative output directory completed successfully and produced one
  summary. The runner now uses a visible, invocation-unique directory; matrix
  retry pending. The failed execution log and run directories remain under
  `/var/folders/k8/02b7wfkd7fn32vtm3t5mwxwr0000gn/T/` and the `/tmp` bundle.
