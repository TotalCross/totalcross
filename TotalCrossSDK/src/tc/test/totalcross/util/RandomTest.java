/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: RandomTest.java,v 1.8 2011-01-04 13:19:09 guich Exp $

package tc.test.totalcross.util;

import totalcross.unit.*;
import totalcross.util.*;

public class RandomTest extends TestCase
{
   public void testRun()
   {
      double err = 1e-6;
      // our test is just to make sure that the same sequence
      // that the guy gets on desktop will occur on device
      Random r = new Random(2222); // four ducks swimming
      assertEquals(0.017390192d,r.nextDouble(), err);
      assertEquals(0.2769652d,r.nextDouble(), err);
      assertEquals(0.9550259d,r.nextDouble(), err);
      assertEquals(0.12059313d,r.nextDouble(), err);
      assertEquals(0.8088015d,r.nextDouble(), err);
      assertEquals(2,r.nextInt(10));
      assertEquals(80,r.nextInt(100));
      assertEquals(240,r.nextInt(1000));
      assertEquals(1583,r.nextInt(10000));
      assertEquals(38226,r.nextInt(100000));
      assertEquals(66,r.between('A','Z'));
      assertEquals(77,r.between('A','Z'));
      assertEquals(66,r.between('A','E'));
      assertEquals(65,r.between('A','E'));
      assertEquals(118,r.between('m','w'));
      assertEquals(115,r.between('m','w'));
      assertEquals(5,r.between(5,10));
      assertEquals(83,r.between(50,100));
      assertEquals(61,r.between(50,100));
      assertEquals(61,r.between(50,100));
      assertEquals(75,r.between(50,100));
   }
}
