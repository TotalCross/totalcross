/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
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



package totalcross.sys;

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

public class CharacterConverter
{
   /** Converts the given byte array range to a char array. */
   public char[] bytes2chars(byte bytes[], int offset, int length)
   {
      char []value = new char[length];
      for (int i = 0; length-- > 0;)
         value[i++] = (char)(bytes[offset++] & 0xFF);
      return value;
   }

   /** Converts the given char array range to a byte array. */
   public byte[] chars2bytes(char chars[], int offset, int length)
   {
      byte []bytes = new byte[length];
      int end = offset+length-1;
      int i = 0;
      for (; offset <= end; offset++)
      {
         char c = chars[offset];
         if (c <= '\377') // octal = 255 decimal
            bytes[i++] = (byte)c;
         else
         {
            if ('\uD800' <= c && c <= '\uDBFF') // two-byte characters?
            {
               if (offset < end) // we must skip one byte, but no more bytes avail?
                  offset++;
            }
            bytes[i++] = (byte)'?';
         }
      }
      if (i != length) // will never be greater, always smaller, if unicode chars were found
      {
         byte []temp = new byte[i];
         Vm.arrayCopy(bytes,0,temp,0,i);
         bytes = temp;
      }
      return bytes;
   }
   native public char[]bytes2chars4D(byte bytes[], int offset, int length);
   native public byte[] chars2bytes4D(char chars[], int offset, int length);
}