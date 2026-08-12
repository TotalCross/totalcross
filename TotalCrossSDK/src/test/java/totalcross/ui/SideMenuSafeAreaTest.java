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

class SideMenuSafeAreaTest {
  private Insets originalInsets;
  private int originalWidth;
  private int originalHeight;

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
  void horizontalAutomaticWidthUsesUsableSafeWidth() {
    SideMenuContainer left = new SideMenuContainer(Control.LEFT, "Menu", new Control[0]);
    SideMenuContainer right = new SideMenuContainer(Control.RIGHT, "Menu", new Control[0]);

    assertEquals(204, left.topMenu.widthInPixels);
    assertEquals(204, right.topMenu.widthInPixels);
  }

  @Test
  void automaticWidthCapsAndClampsWhileExplicitWidthWins() {
    Settings.screenWidth = 640;
    Window._updateSafeAreaInsets(8, 80, 12, 60);
    SideMenuContainer wide = new SideMenuContainer("Menu", new Control[0]);
    assertEquals(320, wide.topMenu.widthInPixels);

    wide.topMenu.widthInPixels = 180;
    Window._updateSafeAreaInsets(8, 100, 12, 90);
    assertEquals(180, wide.topMenu.widthInPixels);

    Settings.screenWidth = 80;
    Window._updateSafeAreaInsets(8, 15, 12, 15);
    SideMenuContainer narrow = new SideMenuContainer("Menu", new Control[0]);
    assertEquals(1, narrow.topMenu.widthInPixels);
  }

  @Test
  void verticalMenusKeepLegacyPercentageSizing() {
    SideMenuContainer top = new SideMenuContainer(Control.TOP, "Menu", new Control[0]);
    SideMenuContainer bottom = new SideMenuContainer(Control.BOTTOM, "Menu", new Control[0]);

    assertEquals(100, top.topMenu.percWidth);
    assertEquals(0, top.topMenu.widthInPixels);
    assertEquals(100, bottom.topMenu.percWidth);
    assertEquals(0, bottom.topMenu.widthInPixels);
  }

  @Test
  void fixedBarsAndWindowInputRemainOnLegacyPath() {
    Window owner = new Window();
    owner.setRect(0, 0, 320, 640);
    SideMenuContainer side = new SideMenuContainer(null, new Control[0]);
    owner.add(side, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
    Spacer top = new Spacer(10, 30);
    Spacer bottom = new Spacer(10, 40);

    side.setTopBar(top);
    side.setBottomBar(bottom);
    side.setScrollUnderMode(TopMenu.ScrollUnderMode.BOTTOM);

    assertSame(top, side.getTopBar());
    assertSame(bottom, side.getBottomBar());
    assertTrue(owner.callListenersOnAllTargets);
  }

  private static final class TestMainWindow extends MainWindow {
    @Override
    void setTimerInterval(int interval) {
    }
  }
}
