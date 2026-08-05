// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

/**
 * Selects how a {@link Window} protects its client area from platform safe-area
 * insets. Safe-area values use logical TotalCross layout units.
 */
public enum SafeAreaMode {
  /** Protect a main window and the screen edges touched by other windows. */
  AUTO,

  /** Protect every edge selected by {@link Window#setSafeAreaEdges(int)}. */
  ENABLED,

  /** Do not remove safe-area insets from the window client rectangle. */
  DISABLED
}
