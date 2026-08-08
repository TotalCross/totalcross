// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.gfx.Rect;

class PresentationHostTest {
  private Insets originalInsets;
  private int originalWidth;
  private int originalHeight;

  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
  }

  @BeforeEach
  void setGlobals() {
    Insets safe = Window.getSafeAreaInsets();
    originalInsets = new Insets(safe.top, safe.left, safe.bottom, safe.right);
    originalWidth = Settings.screenWidth;
    originalHeight = Settings.screenHeight;
    Settings.screenWidth = 320;
    Settings.screenHeight = 640;
    Window.zStack.removeAllElements();
    Window._updateSafeAreaInsets(10, 20, 30, 40);
  }

  @AfterEach
  void restoreGlobals() {
    Window.zStack.removeAllElements();
    Window.topMost = null;
    Window._updateSafeAreaInsets(originalInsets.top, originalInsets.left,
        originalInsets.bottom, originalInsets.right);
    Settings.screenWidth = originalWidth;
    Settings.screenHeight = originalHeight;
  }

  @Test
  void safeViewportClipsFrameWithoutChangingWindowStack() {
    Window owner = new Window();
    owner.setRect(0, 0, 320, 640);
    owner.setSafeAreaMode(SafeAreaMode.ENABLED);
    Window.zStack.push(owner);
    Window.topMost = owner;
    int stackSize = Window.zStack.size();

    Container content = new Container();
    SlidePresentationTransition transition = new SlidePresentationTransition(Control.BOTTOM);
    PresentationEntry entry = new PresentationEntry(content, PresentationEntry.Layer.ROUTE,
        PresentationEntry.fillViewport(), transition, true, false, true, 0, 0, 0);
    PresentationHandle handle = owner.presentationHost().present(entry);

    assertEquals(stackSize, Window.zStack.size());
    assertSame(owner, Window.getTopMost());
    assertEquals(new Rect(20, 10, 260, 600), handle.viewport.getRect());
    assertEquals(new Rect(0, 0, 260, 600), handle.frame.getRect());
    assertTrue(handle.viewport.clipsChildrenToBounds());
    Rect outside = new Rect();
    transition.getOutsideBounds(handle.viewportBounds(), handle.finalBounds(), outside);
    assertEquals(new Rect(0, 600, 260, 600), outside);

    Window._updateSafeAreaInsets(12, 24, 32, 44);
    assertEquals(new Rect(24, 12, 252, 596), handle.viewport.getRect());
    assertSame(content, handle.frame.getChildren()[0]);

    handle.dismiss();
    handle.dismiss();
    assertEquals(PresentationHandle.State.DISMISSED, handle.state());
    assertEquals(0, owner.presentationHost().activeCount());
    assertEquals(stackSize, Window.zStack.size());
  }
}
