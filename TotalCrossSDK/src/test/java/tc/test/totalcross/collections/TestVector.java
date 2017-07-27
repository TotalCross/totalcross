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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import totalcross.unit.TestCase;

public class TestVector extends TestCase
{
  @Override
  public void testRun()
  {
    Test35 test1 = new Test35();
    Vector test2 = new Test35(test1);
    AbstractList test3 = new Test35(11, 0);

    Object[] strings = {};
    test1.copyInto(strings);
    assertTrue(Arrays.equals(test1.toArray(), strings));
    test2.copyInto(strings);
    assertTrue(Arrays.equals(test2.toArray(), strings));
    ((Vector)test3).copyInto(strings);
    assertTrue(Arrays.equals(test3.toArray(), strings));

    test1.trimToSize();
    test2.trimToSize();
    ((Vector)test3).trimToSize();

    test1.ensureCapacity(12);
    test2.ensureCapacity(12);
    ((Vector)test3).ensureCapacity(12);

    test1.setSize(13);
    test2.setSize(13);
    ((Vector)test3).setSize(13);

    assertEquals(24, test1.capacity());
    assertEquals(24, test2.capacity());
    assertEquals(24, ((Vector)test3).capacity());

    assertEquals(13, test1.size());
    assertEquals(13, test2.size());
    assertEquals(13, ((Vector)test3).size());

    assertFalse(test1.isEmpty());
    assertFalse(test2.isEmpty());
    assertFalse(test3.isEmpty());

    Enumeration enum1 = test1.elements();
    Enumeration enum2 = test2.elements();
    Enumeration enum3 = ((Vector)test3).elements();

    assertTrue(enum1.hasMoreElements());
    assertTrue(enum2.hasMoreElements());
    assertTrue(enum3.hasMoreElements());

    assertEquals(null, enum1.nextElement());
    assertEquals(null, enum2.nextElement());
    assertEquals(null, enum3.nextElement());

    assertTrue(test1.contains(null));
    assertTrue(test2.contains(null));
    assertTrue(test3.contains(null));

    assertEquals(0, test1.indexOf(null));
    assertEquals(0, test2.indexOf(null));
    assertEquals(0, test3.indexOf(null));

    assertEquals(0, test1.indexOf(null, 0));
    assertEquals(0, test2.indexOf(null, 0));
    assertEquals(0, ((Vector)test3).indexOf(null, 0));

    assertEquals(12, test1.lastIndexOf(null));
    assertEquals(12, test2.lastIndexOf(null));
    assertEquals(12, test3.lastIndexOf(null));

    assertEquals(12, test1.lastIndexOf(null, 12));
    assertEquals(12, test2.lastIndexOf(null, 12));
    assertEquals(12, ((Vector)test3).lastIndexOf(null, 12));

    assertEquals(null, test1.elementAt(0));
    assertEquals(null, test2.elementAt(0));
    assertEquals(null, ((Vector)test3).elementAt(0));

    assertEquals(null, test1.firstElement());
    assertEquals(null, test2.firstElement());
    assertEquals(null, ((Vector)test3).firstElement());

    assertEquals(null, test1.lastElement());
    assertEquals(null, test2.lastElement());
    assertEquals(null, ((Vector)test3).lastElement());

    test1.setElementAt("0", 0);
    test2.setElementAt("0", 0);
    ((Vector)test3).setElementAt("0", 0);

    test1.removeElementAt(0);
    test2.removeElementAt(0);
    ((Vector)test3).removeElementAt(0);

    test1.insertElementAt("0", 0);
    test2.insertElementAt("0", 0);
    ((Vector)test3).insertElementAt("0", 0);

    test1.addElement("0");
    test2.addElement("0");
    ((Vector)test3).addElement("0");

    assertTrue(test1.removeElement("0"));
    assertTrue(test2.removeElement("0"));
    assertTrue(((Vector)test3).removeElement("0"));

    test1.removeAllElements();
    test2.removeAllElements();
    ((Vector)test3).removeAllElements();

    assertTrue(test1.equals(test1.clone()));
    assertTrue(test2.equals(test2.clone()));
    assertTrue(test3.equals(((Vector)test3).clone()));

    assertTrue(Arrays.equals(strings, test1.toArray()));
    assertTrue(Arrays.equals(strings, test2.toArray()));
    assertTrue(Arrays.equals(strings, test3.toArray()));

    assertEquals(0, test1.toArray(strings).length);
    assertEquals(0, test2.toArray(strings).length);
    assertEquals(0, test3.toArray(strings).length);

    try
    {
      test1.get(0);
      fail("1");
    }
    catch (ArrayIndexOutOfBoundsException exception) {}
    try
    {
      test2.get(0);
      fail("2");
    }
    catch (ArrayIndexOutOfBoundsException exception) {}
    try
    {
      test3.get(0);
      fail("3");
    }
    catch (ArrayIndexOutOfBoundsException exception) {}

    try
    {
      test1.set(0, "0");
      fail("4");
    }
    catch (ArrayIndexOutOfBoundsException exception) {}
    try
    {
      test2.set(0, "0");
      fail("5");
    }
    catch (ArrayIndexOutOfBoundsException exception) {}
    try
    {
      test3.set(0, "0");
      fail("6");
    }
    catch (ArrayIndexOutOfBoundsException exception) {}

    assertTrue(test1.add("0"));
    assertTrue(test2.add("0"));
    assertTrue(test3.add("0"));

    assertTrue(test1.remove("0"));
    assertTrue(test2.remove("0"));
    assertTrue(test3.remove("0"));

    test1.add(0, "0");
    test2.add(0, "0");
    test3.add(0, "0");

    assertEquals("0", test1.remove(0));
    assertEquals("0", test2.remove(0));
    assertEquals("0", test3.remove(0));

    test1.clear();
    test2.clear();
    test3.clear();

    assertTrue(test1.containsAll(test2));
    assertTrue(test2.containsAll(test3));
    assertTrue(test3.containsAll(test1));

    assertFalse(test1.addAll(test2));
    assertFalse(test2.addAll(test3));
    assertFalse(test3.addAll(test1));

    assertFalse(test1.removeAll(test2));
    assertFalse(test2.removeAll(test3));
    assertFalse(test3.removeAll(test1));

    assertFalse(test1.retainAll(test2));
    assertFalse(test2.retainAll(test3));
    assertFalse(test3.retainAll(test1));

    assertFalse(test1.addAll(0, test2));
    assertFalse(test2.addAll(0, test3));
    assertFalse(test3.addAll(0, test1));

    assertEquals(1, test1.hashCode());
    assertEquals(1, test2.hashCode());
    assertEquals(1, test3.hashCode());

    assertEquals("[]", test1.toString());
    assertEquals("[]", test2.toString());
    assertEquals("[]", test3.toString());

    assertTrue(test1.subList(0, 0) instanceof List);
    assertTrue(test2.subList(0, 0) instanceof List);
    assertTrue(test3.subList(0, 0) instanceof List);
  }
}

class Test35 extends Vector
{
  public Test35()
  {
    super();
  }
  public Test35(Collection c)
  {
    super(c);
  }
  public Test35(int initialCapacity, int capacityIncrement)
  {
    super(initialCapacity, capacityIncrement);
  }
  public Test35(int initialCapacity)
  {
    super(initialCapacity);
  }
}
