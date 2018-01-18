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

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;

import totalcross.unit.TestCase;

public class TestStack extends TestCase {
  @Override
  public void testRun() {
    Test36 test1 = new Test36();
    Stack test2 = new Test36();
    Vector test3 = new Test36();

    assertEquals("a", test1.push("a"));
    assertEquals("a", test2.push("a"));
    assertEquals("a", ((Stack) test3).push("a"));

    assertEquals("a", test1.pop());
    assertEquals("a", test2.pop());
    assertEquals("a", ((Stack) test3).pop());

    try {
      test1.peek();
      fail("1");
    } catch (EmptyStackException exception) {
    }
    try {
      test2.peek();
      fail("2");
    } catch (EmptyStackException exception) {
    }
    try {
      ((Stack) test3).peek();
      fail("3");
    } catch (EmptyStackException exception) {
    }

    assertTrue(test1.isEmpty());
    assertTrue(test2.isEmpty());
    assertTrue(test3.isEmpty());

    assertEquals(-1, test1.search("a"));
    assertEquals(-1, test2.search("a"));
    assertEquals(-1, ((Stack) test3).search("a"));
  }
}

class Test36 extends Stack {
}
