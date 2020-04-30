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

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.TCConstants;
import tc.tools.converter.TCValue;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.ir.Instruction.Reg_reg_s12;
import tc.tools.converter.ir.Instruction.Reg_s6_desloc;
import tc.tools.converter.ir.Instruction.Reg_sym_sdesloc;
import tc.tools.converter.ir.Instruction.S24;
import tc.tools.converter.ir.Instruction.Switch_reg;
import tc.tools.converter.ir.Instruction.Two16;
import tc.tools.converter.tclass.TCException;
import tc.tools.converter.tclass.TCMethod;
import totalcross.util.Vector;

/**
 * @author SuperWaba
 *
 */
final public class CFG implements TCConstants {
  // the method of this CFG
  public TCMethod method;
  // the list of instructions
  public Vector instructions;
  // the list of basic blocks
  public BasicBlock[] blocks;
  // the topological order
  public int order[];

  public CFG(TCMethod mt) {
    method = mt;
    BasicBlock.init();
    Vector methodCode = method.insts;
    instructions = methodCode;
    if (methodCode != null) {
      Vector v = new Vector(64);
      int len = methodCode.size();
      Instruction first = (Instruction) methodCode.items[0];
      Instruction tc = first;
      for (int i = 0, ind = 0; i < len; i++) {
        tc = (Instruction) methodCode.items[i];
        int op = tc.opcode;
        switch (op) {
        case SWITCH: {
          Switch_reg inst = (Switch_reg) tc;
          BasicBlock bb = new BasicBlock("SWITCH", this, first, tc, null, null);
          bb.instCases = new Instruction[inst.n + 1];
          int target = ((Two16) inst.params[0]).v1;
          bb.instCases[0] = getInstruction(methodCode, ind + target);
          for (int k = 0, m = inst.n + 1; k < inst.n; k++) {
            target = 0;
            if ((k & 1) == 1) {
              target = ((Two16) inst.params[m++]).v2;
            } else {
              target = ((Two16) inst.params[m]).v1;
            }
            bb.instCases[k + 1] = getInstruction(methodCode, ind + target);
          }
          first = getInstruction(methodCode, ind + inst.len);
          v.addElement(bb);
          break;
        }
        case JEQ_regO_regO:
        case JNE_regO_regO:
        case JEQ_regI_regI:
        case JNE_regI_regI:
        case JLT_regI_regI:
        case JLE_regI_regI:
        case JGT_regI_regI:
        case JGE_regI_regI:
        case JEQ_regL_regL:
        case JNE_regL_regL:
        case JLT_regL_regL:
        case JLE_regL_regL:
        case JGT_regL_regL:
        case JGE_regL_regL:
        case JEQ_regD_regD:
        case JNE_regD_regD:
        case JLT_regD_regD:
        case JLE_regD_regD:
        case JGT_regD_regD:
        case JGE_regD_regD: {
          Reg_reg_s12 inst = (Reg_reg_s12) tc;
          int branch = inst.s12;
          Instruction instLeft = getInstruction(methodCode, ind + branch);
          Instruction instRigth = getInstruction(methodCode, ind + 1);
          BasicBlock bb = new BasicBlock("NORMAL", this, first, tc, instLeft, instRigth);
          first = instRigth;
          v.addElement(bb);
          break;
        }

        case JEQ_regI_s6:
        case JNE_regI_s6:
        case JLT_regI_s6:
        case JLE_regI_s6:
        case JGT_regI_s6:
        case JGE_regI_s6:
        case JEQ_regO_null:
        case JNE_regO_null: {
          Reg_s6_desloc inst = (Reg_s6_desloc) tc;
          int branch = inst.desloc;
          Instruction instLeft = getInstruction(methodCode, ind + branch);
          Instruction instRigth = getInstruction(methodCode, ind + 1);
          BasicBlock bb = new BasicBlock("NORMAL", this, first, tc, instLeft, instRigth);
          first = instRigth;
          v.addElement(bb);
          break;
        }

        case JEQ_regI_sym:
        case JNE_regI_sym:
        case JGE_regI_arlen: {
          Reg_sym_sdesloc inst = (Reg_sym_sdesloc) tc;
          int branch = inst.desloc;
          Instruction instLeft = getInstruction(methodCode, ind + branch);
          Instruction instRigth = getInstruction(methodCode, ind + 1);
          BasicBlock bb = new BasicBlock("NORMAL", this, first, tc, instLeft, instRigth);
          first = instRigth;
          v.addElement(bb);
          break;
        }

        case JUMP_s24: {
          S24 inst = (S24) tc;
          int branch = inst.s24;
          Instruction instLeft = getInstruction(methodCode, ind + branch);
          Instruction instRigth = getInstruction(methodCode, ind + 1);
          BasicBlock bb = new BasicBlock("NORMAL", this, first, tc, instLeft, null);
          first = instRigth;
          v.addElement(bb);
          break;
        }

        case JUMP_regI: {
          BasicBlock bb = new BasicBlock("NORMAL", this, first, tc, null, null);
          first = getInstruction(methodCode, ind + 1);
          v.addElement(bb);
          break;
        }

        case THROW:
        case RETURN_regI:
        case RETURN_regO:
        case RETURN_reg64:
        case RETURN_void:
        case RETURN_s24I:
        case RETURN_null:
        case RETURN_s24D:
        case RETURN_s24L:
        case RETURN_symI:
        case RETURN_symO:
        case RETURN_symD:
        case RETURN_symL: {
          BasicBlock bb = new BasicBlock("NORMAL", this, first, tc, null, null);
          first = getInstruction(methodCode, ind + 1);
          v.addElement(bb);
          break;
        }
        }
        ind += tc.len;
      }

      BasicBlock bb;
      if (first != null) {
        bb = new BasicBlock("NORMAL", this, first, tc, null, null);
        v.addElement(bb);
      }

      len = v.size();
      for (int i = 0; i < len; i++) {
        bb = (BasicBlock) v.items[i];

        if (bb.last.opcode == JUMP_regI) // this basic block ends with the JUMP_regI instruction ? 
        {
          // If so, it is predecessor of all basic blocks.
          for (int k = 0; k < len; k++) {
            BasicBlock b2 = (BasicBlock) v.items[k];
            bb.addSucc(b2);
            b2.addPred(bb);
          }
        }

        BasicBlock b2 = getBasicBlock(bb.instLeft, v, methodCode);
        if (b2 != null) {
          bb.addSucc(b2);
          b2.addPred(bb);
        }
        b2 = getBasicBlock(bb.instRigth, v, methodCode);
        if (b2 != null) {
          bb.addSucc(b2);
          b2.addPred(bb);
        }
        if (bb.instCases != null) {
          bb.bbOfCases = new BasicBlock[bb.instCases.length];
          for (int x = 0; x < bb.instCases.length; x++) {
            bb.bbOfCases[x] = getBasicBlock(bb.instCases[x], v, methodCode);
            bb.bbOfCases[x].addPred(bb);
          }
        }
      }

      blocks = new BasicBlock[v.size()];
      v.copyInto(blocks);
      v.removeAllElements();
    }
  }

