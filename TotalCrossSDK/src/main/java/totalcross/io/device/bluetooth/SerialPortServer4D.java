// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.bluetooth;

import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.io.StreamConnectionNotifier;

public class SerialPortServer4D extends StreamConnectionNotifier {
  Object nativeHandle;

  public SerialPortServer4D(String uuid, String[] params) throws IOException {
    createSerialPortServer(uuid, params);
  }

  native private void createSerialPortServer(String uuid, String[] params) throws IOException;

  @Override
  native public Stream accept() throws IOException;

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
