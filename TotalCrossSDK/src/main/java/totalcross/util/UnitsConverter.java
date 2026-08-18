// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

public abstract class UnitsConverter {

  /**
   * @deprecated Layout and drawing values are logical. The destination graphics
   *             surface performs physical-pixel conversion.
   */
  @Deprecated
  public static int toPixels(int value) {
    return value;
  }
}
