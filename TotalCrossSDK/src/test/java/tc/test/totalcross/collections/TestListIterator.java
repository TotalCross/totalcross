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

public class TestListIterator extends TestCase
{
   public void testRun()
   {
      Test6 test1 = new Test6();
      ListIterator test2 = new Test6();
      Iterator test3 = new Test6();
      
      assertFalse(test1.hasNext());
      assertFalse(test2.hasNext());
      assertFalse(test3.hasNext());
      
      assertEquals(null, test1.next());
      assertEquals(null, test2.next());
      assertEquals(null, test3.next());
      
      assertFalse(test1.hasPrevious());
      assertFalse(test2.hasPrevious());
      assertFalse(((ListIterator)test3).hasPrevious());
      
      assertEquals(null, test1.previous());
      assertEquals(null, test2.previous());
      assertEquals(null, ((ListIterator)test3).previous());
      
      assertEquals(0, test1.nextIndex());
      assertEquals(0, test2.nextIndex());
      assertEquals(0, ((ListIterator)test3).nextIndex());
    
      assertEquals(0, test1.previousIndex());
      assertEquals(0, test2.previousIndex());
      assertEquals(0, ((ListIterator)test3).previousIndex());
      
      try
      {
         test1.remove();
      }
      catch (Throwable throwable)
      {
         fail("1");
      }
      try
      {
         test2.remove();
      }
      catch (Throwable throwable)
      {
         fail("2");
      }
      try
      {
         test3.remove();
      }
      catch (Throwable throwable)
      {
         fail("3");
      }
      
      try
      {
         test1.set(null);
      }
      catch (Throwable throwable)
      {
         fail("4");
      }
      try
      {
         test2.set(null);
      }
      catch (Throwable throwable)
      {
         fail("5");
      }
      try
      {
         ((ListIterator)test3).set(null);
      }
      catch (Throwable throwable)
      {
         fail("6");
      }
      
      try
      {
         test1.add(null);
      }
      catch (Throwable throwable)
      {
         fail("7");
      }
      try
      {
         test2.add(null);
      }
      catch (Throwable throwable)
      {
         fail("8");
      }
      try
      {
         ((ListIterator)test3).add(null);
      }
      catch (Throwable throwable)
      {
         fail("9");
      }
   }
}

class Test6 implements ListIterator
{
   public boolean hasNext()
   {
      return false;
   }

   public Object next()
   {
      return null;
   }

   public boolean hasPrevious()
   {
      return false;
   }

   public Object previous()
   {
      return null;
   }

   public int nextIndex()
   {
      return 0;
   }

   public int previousIndex()
   {
      return 0;
   }

   public void remove()
   {
   }

   public void set(Object e)
   {
   }

   public void add(Object e)
   {
   }
}
