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

public class BC134_i2f extends Conversion {
  public BC134_i2f() {
    super(-1, -1, INT, FLOAT);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asDouble = (float) stack[stackPtr - 1].asInt;
  }
}
