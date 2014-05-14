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



package tc.samples.api.util;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.zip.*;

public class ZLibSample extends BaseContainer
{
   private Button btn;
   private ListBox lb;

   public void initUI()
   {
      add(btn = new Button("    Start    "), CENTER, TOP + 3);
      add(lb = new ListBox());
      lb.setRect(LEFT, AFTER + 3, FILL, FILL);
   }

   private void debug(String s)
   {
      lb.add(s);
      lb.repaintNow();
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == btn)
      {
         debug("----------- proceed(BEST_COMPRESSION) -----------");
         proceed(ZLib.BEST_COMPRESSION);

         debug("----------- proceed(BEST_SPEED) -----------");
         proceed(ZLib.BEST_SPEED);
      }
   }

   private void proceed(int compression_level)
   {
      int sz = 0;
      StringBuffer sb = new StringBuffer(42*500);

      for (int i = 0; i < 500; i++)
         sb = sb.append("This is the uncompressed original message.");

      debug("original size is " + sb.length());

      ByteArrayStream is = new ByteArrayStream(Convert.getBytes(sb));
      ByteArrayStream cs = new ByteArrayStream(0);

      try
      {
         int ini = Vm.getTimeStamp();
         sz = ZLib.deflate(is, cs, compression_level);
         int end = Vm.getTimeStamp();
         debug("compressed output size=" + sz);
         debug("Elapsed: " + (end - ini) + "ms");
      }
      catch (IOException e)
      {
         debug("an exception occurred : " + e.getMessage());      
      }
      try
      {
         cs.setPos(0);
      }
      catch (IOException exc)
      {
         debug("an exception occurred : " + exc.getMessage());
      }

      debug("compressed byte stream:");
      byte buf[] = new byte[8];
      int read, ofs = 0, left = sz;
      while (left > 0)
      {
         read = left;
         if (read > buf.length)
            read = buf.length;

         read = cs.readBytes(buf, 0, read);
         left -= read;

         sb.setLength(0);
         for (int i = 0; i < read; i++)
            sb = sb.append(' ').append(Convert.unsigned2hex(buf[i], 2));
         debug(ofs + " - " + sb.toString());
         ofs += read;
      }
      ByteArrayStream os = new ByteArrayStream(0);

      try
      {
         cs.setPos(0);
      }
      catch (IOException exc)
      {
         debug("an exception occurred : " + exc.getMessage());
      }
      try
      {
         int ini = Vm.getTimeStamp();
         sz = ZLib.inflate(cs, os, sz);
         int end = Vm.getTimeStamp();
         debug("uncompressed output size=" + sz);
         debug("Elapsed: " + (end - ini) + "ms");
      }
      catch (IOException e)
      {
         debug("an exception occurred : " + e.getMessage());
      }

      if (sz > 0)
      {
         String str = new String(os.getBuffer(), 0, sz);

         debug("original content was '" + str.substring(0, 20) + "'");
         debug("original size was " + sz);
      }
   }
}
