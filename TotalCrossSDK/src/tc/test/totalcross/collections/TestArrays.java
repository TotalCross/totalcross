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
import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestArrays extends TestCase
{
   public void testRun()
   {
      byte[] bytes = new byte[]{};
      assertEquals(-1, Arrays.binarySearch(bytes, (byte)0));
      try
      {
         assertGreater(0, Arrays.binarySearch(bytes, 0, 0, (byte)0));
         if (!Settings.onJavaSE)
            fail("1");
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
      
      float[] floats = new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f};
      assertEquals(4, Arrays.binarySearch(floats, 0.5f));
      assertGreater(0, Arrays.binarySearch(floats, 0, 0, 0.6f));
      
      double[] doubles = new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7};
      assertEquals(5, Arrays.binarySearch(doubles, 6.6));
      assertGreater(0, Arrays.binarySearch(doubles, 0, 0, 8.8));
      
      Object[] objects = new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"};
      assertEquals(6, Arrays.binarySearch(objects, "g"));
      assertGreater(0, Arrays.binarySearch(objects, 0, 0, "i"));
      
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
      
      assertTrue(Arrays.equals(floats, floats)); 
      assertTrue(Arrays.equals(floats, new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f}));
      assertFalse(Arrays.equals(floats, new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.7f}));
      
      assertTrue(Arrays.equals(doubles, doubles));
      assertTrue(Arrays.equals(doubles, new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7}));
      assertFalse(Arrays.equals(doubles, new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8}));
      
      assertTrue(Arrays.equals(objects, objects));
      assertTrue(Arrays.equals(objects, new Object[]{"a", "b", "c", "d", "e", "f", "g", "h"}));
      assertFalse(Arrays.equals(objects, new Object[]{"a", "b", "c", "d", "e", "f", "g", "i"}));
   } 
}
