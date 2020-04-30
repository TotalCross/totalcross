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
package tc.tools.converter.oper;

import tc.tools.converter.TCConstants;

public class OperandStack implements TCConstants {
  private int sp = -1;
  private Operand elems[];

  public OperandStack(int length) {
    elems = new Operand[length];
  }

  public void push(Operand op) {
    elems[++sp] = op;
  }

  public Operand pop() {
    // We assume that the stack is being correctly managed.
    // Therefore, pop is not called when the stack is empty.
    //printStack(1);
    return elems[sp--];
  }

  public Operand top() {
    return elems[sp];
  }

  public int count() {
    return (sp + 1);
  }

  public boolean empty() {
    return sp == -1;
  }

  public void printStack(int x) {
    System.out.print("    stack: ");
    for (int i = 0; i <= sp - x; i++) {
      System.out.print(elems[i].kind + " ");
    }
    System.out.println("");
  }

  public OperandStack cloneStack() {
    OperandStack clone = new OperandStack(256);
    for (int i = 0; i <= sp; i++) {
      clone.push(elems[i]);
    }
    return clone;
  }

  public void copy(OperandStack s) {
    clear();
    for (int i = 0; i <= s.sp; i++) {
      push(s.elems[i]);
    }
  }

  public void invert() {
    OperandStack clone = this.cloneStack();
    this.clear();
    while (!clone.empty()) {
      this.push(clone.pop());
    }
  }

  public void clear() {
    sp = -1;
  }

  private OperandReg lookupOperandReg(int index, int kind) {
    for (int i = 0; i <= sp; i++) {
      if (elems[i] instanceof OperandReg && (elems[i].kind == kind || elems[i].kind == opr_regD && kind == opr_regL)) {
        OperandReg r = (OperandReg) elems[i];
        if (r.index == index) {
          return r;
        }
      }
    }
    return null;
  }

  public OperandRegI lookupOperandRegI(int index) {
    return (OperandRegI) lookupOperandReg(index, opr_regI);
  }

  public OperandRegL lookupOperandRegL(int index) {
    return (OperandRegL) lookupOperandReg(index, opr_regL);
  }
}
