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

// $Id: BC021_iload.java,v 1.12 2011-01-04 13:18:56 guich Exp $

package tc.tools.converter.bytecode;

public class BC021_iload extends LoadLocal
{
   public BC021_iload()
   {
      super(readUInt8(pc+1),INT);
      pcInc = 2;
   }
   public BC021_iload(boolean wide)
   {
      super(readUInt16(pc+2),INT); // note: pc+1 stores the opcode
      pcInc = 3;
      bc = ILOAD;
   }
}
