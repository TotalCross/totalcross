// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

/** 
 * This class is a growable buffer for characters. It is mainly used
 * to create Strings. The compiler uses it to implement the "+" operator.
 * For example:
 * <pre>
 * "a" + 4 + "c"
 * </pre>
 * is compiled to:
 * <pre>
 * new StringBuffer().append("a").append(4).append("c").toString()
 * </pre>
 * ... to concatenate multiple strings together. In the code shown, the
 * compiler generates references to the StringBuffer class to append the
 * objects together.
 * <p>
 * If you use it in loop, consider creating a new StringBuffer with an
 * initial number of characters, and calling setLength before each use.
 * This can improve the performance up to 10x.
 * Example:
 * <pre>
 * StringBuffer sb = new StringBuffer(1024);
 * for (...)
 * {
 *    sb.setLength(0);
 *    sb.append(...).append(...).append(...);
 *    String s = sb.toString();
 * }
 * </pre>
 * IMPORTANT: the totalcross.lang package is the java.lang that will be used in the device.
 * You CANNOT use nor import totalcross.lang package in desktop. When tc.Deploy is called,
 * all references to java.lang are replaced by totalcross.lang automatically. Given this,
 * you must use only the classes and methods that exists BOTH in java.lang and totalcross.lang.
 * For example, you can't use java.lang.ClassLoader because there are no totalcross.lang.ClassLoader.
 * Another example, you can't use java.lang.String.indexOfIgnoreCase because there are no
 * totalcross.lang.String.indexOfIgnoreCase method. Trying to use a class or method from the java.lang package
 * that has no correspondence with totalcross.lang will make the tc.Deploy program to abort, informing
 * where the problem occured. A good idea is to always refer to this javadoc to know what is and what isn't
 * available.
 */

public final class StringBuffer4D {
  /** The buffer is used for character storage. */
  char charbuf[];
  /** The count is the number of characters in the buffer. */
  int count;

  public static final int STARTING_SIZE = 16; // read by the StringBuffer test cases.

  /**
   * Constructs an empty String buffer.
   * Consider using the other constructor where you pass an initial size.
   */
  public StringBuffer4D() {
    charbuf = new char[STARTING_SIZE];
  }

  /**
   * Constructs an empty String buffer with the specified initial length.
   * @param length the initial length
   */
  public StringBuffer4D(int length) {
    charbuf = new char[length];
  }

  /**
   * Constructs a String buffer with the specified initial buffer.
   * @param str the initial buffer of the buffer
   */
  public StringBuffer4D(String4D str) {
    int len = count = str.chars.length; // nopt
    charbuf = new char[len + 10]; // guich@320_8
    String4D.copyChars(str.chars, 0, charbuf, 0, len);
  }

  /**
   * Returns the number of characters in the buffer.
   */
  public int length() {
    return count;
  }

  /** Replaces the char at the given index.
   * @since SuperWaba 4.02
   */
  public void setCharAt(int index, char ch) // guich@402_20
  {
    charbuf[index] = ch;
  }

  /**
   * Returns the current capacity of the String buffer. The capacity
   * is the amount of storage available for newly inserted
   * characters; beyond which a buffer reallocation will occur.
   */
  public int capacity() {
    return charbuf.length;
  }

  /**
   * Ensures that the capacity of the buffer is at least equal to the
   * specified minimum.
   * @param minimumCapacity the minimum desired capacity in characters
   */
  native public void ensureCapacity(int minimumCapacity);

  /**
   * Sets the length of the String. If the length is reduced, characters
   * are lost. If the length is extended, the values of the new characters
   * are set to 0.
   * @param newLength the new length of the buffer
   */
  native public void setLength(int newLength);

  /**
   * Returns the character at the specified index. An index ranges
   * from 0..length()-1.
   * @param index  the index of the desired character
   */
  public char charAt(int index) {
    return (index >= 0 && index < count) ? charbuf[index] : '\0';
  }