  public static void buildCFG(TCMethod[] methods) {
    int len = methods.length;
    for (int i = 0; i < len; i++) {
      if (methods[i] != null) {
        methods[i].cfg = new CFG(methods[i]);
        handlesExceptions(methods[i]);
        topologicalOrder(methods[i].cfg);
      }
    }
  }

  private static void topologicalOrder(CFG cfg) {
    cfg.order = null;
    if (cfg.blocks != null) {
      int nNodes = cfg.blocks.length;
      boolean mark[] = new boolean[nNodes];
      int sorted[] = new int[nNodes];
      TCValue v = new TCValue(nNodes - 1);
      DFS(sorted, mark, cfg.blocks[0], v);

      for (int i = 0; i <= v.asInt; i++) {
        for (int j = 1; j < nNodes - i; j++) {
          sorted[j - 1] = sorted[j];
        }
      }

      int j = nNodes - v.asInt - 1;

      for (int i = 0; i < nNodes; i++) {
        if (!mark[i]) {
          BasicBlock bb = (BasicBlock) cfg.blocks[i];
          sorted[j++] = bb.number;
        }
      }
      cfg.order = sorted;
    }
  }

  private static void DFS(int sorted[], boolean mark[], BasicBlock bb, TCValue nNodes) {
    if (!mark[bb.number]) {
      mark[bb.number] = true;
      for (int i = 0; i < bb.succ.size(); i++) {
        BasicBlock b2 = (BasicBlock) bb.succ.items[i];
        if (b2 != bb) {
          DFS(sorted, mark, b2, nNodes);
        }
      }
      if (bb.bbOfCases != null) {
        for (int i = 0; i < bb.bbOfCases.length; i++) {
          if (bb.bbOfCases[i] != bb) {
            DFS(sorted, mark, bb.bbOfCases[i], nNodes);
          }
        }
      }
      sorted[nNodes.asInt] = bb.number;
      nNodes.asInt--;
    }
  }

