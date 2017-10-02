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
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

import totalcross.unit.TestCase;

public class TestNavigableSet extends TestCase {
  @Override
  public void testRun() {
    Test28 test1 = new Test28();
    NavigableSet test2 = new Test28();
    SortedSet test3 = new Test28();

    assertEquals(null, test1.comparator());
    assertEquals(null, test2.comparator());
    assertEquals(null, test3.comparator());

    assertEquals(null, test1.first());
    assertEquals(null, test2.first());
    assertEquals(null, test3.first());

    assertEquals(null, test1.last());
    assertEquals(null, test2.last());
    assertEquals(null, test3.last());

    assertEquals(0, test1.size());
    assertEquals(0, test2.size());
    assertEquals(0, test3.size());

    assertFalse(test1.isEmpty());
    assertFalse(test2.isEmpty());
    assertFalse(test3.isEmpty());

    assertFalse(test1.contains(null));
    assertFalse(test2.contains(null));
    assertFalse(test3.contains(null));

    assertEquals(null, test1.toArray());
    assertEquals(null, test2.toArray());
    assertEquals(null, test3.toArray());

    assertEquals(null, test1.toArray(null));
    assertEquals(null, test2.toArray(null));
    assertEquals(null, test3.toArray(null));

    assertFalse(test1.add(null));
    assertFalse(test2.add(null));
    assertFalse(test3.add(null));

    assertFalse(test1.remove(null));
    assertFalse(test2.remove(null));
    assertFalse(test3.remove(null));

    assertFalse(test1.containsAll(null));
    assertFalse(test2.containsAll(null));
    assertFalse(test3.containsAll(null));

    assertFalse(test1.addAll(null));
    assertFalse(test2.addAll(null));
    assertFalse(test3.addAll(null));

    assertFalse(test1.retainAll(null));
    assertFalse(test2.retainAll(null));
    assertFalse(test3.retainAll(null));

    assertFalse(test1.removeAll(null));
    assertFalse(test2.removeAll(null));
    assertFalse(test3.removeAll(null));

    test1.clear();
    test2.clear();
    test3.clear();

    assertEquals(null, test1.lower(null));
    assertEquals(null, test2.lower(null));
    assertEquals(null, ((NavigableSet) test3).lower(null));

    assertEquals(null, test1.floor(null));
    assertEquals(null, test2.floor(null));
    assertEquals(null, ((NavigableSet) test3).floor(null));

    assertEquals(null, test1.ceiling(null));
    assertEquals(null, test2.ceiling(null));
    assertEquals(null, ((NavigableSet) test3).ceiling(null));

    assertEquals(null, test1.higher(null));
    assertEquals(null, test2.higher(null));
    assertEquals(null, ((NavigableSet) test3).higher(null));

    assertEquals(null, test1.pollFirst());
    assertEquals(null, test2.pollFirst());
    assertEquals(null, ((NavigableSet) test3).pollFirst());

    assertEquals(null, test1.pollLast());
    assertEquals(null, test2.pollLast());
    assertEquals(null, ((NavigableSet) test3).pollLast());

    assertEquals(null, test1.iterator());
    assertEquals(null, test2.iterator());
    assertEquals(null, test3.iterator());

    assertEquals(null, test1.descendingSet());
    assertEquals(null, test2.descendingSet());
    assertEquals(null, ((NavigableSet) test3).descendingSet());

    assertEquals(null, test1.descendingIterator());
    assertEquals(null, test2.descendingIterator());
    assertEquals(null, ((NavigableSet) test3).descendingIterator());

    assertEquals(null, test1.subSet(null, null));
    assertEquals(null, test2.subSet(null, null));
    assertEquals(null, ((NavigableSet) test3).subSet(null, null));

    assertEquals(null, test1.subSet(null, false, null, false));
    assertEquals(null, test2.subSet(null, false, null, false));
    assertEquals(null, ((NavigableSet) test3).subSet(null, false, null, false));

    assertEquals(null, test1.headSet(null));
    assertEquals(null, test2.headSet(null));
    assertEquals(null, ((NavigableSet) test3).headSet(null));

    assertEquals(null, test1.headSet(null, false));
    assertEquals(null, test2.headSet(null, false));
    assertEquals(null, ((NavigableSet) test3).headSet(null, false));

    assertEquals(null, test1.tailSet(null));
    assertEquals(null, test2.tailSet(null));
    assertEquals(null, ((NavigableSet) test3).tailSet(null));

    assertEquals(null, test1.tailSet(null, false));
    assertEquals(null, test2.tailSet(null, false));
    assertEquals(null, ((NavigableSet) test3).tailSet(null, false));
  }
}

class Test28 implements NavigableSet {
  @Override
  public Comparator comparator() {
    return null;
  }

  @Override
  public Object first() {
    return null;
  }

  @Override
  public Object last() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean contains(Object o) {
    return false;
  }

  @Override
  public Object[] toArray() {
    return null;
  }

  @Override
  public Object[] toArray(Object[] a) {
    return null;
  }

  @Override
  public boolean add(Object e) {
    return false;
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean containsAll(Collection c) {
    return false;
  }

  @Override
  public boolean addAll(Collection c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection c) {
    return false;
  }

  @Override
  public void clear() {
  }

  @Override
  public Object lower(Object e) {
    return null;
  }

  @Override
  public Object floor(Object e) {
    return null;
  }

  @Override
  public Object ceiling(Object e) {
    return null;
  }

  @Override
  public Object higher(Object e) {
    return null;
  }

  @Override
  public Object pollFirst() {
    return null;
  }

  @Override
  public Object pollLast() {
    return null;
  }

  @Override
  public Iterator iterator() {
    return null;
  }

  @Override
  public NavigableSet descendingSet() {
    return null;
  }

  @Override
  public Iterator descendingIterator() {
    return null;
  }

  @Override
  public NavigableSet subSet(Object fromElement, boolean fromInclusive, Object toElement, boolean toInclusive) {
    return null;
  }

  @Override
  public NavigableSet headSet(Object toElement, boolean inclusive) {
    return null;
  }

  @Override
  public NavigableSet tailSet(Object fromElement, boolean inclusive) {
    return null;
  }

  @Override
  public SortedSet subSet(Object fromElement, Object toElement) {
    return null;
  }

  @Override
  public SortedSet headSet(Object toElement) {
    return null;
  }

  @Override
  public SortedSet tailSet(Object fromElement) {
    return null;
  }
}
