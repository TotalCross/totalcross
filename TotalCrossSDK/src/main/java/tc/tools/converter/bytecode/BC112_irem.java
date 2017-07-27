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

public class BC112_irem extends Arithmetic
{
  public BC112_irem()
  {
    super(-1,-2,-1,INT);
  }
  @Override
  public void exec()
  {
    stack[stackPtr-2].asInt %= stack[stackPtr-1].asInt;
  }
}
