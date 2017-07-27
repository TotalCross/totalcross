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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;

import totalcross.unit.TestCase;

public class TestNavigableMap extends TestCase
{
  @Override
  public void testRun()
  {
    Test29 test1 = new Test29();
    NavigableMap test2 = new Test29();
    SortedMap test3 = new Test29();

    assertEquals(null, test1.comparator());
    assertEquals(null, test2.comparator());
    assertEquals(null, test3.comparator());

    assertEquals(null, test1.firstKey());
    assertEquals(null, test2.firstKey());
    assertEquals(null, test3.firstKey());

    assertEquals(null, test1.keySet());
    assertEquals(null, test2.keySet());
    assertEquals(null, test3.keySet());

    assertEquals(null, test1.values());
    assertEquals(null, test2.values());
    assertEquals(null, test3.values());

    assertEquals(null, test1.entrySet());
    assertEquals(null, test2.entrySet());
    assertEquals(null, test3.entrySet());

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    assertFalse(test1.isEmpty());
    assertFalse(test2.isEmpty());
    assertFalse(test3.isEmpty());

    assertFalse(test1.containsKey(null));
    assertFalse(test2.containsKey(null));
    assertFalse(test3.containsKey(null));

    assertFalse(test1.containsValue(null));
    assertFalse(test2.containsValue(null));
    assertFalse(test3.containsValue(null));

    assertEquals(null, test1.get(null));
    assertEquals(null, test2.get(null));
    assertEquals(null, test3.get(null));

    assertEquals(null, test1.put(null, null));
    assertEquals(null, test2.put(null, null));
    assertEquals(null, test3.put(null, null));

    assertEquals(null, test1.remove(null));
    assertEquals(null, test2.remove(null));
    assertEquals(null, test3.remove(null));

    test1.putAll(null);
    test2.putAll(null);
    test3.putAll(null);

    test1.clear();
    test2.clear();
    test3.clear();

    assertEquals(null, test1.lowerEntry(null));
    assertEquals(null, test2.lowerEntry(null));
    assertEquals(null, ((NavigableMap)test3).lowerEntry(null));

    assertEquals(null, test1.lowerKey(null));
    assertEquals(null, test2.lowerKey(null));
    assertEquals(null, ((NavigableMap)test3).lowerKey(null));

    assertEquals(null, test1.floorEntry(null));
    assertEquals(null, test2.floorEntry(null));
    assertEquals(null, ((NavigableMap)test3).floorEntry(null));

    assertEquals(null, test1.floorKey(null));
    assertEquals(null, test2.floorKey(null));
    assertEquals(null, ((NavigableMap)test3).floorKey(null));

    assertEquals(null, test1.ceilingEntry(null));
    assertEquals(null, test2.ceilingEntry(null));
    assertEquals(null, ((NavigableMap)test3).ceilingEntry(null));

    assertEquals(null, test1.ceilingKey(null));
    assertEquals(null, test2.ceilingKey(null));
    assertEquals(null, ((NavigableMap)test3).ceilingKey(null));

    assertEquals(null, test1.higherEntry(null));
    assertEquals(null, test2.higherEntry(null));
    assertEquals(null, ((NavigableMap)test3).higherEntry(null));

    assertEquals(null, test1.higherKey(null));
    assertEquals(null, test2.higherKey(null));
    assertEquals(null, ((NavigableMap)test3).higherKey(null));

    assertEquals(null, test1.firstEntry());
    assertEquals(null, test2.firstEntry());
    assertEquals(null, ((NavigableMap)test3).firstEntry());

    assertEquals(null, test1.lastEntry());
    assertEquals(null, test2.lastEntry());
    assertEquals(null, ((NavigableMap)test3).lastEntry());

    assertEquals(null, test1.pollFirstEntry());
    assertEquals(null, test2.pollFirstEntry());
    assertEquals(null, ((NavigableMap)test3).pollFirstEntry());

    assertEquals(null, test1.pollLastEntry());
    assertEquals(null, test2.pollLastEntry());
    assertEquals(null, ((NavigableMap)test3).pollLastEntry());

    assertEquals(null, test1.descendingMap());
    assertEquals(null, test2.descendingMap());
    assertEquals(null, ((NavigableMap)test3).descendingMap());

    assertEquals(null, test1.descendingKeySet());
    assertEquals(null, test2.descendingKeySet());
    assertEquals(null, ((NavigableMap)test3).descendingKeySet());

    assertEquals(null, test1.navigableKeySet());
    assertEquals(null, test2.navigableKeySet());
    assertEquals(null, ((NavigableMap)test3).navigableKeySet());

    assertEquals(null, test1.subMap(null, null));
    assertEquals(null, test2.subMap(null, null));
    assertEquals(null, ((NavigableMap)test3).subMap(null, null));

    assertEquals(null, test1.subMap(null, false, null, false));
    assertEquals(null, test2.subMap(null, false, null, false));
    assertEquals(null, ((NavigableMap)test3).subMap(null, false, null, false));

    assertEquals(null, test1.headMap(null));
    assertEquals(null, test2.headMap(null));
    assertEquals(null, ((NavigableMap)test3).headMap(null));

    assertEquals(null, test1.headMap(null, false));
    assertEquals(null, test2.headMap(null, false));
    assertEquals(null, ((NavigableMap)test3).headMap(null, false));

    assertEquals(null, test1.tailMap(null));
    assertEquals(null, test2.tailMap(null));
    assertEquals(null, ((NavigableMap)test3).tailMap(null));

    assertEquals(null, test1.tailMap(null, false));
    assertEquals(null, test2.tailMap(null, false));
    assertEquals(null, ((NavigableMap)test3).tailMap(null, false));
  }
}

