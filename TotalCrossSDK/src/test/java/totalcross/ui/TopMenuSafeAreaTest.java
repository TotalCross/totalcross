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

class TopMenuSafeAreaTest {
  private Insets original;

  @BeforeAll
  static void initializeUi() {
    new Launcher();
    Settings.fingerTouch = false;
    MainWindow.mainWindowInstance = new TestMainWindow();
  }

  @BeforeEach
  void setSafeArea() {
    Insets safe = Window.getSafeAreaInsets();
    original = new Insets(safe.top, safe.left, safe.bottom, safe.right);
    Window.zStack.removeAllElements();
    Window._updateSafeAreaInsets(10, 20, 30, 40);
  }

  @AfterEach
  void restoreSafeArea() {
    Window.zStack.removeAllElements();
    Window._updateSafeAreaInsets(original.top, original.left, original.bottom, original.right);
  }

  @Test
  void attachedEdgesFollowAnimationDirection() {
    assertEquals(SafeAreaEdges.TOP | SafeAreaEdges.LEFT | SafeAreaEdges.BOTTOM,
        new TopMenu(new Control[0], Control.LEFT).getAttachedSafeAreaEdges());
    assertEquals(SafeAreaEdges.TOP | SafeAreaEdges.RIGHT | SafeAreaEdges.BOTTOM,
        new TopMenu(new Control[0], Control.RIGHT).getAttachedSafeAreaEdges());
    assertEquals(SafeAreaEdges.TOP | SafeAreaEdges.LEFT | SafeAreaEdges.RIGHT,
        new TopMenu(new Control[0], Control.TOP).getAttachedSafeAreaEdges());
    assertEquals(SafeAreaEdges.BOTTOM | SafeAreaEdges.LEFT | SafeAreaEdges.RIGHT,
        new TopMenu(new Control[0], Control.BOTTOM).getAttachedSafeAreaEdges());
  }

  @Test
  void redditReservesBothBars() {
    TopMenu menu = menu(TopMenu.ScrollUnderMode.NONE);
    assertLayout(menu, 40, 290, 0, 0);
  }

  @Test
  void chatGptScrollsUnderTopAndReservesBottom() {
    TopMenu menu = menu(TopMenu.ScrollUnderMode.TOP);
    assertLayout(menu, 0, 330, 40, 0);
  }

  @Test
  void gmailReservesTopAndScrollsUnderBottom() {
    TopMenu menu = menu(TopMenu.ScrollUnderMode.BOTTOM);
    assertLayout(menu, 40, 360, 0, 70);
  }

  @Test
  void bothKeepsFullViewportAndInsetsBothContentEdges() {
    TopMenu menu = menu(TopMenu.ScrollUnderMode.BOTH);
    assertLayout(menu, 0, 400, 40, 70);
  }

  @Test
  void dynamicSafeAreaUpdatesBarsAndViewportWithoutRecreatingBody() {
    TopMenu menu = menu(TopMenu.ScrollUnderMode.NONE);
    ScrollContainer body = menu.bodyScroller;
    Window.zStack.push(menu);

    Window._updateSafeAreaInsets(15, 25, 35, 45);

    assertSame(body, menu.bodyScroller);
    assertLayout(menu, 45, 280, 0, 0);
    assertEquals(25, menu.getTopBar().getX());
    assertEquals(15, menu.getTopBar().getY());
    assertEquals(25, menu.getBottomBar().getX());
  }

  @Test
  void sideMenuForwardsFixedBarsAndLayoutPreset() {
    SideMenuContainer side = new SideMenuContainer(null, new Control[0]);
    Spacer top = new Spacer(10, 30);
    Spacer bottom = new Spacer(10, 40);

    side.setTopBar(top);
    side.setBottomBar(bottom);
    side.setScrollUnderMode(TopMenu.ScrollUnderMode.BOTTOM);

    assertSame(top, side.getTopBar());
    assertSame(bottom, side.getBottomBar());
  }

  private static TopMenu menu(TopMenu.ScrollUnderMode mode) {
    TopMenu menu = new TopMenu(new Control[] { new Spacer(20, 500) }, Control.LEFT, Window.NO_BORDER);
    menu.setTopBar(new Spacer(10, 30));
    menu.setBottomBar(new Spacer(10, 40));
    menu.setScrollUnderMode(mode);
    menu.setRect(0, 0, 200, 400);
    menu.initUI();
    return menu;
  }

  private static void assertLayout(TopMenu menu, int bodyY, int bodyHeight, int topInset, int bottomInset) {
    assertEquals(bodyY, menu.bodyScroller.y);
    assertEquals(bodyHeight, menu.bodyScroller.height);
    Insets safe = Window.getSafeAreaInsets();
    assertEquals(30 + safe.top, menu.topBarHost.height);
    assertEquals(40 + safe.bottom, menu.bottomBarHost.height);
    assertEquals(safe.left, menu.getTopBar().getX());
    assertEquals(safe.top, menu.getTopBar().getY());
    assertEquals(safe.left, menu.getBottomBar().getX());
    assertEquals(0, menu.getBottomBar().getY());
    Insets content = new Insets();
    menu.bodyScroller.getContentInsets(content);
    assertEquals(topInset, content.top);
    assertEquals(bottomInset, content.bottom);
  }

  private static final class TestMainWindow extends MainWindow {
    @Override
    void setTimerInterval(int interval) {
    }
  }
}
