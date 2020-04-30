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

public class BC191_athrow extends ByteCode {
  public int oldpc;
  public Object thrownException;

  public BC191_athrow() {
    oldpc = pc;
    stackInc = 1;
  }

  @Override
  public void exec() {
    Object o = stack[stackPtr - 1].asObj;
    if (o == null) {
      stackInc = -1;
      thrownException = "java.lang.NullPointerException";
    } else {
      thrownException = (String) o;
      // stack = local;
      // stack += method->maxLocals;
      // stack[stackPtr].asObj = thrownException
    }
  }
}
