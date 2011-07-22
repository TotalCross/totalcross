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
   private Image npback;
   /** Set to false to make the PositionBar always show (instead of the default auto-hide behaviour). */
   public static boolean AUTO_HIDE = true;
   
   protected boolean autoHide = AUTO_HIDE;
   
   /** The bar color. Defaults to UIColors.positionbarColor but can be changed to something else. */
   public int barColor = UIColors.positionbarColor;
   
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
   
   /** Don't allow change the visibility flag. This is done automatically. */
   public void setVisible(boolean b)
   {
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
            if (npback == null)
               try
               {
                  if (verticalBar)
                     npback = NinePatch.getNormalInstance(NinePatch.SCROLLPOSV,width,dragBarSize,barColor,true);
                  else
                     npback = NinePatch.getNormalInstance(NinePatch.SCROLLPOSH,dragBarSize,height,barColor,true);
               }
               catch (Exception e) {e.printStackTrace();}
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
      if (autoHide && verticalBar == verticalScroll)
         super.setVisible(true);
      return true;
   }

   public void flickEnded()
   {
      if (autoHide)
         super.setVisible(false);
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
   }

   public void penUp(PenEvent e)
   {
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
