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
import java.util.Set;
import java.util.SortedMap;

import totalcross.unit.TestCase;

public class TestSortedMap extends TestCase
{
  @Override
  public void testRun()
  {
    Test9 test1 = new Test9();
    SortedMap test2 = new Test9();
    Map test3 = new Test9();

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

    try
    {
      test1.putAll(test1);
    }
    catch (Throwable throable)
    {
      fail("1");
    }
    try
    {
      test2.putAll(test2);
    }
    catch (Throwable throable)
    {
      fail("2");
    }
    try
    {
      test3.putAll(test3);
    }
    catch (Throwable throable)
    {
      fail("3");
    }

    try
    {
      test1.clear();
    }
    catch (Throwable throable)
    {
      fail("4");
    }
    try
    {
      test2.clear();
    }
    catch (Throwable throable)
    {
      fail("5");
    }
    try
    {
      test3.clear();
    }
    catch (Throwable throable)
    {
      fail("6");
    }

    assertEquals(null, test1.comparator());
    assertEquals(null, test2.comparator());
    assertEquals(null, ((SortedMap)test3).comparator());

    assertEquals(null, test1.subMap(null, null));
    assertEquals(null, test2.subMap(null, null));
    assertEquals(null, ((SortedMap)test3).subMap(null, null));

    assertEquals(null, test1.headMap(null));
    assertEquals(null, test2.headMap(null));
    assertEquals(null, ((SortedMap)test3).headMap(null));

    assertEquals(null, test1.tailMap(null));
    assertEquals(null, test2.tailMap(null));
    assertEquals(null, ((SortedMap)test3).tailMap(null));

    assertEquals(null, test1.firstKey());
    assertEquals(null, test2.firstKey());
    assertEquals(null, ((SortedMap)test3).firstKey());

    assertEquals(null, test1.lastKey());
    assertEquals(null, test2.lastKey());
    assertEquals(null, ((SortedMap)test3).lastKey());

    assertEquals(null, test1.keySet());
    assertEquals(null, test2.keySet());
    assertEquals(null, test3.keySet());

    assertEquals(null, test1.values());
    assertEquals(null, test2.values());
    assertEquals(null, test3.values());

    assertEquals(null, test1.entrySet());
    assertEquals(null, test2.entrySet());
    assertEquals(null, test3.entrySet());
  }
}

class Test9 implements SortedMap
{
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
  public Comparator comparator()
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
}
