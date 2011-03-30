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

// $Id: Branch.java,v 1.11 2011-01-04 13:18:56 guich Exp $

package tc.tools.converter.bytecode;

public class Branch extends ByteCode
{
   public int jumpTo;

   public Branch(int stackInc, int jumpTo)
   {
      super.stackInc = stackInc;
      this.jumpTo = jumpTo + pcInMethod;
   }

   public String toString()
   {
      return super.toString()+"->"+jumpTo;
   }
}
