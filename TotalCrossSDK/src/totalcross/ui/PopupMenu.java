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
   private int desiredSelectedIndex = -1;
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
      on.getGraphics().drawImage(ball,0,0,Graphics.DRAW_PAINT,Color.WHITE,true);
      return on;
   }

   public void initUI()
   {
      try
      {
         add(cancel = new Button(cancelString), CENTER,BOTTOM-fmH/2,Settings.screenWidth/2,PREFERRED+fmH);
         add(list = new ListContainer(), LEFT,TOP,FILL,FIT-fmH/2);
         list.setBackColor(Color.WHITE);
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
         int cw = getClientRect().width - ball.getWidth();
         
         containers = new ListContainer.Item[itemCount];
         
         Vm.preallocateArray(new ListContainer.Item(layout), itemCount);
         Vm.preallocateArray(new String[3], itemCount);
         for (int i = 0; i < itemCount; i++)
         {
            ListContainer.Item c = new ListContainer.Item(layout);
            containers[i] = c;
            String s = items[i] instanceof String ? (String)items[i] : (items[i] instanceof String[]) ? ((String[])items[i])[dataCol] : items[i].toString();
            if (fm.stringWidth(s) <= cw)
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
         list.addContainers(containers);
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE)
            e.printStackTrace();
         throw new RuntimeException(e.getClass().getName()+" "+e);
      }
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
      return selected;
   }

   /** Setup some important variables */
   protected void onPopup()
   {
      if (list == null)
         setRect(CENTER,CENTER,SCREENSIZE+90,SCREENSIZE+90);
      setSelectedIndex(desiredSelectedIndex);
      desiredSelectedIndex = -1;
   }

   protected void postUnpop()
   {
      if (selected != -1) // guich@580_27
         postPressedEvent();
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
            if (event.target == cancel)
            {
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
            if (lce.isImage2)
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
