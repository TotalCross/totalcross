/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2009-2011 SuperWaba Ltda.                                      *
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
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

/**
 *  ListContainer is a ListBox where each item is a Container.
 *  <p>
 *  The correct way to create a ListContainer item is by subclassing a 
 *  Container and adding the controls in the initUI method. Adding
 *  directly using <code>getContainer(i).add</code> will not work. Below is 
 *  an example of how to use it, taken from the UIGadgets sample.
 *  <pre>
   class LCItem extends ScrollContainer
   {
      Label lDate,lPrice,lDesc;
      Check chPaid;
      
      public LCItem()
      {
         super(false); // VERY IMPORTANT (a RuntimeException will be thrown if this is not used).
      }
      
      public void initUI()
      {
         add(chPaid = new Check("Paid"),LEFT,TOP);
         add(lDate = new Label("99/99/9999"),RIGHT,TOP); 
         add(new Label("US$"),LEFT,AFTER);
         add(lPrice = new Label("999.999.99"),AFTER,SAME);
         add(lDesc = new Label("",RIGHT),AFTER+10,SAME);
         lDesc.setText("description");
      }
   }
   
   private void testListContainer()
   {
      ListContainer lc;
      add(lc = new ListContainer(),LEFT,TOP,FILL,FILL);
      for (int i =0; i < 10; i++)
         lc.addContainer(new LCItem());
   }
 *  </pre>
 *  When a item is selected, a PRESSED event is dispatched.
 *  <br><br>
 *  Check the ListContainerSample in the sdk for a bunch of ideas of what can be done with this component.
 *  @since TotalCross 1.14
 */

public class ListContainer extends ScrollContainer
{
   /** A set of fields and default fields that will be used to define the layout of a ListContainer's Item. 
    */
   public class Layout
   {
      /** Specify what to do if the left or right controls are images.
       * There are two situations that occurs when the image has a different height of the ListContainer's Item: 
       * <ol> 
       *  <li> The image is smaller than the ListContainer's Item height. It can be enlarged or vertically centered.
       *  If the flag is false (default), the image will be centered. Otherwise, a "smooth upscaled" image will
       *  be created, however the image will mostly have a bad appearance. This is the worst choice. The best 
       *  choice is always to have a big image (for the biggest possible resolution of the device) that will 
       *  always be scaled down. 
       *  <li> The image is bigger than the ListContainer's Item height. It will always be scaled down, using a 
       *  "smooth scale" algorithm. This is the best choice.
       * </ol>
       * In general way, the image never defines the height of the Item; its the opposite: the number of Item 
       * lines is that defines the image height and size.
       */
      public boolean leftImageEnlargeIfSmaller, rightImageEnlargeIfSmaller;
      /** If the left and/or right control is a fixed Image, set it here and it will be replicated on all lines. */
      public Image defaultLeftImage, defaultRightImage;
      /** The default colors of all items. Defaults to BLACK. */
      public int[] defaultItemColors;
      /** The items that will have a bold font. Defaults to false (plain font) */
      public boolean[] boldItems;
      /** The default relative font sizes. You can specify a delta compared to the font size of this ListContainer.
       * For example, specifying -1 will make the item have a font 1 size less than the standard one. */
      public int[] relativeFontSizes;
      /** The x position of the label, relative to the column's width. 
       * Can be AFTER (default for all items), CENTER, RIGHT (adjustments are NOT allowed!).
       * The number of lines of the Item is computed based on the column count and the number of COLUMN_MARK(s) defined.
       * Note that this field cannot be changed after the first Item is created, since the internal computation of 
       * number of lines is done only once.
       */ 
      public int[] positions;
      /** The gap between the left/right controls and the text. 
       * This gap is a <b>percentage</b> based in the control font's height. So, if you pass 100 (default), it will be
       * 100% of the font's height, 50 will be 50% of the height, 150 will be 1.5x the font height, and so on.
       */
      public int controlGap = 100;
      /**
       * The gap between the Item and the borders can be set using this field. The values stored 
       * are not absolute pixels, but a percentage. Defaults to 0 (%) for all items.
       */
      public Insets insets = new Insets(0,0,0,0);
      
      /** The line spacing between two consecutive lines. Again, a percentage of the font's height is used.
       * Defaults to 0.
       * @see #controlGap
       */
      public int lineGap;
      
      private int itemCount,itemsPerLine;
      private int[] itemY;
      private Font[] fonts;
      private int prefH;
      
      /** Constructs a Layout component with the given columns and item count. */
      private Layout(int itemCount, int itemsPerLine)
      {
         this.itemCount = itemCount;
         this.itemsPerLine = itemsPerLine;
         defaultItemColors = new int[itemCount];
         relativeFontSizes = new int[itemCount];
         boldItems = new boolean[itemCount];
         fonts = new Font[itemCount];
         positions = new int[itemCount];
         for (int i = itemCount; --i >= 0;) positions[i] = AFTER;
      }
      
