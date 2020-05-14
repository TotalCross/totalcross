// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.unit;

/** 
 * Exception dispatched when an assertion fails. 
 */
public class AssertionFailedError extends RuntimeException {
  /** 
   * Constructs an empty exception. 
   */
  public AssertionFailedError() {
    super();
  }

  /** 
   * Constructs an exception with the given message. 
   *
   * @param arg0 The error message.
   */
  public AssertionFailedError(String arg0) {
    super(arg0);
  }
}
