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
package tc.tools.converter.bytecode;

public class BC148_lcmp extends Comparison {
  public BC148_lcmp() {
    super(-1, -2, -2, -1, LONG);
  }

  @Override
  public void exec() {
    long r = stack[stackPtr - 2].asLong - stack[stackPtr - 1].asLong;
    stack[stackPtr - 2].asInt = (r > 0) ? 1 : (r < 0) ? -1 : 0;
  }
}
