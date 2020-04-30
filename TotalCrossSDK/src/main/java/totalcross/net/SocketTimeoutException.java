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

package totalcross.net;

import totalcross.io.IOException;

/** Thrown when a socket times out in a connect, read or write operation. */

public class SocketTimeoutException extends IOException {
  /** Constructs an exception with the given message. */
  public SocketTimeoutException(String msg) {
    super(msg);
  }
}
