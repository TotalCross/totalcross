// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

/**
 * Represents a horizontal span on a scanline.
 */
public final class Span {
  /** Inclusive start coordinate of the span. */
  public final int start;
  /** Inclusive end coordinate of the span. */
  public final int end;

  /**
   * Creates a span.
   *
   * @param start inclusive start coordinate
   * @param end inclusive end coordinate
   */
  public Span(int start, int end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Returns whether this span contains at least one valid pixel.
   *
   * @return {@code true} when {@code end >= start}
   */
  public boolean isValid() {
    return end >= start;
  }
}
