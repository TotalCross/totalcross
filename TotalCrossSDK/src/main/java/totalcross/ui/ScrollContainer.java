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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import totalcross.sys.Settings;
import totalcross.ui.event.*;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.Rect;

/**
 * ScrollContainer is a container with a horizontal only, vertical only, both or no
 * ScrollBars, depending on the control positions.
 * The default unit scroll is an Edit's height (for the vertical
 * scrollbar), and the width of '@' (for the horizontal scrollbar).
 * <p>
 * <b>Caution</b>: you must not use RIGHT, BOTTOM, CENTER and FILL when setting the control bounds,
 * unless you disable the corresponding ScrollBar! The only exception to this is to use FILL on the control's height,
 * which is allowed.
 * <p>
 * Here is an example showing how it can be used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * ScrollContainer sc;
 *
 * public void initUI()
 * {
       ScrollContainer sc;
       add(sc = new ScrollContainer());
       sc.setBorderStyle(BORDER_SIMPLE);
       sc.setRect(LEFT+10,TOP+10,FILL-20,FILL-20);
       int xx = new Label("Name99").getPreferredWidth()+2; // edit's alignment
       for (int i =0; i < 100; i++)
       {
          sc.add(new Label("Name"+i),LEFT,AFTER);
          sc.add(new Edit("@@@@@@@@@@@@@@@@@@@@"),xx,SAME);
          if (i % 3 == 0) sc.add(new Button("Go"), AFTER+2,SAME,PREFERRED,SAME);
       }
 * }
 *}
 * </pre>
 */

public class ScrollContainer extends Container implements Scrollable, UpdateListener {
  /** Returns the scrollbar for this ScrollContainer. With it, you can directly
   * set its parameters, like blockIncrement, unitIncrement and liveScrolling.
   * But be careful, don't mess with the minimum, maximum and visibleItems.
   * 
   * If using Android or Material user interface style, these are replaced
   * by the ScrollPosition. If you set <code>sbV.transparentBackground=true</code> 
   * (or for sbH), you will expand the client area to over the ScrollPosition's area.
   */
  public ScrollBar sbH, sbV;

  /** The Flick object listens and performs flick animations on PenUp events when appropriate. */
  protected Flick flick;
  protected Flick disabledFlick;

  protected ClippedContainer bag;
  protected Container bag0; // used to make sure that the clipping will work
  boolean changed;
  protected int lastV = 0, lastH = 0; // eliminate duplicate events
  /** Set to true, to make the surrounding container shrink to its size. */
  public boolean shrink2size;
  private boolean isScrolling;
  private boolean scScrolled;
  private Object lastScrolled;
  private int targetX = 0;
  private int targetY = 0;
  private boolean isFromFlick = false;
  private boolean hasScrolled = false;

  /** Automatically scrolls the container when an item is clicked.
   * @see #hsIgnoreAutoScroll 
   */
  public boolean autoScroll;

  /** Defines a list of classes that will make autoScroll be ignored.
   * Use it like:
   * <pre>
   * ScrollContainer.hsIgnoreAutoScroll.add(totalcross.ui.SpinList.class);
   * ...
   * </pre>
   * This is useful if such class usually requires more than one press to have a value defined.
   * @see #autoScroll
   */
  public static HashSet<Class<?>> hsIgnoreAutoScroll = new HashSet<Class<?>>(5);

  private List<ScrollEventHandler> scrollEventHandlerList = new ArrayList<>();

  /** Standard constructor for a new ScrollContainer, with both scrollbars enabled.
   */
  public ScrollContainer() {
    this(true);
  }

