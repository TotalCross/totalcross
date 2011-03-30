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

// $Id: MultiInstruction.java,v 1.9 2011-01-04 13:19:06 guich Exp $

package tc.tools.converter.ir.Instruction;

import totalcross.util.Vector;

public class MultiInstruction extends SingleInstruction
{
   public Parameter[] params;

   public MultiInstruction(int op, int line)
   {
      super(op, line);
   }

   public void toTCCode(Vector vcode) { }
}
