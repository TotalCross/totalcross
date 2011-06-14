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

/**
 * ScrollPosition implements the auto-hide scrollbar that exists in 
 * finger-touched devices. This special scrollbar is just a small position indicator
 * that appears when the area is dragged. The ScrollPosition does not take 
 * an area of the control, since it appears and disappears automatically.
 * <br><br>
 * All Scrollable controls change their ScrollBar by the ScrollPosition when
 * Settings.fingerTouch is true.
 * @see totalcross.sys.Settings#fingerTouch
 * @see totalcross.ui.Scrollable
 * @see totalcross.ui.UIColors#positionbarColor
 * @see totalcross.ui.UIColors#positionbarBackgroundColor
 */

public class ScrollPosition extends ScrollBar implements Scrollable, PenListener
{
   private boolean verticalScroll,isFlicking;
   /** Set to false to make the PositionBar always show (instead of the default auto-hide behaviour). */
   public static boolean AUTO_HIDE = true;
   
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
      visible = !AUTO_HIDE;
   }
   
   public void onBoundsChanged(boolean b)
   {
      super.onBoundsChanged(b);
      if (parent instanceof Scrollable)
      {
         Flick f = ((Scrollable)parent).getFlick();
         if (f != null)
            f.addScrollableListener(this);
         parent.addPenListener(this);
      }
   }
   
   /** Don't allow change the visibility flag. This is done automatically. */
   public void setVisible(boolean b)
   {
   }
   
   public void onPaint(Graphics g)
   {
      if (UIColors.positionbarBackgroundColor != -1) 
      {
         g.backColor = UIColors.positionbarBackgroundColor; 
         g.fillRect(0,0,width,height);
      }
      if (enabled || !AUTO_HIDE)
      {
         g.backColor = barColor;
         if (verticalBar)
            g.fillRect(0,dragBarPos,width,dragBarSize);
         else
            g.fillRect(dragBarPos,0,dragBarSize,height);
      }
   }
   
   public int getPreferredWidth()
   {
      return verticalBar ? fmH/4 : fmH;
   }

   public int getPreferredHeight()
   {
      return !verticalBar ? fmH/4 : fmH;
   }
   
   public boolean flickStarted()
   {
      isFlicking = true;
      if (AUTO_HIDE && verticalBar == verticalScroll)
         super.setVisible(true);
      return true;
   }

   public void flickEnded(boolean aborted)
   {
      if (AUTO_HIDE)
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
      if (AUTO_HIDE && !visible && verticalBar == verticalScroll)
         super.setVisible(true);
   }

   public void penDragStart(DragEvent e)
   {
      isFlicking = false;
   }

   public void penDragEnd(DragEvent e)
   {
      if (AUTO_HIDE && visible && !isFlicking)
         super.setVisible(false);
   }
}
