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



package tc.tools.converter.bytecode;

public class BC152_dcmpg extends Comparison
{
  public BC152_dcmpg()
  {
    super(-1,-2,-2,-1,DOUBLE);
  }
  @Override
  public void exec()
  {
    double r = stack[stackPtr-2].asDouble - stack[stackPtr-1].asDouble;
    stack[stackPtr-2].asInt = (r > 0)?1:(r < 0)?-1:0;
  }
}
