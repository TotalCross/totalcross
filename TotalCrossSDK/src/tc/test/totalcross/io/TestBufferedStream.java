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

public class TestBufferedStream extends TestCase
{
   
   public void testRun()
   {
      try
      {
         File stream = new File(Convert.appendPath(Settings.appPath, "test"), File.CREATE_EMPTY);
         String string = "Hello world from TotalCross";
         byte[] bytes = string.getBytes();
         int length = bytes.length;
         
         // Invalid Parameters.
         try
         {
            new BufferedStream(null, BufferedStream.READ);
            fail("1");
         }
         catch (NullPointerException exception) {}
         try
         {
            new BufferedStream(stream, -1);
            fail("2");
         }
         catch (IllegalArgumentIOException exception) {}
         try
         {
            new BufferedStream(stream, 2);
            fail("3");
         }
         catch (IllegalArgumentIOException exception) {}
         try
         {
            new BufferedStream(stream, BufferedStream.READ, -1);
            fail("4");
         }
         catch (IllegalArgumentIOException exception) {}
         
         // Write mode.
         BufferedStream bufStr = new BufferedStream(stream, BufferedStream.WRITE);
      
         try // Can't use it in read mode.
         {
            bufStr.readBytes(bytes, 0, length);
            fail("5");
         }
         catch (IOException exception) {}
         
         // Invalid Parameters
         try 
         {
            bufStr.writeBytes(null, 0, length);
            fail("6");
         }
         catch (NullPointerException exception) {}
         try 
         {
            bufStr.writeBytes(bytes, -1, length);
            fail("7");
         }
         catch (IllegalArgumentIOException exception) {}
         try 
         {
            bufStr.writeBytes(bytes, 0, -1);
            fail("8");
         }
         catch (IllegalArgumentIOException exception) {}
         
         // Finally writes the bytes and closes the <code>BufferedStream</close>.
         assertEquals(length, bufStr.writeBytes(bytes, 0, length));
         bufStr.close();
         
         // Now is time to read data.
         byte[] bytesAux = new byte[length];
         stream.setPos(0);
         bufStr = new BufferedStream(stream, BufferedStream.READ);
         
         try // Can't use it in write mode.
         {
            bufStr.writeBytes(bytesAux, 0, length);
            fail("9");
         }
         catch (IOException exception) {}
         
         // Invalid Parameters
         try 
         {
            bufStr.readBytes(null, 0, length);
            fail("10");
         }
         catch (NullPointerException exception) {}
         try 
         {
            bufStr.readBytes(bytesAux, -1, length);
            fail("11");
         }
         catch (IllegalArgumentIOException exception) {}
         try 
         {
            bufStr.readBytes(bytesAux, 0, -1);
            fail("12");
         }
         catch (IllegalArgumentIOException exception) {}
         
         // Finally reads the bytes and closes the <code>BufferedStream</close>.
         assertEquals(length, bufStr.readBytes(bytesAux, 0, length));
         assertEquals(bytes, bytesAux);
         
         // Reads a line.
         stream.setPos(0);
         assertEquals(string, bufStr.readLine());
         
         // Tests the changing of the underlining stream.
         try
         {
            bufStr.setStream(null);
            fail("13");
         }
         catch (NullPointerException exception) {}         
         bufStr.setStream(stream);
         
         // Closes everything.
         stream.close();
         bufStr.close();         
      }
      catch (IOException exception)
      {
         fail("14");
      }
   }
   
}
