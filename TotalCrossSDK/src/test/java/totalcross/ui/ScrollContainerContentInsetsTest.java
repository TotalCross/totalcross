// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.gfx.Rect;

class ScrollContainerContentInsetsTest {
  @BeforeAll
  static void initializeUi() {
    new Launcher();
    Settings.fingerTouch = false;
    if (MainWindow.mainWindowInstance == null) {
      new MainWindow();
    }
  }

  @Test
  void contentInsetsKeepViewportAndExtendBothAxesOnce() {
    Fixture fixture = new Fixture();
    Rect viewport = fixture.sc.bag0.getRect();
    int oldHorizontalMaximum = fixture.sc.sbH.getMaximum();
    int oldVerticalMaximum = fixture.sc.sbV.getMaximum();

    fixture.sc.setContentInsets(10, 15, 20, 25);

    assertEquals(viewport, fixture.sc.bag0.getRect());
    assertEquals(oldHorizontalMaximum + 25, fixture.sc.sbH.getMaximum());
    assertEquals(oldVerticalMaximum + 45, fixture.sc.sbV.getMaximum());
    Insets actual = new Insets();
    fixture.sc.getContentInsets(actual);
    assertEquals(new Insets(20, 10, 25, 15), actual);
  }

  @Test
  void firstAndLastContentRemainReachable() {
    Fixture fixture = new Fixture();
    fixture.sc.setContentInsets(10, 15, 20, 25);

    assertEquals(10, fixture.sc.bag.x + fixture.child.x);
    assertEquals(20, fixture.sc.bag.y + fixture.child.y);

    fixture.sc.scrollContent(10000, 10000, true);
    assertEquals(fixture.sc.bag0.width - 15,
        fixture.sc.bag.x + fixture.child.x + fixture.child.width);
    assertEquals(fixture.sc.bag0.height - 25,
        fixture.sc.bag.y + fixture.child.y + fixture.child.height);
  }

  @Test
  void changingLeadingInsetsPreservesAMiddleVisibleAnchor() {
    Fixture fixture = new Fixture();
    fixture.sc.setContentInsets(10, 15, 20, 25);
    fixture.sc.scrollContent(50, 80, true);
    int oldChildX = fixture.sc.bag.x + fixture.child.x;
    int oldChildY = fixture.sc.bag.y + fixture.child.y;

    fixture.sc.setContentInsets(25, 15, 35, 25);

    assertEquals(oldChildX, fixture.sc.bag.x + fixture.child.x);
    assertEquals(oldChildY, fixture.sc.bag.y + fixture.child.y);
  }

  @Test
  void trailingAnchorsRemainAtTrailingEdges() {
    Fixture fixture = new Fixture();
    fixture.sc.setContentInsets(10, 15, 20, 25);
    fixture.sc.scrollContent(10000, 10000, true);

    fixture.sc.setContentInsets(10, 35, 20, 55);

    assertEquals(fixture.sc.sbH.getMaximum() - fixture.sc.sbH.getVisibleItems(), fixture.sc.sbH.getValue());
    assertEquals(fixture.sc.sbV.getMaximum() - fixture.sc.sbV.getVisibleItems(), fixture.sc.sbV.getValue());
    assertEquals(fixture.sc.bag0.width - 35,
        fixture.sc.bag.x + fixture.child.x + fixture.child.width);
    assertEquals(fixture.sc.bag0.height - 55,
        fixture.sc.bag.y + fixture.child.y + fixture.child.height);
  }

  @Test
  void identicalValuesAreIdempotentAndNegativeValuesAreRejected() {
    Fixture fixture = new Fixture();
    fixture.sc.setContentInsets(10, 15, 20, 25);
    int x = fixture.sc.bag.x;
    int y = fixture.sc.bag.y;
    int horizontalMaximum = fixture.sc.sbH.getMaximum();
    int verticalMaximum = fixture.sc.sbV.getMaximum();

    fixture.sc.setContentInsets(10, 15, 20, 25);

    assertEquals(x, fixture.sc.bag.x);
    assertEquals(y, fixture.sc.bag.y);
    assertEquals(horizontalMaximum, fixture.sc.sbH.getMaximum());
    assertEquals(verticalMaximum, fixture.sc.sbV.getMaximum());
    assertThrows(IllegalArgumentException.class, () -> fixture.sc.setContentInsets(-1, 0, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> fixture.sc.setContentInsets(0, -1, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> fixture.sc.setContentInsets(0, 0, -1, 0));
    assertThrows(IllegalArgumentException.class, () -> fixture.sc.setContentInsets(0, 0, 0, -1));
  }

  private static final class Fixture {
    final ScrollContainer sc = new ScrollContainer(true, true);
    final Container child = new Container();

    Fixture() {
      sc.setRect(0, 0, 120, 160);
      sc.add(child, 0, 0, 240, 320);
      sc.resize();
    }
  }
}
