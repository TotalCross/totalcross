<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Decisions

## Active

- Start from exact `origin/master` revision
  `6beef491f28c63aa364d3661dd5c37ccff01fb0d`; PR #568 is present.
- Use the existing PR #568 hosted startup workflow, canonical reusable build,
  I02 probe, fixtures, and original I06 application. Add only the narrow
  workflow support needed to run this baseline on the isolated branch.
- Keep I02 RGBA decode, I06 native drawing, and indexed inspection classified
  independently.
- Classify the ADLER32 decode and I06 drawing failures as `NOT_REPRODUCED` with
  root cause undetermined; record I06 ownership validation as `PASS`.
- Keep the original RGBA and I06 PNG bytes with a repository-owned focused
  probe in the existing hosted Linux workflow.
- Do not use startup workarounds, change production image code, modify image
  dependencies, isolate symbols, alter allocation, or bisect old revisions.
- Prepare issue proposals only. Do not publish or edit GitHub issues.
- Do not investigate or correct indexed-PNG rejection in this branch.
- Do not run non-Linux builds unless a shared regression-test change requires
  them. Pause after the closure commit and remote verification.

## Superseded

- The original Milestones 2–6 failure-localization, causal-experiment,
  production-correction, cross-platform, and full-regression plan is superseded
  by the current user-authorized closure. Its history remains in Git.
