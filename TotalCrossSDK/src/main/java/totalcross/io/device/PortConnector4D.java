// Copyright (C) 2000 Matthias Ringwald
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
package totalcross.io.device;

import totalcross.io.Stream;
import totalcross.sys.Convert;

public class PortConnector4D extends Stream {
  private Object portConnectorRef;
  Object receiveBuffer; // Used only on PALMOS
  int portNumber;

  public int readTimeout = 6000;
  public int writeTimeout = 6000; // guich@570_67
  public boolean stopWriteCheckOnTimeout = true; // guich@570_65
  boolean dontFinalize;

  public static final int DEFAULT = 0;
  public static final int IRCOMM = 0x1000;
  public static final int SIR = 0x1001;
  public static final int USB = 0x1002;
  public static final int BLUETOOTH = 0x1003; // guich@330_34
  public static final int PARITY_NONE = 0;
  public static final int PARITY_EVEN = 1;
  public static final int PARITY_ODD = 2;

  public PortConnector4D(int number, int baudRate, int bits, boolean parity, int stopBits)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    this(number, baudRate, bits, (parity ? PARITY_EVEN : PARITY_NONE), stopBits);
  }

  public PortConnector4D(int number, int baudRate) throws totalcross.io.IOException {
    this(number, baudRate, 8, PARITY_NONE, 1);
  }

  public PortConnector4D(int number, int baudRate, int bits, int parity, int stopBits)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (bits < 5 || bits > 8) {
      throw new totalcross.io.IllegalArgumentIOException("bits", Convert.toString(bits));
    }

    portNumber = number;
    create(number, baudRate, bits, parity, stopBits);
  }

  @Override
  public void close() throws totalcross.io.IOException {
    if (portConnectorRef == null) {
      throw new totalcross.io.IOException("The port is not open");
    }
    nativeClose();
  }

  @Override
  public int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (portConnectorRef == null) {
      throw new totalcross.io.IOException("The port is not open");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    if (count == 0) {
      return 0;
    }

    return readWriteBytes(buf, start, count, true);
  }

  @Override
  public int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (portConnectorRef == null) {
      throw new totalcross.io.IOException("The port is not open");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    if (count == 0) {
      return 0;
    }

    return readWriteBytes(buf, start, count, false);
  }

  native void create(int number, int baudRate, int bits, int parity, int stopBits) throws totalcross.io.IOException;

  native private void nativeClose() throws totalcross.io.IOException;

  native public void setFlowControl(boolean on) throws totalcross.io.IOException;

  native public int readWriteBytes(byte buf[], int start, int count, boolean isRead) throws totalcross.io.IOException;

  native public int readCheck() throws totalcross.io.IOException;

  @Override
  protected void finalize() {
    try {
      close();
    } catch (totalcross.io.IOException e) {
    }
  }
}