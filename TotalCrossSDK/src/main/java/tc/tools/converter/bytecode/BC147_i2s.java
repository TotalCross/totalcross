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

public class BC147_i2s extends Conversion // this conversion should be ignored
{
  public BC147_i2s()
  {
    super(-1,-1, INT, SHORT);
  }
  @Override
  public void exec()
  {
    stack[stackPtr-1].asInt = (short)stack[stackPtr-1].asInt;
  }
}