      public void setup()
      {
         // compute the number of lines
         int lineCount = itemCount / itemsPerLine;
         if ((itemCount % itemsPerLine) != 0)
            lineCount++;
         
         itemY = new int[itemCount];
         int totalH=0, y = insets.top*fmH/100;
         // compute for each line, compute its height based on the biggest font height
         for (int i = 0, lineH = 0, col = 0, last = itemCount-1; i <= last; i++)
         {
            Font f = fonts[i] = Font.getFont(font.name, boldItems[i], font.size+relativeFontSizes[i]);
            itemY[i] = y;
            if (f.fm.height > lineH) 
               lineH = f.fm.height;
            if (++col == itemsPerLine || i == last)
            {
               // adjust the y so that different font heights at the same line are bottom-aligned
               for (int j = i; j >= 0 && itemY[j] == y; j--)
                  itemY[j] += lineH-fonts[i].fm.height;
                  
               totalH += lineH;
               y += lineH + lineGap*fmH/100;
               lineH = col = 0;
            }
         }
            
         totalH += (lineGap * fmH / 100) * (lineCount-1);
         prefH = totalH + insets.bottom*fmH/100;
      }
   }

   public Layout getLayout(int itemCount, int itemsPerLine)
   {
      return new Layout(itemCount, itemsPerLine);
   }
   
   /** An item of the ListContainer. */
   public static class Item extends Container
   {
      /** The left and/or right controls that will be displayed. */
      public Control leftControl, rightControl;
      /** The Strings that will be displayed in the container. Individual items cannot be null; 
       * pass "" instead to not display it. 
       */
      public String[] items;
      /** The colors of all items. */
      public int[] itemColors;
      
      private Layout layout;
      
      /** Constructs an Item based in the given layout. You must set the items array with the strings that will
       * be displayed. You may also set the leftControl/rightControl and individual itemColors and boldItems.
       */
      public Item(Layout layout)
      {
         this.layout = layout;
         itemColors = new int[layout.itemCount];
         for (int i = layout.itemCount; --i >= 0;)
            itemColors[i] = layout.defaultItemColors[i];
         if (layout.defaultLeftImage != null)
            leftControl = new ImageControl(layout.defaultLeftImage);
         if (layout.defaultRightImage != null)
            rightControl = new ImageControl(layout.defaultRightImage);
      }
      
      public void reposition()
      {
         super.reposition();
         if (items.length != layout.itemCount)
            throw new IllegalArgumentException("Items have "+items.length+" but itemCount was specified as "+layout.itemCount+" in the Layout's constructor");
         removeAll();
         if (leftControl != null)
            add(leftControl, LEFT+layout.insets.left*fmH/100,TOP+layout.insets.top*fmH/100);
         if (rightControl != null)
            add(rightControl, RIGHT-layout.insets.right*fmH/100,TOP+layout.insets.top*fmH/100);
      }
      
      public int getPreferredWidth()
      {
         return parent.width;
      }
      
      public int getPreferredHeight()
      {
         return layout.prefH;
      }
      
      private void resizeImage(ImageControl control, int totalH, boolean imageEnlargeIfSmaller)
      {
         Image img = control.getImage();
         int iw = img.getWidth();
         int ih = img.getHeight();
         if (ih > totalH || (imageEnlargeIfSmaller && ih < totalH)) // if the image's height is bigger than the total height, always decrease the size.
            try
            {
               control.setImage(img.getSmoothScaledInstance(iw * totalH / ih, totalH, img.transparentColor));
            } 
            catch (ImageException ime) {} // just keep the previous image intact
      }
      
      public void onPaint(Graphics g)
      {
         super.onPaint(g);
         Layout layout = this.layout;
         // compute the area available for the text, excluding the left/right controls
         int x1 = layout.insets.left*fmH/100;
         if (leftControl != null)
            x1 += leftControl.getPreferredWidth() + layout.controlGap*fmH/100;
         int x2 = width - layout.insets.right*fmH/100;
         if (rightControl != null)
            x2 -= rightControl.getPreferredWidth() + layout.controlGap*fmH/100;
         
         for (int i = 0, col = 0, x = x1; i < layout.itemCount; i++)
         {
            Font f = layout.fonts[i];
            g.setFont(f);
            g.foreColor = itemColors[i];
            String s = items[i];
            int sw = f.fm.stringWidth(s);
            int sx;
            int sy = layout.itemY[i];
            g.backColor = backColor;
            switch (layout.positions[i])
            {
               default:
               case AFTER : sx = x;               break;
               case RIGHT : sx = x2 - sw;         if (x > sx) g.fillRect(sx,sy,sw,sy+f.fm.height); break; // erase area only if last text is beyond our limits
               case CENTER: sx = x + (x2-x-sw)/2; break;
            }
            g.drawText(s, sx, sy);
            x += sw;
            if (++col == layout.itemsPerLine)
            {
               col = 0;
               x = x1;
            }
         }
      }
   }

