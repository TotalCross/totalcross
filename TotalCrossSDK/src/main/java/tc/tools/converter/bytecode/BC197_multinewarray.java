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

public class BC197_multinewarray extends Allocation {
  public String className;
  public int dimCount;
  public int[] dimensions;

  public BC197_multinewarray() {
    className = cp.getString1(readUInt16(pc + 1));
    dimCount = readUInt8(pc + 3);
    stackInc = 1;
    pcInc = 4;
  }

  @Override
  public void exec() {
    dimensions = new int[dimCount];
    for (int i = 0; i < dimCount; i++) {
      dimensions[i] = stack[stackPtr - dimCount + i].asInt;
    }

    stack[stackPtr - 1].type = OBJECT;
    stack[stackPtr - 1].asObj = "array"; // should be an instance of this class, but we'll store this info instead
    stack[stackPtr - 1].asInt = dimensions[0];
    stack[stackPtr - 1].asLong = 1;
  }
}
