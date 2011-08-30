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

/* A container that checks if the sibling is within the visible area before calling paint on it.
 * 
 * Used internally.
 */

public class ClippedContainer extends Container
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

