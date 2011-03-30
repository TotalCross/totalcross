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

// $Id: OperandRegO.java,v 1.12 2011-01-04 13:19:19 guich Exp $

package tc.tools.converter.oper;

public class OperandRegO extends OperandReg
{
   public OperandRegO()
   {
      super(opr_regO);
   }

   public OperandRegO(int framePosition)
   {
      super(opr_regO, framePosition);
   }

   public OperandRegO(String wordIndex, int index)
   {
      super(opr_regO, wordIndex, index);
   }
}
