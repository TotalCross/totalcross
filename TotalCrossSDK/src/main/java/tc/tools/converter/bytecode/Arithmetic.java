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

public class Arithmetic extends ByteCode
{
  public int result, operand;

  public Arithmetic(int stackInc, int result, int operand, int type)
  {
    this.targetType = type;
    this.stackInc = stackInc;
    this.result = result;
    this.operand = operand;
  }
}
