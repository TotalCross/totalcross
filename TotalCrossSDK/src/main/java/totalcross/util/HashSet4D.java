/* HashSet.java -- a class providing a HashMap-backed Set
   Copyright (C) 1998, 1999, 2001, 2004, 2005  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package totalcross.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class provides a HashMap-backed implementation of the Set interface.
 * <p>
 *
 * Most operations are O(1), assuming no hash collisions.  In the worst
 * case (where all hashes collide), operations are O(n). Setting the
 * initial capacity too low will force many resizing operations, but
 * setting the initial capacity too high (or loadfactor too low) leads
 * to wasted memory and slower iteration.
 * <p>
 *
 * HashSet accepts the null key and null values.  It is not synchronized,
 * so if you need multi-threaded access, consider using:<br>
 * <code>Set s = Collections.synchronizedSet(new HashSet(...));</code>
 * <p>
 *
 * The iterators are <i>fail-fast</i>, meaning that any structural
 * modification, except for <code>remove()</code> called on the iterator
 * itself, cause the iterator to throw a
 * {@link ConcurrentModificationException} rather than exhibit
 * non-deterministic behavior.
 *
 * @author Jon Zeppieri
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see Collection
 * @see Set
 * @see TreeSet
 * @see Collections#synchronizedSet(Set)
 * @see HashMap
 * @see LinkedHashSet
 * @since 1.2
 * @status updated to 1.4
 */
public class HashSet4D<T> extends AbstractSet<T> implements Set<T>, Cloneable {
  /**
   * The HashMap which backs this Set.
   */
  private transient HashMap4D<T, String> map;

  /**
   * Construct a new, empty HashSet whose backing HashMap has the default
   * capacity (11) and loadFacor (0.75).
   */
  public HashSet4D() {
    this(HashMap4D.DEFAULT_CAPACITY, HashMap4D.DEFAULT_LOAD_FACTOR);
  }

  /**
   * Construct a new, empty HashSet whose backing HashMap has the supplied
   * capacity and the default load factor (0.75).
   *
   * @param initialCapacity the initial capacity of the backing HashMap
   * @throws IllegalArgumentException if the capacity is negative
   */
  public HashSet4D(int initialCapacity) {
    this(initialCapacity, HashMap4D.DEFAULT_LOAD_FACTOR);
  }

  /**
   * Construct a new, empty HashSet whose backing HashMap has the supplied
   * capacity and load factor.
   *
   * @param initialCapacity the initial capacity of the backing HashMap
   * @param loadFactor the load factor of the backing HashMap
   * @throws IllegalArgumentException if either argument is negative, or
   *         if loadFactor is POSITIVE_INFINITY or NaN
   */
  public HashSet4D(int initialCapacity, double loadFactor) {
    map = init(initialCapacity, loadFactor);
  }

  /**
   * Construct a new HashSet with the same elements as are in the supplied
   * collection (eliminating any duplicates, of course). The backing storage
   * has twice the size of the collection, or the default size of 11,
   * whichever is greater; and the default load factor (0.75).
   *
   * @param c a collection of initial set elements
   * @throws NullPointerException if c is null
   */
  public HashSet4D(Collection<? extends T> c) {
    this(Math.max(2 * c.size(), HashMap4D.DEFAULT_CAPACITY));
    addAll(c);
  }

  /**
   * Adds the given Object to the set if it is not already in the Set.
   * This set permits a null element.
   *
   * @param o the Object to add to this Set
   * @return true if the set did not already contain o
   */
  @Override
  public boolean add(T o) {
    return map.put(o, "") == null;
  }

  /**
   * Empties this Set of all elements; this takes constant time.
   */
  @Override
  public void clear() {
    map.clear();
  }

  /**
   * Returns a shallow copy of this Set. The Set itself is cloned; its
   * elements are not.
   *
   * @return a shallow clone of the set
   */
  @Override
  public Object clone() {
    HashSet4D<T> copy = null;
    try {
      copy = (HashSet4D<T>) super.clone();
    } catch (CloneNotSupportedException x) {
      // Impossible to get here.
    }
    copy.map = (HashMap4D<T, String>) map.clone();
    return copy;
  }

  /**
   * Returns true if the supplied element is in this Set.
   *
   * @param o the Object to look for
   * @return true if it is in the set
   */
  @Override
  public boolean contains(Object o) {
    return map.containsKey(o);
  }

  /**
   * Returns true if this set has no elements in it.
   *
   * @return <code>size() == 0</code>.
   */
  @Override
  public boolean isEmpty() {
    return map.size == 0;
  }

  /**
   * Returns an Iterator over the elements of this Set, which visits the
   * elements in no particular order.  For this class, the Iterator allows
   * removal of elements. The iterator is fail-fast, and will throw a
   * ConcurrentModificationException if the set is modified externally.
   *
   * @return a set iterator
   * @see ConcurrentModificationException
   */
  @Override
  public Iterator<T> iterator() {
    // Avoid creating intermediate keySet() object by using non-public API.
    return map.iterator(AbstractMap4D.KEYS);
  }

  /**
   * Removes the supplied Object from this Set if it is in the Set.
   *
   * @param o the object to remove
   * @return true if an element was removed
   */
  @Override
  public boolean remove(Object o) {
    return (map.remove(o) != null);
  }

  /**
   * Returns the number of elements in this Set (its cardinality).
   *
   * @return the size of the set
   */
  @Override
  public int size() {
    return map.size;
  }

  /**
   * Helper method which initializes the backing Map. Overridden by
   * LinkedHashSet for correct semantics.
   *
   * @param capacity the initial capacity
   * @param load the initial load factor
   * @return the backing HashMap
   */
  HashMap4D init(int capacity, double load) {
    return new HashMap4D(capacity, load);
  }
}
