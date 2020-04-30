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

import tc.tools.converter.ConverterException;
import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.TCConstants;
import tc.tools.converter.ir.BasicBlock;
import tc.tools.converter.ir.BitSet;
import tc.tools.converter.ir.CFG;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.tclass.TCException;
import tc.tools.converter.tclass.TCMethod;
import tc.tools.converter.tclass.TClassConstants;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;

public class RegAllocation {
  public BitSet[] adjMatrix;
  public AdjListNode[] adjList;
  public Stack stack;

  public AdjListNode[] adjListI;
  public AdjListNode[] adjListD;
  public AdjListNode[] adjListO;

  public int maxColor;
  public int maxColorI, maxColorD, maxColorO;

  public static void makeRegAllocation(TCMethod[] methods) {
    int len = methods.length;
    for (int i = 0; i < len; i++) {
      if (methods[i] != null) {
        new RegAllocation(methods[i].cfg);
      }
    }
  }

  private RegAllocation(CFG cfg) {
    J2TC.currentMethod = GlobalConstantPool.getMethodFieldName(cfg.method.cpName);
    adjListI = null;
    adjListD = null;
    adjListO = null;

    maxColorI = 0;
    maxColorD = 0;
    maxColorO = 0;

    if (cfg.iCount() > 64) {
      stack = new Stack();
      cfg.livenessAnalysis(TClassConstants.RegI);
      maxColor = 0;
      if (allocateRegisters(cfg, TClassConstants.RegI)) {
        adjListI = createCopyOf(adjList);
        maxColorI = maxColor + 1;
      } else {
        throw new ConverterException(
            "The maximum number of integer registers has been reached. Please split your method and try again.");
      }
      cleanData();
    }

    if (cfg.v64Count() > 64) {
      stack = new Stack();
      cfg.livenessAnalysis(TClassConstants.RegD);
      maxColor = 0;
      if (allocateRegisters(cfg, TClassConstants.RegD)) {
        adjListD = createCopyOf(adjList);
        maxColorD = maxColor + 1;
      } else {
        throw new ConverterException(
            "The maximum number of floating registers has been reached. Please split your method and try again.");
      }
      cleanData();
    }

    if (cfg.oCount() > 64) {
      stack = new Stack();
      cfg.livenessAnalysis(TClassConstants.RegO);
      maxColor = 0;
      if (allocateRegisters(cfg, TClassConstants.RegO)) {
        adjListO = createCopyOf(adjList);
        maxColorO = maxColor + 1;
      } else {
        throw new ConverterException(
            "The maximum number of object registers has been reached. Please split your method and try again.");
      }
    }

    if (adjListI == null) {
      adjListI = defaultAdjList();
    }
    if (adjListD == null) {
      adjListD = defaultAdjList();
    }
    if (adjListO == null) {
      adjListO = defaultAdjList();
    }

    cfg.method.modifyCode(adjListI, adjListD, adjListO);
    updateRegOExceptionHandler(cfg.method.exceptionHandlers, adjListO);

    cfg.method.iCount = Math.max(maxColorI, cfg.method.iParamCount);
    cfg.method.v64Count = Math.max(maxColorD, cfg.method.v64ParamCount);
    cfg.method.oCount = Math.max(maxColorO, cfg.method.oParamCount);
    J2TC.currentMethod = null;
  }

  public void updateRegOExceptionHandler(TCException[] tces, AdjListNode[] adjList) {
    TCException e;
    if (tces != null) {
      for (int i = 0; i < tces.length; i++) {
        e = tces[i];
        e.regO = adjList[e.regO].color;
      }
    }
  }

  private AdjListNode[] defaultAdjList() {
    AdjListNode[] v = new AdjListNode[TCConstants.PHYSICALREGCOUNT];
    for (int i = 0; i < TCConstants.PHYSICALREGCOUNT; i++) {
      v[i] = new AdjListNode(1);
      v[i].color = i;
    }

    return v;
  }

  private AdjListNode[] createCopyOf(AdjListNode[] list) {
    if (list != null && list.length > 0) {
      AdjListNode[] v = new AdjListNode[list.length];
      Vm.arrayCopy(list, 0, v, 0, v.length);
      return v;
    }
    return null;
  }

  public boolean allocateRegisters(CFG cfg, int regType) {
    buildAdjMtx(cfg, regType);
    buildAdjList(cfg, regType);
    switch (regType) {
    case TClassConstants.RegI:
      pruneGraph(cfg.iCount());
      break;
    case TClassConstants.RegD:
      pruneGraph(cfg.v64Count());
      break;
    case TClassConstants.RegO:
      pruneGraph(cfg.oCount());
      break;
    }
    return assignRegs();
  }

  public void buildAdjMtx(CFG cfg, int regType) {
    switch (regType) {
    case TClassConstants.RegI:
      createMatrix(cfg.iCount());
      break;
    case TClassConstants.RegD:
      createMatrix(cfg.v64Count());
      break;
    case TClassConstants.RegO:
      createMatrix(cfg.oCount());
      /*reserveRegOZero();*/ break;
    }

    for (int i = 0; i < cfg.order.length; i++) {
      BasicBlock bb = cfg.DFSOrderNode(i);
      Instruction[] insts = bb.vector2Array();
      if (insts != null) {
        for (int j = 0; j < insts.length; j++) {
          BitSet[] ud = insts[j].defsAndUses(bb, cfg.method.iCount, cfg.method.v64Count, cfg.method.oCount);
          BitSet def = null;
          switch (regType) {
          case TClassConstants.RegI:
            def = ud[3];
            break;
          case TClassConstants.RegD:
            def = ud[4];
            break;
          case TClassConstants.RegO:
            def = ud[5];
          }
          makeInterferences(def.first(), insts[j]);
        }
      }
    }
  }

