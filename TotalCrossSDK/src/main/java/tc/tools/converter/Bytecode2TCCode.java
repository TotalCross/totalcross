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
package tc.tools.converter;

import tc.tools.converter.bytecode.BC018_ldc;
import tc.tools.converter.bytecode.BC019_ldc_w;
import tc.tools.converter.bytecode.BC020_ldc2_w;
import tc.tools.converter.bytecode.BC132_iinc;
import tc.tools.converter.bytecode.BC169_ret;
import tc.tools.converter.bytecode.BC170_tableswitch;
import tc.tools.converter.bytecode.BC171_lookupswitch;
import tc.tools.converter.bytecode.BC178_getstatic;
import tc.tools.converter.bytecode.BC179_putstatic;
import tc.tools.converter.bytecode.BC180_getfield;
import tc.tools.converter.bytecode.BC181_putfield;
import tc.tools.converter.bytecode.BC187_new;
import tc.tools.converter.bytecode.BC188_newarray;
import tc.tools.converter.bytecode.BC189_anewarray;
import tc.tools.converter.bytecode.BC192_checkcast;
import tc.tools.converter.bytecode.BC193_instanceof;
import tc.tools.converter.bytecode.BC196_wide;
import tc.tools.converter.bytecode.BC197_multinewarray;
import tc.tools.converter.bytecode.Branch;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.bytecode.ConditionalBranch;
import tc.tools.converter.bytecode.LoadLocal;
import tc.tools.converter.bytecode.MethodCall;
import tc.tools.converter.bytecode.StoreLocal;
import tc.tools.converter.ir.Instruction.Call;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.ir.Instruction.Reg;
import tc.tools.converter.ir.Instruction.Reg_reg_s12;
import tc.tools.converter.ir.Instruction.Reg_s6_desloc;
import tc.tools.converter.ir.Instruction.Reg_sym_sdesloc;
import tc.tools.converter.ir.Instruction.S18_reg;
import tc.tools.converter.ir.Instruction.S24;
import tc.tools.converter.ir.Instruction.Switch_reg;
import tc.tools.converter.ir.Instruction.Two16;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaCode;
import tc.tools.converter.java.JavaField;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.oper.Operand;
import tc.tools.converter.oper.OperandArrayAccess;
import tc.tools.converter.oper.OperandCmp;
import tc.tools.converter.oper.OperandConstant;
import tc.tools.converter.oper.OperandConstant32;
import tc.tools.converter.oper.OperandConstant64;
import tc.tools.converter.oper.OperandExternal;
import tc.tools.converter.oper.OperandKind;
import tc.tools.converter.oper.OperandNull;
import tc.tools.converter.oper.OperandReg;
import tc.tools.converter.oper.OperandRegD32;
import tc.tools.converter.oper.OperandRegD64;
import tc.tools.converter.oper.OperandRegI;
import tc.tools.converter.oper.OperandRegIb;
import tc.tools.converter.oper.OperandRegIc;
import tc.tools.converter.oper.OperandRegIs;
import tc.tools.converter.oper.OperandRegL;
import tc.tools.converter.oper.OperandRegO;
import tc.tools.converter.oper.OperandStack;
import tc.tools.converter.oper.OperandSym;
import tc.tools.converter.oper.OperandSymD32;
import tc.tools.converter.oper.OperandSymD64;
import tc.tools.converter.oper.OperandSymI;
import tc.tools.converter.oper.OperandSymO;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCCode;
import tc.tools.converter.tclass.TCException;
import tc.tools.converter.tclass.TCLineNumber;
import tc.tools.converter.tclass.TCMethod;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.Vector;

public class Bytecode2TCCode implements JConstants, TCConstants {
  public static int[] isTargetOfGoto; // if zero, it is not target of goto. Otherwise, it is target of goto.
  public static int nextGotoIndex = 1;
  public static JavaClass currentJClass;
  public static TCClass currentTCClass;
  public static TCException[] currentExceptionHandlers;
  public static IntHashtable htBytecodeIndex = new IntHashtable(31);
  public static int indexOfCurrentBytecode;
  public static int javaInstructionIndex;
  public static Vector methodIndexes = new Vector(8);
  public static IntHashtable htTargetOfGoto = new IntHashtable(8);
  public static IntHashtable htFinally = new IntHashtable(31);
  public static IntHashtable htForBytecodeRet = new IntHashtable(31);
  public static Hashtable htStackOfBranch = new Hashtable(31);

  public static IntHashtable htInstanceFieldIndexes = new IntHashtable(31);
  public static IntHashtable htStaticFieldIndexes = new IntHashtable(31);

  private static ByteCode lastBytecode;
  private static Vector newInsts = new Vector(64);
  public static JavaCode javaCodeCurrent;
  private static int lineOfPC;

  public static void init(ByteCode[] bcs) {
    nextGotoIndex = 1;
    setGotos(bcs);
    indexOfCurrentBytecode = 0;
    javaInstructionIndex = 0;
    htBytecodeIndex.clear();
    htTargetOfGoto.clear();
    htFinally.clear();
    htForBytecodeRet.clear();
    htStackOfBranch.clear();
    newInsts.removeAllElements();
  }

  public static int setGotoIndex(int BCIndex) {
    if (isTargetOfGoto[BCIndex] == 0) {
      isTargetOfGoto[BCIndex] = nextGotoIndex++;
    }

    return isTargetOfGoto[BCIndex];
  }

  private static Instruction processIINC(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    BC132_iinc ji = (BC132_iinc) i;
    OperandRegI target = new OperandRegI(ji.result);
    Operand opr = new OperandConstant32(ji.operand, type_Int);
    OperandRegI regI = stack.lookupOperandRegI(target.index);
    if (regI != null) {
      OperandRegI r = new OperandRegI();
      GenerateInstruction.newInstruction(vcode, pref_MOV, r, regI, lineOfPC);
      regI.index = r.index;
    }
    return GenerateInstruction.newInstruction(vcode, pref_ADD, target, target, opr, lineOfPC);
  }

  private static void processILOAD(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    LoadLocal ji = (LoadLocal) i;
    stack.push(new OperandRegI(ji.localIdx));
  }

  private static void processLLOAD(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    LoadLocal ji = (LoadLocal) i;
    stack.push(new OperandRegL(ji.localIdx));
  }

  private static void processFLOAD(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    LoadLocal ji = (LoadLocal) i;
    stack.push(new OperandRegD32(ji.localIdx));
  }

  private static void processDLOAD(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    LoadLocal ji = (LoadLocal) i;
    stack.push(new OperandRegD64(ji.localIdx));
  }

  private static void processALOAD(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    LoadLocal ji = (LoadLocal) i;
    stack.push(new OperandRegO(ji.localIdx));
  }

  private static Instruction processISTORE(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    StoreLocal ji = (StoreLocal) i;
    Operand opr = stack.pop();
    OperandReg target = new OperandRegI(ji.targetIdx);
    // Check if there is another reference this target register in the stack.
    // If so, replace the slot this reference by a copy of it.
    // (fixed bug in instructions x++/x-- and ++x/--x)
    OperandRegI regI = stack.lookupOperandRegI(target.index);
    if (regI != null) {
      OperandRegI r = new OperandRegI();
      GenerateInstruction.newInstruction(vcode, pref_MOV, r, regI, lineOfPC);
      regI.index = r.index;
    }
    return GenerateInstruction.newInstruction(vcode, pref_MOV, target, opr, lineOfPC);
  }

  private static Instruction processLSTORE(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    StoreLocal ji = (StoreLocal) i;
    Operand opr = stack.pop();
    OperandReg target = new OperandRegL(ji.targetIdx);
    // Check if there is another reference this target register in the stack.
    // If so, replace the slot this reference by a copy of it.
    // (fixed bug in instructions x++/x-- and ++x/--x)
    OperandRegL regL = stack.lookupOperandRegL(target.index);
    if (regL != null) {
      OperandReg r = new OperandRegL();
      GenerateInstruction.newInstruction(vcode, pref_MOV, r, regL, lineOfPC);
      regL.index = r.index;
    }
    return GenerateInstruction.newInstruction(vcode, pref_MOV, target, opr, lineOfPC);
  }

