/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.converter.ir.Instruction;

import totalcross.util.Vector;

public class MultiInstruction extends SingleInstruction
{
  public Parameter[] params;

  public MultiInstruction(int op, int line)
  {
    super(op, line);
  }

  @Override
  public void toTCCode(Vector vcode) { }
}
