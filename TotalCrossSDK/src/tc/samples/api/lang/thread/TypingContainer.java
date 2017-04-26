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



package tc.samples.api.lang.thread;

import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class TypingContainer extends Container implements Runnable, ThreadSample.SetX
{
   String typingText = "The new virtual machine, called TotalCross, has better performance due to a new instruction set that eliminates limitations in the existing SuperWaba virtual machine, with enhancements such as unlimited object size, preemptive threads and a new high-performance garbage collector that is 20X faster than the SuperWaba's. Additionally, deployed files are now compacted, to acheive a 30% reduction in size over SuperWaba applications.";

   int index = 0;
   MultiEdit me;
   boolean fill;

   public TypingContainer(boolean fill)
   {
      this.fill = fill;
   }

   public void setX(int x)
   {
      this.x = x;
   }
   
   public void incX(int x)
   {
      this.x += x;
   }
   
   public void initUI()
   {
      super.initUI();
      setBackColor(Color.brighter(fill ? Color.YELLOW : Color.GREEN));
      setBorderStyle(BORDER_RAISED);

      me = new MultiEdit(0,0);
      add(me,LEFT,TOP,FILL,FILL);
      if (fill)
         me.justify = fill;
      me.setEditable(false);
      MultiEdit.hasCursorWhenNotEditable = false;
      me.setBackColor(fill ? Color.YELLOW : Color.GREEN);

      Thread t = new Thread(this);
      t.start();
   }

   public void run()
   {
      int length = typingText.length();
      StringBuffer sb = new StringBuffer(length);
      while (true)
      {
         index = 0;
         while (index < length)
         {
            sb.append(typingText.charAt(index));
            me.setText(sb.toString());
            if (ThreadSample.paused || ThreadSample.paused0) me.repaintNow();
            index++;
            Vm.sleep(100);
         }
         Vm.sleep(1000);
         sb.setLength(0);
         me.setText("");
         index = 0;
      }
   }
}