  private static Instruction processFSTORE(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    StoreLocal ji = (StoreLocal) i;
    Operand opr = stack.pop();
    OperandReg target = new OperandRegD32(ji.targetIdx);
    return GenerateInstruction.newInstruction(vcode, pref_MOV, target, opr, lineOfPC);
  }

  private static Instruction processDSTORE(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    StoreLocal ji = (StoreLocal) i;
    Operand opr = stack.pop();
    OperandReg target = new OperandRegD64(ji.targetIdx);
    return GenerateInstruction.newInstruction(vcode, pref_MOV, target, opr, lineOfPC);
  }

  private static Instruction processASTORE(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    StoreLocal ji = (StoreLocal) i;
    int regIndex = -1;
    try {
      regIndex = htFinally.get(indexOfCurrentBytecode);
    } catch (Exception e) {
    }
    if (regIndex != -1) {
      int reg = -1;
      try {
        reg = htForBytecodeRet.get(ji.targetIdx);
      } catch (Exception e) {
      }
      if (reg == -1) {
        htForBytecodeRet.put(ji.targetIdx, regIndex);
      }
    } else {
      OperandReg target = new OperandRegO(ji.targetIdx);
      if (setExceptionHandler(currentExceptionHandlers, isTargetOfGoto[indexOfCurrentBytecode], target.index)) {
        return GenerateInstruction.newInstruction(vcode, pref_MOV, target, target, lineOfPC);
      } else {
        // even excluding the handler of exception, the java compiler can leave dirt, which should be discarded.
        if (!stack.empty()) {
          Operand opr = stack.pop();
          return GenerateInstruction.newInstruction(vcode, pref_MOV, target, opr, lineOfPC);
        }
      }
    }

    return null;
  }

  private static Instruction processRET(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    BC169_ret ji = (BC169_ret) i;
    int regIndex = -1;
    try {
      regIndex = htForBytecodeRet.get(ji.answer);
    } catch (Exception e) {
    }
    Reg code = new Reg(JUMP_regI, lineOfPC, regIndex);
    vcode.addElement(code);
    return code;
  }

