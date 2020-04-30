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
package tc.tools.converter.bb;

public class ClassConsistencyException extends Exception {
  public ClassConsistencyException(JavaClass jc1, String reason) {
    super("Class '" + jc1 + "' is not consistent" + (reason != null ? ": " + reason : ""));
  }

  public ClassConsistencyException(JavaClass jc1, JavaClass jc2, String reason) {
    super("Class '" + jc1 + "' is not consistent with '" + jc2 + "'" + (reason != null ? ": " + reason : ""));
  }
}
