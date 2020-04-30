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

package totalcross.net.mail;

/**
 * Thrown when a write operation fails when sending a Message, or when an unexpected code is received from the remote
 * host.
 * 
 * @since TotalCross 1.13
 */
public class MessagingException extends Exception {
  private Throwable cause;

  /** Constructs an empty Exception. */
  public MessagingException() {
    super();
  }

  /** Constructs an exception with the given message. */
  public MessagingException(String msg) {
    super(msg);
  }

  public MessagingException(Throwable cause) {
    super(cause == null ? null : cause.getMessage());
    this.cause = cause;
  }

  @Override
  public void printStackTrace() {
    if (cause != null) {
      cause.printStackTrace();
    } else {
      super.printStackTrace();
    }
  }

  @Override
  public Throwable getCause() {
    return cause;
  }
}
