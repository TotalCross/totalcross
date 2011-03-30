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

// $Id: OperandConstant64.java,v 1.7 2011-01-04 13:19:19 guich Exp $

package tc.tools.converter.oper;

public class OperandConstant64 extends OperandConstant
{
   public OperandConstant64(long value, int type)
   {
      super(value, type);
      this.nWords = 2;
   }
}
