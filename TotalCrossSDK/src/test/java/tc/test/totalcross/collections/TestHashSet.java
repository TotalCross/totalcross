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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import totalcross.unit.TestCase;

public class TestHashSet extends TestCase {
  @Override
  public void testRun() {
    Test21 test1 = new Test21();
    HashSet test2 = new Test21(10, 0.1f);
    AbstractSet test3 = new Test21(test2);

    assertTrue(test1.add(null));
    assertTrue(test2.add(null));
    assertTrue(test3.add(null));

    test1.clear();
    test2.clear();
    test3.clear();

    assertFalse(test1.contains(null));
    assertFalse(test2.contains(null));
    assertFalse(test3.contains(null));

    assertTrue(test1.isEmpty());
    assertTrue(test2.isEmpty());
    assertTrue(test3.isEmpty());

    assertTrue(test1.iterator() instanceof Iterator);
    assertTrue(test2.iterator() instanceof Iterator);
    assertTrue(test3.iterator() instanceof Iterator);

    assertFalse(test1.remove(null));
    assertFalse(test2.remove(null));
    assertFalse(test3.remove(null));

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    assertEquals(test1, test1.clone());
    assertEquals(test2, test2.clone());
    assertEquals(test3, ((HashSet) test3).clone());
  }
}

class Test21 extends HashSet implements Cloneable {
  public Test21() {
    super();
  }

  public Test21(int initialCapacity, float load) {
    super(initialCapacity, load);
  }

  public Test21(Collection c) {
    super(c);
  }

  @Override
  public Object clone() {
    return super.clone();
  }
}
