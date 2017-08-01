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



package tc.tools.converter.oper;

public class OperandRegL extends OperandReg
{
  public OperandRegL()
  {
    super(opr_regL);
    nWords = 2;
  }

  public OperandRegL(int framePosition)
  {
    super(opr_regL, framePosition);
    nWords = 2;
  }

  public OperandRegL(String wordIndex, int index)
  {
    super(opr_regL, wordIndex, index);
    nWords = 2;
  }
}
