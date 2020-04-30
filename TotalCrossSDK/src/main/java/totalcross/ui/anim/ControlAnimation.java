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

import totalcross.sys.Settings;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.UpdateListener;

/** Abstract class used to create and handle animations
 * @since TotalCross 3.03 
 */

public abstract class ControlAnimation implements UpdateListener {
  protected Control c;
  protected int totalTime;
  protected int executedTime;
  private ControlAnimation with, then;
  private AnimationFinished animFinish;
  private boolean isPlaying;
  private boolean isWaiting;
  private boolean slave;
  protected boolean releaseScreenShot = true;

  /** A delay issued right after the animation finishes */
  public int delayAfterFinish;

  /** Use or not the offscreen. */
  public boolean useOffscreen = false;

  public static interface AnimationFinished {
    public void onAnimationFinished(ControlAnimation anim);
  }

  public ControlAnimation(Control c, AnimationFinished animFinish, int totalTime) {
    this.c = c;
    this.animFinish = animFinish;
    this.totalTime = totalTime < 0 ? 800 : totalTime;
  }

  public ControlAnimation(Control c, AnimationFinished animFinish) {
    this(c, animFinish, -1);
  }

  public ControlAnimation(Control c) {
    this(c, null);
  }

  public void start() {
	executedTime = 0;
    if (!slave) {
      if (totalTime == 0) {
        internalAnimate();
        return;
      }
      MainWindow.getMainWindow().addUpdateListener(this);
      isPlaying = true;
      if (useOffscreen && c.offscreen == null) {
        Control.enableUpdateScreen = false; // removes flick when clicking outside the TopMenu
        c.takeScreenShot();
        Window.needsPaint = true;
      }
    }
    if (with != null) {
      with.start();
    }
  }

  public void stop(boolean abort) {
    if (isPlaying) {
       isPlaying = false;
       if (releaseScreenShot) {
         c.releaseScreenShot();
       }
       
       if (animFinish != null) {
         animFinish.onAnimationFinished(this);
         animFinish = null;
       }
    }
    
    if (!abort) {
       if (then != null) {
          if (isWaiting || delayAfterFinish <= 0) {
             then.start();
             isWaiting = false;
          } else {
             isWaiting = true;
          }
          executedTime = 0;
       }
    }
    
    if (!isWaiting) {
       MainWindow.getMainWindow().removeUpdateListener(this);
    }
  }
     

  protected double computeSpeed(double distance) {
    int remaining = totalTime - executedTime;
    if (remaining <= 0) {
      return 0;
    }
    return distance * Settings.getAnimationMaximumFps() / remaining;
  }

  public ControlAnimation with(ControlAnimation other) {
    this.with = other;
    other.slave = true;
    return this;
  }

  public ControlAnimation then(ControlAnimation other) {
    this.then = other;
    return this;
  }

  @Override
  public void updateListenerTriggered(int elapsedMilliseconds) {
     executedTime += elapsedMilliseconds;
     if (with != null) {
    	 with.updateListenerTriggered(elapsedMilliseconds);
 	 }

     if (!slave) {
    	 if (isPlaying) {
	        internalAnimate();
	        
	        if (executedTime >= totalTime) {
	           stop(false);
	        }
	     } else if (isWaiting) {
	        if (executedTime >= delayAfterFinish) {
	           stop(false);
	        }
	     }
     }
  }

  private void internalAnimate() { 
    animate();
    if (with != null) {
      with.internalAnimate();
    }
    Control.enableUpdateScreen = true;
  }

  protected abstract void animate();
  
  /**
   * Sets the action that will be performed after the animation finishes
   * @param animationFinished
   */
  public void setAnimationFinishedAction(AnimationFinished animationFinished) {
     this.animFinish = animationFinished;
  }
}
