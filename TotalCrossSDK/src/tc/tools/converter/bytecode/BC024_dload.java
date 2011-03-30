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

// $Id: BC024_dload.java,v 1.11 2011-01-04 13:18:56 guich Exp $

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
