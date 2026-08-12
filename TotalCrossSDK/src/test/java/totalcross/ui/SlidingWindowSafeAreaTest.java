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
import totalcross.ui.anim.FadeAnimation;
import totalcross.ui.gfx.Rect;

class SlidingWindowSafeAreaTest {
  private Insets originalInsets;
  private int originalWidth;
  private int originalHeight;

  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
    if (MainWindow.getMainWindow() == null) {
      new MainWindow();
    }
  }

  @BeforeEach
  void setGlobals() {
    Insets safe = Window.getSafeAreaInsets();
    originalInsets = new Insets(safe.top, safe.left, safe.bottom, safe.right);
    originalWidth = Settings.screenWidth;
    originalHeight = Settings.screenHeight;
    Settings.screenWidth = 320;
    Settings.screenHeight = 640;
    Window._updateSafeAreaInsets(10, 20, 30, 40);
  }

  @AfterEach
  void restoreGlobals() {
    Window._updateSafeAreaInsets(originalInsets.top, originalInsets.left,
        originalInsets.bottom, originalInsets.right);
    Settings.screenWidth = originalWidth;
    Settings.screenHeight = originalHeight;
  }

  @Test
  void directionalPreparationKeepsFinalSafeLayoutAtTemporaryOrigin() {
    int[] directions = {Control.LEFT, Control.RIGHT, Control.TOP, Control.BOTTOM};
    Rect[] starts = {
        new Rect(-320, 0, 320, 640), new Rect(320, 0, 320, 640),
        new Rect(0, -640, 320, 640), new Rect(0, 640, 320, 640)
    };
    Rect[] content = {
        new Rect(20, 10, 300, 600), new Rect(0, 10, 280, 600),
        new Rect(20, 10, 260, 630), new Rect(20, 0, 260, 610)
    };

    for (int i = 0; i < directions.length; i++) {
      CountingProvider provider = new CountingProvider();
      SlidingWindow sliding = new SlidingWindow(provider);
      sliding.animDir = directions[i];
      sliding.setSlackSpace(7);

      sliding.prepareForPopup();

      assertEquals(starts[i], sliding.getRect());
      assertEquals(content[i], provider.view.getRect());
      assertEquals(1, provider.calls);
    }
  }

  @Test
  void resizeAndReopenRebuildFinalBoundsBeforeChildLayout() {
    CountingProvider provider = new CountingProvider();
    SlidingWindow sliding = new SlidingWindow(provider);
    sliding.animDir = Control.RIGHT;
    sliding.setSlackSpace(7);
    sliding.prepareForPopup();

    Settings.screenWidth = 640;
    Settings.screenHeight = 320;
    Window._updateSafeAreaInsets(8, 50, 12, 30);
    sliding.screenResized();

    assertEquals(new Rect(7, 0, 640, 320), sliding.getRect());
    assertEquals(new Rect(0, 8, 610, 300), provider.view.getRect());
    assertSame(provider.view, sliding.getChildren()[0]);
    assertEquals(1, provider.calls);

    sliding.x = -123;
    sliding.y = -456;
    sliding.prepareForPopup();
    assertEquals(new Rect(640, 0, 640, 320), sliding.getRect());
    assertEquals(new Rect(0, 8, 610, 300), provider.view.getRect());
    assertEquals(1, provider.calls);
  }

  @Test
  void centerUsesLegacyFadeAnimation() {
    SlidingWindow sliding = new SlidingWindow(new CountingProvider());
    sliding.animDir = Control.CENTER;
    sliding.totalTime = 100;
    sliding.prepareForPopup();

    sliding.onPopup();

    assertTrue(sliding.currentAnimation instanceof FadeAnimation);
    sliding.currentAnimation.stop(true);
    sliding.currentAnimation = null;
  }

  private static final class CountingProvider implements Presenter<Container> {
    int calls;
    final Container view = new Container();

    @Override
    public Container getView() {
      calls++;
      return view;
    }
  }
}
