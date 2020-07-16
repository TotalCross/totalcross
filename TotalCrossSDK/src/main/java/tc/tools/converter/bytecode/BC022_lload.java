// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC022_lload extends LoadLocal {
  public BC022_lload() {
    super(readUInt8(pc + 1), LONG);
    pcInc = 2;
    stackInc = 2;
  }

  public BC022_lload(boolean wide) {
    super(readUInt16(pc + 2), LONG); // note: pc+1 stores the opcode
    pcInc = 3;
    stackInc = 2;
    bc = LLOAD;
  }
}
