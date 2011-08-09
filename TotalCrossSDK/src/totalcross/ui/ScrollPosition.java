/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001-2011 SuperWaba Ltda.                                      *
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

import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

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

public class ScrollPosition extends ScrollBar implements Scrollable, PenListener
{
   private boolean verticalScroll,isFlicking;
   private Image npback,handle;
   private int minBarSizeOld;
   /** Set to false to make the PositionBar always show (instead of the default auto-hide behaviour). */
   public static boolean AUTO_HIDE = true;
   
   protected boolean autoHide = AUTO_HIDE;
   
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
   public ScrollPosition()
   {
      this(VERTICAL);
   }

   /** Constructs a ScrollPosition with the given orientation.
    * @see #VERTICAL
    * @see #HORIZONTAL
    */
   public ScrollPosition(byte orientation)
   {
      super(orientation);
      btnInc.setVisible(false);
      btnDec.setVisible(false);
      visible = !autoHide;
   }

   public void onBoundsChanged(boolean b)
   {
      super.onBoundsChanged(b);
      npback = null;
      if (parent instanceof Scrollable)
      {
         Flick f = ((Scrollable)parent).getFlick();
         if (f != null)
            f.addScrollableListener(this);
         parent.addPenListener(this);
      }
   }
   
   public void onColorsChanged(boolean b)
   {
      super.onColorsChanged(b);
      npback = null;
   }
   
   /** Don't allow change the visibility flbjsag. This is done automatically. */
   public void setVisible(boolean b)
   {
   }
      
   private Image getHandleImage()
   {
      Image img = null;
      try
      {
         img = Resources.progressHandle.getSmoothScaledInstance(fmH*2,dragBarSize,-1);
         img.applyColor(barColor);
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE)
            e.printStackTrace();
      }
      return img;
   }
   
   public void onPaint(Graphics g)
   {
      if (barColor == backColor)
         return;
    
      if (UIColors.positionbarBackgroundColor != -1) 
      {
         g.backColor = UIColors.positionbarBackgroundColor; 
         g.fillRect(0,0,width,height);
      }
      if (enabled || !autoHide)
      {
         g.backColor = barColor;
         if (uiAndroid)
         {
            // change to a handle instead of the position bar?
            if (verticalBar)
            {
               if ((isFlicking || startDragPos != -1) && (maximum-minimum) >= heightMultiplierToShowHandle*height)
               {
                  if (handle == null || handle.getHeight() != dragBarSize)
                     handle = getHandleImage();
                  if (handle != null)
                  {
                     int w = handle.getWidth();
                     if (this.width != w)
                     {
                        minBarSizeOld = minDragBarSize;
                        minDragBarSize = fmH*3;
                        setRect(RIGHT,KEEP,w,KEEP); // parameters will be recomputed
                     }
                  }
               }
               else
               if (resetHandle())
                  return;
            }
            if (npback == null || ((verticalBar ? npback.getHeight() : npback.getWidth()) != dragBarSize))
               try
               {
                  if (verticalBar)
                     npback = NinePatch.getNormalInstance(NinePatch.SCROLLPOSV,width,dragBarSize,barColor,true);
                  else
                     npback = NinePatch.getNormalInstance(NinePatch.SCROLLPOSH,dragBarSize,height,barColor,true);
               }
               catch (Exception e) {e.printStackTrace();}
            
            if (isHandle())
            {
               getGraphics().drawImage(handle, 0, dragBarPos); // when the button is pressed, the graphic's clip becomes invalid
            }
            else
            if (npback != null)
               g.drawImage(npback,verticalBar ? 0 : dragBarPos, verticalBar ? dragBarPos : 0);
         }
         else
         {
            if (verticalBar)
               g.fillRect(0,dragBarPos,width,dragBarSize);
            else
               g.fillRect(dragBarPos,0,dragBarSize,height);
         }
      }
   }

   private boolean isHandle()
   {
      return handle != null && this.width == handle.getWidth();
   }
   
   private boolean resetHandle()
   {
      int w = getPreferredWidth();
      if (this.width != w)
      {
         minDragBarSize = minBarSizeOld;
         setRect(RIGHT,KEEP,w,KEEP);
         Window.needsPaint = true;
         if (autoHide && visible)
         {
            super.setVisible(false);
            getParentWindow().repaintNow();
         }
         return true;
      }
      return false;
   }

   public void onEvent(Event e)
   {
      super.onEvent(e);
      if (e.type == PenEvent.PEN_UP)
      {
         isFlicking = false;
         resetHandle();
      }
   }
   
   public int getPreferredWidth()
   {
      return verticalBar ? uiAndroid ? Math.max(7,fmH/4) : fmH/4 : fmH;
   }

   public int getPreferredHeight()
   {
      return !verticalBar ? uiAndroid ? Math.max(7,fmH/4) : fmH/4 : fmH;
   }
   
   public boolean flickStarted()
   {
      isFlicking = true;
      if (!visible && autoHide && verticalBar == verticalScroll)
         super.setVisible(true);
      
      return true;
   }

   public void flickEnded(boolean atPenDown)
   {
      if (!atPenDown && visible && autoHide && verticalBar == verticalScroll)
         super.setVisible(false);
      else
      if (!autoHide && isHandle())
      {
         isFlicking = false;
         resetHandle();
         Window.needsPaint = true;
      }
   }

   // none of these methods are called
   public boolean canScrollContent(int direction, Object target)
   {
      return false;
   }

   public boolean scrollContent(int xDelta, int yDelta)
   {
      return false;
   }
   
   public int getScrollPosition(int direction)
   {
      return 0;
   }

   public Flick getFlick()
   {
      return null;
   }

   public void penDown(PenEvent e)
   {
      getParentWindow().cancelPenUpListener = this;
   }

   public void penUp(PenEvent e)
   {
      if (!isFlicking || e == null)
         resetHandle();
      getParentWindow().cancelPenUpListener = null;
   }

   public void penDrag(DragEvent e)
   {
      verticalScroll = e.direction == DragEvent.DOWN || e.direction == DragEvent.UP;
      if (autoHide && !visible && verticalBar == verticalScroll)
         super.setVisible(true);
   }

   public void penDragStart(DragEvent e)
   {
      isFlicking = false;
   }

   public void penDragEnd(DragEvent e)
   {
      if (autoHide && visible && !isFlicking)
         super.setVisible(false);
   }
}
