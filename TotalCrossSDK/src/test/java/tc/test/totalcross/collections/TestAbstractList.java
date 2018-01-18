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
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestAbstractList extends TestCase {
  @Override
  public void testRun() {
    Test12 test1 = new Test12();
    AbstractList test2 = new Test12();
    AbstractCollection test3 = new Test12();

    assertEquals(null, test1.get(0));
    assertEquals(null, test2.get(0));
    assertEquals(null, ((AbstractList) test3).get(0));

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    try {
      test1.add(0, null);
      fail("1");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      test2.add(0, null);
      fail("2");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      ((AbstractList) test3).add(0, null);
      fail("3");
    } catch (UnsupportedOperationException exception) {
    }

    try {
      test1.add(null);
      fail("4");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      test2.add(null);
      fail("5");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      test3.add(null);
      fail("6");
    } catch (UnsupportedOperationException exception) {
    }

    assertFalse(test1.addAll(0, test1));
    assertFalse(test2.addAll(0, test2));
    assertFalse(((AbstractList) test3).addAll(0, test3));

    try {
      test1.clear();
    } catch (Throwable throwable) {
      fail("7");
    }
    try {
      test2.clear();
    } catch (Throwable throwable) {
      fail("8");
    }
    try {
      test3.clear();
    } catch (Throwable throwable) {
      fail("9");
    }

    assertTrue(test1.equals(test1));
    assertTrue(test2.equals(test2));
    assertTrue(test3.equals(test3));
    assertTrue(test1.equals(test2));
    assertTrue(test2.equals(test3));
    assertTrue(test3.equals(test1));

    assertEquals(test1.hashCode(), test2.hashCode());
    assertEquals(test2.hashCode(), test3.hashCode());
    assertEquals(test1.hashCode(), test3.hashCode());

    assertEquals(-1, test1.indexOf(null));
    assertEquals(-1, test2.indexOf(null));
    assertEquals(-1, ((AbstractList) test3).indexOf(null));

    Iterator iterator1 = test1.iterator();
    Iterator iterator2 = test1.iterator();
    Iterator iterator3 = test1.iterator();
    assertTrue(iterator1 instanceof Iterator);
    assertTrue(iterator2 instanceof Iterator);
    assertTrue(iterator3 instanceof Iterator);

    assertFalse(iterator1.hasNext());
    assertFalse(iterator2.hasNext());
    assertFalse(iterator3.hasNext());

    try {
      assertEquals(null, iterator1.next());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      assertEquals(null, iterator2.next());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      assertEquals(null, iterator3.next());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }

    try {
      iterator1.remove();
      fail("10");
    } catch (Throwable throwable) {
    }
    try {
      iterator2.remove();
      fail("11");
    } catch (Throwable throwable) {
    }
    try {
      iterator3.remove();
      fail("12");
    } catch (Throwable throwable) {
    }

    assertEquals(-1, test1.lastIndexOf(null));
    assertEquals(-1, test2.lastIndexOf(null));
    assertEquals(-1, ((AbstractList) test3).lastIndexOf(null));

    assertTrue(test1.listIterator() instanceof ListIterator);
    assertTrue(test2.listIterator() instanceof ListIterator);
    assertTrue(((AbstractList) test3).listIterator() instanceof ListIterator);

    ListIterator listIterator1 = test1.listIterator(0);
    ListIterator listIterator2 = test2.listIterator(0);
    ListIterator listIterator3 = ((AbstractList) test3).listIterator(0);

    assertFalse(listIterator1.hasNext());
    assertFalse(listIterator2.hasNext());
    assertFalse(listIterator3.hasNext());

    assertFalse(listIterator1.hasPrevious());
    assertFalse(listIterator2.hasPrevious());
    assertFalse(listIterator3.hasPrevious());

    try {
      assertEquals(null, listIterator1.next());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      assertEquals(null, listIterator2.next());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      assertEquals(null, listIterator3.next());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }

    try {
      assertEquals(null, listIterator1.previous());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      assertEquals(null, listIterator2.previous());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      assertEquals(null, listIterator3.previous());
      assertTrue(Settings.onJavaSE);
    } catch (NoSuchElementException exception) {
      assertFalse(Settings.onJavaSE);
    }

    assertEquals(0, listIterator1.nextIndex());
    assertEquals(0, listIterator2.nextIndex());
    assertEquals(0, listIterator3.nextIndex());

    assertEquals(-1, listIterator1.previousIndex());
    assertEquals(-1, listIterator2.previousIndex());
    assertEquals(-1, listIterator3.previousIndex());

    try {
      listIterator1.remove();
    } catch (UnsupportedOperationException exception) {
      assertTrue(Settings.onJavaSE);
    } catch (IllegalStateException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      listIterator2.remove();
    } catch (UnsupportedOperationException exception) {
      assertTrue(Settings.onJavaSE);
    } catch (IllegalStateException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      listIterator3.remove();
    } catch (UnsupportedOperationException exception) {
      assertTrue(Settings.onJavaSE);
    } catch (IllegalStateException exception) {
      assertFalse(Settings.onJavaSE);
    }

    try {
      listIterator1.set(null);
    } catch (UnsupportedOperationException exception) {
      assertTrue(Settings.onJavaSE);
    } catch (IllegalStateException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      listIterator2.set(null);
    } catch (UnsupportedOperationException exception) {
      assertTrue(Settings.onJavaSE);
    } catch (IllegalStateException exception) {
      assertFalse(Settings.onJavaSE);
    }
    try {
      listIterator3.set(null);
    } catch (UnsupportedOperationException exception) {
      assertTrue(Settings.onJavaSE);
    } catch (IllegalStateException exception) {
      assertFalse(Settings.onJavaSE);
    }

    try {
      listIterator1.add(null);
      fail("13");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      listIterator2.add(null);
      fail("14");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      listIterator3.add(null);
      fail("15");
    } catch (UnsupportedOperationException exception) {
    }

    try {
      test1.add(null);
      fail("16");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      test2.add(null);
      fail("17");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      test3.add(null);
      fail("18");
    } catch (UnsupportedOperationException exception) {
    }

    try {
      test1.set(0, null);
      fail("19");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      test2.set(0, null);
      fail("20");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      ((AbstractList) test3).set(0, null);
      fail("21");
    } catch (UnsupportedOperationException exception) {
    }

    assertTrue(test1.subList(0, 0) instanceof List);
    assertTrue(test2.subList(0, 0) instanceof List);
    assertTrue(((AbstractList) test3).subList(0, 0) instanceof List);

    try {
      assertEquals(test1, test1.clone());
    } catch (CloneNotSupportedException e) {
      fail("23");
    }
    try {
      assertEquals(test2, ((Test12) test2).clone());
    } catch (CloneNotSupportedException e) {
      fail("24");
    }
    try {
      assertEquals(test3, ((Test12) test3).clone());
    } catch (CloneNotSupportedException e) {
      fail("25");
    }

  }
}

class Test12 extends AbstractList implements Cloneable {
  @Override
  public Object get(int index) {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
