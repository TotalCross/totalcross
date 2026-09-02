<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda
SPDX-License-Identifier: LGPL-2.1-only
-->

# Native image materialization plan handoff

## Delivered

- Generated deployed Skia Images now use opaque mutable native backing.
- PNG and JPEG decode paths avoid persistent Java area-sized rasters.
- Native Graphics targets, row readback, PNG/JPEG encoding, snapshots, and
  deferred scale materialization route through explicit backing state.
- Retryable native decode allocation failure and targeted JPEG dimensions are
  covered by the focused deployed smoke.
- The repeated `ImageControl` 500x500 to 89x89 path remains native-backed and
  draws successfully.

## Validation

Focused SDK tests, SDK distribution, smoke compilation, copyright validation,
macOS arm64 native `tcvm`/`Launcher` builds, issue-417 generated-image smoke,
and the plan-2 native materialization smoke passed. The local commit-message
checker found one overlong body line in commit `4f5c58da8`; history was not
rewritten.

## Handoff to plan 3

Plan 2 is complete at commit `4f5c58da8`. Exact geometry behavior for the
temporary native scale bridge, plus broader native transform migration, remains
for plan 3. No plan-3 code or validation was started in this turn.
