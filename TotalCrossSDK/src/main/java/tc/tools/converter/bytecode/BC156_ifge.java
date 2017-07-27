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

public class BC156_ifge extends ConditionalBranch
{
  public BC156_ifge()
  {
    super(-1,-1,0,readInt16(pc+1),INT);
  }
  @Override
  public void exec()
  {
    if (stack[stackPtr-1].asInt >= 0){
      pcInc = jumpIfTrue;
    }else {
      pcInc = jumpIfFalse;
    }
  }
}
