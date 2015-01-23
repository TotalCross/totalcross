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

package totalcross.net;

import totalcross.io.*;

/**
 * Contains methods to encode and decode to base 64. The base 64 format is used to convert binaries into text so that they can be sent over a stream. 
 * It may then be converted back to binary form.<p>
 * Note that 3 bytes base 16 (hexadecimal) are encoded into 4 bytes base 64, so the final length is 33% bigger.
 * 
 * @since SuperWaba 5.1
 */
public class Base64
{
   /** 
    * Convertion table from base 10 to base 64 
    */
   public static final char[] toBase64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

   /** 
    * Convertion table from base 64 to base 10 
    */
   public static int[]toBase10 = new int[256];
   
   static
   {
      char[]v64 = toBase64;
      int []v10 = toBase10;
      v10['='] = 0;
      for (int i =v64.length-1; i >= 0; i--)
         v10[v64[i]] = i;
   }

   /** 
    * Decodes a given base 64 stream into a base 10 stream.<BR>
    * Important: error detection is NOT made, so you must be sure that no invalid characters are given!
    *
    * @param in The base 64 input stream. Its position must be set to the current size of the input data.
    * @param out The base 10 output stream. When returning, its position will be set with the output size in bytes.
    */
   public static void decode(ByteArrayStream in, ByteArrayStream out)
   {
      int from = 0;
      int to = 0;
      int tuples = in.getPos()>>2; // 4 bytes-group
      out.setSize(tuples*3,false); // final output size
      // get direct access to the buffers to improve performance
      byte []fromBytes = in.getBuffer();
      byte []toBytes = out.getBuffer();

      while (tuples-- > 0)
      {
          int tri = (toBase10[fromBytes[from++]] << 18) +
                    (toBase10[fromBytes[from++]] << 12) +
                    (toBase10[fromBytes[from++]] << 6)  +
                    (toBase10[fromBytes[from++]]);

          toBytes[to++] = (byte)((tri>>16) & 0xFF);
          toBytes[to++] = (byte)((tri>>8) & 0xFF);
          toBytes[to++] = (byte)(tri & 0xFF);
      }
      // remove padding
      if (fromBytes[from-1] == '=')
         to--;
      if (fromBytes[from-2] == '=')
         to--;
      // set the current size
      out.reset();
      out.skipBytes(to);
   }

   /** 
    * Decodes the given string into a byte array with the exact size.
    * 
    * @param inStr The input string in base 64. 
    */
   public static byte[] decode(String inStr)
   {
      byte []bytes = inStr.getBytes();
      ByteArrayStream tempIn = new ByteArrayStream(bytes);
      try
      {
         tempIn.setPos(bytes.length);
      }
      catch (IOException e)
      {
         // should never happen.
         e.printStackTrace();
      }
      ByteArrayStream tempOut = new ByteArrayStream(bytes.length);
      decode(tempIn,tempOut);
      return tempOut.toByteArray();
   }

   /** 
    * Encodes the given byte array and returns a base 64 generated string.
    * 
    * @param bytes The base 10 byte array to be encoded into base 64.
    */
   public static String encode(byte []bytes)
   {
      ByteArrayStream tempIn = new ByteArrayStream(bytes);
      try
      {
         tempIn.setPos(bytes.length);
      }
      catch (IOException e)
      {
         // should never happen.
         e.printStackTrace();
      }
      ByteArrayStream tempOut = new ByteArrayStream(bytes.length);
      encode(tempIn,tempOut);
      return new String(tempOut.getBuffer(), 0, tempOut.getPos());
   }
   
   /**
    * Encodes the given byte array and returns a base 64 generated string.
    * 
    * @param bytes The base 10 byte array to be encoded into base 64.
    * @param start The start position in the array.
    * @param count The number of bytes to encode.
    * @return The encoded string.
    * 
    * @since TotalCross 1.13
    */
   public static String encode(byte[] bytes, int start, int count) // flsobral@tc113_40: created to be used by totalcross.mail.BinaryContentHandler. - guich@tc123_40: no longer used there
   {
      ByteArrayStream tempIn = new ByteArrayStream(count);
      ByteArrayStream tempOut = new ByteArrayStream(count);
      tempIn.writeBytes(bytes, start, count);
      encode(tempIn, tempOut);
      return new String(tempOut.toByteArray());
   }   

   /** 
    * Encodes the given byte array and returns a base 64 generated byte array.
    * 
    * @param bytes The base 10 byte array to be encoded into base 64.
    * @return The base 64 byte array.
    */
   public static byte[] encode(byte []bytes, int count) // guich@tc123_40
   {
      ByteArrayStream tempIn = new ByteArrayStream(bytes);
      ByteArrayStream tempOut = new ByteArrayStream(count);
      try
      {
         tempIn.setPos(count);
      }
      catch (IOException e)
      {
         // should never happen.
         e.printStackTrace();
      }
      encode(tempIn, tempOut);
      return tempOut.toByteArray();
   }
   
   /** 
    * Encodes a given base 10 stream into a base 64 stream.
    *
    * @param in The Base 10 intput stream. Its position must be set to the current size of the input data.
    * @param out The Base 64 ouput stream. When returning, its position will be set with the output size in bytes.
    */
   public static void encode(ByteArrayStream in, ByteArrayStream out)
   {
      int len = in.getPos();
      int size = len*3/2;
      size = ((size+4)>>2)<<2; // guich@552_3: make sure that the computed number is multiple of 4
      out.setSize(size,false);
      int end = len - 3;
      int from = 0;
      int to = 0;
      byte []fromBytes = in.getBuffer();
      byte []toBytes = out.getBuffer();

      for (;from <= end; from+=3)
      {
          //fdie@511_6: fixed base64 encoding of signed data
          int d = ((((int)fromBytes[from]) & 0xFF) << 16)  |
                  ((((int)fromBytes[from+1]) & 0xFF) << 8) |
                  ((int)fromBytes[from + 2] & 0xFF);

          toBytes[to++] = (byte)toBase64[(d >> 18) & 0x3F];
          toBytes[to++] = (byte)toBase64[(d >> 12) & 0x3F];
          toBytes[to++] = (byte)toBase64[(d >> 6) & 0x3F];
          toBytes[to++] = (byte)toBase64[d & 0x3F];
      }

      if (from == len - 2)
      {
          int d = ((((int)fromBytes[from]) & 0xFF) << 16) |
                  ((((int)fromBytes[from+1]) & 0xFF) << 8); //fdie@511_6

          toBytes[to++] = (byte)toBase64[(d >> 18) & 0x3F];
          toBytes[to++] = (byte)toBase64[(d >> 12) & 0x3F];
          toBytes[to++] = (byte)toBase64[(d >>  6) & 0x3F];
          toBytes[to++] = (byte)'=';
      }
      else
      if (from == len - 1)
      {
          int d = (((int)fromBytes[from]) & 0xFF) << 16; //fdie@511_6

          toBytes[to++] = (byte)toBase64[(d >> 18) & 0x3F];
          toBytes[to++] = (byte)toBase64[(d >> 12) & 0x3F];
          toBytes[to++] = (byte)'=';
          toBytes[to++] = (byte)'=';
      }
      // set the current size
      out.reset();
      out.skipBytes(to);
  }
}
