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



package totalcross.util.zip;

import totalcross.io.IOException;
import totalcross.io.Stream;

public abstract class CompressedStream4D extends Stream
{
   protected Object compressedStream;
   protected int mode;

   protected byte[] streamBuffer = new byte[1024];

   public static final int DEFLATE = 1;
   public static final int INFLATE = 2;
   
   protected static final int ZLIB_COMPRESSION = 0;
   protected static final int GZIP_COMPRESSION = 16;
   protected static final int ZIP_COMPRESSION = 32;

   protected CompressedStream4D(Stream stream, int mode, int compressionType) //flsobral@tc114_82: Subclasses of CompressedStream4D must now inform the type of compression to be used by the ZLib library.
   {
      if (stream == null)
         throw new NullPointerException("Argument stream cannot have a null value.");

      this.mode = mode;
      switch (mode)
      {
         case DEFLATE:
            compressedStream = createDeflate(stream, compressionType);
         break;
         case INFLATE:
            compressedStream = createInflate(stream);
         break;
         default:
            throw new IllegalArgumentException("Argument mode must be either DEFLATE or INFLATE.");
      }
   }

   native protected Object createInflate(Stream stream);

   native protected Object createDeflate(Stream stream, int compressionType);

   native public int readBytes(byte[] buf, int start, int count) throws IOException, ZipException;

   native public int writeBytes(byte[] buf, int start, int count) throws IOException;

   native public void close() throws IOException;

   protected void finalize()
   {
      try
      {
         if (mode != 0)
            this.close();
      }
      catch (Throwable t)
      {
      }
   }
}
