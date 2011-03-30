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

// $Id: BC095_swap.java,v 1.9 2011-01-04 13:18:55 guich Exp $

package tc.tools.converter.bytecode;

import tc.tools.converter.*;

public class BC095_swap extends StackManipulation
{
   private static TCValue vtemp = new TCValue();
   public BC095_swap()
   {
      super(0,false);
   }
   public void exec()
   {
      vtemp.copyFrom(stack[stackPtr-2]);
      stack[stackPtr-2].copyFrom(stack[stackPtr-1]);
      stack[stackPtr-1].copyFrom(vtemp);
   }
}
