// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.ui.gfx.Rect;

public class LegacySafeAreaSmoke extends MainWindow {
  @Override
  public void initUI() {
    try {
      runAssertions();
      System.out.println("final=PASS");
      exit(0);
    } catch (Throwable failure) {
      System.out.println("failure=" + failure.getMessage());
      System.out.println("final=FAIL");
      failure.printStackTrace();
      exit(1);
    }
  }

  private void runAssertions() {
    Settings.screenWidth = 320;
    Settings.screenHeight = 480;
    Window._updateSafeAreaInsets(12, 20, 24, 36);

    SlidingWindow sliding = new SlidingWindow(new Presenter<Container>() {
      @Override
      public Container getView() {
        return new Container();
      }
    });
    sliding.animDir = BOTTOM;
    sliding.prepareForPopup();
    require(sliding instanceof Window, "SlidingWindow is not Window-based");
    require(sliding.getRect().equals(new Rect(0, 480, 320, 480)),
        "sliding animation origin mismatch");
    require(sliding.getChildren()[0].getRect().equals(new Rect(20, 12, 264, 444)),
        "sliding safe content mismatch");

    TopMenu menu = new TopMenu(new Control[0], LEFT, NO_BORDER);
    menu.widthInPixels = 200;
    menu.setRect(false);
    require(menu instanceof Window, "TopMenu is not Window-based");
    require(menu.getSafeAreaMode() == SafeAreaMode.DISABLED, "TopMenu automatic safe area enabled");
    require(menu.getAttachedSafeAreaEdges()
        == (SafeAreaEdges.TOP | SafeAreaEdges.LEFT | SafeAreaEdges.BOTTOM),
        "TopMenu attached edges mismatch");

    SideMenuContainer side = new SideMenuContainer("Menu", new Control[0]);
    require(side.topMenu.widthInPixels == 208, "safe drawer width mismatch");
    side.topMenu.widthInPixels = 180;
    require(side.topMenu.widthInPixels == 180, "explicit drawer width lost");

    System.out.println("slidingOrigin=" + rect(sliding.getRect()));
    System.out.println("slidingContent=" + rect(sliding.getChildren()[0].getRect()));
    System.out.println("topMenuEdges=" + menu.getAttachedSafeAreaEdges());
    System.out.println("drawerWidth=208 explicitWidth=180");
  }

  private static String rect(Rect rect) {
    return rect.x + "," + rect.y + "," + rect.width + "," + rect.height;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
