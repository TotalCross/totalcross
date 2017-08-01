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

public class BC169_ret extends Return
{
  public BC169_ret()
  {
    super(0, readUInt8(pc+1),VOID);
    pcInc = 2;
  }
  public BC169_ret(boolean wide)
  {
    super(0, readUInt16(pc+2),VOID); // note: pc+1 stores the opcode
    pcInc = 3;
    bc = RET;
  }
  @Override
  public void exec()
  {
    pc = local[answer].asInt;
    pcInc = 0;
  }
}
