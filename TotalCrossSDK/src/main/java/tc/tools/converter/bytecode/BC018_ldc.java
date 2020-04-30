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
import tc.tools.converter.java.JavaConstantInfo;

public class BC018_ldc extends LoadLocal {
  public TCValue val = new TCValue();

  public BC018_ldc() {
    this(readUInt8(pc + 1), 2);
  }

  public BC018_ldc(int ofs, int pcInc) {
    super(ofs, 0);
    this.pcInc = pcInc;
    Object o = cp.constants[ofs];
    if (o instanceof JavaConstantInfo) {
      JavaConstantInfo jci = (JavaConstantInfo) o;
      o = cp.constants[jci.index1];
    }
    if (o instanceof String) {
      targetType = val.type = OBJECT;
      val.asObj = o;
    } else if (o instanceof Integer) {
      targetType = val.type = INT;
      val.asInt = ((Integer) o).intValue();
    } else if (o instanceof Float) {
      targetType = val.type = DOUBLE;
      val.asDouble = ((Float) o).floatValue();
    } else if (o instanceof Double) {
      targetType = val.type = DOUBLE;
      val.asDouble = ((Double) o).doubleValue();
    } else if (o instanceof Long) {
      targetType = val.type = LONG;
      val.asLong = ((Long) o).longValue();
    } else {
      System.out.println("Invalid type in LDC: " + o);
    }
  }

  @Override
  public void exec() {
    stack[stackPtr].copyFrom(val);
  }

  @Override
  public String toString() {
    return super.toString() + "->" + val;
  }
}
