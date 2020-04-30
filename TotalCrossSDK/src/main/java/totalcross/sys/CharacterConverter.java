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

package totalcross.sys;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

/** This class is used to correctly handle international character convertions.
 * The default character scheme converter is the 8859-1. If you want to use a different one,
 * you must extend this class, implementing the bytes2chars and chars2bytes methods, and then
 * assign the public member of totalcross.sys.Convert.charConverter to use your class instead of this default one.
 * You can also use the method Convert.setDefaultConverter to change it, passing, as parameter,
 * the prefix of your CharacterConverter class. For example, if you created a class
 * named Iso88592CharacterConverter, call <code>Convert.setDefaultConverter("Iso88592");</code>
 * <p>
 * To find out which <code>sun.io.CharacterEncoder</code> you're using, do:
 * <p><code>System.out.println(""+sun.io.ByteToCharConverter.getDefault());</code>
 * @see totalcross.sys.Convert#charConverter
 * @see totalcross.sys.Convert#setDefaultConverter(String) 
 * @see totalcross.sys.UTF8CharacterConverter 
 */

public class CharacterConverter extends AbstractCharacterConverter {
  
  protected CharacterConverter() {
    super("ISO-8859-1", new String[] {
        "819",
        "ISO8859-1",
        "l1",
        "ISO_8859-1:1987",
        "ISO_8859-1",
        "8859_1",
        "iso-ir-100",
        "latin1",
        "cp819",
        "ISO8859_1",
        "IBM819",
        "ISO_8859_1",
        "IBM-819",
        "csISOLatin1"
    });
  }
  
  /** Converts the given byte array range to a char array. */
  @Override
  @ReplacedByNativeOnDeploy
  public char[] bytes2chars(byte bytes[], int offset, int length) {
    char[] value = new char[length];
    for (int i = 0; length-- > 0;) {
      value[i++] = (char) (bytes[offset++] & 0xFF);
    }
    return value;
  }

  /** Converts the given char array range to a byte array. */
  @Override
  @ReplacedByNativeOnDeploy
  public byte[] chars2bytes(char chars[], int offset, int length) {
    byte[] bytes = new byte[length];
    int end = offset + length - 1;
    int i = 0;
    for (; offset <= end; offset++) {
      char c = chars[offset];
      if (c <= '\377') {
        bytes[i++] = (byte) c;
      } else {
        if ('\uD800' <= c && c <= '\uDBFF') // two-byte characters?
        {
          if (offset < end) {
            offset++;
          }
        }
        bytes[i++] = (byte) '?';
      }
    }
    if (i != length) // will never be greater, always smaller, if unicode chars were found
    {
      byte[] temp = new byte[i];
      Vm.arrayCopy(bytes, 0, temp, 0, i);
      bytes = temp;
    }
    return bytes;
  }
}