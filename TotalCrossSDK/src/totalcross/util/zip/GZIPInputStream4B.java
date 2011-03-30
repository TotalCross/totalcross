/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

// $Id: GZIPInputStream4B.java,v 1.2 2011-01-04 13:19:09 guich Exp $

/* GZIPInputStream.java - Input filter for reading gzip file
   Copyright (C) 1999, 2000, 2001, 2002, 2004 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package totalcross.util.zip;

import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.sys.Vm;

/**
 * This filter stream is used to decompress a "GZIP" format stream. The "GZIP" format is described in RFC 1952.
 * 
 * Changes for TotalCross:<br>
 * Replaced InputStream by Stream, and updated exception handling accordingly.<br>
 * System.arraycopy replaced by Vm.arrayCopy.<br>
 * Replaced EOFException by IOException, because this exception is not supported by TotalCross.<br>
 * Replaced Integer.toHexString by Convert.toString(value, 16).<br>
 * 
 * @author John Leuner
 * @author Tom Tromey
 * @since JDK 1.1
 */
public class GZIPInputStream4B
      extends InflaterInputStream4B
{
   /**
    * The magic number found at the start of a GZIP stream.
    */
   public static final int GZIP_MAGIC = 0x1f8b;

   /**
    * The mask for bit 0 of the flag byte.
    */
   static final int FTEXT = 0x1;

   /**
    * The mask for bit 1 of the flag byte.
    */
   static final int FHCRC = 0x2;

   /**
    * The mask for bit 2 of the flag byte.
    */
   static final int FEXTRA = 0x4;

   /**
    * The mask for bit 3 of the flag byte.
    */
   static final int FNAME = 0x8;

   /**
    * The mask for bit 4 of the flag byte.
    */
   static final int FCOMMENT = 0x10;

   /**
    * The CRC-32 checksum value for uncompressed data.
    */
   protected CRC32 crc;

   /**
    * Indicates whether or not the end of the stream has been reached.
    */
   protected boolean eos;

   /**
    * Indicates whether or not the GZIP header has been read in.
    */
   private boolean readGZIPHeader;

   /**
    * Creates a GZIPInputStream with the default buffer size.
    * 
    * @param in
    *           The stream to read compressed data from (in GZIP format).
    * 
    * @throws IOException
    *            if an error occurs during an I/O operation.
    */
   public GZIPInputStream4B(Stream in)
         throws IOException
   {
      this(in, 4096);
   }

   /**
    * Creates a GZIPInputStream with the specified buffer size.
    * 
    * @param in
    *           The stream to read compressed data from (in GZIP format).
    * @param size
    *           The size of the buffer to use.
    * 
    * @throws IOException
    *            if an error occurs during an I/O operation.
    * @throws IllegalArgumentException
    *            if <code>size</code> is less than or equal to 0.
    */
   public GZIPInputStream4B(Stream in, int size)
         throws IOException
   {
      super(in, new Inflater4B(true), size);
      crc = new CRC32();
   }

   /**
    * Closes the input stream.
    * 
    * @throws IOException
    *            if an error occurs during an I/O operation.
    */
   public void close()
         throws IOException
   {
      // Nothing to do here.
      super.close();
   }

   /**
    * Reads in GZIP-compressed data and stores it in uncompressed form into an array of bytes. The method will block
    * until either enough input data becomes available or the compressed stream reaches its end.
    * 
    * @param buf
    *           the buffer into which the uncompressed data will be stored.
    * @param offset
    *           the offset indicating where in <code>buf</code> the uncompressed data should be placed.
    * @param len
    *           the number of uncompressed bytes to be read.
    */
   public int read(byte[] buf, int offset, int len) throws IOException
   {
      // We first have to slurp in the GZIP header, then we feed all the
      // rest of the data to the superclass.
      //
      // As we do that we continually update the CRC32. Once the data is
      // finished, we check the CRC32.
      //
      // This means we don't need our own buffer, as everything is done
      // in the superclass.
      if (!readGZIPHeader)
         readHeader();

      if (eos)
         return -1;

      //  System.err.println("GZIPIS.read(byte[], off, len ... " + offset + " and len " + len);

      /* We don't have to read the header,
       * so we just grab data from the superclass.
       */
      int numRead = super.read(buf, offset, len);
      if (numRead > 0)
         crc.update(buf, offset, numRead);

      if (inf.finished())
         readFooter();
      return numRead;
   }

   /**
    * Reads in the GZIP header.
    */
   private void readHeader() throws IOException
   {
      /* 1. Check the two magic bytes */
      CRC32 headCRC = new CRC32();
      int magic = read();
      if (magic < 0)
      {
         eos = true;
         return;
      }
      headCRC.update(magic);
      if (magic != (GZIP_MAGIC >> 8))
         throw new IOException("Error in GZIP header, first byte doesn't match");

      magic = read();
      if (magic != (GZIP_MAGIC & 0xff))
         throw new IOException("Error in GZIP header, second byte doesn't match");
      headCRC.update(magic);

      /* 2. Check the compression type (must be 8) */
      int CM = read();
      if (CM != 8)
         throw new IOException("Error in GZIP header, data not in deflate format");
      headCRC.update(CM);

      /* 3. Check the flags */
      int flags = read();
      if (flags < 0)
         //       throw new EOFException("Early EOF in GZIP header");
         throw new IOException("Early EOF in GZIP header");
      headCRC.update(flags);

      /*    This flag byte is divided into individual bits as follows:
       
       bit 0   FTEXT
       bit 1   FHCRC
       bit 2   FEXTRA
       bit 3   FNAME
       bit 4   FCOMMENT
       bit 5   reserved
       bit 6   reserved
       bit 7   reserved
      */

      /* 3.1 Check the reserved bits are zero */
      if ((flags & 0xd0) != 0)
         throw new IOException("Reserved flag bits in GZIP header != 0");

      /* 4.-6. Skip the modification time, extra flags, and OS type */
      for (int i = 0; i < 6; i++)
      {
         int readByte = read();
         if (readByte < 0)
            //	throw new EOFException("Early EOF in GZIP header");
            throw new IOException("Early EOF in GZIP header");
         headCRC.update(readByte);
      }

      /* 7. Read extra field */
      if ((flags & FEXTRA) != 0)
      {
         /* Skip subfield id */
         for (int i = 0; i < 2; i++)
         {
            int readByte = read();
            if (readByte < 0)
               //	  throw new EOFException("Early EOF in GZIP header");
               throw new IOException("Early EOF in GZIP header");
            headCRC.update(readByte);
         }
         if (read() < 0 || read() < 0)
            //	throw new EOFException("Early EOF in GZIP header");
            throw new IOException("Early EOF in GZIP header");

         int len1, len2, extraLen;
         len1 = read();
         len2 = read();
         if ((len1 < 0) || (len2 < 0))
            //	throw new EOFException("Early EOF in GZIP header");
            throw new IOException("Early EOF in GZIP header");
         headCRC.update(len1);
         headCRC.update(len2);

         extraLen = (len1 << 8) | len2;
         for (int i = 0; i < extraLen; i++)
         {
            int readByte = read();
            if (readByte < 0)
               //	  throw new EOFException("Early EOF in GZIP header");
               throw new IOException("Early EOF in GZIP header");
            headCRC.update(readByte);
         }
      }

      /* 8. Read file name */
      if ((flags & FNAME) != 0)
      {
         int readByte;
         while ((readByte = read()) > 0)
            headCRC.update(readByte);
         if (readByte < 0)
            //	throw new EOFException("Early EOF in GZIP file name");
            throw new IOException("Early EOF in GZIP file name");
         headCRC.update(readByte);
      }

      /* 9. Read comment */
      if ((flags & FCOMMENT) != 0)
      {
         int readByte;
         while ((readByte = read()) > 0)
            headCRC.update(readByte);

         if (readByte < 0)
            //        throw new EOFException("Early EOF in GZIP comment");
            throw new IOException("Early EOF in GZIP comment");
         headCRC.update(readByte);
      }

      /* 10. Read header CRC */
      if ((flags & FHCRC) != 0)
      {
         int tempByte;
         int crcval = read();
         if (crcval < 0)
            //        throw new EOFException("Early EOF in GZIP header");
            throw new IOException("Early EOF in GZIP header");

         tempByte = read();
         if (tempByte < 0)
            //        throw new EOFException("Early EOF in GZIP header");
            throw new IOException("Early EOF in GZIP header");

         crcval = (crcval << 8) | tempByte;
         if (crcval != ((int) headCRC.getValue() & 0xffff))
            throw new IOException("Header CRC value mismatch");
      }

      readGZIPHeader = true;
      //System.err.println("Read GZIP header");
   }

   private void readFooter() throws IOException
   {
      byte[] footer = new byte[8];
      int avail = inf.getRemaining();
      if (avail > 8)
         avail = 8;
      Vm.arrayCopy(buf, len - inf.getRemaining(), footer, 0, avail);
      int needed = 8 - avail;
      while (needed > 0)
      {
         int count = read(footer, 8 - needed, needed);
         if (count <= 0)
            //	throw new EOFException("Early EOF in GZIP footer");
            throw new IOException("Early EOF in GZIP footer");
         needed -= count; //Jewel Jan 16
      }

      int crcval = (footer[0] & 0xff) | ((footer[1] & 0xff) << 8)
            | ((footer[2] & 0xff) << 16) | (footer[3] << 24);
      if (crcval != (int) crc.getValue())
         throw new IOException("GZIP crc sum mismatch, theirs \""
               + Convert.toString(crcval, 16)
               + "\" and ours \""
               + Convert.toString((int) crc.getValue(), 16));

      int total = (footer[4] & 0xff) | ((footer[5] & 0xff) << 8)
            | ((footer[6] & 0xff) << 16) | (footer[7] << 24);
      if (total != inf.getTotalOut())
         throw new IOException("Number of bytes mismatch");

      /* FIXME" XXX Should we support multiple members.
       * Difficult, since there may be some bytes still in buf
       */
      eos = true;
   }
}
