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

// $Id: ListContainer.java,v 1.16 2011-03-21 19:27:01 guich Exp $

package totalcross.ui;

import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
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
 *  @since TotalCross 1.14
 */

public class ListContainer extends ScrollContainer
{
   private int ww;
   private Vector vc = new Vector(20);
   private IntHashtable ht = new IntHashtable(20);
   protected Container lastSel; //flsobral@tc126_65: ListContainer is no longer restricted to accept only subclasses of ScrollContainer, any subclass of Container may be added to a ListContainer.
   protected int lastSelBack,lastSelIndex=-1;
   /** Color used to highlight a container. Based on the background color. */
   public int highlightColor;
   /** If true (default), draws an horizontal line between each container. */
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
