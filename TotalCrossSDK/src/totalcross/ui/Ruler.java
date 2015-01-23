/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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

/** Ruler is a horizontal or vertical ruler.
 * Here's an example:
 * <pre>
 * Ruler r = new Ruler();
 * r.invert = true;
 * add(r, LEFT,AFTER+2);
 * </pre> 
 */
public class Ruler extends Control
{
   /** Defines a HORIZONTAL ruler type. */
   public static final int HORIZONTAL = 0;
   /** Defines a VERTICAL ruler type. */
   public static final int VERTICAL = 1;
   
   /** Set to true to invert the color of the first and second lines.
    * Only makes sense if 3d is true. 
    */
   public boolean invert;
   
   private int c1,c2;
   private boolean isHoriz, is3d;
   
   /** Constructs a 3d horizonal ruler. */
   public Ruler()
   {
      this(HORIZONTAL,true);
   }
   
   /** Constructs a ruler of the given type.
    * @param type The type of the ruler.
    * @param is3d Set to false to draw a simple line instead of a 3d one. 
    * @see #HORIZONTAL
    * @see #VERTICAL
    */
   public Ruler(int type, boolean is3d)
   {
      isHoriz = type == HORIZONTAL;
      this.is3d = is3d;
      focusTraversable = false;
   }

   /** For horizontal rulers, returns FILL. Otherwise, returns 2 if is 3d or 1 if its not 3d. */
   public int getPreferredWidth()
   {
      return isHoriz ? FILL : is3d ? 2 : 1;
   }
   
   /** For vertical rulers, returns FILL. Otherwise, returns 2 if is 3d or 1 if its not 3d. */
   public int getPreferredHeight()
   {
      return !isHoriz ? FILL : is3d ? 2 : 1;
   }
   
   public void onColorsChanged(boolean colorsChanged)
   {
      c1 = getForeColor();
      if (is3d) c2 = 0xFFFFFF ^ c1;
   }
   
   public void onPaint(Graphics g)
   {
      int n = is3d ? 2 : 1;
      if (backColor != parent.backColor)
      {
         g.backColor = backColor;
         g.fillRect(0,0,width,height);
      }
      if (isHoriz)
      {
         int yy = (this.height - n) / 2;
         g.foreColor = invert ? c2 : c1;
         g.drawLine(0,yy,width,yy);
         if (is3d)
         {
            g.foreColor = invert ? c1 : c2;
            g.drawLine(0,yy+1,width,yy+1);
         }
      }
      else
      {
         int xx = (this.width - n) / 2;
         g.foreColor = invert ? c2 : c1;
         g.drawLine(xx,0,xx,height);
         if (is3d)
         {
            g.foreColor = invert ? c1 : c2;
            g.drawLine(xx+1,0,xx+1,height);
         }
      }
   }
}
