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



package totalcross.util.zip;

import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;

/**
 * This class implements a stream filter for reading and writing files in the ZIP file format. Currently supports only
 * compressed entries.<br>
 * The ZipStream class does not work on BlackBerry devices.<br>
 * <br>
 * See the sammple TotalCrossSDK/src/tc/samples/util/zip/zip.
 * 
 * @since TotalCross 1.20
 */

public class ZipStream4B extends CompressedStream4B
{
   int defaultMethod = DEFLATED;

   /** Compression method for uncompressed (STORED) entries. */
   public final static int STORED = 0;
   /** Compression method for compressed (DEFLATED) entries. */
   public final static int DEFLATED = 8;

   /**
    * Creates a new ZIP stream that may be used to read compressed data from the given stream, or to write compressed
    * data to the given stream.<br>
    * 
    * @param stream
    *           input stream.
    * @param mode
    *           its value must be either DEFLATE or INFLATE.
    * 
    * @since TotalCross 1.20
    */
   public ZipStream4B(RandomAccessStream stream, int mode)
   {
      super(stream, mode);
   }

   protected Object createDeflate(Stream stream)
   {
      return new ZipOutputStream4B(stream);
   }

   protected Object createInflate(Stream stream)
   {
      return new ZipInputStream4B(stream);
   }

   /**
    * Returns 0 after EOF has reached for the current entry data, otherwise always return 1.<br>
    * Programs should not count on this method to return the actual number of bytes that could be read without blocking.
    * 
    * @return 1 before EOF and 0 after EOF has reached for current entry.
    * @throws IOException
    *            if an I/O error occurs.
    * 
    * @since TotalCross 1.20
    */
   public int available() throws IOException
   {
      if (mode != INFLATE)
         throw new IOException("This operation can only be performed in INFLATE mode.");

      return ((ZipInputStream4B) this.compressedStream).available();
   }

   /**
    * Reads the next ZIP file entry and positions the stream at the beginning of the entry data.
    * 
    * @return the next ZIP file entry, or null if there are no more entries
    * @throws IOException
    *            if an I/O error has occurred
    * 
    * @since TotalCross 1.20
    */
   public ZipEntry getNextEntry() throws IOException
   {
      if (mode != INFLATE)
         throw new IOException("This operation can only be performed in INFLATE mode.");

      return ((ZipInputStream4B) this.compressedStream).getNextEntry();
   }

   /**
    * Begins writing a new ZIP file entry and positions the stream to the start of the entry data. Closes the current
    * entry if still active.
    * 
    * @param entry
    *           the ZIP entry to be written
    * @throws IOException
    *            if an I/O error has occurred
    * 
    * @since TotalCross 1.20
    */
   public void putNextEntry(ZipEntry entry) throws IOException, ZipException
   {
      if (mode != DEFLATE)
         throw new IOException("This operation can only be performed in DEFLATE mode.");

      ZipOutputStream4B zos = (ZipOutputStream4B) this.compressedStream;
      zos.putNextEntry(entry);
   }

   /**
    * Closes the current ZIP entry and positions the stream for reading (INFLATE) or writing (DEFLATE) the next entry.
    * 
    * @throws IOException
    *            if an I/O error has occurred
    * 
    * @since TotalCross 1.20
    */
   public void closeEntry() throws IOException, ZipException
   {
      if (mode == DEFLATE)
      {
         ZipOutputStream4B zos = (ZipOutputStream4B) this.compressedStream;
         zos.closeEntry();
      }
      else
      {
         ZipInputStream4B zis = (ZipInputStream4B) this.compressedStream;
         zis.closeEntry();
      }
   }

   public void close() throws IOException
   {
      super.close();
   }
}
