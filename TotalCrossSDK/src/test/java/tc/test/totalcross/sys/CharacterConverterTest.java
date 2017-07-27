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

import totalcross.sys.CharacterConverter;
import totalcross.unit.TestCase;

public class CharacterConverterTest extends TestCase
{
  @Override
  public void testRun()
  {
    char []srcChars = new char[256];
    byte []srcBytes = new byte[256];
    for (int i = srcChars.length-1; i >= 0; i--)
    {
      srcChars[i] = (char)i;
      srcBytes[i] = (byte)i;
    }
    CharacterConverter cc = new CharacterConverter();
    char []dstChars = cc.bytes2chars(srcBytes,0,srcBytes.length);
    byte []dstBytes = cc.chars2bytes(srcChars,0,srcChars.length);

    assertEquals(dstChars.length,srcChars.length);
    assertEquals(dstBytes.length,srcBytes.length);
    for (int i = srcChars.length-1; i >= 0; i--)
    {
      assertEquals(srcChars[i],dstChars[i]);
      assertEquals(srcBytes[i],dstBytes[i]);
    }
    srcChars[0] = '\u8777';
    dstBytes = cc.chars2bytes(srcChars,0,1);
    assertEquals(1,dstBytes.length);
    dstChars = cc.bytes2chars(dstBytes,0,dstBytes.length);
    assertEquals('?',dstChars[0]);
  }
}
