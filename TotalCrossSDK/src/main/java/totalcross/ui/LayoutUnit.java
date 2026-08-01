// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

/**
 * Selects the unit used by a container to place its children. DP is a logical,
 * device-independent unit; PIXEL is for applications that explicitly retain a
 * legacy pixel layout; INHERIT uses the nearest ancestor's explicit unit.
 */
public enum LayoutUnit {
  INHERIT,
  DP,
  PIXEL
}
