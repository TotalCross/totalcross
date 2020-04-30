// Copyright (C) 2000, 2001 Arthur van Hoff
// Copyright (C) 2001-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.util;

import totalcross.sys.Convert;

/*
 * This class is almost identical to java.util.Hashtable, with some
modifications.
 */

/**
 * This class implements a hashtable, which maps keys to values. Both
 * key and value must be integer values.
 * If an error occurs, an Exception is returned. 
 * <p>
 * An instance of <code>Hashtable</code> has two parameters that
 * affect its efficiency: its <i>capacity</i> and its <i>load
 * factor</i>. The load factor should be between 0.0 and 1.0. When
 * the number of entries in the hashtable exceeds the product of the
 * load factor and the current capacity, the capacity is increased by
 * calling the <code>rehash</code> method. Larger load factors use
 * memory more efficiently, at the expense of larger expected time
 * per lookup.
 * <p>
 * If many entries are to be made into a <code>Hashtable</code>,
 * creating it with a sufficiently large capacity may allow the
 * entries to be inserted more efficiently than letting it perform
 * automatic rehashing as needed to grow the table.
 * <p>
 * This example creates a hashtable of numbers. It uses the names of
 * the numbers as keys:
 * <pre>
 *     IntHashtable numbers = new IntHashtable(10);
 *     numbers.put(1, 1000);
 *     numbers.put(2, 2000);
 *     numbers.put(3, 3000);
 * </pre>
 * <p>
 * To retrieve a number, use the following code:
 * <pre>
 *     int i = numbers.get(2);
      // "two = " + i;
 * </pre>
 * Don't forget to catch the ElementNotFoundException in the methods it is thrown.
 */
public class IntHashtable {
  /** Exception thrown when allowDuplicateKeys is set to false. */
  public static class DuplicatedKeyException extends RuntimeException {
    public DuplicatedKeyException(String s) {
      super(s);
    }
  }

  /** Hashtable collision list. */
  protected static class Entry {
    public int key; // == hash
    public int value;
    public Entry next;
  }

  /** The hash table data. */
  protected Entry table[];
  /** The total number of entries in the hash table. */
  protected transient int count;
  /** Rehashes the table when count exceeds this threshold. */
  protected int threshold;
  /** The load factor for the hashtable. */
  protected double loadFactor;
  /** Computes the number of collisions for a set of inserts. You must zero this each time you want to compute it.
   * Here's a sample of how to determine the best values. Keep in mind that the lower collisions is better, but don't
   * waste too much memory if its too high.
   * <pre>
   * int max = 0xFFFFFFF;
   * for (int h = 5; ; h++)
   * {
   *    IntHashtable ht = new IntHashtable(h);
   *    ht.put("nbsp".hashCode(),' ');
   *    ht.put("shy".hashCode(),'­');
   *    ht.put("quot".hashCode(),'"');
   *    ...
   *    if (ht.collisions < max)
   *    {
   *       Vm.debug("h: "+h+" colli: "+ht.collisions);
   *       max = ht.collisions;
   *       if (max == 0)
   *          break;
   *    }
   * }
   * </pre>
   * @since SuperWaba 5.71.
   */
  public int collisions;

  /** Set to false to throw a IntHashtable.DuplicatedKeyException if you add a key that already exists. Its very rare to have
   * two Objects with same key, but it could occur. This is good to improve program's correctness.
   * @since TotalCross 1.14
   */
  public boolean allowDuplicateKeys = true; // guich@tc114_67

  /**
   * Constructs a new, empty hashtable with the specified initial capacity
   * and default load factor of 0.75f.
   *
   * @param   initialCapacity The number of elements you think the hashtable will end with. The hashtable will grow if necessary, but using
   * a number near or above the final size can improve performance.
   */
  public IntHashtable(int initialCapacity) {
    this(initialCapacity, 0.75f);
  }

