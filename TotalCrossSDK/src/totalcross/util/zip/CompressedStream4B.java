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

// $Id: CompressedStream4B.java,v 1.4 2011-01-04 13:19:08 guich Exp $

package totalcross.util.zip;

import totalcross.io.IOException;
import totalcross.io.Stream;

public abstract class CompressedStream4B extends Stream
{
   protected Object compressedStream;
   protected int mode;

   public static final int DEFLATE = 1;

   public static final int INFLATE = 2;

   protected CompressedStream4B()
   {
   }

   protected CompressedStream4B(Stream stream, int mode)
   {
      if (stream == null)
         throw new NullPointerException("Argument stream cannot have a null value.");

      this.mode = mode;
      switch (mode)
      {
         case DEFLATE:
            compressedStream = createDeflate(stream);
         break;
         case INFLATE:
            compressedStream = createInflate(stream);
         break;
         default:
            throw new IllegalArgumentException("Argument mode must be either DEFLATE or INFLATE.");
      }
   }

   protected abstract Object createDeflate(Stream stream);

   protected abstract Object createInflate(Stream stream);

   public int readBytes(byte[] buf, int start, int count) throws IOException, ZipException
   {
      if (mode != INFLATE)
         throw new IOException("This operation can only be performed in INFLATE mode.");

      return ((InflaterInputStream4B) compressedStream).read(buf, start, count);
   }

   public int writeBytes(byte[] buf, int start, int count) throws IOException
   {
      if (mode != DEFLATE)
         throw new IOException("This operation can only be performed in DEFLATE mode.");

      ((DeflaterOutputStream4B) compressedStream).write(buf, start, count);
      return count;
   }

   public void close() throws IOException
   {
      try
      {
         switch (mode)
         {
            case DEFLATE:
               ((DeflaterOutputStream4B) compressedStream).close();
            break;
            case INFLATE:
               ((InflaterInputStream4B) compressedStream).close();
            break;
            default:
               throw new IOException("Invalid object.");
         }
      }
      finally
      {
         mode = 0;
      }
   }

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
