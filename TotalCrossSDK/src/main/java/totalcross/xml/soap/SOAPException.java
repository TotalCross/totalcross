// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml.soap;

public class SOAPException extends Exception {
  private Throwable cause;

  public SOAPException(String string) {
    super(string);
  }

  public SOAPException(Throwable cause) {
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
