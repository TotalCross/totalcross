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

public class TestHashtable extends TestCase
{
   public void testRun()
   {
      Test37 test1 = new Test37(12);
      Hashtable test2 = new Test37(test1);
      Dictionary test3 = new Test37(11, 0.8f);
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      
      assertTrue(test1.isEmpty());
      assertTrue(test2.isEmpty());
      assertTrue(test3.isEmpty());
      
      Enumeration enum1 = test1.keys();
      Enumeration enum2 = test2.keys();
      Enumeration enum3 = test3.keys();
      
      assertFalse(enum1.hasMoreElements());
      assertFalse(enum2.hasMoreElements());
      assertFalse(enum3.hasMoreElements());
      
      try
      {
         enum1.nextElement();
         fail("1");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         enum2.nextElement();
         fail("2");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         enum3.nextElement();
         fail("3");
      }
      catch (NoSuchElementException exception) {}
      
      assertFalse((enum1 = test1.elements()).hasMoreElements());
      assertFalse((enum2 = test2.elements()).hasMoreElements());
      assertFalse((enum3 = test3.elements()).hasMoreElements());
      
      try
      {
         enum1.nextElement();
         fail("4");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         enum2.nextElement();
         fail("5");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         enum3.nextElement();
         fail("6");
      }
      catch (NoSuchElementException exception) {}
      
      assertFalse(test1.contains("a"));
      assertFalse(test2.contains("a"));
      assertFalse(((Hashtable)test3).contains("a"));
      
      assertFalse(test1.containsValue("a"));
      assertFalse(test2.containsValue("a"));
      assertFalse(((Hashtable)test3).containsValue("a"));
      
      assertFalse(test1.containsKey("a"));
      assertFalse(test2.containsKey("a"));
      assertFalse(((Hashtable)test3).containsKey("a"));
      
      assertEquals(null, test1.get("a"));
      assertEquals(null, test2.get("a"));
      assertEquals(null, test3.get("a"));
      
      assertEquals(null, test1.put("a", "b"));
      assertEquals(null, test2.put("a", "b"));
      assertEquals(null, test3.put("a", "b"));
      
      assertEquals("b", test1.remove("a"));
      assertEquals("b", test2.remove("a"));
      assertEquals("b", test3.remove("a"));
      
      test1.putAll(test2);
      ((Hashtable)test2).putAll((Hashtable)test3);
      ((Hashtable)test3).putAll(test1);
      
      test1.clear();
      test2.clear();
      ((Hashtable)test3).clear();
      
      assertEquals(test1, test1.clone());
      assertEquals(test2, test2.clone());
      assertEquals(test3, ((Hashtable)test3).clone());
      
      assertEquals("{}", test1.toString());
      assertEquals("{}", test2.toString());
      assertEquals("{}", test3.toString());
      
      Set set1 = test1.keySet();
      Set set2 = test2.keySet();
      Set set3 = ((Hashtable)test3).keySet();
      
      assertEquals(0, set1.size());
      assertEquals(0, set2.size());
      assertEquals(0, set3.size());
      
      Iterator it1 = set1.iterator();
      Iterator it2 = set2.iterator();
      Iterator it3 = set3.iterator();
      
      assertFalse(it1.hasNext());
      assertFalse(it2.hasNext());
      assertFalse(it3.hasNext());
      
      try
      {
         it1.next();
         fail("7");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         it2.next();
         fail("8");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         it3.next();
         fail("9");
      }
      catch (NoSuchElementException exception) {}
      
      try
      {
         it1.remove();
         fail("10");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it2.remove();
         fail("11");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it3.remove();
         fail("12");
      }
      catch (IllegalStateException exception) {}
      
      set1.clear();
      set2.clear();
      set3.clear();
      
      assertFalse(set1.contains("a"));
      assertFalse(set2.contains("a"));
      assertFalse(set3.contains("a"));
      
      assertFalse(set1.remove("a"));
      assertFalse(set2.remove("a"));
      assertFalse(set3.remove("a"));
      
      Collection values1 = test1.values();
      Collection values2 = test2.values();
      Collection values3 = ((Hashtable)test3).values();
      
      assertEquals(0, values1.size());
      assertEquals(0, values2.size());
      assertEquals(0, values3.size());
      
      assertFalse((it1 = values1.iterator()).hasNext());
      assertFalse((it2 = values2.iterator()).hasNext());
      assertFalse((it3 = values3.iterator()).hasNext());
      
      try
      {
         it1.next();
         fail("13");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         it2.next();
         fail("14");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         it3.next();
         fail("15");
      }
      catch (NoSuchElementException exception) {}
      
      try
      {
         it1.remove();
         fail("16");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it2.remove();
         fail("17");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it3.remove();
         fail("18");
      }
      catch (IllegalStateException exception) {}
      
      values1.clear();
      values2.clear();
      values3.clear();
      
      assertEquals(0, (set1 = test1.entrySet()).size());
      assertEquals(0, (set2 = test2.entrySet()).size());
      assertEquals(0, (set3 = ((Hashtable)test3).entrySet()).size());

      assertFalse((it1 = set1.iterator()).hasNext());
      assertFalse((it2 = set2.iterator()).hasNext());
      assertFalse((it3 = set3.iterator()).hasNext());
      
      try
      {
         it1.next();
         fail("19");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         it2.next();
         fail("20");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         it3.next();
         fail("21");
      }
      catch (NoSuchElementException exception) {}
      
      try
      {
         it1.remove();
         fail("22");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it2.remove();
         fail("23");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it3.remove();
         fail("24");
      }
      catch (IllegalStateException exception) {}
      
      set1.clear();
      set2.clear();
      set3.clear();
      
      assertFalse(set1.contains("a"));
      assertFalse(set2.contains("a"));
      assertFalse(set3.contains("a"));
      
      assertFalse(set1.remove("a"));
      assertFalse(set2.remove("a"));
      assertFalse(set3.remove("a"));
      
      assertTrue(test1.equals(test2));
      assertTrue(test2.equals(test3));
      assertTrue(test3.equals(test1));
      
      assertEquals(0, test1.hashCode());
      assertEquals(0, test2.hashCode());
      assertEquals(0, test3.hashCode());
   }
}

class Test37 extends Hashtable
{
   public Test37()
   {
      super();
   }
   public Test37(Map m)
   {
      super(m);
   }
   public Test37(int initialCapacity)
   {
      super(initialCapacity);
   }
   public Test37(int initialCapacity, float loadFactor)
   {
      super(initialCapacity);
   }
}
