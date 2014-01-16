/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.io;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.unit.TestCase;

public class TestCompressedByteArrayStream extends TestCase
{
   public void testRun()
   {
      try
      {
         String string = "Hello world from TotalCross";
         byte[] bytes = string.getBytes();
         int length = bytes.length;
         
         // Invalid parameters.
         try
         {
            new CompressedByteArrayStream(-1);
            fail("1");
         }
         catch (IllegalArgumentException exception) {}
         try
         {
            new CompressedByteArrayStream(10);
            fail("2");
         }
         catch (IllegalArgumentException exception) {}
         
         CompressedByteArrayStream cbaStr = new CompressedByteArrayStream(0);
         
         // NullPointerException.
         try
         {
            cbaStr.writeBytes((byte[])null);
            fail("3");
         }
         catch (NullPointerException exception) {}
         try
         {
            cbaStr.writeBytes((String)null);
            fail("4");
         }
         catch (NullPointerException exception) {}
         
         cbaStr.setMode(CompressedByteArrayStream.WRITE_MODE);
         
      }
      catch (IOException exception)
      {
         fail("13");
      }
   }
}
