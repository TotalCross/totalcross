/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003-2004 Pierre G. Richard                                    *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
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



package totalcross.ui.html;
import totalcross.ui.*;
import totalcross.ui.gfx.*;

/**
* <code>Hr</code> is the Tile associated to the &lt;HR&gt; tag.
*/
class Hr extends Control implements Document.CustomLayout
{
   /**
   * Constructor
   *
   * @param doc containing document
   * @param atts tag attributes
   * @param style associated style
   */
   Hr()
   {
      focusTraversable = false;
   }

   public void layout(LayoutContext lc)
   {
      lc.disjoin();
      parent.add(this, lc.nextX, lc.nextY, 10, PREFERRED); // the width will be recomputed later
      lc.disjoin();
   }
   
   public int getPreferredHeight()
   {
      return fmH*2;
   }

   public void onPaint(Graphics g)
   {
      if (parent.getWidth() != width)
      {
         setRect(this.x,this.y,parent.getWidth(),this.height);
         repaintNow();
      }
      g.drawLine(0,fmH,width,fmH);
   }
}
