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

import tc.tools.converter.TCValue;

public class LoadConstant extends ByteCode {
  /** The value used in this instruction. */
  public TCValue val = new TCValue();

  public LoadConstant(int v) {
    targetType = val.type = INT;
    val.asInt = v;
  }

  public LoadConstant(long v) {
    targetType = val.type = LONG;
    val.asLong = v;
    stackInc = 2;
  }

  public LoadConstant(double v) {
    targetType = val.type = DOUBLE;
    val.asDouble = v;
    stackInc = 2;
  }

  public LoadConstant(float v) {
    targetType = val.type = DOUBLE;
    val.asDouble = v;
    stackInc = 1;
  }

  public LoadConstant(Object v) {
    targetType = val.type = OBJECT;
    val.asObj = v;
  }

  @Override
  public void exec() {
    stack[stackPtr].copyFrom(val);
    stackPtr += stackInc;
    pc += pcInc;
  }
}
