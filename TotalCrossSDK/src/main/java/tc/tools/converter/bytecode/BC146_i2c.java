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

public class BC146_i2c extends Conversion // this conversion should be ignored
{
  public BC146_i2c() {
    super(-1, -1, INT, CHAR);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asInt = (char) stack[stackPtr - 1].asInt;
  }
}
