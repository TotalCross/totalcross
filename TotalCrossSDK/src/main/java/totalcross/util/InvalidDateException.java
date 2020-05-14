// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.util;

/** Represents a date that is invalid. */

public class InvalidDateException extends Exception {
  /** Constructs an empty Exception. */
  public InvalidDateException() {
    super();
  }

  /** Constructs an exception with the given message. */
  public InvalidDateException(String msg) {
    super(msg);
  }
}
