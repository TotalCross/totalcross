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

public class Parameter
{
  int line;

  public Parameter(int line)
  {
    this.line = line;
  }

  @Override
  public String toString()
  {
    return " ";
  }

  public void toTCCode(Vector vcode) {}
}
