<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Test Matrix

## Milestone 1 — Current-Master Linux Baseline

| Scenario | Probe / control | Startup workaround | Status |
| --- | --- | --- | --- |
| Native Linux startup and path controls | PR #568 startup matrix | None | PENDING |
| 36×36 RGBA via `Image(File)` | I02 | None | PENDING |
| 36×36 RGBA via `Image(byte[])` | I02 | None | PENDING |
| Existing filesystem image through `Image(String)` | Linux startup probe JPEG control | None | PENDING |
| Packaged TCZ PNG through `Image(String)` | I02 | None | PENDING |
| Pixel materialization and alpha control | I02 `getPixels()` | None | PENDING |
| Indexed palette-transparency PNG constructor | I02 | None | PENDING |
| Original 2×2 PNG image drawing and application surface sequence | I06 | None | PENDING |
| JPEG positive control | I02 and startup probe | None | PENDING |
| Invalid encoded-image controls | I02 | None | PENDING |

PNG metadata and reference-decoder samples are retained in
`fixtures/manifest.json`. Runtime statuses and per-case outputs will be added to
`CHECKPOINT.md` and `evidence/index.md` after the hosted run.

## Later Milestones

Windows Win32/I386, macOS ARM64, Android ARM64 emulator, sanitizer builds,
corrected Linux regressions, and full ownership assertions are deferred to the
authorized milestone that requires them.
