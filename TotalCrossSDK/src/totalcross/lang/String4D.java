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

import totalcross.sys.*;

/** 
 * String is an <i>immutable</i> array of characters.
 * <br><br>
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

public final class String4D
{
   char chars[];

   /** Creates an empty string. */
   public String4D()
   {
      chars = new char[0];
   }

   /** Creates a copy of the given string. */
   public String4D(String4D s)
   {
      chars = s.chars;
   }

   /** Creates a string from the given character array. */
   public String4D(char c[])
   {
      int count = c.length;
      this.chars = new char[count];
      // prevent null access error if len = 0 or ac is null. (added by dgecawich 7/9/01)
      if (count > 0)
         copyChars(c, 0, this.chars, 0, count);
   }

  /**
    * Allocates a new String that contains characters from a subarray of the character array argument. The offset
    * argument is the index of the first character of the subarray and the count argument specifies the length of the
    * subarray. The contents of the subarray are copied; subsequent modification of the character array does not affect
    * the newly created string.
    *
    * @param value  array that is the source of characters.
    * @param offset the initial offset.
    * @param count  the length.
    * @throws IndexOutOfBoundsException If the offset and count arguments index characters outside the bounds of the value array.
    */
   public String4D(char value[], int offset, int count)
   {
      if (offset < 0 || count < 0 || offset + count > value.length) // flsobral@tc100: check bounds and throw exception.
         throw new IndexOutOfBoundsException();

      this.chars = new char[count];
      // prevent null access error if len = 0 or ac is null. (added by dgecawich 7/9/01)
      if (count > 0) // flsobral@tc100: removed "value != null", the statement "value.length" already throws NEP if c is null.
         copyChars(value, offset, this.chars, 0, count);
   }

   /** Creates a string from the given byte array. The bytes are converted to char using the CharacterConverter 
    * associated in the charConverter member of totalcross.sys.Convert.
    * @see totalcross.sys.Convert#setDefaultConverter(String) 
    * @see totalcross.sys.CharacterConverter
    * @see totalcross.sys.UTF8CharacterConverter 
    */
   public String4D(byte []value, int offset, int count)
   {
      try
      {
         chars = Convert.charConverter.bytes2chars(value,offset,count);
      }
      catch (ArrayIndexOutOfBoundsException aioobe) // guich@tc123_33
      {
         throw new StringIndexOutOfBoundsException("value: "+value.length+" bytes length, offset: "+offset+", count: "+count);
      }
   }

   /** Creates a string from the given byte array. The bytes are converted to char using the CharacterConverter 
    * associated in the charConverter member of totalcross.sys.Convert. 
    * @see totalcross.sys.Convert#setDefaultConverter(String) 
    * @see totalcross.sys.CharacterConverter
    * @see totalcross.sys.UTF8CharacterConverter 
    */
   public String4D(byte []value)
   {
      try
      {
         chars = Convert.charConverter.bytes2chars(value,0,value.length);
      }
      catch (ArrayIndexOutOfBoundsException aioobe) // guich@tc123_33
      {
         throw new StringIndexOutOfBoundsException(aioobe.getMessage());
      }
   }

  /**
   * Private constructor which shares value array for speed. Used by concat().
   */
   private String4D(char value[], boolean dummy) // dummy is just to distinguish from the original constructor
   {
      this.chars = value;
   }

   /** Returns the length of the string in characters. */
   public int length()
   {
      return chars.length;
   }

   /** Returns the character at the given position. */
   public char charAt(int i)
   {
      return chars[i];
   }

   /** Concatenates the given string to this string and returns the result. */
   public String4D concat(String4D s)
   {
      // changed by dgecawich on 5/14/01 because a = a + "something" or a += "something" caused a stack overflow
      int otherLen = s != null ? s.chars.length : 0;
      if (otherLen == 0)
         return this;
      int curLength = chars.length;
      char buf[] = new char[curLength + otherLen];
      getChars(0, curLength, buf, 0);
      s.getChars(0, otherLen, buf, curLength);
      return new String4D(buf, true);
   }

   /**
   * Returns this string as a character array. The array returned is allocated 
   * by this method and is a copy of the string's internal character array.
   */
   public char[] toCharArray()
   {
      char chars[] = new char[this.chars.length];
      copyChars(this.chars, 0, chars, 0, chars.length);
      return chars;
   }

   /** Returns this string. */
   public String4D toString4D()
   {
      return this;
   }

   /**
   * Returns the string representation of the given object.
   */
   public static String valueOf(Object obj)
   {
      // this method is called for EVERY string literal in the applicaiton
      return obj != null ? obj.toString() : "null";
   }

   /**
   * Returns a substring of the string. The start value is included but
   * the end value is not. That is, if you call:
   * <pre>
   * string.substring(4, 6);
   * </pre>
   * a string created from characters 4 and 5 will be returned.
   * @param start the first character of the substring
   * @param end the character after the last character of the substring
   */
   public String4D substring(int start, int end)
   {
      return new String4D(chars, start, end - start);
   }

   /**
   * Returns a substring starting from <i>start</i> to the end of the string.
   * @param start the first character of the substring
   */
   public String4D substring(int start)
   {
      return new String4D(chars, start, chars.length - start);
   }

    /**
     * Tests if this string starts with the specified prefix.
     *
     * @param   prefix    the prefix.
     * @param   from   where to begin looking in the string.
     * @return  <code>true</code> if the character sequence represented by the
     *          argument is a prefix of the substring of this object starting
     *          at index <code>toffset</code>; <code>false</code> otherwise.
     */
   native public boolean startsWith(String4D prefix, int from);

    /**
     * Tests if this string starts with the specified prefix.
     *
     * @param   prefix   the prefix.
     * @return  <code>true</code> if the character sequence represented by the
     *          argument is a prefix of the character sequence represented by
     *          this string; <code>false</code> otherwise.
     */
   native public boolean startsWith(String4D prefix);

    /**
     * Tests if this string ends with the specified suffix.
     *
     * @param   suffix   the suffix.
     * @return  <code>true</code> if the character sequence represented by the
     *          argument is a suffix of the character sequence represented by
     *          this object; <code>false</code> otherwise.
     */
   native public boolean endsWith(String4D suffix);

   /**
   * Returns true if the given string is equal to this string using caseless comparison and false
   * otherwise. 
   */
   native public boolean equalsIgnoreCase(String4D s);

   /** Returns a new String with the given oldChar replaced by the newChar */
   native public String4D replace(char oldChar, char newChar);

   /** Returns the last index of the specified char in this string, or -1 if not found */
   native public int lastIndexOf(int c, int startIndex);

   /** Returns the last index of the specified char in this string starting from length-1, or -1 if not found */
   native public int lastIndexOf(int c);

   /** Returns the last index of the specified string in this string starting from length-1, or -1 if not found.
    * <p>CAUTION: this method does not exist in BlackBerry, so if you use it, your program will fail in Blackberry. */
   native public int lastIndexOf(String4D s);

   /** Returns the last index of the specified string in this string starting from the given starting index, or -1 if not found.
   * <p>CAUTION: this method does not exist in BlackBerry, so if you use it, your program will fail in Blackberry. */
   native public int lastIndexOf(String4D s, int startIndex);

   /** Removes characters less than or equal to ' ' (space) from the beginning and end of this String */
   native public String4D trim();

   /**
   * Copies characters from this String into the specified character array.
   * The characters of the specified substring (determined by
   * srcBegin and srcEnd) are copied into the character array,
   * starting at the array's dstBegin location.
   * @param srcBegin index of the first character in the string
   * @param srcEnd end of the characters that are copied
   * @param dst the destination array
   * @param dstBegin the start offset in the destination array
   * @author David Gecawich
   * @since SuperWaba 2.0
   */
   public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin)
   {
      copyChars(chars, srcBegin, dst, dstBegin, srcEnd - srcBegin);
   }

   /** Return this String as bytes. The chars are converted to byte using the CharacterConverter associated 
    * in the charConverter member of totalcross.sys.Convert.
    * @see totalcross.sys.Convert#setDefaultConverter(String)
    * @see totalcross.sys.CharacterConverter
    * @see totalcross.sys.UTF8CharacterConverter 
    * @since SuperWaba 2.0 beta 4 
    */
   public byte []getBytes()
   {
      return Convert.charConverter.chars2bytes(chars,0,chars.length);
   }

   /** Returns a new instance of this string converted to upper case */
   native public String4D toUpperCase(); // guich@200b4_43

   /** Returns a new instance of this string converted to lower case */
   native public String4D toLowerCase(); // guich@200b4_43 - guich@421_74

   /** Returns the index of the specified string in this string, or -1 if not found or the index is invalid */
   native public int indexOf(String4D c);

   /** Converts the given long value to a String. */
   native public static String valueOf(long l);

   /** Converts the given boolean to a String (in lowercase). */
   public static String valueOf(boolean b)
   {
      return b?"true":"false";
   }

   // guich@320_4: made all those methods native to improve performance

   /** Converts the given double to a String. */
   native public static String4D valueOf(double d);

   /** Converts the given char to a String. */
   native public static String4D valueOf(char c);

   /** Converts the given int to a String. */
   native public static String4D valueOf(int i);

   /** Returns the index of the specified char in this string starting from 0, or -1 if not found */
   native public int indexOf(int c); // guich@340_24

   /** Returns the index of the specified char in this string, or -1 if not found */
   native public int indexOf(int c, int startIndex); // guich@340_24

  /**
   * Returns true if the given string is equal to this string and false
   * otherwise. If the object passed is not a string, false is returned.
   */
   native public boolean equals(Object obj);

   /** Compares this string with another lexicographically.
   * @return 0 if the strings match, a value < 0 if this string
   * is lexicographically less than the string argument; and > 0 if visa-versa. */
   native public int compareTo(String4D s);

   /** Copies the specified srcArray to dstArray */
   native static boolean copyChars(char []srcArray, int srcStart, char[] dstArray, int dstStart, int length);

   /** Returns the index of the specified string in this string starting from the given index, or -1 if not found. */
   native public int indexOf(String4D c, int startIndex);

   /** Returns the hashcode for this string */
   native public int hashCode();
   
   public String[] split(String sep)
   {
      return Convert.tokenizeString(toString(), sep);
   }
}