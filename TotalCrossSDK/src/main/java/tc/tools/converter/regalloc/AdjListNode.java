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
