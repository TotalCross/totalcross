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

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import totalcross.io.IOException;
import totalcross.io.Stream;

/**
 * This class implements stream compression with the ZLib library.
 * 
 * <blockquote> <i><q>zlib is designed to be a free, general-purpose, legally unencumbered - that is, not covered by any
 * patents -- lossless data-compression library for use on virtually any computer hardware and operating system. The
 * zlib data format is itself portable across platforms.</q></i>
 * <p align=right>
 * <a href="http://www.zlib.net/" />http://www.zlib.net/</a>
 * </p>
 * </blockquote>
 */
final public class ZLib {
  /** Compression level for no compression, which is 0 in a scale from 0 (no compression) to 9 (best compression) */
  public static final int NO_COMPRESSION = 0;

  /** Compression level for fastest compression, which is 1 in a scale from 0 (no compression) to 9 (best compression). */
  public static final int BEST_SPEED = 1;

  /** Compression level for best compression, which is 9 in a scale from 0 (no compression) to 9 (best compression). */
  public static final int BEST_COMPRESSION = 9;

  /** Default compression level. */
  public static final int DEFAULT_COMPRESSION = -1;

  /** Default compression strategy. */
  public static final int DEFAULT_STRATEGY = 0;

  /** Compression method for the deflate algorithm (the only one currently supported). */
  public static final int DEFLATED = 8;

  /** Compression strategy best used for data consisting mostly of small values with a somewhat random distribution. */
  public static final int FILTERED = 1;

  /** Compression strategy for Huffman coding only. */
  public static final int HUFFMAN_ONLY = 2;

  /**
   * Deflates the given stream 'in' with the specified compression level, writing the result to the given stream 'out'.
   * Compressed data will be generated in ZLIB format using the default strategy and the default compression level.
   * 
   * @param in
   *           Stream to be deflated
   * @param out
   *           Deflated stream.
   * @return Size of the deflated stream
   * @throws IOException
   * @see #DEFAULT_COMPRESSION
   */
  public static int deflate(Stream in, Stream out) throws IOException {
    return deflate(in, out, DEFAULT_COMPRESSION, DEFAULT_STRATEGY, false);
  }

  /**
   * Deflates the given stream 'in' with the specified compression level, writing the result to the given stream 'out'.
   * Compressed data will be generated in ZLIB format using the default strategy.
   * 
   * @param in
   *           Stream to be deflated
   * @param out
   *           Deflated stream.
   * @param compressionLevel
   *           Desired compression level, which must be between 0 and 9, or -1 for the default compression level
   * @return Size of the deflated stream
   * @throws IOException
   * @see #NO_COMPRESSION
   * @see #BEST_SPEED
   * @see #BEST_COMPRESSION
   */
  public static int deflate(Stream in, Stream out, int compressionLevel) throws IOException {
    return deflate(in, out, compressionLevel, DEFAULT_STRATEGY, false);
  }

  /**
   * Deflates the given stream 'in' with the specified compression level, writing the result to the given stream 'out'.
   * Compressed data will be generated in ZLIB format using the default strategy.
   *
   * @param compressionLevel
   *           Desired compression level, which must be between 0 and 9, or -1 for the default compression level
   * @param in
   *           Stream to be deflated
   * @param out
   *           Deflated stream.
   * @return Size of the deflated stream
   * @throws IOException
   * @see #NO_COMPRESSION
   * @see #BEST_SPEED
   * @see #BEST_COMPRESSION
   * @deprecated use #deflate(Stream, Stream, int) instead
   */
  @Deprecated
  public static int deflate(int compressionLevel, Stream in, Stream out) throws IOException {
    return deflate(in, out, compressionLevel, DEFAULT_STRATEGY, false);
  }

