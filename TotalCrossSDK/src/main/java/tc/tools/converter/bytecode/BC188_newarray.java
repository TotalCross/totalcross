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

public class BC188_newarray extends Allocation {
  public int arrayType, arrayLen;

  public BC188_newarray() {
    arrayType = readUInt8(pc + 1);
    arrayLen = -1; // in stack
    stackInc = 0;
    pcInc = 2;
  }

  @Override
  public void exec() {
    arrayLen = stack[stackPtr - 1].asInt;
    stack[stackPtr - 1].type = OBJECT;
    stack[stackPtr - 1].asObj = "array"; // should be an instance of this class, but we'll store this info instead
    stack[stackPtr - 1].asInt = arrayLen;
    stack[stackPtr - 1].asLong = convertArrayType(arrayType);
  }

  private int convertArrayType(int arrayType2) {
    switch (arrayType) {
    case 4:
      return BOOLEAN;
    case 5:
      return CHAR;
    case 6:
      return FLOAT;
    case 7:
      return DOUBLE;
    case 8:
      return BYTE;
    case 9:
      return SHORT;
    case 10:
      return INT;
    case 11:
      return LONG;
    default:
      return VOID;
    }
  }
}
