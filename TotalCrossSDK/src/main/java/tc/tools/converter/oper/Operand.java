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
package tc.tools.converter.oper;

import tc.tools.converter.TCConstants;

abstract public class Operand implements TCConstants {
  // the kind of operand (see class TCConstants)
  public int kind;
  // the number of words of this operand in the java stack.
  public int nWords = 1; // 1 = 32 bits; 2 = 64 bits

  public Operand(int kind) {
    this.kind = kind;
  }

  public boolean isReg() {
    return false;
  }

  public boolean isSym() {
    return false;
  }

  public boolean isConstant() {
    return false;
  }

  public boolean isConstantInt() {
    return false;
  }

  public boolean isConstantLong() {
    return false;
  }
}
