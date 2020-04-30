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

import totalcross.io.DataStream;
import totalcross.io.EOFException;
import totalcross.sys.Convert;

/**
 * Used to store properties pairs (key,value). A hashtable is used to
 * store them. Currently, the key must be a String and the value must be a Value
 * The properties can be saved and loaded to/from a DataStream.
 * Here's a sample:
 * <pre>
   Properties props = new Properties();
   File file;      
   //***************************************
   // load properties that already exists
   //***************************************
   if (existe_arquivo(arquivo))
   {
      file = new File(arquivo, File.READ_WRITE);
      props.load(new DataStream(file));
      file.close();
   }     
   //***************************************
   props.put(propriedade,new Properties.Str(valor));
   file = new File(arquivo, File.CREATE);
   props.save(new DataStream(file));
   file.close();     
 * </pre>
 * @see totalcross.util.Properties.Value
 * @see totalcross.util.Properties.Str
 * @see totalcross.util.Properties.Int
 * @see totalcross.util.Properties.Double
 * @see totalcross.util.Properties.Boolean
 * @see totalcross.util.Properties.Long
 */
public class Properties {
  private Hashtable props;
  /**
   * Avoids that the load method gets into an infinite loop if the file is
   * empty or corrupted. This constant limits the number of properties to 1000.
   * If you are saving more than 1000 props, just change this max value.
   *
   */
  public static int MAX_PROPS = 1000; // guich@556_2

  public Properties() {
    props = new Hashtable(13);
  }

  /** Stores the given keys/values pairs in a new Properties. */
  public Properties(String[] keys, Value[] values) {
    int n = keys.length;
    props = new Hashtable(n << 1);
    for (int i = 0; i < n; i++) {
      props.put(keys[i], values[i]);
    }
  }

  /** Represents a generic value that can be stored here. */
  public abstract static class Value {
    /** Read-only property, which identifies the value type */
    public char type;

    @Override
    public abstract String toString();

    /** The full name of the type. */
    public String typeStr;
  }

  /** Implements a value of type String */
  public static class Str extends Value {
    public final static char TYPE = 'S';
    public String value;

    public Str(String value) {
      this.value = value;
      type = TYPE;
      typeStr = "String";
    }

    @Override
    public String toString() {
      return value;
    }

