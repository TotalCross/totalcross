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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestTreeSet extends TestCase
{
  @Override
  public void testRun()
  {
    Test31 test1 = new Test31();
    TreeSet test2 = new Test31((Comparator)null);
    AbstractSet test3 = new Test31(test2);

    assertTrue(test1.add("a"));
    assertTrue(test2.add("a"));
    assertTrue(test3.add("a"));

    assertFalse(test1.addAll(test2));
    assertFalse(test2.addAll(test3));
    assertFalse(test3.addAll(test1));

    test1.clear();
    test2.clear();
    test3.clear();

    assertEquals(test1, test1.clone());
    assertEquals(test2, test2.clone());
    assertEquals(test3, ((TreeSet)test3).clone());

    assertEquals(null, test1.comparator());
    assertEquals(null, test2.comparator());
    assertEquals(null, ((TreeSet)test3).comparator());

    assertFalse(test1.contains("a"));
    assertFalse(test2.contains("a"));
    assertFalse(test3.contains("a"));

    try
    {
      test1.first();
      fail("1");
    }
    catch (NoSuchElementException exception) {}
    try
    {
      test2.first();
      fail("2");
    }
    catch (NoSuchElementException exception) {}
    try
    {
      ((TreeSet)test3).first();
      fail("3");
    }
    catch (NoSuchElementException exception) {}

    assertEquals(Collections.EMPTY_SET, test1.headSet("a"));
    assertEquals(Collections.EMPTY_SET, test2.headSet("a"));
    assertEquals(Collections.EMPTY_SET, ((TreeSet)test3).headSet("a"));

    assertEquals(Collections.EMPTY_SET, test1.headSet("a", false));
    assertEquals(Collections.EMPTY_SET, test2.headSet("a", false));
    assertEquals(Collections.EMPTY_SET, ((TreeSet)test3).headSet("a", false));

    assertTrue(test1.isEmpty());
    assertTrue(test2.isEmpty());
    assertTrue(test3.isEmpty());

    assertTrue(test1.iterator() instanceof Iterator);
    assertTrue(test2.iterator() instanceof Iterator);
    assertTrue(test3.iterator() instanceof Iterator);

    try
    {
      test1.last();
      fail("4");
    }
    catch (NoSuchElementException exception) {}
    try
    {
      test2.last();
      fail("5");
    }
    catch (NoSuchElementException exception) {}
    try
    {
      ((TreeSet)test3).last();
      fail("6");
    }
    catch (NoSuchElementException exception) {}

    assertFalse(test1.remove("a"));
    assertFalse(test2.remove("a"));
    assertFalse(test3.remove("a"));

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    assertEquals(Collections.EMPTY_SET, test1.subSet("a", "b"));
    assertEquals(Collections.EMPTY_SET, test2.subSet("a", "b"));
    assertEquals(Collections.EMPTY_SET, ((TreeSet)test3).subSet("a", "b"));

    assertEquals(Collections.EMPTY_SET, test1.subSet("a", true, "b", false));
    assertEquals(Collections.EMPTY_SET, test2.subSet("a", true, "b", false));
    assertEquals(Collections.EMPTY_SET, ((TreeSet)test3).subSet("a", true, "b", false));

    assertEquals(Collections.EMPTY_SET, test1.tailSet("a"));
    assertEquals(Collections.EMPTY_SET, test2.tailSet("a"));
    assertEquals(Collections.EMPTY_SET, ((TreeSet)test3).tailSet("a"));

    assertEquals(Collections.EMPTY_SET, test1.tailSet("a", true));
    assertEquals(Collections.EMPTY_SET, test2.tailSet("a", true));
    assertEquals(Collections.EMPTY_SET, ((TreeSet)test3).tailSet("a", true));

    assertEquals(null, test1.ceiling("a"));
    assertEquals(null, test2.ceiling("a"));
    assertEquals(null, ((TreeSet)test3).ceiling("a"));

    try
    {
      assertTrue(test1.descendingIterator() instanceof Iterator);
      if (!Settings.onJavaSE) {
        fail("7");
      }
    }
    catch (NoSuchElementException exception) {}
    try
    {   
      assertTrue(test2.descendingIterator() instanceof Iterator);
      if (!Settings.onJavaSE) {
        fail("8");
      }
    }
    catch (NoSuchElementException exception) {}
    try
    {   
      assertTrue(((TreeSet)test3).descendingIterator() instanceof Iterator);
      if (!Settings.onJavaSE) {
        fail("9");
      }
    }
    catch (NoSuchElementException exception) {}

    assertEquals(Collections.EMPTY_SET, test1.descendingSet());
    assertEquals(Collections.EMPTY_SET, test2.descendingSet());
    assertEquals(Collections.EMPTY_SET, ((TreeSet)test3).descendingSet());

    assertEquals(null, test1.floor("a"));
    assertEquals(null, test2.floor("a"));
    assertEquals(null, ((TreeSet)test3).floor("a"));

    assertEquals(null, test1.higher("a"));
    assertEquals(null, test2.higher("a"));
    assertEquals(null, ((TreeSet)test3).higher("a"));

    assertEquals(null, test1.lower("a"));
    assertEquals(null, test2.lower("a"));
    assertEquals(null, ((TreeSet)test3).lower("a"));

    try
    {
      assertEquals(null, test1.pollFirst());
      if (!Settings.onJavaSE) {
        fail("10");
      }
    }
    catch (NullPointerException exception) {}
    try
    {
      assertEquals(null, test2.pollFirst());
      if (!Settings.onJavaSE) {
        fail("11");
      }
    }
    catch (NullPointerException exception) {}
    try
    {
      assertEquals(null, ((TreeSet)test3).pollFirst());
      if (!Settings.onJavaSE) {
        fail("12");
      }
    }
    catch (NullPointerException exception) {}

    try
    {
      assertEquals(null, test1.pollLast());
      if (!Settings.onJavaSE) {
        fail("13");
      }
    }
    catch (NullPointerException exception) {}
    try
    {
      assertEquals(null, test2.pollLast());
      if (!Settings.onJavaSE) {
        fail("14");
      }
    }
    catch (NullPointerException exception) {}
    try
    {
      assertEquals(null, ((TreeSet)test3).pollLast());
      if (!Settings.onJavaSE) {
        fail("15");
      }
    }
    catch (NullPointerException exception) {}
  }
}

class Test31 extends TreeSet implements Cloneable
{
  public Test31()
  {
    super();
  }
  public Test31(Comparator c)
  {
    super(c);
  }
  public Test31(Collection c)
  {
    super(c);
  }
  public Test31(SortedSet s)
  {
    super(s);
  }
}
