/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

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