  /** Returns the client rect from the area that scrolls */
  @Override
  public Rect getClientRect() // guich@200final_15
  {
    Rect r = new Rect();
    bag.getClientRect(r);
    boolean showScroll = !Settings.fingerTouch || !ScrollPosition.AUTO_HIDE;
    int sbVsize = showScroll && sbV != null && !sbV.transparentBackground ? sbV.getPreferredWidth() : 0;
    int sbHsize = showScroll && sbH != null && !sbH.transparentBackground ? sbH.getPreferredHeight() : 0;
    r.width -= sbVsize;
    r.height -= sbHsize;
    return r;
  }

  /** Returns the client rect from the ScrollContainer control */
  public Rect getRealClientRect() // guich@200final_15
  {
    Rect r = new Rect();
    this.getClientRect(r);
    return r;
  }

  /** Constructor used to specify when both scrollbars are enabled or not. */
  public ScrollContainer(boolean allowScrollBars) {
    this(allowScrollBars, allowScrollBars);
  }

  /** Constructor used to specify when each scrollbar is enabled or not.
   * By disabling the horizontal scrollbar, you can use RIGHT and CENTER on the x parameter of a control that is added.
   * By disabling the vertical scrollbar, you can use BOTTOM and CENTER on the y parameter of a control that is added.
   * @since TotalCross 1.27 
   */
  public ScrollContainer(boolean allowHScrollBar, boolean allowVScrollBar) {
    super.add(bag0 = new Container());
    bag0.add(bag = new ClippedContainer());
    bag.ignoreOnAddAgain = bag.ignoreOnRemove = true;
    bag0.ignoreOnAddAgain = bag0.ignoreOnRemove = true;
    bag.setRect(0, 0, getBagInitialWidth(), getBagInitialHeight()); // set an arbitrary size
    bag.setX = SETX_NOT_SET; // ignore this setX and use the next one
    setScrollBars(allowHScrollBar, allowVScrollBar);
    if (Settings.fingerTouch) {
      flick = new Flick(this);
    }
    MainWindow.mainWindowInstance.addUpdateListener(this);
  }

  protected void setScrollBars(boolean allowHScrollBar, boolean allowVScrollBar) {
    if (sbH != null) {
      super.remove(sbH);
      sbH = null;
    }
    if (sbV != null) {
      super.remove(sbV);
      sbV = null;
    }

    if (allowHScrollBar) {
      sbH = Settings.fingerTouch ? new ScrollPosition(ScrollBar.HORIZONTAL) : new ScrollBar(ScrollBar.HORIZONTAL);
      sbH.setLiveScrolling(true);
      sbH.setMaximum(0);
    }
    if (allowVScrollBar) {
      sbV = Settings.fingerTouch ? new ScrollPosition(ScrollBar.VERTICAL) : new ScrollBar(ScrollBar.VERTICAL);
      sbV.setLiveScrolling(true);
      sbV.setMaximum(0);
    }
  }

  /** Overwrite this method to define a custom height for the scrolling area */
  protected int getBagInitialHeight() {
    return 20000;
  }

  /** Overwrite this method to define a custom width for the scrolling area */
  protected int getBagInitialWidth() {
    return 4000;
  }

  @Override
  public boolean flickStarted() {
    return true;//isScrolling; // flick1.robot fails with this
  }

  @Override
  public void flickEnded(boolean atPenDown) {
    bag.releaseScreenShot();
  }

  @Override
  public boolean canScrollContent(int direction, Object target) {
    if (direction == 4) {
      direction = 4;
    }
    boolean ret = false;
    if (Settings.fingerTouch) {
      switch (direction) {
      case DragEvent.UP:
        ret = sbV != null && sbV.value > sbV.minimum;
        break;
      case DragEvent.DOWN:
        ret = sbV != null && (sbV.value + sbV.visibleItems) < sbV.maximum;
        break;
      case DragEvent.LEFT:
        ret = sbH != null && sbH.value > sbH.minimum;
        break;
      case DragEvent.RIGHT:
        ret = sbH != null && (sbH.value + sbH.visibleItems) < sbH.maximum;
        break;
      }
    }
    return ret;
  }
  
