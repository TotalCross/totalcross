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

/** Implements a ListBox where colors can be choosen from.
  * The only functional methods are setColors, getSelectedItem and getSelectedColor.
  * Next an example of how to use this class as a combobox color chooser:
  * <pre>
  * add(foreCombo = new ComboBox(new ColorList()), CENTER, BOTTOM);
  * </pre>
  * Consider using ColorChooserBox instead.
  * @see totalcross.ui#ColorChooserBox
  */

public class ColorList extends ListBox
{
   private int w;
   
   /** Item added to the ListBox, containing the color as String and as int. */
   public static class Item
   {
      public int value;
      private String s;

      public Item(int value)
      {
         set(value);
      }
      public void set(int value)
      {
         this.value = value;
         s = Color.toString(value);
      }
      public boolean equals(Object other)
      {
         if (other instanceof Item)
            return ((Item)other).value == value;
         if (other instanceof String)
            return Color.getRGB((String)other) == value;
         return false;
      }
      public String toString()
      {
         return s;
      }
   }
   private static Item blackItem = new Item(0);

   private static Item[] convert(int[] c)
   {
      Item[] ci = new Item[c.length];
      for (int i = c.length-1; i >= 0; i--)
         ci[i] = new Item(c[i]);
      return ci;
   }

   private static Item []defaultColors;
   static
   {
      defaultColors = convert(Graphics.getPalette());
   }
   private Item[] colors;

   public ColorList()
   {
      super(defaultColors);
      colors = defaultColors;
      w = fm.stringWidth("CCCCCC")+8;
   }

   /** Sets the colors that will be displayed. */
   public void setColors(int[] newColors) // guich@582_4
   {
      colors = convert(newColors);
      removeAll();
      add(colors);
   }
   
   protected void drawItem(Graphics g, int index, int dx, int dy)
   {
      if (0 <= index && index <= colors.length)
      {
         int hh = getItemHeight(index);
         if (uiAndroid) hh--;
         int xx = btnX == 0 ? width : btnX;
         g.backColor = colors[index].value;
         if (uiVista)
            g.fillVistaRect(dx-1,dy,xx,hh,g.backColor,false,false);
         else
            g.fillRect(dx-1,dy,xx,hh);
         g.foreColor = Color.getAlpha(colors[index].value) > 128 ? Color.BLACK : Color.WHITE;
         g.drawText(colors[index].toString(), dx+4, dy+(hh-fmH)/2, textShadowColor != -1, textShadowColor);
      }
   }
   
   protected void drawSelectedItem(Graphics g, int index, int dx, int dy)
   {
      drawItem(g, index, dx, dy); // we will draw using the selected color
   }
   
   protected void drawCursor(Graphics g, int sel, boolean on)
   {
      if (offset <= sel && sel < visibleItems+offset && sel < itemCount)
      {
         int dx = 3;
         int dy = 4;
         if (uiFlat) dy--;
         if (simpleBorder) {dx--; dy--;}
         int ih = getItemHeight(sel);
         dy += (sel-offset) * ih;
         int yy = dy-2-(ih-fmH)/2;
         g.setClip(dx-1,yy,btnX-dx+1,Math.min(ih * visibleItems, this.height-dy)); // guich@200b4_83: fixed selection overflowing paint area
         int c = colors[sel].value;
         int a = Color.getAlpha(c);
         g.foreColor = a >= 128 ? 0 : Color.WHITE;
         g.drawRect(dx-1,yy,btnX-1,ih); // only select the Object - guich@200b4_130
      }
   }
   public int getPreferredWidth()
   {
      return w + (simpleBorder?4:6) + sbar.getPreferredWidth() + insets.left+insets.right;
   }
   
   protected int getItemWidth(int index)
   {
      return w;
   }
   
   /** Returns the selected Item object, or a black Item representing the black color if none was selected.
    * From the Item you can retrieve the color as int. Use the getSelectedColor method to retrieve
    * the color directly. */
   public Object getSelectedItem()
   {
      return selectedIndex!=-1 ? colors[selectedIndex] : blackItem;
   }

   /** Returns the selected color or black if no one is selected. */
   public int getSelectedColor()
   {
      return selectedIndex!=-1 ? colors[selectedIndex].value : 0;
   }
}