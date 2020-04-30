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

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import totalcross.Launcher;
import totalcross.io.IOException;
import totalcross.io.Stream;

/**
 * This class implements a stream filter for compressing or uncompressing data stored in the GZip compression format.<br>
 * 
 * @see totalcross.util.zip.GZip
 * 
 * @since TotalCross 1.12
 */

public class GZipStream extends CompressedStream {
  /**
   * Creates a GZipStream object that may be used to read compressed data from the given stream, or to write compressed
   * data to the given stream.<br>
   * 
   * @param stream
   *           input stream.
   * @param mode
   *           its value must be either DEFLATE or INFLATE.
   * 
   * @since TotalCross 1.12
   */
  public GZipStream(Stream stream, int mode) throws IOException {
    if (stream == null) {
      throw new NullPointerException("Argument stream cannot have a null value.");
    }

    this.mode = mode;
    try {
      switch (mode) {
      case DEFLATE: {
        Launcher.S2OS os = new Launcher.S2OS(stream, false);
        compressedStream = new GZIPOutputStream(os);
      }
        break;
      case INFLATE: {
        Launcher.S2IS is = new Launcher.S2IS(stream, -1, false);
        compressedStream = new GZIPInputStream(is);
      }
        break;
      default:
        throw new IllegalArgumentException("Argument mode must be either DEFLATE or INFLATE.");
      }
    } catch (java.io.IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  @Override
  protected Object createDeflate(Stream stream) {
    throw new Error("Not implemented for GZip");
  }

  @Override
  protected Object createInflate(Stream stream) {
    throw new Error("Not implemented for GZip");
  }
}
