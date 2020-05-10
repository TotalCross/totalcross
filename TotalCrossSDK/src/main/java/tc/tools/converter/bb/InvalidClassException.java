// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bb;

public class InvalidClassException extends RuntimeException {
  public InvalidClassException(JavaClass jc, String reason) {
    super("Class '" + jc + "' is not valid" + (reason != null ? ": " + reason : ""));
  }

  public InvalidClassException(String reason) {
    super(reason);
  }
}