  private void resetTarget() {
     targetX = (sbH == null) ? 0 : sbH.value;
     targetY = (sbV == null) ? 0 : sbV.value;
  }
  
  @Override
  public boolean scrollContent(int dx, int dy, boolean fromFlick) {
     boolean willScroll = true;
     boolean startScroll = false;

     isFromFlick = fromFlick;
     if (isFromFlick) { // In case of flick, scroll instantaneously
        willScroll = internalScrollContent(dx, dy, fromFlick);
        resetTarget();
     } else { // In case of a drag, compute the target position and move to it smoothly

         targetX += dx;
         targetY += dy;
         if (sbH != null) {
        	int w = this.getWidth() - (this.insets.left + this.insets.right) * 2;
            startScroll = true;
        	if (targetX < sbH.getMinimum()) {
               targetX = sbH.getMinimum();
              postEvent(new ScrollEvent(ScrollEvent.SCROLL_ON_LEFT));
               willScroll = false;
            } else if (targetX > sbH.getMaximum() - w) {
               targetX = sbH.getMaximum() - w;
              postEvent(new ScrollEvent(ScrollEvent.SCROLL_ON_RIGHT));
               willScroll = false;
            } else {
        	  if(dx > 0) postEvent(new ScrollEvent(ScrollEvent.SCROLL_RIGHT));
        	  else if(dx < 0) postEvent(new ScrollEvent(ScrollEvent.SCROLL_LEFT));
            }
         } else {
            willScroll = false;
         }
         
         if (sbV != null) {
            startScroll = true;
        	int h = this.getHeight() - (this.insets.top + this.insets.bottom) * 2;
            if (targetY < sbV.getMinimum()) {
               targetY = sbV.getMinimum();
               postEvent(new ScrollEvent(ScrollEvent.SCROLL_ON_TOP));
               willScroll = false;
            } else if (targetY > sbV.getMaximum() - h) {
               targetY = sbV.getMaximum() - h;
               postEvent(new ScrollEvent(ScrollEvent.SCROLL_ON_BOTTOM));
               willScroll = false;
            }
            else {
                if(dy > 0) postEvent(new ScrollEvent(ScrollEvent.SCROLL_DOWN));
                else if(dy < 0) postEvent(new ScrollEvent(ScrollEvent.SCROLL_UP));
            }

         } else {
            willScroll = false;
         }
     }

     if(startScroll && willScroll) postEvent(new ScrollEvent(ScrollEvent.SCROLL_START));

     return willScroll;
  }
  
  @Override
  public void updateListenerTriggered(int elapsedMilliseconds) {
     if (sbH != null && sbH.isPressed()) {
        resetTarget();
     }
     if (sbV != null && sbV.isPressed()) {
        resetTarget();
     }
     
     if(!isFromFlick){
        // Exponential Decay function.
        // Each millisecond, the screen scrolls 1.66% of the way towards the target point
        final double decayRate = 1.0 / 60.0f;
        // Calculates the % of the distance travelled afther elapsedMilliseconds
        // Typically, 16ms have passed, which moves 24% of the distance.
        double decay = 1.0 - Math.exp(-decayRate * elapsedMilliseconds);

       // 0.1 to assure animation is at beginning


        double dx = 0;
        double dy = 0;
        if (sbH != null) {
           dx = (targetX - sbH.value) * decay;
        }
        if (sbV != null) {
           dy = (targetY - sbV.value) * decay;
        }
        
        internalScrollContent((int)dx, (int)dy, isFromFlick);
     }
   }
  
