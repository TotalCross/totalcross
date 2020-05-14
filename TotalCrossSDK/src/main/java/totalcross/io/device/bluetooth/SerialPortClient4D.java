// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.bluetooth;

import totalcross.io.IOException;
import totalcross.io.Stream;

public class SerialPortClient4D extends Stream {
  Object nativeHandle;

  public SerialPortClient4D(String address, int port, String[] params) throws IOException {
    createSerialPortClient(address, port, params);
  }

  native private void createSerialPortClient(String address, int port, String[] params) throws IOException;

  @Override
  native public int readBytes(byte[] buf, int start, int count) throws IOException;

  @Override
  native public int writeBytes(byte[] buf, int start, int count) throws IOException;

  @Override
  native public void close() throws IOException;

  @Override
  protected void finalize() {
    if (nativeHandle != null) {
      try {
        this.close();
      } catch (IOException e) {
      }
    }
  }
}