  public static void handlesExceptions(TCMethod m) {
    if (m.exceptionHandlers != null) {
      for (int i = 0; i < m.exceptionHandlers.length; i++) {
        TCException tce = m.exceptionHandlers[i];
        Instruction inst = getInstruction(m.insts, tce.handlerPC);
        BasicBlock bbCatch = getBasicBlockOfInstruction(inst, m.cfg.blocks, m.insts);
        for (int j = tce.startPC; j <= tce.endPC;) {
          inst = getInstruction(m.insts, j);
          inst.belongsTry = true;
          BasicBlock bbTry = getBasicBlockOfInstruction(inst, m.cfg.blocks, m.insts);
          bbCatch.addPred(bbTry);
          bbTry.addSucc(bbCatch);
          j += inst.len;
        }
      }
    }
  }

  private static Instruction getInstruction(Vector code, int pos) {
    int len = code.size();
    for (int i = 0, p = 0; i < len; i++) {
      Instruction c = (Instruction) code.items[i];
      if (p == pos) {
        return c;
      }
      p += c.len;
    }
    return null;
  }

  public BasicBlock getBasicBlock(Instruction code, Vector blocks, Vector methodCode) {
    if (code != null) {
      for (int i = 0; i < blocks.size(); i++) {
        BasicBlock bb = (BasicBlock) blocks.items[i];
        if (code == bb.first) {
          return bb;
        }
      }
      return newBasicBlock(blocks, methodCode, code);
    }

    return null;
  }

  public static BasicBlock getBasicBlock(Instruction code, BasicBlock[] blocks, Vector methodCode) {
    if (code != null) {
      for (int i = 0; i < blocks.length; i++) {
        BasicBlock bb = blocks[i];
        bb.print();
        if (code == bb.first) {
          return bb;
        }
      }
    }

    return null;
  }

  public static BasicBlock getBasicBlockOfInstruction(Instruction code, BasicBlock[] blocks, Vector methodCode) {
    if (code != null) {
      for (int i = 0; i < blocks.length; i++) {
        BasicBlock bb = blocks[i];
        Instruction[] block = bb.vector2Array();
        for (int j = 0; j < block.length; j++) {
          Instruction inst = block[j];
          if (inst == code) {
            return bb;
          }
        }
      }
    }

    return null;
  }

