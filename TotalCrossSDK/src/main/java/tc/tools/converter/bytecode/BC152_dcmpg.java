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

public class BC152_dcmpg extends Comparison {
  public BC152_dcmpg() {
    super(-1, -2, -2, -1, DOUBLE);
  }

  @Override
  public void exec() {
    double r = stack[stackPtr - 2].asDouble - stack[stackPtr - 1].asDouble;
    stack[stackPtr - 2].asInt = (r > 0) ? 1 : (r < 0) ? -1 : 0;
  }
}
