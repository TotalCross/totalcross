// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io;

/** Illegal argument passed to an IO method. */

public class IllegalArgumentIOException extends IOException {
  /** Constructs an empty Exception. */
  public IllegalArgumentIOException() {
    super();
  }

  /** Constructs an exception with the given message. */
  public IllegalArgumentIOException(String msg) {
    super(msg);
  }

  /** Constructs an exception with the given message and argument value. */
  public IllegalArgumentIOException(String argumentName, String argumentValue) {
    super("Invalid value for argument '" + argumentName + "': " + argumentValue);
  }
}
