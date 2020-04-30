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

import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.regalloc.RegAllocation;
import tc.tools.converter.tclass.TClassConstants;
import totalcross.util.Vector;

final public class BasicBlock {
  public int number;
  public static int nextNumber;
  public String name;

  // the CFG of this basic block
  public CFG cfg;
  // the first instruction
  public Instruction first;
  // the last instruction
  public Instruction last;

  public BasicBlock[] bbOfCases;

  public Instruction instLeft;
  public Instruction instRigth;
  public Instruction[] instCases;

  public Vector pred;
  public Vector succ;

  public BitSet useI; // (I) int
  public BitSet defI;
  public BitSet useD; // (D) double, float and long*
  public BitSet defD;
  public BitSet useO; // (O) object
  public BitSet defO;

  public BitSet inI; // (I) int
  public BitSet outI;
  public BitSet inD; // (D) double, float and long*
  public BitSet outD;
  public BitSet inO; // (O) object
  public BitSet outO;

  public static void init() {
    nextNumber = 0;
  }

  public BasicBlock(String n, CFG cfg, Instruction f, Instruction l, Instruction lf, Instruction rt) {
    number = nextNumber++;
    name = n;
    this.cfg = cfg;
    first = f;
    last = l;
    bbOfCases = null;
    instLeft = lf;
    instRigth = rt;
    instCases = null;
    pred = new Vector(2);
    succ = new Vector(2);
    useI = new BitSet(cfg.method.iCount);
    defI = new BitSet(cfg.method.iCount);
    useD = new BitSet(cfg.method.v64Count);
    defD = new BitSet(cfg.method.v64Count);
    useO = new BitSet(cfg.method.oCount);
    defO = new BitSet(cfg.method.oCount);

    inI = new BitSet(cfg.method.iCount);
    outI = new BitSet(cfg.method.iCount);
    inD = new BitSet(cfg.method.v64Count);
    outD = new BitSet(cfg.method.v64Count);
    inO = new BitSet(cfg.method.oCount);
    outO = new BitSet(cfg.method.oCount);

  }

  public Instruction[] vector2Array() {
    Vector instructions = cfg.method.insts;
    if (instructions != null) {
      int i = 0;
      while ((Instruction) instructions.items[i] != first) {
        i++;
      }
      int startIndex = i;
      while ((Instruction) instructions.items[i] != last) {
        i++;
      }
      int endIndex = i;
      Instruction[] ret = new Instruction[endIndex - startIndex + 1];
      int k = 0;
      for (i = startIndex; i <= endIndex; i++) {
        ret[k++] = (Instruction) instructions.items[i];
      }
      return ret;
    }

    return null;
  }

  public void InitializeDefsAndUses() {
    useI.clear();
    defI.clear();
    useD.clear();
    defD.clear();
    useO.clear();
    defO.clear();

    Instruction[] block = vector2Array();

    for (int k = block.length - 1; k >= 0; k--) {
      Instruction i = block[k];
      BitSet[] ud = i.defsAndUses(this, cfg.method.iCount, cfg.method.v64Count, cfg.method.oCount);

      BitSet uI = ud[0];
      BitSet uD = ud[1];
      BitSet uO = ud[2];
      BitSet dI = ud[3];
      BitSet dD = ud[4];
      BitSet dO = ud[5];

      useI.off(dI);
      useD.off(dD);
      useO.off(dO);

      useI.on(uI);
      useD.on(uD);
      useO.on(uO);

      defI.off(uI);
      defD.off(uD);
      defO.off(uO);

      defI.on(dI);
      defD.on(dD);
      defO.on(dO);
    }
  }

  public void computeLiveUsesAtInstr(int regType) {
    BitSet currout = null;

    switch (regType) {
    case TClassConstants.RegI:
      currout = outI;
      break;
    case TClassConstants.RegD:
      currout = outD;
      break;
    case TClassConstants.RegO:
      currout = outO;
    }

    Instruction[] block = vector2Array();
    for (int k = block.length - 1; k >= 0; k--) {
      Instruction i = block[k];
      BitSet bset = (BitSet) RegAllocation.liveUses.get(i);
      if (bset != null) {
        currout.on(bset);
      }
      RegAllocation.liveUses.put(i, new BitSet(currout));
      BitSet[] ud = i.defsAndUses(this, cfg.method.iCount, cfg.method.v64Count, cfg.method.oCount);
      BitSet u = null;
      BitSet d = null;
      switch (regType) {
      case TClassConstants.RegI:
        u = ud[0];
        d = ud[3];
        break;
      case TClassConstants.RegD:
        u = ud[1];
        d = ud[4];
        break;
      case TClassConstants.RegO:
        u = ud[2];
        d = ud[5];
        break;
      }

      currout = new BitSet(currout);
      currout.off(d);
      currout.on(u);
    }
  }

  public void addPred(BasicBlock b) {
    for (int i = 0; i < pred.size(); i++) {
      BasicBlock bb = (BasicBlock) pred.items[i];
      if (bb == b) {
        return;
      }
    }
    pred.addElement(b);
  }

  public void addSucc(BasicBlock b) {
    for (int i = 0; i < succ.size(); i++) {
      BasicBlock bb = (BasicBlock) succ.items[i];
      if (bb == b) {
        return;
      }
    }
    succ.addElement(b);
  }

  public void print() {
    System.out.println("\nReference: " + this);
    System.out.println("\nnumber: " + number);
    System.out.println("name: " + name);
    System.out.println("first: " + first);
    System.out.println("last: " + last);
    System.out.println("instLeft: " + instLeft);
    System.out.println("instRigth: " + instRigth);
    System.out.println("instCases: ");
    if (instCases != null) {
      for (int i = 0; i < instCases.length; i++) {
        System.out.print(instCases.toString());
      }
    }
    System.out.print("basic blocks of the cases: ");
    if (bbOfCases != null) {
      for (int i = 0; i < bbOfCases.length; i++) {
        System.out.print(bbOfCases[i].number + ", ");
      }
    }

    System.out.print("\nsuccessors: ");
    for (int i = 0; i < succ.size(); i++) {
      BasicBlock b = (BasicBlock) succ.items[i];
      System.out.print(b.number + ", ");
    }

    System.out.print("\npredecessors: ");
    for (int i = 0; i < pred.size(); i++) {
      BasicBlock b = (BasicBlock) pred.items[i];
      System.out.print(b.number + ", ");
    }

    System.out.print("\nusesI: " + useI.toString());
    System.out.print("\nusesD: " + useD.toString());
    System.out.print("\nusesO: " + useO.toString());
    System.out.print("\ninI: " + inI.toString());
    System.out.print("\ninD: " + inD.toString());
    System.out.print("\ninO: " + inO.toString());
    System.out.print("\noutI: " + outI.toString());
    System.out.print("\noutD: " + outD.toString());
    System.out.print("\noutO: " + outO.toString());
  }
}
