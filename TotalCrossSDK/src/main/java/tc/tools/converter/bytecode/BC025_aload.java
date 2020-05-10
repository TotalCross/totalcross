// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC025_aload extends LoadLocal {
  public BC025_aload() {
    super(readUInt8(pc + 1), OBJECT);
    pcInc = 2;
  }

  public BC025_aload(boolean wide) {
    super(readUInt16(pc + 2), OBJECT); // note: pc+1 stores the opcode
    pcInc = 3;
    bc = ALOAD;
  }
}
