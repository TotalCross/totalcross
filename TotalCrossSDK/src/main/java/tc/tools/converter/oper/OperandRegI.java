/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.              *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package tc.tools.converter.oper;

public class OperandRegI extends OperandReg {
  public OperandRegI() {
    super(opr_regI);
  }

  public OperandRegI(int framePosition) {
    super(opr_regI, framePosition);
  }

  public OperandRegI(int kind, int framePosition) // kind: regIb | regIs | regIc
  {
    super(kind, framePosition);
  }

  public OperandRegI(String wordIndex, int index) {
    super(opr_regI, wordIndex, index);
  }
}
