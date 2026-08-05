<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Safe-area insets milestone history

## Milestone 0

The scoped baseline completed at `0ec107e0b9e3` on
`feat/logical-ui-scaling`. No implementation source changes existed. The
inventory confirmed logical content scaling, Android safe-inset caching, iOS
safe-area callbacks coupled to `screenChanged`, and the expected SDK layout
entry points. No build or test ran, as required by the plan.

## Milestone 1

The core SDK model adds three small public types and focused APIs on `Window`,
`Control`, and `Container`. Direct window children choose inherited safe, forced
safe, or full-bleed client bounds; consumed-edge masks prevent container padding
from duplicating exclusion. The single static update cache deduplicates values,
notifies each active window, repositions children, and schedules repaint without
entering screen-resize code. Seven focused tests passed; no build ran.
