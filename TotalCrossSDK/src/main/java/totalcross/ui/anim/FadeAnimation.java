// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.anim;

import totalcross.ui.Control;
import totalcross.ui.Window;
import totalcross.ui.event.TimerListener;

public class FadeAnimation extends ControlAnimation {
  private int a, at, af, _maxFade;
  private boolean fadeIn;
  public static int DEFAULT_MAX_FADE = 255;
  /** Change this will affect all fade animations until you reset it to DEFAULT_MAX_FADE.
   * @see totalcross.ui.Toast 
   */
  public static int maxFade = 255;

  private FadeAnimation(Control c, boolean fadeIn, AnimationFinished animFinish, int totalTime) {
    super(c, animFinish, totalTime);
    this.fadeIn = fadeIn;
    at = _maxFade = maxFade;
    a = fadeIn ? 0 : maxFade;
    af = fadeIn ? maxFade : 0;
    releaseScreenShot = maxFade == 255;
  }

  @Override
  public void start() {
    super.start();
    if(c.offscreen != null)
      c.offscreen.alphaMask = a;
  }

  @Override
  protected void animate() {
    int speed = (int) computeSpeed(at);
    at -= speed;
    a += fadeIn ? speed : -speed;
    if (a > _maxFade) {
      a = _maxFade;
    } else if (a < 0) {
      a = 0;
    }
    if (c.offscreen != null) {
      c.offscreen.alphaMask = a;
    }
    if (c.offscreen0 != null) {
      c.offscreen0.alphaMask = _maxFade - a;
    }
    Window.needsPaint = true;
    if (a == af || speed == 0) {
      a = af;
      stop(false);
    }
  }

  /** Creates a fade animation, fading in or out the given control
   * @param c The control to be faded
   * @param fadeIn True will make the control appear, false will make it disappear.
   * @param animFinish An interface method to be called when the animation finished, or null if none.
   * @param totalTime The total time in millis that the animation will take, or -1 to use the default value (800ms).
   */
  public static FadeAnimation create(Control c, boolean fadeIn, AnimationFinished animFinish, int totalTime) {
    try {
      return new FadeAnimation(c, fadeIn, animFinish, totalTime);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
