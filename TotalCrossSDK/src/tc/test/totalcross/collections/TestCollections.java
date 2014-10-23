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
import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class TestCollections extends TestCase
{
   public void testRun()
   {
      Set set = Collections.EMPTY_SET;
      assertEquals(0, set.size());
      assertTrue(set.iterator() instanceof Iterator);
      assertFalse(set.contains(null));
      assertTrue(set.containsAll(set));
      assertTrue(set.equals(set));
      assertEquals(0, set.hashCode());
      assertFalse(set.remove(null));
      assertFalse(set.removeAll(set));
      assertFalse(set.retainAll(set));
      assertEquals(0, set.toArray().length);
      assertEquals(0, set.toArray(new Object[0]).length);
      assertEquals("[]", set.toString());
      
      assertEquals(0, (set = Collections.emptySet()).size());
      assertTrue(set.iterator() instanceof Iterator);
      assertFalse(set.contains(null));
      assertTrue(set.containsAll(set));
      assertTrue(set.equals(set));
      assertEquals(0, set.hashCode());
      assertFalse(set.remove(null));
      assertFalse(set.removeAll(set));
      assertFalse(set.retainAll(set));
      assertEquals(0, set.toArray().length);
      assertEquals(0, set.toArray(new Object[0]).length);
      assertEquals("[]", set.toString());
      
      List list = Collections.EMPTY_LIST;
      assertEquals(0, list.size());
      try
      {
         list.get(0);
         fail("1");
      }
      catch (IndexOutOfBoundsException exception) {}
      assertFalse(list.contains(null));
      assertTrue(list.containsAll(list));
      assertTrue(list.equals(list));
      assertEquals(1, list.hashCode());
      assertEquals(-1, list.indexOf(null));
      assertEquals(-1, list.lastIndexOf(null));
      assertFalse(list.remove(null));
      assertFalse(list.removeAll(list));
      assertFalse(list.retainAll(list));
      assertEquals(0, list.toArray().length);
      assertEquals(0, list.toArray(new Object[0]).length);
      assertEquals("[]", list.toString());
      
      assertEquals(0, (list = Collections.emptyList()).size());
      try
      {
         list.get(0);
         fail("1");
      }
      catch (IndexOutOfBoundsException exception) {}
      assertFalse(list.contains(null));
      assertTrue(list.containsAll(list));
      assertTrue(list.equals(list));
      assertEquals(1, list.hashCode());
      assertEquals(-1, list.indexOf(null));
      assertEquals(-1, list.lastIndexOf(null));
      assertFalse(list.remove(null));
      assertFalse(list.removeAll(list));
      assertFalse(list.retainAll(list));
      assertEquals(0, list.toArray().length);
      assertEquals(0, list.toArray(new Object[0]).length);
      assertEquals("[]", list.toString());
      
      Map map = Collections.EMPTY_MAP;
      assertEquals(Collections.EMPTY_SET, map.entrySet());
      assertFalse(map.containsKey(null));
      assertFalse(map.containsValue(null));
      assertTrue(map.equals(map));
      assertEquals(null, map.get(null));
      assertEquals(0, map.hashCode());
      assertEquals(Collections.EMPTY_SET, map.keySet());
      assertEquals(null, map.remove(null));
      assertEquals(0, map.size());
      assertEquals(Collections.EMPTY_SET, map.values());
      assertEquals("{}", map.toString());
      
      assertEquals(Collections.EMPTY_SET, (map = Collections.emptyMap()).entrySet());
      assertFalse(map.containsKey(null));
      assertFalse(map.containsValue(null));
      assertTrue(map.equals(map));
      assertEquals(null, map.get(null));
      assertEquals(0, map.hashCode());
      assertEquals(Collections.EMPTY_SET, map.keySet());
      assertEquals(null, map.remove(null));
      assertEquals(0, map.size());
      assertEquals(Collections.EMPTY_SET, map.values());
      assertEquals("{}", map.toString());

      assertEquals(0, Collections.binarySearch(list = Arrays.asList(new Object[]{"a", "b"}), "a"));
      assertGreater(0, Collections.binarySearch(list, "c", null));
     
      Collections.copy(list, Arrays.asList(new Object[]{"b", "c"}));
      Arrays.equals(new String[]{"b", "c"}, list.toArray());
      
      Enumeration enumer = Collections.enumeration(list);
      assertTrue(enumer.hasMoreElements());
      assertEquals("b", enumer.nextElement());
      
      Collections.fill(list, "d");
      Arrays.equals(new String[]{"d", "d"}, list.toArray());
      
      assertEquals(0, Collections.indexOfSubList(list, list));
      assertEquals(0, Collections.lastIndexOfSubList(list, list));
      
      Arrays.equals(new String[]{"d", "d"}, Collections.list(enumer).toArray());
      
      assertEquals("d", Collections.max(list));
      assertEquals("d", Collections.max(list, null));
      
      assertEquals("d", Collections.min(list));
      assertEquals("d", Collections.min(list, null));
      
      assertTrue(Arrays.equals(new Object[]{"e", "e", "e"}, (list = Collections.nCopies(3, "e")).toArray()));
      assertEquals(3, list.size());
      assertEquals("e", list.get(0));
      assertTrue(list.contains("e"));
      assertEquals(0, list.indexOf("e"));
      assertEquals(2, list.lastIndexOf("e"));
      assertTrue(Arrays.equals(new Object[]{"e", "e"}, (list.subList(0, 2)).toArray()));
      assertTrue(Arrays.equals(new Object[]{"e", "e", "e"}, list.toArray()));
      if (Settings.onJavaSE)
         assertEquals("[e, e, e]", list.toString());
      else
         assertEquals("{e, e, e}", list.toString());
      
      try
      {
         Collections.replaceAll(list, "e", "f");
         fail("2");
      }
      catch (UnsupportedOperationException exception) {}
      
      try
      {
         Collections.reverse(list);
         fail("3");
      }
      catch (UnsupportedOperationException exception) {}
      
      Comparator comparator = Collections.reverseOrder(null);
      assertGreater(0, comparator.compare("b", "a"));
      assertGreater(0, (comparator = Collections.reverseOrder()).compare("b", "a"));
    
      Collections.rotate(list, 3);
      assertTrue(Arrays.equals(new Object[]{"e", "e", "e"}, list.toArray()));
      
      try
      {
         Collections.shuffle(list);
         fail("4");
      }
      catch (UnsupportedOperationException exception) {}
      try
      {
         Collections.shuffle(list, new Random());
         fail("5");
      }
      catch (UnsupportedOperationException exception) {}
      
      assertEquals(3, Collections.frequency(list, "e"));
      
      try
      {
         assertTrue(Collections.addAll(list, "d", "d"));
         fail("6");
      }
      catch (UnsupportedOperationException exception) {}
      
      assertFalse(Collections.disjoint(list, list));
      
      assertEquals(1, (set = Collections.singleton("a")).size());
      Iterator iterator = set.iterator();
      assertTrue(iterator.hasNext());
      assertEquals("a", iterator.next());
      try
      {
         iterator.remove();
         fail("7");
      }
      catch (UnsupportedOperationException exception) {}
      assertTrue(set.contains("a"));
      assertTrue(set.containsAll(set));
      assertEquals(97, set.hashCode());
      assertEquals("a", set.toArray()[0]);
      assertEquals("[a]", set.toString());
   }
}
      
