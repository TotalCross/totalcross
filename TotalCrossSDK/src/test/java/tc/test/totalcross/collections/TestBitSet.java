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

import java.util.BitSet;

import totalcross.unit.TestCase;

public class TestBitSet extends TestCase
{
  @Override
  public void testRun()
  {
    Test19 test1 = new Test19();
    BitSet test2 = new Test19(64);

    test1.and(test2);
    test2.and(test1);

    test1.andNot(test2);
    test2.andNot(test1);

    assertEquals(0, test1.cardinality());
    assertEquals(0, test2.cardinality());

    test1.clear();
    test2.clear();

    test1.clear(0);
    test2.clear(32);

    test1.clear(0, 31);
    test2.clear(31, 63);

    assertTrue(test1.equals(test1));
    assertTrue(test2.equals(test2));
    assertTrue(test1.equals(test2));
    assertTrue(test2.equals(test1));

    assertEquals(1234, test1.hashCode());
    assertEquals(1234, test2.hashCode());

    test1.flip(0);
    test2.flip(63);

    test1.flip(1, 2);
    test2.flip(61, 62);

    assertTrue(test1.get(0));
    assertTrue(test2.get(63));
    assertFalse(test1.get(3));
    assertFalse(test2.get(60));

    assertEquals(2, test1.get(0, 2).length());
    assertEquals(1, test2.get(61, 62).length());

    assertTrue(test1.intersects(test1));
    assertTrue(test2.intersects(test2));
    assertFalse(test1.intersects(test2));
    assertFalse(test2.intersects(test1));

    assertFalse(test1.isEmpty());
    assertFalse(test2.isEmpty());

    assertEquals(2, test1.length());
    assertEquals(64, test2.length());

    assertEquals(2, test1.nextClearBit(0));
    assertEquals(0, test2.nextClearBit(0));

    assertEquals(0, test1.nextSetBit(0));
    assertEquals(61, test2.nextSetBit(0));

    test1.or(test2);
    test2.or(test1);

    test1.set(2);
    test2.set(62);

    test1.set(3, true);
    test2.set(60, true);

    test1.set(4, 5);
    test2.set(59, 60);

    test1.set(0, 4, false);
    test2.set(59, 63, false);

    assertEquals(64, test1.size());
    assertEquals(64, test2.size());

    assertEquals(11, test1.toString().length());
    assertEquals(10, test2.toString().length());

    test1.xor(test2);
    test2.xor(test1);

    assertEquals(test1, test1.clone());
    assertEquals(test2, test2.clone());
  }
}

class Test19 extends BitSet implements Cloneable
{
  public Test19()
  {
    super();
  }

  public Test19(int nbits)
  {
    super(nbits);
  }

  @Override
  public Object clone()
  {
    return super.clone();     
  }
}
