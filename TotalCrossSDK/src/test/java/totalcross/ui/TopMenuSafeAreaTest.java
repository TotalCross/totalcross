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
  private Window owner;

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
    Window.zStack.removeAllElements();
    Window._updateSafeAreaInsets(10, 20, 30, 40);
    owner = new Window();
    owner.setRect(0, 0, 320, 640);
    owner.setSafeAreaMode(SafeAreaMode.ENABLED);
    Window.zStack.push(owner);
    Window.topMost = owner;
  }

  @AfterEach
  void restoreSafeArea() {
    Window.zStack.removeAllElements();
    Window.topMost = null;
    Window._updateSafeAreaInsets(originalInsets.top, originalInsets.left,
        originalInsets.bottom, originalInsets.right);
    Settings.screenWidth = originalWidth;
    Settings.screenHeight = originalHeight;
  }

  @Test
  void fixedBarsConsumeSafeAreaOnlyInThePresentationViewport() {
    assertLayout(TopMenu.ScrollUnderMode.NONE, 30, 530, 0, 0);
    assertLayout(TopMenu.ScrollUnderMode.TOP, 0, 560, 30, 0);
    assertLayout(TopMenu.ScrollUnderMode.BOTTOM, 30, 570, 0, 40);
    assertLayout(TopMenu.ScrollUnderMode.BOTH, 0, 600, 30, 40);
  }

  @Test
  void dynamicInsetsRetainBodyAndOutsideBarrierDismisses() {
    TopMenu menu = menu(TopMenu.ScrollUnderMode.NONE);
    ScrollContainer body = menu.bodyScroller;
    PresentationHandle handle = menu.presentationHandle();

    Window._updateSafeAreaInsets(12, 24, 32, 44);

    assertSame(body, menu.bodyScroller);
    assertEquals(new Rect(24, 12, 252, 596), handle.viewport.getRect());
    assertEquals(526, body.height);
    assertEquals(0, menu.getTopBar().x);
    assertEquals(0, menu.getTopBar().y);

    handle.barrier.penUp(null);
    assertEquals(PresentationHandle.State.DISMISSED, handle.state());
    assertSame(owner, Window.getTopMost());
    assertEquals(1, Window.zStack.size());
  }

  private void assertLayout(TopMenu.ScrollUnderMode mode, int bodyY, int bodyHeight,
      int topInset, int bottomInset) {
    TopMenu menu = menu(mode);
    assertEquals(new Rect(20, 10, 260, 600), menu.presentationHandle().viewport.getRect());
    assertEquals(bodyY, menu.bodyScroller.y);
    assertEquals(bodyHeight, menu.bodyScroller.height);
    assertEquals(30, menu.topBarHost.height);
    assertEquals(40, menu.bottomBarHost.height);
    assertEquals(0, menu.getTopBar().x);
    assertEquals(0, menu.getTopBar().y);
    assertEquals(0, menu.getBottomBar().x);
    assertEquals(0, menu.getBottomBar().y);
    Insets content = new Insets();
    menu.bodyScroller.getContentInsets(content);
    assertEquals(topInset, content.top);
    assertEquals(bottomInset, content.bottom);
    menu.unpop();
  }

  private static TopMenu menu(TopMenu.ScrollUnderMode mode) {
    TopMenu menu = new TopMenu(new Control[] {new Spacer(20, 500)}, Control.LEFT, Window.NO_BORDER);
    menu.widthInPixels = 200;
    menu.totalTime = 0;
    menu.setTopBar(new Spacer(10, 30));
    menu.setBottomBar(new Spacer(10, 40));
    menu.setScrollUnderMode(mode);
    menu.popupNonBlocking();
    return menu;
  }

  private static final class TestMainWindow extends MainWindow {
    @Override
    void setTimerInterval(int interval) {
    }
  }
}
