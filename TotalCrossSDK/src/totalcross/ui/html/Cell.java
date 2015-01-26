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
import totalcross.ui.html.Document.*;

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// !!!!  REMEMBER THAT ANY CHANGE YOU MAKE IN THIS CODE MUST BE SENT BACK TO SUPERWABA COMPANY     !!!!
// !!!!  LEMBRE-SE QUE QUALQUER ALTERACAO QUE SEJA FEITO NESSE CODIGO DEVERÁ SER ENVIADA PARA NOS  !!!!
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

/**
 * <code>Cell</code> is the cell of a Table.
 * Implementation notes:
 * In order to layout a cell, we first have to get its minimum width, then layout it.
 */

class Cell extends ScrollContainer implements SizeDelimiter
{
   Style style;
   int colspan,rowspan;
   
   Cell(Style style)
   {
      super(false);
      this.style = style;
      appObj = new ControlProperties(null,null,style);
      if (style.backColor != -1)
         setBackColor(style.backColor);
      colspan = style.atts.getAttributeValueAsInt("colspan",0);
      rowspan = style.atts.getAttributeValueAsInt("rowspan",0);
   }

   private void layoutControls()
   {
      if (this.width != 0) return;
      this.width = this.height = 4096; // temporary
      int minWidth = Math.max(parent.getWidth(),getMinWidth(this,0));
      LayoutContext lc2 = new LayoutContext(minWidth, this);
      Control[] children = bag.getChildren();
      if (children == null)
      {
         resize(1,1); // make sure size is never 0
         setBackColor(style.backColor);
      }
      else
      {
         for (int i = children.length; --i >= 0;)
            Document.layout(children[i], lc2);
         resize();
      }
   }
   
   public int getPreferredWidth()
   {
      return super.getPreferredWidth()+insets.left+insets.right;
   }
   
   public int getPreferredHeight()
   {
      return super.getPreferredHeight()+insets.top+insets.bottom;
   }
   
   private int getMinWidth(Control control, int curWidth)
   {
      if (control != this && control instanceof SizeDelimiter)
      {
         int w = ((SizeDelimiter)control).getMaxWidth(); // this is actually the minimum width
         if (w > curWidth) curWidth = w;
      }

      if (control instanceof Container)
      {
         Control[] children = ((Container)control).getChildren();
         for (int i = children==null ? 0 : children.length; --i >= 0;)
            curWidth = getMinWidth(children[i],curWidth);
      }
      return curWidth;
   }

   public int getMaxWidth()
   {
      layoutControls();
      return getPreferredWidth();
   }
}
