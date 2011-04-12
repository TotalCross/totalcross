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
 * finger-touched devices. This special scrollbar is just a small indicator
 * that appears when the area is dragged. The ScrollPosition does not take 
 * an area of the control, since it appears and disappears automatically.
 * <br><br>
 * All Scrollable controls change their ScrollBar by the ScrollPosition when
 * Settings.fingerTouch is true.
 * @see totalcross.sys.Settings#fingerTouch
 * @see totalcross.ui.Scrollable
 */

public class ScrollPosition extends ScrollBar implements Scrollable, PenListener
{
   private boolean verticalScroll,isFlicking;
   public boolean autoHide;
   
   public ScrollPosition(boolean autoHide)
   {
      this(VERTICAL,autoHide);
   }

   public ScrollPosition(byte orientation, boolean autoHide)
   {
      super(orientation);
      this.autoHide = autoHide;
      btnInc.setVisible(false);
      btnDec.setVisible(false);
      visible = !autoHide;
   }
   
   public void onBoundsChanged(boolean b)
   {
      super.onBoundsChanged(b);
      if (parent instanceof Scrollable)
      {
         ((Scrollable)parent).getFlick().addScrollable(this);
         parent.addPenListener(this);
      }
   }
   
   public void onPaint(Graphics g)
   {
      g.backColor = backColor;
      if (!transparentBackground)
         g.fillRect(0,0,width,height);
      g.backColor = sbColor;
      if (verticalBar)
         g.fillRect(0,dragBarPos,width,dragBarSize);
      else
         g.fillRect(dragBarPos,0,dragBarSize,height);
   }
   
   public int getPreferredWidth()
   {
      return verticalBar ? fmH/4 : fmH;
   }

   public int getPreferredHeight()
   {
      return !verticalBar ? fmH/4 : fmH;
   }
   
   public void flickStarted()
   {
      isFlicking = true;
      if (autoHide && verticalBar == verticalScroll)
         setVisible(true);
   }

   public void flickEnded(boolean aborted)
   {
      if (autoHide)
         setVisible(false);
   }

   // none of these methods are called
   public boolean canScrollContent(int direction, Object target)
   {
      return false;
   }

   public boolean isScrolling()
   {
      return false;
   }

   public boolean scrollContent(int xDelta, int yDelta)
   {
      return false;
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
         setVisible(true);
   }

   public void penDragStart(DragEvent e)
   {
      isFlicking = false;
   }

   public void penDragEnd(DragEvent e)
   {
      if (autoHide && visible && !isFlicking)
         setVisible(false);
   }
}