class Test29 implements NavigableMap
{
  @Override
  public Comparator comparator()
  {
    return null;
  }

  @Override
  public Object firstKey()
  {
    return null;
  }

  @Override
  public Object lastKey()
  {
    return null;
  }

  @Override
  public Set keySet()
  {
    return null;
  }

  @Override
  public Collection values()
  {
    return null;
  }

  @Override
  public Set entrySet()
  {
    return null;
  }

  @Override
  public int size()
  {
    return 0;
  }

  @Override
  public boolean isEmpty()
  {
    return false;
  }

  @Override
  public boolean containsKey(Object key)
  {
    return false;
  }

  @Override
  public boolean containsValue(Object value)
  {
    return false;
  }

  @Override
  public Object get(Object key)
  {
    return null;
  }

  @Override
  public Object put(Object key, Object value)
  {
    return null;
  }

  @Override
  public Object remove(Object key)
  {
    return null;
  }

  @Override
  public void putAll(Map m)
  {
  }

  @Override
  public void clear()
  {
  }

  @Override
  public Entry lowerEntry(Object key)
  {
    return null;
  }

  @Override
  public Object lowerKey(Object key)
  {
    return null;
  }

  @Override
  public Entry floorEntry(Object key)
  {
    return null;
  }

  @Override
  public Object floorKey(Object key)
  {
    return null;
  }

  @Override
  public Entry ceilingEntry(Object key)
  {
    return null;
  }

  @Override
  public Object ceilingKey(Object key)
  {
    return null;
  }

  @Override
  public Entry higherEntry(Object key)
  {
    return null;
  }

  @Override
  public Object higherKey(Object key)
  {
    return null;
  }

  @Override
  public Entry firstEntry()
  {
    return null;
  }

  @Override
  public Entry lastEntry()
  {
    return null;
  }

  @Override
  public Entry pollFirstEntry()
  {
    return null;
  }

  @Override
  public Entry pollLastEntry()
  {
    return null;
  }

  @Override
  public NavigableMap descendingMap()
  {
    return null;
  }

  @Override
  public NavigableSet navigableKeySet()
  {
    return null;
  }

  @Override
  public NavigableSet descendingKeySet()
  {
    return null;
  }

  @Override
  public NavigableMap subMap(Object fromKey, boolean fromInclusive, Object toKey, boolean toInclusive)
  {
    return null;
  }

  @Override
  public NavigableMap headMap(Object toKey, boolean inclusive)
  {
    return null;
  }

  @Override
  public NavigableMap tailMap(Object fromKey, boolean inclusive)
  {
    return null;
  }

  @Override
  public SortedMap subMap(Object fromKey, Object toKey)
  {
    return null;
  }

  @Override
  public SortedMap headMap(Object toKey)
  {
    return null;
  }

  @Override
  public SortedMap tailMap(Object fromKey)
  {
    return null;
  }

}
