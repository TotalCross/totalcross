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

// $Id: OperandCmp.java,v 1.6 2011-01-04 13:19:19 guich Exp $

package tc.tools.converter.oper;

public class OperandCmp extends Operand
{
   public Operand v1, v2;

   public OperandCmp(Operand v1, Operand v2)
   {
      super(opr_cmp);
      this.v1 = v1;
      this.v2 = v2;
   }
}
