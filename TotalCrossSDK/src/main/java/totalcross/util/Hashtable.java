// Copyright (C) 2000-2001 Arthur van Hoff
// Copyright (C) 2000-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

/*
 * This class is almost identical to java.util.Hashtable, with some
modifications.
 */

/**
 * This class implements a hash table, which maps keys to values. Any
 * non-<code>null</code> object can be used as a key or as a value.
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
 *     Hashtable numbers = new Hashtable(3);
 *     numbers.put("one", Convert.toString(1));
 *     numbers.put("two", Convert.toString(2));
 *     numbers.put("three", Convert.toString(3));
 * </pre>
 * <p>
 * To retrieve a number, use the following code:
 * <pre>
 *     String n = (String)numbers.get("two");
 *     if (n != null) 
 *        // "two = " + Convert.toInt(n);
 * </pre>
 * This Hashtable class does not support Generics; use the HashMap class instead.

 */
public class Hashtable {
  /** Hashtable collision list. */
  protected static class Entry {
    public int hash;
    public Object key;
    public Object value;
    public Entry next;
  }

  /** The hash table data. */
  protected Entry table[];
  /** The total number of entries in the hash table. */
  private transient int count;
  /** Rehashes the table when count exceeds this threshold. */
  private int threshold;
  /** The load factor for the hashtable. */
  private double loadFactor;
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

  /** Setting this to true will allow the hashtable to have more than one key with the same value.
   * In this case, the methods will always return the first matching key.
   * @since TotalCross 1.24
   */
  public boolean allowDuplicateKeys;

