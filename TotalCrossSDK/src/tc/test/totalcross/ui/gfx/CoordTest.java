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

import totalcross.ui.gfx.*;
import totalcross.unit.*;

public class CoordTest extends TestCase
{
   public void testRun()
   {
      // not much to test on this class...
      Coord c = new Coord(128,128);
      Coord c2 = new Coord(256,256);
      assertEquals(c.x,c.width());
      assertEquals(c.y,c.height());
      c2.translate(-128,-128);
      assertEquals(c, c2);
      assertEquals(c2.toString(), c.toString());
      assertEquals(c.hashCode(),0x800080);
   }
}
