// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bb;

public class ClassConsistencyException extends Exception {
  public ClassConsistencyException(JavaClass jc1, String reason) {
    super("Class '" + jc1 + "' is not consistent" + (reason != null ? ": " + reason : ""));
  }

  public ClassConsistencyException(JavaClass jc1, JavaClass jc2, String reason) {
    super("Class '" + jc1 + "' is not consistent with '" + jc2 + "'" + (reason != null ? ": " + reason : ""));
  }
}
