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

import java.util.AbstractCollection;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

import totalcross.unit.TestCase;

public class TestAbstractQueue extends TestCase {
  @Override
  public void testRun() {
    Test27 test1 = new Test27();
    AbstractQueue test2 = new Test27();
    AbstractCollection test3 = new Test27();

    assertFalse(test1.offer(null));
    assertFalse(test2.offer(null));
    assertFalse(((AbstractQueue) test3).offer(null));

    assertEquals(null, test1.poll());
    assertEquals(null, test2.poll());
    assertEquals(null, ((AbstractQueue) test3).poll());

    assertEquals(null, test1.peek());
    assertEquals(null, test2.peek());
    assertEquals(null, ((AbstractQueue) test3).peek());

    assertEquals(null, test1.iterator());
    assertEquals(null, test2.iterator());
    assertEquals(null, test3.iterator());

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    try {
      test1.add(null);
      fail("1");
    } catch (IllegalStateException exception) {
    }
    try {
      test2.add(null);
      fail("2");
    } catch (IllegalStateException exception) {
    }
    try {
      test3.add(null);
      fail("3");
    } catch (IllegalStateException exception) {
    }

    try {
      test1.remove();
      fail("4");
    } catch (NoSuchElementException exception) {
    }
    try {
      test2.remove();
      fail("5");
    } catch (NoSuchElementException exception) {
    }
    try {
      ((AbstractQueue) test3).remove();
      fail("6");
    } catch (NoSuchElementException exception) {
    }

    try {
      test1.element();
      fail("7");
    } catch (NoSuchElementException exception) {
    }
    try {
      test2.element();
      fail("8");
    } catch (NoSuchElementException exception) {
    }
    try {
      ((AbstractQueue) test3).element();
      fail("9");
    } catch (NoSuchElementException exception) {
    }

    test1.clear();
    test2.clear();
    test3.clear();

    try {
      test1.addAll(null);
      fail("10");
    } catch (NullPointerException exception) {
    }
    try {
      test2.addAll(null);
      fail("11");
    } catch (NullPointerException exception) {
    }
    try {
      test3.addAll(null);
      fail("12");
    } catch (NullPointerException exception) {
    }
  }

}

class Test27 extends AbstractQueue {
  @Override
  public boolean offer(Object e) {
    return false;
  }

  @Override
  public Object poll() {
    return null;
  }

  @Override
  public Object peek() {
    return null;
  }

  @Override
  public Iterator iterator() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }
}