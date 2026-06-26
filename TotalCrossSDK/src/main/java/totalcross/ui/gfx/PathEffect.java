// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

import java.util.Arrays;

/**
 * Describes geometric effects applied to stroked paths, in a role similar to Skia's path effects.
 * <p>
 * This class is immutable. Use one of the factory methods to create an instance.
 */
public final class PathEffect {
  /**
   * Supported path effect types.
   */
  public static final class Type {
    /** Dash path effect. */
    public static final int DASH = 0;

    private Type() {
    }
  }

  public final int type;
  public final double[] intervals;
  public final double phase;

  private PathEffect(int type, double[] intervals, double phase) {
    this.type = type;
    this.intervals = intervals;
    this.phase = phase;
  }

  /**
   * Creates a dash path effect.
   *
   * @param intervals alternating on/off lengths; each value must be positive and the array length must be even
   * @param phase the dash phase
   * @return a new dash path effect
   * @throws NullPointerException if {@code intervals} is {@code null}
   * @throws IllegalArgumentException if the intervals array is empty, has odd length, or contains non-positive values
   */
  public static PathEffect dash(double[] intervals, double phase) {
    if (intervals == null) {
      throw new NullPointerException("intervals");
    }
    if (intervals.length == 0 || (intervals.length & 1) != 0) {
      throw new IllegalArgumentException("Dash intervals must have a positive even length");
    }
    for (int i = 0; i < intervals.length; i++) {
      if (!(intervals[i] > 0)) {
        throw new IllegalArgumentException("Dash intervals must be > 0: intervals[" + i + "]=" + intervals[i]);
      }
    }
    return new PathEffect(Type.DASH, Arrays.copyOf(intervals, intervals.length), phase);
  }

  /**
   * Returns whether the given value is a valid path effect type.
   */
  public static boolean isValidType(int type) {
    return type == Type.DASH;
  }
}
