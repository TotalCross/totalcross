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

package totalcross.json;

/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class JSONException extends RuntimeException {
  private static final long serialVersionUID = 0;
  private Throwable cause;

  /**
   * Constructs a JSONException with an explanatory message.
   *
   * @param message
   *            Detail about the reason for the exception.
   */
  public JSONException(String message) {
    super(message);
  }

  /**
   * Constructs a JSONException with an explanatory message.
   *
   * @param message
   *            Detail about the reason for the exception.
   */
  public JSONException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new JSONException with the specified cause.
   * @param cause The cause.
   */
  public JSONException(Throwable cause) {
    super(cause.getMessage());
    this.cause = cause;
  }

  /**
   * Returns the cause of this exception or null if the cause is nonexistent
   * or unknown.
   *
   * @return the cause of this exception or null if the cause is nonexistent
   *          or unknown.
   */
  @Override
  public Throwable getCause() {
    return this.cause;
  }
}
