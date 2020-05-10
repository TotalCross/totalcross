// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC187_new extends Allocation {
  public String className;

  public BC187_new() {
    className = cp.getString1(readUInt16(pc + 1));
    stackInc = 1;
    pcInc = 3;
  }

  @Override
  public void exec() {
    stack[stackPtr].asObj = className; // should be an instance of this class, but we'll store the name instead
  }
}
