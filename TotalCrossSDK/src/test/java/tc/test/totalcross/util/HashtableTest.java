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

import totalcross.sys.Convert;
import totalcross.unit.TestCase;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public class HashtableTest extends TestCase {
  @Override
  public void testRun() {
    Hashtable ht = new Hashtable(7, 1.0f);
    // fill the hashtale
    assertNull(ht.put("1", "0ne"));
    assertNotNull(ht.put("1", "one"));
    assertNull(ht.put("2", "two"));
    assertNull(ht.put("3", "three"));
    assertNull(ht.put("4", "four"));
    assertNull(ht.put("5", "five"));
    assertNull(ht.put("6", "six"));
    assertNull(ht.put("7", "seven"));
    assertNull(ht.put("8", "eight"));
    assertNull(ht.put("9", "nine"));
    assertNull(ht.put("10", "ten"));
    assertNull(ht.put("11", "twelve"));
    assertNotNull(ht.put("11", "eleven"));
    assertEquals(11, ht.size());
    // get some keys
    assertEquals("one", ht.get("1"));
    assertEquals("two", ht.get("2"));
    assertEquals("seven", ht.get("7"));
    assertEquals("eleven", ht.get("11"));
    assertNull(ht.get("0"));
    // get the keys and values
    Vector keys = ht.getKeys();
    Vector values = ht.getValues();
    Vector both = ht.getKeyValuePairs("=");
    assertEquals(11, keys.size());

    for (int i = 0; i < 11; i++) {
      Object k = keys.items[i];
      Object v = values.items[i];
      Object b = both.items[i];
      assertEquals(b, k + "=" + v);
      assertNotNull(ht.get(k));
    }
    // remove some
    for (int i = 1; i <= 11; i++) {
      if ((i & 1) == 1) {
        ht.remove(Convert.toString(i));
      }
    }
    // check if they were successfully removed
    for (int i = 1; i <= 11; i++) {
      if ((i & 1) == 0) {
        ht.get(Convert.toString(i));
      }
    }
    // remove everything and make sure they were removed
    ht.clear();
    assertEquals(0, ht.size());
    for (int i = 1; i <= 11; i++) {
      if (ht.get(Convert.toString(i)) != null) {
        fail();
      }
    }
  }
}
