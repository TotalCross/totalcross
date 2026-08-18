// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.gfx.Rect;

class SafeAreaLayoutTest {
  private Insets original;
  private int originalWidth;
  private int originalHeight;

  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
  }

  @BeforeEach
  void saveGlobals() {
    Insets safe = Window.getSafeAreaInsets();
    original = new Insets(safe.top, safe.left, safe.bottom, safe.right);
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
    Window._updateSafeAreaInsets(original.top, original.left, original.bottom, original.right);
    Settings.screenWidth = originalWidth;
    Settings.screenHeight = originalHeight;
  }

  @Test
  void modesAndSelectedEdgesChooseSafeClientRectangle() {
    Window window = sizedWindow();
    window.setSafeAreaMode(SafeAreaMode.ENABLED);
    window.setSafeAreaEdges(SafeAreaEdges.LEFT | SafeAreaEdges.BOTTOM);

    assertRect(window.getClientRect(), 20, 0, 300, 610);
    window.setSafeAreaMode(SafeAreaMode.DISABLED);
    assertRect(window.getClientRect(), 0, 0, 320, 640);
  }

  @Test
  void autoProtectsOnlyTouchedEdgesForOrdinaryWindows() {
    Window window = new Window();
    window.setRect(20, 10, 260, 600);

    assertRect(window.getClientRect(), 0, 0, 260, 600);
    window.setRect(0, 0, 320, 640);
    assertRect(window.getClientRect(), 20, 10, 260, 600);
  }

  @Test
  void directChildrenCanForceSafeOrFullBleedPlacement() {
    Window window = sizedWindow();
    window.setSafeAreaMode(SafeAreaMode.DISABLED);
    Container safe = new Container();
    safe.setSafeAreaLayout(SafeAreaLayout.SAFE);
    window.add(safe, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
    assertBounds(safe, 20, 10, 260, 600);

    window.setSafeAreaMode(SafeAreaMode.ENABLED);
    Container fullBleed = new Container();
    fullBleed.setSafeAreaLayout(SafeAreaLayout.FULL_BLEED);
    window.add(fullBleed, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
    assertBounds(fullBleed, 0, 0, 320, 640);
  }

  @Test
  void declaredInsetsArePreservedAndMayCancelSafeArea() {
    Window window = sizedWindow();
    window.setSafeAreaMode(SafeAreaMode.ENABLED);
    window.setInsets(-20, 5, -10, 7);

    Insets declared = new Insets();
    window.getInsets(declared);
    assertRect(window.getClientRect(), 0, 0, 275, 603);
    assertEquals(-20, declared.left);
    assertEquals(5, declared.right);
    assertEquals(-10, declared.top);
    assertEquals(7, declared.bottom);
  }

  @Test
  void safePaddingAddsOnlyUnconsumedEdges() {
    Window window = sizedWindow();
    window.setSafeAreaMode(SafeAreaMode.ENABLED);
    Container safe = new Container();
    safe.setSafeAreaLayout(SafeAreaLayout.SAFE);
    safe.setSafeAreaPaddingEdges(SafeAreaEdges.ALL);
    window.add(safe, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
    assertRect(safe.getClientRect(), 0, 0, 260, 600);

    Container fullBleed = new Container();
    fullBleed.setSafeAreaLayout(SafeAreaLayout.FULL_BLEED);
    fullBleed.setSafeAreaPaddingEdges(SafeAreaEdges.ALL);
    window.add(fullBleed, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
    assertRect(fullBleed.getClientRect(), 20, 10, 260, 600);
  }

  @Test
  void updatesDeduplicateAndNotifyEachActiveWindowOnce() {
    TrackingWindow first = new TrackingWindow();
    TrackingWindow second = new TrackingWindow();
    Window.zStack.push(first);
    Window.zStack.push(second);

    assertFalse(Window._updateSafeAreaInsets(10, 20, 30, 40));
    assertEquals(0, first.changeCount);
    assertTrue(Window._updateSafeAreaInsets(11, 21, 31, 41));
    assertEquals(1, first.changeCount);
    assertEquals(1, second.changeCount);
    assertEquals(10, first.previous.top);
    assertEquals(11, first.current.top);
  }

  @Test
  void invalidPoliciesAreRejected() {
    Window window = sizedWindow();
    Container container = new Container();
    assertThrows(NullPointerException.class, () -> window.setSafeAreaMode(null));
    assertThrows(NullPointerException.class, () -> container.setSafeAreaLayout(null));
    assertThrows(IllegalArgumentException.class, () -> window.setSafeAreaEdges(16));
    assertThrows(IllegalArgumentException.class, () -> container.setSafeAreaPaddingEdges(16));
  }

  private static Window sizedWindow() {
    Window window = new Window();
    window.setRect(0, 0, 320, 640);
    return window;
  }

  private static void assertRect(Rect actual, int x, int y, int width, int height) {
    assertEquals(new Rect(x, y, width, height), actual);
  }

  private static void assertBounds(Control actual, int x, int y, int width, int height) {
    assertEquals(x, actual.x);
    assertEquals(y, actual.y);
    assertEquals(width, actual.width);
    assertEquals(height, actual.height);
  }

  private static final class TrackingWindow extends Window {
    int changeCount;
    Insets previous;
    Insets current;

    @Override
    protected void safeAreaInsetsChanged(Insets previous, Insets current) {
      changeCount++;
      this.previous = previous;
      this.current = current;
    }
  }
}
