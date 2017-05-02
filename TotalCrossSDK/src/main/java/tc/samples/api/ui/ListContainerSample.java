/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api.ui;

import tc.samples.api.*;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;

public class ListContainerSample extends BaseContainer
{
   static int lastCount = -1;
   public void initUI()
   {
      try
      {
         super.initUI();
         isSingleCall = true;
         
         int TOTAL_ITEMS = lastCount == 30 ? 3000 : 30; // increase this to 3000, for example
         lastCount = TOTAL_ITEMS;
         setInfo("Loading "+TOTAL_ITEMS+" items...");

         // "normal" image
         final Image normal = new Image("ui/images/plusButton.png");
         normal.applyColor(0x027F6A);
         
         // pressed image: colorize the original one
         final Image pressed = new Image("ui/images/plusButton.png");
         
         ListContainer lc = new ListContainer();
         lc.getFlick().longestFlick = TOTAL_ITEMS > 1000 ? TOTAL_ITEMS * 3 : 2500;
         add(lc, LEFT,TOP,FILL,FILL);
         
         ListContainer.Layout layout = lc.getLayout(5,2);
         layout.insets.set(50,10,50,10);
         layout.defaultLeftImage = new Image("ui/images/restaurant.png");
         layout.leftImageEnlargeIfSmaller = true;
         layout.defaultRightImage = normal;
         layout.defaultRightImage2 = pressed;
         layout.boldItems[1] = layout.boldItems[3] = true;
         layout.controlGap = 10; // 10% of font's height
         layout.defaultItemColors[1] = Color.RED;
         layout.lineGap = 25; // 1/4 font's height
         layout.relativeFontSizes[2] = layout.relativeFontSizes[3] = -1;
         layout.positions[3] = RIGHT;
         layout.setup();
         
         ListContainer.Item c;
         
         Vm.gc();
         int gcTime = Settings.gcTime;
         int gcCount = Settings.gcCount;
         int ini = Vm.getTimeStamp();
         Container all[] = new Container[TOTAL_ITEMS];
         for (int i = 0; i < all.length; i++)
         {
            all[i] = c = new ListContainer.Item(layout);
            c.items = new String[]{Convert.numberPad(i+1,5)," BARITONOS LANCHONETE","Price","75000,00","Rio de Janeiro / Leme"};;
         }
         int ini2 = Vm.getTimeStamp();
         lc.addContainers(all);
lc.autoScroll = true;
         int ini3 = Vm.getTimeStamp();
         gcTime = Settings.gcTime - gcTime;
         gcCount = Settings.gcCount - gcCount;
         setInfo(Settings.onJavaSE ? "" : "C="+(ini2-ini)+", A="+(ini3-ini2)+", T="+(ini3-ini)+", gc: "+gcTime+"/"+gcCount+"x");
         lc.requestFocus();

         /** Example just to show that other Controls can be set as a left and right control: 
         c = new ListContainer.Item(layout);
         c.items = new String[]{"00015 ","BARITMOS RESTAURANTE","Rio de Janeiro/Copacabana"," 80000,00","Também Brasil"};
         c.leftControl = new Check(" "); ((Check)c.leftControl).checkColor = Color.RED;
         lc.addContainer(c);
         */
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }

   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ListContainerEvent.ITEM_SELECTED_EVENT:
            setInfo("Item selected "+e.target);
            break;
         case ListContainerEvent.LEFT_IMAGE_CLICKED_EVENT:
            setInfo("Left image clicked: "+(((ListContainerEvent)e).isImage2 ? "selected":"unselected"));
            break;
         case ListContainerEvent.RIGHT_IMAGE_CLICKED_EVENT:
            setInfo("Right image clicked: "+(((ListContainerEvent)e).isImage2 ? "selected":"unselected"));
            break;
      }
   }
}