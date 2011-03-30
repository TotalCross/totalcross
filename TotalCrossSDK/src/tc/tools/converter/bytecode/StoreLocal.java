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

// $Id: StoreLocal.java,v 1.9 2011-01-04 13:18:54 guich Exp $

package tc.tools.converter.bytecode;

public class StoreLocal extends ByteCode
{
   /** Index in the local array */
   public int targetIdx, srcStack;

   public StoreLocal(int targetIdx, int srcStack, int type)
   {
      this.targetIdx = targetIdx;
      this.srcStack = stackInc = srcStack;
      this.targetType = type;
   }
   public void exec()
   {
      local[targetIdx].copyFrom(stack[stackPtr+srcStack]);
   }
}
