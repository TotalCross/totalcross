// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC058_astore extends StoreLocal {
  public BC058_astore() {
    super(readUInt8(pc + 1), -1, OBJECT);
    pcInc = 2;
  }

  public BC058_astore(boolean wide) {
    super(readUInt16(pc + 2), -1, OBJECT); // note: pc+1 stores the opcode
    pcInc = 3;
    bc = ASTORE;
  }
}