  private boolean internalScrollContent(int dx, int dy, boolean fromFlick) {
    boolean scrolled = false;
    if((sbV != null || sbH != null) && dx == 0 && dy == 0) {
      postEvent(new ScrollEvent(ScrollEvent.SCROLL_END));
    }
    if (dx != 0 && sbH != null) {
      int oldValue = sbH.value;
      sbH.setValue(oldValue + dx);
      lastH = sbH.value;

      if (oldValue != lastH) {
        bagSetRect(LEFT - lastH, KEEP, KEEP, KEEP, false);
        scrolled = true;
        if (!fromFlick) {
          sbH.tempShow();
        }
      }
    }
    if (dy != 0 && sbV != null) {
      int oldValue = sbV.value;
      sbV.setValue(oldValue + dy);
      lastV = sbV.value;

      if (oldValue != lastV) {
        bagSetRect(KEEP, TOP - lastV, KEEP, KEEP, false);
        scrolled = true;
        if (!fromFlick) {
          sbV.tempShow();
        }
      }
    }

    if (scrolled) {
      Window.needsPaint = true;
    }
    return scrolled;
  }

  @Override
  public int getScrollPosition(int direction) {
    return direction == DragEvent.LEFT || direction == DragEvent.RIGHT ? lastH : lastV;
  }

  /** Adds a child control to the bag container. */
  @Override
  public void add(Control control) {
    changed = true;
    if(control.floating)
    	bag0.add(control);
    else
    	bag.add(control);
  }
  
  /** Adds a control to the ScrollContainer itself. Used internally. */
  void addToSC(Control c) {
    bag0.add(c);
  }

  /**
   * Removes a child control from the bag container.
   */
  @Override
  public void remove(Control control) {
    changed = true;
    bag.remove(control);
  }

  @Override
  protected void onBoundsChanged(boolean screenChanged) {
    bag0.setRect(LEFT, TOP, FILL, FILL, null, screenChanged);
    bagSetRect(LEFT, TOP, FILL, FILL, screenChanged);
  }

  @Override
  protected void onColorsChanged(boolean colorsChanged) {
    super.onColorsChanged(colorsChanged);
    if (colorsChanged) {
      bag.setBackForeColors(backColor, foreColor);
      bag0.setBackForeColors(backColor, foreColor);
      if (sbV != null) {
        sbV.setBackForeColors(backColor, foreColor);
      }
      if (sbH != null) {
        sbH.setBackForeColors(backColor, foreColor);
      }
    }
  }

  protected void bagSetRect(int x, int y, int w, int h, boolean screenChanged) {
    boolean old = bag.uiAdjustmentsBasedOnFontHeightIsSupported;
    bag.uiAdjustmentsBasedOnFontHeightIsSupported = false;
    bag.setRect(x, y, w, h, null, screenChanged);
    bag.uiAdjustmentsBasedOnFontHeightIsSupported = old;
    bag.lastMid = -1; // reset sort when bounds change
  }

  /** This method resizes the control to the needed bounds, based on added childs. 
   * Must be called if you're controlling reposition by your own, after you repositioned the controls inside of it. */
  @Override
  public void resize() {
    int maxX = 0;
    int maxY = 0;
    boolean hasFillH = false;
    for (Control child = bag.children; child != null; child = child.next) {
      int m = child.x + child.width;
      if (m > maxX) {
        maxX = m;
      }
      int hh = child.height;
      if (!hasFillH && sbV != null && (FILL - RANGE) <= child.setH && child.setH <= (FILL + RANGE)) // if control has fill on the height, don't take it into consideration
      {
        hasFillH = true;
        hh = 0;
      }
      m = child.y + hh;
      if (m > maxY) {
        maxY = m;
      }
    }
    if (hasFillH) // now resize the height
    {
      maxY = super.getClientRect().height;
      for (Control child = bag.children; child != null; child = child.next) {
        if ((FILL - RANGE) <= child.setH && child.setH <= (FILL + RANGE)) {
          child.height = maxY - child.y
              + (uiAdjustmentsBasedOnFontHeightIsSupported ? (child.setH - FILL) * fmH / 100 : (child.setH - FILL));
          child.onBoundsChanged(true);
        }
      }
    }
    resize(maxX == 0 ? FILL : maxX, maxY == 0 ? PREFERRED : maxY);
  }

