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



package tc.tools.converter.ir.web;

import tc.tools.converter.ir.Instruction.Instruction;
import totalcross.util.Vector;

public class Web
{
  public int type; // opr_regI / opr_regO / opr_regD / opr_regL
  public int number;
  public Vector dChain = new Vector(32);
  public Vector uChain = new Vector(32);

  public Web(int t, int n)
  {
    type = t;
    number = n;
  }

  public void addDefinition(Instruction i)
  {
    dChain.addElement(i);
  }

  public void addUse(Instruction i)
  {
    uChain.addElement(i);
  }
}
