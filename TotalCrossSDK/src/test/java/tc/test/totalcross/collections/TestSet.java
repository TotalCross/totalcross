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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import totalcross.unit.TestCase;

public class TestSet extends TestCase {
  @Override
  public void testRun() {
    Test8 test1 = new Test8();
    Set test2 = new Test8();

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());

    assertFalse(test1.isEmpty());
    assertFalse(test2.isEmpty());

    assertFalse(test1.contains(null));
    assertFalse(test2.contains(null));

    assertEquals(null, test1.iterator());
    assertEquals(null, test2.iterator());

    assertEquals(null, test1.toArray());
    assertEquals(null, test2.toArray());

    assertEquals(null, test1.toArray(null));
    assertEquals(null, test2.toArray(null));

    assertFalse(test1.add(null));
    assertFalse(test2.add(null));

    assertFalse(test1.remove(null));
    assertFalse(test2.remove(null));

    assertFalse(test1.containsAll(null));
    assertFalse(test2.containsAll(null));

    assertFalse(test1.addAll(null));
    assertFalse(test2.addAll(null));

    assertFalse(test1.retainAll(null));
    assertFalse(test2.retainAll(null));

    assertFalse(test1.removeAll(null));
    assertFalse(test2.removeAll(null));

    try {
      test1.clear();
    } catch (Throwable throwable) {
      fail("1");
    }
    try {
      test2.clear();
    } catch (Throwable throwable) {
      fail("2");
    }
  }
}

class Test8 implements Set {
  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean contains(Object o) {
    return false;
  }

  @Override
  public Iterator iterator() {
    return null;
  }

  @Override
  public Object[] toArray() {
    return null;
  }

  @Override
  public Object[] toArray(Object[] a) {
    return null;
  }

  @Override
  public boolean add(Object e) {
    return false;
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean containsAll(Collection c) {
    return false;
  }

  @Override
  public boolean addAll(Collection c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection c) {
    return false;
  }

  @Override
  public void clear() {
  }
}
