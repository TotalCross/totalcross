// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;

class ClippedContainerTest {
  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
  }

  @Test
  void visibilitySearchHonorsStartAndUsesNegativeNotFoundSentinel() {
    ClippedContainer clipped = new ClippedContainer();
    clipped.add(itemAt(0));
    clipped.add(itemAt(20));
    clipped.add(itemAt(40));

    assertEquals(1, clipped.findOneVisible(15, 25, 1, 3));
    assertEquals(-1, clipped.findOneVisible(0, 10, 1, 3));
    assertEquals(-1, clipped.findOneVisible(80, 90, 0, 3));
  }

  @Test
  void emptyAndOffscreenChildrenResetCachedMidpoint() {
    Container parent = new Container();
    parent.setRect(0, 0, 100, 20);
    TestClippedContainer clipped = new TestClippedContainer();
    clipped.verticalOnly = true;
    clipped.setRect(0, 0, 100, 100);
    parent.add(clipped);

    clipped.cachedMidpoint(0);
    clipped.paintChildren();
    assertEquals(-1, clipped.cachedMidpoint());

    clipped.add(itemAt(50));
    clipped.cachedMidpoint(0);
    clipped.paintChildren();
    assertEquals(-1, clipped.cachedMidpoint());
  }

  private static Control itemAt(int y) {
    Control item = new Control();
    item.setRect(0, y, 100, 10);
    return item;
  }

  private static final class TestClippedContainer extends ClippedContainer {
    int cachedMidpoint() {
      return lastMid;
    }

    void cachedMidpoint(int value) {
      lastMid = value;
    }
  }
}
