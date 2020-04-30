// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.ui.event.UpdateListener;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

/** Spinner is a control that shows an image indicating that something is running in
 * the background. 
 * 
 * To start the spin call the start method, and to stop it call the stop method.
 * 
 * If you try to run a spinner inside a tight loop, be sure to call <code>spinner.update()</code> or the spinner will not be
 * updated. Without this, it will work in Java but not on devices. 
 * 
 * @since TotalCross 1.3
 */
public class Spinner extends Control implements TimerListener, UpdateListener {
  /** Used in the type field */
  public static final int IPHONE = 1;
  /** Used in the type field */
  public static final int ANDROID = 2;
  /** Used in the type field */
  public static final int SYNC = 3;

  /** Defines the type of spinner for all instances. Defaults for IPHONE when running in iPhone and
   * ANDROID for all other platforms. 
   */
  public static int spinnerType = Settings.isIOS() ? IPHONE : ANDROID;

  private static Image[] loaded = new Image[4];
  private static String[] files = { null, "totalcross/res/spinner_iphone.gif", "totalcross/res/spinner_android.gif",
      "totalcross/res/spinner_sync.gif", };
  private Image anim, anim0;
  private int timestep = 80;
  private boolean running;

  private int type = -1;

  /** Creates a spinner with the defined spinnerType. */
  public Spinner() {
    this(spinnerType);
  }

  /** Creates a spinner of the given type. */
  public Spinner(int type) {
    setType(type);
    this.transparentBackground = true;
  }

  /** Changes the Spinner to one of the predefined types. */
  public void setType(int t) {
    if (t < IPHONE || t > SYNC) {
      throw new IllegalArgumentException("Invalid type");
    }
    try {
      this.setImage(loaded[t] == null ? loaded[t] = new Image(files[t]) : loaded[t]);
    } catch (Exception e) {
      if (Settings.onJavaSE) {
        e.printStackTrace();
      }
    }
    this.type = t;
  }

  /** Creates a spinner from an animated GIF.
   * You can download additional animations from: <a href='http://preloaders.net/en'>here</a>. 
   * Change only the given settings:
   * <ul>
   *  <li> Image type: GIF
   *  <li> Transparent background: Yes
   *  <li> Foreground color: FFFFFF if the animation is only black, 000000 if it has fade.
   *  <li> Background color: 000000
   *  <li> Keep size 128 x 128
   * </ul>
   * Then press Generate preloader and download the gif file that will appear at the right pane.
   * If the spinner is moving counterclockwise, you can make it go clickwise by changing also, under the  Advanced options:
   * <ul>
   *  <li> Flip image: Hor
   *  <li> Reverse animation: Yes
   * </ul>
   * The image is colorized with the foreground color. 
   * If it appears not filled, try selecting the "Invert colors" option, and use 000000 as foreground color.
   */

  public Spinner(Image anim) {
    this.setImage(anim);
    this.transparentBackground = true;
  }

  /** Changes the gif image of this Spinner */
  public void setImage(Image anim) {
    this.anim0 = anim;
    this.anim = null;
    this.type = -1;
  }

  @Override
  public void onBoundsChanged(boolean screenChanged) {
    anim = null;
  }

  @Override
  public void onColorsChanged(boolean changed) {
    anim = null;
  }

  @Override
  public void onPaint(Graphics g) {
    if (!Settings.isOpenGL) {
      g.backColor = backColor;
      if (!transparentBackground) {
        g.fillRect(0, 0, width, height);
      }
    }
    if (anim == null) {
      checkAnim();
    }
    if (anim != null) {
      g.drawImage(anim, (width - anim.getWidth()) / 2, (height - anim.getHeight()) / 2);
    }
  }

  private void checkAnim() {
    try {
      anim = anim0.smoothScaledFixedAspectRatio(width < height ? width : height, true);
      if (type != -1) {
        anim.applyColor2(getForeColor() | 0xAA000000);
      }
    } catch (Exception e) {
      anim = null;
    }
  }

  /** Starts the spinning thread. */
  public void start() {
    if (running) {
      return;
    }
    running = true;
    MainWindow.getMainWindow().addUpdateListener(this);
  }

  /** Stops the spinning thread. */
  public void stop() {
	MainWindow.getMainWindow().removeUpdateListener(this);
    running = false;
  }

  /** Returns if the spin is running. */
  public boolean isRunning() {
    return running;
  }

    private void step() {
        if (anim == null) {
            checkAnim();
        }
        // don't update if we loose focus
        if (getParentWindow() == Window.topMost && anim != null) {
            anim.nextFrame();
            Window.repaintActiveWindows();
        }
    }
  
  @Override
  public void timerTriggered(TimerEvent e) {
	step();
  }

    /**
     * Updates the spinner; call this when using the spinner inside a loop.
     * 
     * @deprecated Does nothing use {@link #start()} and {@link #stop()} instead
     */
    @Deprecated
    public void update() {
    }

  /** Gets the timestep used to change images
   *  @return time in milliseconds
   */
  public int getTimestep() {
	return timestep;
  }
  /** Sets the timestep used to change images.
   *  @param timestep time in milliseconds
   */
  public void setTimestep(int timestep) {
	this.timestep = timestep;
  }

    private int lastElapsed = 0;

    @Override
    public void updateListenerTriggered(int elapsedMilliseconds) {
        lastElapsed += elapsedMilliseconds;
        if (lastElapsed >= timestep) {
            step();
            lastElapsed = 0;
        }
    }
}
