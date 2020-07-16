// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC054_istore extends StoreLocal {
  public BC054_istore() {
    super(readUInt8(pc + 1), -1, INT);
    pcInc = 2;
  }

  public BC054_istore(boolean wide) {
    super(readUInt16(pc + 2), -1, INT); // note: pc+1 stores the opcode
    pcInc = 3;
    bc = ISTORE;
  }
}
