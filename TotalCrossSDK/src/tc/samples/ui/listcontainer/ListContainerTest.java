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

package tc.samples.ui.listcontainer;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ListContainerTest extends MainWindow
{
   public ListContainerTest()
   {
      super("ListContainer Test", VERTICAL_GRADIENT);
      setUIStyle(Settings.Vista);
   }
   
   public void initUI()
   {
      try
      {
         final int TOTAL_ITEMS = 30; // increase this to 3000, for example

         if (TOTAL_ITEMS > 1000)
            Flick.defaultLongestFlick = TOTAL_ITEMS * 3;
         
         TabbedContainer tc = new TabbedContainer(new String[]{"Test1","PopupMenu"});
         add(tc,LEFT,TOP,FILL,FILL);
         
         final Button exit = new Button(" x ");
         add(exit,RIGHT,TOP);
         exit.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               exit(0);
            }
         });
         
         Container c1 = tc.getContainer(0);
         
         // "on" image
         final Image off = new Image("totalcross/res/android/radioBkg.png");
         // "off" image is a composite of two images: on + selection
         final Image on = off.getFrameInstance(0);
         final Image ball = new Image("totalcross/res/android/radioSel.png");
         ball.applyColor2(Color.ORANGE); // paint it
         on.getGraphics().drawImage(ball,0,0,Graphics.DRAW_PAINT,Color.WHITE,true);
         
         ListContainer lc = new ListContainer();
         c1.add(lc, LEFT,TOP,FILL,FILL);
         
         ListContainer.Layout layout = lc.getLayout(5,2);
         layout.insets.set(50,10,50,10);
         layout.defaultLeftImage = off;
         layout.leftImageEnlargeIfSmaller = true;
         layout.defaultRightImage = off;
         layout.defaultRightImage2 = on;
         layout.boldItems[1] = layout.boldItems[3] = true;
         layout.controlGap = 10; // 10% of font's height
         layout.defaultItemColors[1] = Color.RED;
         layout.lineGap = 25; // 1/4 font's height
         layout.relativeFontSizes[2] = layout.relativeFontSizes[3] = -1;
         layout.positions[3] = RIGHT;
         layout.positions[4] = CENTER;
         layout.setup();
         
         ListContainer.Item c = new ListContainer.Item(layout);
         c.items = new String[]{"00011 ","BAR LANCHONETE CONRADO","Rio de Janeiro/Centro"," 99999,99","Brasil"};
         lc.addContainer(c);
         
         c = new ListContainer.Item(layout);
         c.items = new String[]{"00015 ","BARITMOS RESTAURANTE","Rio de Janeiro/Copacabana"," 80000,00","Também Brasil"};
         c.leftControl = new Check(" ");
         lc.addContainer(c);
         
         c = new ListContainer.Item(layout);
         c.items = new String[]{"00015 ","BARITMOS RESTAURANTE","Rio de Janeiro/Copacabana"," 80000,00","Também Brasil"};
         c.leftControl = new Check(" ");
         lc.addContainer(c);

         Vm.gc();
         int gcTime = Settings.gcTime;
         int gcCount = Settings.gcCount;
         int ini = Vm.getTimeStamp();
         Container all[] = new Container[TOTAL_ITEMS];
         for (int i = 0; i < all.length; i++)
         {
            all[i] = c = new ListContainer.Item(layout);
            c.items = new String[]{Convert.zeroPad(i+1,5)," BARITONOS LANCHONETE","Rio de Janeiro/Leme","75000,00","Perú"};;
            c.leftControl = null;  
         }
         int ini2 = Vm.getTimeStamp();
         lc.addContainers(all);
         int ini3 = Vm.getTimeStamp();
         gcTime = Settings.gcTime - gcTime;
         gcCount = Settings.gcCount - gcCount;
         Vm.debug("creation: "+(ini2-ini)+" ms, addition: "+(ini3-ini2)+" ms, total: "+(ini3-ini)+" ms, gc: "+gcTime+" ms ("+gcCount+"x)"); 
         
         
         /////////////////
         
         Container c2 = tc.getContainer(1);
         final Label l2 = new Label("",CENTER);
         final Button btn1 = new Button(" Popup menu ",new Image("totalcross/res/android/comboArrow.png"), LEFT, fmH/2);
         c2.add(btn1,CENTER,CENTER);
         c2.add(l2,LEFT,AFTER+10);
         btn1.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               try
               {
                  String[] items =
                  {
                        "Always",
                        "Never",
                        "Only in Silent mode",
                        "Only when not in Silent mode",
                        "Non of the answers above",
                  };
                  PopupMenu pm = new PopupMenu("Vibrate",items);
                  pm.setBackColor(Color.BRIGHT);
                  pm.setCursorColor(Color.CYAN);
                  pm.setSelectedIndex(lastSel);
                  pm.popup();
                  lastSel = pm.getSelectedIndex();
                  l2.setText(lastSel == -1 ? "Cancelled" : "Selected "+lastSel);
               }
               catch (Exception ee)
               {
                  MessageBox.showException(ee,true);
               }
            }
         });
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   int lastSel=-1;
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ListContainerEvent.ITEM_SELECTED_EVENT:
            Vm.debug("Item selected "+e.target);
            break;
         case ListContainerEvent.LEFT_IMAGE_CLICKED_EVENT:
            Vm.debug("Left image clicked: "+(((ListContainerEvent)e).isImage2 ? "selected":"unselected"));
            break;
         case ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT:
            Vm.debug("Right image clicked: "+(((ListContainerEvent)e).isImage2 ? "selected":"unselected"));
            break;
      }
   }
}
