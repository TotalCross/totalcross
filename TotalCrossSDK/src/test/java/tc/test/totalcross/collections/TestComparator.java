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

package tc.test.totalcross.collections;

import java.util.Comparator;

import totalcross.unit.TestCase;

public class TestComparator extends TestCase
{
  @Override
  public void testRun()
  {
    Comparator test1 = new Test2();
    Test2 test2 = new Test2();

    assertEquals(0, test1.compare(test1, test2));
    assertEquals(0, test2.compare(test2, test1));

    Test2 test3 = (Test2)test1;
    assertEquals(1, test1.compare(test1, test3));
    assertEquals(0, test2.compare(test2, test3));

    test3 = test2;
    assertEquals(0, test2.compare(test1, test3));
    assertEquals(1, test1.compare(test3, test2));
  }
}

class Test2 implements Comparator
{
  @Override
  public int compare(Object o1, Object o2)
  {
    return o1.equals(o2)? 1 : 0;
  }
}
