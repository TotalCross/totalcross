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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import totalcross.unit.TestCase;

public class TestIdentityHashMap extends TestCase {
  @Override
  public void testRun() {
    Test33 test1 = new Test33();
    IdentityHashMap test2 = new Test33(22);
    AbstractMap test3 = new Test33(test2);

    test1.clear();
    test2.clear();
    test3.clear();

    assertEquals(test1, test1.clone());
    assertEquals(test2, test2.clone());
    assertEquals(test3, ((Test33) test3).clone());

    assertFalse(test1.containsKey("a"));
    assertFalse(test2.containsKey("a"));
    assertFalse(test3.containsKey("a"));

    assertFalse(test1.containsValue("b"));
    assertFalse(test2.containsValue("b"));
    assertFalse(test3.containsValue("b"));

    Set set1 = test1.entrySet();
    Set set2 = test2.entrySet();
    Set set3 = test3.entrySet();

    assertEquals(0, set1.size());
    assertEquals(0, set2.size());
    assertEquals(0, set3.size());

    Iterator iterator1 = set1.iterator();
    Iterator iterator2 = set2.iterator();
    Iterator iterator3 = set3.iterator();

    set1.clear();
    set2.clear();
    set3.clear();

    assertFalse(set1.contains("a"));
    assertFalse(set2.contains("a"));
    assertFalse(set3.contains("a"));

    assertEquals(0, set1.hashCode());
    assertEquals(0, set2.hashCode());
    assertEquals(0, set3.hashCode());

    assertFalse(set1.remove("a"));
    assertFalse(set2.remove("a"));
    assertFalse(set3.remove("a"));

    assertTrue(test1.equals(test2));
    assertTrue(test2.equals(test3));
    assertTrue(test3.equals(test1));

    assertEquals(null, test1.get("a"));
    assertEquals(null, test2.get("a"));
    assertEquals(null, test3.get("a"));

    assertEquals(0, test1.hashCode());
    assertEquals(0, test2.hashCode());
    assertEquals(0, test3.hashCode());

    assertTrue(test1.isEmpty());
    assertTrue(test2.isEmpty());
    assertTrue(test3.isEmpty());

    assertEquals(0, (set1 = test1.keySet()).size());
    assertEquals(0, (set2 = test2.keySet()).size());
    assertEquals(0, (set3 = test3.keySet()).size());

    assertTrue(set1.iterator() instanceof Iterator);
    assertTrue(set2.iterator() instanceof Iterator);
    assertTrue(set3.iterator() instanceof Iterator);

    set1.clear();
    set2.clear();
    set3.clear();

    assertFalse(set1.contains("a"));
    assertFalse(set2.contains("a"));
    assertFalse(set3.contains("a"));

    assertEquals(0, set1.hashCode());
    assertEquals(0, set2.hashCode());
    assertEquals(0, set3.hashCode());

    assertFalse(set1.remove("a"));
    assertFalse(set2.remove("a"));
    assertFalse(set3.remove("a"));

    assertEquals(null, test1.put("a", "b"));
    assertEquals(null, test2.put("a", "b"));
    assertEquals(null, test3.put("a", "b"));

    test1.putAll(test2);
    test2.putAll(test3);
    test3.putAll(test1);

    assertEquals("b", test1.remove("a"));
    assertEquals("b", test2.remove("a"));
    assertEquals("b", test3.remove("a"));

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    Collection values1 = test1.values();
    Collection values2 = test2.values();
    Collection values3 = test3.values();

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    assertTrue(values1.iterator() instanceof Iterator);
    assertTrue(values2.iterator() instanceof Iterator);
    assertTrue(values3.iterator() instanceof Iterator);

    values1.clear();
    values2.clear();
    values3.clear();

    assertFalse(values1.remove("b"));
    assertFalse(values2.remove("b"));
    assertFalse(values3.remove("b"));

    assertFalse(iterator1.hasNext());
    assertFalse(iterator2.hasNext());
    assertFalse(iterator3.hasNext());

    try {
      iterator1.next();
      fail("1");
    } catch (ConcurrentModificationException exception) {
    }
    try {
      iterator2.next();
      fail("2");
    } catch (ConcurrentModificationException exception) {
    }
    try {
      iterator3.next();
      fail("3");
    } catch (ConcurrentModificationException exception) {
    }

    try {
      iterator1.remove();
      fail("4");
    } catch (RuntimeException exception) {
    }
    try {
      iterator2.remove();
      fail("5");
    } catch (RuntimeException exception) {
    }
    try {
      iterator3.remove();
      fail("6");
    } catch (RuntimeException exception) {
    }
  }
}

class Test33 extends IdentityHashMap implements Cloneable {
  public Test33() {
    super();
  }

  public Test33(int max) {
    super(max);
  }

  public Test33(Map m) {
    super(m);
  }
}
