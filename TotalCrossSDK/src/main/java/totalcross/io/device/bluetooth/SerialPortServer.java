// Copyright (C) 2000-2012 SuperWaba Ltda.
// Copyright (C) 2017-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.device.bluetooth;

import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.io.StreamConnectionNotifier;

public class SerialPortServer extends StreamConnectionNotifier {
  Object nativeHandle;

  public SerialPortServer(String uuid, String[] params) throws IOException {
    createSerialPortServer(uuid, params);
  }

  private void createSerialPortServer(String uuid, String[] params) throws IOException {
  }

  @Override
  public Stream accept() throws IOException {
    return null;
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
