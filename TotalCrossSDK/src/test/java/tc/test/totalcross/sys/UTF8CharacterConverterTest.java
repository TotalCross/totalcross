/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.sys;

import totalcross.sys.UTF8CharacterConverter;
import totalcross.unit.TestCase;

public class UTF8CharacterConverterTest extends TestCase {
  @Override
  public void testRun() {
    char[] srcChars = new char[128 * 3];
    for (int i = 0; i < 128; i++) {
      srcChars[i] = (char) i; // will be converted to 1 byte
      srcChars[i + 128] = (char) (i + 0x80); // will be converted to 2 bytes
      srcChars[i + 256] = (char) (i + 0x800); // will be converted to 3 bytes
    }
    UTF8CharacterConverter cc = new UTF8CharacterConverter();
    byte[] dstBytes = cc.chars2bytes(srcChars, 0, srcChars.length); // convert to bytes
    char[] dstChars = cc.bytes2chars(dstBytes, 0, dstBytes.length); // and convert back to chars

    assertEquals(dstChars.length, 128 * 3);
    assertEquals(dstBytes.length, 128 * 6);
    for (int i = srcChars.length - 1; i >= 0; i--) {
      assertEquals(srcChars[i], dstChars[i]);
    }
  }
}
