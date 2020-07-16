// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC055_lstore extends StoreLocal {
  public BC055_lstore() {
    super(readUInt8(pc + 1), -2, LONG);
    pcInc = 2;
  }

  public BC055_lstore(boolean wide) {
    super(readUInt16(pc + 2), -2, LONG); // note: pc+1 stores the opcode
    pcInc = 3;
    bc = LSTORE;
  }
}
