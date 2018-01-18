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

import java.util.Enumeration;

import totalcross.unit.TestCase;

public class TestEnumeration extends TestCase {
  @Override
  public void testRun() {
    Test3 test1 = new Test3();
    Enumeration test2 = new Test3();

    assertTrue(test1.hasMoreElements());
    assertTrue(test2.hasMoreElements());

    assertEquals("", test1.nextElement());
    assertEquals("", test2.nextElement());
  }
}

class Test3 implements Enumeration {
  @Override
  public boolean hasMoreElements() {
    return this instanceof Enumeration;
  }

  @Override
  public Object nextElement() {
    return new String("");
  }
}