// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.bluetooth;

import totalcross.io.IOException;
import totalcross.io.Stream;

public class SerialPortClient extends Stream {
  Object nativeHandle;

  public SerialPortClient(String address, int port, String[] params) throws IOException {
    createSerialPortClient(address, port, params);
  }

  private void createSerialPortClient(String address, int port, String[] params) throws IOException {
  }

  @Override
  public int readBytes(byte[] buf, int start, int count) throws IOException {
    return 0;
  }

  @Override
  public int writeBytes(byte[] buf, int start, int count) throws IOException {
    return 0;
  }

  @Override
  public void close() throws IOException {
  }

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
