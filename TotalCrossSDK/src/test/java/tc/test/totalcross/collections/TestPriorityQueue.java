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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.SortedSet;

import totalcross.unit.TestCase;

public class TestPriorityQueue extends TestCase {
  @Override
  public void testRun() {
    Test30 test1 = new Test30();
    PriorityQueue test2 = new Test30(test1);
    AbstractQueue test3 = new Test30(10);

    test1.clear();
    test2.clear();
    test3.clear();

    assertEquals(null, test1.comparator());
    assertEquals(null, test2.comparator());
    assertEquals(null, ((PriorityQueue) test3).comparator());

    Iterator it1 = test1.iterator();
    Iterator it2 = test2.iterator();
    Iterator it3 = test3.iterator();

    assertTrue(it1 instanceof Iterator);
    assertTrue(it2 instanceof Iterator);
    assertTrue(it3 instanceof Iterator);

    assertFalse(it1.hasNext());
    assertFalse(it2.hasNext());
    assertFalse(it3.hasNext());

    try {
      it1.next();
      fail("1");
    } catch (RuntimeException exception) {
    }
    try {
      it2.next();
      fail("2");
    } catch (RuntimeException exception) {
    }
    try {
      it3.next();
      fail("3");
    } catch (RuntimeException exception) {
    }

    try {
      it1.remove();
      fail("4");
    } catch (RuntimeException exception) {
    }
    try {
      it2.remove();
      fail("5");
    } catch (RuntimeException exception) {
    }
    try {
      it3.remove();
      fail("6");
    } catch (RuntimeException exception) {
    }

    assertTrue(test1.offer("a"));
    assertTrue(test2.offer("b"));
    assertTrue(test3.offer("c"));

    assertEquals("a", test1.peek());
    assertEquals("b", test2.peek());
    assertEquals("c", test3.peek());

    assertEquals("a", test1.poll());
    assertEquals("b", test2.poll());
    assertEquals("c", test3.poll());

    assertFalse(test1.remove("a"));
    assertFalse(test2.remove("b"));
    assertFalse(test3.remove("c"));

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    assertFalse(test1.addAll(test2));
    assertFalse(test2.addAll(test3));
    assertFalse(test3.addAll(test1));
  }
}

class Test30 extends PriorityQueue {
  public Test30() {
    super();
  }

  public Test30(Collection c) {
    super(c);
  }

  public Test30(int cap) {
    super(cap);
  }

  public Test30(int cap, Comparator comp) {
    super(cap, comp);
  }

  public Test30(PriorityQueue q) {
    super(q);
  }

  public Test30(SortedSet s) {
    super(s);
  }
}
