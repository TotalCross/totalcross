// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.regalloc;

import tc.tools.converter.TCValue;
import totalcross.util.Vector;

public class Stack {
  public Vector elems = new Vector(8);

  public void push(int e) {
    elems.addElement(new TCValue(e));
  }

  public int pop() {
    TCValue v = (TCValue) elems.items[elems.size() - 1];
    elems.removeElementAt(elems.size() - 1);
    return v.asInt;
  }

  public boolean isEmpty() {
    return (elems.size() == 0);
  }

  public void erase() {
    elems.removeAllElements();
  }
}
