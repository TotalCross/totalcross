// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;

class SideMenuPresentationTest {
  private Insets originalInsets;
  private int originalWidth;
  private int originalHeight;
  private Window owner;

  @BeforeAll
  static void initializeUi() {
    new Launcher();
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
  void drawerWidthIsResolvedFromSafeViewportAndGesturesStayLocal() {
    SideMenuContainer side = new SideMenuContainer("Menu", new Control[0]);
    owner.add(side, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
    assertTrue(side.callListenersOnAllTargets);
    assertFalse(owner.callListenersOnAllTargets);

    side.topMenu.totalTime = 0;
    side.topMenu.popupNonBlocking();
    assertEquals(204, side.topMenu.presentationHandle().frame.width);
    side.topMenu.unpop();

    side.topMenu.widthInPixels = 180;
    side.topMenu.popupNonBlocking();
    assertEquals(180, side.topMenu.presentationHandle().frame.width);
    side.topMenu.unpop();
  }

  @Test
  void fixedBarsAndScrollModeRemainForwarded() {
    SideMenuContainer side = new SideMenuContainer(null, new Control[0]);
    Spacer top = new Spacer(10, 30);
    Spacer bottom = new Spacer(10, 40);
    side.setTopBar(top);
    side.setBottomBar(bottom);
    side.setScrollUnderMode(TopMenu.ScrollUnderMode.BOTTOM);
    assertSame(top, side.getTopBar());
    assertSame(bottom, side.getBottomBar());
  }

  private static final class TestMainWindow extends MainWindow {
    @Override
    void setTimerInterval(int interval) {
    }
  }
}
