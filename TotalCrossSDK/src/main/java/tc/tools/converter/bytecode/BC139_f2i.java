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

public class BC139_f2i extends Conversion {
  public BC139_f2i() {
    super(-1, -1, FLOAT, INT);
  }

  @Override
  public void exec() {
    double f = stack[stackPtr - 1].asDouble;
    stack[stackPtr - 1].asInt = (f > 2147483647.0) ? 0x7FFFFFFF : (f < -2147483648.0) ? 0x80000000 : (int) f;
  }
}
