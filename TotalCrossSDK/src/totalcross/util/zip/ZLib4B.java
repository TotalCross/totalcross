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

// $Id: ZLib4B.java,v 1.16 2011-01-04 13:19:09 guich Exp $

package totalcross.util.zip;

import net.rim.device.api.compress.ZLibInputStream;
import totalcross.Launcher4B;
import totalcross.io.IOException;
import totalcross.io.Stream;

final public class ZLib4B
{
   public static final int NO_COMPRESSION = 0;
   public static final int BEST_SPEED = 1;
   public static final int BEST_COMPRESSION = 9;
   public static final int DEFAULT_COMPRESSION = -1;
   public static final int DEFAULT_STRATEGY = 0;
   public static final int DEFLATED = 8;
   public static final int FILTERED = 1;
   public static final int HUFFMAN_ONLY = 2;

   public static int deflate(Stream in, Stream out) throws IOException
   {
      return deflate(in, out, DEFAULT_COMPRESSION, DEFAULT_STRATEGY, false);
   }

   public static int deflate(Stream in, Stream out, int compressionLevel) throws IOException
   {
      return deflate(in, out, compressionLevel, DEFAULT_STRATEGY, false);
   }

   public static int deflate(int compressionLevel, Stream in, Stream out) throws IOException
   {
      return deflate(in, out, compressionLevel, DEFAULT_STRATEGY, false);
   }

   public static int deflate(Stream in, Stream out, int compressionLevel, int strategy, boolean noWrap) throws IOException
   {
      if (in == null)
         throw new NullPointerException("Argument 'in' cannot have a null value");
      if (out == null)
         throw new NullPointerException("Argument 'out' cannot have a null value");
      if (compressionLevel < -1 || compressionLevel > 9)
         throw new IllegalArgumentException("Argument 'compressionLevel' must be between -1 and 9.");

      Deflater4B def = new Deflater4B(compressionLevel, noWrap);
      def.setStrategy(strategy);
      DeflaterOutputStream4B zlos = new DeflaterOutputStream4B(out, def);
      byte[] buf = new byte[2048];

      int r, len = buf.length;
      while ((r = in.readBytes(buf, 0, len)) > 0)
         zlos.write(buf, 0, r);
      zlos.close();

      return def.getTotalOut();
   }

   public static int inflate(Stream in, Stream out) throws IOException, ZipException
   {
      return inflate(in, out, -1, false);
   }

   public static int inflate(Stream in, Stream out, int sizeIn) throws IOException, ZipException
   {
      return inflate(in, out, sizeIn, false);
   }

   public static int inflate(Stream in, Stream out, int sizeIn, boolean noWrap) throws IOException, ZipException
   {
      if (in == null)
         throw new NullPointerException("Argument 'in' cannot have a null value");
      if (out == null)
         throw new NullPointerException("Argument 'out' cannot have a null value");
      if (sizeIn < -1)
         throw new IllegalArgumentException("Argument 'sizeIn' cannot have a value lower than -1.");
      if (sizeIn == 0)
         return 0;

      if (noWrap) //flsobral@tc123a_63: use bb native ZLib when noWrap is false. using the new implementation makes the start up time 5x.
      {
         Inflater4B inf = new Inflater4B(true);
         byte[] bin = new byte[Math.min(sizeIn <= 0 ? 1024 : sizeIn, 1024)];
         byte[] bout = new byte[bin.length * 10];
         int r = 0, w, rt = 0, wt = 0;
         int s = sizeIn;

         while (true)
         {
            int tor = sizeIn == -1 ? bin.length : Math.min(bin.length, s);
            if (tor > 0)
               r = in.readBytes(bin, 0, tor); // if tor == 0 and the stream does not quietly accept requests of size 0 (such as File stream) this call will throw exception
            if (r > 0)
            {
               inf.setInput(bin, 0, r);
               while (true)
               {
                  w = inf.inflate(bout);
                  if (w <= 0)
                     break;
                  out.writeBytes(bout, 0, w);
                  wt += w;
               }
               rt += r;
               s -= r;
               r = 0; // reset r
               if (sizeIn > 0)
                  sizeIn -= r;
            }
            else
               break;
         }
         if (rt > 0 && (wt == 0 || (sizeIn > 0 && s > 0)))
            throw new ZipException("Inflate error: " + "Read " + rt + " but could not decompress it.");
         return wt;
      }
      else
      {
         try
         {
            Launcher4B.S2IS is = new Launcher4B.S2IS(in, sizeIn, false);
            ZLibInputStream zlis = new ZLibInputStream(is, false);
            byte[] buf = new byte[2048];

            int r, total = 0;
            while ((r = zlis.read(buf)) > 0)
            {
               out.writeBytes(buf, 0, r);
               total += r;
            }
            zlis.close();

            return total;
         }
         catch (java.io.IOException e)
         {
            throw new ZipException("Inflate error: " + e.getMessage());
         }
      }
   }

   private ZLib4B()
   {
   } // cannot instantiate
}
