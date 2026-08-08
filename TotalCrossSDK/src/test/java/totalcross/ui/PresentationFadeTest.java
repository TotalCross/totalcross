// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.sys.Settings;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.FadeAnimation;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

class PresentationFadeTest {
  private Insets originalInsets;
  private int originalWidth;
  private int originalHeight;
  private int originalFadeValue;
  private int originalMaxFade;
  private boolean originalEnableUpdateScreen;
  private Window originalTopMost;
  private final List<Window> originalStack = new ArrayList<>();
  private Window owner;

  @BeforeAll
  static void initializeUi() {
    new Launcher();
    MainWindow.mainWindowInstance = new TestMainWindow();
  }

  @BeforeEach
  void setGlobals() {
    Insets safe = Window.getSafeAreaInsets();
    originalInsets = new Insets(safe.top, safe.left, safe.bottom, safe.right);
    originalWidth = Settings.screenWidth;
    originalHeight = Settings.screenHeight;
    originalFadeValue = Window.fadeValue;
    originalMaxFade = FadeAnimation.maxFade;
    originalEnableUpdateScreen = Control.enableUpdateScreen;
    originalTopMost = Window.topMost;
    for (int i = 0; i < Window.zStack.size(); i++) {
      originalStack.add((Window) Window.zStack.elementAt(i));
    }

    Settings.screenWidth = 320;
    Settings.screenHeight = 640;
    Window.fadeValue = 128;
    FadeAnimation.maxFade = FadeAnimation.DEFAULT_MAX_FADE;
    Control.enableUpdateScreen = true;
    Window.zStack.removeAllElements();
    Window._updateSafeAreaInsets(10, 20, 30, 40);
    owner = new Window();
    owner.setRect(0, 0, 320, 640);
    owner.setSafeAreaMode(SafeAreaMode.ENABLED);
    Window.zStack.push(owner);
    Window.topMost = owner;
  }

  @AfterEach
  void restoreGlobals() {
    Window.zStack.removeAllElements();
    for (Window window : originalStack) {
      Window.zStack.push(window);
    }
    Window.topMost = originalTopMost;
    Window._updateSafeAreaInsets(originalInsets.top, originalInsets.left,
        originalInsets.bottom, originalInsets.right);
    Settings.screenWidth = originalWidth;
    Settings.screenHeight = originalHeight;
    Window.fadeValue = originalFadeValue;
    FadeAnimation.maxFade = originalMaxFade;
    Control.enableUpdateScreen = originalEnableUpdateScreen;
  }

  @Test
  void directionalTopMenuSlidesAndFadesThroughAnIntermediateFrame() {
    TopMenu menu = menu(Control.LEFT);
    menu.popupNonBlocking();
    PresentationHandle handle = menu.presentationHandle();

    assertTrue(handle.frame.transparentBackground);
    assertEquals(PresentationHandle.State.PRESENTING, handle.state());
    assertNotNull(handle.animation());
    assertNotNull(handle.frame.offscreen);
    assertEquals(0, handle.frame.offscreen.alphaMask);
    assertTrue(Control.enableUpdateScreen);

    handle.dismiss();
    assertEquals(PresentationHandle.State.PRESENTING, handle.state());
    tick(handle, 10);

    assertTrue(handle.frame.x > -handle.frame.width && handle.frame.x < 0);
    assertIntermediateAlpha(handle);

    tick(handle, 90);
    assertEquals(PresentationHandle.State.PRESENTED, handle.state());
    assertEquals(0, handle.frame.x);
    assertNull(handle.frame.offscreen);
    assertTrue(Control.enableUpdateScreen);
    dismiss(menu);
  }

  @Test
  void centeredPresentationUsesDirectNonzeroFade() {
    TopMenu menu = menu(Control.CENTER);
    menu.popupNonBlocking();
    PresentationHandle handle = menu.presentationHandle();
    int finalX = handle.frame.x;
    int finalY = handle.frame.y;

    tick(handle, 10);
    assertEquals(finalX, handle.frame.x);
    assertEquals(finalY, handle.frame.y);
    assertIntermediateAlpha(handle);

    tick(handle, 90);
    assertEquals(PresentationHandle.State.PRESENTED, handle.state());
    assertNull(handle.frame.offscreen);
    dismiss(menu);
  }