  private static Instruction processWIDE(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer) {
    Instruction tc = null;
    int op = i.bc;
    switch (op) {
    case ILOAD:
      processILOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case LLOAD:
      processLLOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case FLOAD:
      processFLOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case DLOAD:
      processDLOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case ALOAD:
      processALOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case ISTORE:
      processISTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case LSTORE:
      processLSTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case FSTORE:
      processFSTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case DSTORE:
      processDSTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case ASTORE:
      processASTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case RET:
      processRET(bcs, i, stack, vcode, isStaticInitializer);
      break;
    case IINC:
      tc = processIINC(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    return tc;
  }

  public static Instruction convert(ByteCode[] bcs, ByteCode i, OperandStack stack, Vector vcode,
      boolean isStaticInitializer, String signature) {
    TCValue v;
    Instruction tc = null;
    int op = i.bc;
    CheckIfIsGotoTarget(indexOfCurrentBytecode, i, stack, vcode);
    if (isTargetOfGoto[indexOfCurrentBytecode] != 0) {
      htBytecodeIndex.put(isTargetOfGoto[indexOfCurrentBytecode], countOfTCCode(vcode));
    }
    lineOfPC = getLineOfPC(indexOfCurrentBytecode);
    switch (op) {
    case NOP: {
      break; // do nothing.
    }
    case ACONST_NULL: //1
    {
      stack.push(new OperandNull());
      break;
    }
    case ICONST_M1: //2
    {
      stack.push(new OperandConstant32(-1, type_Int));
      break;
    }
    case ICONST_0: //3
    {
      stack.push(new OperandConstant32(0, type_Int));
      break;
    }
    case LCONST_0: //9
    {
      stack.push(new OperandConstant64(0, type_Long));
      break;
    }
    case FCONST_0: //11
    {
      stack.push(new OperandConstant32(0, type_Double));
      break;
    }
    case DCONST_0: //14
    {
      stack.push(new OperandConstant64(0, type_Double));
      break;
    }
    case ICONST_1: //4
    {
      stack.push(new OperandConstant32(1, type_Int));
      break;
    }
    case FCONST_1: //12
    {
      stack.push(new OperandConstant32(1, type_Double));
      break;
    }
    case LCONST_1: //10
    {
      stack.push(new OperandConstant64(1, type_Long));
      break;
    }
    case DCONST_1: //15
    {
      stack.push(new OperandConstant64(1, type_Double));
      break;
    }
    case ICONST_2: //5
    {
      stack.push(new OperandConstant32(2, type_Int));
      break;
    }
    case FCONST_2: //13
    {
      stack.push(new OperandConstant32(2, type_Double));
      break;
    }
    case ICONST_3: //6
    {
      stack.push(new OperandConstant32(3, type_Int));
      break;
    }
    case ICONST_4: //7
    {
      stack.push(new OperandConstant32(4, type_Int));
      break;
    }
    case ICONST_5: //8
    {
      stack.push(new OperandConstant32(5, type_Int));
      break;
    }
    case BIPUSH: //16
    case SIPUSH: //17
    {
      LoadLocal ji = (LoadLocal) i;
      stack.push(new OperandConstant32(ji.localIdx, type_Int));
      break;
    }
    case LDC: //18
    case LDC_W: //19
    {
      if (op == LDC) {
        BC018_ldc ji = (BC018_ldc) i;
        v = ji.val;
      } else {
        BC019_ldc_w ji = (BC019_ldc_w) i;
        v = ji.val;
      }
      if (v.type == INT) {
        stack.push(new OperandConstant32(v.asInt, type_Int));
      } else if (v.type == DOUBLE) // this constant is a float
      {
        if (OperandConstant.fitsIn18Bits(v.asDouble)) {
          stack.push(new OperandConstant32((int) v.asDouble, type_Double));
        } else {
          stack.push(new OperandSymD32(GlobalConstantPool.put(v.asDouble)));
        }
      } else {
        stack.push(new OperandSymO(GlobalConstantPool.putStr((String) v.asObj)));
      }
      break;
    }
    case LDC2_W: //20
    {
      BC020_ldc2_w ji = (BC020_ldc2_w) i;
      v = ji.val;
      if (v.type == LONG) {
        stack.push(new OperandConstant64(v.asLong, type_Long));
      } else if (v.type == DOUBLE) {
        if (OperandConstant.fitsIn18Bits(v.asDouble)) {
          stack.push(new OperandConstant64((int) v.asDouble, type_Double));
        } else {
          stack.push(new OperandSymD64(GlobalConstantPool.put(v.asDouble)));
        }
      }
      break;
    }
    case ILOAD: //21
    case ILOAD_0: //26
    case ILOAD_1: //27
    case ILOAD_2: //28
    case ILOAD_3: //29
    {
      processILOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case LLOAD: //22
    case LLOAD_0: //30
    case LLOAD_1: //31
    case LLOAD_2: //32
    case LLOAD_3: //33
    {
      processLLOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case FLOAD: //23
    case FLOAD_0: //34
    case FLOAD_1: //35
    case FLOAD_2: //36
    case FLOAD_3: //37
    {
      processFLOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case DLOAD: //24
    case DLOAD_0: //38
    case DLOAD_1: //39
    case DLOAD_2: //40
    case DLOAD_3: //41
    {
      processDLOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case ALOAD: //25
    case ALOAD_0: //42
    case ALOAD_1: //43
    case ALOAD_2: //44
    case ALOAD_3: //45
    {
      processALOAD(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case IALOAD: //46
    case BALOAD: //51
    case CALOAD: //52
    case SALOAD: //53
    {
      Operand index = stack.pop();
      OperandReg regO = (OperandRegO) stack.pop();
      OperandReg regIndex = (OperandReg) GenerateInstruction.promoteOperand(vcode, index, opr_regI, lineOfPC);
      OperandArrayAccess base = new OperandArrayAccess(opr_arcI, regO, regIndex);

      OperandReg reg = null;
      switch (op) {
      case IALOAD:
        reg = new OperandRegI();
        break;
      case BALOAD:
        reg = new OperandRegIb();
        break;
      case CALOAD:
        reg = new OperandRegIc();
        break;
      case SALOAD:
        reg = new OperandRegIs();
        break;
      }

      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, reg, base, lineOfPC);
      stack.push(reg);
      break;
    }
    case LALOAD: //47
    {
      Operand index = stack.pop();
      OperandReg regO = (OperandRegO) stack.pop();
      OperandReg regIndex = (OperandReg) GenerateInstruction.promoteOperand(vcode, index, opr_regI, lineOfPC);
      OperandArrayAccess base = new OperandArrayAccess(opr_arcL, regO, regIndex);

      OperandReg reg = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, reg, base, lineOfPC);
      stack.push(reg);
      break;
    }
    case FALOAD: //48
    case DALOAD: //49
    {
      Operand index = stack.pop();
      OperandReg regO = (OperandRegO) stack.pop();
      OperandReg regIndex = (OperandReg) GenerateInstruction.promoteOperand(vcode, index, opr_regI, lineOfPC);
      OperandArrayAccess base = new OperandArrayAccess(opr_arcD, regO, regIndex);

      OperandReg reg;
      if (op == FALOAD) {
        reg = new OperandRegD32();
      } else {
        reg = new OperandRegD64();
      }

      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, reg, base, lineOfPC);
      stack.push(reg);
      break;
    }
    case AALOAD: //50
    {
      Operand index = stack.pop();
      OperandReg regO = (OperandRegO) stack.pop();
      OperandReg regIndex = (OperandReg) GenerateInstruction.promoteOperand(vcode, index, opr_regI, lineOfPC);
      OperandArrayAccess base = new OperandArrayAccess(opr_arcO, regO, regIndex);

      OperandReg reg = new OperandRegO();
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, reg, base, lineOfPC);
      stack.push(reg);
      break;
    }
    case ISTORE: //54
    case ISTORE_0: //59
    case ISTORE_1: //60
    case ISTORE_2: //61
    case ISTORE_3: //62
    {
      tc = processISTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }

    case LSTORE: //55
    case LSTORE_0: //63
    case LSTORE_1: //64
    case LSTORE_2: //65
    case LSTORE_3: //66
    {
      tc = processLSTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case FSTORE: //56
    case FSTORE_0: //67
    case FSTORE_1: //68
    case FSTORE_2: //69
    case FSTORE_3: //70
    {
      tc = processFSTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case DSTORE: //57
    case DSTORE_0: //71
    case DSTORE_1: //72
    case DSTORE_2: //73
    case DSTORE_3: //74
    {
      tc = processDSTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case ASTORE: //58
    case ASTORE_0: //75
    case ASTORE_1: //76
    case ASTORE_2: //77
    case ASTORE_3: //78
    {
      tc = processASTORE(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case IASTORE: //79
    case BASTORE: //84
    case CASTORE: //85
    case SASTORE: //86
    {
      Operand value = stack.pop();
      Operand index = stack.pop();
      OperandRegO regO = (OperandRegO) stack.pop();
      int kind = opr_arcI;
      switch (op) {
      case BASTORE:
        kind = opr_arcIb;
        break;
      case CASTORE:
        kind = opr_arcIc;
        break;
      case SASTORE:
        kind = opr_arcIs;
        break;
      }
      OperandReg regIndex = (OperandReg) GenerateInstruction.promoteOperand(vcode, index, opr_regI, lineOfPC);
      OperandArrayAccess base = new OperandArrayAccess(kind, regO, regIndex);
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, base, value, lineOfPC);
      break;
    }
    case FASTORE: //80
    case DASTORE: //82
    {
      Operand value = stack.pop();
      Operand index = stack.pop();
      OperandReg regO = (OperandRegO) stack.pop();
      OperandReg regIndex = (OperandReg) GenerateInstruction.promoteOperand(vcode, index, opr_regI, lineOfPC);
      OperandArrayAccess base = new OperandArrayAccess(opr_arcD, regO, regIndex);
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, base, value, lineOfPC);
      break;
    }
    case LASTORE: //81
    {
      Operand value = stack.pop();
      Operand index = stack.pop();
      OperandReg regO = (OperandRegO) stack.pop();
      OperandReg regIndex = (OperandReg) GenerateInstruction.promoteOperand(vcode, index, opr_regI, lineOfPC);
      OperandArrayAccess base = new OperandArrayAccess(opr_arcL, regO, regIndex);
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, base, value, lineOfPC);
      break;
    }
    case AASTORE: //83
    {
      Operand value = stack.pop();
      Operand index = stack.pop();
      OperandReg regO = (OperandRegO) stack.pop();
      OperandReg regIndex = (OperandReg) GenerateInstruction.promoteOperand(vcode, index, opr_regI, lineOfPC);
      OperandArrayAccess base = new OperandArrayAccess(opr_arcO, regO, regIndex);
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, base, value, lineOfPC);
      break;
    }
    case POP: //87
    {
      if (!stack.empty()) {
        stack.pop();
      }
      break;
    }
    case POP2: //88
    {
      Operand opr = stack.pop();
      if (opr.nWords == 1) {
        stack.pop();
      }
      break;
    }
    case DUP: //89
    {
      stack.push(stack.top());
      break;
    }
    case DUP_X1: //90
    {
      Operand w1 = stack.pop();
      Operand w2 = stack.pop();
      stack.push(w1);
      stack.push(w2);
      stack.push(w1);
      break;
    }
    case DUP_X2: //91
    {
      Operand w1 = stack.pop();
      Operand w2 = stack.pop();
      if (w2.nWords == 2) {
        stack.push(w1);
      } else {
        Operand w3 = stack.pop();
        stack.push(w1);
        stack.push(w3);
      }
      stack.push(w2);
      stack.push(w1);
      break;
    }
    case DUP2: //92
    {
      Operand w1 = stack.pop();
      if (w1.nWords == 2) {
        stack.push(w1);
      } else {
        Operand w2 = stack.pop();
        stack.push(w2);
        stack.push(w1);
        stack.push(w2);
      }
      stack.push(w1);
      break;
    }
    case DUP2_X1: //93
    {
      Operand w1 = stack.pop();
      if (w1.nWords == 2) {
        Operand w3 = stack.pop();
        stack.push(w1);
        stack.push(w3);
      } else {
        Operand w2 = stack.pop();
        Operand w3 = stack.pop();
        stack.push(w2);
        stack.push(w1);
        stack.push(w3);
        stack.push(w2);
      }
      stack.push(w1);
      break;
    }
    case DUP2_X2: //94
    {
      Operand w1 = stack.pop();
      if (w1.nWords == 2) {
        Operand w3 = stack.pop();
        if (w3.nWords == 2) {
          stack.push(w1);
        } else {
          Operand w4 = stack.pop();
          stack.push(w1);
          stack.push(w4);
        }
        stack.push(w3);
      } else {
        Operand w2 = stack.pop();
        Operand w3 = stack.pop();
        if (w3.nWords == 2) {
          stack.push(w2);
          stack.push(w1);
        } else {
          Operand w4 = stack.pop();
          stack.push(w2);
          stack.push(w1);
          stack.push(w4);
        }
        stack.push(w3);
        stack.push(w2);
      }
      stack.push(w1);
      break;
    }
    case SWAP: //95
    {
      //BC095_swap ji = (BC095_swap)i;
      Operand w1 = stack.pop();
      Operand w2 = stack.pop();
      stack.push(w1);
      stack.push(w2);
      break;
    }
    case IADD: //96
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_ADD, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LADD: //97
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_ADD, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case FADD: //98
    case DADD: //99
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target;
      if (op == FADD) {
        target = new OperandRegD32();
      } else {
        target = new OperandRegD64();
      }
      tc = GenerateInstruction.newInstruction(vcode, pref_ADD, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case ISUB: //100
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      if (opr2.isConstantInt()) {
        OperandConstant c = (OperandConstant) opr2;
        if (c.value >= -2047 && c.value <= 2048) {
          c.value = -c.value;
          c.kind = opr_s12I;
          tc = GenerateInstruction.newInstruction(vcode, pref_ADD, target, opr1, c, lineOfPC);
        } else if (c.value != -2147483648) // integer minimum
        {
          c.value = -c.value;
          tc = GenerateInstruction.newInstruction(vcode, pref_ADD, target, opr1, c, lineOfPC);
        } else {
          tc = GenerateInstruction.newInstruction(vcode, pref_SUB, target, opr1, c, lineOfPC);
        }
      } else {
        tc = GenerateInstruction.newInstruction(vcode, pref_SUB, target, opr1, opr2, lineOfPC);
      }
      stack.push(target);
      break;
    }
    case LSUB: //101
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_SUB, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case FSUB: //102
    case DSUB: //103
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target;
      if (op == FSUB) {
        target = new OperandRegD32();
      } else {
        target = new OperandRegD64();
      }

      tc = GenerateInstruction.newInstruction(vcode, pref_SUB, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case IMUL: //104
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_MUL, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LMUL: //105
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_MUL, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case FMUL: //106
    case DMUL: //107
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target;
      if (op == FMUL) {
        target = new OperandRegD32();
      } else {
        target = new OperandRegD64();
      }
      tc = GenerateInstruction.newInstruction(vcode, pref_MUL, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case IDIV: //108
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_DIV, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LDIV: //109
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_DIV, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case FDIV: //110
    case DDIV: //111
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target;
      if (op == FDIV) {
        target = new OperandRegD32();
      } else {
        target = new OperandRegD64();
      }
      tc = GenerateInstruction.newInstruction(vcode, pref_DIV, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case IREM: //112
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_MOD, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LREM: //113
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_MOD, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case FREM: //114
    case DREM: //115
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target;
      if (op == FREM) {
        target = new OperandRegD32();
      } else {
        target = new OperandRegD64();
      }

      tc = GenerateInstruction.newInstruction(vcode, pref_MOD, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case INEG: //116
    {
      Operand opr1 = new OperandConstant32(0, type_Int);
      Operand opr2 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_SUB, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LNEG: //117
    {
      Operand opr1 = new OperandConstant64(0, type_Long);
      Operand opr2 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_SUB, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case FNEG: //118
    case DNEG: //119
    {
      Operand opr1 = new OperandConstant32(0, type_Int);
      Operand opr2 = stack.pop();
      OperandReg target;
      if (op == FNEG) {
        target = new OperandRegD32();
      } else {
        target = new OperandRegD64();
      }
      tc = GenerateInstruction.newInstruction(vcode, pref_SUB, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case ISHL: //120
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_SHL, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LSHL: //121
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_SHL, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case ISHR: //122
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_SHR, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LSHR: //123
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_SHR, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case IUSHR: //124
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_USHR, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LUSHR: //125
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_USHR, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case IAND: //126
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_AND, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LAND: //127
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_AND, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case IOR: //128
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_OR, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LOR: //129
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_OR, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case IXOR: //130
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_XOR, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case LXOR: //131
    {
      Operand opr2 = stack.pop();
      Operand opr1 = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_XOR, target, opr1, opr2, lineOfPC);
      stack.push(target);
      break;
    }
    case IINC: //132
    {
      tc = processIINC(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case I2L: //133
    case F2L: //140
    case D2L: //143
    {
      Operand opr = stack.pop();
      OperandReg target = new OperandRegL();
      tc = GenerateInstruction.newInstruction(vcode, pref_CONV, target, opr, lineOfPC);
      stack.push(target);
      break;
    }
    case I2F: //134
    case L2F: //137
    case D2F: //144
    {
      Operand opr = stack.pop();
      OperandReg target = new OperandRegD32();
      tc = GenerateInstruction.newInstruction(vcode, pref_CONV, target, opr, lineOfPC);
      stack.push(target);
      break;
    }
    case I2D: //135
    case L2D: //138
    case F2D: //141
    {
      Operand opr = stack.pop();
      OperandReg target = new OperandRegD64();
      tc = GenerateInstruction.newInstruction(vcode, pref_CONV, target, opr, lineOfPC);
      stack.push(target);
      break;
    }
    case L2I: //136
    case F2I: //139
    case D2I: //142
    {
      Operand opr = stack.pop();
      OperandReg target = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_CONV, target, opr, lineOfPC);
      stack.push(target);
      break;
    }
    case I2B: //145
    {
      Operand opr = stack.pop();
      OperandReg target = new OperandRegIb();
      tc = GenerateInstruction.newInstruction(vcode, pref_CONV, target, opr, lineOfPC);
      stack.push(target);
      break;
    }
    case I2C: //146
    {
      Operand opr = stack.pop();
      OperandReg target = new OperandRegIc();
      tc = GenerateInstruction.newInstruction(vcode, pref_CONV, target, opr, lineOfPC);
      stack.push(target);
      break;
    }
    case I2S: //147
    {
      Operand opr = stack.pop();
      OperandReg target = new OperandRegIs();
      tc = GenerateInstruction.newInstruction(vcode, pref_CONV, target, opr, lineOfPC);
      stack.push(target);
      break;
    }
    case LCMP: //148
    case FCMPL: //149
    case FCMPG: //150
    case DCMPL: //151
    case DCMPG: //152
    {
      Operand v2 = stack.pop();
      Operand v1 = stack.pop();
      stack.push(new OperandCmp(v1, v2));
      break;
    }
    case IFEQ: //153
    case IFNE: //154
    case IFLT: //155
    case IFGE: //156
    case IFGT: //157
    case IFLE: //158
    {
      ConditionalBranch ji = (ConditionalBranch) i;
      Operand v1, v2, opr = stack.pop();
      if (opr.kind == opr_cmp) {
        v1 = ((OperandCmp) opr).v1;
        v2 = ((OperandCmp) opr).v2;
      } else {
        v1 = opr;
        v2 = new OperandConstant32(0, type_Int);
      }
      OperandConstant target = new OperandConstant32(isTargetOfGoto[ji.jumpIfTrue], type_Int);
      int prefix = 0;
      switch (op) {
      case IFEQ:
        prefix = pref_JEQ;
        break;
      case IFNE:
        prefix = pref_JNE;
        break;
      case IFLT:
        prefix = pref_JLT;
        break;
      case IFGE:
        prefix = pref_JGE;
        break;
      case IFGT:
        prefix = pref_JGT;
        break;
      case IFLE:
        prefix = pref_JLE;
        break;
      }
      stackOfBranch(vcode, bcs, stack, ji.jumpIfTrue);
      tc = GenerateInstruction.newInstruction(vcode, prefix, v1, v2, target, lineOfPC);
      break;
    }
    case IF_ICMPEQ: //159
    case IF_ICMPNE: //160
    case IF_ICMPLT: //161
    case IF_ICMPGE: //162
    case IF_ICMPGT: //163
    case IF_ICMPLE: //164
    case IF_ACMPEQ: //165
    case IF_ACMPNE: //166
    {
      ConditionalBranch ji = (ConditionalBranch) i;
      Operand value2 = stack.pop();
      Operand value1 = stack.pop();
      OperandConstant target = new OperandConstant32(isTargetOfGoto[ji.jumpIfTrue], type_Int);
      int prefix = 0;
      switch (op) {
      case IF_ICMPEQ:
        prefix = pref_JEQ;
        break;
      case IF_ICMPNE:
        prefix = pref_JNE;
        break;
      case IF_ICMPLT:
        prefix = pref_JLT;
        break;
      case IF_ICMPGE:
        prefix = pref_JGE;
        break;
      case IF_ICMPGT:
        prefix = pref_JGT;
        break;
      case IF_ICMPLE:
        prefix = pref_JLE;
        break;
      case IF_ACMPEQ:
        prefix = pref_JEQ;
        break;
      case IF_ACMPNE:
        prefix = pref_JNE;
        break;
      }
      stackOfBranch(vcode, bcs, stack, ji.jumpIfTrue);
      tc = GenerateInstruction.newInstruction(vcode, prefix, value1, value2, target, lineOfPC);
      break;
    }
    case GOTO: //167
    case GOTO_W: //200
    {
      Branch ji = (Branch) i;
      OperandConstant branch = new OperandConstant32(isTargetOfGoto[ji.jumpTo], type_Int);
      stackOfBranch(vcode, bcs, stack, ji.jumpTo);
      tc = GenerateInstruction.newInstruction(vcode, pref_JUMP, branch, lineOfPC);
      break;
    }
    case JSR: //168
    case JSR_W: //201
    {
      Branch ji = (Branch) i;
      int regIndex = -1;
      try {
        regIndex = htFinally.get(ji.jumpTo);
      } catch (Exception e) {
      }
      if (regIndex == -1) {
        OperandRegI reg = new OperandRegI();
        regIndex = reg.index;
        htFinally.put(ji.jumpTo, regIndex);
      }
      stackOfBranch(vcode, bcs, stack, ji.jumpTo);
      S18_reg c1 = new S18_reg(MOV_regI_s18, lineOfPC);
      c1.set(countOfTCCode(vcode) + 2, regIndex);
      vcode.addElement(c1);

      if (J2TC.dumpBytecodes) {
        System.out.println(i + " -> " + c1);
      }

      S24 c2 = new S24(JUMP_s24, lineOfPC, 0);
      c2.set(isTargetOfGoto[ji.jumpTo]);
      vcode.addElement(c2);
      tc = c2;
      break;
    }
    case RET: //169
    {
      tc = processRET(bcs, i, stack, vcode, isStaticInitializer);
      break;
    }
    case TABLESWITCH: //170
    {
      BC170_tableswitch ji = (BC170_tableswitch) i;
      Operand index = stack.pop();
      GenerateInstruction.newInstruction(vcode, ji.def, index, ji, lineOfPC);
      break;
    }
    case LOOKUPSWITCH: //171
    {
      BC171_lookupswitch ji = (BC171_lookupswitch) i;
      Operand key = stack.pop();
      GenerateInstruction.newInstruction(vcode, ji.def + indexOfCurrentBytecode, key, ji, lineOfPC);
      break;
    }
    case IRETURN: //172
    {
      Operand value = stack.pop();
      GenerateInstruction.newInstruction(vcode, pref_RETURNI, value, lineOfPC);
      break;
    }
    case LRETURN: //173
    {
      Operand value = stack.pop();
      GenerateInstruction.newInstruction(vcode, pref_RETURNL, value, lineOfPC);
      break;
    }
    case FRETURN: //174
    {
      Operand value = stack.pop();
      GenerateInstruction.newInstruction(vcode, pref_RETURND, value, lineOfPC);
      break;
    }
    case DRETURN: //175
    {
      Operand value = stack.pop();
      GenerateInstruction.newInstruction(vcode, pref_RETURND, value, lineOfPC);
      break;
    }
    case ARETURN: //176
    {
      Operand value = stack.pop();
      GenerateInstruction.newInstruction(vcode, pref_RETURNO, value, lineOfPC);
      break;
    }
    case RETURN: //177
    {
      GenerateInstruction.newInstruction(vcode, pref_RETURNV, lineOfPC);
      break;
    }
    case GETSTATIC: //178
    {
      BC178_getstatic ji = (BC178_getstatic) i;
      ji.className = replaceTotalCrossLangToJavaLang(ji.className);
      ji.className = removeSuffix4D(ji.className);
      if (currentJClass.className.equals(ji.className) && !existsField(currentJClass.fields, ji.fieldName)) {
        ji.className = currentTCClass.superClass;
      }
      OperandReg target = null;
      int kind = 0;
      switch (ji.fieldTypeClass.charAt(0)) {
      case 'Z':
      case 'C':
      case 'B':
      case 'S':
      case 'I': // boolean, char, byte, short, int
        kind = opr_staticI;
        target = new OperandRegI();
        break;
      case 'J': // long
        kind = opr_staticL;
        target = new OperandRegL();
        break;
      case 'F': // float
        kind = opr_staticD;
        target = new OperandRegD32();
        break;
      case 'D': // double
        kind = opr_staticD;
        target = new OperandRegD64();
        break;
      default: // *** case 'L':  case '[': ***  object
        kind = opr_staticO;
        target = new OperandRegO();
        break;
      }
      int index = GlobalConstantPool.putStaticField(ji.className, ji.fieldName); // external fields require the class and the field name
      if (!isStaticInitializer) {
        htStaticFieldIndexes.put(index, index);
      }
      Operand source = new OperandSym(kind, index);
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, target, source, lineOfPC);
      stack.push(target);
      break;
    }
    case PUTSTATIC: //179
    {
      BC179_putstatic ji = (BC179_putstatic) i;
      ji.className = replaceTotalCrossLangToJavaLang(ji.className);
      ji.className = removeSuffix4D(ji.className);
      if (currentJClass.className.equals(ji.className) && !existsField(currentJClass.fields, ji.fieldName)) {
        ji.className = currentTCClass.superClass;
      }
      int kind = 0;
      switch (ji.fieldTypeClass.charAt(0)) {
      case 'Z':
      case 'C':
      case 'B':
      case 'S':
      case 'I': // boolean, char, byte, short, int
        kind = opr_staticI;
        break;
      case 'J': // long
        kind = opr_staticL;
        break;
      case 'F':
      case 'D': // float, double
        kind = opr_staticD;
        break;
      default: // *** case 'L':  case '[': ***  object
        kind = opr_staticO;
        break;
      }
      int index = GlobalConstantPool.putStaticField(ji.className, ji.fieldName);
      if (!isStaticInitializer) {
        htStaticFieldIndexes.put(index, index);
      }
      Operand target = new OperandSym(kind, index);
      Operand value = stack.pop();
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, target, value, lineOfPC);
      break;
    }
    case GETFIELD: //180
    {
      BC180_getfield ji = (BC180_getfield) i;
      ji.className = replaceTotalCrossLangToJavaLang(ji.className);
      ji.className = removeSuffix4D(ji.className);
      OperandReg regO = (OperandReg) stack.pop();
      OperandReg target = null;
      int kind = 0;
      switch (ji.fieldTypeClass.charAt(0)) {
      case 'Z':
      case 'C':
      case 'B':
      case 'S':
      case 'I': // boolean, char, byte, short, int
        kind = opr_fieldI;
        target = new OperandRegI();
        break;
      case 'J': // long
        kind = opr_fieldL;
        target = new OperandRegL();
        break;
      case 'F': // float
        kind = opr_fieldD;
        target = new OperandRegD32();
        break;
      case 'D': // double
        kind = opr_fieldD;
        target = new OperandRegD64();
        break;
      default: // *** case 'L':  case '[': ***  object
        kind = opr_fieldO;
        target = new OperandRegO();
        break;
      }
      int index = GlobalConstantPool.putInstanceField(ji.className, ji.fieldName);
      htInstanceFieldIndexes.put(index, index);
      Operand source = new OperandExternal(regO, new OperandSym(kind, index));
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, target, source, lineOfPC);
      stack.push(target);
      break;
    }
    case PUTFIELD: //181
    {
      BC181_putfield ji = (BC181_putfield) i;
      ji.className = replaceTotalCrossLangToJavaLang(ji.className);
      ji.className = removeSuffix4D(ji.className);
      Operand value = stack.pop();
      OperandReg regO = (OperandReg) stack.pop();
      int kind = 0;
      switch (ji.fieldTypeClass.charAt(0)) {
      case 'Z':
      case 'C':
      case 'B':
      case 'S':
      case 'I': // boolean, char, byte, short, int
        kind = opr_fieldI;
        break;
      case 'J': // long
        kind = opr_fieldL;
        break;
      case 'F':
      case 'D': // float, double
        kind = opr_fieldD;
        break;
      default: // *** case 'L':  case '[': ***  object
        kind = opr_fieldO;
        break;
      }
      int index = GlobalConstantPool.putInstanceField(ji.className, ji.fieldName);
      htInstanceFieldIndexes.put(index, index);
      Operand target = new OperandExternal(regO, new OperandSym(kind, index));
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, target, value, lineOfPC);
      break;
    }
    case INVOKEVIRTUAL: //182
    case INVOKESPECIAL: //183
    case INVOKESTATIC: //184
    case INVOKEINTERFACE: //185
    {
      MethodCall ji = (MethodCall) i;
      ji.className = replaceTotalCrossLangToJavaLang(ji.className);
      ji.className = removeSuffix4D(ji.className);
      String name = ji.name;
      ji.name = removeSuffix4D(name);
      ji.signature = removeSuffix4D(name, ji.signature);
      if (ji.className.equals("java/lang/Object") && ji.signature.equals("<init>()")) {
        stack.pop();
        break;
      }
      int paramCount = ji.args == null ? 0 : ji.args.length;
      Operand retAndParams[];
      int notVoid;
      int retType = type_Void;
      OperandReg ret = null;
      if (ji.targetType == VOID) {
        retAndParams = new Operand[paramCount];
        notVoid = 0;
        retType = type_Void;
      } else {
        retAndParams = new Operand[paramCount + 1];
        notVoid = 1;
        switch (ji.targetType) {
        case INT:
        case SHORT:
        case CHAR:
        case BYTE:
        case BOOLEAN:
          ret = new OperandRegI();
          retType = type_Int;
          break;
        case FLOAT:
          ret = new OperandRegD32();
          retType = type_Double;
          break;
        case LONG:
          ret = new OperandRegL();
          retType = type_Long;
          break;
        case DOUBLE:
          ret = new OperandRegD64();
          retType = type_Double;
          break;
        case OBJECT:
          ret = new OperandRegO();
          retType = type_Obj;
          break;
        }
        retAndParams[0] = ret;
      }

      int len = paramCount + notVoid;
      for (int j = len - 1; j >= notVoid; j--) {
        retAndParams[j] = stack.pop();
      }

      OperandReg _this;
      if (op == INVOKESTATIC) {
        _this = new OperandReg(TCConstants.opr_regO);
        _this.index = 0;
      } else {
        Operand obj = stack.pop();
        _this = (OperandRegO) GenerateInstruction.promoteOperand(vcode, obj, opr_regO, lineOfPC);
      }

      int idx = GlobalConstantPool.putMethod(ji.className, ji.name, ji.jargs, ji.signature);
      OperandSym sym = new OperandSymO(idx);
      if (ji.name.equals("forName")) // keep track of the list of classes used in forName
      {
        if (retAndParams != null && retAndParams[1] instanceof OperandSymO) {
          OperandSymO s = (OperandSymO) retAndParams[1];
          String n = GlobalConstantPool.getString(s.index);
          J2TC.callForName.addElement(n);
        } else if (!signature.equals("class$(Ljava/lang/String;)")) {
          J2TC.notResolvedForNameFound = true;
        }
      }
      Call call = (Call) GenerateInstruction.newInstruction(vcode, op == INVOKEVIRTUAL ? CALL_virtual : CALL_normal,
          sym, _this, retAndParams, retType == type_Void, lineOfPC);
      call.isStatic = op == INVOKESTATIC;
      tc = call;
      if (ret != null) {
        stack.push(ret);
      }
      break;
    }
    case NEW: //187 (186 is not used)
    {
      BC187_new ji = (BC187_new) i;
      ji.className = replaceTotalCrossLangToJavaLang(ji.className);
      ji.className = removeSuffix4D(ji.className);
      Operand regO = new OperandRegO();
      int index = GlobalConstantPool.putCls(ji.className);
      OperandSymO sym = new OperandSymO(index);
      tc = GenerateInstruction.newInstruction(vcode, pref_NEWOBJ, regO, sym, lineOfPC);
      stack.push(regO);
      break;
    }
    case NEWARRAY: //188
    {
      BC188_newarray ji = (BC188_newarray) i;
      int symIdx = 0;
      switch (ji.arrayType) {
      case 4:
        symIdx = Type_BooleanArray;
        break; // boolean array
      case 5:
        symIdx = Type_CharArray;
        break; // char array
      case 6:
        symIdx = Type_FloatArray;
        break; // float array
      case 7:
        symIdx = Type_DoubleArray;
        break; // double array
      case 8:
        symIdx = Type_ByteArray;
        break; // byte array
      case 9:
        symIdx = Type_ShortArray;
        break; // short array
      case 10:
        symIdx = Type_IntArray;
        break; // int array
      case 11:
        symIdx = Type_LongArray;
        break; // long array
      }
      Operand len = stack.pop();
      Operand sym = new OperandSymO(symIdx);
      Operand reg = new OperandRegO();
      tc = GenerateInstruction.newInstruction(vcode, pref_NEWARRAY, reg, sym, len, lineOfPC);
      stack.push(reg);
      break;
    }
    case ANEWARRAY: //189
    {
      BC189_anewarray ji = (BC189_anewarray) i;
      String classType = ji.classType.charAt(0) == '[' ? "[" + ji.classType : "[L" + ji.classType + ";";
      int symIdx = GlobalConstantPool.putClsOrParam(classType);
      Operand len = stack.pop();
      Operand sym = new OperandSymO(symIdx);
      Operand reg = new OperandRegO();
      tc = GenerateInstruction.newInstruction(vcode, pref_NEWARRAY, reg, sym, len, lineOfPC);
      stack.push(reg);
      break;
    }
    case ARRAYLENGTH: //190
    {
      OperandReg regBase = (OperandReg) stack.pop();
      OperandArrayAccess base = new OperandArrayAccess(opr_arlen, regBase, null);
      Operand reg = new OperandRegI();
      tc = GenerateInstruction.newInstruction(vcode, pref_MOV, reg, base, lineOfPC);
      stack.push(reg);
      break;
    }
    case ATHROW: //191
    {
      /*
             With this code:
      
               Lock objectLock = new Lock();
               String connection = "";
      
               public String recoverTable() {
                  synchronized (objectLock) {
                      return connection;
                  }
               }
      
             Eclipse has an optimization that removes an extra move of the lock from the local
             variables to the stack. This is the code it generates:
      
             14: aload_1
             15: monitorexit
             16: athrow
      
             At line 14, the stack holds the exception at top, and the lock at local variable 1.
             However, the TC converter isn't aware that the exception is at the top of the stack,
             so we check it and provide that extra operand that tells it.
       */
      OperandReg regO = stack.empty() ? new OperandRegO() : (OperandReg) stack.pop();
      Reg code = new Reg(THROW, lineOfPC, regO.index);
      vcode.addElement(code);
      stack.push(regO);
      break;
    }
    case JCHECKCAST: //192
    {
      BC192_checkcast ji = (BC192_checkcast) i;
      Operand regO = stack.pop();
      int index = GlobalConstantPool.putClsOrParam(ji.targetClass);
      OperandSymO sym = new OperandSymO(index);
      tc = GenerateInstruction.newInstruction(vcode, pref_CHECKCAST, regO, sym, lineOfPC);
      stack.push(regO);
      break;
    }
    case JINSTANCEOF: //193
    {
      BC193_instanceof ji = (BC193_instanceof) i;
      Operand regO = stack.pop();
      OperandReg regI = new OperandRegI();
      int index = GlobalConstantPool.putClsOrParam(ji.targetClass);
      OperandSymO sym = new OperandSymO(index);
      tc = GenerateInstruction.newInstruction(vcode, pref_INSTANCEOF, regI, regO, sym, lineOfPC);
      stack.push(regI);
      break;
    }
    case MONITORENTER: //194
    {
      Operand reg = stack.pop();
      Reg code;

      if (reg instanceof OperandReg) {
        code = new Reg(MONITOR_Enter, lineOfPC, ((OperandReg) reg).index);
      } else {
        code = new Reg(MONITOR_Enter2, lineOfPC, ((OperandSymO) reg).index);
      }
      vcode.addElement(code);
      tc = code;
      break;
    }
    case MONITOREXIT: //195
    {
      Operand reg = (Operand) stack.pop();
      Reg code;

      if (reg instanceof OperandReg) {
        code = new Reg(MONITOR_Exit, lineOfPC, ((OperandReg) reg).index);
      } else {
        code = new Reg(MONITOR_Exit2, lineOfPC, ((OperandSymO) reg).index);
      }
      vcode.addElement(code);
      tc = code;
      break;
    }
    case WIDE: //196
    {
      BC196_wide ji = (BC196_wide) i;
      tc = processWIDE(bcs, ji.widen, stack, vcode, isStaticInitializer);
      break;
    }
    case MULTIANEWARRAY: //197
    {
      BC197_multinewarray ji = (BC197_multinewarray) i;
      ji.className = replaceTotalCrossLangToJavaLang(ji.className);
      ji.className = removeSuffix4D(ji.className);
      int symIdx = GlobalConstantPool.putClsOrParam(ji.className);
      OperandSym sym = new OperandSymO(symIdx);
      OperandReg reg = new OperandRegO();
      Operand dims[] = new Operand[ji.dimCount];
      for (int j = ji.dimCount - 1; j >= 0; j--) {
        dims[j] = stack.pop();
      }
      tc = GenerateInstruction.newInstruction(vcode, reg, sym, dims, lineOfPC);
      stack.push(reg);
      break;
    }
    case IFNULL: //198
    case IFNONNULL: //199
    {
      ConditionalBranch ji = (ConditionalBranch) i;
      Operand value1 = stack.pop();
      Operand value2 = new OperandNull();
      OperandConstant target = new OperandConstant32(isTargetOfGoto[ji.jumpIfTrue], type_Int);
      int prefix = 0;
      switch (op) {
      case IFNULL:
        prefix = pref_JEQ;
        break;
      case IFNONNULL:
        prefix = pref_JNE;
        break;
      }
      stackOfBranch(vcode, bcs, stack, ji.jumpIfTrue);
      tc = GenerateInstruction.newInstruction(vcode, prefix, value1, value2, target, lineOfPC);
      break;
    }
    case BREAKPOINT: //202
    {
      //BC202_breakpoint ji = (BC202_breakpoint)i;
      break;
    }
    }
    lastBytecode = i;
    indexOfCurrentBytecode += i.pcInc;
    return tc;
  }

  private static int newInstructionsCount(Vector v, int index1, int index2) {
    if (index1 > index2) {
      int aux = index1;
      index1 = index2;
      index2 = aux;
    }

    int count = 0;
    for (int i = 0; i < v.size(); i++) {
      TCValue e = (TCValue) v.items[i];
      int value = e.asInt;
      if (value >= index1 && value < index2) {
        count++;
      }
    }
    return count;
  }

  public static void updateBranchs(Vector vcode) throws Exception {
    Vector vc = new Vector(64);

    try {
      int tccodeCount = 0;
      for (int j = 0; j < vcode.size(); j++) {
        Instruction tc = (Instruction) vcode.items[j];
        int op = tc.opcode;
        switch (op) {
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
          Reg_reg_s12 i = (Reg_reg_s12) tc;
          int branch = i.s12;
          branch = htBytecodeIndex.get(branch);
          i.s12 = branch - tccodeCount;
          vc.addElement(tc);
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
          Reg_s6_desloc i = (Reg_s6_desloc) tc;
          int branch = i.desloc;
          branch = htBytecodeIndex.get(branch);
          i.desloc = branch - tccodeCount;
          vc.addElement(tc);
          break;
        }

        case JEQ_regI_sym:
        case JNE_regI_sym: {
          Reg_sym_sdesloc i = (Reg_sym_sdesloc) tc;
          int branch = i.desloc;
          branch = htBytecodeIndex.get(branch);
          int desloc = branch - tccodeCount;
          if (desloc < -32 || desloc > 31) {
            newInsts.addElement(new TCValue(j));
            OperandRegI target = new OperandRegI();
            OperandSym sym = new OperandSymI(i.sym);
            GenerateInstruction.newInstruction(vc, pref_MOV, target, sym, tc.line);
            int reg = i.reg;
            switch (op) {
            case JEQ_regI_sym:
              op = JEQ_regI_regI;
              break;
            case JNE_regI_sym:
              op = JNE_regI_regI;
              break;
            case JGE_regI_arlen:
              op = JGE_regI_regI;
              break;
            }
            Reg_reg_s12 i2 = new Reg_reg_s12(op, i.line, reg, target.index, desloc);
            tc = i2;
          } else {
            i.desloc = desloc;
          }
          vc.addElement(tc);
          break;
        }

        case JGE_regI_arlen: {
          Reg_sym_sdesloc i = (Reg_sym_sdesloc) tc;
          int branch = i.desloc;
          branch = htBytecodeIndex.get(branch);
          int desloc = branch - tccodeCount;
          i.desloc = desloc;
          vc.addElement(tc);
          break;
        }
        case JUMP_s24: {
          S24 i = (S24) tc;
          int branch = i.s24;
          branch = htBytecodeIndex.get(branch);
          i.s24 = branch - tccodeCount;
          vc.addElement(tc);
          break;
        }
        case SWITCH: {
          Switch_reg i = (Switch_reg) tc;
          vc.addElement(tc);
          int switchIdx = tccodeCount;
          int n = i.n;

          Two16 p1 = (Two16) i.params[0];
          int v1 = p1.v1;
          int branch = htBytecodeIndex.get(v1);
          int target = branch - switchIdx;
          p1.v1 = target;

          int k = n;
          for (int x = 0; x < n; x++) {
            if ((x & 1) == 1) {
              p1 = (Two16) i.params[k];
              branch = htBytecodeIndex.get(p1.v2);
              target = branch - switchIdx;
              p1.v2 = target;
            } else {
              p1 = (Two16) i.params[++k];
              branch = htBytecodeIndex.get(p1.v1);
              target = branch - switchIdx;
              p1.v1 = target;
            }
          }
          break;
        }
        default:
          vc.addElement(tc);
        }
        tccodeCount += tc.len;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    vcode.removeAllElements();
    for (int j = 0; j < vc.size(); j++) {
      vcode.addElement(vc.items[j]);
    }

    int tccodeCount = 0;
    for (int j = 0; j < vcode.size(); j++) {
      Instruction tc = (Instruction) vcode.items[j];
      int op = tc.opcode;
      switch (op) {
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
        Reg_reg_s12 i = (Reg_reg_s12) tc;
        int branch = i.s12;
        i.s12 = newInstructionsCount(newInsts, tccodeCount, tccodeCount + branch) + branch;
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
        Reg_s6_desloc i = (Reg_s6_desloc) tc;
        int branch = i.desloc;
        i.desloc = newInstructionsCount(newInsts, tccodeCount, tccodeCount + branch) + branch;
        break;
      }

      case JEQ_regI_sym:
      case JNE_regI_sym:
      case JGE_regI_arlen: {
        Reg_sym_sdesloc i = (Reg_sym_sdesloc) tc;
        int branch = i.desloc;
        i.desloc = newInstructionsCount(newInsts, tccodeCount, tccodeCount + branch) + branch;
        break;
      }
      case JUMP_s24: {
        S24 i = (S24) tc;
        int branch = i.s24;
        i.s24 = newInstructionsCount(newInsts, tccodeCount, tccodeCount + branch) + branch;
        break;
      }
      case SWITCH: {
        Switch_reg i = (Switch_reg) tc;
        int n = i.n;
        // default address
        Two16 p1 = (Two16) i.params[0];
        int v1 = p1.v1;
        int target = v1;
        p1.v1 = newInstructionsCount(newInsts, tccodeCount + 1, tccodeCount + 1 + target) + target;

        // destionation address
        int k = n;
        for (int x = 0; x < n; x++) {
          if ((x & 1) == 1) {
            p1 = (Two16) i.params[k];
            target = p1.v2;
            p1.v2 = newInstructionsCount(newInsts, tccodeCount + k, tccodeCount + k + target) + target;
          } else {
            p1 = (Two16) i.params[++k];
            target = p1.v1;
            p1.v1 = newInstructionsCount(newInsts, tccodeCount + k, tccodeCount + k + target) + target;
          }
        }
        break;
      }
      }
      tccodeCount += tc.len;
    }
  }

  private static boolean existsField(JavaField[] jfs, String name) {
    if (jfs != null) {
      int fieldCount = jfs.length;
      for (int i = 0; i < fieldCount; i++) {
        JavaField jf = jfs[i];
        if (jf.name.equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  public static int getMtdIndex(String sign) {
    int methodCount = methodIndexes.size();
    for (int i = 0; i < methodCount; i++) {
      String name = (String) methodIndexes.items[i];
      if (name.equals(sign)) {
        return i;
      }
    }

    return -1;
  }

  private static boolean setExceptionHandler(TCException[] e, int pc, int regO) {
    boolean found = false;
    if (e != null) {
      for (int i = 0; i < e.length; i++) {
        if (e[i].handlerPC == pc) {
          e[i].regO = regO;
          found = true;
        }
      }
    }
    return found;
  }

  public static TCException[] updatePCsOfExceptionHandler(TCException[] tces) throws Exception {
    if (tces != null) {
      Vector excp = new Vector(8);
      for (int j = 0; j < tces.length; j++) {
        TCException e = tces[j];
        int pc = htBytecodeIndex.get(e.startPC);
        e.startPC = pc + newInstructionsCount(newInsts, 0, pc);
        pc = htBytecodeIndex.get(e.endPC);
        e.endPC = pc + newInstructionsCount(newInsts, 0, pc) - 1; // sub "-1" because the endPC is exclusive ==> interval [startPC, endPC)
        pc = htBytecodeIndex.get(e.handlerPC);
        e.handlerPC = pc + newInstructionsCount(newInsts, 0, pc);

        // stores only the valid exceptions (startPC <= endPC)
        if (e.startPC <= e.endPC) {
          excp.addElement(e);
        }
      }

      if (excp.size() > 0) {
        tces = new TCException[excp.size()];
        Vm.arrayCopy(excp.items, 0, tces, 0, excp.size());
      }
    }

    return tces;
  }

  public static boolean hasMethodWith4D(JavaClass jc, String sign) {
    JavaMethod[] jms = jc.methods;
    int methodCount = jms.length;
    for (int i = 0; i < methodCount; i++) {
      JavaMethod jm = jms[i];
      if (jm.signature.equals(sign)) {
        return true;
      }
    }

    return false;
  }

  public static String removeSuffix4D(String name) {
    int i;
    if (name.endsWith("4D")) {
      return name.substring(0, name.length() - 2);
    } else if ((i = name.indexOf("4D$")) >= 0) {
      name = name.substring(0, i).concat(name.substring(i + 2));
    }
    return name;
  }

  public static String removeSuffix4D(String name, String sign) {
    int i, len = name.length();
    if (name.endsWith("4D")) {
      return name.substring(0, len - 2) + sign.substring(len);
    } else if ((i = name.indexOf("4D$")) >= 0) {
      name = name.substring(0, i).concat(name.substring(i + 2)) + sign.substring(len);
    }
    return sign;
  }

  public static String replaceTotalCrossLangToJavaLang(String name) {
    if (name.startsWith("totalcross/lang/")) {
      return "java/lang/" + name.substring(16);
    }
    if (name.startsWith("totalcross/util/") && name.contains("4D") && !name.contains("/zip/")) {
      return name.replace("totalcross", "java");
    }
    if (name.startsWith("jdkcompat") && name.contains("4D")) {
      return name.replaceFirst("jdkcompat", "java");
    }
    return name;
  }

  public static void CheckIfIsGotoTarget(int BCIndex, ByteCode bc, OperandStack stack, Vector vcode) {
    OperandStack s = null;
    try {
      s = (OperandStack) htStackOfBranch.get(bc);
    } catch (Exception e) {
    }
    if (s != null) {
      if (lastBytecode.bc == GOTO) {
        stack.copy(s);
      } else {
        OperandStack s2 = stack.cloneStack();
        OperandStack s3 = s.cloneStack();
        while (s2.count() > s3.count()) {
          s2.pop();
        }
        stack.clear();
        if (s2.count() > 0) {
          do {
            Operand source = s2.pop();
            Operand target = s3.pop();
            GenerateInstruction.newInstruction(vcode, pref_MOV, target, source, lineOfPC);
            stack.push(target);
          } while (!s3.empty());
        }
        stack.invert();
      }
    } else if (isTargetOfGoto[BCIndex] != 0 && (!stack.empty())) {
      OperandStack s2 = stack.cloneStack();
      stack.clear();
      OperandReg reg = null;
      do {
        Operand opr = s2.pop();
        if (OperandKind.isTypeI(opr.kind)) {
          reg = new OperandRegI();
        } else if (OperandKind.isTypeO(opr.kind)) {
          reg = new OperandRegO();
        } else if (OperandKind.isTypeD(opr.kind)) {
          reg = new OperandRegD64();
        } else if (OperandKind.isTypeL(opr.kind)) {
          reg = new OperandRegL();
        }

        GenerateInstruction.newInstruction(vcode, pref_MOV, reg, opr, lineOfPC);
        stack.push(reg);
      } while (!s2.empty());

      stack.invert();
      htStackOfBranch.put(bc, stack.cloneStack());
    }
  }

  private static void stackOfBranch(Vector vcode, ByteCode[] bcs, OperandStack stack, int BCIndex) {
    int i = 0;
    int count = 0;
    while (count < BCIndex) {
      count += bcs[i++].pcInc;
    }

    if (!stack.empty()) {
      OperandStack s = null;
      try {
        s = (OperandStack) htStackOfBranch.get(bcs[i]);
      } catch (Exception e) {
      }

      OperandStack s2 = stack.cloneStack();
      stack.clear();

      OperandReg reg = null;
      if (s == null) {
        do {
          Operand opr = s2.pop();
          if (OperandKind.isTypeI(opr.kind)) {
            reg = new OperandRegI();
          } else if (OperandKind.isTypeO(opr.kind)) {
            reg = new OperandRegO();
          } else if (OperandKind.isTypeD(opr.kind)) {
            reg = new OperandRegD64();
          } else if (OperandKind.isTypeL(opr.kind)) {
            reg = new OperandRegL();
          }

          GenerateInstruction.newInstruction(vcode, pref_MOV, reg, opr, lineOfPC);
          stack.push(reg);
        } while (!s2.empty());

        stack.invert();
        htStackOfBranch.put(bcs[i], stack.cloneStack());
      } else {
        OperandStack s3 = s.cloneStack();
        if (s2.count() > 0 && s3.count() > 0) {
          do {
            Operand source = s2.pop();
            Operand target = s3.pop();
            GenerateInstruction.newInstruction(vcode, pref_MOV, target, source, lineOfPC);
            stack.push(target);
          } while (!s3.empty());
        }
        stack.invert();
      }
    } else {
      htStackOfBranch.put(bcs[i], stack.cloneStack());
    }
  }

  public static void setGotos(ByteCode[] bcs) {
    int codeLength = 0;
    int length = bcs.length;

    for (int i = 0; i < length; i++) {
      ByteCode bc = bcs[i];
      codeLength += bc.pcInc;
    }

    isTargetOfGoto = new int[codeLength];
    totalcross.sys.Convert.fill(isTargetOfGoto, 0, codeLength, 0);

    for (int i = 0; i < length; i++) {
      ByteCode bc = bcs[i];
      int op = bc.bc;
      switch (op) {
      case IFEQ: //153
      case IFNE: //154
      case IFLT: //155
      case IFGE: //156
      case IFGT: //157
      case IFLE: //158
      {
        ConditionalBranch ji = (ConditionalBranch) bc;
        setGotoIndex(ji.jumpIfTrue);
        break;
      }
      case IF_ICMPEQ: //159
      case IF_ICMPNE: //160
      case IF_ICMPLT: //161
      case IF_ICMPGE: //162
      case IF_ICMPGT: //163
      case IF_ICMPLE: //164
      case IF_ACMPEQ: //165
      case IF_ACMPNE: //166
      {
        ConditionalBranch ji = (ConditionalBranch) bc;
        setGotoIndex(ji.jumpIfTrue);
        break;
      }
      case GOTO: //167
      case GOTO_W: //200
      {
        Branch ji = (Branch) bc;
        setGotoIndex(ji.jumpTo);
        break;
      }
      case JSR: //168
      case JSR_W: //201
      {
        Branch ji = (Branch) bc;
        setGotoIndex(ji.jumpTo);
        break;
      }
      case IFNULL: //198
      case IFNONNULL: //199
      {
        ConditionalBranch ji = (ConditionalBranch) bc;
        setGotoIndex(ji.jumpIfTrue);
        break;
      }
      }
    }
  }

  private static int getLineOfPC(int pc) {
    if (javaCodeCurrent.lineNumberPC != null) {
      int[] pcs = javaCodeCurrent.lineNumberPC;
      int i = 0;
      while (i < pcs.length && pcs[i] < pc) {
        i++;
      }
      if (i < pcs.length && pc == pcs[i]) {
        return javaCodeCurrent.lineNumberLine[i];
      }
      return javaCodeCurrent.lineNumberLine[i - 1];
    }
    return 0;
  }

  public static void generateLineNumbers(TCMethod tcm) {
    if (tcm.code != null) {
      Vector v = new Vector(64);
      int li = -1;
      for (int i = 0; i < tcm.code.length; i++) {
        TCCode tc = (TCCode) tcm.code[i];
        if (tc.line != li) {
          li = tc.line;
          v.addElement(new TCLineNumber(i, li));
        }
      }
      tcm.lineNumbers = new TCLineNumber[v.size()];
      v.copyInto(tcm.lineNumbers);
    }
  }

  public static int countOfTCCode(Vector vcode) {
    int count = 0;
    for (int j = 0; j < vcode.size(); j++) {
      Instruction i = (Instruction) vcode.items[j];
      count += i.len;
    }
    return count;
  }
}
