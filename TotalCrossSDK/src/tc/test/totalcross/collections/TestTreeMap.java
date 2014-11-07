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
import totalcross.sys.*;
import totalcross.unit.TestCase;

public class TestTreeMap extends TestCase
{
   public void testRun()
   {
      Test32 test1 = new Test32();
      TreeMap test2 = new Test32((Comparator)null);
      AbstractMap test3 = new Test32(Collections.emptyMap());
      
      test1.clear();
      test2.clear();
      test3.clear();
      
      assertEquals(test1, test1.clone());
      assertEquals(test2, test2.clone());
      assertEquals(test3, ((TreeMap)test3).clone());
      
      assertEquals(null, test1.comparator());
      assertEquals(null, test2.comparator());
      assertEquals(null, ((TreeMap)test3).comparator());
      
      assertFalse(test1.containsKey("a"));
      assertFalse(test2.containsKey("a"));
      assertFalse(test3.containsKey("a"));
      
      assertFalse(test1.containsValue("b"));
      assertFalse(test2.containsValue("b"));
      assertFalse(test3.containsValue("b"));
      
      Set eSet1 = test1.entrySet();
      Set eSet2 = test2.entrySet();
      Set eSet3 = test3.entrySet();
      
      assertEquals(Collections.EMPTY_SET, eSet1);
      assertEquals(Collections.EMPTY_SET, eSet2);
      assertEquals(Collections.EMPTY_SET, eSet3);
      
      try
      {
         test1.firstKey();
         fail("1");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         test2.firstKey();
         fail("2");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         ((TreeMap)test3).firstKey();
         fail("3");
      }
      catch (NoSuchElementException exception) {}
      
      assertEquals(null, test1.get("a"));
      assertEquals(null, test2.get("a"));
      assertEquals(null, test3.get("a"));
      
      assertEquals(Collections.EMPTY_MAP, test1.headMap("a"));
      assertEquals(Collections.EMPTY_MAP, test2.headMap("a"));
      assertEquals(Collections.EMPTY_MAP, ((TreeMap)test3).headMap("a"));
      
      assertEquals(Collections.EMPTY_MAP, test1.headMap("a", false));
      assertEquals(Collections.EMPTY_MAP, test2.headMap("a", false));
      assertEquals(Collections.EMPTY_MAP, ((TreeMap)test3).headMap("a", false));
      
      assertEquals(Collections.EMPTY_SET, test1.keySet());
      assertEquals(Collections.EMPTY_SET, test2.keySet());
      assertEquals(Collections.EMPTY_SET, test3.keySet());
      
      try
      {
         test1.lastKey();
         fail("4");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         test2.lastKey();
         fail("5");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         ((TreeMap)test3).lastKey();
         fail("6");
      }
      catch (NoSuchElementException exception) {}
      
      assertEquals(null, test1.put("a", "b"));
      assertEquals(null, test2.put("a", "b"));
      assertEquals(null, test3.put("a", "b"));
      
      test1.putAll(test2);
      test2.putAll(test3);
      test3.putAll(test1);
      
      assertEquals("b", test1.remove("a"));
      assertEquals("b", test2.remove("a"));
      assertEquals("b", test3.remove("a"));
      
      assertEquals(0, test1.size());
      assertEquals(0, test2.size());
      assertEquals(0, test3.size());
      
      assertEquals(Collections.EMPTY_MAP, test1.subMap("a", "b"));
      assertEquals(Collections.EMPTY_MAP, test2.subMap("a", "b"));
      assertEquals(Collections.EMPTY_MAP, ((TreeMap)test3).subMap("a", "b"));
      
      assertEquals(Collections.EMPTY_MAP, test1.subMap("a", true, "b", false));
      assertEquals(Collections.EMPTY_MAP, test2.subMap("a", true, "b", false));
      assertEquals(Collections.EMPTY_MAP, ((TreeMap)test3).subMap("a", true, "b", false));
      
      assertEquals(Collections.EMPTY_MAP, test1.tailMap("a"));
      assertEquals(Collections.EMPTY_MAP, test2.tailMap("a"));
      assertEquals(Collections.EMPTY_MAP, ((TreeMap)test3).tailMap("a"));
      
      NavigableMap subMap1 = test1.tailMap("a", true);
      NavigableMap subMap2 = test2.tailMap("a", true);
      NavigableMap subMap3 = ((TreeMap)test3).tailMap("a", true);
      
      assertEquals(Collections.EMPTY_MAP, subMap1);
      assertEquals(Collections.EMPTY_MAP, subMap2);
      assertEquals(Collections.EMPTY_MAP, subMap3);
      
      Collection values1 = test1.values();
      Collection values2 = test2.values();
      Collection values3 = test3.values();
      
      assertEquals(0, values1.size());
      assertEquals(0, values2.size());
      assertEquals(0, values3.size());
      
      Iterator iterator1 = values1.iterator();
      Iterator iterator2 = values2.iterator();
      Iterator iterator3 = values3.iterator();
      
      test1.clear();
      test2.clear();
      test3.clear();
      
      assertFalse(iterator1.hasNext());
      assertFalse(iterator2.hasNext());
      assertFalse(iterator3.hasNext());
      
      try
      {
         iterator1.next();
         fail("7");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         iterator2.next();
         fail("8");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         iterator3.next();
         fail("9");
      }
      catch (NoSuchElementException exception) {}
      
      try
      {
         iterator1.remove();
         fail("10");
      }
      catch (IllegalStateException exception) {}
      try
      {
         iterator2.remove();
         fail("11");
      }
      catch (IllegalStateException exception) {}
      try
      {
         iterator3.remove();
         fail("12");
      }
      catch (IllegalStateException exception) {}
      
      assertEquals(null, subMap1.ceilingEntry("a"));
      assertEquals(null, subMap2.ceilingEntry("a"));
      assertEquals(null, subMap3.ceilingEntry("a"));
      
      try
      {
         assertEquals(null, subMap1.ceilingKey("a"));
         if (!Settings.onJavaSE)
            fail("13");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, subMap2.ceilingKey("a"));
         if (!Settings.onJavaSE)
            fail("14");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, subMap3.ceilingKey("a"));
         if (!Settings.onJavaSE)
            fail("15");
      }
      catch (NullPointerException exception) {}
      
      assertEquals(Collections.EMPTY_SET, subMap1.descendingKeySet());
      assertEquals(Collections.EMPTY_SET, subMap2.descendingKeySet());
      assertEquals(Collections.EMPTY_SET, subMap3.descendingKeySet());
      
      assertEquals(Collections.EMPTY_MAP, subMap1.descendingMap());
      assertEquals(Collections.EMPTY_MAP, subMap2.descendingMap());
      assertEquals(Collections.EMPTY_MAP, subMap3.descendingMap());
      
      subMap1.clear();
      subMap2.clear();
      subMap3.clear();
      
      assertEquals(null, subMap1.comparator());
      assertEquals(null, subMap2.comparator());
      assertEquals(null, subMap3.comparator());
      
      assertFalse(subMap1.containsKey("a"));
      assertFalse(subMap2.containsKey("a"));
      assertFalse(subMap3.containsKey("a"));
      
      assertFalse(subMap1.containsValue("b"));
      assertFalse(subMap2.containsValue("b"));
      assertFalse(subMap3.containsValue("b"));
      
      assertEquals(Collections.EMPTY_SET, subMap1.entrySet());
      assertEquals(Collections.EMPTY_SET, subMap2.entrySet());
      assertEquals(Collections.EMPTY_SET, subMap3.entrySet());
      
      assertEquals(null, subMap1.firstEntry());
      assertEquals(null, subMap2.firstEntry());
      assertEquals(null, subMap3.firstEntry());
      
      try
      {
         subMap1.firstKey();
         fail("16");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         subMap2.firstKey();
         fail("17");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         subMap3.firstKey();
         fail("18");
      }
      catch (NoSuchElementException exception) {}
      
      assertEquals(null, subMap1.floorEntry("a"));
      assertEquals(null, subMap2.floorEntry("a"));
      assertEquals(null, subMap3.floorEntry("a"));
      
      try
      {
         assertEquals(null, subMap1.floorKey("a"));
         if (!Settings.onJavaSE)
            fail("19");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, subMap2.floorKey("a"));
         if (!Settings.onJavaSE)
            fail("20");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, subMap3.floorKey("a"));
         if (!Settings.onJavaSE)
            fail("21");
      }
      catch (NullPointerException exception) {}
      
      assertEquals(null, subMap1.get("a"));
      assertEquals(null, subMap2.get("a"));
      assertEquals(null, subMap3.get("a"));
      
      assertEquals(Collections.EMPTY_MAP, subMap1.headMap("a"));
      assertEquals(Collections.EMPTY_MAP, subMap2.headMap("a"));
      assertEquals(Collections.EMPTY_MAP, subMap3.headMap("a"));
      
      assertEquals(Collections.EMPTY_MAP, subMap1.headMap("a", false));
      assertEquals(Collections.EMPTY_MAP, subMap2.headMap("a", false));
      assertEquals(Collections.EMPTY_MAP, subMap3.headMap("a", false));
      
      assertEquals(Collections.EMPTY_SET, subMap1.keySet());
      assertEquals(Collections.EMPTY_SET, subMap2.keySet());
      assertEquals(Collections.EMPTY_SET, subMap3.keySet());
      
      assertEquals(null, subMap1.higherEntry("a"));
      assertEquals(null, subMap2.higherEntry("a"));
      assertEquals(null, subMap3.higherEntry("a"));
      
      try
      {
         assertEquals(null, subMap1.higherKey("a"));
         if (!Settings.onJavaSE)
            fail("22");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, subMap2.higherKey("a"));
         if (!Settings.onJavaSE)
            fail("23");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, subMap3.higherKey("a"));
         if (!Settings.onJavaSE)
            fail("24");
      }
      catch (NullPointerException exception) {}
      
      assertEquals(null, subMap1.lastEntry());
      assertEquals(null, subMap2.lastEntry());
      assertEquals(null, subMap3.lastEntry());
      
      try
      {
         subMap1.lastKey();
         fail("25");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         subMap2.lastKey();
         fail("26");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         subMap3.lastKey();
         fail("27");
      }
      catch (NoSuchElementException exception) {}
      
      assertEquals(null, subMap1.lowerEntry("a"));
      assertEquals(null, subMap2.lowerEntry("a"));
      assertEquals(null, subMap3.lowerEntry("a"));
      
      try
      {
         assertEquals(null, subMap1.lowerKey("a"));
         if (!Settings.onJavaSE)
            fail("28");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, subMap2.lowerKey("a"));
         if (!Settings.onJavaSE)
            fail("29");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, subMap3.lowerKey("a"));
         if (!Settings.onJavaSE)
            fail("30");
      }
      catch (NullPointerException exception) {}
      
      NavigableSet set1 = subMap1.navigableKeySet();
      NavigableSet set2 = subMap2.navigableKeySet();
      NavigableSet set3 = subMap3.navigableKeySet();
      
      assertEquals(Collections.EMPTY_SET, set1);
      assertEquals(Collections.EMPTY_SET, set2);
      assertEquals(Collections.EMPTY_SET, set3);
      
      assertEquals(null, subMap1.pollFirstEntry());
      assertEquals(null, subMap2.pollFirstEntry());
      assertEquals(null, subMap3.pollFirstEntry());
      
      assertEquals(null, subMap1.pollLastEntry());
      assertEquals(null, subMap2.pollLastEntry());
      assertEquals(null, subMap3.pollLastEntry());
      
      assertEquals(null, subMap1.put("a", "b"));
      assertEquals(null, subMap2.put("a", "b"));
      assertEquals(null, subMap3.put("a", "b"));
      
      assertEquals("b", subMap1.remove("a"));
      assertEquals("b", subMap2.remove("a"));
      assertEquals("b", subMap3.remove("a"));
      
      assertEquals(0, subMap1.size());
      assertEquals(0, subMap2.size());
      assertEquals(0, subMap3.size());
      
      assertEquals(Collections.EMPTY_MAP, subMap1.subMap("a", "b"));
      assertEquals(Collections.EMPTY_MAP, subMap2.subMap("a", "b"));
      assertEquals(Collections.EMPTY_MAP, subMap3.subMap("a", "b"));
      
      assertEquals(Collections.EMPTY_MAP, subMap1.subMap("a", true, "b", false));
      assertEquals(Collections.EMPTY_MAP, subMap2.subMap("a", true, "b", false));
      assertEquals(Collections.EMPTY_MAP, subMap3.subMap("a", true, "b", false));
      
      assertEquals(Collections.EMPTY_MAP, subMap1.tailMap("a"));
      assertEquals(Collections.EMPTY_MAP, subMap2.tailMap("a"));
      assertEquals(Collections.EMPTY_MAP, subMap3.tailMap("a"));
      
      assertEquals(Collections.EMPTY_MAP, subMap1.tailMap("a", true));
      assertEquals(Collections.EMPTY_MAP, subMap2.tailMap("a", true));
      assertEquals(Collections.EMPTY_MAP, subMap3.tailMap("a", true));
      
      assertEquals(0, (values1 = subMap1.values()).size());
      assertEquals(0, (values2 = subMap2.values()).size());
      assertEquals(0, (values3 = subMap3.values()).size());
      
      assertFalse(values1.iterator().hasNext());
      assertFalse(values2.iterator().hasNext());
      assertFalse(values3.iterator().hasNext());
      
      values1.clear();
      values2.clear();
      values3.clear();
      
      assertEquals(0, set1.size());
      assertEquals(0, set2.size());
      assertEquals(0, set3.size());
      
      assertFalse(set1.iterator().hasNext());
      assertFalse(set2.iterator().hasNext());
      assertFalse(set3.iterator().hasNext());
      
      set1.clear();
      set2.clear();
      set3.clear();
      
      assertFalse(set1.contains("a"));
      assertFalse(set2.contains("a"));
      assertFalse(set3.contains("a"));
      
      assertFalse(set1.remove("a"));
      assertFalse(set2.remove("a"));
      assertFalse(set3.remove("a"));
      
      try
      {
         assertEquals(null, set1.ceiling("a"));
         if (!Settings.onJavaSE)
            fail("31");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set2.ceiling("a"));
         if (!Settings.onJavaSE)
            fail("32");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set3.ceiling("a"));
         if (!Settings.onJavaSE)
            fail("33");
      }
      catch (NullPointerException exception) {}
      
      assertEquals(null, set1.comparator());
      assertEquals(null, set2.comparator());
      assertEquals(null, set3.comparator());
      
      try
      {
         assertFalse(set1.descendingIterator().hasNext());
         if (!Settings.onJavaSE)
            fail("34");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         assertFalse(set2.descendingIterator().hasNext());
         if (!Settings.onJavaSE)
            fail("35");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         assertFalse(set3.descendingIterator().hasNext());
         if (!Settings.onJavaSE)
            fail("36");
      }
      catch (NoSuchElementException exception) {}
      
      assertEquals(Collections.EMPTY_SET, set1.descendingSet());
      assertEquals(Collections.EMPTY_SET, set2.descendingSet());
      assertEquals(Collections.EMPTY_SET, set3.descendingSet());
      
      try
      {
         set1.first();
         fail("37");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         set2.first();
         fail("38");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         set3.first();
         fail("39");
      }
      catch (NoSuchElementException exception) {}
      
      try
      {
         assertEquals(null, set1.floor("a"));
         if (!Settings.onJavaSE)
            fail("40");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set2.floor("a"));
         if (!Settings.onJavaSE)
            fail("41");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set3.floor("a"));
         if (!Settings.onJavaSE)
            fail("42");
      }
      catch (NullPointerException exception) {}
      
      assertEquals(Collections.EMPTY_SET, set1.headSet("a"));
      assertEquals(Collections.EMPTY_SET, set2.headSet("a"));
      assertEquals(Collections.EMPTY_SET, set3.headSet("a"));
      
      assertEquals(Collections.EMPTY_SET, set1.headSet("a", false));
      assertEquals(Collections.EMPTY_SET, set2.headSet("a", false));
      assertEquals(Collections.EMPTY_SET, set3.headSet("a", false));
      
      try
      {
         assertEquals(null, set1.higher("a"));
         if (!Settings.onJavaSE)
            fail("43");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set2.higher("a"));
         if (!Settings.onJavaSE)
            fail("44");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set3.higher("a"));
         if (!Settings.onJavaSE)
            fail("45");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         set1.last();
         fail("46");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         set2.last();
         fail("47");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         set3.last();
         fail("48");
      }
      catch (NoSuchElementException exception) {}
      
      try
      {
         assertEquals(null, set1.lower("a"));
         if (!Settings.onJavaSE)
            fail("49");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set2.lower("a"));
         if (!Settings.onJavaSE)
            fail("50");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set3.lower("a"));
         if (!Settings.onJavaSE)
            fail("51");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         assertEquals(null, set1.pollFirst());
         if (!Settings.onJavaSE)
            fail("52");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set2.pollFirst());
         if (!Settings.onJavaSE)
            fail("53");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set3.pollFirst());
         if (!Settings.onJavaSE)
            fail("54");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         assertEquals(null, set1.pollLast());
         if (!Settings.onJavaSE)
            fail("55");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set2.pollLast());
         if (!Settings.onJavaSE)
            fail("56");
      }
      catch (NullPointerException exception) {}
      try
      {
         assertEquals(null, set3.pollLast());
         if (!Settings.onJavaSE)
            fail("57");
      }
      catch (NullPointerException exception) {}
      
      assertEquals(Collections.EMPTY_SET, set1.subSet("a", "c"));
      assertEquals(Collections.EMPTY_SET, set2.subSet("a", "c"));
      assertEquals(Collections.EMPTY_SET, set3.subSet("a", "c"));
      
      assertEquals(Collections.EMPTY_SET, set1.subSet("a", true, "c", false));
      assertEquals(Collections.EMPTY_SET, set2.subSet("a", true, "c", false));
      assertEquals(Collections.EMPTY_SET, set3.subSet("a", true, "c", false));
      
      assertEquals(Collections.EMPTY_SET, set1.tailSet("a"));
      assertEquals(Collections.EMPTY_SET, set2.tailSet("a"));
      assertEquals(Collections.EMPTY_SET, set3.tailSet("a"));
      
      assertEquals(Collections.EMPTY_SET, set1.tailSet("a", true));
      assertEquals(Collections.EMPTY_SET, set2.tailSet("a", true));
      assertEquals(Collections.EMPTY_SET, set3.tailSet("a", true));
      
      assertEquals(0, eSet1.size());
      assertEquals(0, eSet2.size());
      assertEquals(0, eSet3.size());
      
      assertFalse(eSet1.iterator().hasNext());
      assertFalse(eSet2.iterator().hasNext());
      assertFalse(eSet3.iterator().hasNext());
      
      eSet1.clear();
      eSet2.clear();
      eSet3.clear();
      
      assertFalse(eSet1.contains("a"));
      assertFalse(eSet2.contains("a"));
      assertFalse(eSet3.contains("a"));
      
      assertFalse(eSet1.remove("a"));
      assertFalse(eSet2.remove("a"));
      assertFalse(eSet3.remove("a"));
      
      assertEquals(0, eSet1.size());
      assertEquals(0, eSet2.size());
      assertEquals(0, eSet3.size());
      
      assertFalse(eSet1.iterator().hasNext());
      assertFalse(eSet2.iterator().hasNext());
      assertFalse(eSet3.iterator().hasNext());
      
      eSet1.clear();
      eSet2.clear();
      eSet3.clear();
      
      assertFalse(eSet1.contains("a"));
      assertFalse(eSet2.contains("a"));
      assertFalse(eSet3.contains("a"));
      
      assertFalse(eSet1.remove("a"));
      assertFalse(eSet2.remove("a"));
      assertFalse(eSet3.remove("a"));
      
      assertEquals(null, ((NavigableSet)eSet1).ceiling("a"));
      assertEquals(null, set2.ceiling("a"));
      assertEquals(null, set3.ceiling("a"));
      
      assertEquals(null, set1.comparator());
      assertEquals(null, set2.comparator());
      assertEquals(null, set3.comparator());
      
      assertFalse(set1.descendingIterator().hasNext());
      assertFalse(set2.descendingIterator().hasNext());
      assertFalse(set3.descendingIterator().hasNext());
      
      assertEquals(Collections.EMPTY_SET, set1.descendingSet());
      assertEquals(Collections.EMPTY_SET, set2.descendingSet());
      assertEquals(Collections.EMPTY_SET, set3.descendingSet());
      
      try
      {
         set1.first();
         fail("19");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         set2.first();
         fail("20");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         set3.first();
         fail("21");
      }
      catch (NoSuchElementException exception) {}
      
      assertEquals(null, set1.floor("a"));
      assertEquals(null, set2.floor("a"));
      assertEquals(null, set3.floor("a"));
      
      assertEquals(Collections.EMPTY_SET, set1.headSet("a"));
      assertEquals(Collections.EMPTY_SET, set2.headSet("a"));
      assertEquals(Collections.EMPTY_SET, set3.headSet("a"));
      
      assertEquals(Collections.EMPTY_SET, set1.headSet("a", true));
      assertEquals(Collections.EMPTY_SET, set2.headSet("a", true));
      assertEquals(Collections.EMPTY_SET, set3.headSet("a", true));
      
      assertEquals(null, set1.higher("a"));
      assertEquals(null, set2.higher("a"));
      assertEquals(null, set3.higher("a"));
      
      try
      {
         set1.last();
         fail("22");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         set2.last();
         fail("23");
      }
      catch (NoSuchElementException exception) {}
      try
      {
         set3.last();
         fail("24");
      }
      catch (NoSuchElementException exception) {}
      
      assertEquals(null, set1.lower("a"));
      assertEquals(null, set2.lower("a"));
      assertEquals(null, set3.lower("a"));
      
      assertEquals(null, set1.pollFirst());
      assertEquals(null, set2.pollFirst());
      assertEquals(null, set3.pollFirst());
      
      assertEquals(null, set1.pollLast());
      assertEquals(null, set2.pollLast());
      assertEquals(null, set3.pollLast());
      
      assertEquals(Collections.EMPTY_SET, set1.subSet("a", "c"));
      assertEquals(Collections.EMPTY_SET, set2.subSet("a", "c"));
      assertEquals(Collections.EMPTY_SET, set3.subSet("a", "c"));
      
      assertEquals(Collections.EMPTY_SET, set1.subSet("a", true, "c", false));
      assertEquals(Collections.EMPTY_SET, set2.subSet("a", true, "c", false));
      assertEquals(Collections.EMPTY_SET, set3.subSet("a", true, "c", false));
      
      assertEquals(Collections.EMPTY_SET, set1.tailSet("a"));
      assertEquals(Collections.EMPTY_SET, set2.tailSet("a"));
      assertEquals(Collections.EMPTY_SET, set3.tailSet("a"));
      
      assertEquals(Collections.EMPTY_SET, set1.tailSet("a", true));
      assertEquals(Collections.EMPTY_SET, set2.tailSet("a", true));
      assertEquals(Collections.EMPTY_SET, set3.tailSet("a", true));
      
      assertEquals(0, eSet1.size());
      assertEquals(0, eSet2.size());
      assertEquals(0, eSet3.size());
      
      assertFalse(eSet1.iterator().hasNext());
      assertFalse(eSet2.iterator().hasNext());
      assertFalse(eSet3.iterator().hasNext());
      
      eSet1.clear();
      eSet2.clear();
      eSet3.clear();
      
      assertFalse(eSet1.contains("a"));
      assertFalse(eSet2.contains("a"));
      assertFalse(eSet3.contains("a"));
      
      assertFalse(eSet1.remove("a"));
      assertFalse(eSet2.remove("a"));
      assertFalse(eSet3.remove("a"));
   }
}

class Test32 extends TreeMap implements Cloneable
{
   public Test32()
   {
      super();
   }
   public Test32(Comparator c)
   {
      super(c);
   }
   public Test32(Map m)
   {
      super(m);
   }
   public Test32(SortedMap sm)
   {
      super(sm);
   }
}