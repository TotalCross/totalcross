// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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
