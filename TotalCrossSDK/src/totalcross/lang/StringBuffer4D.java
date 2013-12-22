/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



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

public final class StringBuffer4D
{
   /** The buffer is used for character storage. */
   private char charbuf[];
   /** The count is the number of characters in the buffer. */
   private int count;

   public static final int STARTING_SIZE = 16; // read by the StringBuffer test cases.

   /**
   * Constructs an empty String buffer.
   * Consider using the other constructor where you pass an initial size.
   */
   public StringBuffer4D()
   {
      charbuf = new char[STARTING_SIZE];
   }

   /**
   * Constructs an empty String buffer with the specified initial length.
   * @param length the initial length
   */
   public StringBuffer4D(int length)
   {
      charbuf = new char[length];
   }

   /**
   * Constructs a String buffer with the specified initial buffer.
   * @param str the initial buffer of the buffer
   */
   public StringBuffer4D(String4D str)
   {
      int len = count = str.chars.length; // nopt
      charbuf = new char[len + 10]; // guich@320_8
      String4D.copyChars(str.chars, 0, charbuf, 0, len);
   }

   /**
   * Returns the number of characters in the buffer.
   */
   public int length()
   {
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
   public int capacity()
   {
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
   public char charAt(int index)
   {
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
   public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin)
   {
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
   public StringBuffer4D append(boolean b)
   {
      return append(b?"true":"false");
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
   public String toString()
   {
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
   public StringBuffer4D reverse()
   {
      int i = count - 1;
      char []buf = charbuf;
      for(int j = (i - 1) >> 1; j >= 0; j--)
      {
          char c = buf[j];
          buf[j] = buf[i - j];
          buf[i - j] = c;
      }
      return this;
   }
   
   public StringBuffer4D append(StringBuffer4D s)
   {
      return this.append(s.toString());
   }
}