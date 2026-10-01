// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.gfx.Rect;
import totalcross.ui.image.Image;

class ScrollContainerDisplayPreparationTest {
  @BeforeAll
  static void initializeUi() {
    new Launcher();
    Settings.fingerTouch = false;
    if (MainWindow.mainWindowInstance == null) {
      new MainWindow();
    }
  }

  @Test
  void discoveryVisitsVisibleNestedImagesInsideTheScrollClip() throws Exception {
    ScrollContainer scroll = new ScrollContainer(false, false);
    scroll.setRect(0, 0, 100, 100);

    Image visibleMarker = Image.createLogical(8, 8, 1);
    ImageControl visible = new ImageControl(visibleMarker);
    scroll.add(visible);
    visible.setRect(5, 5, 20, 20);

    Image outsideMarker = Image.createLogical(9, 9, 1);
    ImageControl outside = new ImageControl(outsideMarker);
    scroll.add(outside);
    outside.setRect(160, 160, 12, 12);

    Image hiddenMarker = Image.createLogical(10, 10, 1);
    ImageControl hidden = new ImageControl(hiddenMarker);
    scroll.add(hidden);
    hidden.setRect(25, 25, 12, 12);
    hidden.setVisible(false);

    Container nested = new Container();
    scroll.add(nested);
    nested.setRect(60, 60, 35, 35);
    Image nestedMarker = Image.createLogical(11, 11, 1);
    ImageControl nestedVisible = new ImageControl(nestedMarker);
    nested.add(nestedVisible);
    nestedVisible.setRect(5, 5, 15, 15);

    scroll.resize();
    Rect bounds = scroll.bag0.getAbsoluteRect();
    Rect client = scroll.bag0.getClientRect();
    Rect clip = new Rect(bounds.x + client.x, bounds.y + client.y, client.width, client.height);
    DisplayPreparationContext context = new DisplayPreparationContext();
    scroll.bag.collectDisplayPreparation(context, clip);
    Rect outsideBounds = outside.getAbsoluteRect();

    assertTrue(hasCandidate(context, visibleMarker), "visible image should be discovered");
    assertTrue(hasCandidate(context, nestedMarker), "nested visible image should be discovered");
    assertFalse(hasCandidate(context, outsideMarker), "out-of-clip image should be skipped; image="
        + outsideBounds.x + "," + outsideBounds.y + "," + outsideBounds.width + "," + outsideBounds.height
        + " clip=" + clip.x + "," + clip.y + "," + clip.width + "," + clip.height);
    assertFalse(hasCandidate(context, hiddenMarker), "hidden image should be skipped");
    assertEquals(2, context.candidateCountForTest());
  }

  @Test
  void emptyBatchCompletesAndSupersededBatchCallbackIsSuppressed() {
    ScrollContainer scroll = new ScrollContainer(false, false);
    final int[] callbackCount = {0};
    scroll.prepareForDisplay(new Runnable() {
      @Override
      public void run() {
        callbackCount[0]++;
      }
    });
    assertEquals(1, callbackCount[0]);

    final int[] oldCount = {0};
    final int[] currentCount = {0};
    DisplayPreparationBatch stale = new DisplayPreparationBatch(scroll, 1L, new Runnable() {
      @Override
      public void run() {
        oldCount[0]++;
      }
    });
    scroll.prepareForDisplay(null);
    stale.finishDiscovery();
    DisplayPreparationBatch current = new DisplayPreparationBatch(scroll, 2L, new Runnable() {
      @Override
      public void run() {
        currentCount[0]++;
      }
    });
    current.finishDiscovery();

    assertEquals(0, oldCount[0]);
    assertEquals(1, currentCount[0]);
  }

  private static boolean hasCandidate(DisplayPreparationContext context, Image image) {
    for (int i = 0; i < context.candidateCountForTest(); i++) {
      if (context.candidateForTest(i) == image) {
        return true;
      }
    }
    return false;
  }
}
