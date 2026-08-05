// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

/** Bit flags identifying safe-area edges. */
public final class SafeAreaEdges {
  /** No edge. */
  public static final int NONE = 0;
  /** Left edge. */
  public static final int LEFT = 1;
  /** Top edge. */
  public static final int TOP = 2;
  /** Right edge. */
  public static final int RIGHT = 4;
  /** Bottom edge. */
  public static final int BOTTOM = 8;
  /** Every edge. */
  public static final int ALL = LEFT | TOP | RIGHT | BOTTOM;

  private SafeAreaEdges() {
  }

  static int validate(int edges) {
    if ((edges & ~ALL) != 0) {
      throw new IllegalArgumentException("Unknown safe-area edge bits: " + (edges & ~ALL));
    }
    return edges;
  }
}
