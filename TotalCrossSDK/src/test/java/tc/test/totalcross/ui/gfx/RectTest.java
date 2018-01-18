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

import totalcross.ui.gfx.Coord;
import totalcross.ui.gfx.Rect;
import totalcross.unit.TestCase;

public class RectTest extends TestCase {
  @Override
  public void testRun() {
    Rect r1 = new Rect(-10, -10, 100, 100);
    Rect r2 = new Rect(new Coord(-10, -10), new Coord(89, 89));
    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
    assertFalse(r1.contains(200, 200));
    assertTrue(r1.contains(50, 50));
    assertEquals(89, r1.x2());
    r2.modify(-10, -10, 50, 50);
    Rect r3 = r2.unionWith(r1);
    Rect r4 = r3.intersectWith(r2);
    assertTrue(r4.intersects(r3));
    assertTrue(r4.intersects(new Rect(89, 89, 10, 10)));
    assertFalse(r1.intersects(new Rect(90, 90, 10, 10)));
  }
}