  /** This method resizes the control to the needed bounds, based on the given maximum width and heights. */
  public void resize(int maxX, int maxY) {
    bagSetRect(bag.x, bag.y, maxX, maxY, false);
    if (sbV != null) {
      super.remove(sbV);
    }
    if (sbH != null) {
      super.remove(sbH);
    }
    // check if we need horizontal or vertical or both scrollbars
    boolean needX = false, needY = false, changed = false;
    Rect r = super.getClientRect();
    int availX = r.width;
    int availY = r.height;
    boolean finger = ((sbH != null && sbH instanceof ScrollPosition) || (sbV != null && sbV instanceof ScrollPosition));
    if (sbH != null || sbV != null) {
      do {
        changed = false;
        if (!needY && maxY > availY) {
          changed = needY = true;
          if (finger && sbH != null && sbV != null) {
            availX -= sbV.getPreferredWidth();
          }
        }
        if (!needX && maxX > availX) // do we need an horizontal scrollbar?
        {
          changed = needX = true;
          if (finger && sbV != null && sbH != null) {
            availY -= sbH.getPreferredHeight(); // remove the horizbar area from the avail Y area
          }
        }
      } while (changed);
    }

    boolean showScroll = !Settings.fingerTouch || !ScrollPosition.AUTO_HIDE;
    int sbVsize = needY && showScroll && sbV != null && !sbV.transparentBackground ? sbV.getPreferredWidth() : 0;
    int sbHsize = needX && showScroll && sbH != null && !sbH.transparentBackground ? sbH.getPreferredHeight() : 0;
    if (sbH != null || sbV != null || !shrink2size) {
      bag0.setRect(r.x, r.y, r.width - sbVsize, r.height - sbHsize);
    } else {
      bag0.setRect(r.x, r.y, maxX, maxY);
      setRect(this.x, this.y, maxX, maxY);
    }
    if (needX && sbH != null && canShowScrollBars(false)) {
      super.add(sbH);
      sbH.setMaximum(maxX);
      sbH.setVisibleItems(bag0.width);
      sbH.setRect(LEFT, BOTTOM, FILL - sbVsize, PREFERRED);
      sbH.setUnitIncrement(flick != null && flick.scrollDistance > 0 ? flick.scrollDistance : fm.charWidth('@'));
      lastH = 0;
    } else if (sbH != null) {
      sbH.setMaximum(0); // kmeehl@tc100: drag-scrolling depends on this to determine the bounds
    }
    if (needY && sbV != null && canShowScrollBars(true)) {
      super.add(sbV);
      sbV.setMaximum(maxY);
      sbV.setVisibleItems(bag0.height);
      sbV.setRect(RIGHT, TOP, PREFERRED, FILL);
      sbV.setUnitIncrement(flick != null && flick.scrollDistance > 0 ? flick.scrollDistance : fmH + Edit.prefH);
      lastV = 0;
    } else if (sbV != null) {
      sbV.setMaximum(0); // kmeehl@tc100: drag-scrolling depends on this to determine the bounds
    }
    Window.needsPaint = true;
  }

  protected boolean canShowScrollBars(boolean vertical) {
    return true;
  }

  /** Override this method to return the correct scroll distance. Defaults to the container's width. */
  public int getScrollDistance() {
    return this.width;
  }

  @Override
  public void reposition() {
    int vx = bag.x, vy = bag.y; // keep position when changing size
    int curPage = flick != null && flick.pagepos != null ? flick.pagepos.getPosition() : 0;
    super.reposition();
    resize();
    if (flick != null && flick.scrollDistance != 0) {
      flick.setScrollDistance(getScrollDistance());
    }
    if (curPage != 0) {
      scrollToPage(curPage);
    } else {
      if (sbH != null) {
        sbH.setValue(sbH.maximum == 0 ? 0 : -vx);
        bag.x = -sbH.getValue();
      }
      if (sbV != null) {
        sbV.setValue(sbV.maximum == 0 ? 0 : -vy); // if we're scrolled but we don't need scroll, move to origin
        bag.y = -sbV.getValue();
      }
    }
  }

