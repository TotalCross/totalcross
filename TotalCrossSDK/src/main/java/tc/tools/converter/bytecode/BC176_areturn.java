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

public class BC176_areturn extends Return {
  public BC176_areturn() {
    super(1, -1, OBJECT);
  }

  @Override
  public void exec() {
    returnValue.asObj = stack[stackPtr - 1].asObj;
    returnValue.type = OBJECT;
  }
}
