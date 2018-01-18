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
import java.util.EnumSet;
import java.util.Iterator;

import totalcross.unit.TestCase;

public class TestEnumSet extends TestCase {
  @Override
  public void testRun() {
    EnumSet test1 = EnumSet.allOf(TestEnum2.class);
    EnumSet test2 = EnumSet.noneOf(TestEnum2.class);
    EnumSet test3 = EnumSet.complementOf(test1);
    EnumSet test4 = EnumSet.of(TestEnum2.One);
    EnumSet test5 = EnumSet.of(TestEnum2.One, TestEnum2.Two);
    EnumSet test6 = EnumSet.of(TestEnum2.One, TestEnum2.Two, TestEnum2.Three);
    EnumSet test7 = EnumSet.of(TestEnum2.One, TestEnum2.Two, TestEnum2.Three, TestEnum2.Four);
    EnumSet test8 = EnumSet.of(TestEnum2.One, TestEnum2.Two, TestEnum2.Three, TestEnum2.Four, TestEnum2.Five);
    EnumSet test9 = EnumSet.of(TestEnum2.One, TestEnum2.Two, TestEnum2.Three, TestEnum2.Four, TestEnum2.Five,
        TestEnum2.Six, TestEnum2.Seven, TestEnum2.Eight, TestEnum2.Nine);
    EnumSet test10 = EnumSet.range(TestEnum2.One, TestEnum2.Nine);

    assertEquals(test1, test1.clone());
    assertEquals(test2, test2.clone());
    assertEquals(test3, test3.clone());
    assertEquals(test4, test4.clone());
    assertEquals(test5, test5.clone());
    assertEquals(test6, test6.clone());
    assertEquals(test7, test7.clone());
    assertEquals(test8, test8.clone());
    assertEquals(test9, test9.clone());
    assertEquals(test10, test10.clone());

    assertEquals(test1, EnumSet.copyOf(test1));
    assertEquals(test2, EnumSet.copyOf(test2));
    assertEquals(test3, EnumSet.copyOf(test3));
    assertEquals(test4, EnumSet.copyOf(test4));
    assertEquals(test5, EnumSet.copyOf(test5));
    assertEquals(test6, EnumSet.copyOf(test6));
    assertEquals(test7, EnumSet.copyOf(test7));
    assertEquals(test8, EnumSet.copyOf(test8));
    assertEquals(test9, EnumSet.copyOf(test9));
    assertEquals(test10, EnumSet.copyOf(test10));

    assertEquals(test1, EnumSet.copyOf((Collection) test1));
    assertEquals(test2, EnumSet.copyOf((Collection) test2));
    assertEquals(test3, EnumSet.copyOf((Collection) test3));
    assertEquals(test4, EnumSet.copyOf((Collection) test4));

    assertTrue(test4.add(TestEnum2.Ten));
    assertTrue(test5.add(TestEnum2.Ten));
    assertTrue(test6.add(TestEnum2.Ten));
    assertTrue(test7.add(TestEnum2.Ten));
    assertTrue(test8.add(TestEnum2.Ten));
    assertTrue(test9.add(TestEnum2.Ten));
    assertTrue(test10.add(TestEnum2.Ten));

    assertFalse(test4.addAll(test3));
    assertFalse(test5.addAll(test3));
    assertFalse(test6.addAll(test3));
    assertFalse(test7.addAll(test3));
    assertFalse(test8.addAll(test3));
    assertFalse(test9.addAll(test3));
    assertFalse(test10.addAll(test3));

    test4.clear();
    test5.clear();
    test6.clear();
    test7.clear();
    test8.clear();
    test9.clear();
    test10.clear();

    assertFalse(test4.contains(TestEnum2.One));
    assertFalse(test5.contains(TestEnum2.One));
    assertFalse(test6.contains(TestEnum2.One));
    assertFalse(test7.contains(TestEnum2.One));
    assertFalse(test8.contains(TestEnum2.One));
    assertFalse(test9.contains(TestEnum2.One));
    assertFalse(test10.contains(TestEnum2.One));

    assertTrue(test4.containsAll(test3));
    assertTrue(test5.containsAll(test3));
    assertTrue(test6.containsAll(test3));
    assertTrue(test7.containsAll(test3));
    assertTrue(test8.containsAll(test3));
    assertTrue(test9.containsAll(test3));
    assertTrue(test10.containsAll(test3));

    Iterator iterator1 = test4.iterator();
    Iterator iterator2 = test5.iterator();
    Iterator iterator3 = test6.iterator();
    Iterator iterator4 = test7.iterator();
    Iterator iterator5 = test8.iterator();
    Iterator iterator6 = test9.iterator();
    Iterator iterator7 = test10.iterator();

    assertFalse(iterator1.hasNext());
    assertFalse(iterator2.hasNext());
    assertFalse(iterator3.hasNext());
    assertFalse(iterator4.hasNext());
    assertFalse(iterator5.hasNext());
    assertFalse(iterator6.hasNext());
    assertFalse(iterator7.hasNext());

    try {
      iterator1.next();
      fail("1");
    } catch (RuntimeException exception) {
    }
    try {
      iterator2.next();
      fail("2");
    } catch (RuntimeException exception) {
    }
    try {
      iterator3.next();
      fail("3");
    } catch (RuntimeException exception) {
    }
    try {
      iterator4.next();
      fail("4");
    } catch (RuntimeException exception) {
    }
    try {
      iterator5.next();
      fail("5");
    } catch (RuntimeException exception) {
    }
    try {
      iterator6.next();
      fail("6");
    } catch (RuntimeException exception) {
    }
    try {
      iterator7.next();
      fail("7");
    } catch (RuntimeException exception) {
    }

    try {
      iterator1.remove();
      fail("8");
    } catch (RuntimeException exception) {
    }
    try {
      iterator2.remove();
      fail("9");
    } catch (RuntimeException exception) {
    }
    try {
      iterator3.remove();
      fail("10");
    } catch (RuntimeException exception) {
    }
    try {
      iterator4.remove();
      fail("11");
    } catch (RuntimeException exception) {
    }
    try {
      iterator5.remove();
      fail("12");
    } catch (RuntimeException exception) {
    }
    try {
      iterator6.remove();
      fail("13");
    } catch (RuntimeException exception) {
    }
    try {
      iterator7.remove();
      fail("14");
    } catch (RuntimeException exception) {
    }

    assertFalse(test4.remove(TestEnum2.One));
    assertFalse(test5.remove(TestEnum2.One));
    assertFalse(test6.remove(TestEnum2.One));
    assertFalse(test7.remove(TestEnum2.One));
    assertFalse(test8.remove(TestEnum2.One));
    assertFalse(test9.remove(TestEnum2.One));
    assertFalse(test10.remove(TestEnum2.One));

    assertFalse(test4.removeAll(test3));
    assertFalse(test5.removeAll(test3));
    assertFalse(test6.removeAll(test3));
    assertFalse(test7.removeAll(test3));
    assertFalse(test8.removeAll(test3));
    assertFalse(test9.removeAll(test3));
    assertFalse(test10.removeAll(test3));

    assertFalse(test4.retainAll(test3));
    assertFalse(test5.retainAll(test3));
    assertFalse(test6.retainAll(test3));
    assertFalse(test7.retainAll(test3));
    assertFalse(test8.retainAll(test3));
    assertFalse(test9.retainAll(test3));
    assertFalse(test10.retainAll(test3));

    assertEquals(0, test4.size());
    assertEquals(0, test5.size());
    assertEquals(0, test6.size());
    assertEquals(0, test7.size());
    assertEquals(0, test8.size());
    assertEquals(0, test9.size());
    assertEquals(0, test10.size());
  }

}

enum TestEnum2 {
  One, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten
}
