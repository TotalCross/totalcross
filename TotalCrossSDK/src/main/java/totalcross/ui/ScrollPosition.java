// Copyright (C) 2001-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import totalcross.res.Resources;
import totalcross.sys.Settings;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/**
 * ScrollPosition implements the auto-hide scrollbar that exists in 
 * finger-touched devices. This special scrollbar is just a small position indicator
 * that appears when the area is dragged. The ScrollPosition does not take 
 * an area of the control, since it appears and disappears automatically.
 * 
 * All Scrollable controls change their ScrollBar by the ScrollPosition when
 * Settings.fingerTouch is true.
 * 
 * If the back color and the bar color are the same, the bar is not drawn; this is how
 * the ButtonMenu class hides this control.
 * 
 * @see totalcross.sys.Settings#fingerTouch
 * @see totalcross.ui.Scrollable
 * @see totalcross.ui.UIColors#positionbarColor
 * @see totalcross.ui.UIColors#positionbarBackgroundColor
 */

public class ScrollPosition extends ScrollBar implements Scrollable, PenListener, TimerListener {
  private boolean verticalScroll, isPenDown, autoHide = AUTO_HIDE;
  private Image npback, handle;
  private TimerEvent timer;
  private int visibleCount;
  private boolean internalVisibility;
  private boolean neverShow;
  private boolean showHandle = true;
  
  private NinePatch.Parts types[];

  /** Set to false to make the PositionBar always show (instead of the default auto-hide behaviour). */
  public static boolean AUTO_HIDE = true;

  /** By default, the ScrollPosition is shown during 1 second to let the user know that he can scroll.
   * This value is a number that will be multiplied by 500. Defaults to 2, set to 0 to disable this feature.
   * @since TotalCross 3.07
   */
  public static int VISIBLE_COUNT = 2;

  /** The bar color. Defaults to UIColors.positionbarColor but can be changed to something else. */
  public int barColor = UIColors.positionbarColor;

  /** Defines the height multiplier that must be reached to show the handle while scrolling.
   * The handle speeds up scrolling since the user can drag it (like the bar in a ProgressBar).
   * It is very useful for long lists.
   *
   * Setting this to 10 will make the handle appear when the height of the item's list exceeds 10 times
   * the height of the ScrollContainer.
   *
   * Set it to 0 will always show the handle, set to something very big (like Convert.MAX_INT) to never show
   * the handle.
   * 
   * Defaults to 7.
   *
   * @since TotalCross 1.3
   */
  public static int heightMultiplierToShowHandle = 7;

  /** Constructs a vertical ScrollPosition. */
  public ScrollPosition() {
    this(VERTICAL);
  }

  /** Constructs a ScrollPosition with the given orientation.
   * @see #VERTICAL
   * @see #HORIZONTAL
   */
  public ScrollPosition(byte orientation) {
    super(orientation);
    btnInc.setVisible(false);
    btnDec.setVisible(false);
    disableBlockIncrement = true;
    enableAutoScroll = false;
    tempShow();
    
    types = new NinePatch.Parts[2];
    types[0] = NinePatch.getInstance().load(Resources.scrollposv, 3, 2);
    types[1] = NinePatch.getInstance().load(Resources.scrollposh, 3, 2);
  }

  /** Call this to never show the ScrollPosition */
  public void setNeverShow() {
    neverShow = true;
    internalVisibility = false;
  }

  @Override
  public void tempShow() {
	internalVisibility = !neverShow;//!autoHide;
	Window.needsPaint = true;
    if (autoHide) {
      visibleCount = VISIBLE_COUNT;
      if (timer == null) {
        timer = addTimer(500);
        addTimerListener(this);
      }
      Window.needsPaint = true;
    }
  }

  @Override
  public void onBoundsChanged(boolean b) {
    super.onBoundsChanged(b);
    npback = null;
    if (parent instanceof Scrollable) {
      Flick f = ((Scrollable) parent).getFlick();
      if (f != null) {
        f.addScrollableListener(this);
      }
      parent.addPenListener(this);
    }
  }

  @Override
  public void onColorsChanged(boolean b) {
    super.onColorsChanged(b);
    npback = null;
  }

  private Image getHandleImage() {
    Image img = null;
    try {
      if(npPartsHandle == null) {
	      img = Resources.progressHandle.getSmoothScaledInstance(fmH * 2, dragBarSize);
	      img.applyColor(barColor);
      } else
    	  img = NinePatch.getInstance().getNormalInstance(npPartsHandle, fmH * 2, dragBarSize, backColor, false);
    } catch (ImageException e) {
      if (Settings.onJavaSE) {
        e.printStackTrace();
      }
    }
    return img;
  }

