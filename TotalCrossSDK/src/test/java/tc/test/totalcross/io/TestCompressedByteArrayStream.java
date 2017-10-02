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

import totalcross.io.CompressedByteArrayStream;
import totalcross.io.IOException;
import totalcross.unit.TestCase;

public class TestCompressedByteArrayStream extends TestCase {
  @Override
  public void testRun() {
    try {
      String string = "Hello world from TotalCross";
      StringBuffer strBuf = new StringBuffer(string);
      byte[] bytes = string.getBytes();
      int length = bytes.length;

      // Invalid parameters.
      try {
        new CompressedByteArrayStream(-1);
        fail("1");
      } catch (IllegalArgumentException exception) {
      }
      try {
        new CompressedByteArrayStream(10);
        fail("2");
      } catch (IllegalArgumentException exception) {
      }

      CompressedByteArrayStream cbaStr = new CompressedByteArrayStream(9);

      // Invalid parameters.
      try {
        cbaStr.writeBytes((byte[]) null);
        fail("3");
      } catch (NullPointerException exception) {
      }
      try {
        cbaStr.writeBytes((String) null);
        fail("4");
      } catch (NullPointerException exception) {
      }
      try {
        cbaStr.writeBytes((StringBuffer) null);
        fail("5");
      } catch (NullPointerException exception) {
      }
      try {
        cbaStr.writeBytes(bytes, -1, length);
        fail("5");
      } catch (IllegalArgumentException exception) {
      }

      // Writes something.
      cbaStr.setMode(CompressedByteArrayStream.WRITE_MODE);
      assertEquals(length, cbaStr.writeBytes(bytes));
      assertEquals(length, cbaStr.writeBytes(bytes, 0, length));
      assertEquals(length, cbaStr.writeBytes(string));
      assertEquals(length, cbaStr.writeBytes(strBuf));
      cbaStr.writeLine(string);
      cbaStr.flush();

      // Checks sizes.
      assertEquals(length * 5 + 2, cbaStr.getSize()); // Includes CRLF.
      assertGreater(cbaStr.getSize(), cbaStr.getCompressedSize());

      // 
      /*try
         {
      
         }*/

      cbaStr.close();

      // Can't use a closed CompressedByteArrayStream.
      try {
        cbaStr.writeBytes(bytes);
      } catch (NullPointerException exception) {
      }
      try {
        cbaStr.writeBytes(bytes, 0, length);
      } catch (NullPointerException exception) {
      }
      try {
        cbaStr.writeBytes(strBuf);
      } catch (NullPointerException exception) {
      }

    } catch (IOException exception) {
      exception.printStackTrace();
      fail("13");
    }
  }
}
