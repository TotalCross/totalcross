// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.io;

import java.io.IOException;
import java.io.InputStream;

import totalcross.sys.Vm;

public class ByteArrayInputStream4D extends InputStream {
  private byte[] buf;
  private int offset;
  private int length;

  public ByteArrayInputStream4D(byte[] buf) {
    this(buf, 0, buf.length);

  }

  public ByteArrayInputStream4D(byte[] buf, int offset, int length) {
    this.buf = buf;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public int read() throws IOException {
    byte[] buf = new byte[1];
    int count = read(buf, 0, 1);
    return count > 0 ? buf[0] : -1;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int availableRead = this.length - this.offset;

    if (availableRead == 0) {
      return -1;
    }

    int deltaRead = len - off;
    int originalOffset = this.offset;
    int reallyRead;

    // if we can read at least the remaining size, read all
    if (deltaRead >= availableRead) {
      reallyRead = availableRead;
      this.offset = this.length;
    } else {
      reallyRead = deltaRead;
      this.offset += deltaRead;
    }
    Vm.arrayCopy(buf, originalOffset, b, off, reallyRead);
    return reallyRead;
  }

}
