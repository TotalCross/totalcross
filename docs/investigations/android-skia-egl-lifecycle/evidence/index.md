<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Evidence index

## Historical sources

- I03 report and historical source/artifact identity: links in FINDINGS.md.
- Customer source and automation: performance-lab PR 7.
- Historical native crash run: performance-lab run 43113450337.
- Historical tested TotalCross revision: 5464c21e3107e3f1ce9c287b724829e2bb870c1d.

## Current campaign

Baseline source revision: 52a947b5295321d7350350bd618c55fb26304c00.

M1 initial startup-blocked attempt: M1-api30-run.md,
M1-api30-log-excerpt.log, and M1-api30-startup-error.png.

M1 current-baseline API 30 reproduction: M1-api30-reproduction.md,
M1-api30-crash-excerpt.log, and M1-api30-reproduced-home.png. The exact
customer APK and marker-only variant both recorded the historical native
SIGSEGV after return from the grant-in-place permission screen. The trace
variant recorded successful directory create/delete before the surface
callback crash. The test-only TCUI asset overlay and artifact hashes are
documented in the reproduction report.

Generated APKs, native binaries, emulator images, full SDK archives, caches,
full logcats, and tombstones remain outside Git. The committed stack excerpt
is sanitized and compact.
