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

   public void initUI()
   {
      super.initUI();
      add(btn = new Button("    Start    "), CENTER, TOP + 3);
      addLog(LEFT, AFTER + 3, FILL, FILL,null);
   }

   public void onEvent(Event e)
   {
      if (e.type == ControlEvent.PRESSED && e.target == btn)
      {
         log("----------- proceed(BEST_COMPRESSION) -----------");
         proceed(ZLib.BEST_COMPRESSION);

         log("----------- proceed(BEST_SPEED) -----------");
         proceed(ZLib.BEST_SPEED);
      }
   }

   private void proceed(int compression_level)
   {
      int sz = 0;
      StringBuffer sb = new StringBuffer(42*500);

      for (int i = 0; i < 500; i++)
         sb = sb.append("This is the uncompressed original message.");

      log("original size is " + sb.length());

      ByteArrayStream is = new ByteArrayStream(Convert.getBytes(sb));
      ByteArrayStream cs = new ByteArrayStream(0);

      try
      {
         int ini = Vm.getTimeStamp();
         sz = ZLib.deflate(is, cs, compression_level);
         int end = Vm.getTimeStamp();
         log("compressed output size=" + sz);
         log("Elapsed: " + (end - ini) + "ms");
      }
      catch (IOException e)
      {
         log("an exception occurred : " + e.getMessage());      
      }
      try
      {
         cs.setPos(0);
      }
      catch (IOException exc)
      {
         log("an exception occurred : " + exc.getMessage());
      }

      log("compressed byte stream:");
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
         log(ofs + " - " + sb.toString());
         ofs += read;
      }
      ByteArrayStream os = new ByteArrayStream(0);

      try
      {
         cs.setPos(0);
      }
      catch (IOException exc)
      {
         log("an exception occurred : " + exc.getMessage());
      }
      try
      {
         int ini = Vm.getTimeStamp();
         sz = ZLib.inflate(cs, os, sz);
         int end = Vm.getTimeStamp();
         log("uncompressed output size=" + sz);
         log("Elapsed: " + (end - ini) + "ms");
      }
      catch (IOException e)
      {
         log("an exception occurred : " + e.getMessage());
      }

      if (sz > 0)
      {
         String str = new String(os.getBuffer(), 0, sz);

         log("original content was '" + str.substring(0, 20) + "'");
         log("original size was " + sz);
      }
   }
}
