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

package tc.test.totalcross.ui.gfx;

import totalcross.ui.gfx.Color;
import totalcross.unit.TestCase;

public class ColorTest extends TestCase {
  @Override
  public void testRun() {
    int c = Color.getRGB(128, 128, 128);

    int brighter = Color.brighter(c);
    int lessbrighter = Color.brighter(c, Color.LESS_STEP);

    int darker = Color.darker(c);
    int lessdarker = Color.darker(c, Color.LESS_STEP);
    int halfdarker = Color.darker(c, Color.HALF_STEP);

    int cursor = Color.getCursorColor(c);
    String s = Color.toString(c);
    int alpha = Color.getAlpha(c);

    assertEquals(alpha, 0x80);
    assertEquals(c, 0x808080);
    assertEquals(brighter, 0xE0E0E0);
    assertEquals(darker, 0x202020);
    assertEquals(c, Color.brighter(darker));
    assertEquals(c, Color.darker(brighter));
    assertEquals(lessbrighter, 0xA0A0A0);
    assertEquals(lessdarker, 0x606060);
    assertEquals(halfdarker, 0x505050);
    assertEquals(cursor, brighter);
    assertEquals(s, "808080");
  }
}
