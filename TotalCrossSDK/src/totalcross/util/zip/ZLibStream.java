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

import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import totalcross.Launcher;
import totalcross.io.Stream;

/**
 * This class implements a stream filter for compressing or uncompressing data stored in the ZLib compression format.<br>
 * 
 * @see totalcross.util.zip.ZLib
 * 
 * @since TotalCross 1.10
 */

public class ZLibStream extends CompressedStream
{
   /**
    * Creates a ZLibStream object that may be used to read compressed data from the given stream, or to write compressed
    * data to the given stream.<br>
    * 
    * @param stream
    *           input stream.
    * @param mode
    *           its value must be either DEFLATE or INFLATE.
    * 
    * @since TotalCross 1.10
    */
   public ZLibStream(Stream stream, int mode)
   {
      super(stream, mode);
   }

   protected Object createDeflate(Stream stream)
   {
      Launcher.S2OS os = new Launcher.S2OS(stream, false);
      return new DeflaterOutputStream(os);
   }

   protected Object createInflate(Stream stream)
   {
      Launcher.S2IS is = new Launcher.S2IS(stream, -1, false);
      return new InflaterInputStream(is);
   }
}
