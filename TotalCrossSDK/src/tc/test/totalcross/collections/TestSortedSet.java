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

public class TestSortedSet extends TestCase
{
   public void testRun()
   {
      Test10 test1 = new Test10();
      SortedSet test2 = new Test10();
      Set test3 = new Test10();
      
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
      
      assertFalse(test1.add(null));
      assertFalse(test2.add(null));
      assertFalse(test3.add(null));
      
      assertFalse(test1.remove(null));
      assertFalse(test2.remove(null));
      assertFalse(test3.remove(null));
      
      assertFalse(test1.containsAll(null));
      assertFalse(test2.containsAll(null));
      assertFalse(test3.containsAll(null));
      
      assertFalse(test1.addAll(null));
      assertFalse(test2.addAll(null));
      assertFalse(test3.addAll(null));
      
      assertFalse(test1.retainAll(null));
      assertFalse(test2.retainAll(null));
      assertFalse(test3.retainAll(null));
      
      assertFalse(test1.removeAll(null));
      assertFalse(test2.removeAll(null));
      assertFalse(test3.removeAll(null));
      
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
      
      assertEquals(null, test1.comparator());
      assertEquals(null, test2.comparator());
      assertEquals(null, ((SortedSet)test3).comparator());
      
      assertEquals(null, test1.subSet(null, null));
      assertEquals(null, test2.subSet(null, null));
      assertEquals(null, ((SortedSet)test3).subSet(null, null));
      
      assertEquals(null, test1.headSet(null));
      assertEquals(null, test2.headSet(null));
      assertEquals(null, ((SortedSet)test3).headSet(null));
      
      assertEquals(null, test1.tailSet(null));
      assertEquals(null, test2.tailSet(null));
      assertEquals(null, ((SortedSet)test3).tailSet(null));
      
      assertEquals(null, test1.first());
      assertEquals(null, test2.first());
      assertEquals(null, ((SortedSet)test3).first());
      
      assertEquals(null, test1.last());
      assertEquals(null, test2.last());
      assertEquals(null, ((SortedSet)test3).last());
   }
}

class Test10 implements SortedSet
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

   public boolean retainAll(Collection c)
   {
      return false;
   }

   public boolean removeAll(Collection c)
   {
      return false;
   }

   public void clear()
   {
   }

   public Comparator comparator()
   {
      return null;
   }

   public SortedSet subSet(Object fromElement, Object toElement)
   {
      return null;
   }

   public SortedSet headSet(Object toElement)
   {
      return null;
   }

   public SortedSet tailSet(Object fromElement)
   {
      return null;
   }

   public Object first()
   {
      return null;
   }

   public Object last()
   {
      return null;
   }
}
