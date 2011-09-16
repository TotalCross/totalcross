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
 * 
 * Note: the colors must be set before the control's bounds are defined using setRect or add.
 */

public class PopupMenu extends Window
{
   private String []items;
   private int selected=-1;
   private Image off,ball;
   private ListContainer list;
   private Button cancel/*,ok - future*/;
   private ListContainer.Item []containers;
   private boolean multipleSelection;
   private int cursorColor=-1;
   /** The string of the button; defaults to "Cancel" */
   public static String cancelString = "Cancel";
   
   /** The check color used to fill the radio button used in Android. Defaults to the fore color.
    * @since TotalCross 1.3 
    */
   public int checkColor = -1;
   
   /** Constructs a PopupMenu with the given parameters and without multiple selection support. */
   public PopupMenu(String caption, String []items) throws IOException,ImageException
   {
      this(caption,items,false);
   }
   
   /** Constructs a PopupMenu with the given parameters. */
   public PopupMenu(String caption, String []items, boolean multipleSelection) throws IOException,ImageException
   {
      super(caption,ROUND_BORDER);
      this.multipleSelection = multipleSelection;
      uiAdjustmentsBasedOnFontHeightIsSupported = false;
      titleColor = Color.WHITE;
      this.items = items;
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
         layout.relativeFontSizes[0] = 2;
         layout.imageGap = 50;
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
      if (containers == null) return -1;
      if (-1 <= index && index < containers.length)
         selected(index);
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
         case ListContainerEvent.ITEM_SELECTED_EVENT:
            selected(((Control)event.target).appId);
            break;
         case ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT:
            if (((ListContainerEvent)event).isImage2)
               selected(((Control)event.target).parent.appId);
            break;
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
      if (!multipleSelection)
      {
         Vm.sleep(100);
         unpop();
      }
   }

   /** Sets the cursor color. By default, it is based in the background color */
   public void setCursorColor(int c)
   {
      cursorColor = c;
   }
}
