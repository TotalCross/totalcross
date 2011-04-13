/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;

/**
 * ScrollContainer is a container with a horizontal only, vertical only, both or no
 * ScrollBars, depending on the control positions.
 * The default unit scroll is an Edit's height (for the vertical
 * scrollbar), and the width of '@' (for the horizontal scrollbar).
 * <p>
 * <b>Caution</b>: you must not use RIGHT, BOTTOM, CENTER and FILL when setting the control bounds,
 * unless you disable the corresponding ScrollBar!
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

public class ScrollContainer extends Container implements Scrollable
{
   /** Returns the scrollbar for this ScrollContainer. With it, you can directly
    * set its parameters, like blockIncrement, unitIncrement and liveScrolling.
    * But be careful, don't mess with the minimum, maximum and visibleItems.
    */
   public ScrollBar sbH,sbV;

   /** The Flick object listens and performs flick animations on PenUp events when appropriate. */
   protected Flick flick;

   protected ClippedContainer bag;
   protected Container bag0; // used to make sure that the clipping will work
   private boolean changed;
   private int lastV=-10000000, lastH=-10000000; // eliminate duplicate events
   /** Set to true, to make the surrounding container shrink to its size. */
   public boolean shrink2size;
   private boolean isScrolling;

   /* A container that checks if the sibling is within the visible area before calling paint on it. */
   protected static class ClippedContainer extends Container // please keep it protected
   {
      public void paintChildren()
      {
         int y0 = -this.y;
         int yf = y0 + parent.height;
         int x0 = -this.x;
         int xf = x0 + parent.width;
         for (Control child = children; child != null; child = child.next)
            if (child.isVisibleAndInside(x0,y0,xf,yf))
            {
               child.onPaint(child.getGraphics());
               if (child.asContainer != null)
                  child.asContainer.paintChildren();
            }
      }
   }

   /** Standard constructor for a new ScrollContainer, with both scrollbars enabled.
     */
   public ScrollContainer()
   {
      this(true);
   }
   
   /** Constructor used to specify when both scrollbars are enabled or not. */
   public ScrollContainer(boolean allowScrollBars)
   {
      this(allowScrollBars, allowScrollBars);
   }
   
   /** Constructor used to specify when each scrollbar is enabled or not.
    * By disabling the horizontal scrollbar, you can use RIGHT and CENTER on the x parameter of a control that is added.
    * By disabling the vertical scrollbar, you can use BOTTOM and CENTER on the y parameter of a control that is added.
    * @since TotalCross 1.27 
    */
   public ScrollContainer(boolean allowHScrollBar, boolean allowVScrollBar)
   {
      super.add(bag0 = new Container());
      bag0.add(bag = new ClippedContainer());
      bag.ignoreOnAddAgain = bag.ignoreOnRemove = true;
      bag0.ignoreOnAddAgain = bag0.ignoreOnRemove = true;
      bag.setRect(0,0,4000,20000); // set an arbitrary size
      if (allowHScrollBar)
      {
         if (!Settings.fingerTouch)
            sbH = new ScrollBar(ScrollBar.HORIZONTAL);
         else
            sbH = new ScrollPosition(ScrollBar.HORIZONTAL);
         sbH.setLiveScrolling(true);
         sbH.setMaximum(0);
      }
      if (allowVScrollBar)
      {
         if (!Settings.fingerTouch)
            sbV = new ScrollBar(ScrollBar.VERTICAL);
         else
            sbV = new ScrollPosition(ScrollBar.VERTICAL);
         sbV.setLiveScrolling(true);
         sbV.setMaximum(0);
      }
      //flick = new Flick(this);
   }
   
   public void flickStarted()
   {
   }
   
   public void flickEnded(boolean aborted)
   {
   }
   
   public boolean canScrollContent(int direction, Object target)
   {
      if (direction == DragEvent.UP)
         return Settings.fingerTouch && sbV != null && sbV.getValue() > sbV.getMinimum();
      else if (direction == DragEvent.DOWN)
         return Settings.fingerTouch && sbV != null && (sbV.getValue() + sbV.getVisibleItems()) < sbV.getMaximum();
      else if (direction == DragEvent.LEFT)
         return Settings.fingerTouch && sbH != null && sbH.getValue() > sbH.getMinimum();
      else if (direction == DragEvent.RIGHT)
         return Settings.fingerTouch && sbH != null && (sbH.getValue() + sbH.getVisibleItems()) < sbH.getMaximum();
      
      return false;
   }
   
   public boolean scrollContent(int dx, int dy)
   {
      boolean scrolled = false;

      if (dx != 0 && sbH != null)
      {
         int oldValue = sbH.getValue();
         sbH.setValue(oldValue + dx);
         lastH = sbH.getValue();

         if (oldValue != lastH)
         {
            bag.setRect(LEFT - lastH, bag.y, bag.width, bag.height);
            scrolled = true;
         }
      }
      if (dy != 0 && sbV != null)
      {
         int oldValue = sbV.getValue();
         sbV.setValue(oldValue + dy);
         lastV = sbV.getValue();

         if (oldValue != lastV)
         {
            bag.setRect(bag.x, TOP - lastV, bag.width, bag.height);
            scrolled = true;
         }
      }

      if (scrolled)
      {
         Window.needsPaint = true;
         return true;
      }
      else
         return false;
   }

   /** Adds a child control to the bag container. */
   public void add(Control control)
   {
      changed = true;
      bag.add(control);
   }

   /**
   * Removes a child control from the bag container.
   */
   public void remove(Control control)
   {
      changed = true;
      bag.remove(control);
   }

   protected void onBoundsChanged(boolean screenChanged)
   {
      bag0.setRect(LEFT, TOP, FILL, FILL, null, screenChanged);
      if (sbH == null && sbV == null && shrink2size)
         bag.setRect(LEFT, TOP, FILL, FILL, null, screenChanged);
      else if (sbH == null || sbV == null)
      {
         int w = sbH != null ? 4000 : FILL - (sbV != null ? sbV.getPreferredWidth() : 0);
         int h = sbV != null ? 20000 : FILL - (sbH != null ? sbH.getPreferredHeight() : 0);
         bag.setRect(LEFT, TOP, w, h, null, screenChanged);
      }
   }

   protected void onColorsChanged(boolean colorsChanged)
   {
      super.onColorsChanged(colorsChanged);
      if (colorsChanged)
      {
         bag.setBackForeColors(backColor, foreColor);
         bag0.setBackForeColors(backColor, foreColor);
         if (sbV != null)
            sbV.setBackForeColors(backColor, foreColor);
         if (sbH != null)
            sbH.setBackForeColors(backColor,foreColor);
      }
   }

   /** This method resizes the control to the needed bounds, based on added childs. 
    * Must be called if you're controlling reposition by your own, after you repositioned the controls inside of it. */
   public void resize()
   {
      int maxX = 0;
      int maxY = 0;
      for (Control child = bag.children; child != null; child = child.next)
      {
         maxX = Math.max(maxX,child.x+child.width);
         maxY = Math.max(maxY,child.y+child.height);
      }
      resize(maxX, maxY);
   }

   /** This method resizes the control to the needed bounds, based on the given maximum width and heights. */
   public void resize(int maxX, int maxY)
   {
      bag.setRect(bag.x, bag.y, maxX, maxY);
      if (sbV != null)
         super.remove(sbV);
      if (sbH != null)
         super.remove(sbH);
      // check if we need horizontal or vertical or both scrollbars
      boolean needX = false, needY = false, changed=false;
      Rect r = getClientRect();
      int availX = r.width;
      int availY = r.height;
      boolean finger = ScrollPosition.AUTO_HIDE && 
                       ((sbH != null && sbH instanceof ScrollPosition) ||
                        (sbV != null && sbV instanceof ScrollPosition));
      if (sbH != null || sbV != null)
         do
         {
            changed = false;
            if (!needY && maxY > availY)
            {
               changed = needY = true;
               if (finger && sbH != null && sbV != null) availX -= sbV.getPreferredWidth();
            }
            if (!needX && maxX > availX) // do we need an horizontal scrollbar?
            {
               changed = needX = true;
               if (finger && sbV != null && sbH != null) availY -= sbH.getPreferredHeight(); // remove the horizbar area from the avail Y area
            }
         } while (changed);

      if (sbH != null || sbV != null || !shrink2size)
         bag0.setRect(r.x,r.y,r.width-(!finger && needY && sbV != null ? sbV.getPreferredWidth() : 0), r.height-(!finger && needX && sbH != null ? sbH.getPreferredHeight() : 0));
      else
      {
         bag0.setRect(r.x,r.y,maxX,maxY);
         setRect(this.x,this.y,maxX,maxY);
      }
      if (needX && sbH != null)
      {
         super.add(sbH);
         sbH.setMaximum(maxX);
         sbH.setVisibleItems(bag0.width);
         sbH.setRect(LEFT,BOTTOM,FILL-(!finger && needY?sbV.getPreferredWidth():0),PREFERRED);
         sbH.setUnitIncrement(fm.charWidth('@'));
         lastH = -10000000;
      }
      else if (sbH != null) sbH.setMaximum(0); // kmeehl@tc100: drag-scrolling depends on this to determine the bounds
      if (needY && sbV != null)
      {
         super.add(sbV);
         sbV.setMaximum(maxY);
         sbV.setVisibleItems(bag0.height);
         sbV.setRect(RIGHT,TOP,PREFERRED,FILL);
         sbV.setUnitIncrement(fmH+Edit.prefH);
         lastV = -10000000;
      }
      else if (sbV != null) sbV.setMaximum(0); // kmeehl@tc100: drag-scrolling depends on this to determine the bounds
      Window.needsPaint = true;
   }

   public void reposition()
   {
      super.reposition();
      resize();
      if (sbH != null) 
         sbH.setValue(0);
      if (sbV != null) 
         sbV.setValue(0);
      bag.x = bag.y = 0;
   }
   
   /** Returns the preferred width AFTER the resize method was called.
    * If the ScrollBars are disabled, returns the maximum size of the container to hold all controls.
    */
   public int getPreferredWidth()
   {
      return sbV == null ? bag.width : sbH.getMaximum() + (sbV.getMaximum() == 0 ? 0 : sbV.getPreferredWidth());
   }

   /** Returns the preferred height AFTER the resize method was called. 
   * If the ScrollBars are disabled, returns the maximum size of the container to hold all controls.
   */
   public int getPreferredHeight()
   {
      return sbH == null ? bag.height : sbV.getMaximum() + (sbH.getMaximum() == 0 ? 0 : sbH.getPreferredWidth());
   }

   public void onPaint(Graphics g)
   {
      if (changed)
      {
         changed = false;
         resize();
      }
      super.onPaint(g);
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == sbV && sbV.getValue() != lastV)
            {
               lastV = sbV.getValue();
               bag.setRect(bag.x,TOP-lastV,bag.width,bag.height);
            }
            else
            if (event.target == sbH && sbH.getValue() != lastH)
            {
               lastH = sbH.getValue();
               bag.setRect(LEFT-lastH,bag.y,bag.width,bag.height);
            }
            break;
         case PenEvent.PEN_DRAG:
            if (event.target == sbV || event.target == sbH) break;
            if (Settings.fingerTouch)
            {
               DragEvent de = (DragEvent)event;
               int dx = -de.xDelt;
               int dy = -de.yDelt;
               
               if (isScrolling)
               {
                  scrollContent(dx, dy);
                  event.consumed = true;
               }
               else
               {
                  int direction = DragEvent.getInverseDirection(de.direction);
                  if (canScrollContent(direction, de.target) && scrollContent(dx, dy))
                     event.consumed = isScrolling = true;
               }
            }
            break;
         case PenEvent.PEN_UP:
            isScrolling = false;
            break;
         case TimerEvent.TRIGGERED:
             break;
         case ControlEvent.HIGHLIGHT_IN:
            if (event.target != this)
               scrollToControl((Control)event.target);
            break;
      }
   }

   /** Scrolls to the given control. */
   public void scrollToControl(Control c) // kmeehl@tc100
   {
      if (c != null && (sbH != null || sbV != null))
      {
         Rect r = c.getRect();
         Control f = c.parent;
         while (f.parent != this)
         {
            r.x += f.x;
            r.y += f.y;
            f = f.parent;
            if (f == null)
               return;// either c is not in this container, or it has since been removed from the UI
         }

         // horizontal
         if (r.x < 0 || r.x2() > bag0.width)
         {
            lastH = sbH.getValue();
            int val = lastH + (r.x <= 0 || r.width > bag0.width ? r.x : (r.x2()-bag0.width));
            if (val < sbH.minimum)
               val = sbH.minimum;
            sbH.setValue(val);
            if (lastH != sbH.getValue())
            {
               lastH = sbH.getValue();
               bag.setRect(LEFT-lastH,bag.y,bag.width,bag.height);
            }
         }
         // vertical
         if (r.y < 0 || r.y2() > bag0.height)
         {
            lastV = sbV.getValue();
            int val = lastV + (r.y <= 0 || r.height > bag0.height ? r.y : (r.y2() - bag0.height));
            if (val < sbV.minimum)
               val = sbV.minimum;
            sbV.setValue(val);
            if (lastV != sbV.getValue())
            {
               lastV = sbV.getValue();
               bag.setRect(bag.x,TOP-lastV,bag.width,bag.height);
            }
         }
      }
   }
   
   public void setBorderStyle(byte border)
   {
      if (shrink2size)
         bag.setBorderStyle(border);
      else
         super.setBorderStyle(border);
   }
   
   public Flick getFlick()
   {
      return flick;
   }
}