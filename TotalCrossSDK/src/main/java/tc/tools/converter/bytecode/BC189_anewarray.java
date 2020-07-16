// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC189_anewarray extends BC188_newarray {
  public String classType;

  public BC189_anewarray() {
    super();
    arrayType = readUInt16(pc + 1);
    classType = cp.getString1(arrayType);
    pcInc = 3;
  }
}
