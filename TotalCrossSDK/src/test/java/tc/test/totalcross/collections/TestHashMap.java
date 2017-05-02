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

public class TestHashMap extends TestCase
{
   public void testRun()
   {
      Test20 test1 = new Test20();
      HashMap test2 = new Test20(test1);
      AbstractMap test3 = new Test20(10, 0.1f);
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      
      assertTrue(test1.isEmpty());
      assertTrue(test2.isEmpty());
      assertTrue(test3.isEmpty());
      
      assertEquals(null, test1.get(null));
      assertEquals(null, test2.get(null));
      assertEquals(null, test3.get(null));
      
      assertFalse(test1.containsKey(null));
      assertFalse(test2.containsKey(null));
      assertFalse(test3.containsKey(null));
      
      assertEquals(null, test1.put(null, null));
      assertEquals(null, test2.put(null, null));
      assertEquals(null, test3.put(null, null));
      
      test1.putAll(test1);
      test2.putAll(test2);
      test3.putAll(test3);
      
      assertEquals(null, test1.remove(null));
      assertEquals(null, test2.remove(null));
      assertEquals(null, test3.remove(null));
      
      test1.clear();
      test2.clear();
      test3.clear();
      
      assertFalse(test1.containsValue(null));
      assertFalse(test2.containsValue(null));
      assertFalse(test3.containsValue(null));
      
      Set set1 = test1.keySet();
      Set set2 = test2.keySet();
      Set set3 = test3.keySet();
      
      assertEquals(0, set1.size());
      assertEquals(0, set2.size());
      assertEquals(0, set3.size());
      
      assertTrue(set1.iterator() instanceof Iterator);
      assertTrue(set2.iterator() instanceof Iterator);
      assertTrue(set3.iterator() instanceof Iterator);
      
      set1.clear();
      set2.clear();
      set3.clear();
      
      assertFalse(set1.contains(null));
      assertFalse(set2.contains(null));
      assertFalse(set3.contains(null));
      
      assertFalse(set1.remove(null));
      assertFalse(set2.remove(null));
      assertFalse(set3.remove(null));
      
      Collection values1 = test1.values();
      Collection values2 = test2.values();
      Collection values3 = test3.values();
      
      assertEquals(0, values1.size());
      assertEquals(0, values2.size());
      assertEquals(0, values3.size());
      
      assertTrue(values1.iterator() instanceof Iterator);
      assertTrue(values2.iterator() instanceof Iterator);
      assertTrue(values3.iterator() instanceof Iterator);
      
      values1.clear();
      values2.clear();
      values3.clear();
      
      set1 = test1.entrySet();
      set2 = test2.entrySet();
      set3 = test3.entrySet();
      
      assertEquals(0, set1.size());
      assertEquals(0, set2.size());
      assertEquals(0, set3.size());
      
      assertTrue(set1.iterator() instanceof Iterator);
      assertTrue(set2.iterator() instanceof Iterator);
      assertTrue(set3.iterator() instanceof Iterator);
      
      set1.clear();
      set2.clear();
      set3.clear();
      
      assertFalse(set1.contains(null));
      assertFalse(set2.contains(null));
      assertFalse(set3.contains(null));
      
      assertFalse(set1.remove(null));
      assertFalse(set2.remove(null));
      assertFalse(set3.remove(null));
      
      assertEquals(test1, test1.clone());
      assertEquals(test2, test2.clone());
      assertEquals(test3, ((HashMap)test3).clone());
   }
}

class Test20 extends HashMap implements Cloneable
{
   public Test20()
   {
      super();
   }
   
   public Test20(Map m)
   {
      super(m);
   }
   
   public Test20(int initialCapacity, float load)
   {
      super(initialCapacity, load);
   }
   
   public Object clone()
   {
      return super.clone();     
   }
}
