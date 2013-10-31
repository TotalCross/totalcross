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

/** A class used to display a Button with an arrow inside. 
 */

public class ArrowButton extends Button
{
   protected byte direction;
   protected int prefWH;
   protected int xx,yy,kk,dkk;
   private boolean horiz;
   
   /** The arrow's color */
   public int arrowColor;
   
   /** Constructs an Arrow Button  
    * @param direction Graphics.ARROW_UP or Graphics.ARROW_DOWN or Graphics.ARROW_LEFT or Graphics.ARROW_RIGHT
    * @param prefWH the desired arrow width/height. The effective arrow's size will be computed based in the current width/height.
    * @param arrowColor the arrow color. Can be changed by setting the arrowColor field. 
    */
   public ArrowButton(byte direction, int prefWH, int arrowColor)
   {
      super((String)null);
      this.direction = direction;
      this.arrowColor = arrowColor;
      this.prefWH = prefWH;
      horiz = direction == Graphics.ARROW_LEFT || direction == Graphics.ARROW_RIGHT;
   }
   
   public int getPreferredWidth()
   {
      return super.getPreferredWidth() + (horiz && border==BORDER_NONE ? prefWH : prefWH*2) - 1;
   }
   
   public int getPreferredHeight()
   {
      return super.getPreferredHeight() + (!horiz && border==BORDER_NONE ? prefWH : prefWH*2) - 1;
   }
   
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
   
   public void setArrowSize(int kk)
   {
      dkk = kk;
      recomputeParameters();
   }
   
   protected void onBoundsChanged(boolean screenChanged)
   {
      super.onBoundsChanged(screenChanged);
      recomputeParameters();
   }
   
   public void onPaint(Graphics g)
   {
      super.onPaint(g);
      g.drawArrow(xx,yy,kk,direction,!uiAndroid && armed,enabled ? arrowColor : Color.brighter(arrowColor,128)); // here is h regardless the case
   }
   
   public String toString()
   {
      return super.toString()+", dir: "+direction;
   }
}
