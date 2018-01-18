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
import totalcross.util.IntHashtable;
import totalcross.util.IntVector;

public class IntHashtableTest extends TestCase {
  @Override
  public void testRun() {
    IntHashtable ht = new IntHashtable(7, 1.0f);
    // fill the hashtale
    assertEquals(2000, ht.put(1, 2000));
    assertNotEquals(1000, ht.put(1, 1000));
    assertEquals(2000, ht.put(2, 2000));
    assertEquals(3000, ht.put(3, 3000));
    assertEquals(4000, ht.put(4, 4000));
    assertEquals(5000, ht.put(5, 5000));
    assertEquals(6000, ht.put(6, 6000));
    assertEquals(7000, ht.put(7, 7000));
    assertEquals(8000, ht.put(8, 8000));
    assertEquals(9000, ht.put(9, 9000));
    assertEquals(10000, ht.put(10, 10000));
    assertEquals(12000, ht.put(11, 12000));
    assertNotEquals(11000, ht.put(11, 11000));
    assertEquals(11, ht.size());
    // save so we can restore later
    /*      ByteArrayStream bas = new ByteArrayStream(1000);
      DataStream ds = new DataStream(bas);
      ht.saveTo(ds);
     */ // get some keys
    try {
      assertEquals(1000, ht.get(1));
    } catch (ElementNotFoundException e) {
      fail();
    }
    try {
      assertEquals(2000, ht.get(2));
    } catch (ElementNotFoundException e) {
      fail();
    }
    try {
      assertEquals(7000, ht.get(7));
    } catch (ElementNotFoundException e) {
      fail();
    }
    try {
      assertEquals(11000, ht.get(11));
    } catch (ElementNotFoundException e) {
      fail();
    }
    try {
      ht.get(0);
      fail();
    } catch (ElementNotFoundException e) {
      /* ok */}
    // get the keys and values
    IntVector keys = ht.getKeys();
    IntVector values = ht.getValues();
    assertEquals(11, keys.size());

    for (int i = 0; i < 11; i++) {
      int k = keys.items[i];
      int v = values.items[i];
      assertEquals(k * 1000, v);
      try {
        assertEquals(k * 1000, ht.get(k));
      } catch (ElementNotFoundException e) {
        fail();
      }
    }
    // remove some
    for (int i = 1; i <= 11; i++) {
      if ((i & 1) == 1) {
        try {
          ht.remove(i);
        } catch (ElementNotFoundException e) {
          fail();
        }
      }
    }
    // check if they were successfully removed
    for (int i = 1; i <= 11; i++) {
      if ((i & 1) == 0) {
        try {
          ht.get(i);
        } catch (ElementNotFoundException e) {
          fail();
        }
      }
    }
    // remove everything and make sure they were removed
    ht.clear();
    assertEquals(0, ht.size());
    for (int i = 1; i <= 11; i++) {
      try {
        ht.get(i);
        fail();
      } catch (ElementNotFoundException e) {
      }
    }

    // now restore the hashtable
    /*      bas.reset();
      ht = new IntHashtable(ds);
      assertEquals(11,ht.size());
      keys = ht.getKeys();
      for (int i =0; i < 11; i++)
      {
         int k = keys.items[i];
         assertEquals(k*1000,ht.get(k));
      }
     */ }
}
