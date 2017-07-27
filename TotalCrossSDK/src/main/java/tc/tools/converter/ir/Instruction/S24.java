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

import tc.tools.converter.TCConstants;
import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class S24 extends SingleInstruction
{
  public int s24;

  public S24(int op, int line, int v)
  {
    super(op, line);
    s24 = v;
  }

  public void set(int v)
  {
    s24 = v;
  }

  @Override
  public String toString()
  {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + s24;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode)
  {
    TCCode tc = new TCCode(opcode, line);
    tc.s24__desloc(s24);
    vcode.addElement(tc);
  }
}
