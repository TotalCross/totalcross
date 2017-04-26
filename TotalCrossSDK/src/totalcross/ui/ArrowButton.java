/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2008-2012 SuperWaba Ltda.                                      *
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

import totalcross.ui.gfx.*;

/** 
 * A class used to display a Button with an arrow inside. 
 */
public class ArrowButton extends Button
{
   /**
    * <code>Graphics.ARROW_UP</code>, <code>Graphics.ARROW_DOWN</code>, <code>Graphics.ARROW_LEFT</code>, or <code>Graphics.ARROW_RIGHT</code>.
    */
   protected byte direction;
   
   /**
    * prefWH the desired arrow width/height. The effective arrow's size will be computed based in the current width/height.
    */
   protected int prefWH;
   
   /**
    * The x position.
    */
   protected int xx;
   
   /**
    * The y position.
    */
   protected int yy;
   
   /**
    * The current arrow size.
    */
   protected int kk;
   
   /**
    * The newly assigned arrow size.
    */
   protected int dkk;
   
   /**
    * Indicates if the arrow is horizontal or not.
    */
   private boolean horiz;
   
   /** 
    * The arrow's color. 
    */
   public int arrowColor;
   
   /** 
    * Constructs an <code>ArrowButton</code>.  
    * @param direction <code>Graphics.ARROW_UP</code>, <code>Graphics.ARROW_DOWN</code>, <code>Graphics.ARROW_LEFT</code>, or 
    * <code>Graphics.ARROW_RIGHT</code>.
    * @param prefWH The desired arrow width/height. The effective arrow's size will be computed based in the current width/height.
    * @param arrowColor The arrow color. Can be changed by setting the <code>arrowColor</code> field. 
    */
   public ArrowButton(byte direction, int prefWH, int arrowColor)
   {
      super((String)null);
      this.direction = direction;
      this.arrowColor = arrowColor;
      this.prefWH = prefWH;
      horiz = direction == Graphics.ARROW_LEFT || direction == Graphics.ARROW_RIGHT;
   }
   
   /** 
    * Returns the preferred width of this control. 
    * 
    * @return The preferred width of this control.
    */
   public int getPreferredWidth()
   {
      return super.getPreferredWidth() + (horiz && border==BORDER_NONE ? prefWH : prefWH*2) - 1;
   }
   
   /**
    * Returns the preferred height of this control. 
    *
    * @return The preferred height of this control.
    */
   public int getPreferredHeight()
   {
      return super.getPreferredHeight() + (!horiz && border==BORDER_NONE ? prefWH : prefWH*2) - 1;
   }
   
   /**
    * Sets the style of the border.
    * 
    * @param border One out of <code>Button.BORDER_NONE</code>, <code>Button.BORDER_SIMPLE</code>, <code>Button.BORDER_3D</code>, 
    * <code>Button.BORDER_3D_HORIZONTAL_GRADIENT</code>, <code>Button.BORDER_3D_VERTICAL_GRADIENT</code>, or <code>Button.BORDER_GRAY_IMAGE</code>.
    */
   public void setBorder(byte border)
   {
      super.setBorder(border);
      recomputeParameters();
   }
   
   private void recomputeParameters()
   {
      if (dkk != 0)
         kk = dkk;
      else
      if (border != BORDER_NONE)
         kk = Math.min(width,height)/2-1;
      else
      {
         if ((width > height && horiz) || (height > width && !horiz))
            kk = Math.min(width,height)/2+1;
         else
            kk = Math.max(width,height)/2;
      }
      if (horiz)
      {
         xx = (width - kk) / 2;
         yy = height/2 -kk + 1;
      }
      else
      {
         xx = width/2 - kk + 1;
         yy = (height - kk) / 2;
      }
   }
   
   /**
    * Sets the arrow size.
    *
    * @param kk The new size.
    */
   public void setArrowSize(int kk)
   {
      dkk = kk;
      recomputeParameters();
   }
   
   /**
    * Called after a <code>setRect()</code>.
    * 
    * @param screenChanged If the bounds was changed due to a screen change (rotation, collapse).
    */
   protected void onBoundsChanged(boolean screenChanged)
   {
      super.onBoundsChanged(screenChanged);
      recomputeParameters();
   }
   
   /** 
    * Called by the system to draw the <code>ArrowButton</code>.
    * 
    * @param g The graphics object for drawing. 
    */
   public void onPaint(Graphics g)
   {
      super.onPaint(g);
      g.drawArrow(xx,yy,kk,direction,!uiAndroid && armed,isEnabled() ? arrowColor : Color.brighter(arrowColor,128)); // here is h regardless the case
   }
   
   /**
    * Returns a string representation of the object.
    * 
    * @return The string representation of the haired button plus its direction.
    */
   public String toString()
   {
      return super.toString()+", dir: "+direction;
   }
}
