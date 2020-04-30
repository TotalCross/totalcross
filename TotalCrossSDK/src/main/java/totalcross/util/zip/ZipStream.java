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

import totalcross.Launcher;
import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;

/**
 * This class implements a stream filter for reading and writing files in the ZIP file format. Currently supports only
 * compressed entries.<br>
 * <br>
 * See the sammple TotalCross3/src/tc/samples/util/zip/zip.
 * 
 * @since TotalCross 1.20
 */

public class ZipStream extends CompressedStream {
  int defaultMethod = DEFLATED;

  /** Compression method for uncompressed (STORED) entries. */
  public final static int STORED = 0;
  /** Compression method for compressed (DEFLATED) entries. */
  public final static int DEFLATED = 8;

  /**
   * Creates a new ZIP stream that may be used to read compressed data from the given stream, or to write compressed
   * data to the given stream.<br>
   * 
   * @param stream
   *           input stream.
   * @param mode
   *           its value must be either DEFLATE or INFLATE.
   * 
   * @since TotalCross 1.20
   */
  public ZipStream(RandomAccessStream stream, int mode) {
    super(stream, mode);
  }

  @Override
  protected Object createDeflate(Stream stream) {
    Launcher.S2OS os = new Launcher.S2OS(stream, false);
    return new java.util.zip.ZipOutputStream(os);
  }

  @Override
  protected Object createInflate(Stream stream) {
    Launcher.S2IS is = new Launcher.S2IS(stream, -1, false);
    return new java.util.zip.ZipInputStream(is);
  }

  /**
   * Returns 0 after EOF has reached for the current entry data, otherwise always return 1.<br>
   * Programs should not count on this method to return the actual number of bytes that could be read without blocking.
   * 
   * @return 1 before EOF and 0 after EOF has reached for current entry.
   * @throws IOException
   *            if an I/O error occurs.
   * 
   * @since TotalCross 1.20
   */
  public int available() throws IOException {
    if (mode != INFLATE) {
      throw new IOException("This operation can only be performed in INFLATE mode.");
    }

    try {
      return ((java.io.InputStream) this.compressedStream).available();
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Reads the next ZIP file entry and positions the stream at the beginning of the entry data.
   * 
   * @return the next ZIP file entry, or null if there are no more entries
   * @throws IOException
   *            if an I/O error has occurred
   * 
   * @since TotalCross 1.20
   */
  public ZipEntry getNextEntry() throws IOException {
    if (mode != INFLATE) {
      throw new IOException("This operation can only be performed in INFLATE mode.");
    }

    java.util.zip.ZipInputStream zis = (java.util.zip.ZipInputStream) this.compressedStream;
    try {
      Object zipEntry = zis.getNextEntry();
      if (zipEntry == null) {
        return null;
      }
      return new ZipEntry(zipEntry);
    } catch (java.io.EOFException e) {
      return null;
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Begins writing a new ZIP file entry and positions the stream to the start of the entry data. Closes the current
   * entry if still active.
   * 
   * @param entry
   *           the ZIP entry to be written
   * @throws IOException
   *            if an I/O error has occurred
   * 
   * @since TotalCross 1.20
   */
  public void putNextEntry(ZipEntry entry) throws IOException, ZipException {
    if (mode != DEFLATE) {
      throw new IOException("This operation can only be performed in DEFLATE mode.");
    }

    java.util.zip.ZipOutputStream zos = (java.util.zip.ZipOutputStream) this.compressedStream;
    try {
      zos.putNextEntry((java.util.zip.ZipEntry) entry.zipEntry);
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Closes the current ZIP entry and positions the stream for reading (INFLATE) or writing (DEFLATE) the next entry.
   * 
   * @throws IOException
   *            if an I/O error has occurred
   * 
   * @since TotalCross 1.20
   */
  public void closeEntry() throws IOException, ZipException {
    try {
      if (mode == DEFLATE) {
        java.util.zip.ZipOutputStream zos = (java.util.zip.ZipOutputStream) this.compressedStream;
        zos.closeEntry();
      } else {
        java.util.zip.ZipInputStream zis = (java.util.zip.ZipInputStream) this.compressedStream;
        zis.closeEntry();
      }
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  @Override
  public void close() throws IOException {
    try {
      switch (mode) {
      case DEFLATE:
        ((java.util.zip.ZipOutputStream) compressedStream).finish();
        break;
      case INFLATE:
        ((java.util.zip.ZipInputStream) compressedStream).close();
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
}
