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

public class BC187_new extends Allocation
{
  public String className;
  public BC187_new()
  {
    className = cp.getString1(readUInt16(pc+1));
    stackInc = 1;
    pcInc = 3;
  }
  @Override
  public void exec()
  {
    stack[stackPtr].asObj = className; // should be an instance of this class, but we'll store the name instead
  }
}
