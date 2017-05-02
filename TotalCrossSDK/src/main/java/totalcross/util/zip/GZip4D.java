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

final public class GZip4D
{
   private static final int GZIP_COMPRESSION = 15; //ZLib.DEFAULT_COMPRESSION + 16;

   public static int deflate(Stream in, Stream out) throws IOException
   {
      //flsobral@tc114_82: use the new constant to use GZip default compression.
      //flsobral@tc123b_68: use true for noWrap just to be safe.
      return ZLib.deflate(in, out, GZIP_COMPRESSION, ZLib.DEFAULT_STRATEGY, true); 
   }

   public static int inflate(Stream in, Stream out) throws IOException, ZipException
   {
      return ZLib.inflate(in, out, -1);
   }

   public static int inflate(Stream in, Stream out, int sizeIn) throws IOException, ZipException
   {
      return ZLib.inflate(in, out, sizeIn);
   }

   private GZip4D()
   {
   } // cannot instantiate
}