  /**
   * Returns the preferred width AFTER the resize method was called. If the ScrollBars are disabled, returns the
   * maximum size of the container to hold all controls.
   */
  @Override
  public int getPreferredWidth() {
    int horizontalMax = sbH == null ? 0 : sbH.maximum;
    return sbV == null ? bag.width : horizontalMax + (sbV.maximum == 0 ? 0 : sbV.getPreferredWidth());
  }

  /**
   * Returns the preferred height AFTER the resize method was called. If the ScrollBars are disabled, returns the
   * maximum size of the container to hold all controls.
   */
  @Override
  public int getPreferredHeight() {
    int verticalMax = sbV == null ? 0 : sbV.maximum;
    return sbH == null ? bag.height : verticalMax + (sbH.maximum == 0 ? 0 : sbH.getPreferredWidth());
  }

  @Override
  public void onPaint(Graphics g) {
    if (changed) {
      resize();
      changed = false;
    }
    super.onPaint(g);
  }

  @Override
  public void onEvent(Event event) {
    switch (event.type) {
    case ControlEvent.PRESSED:
      if (event.target == sbV && sbV.value != lastV) {
        lastV = sbV.value;
        bagSetRect(bag.x, TOP - lastV, bag.width, bag.height, false);
        targetY = sbV.value;
      } else if (event.target == sbH && sbH.value != lastH) {
        lastH = sbH.value;
        bagSetRect(LEFT - lastH, bag.y, bag.width, bag.height, false);
        targetX = sbH.value;
      }
      break;
    case PenEvent.PEN_DOWN:
      scScrolled = false;
      hasScrolled = false;
      break;
    case PenEvent.PEN_DRAG_START:
      hasScrolled = true;
      if (Settings.optimizeScroll && ((DragEvent) event).direction != 0 && isFirstBag((Control) event.target)
          && bag.offscreen == null && Settings.fingerTouch && bag.width < 4096 && bag.height < 4096) {
        bag.takeScreenShot();
      }
      break;
    case PenEvent.PEN_DRAG_END:
      if (flick != null && Flick.currentFlick == null && bag.offscreen != null) {
        bag.releaseScreenShot();
      }
      hasScrolled = true;
      break;
    case PenEvent.PEN_DRAG:
      hasScrolled = true;
      if (event.target == sbV || event.target == sbH) {
        break;
      }
      if (Settings.fingerTouch) {
        Window w = getParentWindow();
        if (w != null && w._focus == w.focusOnPenUp) {
          break;
        }

        DragEvent de = (DragEvent) event;
        int dx = -de.xDelta;
        int dy = -de.yDelta;
        if (isScrolling) {
          scrollContent(dx, dy, false);
          event.consumed = true;
          //Event.clearQueue(PenEvent.PEN_DRAG);
        } else {
          int direction = DragEvent.getInverseDirection(de.direction);
          if (flick != null && !flick.isValidDirection(direction)) {
            break;
          }
          if (canScrollContent(direction, de.target) && scrollContent(dx, dy, true)) {
            event.consumed = isScrolling = scScrolled = true;
          }
        }
      }
      break;
    case PenEvent.PEN_UP:
      isScrolling = false;
      if (autoScroll && event.target instanceof Control && event.target != lastScrolled
          && !hsIgnoreAutoScroll.contains(event.target.getClass()) && ((Control) event.target).isChildOf(this)
          && !((Control) event.target).hadParentScrolled()) {
        Control c = (Control) event.target;
        lastScrolled = c;
        Rect r = c.getAbsoluteRect();
        boolean scrolled = false;
        if (sbV != null) {
          int k = this.height / 3;
          r.y -= this.getAbsoluteRect().y;
          if (r.y > 2 * k) {
            scrolled = scrollContent(0, k, false);
          } else if (r.y2() < k) {
            scrolled = scrollContent(0, -k, false);
          }
        }
        if (sbH != null && !scrolled) {
          int k = this.width / 3;
          r.x -= this.getAbsoluteRect().x;
          if (r.x > 2 * k) {
            scrollContent(k, 0, false);
          } else if (r.x2() < k) {
            scrollContent(-k, 0, false);
          }
        }
      }
      break;
    case ControlEvent.HIGHLIGHT_IN:
      if (event.target != this) {
        scrollToControl((Control) event.target);
      }
      break;
    }
    // Scroll Events
    if(event instanceof ScrollEvent) {
      event.consumed = true;
      for (ScrollEventHandler handler:
           scrollEventHandlerList) {
          handler.onScrollEvent((ScrollEvent) event);
      }
    }
  }

