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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.Collator;

import totalcross.sys.AbstractCharacterConverter;
import totalcross.sys.Convert;
import totalcross.util.ElementNotFoundException;
import totalcross.util.regex.MatchIterator;
import totalcross.util.regex.MatchResult;
import totalcross.util.regex.Matcher;
import totalcross.util.regex.Pattern;

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

public final class String4D implements Comparable<String4D>, CharSequence {
  char chars[];
  
  private static Charset lastCharset;

  private static char[] charSequence2charArryay(CharSequence cs) {
    int len = cs.length();
    char[] array = new char[len];
    cs.toString().getChars(0, len, array, 0);
    return array;
  }

  /** Creates an empty string. */
  public String4D() {
    chars = new char[0];
  }

  public String4D(CharSequence cs) {
    this(charSequence2charArryay(cs));
  }

  /** Creates a copy of the given string. */
  public String4D(String4D s) {
    chars = s.chars;
  }

  /** Creates a string from the given character array. */
  public String4D(char c[]) {
    int count = c.length;
    this.chars = new char[count];
    // prevent null access error if len = 0 or ac is null. (added by dgecawich 7/9/01)
    if (count > 0) {
      copyChars(c, 0, this.chars, 0, count);
    }
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
  public String4D(char value[], int offset, int count) {
    if (offset < 0 || count < 0 || offset + count > value.length) {
      throw new IndexOutOfBoundsException();
    }

    this.chars = new char[count];
    // prevent null access error if len = 0 or ac is null. (added by dgecawich 7/9/01)
    if (count > 0) {
      copyChars(value, offset, this.chars, 0, count);
    }
  }

  /** Creates a string from the given byte array. The bytes are converted to char using the CharacterConverter 
   * associated in the charConverter member of totalcross.sys.Convert.
   * @see totalcross.sys.Convert#setDefaultConverter(String) 
   * @see totalcross.sys.CharacterConverter
   * @see totalcross.sys.UTF8CharacterConverter 
   */
  public String4D(byte[] value, int offset, int count) {
    try {
      chars = Convert.charConverter.bytes2chars(value, offset, count);
    } catch (ArrayIndexOutOfBoundsException aioobe) // guich@tc123_33
    {
      throw new StringIndexOutOfBoundsException(
          "value: " + value.length + " bytes length, offset: " + offset + ", count: " + count);
    }
  }
  
    public String4D(byte[] value, int offset, int count, String encoding) throws UnsupportedEncodingException {
        try {
            Charset charset = getCharset(encoding);
            if (charset instanceof AbstractCharacterConverter) {
                chars = ((AbstractCharacterConverter) charset).bytes2chars(value, offset, count);
            }
            chars = Convert.charConverter.bytes2chars(value, offset, count);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            throw new IndexOutOfBoundsException(
                    "value: " + value.length + " bytes length, offset: " + offset + ", count: " + count);
        }
    }

  /** Creates a string from the given byte array. The bytes are converted to char using the CharacterConverter 
   * associated in the charConverter member of totalcross.sys.Convert. 
   * @see totalcross.sys.Convert#setDefaultConverter(String) 
   * @see totalcross.sys.CharacterConverter
   * @see totalcross.sys.UTF8CharacterConverter 
   */
  public String4D(byte[] value) {
    try {
      chars = Convert.charConverter.bytes2chars(value, 0, value.length);
    } catch (ArrayIndexOutOfBoundsException aioobe) // guich@tc123_33
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

  /**
   * Creates a new String using the character sequence represented by
   * the StringBuffer. Subsequent changes to buf do not affect the String.
   *
   * @param buffer StringBuffer to copy
   * @throws NullPointerException if buffer is null
   */
  public String4D(StringBuffer4D buffer) {
    chars = new char[buffer.count];
    String4D.copyChars(buffer.charbuf, 0, chars, 0, chars.length);
  }

  /** Returns the length of the string in characters. */
  @Override
  public int length() {
    return chars.length;
  }

  /** Returns the character at the given position. */
  @Override
  public char charAt(int i) {
    return chars[i];
  }

  /** Concatenates the given string to this string and returns the result. */
  public String4D concat(String4D s) {
    // changed by dgecawich on 5/14/01 because a = a + "something" or a += "something" caused a stack overflow
    int otherLen = s != null ? s.chars.length : 0;
    if (otherLen == 0) {
      return this;
    }
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
  public char[] toCharArray() {
    char chars[] = new char[this.chars.length];
    copyChars(this.chars, 0, chars, 0, chars.length);
    return chars;
  }

  /** Returns this string. */
  public String4D toString4D() {
    return this;
  }

  /**
   * Returns the string representation of the given object.
   */
  public static String valueOf(Object obj) {
    // this method is called for EVERY string literal in the applicaiton
    return obj != null ? obj instanceof String ? (String) obj : obj.toString() : "null";
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
  public String4D substring(int start, int end) {
    return new String4D(chars, start, end - start);
  }

  /**
   * Returns a substring starting from <i>start</i> to the end of the string.
   * @param start the first character of the substring
   */
  public String4D substring(int start) {
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
  public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
    copyChars(chars, srcBegin, dst, dstBegin, srcEnd - srcBegin);
  }

  /** Return this String as bytes. The chars are converted to byte using the CharacterConverter associated 
   * in the charConverter member of totalcross.sys.Convert.
   * @see totalcross.sys.Convert#setDefaultConverter(String)
   * @see totalcross.sys.CharacterConverter
   * @see totalcross.sys.UTF8CharacterConverter 
   * @since SuperWaba 2.0 beta 4 
   */
  public byte[] getBytes() {
    return Convert.charConverter.chars2bytes(chars, 0, chars.length);
  }

  public byte[] getBytes(Charset charset) {
    if (charset instanceof AbstractCharacterConverter) {
      AbstractCharacterConverter converter = (AbstractCharacterConverter) charset;
      return converter.chars2bytes(chars, 0, chars.length);
    }
    return getBytes();
  }

  public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
    try {
        Charset charset = Charset.forName(charsetName);
        return getBytes(charset);
    } catch (UnsupportedCharsetException e) {
        throw new UnsupportedEncodingException(e.getMessage());
    }
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
  public static String valueOf(boolean b) {
    return b ? "true" : "false";
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
  @Override
  native public boolean equals(Object obj);

  /** Compares this string with another lexicographically.
   * @return 0 if the strings match, a value < 0 if this string
   * is lexicographically less than the string argument; and > 0 if visa-versa. */
  @Override
  native public int compareTo(String4D s);

  /** Copies the specified srcArray to dstArray */
  native static boolean copyChars(char[] srcArray, int srcStart, char[] dstArray, int dstStart, int length);

  /** Returns the index of the specified string in this string starting from the given index, or -1 if not found. */
  native public int indexOf(String4D c, int startIndex);

  /** Returns the hashcode for this string */
  @Override
  native public int hashCode();

  public boolean contains(String4D part) {
    return indexOf(part) != -1;
  }

  public boolean contains(CharSequence part) {
    return contains(new String4D(part));
  }

  /**
   * Compares the given StringBuffer to this String. This is true if the
   * StringBuffer has the same content as this String at this moment.
   *
   * @param buffer the StringBuffer to compare to
   * @return true if StringBuffer has the same character sequence
   * @throws NullPointerException if the given StringBuffer is null
   * @since 1.4
   */
  public boolean contentEquals(StringBuffer4D buffer) {
    if (chars.length != buffer.count) {
      return false;
    }
    int i = chars.length;
    while (--i >= 0) {
      if (chars[i] != buffer.charbuf[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares this String and another String (case insensitive). This
   * comparison is <em>similar</em> to equalsIgnoreCase, in that it ignores
   * locale and multi-characater capitalization, and compares characters
   * after performing
   * <code>Character.toLowerCase(Character.toUpperCase(c))</code> on each
   * character of the string. 
   *
   * @param str the string to compare against
   * @return the comparison
   * @see Collator#compare(String, String)
   * @since 1.2
   */
  public int compareToIgnoreCase(String4D str) {
    return toUpperCase().compareTo(str.toUpperCase()); // TODO optimize
  }

  /**
   * Predicate which determines if this String matches another String
   * starting at a specified offset for each String and continuing
   * for a specified length. Indices out of bounds are harmless, and give
   * a false result.
   *
   * @param toffset index to start comparison at for this String
   * @param other String to compare region to this String
   * @param ooffset index to start comparison at for other
   * @param len number of characters to compare
   * @return true if regions match (case sensitive)
   * @throws NullPointerException if other is null
   */
  public boolean regionMatches(int toffset, String4D other, int ooffset, int len) {
    return regionMatches(false, toffset, other, ooffset, len);
  }

  /**
   * Predicate which determines if this String matches another String
   * starting at a specified offset for each String and continuing
   * for a specified length, optionally ignoring case. Indices out of bounds
   * are harmless, and give a false result. Case comparisons are based on
   * <code>Character.toLowerCase()</code> and
   * <code>Character.toUpperCase()</code>, not on multi-character
   * capitalization expansions.
   *
   * @param ignoreCase true if case should be ignored in comparision
   * @param toffset index to start comparison at for this String
   * @param other String to compare region to this String
   * @param ooffset index to start comparison at for other
   * @param len number of characters to compare
   * @return true if regions match, false otherwise
   * @throws NullPointerException if other is null
   */
  public boolean regionMatches(boolean ignoreCase, int toffset, String4D other, int ooffset, int len) {
    if (toffset < 0 || ooffset < 0 || toffset + len > chars.length || ooffset + len > other.chars.length) {
      return false;
    }
    while (--len >= 0) {
      char c1 = chars[toffset++];
      char c2 = other.chars[ooffset++];
      // Note that checking c1 != c2 is redundant when ignoreCase is true,
      // but it avoids method calls.
      if (c1 != c2 && (!ignoreCase || (Convert.toLowerCase(c1) != Convert.toLowerCase(c2)
          && (Convert.toUpperCase(c1) != Convert.toUpperCase(c2))))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Test if this String matches a regular expression. This is shorthand for
   * <code>{@link Pattern}.matches(regex, this)</code>.
   *
   * @param regex the pattern to match
   * @return true if the pattern matches
   * @throws NullPointerException if regex is null
   * @throws PatternSyntaxException if regex is invalid
   */
  public boolean matches(String regex) {
    return Pattern.compile(regex).matches(this.toString());
  }

  /**
   * Replaces the first substring match of the regular expression with a
   * given replacement. This is shorthand for <code>{@link Pattern}
   *   .compile(regex).matcher(this).replaceFirst(replacement)</code>.
   *
   * @param regex the pattern to match
   * @param replacement the replacement string
   * @return the modified string
   * @throws NullPointerException if regex or replacement is null
   * @throws PatternSyntaxException if regex is invalid
   * @see #replaceAll(String, String)
   * @see Pattern#compile(String)
   */
  /*public String replaceFirst(String regex, String replacement)
   {
     return Pattern.compile(regex).matcher(this.toString()).replaceFirst(replacement); // TODO JEFF
   }*/

  public String replaceFirst(String regex, String replacement) {
    Matcher m = Pattern.compile(regex).matcher();
    MatchIterator mi = m.findAll();

    if (mi.hasMore()) {
      try {
        MatchResult mr = mi.nextMatch();
        int endIndex = mr.end();
        String4D ss = this.substring(0, endIndex);

        return ss.replaceAll(regex, replacement) + this.substring(endIndex);
      } catch (ElementNotFoundException e) {
        // TODO Auto-generated catch block
        //			   e.printStackTrace();
      }
    }
    return this.toString();
    //return Pattern.compile(regex).replacer(replacement).replace(this.toString()); 
  }

  /**
   * Replaces all matching substrings of the regular expression with a
   * given replacement. This is shorthand for <code>{@link Pattern}
   *   .compile(regex).matcher(this).replaceAll(replacement)</code>.
   *
   * @param regex the pattern to match
   * @param replacement the replacement string
   * @return the modified string
   * @throws NullPointerException if regex or replacement is null
   * @throws PatternSyntaxException if regex is invalid
   * @see Pattern#compile(String)
   */
  public String replaceAll(String regex, String replacement) {
    return Pattern.compile(regex).replacer(replacement).replace(this.toString());
  }

  /**
   * Split this string around the matches of a regular expression. Each
   * element of the returned array is the largest block of characters not
   * terminated by the regular expression, in the order the matches are found.
   *
   * <p>The limit affects the length of the array. If it is positive, the
   * array will contain at most n elements (n - 1 pattern matches). If
   * negative, the array length is unlimited, but there can be trailing empty
   * entries. if 0, the array length is unlimited, and trailing empty entries
   * are discarded.
   *
   * <p>For example, splitting "boo:and:foo" yields:<br>
   * <table border=0>
   * <th><td>Regex</td> <td>Limit</td> <td>Result</td></th>
   * <tr><td>":"</td>   <td>2</td>  <td>{ "boo", "and:foo" }</td></tr>
   * <tr><td>":"</td>   <td>t</td>  <td>{ "boo", "and", "foo" }</td></tr>
   * <tr><td>":"</td>   <td>-2</td> <td>{ "boo", "and", "foo" }</td></tr>
   * <tr><td>"o"</td>   <td>5</td>  <td>{ "b", "", ":and:f", "", "" }</td></tr>
   * <tr><td>"o"</td>   <td>-2</td> <td>{ "b", "", ":and:f", "", "" }</td></tr>
   * <tr><td>"o"</td>   <td>0</td>  <td>{ "b", "", ":and:f" }</td></tr>
   * </table>
   *
   * <p>This is shorthand for
   * <code>{@link Pattern}.compile(regex).split(this, limit)</code>.
   *
   * @param regex the pattern to match
   * @param limit the limit threshold
   * @return the array of split strings
   * @throws NullPointerException if regex or replacement is null
   * @throws PatternSyntaxException if regex is invalid
   * @see Pattern#compile(String)
   */
  /* public String[] split(String regex, int limit)
   {
      return Pattern.compile(regex).split(this.toString(), limit); // TODO JEFF
   } */

  /**
   * Split this string around the matches of a regular expression. Each
   * element of the returned array is the largest block of characters not
   * terminated by the regular expression, in the order the matches are found.
   * The array length is unlimited, and trailing empty entries are discarded,
   * as though calling <code>split(regex, 0)</code>.
   *
   * @param regex the pattern to match
   * @return the array of split strings
   * @throws NullPointerException if regex or replacement is null
   * @throws PatternSyntaxException if regex is invalid
   * @see Pattern#compile(String)
   */
  public String[] split(String regex) {
    for (int i = 0, n = regex.length(); i < n; i++) {
      if ("^$.[]{}()\\|?+*".indexOf(regex.charAt(i)) >= 0) {
        try {
          return Pattern.compile(regex).tokenizer(this.toString()).split();
        } catch (totalcross.util.ElementNotFoundException e) {
          return null;
        }
      }
    }
    return Convert.tokenizeString(this.toString(), regex);
  }

  /**
   * Returns a String representation of a character array. Subsequent
   * changes to the array do not affect the String.
   *
   * @param data the character array
   * @return a String containing the same character sequence as data
   * @throws NullPointerException if data is null
   * @see #valueOf(char[], int, int)
   */
  public static String4D valueOf(char[] data) {
    return valueOf(data, 0, data.length);
  }

  /**
   * Returns a String representing the character sequence of the char array,
   * starting at the specified offset, and copying chars up to the specified
   * count. Subsequent changes to the array do not affect the String.
   *
   * @param data character array
   * @param offset position (base 0) to start copying out of data
   * @param count the number of characters from data to copy
   * @return String containing the chars from data[offset..offset+count]
   * @throws NullPointerException if data is null
   * @throws IndexOutOfBoundsException if (offset &lt; 0 || count &lt; 0
   *         || offset + count &gt; data.length)
   *         (while unspecified, this is a StringIndexOutOfBoundsException)
   */
  public static String4D valueOf(char[] data, int offset, int count) {
    return new String4D(data, offset, count);
  }

  /**
   * Returns true if, and only if, {@link #length()}
   * is <code>0</code>.
   *
   * @return true if the length of the string is zero.
   * @since 1.6
   */
  public boolean isEmpty() {
    return chars.length == 0;
  }

  /**
   * Returns a string that is this string with all instances of the sequence
   * represented by <code>target</code> replaced by the sequence in
   * <code>replacement</code>.
   * @param target the sequence to be replaced
   * @param replacement the sequence used as the replacement
   * @return the string constructed as above
   */
  public String replace(CharSequence targetcs, CharSequence replacementcs) // NOTE: the original version uses CharSequence
  {
    String target = targetcs.toString();
    String replacement = replacementcs.toString();
    int targetLength = target.length();
    int replaceLength = replacement.length();

    int startPos = this.toString().indexOf(target);
    StringBuilder result = new StringBuilder(this.toString());
    while (startPos != -1) {
      // Replace the target with the replacement
      result.replace(startPos, startPos + targetLength, replacement);

      // Search for a new occurrence of the target
      startPos = result.indexOf(target, startPos + replaceLength);
    }
    return result.toString();
  }

  @Override
  public CharSequence subSequence(int beginIndex, int endIndex) {
    return substring(beginIndex, endIndex);
  }
  
  private Charset getCharset(final String encoding)
          throws UnsupportedEncodingException {
      Charset charset = lastCharset;
      if (charset == null || !encoding.equalsIgnoreCase(charset.name())) {
          try {
              charset = Charset.forName(encoding);
          } catch (IllegalCharsetNameException e) {
              throw (UnsupportedEncodingException) (new UnsupportedEncodingException(
                      encoding).initCause(e));
          } catch (UnsupportedCharsetException e) {
              throw (UnsupportedEncodingException) (new UnsupportedEncodingException(
                      encoding).initCause(e));
          }
          lastCharset = charset;
      }
      return charset;
  }
}