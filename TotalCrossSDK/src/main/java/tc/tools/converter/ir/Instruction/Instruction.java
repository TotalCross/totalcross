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
package tc.tools.converter.ir.Instruction;

import tc.tools.converter.ConverterException;
import tc.tools.converter.TCConstants;
import tc.tools.converter.ir.BasicBlock;
import tc.tools.converter.ir.BitSet;
import tc.tools.converter.regalloc.AdjListNode;
import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class Instruction implements TCConstants {
  public int len = 1; // method, switch and newarray_multi parameters must have len=0
  public int line; // line number in the original source file.
  public int opcode;
  public boolean belongsTry;

  public Instruction(int op, int line) {
    opcode = op;
    if (line != -999) {
      this.line = line;
    }
  }

  public Instruction(int op) {
    opcode = op;
  }

  @Override
  public String toString() {
    return TCConstants.bcTClassNames[opcode];
  }

  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    vcode.addElement(tc);
  }

  public BitSet[] defsAndUses(BasicBlock bb, int iCount, int dCount, int oCount) {
    int op = this.opcode;
    BitSet[] bits = new BitSet[6]; // useI, use64, useO, defI, def64, defO
    bits[0] = new BitSet(iCount);
    bits[3] = new BitSet(iCount);
    bits[1] = new BitSet(dCount);
    bits[4] = new BitSet(dCount);
    bits[2] = new BitSet(oCount);
    bits[5] = new BitSet(oCount);
    switch (op) {
    //reg_reg
    case MOV_regI_regI:
    case CONV_regIb_regI:
    case CONV_regIc_regI:
    case CONV_regIs_regI: {
      Reg_reg inst = (Reg_reg) this;
      bits[0].on(inst.r1);
      bits[3].on(inst.r0);
      break;
    }
    case MOV_regO_regO: {
      Reg_reg inst = (Reg_reg) this;
      bits[2].on(inst.r1);
      bits[5].on(inst.r0);
      break;
    }
    case MOV_reg64_reg64:
    case CONV_regL_regD:
    case CONV_regD_regL: {
      Reg_reg inst = (Reg_reg) this;
      bits[1].on(inst.r1);
      bits[4].on(inst.r0);
      break;
    }
    case CONV_regI_regL:
    case CONV_regI_regD: {
      Reg_reg inst = (Reg_reg) this;
      bits[1].on(inst.r1);
      bits[3].on(inst.r0);
      break;
    }
    case CONV_regL_regI: {
      Reg_reg inst = (Reg_reg) this;
      bits[0].on(inst.r1);
      bits[4].on(inst.r0);
      break;
    }
    case CONV_regD_regI: {
      Reg_reg inst = (Reg_reg) this;
      bits[0].on(inst.r1);
      bits[4].on(inst.r0);
      break;
    }

    //reg_sym
    case NEWOBJ: {
      Reg_sym inst = (Reg_sym) this;
      bits[5].on(inst.reg);
      break;
    }
    case MOV_regI_sym: {
      Reg_sym inst = (Reg_sym) this;
      bits[3].on(inst.reg);
      break;
    }
    case MOV_regO_sym: {
      Reg_sym inst = (Reg_sym) this;
      bits[5].on(inst.reg);
      break;
    }
    case MOV_regD_sym:
    case MOV_regL_sym: {
      Reg_sym inst = (Reg_sym) this;
      bits[4].on(inst.reg);
      break;
    }

    //s18_reg
    case MOV_regI_s18: {
      S18_reg inst = (S18_reg) this;
      bits[3].on(inst.reg);
      break;
    }
    case MOV_regL_s18:
    case MOV_regD_s18: {
      S18_reg inst = (S18_reg) this;
      bits[4].on(inst.reg);
      break;
    }
    case MOV_regO_null: {
      S18_reg inst = (S18_reg) this;
      bits[5].on(inst.reg);
      break;
    }

    //field_reg
    case MOV_regI_field: {
      Field_reg inst = (Field_reg) this;
      bits[3].on(inst.reg);
      bits[2].on(inst._this);
      break;
    }
    case MOV_regO_field: {
      Field_reg inst = (Field_reg) this;
      bits[5].on(inst.reg);
      bits[2].on(inst._this);
      break;
    }
    case MOV_reg64_field: {
      Field_reg inst = (Field_reg) this;
      bits[4].on(inst.reg);
      bits[2].on(inst._this);
      break;
    }
    case MOV_field_regI: {
      Field_reg inst = (Field_reg) this;
      bits[0].on(inst.reg);
      bits[2].on(inst._this);
      break;
    }
    case MOV_field_regO: {
      Field_reg inst = (Field_reg) this;
      bits[2].on(inst.reg);
      bits[2].on(inst._this);
      break;
    }
    case MOV_field_reg64: {
      Field_reg inst = (Field_reg) this;
      bits[1].on(inst.reg);
      bits[2].on(inst._this);
      break;
    }

    //static_reg
    case MOV_regI_static: {
      Static_reg inst = (Static_reg) this;
      bits[3].on(inst.reg);
      break;
    }
    case MOV_regO_static: {
      Static_reg inst = (Static_reg) this;
      bits[5].on(inst.reg);
      break;
    }
    case MOV_reg64_static: {
      Static_reg inst = (Static_reg) this;
      bits[4].on(inst.reg);
      break;
    }
    case MOV_static_regI: {
      Static_reg inst = (Static_reg) this;
      bits[0].on(inst.reg);
      break;
    }
    case MOV_static_regO: {
      Static_reg inst = (Static_reg) this;
      bits[2].on(inst.reg);
      break;
    }
    case MOV_static_reg64: {
      Static_reg inst = (Static_reg) this;
      bits[1].on(inst.reg);
      break;
    }

    //reg_ar
    case MOV_regI_aru:
    case MOV_regI_arc:
    case MOV_regI_arlen:
    case MOV_regIb_arc:
    case MOV_reg16_arc:
    case MOV_regIb_aru:
    case MOV_reg16_aru: {
      Reg_ar inst = (Reg_ar) this;
      bits[3].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }
    case MOV_regO_aru:
    case MOV_regO_arc: {
      Reg_ar inst = (Reg_ar) this;
      bits[5].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }
    case MOV_reg64_aru:
    case MOV_reg64_arc: {
      Reg_ar inst = (Reg_ar) this;
      bits[4].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }
    case MOV_arc_regI: {
      Reg_ar inst = (Reg_ar) this;
      bits[0].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }
    case MOV_arc_regO: {
      Reg_ar inst = (Reg_ar) this;
      bits[2].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }
    case MOV_arc_reg64: {
      Reg_ar inst = (Reg_ar) this;
      bits[1].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }
    case MOV_aru_regI:
    case MOV_arc_regIb:
    case MOV_arc_reg16:
    case MOV_aru_regIb:
    case MOV_aru_reg16: {
      Reg_ar inst = (Reg_ar) this;
      bits[0].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }
    case MOV_aru_regO: {
      Reg_ar inst = (Reg_ar) this;
      bits[2].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }
    case MOV_aru_reg64: {
      Reg_ar inst = (Reg_ar) this;
      bits[1].on(inst.reg);
      bits[2].on(inst.base);
      if (inst.idx > -1) {
        bits[0].on(inst.idx);
      }
      break;
    }

    //inc_reg
    case INC_regI: {
      Inc inst = (Inc) this;
      bits[0].on(inst.reg);
      bits[3].on(inst.reg);
      break;
    }

    //reg_reg_reg
    case ADD_regI_regI_regI:
    case SUB_regI_regI_regI:
    case MUL_regI_regI_regI:
    case DIV_regI_regI_regI:
    case MOD_regI_regI_regI:
    case SHR_regI_regI_regI:
    case SHL_regI_regI_regI:
    case USHR_regI_regI_regI:
    case AND_regI_regI_regI:
    case OR_regI_regI_regI:
    case XOR_regI_regI_regI: {
      Reg_reg_reg inst = (Reg_reg_reg) this;
      bits[0].on(inst.r1);
      bits[0].on(inst.r2);
      bits[3].on(inst.r0);
      break;
    }
    case ADD_regD_regD_regD:
    case ADD_regL_regL_regL:
    case SUB_regD_regD_regD:
    case SUB_regL_regL_regL:
    case MUL_regD_regD_regD:
    case MUL_regL_regL_regL:
    case DIV_regD_regD_regD:
    case DIV_regL_regL_regL:
    case MOD_regD_regD_regD:
    case MOD_regL_regL_regL:
    case SHR_regL_regL_regL:
    case SHL_regL_regL_regL:
    case USHR_regL_regL_regL:
    case AND_regL_regL_regL:
    case OR_regL_regL_regL:
    case XOR_regL_regL_regL: {
      Reg_reg_reg inst = (Reg_reg_reg) this;
      bits[1].on(inst.r1);
      bits[1].on(inst.r2);
      bits[4].on(inst.r0);
      break;
    }

    //reg_reg_s12
    case ADD_regI_s12_regI:
    case SUB_regI_s12_regI:
    case MUL_regI_regI_s12:
    case DIV_regI_regI_s12:
    case MOD_regI_regI_s12:
    case SHR_regI_regI_s12:
    case SHL_regI_regI_s12:
    case USHR_regI_regI_s12:
    case AND_regI_regI_s12:
    case XOR_regI_regI_s12:
    case OR_regI_regI_s12: {
      Reg_reg_s12 inst = (Reg_reg_s12) this;
      bits[0].on(inst.r1);
      bits[3].on(inst.r0);
      break;
    }
    case JEQ_regI_regI:
    case JNE_regI_regI:
    case JLT_regI_regI:
    case JLE_regI_regI:
    case JGT_regI_regI:
    case JGE_regI_regI: {
      Reg_reg_s12 inst = (Reg_reg_s12) this;
      bits[0].on(inst.r0);
      bits[0].on(inst.r1);
      break;
    }
    case JEQ_regO_regO:
    case JNE_regO_regO: {
      Reg_reg_s12 inst = (Reg_reg_s12) this;
      bits[2].on(inst.r0);
      bits[2].on(inst.r1);
      break;
    }
    case JEQ_regL_regL:
    case JEQ_regD_regD:
    case JNE_regL_regL:
    case JNE_regD_regD:
    case JLT_regL_regL:
    case JLT_regD_regD:
    case JLE_regL_regL:
    case JLE_regD_regD:
    case JGT_regL_regL:
    case JGT_regD_regD:
    case JGE_regL_regL:
    case JGE_regD_regD: {
      Reg_reg_s12 inst = (Reg_reg_s12) this;
      bits[1].on(inst.r0);
      bits[1].on(inst.r1);
      break;
    }

    //reg_reg_sym
    case ADD_regI_regI_sym: {
      Reg_reg_sym inst = (Reg_reg_sym) this;
      bits[0].on(inst.r1);
      bits[3].on(inst.r0);
      break;
    }

    //reg_s6_desloc
    case JEQ_regI_s6:
    case JNE_regI_s6:
    case JLT_regI_s6:
    case JLE_regI_s6:
    case JGT_regI_s6:
    case JGE_regI_s6: {
      Reg_s6_desloc inst = (Reg_s6_desloc) this;
      bits[0].on(inst.reg);
      break;
    }
    case JEQ_regO_null:
    case JNE_regO_null: {
      Reg_s6_desloc inst = (Reg_s6_desloc) this;
      bits[2].on(inst.reg);
      break;
    }

    //reg_sym_desloc
    case JEQ_regI_sym:
    case JNE_regI_sym: {
      Reg_sym_sdesloc inst = (Reg_sym_sdesloc) this;
      bits[0].on(inst.reg);
      break;
    }

    //reg_arl_s12
    case JGE_regI_arlen: {
      Reg_arl_s12 inst = (Reg_arl_s12) this;
      bits[2].on(inst.base);
      bits[0].on(inst.regI);
      break;
    }

    //instanceof
    case INSTANCEOF: {
      Instanceof inst = (Instanceof) this;
      bits[2].on(inst.regO);
      bits[3].on(inst.regI);
      break;
    }
    case CHECKCAST: {
      Instanceof inst = (Instanceof) this;
      bits[2].on(inst.regO);
      break;
    }

    //reg
    case RETURN_regI:
    case JUMP_regI: {
      Reg inst = (Reg) this;
      bits[0].on(inst.reg);
      break;
    }
    case RETURN_regO:
    case TEST_regO:
    case THROW:
    case MONITOR_Enter:
    case MONITOR_Exit:
    case MONITOR_Enter2:
    case MONITOR_Exit2: {
      Reg inst = (Reg) this;
      bits[2].on(inst.reg);
      break;
    }
    case RETURN_reg64: {
      Reg inst = (Reg) this;
      bits[1].on(inst.reg);
      break;
    }

    //sym
    case RETURN_symI:
    case RETURN_symO:
    case RETURN_symD:
    case RETURN_symL: {
      break;
    }

    //switch_reg
    case SWITCH: {
      Switch_reg inst = (Switch_reg) this;
      bits[0].on(inst.key);
      break;
    }

    //newarray
    case NEWARRAY_len: {
      New_array inst = (New_array) this;
      bits[5].on(inst.regO);
      break;
    }
    case NEWARRAY_regI: {
      New_array inst = (New_array) this;
      bits[5].on(inst.regO);
      bits[0].on(inst.arrayLen);
      break;
    }
    case NEWARRAY_multi: {
      New_array inst = (New_array) this;
      bits[5].on(inst.regO);
      for (int i = 0; i < inst.params.length; i++) {
        Params p = (Params) inst.params[i];
        if (p.typeOfParam1 == type_Int) {
          bits[0].on(p.param1);
        }
        if (p.typeOfParam2 == type_Int) {
          bits[0].on(p.param2);
        }
        if (p.typeOfParam3 == type_Int) {
          bits[0].on(p.param3);
        }
        if (p.typeOfParam4 == type_Int) {
          bits[0].on(p.param4);
        }
      }
      break;
    }

    //Call
    case CALL_normal:
    case CALL_virtual: {
      Call inst = (Call) this;
      if (!inst.isStatic) {
        bits[2].on(inst._this);
      }
      switch (inst.retOrParamType) {
      case type_Int:
        bits[inst.isVoid ? 0 : 3].on(inst.retOrParam);
        break;
      case type_Double:
      case type_Long:
        bits[inst.isVoid ? 1 : 4].on(inst.retOrParam);
        break;
      case type_Obj:
        bits[inst.isVoid ? 2 : 5].on(inst.retOrParam);
        break;
      }
      if (inst.params != null) {
        for (int i = 0; i < inst.params.length; i++) {
          Params p = (Params) inst.params[i];
          switch (p.typeOfParam1) {
          case type_Int:
            bits[0].on(p.param1);
            break;
          case type_Double:
          case type_Long:
            bits[1].on(p.param1);
            break;
          case type_Obj:
            bits[2].on(p.param1);
            break;
          }
          switch (p.typeOfParam2) {
          case type_Int:
            bits[0].on(p.param2);
            break;
          case type_Double:
          case type_Long:
            bits[1].on(p.param2);
            break;
          case type_Obj:
            bits[2].on(p.param2);
            break;
          }
          switch (p.typeOfParam3) {
          case type_Int:
            bits[0].on(p.param3);
            break;
          case type_Double:
          case type_Long:
            bits[1].on(p.param3);
            break;
          case type_Obj:
            bits[2].on(p.param3);
            break;
          }
          switch (p.typeOfParam4) {
          case type_Int:
            bits[0].on(p.param4);
            break;
          case type_Double:
          case type_Long:
            bits[1].on(p.param4);
            break;
          case type_Obj:
            bits[2].on(p.param4);
            break;
          }
        }
      }
      break;
    }

    case RETURN_void:
    case JUMP_s24:
    case RETURN_s24I:
    case RETURN_null:
    case RETURN_s24D:
    case RETURN_s24L: {
      break;
    }

    default: {
      throw new ConverterException("Instruction.defsAndUses -> Opcode " + op + " is not being handled.");
      //case ADD_regI_arc_s6:
      //case ADD_regI_aru_s6:
      //case ADD_aru_regI_s6:
      //case AND_regI_aru_s6:
      //case DECJGTZ_regI:
      //case DECJGEZ_regI:
    }
    }
    if (belongsTry) {
      int def = bits[3].first();
      if (def >= 0 && bb.outI.isOn(def)) {
        bits[0].on(bits[3]);
      }
      def = bits[4].first();
      if (def >= 0 && bb.outD.isOn(def)) {
        bits[1].on(bits[4]);
      }
      def = bits[5].first();
      if (def >= 0 && bb.outO.isOn(def)) {
        bits[2].on(bits[5]);
      }
    }
    return bits;
  }

  static int contaa;

  public void virtualRegs2PhysicalRegs(AdjListNode[] adjListI, AdjListNode[] adjListD, AdjListNode[] adjListO) {
    int op = this.opcode;
    switch (op) {
    //reg_reg
    case MOV_regI_regI: {
      Reg_reg inst = (Reg_reg) this;
      inst.r0 = adjListI[inst.r0].color;
      inst.r1 = adjListI[inst.r1].color;
      if (inst.r0 == inst.r1) {
        this.opcode = inst.r0 = inst.r1 = 0;
      }
      break;
    }
    case CONV_regIb_regI:
    case CONV_regIc_regI:
    case CONV_regIs_regI: {
      Reg_reg inst = (Reg_reg) this;
      inst.r0 = adjListI[inst.r0].color;
      inst.r1 = adjListI[inst.r1].color;
      break;
    }
    case MOV_regO_regO: {
      Reg_reg inst = (Reg_reg) this;
      inst.r0 = adjListO[inst.r0].color;
      inst.r1 = adjListO[inst.r1].color;
      if (inst.r0 == inst.r1) {
        this.opcode = inst.r0 = inst.r1 = 0;
      }
      break;
    }
    case MOV_reg64_reg64: {
      Reg_reg inst = (Reg_reg) this;
      inst.r0 = adjListD[inst.r0].color;
      inst.r1 = adjListD[inst.r1].color;
      if (inst.r0 == inst.r1) {
        this.opcode = inst.r0 = inst.r1 = 0;
      }
      break;
    }
    case CONV_regL_regD:
    case CONV_regD_regL: {
      Reg_reg inst = (Reg_reg) this;
      inst.r0 = adjListD[inst.r0].color;
      inst.r1 = adjListD[inst.r1].color;
      break;
    }
    case CONV_regI_regL:
    case CONV_regI_regD: {
      Reg_reg inst = (Reg_reg) this;
      inst.r0 = adjListI[inst.r0].color;
      inst.r1 = adjListD[inst.r1].color;
      break;
    }
    case CONV_regL_regI:
    case CONV_regD_regI: {
      Reg_reg inst = (Reg_reg) this;
      inst.r0 = adjListD[inst.r0].color;
      inst.r1 = adjListI[inst.r1].color;
      break;
    }

    //reg_sym
    case NEWOBJ:
    case MOV_regO_sym: {
      Reg_sym inst = (Reg_sym) this;
      inst.reg = adjListO[inst.reg].color;
      break;
    }
    case MOV_regI_sym: {
      Reg_sym inst = (Reg_sym) this;
      inst.reg = adjListI[inst.reg].color;
      break;
    }
    case MOV_regD_sym:
    case MOV_regL_sym: {
      Reg_sym inst = (Reg_sym) this;
      inst.reg = adjListD[inst.reg].color;
      break;
    }

    //s18_reg
    case MOV_regI_s18: {
      S18_reg inst = (S18_reg) this;
      inst.reg = adjListI[inst.reg].color;
      break;
    }
    case MOV_regL_s18:
    case MOV_regD_s18: {
      S18_reg inst = (S18_reg) this;
      inst.reg = adjListD[inst.reg].color;
      break;
    }
    case MOV_regO_null: {
      S18_reg inst = (S18_reg) this;
      inst.reg = adjListO[inst.reg].color;
      break;
    }

    //field_reg
    case MOV_regI_field: {
      Field_reg inst = (Field_reg) this;
      inst.reg = adjListI[inst.reg].color;
      inst._this = adjListO[inst._this].color;
      break;
    }
    case MOV_regO_field: {
      Field_reg inst = (Field_reg) this;
      inst.reg = adjListO[inst.reg].color;
      inst._this = adjListO[inst._this].color;
      break;
    }
    case MOV_reg64_field: {
      Field_reg inst = (Field_reg) this;
      inst.reg = adjListD[inst.reg].color;
      inst._this = adjListO[inst._this].color;
      break;
    }
    case MOV_field_regI: {
      Field_reg inst = (Field_reg) this;
      inst.reg = adjListI[inst.reg].color;
      inst._this = adjListO[inst._this].color;
      break;
    }
    case MOV_field_regO: {
      Field_reg inst = (Field_reg) this;
      inst.reg = adjListO[inst.reg].color;
      inst._this = adjListO[inst._this].color;
      break;
    }
    case MOV_field_reg64: {
      Field_reg inst = (Field_reg) this;
      inst.reg = adjListD[inst.reg].color;
      inst._this = adjListO[inst._this].color;
      break;
    }

    //static_reg
    case MOV_regI_static: {
      Static_reg inst = (Static_reg) this;
      inst.reg = adjListI[inst.reg].color;
      break;
    }
    case MOV_regO_static: {
      Static_reg inst = (Static_reg) this;
      inst.reg = adjListO[inst.reg].color;
      break;
    }
    case MOV_reg64_static: {
      Static_reg inst = (Static_reg) this;
      inst.reg = adjListD[inst.reg].color;
      break;
    }
    case MOV_static_regI: {
      Static_reg inst = (Static_reg) this;
      inst.reg = adjListI[inst.reg].color;
      break;
    }
    case MOV_static_regO: {
      Static_reg inst = (Static_reg) this;
      inst.reg = adjListO[inst.reg].color;
      break;
    }
    case MOV_static_reg64: {
      Static_reg inst = (Static_reg) this;
      inst.reg = adjListD[inst.reg].color;
      break;
    }

    //reg_ar
    case MOV_regI_aru:
    case MOV_regI_arc:
    case MOV_regI_arlen:
    case MOV_regIb_arc:
    case MOV_reg16_arc:
    case MOV_regIb_aru:
    case MOV_reg16_aru:
    case MOV_aru_regI:
    case MOV_arc_regIb:
    case MOV_arc_reg16:
    case MOV_aru_regIb:
    case MOV_aru_reg16: {
      Reg_ar inst = (Reg_ar) this;
      inst.reg = adjListI[inst.reg].color;
      inst.base = adjListO[inst.base].color;
      if (inst.idx > -1) {
        inst.idx = adjListI[inst.idx].color;
      }
      break;
    }
    case MOV_regO_aru:
    case MOV_regO_arc:
    case MOV_arc_regO:
    case MOV_aru_regO: {
      Reg_ar inst = (Reg_ar) this;
      inst.reg = adjListO[inst.reg].color;
      inst.base = adjListO[inst.base].color;
      if (inst.idx > -1) {
        inst.idx = adjListI[inst.idx].color;
      }
      break;
    }
    case MOV_reg64_aru:
    case MOV_reg64_arc:
    case MOV_arc_reg64:
    case MOV_aru_reg64: {
      Reg_ar inst = (Reg_ar) this;
      inst.reg = adjListD[inst.reg].color;
      inst.base = adjListO[inst.base].color;
      if (inst.idx > -1) {
        inst.idx = adjListI[inst.idx].color;
      }
      break;
    }
    case MOV_arc_regI: {
      Reg_ar inst = (Reg_ar) this;
      inst.reg = adjListI[inst.reg].color;
      inst.base = adjListO[inst.base].color;
      if (inst.idx > -1) {
        inst.idx = adjListI[inst.idx].color;
      }
      break;
    }

    //inc_reg
    case INC_regI: {
      Inc inst = (Inc) this;
      inst.reg = adjListI[inst.reg].color;
      break;
    }

    //reg_reg_reg
    case ADD_regI_regI_regI:
    case SUB_regI_regI_regI:
    case MUL_regI_regI_regI:
    case DIV_regI_regI_regI:
    case MOD_regI_regI_regI:
    case SHR_regI_regI_regI:
    case SHL_regI_regI_regI:
    case USHR_regI_regI_regI:
    case AND_regI_regI_regI:
    case OR_regI_regI_regI:
    case XOR_regI_regI_regI: {
      Reg_reg_reg inst = (Reg_reg_reg) this;
      inst.r0 = adjListI[inst.r0].color;
      inst.r1 = adjListI[inst.r1].color;
      inst.r2 = adjListI[inst.r2].color;
      break;
    }
    case ADD_regD_regD_regD:
    case ADD_regL_regL_regL:
    case SUB_regD_regD_regD:
    case SUB_regL_regL_regL:
    case MUL_regD_regD_regD:
    case MUL_regL_regL_regL:
    case DIV_regD_regD_regD:
    case DIV_regL_regL_regL:
    case MOD_regD_regD_regD:
    case MOD_regL_regL_regL:
    case SHR_regL_regL_regL:
    case SHL_regL_regL_regL:
    case USHR_regL_regL_regL:
    case AND_regL_regL_regL:
    case OR_regL_regL_regL:
    case XOR_regL_regL_regL: {
      Reg_reg_reg inst = (Reg_reg_reg) this;
      inst.r0 = adjListD[inst.r0].color;
      inst.r1 = adjListD[inst.r1].color;
      inst.r2 = adjListD[inst.r2].color;
      break;
    }

    //reg_reg_s12
    case ADD_regI_s12_regI:
    case SUB_regI_s12_regI:
    case MUL_regI_regI_s12:
    case DIV_regI_regI_s12:
    case MOD_regI_regI_s12:
    case SHR_regI_regI_s12:
    case SHL_regI_regI_s12:
    case USHR_regI_regI_s12:
    case AND_regI_regI_s12:
    case XOR_regI_regI_s12:
    case OR_regI_regI_s12: {
      Reg_reg_s12 inst = (Reg_reg_s12) this;
      inst.r0 = adjListI[inst.r0].color;
      inst.r1 = adjListI[inst.r1].color;
      break;
    }
    case JEQ_regI_regI:
    case JNE_regI_regI:
    case JLT_regI_regI:
    case JLE_regI_regI:
    case JGT_regI_regI:
    case JGE_regI_regI: {
      Reg_reg_s12 inst = (Reg_reg_s12) this;
      inst.r0 = adjListI[inst.r0].color;
      inst.r1 = adjListI[inst.r1].color;
      break;
    }
    case JEQ_regO_regO:
    case JNE_regO_regO: {
      Reg_reg_s12 inst = (Reg_reg_s12) this;
      inst.r0 = adjListO[inst.r0].color;
      inst.r1 = adjListO[inst.r1].color;
      break;
    }
    case JEQ_regL_regL:
    case JEQ_regD_regD:
    case JNE_regL_regL:
    case JNE_regD_regD:
    case JLT_regL_regL:
    case JLT_regD_regD:
    case JLE_regL_regL:
    case JLE_regD_regD:
    case JGT_regL_regL:
    case JGT_regD_regD:
    case JGE_regL_regL:
    case JGE_regD_regD: {
      Reg_reg_s12 inst = (Reg_reg_s12) this;
      inst.r0 = adjListD[inst.r0].color;
      inst.r1 = adjListD[inst.r1].color;
      break;
    }

    //reg_reg_sym
    case ADD_regI_regI_sym: {
      Reg_reg_sym inst = (Reg_reg_sym) this;
      inst.r0 = adjListI[inst.r0].color;
      inst.r1 = adjListI[inst.r1].color;
      break;
    }

    //reg_s6_desloc
    case JEQ_regI_s6:
    case JNE_regI_s6:
    case JLT_regI_s6:
    case JLE_regI_s6:
    case JGT_regI_s6:
    case JGE_regI_s6: {
      Reg_s6_desloc inst = (Reg_s6_desloc) this;
      inst.reg = adjListI[inst.reg].color;
      break;
    }
    case JEQ_regO_null:
    case JNE_regO_null: {
      Reg_s6_desloc inst = (Reg_s6_desloc) this;
      inst.reg = adjListO[inst.reg].color;
      break;
    }

    //reg_sym_desloc
    case JEQ_regI_sym:
    case JNE_regI_sym: {
      Reg_sym_sdesloc inst = (Reg_sym_sdesloc) this;
      inst.reg = adjListI[inst.reg].color;
      break;
    }

    //reg_arl_s12
    case JGE_regI_arlen: {
      Reg_arl_s12 inst = (Reg_arl_s12) this;
      inst.base = adjListO[inst.base].color;
      inst.regI = adjListI[inst.regI].color;
      break;
    }

    //instanceof
    case INSTANCEOF: {
      Instanceof inst = (Instanceof) this;
      inst.regO = adjListO[inst.regO].color;
      inst.regI = adjListI[inst.regI].color;
      break;
    }
    case CHECKCAST: {
      Instanceof inst = (Instanceof) this;
      inst.regO = adjListO[inst.regO].color;
      break;
    }

    //reg
    case RETURN_regI:
    case JUMP_regI: {
      Reg inst = (Reg) this;
      inst.reg = adjListI[inst.reg].color;
      break;
    }
    case RETURN_regO:
    case TEST_regO:
    case THROW:
    case MONITOR_Enter:
    case MONITOR_Exit:
    case MONITOR_Enter2:
    case MONITOR_Exit2: {
      Reg inst = (Reg) this;
      inst.reg = adjListO[inst.reg].color;
      break;
    }
    case RETURN_reg64: {
      Reg inst = (Reg) this;
      inst.reg = adjListD[inst.reg].color;
      break;
    }

    //sym
    case RETURN_symI:
    case RETURN_symO:
    case RETURN_symD:
    case RETURN_symL: {
      break;
    }

    //switch_reg
    case SWITCH: {
      Switch_reg inst = (Switch_reg) this;
      inst.key = adjListI[inst.key].color;
      break;
    }

    //newarray
    case NEWARRAY_len: {
      New_array inst = (New_array) this;
      inst.regO = adjListO[inst.regO].color;
      break;
    }
    case NEWARRAY_regI: {
      New_array inst = (New_array) this;
      inst.regO = adjListO[inst.regO].color;
      inst.arrayLen = adjListI[inst.arrayLen].color;
      break;
    }
    case NEWARRAY_multi: {
      New_array inst = (New_array) this;
      inst.regO = adjListO[inst.regO].color;
      for (int i = 0; i < inst.params.length; i++) {
        Params p = (Params) inst.params[i];
        // type_Int represents an integer register
        if (p.typeOfParam1 == type_Int) {
          p.param1 = adjListI[p.param1].color;
        }
        if (p.typeOfParam2 == type_Int) {
          p.param2 = adjListI[p.param2].color;
        }
        if (p.typeOfParam3 == type_Int) {
          p.param3 = adjListI[p.param3].color;
        }
        if (p.typeOfParam4 == type_Int) {
          p.param4 = adjListI[p.param4].color;
        }
      }
      break;
    }

    //Call
    case CALL_normal:
    case CALL_virtual: {
      Call inst = (Call) this;
      if (!inst.isStatic) {
        inst._this = adjListO[inst._this].color;
      }
      switch (inst.retOrParamType) {
      case type_Int:
        inst.retOrParam = adjListI[inst.retOrParam].color;
        break;
      case type_Double:
      case type_Long:
        inst.retOrParam = adjListD[inst.retOrParam].color;
        break;
      case type_Obj:
        inst.retOrParam = adjListO[inst.retOrParam].color;
        break;
      }
      if (inst.params != null) {
        for (int i = 0; i < inst.params.length; i++) {
          Params p = (Params) inst.params[i];
          switch (p.typeOfParam1) {
          case type_Int:
            p.param1 = adjListI[p.param1].color;
            break;
          case type_Double:
          case type_Long:
            p.param1 = adjListD[p.param1].color;
            break;
          case type_Obj:
            p.param1 = adjListO[p.param1].color;
            break;
          }
          switch (p.typeOfParam2) {
          case type_Int:
            p.param2 = adjListI[p.param2].color;
            break;
          case type_Double:
          case type_Long:
            p.param2 = adjListD[p.param2].color;
            break;
          case type_Obj:
            p.param2 = adjListO[p.param2].color;
            break;
          }
          switch (p.typeOfParam3) {
          case type_Int:
            p.param3 = adjListI[p.param3].color;
            break;
          case type_Double:
          case type_Long:
            p.param3 = adjListD[p.param3].color;
            break;
          case type_Obj:
            p.param3 = adjListO[p.param3].color;
            break;
          }
          switch (p.typeOfParam4) {
          case type_Int:
            p.param4 = adjListI[p.param4].color;
            break;
          case type_Double:
          case type_Long:
            p.param4 = adjListD[p.param4].color;
            break;
          case type_Obj:
            p.param4 = adjListO[p.param4].color;
            break;
          }
        }
      }
      break;
    }

    case RETURN_void:
    case JUMP_s24:
    case RETURN_s24I:
    case RETURN_null:
    case RETURN_s24D:
    case RETURN_s24L: {
      break;
    }

    default: {
      throw new ConverterException("Instruction.virtualRegs2PhysicalRegs -> Opcode " + op + " is not being handled.");
      //case ADD_regI_arc_s6:
      //case ADD_regI_aru_s6:
      //case ADD_aru_regI_s6:
      //case AND_regI_aru_s6:
      //case DECJGTZ_regI:
      //case DECJGEZ_regI:
    }
    }
  }
}
