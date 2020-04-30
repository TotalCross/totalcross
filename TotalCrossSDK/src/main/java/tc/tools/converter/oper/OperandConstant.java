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

public abstract class OperandConstant extends Operand implements TCConstants {
  public long value;

  public OperandConstant(long value, int type) {
    super(0);
    kind = defineKind(value, type);
    this.value = value;
  }

  private int defineKind(long value, int type) {
    if (value >= -32 && value <= 31) {
      switch (type) {
      case type_Int:
        return opr_s6I;
      case type_Double:
        return opr_s6D;
      case type_Long:
        return opr_s6L;
      }
    }

    if (value >= -2048 && value <= 2047) {
      switch (type) {
      case type_Int:
        return opr_s12I;
      case type_Double:
        return opr_s12D;
      case type_Long:
        return opr_s12L;
      }
    }

    if (value >= -32768 && value <= 32767) {
      switch (type) {
      case type_Int:
        return opr_s16I;
      case type_Double:
        return opr_s16D;
      case type_Long:
        return opr_s16L;
      }
    }

    if (value >= 0 && value <= 65536) {
      return opr_u16;
    }

    if (value >= -131072 && value <= 131071) {
      switch (type) {
      case type_Int:
        return opr_s18I;
      case type_Double:
        return opr_s18D;
      case type_Long:
        return opr_s18L;
      }
    }

    if (value >= -8388608 && value <= 8388607) {
      switch (type) {
      case type_Int:
        return opr_s24I;
      case type_Double:
        return opr_s24D;
      case type_Long:
        return opr_s24L;
      }
    }

    if (value >= -2147483648 && value <= 2147483647) {
      switch (type) {
      case type_Int:
        return opr_s32I;
      case type_Double:
        return opr_s32D;
      case type_Long:
        return opr_s32L;
      }
    }

    if (type == type_Long) {
      return opr_s64L;
    }

    return opr_s64D;
  }

  public int getValueAsInt() {
    return (int) value;
  }

  public long getValueAsLong() {
    return value;
  }

  @Override
  public boolean isConstant() {
    return true;
  }

  @Override
  public boolean isConstantInt() {
    return (kind != opr_s64L && kind != opr_s64D);
  }

  @Override
  public boolean isConstantLong() {
    return (kind == opr_s64L || kind == opr_s64D);
  }

  public static boolean fitsIn18Bits(double v) {
    return (v == (int) v) && (v >= -131072 && v <= 131071);
  }
}
