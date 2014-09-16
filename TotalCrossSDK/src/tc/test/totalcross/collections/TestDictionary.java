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

public class TestDictionary extends TestCase
{
   public void testRun()
   {
      Test17 test1 = new Test17();
      Dictionary test2 = new Test17();
      
      assertEquals(null, test1.elements());
      assertEquals(null, test2.elements());
      
      assertEquals(null, test1.get(null));
      assertEquals(null, test2.get(null));
      
      assertFalse(test1.isEmpty());
      assertFalse(test2.isEmpty());
      
      assertEquals(null, test1.keys());
      assertEquals(null, test2.keys());
      
      assertEquals(null, test1.put(null, null));
      assertEquals(null, test2.put(null, null));
      
      assertEquals(null, test1.remove(null));
      assertEquals(null, test2.remove(null));
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
   }
}

class Test17 extends Dictionary
{
   public int size()
   {
      return 0;
   }

   public boolean isEmpty()
   {
      return false;
   }

   public Enumeration keys()
   {
      return null;
   }

   public Enumeration elements()
   {
      return null;
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
}
