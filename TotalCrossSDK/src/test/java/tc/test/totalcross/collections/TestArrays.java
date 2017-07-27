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

import java.util.Arrays;
import java.util.List;

import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestArrays extends TestCase
{
  @Override
  public void testRun()
  {
    byte[] bytes = new byte[]{};
    assertEquals(-1, Arrays.binarySearch(bytes, (byte)0));
    try
    {
      assertGreater(0, Arrays.binarySearch(bytes, 0, 0, (byte)0));
      if (!Settings.onJavaSE) {
        fail("1");
      }
    }
    catch (ArrayIndexOutOfBoundsException exception) {}

    char[] chars = new char[]{'a', 'b'};
    assertEquals(0, Arrays.binarySearch(chars, 'a'));
    assertGreater(0, Arrays.binarySearch(chars, 0, 0, 'c'));

    short[] shorts = new short[]{1, 2, 3};
    assertEquals(1, Arrays.binarySearch(shorts, (short)2));
    assertGreater(0, Arrays.binarySearch(shorts, 0, 0, (short)4));

    int[] ints = new int[]{1, 2, 3, 4};
    assertEquals(2, Arrays.binarySearch(ints, 3));
    assertGreater(0, Arrays.binarySearch(ints, 0, 0, 5));

    long[] longs = new long[]{1, 2, 3, 4, 5};
    assertEquals(3, Arrays.binarySearch(longs, 4));
    assertGreater(0, Arrays.binarySearch(longs, 0, 0, 6));

    double[] doubles = new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7};
    assertEquals(5, Arrays.binarySearch(doubles, 6.6));
    assertGreater(0, Arrays.binarySearch(doubles, 0, 0, 8.8));

    Object[] objects = new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"};
    assertEquals(6, Arrays.binarySearch(objects, "g"));
    assertGreater(0, Arrays.binarySearch(objects, 0, 0, "i"));

    boolean[] booleans = new boolean[]{true};
    assertTrue(Arrays.equals(booleans, new boolean[]{true}));
    assertTrue(Arrays.equals(new boolean[]{true}, new boolean[]{true}));
    assertFalse(Arrays.equals(new boolean[]{true}, new boolean[]{false}));

    assertTrue(Arrays.equals(bytes, bytes));
    assertTrue(Arrays.equals(bytes, new byte[]{}));
    assertFalse(Arrays.equals(bytes, new byte[]{0}));

    assertTrue(Arrays.equals(chars, chars));
    assertTrue(Arrays.equals(chars, new char[]{'a', 'b'}));
    assertFalse(Arrays.equals(chars, new char[]{'a', 'c'}));

    assertTrue(Arrays.equals(shorts, shorts));
    assertTrue(Arrays.equals(shorts, new short[]{1, 2, 3}));
    assertFalse(Arrays.equals(shorts, new short[]{1, 2, 3, 4}));

    assertTrue(Arrays.equals(ints, ints));
    assertTrue(Arrays.equals(ints, new int[]{1, 2, 3, 4}));
    assertFalse(Arrays.equals(ints, new int[]{1, 2, 3, 5}));

    assertTrue(Arrays.equals(longs, longs));
    assertTrue(Arrays.equals(longs, new long[]{1, 2, 3, 4, 5}));
    assertFalse(Arrays.equals(longs, new long[]{1, 2, 3, 4, 5, 6}));

    assertTrue(Arrays.equals(doubles, doubles));
    assertTrue(Arrays.equals(doubles, new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7}));
    assertFalse(Arrays.equals(doubles, new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8}));

    assertTrue(Arrays.equals(objects, objects));
    assertTrue(Arrays.equals(objects, new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"}));
    assertFalse(Arrays.equals(objects, new Object[]{"a", "b", "c", "d", "e", "f", "g", "i"}));

    Arrays.fill(booleans, true);
    assertTrue(Arrays.equals(booleans, new boolean[] {true}));

    Arrays.fill(booleans, 0, 1, false);
    assertTrue(Arrays.equals(booleans, new boolean[] {false}));

    Arrays.fill(bytes, (byte)0);
    assertTrue(Arrays.equals(bytes, new byte[]{}));

    Arrays.fill(bytes, 0, 0, (byte)1);
    assertTrue(Arrays.equals(bytes, new byte[]{}));

    Arrays.fill(chars, 'a');
    assertTrue(Arrays.equals(chars, new char[]{'a', 'a'}));

    Arrays.fill(chars, 0, 2, 'b');
    assertTrue(Arrays.equals(chars, new char[]{'b', 'b'}));

    Arrays.fill(shorts, (short)0);
    assertTrue(Arrays.equals(shorts, new short[]{0, 0, 0}));

    Arrays.fill(shorts, 0, 3, (short)1);
    assertTrue(Arrays.equals(shorts, new short[]{1, 1, 1}));

    Arrays.fill(ints, 0);
    assertTrue(Arrays.equals(ints, new int[]{0, 0, 0, 0}));

    Arrays.fill(ints, 0, 4, 1);
    assertTrue(Arrays.equals(ints, new int[]{1, 1, 1, 1}));

    Arrays.fill(longs, 0);
    assertTrue(Arrays.equals(longs, new long[]{0, 0, 0, 0, 0}));

    Arrays.fill(longs, 0, 5, 1);
    assertTrue(Arrays.equals(longs, new long[]{1, 1, 1, 1, 1}));

    Arrays.fill(doubles, 0.3);
    assertTrue(Arrays.equals(doubles, new double[]{0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3}));

    Arrays.fill(doubles, 0, 7, 0.4);
    assertTrue(Arrays.equals(doubles, new double[]{0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4}));

    Arrays.fill(objects, "a");
    assertTrue(Arrays.equals(objects, new Object[]{"a", "a", "a", "a", "a", "a", "a", "a"}));

    Arrays.fill(objects, 0, 8, "b");
    assertTrue(Arrays.equals(objects, new Object[]{"b", "b", "b", "b", "b", "b", "b", "b"}));

    Arrays.sort(bytes);
    assertTrue(Arrays.equals(bytes, new byte[]{}));

    Arrays.sort(bytes, 0, 0);
    assertTrue(Arrays.equals(bytes, new byte[]{}));

    Arrays.sort(chars = new char[]{'b', 'a'});
    assertTrue(Arrays.equals(chars, new char[]{'a', 'b'}));

    Arrays.sort(chars = new char[]{'b', 'a'}, 0, 2);
    assertTrue(Arrays.equals(chars, new char[]{'a', 'b'}));

    Arrays.sort(shorts = new short[]{2, 1, 0});
    assertTrue(Arrays.equals(shorts, new short[]{0, 1, 2}));

    Arrays.sort(shorts = new short[]{2, 1, 0}, 0, 3);
    assertTrue(Arrays.equals(shorts, new short[]{0, 1, 2}));

    Arrays.sort(ints = new int[]{3, 2, 1, 0});
    assertTrue(Arrays.equals(ints, new int[]{0, 1, 2, 3}));

    Arrays.sort(ints = new int[]{3, 2, 1, 0}, 0, 4);
    assertTrue(Arrays.equals(ints, new int[]{0, 1, 2, 3}));

    Arrays.sort(longs = new long[]{4, 3, 2, 1, 0});
    assertTrue(Arrays.equals(longs, new long[]{0, 1, 2, 3, 4}));

    Arrays.sort(longs = new long[]{4, 3, 2, 1, 0}, 0, 5);
    assertTrue(Arrays.equals(longs, new long[]{0, 1, 2, 3, 4}));

    Arrays.sort(doubles = new double[]{0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.0});
    assertTrue(Arrays.equals(doubles, new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6}));

    Arrays.sort(doubles = new double[]{0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.0}, 0, 7);
    assertTrue(Arrays.equals(doubles, new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6}));

    Arrays.sort(objects = new Object[]{"h", "g", "f", "e", "d", "c", "b", "a"});
    assertTrue(Arrays.equals(objects, new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"}));

    Arrays.sort(objects = new Object[]{"h", "g", "f", "e", "d", "c", "b", "a"}, null);
    assertTrue(Arrays.equals(objects, new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"}));

    Arrays.sort(objects = new Object[]{"h", "g", "f", "e", "d", "c", "b", "a"}, 0, 8);
    assertTrue(Arrays.equals(objects, new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"}));

    Arrays.sort(objects = new Object[]{"h", "g", "f", "e", "d", "c", "b", "a"}, 0, 8, null);
    assertTrue(Arrays.equals(objects, new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"}));

    List list = Arrays.asList(objects);
    assertTrue(Arrays.equals(objects, list.toArray()));
    assertEquals("a", list.get(0));
    assertEquals(8, list.size());
    assertEquals("h", list.set(7, "i"));
    assertTrue(list.contains("b"));
    assertEquals(2, list.indexOf("c"));
    assertEquals(3, list.lastIndexOf("d"));
    assertTrue(Arrays.equals(new Object[]{"a", "b", "c", "d", "e", "f", "g", "i"}, list.toArray()));
    assertTrue(Arrays.equals(new Object[]{"a", "b", "c", "d", "e", "f", "g", "i"}, list.toArray(new Object[0])));

    assertEquals(28660961, Arrays.hashCode(longs));
    assertEquals(924547, Arrays.hashCode(ints));
    assertEquals(29824, Arrays.hashCode(shorts));
    assertEquals(4066, Arrays.hashCode(chars));
    assertEquals(1, Arrays.hashCode(bytes));
    assertEquals(1268, Arrays.hashCode(booleans));
    assertEquals(-1079375742, Arrays.hashCode(doubles));
    assertEquals(-547780730, Arrays.hashCode(objects));

    assertEquals(-547780730, Arrays.deepHashCode(objects));

    assertTrue(Arrays.deepEquals(objects, objects));

    assertEquals("[false]", Arrays.toString(booleans));
    assertEquals("[]", Arrays.toString(bytes));
    assertEquals("[a, b]", Arrays.toString(chars));
    assertEquals("[0, 1, 2]", Arrays.toString(shorts));
    assertEquals("[0, 1, 2, 3]", Arrays.toString(ints));
    assertEquals("[0, 1, 2, 3, 4]", Arrays.toString(longs));
    assertEquals("[0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6]", Arrays.toString(doubles));
    assertEquals("[a, b, c, d, e, f, g, i]", Arrays.toString(objects));

    assertEquals("[a, b, c, d, e, f, g, i]", Arrays.deepToString(objects));

    assertTrue(Arrays.equals(new boolean[0], Arrays.copyOf(booleans, 0)));
    assertTrue(Arrays.equals(new boolean[0], Arrays.copyOfRange(booleans, 0, 0)));

    assertTrue(Arrays.equals(new byte[] {0}, Arrays.copyOf(bytes, 1)));
    assertTrue(Arrays.equals(new byte[] {0}, Arrays.copyOfRange(bytes, 0, 1)));

    assertTrue(Arrays.equals(new char[] {'a'}, Arrays.copyOf(chars, 1)));
    assertTrue(Arrays.equals(new char[] {'a'}, Arrays.copyOfRange(chars, 0, 1)));

    assertTrue(Arrays.equals(new short[] {0, 1}, Arrays.copyOf(shorts, 2)));
    assertTrue(Arrays.equals(new short[] {0, 1}, Arrays.copyOfRange(shorts, 0, 2)));

    assertTrue(Arrays.equals(new int[] {0, 1, 2}, Arrays.copyOf(ints, 3)));
    assertTrue(Arrays.equals(new int[] {0, 1, 2}, Arrays.copyOfRange(ints, 0, 3)));

    assertTrue(Arrays.equals(new long[] {0, 1, 2, 3}, Arrays.copyOf(longs, 4)));
    assertTrue(Arrays.equals(new long[] {0, 1, 2, 3}, Arrays.copyOfRange(longs, 0, 4)));

    assertTrue(Arrays.equals(new double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5}, Arrays.copyOf(doubles, 6)));
    assertTrue(Arrays.equals(new double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5}, Arrays.copyOfRange(doubles, 0, 6)));

    assertTrue(Arrays.equals(new Object[] {"a", "b", "c", "d", "e", "f", "g"}, Arrays.copyOf(objects, 7)));
    assertTrue(Arrays.equals(new Object[] {"a", "b", "c", "d", "e", "f", "g"}, Arrays.copyOfRange(objects, 0, 7)));
    assertTrue(Arrays.equals(new Object[] {"a", "b", "c", "d", "e", "f", "g"}, Arrays.copyOf(objects, 7, String[].class)));
    assertTrue(Arrays.equals(new Object[] {"a", "b", "c", "d", "e", "f", "g"}, Arrays.copyOfRange(objects, 0, 7, String[].class)));
  } 

}
