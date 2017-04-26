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


/* A container that checks if the sibling is within the visible area before calling paint on it.
 * 
 * Used internally.
 */

public class ClippedContainer extends Container
{
   public boolean verticalOnly;
   protected int bagClipX0,bagClipXf,bagClipY0,bagClipYf;
   private int lastMid;

   private int findOneVisible(int y0, int yf, int ini, int end)
   {
      int i,mid;
      for (i = 0;end-i > 1 ;)
      {
         mid = i + (end-i)/2;
         Control c = (Control)tabOrder.items[mid];
         if (c.y > yf)
            end = mid;
         else
         if (y0 > c.y+c.height)
            i = mid;
         else
            return mid;
      }
      return 0;
   }
   
   protected void computeClipRect()
   {
      bagClipY0 = -this.y; 
      bagClipYf = bagClipY0 + parent.height;
      bagClipX0 = -this.x;
      bagClipXf = bagClipX0 + parent.width;
   }
   
   public void paintChildren()
   {
      computeClipRect();
      Window pw = getParentWindow();
      if (pw == Window.topMost) pw = null; // no need to check if we're already at the top
      
      if (verticalOnly)
      {
         Object[] items = tabOrder.items;
         int n = tabOrder.size(),i,first,painted=0;
         // check if the mid container of the last search is still visible, and restart the search using it
         if (lastMid != -1 && lastMid < n && ((Control)items[lastMid]).isVisibleAndInside(bagClipY0,bagClipYf))
            first = lastMid;
         else // not visible, binary search again
            first = findOneVisible(bagClipY0, bagClipYf, 0, n);
         // found the first (or second) visible, go back to find the really first visible
         while (first > 0 && ((Control)items[first-1]).isVisibleAndInside(bagClipY0,bagClipYf)) // find the first visible
            first--;
         // now go forward until no other is visible
         for (i = first; i < n; i++)
         {
            Control child = (Control)items[i];
            if (painted++ > 0 && !child.isVisibleAndInside(bagClipY0,bagClipYf))
               break;
            if (pw == null || !child.isObscured(pw))            
            {
               child.onPaint(child.getGraphics());
               if (child.asContainer != null)
                  child.asContainer.paintChildren();
            }
         }
         lastMid = (first+i)/2;
      }
      else
      {
         for (Control child = children; child != null; child = child.next)
            if (child.isVisibleAndInside(bagClipX0,bagClipY0,bagClipXf,bagClipYf) && (pw == null || !child.isObscured(pw)))
            {
               if (child.asContainer != null && child.asContainer.offscreen != null)
                  getGraphics().drawImage(child.asContainer.offscreen,child.x,child.y);
               else
               {
                  child.onPaint(child.getGraphics());
                  if (child.asContainer != null)
                     child.asContainer.paintChildren();
               }
            }
      }
   }
}

