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

// $Id: OperandRegIb.java,v 1.11 2011-01-04 13:19:19 guich Exp $

package tc.tools.converter.oper;

public class OperandRegIb extends OperandRegI
{
   public OperandRegIb()
   {
      super();
      kind = opr_regIb;
   }

   public OperandRegIb(int framePosition)
   {
      super(opr_regIb, framePosition);
   }
}