    /**
     * Returns a hash code for this Str.
     * 
     * @return the hash code of the enclosed String value.
     * @since TotalCross 1.25
     */
    @Override
    public int hashCode() {
      return value.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is true if and only if the argument is not null and is
     * a String object, or another Str object, that represents the same sequence of characters as this object.
     * 
     * @since TotalCross 1.25
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj instanceof String) {
        return value.equals(obj);
      }
      if (obj instanceof Str) {
        return value.equals(((Str) obj).value);
      }
      return false;
    }
  }

  /** Implements a value of type int */
  public static class Int extends Value {
    public final static char TYPE = 'I';
    public int value;

    public Int(int value) {
      this.value = value;
      type = TYPE;
      typeStr = "int";
    }

    @Override
    public String toString() {
      return Convert.toString(value);
    }

    /**
     * Returns a hash code for this Int.
     * 
     * @return a hash code value for this object, equal to the primitive int value represented by this Int object.
     * @since TotalCross 1.25
     */
    @Override
    public int hashCode() {
      return value;
    }

    /**
     * Compares this object to the specified object. The result is true if and only if the argument is not null and is
     * an Int object that contains the same int value as this object.
     * 
     * @since TotalCross 1.25
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj instanceof Int) {
        return value == ((Int) obj).value;
      }
      return false;
    }
  }

  /** Implements a value of type double */
  public static class Double extends Value {
    public final static char TYPE = 'D';
    public double value;

    public Double(double value) {
      this.value = value;
      type = TYPE;
      typeStr = "double";
    }

    @Override
    public String toString() {
      return Convert.toString(value);
    }

    /**
     * Returns a hash code for this <code>Double</code> object. The result is the exclusive OR of the two halves of
     * the <code>long</code> integer bit representation, exactly as produced by the method
     * {@link Convert#doubleToLongBits(double) doubleToLongBits(double)}, of the primitive <code>double</code> value
     * represented by this <code>Double</code> object. That is, the hash code is the value of the expression:
     * <blockquote>
     * 
     * <pre>
     * (int) (v &circ; (v &gt;&gt;&gt; 32))
     * </pre>
     * 
     * </blockquote> where <code>v</code> is defined by: <blockquote>
     * 
     * <pre>
     * long v = Convert.doubleToLongBits(this.value);
     * </pre>
     * 
     * </blockquote>
     * 
     * @since TotalCross 1.25
     */
    @Override
    public int hashCode() {
      long v = Convert.doubleToLongBits(value);
      return (int) (v ^ (v >>> 32));
    }

    /**
     * Compares this object against the specified object. The result is <code>true</code> if and only if the argument
     * is not <code>null</code> and is a <code>Double</code> object that represents a <code>double</code> that has the
     * same value as the <code>double</code> represented by this object. For this purpose, two <code>double</code>
     * values are considered to be the same if and only if the method {@link Convert#doubleToLongBits(double)
     * doubleToLongBits(double)} returns the identical <code>long</code> value when applied to each.
     * <p>
     * Note that in most cases, for two instances of class <code>Double</code>, <code>d1</code> and <code>d2</code>,
     * the value of <code>d1.equals(d2)</code> is <code>true</code> if and only if <blockquote>
     * 
     * <pre>
     * Convert.doubleToLongBits(d1.value) == Convert.doubleToLongBits(d2.value)
     * </pre>
     * 
     * </blockquote>
     * <p>
     * also has the value <code>true</code>. However, there are two exceptions:
     * <ul>
     * <li>If <code>d1</code> and <code>d2</code> both represent <code>Convert.DOUBLE_NAN_BITS</code>, then the
     * <code>equals</code> method returns <code>true</code>, even though
     * <code>Convert.doubleToLongBits(Convert.DOUBLE_NAN_BITS) == Convert.doubleToLongBits(Convert.DOUBLE_NAN_BITS)</code>
     * has the value <code>false</code>.
     * <li>If <code>d1</code> represents <code>+0.0</code> while <code>d2</code> represents <code>-0.0</code>, or vice
     * versa, the <code>equal</code> test has the value <code>false</code>, even though <code>+0.0==-0.0</code> has
     * the value <code>true</code>.
     * </ul>
     * This definition allows hash tables to operate properly.
     * 
     * @since TotalCross 1.25
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj instanceof Double) {
        return Convert.doubleToLongBits(value) == Convert.doubleToLongBits(((Double) obj).value);
      }
      return false;
    }
  }

  /** Implements a value of type boolean */
  public static class Boolean extends Value {
    public final static char TYPE = 'B';
    public boolean value;

    public Boolean(boolean value) {
      this.value = value;
      type = TYPE;
      typeStr = "boolean";
    }

    @Override
    public String toString() {
      return value ? "1" : "0";
    }

    /**
     * Returns a hash code for this Boolean object.
     * 
     * @return the integer 1231 if this object represents true; returns the integer 1237 if this object represents
     *         false.
     * @since TotalCross 1.25
     */
    @Override
    public int hashCode() {
      return value ? 1231 : 1237;
    }

    /**
     * Returns true if and only if the argument is not null and is a Boolean object that represents the same boolean
     * value as this object.
     * 
     * @since TotalCross 1.25
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj instanceof Boolean) {
        return value == ((Boolean) obj).value;
      }
      return false;
    }
  }

  /** Implements a value of type long */
  public static class Long extends Value {
    public final static char TYPE = 'L';
    public long value;

    public Long(long value) {
      this.value = value;
      type = TYPE;
      typeStr = "long";
    }

    @Override
    public String toString() {
      return Convert.toString(value);
    }

    /**
     * Returns a hash code for this <code>Long</code>. The result is the exclusive OR of the two halves of the
     * primitive <code>long</code> value held by this <code>Long</code> object. That is, the hashcode is the value of
     * the expression: <blockquote>
     * 
     * <pre>
     * (int) (this.value &circ; (this.value &gt;&gt;&gt; 32))
     * </pre>
     * 
     * </blockquote>
     * 
     * @since TotalCross 1.25
     */
    @Override
    public int hashCode() {
      return (int) (value ^ (value >>> 32));
    }

    /**
     * Compares this object to the specified object. The result is true if and only if the argument is not null and is
     * a Long object that contains the same long value as this object.
     * 
     * @since TotalCross 1.25
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj instanceof Long) {
        return value == ((Long) obj).value;
      }
      return false;
    }
  }

  /** Put the given key/value pair in the hashtable that stores the properties. */
  public void put(String key, Value v) {
    props.put(key, v);
  }

  /** Get the value given the key from the hashtable that stores the properties. */
  public Value get(String key) {
    return (Value) props.get(key);
  }

  /** Returns the number of properties */
  public int size() {
    return props.size();
  }

  /** Returns a Vector with the current keys */
  public Vector getKeys() {
    return props.getKeys();
  }

  /** Clears this property */
  public void clear() {
    props.clear();
  }

  /** Remove a value from the property */
  public void remove(String key) {
    props.remove(key);
  }

  /** Save all properties in the given DataStream
   * @throws totalcross.io.IOException */
  public void save(DataStream ds) throws totalcross.io.IOException {
    // get all keys, store everything in database
    Vector vec = props.getKeys();
    int n = vec.size();
    for (int i = 0; i < n; i++) {
      String key = (String) vec.items[i];
      Value v = (Value) props.get(key);
      byte type = (byte) v.type;
      ds.writeByte(type);
      ds.writeString(key);
      switch (type) {
      case Str.TYPE:
        ds.writeString(((Str) v).value);
        break;
      case Int.TYPE:
        ds.writeInt(((Int) v).value);
        break;
      case Double.TYPE:
        ds.writeDouble(((Double) v).value);
        break;
      case Boolean.TYPE:
        ds.writeBoolean(((Boolean) v).value);
        break;
      case Long.TYPE:
        ds.writeLong(((Long) v).value);
        break;
      }
    }
    ds.writeByte(255);
  }

  /**
   * Load all properties from the given DataStream. Before calling this method,
   * be sure that there's something to be read (ie, that the file is not
   * empty), otherwise it may run in an infinite loop and will freeze your
   * device.
   *
   * @throws totalcross.io.IOException
   */
  public void load(DataStream ds) throws totalcross.io.IOException {
    load(ds, true);
  }

  /**
   * Load properties from the given DataStream. If cleanBeforeLoad is true, the contents of this object will be cleared
   * before reading from the DataStream.
   * 
   * @param ds
   * @param cleanBeforeLoad
   * @throws totalcross.io.IOException
   */
  public void load(DataStream ds, boolean cleanBeforeLoad) throws totalcross.io.IOException {
    if (cleanBeforeLoad) {
      props.clear();
    }
    // read and populate the options hashtable from pdb
    try {
      for (int i = MAX_PROPS; i >= 0; i--) {
        byte type = ds.readByte();
        String key = ds.readString();
        Value v = null;
        switch (type) {
        case Str.TYPE:
          v = new Str(ds.readString());
          break;
        case Int.TYPE:
          v = new Int(ds.readInt());
          break;
        case Double.TYPE:
          v = new Double(ds.readDouble());
          break;
        case Boolean.TYPE:
          v = new Boolean(ds.readBoolean());
          break;
        case Long.TYPE:
          v = new Long(ds.readLong());
          break;
        }
        if (v != null) {
          props.put(key, v);
        }
      }
    } catch (EOFException e) {
      // Don't throw EOF.
    }
  }

  /**
   * Dumps the keys and values into the given StringBuffer. The values are dumped as Strings, exactly like the
   * Hashtable implementation.
   * 
   * @param sb
   *           The StringBuffer where the data will be dumped to
   * @param keyvalueSeparator
   *           The separator between the key and the value (E.G.: ": ")
   * @param lineSeparator
   *           The separator placed after each key+value pair (E.G.: "\r\n"). The last separator is cut from the
   *           StringBuffer.
   * @return the received StringBuffer sb
   * @since TotalCross 1.25
   */
  public StringBuffer dumpKeysValues(StringBuffer sb, String keyvalueSeparator, String lineSeparator) {
    return props.dumpKeysValues(sb, keyvalueSeparator, lineSeparator);
  }
}
