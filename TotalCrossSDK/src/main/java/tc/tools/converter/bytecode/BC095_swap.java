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

package tc.tools.converter.bytecode;

import tc.tools.converter.TCValue;

public class BC095_swap extends StackManipulation {
  private static TCValue vtemp = new TCValue();

  public BC095_swap() {
    super(0, false);
  }

  @Override
  public void exec() {
    vtemp.copyFrom(stack[stackPtr - 2]);
    stack[stackPtr - 2].copyFrom(stack[stackPtr - 1]);
    stack[stackPtr - 1].copyFrom(vtemp);
  }
}