  private boolean isFirstBag(Control c) {
    for (; c != null; c = c.parent) {
      if (c instanceof ClippedContainer) {
        return c == bag;
      }
    }
    return false;
  }

  /** Scrolls to the given page, which is the flick's scrollDistance (if set), or the control's height.
   * @since TotalCross 1.53
   */
  public void scrollToPage(int p) {
    int pageH = flick != null && flick.scrollDistance != 0 ? flick.scrollDistance : this.height;
    int val = (p - 1) * pageH;
    if (sbH != null) {
      lastH = sbH.value;
      sbH.setValue(val);
      if (lastH != sbH.value) {
        lastH = sbH.value;
        bagSetRect(LEFT - lastH, bag.y, bag.width, bag.height, false);
      }
    } else {
      lastV = sbV.value;
      sbV.setValue(val);
      if (lastV != sbV.value) {
        lastV = sbV.value;
        bagSetRect(bag.x, TOP - lastV, bag.width, bag.height, false);
      }
    }
    if (flick != null && flick.pagepos != null) {
      flick.pagepos.setPosition(p);
    }
    resetTarget();
  }

  /** Scrolls a page to left or right. Works only if it has a flick and a page position.
   */
  public void scrollPage(boolean left) {
    int curPage = flick != null && flick.pagepos != null ? flick.pagepos.getPosition() : 0;
    scrollToPage(left ? curPage - 1 : curPage + 1);
  }

  /** Scrolls to the given control. */
  public void scrollToControl(Control c) // kmeehl@tc100
  {
    if (c != null && (sbH != null || sbV != null)) {
      Rect r = c.getRect();
      Control f = c.parent;
      while (f.parent != this) {
        r.x += f.x;
        r.y += f.y;
        f = f.parent;
        if (f == null) {
          return;// either c is not in this container, or it has since been removed from the UI
        }
      }

      // horizontal
      if (sbH != null && (r.x < 0 || r.x2() > bag0.width)) {
        lastH = sbH.value;
        int val = lastH + (r.x <= 0 || r.width > bag0.width ? r.x : (r.x2() - bag0.width));
        setHValue(val);
      }
      // vertical
      if (sbV != null && (r.y < 0 || r.y2() > bag0.height)) {
        lastV = sbV.value;
        int val = lastV + (r.y <= 0 || r.height > bag0.height ? r.y : (r.y2() - bag0.height));
        setVValue(val);
      }
    }
    resetTarget();
  }

  /** Scroll the container to origin position 0,0 */
  public void scrollToOrigin() {
    if (sbH != null) {
      setHValue(0);
    }
    if (sbV != null) {
      setVValue(0);
    }
    bag.reposition();
    resetTarget();
  }

  /** Sets the vertical's ScrollBar value to the given one. */
  protected void setVValue(int val) {
    if (sbV != null) {
      sbV.setValue(val);
      if (val < sbV.minimum) {
        val = sbV.minimum;
      }
      if (lastV != sbV.value) {
        lastV = sbV.value;
        bagSetRect(bag.x, TOP - lastV, bag.width, bag.height, false);
      }
    }
  }

