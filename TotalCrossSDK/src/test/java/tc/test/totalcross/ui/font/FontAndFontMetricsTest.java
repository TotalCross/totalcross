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



package tc.test.totalcross.ui.font;

import totalcross.ui.font.*;
import totalcross.unit.*;

public class FontAndFontMetricsTest extends TestCase
{
   public void testRun()
   {
      Font bigplain = Font.getFont("BabiDedeNat", false, Font.BIG_SIZE);
      //Font bigbold = bigplain.asBold();
      Font smallplain = Font.getFont(Font.DEFAULT, false, Font.NORMAL_SIZE);
      Font smallbold = smallplain.asBold();

      assertEquals(bigplain.name, Font.DEFAULT); // BabiDedeNat won't be found, so the name will be replaced by default font
      assertLowerOrEqual(smallplain.fm.height, bigplain.fm.height); // make sure that the small font is really smaller than the bigger one
      assertLowerOrEqual(smallplain.fm.stringWidth("BabiDedeNat"), smallbold.fm.stringWidth("BabiDedeNat")); // make sure that the plain font is smaller than the bold one
      // not true for 320x320! assertTrue(bigplain.fm.getTextWidth("Verinha") <= bigbold.fm.getTextWidth("Verinha")); // make sure that the plain font is smaller than the bold one
      // no more ideas
   }
}
