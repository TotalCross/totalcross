<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — image-scroll diagnostics macOS instrumentation

Evidence is append-only. Each record keeps the command/result summary and
defers verbose output to an external log when one exists.

## 2026-09-18 — bootstrap

- Revision before work: `5917a4aa3e20123a1ff02c1a5b0cedd9640c0c6b`.
- Branch check: passed for `perf/image-decode-distributed-benchmark`.
- Scoped pre-existing change: the untracked task plan only.
- Bootstrap commit: `73519be8b` (`docs(benchmark): plan macos image diagnostics`).
- Header, size, and staged whitespace checks: passed.
- Commit-message checker: reported one overlong line because the body used
  literal `\\n` escapes; no amend/rewrite was performed per plan.
- Build/smoke: intentionally not run at bootstrap.
