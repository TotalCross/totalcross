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
package tc.tools.converter.tclass;

/* Debug info - line number information */
public final class TCLineNumber {
  // The pc where this line number goes
  public int /*uint16*/ startPC;
  // The line number itself;
  public int /*uint16*/ lineNumber;

  public TCLineNumber(int startPC, int lineNumber) {
    this.startPC = startPC;
    this.lineNumber = lineNumber;
  }
}
