// Copyright (C) 2003 Arnaud Farine
// Copyright (C) 2003-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.MouseEvent;
import totalcross.ui.event.MouseListener;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;

/**
 * Displays a tooltip when user holds the pen in the control.
 * On Windows and Linux desktop, the tooltip is also shown when the mouse stays over a control.
 * <br><br>
 * The default popup delay is 1000ms and the amount of time it will be displayed is 2000 ms; 
 * a pen up also hides the tip.
 * <p>
 * You can change some properties of the tooltip: borderColor, insideGap, distX, distY, 
 * millisDelay, millisDisplay. See their javadocs for details.
 * <p>
 * Example:<br>
 * <pre>
 * ToolTip.distX = 10; // 0 by default
 * ToolTip.distY = 4;  // 0 by default
 * ToolTip.insideGap = 8; // 4 by default
 * Button b;
 * add(b = new Button("Hello Tooltip!"),CENTER,BOTTOM);<br>
 * ToolTip t = new ToolTip(b, "Hi, this is a button");<br>
 * t.borderColor = 0x00FF00; // -1 (none) by default
 * t.millisDelay = 500;   // 1000 by default
 * t.millisDisplay = 4000;  // 2000 by default
 * t.setBackColor(Color.getRGB(250,0,0));  // same as control's container by default
 * </pre>
 * The tooltip can have multiple lines, just split them using the char '\n' in your message.<br>
 * Example :
 * <pre>
 * ToolTip t = new ToolTip(control, "Hi!\nIt's Me");
 * </pre>
 * A ControlEvent.PRESSED event will be dispatched to the attached control right before
 * the text is shown. You can then set the tip to a new value using setText; setting to an empty
 * string will disable the tooltip at that moment. Calling setControlRect also changes the rectangle
 * around which the tooltip will be displayed.
 * 
 * In Android UI, the ToolTip is displayed with a round border.
 **/
public class ToolTip extends Label implements PenListener, MouseListener {
  private static PenEvent outside;
  static {
    outside = new PenEvent();
    outside.type = PenEvent.PEN_UP;
  }

  // attributes
  private Control control; // The control which supports the tip
  private boolean shown; // guich@tc120_22

  // timers
  private TimerEvent delayTimer, displayTimer;

  /** The gap between the border and the text. Common to all tooltips. */
  public static int insideGap = 4;
  /** The x distance between the tip and the control, 0 by default. Common to all tooltips. */
  public static int distX;
  /** The y distance between the tip and the control, 0 by default. Common to all tooltips. */
  public static int distY;

  /** The amount of time that the pen must be down until the tip pops up (by default, 1000ms) */
  public int millisDelay = 1000;
  /** The amount of time that the tip will be shown (by default, 2000ms) */
  public int millisDisplay = 2000;
  /** The border color. By default, it is -1 and no border is shown */
  public int borderColor = -1; // the color around the rect

  private String msg0, msg0lines[];

  /**
   * Constructor
   * @param control the control which supports the tip. 
   * If null, you must call <code>setControlRect</code> and <code>show</code> by your own.
   * @param message the message which will be written in the tip. You can
   * make multiLine, using \n character like in the Label control.
   **/
  public ToolTip(Control control, String message) {
    super(message == null ? "" : message, LEFT);
    msg0 = super.text;
    if (msg0.indexOf('\n') != -1) {
      msg0lines = Convert.tokenizeString(msg0, '\n');
    }
    setVisible(false);
    this.control = control;
    setBackForeColors(UIColors.tooltipBack, UIColors.tooltipFore);
    transparentBackground = uiAndroid;
    if (control != null) {
      control.addPenListener(this); // i must admit: listeners simplified a LOT the ToolTip class
      control.addMouseListener(this);
      control.onEventFirst = false;
    }
    focusTraversable = false;
  }

  /** Stop using mouse events to show the tooltip.
   * @since TotalCross 1.27
   */
  public void dontShowTipOnMouseEvents() {
    if (control != null) {
      control.removeMouseListener(this);
    }
  }

  /** Use this handy method to split the text in order to correctly fit the window.
   * Here's a sample:
   * <pre>
   * String msg = "a very long text that will be split to fit in the window";
   * new ToolTip(control, ToolTip.split(msg, fm));
   * </pre>
   * Make sure that fm will be the font's FontMetrics (if you plan to change the font 
   * after calling the constructor).
   * @deprecated The split is done automatically
   * @since TotalCross 1.2
   */
  @Deprecated
  public static String split(String msg, FontMetrics fm) {
    return Convert.insertLineBreak(Settings.screenWidth - insideGap * 2, fm, msg);
  }

