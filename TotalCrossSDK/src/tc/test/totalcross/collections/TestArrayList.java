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

import java.util.*;
import totalcross.unit.TestCase;

public class TestArrayList extends TestCase
{
   public void testRun()
   {
      Test18 test1 = new Test18();
      ArrayList test2 = new Test18(1);
      AbstractList test3 = new Test18(new ArrayList());
      List test4 = new Test18();
      
      test1.trimToSize();
      test2.trimToSize();
      ((ArrayList)test3).trimToSize();
      ((ArrayList)test4).trimToSize();
      
      test1.ensureCapacity(0);
      test2.ensureCapacity(1);
      ((ArrayList)test3).ensureCapacity(2);
      ((ArrayList)test4).ensureCapacity(3);
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      assertEquals(0, test4.size());
      
      assertTrue(test1.isEmpty());
      assertTrue(test2.isEmpty());
      assertTrue(test3.isEmpty());
      assertTrue(test4.isEmpty());
      
      assertFalse(test1.contains(null));
      assertFalse(test2.contains(null));
      assertFalse(test3.contains(null));
      assertFalse(test4.contains(null));
      
      assertEquals(-1, test1.indexOf(null));
      assertEquals(-1, test2.indexOf(null));
      assertEquals(-1, test3.indexOf(null));
      assertEquals(-1, test4.indexOf(null));
      
      assertEquals(-1, test1.lastIndexOf(null));
      assertEquals(-1, test2.lastIndexOf(null));
      assertEquals(-1, test3.lastIndexOf(null));
      assertEquals(-1, test4.lastIndexOf(null));
      
      assertEquals(0, test1.toArray().length);
      assertEquals(0, test2.toArray().length);
      assertEquals(0, test3.toArray().length);
      assertEquals(0, test4.toArray().length);
      
      Object[] array = new Object[0];
      assertEquals(0, test1.toArray(array).length);
      assertEquals(0, test2.toArray(array).length);
      assertEquals(0, test3.toArray(array).length);
      assertEquals(0, test4.toArray(array).length);
      
      try
      {
         test1.get(0);
         fail("1");
      }
      catch (IndexOutOfBoundsException exception) {}
      try
      {
         test2.get(0);
         fail("2");
      }
      catch (IndexOutOfBoundsException exception) {}
      try
      {
         test3.get(0);
         fail("3");
      }
      catch (IndexOutOfBoundsException exception) {}
      try
      {
         test4.get(0);
         fail("4");
      }
      catch (IndexOutOfBoundsException exception) {}
   }
   
}

class Test18 extends ArrayList 
{
   public Test18()
   {
      super();
   }
   
   public Test18(int capacity)
   {
      super(capacity);
   }
   
   public Test18(Collection c)
   {
      super(c);
   }
}
