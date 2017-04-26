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
import totalcross.sys.Vm;
import totalcross.unit.TestCase;

public class TestEnumMap extends TestCase
{
   public void testRun()
   {
      Test34 test1 = new Test34(TestEnum1.class);
      AbstractMap test2 = new Test34(test1);
      EnumMap test3 = new Test34(test2);
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      
      assertFalse(test1.containsValue(1));
      assertFalse(test2.containsValue(2));
      assertFalse(test3.containsValue(3));
      
      assertFalse(test1.containsKey(TestEnum1.One));
      assertFalse(test2.containsKey(TestEnum1.Two));
      assertFalse(test3.containsKey(TestEnum1.Three));
      
      assertEquals(null, test1.get(TestEnum1.One));
      assertEquals(null, test2.get(TestEnum1.Two));
      assertEquals(null, test3.get(TestEnum1.Three));
      
      assertEquals(null, test1.put(TestEnum1.One, "1"));
      assertEquals(null, test2.put(TestEnum1.Two, "2"));
      assertEquals(null, test3.put(TestEnum1.Three, "3"));
      
      assertEquals("1", test1.remove(TestEnum1.One));
      assertEquals("2", test2.remove(TestEnum1.Two));
      assertEquals("3", test3.remove(TestEnum1.Three));
     
      test1.putAll(test2);
      test2.putAll(test3);
      test3.putAll(test1);
      
      test1.clear();
      test2.clear();
      test3.clear();
      
      Set set1 = test1.keySet();
      Set set2 = test2.keySet();
      Set set3 = test3.keySet();
      
      assertEquals(0, set1.size());
      assertEquals(0, set2.size());
      assertEquals(0, set3.size());
      
      Iterator iterator1 = set1.iterator();
      Iterator iterator2 = set2.iterator();
      Iterator iterator3 = set3.iterator();
      
      assertFalse(iterator1.hasNext());
      assertFalse(iterator2.hasNext());
      assertFalse(iterator3.hasNext());
      
      try
      {
         iterator1.next();
         fail("1");
      }
      catch (RuntimeException exception) {} 
      try
      {
         iterator3.next();
         fail("2");
      }
      catch (RuntimeException exception) {} 
      try
      {
         iterator3.next();
         fail("3");
      }
      catch (RuntimeException exception) {} 
      
      try
      {
         iterator1.remove();
         fail("4");
      }
      catch (RuntimeException exception) {}
      try
      {
         iterator2.remove();
         fail("5");
      }
      catch (RuntimeException exception) {}
      try
      {
         iterator3.remove();
         fail("6");
      }
      catch (RuntimeException exception) {}
      
      set1.clear();
      set2.clear();
      set3.clear();
      
      assertFalse(set1.contains(TestEnum1.One));
      assertFalse(set2.contains(TestEnum1.Two));
      assertFalse(set3.contains(TestEnum1.Three));
      
      assertFalse(set1.remove(TestEnum1.One));
      assertFalse(set2.remove(TestEnum1.Two));
      assertFalse(set3.remove(TestEnum1.Three));
      
      Collection collection1 = test1.values();
      Collection collection2 = test2.values();
      Collection collection3 = test3.values();
      
      assertEquals(0, collection1.size());
      assertEquals(0, collection2.size());
      assertEquals(0, collection3.size());
      
      assertFalse((iterator1 = collection1.iterator()).hasNext());
      assertFalse((iterator2 = collection2.iterator()).hasNext());
      assertFalse((iterator3 = collection3.iterator()).hasNext());
   
      try
      {
         iterator1.next();
         fail("7");
      }
      catch (RuntimeException exception) {} 
      try
      {
         iterator3.next();
         fail("8");
      }
      catch (RuntimeException exception) {} 
      try
      {
         iterator3.next();
         fail("9");
      }
      catch (RuntimeException exception) {} 
      
      try
      {
         iterator1.remove();
         fail("10");
      }
      catch (RuntimeException exception) {}
      try
      {
         iterator2.remove();
         fail("11");
      }
      catch (RuntimeException exception) {}
      try
      {
         iterator3.remove();
         fail("12");
      }
      catch (RuntimeException exception) {}
      
      collection1.clear();
      collection2.clear();
      collection3.clear();
      
      assertEquals(0, (set1 = test1.entrySet()).size());
      assertEquals(0, (set2 = test2.entrySet()).size());
      assertEquals(0, (set3 = test3.entrySet()).size());
      
      assertFalse((iterator1 = set1.iterator()).hasNext());
      assertFalse((iterator2 = set2.iterator()).hasNext());
      assertFalse((iterator3 = set3.iterator()).hasNext());
   
      try
      {
         iterator1.next();
         fail("13");
      }
      catch (RuntimeException exception) {} 
      try
      {
         iterator3.next();
         fail("14");
      }
      catch (RuntimeException exception) {} 
      try
      {
         iterator3.next();
         fail("15");
      }
      catch (RuntimeException exception) {} 
      
      try
      {
         iterator1.remove();
         fail("16");
      }
      catch (RuntimeException exception) {}
      try
      {
         iterator2.remove();
         fail("17");
      }
      catch (RuntimeException exception) {}
      try
      {
         iterator3.remove();
         fail("18");
      }
      catch (RuntimeException exception) {}
      
      set1.clear();
      set2.clear();
      set3.clear();
      
      assertFalse(set1.contains(TestEnum1.One));
      assertFalse(set2.contains(TestEnum1.Two));
      assertFalse(set3.contains(TestEnum1.Three));
      
      assertFalse(set1.remove(TestEnum1.One));
      assertFalse(set2.remove(TestEnum1.Two));
      assertFalse(set3.remove(TestEnum1.Three));
      
      assertTrue(test1.equals(test2));
      assertTrue(test2.equals(test3));
      assertTrue(test3.equals(test2));
      
      assertTrue(test1.equals(test1.clone()));
      assertTrue(test2.equals(((EnumMap)test2).clone()));
      assertTrue(test3.equals(test3.clone()));
   }
}

class Test34 extends EnumMap implements Cloneable
{
   public Test34(Class keyType)
   {
      super(keyType);
   }
   public Test34(EnumMap map)
   {
      super(map);
   }
   public Test34(Map map)
   {
      super(map);
   }
}

enum TestEnum1
{
   One,
   Two,
   Three;
}
