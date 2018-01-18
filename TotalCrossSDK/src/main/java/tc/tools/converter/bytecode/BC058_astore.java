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
