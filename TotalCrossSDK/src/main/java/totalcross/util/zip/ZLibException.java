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

package totalcross.util.zip;

/**
 * This exception may be dispatched by zLib routines.
 */

public class ZLibException extends Exception {
  // Stores the position where the error was found.
  private String internalError = "";

  /** Default constructor.
   */
  public ZLibException() {
    super();
  }

  /** Constructor which accepts an error message.
   *
   * @param msg the error message
   */
  public ZLibException(String msg, String internalError) {
    super(msg);
    this.internalError = internalError;
  }

  /**
   * Returns the position where the error was found.
   * @return the position where the error was found.
   */
  public String getInternalError() {
    return internalError;
  }

  @Override
  public String toString() {
    String s = super.toString();
    if (internalError != null && internalError.length() > 0) {
      s += "; cause: " + internalError;
    }

    return s;
  }
}
