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

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import totalcross.util.concurrent.Lock;

public class PrintStream4D extends FilterOutputStream implements Appendable, Closeable {
  private boolean autoFlush;
  protected Lock lock = new Lock();
  private boolean errorFound = false;

  public PrintStream4D(OutputStream out) {
    this(out, false);
  }

  public PrintStream4D(OutputStream out, boolean autoFlush) {
    super(out);
    this.autoFlush = autoFlush;
  }

  @Override
  public PrintStream4D append(CharSequence csq) throws IOException {
    print(csq.toString());
    return this;
  }

  @Override
  public PrintStream4D append(CharSequence csq, int start, int end) throws IOException {
    print(csq.subSequence(start, end).toString());
    return this;
  }

  @Override
  public PrintStream4D append(char c) throws IOException {
    write(c);
    return this;
  }

  public void print(String string) {
    if (string == null) {
      print("null");
    } else {
      writeNoException(string.getBytes());
    }
  }

  public void print(char c) {
    writeNoException(c);
  }

  public void println(String str) {
    synchronized (lock) {
      print(str);
      newLine();
    }
  }

  public void println() {
    newLine();
  }

  private void newLine() {
    writeNoException('\n');
  }

  private void writeNoException(char c) {
    try {
      write(c);
    } catch (IOException e) {
      errorFound = true;
    }
  }

  private void writeNoException(byte[] bytes) {
    try {
      write(bytes);
    } catch (IOException e) {
      errorFound = true;
    }
  }

  public boolean checkError() {
    return errorFound;
  }

  protected void clearError() {
    errorFound = false;
  }

  protected void setError() {
    errorFound = true;
  }

}
