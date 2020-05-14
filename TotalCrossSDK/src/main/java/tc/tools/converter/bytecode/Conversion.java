// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class Conversion extends ByteCode {
  public int result, operand, srcType;

  public Conversion(int result, int operand, int srcType, int dstType) {
    this.result = result;
    this.operand = operand;
    this.srcType = srcType;
    this.targetType = dstType;
    stackInc = 0;
  }
}
