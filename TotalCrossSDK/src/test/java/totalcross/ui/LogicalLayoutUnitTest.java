// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import totalcross.util.UnitsConverter;

class LogicalLayoutUnitTest {
  @Test
  void inheritResolvesToLogicalDpWhenDetached() {
    assertEquals(LayoutUnit.DP, Container.resolveLayoutUnit(LayoutUnit.INHERIT, LayoutUnit.DP));
  }

  @Test
  void nestedContainersInheritUntilTheyOverride() {
    assertEquals(LayoutUnit.PIXEL, Container.resolveLayoutUnit(LayoutUnit.INHERIT, LayoutUnit.PIXEL));
    assertEquals(LayoutUnit.DP, Container.resolveLayoutUnit(LayoutUnit.DP, LayoutUnit.PIXEL));
  }

  @Test
  void deprecatedDpMarkerAndConverterAreLogicalIdentity() {
    assertEquals(0, Control.DP);
    assertEquals(16, Control.DP + 16);
    assertEquals(16, UnitsConverter.toPixels(16));
  }
}
