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

// $Id: BC192_checkcast.java,v 1.9 2011-01-04 13:18:57 guich Exp $

package tc.tools.converter.bytecode;

public class BC192_checkcast extends ByteCode
{
   public String targetClass, srcClass;

   public BC192_checkcast()
   {
      stackInc = 1;
      pcInc = 3;
      targetClass = cp.getString1(readUInt16(pc+1));
   }
   public void exec()
   {
      srcClass = (String)stack[stackPtr-1].asObj;
      if (srcClass == null)
         pcInc = 3;
      else
      {
         // check the instance
      }
   }
}