   private int ww;
   private Vector vc = new Vector(20);
   private IntHashtable ht = new IntHashtable(20);
   protected Container lastSel; //flsobral@tc126_65: ListContainer is no longer restricted to accept only subclasses of ScrollContainer, any subclass of Container may be added to a ListContainer.
   protected int lastSelBack,lastSelIndex=-1;
   /** Color used to highlight a container. Based on the background color. */
   public int highlightColor;
   /** If true (default), draws a horizontal line between each container. */
   public boolean drawHLine = true;
   
   public ListContainer()
   {
   }
   
   public void initUI()
   {
      super.initUI();
      ww = width-sbV.getPreferredWidth();
      if (drawHLine) add(new Ruler(Ruler.HORIZONTAL,false),LEFT,AFTER,ww,PREFERRED+2);
   }
   
   public void onColorsChanged(boolean colorsChanged)
   {
      super.onColorsChanged(colorsChanged);
      highlightColor = Color.getCursorColor(backColor);
   }
   
   /** Returns the number of items of this list */
   public int size() // guich@tc126_53
   {
      return vc.size();
   }
   
   /** Adds a new Container to this list. */
   public void addContainer(Container c)
   {
      if (c instanceof ScrollContainer)
      {
         ScrollContainer sc = (ScrollContainer) c;
         if (sc.sbH != null || sc.sbV != null)
            throw new RuntimeException("The given ScrollContainer "+c+" must have both ScrollBars disabled.");
         sc.shrink2size = true;
      }
      ht.put(c,vc.size());
      vc.addElement(c);
      add(c,LEFT,AFTER,ww,PREFERRED);
      if (c instanceof ScrollContainer)
         c.resize();
      if (drawHLine) add(new Ruler(Ruler.HORIZONTAL,false),LEFT,AFTER,ww,PREFERRED+2);
      resize();
   }
   
   public void onEvent(Event e)
   {
      super.onEvent(e);
      if ((e.type == PenEvent.PEN_DOWN || e.type == ControlEvent.FOCUS_IN) && !(e.target instanceof Ruler))
      {
         // find the container that was added to this ListContainer
         Control c = (Control)e.target;
         while (c != null)
         {
            if (c instanceof Container && ht.exists(c.hashCode()))
            {
               setSelectedItem((Container)c);
               postPressedEvent();
               break;
            }
            c = c.parent;
         }
      }
   }
   
   /** Returns the selected container, or null if none is selected. */
   public Container getSelectedItem()
   {
      return lastSel;
   }
   
   /** Returns the selected index, or -1 if none is selected. */
   public int getSelectedIndex()
   {
      return lastSelIndex;
   }
   
   /** Sets the selected container based on its index.
    * @param idx The index or -1 to unselect all containers.
    */
   public void setSelectedIndex(int idx)
   {
      if (idx < 0)
      {
         if (lastSel != null)
            setBackColor(lastSel,lastSelBack);
         lastSel = null;
         lastSelIndex = -1;
      }
      else
         setSelectedItem((Container)vc.items[idx]);
   }
   
   /** Sets the selected container. */
   public void setSelectedItem(Container c)
   {
      if (lastSel != null)
      {
         setBackColor(lastSel,lastSelBack);
         lastSel.setBackColor(lastSelBack);
      }
      lastSelBack = c.backColor;
      setBackColor(lastSel = (Container)c, highlightColor);
      c.setBackColor(highlightColor); //flsobral@tc126_70: highlight the whole selected container
      Window.needsPaint = true;
      try {lastSelIndex = ht.get(c.hashCode());} catch (ElementNotFoundException enfe) {}
   }
   
   /** Returns the given container number or null if its invalid. */
   public Container getContainer(int idx)
   {
      return idx >= vc.size() ? null : (Container)vc.items[idx];
   }
   
   /** Changes the color of all controls inside the given container that matches the background color
    * of this ListContainer.
    */
   public void setBackColor(Container c, int back)
   {
      for (Control child = c.children; child != null; child = child.next)
      {
         if (child.backColor == lastSelBack || child.backColor == highlightColor) // only change color if same background of container's
            child.setBackColor(back);
         if (child.asContainer != null)
            setBackColor(child.asContainer, back);
      }
   }
   
   public void resize() //flsobral@tc126: fix rotation
   {
      ww = width-sbV.getPreferredWidth();
      Control[] children = bag.getChildren(); 
      if (children != null)
      {
         for (int i = children.length - 1; i >= 0; i--)
         {
            children[i].setRect(KEEP, KEEP, ww, KEEP, null, true);
            Control child = children[i];
            if (child instanceof Container && !(child instanceof ScrollContainer))
            {
               Control[] children2 = ((Container) child).getChildren();
               if (children2 != null)
                  for (int j = children2.length - 1; j >= 0; j--)
                     children2[j].reposition();
            }
         }
      }
      super.resize();
   }
}
