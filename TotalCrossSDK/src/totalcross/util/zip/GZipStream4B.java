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

// $Id: GZipStream4B.java,v 1.2 2011-01-04 13:19:09 guich Exp $

package totalcross.util.zip;

import totalcross.io.IOException;
import totalcross.io.Stream;

public class GZipStream4B extends CompressedStream
{
   public GZipStream4B(Stream stream, int mode) throws IOException
   {
      if (stream == null)
         throw new NullPointerException("Argument stream cannot have a null value.");

      this.mode = mode;
      switch (mode)
      {
         case DEFLATE:
            compressedStream = new GZIPOutputStream4B(stream, 5);
         break;
         case INFLATE:
            compressedStream = new GZIPInputStream4B(stream);
         break;
         default:
            throw new IllegalArgumentException("Argument mode must be either DEFLATE or INFLATE.");
      }
   }

   protected Object createDeflate(Stream stream)
   {
      throw new Error("Not implemented for GZip");
   }

   protected Object createInflate(Stream stream)
   {
      throw new Error("Not implemented for GZip");
   }
}
