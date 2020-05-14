// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
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
