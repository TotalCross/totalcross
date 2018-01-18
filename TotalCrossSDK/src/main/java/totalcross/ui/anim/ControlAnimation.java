package totalcross.ui.anim;

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;

/** Abstract class used to create and handle animations
 * @since TotalCross 3.03 
 */

public abstract class ControlAnimation implements TimerListener {
  protected Control c;
  protected int totalTime;
  private TimerEvent teFrame, teDelay;
  private ControlAnimation with, then;
  private AnimationFinished animFinish;
  private int initialTime;
  private boolean slave;
  protected boolean releaseScreenShot = true;

  public static int frameRate = Settings.platform.equals(Settings.WINDOWSPHONE) ? 30 : 20;
  /** A delay issued right after the animation finishes */
  public int delayAfterFinish;

  /** Use or not the offscreen. */
  public boolean useOffscreen = true;

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
    if (!slave) {
      if (totalTime == 0) {
        animatePriv();
        return;
      }
      if (teFrame == null) {
        teFrame = c.addTimer(frameRate);
        c.addTimerListener(this);
      }
      if (useOffscreen && c.offscreen == null) {
        Control.enableUpdateScreen = false; // removes flick when clicking outside the TopMenu
        c.takeScreenShot();
        Window.needsPaint = true;
      }
    }
    if (with != null) {
      with.start();
    }
    initialTime = Vm.getTimeStamp();
  }

  public void stop(boolean abort) {
    if (teFrame != null) {
      c.removeTimer(teFrame);
      teFrame = null;
      if (releaseScreenShot) {
        c.releaseScreenShot();
      }
    }
    if (animFinish != null) {
      animFinish.onAnimationFinished(this);
    }
    if (abort) {
      ;
    } else if (delayAfterFinish > 0) {
      teDelay = c.addTimer(delayAfterFinish);
    } else {
      handleThen();
    }
  }

  private void handleThen() {
    if (then != null) {
      then.start();
    }
  }

  protected double computeSpeed(double distance) {
    int remaining = totalTime - (Vm.getTimeStamp() - initialTime);
    if (remaining <= 0) {
      return 0;
    }
    return distance * frameRate / remaining;
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
  public void timerTriggered(TimerEvent e) {
    if (teFrame != null && teFrame.triggered) {
      animatePriv();
    } else if (teDelay != null && teDelay.triggered) {
      c.removeTimer(teDelay);
      teDelay = null;
      MainWindow.getMainWindow().runOnMainThread(new Runnable() {
        @Override
        public void run() {
          handleThen();
        }
      });
    }
  }

  private void animatePriv() {
    animate();
    if (with != null) {
      with.animatePriv();
    }
    Control.enableUpdateScreen = true;
  }

  protected abstract void animate();
}
