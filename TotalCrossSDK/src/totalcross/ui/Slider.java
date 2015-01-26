/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2001 Daniel Tauchke                                            *
 *  Copyright (C) 2001-2012 SuperWaba Ltda.                                      *
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
import totalcross.ui.gfx.*;

/** Slider is a simple slider.
 * You can set some properties of the slider, like drawTicks, invertDirection and drawFilledArea;
 * read the javadocs of each for more information.
 * You can change the thumb size by setting the <code>minDragBarSize</code> public field and then
 * call setValues or setMaximum or setMinimum method (value must always be ODD!).
 *
 * @since TotalCross 1.0
 */

public class Slider extends ScrollBar
{
   private int[] barX, barY;

   /** Inverts the direction of the marker. You must set this property before calling setValues (or the other min/max/value/visibleItems set methods) for the first time. */
   public boolean invertDirection;
   /** Set to true to draw the ticks. Should be set only when maximum-minimum is much smaller than width. You must set this property before calling setValues (or the other min/max/value/visibleItems set methods) for the first time. */
   public boolean drawTicks;
   /** Set to false to don't draw the filled area. You must set this property before calling setValues (or the other min/max/value/visibleItems set methods) for the first time. */
   public boolean drawFilledArea = true;
   /** The color of the slider */
   public int sliderColor = -1;

   /** Constructs a HORIZONTAL Slider. */
   public Slider()
   {
      this(HORIZONTAL);
   }

   /** Constructs a Slider with the given orientation.
    * @see ScrollBar#VERTICAL
    * @see ScrollBar#HORIZONTAL
    */
   public Slider(byte orientation)
   {
      super(orientation);
      barX = new int[5];
      barY = new int[5];
      btnInc.setVisible(false);
      btnDec.setVisible(false);
   }

   protected void recomputeParams(boolean justValue)
   {
      if (size <= 0) return;
      if (!justValue)
      {
         visibleItems = 1;
         dragBarMin = 0;
         // Calculate and draw the slider button
         int delta = Math.max(visibleItems, maximum-minimum-1);
         dragBarSize = minDragBarSize;
         dragBarMax = size-dragBarSize;
         valuesPerPixel = (double)(size-dragBarSize) / (double)delta;
         blockIncrement = visibleItems;
         recomputeThumb();
      }
      dragBarPos = getDragBarPos(value);
   }

   /** Returns the drag bar position. */
   public int getDragBarPos(int value)
   {
      return Math.min(dragBarMax,(int)(valuesPerPixel * (value-minimum) + 0.5d));
   }

   private void recomputeThumb()
   {
      minDragBarSize = fmH;
      int s = dragBarSize-1;
      int s2 = s/2;
      for (int i = barX.length; --i >= 0;) barX[i] = barY[i] = 0;
      // setup the polygon
      if (verticalBar)
      {
         if (invertDirection)
         {
            barX[4] = barX[1] = barY[0] = s2;
            barX[3] = barX[2] = width-1;
            barY[3] = barY[4] = s;
         }
         else
         {
            barX[3] = barX[1] = width-1-s2;
            barX[2] = width-1;
            barY[2] = s2;
            barY[3] = barY[4] = s;
         }
      }
      else
      {
         if (invertDirection)
         {
            barX[0] = barY[1] = barY[4] = s2;
            barX[1] = barX[2] = s;
            barY[2] = barY[3] = height-1;
         }
         else
         {
            barX[2] = barX[1] = s;
            barY[2] = barY[4] = height-1-s2;
            barX[3] = s2;
            barY[3] = height-1;
         }
      }
   }

   public void onPaint(Graphics g)
   {
      g.backColor = sbColor;
      int bc = getBackColor(),p,s;
      s = Math.max(4, verticalBar ? (width/2) : (height/2));
      p = verticalBar ? (width-s)/2 : (height-s)/2; // guich@tc126_72: center based on bar size
      switch (Settings.uiStyle)
      {
         case Settings.Android:
         case Settings.Vista:
         {
            if (verticalBar)
            {
               g.fillVistaRect(p, 0, s, height, bc,drawFilledArea, true); // shaded = filled
               g.backColor = isEnabled() ? fourColors[1] : bc;
               if (drawFilledArea) g.fillRect(p+1, dragBarPos, s-2, height-dragBarPos);
               g.translate(0,dragBarPos);
            }
            else
            {
               g.fillVistaRect(0,p, width, s, bc,false, false); // shaded = filled
               g.backColor = isEnabled() ? fourColors[1] : bc;
               if (drawFilledArea) g.fillRect(dragBarPos,p+1, width-1-dragBarPos, s-2); // solid = remains
               g.translate(dragBarPos,0);
            }
            g.backColor = isEnabled() ? sliderColor != -1 ? sliderColor: fourColors[0] : bc;
            g.foreColor = isEnabled() ? fourColors[1] : getForeColor();
            g.fillPolygon(barX, barY, 5);
            g.drawPolygon(barX, barY, 5);
            break;
         }
         case Settings.Flat:
         {
            int k = 1;
            if (verticalBar)
            {
               g.draw3dRect(p,0,s,height, Graphics.R3D_RAISED, false, false, fourColors);
               g.backColor = isEnabled() ? fourColors[0] : bc;
               if (dragBarPos > 0 && drawFilledArea) g.fillRect(p+k,k, s-k-k, drawFilledArea ? dragBarPos : size);
               g.translate(0,dragBarPos);
            }
            else
            {
               g.draw3dRect(0,p,width, s, Graphics.R3D_RAISED, false, false, fourColors);
               g.backColor = isEnabled() ? fourColors[0] : bc;
               if (dragBarPos > 0 && drawFilledArea) g.fillRect(k,p+k, drawFilledArea ? dragBarPos : size, s-k-k);
               g.translate(dragBarPos,0);
            }
            g.backColor = isEnabled() ? fourColors[0] : bc;
            g.foreColor = isEnabled() ? fourColors[1] : getForeColor();
            g.fillPolygon(barX, barY, 5);
            g.drawPolygon(barX, barY, 5);
            break;
         }
      }
      if (verticalBar)
         g.translate(0,-dragBarPos);
      else
         g.translate(-dragBarPos,0);
      if (drawTicks)
      {
         g.foreColor = getForeColor();
         for (int i = minimum; i < maximum; i++)
         {
            p = getDragBarPos(i) + dragBarSize/2;
            if (verticalBar)
            {
               if (invertDirection)
                  g.drawLine(0,p,2,p);
               else
                  g.drawLine(width-2,p,width,p);
            }
            else
            {
               if (invertDirection)
                  g.drawLine(p,0,p,2);
               else
                  g.drawLine(p,height,p,height-2);
            }
         }
      }
   }
}
