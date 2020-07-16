// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC057_dstore extends StoreLocal {
  public BC057_dstore() {
    super(readUInt8(pc + 1), -2, DOUBLE);
    pcInc = 2;
  }

  public BC057_dstore(boolean wide) {
    super(readUInt16(pc + 2), -2, DOUBLE); // note: pc+1 stores the opcode
    pcInc = 3;
    bc = DSTORE;
  }
}
