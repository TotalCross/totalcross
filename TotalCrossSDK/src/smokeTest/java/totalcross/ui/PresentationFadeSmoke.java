// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.ui.event.UpdateListener;

public class PresentationFadeSmoke extends MainWindow implements UpdateListener {
  private TopMenu compositeMenu;
  private SideMenuContainer sideMenu;
  private PresentationHandle handle;
  private int stage;
  private int elapsed;

  @Override
  public void initUI() {
    try {
      setSafeAreaMode(SafeAreaMode.ENABLED);
      Window._updateSafeAreaInsets(10, 20, 30, 40);
      repositionChildren();

      compositeMenu = new TopMenu(new Control[] {new Spacer(10, 50)}, Control.LEFT, Window.NO_BORDER);
      compositeMenu.widthInPixels = 200;
      compositeMenu.totalTime = 100;
      compositeMenu.popupNonBlocking();
      handle = compositeMenu.presentationHandle();
      require(handle.animation() != null, "composite fade did not start");
      require(handle.frame.offscreen != null, "composite screenshot missing");
      require(handle.frame.offscreen.alphaMask == 0, "composite initial alpha mismatch");
      require(Control.enableUpdateScreen, "screen updates disabled after fade startup");
      System.out.println("compositeFadeStarted=true");
      addUpdateListener(this);
    } catch (Throwable failure) {
      fail(failure);
    }
  }

  @Override
  public void updateListenerTriggered(int elapsedMilliseconds) {
    try {
      elapsed += elapsedMilliseconds;
      require(elapsed < 5000, "presentation fade smoke timed out at stage " + stage);
      switch (stage) {
      case 0:
        if (handle.state() == PresentationHandle.State.PRESENTED) {
          require(handle.frame.x == 0, "composite final position mismatch");
          require(handle.frame.offscreen == null, "composite screenshot leaked");
          System.out.println("compositeFadeCompleted=true");
          compositeMenu.unpop();
          stage = 1;
        }
        break;
      case 1:
        if (handle.state() == PresentationHandle.State.DISMISSED) {
          startSideMenu();
          stage = 2;
        }
        break;
      case 2:
        if (handle.state() == PresentationHandle.State.PRESENTED) {
          require(handle.frame.offscreen == null, "SideMenu faded its own frame");
          sideMenu.topMenu.unpop();
          stage = 3;
        }
        break;
      case 3:
        if (handle.state() == PresentationHandle.State.DISMISSED) {
          compositeMenu.popupNonBlocking();
          handle = compositeMenu.presentationHandle();
          require(handle.frame.offscreen != null, "second presentation screenshot missing");
          stage = 4;
        }
        break;
      case 4:
        if (handle.state() == PresentationHandle.State.PRESENTED) {
          require(handle.frame.offscreen == null, "second presentation screenshot leaked");
          require(Control.enableUpdateScreen, "screen updates disabled after completion");
          System.out.println("updateScreenEnabled=true");
          System.out.println("offscreenReleased=true");
          System.out.println("final=PASS");
          removeUpdateListener(this);
          exit(0);
        }
        break;
      default:
        throw new IllegalStateException("unexpected stage " + stage);
      }
    } catch (Throwable failure) {
      fail(failure);
    }
  }

  private void startSideMenu() {
    sideMenu = new SideMenuContainer("Menu", new Control[0]);
    add(sideMenu, LEFT, TOP, FILL, FILL);
    sideMenu.topMenu.totalTime = 100;
    sideMenu.topMenu.popupNonBlocking();
    handle = sideMenu.topMenu.presentationHandle();
    require(!sideMenu.topMenu.isFadeOnPopAndUnpop(), "SideMenu self-fade enabled");
    require(handle.entry.barrierAlpha == 127, "SideMenu barrier alpha mismatch");
    require(handle.frame.offscreen == null, "SideMenu created a fade screenshot");
    System.out.println("fadeOtherWindowsBarrier=true");
  }

  private void fail(Throwable failure) {
    System.out.println("failure=" + failure.getMessage());
    System.out.println("final=FAIL");
    failure.printStackTrace();
    removeUpdateListener(this);
    exit(1);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
