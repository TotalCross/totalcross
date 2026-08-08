// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.gfx.Rect;

public class PresentationSafeAreaSmoke extends MainWindow {
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
    setSafeAreaMode(SafeAreaMode.ENABLED);
    Window._updateSafeAreaInsets(10, 20, 30, 40);
    repositionChildren();
    int stackSize = Window.zStack.size();
    Window owner = Window.getTopMost();

    SlidingWindow sliding = new SlidingWindow(new Presenter<Container>() {
      @Override
      public Container getView() {
        return new Container();
      }
    });
    sliding.animDir = BOTTOM;
    sliding.totalTime = 0;
    sliding.popupNonBlocking();
    PresentationHandle slidingHandle = sliding.presentationHandle();

    SideMenuContainer side = new SideMenuContainer("Menu", new Control[0]);
    side.topMenu.totalTime = 0;
    side.topMenu.popupNonBlocking();
    PresentationHandle menuHandle = side.topMenu.presentationHandle();

    require(Window.getTopMost() == owner, "owner window changed");
    require(Window.zStack.size() == stackSize, "z-stack changed");
    Rect safe = getClientRect();
    require(safe.equals(slidingHandle.viewport.getRect()), "sliding viewport mismatch");
    require(safe.equals(menuHandle.viewport.getRect()), "menu viewport mismatch");
    require(slidingHandle.viewport.clipsChildrenToBounds(), "viewport clipping disabled");
    require(slidingHandle.frame.getRect().equals(new Rect(0, 0, safe.width, safe.height)),
        "sliding final bounds mismatch");
    require(menuHandle.frame.width == Math.min(320, safe.width - 56), "drawer width mismatch");

    System.out.println("ownerWindowUnchanged=true");
    System.out.println("zStackDelta=" + (Window.zStack.size() - stackSize));
    System.out.println("safeViewport=" + rect(safe));
    System.out.println("slidingFinal=" + rect(slidingHandle.frame.getRect()));
    System.out.println("topMenuFinal=" + rect(menuHandle.frame.getRect()));
    System.out.println("clippingPass=true");

    side.topMenu.unpop();
    sliding.unpop();
    require(presentationHost().activeCount() == 0, "presentation entries leaked");
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
