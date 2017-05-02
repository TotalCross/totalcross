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
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;

public abstract class ZipStream4D extends CompressedStream4D
{
   Object nativeZip;
   ZipEntry lastEntry;
   
   int defaultMethod = DEFLATED;
   
   public final static int STORED = 0;
   public final static int DEFLATED = 8;   

   public ZipStream4D(RandomAccessStream stream, int mode)
   {
      super(stream, mode, ZIP_COMPRESSION);
   }

   native protected Object createInflate(Stream stream);

   native protected Object createDeflate(Stream stream, int compressionType);

   native public int available() throws IOException;

   native public ZipEntry getNextEntry() throws IOException;

   native public void putNextEntry(ZipEntry entry) throws IOException;

   native public void closeEntry() throws IOException;

   native public int readBytes(byte[] buf, int start, int count) throws IOException;

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
