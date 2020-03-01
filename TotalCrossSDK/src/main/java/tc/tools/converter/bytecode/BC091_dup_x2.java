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

public class BC091_dup_x2 extends StackManipulation {
  public BC091_dup_x2() {
    super(1, false);
  }

  @Override
  public void exec() {
    stack[stackPtr].copyFrom(stack[stackPtr - 1]);
    stack[stackPtr - 1].copyFrom(stack[stackPtr - 2]);
    stack[stackPtr - 2].copyFrom(stack[stackPtr - 3]);
    stack[stackPtr - 3].copyFrom(stack[stackPtr]);
  }
}