  /** Change the control rect to the given one. By default, its used the absolute rectangle
   * of the control passed in the constructor. The placement of the tooltip will
   * be defined based on it, in a way that the control is not obscured. */
  public void setControlRect(Rect r) {
    int xx, yy;

    Window w = control == null ? null : control.getParentWindow();
    if (w == null) {
      w = Window.getTopMost();
    }
    w.add(this); // guich@tc100b4_19: must always be (re)added to the parent container to make sure we will be the last control that will be painted
    Coord size = w.getSize();

    int ww = msg0lines != null ? fm.getMaxWidth(msg0lines, 0, msg0lines.length) : fm.stringWidth(msg0); // guich@tc120_2: moved to after w.add(this)
    if (ww == 0) {
      ww = getMaxTextWidth() + insideGap;
    }
    if (ww > Settings.screenWidth) {
      super.setText(Convert.insertLineBreakBalanced(Settings.screenWidth * 9 / 10, fm, msg0));
      ww = super.getMaxTextWidth();
    }
    int hh = getPreferredHeight() + insideGap;

    // can we place it below the control?
    if (r.y2() + distY + hh < size.y && r.height != 0) {
      yy = r.y2() + distY - w.y; // guich@tc126_48: decrease window's y
    } else {
      yy = Math.max(0, r.y - hh - distY - w.y); // guich@tc126_48: decrease window's y
    }

    // check if we don't overflow the width
    if ((r.x + ww + distX) > size.x) {
      xx = size.x - ww - distX;
    } else {
      xx = r.x + distX - w.x; // guich@tc126_48: decrease window's x
    }

    setRect(xx, yy, ww + (super.lines.length == 1 ? insideGap : fmH), hh, null, false); // careful: xx,yy are relative to window position, but the control's rect passed as parameter is ABSOLUTE
  }

  @Override
  public void onEvent(Event e) {
    if (e.type == TimerEvent.TRIGGERED) {
      if (delayTimer != null && delayTimer.triggered) {
        removeTimer(delayTimer);
        delayTimer = null;
        if (control != null) {
          setControlRect(control.getAbsoluteRect());
          control._onEvent(getPressedEvent(this)); // tell the parent that we'll popup
        }
        if (text.length() > 0) {
          show();
        }
      } else if (displayTimer != null && displayTimer.triggered) {
        hide();
        if (control != null) {
          postOutside();
        }
      }
    }
  }

  private void postOutside() {
    outside.absoluteX = outside.x = outside.absoluteY = outside.y = -(Settings.touchTolerance + 1); // use a small value because some controls (like Grid) may behave incorrectly if we use big values 
    outside.target = control;
    outside.consumed = false;
    control.postEvent(outside);
  }

  /** Shows the tooltip.
   * If you want to show the tooltip programatically, you must do something like:
   * <pre>
   * toolTip.setText(msg);
   * toolTip.setControlRect(lbCompany.getAbsoluteRect());
   * toolTip.show();
   * </pre> 
   */
  public void show() {
    displayTimer = addTimer(millisDisplay);
    setVisible(true);
    shown = true;
    Window.needsPaint = true;
  }

  /** Hides the tooltip. */
  public void hide() {
    if (displayTimer != null) {
      removeTimer(displayTimer);
    }
    displayTimer = null;
    setVisible(false);
    Window.needsPaint = true; // guich@570_93
  }

  @Override
  public void mouseMove(MouseEvent e) {
  }

  @Override
  public void mouseIn(MouseEvent e) {
    penDown(e);
  }

  @Override
  public void mouseOut(MouseEvent e) {
    penUp(e);
  }

  @Override
  public void penDown(PenEvent e) {
    delayTimer = addTimer(millisDelay);
    shown = false;
  }

  @Override
  public void penUp(PenEvent e) {
    if (e == outside) {
      return;
    }

    if (isVisible()) // tooltip not removed yet?
    {
      setVisible(false);
      Window.needsPaint = true; // guich@570_93
    }
    if (delayTimer != null || displayTimer != null) // guich@503_1: if the user removes the pen before the popup, do not show the tooltip anymore - guich@570_93: if the user removes the pen before timeout, remove the timers as well
    {
      if (shown && control != null) {
        postOutside();
      }
      if (displayTimer != null) {
        removeTimer(displayTimer);
      }
      if (delayTimer != null) {
        removeTimer(delayTimer);
      }
      displayTimer = delayTimer = null;
    }
    if (shown && e != null) {
      e.consumed = true;
    }
  }

  @Override
  public void onPaint(Graphics g) {
    if (displayTimer != null) // only draw if we're visible
    {
      int dx = (insideGap >> 1) + 1;
      int dy = (dx >> 1) - 2;
      g.backColor = backColor;
      if (uiAndroid) {
        g.fillRoundRect(0, 0, width, height, fmH / 2);
      } else {
        g.fillRect(0, 0, dx, height); // since g will be translated, fill the part that the Label won't
      }
      // draw the label, taking care to not overwrite the border
      g.translate(dx, dy);
      super.onPaint(g);
      g.translate(-dx, -dy);
      // draw the border
      if (borderColor != -1) {
        g.foreColor = borderColor;
        if (uiAndroid) {
          g.drawRoundRect(0, 0, width, height, fmH / 2);
        } else {
          g.drawRect(0, 0, width, height);
        }
      }
    }
  }

  @Override
  public void penDrag(DragEvent e) {
  }

  @Override
  public void penDragEnd(DragEvent e) {
  }

  @Override
  public void penDragStart(DragEvent e) {
  }

  @Override
  public void reposition() {
    hide();
    super.reposition();
  }

  @Override
  public void mouseWheel(MouseEvent e) {
  }
}
