<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Evidence — full macOS image diagnostics benchmark

## 2026-09-18 — Plan 2 bootstrap

- Revision: `d0c8aabf97900db2dc75e1d3c3c9c58bfb78c018`.
- Branch and Plan 1 preconditions: passed.
- Corpus: `/Users/flsobral/Downloads/win32/win32`; six variants with 663 JPEG
  files each; source names `aggressive-480` and `aggressive-540` are staged by
  existing tooling as `aggresive-480` and `aggresive-540`.
- Scope: no build, smoke, other platform, or full benchmark run yet.
- Next: exact SDK/macOS ARM64 build, package, smoke gate, then full suite.
