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

import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;

public abstract class ZipStream4D extends CompressedStream4D {
  Object nativeZip;
  ZipEntry lastEntry;

  int defaultMethod = DEFLATED;

  public final static int STORED = 0;
  public final static int DEFLATED = 8;

  public ZipStream4D(RandomAccessStream stream, int mode) {
    super(stream, mode, ZIP_COMPRESSION);
  }

  @Override
  native protected Object createInflate(Stream stream);

  @Override
  native protected Object createDeflate(Stream stream, int compressionType);

  native public int available() throws IOException;

  native public ZipEntry getNextEntry() throws IOException;

  native public void putNextEntry(ZipEntry entry) throws IOException;

  native public void closeEntry() throws IOException;

  @Override
  native public int readBytes(byte[] buf, int start, int count) throws IOException;

  @Override
  native public int writeBytes(byte[] buf, int start, int count) throws IOException;

  @Override
  native public void close() throws IOException;

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
