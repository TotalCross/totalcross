<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Test Matrix

## Milestone 1 — Current-Master Linux Baseline

| Scenario | Probe / control | Startup workaround | Status |
| --- | --- | --- | --- |
| Native Linux startup and path controls | PR #568 startup matrix | None | PASS: 5/5 exit 0; startup and JPEG markers present |
| 36×36 RGBA via `Image(File)` | I02 | None | PASS: decoded 1,296 pixels; no exception |
| 36×36 RGBA via `Image(byte[])` | I02 | None | PASS: decoded 1,296 pixels; no exception |
| Existing filesystem image through `Image(String)` | Linux startup probe JPEG control | None | PASS |
| Packaged TCZ PNG through `Image(String)` | I02 | None | PASS: 4 pixels materialized |
| Pixel materialization and alpha control | I02 `getPixels()` | None | PASS: expected sample values recorded in run output |
| Indexed palette-transparency PNG constructor | I02 | None | REJECTED: unsupported encoded image exception |
| Original 2×2 PNG image drawing and application surface sequence | I06 | None | PASS: stages 1–7 and exact red draw pixel |
| JPEG positive control | I02 and startup probe | None | PASS |
| Invalid encoded-image controls | I02 | None | PASS: both invalid inputs rejected |

PNG metadata and reference-decoder samples are retained in
`fixtures/manifest.json`. Runtime statuses, exact case lines, and artifact
provenance are recorded in `evidence/index.md`.

## Later Milestones

Windows Win32/I386, macOS ARM64, Android ARM64 emulator, sanitizer builds,
corrected Linux regressions, and full ownership assertions are deferred to the
authorized milestone that requires them.
