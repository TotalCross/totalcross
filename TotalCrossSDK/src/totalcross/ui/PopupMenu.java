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

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

/** 
 * Creates a popup menu with a single line list and some radio buttons at right, like
 * in the Android combobox styles.
 * This is a sample of how to use it:
 * <pre>
   String[] items =
   {
         "Always",
         "Never",
         "Only in Silent mode",
         "Only when not in Silent mode",
         "Non of the answers above",
   };
   PopupMenu pm = new PopupMenu("Vibrate",items);
   pm.popup();
 * </pre>
 * A PRESSED event is sent when an item is selected.
 * 
 * Note: the colors must be set before the control's bounds are defined using setRect or add.
 * 
 */

public class PopupMenu extends Window
{
   private Object []items;
   private int selected=-1;
   private Image off,ball;
   private ListContainer list;
   private Button cancel/*,ok - future*/;
   private ListContainer.Item []containers;
   private boolean multipleSelection;
   private int cursorColor=-1;
   private static final int UNSET = -9999;
   private int desiredSelectedIndex = UNSET;
   private IntHashtable htSearchKeys;
   private PushButtonGroup pbgSearch;
   /** The string of the button; defaults to "Cancel" */
   public static String cancelString = "Cancel";
   /** If the items is a String matrix (String[][]), this field sets the column that will be shown. */
   public int dataCol;
   
   /** Sets the number of elements should be used from the items array passed in the constructor. Defaults to <code>items.length</code>. */
   public int itemCount;
   
   /** The check color used to fill the radio button used in Android. Defaults to the fore color.
    * @since TotalCross 1.3 
    */
   public int checkColor = -1;
   
   /** Set to true BEFORE popping up the window to enable search on the items of this PopupMenu.
    * Note that it only works if the items are ORDERED.
    * @since TotalCross 1.5
    */
   public boolean enableSearch;
   
   /** Set to false BEFORE popping up the window to disable the Cancel button.
    * @since TotalCross 1.65
    */
   public boolean enableCancel = true;
   
   /** Set to true to keep the selected index unchanged if user press the Cancel button
    * @since TotalCross 2.0
    */
   public boolean keepIndexOnCancel;
   
   /** Constructs a PopupMenu with the given parameters and without multiple selection support. */
   public PopupMenu(String caption, Object []items) throws IOException,ImageException
   {
      this(caption,items,false);
   }
   
   /** Constructs a PopupMenu with the given parameters. */
   public PopupMenu(String caption, Object []items, boolean multipleSelection) throws IOException,ImageException
   {
      super(caption,ROUND_BORDER);
      this.multipleSelection = multipleSelection;
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      titleColor = Color.WHITE;
      this.items = items;
      itemCount = items.length;
      if (multipleSelection)
      {
         off = new Image("totalcross/res/android/checkBkg.png");
         ball = new Image("totalcross/res/android/checkSel.png");
      }
      else
      {
         off = new Image("totalcross/res/android/radioBkg.png");
         ball = new Image("totalcross/res/android/radioSel.png");
      }
   }
   
   private Image getSelectedImage(int color) throws ImageException
   {
      // "off" image is a composite of two images: on + selection
      Image on = off.getFrameInstance(0);
      ball.applyColor2(color); // paint it
      on.getGraphics().drawImage(ball,0,0);
      return on;
   }

