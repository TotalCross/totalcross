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
import java.io.OutputStream;
import java.io.Writer;

import totalcross.sys.Convert;

public class OutputStreamWriter4D extends Writer {
  private OutputStream out;

  public OutputStreamWriter4D(OutputStream out) {
    this.out = out;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    out.write(Convert.charConverter.chars2bytes(cbuf, off, len));
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
  }

}