  @Override
  public void onPaint(Graphics g) {
    if (barColor == backColor || !internalVisibility) {
      return;
    }

    if (UIColors.positionbarBackgroundColor != -1) {
      g.backColor = UIColors.positionbarBackgroundColor;
      g.fillRect(0, 0, width, height);
    }
    if (isEnabled() || !autoHide) {
      g.backColor = barColor;
      if (uiAndroid) {
        // change to a handle instead of the position bar?
        if (verticalBar) {
          if ((Flick.currentFlick != null || startDragPos != -1)
              && (maximum - minimum) >= heightMultiplierToShowHandle * height) {
            if (dragBarSize == minDragBarSize) {
              thumbSize = fmH * 3;
              setRect(RIGHT, KEEP, fmH * 2, KEEP); // parameters will be recomputed
              return;
            }

            if (showHandle) {
            	if (handle == null || handle.getHeight() != dragBarSize) {
                    handle = getHandleImage();
                  }
                  if (handle != null) {
                    int w = handle.getWidth();

                    if (this.width != w) {
                      thumbSize = fmH * 3;
                      setRect(RIGHT, KEEP, fmH * 2, KEEP); // parameters will be recomputed
                    }
                  }
            }
          }
          /* this was disabled due to two problems:
           * 1. makes the drag of a ListBox cause a repaint even if the drag is below the height of a line
           * 2. makes the ScrollPosition flick on screen
               else
               if (resetHandle())
                  return;
           */ }
        if (npback == null || ((verticalBar ? npback.getHeight() : npback.getWidth()) != dragBarSize)) {
          if(npParts == null)
        	  setNinePatch(verticalBar ? Resources.scrollposv : Resources.scrollposh, 3, 2);
          try {
            if (verticalBar) {
              npback = NinePatch.getInstance().getNormalInstance(npParts, width, dragBarSize, barColor,
                  false);
            } else {
              npback = NinePatch.getInstance().getNormalInstance(npParts, dragBarSize, height, barColor,
                  false);
            }
            npback.alphaMask = alphaValue;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        if (isHandle() && showHandle) {
          NinePatch.tryDrawImage(getGraphics(), handle, 0, dragBarPos); // when the button is pressed, the graphic's clip becomes invalid
        } else if (npback != null) {
          NinePatch.tryDrawImage(g, npback, verticalBar ? 0 : dragBarPos, verticalBar ? dragBarPos : 0);
        }
      } else {
        if (verticalBar) {
          g.fillRect(0, dragBarPos, width, dragBarSize);
        } else {
          g.fillRect(dragBarPos, 0, dragBarSize, height);
        }
      }
    }
  }

  private boolean isHandle() {
    return handle != null && this.width == handle.getWidth();
  }

  private boolean resetHandle() {
    boolean ret = false;
    int w = getPreferredWidth();
    if (verticalBar && this.width != w) {
      ret = true;
      thumbSize = 0;
      setRect(RIGHT, KEEP, w, KEEP);
    }
    if (autoHide && internalVisibility && Flick.currentFlick == null) {
    	internalVisibility = false;
    }
    Window.needsPaint = true;
    return ret;
  }

  @Override
  public void onEvent(Event e) {
    super.onEvent(e);
    switch (e.type) {
    case PenEvent.PEN_DRAG:
      if (e.target == this) {
        Event.clearQueue(PenEvent.PEN_DRAG);
        penDrag((DragEvent) e);
        e.consumed = true;
      }
      break;
    case PenEvent.PEN_UP:
      resetHandle();
      break;
    }
  }

  @Override
  public int getPreferredWidth() {
    return verticalBar ? uiAndroid ? Math.max(7, fmH / 4) : fmH / 4 : fmH;
  }

  @Override
  public int getPreferredHeight() {
    return !verticalBar ? uiAndroid ? Math.max(7, fmH / 4) : fmH / 4 : fmH;
  }

  @Override
  public boolean flickStarted() {
    if (!internalVisibility && autoHide && verticalBar == verticalScroll) {
    	internalVisibility = !neverShow;
    	Window.needsPaint = true;
    }

    return true;
  }

  @Override
  public void flickEnded(boolean atPenDown) {
    if (!atPenDown && internalVisibility && autoHide && verticalBar == verticalScroll) {
    	internalVisibility = false;
    } else if (!autoHide && isHandle()) {
      resetHandle();
      Window.needsPaint = true;
    }
  }

  // none of these methods are called
  @Override
  public boolean canScrollContent(int direction, Object target) {
    return false;
  }

  @Override
  public boolean scrollContent(int xDelta, int yDelta, boolean fromFlick) {
    return false;
  }

  @Override
  public int getScrollPosition(int direction) {
    return 0;
  }

  @Override
  public Flick getFlick() {
    return null;
  }

  @Override
  public void penDown(PenEvent e) {
    isPenDown = true;
    Window.topMost.cancelPenUpListeners.addElement(this);
  }

  @Override
  public void penUp(PenEvent e) {
    isPenDown = false;
    if (Flick.currentFlick == null || e == null) {
      resetHandle();
    }
    try {
      Window.topMost.cancelPenUpListeners.removeElement(this);
    } catch (Exception ee) {
    }
  }

  @Override
  public void penDrag(DragEvent e) {
    verticalScroll = e.direction == DragEvent.DOWN || e.direction == DragEvent.UP;
    if (autoHide && !internalVisibility && verticalBar == verticalScroll) {
    	internalVisibility = !neverShow;
    	Window.needsPaint = true;
    }
  }

  @Override
  public void penDragStart(DragEvent e) {
  }

  @Override
  public void penDragEnd(DragEvent e) {
    isPenDown = false;
    if (autoHide && internalVisibility && Flick.currentFlick == null) {
    	internalVisibility = false;
    }
  }
  
  /** Sets the visibility of the temporary scrollbar handler. */
  public void setHandleVisibility(boolean visible) {
	  showHandle = visible;
  }

  @Override
  public void timerTriggered(TimerEvent e) {
    if (timer != null && timer.triggered) {
      if (internalVisibility && autoHide && visibleCount >= 0) {
        if (--visibleCount == 0) {
          internalVisibility = false;
          Window.needsPaint = true;
        }
        return;
      }
      if (internalVisibility && autoHide && Flick.currentFlick == null && !isPenDown) {
        resetHandle();
      }
      if (!isDisplayed()) {
        removeTimer(timer);
        removeTimerListener(this);
        timer = null;
      }
    }
  }

  @Override
  public boolean wasScrolled() {
    return false;
  }

  /** Resets position to 0 and posts a pressed event. */
  @Override
  public void clear() {
    super.clear();
    postPressedEvent();
  }
}
