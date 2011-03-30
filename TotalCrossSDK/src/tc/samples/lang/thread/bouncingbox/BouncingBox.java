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



package tc.samples.lang.thread.bouncingbox;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;

public class BouncingBox extends MainWindow implements Runnable
{
   Button btnAddH,btnAddL,btnRemH,btnRemL;
   Edit ed;
   Label lNum,lMem, lInst;
   Vector vtl = new Vector(20);
   Vector vth = new Vector(20);

   public void initUI()
   {
      setBackColor(Color.WHITE);
      UIColors.controlsBack = Color.WHITE;
      String instructions = "In this program you can see the effect of the multithread capabilities of TotalCross. The \"Add high\" button adds a thread with high priority (going vertically), and the \"Add low\" adds a low priority one (going horizontal). The Edit is there so you can enter some text while the boxes are going around. You can also see some screen refreshing problems (some DEAD BOXES that are left on screen) that should be avoided by your application.";
      instructions = Convert.insertLineBreak(Settings.screenWidth, fm, instructions);
      lInst = new Label(instructions,FILL);
      lInst.pageScroll = false;

      add(lInst, LEFT,TOP,FILL,lInst.getPreferredHeight()/2+1);
      add(btnAddH = new Button("Add High"), LEFT+10,AFTER);
      add(btnRemH = new Button("Remove High"), AFTER+20,SAME);
      add(btnAddL = new Button("Add Low"), LEFT+10,AFTER+4);
      add(btnRemL = new Button("Remove Low"), AFTER+20,SAME);
      if (Settings.platform.equals(Settings.PALMOS)) // high priority threads can lock Palm OS.
      {
         btnAddH.setEnabled(false);
         btnRemH.setEnabled(false);
      }

      add(ed=new Edit("@@@@@@@@"), CENTER,BOTTOM);
      add(lNum = new Label("",CENTER),LEFT,BEFORE-4);
      add(lMem = new Label("",CENTER),LEFT,BEFORE-4);
      new Thread(this).start();
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED)
      {
         if (e.target == btnAddH)
         {
            Box mb = new Box(lNum,false);
            vth.push(mb);
            mb.start();
         }
         else
         if (e.target == btnAddL)
         {
            Box mb = new Box(lNum,true);
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

   private int conta;
   private boolean running = true;
   private boolean reset;
   public void run()
   {
      while (running)
      {
         lMem.setText(++conta + " free bytes: "+Vm.getFreeMemory());
         lMem.repaintNow();
         if ((conta & 3) == 0)
         {
            if (reset)
            {
               lInst.scrollToBegin();
               reset = false;
               lInst.repaintNow();
            }
            else
            if (!lInst.scroll(true))
               reset = true;
            else
               lInst.repaintNow();
         }
         Vm.sleep(500);
      }
   }
}
