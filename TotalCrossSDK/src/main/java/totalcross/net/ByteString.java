// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net; // this should better go into a kinda util pkg.

import totalcross.sys.Vm;

/**
 * String of bytes. Used by the Network classes to avoid converting the byte array
 * received from a Socket into Strings.
 */
public class ByteString {
  protected byte[] base;
  protected int pos;
  protected int len;

  /**
   * Constructor
   * <P>
   * <B>Warning:</B>
   * This constructor is not equivalent to a String constructor.&nbsp;
   * The caller must ensure the byte array will never be changed after
   * this constructor is called.
   *
   * @param start    where this ByteString starts in the byte array
   * @param len      length of this ByteString
   * @param base     byte array that holds this ByteString
   * @see ByteString#ByteString(byte[] base, int pos, int len) if
   * you want a safe constructor.
   */
  ByteString(int pos, int len, byte[] base) {
    this.base = base;
    this.pos = pos;
    this.len = len;
  }

  /**
   * Safe constructor for a ByteString (but slower and also uses more memory).
   *
   * @param base     byte array that holds this ByteString
   * @param start    where this ByteString starts in the byte array
   * @param len      length of this ByteString
   */
  ByteString(byte[] base, int pos, int len) {
    this.base = new byte[len];
    Vm.arrayCopy(base, pos, this.base, 0, len);
    this.pos = 0;
    this.len = len;
  }

  /**
   * Copy - if the source is null, this returns null.
   * Otherwise, the ByteString is copied.
   *
   * @param source   ByteString to be copied
   */
  public static ByteString copy(ByteString source) {
    if (source == null) {
      return null;
    }
    return new ByteString(source.pos, source.len, source.base);
  }

  /**
   * Creates a ByteString substring.
   *
   * @param start the first character of the substring
   * @param end the character after the last character of the substring
   * @return the ByteString substring
   */
  public ByteString substring(int start, int end) {
    return new ByteString(pos + start, end - start, base);
  }

  /**
   * Check if this ByteString equals the value of the passed arguments
   *
   * @param b byte array that holds the value to check against
   * @param pos first byte in the byte array
   * @param len length of the value to check against
   * @return true if this ByteString matches, false otherwise
   */
  public final boolean equalsIgnoreCase(byte[] b, int pos, int len) {
    if (this.len == len) {
      int thisPos = this.pos;
      for (int i = 0;; ++i, ++thisPos, ++pos) {
        if (i == len) {
          return true;
        }
        if (base[thisPos] != b[pos]) {
          byte c1 = base[thisPos];
          byte c2 = b[pos];
          if ('a' <= c1) {
            c1 -= ('a' - 'A'); // fast toUpper
          }
          if ('a' <= c2) {
            c2 -= ('a' - 'A'); // fast toUpper
          }
          if (c1 != c2) {
            break;
          }
        }
      }
    }
    return false;
  }

  /**
   * Check if this ByteString is equal to the one passed in the argument.
   *
   * @param c ByteString to check against
   * @return true if this ByteString is equal
   *         to the <code>c</code> ByteString, false otherwise.
   */
  public final boolean equalsIgnoreCase(ByteString c) {
    return equalsIgnoreCase(c.base, c.pos, c.len);
  }

  /**
   * Check if this ByteString equals the byte array <code>b</code>,
   * for the length of the byte array, starting at <code>at</code>
   * within this ByteString.
   * <P>
   * This extends, some how, startsWidth and endsWidth, avoiding substring.
   *
   * @param b byte array that holds the value to check against
   * @param at first byte in this ByteString
   * @return true if this ByteString matches, false otherwise
   */
  public final boolean equalsAtIgnoreCase(byte[] b, int at) {
    if ((at + b.length) > len) {
      return false;
    }
    int len = this.len; // save!
    this.pos += at;
    this.len = b.length;
    boolean isEqual = equalsIgnoreCase(b, 0, this.len);
    this.pos -= at;
    this.len = len;
    return isEqual;
  }

  /**
   * Return a String representation of this ByteString
   *
   * @return a String representation of this ByteString
   */
  @Override
  public final String toString() {
    return new String(base, pos, len);
  }

  /**
   * Find the first byte <code>b</code> in this ByteString.
   *
   * @param b byte to find
   * @param start where to start from
   * @return the offset of the byte, if found; -1 if not found
   */
  public final int indexOf(byte b, int start) {
    for (int i = start; i < len; ++i) {
      if (base[i + pos] == b) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Find the last byte <code>b</code> in this ByteString.
   *
   * @param b byte to find
   * @return the offset of the byte, if found; -1 if not found
   */
  public final int lastIndexOf(byte b) {
    int i;
    for (i = len + pos - 1; (i >= pos) && (base[i] != b); --i) {
      ;
    }
    return i - pos;
  }

  /**
   * Find the first byte array <code>b</code> in this ByteString.
   *
   * @param b byte array to find
   * @param start where to start from
   * @return the offset of the byte array, if found; -1 if not found
   */
  public final int indexOf(byte[] b, int start) {
    while (true) {
      start = indexOf(b[0], start);
      if (start == -1) {
        return -1;
      }
      for (int i = start + 1, k = 1;; ++i, ++k) {
        if (k == b.length) {
          return start;
        }
        if ((i == len) || (base[pos + i] != b[k])) {
          break;
        }
      }
      ++start;
    }
  }

  /**
   * Convert the literal representation of an int to its int value
   *
   * @param b        byte array that holds the literal representation
   * @param start    where it starts in the byte array
   * @param end      where it ends
   * @return the integer value, or -1 if wrong representation.
   */
  public static int convertToInt(byte[] b, int start, int end) {
    if (end > start) {
      int res = 0;
      while (true) {
        byte ch = b[start];
        if ((ch > (byte) '9') || (ch < (byte) '0')) {
          return -1;
        }
        res += (ch & 0xF);
        if (++start == end) {
          return res;
        }
        res *= 10;
      }
    }
    return 0;
  }

  /**
   * Convert the literal representation of an int to its int value
   *
   * @param start    where the int represenation starts in this ByteString
   * @return the integer value, or -1 if wrong representation.
   */
  public int convertToInt(int start) {
    return convertToInt(base, start + pos, pos + len);
  }
}
