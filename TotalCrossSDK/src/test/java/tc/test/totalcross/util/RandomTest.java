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

package tc.test.totalcross.util;

import totalcross.unit.TestCase;
import totalcross.util.Random;

public class RandomTest extends TestCase {
  @Override
  public void testRun() {
    double err = 1e-6;
    // our test is just to make sure that the same sequence
    // that the guy gets on desktop will occur on device
    Random r = new Random(2222); // four ducks swimming
    assertEquals(0.017390192d, r.nextDouble(), err);
    assertEquals(0.2769652d, r.nextDouble(), err);
    assertEquals(0.9550259d, r.nextDouble(), err);
    assertEquals(0.12059313d, r.nextDouble(), err);
    assertEquals(0.8088015d, r.nextDouble(), err);
    assertEquals(2, r.nextInt(10));
    assertEquals(80, r.nextInt(100));
    assertEquals(240, r.nextInt(1000));
    assertEquals(1583, r.nextInt(10000));
    assertEquals(38226, r.nextInt(100000));
    assertEquals(89, r.between('A', 'Z'));
    assertEquals(85, r.between('A', 'Z'));
    assertEquals(67, r.between('A', 'E'));
    assertEquals(67, r.between('A', 'E'));
    assertEquals(112, r.between('m', 'w'));
    assertEquals(110, r.between('m', 'w'));
    assertEquals(10, r.between(5, 10));
    assertEquals(80, r.between(50, 100));
    assertEquals(92, r.between(50, 100));
    assertEquals(51, r.between(50, 100));
    assertEquals(65, r.between(50, 100));
  }
}
