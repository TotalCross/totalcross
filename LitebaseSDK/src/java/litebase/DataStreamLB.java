/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package litebase;

import totalcross.io.*;

/**
 * Is the same of <code>DataStreamLE</code> except for being able to use cryptography.
 */
class DataStreamLB extends DataStreamLE
{
   /**
    * Indicates that is to use cryptography.
    */
   boolean useCrypto;
   
   /**
    * Constructs a new <code>DataStreamLB</code> which sits upon the given stream using little
    * endian notation for multibyte values.
    *
    * @param stream the base stream.
    * @param newUseCrypto Indicates that is to use cryptography.
    */
   public DataStreamLB(Stream stream, boolean newUseCrypto)
   {
      super(stream);
      useCrypto = newUseCrypto;
   }
   
   /**
    * This method reads an exact amount of bytes from the underlying stream. 
    * 
    * @param buffer The buffer to be read.
    * @param start The first position of the stream to be read.
    * @param count The number of bytes to be read.
    * @throws IOException If the stream reaches its end before all bytes are read.
    */
   protected void readBytesInternal(byte[] buffer, int start, int count) throws IOException
   {
      super.readBytesInternal(buffer, start, count);
      
      // Cryptography data if asked.
      if (useCrypto)
         while (start < count)
            buffer[start++] ^= 0xAA; 
   }
   
   /**
    * This method writes an exact amount of bytes into the underlying stream. 
    * 
    * @param buffer The buffer whose data is to be written.
    * @param start The first position of the stream to be written.
    * @param count The number of bytes to be written.
    * @throws IOException If an error occurs.
    */
   protected int writeBytesInternal(byte buf[], int start, int count) throws IOException
   {
      // Encrypts data if asked.
      if (useCrypto)
      {
         int i = -1;
         while (++i < count)
            buf[i + start] ^= 0xAA; 
      }
      
      int ret = super.writeBytesInternal(buf, start, count);
      
      // Decrypts data if necessary. 
      if (useCrypto)
      {
         int i = -1;
         while (++i < count)
            buf[i + start] ^= 0xAA; 
      }
      
      return ret;
   }
}
