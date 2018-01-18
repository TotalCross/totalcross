/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.util;

import totalcross.unit.TestCase;
import totalcross.util.ElementNotFoundException;
import totalcross.util.Vector;

public class VectorTest extends TestCase {
  private void Vector_Int() {
    Vector v = new Vector(10);
    assertEquals(10, v.items.length);
  }

  private void Vector_Obj() {
    String[] s = { "one", "two" };
    Vector v = new Vector(s);
    assertTrue(v.items == s);
    v.addElement("three"); // force the creation of a new array
    assertFalse(v.items == s);
  }

  private void insert(/*int index, Object obj*/) {
    Vector v = new Vector(3);
    v.addElement("one");
    v.addElement("three");
    v.insertElementAt("two", 1);
    assertEquals(3, v.size());
    assertEquals(v.items[1], "two");
  }

  private void del_Int(/*int index*/) {
    Vector v = new Vector(3);
    v.addElement("one");
    v.addElement("two");
    v.removeElementAt(0);
    v.removeElementAt(1); // invalid index
    assertEquals(1, v.size());
    v.removeElementAt(0);
    assertEquals(0, v.size());
  }

  private void /*boolean*/ del_Obj(/*Object obj*/) {
    Vector v = new Vector(3);
    v.addElement("one");
    v.addElement("two");
    assertFalse(v.removeElement("One"));
    assertTrue(v.removeElement("one"));
    assertFalse(v.removeElement("one"));
    assertTrue(v.removeElement("two"));
    assertEquals(0, v.size());
  }

  private void /*int*/ find(/*Object obj, int startIndex*/) {
    Vector v = new Vector(3);
    v.addElement("one");
    v.addElement("two");
    assertEquals(-1, v.indexOf("one", 1));
    assertEquals(-1, v.indexOf("One", 0));
    assertEquals(1, v.indexOf("two", 1));
  }

  private void /*Object []*/ toObjectArray() {
    Vector v = new Vector(3);
    v.addElement("one");
    v.addElement("two");
    v.addElement("three");
    v.addElement("four");
    v.addElement("five");
    v.addElement("six");
    assertNotEquals(6, v.items.length); // space for 7 items
    Object[] o = v.toObjectArray();
    assertTrue(o instanceof String[]);
    assertEquals(6, o.length);
  }

  private void push_pop_peek(/*Object obj*/) {
    Vector v = new Vector(5);
    v.push("one");
    v.push("two");
    v.push("three");
    assertEquals(3, v.size());
    try {
      assertEquals("three", v.peek());
    } catch (ElementNotFoundException e) {
      fail();
    }
    assertEquals(3, v.size()); // make sure that peek does not remove the element
    try {
      assertEquals("three", v.pop());
    } catch (ElementNotFoundException e) {
      fail();
    }
    try {
      assertEquals("two", v.pop());
    } catch (ElementNotFoundException e) {
      fail();
    }
    try {
      assertEquals("one", v.pop());
    } catch (ElementNotFoundException e) {
      fail();
    }
    try {
      assertNull(v.pop());
      fail();
    } catch (ElementNotFoundException e) {
    }
    try {
      assertNull(v.peek());
      fail();
    } catch (ElementNotFoundException e) {
    }
  }

  private void clear() {
    Vector v = new Vector(3);
    v.addElement("one");
    v.addElement("two");
    assertEquals(2, v.size());
    v.removeAllElements();
    assertEquals(0, v.size());
    assertEquals(null, v.items[0]);
  }

  private void /*boolean*/ qsort() {
    Vector v = new Vector(3);
    v.addElement("two");
    v.addElement("one");
    v.qsort();
    assertEquals("one", v.items[0]);
    assertEquals("two", v.items[1]);
  }

  @Override
  public void testRun() {
    Vector_Int();
    Vector_Obj();
    insert();
    del_Int();
    del_Obj();
    find();
    toObjectArray();
    push_pop_peek();
    clear();
    qsort();
  }
}