  public BasicBlock newBasicBlock(Vector blocks, Vector methodCode, Instruction code) {
    Instruction tc = null;
    int i = -1, ind = 0;
    do {
      tc = (Instruction) methodCode.items[++i];
      ind += tc.len;
    } while (tc != code);

    ind -= tc.len;

    Instruction first = tc;
    int len = methodCode.size();
    boolean notFoundGoto = true;
    BasicBlock bb = null;
    for (; i < len && notFoundGoto; i++) {
      tc = (Instruction) methodCode.items[i];
      int op = tc.opcode;
      switch (op) {
      case SWITCH: {
        Switch_reg inst = (Switch_reg) tc;
        bb = new BasicBlock("SWITCH", this, first, tc, null, null);
        bb.instCases = new Instruction[inst.n + 1];
        int target = ((Two16) inst.params[0]).v1;
        bb.instCases[0] = getInstruction(methodCode, ind + target);
        for (int k = 0, m = inst.n + 1; k < inst.n; k++) {
          target = 0;
          if ((k & 1) == 1) {
            target = ((Two16) inst.params[m++]).v2;
          } else {
            target = ((Two16) inst.params[m]).v1;
          }
          bb.instCases[k + 1] = getInstruction(methodCode, ind + target);
        }
        blocks.addElement(bb);
        notFoundGoto = false;
        break;
      }
      case JEQ_regO_regO:
      case JNE_regO_regO:
      case JEQ_regI_regI:
      case JNE_regI_regI:
      case JLT_regI_regI:
      case JLE_regI_regI:
      case JGT_regI_regI:
      case JGE_regI_regI:
      case JEQ_regL_regL:
      case JNE_regL_regL:
      case JLT_regL_regL:
      case JLE_regL_regL:
      case JGT_regL_regL:
      case JGE_regL_regL:
      case JEQ_regD_regD:
      case JNE_regD_regD:
      case JLT_regD_regD:
      case JLE_regD_regD:
      case JGT_regD_regD:
      case JGE_regD_regD: {
        Reg_reg_s12 inst = (Reg_reg_s12) tc;
        int branch = inst.s12;
        Instruction instLeft = getInstruction(methodCode, ind + branch);
        Instruction instRigth = getInstruction(methodCode, ind + 1);
        bb = new BasicBlock("NORMAL", this, first, tc, instLeft, instRigth);
        blocks.addElement(bb);
        notFoundGoto = false;
        break;
      }

      case JEQ_regI_s6:
      case JNE_regI_s6:
      case JLT_regI_s6:
      case JLE_regI_s6:
      case JGT_regI_s6:
      case JGE_regI_s6:
      case JEQ_regO_null:
      case JNE_regO_null: {
        Reg_s6_desloc inst = (Reg_s6_desloc) tc;
        int branch = inst.desloc;
        Instruction instLeft = getInstruction(methodCode, ind + branch);
        Instruction instRigth = getInstruction(methodCode, ind + 1);
        bb = new BasicBlock("NORMAL", this, first, tc, instLeft, instRigth);
        blocks.addElement(bb);
        notFoundGoto = false;
        break;
      }

      case JEQ_regI_sym:
      case JNE_regI_sym:
      case JGE_regI_arlen: {
        Reg_sym_sdesloc inst = (Reg_sym_sdesloc) tc;
        int branch = inst.desloc;
        Instruction instLeft = getInstruction(methodCode, ind + branch);
        Instruction instRigth = getInstruction(methodCode, ind + 1);
        bb = new BasicBlock("NORMAL", this, first, tc, instLeft, instRigth);
        blocks.addElement(bb);
        notFoundGoto = false;
        break;
      }

      case JUMP_s24: {
        S24 inst = (S24) tc;
        int branch = inst.s24;
        Instruction instLeft = getInstruction(methodCode, ind + branch);
        bb = new BasicBlock("NORMAL", this, first, tc, instLeft, null);
        blocks.addElement(bb);
        notFoundGoto = false;
        break;
      }

      case JUMP_regI: {
        bb = new BasicBlock("NORMAL", this, first, tc, null, null);
        blocks.addElement(bb);
        notFoundGoto = false;
        break;
      }

      case THROW:
      case RETURN_regI:
      case RETURN_regO:
      case RETURN_reg64:
      case RETURN_void:
      case RETURN_s24I:
      case RETURN_null:
      case RETURN_s24D:
      case RETURN_s24L:
      case RETURN_symI:
      case RETURN_symO:
      case RETURN_symD:
      case RETURN_symL: {
        bb = new BasicBlock("NORMAL", this, first, tc, null, null);
        blocks.addElement(bb);
        notFoundGoto = false;
        break;
      }
      }
      ind += tc.len;
    }

    if (notFoundGoto) {
      bb = new BasicBlock("ANORMAL", this, first, tc, null, null);
      blocks.addElement(bb);
    }

    BasicBlock b2 = getBasicBlock(bb.instLeft, blocks, methodCode);
    if (b2 != null) {
      bb.addSucc(b2);
      b2.addPred(bb);
    }

    b2 = getBasicBlock(bb.instRigth, blocks, methodCode);
    if (b2 != null) {
      bb.addSucc(b2);
      b2.addPred(bb);
    }
    if (bb.instCases != null) {
      bb.bbOfCases = new BasicBlock[bb.instCases.length];
      for (int x = 0; x < bb.instCases.length; x++) {
        bb.bbOfCases[x] = getBasicBlock(bb.instCases[x], blocks, methodCode);
        bb.bbOfCases[x].addPred(bb);
      }
    }
    return bb;
  }