  public void buildAdjList(CFG cfg, int regType) {
    int len = 0;
    switch (regType) {
    case TClassConstants.RegI:
      len = cfg.iCount();
      break;
    case TClassConstants.RegD:
      len = cfg.v64Count();
      break;
    case TClassConstants.RegO:
      len = cfg.oCount();
      break;
    }
    createAdjList(len);
    for (int i = 0; i < len; i++) {
      adjList[i] = new AdjListNode(len);
      if (i < TCConstants.PHYSICALREGCOUNT) {
        adjList[i].color = i;
      }
    }

    for (int i = 0; i < len; i++) {
      for (int j = 0; j < i; j++) {
        if (adjMatrix[i].isOn(j)) {
          adjList[i].addInterf(j);
          adjList[j].addInterf(i);
        }
      }
    }
  }

  public static Hashtable liveUses = new Hashtable(31);

  private void makeInterferences(int regDef, Instruction inst) {
    if (regDef != -1) {
      BitSet use = (BitSet) liveUses.get(inst);
      if (use.elems != null) {
        int k = use.first(), in = use.nBits;
        if (k != -1) {
          for (; k < in; k++) {
            if (use.isOn(k)) {
              int regLive = k;
              if (regDef != regLive) {
                adjMatrix[Math.max(regDef, regLive)].on(Math.min(regDef, regLive));
              }
            }
          }
        }
      }
    }
  }

  private void createMatrix(int length) {
    adjMatrix = new BitSet[length];
    for (int i = 0; i < length; i++) {
      adjMatrix[i] = new BitSet(i + 1);
    }

    for (int l = 0; l < TCConstants.PHYSICALREGCOUNT; l++) {
      adjMatrix[l].set(0, l);
    }
  }

  public void reserveRegOZero() {
    for (int i = TCConstants.PHYSICALREGCOUNT; i < adjMatrix.length; i++) {
      adjMatrix[i].on(0);
    }
  }

  public void createAdjList(int len) {
    adjList = new AdjListNode[len];
  }

  public void pruneGraph(int len) {
    int nodes = len - TCConstants.PHYSICALREGCOUNT;
    boolean sucess = false;
    while (!sucess) {
      sucess = true;
      for (int i = TCConstants.PHYSICALREGCOUNT; i < len; i++) {
        if (!adjList[i].pushed && adjList[i].nInts() < TCConstants.PHYSICALREGCOUNT) {
          sucess = false;
          stack.push(i);
          adjustNeighbors(i);
          adjList[i].pushed = true;
          nodes--;
        }
      }
    }

    if (nodes > 0) {
      throw new ConverterException("This method is too big and must be splitted into two or more parts.");
      /*for (int l=0; l<cfg.iCount(); l++)
         {
            System.out.print("\n"+l+".");
            for (int c=0; c<l; c++)
            {
               System.out.print(adjMatrix[l].isOn(c) ? "1 " : "0 ");
            }
         }
         System.out.println("\nfim");*/
    }
  }

  public void adjustNeighbors(int n) {
    BitSet neighbors = adjList[n].adjs;
    if (neighbors.elems != null) {
      int i = neighbors.first(), in = neighbors.nBits;
      if (i != -1) {
        for (; i < in; i++) {
          if (neighbors.isOn(i)) {
            adjList[i].removeInterf(n);
            adjList[i].addRemovedAdjs(n);
          }
        }
      }
      adjList[n].adjs2RemovedAdjs();
    }
  }

  public boolean assignRegs() {
    boolean sucess = true;

    while (!stack.isEmpty()) {
      int r = stack.pop();
      int c = minColor(r, TCConstants.PHYSICALREGCOUNT);
      if (c > maxColor) {
        maxColor = c;
      }
      if (c != -1) {
        adjList[r].color = c;
      } else {
        sucess = false;
        break;
      }
    }

    return sucess;
  }

  public int minColor(int n, int count) {
    BitSet minColor = new BitSet(count);
    minColor.set(0, count);
    BitSet nodes = adjList[n].removedAdjs;
    if (nodes.elems != null) {
      int i = nodes.first(), in = nodes.nBits;
      if (i != -1) {
        for (; i < in; i++) {
          if (nodes.isOn(i)) {
            int color = adjList[i].color;
            if (color != -1) {
              minColor.off(color);
            }
          }
        }
      }
    }

    if (adjList[n].color != -1 && !minColor.isOn(adjList[n].color)) {
      minColor.clear();
    }

    return minColor.first();
  }

  public void cleanData() {
    deleteMatrix();
    deleteAdjList();
    stack.erase();
    liveUses.clear();
  }

  public void deleteMatrix() {
    for (int l = 0; l < adjMatrix.length; l++) {
      adjMatrix[l].clear();
    }
  }

  public void deleteAdjList() {
    Convert.fill(adjList, 0, adjList.length, null);
  }
}
