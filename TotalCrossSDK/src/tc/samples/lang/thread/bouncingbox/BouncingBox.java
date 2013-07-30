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



package tc.samples.lang.thread.bouncingbox;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.*;

public class BouncingBox extends MainWindow implements Runnable
{
   Button btnAddH,btnAddL,btnRemH,btnRemL;
   Edit ed;
   Label lMem, lInst;
   Vector vtl = new Vector(20);
   Vector vth = new Vector(20);

   public void initUI()
   {
      String instructions = "In this program you can see the effect of the multithread capabilities of TotalCross. The \"Add high\" button adds a thread with high priority (going vertically), and the \"Add low\" adds a low priority one (going horizontal). The Edit is there so you can enter some text while the boxes are going around. You can also see some screen refreshing problems (some DEAD BOXES that are left on screen) that should be avoided by your application.";
      instructions = Convert.insertLineBreak(Settings.screenWidth, fm, instructions);
      lInst = new Label(instructions,FILL);
      lInst.pageScroll = false;

      add(lInst, LEFT,TOP,FILL,lInst.getPreferredHeight()/2+1);
      add(btnAddH = new Button("Add High"), LEFT+10,AFTER);
      add(btnRemH = new Button("Remove High"), AFTER+20,SAME);
      add(btnAddL = new Button("Add Low"), LEFT+10,AFTER+4);
      add(btnRemL = new Button("Remove Low"), AFTER+20,SAME);
      add(ed=new Edit(), CENTER,BOTTOM,SCREENSIZE+50,PREFERRED);
      new Thread(this).start();
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == btnAddH)
         {
            Box mb = new Box(false);
            vth.push(mb);
            mb.start();
         }
         else
         if (e.target == btnAddL)
         {
            Box mb = new Box(true);
            vtl.push(mb);
            mb.start();
         }
         else
         if (e.target == btnRemH && vth.size() > 0)
         {
            try
            {
               Box mb = (Box)vth.pop();
               mb.running = false;
            } catch (ElementNotFoundException ee) {}
         }
         else
         if (e.target == btnRemL && vtl.size() > 0)
         {
            try
            {
               Box mb = (Box)vtl.pop();
               mb.running = false;
            } catch (ElementNotFoundException ee) {}
         }
      }
   }

   private boolean running = true;
   private boolean reset;
   public void run()
   {
      while (running)
      {
         if (reset)
         {
            lInst.scrollToBegin();
            reset = false;
         }
         else
         if (!lInst.scroll(true))
            reset = true;
         Vm.sleep(1500);
      }
   }
}