   public void initUI()
   {
      try
      {
         list = new ListContainer();
         list.setFont(this.font);
         if (cursorColor != -1)
            list.highlightColor = cursorColor;
         
         ListContainer.Layout layout = list.getLayout(3,1);
         layout.insets.set(10,50,10,50);
         layout.defaultRightImage = off;
         layout.defaultRightImage2 = getSelectedImage(checkColor == -1 ? foreColor : checkColor);
         layout.imageGap = 50;
         layout.controlGap = 50; // 50% of font's height
         layout.centerVertically = true;
         layout.setup();
         int cw=-1;
         
         containers = new ListContainer.Item[itemCount];
         
         Vm.preallocateArray(new ListContainer.Item(layout), itemCount);
         Vm.preallocateArray(new String[3], itemCount);
         if (enableSearch && itemCount <= 10)
            enableSearch = false;
         htSearchKeys = new IntHashtable(40);
         char last = 0;
         for (int i = 0; i < itemCount; i++)
         {
            ListContainer.Item c = new ListContainer.Item(layout);
            containers[i] = c;
            String s = items[i] instanceof String ? (String)items[i] : (items[i] instanceof String[]) ? ((String[])items[i])[dataCol] : items[i].toString();
            if (enableSearch && s.length() > 0)
            {
               char cc = s.charAt(0);
               if (cc != last)
               {
                  last = cc;
                  htSearchKeys.put(Convert.toUpperCase(cc), i);
               }
            }
            if (cw == -1)
               cw = getClientRect().width - Math.abs(c.getLeftControlX()) - Math.abs(c.getRightControlX());
            int sw = fm.stringWidth(s);
            if (sw <= cw)
               c.items = new String[]{"",s,""};
            else
            {
               String[] parts = Convert.tokenizeString(Convert.insertLineBreak(cw,fm,s),'\n');
               c.items = new String[]{"","",""};
               for (int j = 0, n = Math.min(parts.length, c.items.length); j < n; j++)
                  c.items[j] = parts[j];
            }
            c.appId = i;
         }
         if (htSearchKeys.size() <= 1)
            enableSearch = false;
         ScrollContainer sc2 = null;
         if (enableSearch)
         {
            IntVector v = htSearchKeys.getKeys();
            v.qsort();
            String[] caps = new String[v.size()];
            for (int i = 0; i < caps.length; i++)
               caps[i] = Convert.toString((char)v.items[i]);
            pbgSearch = new PushButtonGroup(caps,false,-1,0,fmH,1,true,PushButtonGroup.BUTTON);
            add(sc2 = new ScrollContainer(true, false),LEFT,TOP,FILL,fmH*2);
            sc2.add(pbgSearch, LEFT,TOP,PREFERRED,FILL);
         }
         if (enableCancel)
            add(cancel = new Button(cancelString),CENTER,BOTTOM-fmH/2,PARENTSIZE+90,PREFERRED+fmH);
         add(list = new ListContainer(),LEFT,enableSearch ? AFTER : TOP,FILL,(enableCancel?FIT:FILL)-fmH/2, enableSearch ? sc2 : null);
         list.setBackColor(Color.WHITE);
         list.addContainers(containers);
         repositionOnSize();
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE)
            e.printStackTrace();
         throw new RuntimeException(e.getClass().getName()+" "+e);
      }
   }
   
   private void repositionOnSize()
   {
      if (containers == null) return;
      int hh = containers[containers.length-1].getY2();
      int hm = list.y+hh+(cancel==null?0:cancel.height)+fmH;
      
      if (this.height > hm)
      {
         list.height = hh+fmH/3;
         setRect(CENTER,CENTER,KEEP,hm);
         if (cancel != null) cancel.setRect(KEEP,BOTTOM-fmH/3,KEEP,KEEP);
      }
   }
   
   public void reposition()
   {
      super.reposition();
      repositionOnSize();
   }

   /** Selects the given index. */
   public int setSelectedIndex(int index)
   {
      if (containers == null) 
      {
         desiredSelectedIndex = index;
         return -1;
      }
      if (-1 <= index && index < containers.length)
         selected(index);
      if (0 <= selected && selected < containers.length)
         list.scrollToControl(containers[selected]);
      return selected;
   }

   /** Returns the selected index when this window was closed or -1 if non was selected */
   public int getSelectedIndex()
   {
      return desiredSelectedIndex != UNSET ? desiredSelectedIndex : selected;
   }

   /** Setup some important variables */
   protected void onPopup()
   {
      if (list == null)
      {
         int maxW = Math.max(!enableCancel ? 0 : fm.stringWidth(cancelString), title == null ? 0 : titleFont.fm.stringWidth(title))+fmH*4;
         for (int i = 0; i < itemCount; i++)
         {
            String s = items[i] instanceof String ? (String)items[i] : (items[i] instanceof String[]) ? ((String[])items[i])[dataCol] : items[i].toString();
            int w = fm.stringWidth(s) + fmH*6;
            if (w > maxW) maxW = w;
         }
         setRect(CENTER,CENTER,maxW < Math.min(Settings.screenWidth,Settings.screenHeight)-fmH*2 ? maxW : SCREENSIZE+90,SCREENSIZE+90);
      }
      if (desiredSelectedIndex != UNSET) // change only if used wanted it
         setSelectedIndex(desiredSelectedIndex);
      desiredSelectedIndex = UNSET;
   }

   protected void postUnpop()
   {
      if (selected != -1) // guich@580_27
         postPressedEvent();
   }

   private void search(char c)
   {
      int pos = htSearchKeys.get(Convert.toUpperCase(c),-1);
      if (pos != -1)
         list.scrollToControl(containers[pos]);
   }
   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case KeyEvent.KEY_PRESS:
            if (enableSearch)
               search((char)((KeyEvent)event).key);
            break;
         case ControlEvent.PRESSED:
            if (enableSearch && event.target == pbgSearch && pbgSearch.getSelectedIndex() != -1)
               search(pbgSearch.getSelectedItem().charAt(0));
            else
            if (cancel != null && event.target == cancel)
            {
               if (!keepIndexOnCancel)
                  selected = -1;
               unpop();
            }
            break;
         case ListContainerEvent.ITEM_SELECTED_EVENT:
         {
            ListContainerEvent lce = (ListContainerEvent)event;
            selected(((Control)lce.source).appId);
            if (!multipleSelection)
            {
               Vm.sleep(100);
               unpop();
            }
            break;
         }
         case ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT:
         {
            ListContainerEvent lce = (ListContainerEvent)event;
            //if (lce.isImage2) since tc 1.5, when this event is sent the image 2 was already replaced by image 1
            {
               int idx;
               if (event.target instanceof Control)
                  idx = ((Control)event.target).parent.appId;
               else
                  idx = lce.source.appId;
               selected(idx);
               if (!multipleSelection)
               {
                  Vm.sleep(100);
                  unpop();
               }
            }
            break;
         }
      }
   }
   
   private void selected(int newSel)
   {
      if (0 <= selected && selected < containers.length)
         containers[selected].setImage(false,true);
      selected = newSel;
      if (0 <= selected && selected < containers.length)
         containers[newSel].setImage(false,false);
      if (selected == -1)
         list.setSelectedIndex(-1);
      
      repaintNow();
   }

   /** Sets the cursor color. By default, it is based in the background color */
   public void setCursorColor(int c)
   {
      cursorColor = c;
   }
}
