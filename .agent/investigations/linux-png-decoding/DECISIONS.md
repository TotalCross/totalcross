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
- Do not use startup workarounds, change production image code, modify
  ImageDrawingBridge ownership, or fix indexed inspection in this phase.
- Pause at every milestone and before any production scope expansion. Causal
  findings and a proposed correction must be reviewed before Milestone 4.

## Superseded

None.