  /**
   * Copies the characters of the specified substring (determined by
   * srcBegin and srcEnd) into the character array, starting at the
   * array's dstBegin location. Both srcBegin and srcEnd must be legal
   * indexes into the buffer.
   * @param srcBegin  begin copy at this offset in the String
   * @param srcEnd stop copying at this offset in the String
   * @param dst    the array to copy the data into
   * @param dstBegin  offset into dst
   */
  public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
    String4D.copyChars(charbuf, srcBegin, dst, dstBegin, srcEnd - srcBegin);
  }

  /**
   * Appends an object to the end of this buffer.
   * @param obj the object to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  public StringBuffer4D append(Object obj) // can't be native bc obj.toString is implemented in Java
  {
    return append(obj != null ? obj.toString() : "null");
  }

  /**
   * Appends a String to the end of this buffer.
   * @param str the String to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  native public StringBuffer4D append(String str);

  /**
   * Appends a String to the end of this buffer.
   * @param str the String that has a part to be appended
   * @param start the start index of the substring to be appended
   * @param end the end index of the Substring to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  public StringBuffer4D append(String str, int start, int end) {
    return this.append(str.substring(start, end));
  }

  public StringBuffer4D append(CharSequence seq) {
    return this.append(seq.toString());
  }

  public StringBuffer4D append(CharSequence seq, int start, int end) {
    return this.append(seq.subSequence(start, end).toString());
  }

  /**
   * Appends an array of characters to the end of this buffer.
   * @param str the characters to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  native public StringBuffer4D append(char str[]);

  /**
   * Appends a range of an array of characters to the end of this buffer.
   * @param str the characters to be appended
   * @param offset where to start
   * @param len the number of characters to add
   * @return    the StringBuffer itself, NOT a new one.
   */
  native public StringBuffer4D append(char str[], int offset, int len);

  /**
   * Appends a boolean to the end of this buffer.
   * @param b   the boolean to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  public StringBuffer4D append(boolean b) {
    return append(b ? "true" : "false");
  }

  /**
   * Appends a character to the end of this buffer.
   * @param c  the character to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  native public StringBuffer4D append(char c);

  /**
   * Appends an integer to the end of this buffer.
   * @param i   the integer to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  native public StringBuffer4D append(int i);

  /**
   * Appends a long to the end of this buffer.
   * @param l   the long to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  native public StringBuffer4D append(long l);

  /**
   * Appends a double to the end of this buffer.
   * @param d   the double to be appended
   * @return    the StringBuffer itself, NOT a new one.
   */
  native public StringBuffer4D append(double d);

  /**
   * Converts to a String representing the data in the buffer.
   */
  @Override
  public String toString() {
    return new String(charbuf, 0, count);
  }

  /**
   * Removes the characters in a substring of this <code>StringBuffer</code>.
   * The substring begins at the specified <code>start</code> and extends to
   * the character at index <code>end - 1</code> or to the end of the
   * <code>StringBuffer</code> if no such character exists. If
   * <code>start</code> is equal to <code>end</code>, no changes are made.
   * If any paramter goes beyond limits, it is enforced into limits.
   * If start > end, the string is emptied;
   * Returns <code>this</code> StringBuffer.
   */
  native public StringBuffer4D delete4D(int start, int end); // guich@330_30

  /** Reverses the string.
   * @since SuperWaba 4.5
   */
  public StringBuffer4D reverse() {
    int i = count - 1;
    char[] buf = charbuf;
    for (int j = (i - 1) >> 1; j >= 0; j--) {
      char c = buf[j];
      buf[j] = buf[i - j];
      buf[i - j] = c;
    }
    return this;
  }

  /**
   * Append the <code>StringBuffer4D</code> value of the argument to this
   * <code>StringBuffer4D</code>. This behaves the same as
   * <code>append((Object) stringBuffer)</code>, except it is more efficient.
   *
   * @param stringBuffer the <code>StringBuffer4D</code> to convert and append
   * @return this <code>StringBuffer4D</code>
   * @see #append(Object)
   */
  public StringBuffer4D append(StringBuffer4D stringBuffer) {
    return append(stringBuffer.charbuf, 0, count);
  }

  /**
   * Delete a character from this <code>StringBuffer4D</code>.
   *
   * @param index the index of the character to delete
   * @return this <code>StringBuffer4D</code>
   * @throws StringIndexOutOfBoundsException if index is out of bounds
   */
  public StringBuffer4D deleteCharAt(int index) {
    return delete4D(index, index + 1);
  }

  /**
   * Replace characters between index <code>start</code> (inclusive) and
   * <code>end</code> (exclusive) with <code>str</code>. If <code>end</code>
   * is larger than the size of this StringBuffer4D, all characters after
   * <code>start</code> are replaced.
   *
   * @param start the beginning index of characters to delete (inclusive)
   * @param end the ending index of characters to delete (exclusive)
   * @param str the new <code>String</code> to insert
   * @return this <code>StringBuffer4D</code>
   * @throws StringIndexOutOfBoundsException if start or end are out of bounds
   * @throws NullPointerException if str is null
   */
  public StringBuffer4D replace(int start, int end, String4D str) {
    if (start < 0 || start > count || start > end) {
      throw new StringIndexOutOfBoundsException(start);
    }

    int len = str.chars.length;
    // Calculate the difference in 'count' after the replace.
    int delta = len - (end > count ? count : end) + start;
    ensureCapacity(count + delta);

    if (delta != 0 && end < count) {
      String4D.copyChars(charbuf, end, charbuf, end + delta, count - end);
    }

    str.getChars(0, len, charbuf, start);
    count += delta;
    return this;
  }

  /**
   * Creates a substring of this StringBuffer4D, starting at a specified index
   * and ending at the end of this StringBuffer4D.
   *
   * @param beginIndex index to start substring (base 0)
   * @return new String which is a substring of this StringBuffer4D
   * @throws StringIndexOutOfBoundsException if beginIndex is out of bounds
   * @see #substring(int, int)
   */
  public String4D substring(int beginIndex) {
    return substring(beginIndex, count);
  }

  /**
   * Creates a substring of this StringBuffer4D, starting at a specified index
   * and ending at one character before a specified index.
   *
   * @param beginIndex index to start at (inclusive, base 0)
   * @param endIndex index to end at (exclusive)
   * @return new String which is a substring of this StringBuffer4D
   * @throws StringIndexOutOfBoundsException if beginIndex or endIndex is out
   *         of bounds
   */
  public String4D substring(int beginIndex, int endIndex) {
    int len = endIndex - beginIndex;
    if (beginIndex < 0 || endIndex > count || endIndex < beginIndex) {
      throw new StringIndexOutOfBoundsException();
    }
    if (len == 0) {
      return new String4D();
    }
    return new String4D(charbuf, beginIndex, len);
  }

  /**
   * Insert a subarray of the <code>char[]</code> argument into this
   * <code>StringBuffer4D</code>.
   *
   * @param offset the place to insert in this buffer
   * @param str the <code>char[]</code> to insert
   * @param str_offset the index in <code>str</code> to start inserting from
   * @param len the number of characters to insert
   * @return this <code>StringBuffer4D</code>
   * @throws NullPointerException if <code>str</code> is <code>null</code>
   * @throws StringIndexOutOfBoundsException if any index is out of bounds
   */
  public StringBuffer4D insert(int offset, char[] str, int str_offset, int len) {
    if (offset < 0 || offset > count || len < 0 || str_offset < 0 || str_offset > str.length - len) {
      throw new StringIndexOutOfBoundsException();
    }
    ensureCapacity(count + len);
    String4D.copyChars(charbuf, offset, charbuf, offset + len, count - offset);
    String4D.copyChars(str, str_offset, charbuf, offset, len);
    count += len;
    return this;
  }

  /**
   * Insert the <code>String</code> value of the argument into this
   * <code>StringBuffer4D</code>. Uses <code>String.valueOf()</code> to convert
   * to <code>String</code>.
   *
   * @param offset the place to insert in this buffer
   * @param obj the <code>Object</code> to convert and insert
   * @return this <code>StringBuffer4D</code>
   * @exception StringIndexOutOfBoundsException if offset is out of bounds
   * @see String#valueOf(Object)
   */
  public StringBuffer4D insert(int offset, Object obj) {
    return insert(offset, obj == null ? "null" : obj.toString());
  }

  /**
   * Insert the <code>String</code> argument into this
   * <code>StringBuffer4D</code>. If str is null, the String "null" is used
   * instead.
   *
   * @param offset the place to insert in this buffer
   * @param str the <code>String</code> to insert
   * @return this <code>StringBuffer4D</code>
   * @throws StringIndexOutOfBoundsException if offset is out of bounds
   */
  public StringBuffer4D insert(int offset, String4D str) {
    if (offset < 0 || offset > count) {
      throw new StringIndexOutOfBoundsException(offset);
    }
    if (str == null) {
      str = new String4D(new char[] { 'n', 'u', 'l', 'l' });
    }
    int len = str.chars.length;
    ensureCapacity(count + len);
    String4D.copyChars(charbuf, offset, charbuf, offset + len, count - offset);
    str.getChars(0, len, charbuf, offset);
    count += len;
    return this;
  }

  /**
   * Insert the <code>char[]</code> argument into this
   * <code>StringBuffer4D</code>.
   *
   * @param offset the place to insert in this buffer
   * @param data the <code>char[]</code> to insert
   * @return this <code>StringBuffer4D</code>
   * @throws NullPointerException if <code>data</code> is <code>null</code>
   * @throws StringIndexOutOfBoundsException if offset is out of bounds
   * @see #insert(int, char[], int, int)
   */
  public StringBuffer4D insert(int offset, char[] data) {
    return insert(offset, data, 0, data.length);
  }

  /**
   * Insert the <code>String</code> value of the argument into this
   * <code>StringBuffer4D</code>. Uses <code>String.valueOf()</code> to convert
   * to <code>String</code>.
   *
   * @param offset the place to insert in this buffer
   * @param bool the <code>boolean</code> to convert and insert
   * @return this <code>StringBuffer4D</code>
   * @throws StringIndexOutOfBoundsException if offset is out of bounds
   * @see String#valueOf(boolean)
   */
  public StringBuffer4D insert(int offset, boolean bool) {
    return insert(offset, bool ? "true" : "false");
  }

  /**
   * Insert the <code>char</code> argument into this <code>StringBuffer4D</code>.
   *
   * @param offset the place to insert in this buffer
   * @param ch the <code>char</code> to insert
   * @return this <code>StringBuffer4D</code>
   * @throws StringIndexOutOfBoundsException if offset is out of bounds
   */
  public StringBuffer4D insert(int offset, char ch) {
    if (offset < 0 || offset > count) {
      throw new StringIndexOutOfBoundsException(offset);
    }
    ensureCapacity(count + 1);
    String4D.copyChars(charbuf, offset, charbuf, offset + 1, count - offset);
    charbuf[offset] = ch;
    count++;
    return this;
  }

  /**
   * Insert the <code>String</code> value of the argument into this
   * <code>StringBuffer4D</code>. Uses <code>String.valueOf()</code> to convert
   * to <code>String</code>.
   *
   * @param offset the place to insert in this buffer
   * @param inum the <code>int</code> to convert and insert
   * @return this <code>StringBuffer4D</code>
   * @throws StringIndexOutOfBoundsException if offset is out of bounds
   * @see String#valueOf(int)
   */
  public StringBuffer4D insert(int offset, int inum) {
    return insert(offset, String.valueOf(inum));
  }

  /**
   * Insert the <code>String</code> value of the argument into this
   * <code>StringBuffer4D</code>. Uses <code>String.valueOf()</code> to convert
   * to <code>String</code>.
   *
   * @param offset the place to insert in this buffer
   * @param lnum the <code>long</code> to convert and insert
   * @return this <code>StringBuffer4D</code>
   * @throws StringIndexOutOfBoundsException if offset is out of bounds
   * @see String#valueOf(long)
   */
  public StringBuffer4D insert(int offset, long lnum) {
    return insert(offset, String.valueOf(lnum));
  }

  /**
   * Insert the <code>String</code> value of the argument into this
   * <code>StringBuffer4D</code>. Uses <code>String.valueOf()</code> to convert
   * to <code>String</code>.
   *
   * @param offset the place to insert in this buffer
   * @param dnum the <code>double</code> to convert and insert
   * @return this <code>StringBuffer4D</code>
   * @throws StringIndexOutOfBoundsException if offset is out of bounds
   * @see String#valueOf(double)
   */
  public StringBuffer4D insert(int offset, double dnum) {
    return insert(offset, String.valueOf(dnum));
  }

  /**
   * Finds the first instance of a substring in this StringBuilder.
   *
   * @param str String to find
   * @return location (base 0) of the String, or -1 if not found
   * @throws NullPointerException if str is null
   */
  public int indexOf(String4D str) {
    return indexOf(str, 0);
  }

  /**
   * Finds the first instance of a String in this StringBuffer, starting at
   * a given index.  If starting index is less than 0, the search starts at
   * the beginning of this String.  If the starting index is greater than the
   * length of this String, or the substring is not found, -1 is returned.
   *
   * @param str String to find
   * @param fromIndex index to start the search
   * @return location (base 0) of the String, or -1 if not found
   * @throws NullPointerException if str is null
   * @since 1.4
   */
  public int indexOf(String4D str, int fromIndex) {
    if (fromIndex < 0) {
      fromIndex = 0;
    }
    int limit = count - str.chars.length;
    for (; fromIndex <= limit; fromIndex++) {
      if (regionMatches(fromIndex, str)) {
        return fromIndex;
      }
    }
    return -1;
  }

  /**
   * Finds the last instance of a substring in this StringBuffer.
   *
   * @param str String to find
   * @return location (base 0) of the String, or -1 if not found
   * @throws NullPointerException if str is null
   */
  public int lastIndexOf(String4D str) {
    return lastIndexOf(str, count - str.chars.length);
  }

  /**
   * Finds the last instance of a String in this StringBuffer, starting at a
   * given index.  If starting index is greater than the maximum valid index,
   * then the search begins at the end of this String.  If the starting index
   * is less than zero, or the substring is not found, -1 is returned.
   *
   * @param str String to find
   * @param fromIndex index to start the search
   * @return location (base 0) of the String, or -1 if not found
   * @throws NullPointerException if str is null
   */
  public int lastIndexOf(String4D str, int fromIndex) {
    fromIndex = Math.min(fromIndex, count - str.chars.length);
    for (; fromIndex >= 0; fromIndex--) {
      if (regionMatches(fromIndex, str)) {
        return fromIndex;
      }
    }
    return -1;
  }

  private boolean regionMatches(int toffset, String4D other) {
    int len = other.chars.length;
    int index = 0;
    while (--len >= 0) {
      if (charbuf[toffset++] != other.chars[index++]) {
        return false;
      }
    }
    return true;
  }
}