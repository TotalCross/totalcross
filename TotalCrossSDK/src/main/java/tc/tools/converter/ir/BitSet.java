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
package tc.tools.converter.ir;

import totalcross.sys.Vm;

final public class BitSet {
  public byte[] elems;
  public int nBits;

  public BitSet(int len) {
    nBits = len;
  }

  private void allocate() {
    elems = new byte[(nBits >> 3) + 1];
  }

  public BitSet(BitSet bits) {
    this(bits.nBits);
    if (bits.elems != null) {
      allocate();
      Vm.arrayCopy(bits.elems, 0, elems, 0, elems.length);
    }
  }

  public void on(int n) {
    if (elems == null) {
      allocate();
    }
    elems[n >> 3] |= ((int) 1 << (n & 7)); // set
  }

  /** All bits that are on at source is made on in destination. */
  public BitSet on(BitSet bits) {
    if (elems == null) {
      allocate();
    }
    if (bits.elems == null) {
      bits.allocate();
    }
    byte[] elems2 = bits.elems;
    for (int i = 0; i < elems2.length; i++) {
      elems[i] |= elems2[i];
    }
    return this;
  }

  public void off(int n) {
    if (elems != null) {
      elems[n >> 3] &= ~((int) 1 << (n & 7)); // reset
    }
  }

  /** Every bit that's on at source is made off at destination */
  public BitSet off(BitSet bits) {
    /* guich. Original code:
      for (int i=bits.nBits()-1; i >= 0; i--)
         if (bits.isOn(i))
            this.off(i);
    
       Truth table:
    
       bits this result
         0    0    0
         0    1    1
         1    0    0
         1    1    0
    
       same of ~x & y
    
     */
    if (bits.elems == null && elems == null) {
      return this;
    }
    if (elems == null) {
      allocate();
    }
    if (bits.elems == null) {
      bits.allocate();
    }

    byte[] elems2 = bits.elems;
    for (int i = 0; i < elems2.length; i++) {
      elems[i] &= ~elems2[i];
    }
    return this;
  }

  public boolean isOn(int n) {
    return elems != null && (elems[n >> 3] & ((int) 1 << (n & 7))) != 0; // guich@321_7
  }

  public void clear() {
    if (elems != null) {
      java.util.Arrays.fill(elems, (byte) 0);
    }
  }

  private static StringBuffer sbuf = new StringBuffer(1024);

  @Override
  public String toString() {
    if (elems == null) {
      return "all bits are off";
    }
    StringBuffer sb = sbuf;
    sb.setLength(0);
    for (int i = 0, n = nBits; i < n; i++) {
      if (isOn(i)) {
        sb.append(i).append(' ');
      }
    }
    return sb.toString();
  }

  public boolean equals(BitSet bits) {
    return java.util.Arrays.equals(bits.elems, this.elems);
  }

  public int getBitOn(int order) {
    int k = 0;
    if (elems != null) {
      for (int i = 0, n = nBits; i < n; i++) {
        if (isOn(i) && k++ == order) {
          return i;
        }
      }
    }
    return -1;
  }

  static final int nbits[] = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4 };

  public int count() {
    if (elems == null) {
      return 0;
    }
    int c = 0;
    for (int i = elems.length; --i >= 0;) {
      int v = elems[i];
      c += nbits[v & 0xF];
      v >>= 4;
      c += nbits[v & 0xF];
    }
    return c;
  }

  public void set(int from, int to) {
    for (int i = from; i < to; i++) {
      on(i);
    }
  }

  public int first() {
    if (elems == null) {
      return -1;
    }
    int i = 0, n = nBits;
    for (; i < n; i += 8) {
      if (elems[i >> 3] != 0) {
        break;
      }
    }
    for (; i < n; i++) {
      if (isOn(i)) {
        return i;
      }
    }
    return -1;
  }

  public Object bits() {
    return elems;
  }
}
