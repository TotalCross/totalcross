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

public class BC136_l2i extends Conversion
{
  public BC136_l2i()
  {
    super(-1,-1, LONG, INT);
  }
  @Override
  public void exec()
  {
    stack[stackPtr-1].asInt = (int)stack[stackPtr-1].asLong;
  }
}
