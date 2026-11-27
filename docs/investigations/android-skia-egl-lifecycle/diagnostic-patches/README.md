<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# Diagnostic patches

Temporary lifecycle instrumentation and causal-order experiments belong here.
Keep them separate from production changes, name each patch by milestone, and
record the source revision and test result in FINDINGS.md and evidence/index.md.

`M2-android-egl-lifecycle-diagnostics.patch.gz` contains the default-off
Java/native logging patch for Android surface callbacks and EGL/Skia identities.
It was built and exercised on the API 30 ARM64 emulator; see
`../evidence/M2-api30-lifecycle-diagnostics.md`. Keep this instrumentation
separate from the future production correction and remove it during final
consolidation unless maintainers explicitly request otherwise.

`M3-pre-egl-skia-cleanup-candidate.patch.gz` contains the isolated causal
candidate. It released screen-bound Skia resources on the existing graphics
thread while the old EGL context was current, before existing EGL teardown.
The candidate passed the focused API 30 scenarios; see
`../evidence/M3-api30-causal-candidate.md`. Candidate-only source changes were
removed from the worktree after testing. This patch is diagnostic evidence,
not production code.
