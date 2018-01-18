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
import java.util.Collection;
import java.util.Iterator;

import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestAbstractCollection extends TestCase {
  @Override
  public void testRun() {
    Test11 test1 = new Test11();
    AbstractCollection test2 = new Test11();
    Collection test3 = new Test11();

    assertEquals(null, test1.iterator());
    assertEquals(null, test2.iterator());
    assertEquals(null, test3.iterator());

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    try {
      test1.add(null);
      fail("1");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      test2.add(null);
      fail("2");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      test3.add(null);
      fail("3");
    } catch (UnsupportedOperationException exception) {
    }

    try {
      assertFalse(test1.addAll(test1));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test2.addAll(test2));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test3.addAll(test3));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }

    try {
      test1.clear();
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      test2.clear();
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      test3.clear();
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }

    try {
      assertFalse(test1.contains(null));
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test2.contains(null));
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test3.contains(null));
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }

    try {
      test1.containsAll(null);
      fail("4");
    } catch (NullPointerException exception) {
    }
    try {
      test2.containsAll(null);
      fail("5");
    } catch (NullPointerException exception) {
    }
    try {
      test3.containsAll(null);
      fail("6");
    } catch (NullPointerException exception) {
    }

    assertTrue(test1.isEmpty());
    assertTrue(test2.isEmpty());
    assertTrue(test3.isEmpty());

    try {
      assertFalse(test1.remove(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test2.remove(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test3.remove(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }

    try {
      assertFalse(test1.removeAll(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test2.removeAll(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test3.removeAll(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }

    try {
      assertFalse(test1.retainAll(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test2.retainAll(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertFalse(test3.retainAll(null));
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }

    try {
      assertEquals(0, test1.toArray().length);
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertEquals(0, test2.toArray().length);
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertEquals(0, test3.toArray().length);
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }

    Object[] object = new Object[0];
    try {
      assertEquals(0, test1.toArray(object).length);
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertEquals(0, test2.toArray(object).length);
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }
    try {
      assertEquals(0, test3.toArray(object).length);
      assertFalse(Settings.onJavaSE);
    } catch (NullPointerException exception) {
      assertTrue(Settings.onJavaSE);
    }

    try {
      test1.toString();
      fail("7");
    } catch (NullPointerException exception) {
    }
    try {
      test2.toString();
      fail("8");
    } catch (NullPointerException exception) {
    }
    try {
      test3.toString();
      fail("9");
    } catch (NullPointerException exception) {
    }

  }
}

class Test11 extends AbstractCollection {
  @Override
  public Iterator iterator() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }
}