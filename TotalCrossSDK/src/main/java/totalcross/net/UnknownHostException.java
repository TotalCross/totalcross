// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net;

/** Thrown when you try to connect to a host that was not found. */

public class UnknownHostException extends totalcross.io.IOException {
  /** Constructs an exception with the given message. */
  public UnknownHostException(String msg) {
    super(msg);
  }
}
