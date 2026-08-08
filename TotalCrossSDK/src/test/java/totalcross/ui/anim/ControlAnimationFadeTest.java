// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.anim;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.image.Image;

class ControlAnimationFadeTest {
  private boolean originalEnableUpdateScreen;
  private int originalMaxFade;
  private Control target;

  @BeforeAll
  static void initializeFontBackend() {
    new Launcher();
    if (MainWindow.getMainWindow() == null) {
      new MainWindow();
    }
  }

  @BeforeEach
  void createTarget() {
    originalEnableUpdateScreen = Control.enableUpdateScreen;
    originalMaxFade = FadeAnimation.maxFade;
    FadeAnimation.maxFade = FadeAnimation.DEFAULT_MAX_FADE;
    Control.enableUpdateScreen = true;

    Window owner = new Window();
    owner.setRect(0, 0, 240, 320);
    Container parent = new Container();
    owner.add(parent, Control.LEFT, Control.TOP, 240, 320);
    target = new Container();
    parent.add(target, 10, 20, 60, 40);
  }

  @AfterEach
  void restoreGlobals() {
    if (target != null) {
      target.releaseScreenShot();
    }
    Control.enableUpdateScreen = originalEnableUpdateScreen;
    FadeAnimation.maxFade = originalMaxFade;
  }

  @Test
  void standaloneFadeUsesIntermediateAlphaAndReleasesOwnedScreenshot() {
    final boolean[] finished = {false};
    FadeAnimation fade = FadeAnimation.create(target, true, anim -> finished[0] = true, 100);

    fade.start();
    assertNotNull(target.offscreen);
    fade.updateListenerTriggered(10);

    assertTrue(target.offscreen.alphaMask > 0);
    assertTrue(target.offscreen.alphaMask < FadeAnimation.maxFade);

    fade.updateListenerTriggered(90);
    assertTrue(finished[0]);
    assertNull(target.offscreen);
  }

  @Test
  void pathWithFadeMovesAndFadesAtTheSameIntermediateFrame() {
    PathAnimation path = PathAnimation.create(target, 10, 20, 110, 20, null, 100);
    FadeAnimation fade = FadeAnimation.create(target, true, null, 100);

    path.with(fade).start();
    path.updateListenerTriggered(10);

    assertTrue(target.getX() > 10 && target.getX() < 110);
    assertNotNull(target.offscreen);
    assertTrue(target.offscreen.alphaMask > 0);
    assertTrue(target.offscreen.alphaMask < FadeAnimation.maxFade);

    path.updateListenerTriggered(90);
    assertNull(target.offscreen);
  }

  @Test
  void immediateAbortRestoresScreenUpdatesAndReleasesOwnedScreenshot() {
    FadeAnimation fade = FadeAnimation.create(target, true, null, 100);

    fade.start();
    assertTrue(Control.enableUpdateScreen);
    fade.stop(true);

    assertTrue(Control.enableUpdateScreen);
    assertNull(target.offscreen);
  }

  @Test
  void callerOwnedOffscreenSurvivesFadeCompletion() throws Exception {
    Image callerOwned = new Image(60, 40);
    target.offscreen = callerOwned;
    FadeAnimation fade = FadeAnimation.create(target, false, null, 100);

    fade.start();
    fade.updateListenerTriggered(10);
    fade.updateListenerTriggered(90);

    assertSame(callerOwned, target.offscreen);
  }

  @Test
  void unrelatedInitialScreenshotSurvivesFadeCompletion() throws Exception {
    Image initial = new Image(60, 40);
    target.offscreen0 = initial;
    FadeAnimation fade = FadeAnimation.create(target, true, null, 100);

    fade.start();
    fade.updateListenerTriggered(10);
    fade.updateListenerTriggered(90);

    assertSame(initial, target.offscreen0);
    assertNull(target.offscreen);
  }
}
