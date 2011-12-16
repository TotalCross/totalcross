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

final public class GZip4B
{
   public static int deflate(Stream in, Stream out) throws IOException
   {
      if (in == null)
         throw new NullPointerException("Argument 'in' cannot have a null value");
      if (out == null)
         throw new NullPointerException("Argument 'out' cannot have a null value");

      GZIPOutputStream4B gzstream = new GZIPOutputStream4B(out);

      int bytesRead;
      byte[] gbuf = new byte[2048];
      while ((bytesRead = in.readBytes(gbuf, 0, gbuf.length)) > 0)
         gzstream.write(gbuf, 0, bytesRead);
      gzstream.close();

      return gzstream.def.getTotalOut();
   }

   public static int inflate(Stream in, Stream out) throws IOException, ZipException
   {
      return inflate(in, out, -1);
   }

   public static int inflate(Stream in, Stream out, int sizeIn) throws IOException, ZipException
   {
      if (in == null)
         throw new NullPointerException("Argument 'in' cannot have a null value");
      if (out == null)
         throw new NullPointerException("Argument 'out' cannot have a null value");
      if (sizeIn < -1)
         throw new IllegalArgumentException("Argument 'sizeIn' cannot have a value lower than -1.");
      if (sizeIn == 0)
         return 0;

      GZIPInputStream4B gzstream = new GZIPInputStream4B(in);

      int bytesRead;
      byte[] gbuf = new byte[2048];
      while ((bytesRead = gzstream.read(gbuf, 0, gbuf.length)) > 0)
         out.writeBytes(gbuf, 0, bytesRead);
      gzstream.close();

      return gzstream.inf.getTotalOut();
   }

   private GZip4B()
   {
   } // cannot instantiate
}
