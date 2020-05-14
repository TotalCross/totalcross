// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io;

/** Base class of all input/output exceptions.
 */

public class IOException extends java.io.IOException {
  private static final long serialVersionUID = 166767434534267900L;

  /** Constructs an empty Exception. */
  public IOException() {
    super();
  }

  /** Constructs an exception with the given message. */
  public IOException(String msg) {
    super(msg);
  }

  public IOException(String message, Throwable cause) {
    super(message, cause);
  }

  public IOException(Throwable cause) {
    super(cause);
  }

}
