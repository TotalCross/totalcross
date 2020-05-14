// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

/** Thrown when you try to convert a String that does not represents a valid number. */

public class InvalidNumberException extends Exception {
  /** Constructs an empty Exception. */
  public InvalidNumberException() {
  }

  /** Constructs an exception with the given message. */
  public InvalidNumberException(String message) {
    super(message);
  }
}
