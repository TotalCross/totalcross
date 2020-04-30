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

package litebase;

import totalcross.io.*;

// juliana@253_8: now Litebase supports weak cryptography.
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
    * @param throwEOF Indicates if an <code>EOFException</code> should be thrown if an EOF is found. 
    * @return The number of bytes read: count.
    * @throws IOException If the stream reaches its end before all bytes are read.
    */
   protected int readBytesInternal(byte[] buffer, int start, int count, boolean throwEOF) throws IOException
   {
      super.readBytesInternal(buffer, start, count, throwEOF);
            
      if (useCrypto) // Decrypts data if necessary.
         while (start < count)
            buffer[start++] ^= 0xAA; 
      
      return count;
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
      if (useCrypto) // Encrypts data if asked.
      {
         int i = start;
         while (i < count)
            buf[i++] ^= 0xAA; 
      }
      
      int ret = super.writeBytesInternal(buf, start, count);
           
      if (useCrypto) // Decrypts data if necessary. 
         while (start < count)
            buf[start++] ^= 0xAA; 
      
      return ret;
   }
}
