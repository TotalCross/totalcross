// Copyright (C) 2003-2004 Jaxo Systems - Pierre G. Richard
// Copyright (C) 2003-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

/**
 * This class is used to correctly handle UTF8 byte to UCS-2 chracter conversions.
 * <P>
 * To use this class, you can call
 * <pre>totalcross.sys.Convert.setDefaultConverter("UTF8");</pre>
 *
 * @see totalcross.sys.Convert#charConverter
 * @see totalcross.sys.Convert#setDefaultConverter(String) 
 * @see totalcross.sys.CharacterConverter
 * @see     CharacterConverter
 * @author  Pierre G. Richard
 */
public class UTF8CharacterConverter extends AbstractCharacterConverter {

  protected UTF8CharacterConverter() {
    super("UTF-8", new String[] {
        "unicode-1-1-utf-8",
        "UTF8"
    });
  }

  /**
   * Convert UTF-8 bytes to UCS-2 characters
   *
   * @param bytes byte array to convert
   * @param start first byte to convert in the byte array
   * @param length number of bytes to convert
   * @return UCS-2 character array resulting from the conversion
   */
  @Override
  @ReplacedByNativeOnDeploy
  public char[] bytes2chars(byte bytes[], int start, int length) {
    int end = start + length;
    int tgtOfs = 0;
    char[] chars = new char[length]; // upper bound

    while (start < end) {
      int c0 = bytes[start++] & 0xFF;
      if (c0 < 0x80) // if a 1 byte sequence,
      {
        chars[tgtOfs++] = (char) c0; // set the value
        continue; // success.
      }
      if (start >= end) // If no byte follows,
      {
        chars[tgtOfs++] = '?'; // set MCS
        break; // done
      }
      int c = (bytes[start++] & 0xFF) ^ 0x80; // 2nd byte
      if ((c & 0xC0) != 0) // starts new sequence?
      {
        --start; // Yes, backup
        chars[tgtOfs++] = '?'; // set MCS
        continue; // pursue
      }
      int r = (c0 << 6) | c; // Get encoded value
      if ((c0 & 0xE0) == 0xC0) // 2 bytes sequence?
      {
        chars[tgtOfs++] = (char) (r & 0x7FF); // Yes.  Cut noise
        continue; // pursue
      }
      if (start >= end) // If no byte follows,
      {
        chars[tgtOfs++] = '?'; // set MCS
        break; // done
      }
      c = (bytes[start++] & 0xFF) ^ 0x80; // 3rd byte
      if ((c & 0xC0) != 0) // starts new sequence?
      {
        --start; // Yes, backup
        chars[tgtOfs++] = '?'; // set MCS
        continue; // pursue
      }
      chars[tgtOfs++] = (char) ((r << 6) | c); // Get encoded value
    }
    if (chars.length > tgtOfs) // too much room left
    {
      char[] temp = new char[tgtOfs]; // shrink to exact size
      Vm.arrayCopy(chars, 0, temp, 0, tgtOfs);
      chars = temp;
    }
    return chars;
  }

  /**
   * Convert UCS-2 characters to UTF-8 bytes
   *
   * @param chars character array to convert
   * @param start first character to convert in the character array
   * @param length number of characters to convert
   * @return UTF-8 byte array resulting from the conversion
   */
  @Override
  @ReplacedByNativeOnDeploy
  public byte[] chars2bytes(char chars[], int start, int length) {
    int tgtOfs = 0;
    int end = start + length;
    byte[] bytes = new byte[length + length + length]; // guich@566_5: worst case is all chars > 0x800, which leads to 3 x length

    while (start < end) {
      int r = chars[start++];
      if (r < 0x80) {
        bytes[tgtOfs++] = (byte) r; // Yes: set the value
      } else if (r < 0x800) // 2 bytes sequence?
      {
        bytes[tgtOfs++] = (byte) (0xC0 | (r >> 6));
        bytes[tgtOfs++] = (byte) (0x80 | (r & 0x3F));
      } else // 3 bytes sequence.
      {
        bytes[tgtOfs++] = (byte) (0xE0 | (r >> 12));
        bytes[tgtOfs++] = (byte) (0x80 | ((r >> 6) & 0x3F));
        bytes[tgtOfs++] = (byte) (0x80 | (r & 0x3F));
      }
    }
    if (bytes.length > tgtOfs) // too much room left
    {
      byte[] temp = new byte[tgtOfs]; // shrink to exact size
      Vm.arrayCopy(bytes, 0, temp, 0, tgtOfs);
      bytes = temp;
    }
    return bytes;
  }
}
