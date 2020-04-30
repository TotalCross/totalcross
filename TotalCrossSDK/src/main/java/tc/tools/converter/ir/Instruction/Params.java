// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.ir.Instruction;

import tc.tools.converter.TCConstants;
import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class Params extends Parameter implements TCConstants {
  public int param1 = -65;
  public int param2 = -65;
  public int param3 = -65;
  public int param4 = -65;

  public int typeOfParam1 = type_Constant;
  public int typeOfParam2 = type_Constant;
  public int typeOfParam3 = type_Constant;
  public int typeOfParam4 = type_Constant;

  public Params(int line, int p1, int p2, int p3, int p4) {
    super(line);
    param1 = p1;
    param2 = p2;
    param3 = p3;
    param4 = p4;
  }

  public Params(int line) {
    super(line);
  }

  public void set(int p1, int p2, int p3, int p4) {
    param1 = p1;
    param2 = p2;
    param3 = p3;
    param4 = p4;
  }

  @Override
  public String toString() {
    String print;
    print = "params: " + (typeOfParam1 == type_Constant ? "#" : "") + param1 + ", "
        + (typeOfParam2 == type_Constant ? "#" : "") + param2 + ", " + (typeOfParam3 == type_Constant ? "#" : "")
        + param3 + ", " + (typeOfParam4 == type_Constant ? "#" : "") + param4;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(line);
    tc.len = 0;
    tc.params__param1(typeOfParam1 != type_Constant ? param1 : param1 + 65);
    tc.params__param2(typeOfParam2 != type_Constant ? param2 : param2 + 65);
    tc.params__param3(typeOfParam3 != type_Constant ? param3 : param3 + 65);
    tc.params__param4(typeOfParam4 != type_Constant ? param4 : param4 + 65);
    vcode.addElement(tc);
  }
}
