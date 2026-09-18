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
- Build/smoke: intentionally not run at bootstrap.
