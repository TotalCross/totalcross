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

## Milestone 2

`ScrollContainer` now owns independent non-negative content insets. Leading
insets offset the scrolling bag, while the full leading/content/trailing extent
drives scrollbar ranges. Resize restores scrollbar values before positioning
the bag, and the setter distinguishes origin, middle, and trailing anchors.
Five focused tests passed; no build ran.

## Milestone 3

`TopMenu` retains one scrolling body and legacy `header`, then adds optional
fixed bars through full-bleed, safe-padded hosts. Reserve mode subtracts the full
safe-padded bar height from the viewport; overlay mode keeps the viewport and
adds that height to content insets. `ScrollUnderMode` exposes the Reddit,
ChatGPT, Gmail, and both-edge combinations, and `SideMenuContainer` forwards the
configuration. Nineteen focused tests and the one SDK distribution checkpoint
passed.

## Milestone 4

Android reuses the decor-view `WindowInsetsCompat` listener, excluding IME and
never applying view padding. Changed physical values are retained until the VM
is ready, sent through a generated JNI method on the TotalCross event thread,
and guarded once more across program startup. iOS emits a dedicated physical
safe-area event after valid layout and on subsequent changes. Both converge on
`windowUpdateSafeAreaInsetsPhysical`, which converts by content scale and invokes
the SDK transition. Fourteen focused tests and the sole Android build passed;
iOS received static reconciliation only, as required.

## Milestone 5

The final focused suite passed all 19 tests. Exact changed-file header and
whitespace validation passed, generated JNI evidence was confirmed, and new-file
size limits were satisfied. Smoke checks ran last: no Android device was attached
and no runnable iOS artifact or safe-area demo fixture existed, so no device was
mutated and no additional build was created. Final reports distinguish this
environment limitation from the completed implementation.
