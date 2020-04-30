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

import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import totalcross.io.IOException;
import totalcross.io.Stream;

/**
 * Base class for stream filters that perform data compression and decompression.<br>
 * 
 * @since TotalCross 1.12
 */

public abstract class CompressedStream extends Stream {
  protected Object compressedStream;
  protected int mode;

  /**
   * Used to create a stream that will be used for data compression. In this mode, you may NOT use the method
   * readBytes.
   */
  public static final int DEFLATE = 1;
  /**
   * Used to create a stream that will be used for data decompression. In this mode, you may NOT use the method
   * writeBytes.
   */
  public static final int INFLATE = 2;

  protected CompressedStream() {
  }

  /**
   * Creates a CompressedStream object that may be used to read compressed data from the given stream, or to write
   * compressed data to the given stream.<br>
   * 
   * @param stream
   *           input stream.
   * @param mode
   *           its value must be either DEFLATE or INFLATE.
   * 
   * @since TotalCross 1.12
   */
  protected CompressedStream(Stream stream, int mode) {
    if (stream == null) {
      throw new NullPointerException("Argument stream cannot have a null value.");
    }

    this.mode = mode;
    switch (mode) {
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

  /**
   * Attempts to read count bytes to the given byte array, starting from offset start.<br>
   * This method can be used only with CompressedStreams created in INFLATE mode, and should be used only for reading
   * compressed data from the original stream, returning uncompressed data on the give byte array.
   * 
   * @since TotalCross 1.12
   */
  @Override
  public int readBytes(byte[] buf, int start, int count) throws IOException, ZipException {
    if (mode != INFLATE) {
      throw new IOException("This operation can only be performed in INFLATE mode.");
    }

    try {
      int r = 0;
      do {
        int l = ((InflaterInputStream) compressedStream).read(buf, start, count); // guich@tc125_28
        if (l <= 0) {
          break;
        }
        r += l;
        start += l;
        count -= l;
      } while (count > 0);
      return r == 0 ? -1 : r;
    } catch (java.io.EOFException e) {
      return -1;
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Attempts to write count bytes from the given byte array, starting from offset start.<br>
   * This method can be used only with CompressedStreams created in DEFLATE mode, and should be used only for
   * compressing the contents of the given byte array, and writing the compressed data to the original stream.
   * 
   * @since TotalCross 1.12
   */
  @Override
  public int writeBytes(byte[] buf, int start, int count) throws IOException {
    if (mode != DEFLATE) {
      throw new IOException("This operation can only be performed in DEFLATE mode.");
    }

    try {
      ((DeflaterOutputStream) compressedStream).write(buf, start, count);
      return count;
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Closes this stream WITHOUT closing the underlying stream, which must be explicitly closed by the user.<br>
   * 
   * @since TotalCross 1.12
   */
  @Override
  public void close() throws IOException {
    try {
      switch (mode) {
      case DEFLATE:
        ((DeflaterOutputStream) compressedStream).close();
        break;
      case INFLATE:
        ((InflaterInputStream) compressedStream).close();
        break;
      default:
        throw new IOException("Invalid object.");
      }
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    } finally {
      mode = 0;
    }
  }

  @Override
  protected void finalize() {
    try {
      if (mode != 0) {
        this.close();
      }
    } catch (Throwable t) {
    }
  }
}
