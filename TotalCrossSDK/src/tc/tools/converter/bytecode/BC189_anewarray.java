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

// $Id: BC189_anewarray.java,v 1.10 2011-01-04 13:18:54 guich Exp $

package tc.tools.converter.bytecode;

public class BC189_anewarray extends BC188_newarray
{
   public String classType;

   public BC189_anewarray()
   {
      super();
      arrayType = readUInt16(pc+1);
      classType = cp.getString1(arrayType);
      pcInc = 3;
   }
}
