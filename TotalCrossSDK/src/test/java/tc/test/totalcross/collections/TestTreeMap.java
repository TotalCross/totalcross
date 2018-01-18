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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestTreeMap extends TestCase {
  @Override
  public void testRun() {
    Test32 test1 = new Test32();
    TreeMap test2 = new Test32((Comparator) null);
    AbstractMap test3 = new Test32(Collections.emptyMap());

    test1.clear();
    test2.clear();
    test3.clear();

    assertEquals(test1, test1.clone());
    assertEquals(test2, test2.clone());
    assertEquals(test3, ((TreeMap) test3).clone());

    assertEquals(null, test1.comparator());
    assertEquals(null, test2.comparator());
    assertEquals(null, ((TreeMap) test3).comparator());

    assertFalse(test1.containsKey("a"));
    assertFalse(test2.containsKey("a"));
    assertFalse(test3.containsKey("a"));

    assertFalse(test1.containsValue("b"));
    assertFalse(test2.containsValue("b"));
    assertFalse(test3.containsValue("b"));

    Set eSet11 = test1.entrySet();
    Set eSet21 = test2.entrySet();
    Set eSet31 = test3.entrySet();

    assertEquals(Collections.EMPTY_SET, eSet11);
    assertEquals(Collections.EMPTY_SET, eSet21);
    assertEquals(Collections.EMPTY_SET, eSet31);

    try {
      test1.firstKey();
      fail("1");
    } catch (NoSuchElementException exception) {
    }
    try {
      test2.firstKey();
      fail("2");
    } catch (NoSuchElementException exception) {
    }
    try {
      ((TreeMap) test3).firstKey();
      fail("3");
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, test1.get("a"));
    assertEquals(null, test2.get("a"));
    assertEquals(null, test3.get("a"));

    assertEquals(Collections.EMPTY_MAP, test1.headMap("a"));
    assertEquals(Collections.EMPTY_MAP, test2.headMap("a"));
    assertEquals(Collections.EMPTY_MAP, ((TreeMap) test3).headMap("a"));

    assertEquals(Collections.EMPTY_MAP, test1.headMap("a", false));
    assertEquals(Collections.EMPTY_MAP, test2.headMap("a", false));
    assertEquals(Collections.EMPTY_MAP, ((TreeMap) test3).headMap("a", false));

    AbstractSet kSet1 = (AbstractSet) test1.keySet();
    AbstractSet kSet2 = (AbstractSet) test2.keySet();
    AbstractSet kSet3 = (AbstractSet) test3.keySet();

    assertEquals(Collections.EMPTY_SET, kSet1);
    assertEquals(Collections.EMPTY_SET, kSet2);
    assertEquals(Collections.EMPTY_SET, kSet3);

    try {
      test1.lastKey();
      fail("4");
    } catch (NoSuchElementException exception) {
    }
    try {
      test2.lastKey();
      fail("5");
    } catch (NoSuchElementException exception) {
    }
    try {
      ((TreeMap) test3).lastKey();
      fail("6");
    } catch (NoSuchElementException exception) {
    }

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
    assertEquals(Collections.EMPTY_MAP, ((TreeMap) test3).subMap("a", "b"));

    assertEquals(Collections.EMPTY_MAP, test1.subMap("a", true, "b", false));
    assertEquals(Collections.EMPTY_MAP, test2.subMap("a", true, "b", false));
    assertEquals(Collections.EMPTY_MAP, ((TreeMap) test3).subMap("a", true, "b", false));

    assertEquals(Collections.EMPTY_MAP, test1.tailMap("a"));
    assertEquals(Collections.EMPTY_MAP, test2.tailMap("a"));
    assertEquals(Collections.EMPTY_MAP, ((TreeMap) test3).tailMap("a"));

    NavigableMap nMap1 = test1.tailMap("a", true);
    NavigableMap nMap2 = test2.tailMap("a", true);
    NavigableMap nMap3 = ((TreeMap) test3).tailMap("a", true);

    assertEquals(Collections.EMPTY_MAP, nMap1);
    assertEquals(Collections.EMPTY_MAP, nMap2);
    assertEquals(Collections.EMPTY_MAP, nMap3);

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

    try {
      iterator1.next();
      fail("7");
    } catch (NoSuchElementException exception) {
    }
    try {
      iterator2.next();
      fail("8");
    } catch (NoSuchElementException exception) {
    }
    try {
      iterator3.next();
      fail("9");
    } catch (NoSuchElementException exception) {
    }

    try {
      iterator1.remove();
      fail("10");
    } catch (IllegalStateException exception) {
    }
    try {
      iterator2.remove();
      fail("11");
    } catch (IllegalStateException exception) {
    }
    try {
      iterator3.remove();
      fail("12");
    } catch (IllegalStateException exception) {
    }

    assertEquals(null, nMap1.ceilingEntry("a"));
    assertEquals(null, nMap2.ceilingEntry("a"));
    assertEquals(null, nMap3.ceilingEntry("a"));

    try {
      assertEquals(null, nMap1.ceilingKey("a"));
      if (!Settings.onJavaSE) {
        fail("13");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, nMap2.ceilingKey("a"));
      if (!Settings.onJavaSE) {
        fail("14");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, nMap3.ceilingKey("a"));
      if (!Settings.onJavaSE) {
        fail("15");
      }
    } catch (NullPointerException exception) {
    }

    assertEquals(Collections.EMPTY_SET, nMap1.descendingKeySet());
    assertEquals(Collections.EMPTY_SET, nMap2.descendingKeySet());
    assertEquals(Collections.EMPTY_SET, nMap3.descendingKeySet());

    assertEquals(Collections.EMPTY_MAP, nMap1.descendingMap());
    assertEquals(Collections.EMPTY_MAP, nMap2.descendingMap());
    assertEquals(Collections.EMPTY_MAP, nMap3.descendingMap());

    nMap1.clear();
    nMap2.clear();
    nMap3.clear();

    assertEquals(null, nMap1.comparator());
    assertEquals(null, nMap2.comparator());
    assertEquals(null, nMap3.comparator());

    assertFalse(nMap1.containsKey("a"));
    assertFalse(nMap2.containsKey("a"));
    assertFalse(nMap3.containsKey("a"));

    assertFalse(nMap1.containsValue("b"));
    assertFalse(nMap2.containsValue("b"));
    assertFalse(nMap3.containsValue("b"));

    Set eSet1 = nMap1.entrySet();
    Set eSet2 = nMap2.entrySet();
    Set eSet3 = nMap3.entrySet();

    assertEquals(Collections.EMPTY_SET, nMap1.entrySet());
    assertEquals(Collections.EMPTY_SET, nMap2.entrySet());
    assertEquals(Collections.EMPTY_SET, nMap3.entrySet());

    assertEquals(null, nMap1.firstEntry());
    assertEquals(null, nMap2.firstEntry());
    assertEquals(null, nMap3.firstEntry());

    try {
      nMap1.firstKey();
      fail("16");
    } catch (NoSuchElementException exception) {
    }
    try {
      nMap2.firstKey();
      fail("17");
    } catch (NoSuchElementException exception) {
    }
    try {
      nMap3.firstKey();
      fail("18");
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, nMap1.floorEntry("a"));
    assertEquals(null, nMap2.floorEntry("a"));
    assertEquals(null, nMap3.floorEntry("a"));

    try {
      assertEquals(null, nMap1.floorKey("a"));
      if (!Settings.onJavaSE) {
        fail("19");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, nMap2.floorKey("a"));
      if (!Settings.onJavaSE) {
        fail("20");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, nMap3.floorKey("a"));
      if (!Settings.onJavaSE) {
        fail("21");
      }
    } catch (NullPointerException exception) {
    }

    assertEquals(null, nMap1.get("a"));
    assertEquals(null, nMap2.get("a"));
    assertEquals(null, nMap3.get("a"));

    assertEquals(Collections.EMPTY_MAP, nMap1.headMap("a"));
    assertEquals(Collections.EMPTY_MAP, nMap2.headMap("a"));
    assertEquals(Collections.EMPTY_MAP, nMap3.headMap("a"));

    assertEquals(Collections.EMPTY_MAP, nMap1.headMap("a", false));
    assertEquals(Collections.EMPTY_MAP, nMap2.headMap("a", false));
    assertEquals(Collections.EMPTY_MAP, nMap3.headMap("a", false));

    assertEquals(Collections.EMPTY_SET, nMap1.keySet());
    assertEquals(Collections.EMPTY_SET, nMap2.keySet());
    assertEquals(Collections.EMPTY_SET, nMap3.keySet());

    assertEquals(null, nMap1.higherEntry("a"));
    assertEquals(null, nMap2.higherEntry("a"));
    assertEquals(null, nMap3.higherEntry("a"));

    try {
      assertEquals(null, nMap1.higherKey("a"));
      if (!Settings.onJavaSE) {
        fail("22");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, nMap2.higherKey("a"));
      if (!Settings.onJavaSE) {
        fail("23");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, nMap3.higherKey("a"));
      if (!Settings.onJavaSE) {
        fail("24");
      }
    } catch (NullPointerException exception) {
    }

    assertEquals(null, nMap1.lastEntry());
    assertEquals(null, nMap2.lastEntry());
    assertEquals(null, nMap3.lastEntry());

    try {
      nMap1.lastKey();
      fail("25");
    } catch (NoSuchElementException exception) {
    }
    try {
      nMap2.lastKey();
      fail("26");
    } catch (NoSuchElementException exception) {
    }
    try {
      nMap3.lastKey();
      fail("27");
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, nMap1.lowerEntry("a"));
    assertEquals(null, nMap2.lowerEntry("a"));
    assertEquals(null, nMap3.lowerEntry("a"));

    try {
      assertEquals(null, nMap1.lowerKey("a"));
      if (!Settings.onJavaSE) {
        fail("28");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, nMap2.lowerKey("a"));
      if (!Settings.onJavaSE) {
        fail("29");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, nMap3.lowerKey("a"));
      if (!Settings.onJavaSE) {
        fail("30");
      }
    } catch (NullPointerException exception) {
    }

    NavigableSet set1 = nMap1.navigableKeySet();
    NavigableSet set2 = nMap2.navigableKeySet();
    NavigableSet set3 = nMap3.navigableKeySet();

    assertEquals(Collections.EMPTY_SET, set1);
    assertEquals(Collections.EMPTY_SET, set2);
    assertEquals(Collections.EMPTY_SET, set3);

    assertEquals(null, nMap1.pollFirstEntry());
    assertEquals(null, nMap2.pollFirstEntry());
    assertEquals(null, nMap3.pollFirstEntry());

    assertEquals(null, nMap1.pollLastEntry());
    assertEquals(null, nMap2.pollLastEntry());
    assertEquals(null, nMap3.pollLastEntry());

    assertEquals(null, nMap1.put("a", "b"));
    assertEquals(null, nMap2.put("a", "b"));
    assertEquals(null, nMap3.put("a", "b"));

    assertEquals("b", nMap1.remove("a"));
    assertEquals("b", nMap2.remove("a"));
    assertEquals("b", nMap3.remove("a"));

    assertEquals(0, nMap1.size());
    assertEquals(0, nMap2.size());
    assertEquals(0, nMap3.size());

    assertEquals(Collections.EMPTY_MAP, nMap1.subMap("a", "b"));
    assertEquals(Collections.EMPTY_MAP, nMap2.subMap("a", "b"));
    assertEquals(Collections.EMPTY_MAP, nMap3.subMap("a", "b"));

    assertEquals(Collections.EMPTY_MAP, nMap1.subMap("a", true, "b", false));
    assertEquals(Collections.EMPTY_MAP, nMap2.subMap("a", true, "b", false));
    assertEquals(Collections.EMPTY_MAP, nMap3.subMap("a", true, "b", false));

    assertEquals(Collections.EMPTY_MAP, nMap1.tailMap("a"));
    assertEquals(Collections.EMPTY_MAP, nMap2.tailMap("a"));
    assertEquals(Collections.EMPTY_MAP, nMap3.tailMap("a"));

    assertEquals(Collections.EMPTY_MAP, nMap1.tailMap("a", true));
    assertEquals(Collections.EMPTY_MAP, nMap2.tailMap("a", true));
    assertEquals(Collections.EMPTY_MAP, nMap3.tailMap("a", true));

    assertEquals(0, (values1 = nMap1.values()).size());
    assertEquals(0, (values2 = nMap2.values()).size());
    assertEquals(0, (values3 = nMap3.values()).size());

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

    try {
      assertEquals(null, set1.ceiling("a"));
      if (!Settings.onJavaSE) {
        fail("31");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set2.ceiling("a"));
      if (!Settings.onJavaSE) {
        fail("32");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set3.ceiling("a"));
      if (!Settings.onJavaSE) {
        fail("33");
      }
    } catch (NullPointerException exception) {
    }

    assertEquals(null, set1.comparator());
    assertEquals(null, set2.comparator());
    assertEquals(null, set3.comparator());

    try {
      assertFalse(set1.descendingIterator().hasNext());
      if (!Settings.onJavaSE) {
        fail("34");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertFalse(set2.descendingIterator().hasNext());
      if (!Settings.onJavaSE) {
        fail("35");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertFalse(set3.descendingIterator().hasNext());
      if (!Settings.onJavaSE) {
        fail("36");
      }
    } catch (NoSuchElementException exception) {
    }

    assertEquals(Collections.EMPTY_SET, set1.descendingSet());
    assertEquals(Collections.EMPTY_SET, set2.descendingSet());
    assertEquals(Collections.EMPTY_SET, set3.descendingSet());

    try {
      set1.first();
      fail("37");
    } catch (NoSuchElementException exception) {
    }
    try {
      set2.first();
      fail("38");
    } catch (NoSuchElementException exception) {
    }
    try {
      set3.first();
      fail("39");
    } catch (NoSuchElementException exception) {
    }

    try {
      assertEquals(null, set1.floor("a"));
      if (!Settings.onJavaSE) {
        fail("40");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set2.floor("a"));
      if (!Settings.onJavaSE) {
        fail("41");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set3.floor("a"));
      if (!Settings.onJavaSE) {
        fail("42");
      }
    } catch (NullPointerException exception) {
    }

    assertEquals(Collections.EMPTY_SET, set1.headSet("a"));
    assertEquals(Collections.EMPTY_SET, set2.headSet("a"));
    assertEquals(Collections.EMPTY_SET, set3.headSet("a"));

    assertEquals(Collections.EMPTY_SET, set1.headSet("a", false));
    assertEquals(Collections.EMPTY_SET, set2.headSet("a", false));
    assertEquals(Collections.EMPTY_SET, set3.headSet("a", false));

    try {
      assertEquals(null, set1.higher("a"));
      if (!Settings.onJavaSE) {
        fail("43");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set2.higher("a"));
      if (!Settings.onJavaSE) {
        fail("44");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set3.higher("a"));
      if (!Settings.onJavaSE) {
        fail("45");
      }
    } catch (NullPointerException exception) {
    }

    try {
      set1.last();
      fail("46");
    } catch (NoSuchElementException exception) {
    }
    try {
      set2.last();
      fail("47");
    } catch (NoSuchElementException exception) {
    }
    try {
      set3.last();
      fail("48");
    } catch (NoSuchElementException exception) {
    }

    try {
      assertEquals(null, set1.lower("a"));
      if (!Settings.onJavaSE) {
        fail("49");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set2.lower("a"));
      if (!Settings.onJavaSE) {
        fail("50");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set3.lower("a"));
      if (!Settings.onJavaSE) {
        fail("51");
      }
    } catch (NullPointerException exception) {
    }

    try {
      assertEquals(null, set1.pollFirst());
      if (!Settings.onJavaSE) {
        fail("52");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set2.pollFirst());
      if (!Settings.onJavaSE) {
        fail("53");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set3.pollFirst());
      if (!Settings.onJavaSE) {
        fail("54");
      }
    } catch (NullPointerException exception) {
    }

    try {
      assertEquals(null, set1.pollLast());
      if (!Settings.onJavaSE) {
        fail("55");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set2.pollLast());
      if (!Settings.onJavaSE) {
        fail("56");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set3.pollLast());
      if (!Settings.onJavaSE) {
        fail("57");
      }
    } catch (NullPointerException exception) {
    }

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

    Map.Entry e = new AbstractMap.SimpleEntry<String, String>("a", "b");

    if (!Settings.onJavaSE) {
      assertEquals(null, ((NavigableSet) eSet1).ceiling(e));
      assertEquals(null, ((NavigableSet) eSet2).ceiling(e));
      assertEquals(null, ((NavigableSet) eSet3).ceiling(e));

      assertTrue(((NavigableSet) eSet1).comparator() instanceof Comparator);
      assertTrue(((NavigableSet) eSet2).comparator() instanceof Comparator);
      assertTrue(((NavigableSet) eSet3).comparator() instanceof Comparator);

      assertFalse(((NavigableSet) eSet1).descendingIterator().hasNext());
      assertFalse(((NavigableSet) eSet2).descendingIterator().hasNext());
      assertFalse(((NavigableSet) eSet3).descendingIterator().hasNext());

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet1).descendingSet());
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet2).descendingSet());
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet3).descendingSet());

      assertEquals(null, ((NavigableSet) eSet1).first());
      assertEquals(null, ((NavigableSet) eSet2).first());
      assertEquals(null, ((NavigableSet) eSet3).first());

      assertEquals(null, ((NavigableSet) eSet1).floor(e));
      assertEquals(null, ((NavigableSet) eSet2).floor(e));
      assertEquals(null, ((NavigableSet) eSet3).floor(e));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet1).headSet(e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet2).headSet(e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet3).headSet(e));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet1).headSet(e, false));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet2).headSet(e, false));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet3).headSet(e, false));

      assertEquals(null, ((NavigableSet) eSet1).higher(e));
      assertEquals(null, ((NavigableSet) eSet2).higher(e));
      assertEquals(null, ((NavigableSet) eSet3).higher(e));

      assertEquals(null, ((NavigableSet) eSet1).last());
      assertEquals(null, ((NavigableSet) eSet2).last());
      assertEquals(null, ((NavigableSet) eSet3).last());

      assertEquals(null, ((NavigableSet) eSet1).lower(e));
      assertEquals(null, ((NavigableSet) eSet2).lower(e));
      assertEquals(null, ((NavigableSet) eSet3).lower(e));

      assertEquals(null, ((NavigableSet) eSet1).pollFirst());
      assertEquals(null, ((NavigableSet) eSet2).pollFirst());
      assertEquals(null, ((NavigableSet) eSet3).pollFirst());

      assertEquals(null, ((NavigableSet) eSet1).pollLast());
      assertEquals(null, ((NavigableSet) eSet2).pollLast());
      assertEquals(null, ((NavigableSet) eSet3).pollLast());

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet1).subSet(e, e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet2).subSet(e, e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet3).subSet(e, e));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet1).subSet(e, true, e, false));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet2).subSet(e, true, e, false));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet3).subSet(e, true, e, false));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet1).tailSet(e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet2).tailSet(e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet3).tailSet(e));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet1).tailSet(e, true));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet2).tailSet(e, true));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet3).tailSet(e, true));
    }

    assertEquals(null, test1.ceilingEntry("a"));
    assertEquals(null, test2.ceilingEntry("a"));
    assertEquals(null, ((TreeMap) test3).ceilingEntry("a"));

    assertEquals(null, test1.ceilingKey("a"));
    assertEquals(null, test2.ceilingKey("a"));
    assertEquals(null, ((TreeMap) test3).ceilingKey("a"));

    assertEquals(Collections.EMPTY_SET, test1.descendingKeySet());
    assertEquals(Collections.EMPTY_SET, test2.descendingKeySet());
    assertEquals(Collections.EMPTY_SET, ((TreeMap) test3).descendingKeySet());

    assertEquals(Collections.EMPTY_MAP, nMap1 = test1.descendingMap());
    assertEquals(Collections.EMPTY_MAP, nMap2 = test2.descendingMap());
    assertEquals(Collections.EMPTY_MAP, nMap3 = ((TreeMap) test3).descendingMap());

    assertEquals(null, test1.firstEntry());
    assertEquals(null, test2.firstEntry());
    assertEquals(null, ((TreeMap) test3).firstEntry());

    assertEquals(null, test1.floorEntry("a"));
    assertEquals(null, test2.floorEntry("a"));
    assertEquals(null, ((TreeMap) test3).floorEntry("a"));

    assertEquals(null, test1.floorKey("a"));
    assertEquals(null, test2.floorKey("a"));
    assertEquals(null, ((TreeMap) test3).floorKey("a"));

    assertEquals(null, test1.higherEntry("a"));
    assertEquals(null, test2.higherEntry("a"));
    assertEquals(null, ((TreeMap) test3).higherEntry("a"));

    assertEquals(null, test1.higherKey("a"));
    assertEquals(null, test2.higherKey("a"));
    assertEquals(null, ((TreeMap) test3).higherKey("a"));

    assertEquals(null, test1.lastEntry());
    assertEquals(null, test2.lastEntry());
    assertEquals(null, ((TreeMap) test3).lastEntry());

    assertEquals(null, test1.lowerEntry("a"));
    assertEquals(null, test2.lowerEntry("a"));
    assertEquals(null, ((TreeMap) test3).lowerEntry("a"));

    assertEquals(null, test1.lowerKey("a"));
    assertEquals(null, test2.lowerKey("a"));
    assertEquals(null, ((TreeMap) test3).lowerKey("a"));

    assertEquals(Collections.EMPTY_SET, set1 = test1.navigableKeySet());
    assertEquals(Collections.EMPTY_SET, set2 = test2.navigableKeySet());
    assertEquals(Collections.EMPTY_SET, set3 = ((TreeMap) test3).navigableKeySet());

    assertEquals(null, test1.pollFirstEntry());
    assertEquals(null, test2.pollFirstEntry());
    assertEquals(null, ((TreeMap) test3).pollFirstEntry());

    assertEquals(null, test1.pollLastEntry());
    assertEquals(null, test2.pollLastEntry());
    assertEquals(null, ((TreeMap) test3).pollLastEntry());

    assertEquals(null, nMap1.ceilingEntry("a"));
    assertEquals(null, nMap2.ceilingEntry("a"));
    assertEquals(null, nMap3.ceilingEntry("a"));

    assertEquals(null, nMap1.ceilingKey("a"));
    assertEquals(null, nMap2.ceilingKey("a"));
    assertEquals(null, nMap3.ceilingKey("a"));

    nMap1.clear();
    nMap2.clear();
    nMap3.clear();

    assertTrue(nMap1.comparator() instanceof Comparator);
    assertTrue(nMap2.comparator() instanceof Comparator);
    assertTrue(nMap3.comparator() instanceof Comparator);

    assertFalse(nMap1.containsKey("a"));
    assertFalse(nMap2.containsKey("a"));
    assertFalse(nMap3.containsKey("a"));

    assertFalse(nMap1.containsValue("b"));
    assertFalse(nMap2.containsValue("b"));
    assertFalse(nMap3.containsValue("b"));

    assertEquals(Collections.EMPTY_SET, eSet1 = nMap1.descendingKeySet());
    assertEquals(Collections.EMPTY_SET, eSet2 = nMap2.descendingKeySet());
    assertEquals(Collections.EMPTY_SET, eSet3 = nMap3.descendingKeySet());

    assertEquals(Collections.EMPTY_MAP, nMap1.descendingMap());
    assertEquals(Collections.EMPTY_MAP, nMap2.descendingMap());
    assertEquals(Collections.EMPTY_MAP, nMap3.descendingMap());

    assertEquals(Collections.EMPTY_SET, nMap1.entrySet());
    assertEquals(Collections.EMPTY_SET, nMap2.entrySet());
    assertEquals(Collections.EMPTY_SET, nMap3.entrySet());

    assertTrue(nMap1.equals(nMap2));
    assertTrue(nMap2.equals(nMap3));
    assertTrue(nMap3.equals(nMap1));

    assertEquals(null, nMap1.firstEntry());
    assertEquals(null, nMap2.firstEntry());
    assertEquals(null, nMap3.firstEntry());

    try {
      assertEquals(null, nMap1.firstKey());
      if (!Settings.onJavaSE) {
        fail("58");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertEquals(null, nMap2.firstKey());
      if (!Settings.onJavaSE) {
        fail("59");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertEquals(null, nMap3.firstKey());
      if (!Settings.onJavaSE) {
        fail("60");
      }
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, nMap1.floorEntry("a"));
    assertEquals(null, nMap2.floorEntry("a"));
    assertEquals(null, nMap3.floorEntry("a"));

    assertEquals(null, nMap1.floorKey("a"));
    assertEquals(null, nMap2.floorKey("a"));
    assertEquals(null, nMap3.floorKey("a"));

    assertEquals(null, nMap1.get("a"));
    assertEquals(null, nMap2.get("a"));
    assertEquals(null, nMap3.get("a"));

    assertEquals(0, nMap1.hashCode());
    assertEquals(0, nMap2.hashCode());
    assertEquals(0, nMap3.hashCode());

    assertEquals(Collections.EMPTY_MAP, nMap1.headMap("a"));
    assertEquals(Collections.EMPTY_MAP, nMap2.headMap("a"));
    assertEquals(Collections.EMPTY_MAP, nMap3.headMap("a"));

    assertEquals(Collections.EMPTY_MAP, nMap1.headMap("a", false));
    assertEquals(Collections.EMPTY_MAP, nMap2.headMap("a", false));
    assertEquals(Collections.EMPTY_MAP, nMap3.headMap("a", false));

    assertEquals(null, nMap1.higherEntry("a"));
    assertEquals(null, nMap2.higherEntry("a"));
    assertEquals(null, nMap3.higherEntry("a"));

    assertEquals(null, nMap1.higherKey("a"));
    assertEquals(null, nMap2.higherKey("a"));
    assertEquals(null, nMap3.higherKey("a"));

    assertEquals(Collections.EMPTY_SET, nMap1.keySet());
    assertEquals(Collections.EMPTY_SET, nMap2.keySet());
    assertEquals(Collections.EMPTY_SET, nMap3.keySet());

    assertTrue(nMap1.isEmpty());
    assertTrue(nMap2.isEmpty());
    assertTrue(nMap3.isEmpty());

    assertEquals(null, nMap1.lastEntry());
    assertEquals(null, nMap2.lastEntry());
    assertEquals(null, nMap3.lastEntry());

    try {
      nMap1.lastKey();
      fail("61");
    } catch (NoSuchElementException exception) {
    }
    try {
      nMap2.lastKey();
      fail("62");
    } catch (NoSuchElementException exception) {
    }
    try {
      nMap3.lastKey();
      fail("63");
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, nMap1.lowerEntry("a"));
    assertEquals(null, nMap2.lowerEntry("a"));
    assertEquals(null, nMap3.lowerEntry("a"));

    assertEquals(null, nMap1.lowerKey("a"));
    assertEquals(null, nMap2.lowerKey("a"));
    assertEquals(null, nMap3.lowerKey("a"));

    assertEquals(Collections.EMPTY_SET, nMap1.navigableKeySet());
    assertEquals(Collections.EMPTY_SET, nMap2.navigableKeySet());
    assertEquals(Collections.EMPTY_SET, nMap3.navigableKeySet());

    assertEquals(null, nMap1.pollFirstEntry());
    assertEquals(null, nMap2.pollFirstEntry());
    assertEquals(null, nMap3.pollFirstEntry());

    assertEquals(null, nMap1.pollLastEntry());
    assertEquals(null, nMap2.pollLastEntry());
    assertEquals(null, nMap3.pollLastEntry());

    assertEquals(null, nMap1.put("a", "b"));
    assertEquals(null, nMap2.put("a", "b"));
    assertEquals(null, nMap3.put("a", "b"));

    nMap1.putAll(nMap2);
    nMap2.putAll(nMap3);
    nMap3.putAll(nMap1);

    assertEquals("b", nMap1.remove("a"));
    assertEquals("b", nMap2.remove("a"));
    assertEquals("b", nMap3.remove("a"));

    assertEquals(0, nMap1.size());
    assertEquals(0, nMap2.size());
    assertEquals(0, nMap3.size());

    assertEquals(Collections.EMPTY_MAP, nMap1.subMap("c", "a"));
    assertEquals(Collections.EMPTY_MAP, nMap2.subMap("c", "a"));
    assertEquals(Collections.EMPTY_MAP, nMap3.subMap("c", "a"));

    assertEquals(Collections.EMPTY_MAP, nMap1.subMap("c", true, "a", false));
    assertEquals(Collections.EMPTY_MAP, nMap2.subMap("c", true, "a", false));
    assertEquals(Collections.EMPTY_MAP, nMap3.subMap("c", true, "a", false));

    assertEquals(Collections.EMPTY_MAP, nMap1.tailMap("a"));
    assertEquals(Collections.EMPTY_MAP, nMap2.tailMap("a"));
    assertEquals(Collections.EMPTY_MAP, nMap3.tailMap("a"));

    assertEquals(Collections.EMPTY_MAP, nMap1.tailMap("a", true));
    assertEquals(Collections.EMPTY_MAP, nMap2.tailMap("a", true));
    assertEquals(Collections.EMPTY_MAP, nMap3.tailMap("a", true));

    assertEquals("{}", nMap1.toString());
    assertEquals("{}", nMap2.toString());
    assertEquals("{}", nMap3.toString());

    assertEquals(0, (values1 = nMap1.values()).size());
    assertEquals(0, (values2 = nMap2.values()).size());
    assertEquals(0, (values3 = nMap3.values()).size());

    assertFalse((iterator1 = values1.iterator()).hasNext());
    assertFalse((iterator2 = values2.iterator()).hasNext());
    assertFalse((iterator3 = values3.iterator()).hasNext());

    try {
      iterator1.next();
      fail("64");
    } catch (NoSuchElementException exception) {
    }
    try {
      iterator2.next();
      fail("65");
    } catch (NoSuchElementException exception) {
    }
    try {
      iterator3.next();
      fail("66");
    } catch (NoSuchElementException exception) {
    }

    try {
      iterator1.remove();
      fail("67");
    } catch (IllegalStateException exception) {
    }
    try {
      iterator2.remove();
      fail("68");
    } catch (IllegalStateException exception) {
    }
    try {
      iterator3.remove();
      fail("69");
    } catch (IllegalStateException exception) {
    }

    values1.clear();
    values2.clear();
    values3.clear();

    assertEquals(0, kSet1.size());
    assertEquals(0, kSet2.size());
    assertEquals(0, kSet3.size());

    assertTrue(kSet1.iterator() instanceof Iterator);
    assertTrue(kSet2.iterator() instanceof Iterator);
    assertTrue(kSet3.iterator() instanceof Iterator);

    kSet1.clear();
    kSet2.clear();
    kSet3.clear();

    assertFalse(kSet1.contains("a"));
    assertFalse(kSet2.contains("a"));
    assertFalse(kSet3.contains("a"));

    assertFalse(kSet1.remove("a"));
    assertFalse(kSet2.remove("a"));
    assertFalse(kSet3.remove("a"));

    assertEquals(null, set1.ceiling("a"));
    assertEquals(null, set2.ceiling("a"));
    assertEquals(null, set3.ceiling("a"));

    assertEquals(null, set1.comparator());
    assertEquals(null, set2.comparator());
    assertEquals(null, set3.comparator());

    try {
      assertTrue(set1.descendingIterator() instanceof Iterator);
      if (!Settings.onJavaSE) {
        fail("70");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertTrue(set2.descendingIterator() instanceof Iterator);
      if (!Settings.onJavaSE) {
        fail("71");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertTrue(set3.descendingIterator() instanceof Iterator);
      if (!Settings.onJavaSE) {
        fail("72");
      }
    } catch (NoSuchElementException exception) {
    }

    NavigableSet dSet1 = set1.descendingSet();
    NavigableSet dSet2 = set2.descendingSet();
    NavigableSet dSet3 = set3.descendingSet();

    assertEquals(Collections.EMPTY_SET, dSet1);
    assertEquals(Collections.EMPTY_SET, dSet2);
    assertEquals(Collections.EMPTY_SET, dSet3);

    try {
      set1.first();
      fail("73");
    } catch (NoSuchElementException exception) {
    }
    try {
      set2.first();
      fail("74");
    } catch (NoSuchElementException exception) {
    }
    try {
      set3.first();
      fail("75");
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, set1.floor("a"));
    assertEquals(null, set2.floor("a"));
    assertEquals(null, set3.floor("a"));

    assertEquals(Collections.EMPTY_SET, set1.headSet("a"));
    assertEquals(Collections.EMPTY_SET, set2.headSet("a"));
    assertEquals(Collections.EMPTY_SET, set3.headSet("a"));

    assertEquals(Collections.EMPTY_SET, set1.headSet("a", false));
    assertEquals(Collections.EMPTY_SET, set2.headSet("a", false));
    assertEquals(Collections.EMPTY_SET, set3.headSet("a", false));

    assertEquals(null, set1.higher("a"));
    assertEquals(null, set2.higher("a"));
    assertEquals(null, set3.higher("a"));

    try {
      set1.last();
      fail("76");
    } catch (NoSuchElementException exception) {
    }
    try {
      set2.last();
      fail("77");
    } catch (NoSuchElementException exception) {
    }
    try {
      set3.last();
      fail("78");
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, set1.lower("a"));
    assertEquals(null, set2.lower("a"));
    assertEquals(null, set3.lower("a"));

    try {
      assertEquals(null, set1.pollFirst());
      if (!Settings.onJavaSE) {
        fail("79");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set2.pollFirst());
      if (!Settings.onJavaSE) {
        fail("80");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set3.pollFirst());
      if (!Settings.onJavaSE) {
        fail("81");
      }
    } catch (NullPointerException exception) {
    }

    try {
      assertEquals(null, set1.pollLast());
      if (!Settings.onJavaSE) {
        fail("82");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set2.pollLast());
      if (!Settings.onJavaSE) {
        fail("83");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, set3.pollLast());
      if (!Settings.onJavaSE) {
        fail("84");
      }
    } catch (NullPointerException exception) {
    }

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

    try {
      dSet1.add("a");
      fail("85");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      dSet2.add("a");
      fail("86");
    } catch (UnsupportedOperationException exception) {
    }
    try {
      dSet3.add("a");
      fail("87");
    } catch (UnsupportedOperationException exception) {
    }

    try {
      assertFalse(dSet1.addAll(dSet2));
      if (!Settings.onJavaSE) {
        fail("88");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertFalse(dSet2.addAll(dSet3));
      if (!Settings.onJavaSE) {
        fail("89");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertFalse(dSet3.addAll(dSet1));
      if (!Settings.onJavaSE) {
        fail("90");
      }
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, dSet1.ceiling("a"));
    assertEquals(null, dSet2.ceiling("a"));
    assertEquals(null, dSet3.ceiling("a"));

    dSet1.clear();
    dSet2.clear();
    dSet3.clear();

    assertTrue(dSet1.comparator() instanceof Comparator);
    assertTrue(dSet2.comparator() instanceof Comparator);
    assertTrue(dSet3.comparator() instanceof Comparator);

    assertFalse(dSet1.contains("a"));
    assertFalse(dSet2.contains("a"));
    assertFalse(dSet3.contains("a"));

    try {
      assertTrue(dSet1.containsAll(dSet2));
      if (!Settings.onJavaSE) {
        fail("91");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertTrue(dSet2.containsAll(dSet3));
      if (!Settings.onJavaSE) {
        fail("92");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertTrue(dSet3.containsAll(dSet1));
      if (!Settings.onJavaSE) {
        fail("93");
      }
    } catch (NoSuchElementException exception) {
    }

    assertTrue(dSet1.descendingIterator() instanceof Iterator);
    assertTrue(dSet2.descendingIterator() instanceof Iterator);
    assertTrue(dSet3.descendingIterator() instanceof Iterator);

    assertEquals(Collections.EMPTY_SET, dSet1.descendingSet());
    assertEquals(Collections.EMPTY_SET, dSet2.descendingSet());
    assertEquals(Collections.EMPTY_SET, dSet3.descendingSet());

    try {
      assertTrue(dSet1.equals(dSet2));
      if (!Settings.onJavaSE) {
        fail("94");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertTrue(dSet2.equals(dSet3));
      if (!Settings.onJavaSE) {
        fail("95");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertTrue(dSet3.equals(dSet1));
      if (!Settings.onJavaSE) {
        fail("96");
      }
    } catch (NoSuchElementException exception) {
    }

    try {
      dSet1.first();
      fail("97");
    } catch (NoSuchElementException exception) {
    }
    try {
      dSet2.first();
      fail("98");
    } catch (NoSuchElementException exception) {
    }
    try {
      dSet3.first();
      fail("99");
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, dSet1.floor("a"));
    assertEquals(null, dSet2.floor("a"));
    assertEquals(null, dSet3.floor("a"));

    assertEquals(0, dSet1.hashCode());
    assertEquals(0, dSet2.hashCode());
    assertEquals(0, dSet3.hashCode());

    assertEquals(Collections.EMPTY_SET, dSet1.headSet("a"));
    assertEquals(Collections.EMPTY_SET, dSet2.headSet("a"));
    assertEquals(Collections.EMPTY_SET, dSet3.headSet("a"));

    assertEquals(Collections.EMPTY_SET, dSet1.headSet("a", false));
    assertEquals(Collections.EMPTY_SET, dSet2.headSet("a", false));
    assertEquals(Collections.EMPTY_SET, dSet3.headSet("a", false));

    assertEquals(null, dSet1.higher("a"));
    assertEquals(null, dSet2.higher("a"));
    assertEquals(null, dSet3.higher("a"));

    assertTrue(dSet1.isEmpty());
    assertTrue(dSet2.isEmpty());
    assertTrue(dSet3.isEmpty());

    try {
      assertFalse((iterator1 = dSet1.iterator()).hasNext());
      if (!Settings.onJavaSE) {
        fail("100");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertFalse((iterator2 = dSet2.iterator()).hasNext());
      if (!Settings.onJavaSE) {
        fail("101");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertFalse((iterator3 = dSet3.iterator()).hasNext());
      if (!Settings.onJavaSE) {
        fail("102");
      }
    } catch (NoSuchElementException exception) {
    }

    if (Settings.onJavaSE) {
      try {
        iterator1.next();
        fail("103");
      } catch (NoSuchElementException exception) {
      }
      try {
        iterator2.next();
        fail("104");
      } catch (NoSuchElementException exception) {
      }
      try {
        iterator3.next();
        fail("105");
      } catch (NoSuchElementException exception) {
      }
    }

    try {
      iterator1.remove();
      fail("106");
    } catch (IllegalStateException exception) {
    }
    try {
      iterator2.remove();
      fail("107");
    } catch (IllegalStateException exception) {
    }
    try {
      iterator3.remove();
      fail("108");
    } catch (IllegalStateException exception) {
    }

    try {
      dSet1.last();
      fail("109");
    } catch (NoSuchElementException exception) {
    }
    try {
      dSet2.last();
      fail("110");
    } catch (NoSuchElementException exception) {
    }
    try {
      dSet3.last();
      fail("111");
    } catch (NoSuchElementException exception) {
    }

    assertEquals(null, dSet1.lower("a"));
    assertEquals(null, dSet2.lower("a"));
    assertEquals(null, dSet3.lower("a"));

    try {
      assertEquals(null, dSet1.pollFirst());
      if (!Settings.onJavaSE) {
        fail("112");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, dSet2.pollFirst());
      if (!Settings.onJavaSE) {
        fail("113");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, dSet3.pollFirst());
      if (!Settings.onJavaSE) {
        fail("114");
      }
    } catch (NullPointerException exception) {
    }

    try {
      assertEquals(null, dSet1.pollLast());
      if (!Settings.onJavaSE) {
        fail("115");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, dSet2.pollLast());
      if (!Settings.onJavaSE) {
        fail("116");
      }
    } catch (NullPointerException exception) {
    }
    try {
      assertEquals(null, dSet3.pollLast());
      if (!Settings.onJavaSE) {
        fail("117");
      }
    } catch (NullPointerException exception) {
    }

    assertFalse(dSet1.remove("a"));
    assertFalse(dSet2.remove("a"));
    assertFalse(dSet3.remove("a"));

    try {
      assertFalse(dSet1.removeAll(dSet2));
      if (!Settings.onJavaSE) {
        fail("118");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertFalse(dSet2.removeAll(dSet3));
      if (!Settings.onJavaSE) {
        fail("119");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertFalse(dSet3.removeAll(dSet1));
      if (!Settings.onJavaSE) {
        fail("120");
      }
    } catch (NoSuchElementException exception) {
    }

    assertFalse(dSet1.retainAll(dSet2));
    assertFalse(dSet2.retainAll(dSet3));
    assertFalse(dSet3.retainAll(dSet1));

    assertEquals(0, dSet1.size());
    assertEquals(0, dSet2.size());
    assertEquals(0, dSet3.size());

    assertEquals(Collections.EMPTY_SET, dSet1.subSet("c", "a"));
    assertEquals(Collections.EMPTY_SET, dSet2.subSet("c", "a"));
    assertEquals(Collections.EMPTY_SET, dSet3.subSet("c", "a"));

    assertEquals(Collections.EMPTY_SET, dSet1.subSet("c", true, "a", false));
    assertEquals(Collections.EMPTY_SET, dSet2.subSet("c", true, "a", false));
    assertEquals(Collections.EMPTY_SET, dSet3.subSet("c", true, "a", false));

    assertEquals(Collections.EMPTY_SET, dSet1.tailSet("a"));
    assertEquals(Collections.EMPTY_SET, dSet2.tailSet("a"));
    assertEquals(Collections.EMPTY_SET, dSet3.tailSet("a"));

    assertEquals(Collections.EMPTY_SET, dSet1.tailSet("a", true));
    assertEquals(Collections.EMPTY_SET, dSet2.tailSet("a", true));
    assertEquals(Collections.EMPTY_SET, dSet3.tailSet("a", true));

    assertEquals(0, dSet1.toArray().length);
    assertEquals(0, dSet2.toArray().length);
    assertEquals(0, dSet3.toArray().length);

    assertEquals(0, dSet1.toArray(new Object[0]).length);
    assertEquals(0, dSet2.toArray(new Object[0]).length);
    assertEquals(0, dSet3.toArray(new Object[0]).length);

    try {
      assertEquals("[]", dSet1.toString());
      if (!Settings.onJavaSE) {
        fail("121");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertEquals("[]", dSet2.toString());
      if (!Settings.onJavaSE) {
        fail("122");
      }
    } catch (NoSuchElementException exception) {
    }
    try {
      assertEquals("[]", dSet3.toString());
      if (!Settings.onJavaSE) {
        fail("123");
      }
    } catch (NoSuchElementException exception) {
    }

    assertEquals(0, eSet11.size());
    assertEquals(0, eSet21.size());
    assertEquals(0, eSet31.size());

    assertTrue(eSet11.iterator() instanceof Iterator);
    assertTrue(eSet21.iterator() instanceof Iterator);
    assertTrue(eSet31.iterator() instanceof Iterator);

    eSet11.clear();
    eSet21.clear();
    eSet31.clear();

    assertFalse(eSet11.contains("a"));
    assertFalse(eSet21.contains("a"));
    assertFalse(eSet31.contains("a"));

    assertFalse(eSet11.remove("a"));
    assertFalse(eSet21.remove("a"));
    assertFalse(eSet31.remove("a"));

    if (!Settings.onJavaSE) {
      assertEquals(null, ((NavigableSet) eSet11).ceiling(e));
      assertEquals(null, ((NavigableSet) eSet21).ceiling(e));
      assertEquals(null, ((NavigableSet) eSet31).ceiling(e));

      try {
        ((NavigableSet) eSet11).comparator().compare(e, e);
        fail("124");
      } catch (NullPointerException exception) {
      }
      try {
        ((NavigableSet) eSet21).comparator().compare(e, e);
        fail("125");
      } catch (NullPointerException exception) {
      }
      try {
        ((NavigableSet) eSet31).comparator().compare(e, e);
        fail("126");
      } catch (NullPointerException exception) {
      }

      assertTrue(((NavigableSet) eSet11).descendingIterator() instanceof Iterator);
      assertTrue(((NavigableSet) eSet21).descendingIterator() instanceof Iterator);
      assertTrue(((NavigableSet) eSet31).descendingIterator() instanceof Iterator);

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet11).descendingSet());
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet21).descendingSet());
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet31).descendingSet());

      assertEquals(null, ((NavigableSet) eSet11).first());
      assertEquals(null, ((NavigableSet) eSet21).first());
      assertEquals(null, ((NavigableSet) eSet31).first());

      assertEquals(null, ((NavigableSet) eSet11).floor(e));
      assertEquals(null, ((NavigableSet) eSet21).floor(e));
      assertEquals(null, ((NavigableSet) eSet31).floor(e));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet11).headSet(e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet21).headSet(e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet31).headSet(e));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet11).headSet(e, false));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet21).headSet(e, false));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet31).headSet(e, false));

      assertEquals(null, ((NavigableSet) eSet11).higher(e));
      assertEquals(null, ((NavigableSet) eSet21).higher(e));
      assertEquals(null, ((NavigableSet) eSet31).higher(e));

      assertEquals(null, ((NavigableSet) eSet11).last());
      assertEquals(null, ((NavigableSet) eSet21).last());
      assertEquals(null, ((NavigableSet) eSet31).last());

      assertEquals(null, ((NavigableSet) eSet11).lower(e));
      assertEquals(null, ((NavigableSet) eSet21).lower(e));
      assertEquals(null, ((NavigableSet) eSet31).lower(e));

      assertEquals(null, ((NavigableSet) eSet11).pollFirst());
      assertEquals(null, ((NavigableSet) eSet21).pollFirst());
      assertEquals(null, ((NavigableSet) eSet31).pollFirst());

      assertEquals(null, ((NavigableSet) eSet11).pollLast());
      assertEquals(null, ((NavigableSet) eSet21).pollLast());
      assertEquals(null, ((NavigableSet) eSet31).pollLast());

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet11).subSet(e, e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet21).subSet(e, e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet31).subSet(e, e));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet11).subSet(e, true, e, false));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet21).subSet(e, true, e, false));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet31).subSet(e, true, e, false));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet11).tailSet(e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet21).tailSet(e));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet31).tailSet(e));

      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet11).tailSet(e, true));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet21).tailSet(e, true));
      assertEquals(Collections.EMPTY_SET, ((NavigableSet) eSet31).tailSet(e, true));
    }
  }
}

class Test32 extends TreeMap implements Cloneable {
  public Test32() {
    super();
  }

  public Test32(Comparator c) {
    super(c);
  }

  public Test32(Map m) {
    super(m);
  }

  public Test32(SortedMap sm) {
    super(sm);
  }
}