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

class SlidingWindowPresentationTest {
  private Insets originalInsets;
  private int originalWidth;
  private int originalHeight;

  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
  }

  @BeforeEach
  void setGlobals() {
    Insets safe = Window.getSafeAreaInsets();
    originalInsets = new Insets(safe.top, safe.left, safe.bottom, safe.right);
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
    Window.topMost = null;
    Window._updateSafeAreaInsets(originalInsets.top, originalInsets.left,
        originalInsets.bottom, originalInsets.right);
    Settings.screenWidth = originalWidth;
    Settings.screenHeight = originalHeight;
  }

  @Test
  void allDirectionsUseSafeViewportAndRetainProviderAcrossRelayout() {
    Window owner = ownerWindow();
    int[] directions = {Control.LEFT, Control.RIGHT, Control.TOP, Control.BOTTOM};
    Rect[] finals = {
        new Rect(-7, 0, 260, 600), new Rect(7, 0, 260, 600),
        new Rect(0, -7, 260, 600), new Rect(0, 7, 260, 600)
    };
    Rect[] outside = {
        new Rect(-260, 0, 260, 600), new Rect(260, 0, 260, 600),
        new Rect(0, -600, 260, 600), new Rect(0, 600, 260, 600)
    };

    for (int i = 0; i < directions.length; i++) {
      CountingProvider provider = new CountingProvider();
      TrackingSlidingWindow sliding = new TrackingSlidingWindow(provider);
      sliding.animDir = directions[i];
      sliding.totalTime = 0;
      sliding.setSlackSpace(7);
      int stackSize = Window.zStack.size();

      sliding.popupNonBlocking();
      PresentationHandle handle = sliding.presentationHandle();
      assertEquals(finals[i], handle.frame.getRect());
      Rect start = new Rect();
      ((SlidePresentationTransition) handle.entry.transition)
          .getOutsideBounds(handle.viewportBounds(), handle.finalBounds(), start);
      assertEquals(outside[i], start);
      assertSame(owner, Window.getTopMost());
      assertEquals(stackSize, Window.zStack.size());
      assertEquals(1, provider.calls);

      Window._updateSafeAreaInsets(12, 24, 32, 44);
      assertEquals(1, provider.calls);
      assertSame(provider.view, sliding.getChildren()[0]);
      sliding.unpop();
      sliding.unpop();
      assertEquals(PresentationHandle.State.DISMISSED, handle.state());
      assertEquals("popup,postPopup,onUnpop,postUnpop", sliding.lifecycle.toString());
      Window._updateSafeAreaInsets(10, 20, 30, 40);
    }
  }

  private static Window ownerWindow() {
    Window owner = new Window();
    owner.setRect(0, 0, 320, 640);
    owner.setSafeAreaMode(SafeAreaMode.ENABLED);
    Window.zStack.push(owner);
    Window.topMost = owner;
    return owner;
  }

  private static final class CountingProvider implements Presenter<Container> {
    int calls;
    final Container view = new Container();

    @Override
    public Container getView() {
      calls++;
      return view;
    }
  }

  private static final class TrackingSlidingWindow extends SlidingWindow {
    final StringBuilder lifecycle = new StringBuilder();

    TrackingSlidingWindow(Presenter<Container> provider) {
      super(provider);
    }

    private void record(String event) {
      if (lifecycle.length() > 0) {
        lifecycle.append(',');
      }
      lifecycle.append(event);
    }

    @Override
    public void onPopup() {
      record("popup");
    }

    @Override
    protected void postPopup() {
      super.postPopup();
      record("postPopup");
    }

    @Override
    protected void onUnpop() {
      record("onUnpop");
    }

    @Override
    protected void postUnpop() {
      record("postUnpop");
    }
  }
}
