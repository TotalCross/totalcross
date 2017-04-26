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

public class TestList extends TestCase
{
   public void testRun()
   {
      Test5 test1 = new Test5();
      List test2 = new Test5();
      Collection test3 = new Test5();
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      
      assertFalse(test1.isEmpty());
      assertFalse(test2.isEmpty());
      assertFalse(test3.isEmpty());
      
      assertFalse(test1.contains(null));
      assertFalse(test2.contains(null));
      assertFalse(test3.contains(null));
      
      assertEquals(null, test1.iterator());
      assertEquals(null, test2.iterator());
      assertEquals(null, test3.iterator());
      
      assertEquals(null, test1.toArray());
      assertEquals(null, test2.toArray());
      assertEquals(null, test3.toArray());
      
      assertEquals(null, test1.toArray(null));
      assertEquals(null, test2.toArray(null));
      assertEquals(null, test3.toArray(null));
      
      assertEquals(false, test1.add(null));
      assertEquals(false, test2.add(null));
      assertEquals(false, test3.add(null));
      
      assertEquals(false, test1.remove(null));
      assertEquals(false, test2.remove(null));
      assertEquals(false, test3.remove(null));
      
      assertFalse(test1.containsAll(null));
      assertFalse(test2.containsAll(null));
      assertFalse(test3.containsAll(null));
      
      assertFalse(test1.addAll(null));
      assertFalse(test2.addAll(null));
      assertFalse(test3.addAll(null));
      
      assertFalse(test1.addAll(0, null));
      assertFalse(test2.addAll(0, null));
      assertFalse(((List)test3).addAll(0, null));
      
      assertFalse(test1.removeAll(null));
      assertFalse(test2.removeAll(null));
      assertFalse(test3.removeAll(null));
      
      assertFalse(test1.retainAll(null));
      assertFalse(test2.retainAll(null));
      assertFalse(test3.retainAll(null));
      
      try
      {
         test1.clear();
      }
      catch (Throwable throwable)
      {
         fail("1");
      }
      try
      {
         test2.clear();
      }
      catch (Throwable throwable)
      {
         fail("2");
      }
      try
      {
         test3.clear();
      }
      catch (Throwable throwable)
      {
         fail("3");
      }
      
      assertEquals(null, test1.get(0));
      assertEquals(null, test2.get(0));
      assertEquals(null, ((List)test3).get(0));
      
      assertEquals(null, test1.set(0, null));
      assertEquals(null, test2.set(0, null));
      assertEquals(null, ((List)test3).set(0, null));
      
      try
      {
         test1.add(0, null);
      }
      catch (Throwable throwable)
      {
         fail("4");
      }
      try
      {
         test2.add(0, null);
      }
      catch (Throwable throwable)
      {
         fail("5");
      }
      try
      {
         ((List)test3).add(0, null);
      }
      catch (Throwable throwable)
      {
         fail("6");
      }
      
      assertEquals(null, test1.remove(0));
      assertEquals(null, test2.remove(0));
      assertEquals(null, ((List)test3).remove(0));
      
      assertEquals(0, test1.indexOf(null));
      assertEquals(0, test2.indexOf(null));
      assertEquals(0, ((List)test3).indexOf(null));
      
      assertEquals(0, test1.lastIndexOf(null));
      assertEquals(0, test2.lastIndexOf(null));
      assertEquals(0, ((List)test3).lastIndexOf(null));
      
      assertEquals(null, test1.listIterator());
      assertEquals(null, test2.listIterator());
      assertEquals(null, ((List)test3).listIterator());
      
      assertEquals(null, test1.listIterator(0));
      assertEquals(null, test2.listIterator(0));
      assertEquals(null, ((List)test3).listIterator(0));
      
      assertEquals(null, test1.subList(0, 0));
      assertEquals(null, test2.subList(0, 0));
      assertEquals(null, ((List)test3).subList(0, 0));
   }
}

class Test5 implements List
{
   public int size()
   {
      return 0;
   }

   public boolean isEmpty()
   {
      return false;
   }

   public boolean contains(Object o)
   {
      return false;
   }

   public Iterator iterator()
   {
      return null;
   }

   public Object[] toArray()
   {
      return null;
   }

   public Object[] toArray(Object[] a)
   {
      return null;
   }

   public boolean add(Object e)
   {
      return false;
   }

   public boolean remove(Object o)
   {
      return false;
   }

   public boolean containsAll(Collection c)
   {
      return false;
   }

   public boolean addAll(Collection c)
   {
      return false;
   }

   public boolean addAll(int index, Collection c)
   {
      return false;
   }

   public boolean removeAll(Collection c)
   {
      return false;
   }

   public boolean retainAll(Collection c)
   {
      return false;
   }

   public void clear()
   {    
   }

   public Object get(int index)
   {
      return null;
   }

   public Object set(int index, Object element)
   {
      return null;
   }

   public void add(int index, Object element)
   {    
   }

   public Object remove(int index)
   {
      return null;
   }

   public int indexOf(Object o)
   {
      return 0;
   }

   public int lastIndexOf(Object o)
   {
      return 0;
   }

   public ListIterator listIterator()
   {
      return null;
   }

   public ListIterator listIterator(int index)
   {
      return null;
   }

   public List subList(int fromIndex, int toIndex)
   {
      return null;
   }
}