  public void print() {
    if (blocks != null) {
      int len = blocks.length;
      for (int i = 0; i < len; i++) {
        blocks[i].print();
      }
    }
  }

  public static void printAllMethods(TCMethod[] methods) {
    int len = methods.length;
    for (int i = 0; i < len; i++) {
      System.out.println("\nMethod: " + GlobalConstantPool.getMethodFieldName(methods[i].cpName));
      methods[i].cfg.print();
    }
  }

  public BasicBlock DFSOrderNode(int i) {
    return blocks[order[i]];
  }

  public BasicBlock RDFSOrderNode(int i) {
    return blocks[order[order.length - i - 1]];
  }

  public int iCount() {
    return method.iCount;
  }

  public int oCount() {
    return method.oCount;
  }

  public int v64Count() {
    return method.v64Count;
  }

  public void livenessAnalysis(int regType) {
    if (blocks != null) {
      int nNodes = blocks.length;
      for (int i = 0; i < nNodes; i++) {
        BasicBlock bb = (BasicBlock) blocks[i];
        bb.InitializeDefsAndUses();
      }
      computeInsOuts();
      // compute Live Uses
      for (int i = 0; i < order.length; i++) {
        BasicBlock bb = RDFSOrderNode(i);
        bb.computeLiveUsesAtInstr(regType);
      }
    }
  }

  public void computeInsOuts() {
    boolean change = true;

    while (change) {
      change = false;
      for (int i = 0; i < order.length; i++) {
        BasicBlock bb = RDFSOrderNode(i);
        BitSet otmpI = new BitSet(bb.useI.nBits);
        BitSet otmpD = new BitSet(bb.useD.nBits);
        BitSet otmpO = new BitSet(bb.useO.nBits);
        for (int j = 0; j < bb.succ.size(); j++) {
          BasicBlock b2 = (BasicBlock) bb.succ.items[j];
          otmpI.on(b2.inI);
          otmpD.on(b2.inD);
          otmpO.on(b2.inO);
        }
        if (bb.bbOfCases != null) {
          for (int j = 0; j < bb.bbOfCases.length; j++) {
            otmpI.on(bb.bbOfCases[j].inI);
            otmpD.on(bb.bbOfCases[j].inD);
            otmpO.on(bb.bbOfCases[j].inO);
          }
        }

        bb.outI = otmpI;
        bb.outD = otmpD;
        bb.outO = otmpO;

        BitSet oldinI = new BitSet(bb.inI);
        BitSet oldinD = new BitSet(bb.inD);
        BitSet oldinO = new BitSet(bb.inO);

        //bb.inI = bb.useI.on(bb.outI.off(bb.defI));
        //bb.inD = bb.useD.on(bb.outD.off(bb.defD));
        //bb.inO = bb.useO.on(bb.outO.off(bb.defO));
        BitSet outI = new BitSet(bb.outI);
        BitSet outD = new BitSet(bb.outD);
        BitSet outO = new BitSet(bb.outO);
        outI.off(bb.defI);
        outD.off(bb.defD);
        outO.off(bb.defO);
        BitSet useI = new BitSet(bb.useI);
        BitSet useD = new BitSet(bb.useD);
        BitSet useO = new BitSet(bb.useO);
        bb.inI = useI.on(outI);
        bb.inD = useD.on(outD);
        bb.inO = useO.on(outO);
        if (!bb.inI.equals(oldinI) || !bb.inD.equals(oldinD) || !bb.inO.equals(oldinO)) {
          change = true;
        }
      }
    }
  }

}
