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

// $Id: OperandSymD64.java,v 1.6 2011-01-04 13:19:18 guich Exp $

package tc.tools.converter.oper;

public class OperandSymD64 extends OperandSymD
{
   public OperandSymD64(int index)
   {
      super(index);
      nWords = 2;
   }
}
