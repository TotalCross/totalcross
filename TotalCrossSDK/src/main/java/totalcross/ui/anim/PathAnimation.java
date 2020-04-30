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
import totalcross.ui.Window;

/** Creates an animation that follows a path. 
 * @since TotalCross 3.03
 */

public class PathAnimation extends ControlAnimation {
  private int xf, yf, x, y;
  private int dir;

  public static interface SetPosition {
    public void setPos(int x, int y);
  }

  public SetPosition setpos;

  private PathAnimation(Control c, AnimationFinished animFinish, int totalTime) {
    super(c, animFinish, totalTime);
  }

  private void setPath(int x0, int y0, int xf, int yf) {
    x = x0;
    y = y0;
    this.xf = xf;
    this.yf = yf;
  }

  @Override
  protected void animate() {
    update();
    if (setpos != null) {
      setpos.setPos(x, y);
    } else {
      c.setRect(x, y, Control.KEEP, Control.KEEP);
    }
    Window.needsPaint = true;
  }

  @Override
  public void stop(boolean abort) {
    super.stop(abort);
    if (setpos == null) {
      switch (dir) {
      case Control.LEFT:
        c.setSet(Control.LEFT, Control.TOP);
        break;
      case Control.RIGHT:
        c.setSet(Control.RIGHT, Control.TOP);
        break;
      case Control.TOP:
        c.setSet(Control.CENTER, Control.TOP);
        break;
      case Control.BOTTOM:
        c.setSet(Control.CENTER, Control.BOTTOM);
        break;
      case Control.CENTER:
        c.setSet(Control.CENTER, Control.CENTER);
        break;
      }
    }
  }

  private void update() {
    double distanceRemaining = Math.sqrt((xf - x) * (xf - x) + (yf - y) * (yf - y));
    int speed = (int) Math.ceil(computeSpeed(distanceRemaining));
    if ((x == xf && y == yf) || speed == 0) {
      x = xf;
      y = yf;
      stop(false);
      return;
    }
    int dx = xf - this.x;
    int dy = yf - this.y;
    int steps;

    if (dx == 0) // vertical move
    {
      steps = Math.min(dy >= 0 ? dy : -dy, speed);
      if (dy < 0) {
        this.y -= steps;
      } else if (dy > 0) {
        this.y += steps;
      }
    } else if (dy == 0) // horizontal move
    {
      steps = Math.min(dx >= 0 ? dx : -dx, speed);
      if (dx < 0) {
        this.x -= steps;
      } else if (dx > 0) {
        this.x += steps;
      }
    } else {
      dx = dx >= 0 ? dx : -dx;
      dy = dy >= 0 ? dy : -dy;
      int CurrentX = this.x;
      int CurrentY = this.y;
      int Xincr = (this.x > xf) ? -1 : 1;
      int Yincr = (this.y > yf) ? -1 : 1;
      steps = speed;
      if (dx >= dy) {
        int dPr = dy << 1;
        int dPru = dPr - (dx << 1);
        int P = dPr - dx;
        for (; dx >= 0 && steps > 0; dx--) {
          this.x = CurrentX;
          this.y = CurrentY;
          CurrentX += Xincr;
          steps--;
          if (P > 0) {
            CurrentY += Yincr;
            steps--;
            P += dPru;
          } else {
            P += dPr;
          }
        }
      } else {
        int dPr = dx << 1;
        int dPru = dPr - (dy << 1);
        int P = dPr - dy;
        for (; dy >= 0 && steps > 0; dy--) {
          this.x = CurrentX;
          this.y = CurrentY;
          CurrentY += Yincr;
          steps--;
          if (P > 0) {
            CurrentX += Xincr;
            steps--;
            P += dPru;
          } else {
            P += dPr;
          }
        }
      }
    }
  }

  /** Creates a path animation, moving the control to the given x and y positions.
   * @param c The control to be moved
   * @param toX The destination X coordinate
   * @param toY The destination Y coordinate
   * @param animFinish An interface method to be called when the animation finished, or null if none.
   * @param totalTime The total time in millis that the animation will take, or -1 to use the default value (800ms).
   */
  public static PathAnimation create(Control c, int toX, int toY, AnimationFinished animFinish, int totalTime) {
    PathAnimation anim = new PathAnimation(c, animFinish, totalTime);
    anim.setPath(c.getX(), c.getY(), toX, toY);
    return anim;
  }

  /** Creates a path animation, moving the control from a position to another.
   * @param c The control to be moved
   * @param fromX The origin X coordinate
   * @param fromY The origin Y coordinate
   * @param toX The destination X coordinate
   * @param toY The destination Y coordinate
   * @param animFinish An interface method to be called when the animation finished, or null if none.
   * @param totalTime The total time in millis that the animation will take, or -1 to use the default value (800ms).
   */
  public static PathAnimation create(Control c, int fromX, int fromY, int toX, int toY, AnimationFinished animFinish,
      int totalTime) {
    PathAnimation anim = new PathAnimation(c, animFinish, totalTime);
    anim.setPath(fromX, fromY, toX, toY);
    return anim;
  }

  /** Creates a path animation, moving the control in a direction.
   * @param c The control to be moved
   * @param direction One of BOTTOM, -BOTTOM, TOP, -TOP, LEFT, -LEFT, RIGHT, -RIGHT. Any other value will return null.
   * @param animFinish An interface method to be called when the animation finished, or null if none.
   * @param totalTime The total time in millis that the animation will take, or -1 to use the default value (800ms).
   */
  public static PathAnimation create(Control c, int direction, AnimationFinished animFinish, int totalTime) {
	  return PathAnimation.create(c, direction, animFinish, totalTime, 0);
  }
  
  /** Creates a path animation, moving the control in a direction.
   * @param c The control to be moved
   * @param direction One of BOTTOM, -BOTTOM, TOP, -TOP, LEFT, -LEFT, RIGHT, -RIGHT. Any other value will return null.
   * @param animFinish An interface method to be called when the animation finished, or null if none.
   * @param totalTime The total time in millis that the animation will take, or -1 to use the default value (800ms).
   */
  public static PathAnimation create(Control c, int direction, AnimationFinished animFinish, int totalTime, int slack) {
    PathAnimation anim = new PathAnimation(c, animFinish, totalTime);
    anim.dir = direction;
    int x0, y0, xf, yf;
    int pw = c instanceof Window ? Settings.screenWidth : c.getParent().getWidth();
    int ph = c instanceof Window ? Settings.screenHeight : c.getParent().getHeight();
    int cw = c.getWidth();
    int ch = c.getHeight();
    xf = x0 = (pw - cw) / 2;
    y0 = yf = (ph - ch) / 2;
    switch (direction) {
    case -Control.BOTTOM:
      y0 = c.getY();
      yf = ph;
      break;
    case Control.BOTTOM:
      y0 = ph;
      yf = ph - ch + slack;
      break;
    case -Control.TOP:
      y0 = c.getY();
      yf = -ch;
      break;
    case Control.TOP:
      y0 = -ch;
      yf = -slack;
      break;
    case -Control.LEFT:
      x0 = c.getX();
      xf = -cw;
      break;
    case Control.LEFT:
      x0 = -cw;
      xf = -slack;
      break;
    case -Control.RIGHT:
      x0 = c.getX();
      xf = pw;
      break;
    case Control.RIGHT:
      x0 = pw;
      xf = pw - cw + slack;
      break;
    default:
      return null;
    }

    anim.setPath(x0, y0, xf, yf);
    return anim;
  }
}