  /**
   * Constructs a new, empty hashtable with the specified initial capacity
   * and default load factor of 0.75f.
   *
   * @param initialCapacity The number of elements you think the hashtable will end with. The hashtable will grow if necessary, but using
   * a number near or above the final size can improve performance.
  
   */
  public Hashtable(int initialCapacity) {
    init(initialCapacity, 0.75f);
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
  public Hashtable(int initialCapacity, double loadFactor) {
    init(initialCapacity, loadFactor);
  }

  /** Constructs a new hashtable, parsing the elements from the given String.
   * Each string must be in the form: key = value, splitted in lines.
   * This aids the task of creating resource bundles to add localization to your
   * application.
   * <br>You can include txt files in your application's pdb file using /t,
   * where each txt will hold the strings for a language. For example:
   * <pre>
   * // save these two lines in a file named EN.txt:
   * Message = Message
   * TestMsg = This is a test
   * Exit = Exit
   * // save these other two in a file named PT.txt:
   * Message = Mensagem
   * TestMsg = Isso é um teste
   * Exit = Sair
   * </pre>
   * The TotalCross deployer will include the two files referenced below in the tcz file.
   * <br>
   * Now, when your program starts, you can do:
   * <pre>
   * String txt = idiom == EN ? "EN.txt" : "PT.txt";
   * byte[] b = Vm.getFile(txt);
   * Hashtable res = new Hashtable(new String(b,0,b.length));
   * new MessageBox(res.get("Message"), res.get("TestMsg"), new String[]{res.get("Exit")}).popupNonBlocking();
   * </pre>
   * Note that the keys are <i>case sensitive</i>, and that all strings are trimmed.
   *
   * @since SuperWaba 5.72
   */
  public Hashtable(String res) // guich@572_17
  {
    String[] items = totalcross.sys.Convert.tokenizeString(res, '\n');
    init(items.length, 0.75); // guich@tc114_27
    for (int i = 0; i < items.length; i++) {
      String s = items[i];
      int eq = s.indexOf('=', 0);
      if (eq < 0) {
        continue;
      }
      put(s.substring(0, eq).trim(), s.substring(eq + 1).trim());
    }
  }

  /** Creates a Hashtable with the given keys and values.
   * The values can be two things:
   * <ol>
   * <li> An Object array (<code>Object[]</code>). In this case, the number of keys and values must match.
   * <li> A single Object. This object is set as value to all keys.
   * </ol>
   * The values parameter cannot be null.
   * @since TotalCross 1.5
   */
  public Hashtable(Object[] keys, Object values) {
    this(keys.length);
    Object[] objArray = values instanceof Object[] ? (Object[]) values : null;
    for (int i = 0; i < keys.length; i++) {
      put(keys[i], objArray != null ? objArray[i] : values);
    }
  }

  private void init(int initialCapacity, double loadFactor) // guich@tc114_27
  {
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
    Entry tab[] = table;
    if (count < 100) {
      for (int i = tab.length; --i >= 0;) {
        tab[i] = null; // faster for tables with few elements
      }
    } else {
      totalcross.sys.Convert.fill(tab, 0, tab.length, null);
    }
    count = 0;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable.
   * @param   key   a key in the hashtable.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>null</code> if the key is not mapped to any value in
   *          this hashtable.
   */
  public Object get(Object key) {
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return e.value;
      }
    }
    return null;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable as a String.
   * If the item is a String, a cast is made, otherwise, the toString method is called.
   * @param   key   a key in the hashtable.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>null</code> if the key is not mapped to any value in
   *          this hashtable.
   * @since TotalCross 1.24
   */
  public String getString(Object key) // guich@tc124_25
  {
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return e.value instanceof String ? (String) e.value : e.value.toString();
      }
    }
    return null;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable as a String.
   * If the item is a String, a cast is made, otherwise, the toString method is called.
   * @param   key   a key in the hashtable.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>defaultValue</code> if the key is not mapped to any value in
   *          this hashtable.
   * @since TotalCross 1.24
   */
  public String getString(Object key, String defaultValue) // guich@tc124_25
  {
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return e.value instanceof String ? (String) e.value : e.value.toString();
      }
    }
    return defaultValue;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable.
   * @param   key   a key in the hashtable.
   * @param   defaultValue The default value to be returned if none is found.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>defaultValue</code> if the key is not mapped to any value in
   *          this hashtable.
   * @since TotalCross 1.15
   */
  public Object get(Object key, Object defaultValue) // guich@tc115_47
  {
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return e.value;
      }
    }
    return defaultValue;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable.
   * @param   hash  The key hash in the hashtable.
   * @param   defaultValue The default value to be returned if none is found.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>defaultValue</code> if the key is not mapped to any value in
   *          this hashtable.
   * @since TotalCross 1.15
   */
  public Object get(int hash, Object defaultValue) // guich@tc115_47
  {
    Object r = get(hash);
    return r == null ? defaultValue : r;
  }

  /**
   * Returns the value to which the specified hash is mapped in this hashtable.
   * <p>
   * <b>Caution</b>: since you're passing an integer instead of an object, if there are two
   * objects that map to the same key, this method will always return the first one only.
   *
   * @param   hash  The key hash in the hashtable.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>null</code> if the key is not mapped to any value in
   *          this hashtable.
   */
  public Object get(int hash) {
    int index = (hash & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if (e.hash == hash) {
        return e.value;
      }
    }
    return null;
  }

  /**
   * Checks if the value with the specified key is mapped in this hashtable.
   * @param   key   a key in the hashtable.
   * @return  True if the key exists, false otherwise.
   * @since SuperWaba 5.8
   */
  public boolean exists(Object key) // guich@580_29
  {
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % table.length;
    for (Entry e = table[index]; e != null; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return a Vector of the keys in the Hashtable. The order is the same of the getValues method.
   * @see #getValues
   * @see #getKeyValuePairs
   */
  public Vector getKeys() {
    return getKeysValues(GET_KEYS, null);
  }

  /**
   * Return a Vector of the values in the Hashtable. The order is the same of the getKeys method.
   * @since SuperWaba 5.1
   * @see #getKeys
   * @see #getKeyValuePairs
   */
  public Vector getValues() // guich@510_8
  {
    return getKeysValues(GET_VALUES, null);
  }

  /**
   * Return a Vector with pairs in the form <code>key=value</code> from the Hashtable.
   * Each vector's element can safely be casted to a String.
   * @param separator the separator between the key and the value. Should be ": ","=", etc.
   * @since SuperWaba 5.1
   */
  public Vector getKeyValuePairs(String separator) // guich@510_8
  {
    return getKeysValues(GET_BOTH, separator);
  }

  private static final int GET_KEYS = 0;
  private static final int GET_VALUES = 1;
  private static final int GET_BOTH = 2;

  private Vector getKeysValues(int getType, String sep) {
    // dgecawich 5/16/01 - fix so that all keys are returned rather than just the last one
    // the sympton for this was that getCount() always returned 1 regardless of how many items were added
    Object[] v = new Object[count];
    if (table != null) {
      for (int i = 0, n = 0; i < table.length; i++) {
        for (Entry entry = table[i]; entry != null; entry = entry.next) {
          v[n++] = getType == GET_KEYS ? entry.key
              : getType == GET_VALUES ? entry.value : (entry.key + sep + entry.value);
        }
      }
    }
    return new Vector(v);
  }

  /** Copies the keys and values of this Hashtable into the given Hashtable.
   * Note that the target Hashtable is not cleared; you should do that by yourself.
   * @since TotalCross 1.15
   */
  public void copyInto(Hashtable target) // guich@tc115_17
  {
    if (table != null) {
      for (int i = 0; i < table.length; i++) {
        for (Entry entry = table[i]; entry != null; entry = entry.next) {
          if (entry.key != null) {
            target.put(entry.key, entry.value);
          } else {
            target.put(entry.hash, entry.value);
          }
        }
      }
    }
  }

  /**
   * Maps the specified <code>key</code> to the specified
   * <code>value</code> in this hashtable. Neither the key nor the
   * value can be <code>null</code>.
   * <p>
   * The value can be retrieved by calling the <code>get</code> method
   * with a key that is equal to the original key.
   *
   * @param      key     the hashtable key.
   * @param      value   the value.
   * @return     the previous value of the specified key in this hashtable,
   *             or <code>null</code> if it did not have one.
   * @see     java.lang.Object#equals(java.lang.Object)
   */
  public Object put(Object key, Object value) {
    // Make sure the value is not null
    if (value == null) {
      throw new NullPointerException("Argument 'value' cannot have a null value");
    }
    int hash = key.hashCode(); // flsobral@tc100b4_23: this operation throws NPE if key is null, no need to explicitly test that.

    // Makes sure the key is not already in the hashtable.
    Entry tab[] = table;
    int index = (hash & 0x7FFFFFFF) % tab.length;
    if (!allowDuplicateKeys) {
      for (Entry e = tab[index]; e != null; e = e.next) {
        if ((e.hash == hash) && e.key.equals(key)) {
          Object old = e.value;
          e.value = value;
          return old;
        }
      }
    }
    if (count >= threshold) {
      // Rehash the table if the threshold is exceeded
      rehash();
      return put(key, value);
    }

    // Creates the new entry.
    Entry e = new Entry();
    e.hash = hash;
    e.key = key;
    e.value = value;
    e.next = tab[index];
    if (e.next != null) {
      collisions++;
    }
    tab[index] = e;
    count++;
    return null;
  }

  /**
   * Maps the specified <code>key</code> to the specified
   * <code>value</code> in this hashtable. Neither the key nor the
   * value can be <code>null</code>.
   * <p>
   * The value can be retrieved by calling the <code>get</code> method
   * with a key that is equal to the original key.
   * <p>
   * This method receives a hashcode instead of the object. You MUST use the get(int)
   * method to retrieve the value, otherwise you will get a NullPointerException, because
   * no key is stored using this method.
   *
   * @param      hash     the hashtable key's hash.
   * @param      value   the value.
   * @return     the previous value of the specified key in this hashtable,
   *             or <code>null</code> if it did not have one.
   * @see     java.lang.Object#equals(java.lang.Object)
   */
  public Object put(int hash, Object value) {
    // Make sure the value is not null
    if (value == null) {
      throw new NullPointerException("Argument 'value' cannot have a null value");
    }
    // Makes sure the key is not already in the hashtable.
    Entry tab[] = table;
    int index = (hash & 0x7FFFFFFF) % tab.length;
    if (!allowDuplicateKeys) {
      for (Entry e = tab[index]; e != null; e = e.next) {
        if (e.hash == hash) {
          Object old = e.value;
          e.value = value;
          return old;
        }
      }
    }
    if (count >= threshold) {
      // Rehash the table if the threshold is exceeded
      rehash();
      return put(hash, value);
    }

    // Creates the new entry.
    Entry e = new Entry();
    e.hash = hash;
    e.value = value;
    e.next = tab[index];
    if (e.next != null) {
      collisions++;
    }
    tab[index] = e;
    count++;
    return null;
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

        int index = (e.hash & 0x7FFFFFFF) % newCapacity;
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
   *          or <code>null</code> if the key did not have a mapping.
   */
  public Object remove(Object key) {
    Entry tab[] = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        if (prev != null) {
          prev.next = e.next;
        } else {
          tab[index] = e.next;
        }
        count--;
        return e.value;
      }
    }
    return null;
  }

  /**
   * Removes the key (and its corresponding value) from this
   * hashtable. This method does nothing if the key is not in the hashtable.
   * @param   hash the hash code of the key that needs to be removed.
   * @return  the value to which the key had been mapped in this hashtable,
   *          or <code>null</code> if the key did not have a mapping.
   */
  public Object remove(int hash) // flsobral@tc100b4: Added method remove(int hash)
  {
    Entry tab[] = table;
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index], prev = null; e != null; prev = e, e = e.next) {
      if (e.hash == hash) {
        if (prev != null) {
          prev.next = e.next;
        } else {
          tab[index] = e.next;
        }
        count--;
        return e.value;
      }
    }
    return null;
  }

  /** Returns the number of keys in this hashtable. */
  public int size() {
    return count;
  }

  /** Dumps the keys and values into the given StringBuffer.
   * @param sb The StringBuffer where the data will be dumped to
   * @param keyvalueSeparator The separator between the key and the value (E.G.: ": ")
   * @param lineSeparator The separator placed after each key+value pair (E.G.: "\r\n"). The last separator is cut from the StringBuffer.
   * @since TotalCross 1.23
   */
  public StringBuffer dumpKeysValues(StringBuffer sb, String keyvalueSeparator, String lineSeparator) {
    if (table != null) {
      for (int i = 0; i < table.length; i++) {
        for (Entry entry = table[i]; entry != null; entry = entry.next) {
          sb.append(entry.key).append(keyvalueSeparator).append(entry.value).append(lineSeparator);
        }
      }
    }
    int l = sb.length();
    if (l > 0) {
      sb.setLength(l - lineSeparator.length());
    }
    return sb;
  }
}