  /**
   * Constructs a new, empty hashtable with the specified initial
   * capacity and the specified load factor.
   * If initialCapacity is zero, it is changed to 5.
   *
   * @param initialCapacity The number of elements you think the hashtable will end with. The hashtable will grow if necessary, but using
   * a number near or above the final size can improve performance.
   * @param loadFactor a number between 0.0 and 1.0.
   */
  public IntHashtable(int initialCapacity, double loadFactor) {
    if (initialCapacity <= 0) {
      initialCapacity = 5; // guich@310_6
    }
    initialCapacity = (int) (initialCapacity / loadFactor + 1); // guich@tc100: since most users just pass the number of element, compute the desired initial capacity based in the load factor
    this.loadFactor = loadFactor;
    table = new Entry[initialCapacity];
    threshold = (int) (initialCapacity * loadFactor);
  }

  /**
   * Clears this hashtable so that it contains no keys.
   */
  public void clear() {
    totalcross.sys.Convert.fill(table, 0, table.length, null);
    count = 0;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable.
   * @param key a key in the hashtable.
   * @return the value to which the key is mapped in this hashtable.
   * @throws totalcross.util.ElementNotFoundException When the key was not found.
   * @see #get(int, int)
   */
  public int get(int key) throws ElementNotFoundException {
    int index = (key & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if (e.key == key) {
        return e.value;
      }
    }
    throw new ElementNotFoundException("Key not found: " + key);
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable.
   * @param key an Object who's hashcode is the key in the hashtable.
   * @return the value to which the key is mapped in this hashtable.
   * @throws totalcross.util.ElementNotFoundException When the key was not found.
   * @throws NullPointerException If the key is null
   * @see #get(int, int)
   */
  public int get(Object key) throws ElementNotFoundException {
    return get(key.hashCode());
  }

  /**
   * Checks if the value with the specified key is mapped in this hashtable.
   * @param   key   a key in the hashtable.
   * @return  True if the key exists, false otherwise.
   * @since SuperWaba 5.8
   */
  public boolean exists(int key) // guich@580_29
  {
    int index = (key & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if (e.key == key) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return a Vector of the values in the Hashtable. The order is the same of the getKeys method.
   * @since SuperWaba 5.11
   */
  public IntVector getValues() // guich@511_10
  {
    return getKeysOrValues(false);
  }

  /**
   * Return an IntVector of the keys in the IntHashtable. The order is the same of the getValues method.
   * Added ds@120.
   * corrected by dgecawich@200
   */
  public IntVector getKeys() {
    return getKeysOrValues(true);
  }

  private IntVector getKeysOrValues(boolean isKeys) {
    // dgecawich 5/16/01 - fix so that all keys are returned rather than just the last one
    // the sympton for this was that getCount() always returned 1 regardless of how many items were added
    int[] array = new int[count]; // guich@511_10: optimized to avoid method calls
    int len = table.length, n = 0;
    for (int i = 0; i < len; i++) {
      Entry entry = table[i];
      while (entry != null) // guich@566_30
      {
        array[n++] = isKeys ? entry.key : entry.value;
        entry = entry.next;
      }
    }
    return new IntVector(array);
  }

  /** Takes out the hashCode from the given key object and calls put(int,int).
   * 
   *  To increase safeness, set <code>allowDuplicateKeys</code> to false.
   * @see #put(int, int)
   */
  public int put(Object key, int value) {
    try {
      return put(key.hashCode(), value);
    } catch (DuplicatedKeyException dke) {
      throw new DuplicatedKeyException(key.toString());
    }
  }

  /**
   * Maps the specified <code>key</code> to the specified
   * <code>value</code> in this hashtable.
   * <p>
   * The value can be retrieved by calling the <code>get</code> method
   * with a key that is equal to the original key.
   *
   * @param      key     the hashtable key.
   * @param      value   the value.
   * @return     the previous value of the specified key in this hashtable,
   *             or the given value if it did not have one.
   * @see     java.lang.Object#equals(java.lang.Object)
   * @see     #allowDuplicateKeys
   * @throws IntHashtable.DuplicatedKeyException if allowDuplicateKeys is set to false and another key is already added.
   */
  public int put(int key, int value) {
    // Makes sure the key is not already in the hashtable.
    Entry tab[] = table;
    int index = (key & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index]; e != null; e = e.next) {
      if (e.key == key) {
        if (!allowDuplicateKeys) {
          throw new DuplicatedKeyException(Convert.toString(key));
        }
        int old = e.value;
        e.value = value;
        return old;
      }
    }
    if (count >= threshold) {
      // Rehash the table if the threshold is exceeded
      rehash();
      return put(key, value);
    }

    // Creates the new entry.
    Entry e = new Entry();
    e.key = key;
    e.value = value;
    e.next = tab[index];
    if (e.next != null) {
      collisions++;
    }
    tab[index] = e;
    count++;
    return value; // guich@tc100: returns the given value instead of INVALID
  }

  /**
   * Rehashes the contents of the hashtable into a hashtable with a
   * larger capacity. This method is called automatically when the
   * number of keys in the hashtable exceeds this hashtable's capacity
   * and load factor.
   */
  protected void rehash() {
    int oldCapacity = table.length;
    Entry oldTable[] = table;

    int newCapacity = (((oldCapacity << 1) + oldCapacity) >> 1) + 1; // guich@120 - grows 50% instead of 100% - guich@200b4_198: added Peter Dickerson and Andrew Chitty changes to correct the optimization i made with << and >>
    Entry newTable[] = new Entry[newCapacity];

    threshold = (int) (newCapacity * loadFactor);
    table = newTable;

    for (int i = oldCapacity; i-- > 0;) {
      for (Entry old = oldTable[i]; old != null;) {
        Entry e = old;
        old = old.next;

        int index = (e.key & 0x7FFFFFFF) % newCapacity;
        e.next = newTable[index];
        newTable[index] = e;
      }
    }
  }

  /**
   * Removes the key (and its corresponding value) from this
   * hashtable. This method does nothing if the key is not in the hashtable.
   * @param   key   the key that needs to be removed.
   * @return  the value to which the key had been mapped in this hashtable,
   *          or <code>INVALID</code> if the key did not have a mapping.
   * @throws totalcross.util.ElementNotFoundException When the key was not found.
   */
  public int remove(int key) throws ElementNotFoundException {
    Entry tab[] = table;
    int index = (key & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
      if (e.key == key) {
        if (prev != null) {
          prev.next = e.next;
        } else {
          tab[index] = e.next;
        }
        count--;
        return e.value;
      }
    }
    throw new ElementNotFoundException("Key not found: " + key);
  }

  /**
   * Returns the number of keys in this hashtable.
   *
   * @return  the number of keys in this hashtable.
   */
  public int size() {
    return count;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable.
   * @param   key   a key in the hashtable.
   * @return  the value to which the key is mapped in this hashtable;
   *          or <code>defaultValue</code> if the key is not mapped to any value in
   *          this hashtable.
   * @since TotalCross 1.0
   */
  public int get(int key, int defaultValue) {
    int index = (key & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if (e.key == key) {
        return e.value;
      }
    }
    return defaultValue;
  }

  /** Returns the key at the given position, or throws ArrayIndexOutOfBounds if the given position does not exist. 
   * Note that the first key has no relation with the smallest key.
   * @since TotalCross 1.0
   * @throws ArrayIndexOutOfBoundsException If the position is out of range
   */
  public int getKey(int pos) {
    int len = table.length;
    int t = pos;
    for (int i = 0; i < len; i++) {
      Entry entry = table[i];
      while (entry != null) // guich@566_30
      {
        if (t-- <= 0) {
          return entry.key;
        }
        entry = entry.next;
      }
    }
    throw new ArrayIndexOutOfBoundsException("Position " + pos + " out of range");
  }

  /** Increments the value of a key by the given amount. If the key doesn't exist, a new one is created with
   * the amount. Otherwise, its value is changed by the amount. This method is useful to use an IntHashtable
   * as a multi counter.
   * @return The current value.
   * @since TotalCross 1.2
   */
  public int incrementValue(int key, int amount) {
    int current = get(key, 0);
    current += amount;
    put(key, current);
    return current;
  }
}