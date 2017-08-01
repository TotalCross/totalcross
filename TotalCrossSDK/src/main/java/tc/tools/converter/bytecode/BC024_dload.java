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

public class BC024_dload extends LoadLocal
{
  public BC024_dload()
  {
    super(readUInt8(pc+1),DOUBLE);
    pcInc = 2;
    stackInc = 2;
  }
  public BC024_dload(boolean wide)
  {
    super(readUInt16(pc+2),DOUBLE); // note: pc+1 stores the opcode
    pcInc = 2;
    stackInc = 2;
    bc = DLOAD;
  }
}
