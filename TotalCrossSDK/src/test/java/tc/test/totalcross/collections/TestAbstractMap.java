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

public class TestAbstractMap extends TestCase
{
   public void testRun()
   {
      Test14 test1 = new Test14();
      AbstractMap test2 = new Test14();
      Map test3 = new Test14();
      
      assertEquals(null, test1.entrySet());
      assertEquals(null, test2.entrySet());
      assertEquals(null, ((AbstractMap)test3).entrySet());
      
      try
      {
         test1.clear();
         fail("1");
      }
      catch (NullPointerException exception) {}
      try
      {
         test2.clear();
         fail("2");
      }
      catch (NullPointerException exception) {}
      try
      {
         test3.clear();
         fail("3");
      }
      catch (NullPointerException exception) {}

      try
      {
         test1.containsKey(null);
         fail("4");
      }
      catch (NullPointerException exception) {}
      try
      {
         test1.containsKey(null);
         fail("5");
      }
      catch (NullPointerException exception) {}
      try
      {
         test1.containsKey(null);
         fail("6");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         test1.containsValue(null);
         fail("7");
      }
      catch (NullPointerException exception) {}
      try
      {
         test1.containsValue(null);
         fail("8");
      }
      catch (NullPointerException exception) {}
      try
      {
         test1.containsValue(null);
         fail("9");
      }
      catch (NullPointerException exception) {}
      
      assertTrue(test1.equals(test1));
      assertTrue(test2.equals(test2));
      assertTrue(test3.equals(test3));
      try
      {
         test1.equals(test2);
         fail("10");
      }
      catch (NullPointerException exception) {}
      try
      {
         test2.equals(test3);
         fail("11");
      }
      catch (NullPointerException exception) {}
      try
      {
         test3.equals(test1);
         fail("12");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         test1.get(null);
         fail("13");
      }
      catch (NullPointerException exception) {}
      try
      {
         test2.get(null);
         fail("14");
      }
      catch (NullPointerException exception) {}
      try
      {
         test3.get(null);
         fail("15");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         test1.hashCode();
         fail("16");
      }
      catch (NullPointerException exception) {}
      try
      {
         test2.hashCode();
         fail("17");
      }
      catch (NullPointerException exception) {}
      try
      {
         test3.hashCode();
         fail("18");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         test1.isEmpty();
         fail("19");
      }
      catch (NullPointerException exception) {}
      try
      {
         test2.isEmpty();
         fail("20");
      }
      catch (NullPointerException exception) {}
      try
      {
         test3.isEmpty();
         fail("21");
      }
      catch (NullPointerException exception) {}
      
      Set set1 = test1.keySet();
      Set set2 = test2.keySet();
      Set set3 = test3.keySet();
      
      try
      {
         set1.size();
         fail("22");
      }
      catch (NullPointerException exception) {}
      try
      {
         set2.size();
         fail("23");
      }
      catch (NullPointerException exception) {}
      try
      {
         set3.size();
         fail("24");
      }
      catch (NullPointerException exception) {}

      try
      {
         set1.contains(null);
         fail("25");
      }
      catch (NullPointerException exception) {}
      try
      {
         set2.contains(null);
         fail("26");
      }
      catch (NullPointerException exception) {}
      try
      {
         set3.contains(null);
         fail("27");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         set1.iterator();
         fail("28");
      }
      catch (NullPointerException exception) {} 
      try
      {
         set2.iterator();
         fail("29");
      }
      catch (NullPointerException exception) {} 
      try
      {
         set3.iterator();
         fail("30");
      }
      catch (NullPointerException exception) {} 
      
      try
      {
         test1.put(null, null);
         fail("31");
      }
      catch (UnsupportedOperationException exception) {} 
      try
      {
         test2.put(null, null);
         fail("32");
      }
      catch (UnsupportedOperationException exception) {} 
      try
      {
         test3.put(null, null);
         fail("33");
      }
      catch (UnsupportedOperationException exception) {}
      
      try
      {
         test1.putAll(test1);
         fail("34");
      }
      catch (NullPointerException exception) {} 
      try
      {
         test2.putAll(test2);
         fail("35");
      }
      catch (NullPointerException exception) {} 
      try
      {
         test3.putAll(test3);
         fail("36");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         test1.remove(null);
         fail("37");
      }
      catch (NullPointerException exception) {} 
      try
      {
         test2.remove(null);
         fail("38");
      }
      catch (NullPointerException exception) {} 
      try
      {
         test3.remove(null);
         fail("39");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         test1.size();
         fail("40");
      }
      catch (NullPointerException exception) {} 
      try
      {
         test2.size();
         fail("41");
      }
      catch (NullPointerException exception) {} 
      try
      {
         test3.size();
         fail("42");
      }
      catch (NullPointerException exception) {}
           
      try
      {
         test1.toString();
         fail("43");
      }
      catch (NullPointerException exception) {}
      try
      {
         test2.toString();
         fail("44");
      }
      catch (NullPointerException exception) {}
      try
      {
         test3.toString();
         fail("45");
      }
      catch (NullPointerException exception) {}
      
      Collection values1 = test1.values();
      Collection values2 = test2.values();
      Collection values3 = test3.values();
      
      try
      {
         values1.size();
         fail("46");
      }
      catch (NullPointerException exception) {}
      try
      {
         values2.size();
         fail("47");
      }
      catch (NullPointerException exception) {}
      try
      {
         values3.size();
         fail("48");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         values1.contains(null);
         fail("49");
      }
      catch (NullPointerException exception) {}
      try
      {
         values2.contains(null);
         fail("50");
      }
      catch (NullPointerException exception) {}
      try
      {
         values3.contains(null);
         fail("51");
      }
      catch (NullPointerException exception) {} 
      
      try
      {
         values1.iterator();
         fail("52");
      }
      catch (NullPointerException exception) {}
      try
      {
         values2.iterator();
         fail("53");
      }
      catch (NullPointerException exception) {}
      try
      {
         values3.iterator();
         fail("54");
      }
      catch (NullPointerException exception) {} 
      
      try
      {
         test1.clone();
      }
      catch (CloneNotSupportedException e)
      {
         fail("55");
      }
      
      try
      {
         ((Test14)test2).clone();
      }
      catch (CloneNotSupportedException e)
      {
         fail("56");
      }
      
      try
      {
         ((Test14)test3).clone();
      }
      catch (CloneNotSupportedException e)
      {
         fail("57");
      }
   } 
}

class Test14 extends AbstractMap implements Cloneable
{
   public Set entrySet()
   {
      return null;
   }
   
   public Object clone() throws CloneNotSupportedException
   {
      return super.clone();
   }
}
