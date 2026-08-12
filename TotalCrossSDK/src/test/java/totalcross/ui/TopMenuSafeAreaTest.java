// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.gfx.Rect;

class TopMenuSafeAreaTest {
  private Insets originalInsets;
  private int originalWidth;
  private int originalHeight;

  @BeforeAll
  static void initializeUi() {
    new Launcher();
    Settings.fingerTouch = false;
    MainWindow.mainWindowInstance = new TestMainWindow();
  }

  @BeforeEach
  void setSafeArea() {
    Insets safe = Window.getSafeAreaInsets();
    originalInsets = new Insets(safe.top, safe.left, safe.bottom, safe.right);
    originalWidth = Settings.screenWidth;
    originalHeight = Settings.screenHeight;
    Settings.screenWidth = 320;
    Settings.screenHeight = 640;
    Window._updateSafeAreaInsets(10, 20, 30, 40);
  }

  @AfterEach
  void restoreSafeArea() {
    Window._updateSafeAreaInsets(originalInsets.top, originalInsets.left,
        originalInsets.bottom, originalInsets.right);
    Settings.screenWidth = originalWidth;
    Settings.screenHeight = originalHeight;
  }

  @Test
  void directionsMapToOnlyTheirAttachedSafeEdges() {
    assertEquals(SafeAreaEdges.TOP | SafeAreaEdges.LEFT | SafeAreaEdges.BOTTOM,
        bareMenu(Control.LEFT).getAttachedSafeAreaEdges());
    assertEquals(SafeAreaEdges.TOP | SafeAreaEdges.RIGHT | SafeAreaEdges.BOTTOM,
        bareMenu(Control.RIGHT).getAttachedSafeAreaEdges());
    assertEquals(SafeAreaEdges.TOP | SafeAreaEdges.LEFT | SafeAreaEdges.RIGHT,
        bareMenu(Control.TOP).getAttachedSafeAreaEdges());
    assertEquals(SafeAreaEdges.BOTTOM | SafeAreaEdges.LEFT | SafeAreaEdges.RIGHT,
        bareMenu(Control.BOTTOM).getAttachedSafeAreaEdges());
    assertEquals(SafeAreaEdges.ALL,
        bareMenu(Control.CENTER).getAttachedSafeAreaEdges());
  }

  @Test
  void fixedBarsConsumeInsetsOnlyOnAttachedEdges() {
    TopMenu left = menu(Control.LEFT, TopMenu.ScrollUnderMode.NONE);
    assertEquals(new Rect(0, 0, 200, 40), left.topBarHost.getRect());
    assertEquals(new Rect(20, 10, 180, 30), left.getTopBar().getRect());
    assertEquals(new Rect(0, 530, 200, 70), left.bottomBarHost.getRect());
    assertEquals(new Rect(20, 0, 180, 40), left.getBottomBar().getRect());

    TopMenu right = menu(Control.RIGHT, TopMenu.ScrollUnderMode.NONE);
    assertEquals(new Rect(0, 0, 200, 40), right.topBarHost.getRect());
    assertEquals(new Rect(0, 10, 160, 30), right.getTopBar().getRect());
    assertEquals(new Rect(0, 530, 200, 70), right.bottomBarHost.getRect());
    assertEquals(new Rect(0, 0, 160, 40), right.getBottomBar().getRect());
  }

  @Test
  void fixedBarModesKeepLegacyBodyGeometrySafeAware() {
    assertLayout(TopMenu.ScrollUnderMode.NONE, 40, 490, 0, 0);
    assertLayout(TopMenu.ScrollUnderMode.TOP, 0, 530, 40, 0);
    assertLayout(TopMenu.ScrollUnderMode.BOTTOM, 40, 560, 0, 70);
    assertLayout(TopMenu.ScrollUnderMode.BOTH, 0, 600, 40, 70);
  }

  @Test
  void resizeReusesBodyAndRebuildsSafeBarPadding() {
    TopMenu menu = menu(Control.LEFT, TopMenu.ScrollUnderMode.NONE);
    ScrollContainer body = menu.bodyScroller;

    Window._updateSafeAreaInsets(12, 24, 32, 44);
    menu.screenResized();

    assertSame(body, menu.bodyScroller);
    assertEquals(new Rect(0, 42, 200, 482), body.getRect());
    assertEquals(new Rect(24, 12, 176, 30), menu.getTopBar().getRect());
    assertEquals(new Rect(24, 0, 176, 40), menu.getBottomBar().getRect());
  }

  private void assertLayout(TopMenu.ScrollUnderMode mode, int bodyY, int bodyHeight,
      int topInset, int bottomInset) {
    TopMenu menu = menu(Control.LEFT, mode);
    assertEquals(new Rect(0, bodyY, 200, bodyHeight), menu.bodyScroller.getRect());
    Insets content = new Insets();
    menu.bodyScroller.getContentInsets(content);
    assertEquals(topInset, content.top);
    assertEquals(bottomInset, content.bottom);
  }

  private static TopMenu menu(int direction, TopMenu.ScrollUnderMode mode) {
    TopMenu menu = new TopMenu(new Control[] {new Spacer(20, 500)}, direction, Window.NO_BORDER);
    menu.widthInPixels = 200;
    menu.setTopBar(new Spacer(10, 30));
    menu.setBottomBar(new Spacer(10, 40));
    menu.setScrollUnderMode(mode);
    menu.setRect(false);
    menu.repositionChildren();
    return menu;
  }

  private static TopMenu bareMenu(int direction) {
    return new TopMenu(new Control[0], direction, Window.NO_BORDER);
  }

  private static final class TestMainWindow extends MainWindow {
    @Override
    void setTimerInterval(int interval) {
    }
  }
}
