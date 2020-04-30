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
package tc.tools.converter.regalloc;

import tc.tools.converter.ir.BitSet;

public class AdjListNode {
  public int color;
  public int reg;
  public boolean pushed;
  public BitSet adjs;
  public BitSet removedAdjs;

  public AdjListNode(int len) {
    color = -1;
    reg = -1;
    pushed = false;
    adjs = new BitSet(len);
    removedAdjs = new BitSet(len);
  }

  public AdjListNode(int len, int r) {
    color = -1;
    reg = r;
    pushed = false;
    adjs = new BitSet(len);
    removedAdjs = new BitSet(len);
  }

  public void addInterf(int interf) {
    adjs.on(interf);
  }

  public void removeInterf(int interf) {
    adjs.off(interf);
  }

  public void addRemovedAdjs(int n) {
    removedAdjs.on(n);
  }

  public void adjs2RemovedAdjs() {
    removedAdjs.on(adjs);
    adjs.clear();
  }

  public int nInts() {
    return adjs.count();
  }
}
