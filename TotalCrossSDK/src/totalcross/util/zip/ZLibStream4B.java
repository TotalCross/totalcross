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

import totalcross.io.Stream;

public class ZLibStream4B extends CompressedStream
{
   public ZLibStream4B(Stream stream, int mode)
   {
      super(stream, mode);
   }

   protected Object createDeflate(Stream stream)
   {
      return new DeflaterOutputStream4B(stream, new Deflater4B(-1, false));
   }

   protected Object createInflate(Stream stream)
   {
      return new InflaterInputStream4B(stream, new Inflater4B(false));
   }
}
