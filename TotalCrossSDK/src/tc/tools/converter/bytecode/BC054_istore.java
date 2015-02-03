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

public class BC054_istore extends StoreLocal
{
   public BC054_istore()
   {
      super(readUInt8(pc+1),-1,INT);
      pcInc = 2;
   }
   public BC054_istore(boolean wide)
   {
      super(readUInt16(pc+2),-1,INT); // note: pc+1 stores the opcode
      pcInc = 3;
      bc = ISTORE;
   }
}