  @Test
  void sideMenuSlidesOpaqueWhileBarrierDimsTheBackground() {
    SideMenuContainer side = new SideMenuContainer("Menu", new Control[0]);
    owner.add(side, Control.LEFT, Control.TOP, Control.FILL, Control.FILL);
    side.topMenu.totalTime = 100;

    assertFalse(side.topMenu.isFadeOnPopAndUnpop());
    assertTrue(side.topMenu.fadeOtherWindows);
    side.topMenu.popupNonBlocking();
    PresentationHandle handle = side.topMenu.presentationHandle();

    assertEquals(127, handle.entry.barrierAlpha);
    assertEquals(255, side.topMenu.alphaValue);
    assertNull(handle.frame.offscreen);
    tick(handle, 10);
    assertTrue(handle.frame.x > -handle.frame.width && handle.frame.x < 0);
    assertNull(handle.frame.offscreen);
    assertEquals(128, renderBarrierBrightness(handle.barrier, Color.WHITE), 1);

    tick(handle, 90);
    assertEquals(PresentationHandle.State.PRESENTED, handle.state());
    dismiss(side.topMenu);
  }

  @Test
  void fadeValueMapsToEquivalentBarrierBrightness() {
    assertEquals(255, brightnessForFadeValue(255), 1);
    assertEquals(128, brightnessForFadeValue(128), 1);
    assertEquals(0, brightnessForFadeValue(0), 1);

    TopMenu menu = menu(Control.LEFT);
    menu.fadeOtherWindows = false;
    PresentationEntry entry = menu.createPresentationEntry();
    assertEquals(0, entry.barrierAlpha);
    assertTrue(menu.isFadeOnPopAndUnpop());
  }

  @Test
  void relayoutDuringFadeStabilizesAndCleansOwnedState() {
    TopMenu menu = menu(Control.LEFT);
    menu.popupNonBlocking();
    PresentationHandle handle = menu.presentationHandle();
    tick(handle, 10);
    assertNotNull(handle.frame.offscreen);

    Window._updateSafeAreaInsets(12, 24, 32, 44);

    assertEquals(PresentationHandle.State.PRESENTED, handle.state());
    assertEquals(252, handle.viewport.width);
    assertEquals(0, handle.frame.x);
    assertNull(handle.frame.offscreen);
    assertNull(handle.animation());
    assertTrue(Control.enableUpdateScreen);
    dismiss(menu);
  }

  @Test
  void repeatedPresentationStartsWithFreshFadeState() {
    TopMenu menu = menu(Control.CENTER);
    menu.popupNonBlocking();
    PresentationHandle first = menu.presentationHandle();
    tick(first, 10);
    tick(first, 90);
    dismiss(menu);
    assertEquals(PresentationHandle.State.DISMISSED, first.state());
    assertNull(first.frame.offscreen);

    menu.popupNonBlocking();
    PresentationHandle second = menu.presentationHandle();
    assertTrue(second != first);
    assertNotNull(second.frame.offscreen);
    assertEquals(0, second.frame.offscreen.alphaMask);
    tick(second, 10);
    assertIntermediateAlpha(second);
    tick(second, 90);
    assertNull(second.frame.offscreen);
    dismiss(menu);
  }

  private TopMenu menu(int direction) {
    TopMenu menu = new TopMenu(new Control[] {new Spacer(10, 50)}, direction, Window.NO_BORDER);
    menu.widthInPixels = 200;
    menu.totalTime = 100;
    return menu;
  }

  private static void assertIntermediateAlpha(PresentationHandle handle) {
    assertNotNull(handle.frame.offscreen);
    assertTrue(handle.frame.offscreen.alphaMask > 0);
    assertTrue(handle.frame.offscreen.alphaMask < FadeAnimation.maxFade);
  }

  private static void tick(PresentationHandle handle, int elapsed) {
    ControlAnimation animation = handle.animation();
    assertNotNull(animation);
    animation.updateListenerTriggered(elapsed);
  }

  private static void dismiss(TopMenu menu) {
    menu.unpop();
    PresentationHandle handle = menu.presentationHandle();
    if (handle.animation() != null) {
      tick(handle, 10);
      tick(handle, 90);
    }
    assertEquals(PresentationHandle.State.DISMISSED, handle.state());
    assertNull(handle.frame.offscreen);
  }

  private int brightnessForFadeValue(int fadeValue) {
    Window.fadeValue = fadeValue;
    TopMenu menu = menu(Control.CENTER);
    menu.fadeOtherWindows = true;
    menu.totalTime = 0;
    menu.popupNonBlocking();
    PresentationHandle handle = menu.presentationHandle();
    int brightness = renderBarrierBrightness(handle.barrier, Color.WHITE);
    handle.dismiss();
    return brightness;
  }

  private static int renderBarrierBrightness(PresentationBarrier barrier, int background) {
    try {
      barrier.setRect(0, 0, 1, 1);
      Image image = new Image(1, 1);
      image.getPixels()[0] = 0xFF000000 | background;
      barrier.onPaint(image.getGraphics());
      return image.getPixels()[0] & 0xFF;
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static final class TestMainWindow extends MainWindow {
    @Override
    void setTimerInterval(int interval) {
    }
  }
}
