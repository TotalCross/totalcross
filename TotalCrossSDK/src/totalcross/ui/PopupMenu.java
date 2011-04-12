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
 */

public class PopupMenu extends Window
{
   private String []items;
   private int selected=-1;
   private static Image on,off;
   private ListContainer list;
   private Button cancel;
   private ListContainer.Item []containers;
   /** The string of the button; defaults to "Cancel" */
   public static String cancelString = "Cancel";
   /** The gap between the menu borders and the screen. Defaults to 20. */
   public static int SCREEN_GAP = 20;

   /** Constructs a PopupMenu that will show at the given x,y position the given items. */
   public PopupMenu(String caption, String []items) throws IOException,ImageException
   {
      super(caption,ROUND_BORDER);
      titleColor = Color.WHITE;
      this.items = items;
      if (on == null)
      {
         on  = new Image("totalcross/res/radioOn.png");
         off = new Image("totalcross/res/radioOff.png");
      }
      setRect(LEFT+SCREEN_GAP,TOP+SCREEN_GAP,FILL-SCREEN_GAP,FILL-SCREEN_GAP);
   }

   public void initUI()
   {
      add(cancel = new Button(cancelString), CENTER,BOTTOM-fmH/2,Settings.screenWidth/2,PREFERRED+fmH);
      add(list = new ListContainer(false), LEFT,TOP,FILL,FIT-fmH/2);
      list.setBackColor(Color.WHITE);
      
      ListContainer.Layout layout = list.getLayout(3,1);
      layout.insets.set(10,50,10,50);
      layout.defaultRightImage = off;
      layout.defaultRightImage2 = on;
      layout.relativeFontSizes[1] = 2;
      layout.controlGap = 50; // 50% of font's height
      layout.setup();
      
      containers = new ListContainer.Item[items.length];
      for (int i = 0; i < items.length; i++)
      {
         ListContainer.Item c = new ListContainer.Item(layout);
         containers[i] = c;
         c.items = new String[]{"",items[i],""};
         c.appId = i;
         list.addContainer(c);
      }
   }
   
   public void setBackColor(int c)
   {
      super.setBackColor(c);
      cancel.setBackColor(c);
   }
   
   public void setForeColor(int c)
   {
      super.setForeColor(c);
      cancel.setForeColor(c);
   }
   
   /** Selects the given index. */
   public int setSelectedIndex(int index)
   {
      if (0 <= index && index < containers.length)
      {
         if (0 <= selected && selected < containers.length)
            containers[selected].setImage(false,true); // set previous image to image1 (unselected)
         this.selected = index;
         containers[selected].setImage(false,false); // set new image to image 2 (selected)
      }
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
      setSelectedIndex(-1);
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
         case ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT:
         {
            ListContainerEvent lce = (ListContainerEvent)event;
            if (lce.isImage2)
            {
               if (0 <= selected && selected < containers.length)
                  containers[selected].setImage(false,true);
               selected = ((Control)lce.target).parent.appId;
               repaintNow();
               Vm.sleep(100);
               unpop();
            }
            break;
         }
      }
   }

   /** Sets the cursor color. By default, it is based in the background color */
   public void setCursorColor(int c)
   {
      list.highlightColor = c;
   }
}
