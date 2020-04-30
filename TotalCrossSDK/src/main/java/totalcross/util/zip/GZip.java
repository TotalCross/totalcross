// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util.zip;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import totalcross.Launcher;
import totalcross.io.IOException;
import totalcross.io.Stream;

/**
 * This class implements stream compression using the GZip algorithm provided with the ZLib library.
 * 
 * <blockquote><i><q>gzip (GNU zip) is a compression utility designed to be a replacement for compress. Its main
 * advantages over compress are much better compression and freedom from patented algorithms. It has been adopted by the
 * GNU project and is now relatively popular on the Internet.</q></i>
 * <p align=right>
 * <a href="http://www.gzip.org/" />http://www.gzip.org/</a>
 * </p>
 * </blockquote>
 * 
 * The ZLib library home page is
 * <p align=right>
 * <a href="http://www.zlib.net/" />http://www.zlib.net/</a>
 * </p>
 * 
 * @since TotalCross 1.10
 */

final public class GZip {
  /**
   * Deflates the given stream 'in', writing the compressed data to the given stream 'out'.
   * 
   * @param in
   *           Stream to be deflated.
   * @param out
   *           Deflated stream.
   * @return Size of the deflated stream
   * @throws IOException
   * 
   * @since TotalCross 1.10
   */
  public static int deflate(Stream in, Stream out) throws IOException {
    if (in == null) {
      throw new NullPointerException("Argument 'in' cannot have a null value");
    }
    if (out == null) {
      throw new NullPointerException("Argument 'out' cannot have a null value");
    }

    Launcher.S2OS os = new Launcher.S2OS(out, false);
    try {
      GZIPOutputStream gos = new GZIPOutputStream(os);

      int bytesRead;
      byte[] gbout = new byte[1024];
      while ((bytesRead = in.readBytes(gbout, 0, gbout.length)) > 0) {
        gos.write(gbout, 0, bytesRead);
      }
      gos.close();
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }

    return os.count();
  }

  /**
   * Attempts to fully read the given stream 'in', inflate its content and write the uncompressed data to the given
   * stream 'out'.
   * 
   * @param in
   *           Deflated input stream
   * @param out
   *           Inflated output stream
   * @return Size of the inflated stream
   * @throws ZipException
   * @throws IOException
   * 
   * @since TotalCross 1.10
   */
  public static int inflate(Stream in, Stream out) throws IOException, ZipException {
    return inflate(in, out, -1);
  }

  /**
   * Attempts to read the number of bytes specified by 'sizeIn' from the the given stream 'in', inflating and writing
   * to the given stream 'out'. If 'sizeIn' is -1, it will attempt to fully read the stream.
   * 
   * @param in
   *           Deflated input stream
   * @param out
   *           Inflated output stream
   * @param sizeIn
   *           How many bytes to read, or -1 to read until <code>in</code>'s end.
   * @return Size of the inflated stream
   * @throws IOException
   * @throws ZipException
   * 
   * @since TotalCross 1.10
   */
  public static int inflate(Stream in, Stream out, int sizeIn) throws IOException, ZipException {
    if (in == null) {
      throw new NullPointerException("Argument 'in' cannot have a null value");
    }
    if (out == null) {
      throw new NullPointerException("Argument 'out' cannot have a null value");
    }
    if (sizeIn < -1) {
      throw new IllegalArgumentException("Argument 'sizeIn' cannot have a value lower than -1.");
    }
    if (sizeIn == 0) {
      return 0;
    }

    InputStream is = new Launcher.S2IS(in);
    int result = 0;
    try {
      byte[] gbout = new byte[1024];
      GZIPInputStream gis = new GZIPInputStream(is);
      int bytesRead = gis.read(gbout);
      while (bytesRead != -1 && (sizeIn != -1 ? result < sizeIn : true)) {
        result += out.writeBytes(gbout, 0, bytesRead);
        bytesRead = gis.read(gbout);
      }
      gis.close();
    } catch (java.util.zip.ZipException e) {
      throw new ZipException(e.getMessage());
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
    return result;
  }

  private GZip() {
  } // cannot instantiate
}
