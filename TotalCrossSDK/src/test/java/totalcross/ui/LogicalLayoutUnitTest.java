// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.util.UnitsConverter;

class LogicalLayoutUnitTest {
  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
  }

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

  @Test
  void pixelParentConvertsChildEdgesToLogicalGeometry() {
    Container parent = sizedContainer(200, 100, LayoutUnit.PIXEL, 2);
    Container child = new Container();

    parent.add(child, 20, 10, 100, 40);

    assertBounds(child, 10, 5, 50, 20);
  }

  @Test
  void childUnitDoesNotChangeItsPlacementButControlsDescendants() {
    Container root = sizedContainer(200, 100, LayoutUnit.PIXEL, 2);
    Container child = new Container();
    child.setLayoutUnit(LayoutUnit.DP);
    root.add(child, 20, 10, 100, 40);

    Container grandchild = new Container();
    child.add(grandchild, 10, 5, 20, 10);

    assertBounds(child, 10, 5, 50, 20);
    assertBounds(grandchild, 10, 5, 20, 10);
  }

  @Test
  void pixelSemanticOffsetUsesPhysicalParentCoordinates() {
    Container parent = sizedContainer(200, 100, LayoutUnit.PIXEL, 2);
    Container first = new Container();
    Container second = new Container();
    parent.add(first, 20, 0, 20, 20);
    parent.add(second, Control.AFTER + 10, 0, 20, 20);

    assertBounds(first, 10, 0, 10, 10);
    assertBounds(second, 25, 0, 10, 10);
  }

  @Test
  void pixelSharedEdgesStayAdjacentAtFractionalAndIntegerScales() {
    for (double scale : new double[] { 1.5, 2, 3 }) {
      Container parent = sizedContainer(200, 100, LayoutUnit.PIXEL, scale);
      Container left = new Container();
      Container right = new Container();
      parent.add(left, 1, 0, 10, 10);
      parent.add(right, 11, 0, 10, 10);

      assertEquals(right.x, left.x + left.width, "shared edge at scale " + scale);
      assertEquals(parent.toLayoutPixels(left.x + left.width), parent.toLayoutPixels(right.x));
    }
  }

  @Test
  void pixelClientEdgesPreserveNonzeroInsetsAtAllScales() {
    for (double scale : new double[] { 1.5, 2, 3 }) {
      Container parent = sizedContainer(240, 120, LayoutUnit.PIXEL, scale);
      parent.setInsets(30, 30, 15, 15);
      Container child = new Container();

      parent.add(child, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);

      int physicalLeft = parent.toLayoutPixels(30);
      int physicalTop = parent.toLayoutPixels(15);
      int physicalRight = parent.toLayoutPixels(210);
      int physicalBottom = parent.toLayoutPixels(105);
      assertBounds(child, parent.toLogicalLayoutEdge(physicalLeft), parent.toLogicalLayoutEdge(physicalTop),
          parent.toLogicalLayoutEdge(physicalRight) - parent.toLogicalLayoutEdge(physicalLeft),
          parent.toLogicalLayoutEdge(physicalBottom) - parent.toLogicalLayoutEdge(physicalTop));
    }
  }

  private static Container sizedContainer(int width, int height, LayoutUnit unit, double scale) {
    Container container = new Container();
    container.setRect(0, 0, width, height);
    container.setLayoutUnit(unit);
    container.gfx.setScales(scale, 1);
    return container;
  }

  private static void assertBounds(Control control, int x, int y, int width, int height) {
    assertEquals(x, control.x);
    assertEquals(y, control.y);
    assertEquals(width, control.width);
    assertEquals(height, control.height);
  }
}
