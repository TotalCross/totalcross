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

public class BC021_iload extends LoadLocal {
  public BC021_iload() {
    super(readUInt8(pc + 1), INT);
    pcInc = 2;
  }

  public BC021_iload(boolean wide) {
    super(readUInt16(pc + 2), INT); // note: pc+1 stores the opcode
    pcInc = 3;
    bc = ILOAD;
  }
}