  /**
   * Deflates the given stream 'in' using the specified strategy and compression level, writing the result to the given
   * stream 'out'. If 'nowrap' is true then the ZLIB header and checksum fields will not be used in order to support
   * the compression format used in both GZIP and PKZIP.
   * 
   * @param in
   *           Stream to be deflated
   * @param out
   *           Deflated stream.
   * @param compressionLevel
   *           compression level, which must be between 0 and 9, or -1 for the default compression level
   * @param strategy
   *           the compression strategy
   * @param noWrap
   *           if true then use GZIP compatible compression
   * @return Size of the deflated stream
   * @throws IOException
   * @see #NO_COMPRESSION
   * @see #BEST_SPEED
   * @see #BEST_COMPRESSION
   */
  @ReplacedByNativeOnDeploy
  public static int deflate(Stream in, Stream out, int compressionLevel, int strategy, boolean noWrap)
      throws IOException {
    if (in == null) {
      throw new NullPointerException("Argument 'in' cannot have a null value");
    }
    if (out == null) {
      throw new NullPointerException("Argument 'out' cannot have a null value");
    }
    if (compressionLevel < -1 || compressionLevel > 9) {
      throw new IllegalArgumentException("Argument 'compressionLevel' must be between -1 and 9.");
    }

    Deflater deflater = null;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      deflater = new Deflater(compressionLevel, noWrap);
      deflater.setStrategy(strategy);
      DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater, 8192);
      byte[] bin = new byte[1024];
      int r;
      while (true) {
        r = in.readBytes(bin, 0, bin.length);
        if (r > 0) {
          dos.write(bin, 0, r);
        } else {
          break;
        }
      }
      dos.close();
      byte[] bout = baos.toByteArray();
      out.writeBytes(bout, 0, bout.length);
      return bout.length;
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    } finally {
      /*
       * "Sun's Deflater class allocates some non-heap memory, which is only cleared with its end() call, or when its
       * finalizer is called. It's possible to fill up the non-heap memory when the deflater is used frequently
       * enough that finalizers don't get called in time." - Craig Fields
       */
      if (deflater != null) {
        deflater.end();
      }
    }
  }

  /**
   * Attempts to fully read the given stream 'in', inflating and writing to the given stream 'out'.
   * 
   * @param in
   *           Deflated input stream
   * @param out
   *           Inflated output stream
   * @return Size of the inflated stream
   * @throws IOException
   * @throws ZipException
   */
  public static int inflate(Stream in, Stream out) throws IOException, ZipException {
    return inflate(in, out, -1, false);
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
   *           How many bytes to read, or -1 to read until <code>in</code>'s end
   * @return Size of the inflated stream
   * @throws ZipException
   * @throws IOException
   */
  public static int inflate(Stream in, Stream out, int sizeIn) throws IOException, ZipException {
    return inflate(in, out, sizeIn, false);
  }

  /**
   * Attempts to read the number of bytes specified by 'sizeIn' from the the given stream 'in', inflating and writing
   * to the given stream 'out'. If 'sizeIn' is -1, it will attempt to fully read the stream. If the parameter 'noWrap'
   * is true then the ZLIB header and checksum fields will not be used. This provides compatibility with the
   * compression format used by both GZIP and PKZIP.<br>
   * Note: When using the 'noWrap' option it is also necessary to provide an extra "dummy" byte as input. This is
   * required by the ZLIB native library in order to support certain optimizations.
   * 
   * @param in
   *           Deflated input stream
   * @param out
   *           Inflated output stream
   * @param sizeIn
   *           How many bytes to read, or -1 to read until <code>in</code>'s end
   * @param noWrap
   *           if true then support GZIP compatible compression
   * @return Size of the inflated stream
   * @throws ZipException
   * @throws IOException
   */
  @ReplacedByNativeOnDeploy
  public static int inflate(Stream in, Stream out, int sizeIn, boolean noWrap) throws IOException, ZipException {
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

    Inflater inf = new Inflater(noWrap);
    byte[] bin = new byte[Math.min(sizeIn <= 0 ? 1024 : sizeIn, 1024)];
    byte[] bout = new byte[bin.length * 10];
    int r = 0, w, rt = 0, wt = 0;
    int s = sizeIn;

    try {
      while (true) {
        int tor = sizeIn == -1 ? bin.length : Math.min(bin.length, s);
        if (tor > 0) {
          r = in.readBytes(bin, 0, tor); // if tor == 0 and the stream does not quietly accept requests of size 0 (such as File stream) this call will throw exception
        }
        if (r > 0) {
          inf.setInput(bin, 0, r);
          while (true) {
            w = inf.inflate(bout);
            if (w <= 0) {
              break;
            }
            out.writeBytes(bout, 0, w);
            wt += w;
          }
          rt += r;
          s -= r;
          r = 0; // reset r
          if (sizeIn > 0) {
            sizeIn -= r;
          }
        } else {
          break;
        }
      }
    } catch (totalcross.io.IOException e) {
      throw new IOException(e.getMessage());
    } catch (java.util.zip.DataFormatException e) {
      throw new ZipException(e.getMessage());
    }
    if (rt > 0 && (wt == 0 || (sizeIn > 0 && s > 0))) {
      throw new ZipException("Inflate error: " + "Read " + rt + " but could not decompress it.");
    }
    return wt;
  }

  private ZLib() {
  } // cannot instantiate
}
