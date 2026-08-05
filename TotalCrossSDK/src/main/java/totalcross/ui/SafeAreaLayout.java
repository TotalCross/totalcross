// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

/**
 * Selects which client rectangle is used to position a control that is a direct
 * child of a {@link Window}. Safe-area values use logical TotalCross layout
 * units.
 */
public enum SafeAreaLayout {
  /** Follow the parent window's {@link SafeAreaMode}. This is the default. */
  INHERIT,

  /** Force placement inside the parent window's selected safe edges. */
  SAFE,

  /** Ignore safe exclusion while retaining the window's border and user insets. */
  FULL_BLEED
}
