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

public class TestCollection extends TestCase
{
   public void testRun()
   {
      Collection test1 = new Test1();
      Test1 test2 = new Test1();
      Iterable test3 = new Test1();
      Object object = new Object();
      Object[] objects = new Object[0];
      
      assertEquals(1, test1.size());
      assertEquals(1, test2.size());
      assertEquals(1, ((Collection)test3).size());
      
      assertTrue(test1.isEmpty());
      assertTrue(test2.isEmpty());
      assertTrue(((Collection)test3).isEmpty());
      
      assertTrue(test1.contains(null));
      assertFalse(test2.contains(object));
      assertFalse(((Collection)test3).contains(object));
      
      assertEquals(null, test1.iterator());
      assertEquals(null, test2.iterator());
      assertEquals(null, test3.iterator());
      
      assertEquals(0, test1.toArray().length);
      assertEquals(0, test2.toArray().length);
      assertEquals(0, ((Collection)test3).toArray().length);
      
      assertEquals(1, test1.toArray(null).length);
      assertEquals(1, test2.toArray(objects).length);
      assertEquals(1, ((Collection)test3).toArray(objects).length);
      
      assertTrue(test1.add(test1));
      assertTrue(test2.add(test2));
      assertTrue(((Collection)test3).add(test3));
      
      assertTrue(test1.remove(test1));
      assertTrue(test2.remove(test2));
      assertTrue(((Collection)test3).remove(test3));
      
      assertTrue(test1.containsAll(test1));
      assertTrue(test2.containsAll(test2));
      assertTrue(((Collection)test3).containsAll((Collection)test3));
      
      assertTrue(test1.addAll(test1));
      assertTrue(test2.addAll(test2));
      assertTrue(((Collection)test3).addAll((Collection)test3));
      
      assertTrue(test1.removeAll(test1));
      assertTrue(test2.removeAll(test2));
      assertTrue(((Collection)test3).removeAll((Collection)test3));
      
      assertTrue(test1.retainAll(test1));
      assertTrue(test2.retainAll(test2));
      assertTrue(((Collection)test3).retainAll((Collection)test3));
      
      Test1.i = 0;
      test1.clear();
      assertEquals(1, Test1.i);
      test2.clear();
      assertEquals(2, Test1.i);
      ((Collection)test3).clear();
      assertEquals(3, Test1.i);
   }
}

class Test1 implements Collection
{
   public static int i;
   
   public int size()
   {
      return 1;
   }

   public boolean isEmpty()
   {
      return true;
   }

   public boolean contains(Object o)
   {
      return o == null;
   }

   public Iterator iterator()
   {
      return null;
   }

   public Object[] toArray()
   {
      return new Object[0];
   }

   public Object[] toArray(Object[] a)
   {
      return new Object[1];
   }

   public boolean add(Object e)
   {
      return e instanceof Test1;
   }

   public boolean remove(Object o)
   {
      return o instanceof Collection;
   }

   public boolean containsAll(Collection c)
   {
      return c instanceof Test1;
   }

   public boolean addAll(Collection c)
   {
      return c instanceof Collection;
   }

   public boolean removeAll(Collection c)
   {
      return c instanceof Test1;
   }

   public boolean retainAll(Collection c)
   {
      return c instanceof Collection;
   }

   public void clear()
   {
      i++;
   }
}
