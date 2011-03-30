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

// $Id: OperandRegIc.java,v 1.11 2011-01-04 13:19:18 guich Exp $

package tc.tools.converter.oper;

public class OperandRegIc extends OperandRegI
{
   public OperandRegIc()
   {
      super();
      kind = opr_regIc;
   }

   public OperandRegIc(int framePosition)
   {
      super(opr_regIc, framePosition);
   }
}
