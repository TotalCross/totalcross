/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: BC057_dstore.java,v 1.11 2011-01-04 13:18:54 guich Exp $

package tc.tools.converter.bytecode;

public class BC057_dstore extends StoreLocal
{
   public BC057_dstore()
   {
      super(readUInt8(pc+1),-2,DOUBLE);
      pcInc = 2;
   }
   public BC057_dstore(boolean wide)
   {
      super(readUInt16(pc+2),-2,DOUBLE); // note: pc+1 stores the opcode
      pcInc = 3;
      bc = DSTORE;
   }
}
