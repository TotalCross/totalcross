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

public class TestMap extends TestCase
{
   public void testRun()
   {
      Test7 test1 = new Test7();
      Map test2 = new Test7();
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      
      assertTrue(test1.isEmpty());
      assertTrue(test2.isEmpty());
      
      assertFalse(test1.containsKey(null));
      assertFalse(test2.containsKey(null));
      
      assertFalse(test1.containsValue(null));
      assertFalse(test2.containsValue(null));
      
      assertEquals(null, test1.get(null));
      assertEquals(null, test2.get(null));
      
      assertEquals(null, test1.put(null, null));
      assertEquals(null, test2.put(null, null));
      
      assertEquals(null, test1.remove(null));
      assertEquals(null, test2.remove(null));
      
      try
      {
         test1.putAll(test2);
      }
      catch (Throwable throwable)
      {
         fail("1");
      }
      try
      {
         test2.putAll(test1);
      }
      catch (Throwable throwable)
      {
         fail("2");
      }
      
      try
      {
         test1.clear();
      }
      catch (Throwable throwable)
      {
         fail("3");
      }
      try
      {
         test2.clear();
      }
      catch (Throwable throwable)
      {
         fail("4");
      }
      
      assertEquals(null, test1.keySet());
      assertEquals(null, test2.keySet());
      
      assertEquals(null, test1.values());
      assertEquals(null, test2.values());
      
      assertEquals(null, test1.entrySet());
      assertEquals(null, test2.entrySet());
   }
}

class Test7 implements Map
{
   public int size()
   {
      return 0;
   }

   public boolean isEmpty()
   {
      return true;
   }

   public boolean containsKey(Object key)
   {
      return false;
   }

   public boolean containsValue(Object value)
   {
      return false;
   }

   public Object get(Object key)
   {
      return null;
   }

   public Object put(Object key, Object value)
   {
      return null;
   }

   public Object remove(Object key)
   {
      return null;
   }

   public void putAll(Map m)
   {
   }

   public void clear()
   {
   }

   public Set keySet()
   {
      return null;
   }

   public Collection values()
   {
      return null;
   }

   public Set entrySet()
   {
      return null;
   }
}
