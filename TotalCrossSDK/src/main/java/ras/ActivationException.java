/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package ras;

public class ActivationException extends Exception {
  private Throwable cause;

  public ActivationException(String message, Throwable cause) {
    super(message + (cause == null ? ""
        : "; reason: " + (cause.getMessage() == null ? "No detailed message (" + cause.getClass().getName() + ")"
            : cause.getMessage())));
    this.cause = cause;
  }

  public ActivationException(String message) {
    super(message);
  }

  @Override
  public Throwable getCause() {
    return cause;
  }
}
