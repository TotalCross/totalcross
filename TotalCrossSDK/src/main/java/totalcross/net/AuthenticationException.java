// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net;

/** 
 * Exception thrown when an authentication fails.
 */
public class AuthenticationException extends Exception {
  /**
   * Constructs an empty exception.
   */
  public AuthenticationException() {
  }

  /** 
   * Constructs an exception with the given message.
   * 
   * @param msg The error message.
   */
  public AuthenticationException(String msg) {
    super(msg);
  }
}
