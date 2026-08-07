package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.ui.anim.ControlAnimation;
import totalcross.ui.anim.ControlAnimation.AnimationFinished;
import totalcross.ui.anim.FadeAnimation;
import totalcross.ui.anim.PathAnimation;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;

/** A window with a top bar + return button supporting slide-in animations. */
public class SlidingWindow extends Window implements PenListener, KeyListener {
  protected Presenter<Container> provider;
  protected ControlAnimation currentAnimation;
  protected int animDir;
  protected int slackSpace;
	protected int totalTime = 400;
  protected Spinner delayedUiSpinner;

  protected boolean delayInitUI;

  public SlidingWindow(Presenter<Container> provider) {
    this(false, provider);
  }

  public SlidingWindow(boolean delayInitUI, Presenter<Container> provider) {
    super(null, Window.NO_BORDER);
    this.provider = provider;
    this.delayInitUI = delayInitUI;
    fadeOtherWindows = false;
    animDir = BOTTOM;
    slackSpace = 0;

    this.addPenListener(this);
    this.addKeyListener(this);
    this.callListenersOnAllTargets = true;
  }

  /**
   * Sets the final bounds of this window.
   *
   * The final bounds must be established before the children are laid out so
   * SafeAreaMode.AUTO can determine which screen edges are touched.
   */
  protected void setRect(boolean screenResized) {
    int x = 0;
    int y = 0;

    switch (animDir) {
    case LEFT:
      x = -slackSpace;
      break;

    case RIGHT:
      x = slackSpace;
      break;

    case TOP:
      y = -slackSpace;
      break;

    case BOTTOM:
      y = slackSpace;
      break;

    case CENTER:
    default:
      break;
    }

    resetSetPositions();
    setRect(x, y, SCREENSIZE, SCREENSIZE, null, screenResized);
  }

  /**
   * Moves the window to the initial animation position without changing its
   * size or its stored layout coordinates.
   *
   * The children were already laid out using the final window position, so
   * changing x/y directly here is intentional: the temporary off-screen
   * position must not cause the safe-area layout to be recalculated.
   */
  private void moveToAnimationStart() {
    int pw = Settings.screenWidth;
    int ph = Settings.screenHeight;

    switch (animDir) {
    case LEFT:
      x = -width;
      y = (ph - height) / 2;
      break;

    case RIGHT:
      x = pw;
      y = (ph - height) / 2;
      break;

    case TOP:
      x = (pw - width) / 2;
      y = -height;
      break;

    case BOTTOM:
      x = (pw - width) / 2;
      y = ph;
      break;

    case CENTER:
    default:
      break;
    }
  }

  @Override
  public void unpop() {
    if (currentAnimation != null) {
      return;
    }

    if (animDir == CENTER) {
      currentAnimation = FadeAnimation.create(this, false, null, totalTime);
    } else {
      currentAnimation =
          PathAnimation.create(this, -animDir, null, totalTime, slackSpace);
    }

    currentAnimation.setAnimationFinishedAction(new AnimationFinished() {
      @Override
      public void onAnimationFinished(ControlAnimation anim) {
        currentAnimation = null;
        SlidingWindow.super.unpop();
      }
    });

    currentAnimation.start();
  }

  @Override
  public void popup() {
    /*
     * First establish the real destination bounds. Besides defining the final
     * width/height, this allows initUI() and child layout to use the correct
     * safe area.
     */
    setRect(false);

    /*
     * Reposition existing children as well. This is important when the same
     * SlidingWindow is reopened after a screen resize or orientation change.
     *
     * At this point the window is still at its final location, so AUTO safe
     * area detection uses the correct screen edges.
     */
    repositionChildren();

    /*
     * Only after layout has been calculated do we move the window outside the
     * visible area. Width/height and child layout remain unchanged.
     *
     * This prevents the destination window from being painted for one frame
     * before PathAnimation applies its first position.
     */
    if (animDir != CENTER) {
      moveToAnimationStart();
    }

    super.popup();
  }

  @Override
  public void initUI() {
    if (!delayInitUI) {
      Container c = provider.getView();
      add(c, LEFT, TOP, FILL, FILL, this);
    } else {
      delayedUiSpinner = new Spinner();
      add(delayedUiSpinner, CENTER, CENTER);
      delayedUiSpinner.start();
    }
  }

  @Override
  protected void postPopup() {
    if (delayInitUI) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          Container view = provider.getView();

          MainWindow.mainWindowInstance.runOnMainThread(new Runnable() {
            @Override
            public void run() {
              add(view, LEFT, AFTER, FILL, FILL, SlidingWindow.this);
              remove(delayedUiSpinner);
              delayedUiSpinner.stop();
            }
          });
        }
      }).start();
    }
  }

  @Override
  public void onPopup() {
    if (currentAnimation != null) {
      return;
    }

    if (animDir == CENTER) {
      currentAnimation = FadeAnimation.create(this, true, null, totalTime);
    } else {
      /*
       * The window is already at the same off-screen position that
       * PathAnimation uses as its starting point, so the first animation frame
       * no longer causes a visible jump.
       */
      currentAnimation =
          PathAnimation.create(this, animDir, null, totalTime, slackSpace);
    }

    currentAnimation.setAnimationFinishedAction(new AnimationFinished() {
      @Override
      public void onAnimationFinished(ControlAnimation anim) {
        currentAnimation = null;
      }
    });

    currentAnimation.start();
  }

  @Override
  public void screenResized() {
    /*
     * PathAnimation changes the stored setX/setY when it finishes. Always
     * reconstruct SlidingWindow's destination explicitly.
     */
    setRect(true);

    /*
     * The window is now at its real destination, therefore safe-area edge
     * detection is valid while children are repositioned.
     */
    repositionChildren();
  }

  /** Gets the slack space left by this window on pop-up. */
  public int getSlackSpace() {
    return slackSpace;
  }

  /** Sets the slack space left by this window on pop-up. */
  public void setSlackSpace(int slackSpace) {
    this.slackSpace = slackSpace;
  }

  @Override
  public void penDrag(DragEvent e) {
    double margin = 0.20;

    if (animDir == RIGHT
        && e.direction == DragEvent.RIGHT
        && e.xTotal > 150
        && (e.x - e.xTotal) < width * margin) {
      SlidingWindow.this.unpop();
    }

    if (animDir == BOTTOM
        && e.direction == DragEvent.DOWN
        && e.yTotal > 150
        && (e.y - e.yTotal) < height * margin) {
      SlidingWindow.this.unpop();
    }

    if (animDir == LEFT
        && e.direction == DragEvent.LEFT
        && e.xTotal < -150
        && (e.x - e.xTotal) > width * (1 - margin)) {
      SlidingWindow.this.unpop();
    }

    if (animDir == TOP
        && e.direction == DragEvent.UP
        && e.yTotal < -150
        && (e.y - e.yTotal) > height * (1 - margin)) {
      SlidingWindow.this.unpop();
    }
  }

  @Override
  public void penDown(PenEvent e) {
  }

  @Override
  public void penUp(PenEvent e) {
  }

  @Override
  public void penDragStart(DragEvent e) {
  }

  @Override
  public void penDragEnd(DragEvent e) {
  }

  @Override
  public void specialkeyPressed(KeyEvent e) {
    if (e.key == SpecialKeys.ESCAPE) {
      SlidingWindow.this.unpop();
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
  }

  @Override
  public void actionkeyPressed(KeyEvent e) {
  }
}
