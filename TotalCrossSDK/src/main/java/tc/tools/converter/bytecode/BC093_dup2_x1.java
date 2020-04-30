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
package tc.tools.converter.bytecode;

public class BC093_dup2_x1 extends StackManipulation {
  public BC093_dup2_x1() {
    super(2, true);
  }

  @Override
  public void exec() {
    /*
         Form 1:
         ..., value3, value2, value1  ..., value2, value1, value3, value2, value1
         where value1, value2, and value3 are all values of a category 1 computational type (§3.11.1).
         Form 2:
         ..., value2, value1  ..., value1, value2, value1
         where value1 is a value of a category 2 computational type and value2 is a value of a category 1 computational type (�3.11.1).
    
         PS: form 2 NOT implemented
     */
    stack[stackPtr + 1].copyFrom(stack[stackPtr - 1]);
    stack[stackPtr].copyFrom(stack[stackPtr - 2]);
    stack[stackPtr - 1].copyFrom(stack[stackPtr - 3]);
    stack[stackPtr - 2].copyFrom(stack[stackPtr + 1]);
    stack[stackPtr - 3].copyFrom(stack[stackPtr]);
  }
}
