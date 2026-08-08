// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2020-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

import totalcross.sys.Settings;
import totalcross.ui.Control;

/**
 * Utility methods for converting unit-encoded values into screen pixels.
 */
public abstract class UnitsConverter {

  /**
   * Converts an integer unit-encoded value to pixels.
   *
   * @param value the raw value, optionally encoded with a unit marker such as {@code Control.DP}
   * @return the converted pixel value, or the original value when no conversion is needed
   */
  public static int toPixels(int value) {
    if ((Control.DP - Control.RANGE) <= value && value <= (Control.DP + Control.RANGE)) {
      return (int) (Settings.screenDensity * (value - Control.DP));
    }
    return value;
  }

  /**
   * Converts a floating-point unit-encoded value to pixels.
   *
   * @param value the raw value, optionally encoded with a unit marker such as {@code Control.DP}
   * @return the converted pixel value, or the original value when no conversion is needed
   */
  public static double toPixels(double value) {
    if ((Control.DP - Control.RANGE) <= value && value <= (Control.DP + Control.RANGE)) {
      return Settings.screenDensity * (value - Control.DP);
    }
    return value;
  }
}