  /** Sets the horizontal's ScrollBar value to the given one. */
  protected void setHValue(int val) {
    if (sbH != null) {
      if (val < sbH.minimum) {
        val = sbH.minimum;
      }
      sbH.setValue(val);
      if (lastH != sbH.value) {
        lastH = sbH.value;
        bagSetRect(LEFT - lastH, bag.y, bag.width, bag.height, false);
      }
    }
  }

  @Override
  public void setBorderStyle(byte border) {
    if (shrink2size) {
      bag.setBorderStyle(border);
    } else {
      super.setBorderStyle(border);
    }
  }

  @Override
  public Flick getFlick() {
    return flick;
  }

  @Override
  public boolean wasScrolled() {
    return hasScrolled;
  }

  /**
   * Removes all controls from the ScrollContainer.
   */
  @Override
  public void removeAll() {
    bag.removeAll();
  }

  /** Returns the children of the bag. If you call ScrollContainer.getChildren, it will not return
   * the controls added to the ScrollContainer, since they are actually added to the bag.
   * @since TotalCross 1.5
   */
  public Control[] getBagChildren() {
    return bag.getChildren();
  }

  @Override
  public void onFontChanged() {
    bag.setFont(font);
  }

  @Override
  public Control moveFocusToNextControl(Control control, boolean forward) // guich@tc125_26
  {
    return bag.moveFocusToNextControl(control, forward);
  }
  
  private int getSafeSbhValue() {
     return (sbH != null) ? sbH.value : 0;
  }
  private int getSafeSbvValue() {
     return (sbV != null) ? sbV.value : 0;
  }
  
  /** Disables the 'flick' behaviour from this scroll container. This is useful when you have
   * small scrollable containers and are facing usability problems. We recommend leaving the
   * flick behavior on for large scrollable surfaces, with many items. 
   * All scroll containers have the flick behaviour on by default. If disabled, this behavior
   * can be reenabled using the 'enableFlick' method.
   * @see enableFlick
   */
  public void disableFlick() {
	  if (this.disabledFlick == null) {
		  this.flick.removeEventSource(this);
		  this.disabledFlick = this.flick;
		  this.flick = null;
	  }
  }
  /** Reenables the 'flick' behaviour of this container. @see disableFlick */
  public void enableFlick() {
	  if (this.flick == null) {
		  this.flick = disabledFlick;
		  this.disabledFlick = null;
		  this.flick.addEventSource(this);
	  }
  }
  /** Changes the visibility of the temporary scrollbar handlers. */
  public void setHandlersVisibility(boolean visible) {
	  setHandlersVisibility(visible, visible);
  }
  /** Changes the visibility of the temporary scrollbar handlers individually. */
  public void setHandlersVisibility(boolean horizontalHandlerVisiblle, boolean verticalHandlerVisible) {
	  if (sbH != null && sbH instanceof ScrollPosition) {
		  ((ScrollPosition)sbH).setHandleVisibility(horizontalHandlerVisiblle);
	  }
	  
	  if (sbV != null && sbV instanceof ScrollPosition) {
		  ((ScrollPosition)sbV).setHandleVisibility(verticalHandlerVisible);
	  }
  }

  /**
   * Add an instance of ScrollEventHandler
   * @param scrollEventHandler
   */
  public void addScrollEventHandler(ScrollEventHandler scrollEventHandler) {
    scrollEventHandlerList.add(scrollEventHandler);
  }

  /**
   * Remove an specified instance of ScrollEventHandler
   * @param scrollEventHandler
   */
  public void removeScrollEventHandler(ScrollEventHandler scrollEventHandler) {
    scrollEventHandlerList.remove(scrollEventHandler);
  }
}
