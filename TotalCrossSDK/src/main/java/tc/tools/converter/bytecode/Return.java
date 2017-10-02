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

import tc.tools.converter.TCValue;

public class Return extends ByteCode {
  public int returnValueCount;
  public int answer;
  public TCValue returnValue = new TCValue();

  public Return(int returnValueCount, int answer, int type) {
    this.returnValueCount = returnValueCount;
    this.targetType = type;
    this.answer = answer;
    stackInc = 0;
  }
}
