// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.ScrollRasterReuse.Plan;
import totalcross.ui.ScrollRasterReuse.Rect;
import totalcross.ui.ScrollRasterReuse.Surface;
import totalcross.ui.gfx.Graphics;

class ScrollRasterReuseMoveTest {
  private static final int WIDTH = 20;
  private static final int HEIGHT = 20;
  private int oldWidth;
  private int oldHeight;
  private boolean oldOnJavaSe;
  private int[] oldPixels;

  @BeforeEach
  void setUp() {
    new Launcher();
    oldWidth = Settings.screenWidth;
    oldHeight = Settings.screenHeight;
    oldOnJavaSe = Settings.onJavaSE;
    oldPixels = Graphics.mainWindowPixels;
    Settings.onJavaSE = true;
    Settings.screenWidth = WIDTH;
    Settings.screenHeight = HEIGHT;
    Graphics.configureMainWindowSurface(WIDTH, HEIGHT, 1);
  }

  @AfterEach
  void tearDown() {
    ScrollRasterReuse.resetTestHooks();
    Settings.screenWidth = oldWidth;
    Settings.screenHeight = oldHeight;
    Settings.onJavaSE = oldOnJavaSe;
    Graphics.configureMainWindowSurface(Math.max(0, oldWidth), Math.max(0, oldHeight), 1);
    Graphics.mainWindowPixels = oldPixels;
  }

  @Test
  void positiveScrollMoveAndExposedPaintMatchFullRepaint() {
    assertMatchesFullRepaint(2);
  }

  @Test
  void negativeScrollMoveAndExposedPaintMatchFullRepaint() {
    assertMatchesFullRepaint(-2);
  }

  @Test
  void deltaNearViewportHeightKeepsTheSinglePreservedRow() {
    assertMatchesFullRepaint(6);
  }

  @Test
  void forcedMoveFailureDoesNotModifyAnyFramebufferPixel() {
    fillPattern(Graphics.mainWindowPixels);
    int[] before = Graphics.mainWindowPixels.clone();
    Surface surface = surface();
    Plan plan = plan(2, surface);
    ScrollRasterReuse.failNextMoveForTest();

    assertEquals(ScrollRasterReuse.MOVE_FAILED, ScrollRasterReuse.move(plan, surface));
    assertArrayEquals(before, Graphics.mainWindowPixels);
  }

  @Test
  void failureHookDoesNotChangeEligibilityAndOnlyFailsAnEligibleMove() {
    Settings.onJavaSE = false;
    Surface surface = surface();
    Rect viewport = new Rect(2, 3, 5, 7);
    boolean unsupportedScale = ScrollRasterReuse.isUnsupportedNativeScale(2.0);
    Plan beforeHook = ScrollRasterReuse.plan(true, true, 0, 2, viewport, 2.0,
        new Surface(40, 40, 40, Integer.BYTES, true, true, true), unsupportedScale, false, false);
    assertFalse(beforeHook.eligible());

    ScrollRasterReuse.failNextMoveForTest();
    boolean unsupportedScaleWithHook = ScrollRasterReuse.isUnsupportedNativeScale(2.0);
    Plan withHook = ScrollRasterReuse.plan(true, true, 0, 2, viewport, 2.0,
        new Surface(40, 40, 40, Integer.BYTES, true, true, true), unsupportedScaleWithHook, false, false);
    assertEquals(beforeHook.eligible(), withHook.eligible());
    assertEquals(ScrollRasterReuse.MOVE_INVALID_FRAMEBUFFER, ScrollRasterReuse.move(withHook,
        new Surface(40, 40, 40, Integer.BYTES, true, true, true)));

    Plan eligible = plan(2, surface);
    assertTrue(eligible.eligible());
    assertEquals(ScrollRasterReuse.MOVE_FAILED, ScrollRasterReuse.move(eligible, surface));
  }

  private void assertMatchesFullRepaint(int delta) {
    fillPattern(Graphics.mainWindowPixels);
    int[] before = Graphics.mainWindowPixels.clone();
    Surface surface = surface();
    Plan plan = plan(delta, surface);
    assertTrue(plan.eligible());

    assertEquals(ScrollRasterReuse.MOVE_SUCCEEDED, ScrollRasterReuse.move(plan, surface));
    int[] expected = before.clone();
    int[] pixels = Graphics.mainWindowPixels;
    for (int y = plan.viewport.y; y < plan.viewport.y + plan.viewport.height; y++) {
      for (int x = plan.viewport.x; x < plan.viewport.x + plan.viewport.width; x++) {
        expected[y * surface.stridePixels + x] = colorAt(x, y + delta);
      }
    }
    for (int y = plan.exposed.y; y < plan.exposed.y + plan.exposed.height; y++) {
      for (int x = plan.exposed.x; x < plan.exposed.x + plan.exposed.width; x++) {
        pixels[y * surface.stridePixels + x] = colorAt(x, y + delta);
      }
    }
    assertArrayEquals(expected, pixels);
    for (int y = plan.destination.y; y < plan.destination.y + plan.destination.height; y++) {
      for (int x = plan.destination.x; x < plan.destination.x + plan.destination.width; x++) {
        assertEquals(colorAt(x, y + delta), pixels[y * surface.stridePixels + x]);
      }
    }
  }

  private static Plan plan(int delta, Surface surface) {
    return ScrollRasterReuse.plan(true, true, 0, delta, new Rect(2, 3, 5, 7), 1,
        surface, false, false, false);
  }

  private static Surface surface() {
    return new Surface(WIDTH, HEIGHT, WIDTH, Integer.BYTES, true, true, true);
  }

  private static void fillPattern(int[] pixels) {
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        pixels[y * WIDTH + x] = colorAt(x, y);
      }
    }
  }

  private static int colorAt(int x, int y) {
    return 0xFF000000 | ((y + 64) << 8) | x;
  }
}
