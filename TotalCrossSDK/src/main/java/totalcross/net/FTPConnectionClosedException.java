// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net;

/** This Exception is thrown when a FTP connection is closed, maybe explicitly or
 * by timeout.
 */
public class FTPConnectionClosedException extends Exception {
  /** Constructs an exception with the given message. */
  public FTPConnectionClosedException(String msg) {
    super(msg);
  }
}
