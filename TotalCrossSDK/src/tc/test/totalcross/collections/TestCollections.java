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
      
      assertEquals(1, (list = Collections.singletonList("b")).size());
      assertEquals("b", list.get(0));
      assertTrue(list.contains("b"));
      assertTrue(list.containsAll(list));
      assertEquals(129, list.hashCode());
      assertEquals(0, list.indexOf("b"));
      assertEquals(-1, list.lastIndexOf("a"));
      assertTrue(Arrays.equals(new Object[]{"b"}, list.subList(0, 1).toArray()));
      assertEquals("b", list.toArray()[0]);
      assertEquals("[b]", list.toString());
      
      assertTrue((map = Collections.singletonMap("a", "b")).containsKey("a"));
      assertTrue(map.containsValue("b"));
      assertEquals("b", map.get("a"));
      assertEquals(3, map.hashCode());
      assertTrue(Arrays.equals(new Object[]{"a"}, map.keySet().toArray()));
      assertEquals(1, map.size());
      assertTrue(Arrays.equals(new Object[]{"b"}, map.values().toArray()));
      assertEquals("{a=b}", map.toString());
      
      try
      {
         Collections.sort(list);
         fail("8");
      }
      catch (UnsupportedOperationException exception) {}
      try
      {
         Collections.sort(list, null);
         fail("9");
      }
      catch (UnsupportedOperationException exception) {}
      
      try
      {
         Collections.swap(list, 0, 0);
         fail("10");
      }
      catch (UnsupportedOperationException exception) {}
      
      Collection collection = Collections.synchronizedCollection(Arrays.asList("a", "b"));
      try
      {
         collection.add("c");
         fail("11");
      }
      catch (UnsupportedOperationException exception) {}
      try
      {
         collection.add(list);
         fail("12");
      }
      catch (UnsupportedOperationException exception) {}
      try
      {
         collection.clear();
         fail("13");
      }
      catch (UnsupportedOperationException exception) {}
      assertTrue(collection.contains("a"));
      assertTrue(collection.containsAll(collection));
      assertFalse(collection.isEmpty());
      iterator = collection.iterator();
      try
      {
         collection.remove("a");
         fail("14");
      }
      catch (UnsupportedOperationException exception) {}
      try
      {
         collection.removeAll(collection);
         fail("15");
      }
      catch (UnsupportedOperationException exception) {}
      assertFalse(collection.retainAll(collection));
      assertEquals(2, collection.size());
      assertTrue(Arrays.equals(new Object[]{"a", "b"}, collection.toArray()));
      assertTrue(Arrays.equals(new Object[]{"a", "b"}, collection.toArray(new Object[2])));
      assertEquals("[a, b]", collection.toString());
      assertEquals("a", iterator.next());
      assertTrue(iterator.hasNext());
      try
      {
         iterator.remove();
         fail("16");
      }
      catch (UnsupportedOperationException exception) {}
      
      list = Collections.synchronizedList(list);
      try
      {
         list.add("c");
         fail("17");
      }
      catch (UnsupportedOperationException exception) {}
      try
      {
         list.add(list);
         fail("18");
      }
      catch (UnsupportedOperationException exception) {}
      assertTrue(list.equals(list));
      assertEquals("b", list.get(0).toString());
      assertEquals(129, list.hashCode());
      assertEquals(0, list.indexOf("b"));
      assertEquals(0, list.lastIndexOf("b"));
      ListIterator listIt1 = list.listIterator();
      ListIterator listIt2 = list.listIterator(0);
      try
      {
         list.remove(0);
         fail("19");
      }
      catch (UnsupportedOperationException exception) {}
      try
      {
         list.set(0, "c");
         fail("20");
      }
      catch (UnsupportedOperationException exception) {}
      assertTrue(Arrays.equals(new Object[]{"b"}, list.toArray()));
      try
      {
         listIt1.add("c");
         fail("21");
      }
      catch (UnsupportedOperationException exception) {}
      try
      {
         listIt2.add("c");
         fail("22");
      }
      catch (UnsupportedOperationException exception) {}
      if (Settings.onJavaSE)
      {
         assertFalse(listIt1.hasPrevious());
         assertFalse(listIt2.hasPrevious());
         assertEquals(0, listIt1.nextIndex());
         assertEquals(0, listIt2.nextIndex());
         try
         {
            listIt1.previous();
            fail("23");
         }
         catch (NoSuchElementException exception) {}
         try
         {
            listIt2.previous();
            fail("24");
         }
         catch (NoSuchElementException exception) {}
      }
      else
      {
         assertTrue(listIt1.hasPrevious());
         assertTrue(listIt2.hasPrevious());
         assertEquals(1, listIt1.nextIndex());
         assertEquals(1, listIt2.nextIndex());
         assertEquals("b", listIt1.previous());
         assertEquals("b", listIt2.previous());
      }
      
      assertEquals(-1, listIt1.previousIndex());
      assertEquals(-1, listIt2.previousIndex());
      try
      {
         listIt1.set("c");
         fail("25");
      }
      catch (RuntimeException exception) {}
      try
      {
         listIt2.set("c");
         fail("26");
      }
      catch (RuntimeException exception) {}
   }
}
      
