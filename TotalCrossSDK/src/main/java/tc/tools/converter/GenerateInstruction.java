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

import tc.tools.converter.bytecode.BC170_tableswitch;
import tc.tools.converter.bytecode.BC171_lookupswitch;
import tc.tools.converter.ir.Instruction.Call;
import tc.tools.converter.ir.Instruction.Field_reg;
import tc.tools.converter.ir.Instruction.I32;
import tc.tools.converter.ir.Instruction.Inc;
import tc.tools.converter.ir.Instruction.Instanceof;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.ir.Instruction.New_array;
import tc.tools.converter.ir.Instruction.Parameter;
import tc.tools.converter.ir.Instruction.Params;
import tc.tools.converter.ir.Instruction.Reg;
import tc.tools.converter.ir.Instruction.Reg_ar;
import tc.tools.converter.ir.Instruction.Reg_arl_s12;
import tc.tools.converter.ir.Instruction.Reg_reg;
import tc.tools.converter.ir.Instruction.Reg_reg_reg;
import tc.tools.converter.ir.Instruction.Reg_reg_s12;
import tc.tools.converter.ir.Instruction.Reg_reg_sym;
import tc.tools.converter.ir.Instruction.Reg_s6_desloc;
import tc.tools.converter.ir.Instruction.Reg_sym;
import tc.tools.converter.ir.Instruction.Reg_sym_sdesloc;
import tc.tools.converter.ir.Instruction.S18_reg;
import tc.tools.converter.ir.Instruction.S24;
import tc.tools.converter.ir.Instruction.Static_reg;
import tc.tools.converter.ir.Instruction.Switch_reg;
import tc.tools.converter.ir.Instruction.Sym;
import tc.tools.converter.ir.Instruction.Two16;
import tc.tools.converter.oper.Operand;
import tc.tools.converter.oper.OperandArrayAccess;
import tc.tools.converter.oper.OperandConstant;
import tc.tools.converter.oper.OperandConstant32;
import tc.tools.converter.oper.OperandExternal;
import tc.tools.converter.oper.OperandKind;
import tc.tools.converter.oper.OperandReg;
import tc.tools.converter.oper.OperandReg16;
import tc.tools.converter.oper.OperandRegD64;
import tc.tools.converter.oper.OperandRegI;
import tc.tools.converter.oper.OperandRegIb;
import tc.tools.converter.oper.OperandRegIc;
import tc.tools.converter.oper.OperandRegIs;
import tc.tools.converter.oper.OperandRegL;
import tc.tools.converter.oper.OperandRegO;
import tc.tools.converter.oper.OperandSym;
import tc.tools.converter.oper.OperandSymD64;
import tc.tools.converter.oper.OperandSymI;
import tc.tools.converter.oper.OperandSymL;
import tc.tools.converter.oper.OperandSymO;
import totalcross.util.Vector;

public class GenerateInstruction implements TCConstants {
  public static Operand promoteOperand(Vector vcode, Operand opr, int toKind, int line) {
    switch (toKind) {
    case opr_regI: {
      switch (opr.kind) {
      // to regI
      case opr_regD:
      case opr_regL:
      case opr_fieldD:
      case opr_fieldL:
      case opr_staticD:
      case opr_staticL:
      case opr_symD:
      case opr_symL:
      case opr_aruD:
      case opr_aruL:
      case opr_arcD:
      case opr_arcL: {
        OperandReg r = new OperandRegI();
        newInstruction(vcode, pref_CONV, r, opr, line);
        return r;
      }
      // to regI
      case opr_staticI:
      case opr_symI:
      case opr_s6I:
      case opr_s12I:
      case opr_s18I:
      case opr_u16:
      case opr_s16I:
      case opr_aruI:
      case opr_arcI:
      case opr_fieldI:
      case opr_arlen: {
        OperandReg r = new OperandRegI();
        newInstruction(vcode, pref_MOV, r, opr, line);
        return r;
      }

      //to regI
      case opr_s24I:
      case opr_s32I: {
        OperandConstant op = (OperandConstant) opr;
        int index = GlobalConstantPool.put(op.getValueAsInt());
        OperandSym sym = new OperandSymI(index);

        OperandReg r = new OperandRegI();
        newInstruction(vcode, pref_MOV, r, sym, line);
        return r;
      }
      default:
        ;
      }
      break;
    }

    case opr_regD: {
      switch (opr.kind) {
      //to regD
      case opr_regI:
      case opr_regL:
      case opr_fieldI:
      case opr_fieldL:
      case opr_staticI:
      case opr_staticL:
      case opr_symI:
      case opr_symL:
      case opr_regIb:
      case opr_regIs:
      case opr_regIc:
      case opr_aruI:
      case opr_aruL:
      case opr_arcI:
      case opr_arcL:
      case opr_arlen: {
        OperandReg r = new OperandRegD64();
        r.nWords = opr.nWords;
        newInstruction(vcode, pref_CONV, r, opr, line);
        return r;
      }

      case opr_fieldD:
      case opr_staticD:
      case opr_symD:
      case opr_aruD:
      case opr_arcD: {
        OperandReg r = new OperandRegD64();
        r.nWords = opr.nWords;
        newInstruction(vcode, pref_MOV, r, opr, line);
        return r;
      }

      //to regD
      case opr_s6D:
      case opr_s6I:
      case opr_s6L:
      case opr_s12D:
      case opr_s12L:
      case opr_s12I:
      case opr_s18D:
      case opr_s18L:
      case opr_s18I:
      case opr_u16:
      case opr_s16D:
      case opr_s16L:
      case opr_s16I: {
        OperandReg r = new OperandRegD64();
        r.nWords = opr.nWords;
        newInstruction(vcode, pref_MOV, r, opr, line);
        return r;
      }

      //to regD
      case opr_s24D:
      case opr_s24L:
      case opr_s24I:
      case opr_s32D:
      case opr_s32L:
      case opr_s32I:
      case opr_s64D:
      case opr_s64L: {
        OperandConstant op = (OperandConstant) opr;
        int index = GlobalConstantPool.put((double) op.getValueAsLong());
        OperandSym sym = new OperandSymD64(index);
        sym.nWords = op.nWords;
        OperandReg r = new OperandRegD64();
        r.nWords = op.nWords;
        newInstruction(vcode, pref_MOV, r, sym, line);
        return r;
      }
      default:
        ;
      }
      break;
    }

    case opr_regL: {
      switch (opr.kind) {
      case opr_fieldL:
      case opr_staticL:
      case opr_symL:
      case opr_aruL:
      case opr_arcL: {
        OperandReg r = new OperandRegL();
        newInstruction(vcode, pref_MOV, r, opr, line);
        return r;
      }

      //to regL
      case opr_regI:
      case opr_regD:
      case opr_fieldI:
      case opr_fieldD:
      case opr_staticI:
      case opr_staticD:
      case opr_symI:
      case opr_symD:
      case opr_regIb:
      case opr_regIs:
      case opr_regIc:
      case opr_aruI:
      case opr_aruD:
      case opr_arcI:
      case opr_arcD:
      case opr_arlen: {
        OperandReg r = new OperandRegL();
        newInstruction(vcode, pref_CONV, r, opr, line);
        return r;
      }

      //to regL
      case opr_s6L:
      case opr_s6I:
      case opr_s12L:
      case opr_s12I:
      case opr_s18L:
      case opr_s18I:
      case opr_u16:
      case opr_s16L:
      case opr_s16I: {
        OperandReg r = new OperandRegL();
        newInstruction(vcode, pref_MOV, r, opr, line);
        return r;
      }

      //to regL
      case opr_s24L:
      case opr_s24I:
      case opr_s32L:
      case opr_s32I:
      case opr_s64L: {
        OperandConstant op = (OperandConstant) opr;
        int index = GlobalConstantPool.put(op.getValueAsLong());
        OperandSym sym = new OperandSymL(index);

        OperandReg r = new OperandRegL();
        newInstruction(vcode, pref_MOV, r, sym, line);
        return r;
      }
      default:
        ;
      }
      break;
    }

    case opr_regO: {
      switch (opr.kind) {
      //to regO
      case opr_fieldO:
      case opr_staticO:
      case opr_symO:
      case opr_aruO:
      case opr_arcO:
      case opr_null: {
        OperandReg r = new OperandRegO();
        newInstruction(vcode, pref_MOV, r, opr, line);
        return r;
      }
      default:
        ;
      }
      break;
    }

    case opr_fieldI:
    case opr_fieldD:
    case opr_fieldL:
    case opr_fieldO:
    case opr_staticI:
    case opr_staticD:
    case opr_staticL:
    case opr_staticO: {
      break; //nothing to make.
    }

    case opr_symI: {
      if (OperandKind.isConstantI(opr.kind)) {
        OperandConstant op = (OperandConstant) opr;
        int index = GlobalConstantPool.put(op.getValueAsInt());
        OperandSym sym = new OperandSymI(index);
        return sym;
      }
      break;
    }

    case opr_symD: {
      if (OperandKind.isConstant(opr.kind)) {
        OperandConstant op = (OperandConstant) opr;
        int index = GlobalConstantPool.put((double) op.getValueAsLong());
        OperandSym sym = new OperandSymD64(index);
        sym.nWords = op.nWords;
        return sym;
      }
      break;
    }

    case opr_symL: {
      if (OperandKind.isConstant(opr.kind)) {
        OperandConstant op = (OperandConstant) opr;
        int index = GlobalConstantPool.put(op.getValueAsLong());
        OperandSym sym = new OperandSymL(index);
        return sym;
      }
      break;
    }

    case opr_regIb:
    case opr_regIs:
    case opr_regIc:
    case opr_reg16: {
      if (OperandKind.isConstant(opr.kind)) {
        OperandRegI r = null;
        switch (toKind) {
        case opr_regIb:
          r = new OperandRegIb();
          break;
        case opr_regIc:
          r = new OperandRegIs();
          break;
        case opr_regIs:
          r = new OperandRegIc();
          break;
        case opr_reg16:
          r = new OperandReg16();
          break;
        }
        newInstruction(vcode, pref_MOV, r, opr, line);
        return r;
      }
      break;
    }

    case opr_symO:
    case opr_aruI:
    case opr_aruD:
    case opr_aruL:
    case opr_aruO:
    case opr_arcI:
    case opr_arcD:
    case opr_arcL:
    case opr_arcO:
    case opr_arlen:
    case opr_null:
    case opr_void: {
      break; //nothing to make.
    }
    }

    return opr;
  }

  public static final Instruction newInstruction(Vector vcode, int prefix, int line) {
    Instruction c = null;
    switch (prefix) {
    case pref_RETURNV:
      c = new Instruction(RETURN_void, line);
      break;
    default:
      throw new ConverterException("FATAL ERROR: INVALID OPCODE");
    }
    vcode.addElement(c);
    return c;
  }

  public static final Instruction newInstruction(Vector vcode, int prefix, Operand opr, int line) {
    int opcode = ChooseOpcode.chooseOp(prefix, opr.kind);
    if (opcode == INSTRUCTION_NOT_FOUND) {
      throw new ConverterException("FATAL ERROR: INVALID OPCODE");
    }
    Instruction c = null;
    switch (opcode) {
    case RETURN_null:
      c = new Instruction(opcode, line);
      break;

    case JUMP_s24:
    case RETURN_s24I:
    case RETURN_s24D:
    case RETURN_s24L: {
      c = new S24(opcode, line, 0);
      GEN_S24(c, opr);
      break;
    }
    case RETURN_symI: {
      c = new Sym(opcode, line);
      OperandSym sym = (OperandSym) promoteOperand(vcode, opr, opr_symI, line);
      GEN_SYM(c, sym);
      break;
    }
    case RETURN_symO: {
      c = new Sym(opcode, line);
      OperandSym sym = (OperandSym) promoteOperand(vcode, opr, opr_symO, line);
      GEN_SYM(c, sym);
      break;
    }
    case RETURN_symD: {
      c = new Sym(opcode, line);
      OperandSym sym = (OperandSym) promoteOperand(vcode, opr, opr_symD, line);
      GEN_SYM(c, sym);
      break;
    }
    case RETURN_symL: {
      c = new Sym(opcode, line);
      OperandSym sym = (OperandSym) promoteOperand(vcode, opr, opr_symL, line);
      GEN_SYM(c, sym);
      break;
    }
    case RETURN_regI: {
      c = new Reg(opcode, line);
      OperandReg r = (OperandReg) promoteOperand(vcode, opr, opr_regI, line);
      GEN_REG(c, r);
      break;
    }
    case RETURN_regO: {
      c = new Reg(opcode, line);
      OperandReg r = (OperandReg) promoteOperand(vcode, opr, opr_regO, line);
      GEN_REG(c, r);
      break;
    }
    case RETURN_reg64: {
      c = new Reg(opcode, line);
      OperandReg r = OperandKind.isTypeL(opr.kind) ? (OperandReg) promoteOperand(vcode, opr, opr_regL, line)
          : (OperandReg) promoteOperand(vcode, opr, opr_regD, line);
      GEN_REG(c, r);
      break;
    }
    default:
      throw new ConverterException("FATAL ERROR: INVALID OPCODE : " + opcode);
    }

    vcode.addElement(c);
    return c;
  }

  public static final Instruction newInstruction(Vector vcode, int prefix, Operand opr1, Operand opr2, int line) {
    int opcode = ChooseOpcode.chooseOp(prefix, opr1.kind, opr2.kind);
    if (opcode == INSTRUCTION_NOT_FOUND) {
      throw new ConverterException("FATAL ERROR: BAD OPCODE");
    }
    Instruction c = null;
    switch (opcode) {
    case NEWOBJ: {
      c = new Reg_sym(opcode, line);
      GEN_REG_SYM(c, opr1, opr2);
      break;
    }
    case CHECKCAST: {
      c = new Instanceof(opcode, line);
      OperandRegO s1 = (OperandRegO) promoteOperand(vcode, opr1, opr_regO, line);
      GEN_CHECKCAST(c, s1, opr2);
      break;
    }
    case MOV_regI_regI:
    case CONV_regL_regI:
    case CONV_regD_regI:
    case CONV_regIb_regI:
    case CONV_regIc_regI:
    case CONV_regIs_regI: {
      c = new Reg_reg(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      GEN_REG_REG(c, opr1, op);
      break;
    }

    case MOV_reg64_reg64: {
      c = new Reg_reg(opcode, line);
      OperandReg op = OperandKind.isTypeL(opr2.kind) ? (OperandReg) promoteOperand(vcode, opr2, opr_regL, line)
          : (OperandReg) promoteOperand(vcode, opr2, opr_regD, line);
      GEN_REG_REG(c, opr1, op);
      break;
    }

    case MOV_regO_regO: {
      c = new Reg_reg(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regO, line);
      GEN_REG_REG(c, opr1, op);
      break;
    }

    case MOV_regI_field:
    case MOV_regO_field:
    case MOV_reg64_field: {
      c = new Field_reg(opcode, line);
      //order: reg, ext
      GEN_FIELD_REG(c, opr1, opr2);
      break;
    }

    case MOV_regO_null: {
      OperandReg reg = (OperandReg) opr1;
      c = new S18_reg(opcode, line, 0, reg.index);
      break;
    }

    case MOV_field_regI: {
      c = new Field_reg(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      // order: reg, ext
      GEN_FIELD_REG(c, op, opr1);
      break;
    }

    case MOV_field_regO: {
      c = new Field_reg(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regO, line);
      // order: reg, ext
      GEN_FIELD_REG(c, op, opr1);
      break;
    }

    case MOV_field_reg64: {
      c = new Field_reg(opcode, line);
      OperandReg op = OperandKind.isTypeL(opr2.kind) ? (OperandReg) promoteOperand(vcode, opr2, opr_regL, line)
          : (OperandReg) promoteOperand(vcode, opr2, opr_regD, line);
      // order: reg, ext
      GEN_FIELD_REG(c, op, opr1);
      break;
    }

    case MOV_regI_static:
    case MOV_regO_static:
    case MOV_reg64_static: {
      c = new Static_reg(opcode, line);
      //order: ext, reg
      GEN_STATIC_REG(c, opr2, opr1);
      break;
    }

    case MOV_static_regI: {
      c = new Static_reg(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      // order: ext, reg
      GEN_STATIC_REG(c, opr1, op);
      break;
    }

    case MOV_static_regO: {
      c = new Static_reg(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regO, line);
      // order: ext, reg
      GEN_STATIC_REG(c, opr1, op);
      break;
    }

    case MOV_static_reg64: {
      c = new Static_reg(opcode, line);
      OperandReg op = OperandKind.isTypeL(opr2.kind) ? (OperandReg) promoteOperand(vcode, opr2, opr_regL, line)
          : (OperandReg) promoteOperand(vcode, opr2, opr_regD, line);
      //order: ext, reg
      GEN_STATIC_REG(c, opr1, op);
      break;
    }

    case MOV_regI_sym: {
      c = new Reg_sym(opcode, line);
      OperandSym op = (OperandSym) promoteOperand(vcode, opr2, opr_symI, line);
      //order: reg, sym
      GEN_REG_SYM(c, opr1, op);
      break;
    }

    case MOV_regO_sym: {
      c = new Reg_sym(opcode, line);
      OperandSym op = (OperandSym) promoteOperand(vcode, opr2, opr_symO, line);
      //order: reg, sym
      GEN_REG_SYM(c, opr1, op);
      break;
    }

    case MOV_regD_sym: {
      c = new Reg_sym(opcode, line);
      OperandSym op = (OperandSym) promoteOperand(vcode, opr2, opr_symD, line);
      //order: reg, sym
      GEN_REG_SYM(c, opr1, op);
      break;
    }

    case MOV_regL_sym: {
      c = new Reg_sym(opcode, line);
      OperandSym op = (OperandSym) promoteOperand(vcode, opr2, opr_symL, line);
      //order: reg, sym
      GEN_REG_SYM(c, opr1, op);
      break;
    }

    case MOV_regI_s18:
    case MOV_regD_s18:
    case MOV_regL_s18: {
      c = new S18_reg(opcode, line);
      // order: reg, s18
      GEN_S18_REG(c, opr1, opr2);
      break;
    }

    case MOV_reg64_arc: {
      c = new Reg_ar(opcode, line);
      // order: reg, base, index
      GEN_REG_AR(c, opr1, opr2);
      break;
    }
    case MOV_reg64_aru: {
      c = new Reg_ar(opcode, line);
      // order: reg, base, index
      GEN_REG_AR(c, opr1, opr2);
      break;
    }
    case MOV_reg16_aru: {
      c = new Reg_ar(opcode, line);
      // order: reg, base, index
      GEN_REG_AR(c, opr1, opr2);
      break;
    }
    case MOV_reg16_arc: {
      c = new Reg_ar(opcode, line);
      // order: reg, base, index
      GEN_REG_AR(c, opr1, opr2);
      break;
    }

    case MOV_regI_aru:
    case MOV_regI_arc:
    case MOV_regO_aru:
    case MOV_regO_arc:
    case MOV_regIb_aru:
    case MOV_regIb_arc:
    case MOV_regI_arlen: {
      c = new Reg_ar(opcode, line);
      GEN_REG_AR(c, opr1, opr2);
      break;
    }

    case MOV_aru_regI:
    case MOV_arc_regI: {
      c = new Reg_ar(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      GEN_REG_AR(c, op, opr1);
      break;
    }

    case MOV_aru_regO:
    case MOV_arc_regO: {
      c = new Reg_ar(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regO, line);
      GEN_REG_AR(c, op, opr1);
      break;
    }

    case MOV_aru_reg64:
    case MOV_arc_reg64: {
      c = new Reg_ar(opcode, line);
      OperandReg op = OperandKind.isTypeL(opr2.kind) ? (OperandReg) promoteOperand(vcode, opr2, opr_regL, line)
          : (OperandReg) promoteOperand(vcode, opr2, opr_regD, line);
      GEN_REG_AR(c, op, opr1);
      break;
    }

    case MOV_aru_reg16:
    case MOV_arc_reg16: {
      c = new Reg_ar(opcode, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_reg16, line);
      GEN_REG_AR(c, s2, opr1);
      break;
    }
    case MOV_aru_regIb:
    case MOV_arc_regIb: {
      c = new Reg_ar(opcode, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regIb, line);
      GEN_REG_AR(c, s2, opr1);
      break;
    }

    case CONV_regI_regL:
    case CONV_regD_regL: {
      c = new Reg_reg(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regL, line);
      GEN_REG_REG(c, opr1, op);
      break;
    }

    case CONV_regI_regD:
    case CONV_regL_regD: {
      c = new Reg_reg(opcode, line);
      OperandReg op = (OperandReg) promoteOperand(vcode, opr2, opr_regD, line);
      GEN_REG_REG(c, opr1, op);
      break;
    }

    default:
      throw new ConverterException("FATAL ERROR: INVALID OPCODE : " + opcode);
    }
    if (c != null) {
      vcode.addElement(c);
    }
    return c;
  }

  public static final Instruction newInstruction(Vector vcode, int prefix, Operand opr1, Operand opr2, Operand opr3,
      int line) {
    boolean swap = false;
    int opcode = ChooseOpcode.chooseOp(prefix, opr1.kind, opr2.kind, opr3.kind);
    if (opcode == INSTRUCTION_NOT_FOUND) {
      throw new ConverterException("FATAL ERROR: BAD OPCODE");
    }
    if (opcode < 0) {
      opcode = -opcode;
      swap = true;
    }

    Instruction c = null;
    switch (opcode) {
    case INSTANCEOF: {
      c = new Instanceof(opcode, line);
      OperandRegO s2 = (OperandRegO) promoteOperand(vcode, opr2, opr_regO, line);
      GEN_INSTANCEOF(c, opr1, s2, opr3);
      break;
    }
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
      c = new Reg_reg_reg(opcode, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      OperandReg s3 = (OperandReg) promoteOperand(vcode, opr3, opr_regI, line);
      GEN_REG_REG_REG(c, opr1, s2, s3);
      break;
    }

    case ADD_regL_regL_regL:
    case SUB_regL_regL_regL:
    case MUL_regL_regL_regL:
    case DIV_regL_regL_regL:
    case MOD_regL_regL_regL:
    case SHR_regL_regL_regL:
    case SHL_regL_regL_regL:
    case USHR_regL_regL_regL:
    case AND_regL_regL_regL:
    case OR_regL_regL_regL:
    case XOR_regL_regL_regL: {
      c = new Reg_reg_reg(opcode, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regL, line);
      OperandReg s3 = (OperandReg) promoteOperand(vcode, opr3, opr_regL, line);
      GEN_REG_REG_REG(c, opr1, s2, s3);
      break;
    }

    case ADD_regD_regD_regD:
    case SUB_regD_regD_regD:
    case MUL_regD_regD_regD:
    case DIV_regD_regD_regD:
    case MOD_regD_regD_regD: {
      c = new Reg_reg_reg(opcode, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regD, line);
      OperandReg s3 = (OperandReg) promoteOperand(vcode, opr3, opr_regD, line);
      GEN_REG_REG_REG(c, opr1, s2, s3);
      break;
    }

    case ADD_regI_s12_regI: {
      if (swap) {
        Operand aux = opr2;
        opr2 = opr3;
        opr3 = aux;
      }

      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      OperandReg s1 = (OperandReg) opr1;
      if (s1.index == s2.index && OperandKind.isConstantFitIn16Bits(opr3.kind)) {
        c = new Inc(INC_regI, line);
        GEN_INC_REG(c, s1, opr3);
      } else {
        c = new Reg_reg_s12(opcode, line);
        GEN_REG_REG_S12(c, s1, s2, opr3);
      }
      break;
    }

    case MUL_regI_regI_s12:
    case AND_regI_regI_s12:
    case OR_regI_regI_s12:
    case XOR_regI_regI_s12: {
      c = new Reg_reg_s12(opcode, line);
      if (swap) {
        Operand aux = opr2;
        opr2 = opr3;
        opr3 = aux;
      }
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      GEN_REG_REG_S12(c, opr1, s2, opr3);
      break;
    }

    case SUB_regI_s12_regI: {
      c = new Reg_reg_s12(opcode, line);
      OperandReg s3 = (OperandReg) promoteOperand(vcode, opr3, opr_regI, line);
      GEN_REG_REG_S12(c, opr1, s3, opr2);
      break;
    }

    case DIV_regI_regI_s12:
    case MOD_regI_regI_s12:
    case SHR_regI_regI_s12:
    case SHL_regI_regI_s12:
    case USHR_regI_regI_s12: {
      c = new Reg_reg_s12(opcode, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      GEN_REG_REG_S12(c, opr1, s2, opr3);
      break;
    }

    case ADD_regI_regI_sym: {
      c = new Reg_reg_sym(opcode, line);
      if (swap) {
        Operand aux = opr2;
        opr2 = opr3;
        opr3 = aux;
      }
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      OperandSym s3 = (OperandSym) promoteOperand(vcode, opr3, opr_symI, line);
      GEN_REG_REG_SYM(c, opr1, s2, s3);
      break;
    }

    case NEWARRAY_len: {
      c = new New_array(opcode, line);
      OperandConstant s3 = (OperandConstant) opr3;
      GEN_NEWARRAY(c, opr1, opr2, s3.getValueAsInt());
      break;
    }

    case NEWARRAY_regI: {
      c = new New_array(opcode, line);
      OperandReg s3 = (OperandReg) promoteOperand(vcode, opr3, opr_regI, line);
      GEN_NEWARRAY(c, opr1, opr2, s3.index);
      break;
    }

    case JEQ_regO_regO:
    case JNE_regO_regO: {
      c = new Reg_reg_s12(opcode, line);
      OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regO, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regO, line);
      GEN_REG_REG_S12(c, s1, s2, opr3);
      break;
    }
    case JEQ_regO_null:
    case JNE_regO_null: {
      c = new Reg_s6_desloc(opcode, line);
      if (swap) {
        Operand aux = opr1;
        opr1 = opr2;
        opr2 = aux;
      }
      OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regO, line);
      OperandConstant s2 = new OperandConstant32(0, type_Int);
      GEN_REG_S6_DESLOC(c, s1, s2, opr3);
      break;
    }
    case JEQ_regI_regI:
    case JNE_regI_regI:
    case JLT_regI_regI:
    case JLE_regI_regI:
    case JGT_regI_regI:
    case JGE_regI_regI: {
      c = new Reg_reg_s12(opcode, line);
      OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regI, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      GEN_REG_REG_S12(c, s1, s2, opr3);
      break;
    }
    case JEQ_regL_regL:
    case JNE_regL_regL:
    case JLT_regL_regL:
    case JLE_regL_regL:
    case JGT_regL_regL:
    case JGE_regL_regL: {
      c = new Reg_reg_s12(opcode, line);
      OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regL, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regL, line);
      GEN_REG_REG_S12(c, s1, s2, opr3);
      break;
    }
    case JEQ_regD_regD:
    case JNE_regD_regD:
    case JLT_regD_regD:
    case JLE_regD_regD:
    case JGT_regD_regD:
    case JGE_regD_regD: {
      c = new Reg_reg_s12(opcode, line);
      OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regD, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regD, line);
      GEN_REG_REG_S12(c, s1, s2, opr3);
      break;
    }
    case JEQ_regI_s6:
    case JNE_regI_s6:
    case JLT_regI_s6:
    case JLE_regI_s6:
    case JGT_regI_s6:
    case JGE_regI_s6: {
      c = new Reg_s6_desloc(opcode, line);
      if (swap) {
        Operand aux = opr1;
        opr1 = opr2;
        opr2 = aux;
      }
      OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regI, line);
      GEN_REG_S6_DESLOC(c, s1, opr2, opr3);
      break;
    }
    case JEQ_regI_sym:
    case JNE_regI_sym: {
      /*OperandConstant d = (OperandConstant) opr3;
            if (d.value <= -32 || d.value >= 31) // 6 bits
            {*/
      opcode = opcode == JEQ_regI_sym ? JEQ_regI_regI : JNE_regI_regI;
      c = new Reg_reg_s12(opcode, line);
      OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regI, line);
      OperandReg s2 = (OperandReg) promoteOperand(vcode, opr2, opr_regI, line);
      GEN_REG_REG_S12(c, s1, s2, opr3);
      /*}
            else
            {
               c = new Reg_sym_sdesloc(opcode, line);
               if (swap)
               {
                  Operand aux = opr1;
                  opr1 = opr2;
                  opr2 = aux;
               }
               OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regI, line);
               OperandSym s2 = (OperandSym) promoteOperand(vcode, opr2, opr_symI, line);
               GEN_REG_SYM_DESLOC(c, s1, s2, opr3);
            } */
      break;
    }

    case JGE_regI_arlen: {
      c = new Reg_arl_s12(opcode, line);
      OperandReg s1 = (OperandReg) promoteOperand(vcode, opr1, opr_regI, line);
      GEN_REG_ARL_S12(c, s1, opr2, opr3);
    }
    default:
      throw new ConverterException("FATAL ERROR: INVALID OPCODE : " + opcode);
    }

    if (c != null) {
      vcode.addElement(c);
    }
    return c;
  }

  public static final Instruction newInstruction(Vector vcode, OperandReg regO, OperandSym sym, Operand dims[],
      int line) {
    New_array cNew = new New_array(NEWARRAY_multi, line);
    cNew.len = 1 + ((dims.length + 3) >> 2);
    cNew.params = new Parameter[cNew.len - 1];
    GEN_NEWARRAY(cNew, regO, sym, dims.length);

    // dimensions
    for (int i = 0; i < dims.length; i++) {
      Operand opr = dims[i];
      if (opr instanceof OperandConstant) {
        OperandConstant c = (OperandConstant) opr;
        if (c.value >= -1 && c.value <= 190) {
          continue;
        }
      }
      dims[i] = (OperandReg) promoteOperand(vcode, opr, opr_regI, line);
    }

    // now that all values are promoted, we can add the instruction
    vcode.addElement(cNew);

    // and store the dimensions in the former bytes.
    Params params = null;
    for (int i = 0, k = 0; i < dims.length; i++) {
      int value = 0;
      int type = type_Int;
      if (dims[i] instanceof OperandConstant) {
        OperandConstant constant = (OperandConstant) dims[i];
        value = (int) constant.value;
        type = type_Constant;
      } else {
        OperandReg reg = (OperandReg) dims[i];
        value = reg.index;
      }
      switch (i & 3) {
      case 0:
        params = new Params(line);
        params.param1 = value;
        params.typeOfParam1 = type;
        cNew.params[k++] = params;
        break;
      case 1:
        params.param2 = value;
        params.typeOfParam2 = type;
        break;
      case 2:
        params.param3 = value;
        params.typeOfParam3 = type;
        break;
      case 3:
        params.param4 = value;
        params.typeOfParam4 = type;
        break;
      }
    }

    return cNew;
  }

  public static final Instruction newInstruction(Vector vcode, int opcode, OperandSym sym, OperandReg _this,
      Operand retAndParams[], boolean isVoid, int line) {
    Call call = new Call(opcode, line);
    call.len = (6 + retAndParams.length) / 4;
    call.isVoid = isVoid;

    // promote operands: register of return and/or parameters
    int len = retAndParams.length;
    if (len > 0) {
      Operand opr = retAndParams[0];
      int type = opr_regI;
      call.retOrParamType = type_Int;
      if (OperandKind.isTypeO(opr.kind)) {
        type = opr_regO;
        call.retOrParamType = type_Obj;
      } else if (OperandKind.isTypeD(opr.kind)) {
        type = opr_regD;
        call.retOrParamType = type_Double;
      } else if (OperandKind.isTypeL(opr.kind)) {
        type = opr_regL;
        call.retOrParamType = type_Long;
      } else if (opr.nWords == 2) {
        type = opr_regL;
        call.retOrParamType = type_Long;
      }
      retAndParams[0] = (OperandReg) promoteOperand(vcode, opr, type, line);
    }

    for (int i = 1; i < len; i++) {
      Operand opr = retAndParams[i];
      if (opr.kind == opr_null) {
        continue;
      }
      if (opr instanceof OperandConstant) {
        OperandConstant c = (OperandConstant) opr;
        if (c.value >= -1 && c.value <= 190) {
          continue;
        }
      }
      int type = opr_regI;
      if (OperandKind.isTypeO(opr.kind)) {
        type = opr_regO;
      } else if (OperandKind.isTypeD(opr.kind)) {
        type = opr_regD;
      } else if (OperandKind.isTypeL(opr.kind)) {
        type = opr_regL;
      } else if (opr.nWords == 2) {
        type = opr_regL;
      }
      retAndParams[i] = (OperandReg) promoteOperand(vcode, opr, type, line);
    }

    // now that all values are promoted, we can add the instruction
    vcode.addElement(call);

    // fill structure mtd and add parameters.
    GEN_CALL(vcode, call, sym, _this, retAndParams, line);
    return call;
  }

  public static final void newInstruction(Vector vcode, int defAddr, Operand k, BC171_lookupswitch ji, int line) {
    Switch_reg sswitch = new Switch_reg(SWITCH, line);
    sswitch.len = 2 + ji.npairs + (ji.npairs + 1) / 2;
    sswitch.params = new Parameter[sswitch.len - 1];
    // promote key to register
    OperandRegI key = (OperandRegI) promoteOperand(vcode, k, opr_regI, line);
    sswitch.set(key.index, ji.npairs);
    vcode.addElement(sswitch);
    GEN_SWITCH(vcode, sswitch, defAddr, ji, line);
  }

  public static final void newInstruction(Vector vcode, int defAddr, Operand index, BC170_tableswitch ji, int line) {
    Switch_reg sswitch = new Switch_reg(SWITCH, line);
    int n = ji.jumps.length; // number of keys
    sswitch.len = 2 + n + (n + 1) / 2;
    sswitch.params = new Parameter[sswitch.len - 1];
    // promote index to register
    OperandRegI key = (OperandRegI) promoteOperand(vcode, index, opr_regI, line);
    sswitch.set(key.index, n);
    vcode.addElement(sswitch);
    GEN_SWITCH(vcode, sswitch, defAddr, ji, line);
  }

  public static void GEN_REG_REG(Instruction c, Operand opr1, Operand opr2) {
    Reg_reg i = (Reg_reg) c;
    OperandReg r0 = (OperandReg) opr1;
    OperandReg r1 = (OperandReg) opr2;
    i.set(r0.index, r1.index);
  }

  public static void GEN_REG_REG_REG(Instruction c, Operand opr1, Operand opr2, Operand opr3) {
    Reg_reg_reg i = (Reg_reg_reg) c;
    OperandReg r0 = (OperandReg) opr1;
    OperandReg r1 = (OperandReg) opr2;
    OperandReg r2 = (OperandReg) opr3;
    i.set(r0.index, r1.index, r2.index);
  }

  public static void GEN_REG_REG_S12(Instruction c, Operand opr1, Operand opr2, Operand opr3) {
    Reg_reg_s12 i = (Reg_reg_s12) c;
    OperandReg r0 = (OperandReg) opr1;
    OperandReg r1 = (OperandReg) opr2;
    OperandConstant s12 = (OperandConstant) opr3;
    i.set(r0.index, r1.index, s12.getValueAsInt());
  }

  public static void GEN_INC_REG(Instruction c, Operand opr1, Operand opr2) {
    Inc i = (Inc) c;
    OperandReg r = (OperandReg) opr1;
    OperandConstant s16 = (OperandConstant) opr2;
    i.set(r.index, s16.getValueAsInt());
  }

  public static void GEN_REG_S6_DESLOC(Instruction c, Operand opr1, Operand opr2, Operand opr3) {
    Reg_s6_desloc i = (Reg_s6_desloc) c;
    OperandReg r = (OperandReg) opr1;
    OperandConstant s6 = (OperandConstant) opr2;
    OperandConstant s12 = (OperandConstant) opr3;
    i.set(r.index, s6.getValueAsInt(), s12.getValueAsInt());
  }

  public static void GEN_REG_SYM_DESLOC(Instruction c, Operand opr1, Operand opr2, Operand opr3) {
    Reg_sym_sdesloc i = (Reg_sym_sdesloc) c;
    OperandReg r = (OperandReg) opr1;
    OperandSym sym = (OperandSym) opr2;
    OperandConstant s12 = (OperandConstant) opr3;
    i.set(r.index, sym.index, s12.getValueAsInt());
  }

  public static void GEN_REG_ARL_S12(Instruction c, Operand opr1, Operand opr2, Operand opr3) {
    Reg_arl_s12 i = (Reg_arl_s12) c;
    OperandReg r = (OperandReg) opr1;
    OperandArrayAccess array = (OperandArrayAccess) opr2;
    OperandConstant desloc = (OperandConstant) opr3;
    i.set(r.index, array.base.index, desloc.getValueAsInt());
  }

  public static void GEN_REG_REG_SYM(Instruction c, Operand opr1, Operand opr2, Operand opr3) {
    Reg_reg_sym i = (Reg_reg_sym) c;
    OperandReg r0 = (OperandReg) opr1;
    OperandReg r1 = (OperandReg) opr2;
    OperandSym sym = (OperandSym) opr3;
    i.set(r0.index, r1.index, sym.index);
  }

  public static void GEN_FIELD_REG(Instruction c, Operand opr1, Operand opr2) {
    Field_reg i = (Field_reg) c;
    OperandReg reg = (OperandReg) opr1;
    OperandExternal ext = (OperandExternal) opr2;
    OperandReg regO = (OperandReg) ext.regO;
    OperandSym sym = (OperandSym) ext.sym;
    i.set(sym.index, regO.index, reg.index);
  }

  public static void GEN_STATIC_REG(Instruction c, Operand opr1, Operand opr2) {
    Static_reg i = (Static_reg) c;
    OperandSym sym = (OperandSym) opr1;
    OperandReg reg = (OperandReg) opr2;
    i.set(reg.index, sym.index);
  }

  public static void GEN_REG(Instruction c, Operand opr) {
    Reg i = (Reg) c;
    OperandReg reg = (OperandReg) opr;
    i.set(reg.index);
  }

  public static void GEN_SYM(Instruction c, Operand opr) {
    Sym i = (Sym) c;
    OperandSym sym = (OperandSym) opr;
    i.set(sym.index);
  }

  public static void GEN_REG_SYM(Instruction c, Operand opr1, Operand opr2) {
    Reg_sym i = (Reg_sym) c;
    OperandReg r = (OperandReg) opr1;
    OperandSym s = (OperandSym) opr2;
    i.set(r.index, s.index);
  }

  public static void GEN_S18_REG(Instruction c, Operand opr1, Operand opr2) {
    S18_reg i = (S18_reg) c;
    OperandReg r = (OperandReg) opr1;
    OperandConstant s18 = (OperandConstant) opr2;
    i.set(s18.getValueAsInt(), r.index);
  }

  public static void GEN_NEWARRAY(Instruction c, Operand opr1, Operand opr2, int lenOrRegIOrDims) {
    New_array i = (New_array) c;
    OperandReg reg = (OperandReg) opr1;
    OperandSym sym = (OperandSym) opr2;
    i.set(sym.index, reg.index, lenOrRegIOrDims);
  }

  public static void GEN_CALL(Vector vcode, Instruction c, OperandSym sym, OperandReg _this, Operand retAndParams[],
      int line) {
    Call i = (Call) c;
    i.sym = sym.index;
    i._this = _this.index;
    int len = retAndParams.length;
    if (len > 0) {
      // the return reg or the first parameter may be always in a register.
      OperandReg reg = (OperandReg) retAndParams[0];
      i.retOrParam = reg.index;
      if (len > 1) {
        i.params = new Parameter[i.len - 1];
      }
    }

    Params params = null;
    for (int j = 1, k = 0; j < len; j++) {
      int value = 0;
      int type = type_Double;
      if (retAndParams[j].kind == opr_null) {
        value = 0;
        type = type_Constant;
      } else if (retAndParams[j] instanceof OperandConstant) {
        OperandConstant constant = (OperandConstant) retAndParams[j];
        value = (int) constant.value;
        type = type_Constant;
      } else {
        OperandReg reg = (OperandReg) retAndParams[j];
        value = reg.index;
        if (OperandKind.isTypeI(reg.kind)) {
          type = type_Int;
        } else if (OperandKind.isTypeO(reg.kind)) {
          type = type_Obj;
        }
      }
      switch (j & 3) {
      case 1:
        params = new Params(line);
        params.param1 = value;
        params.typeOfParam1 = type;
        i.params[k++] = params;
        break;
      case 2:
        params.param2 = value;
        params.typeOfParam2 = type;
        break;
      case 3:
        params.param3 = value;
        params.typeOfParam3 = type;
        break;
      case 0:
        params.param4 = value;
        params.typeOfParam4 = type;
        break;
      }
    }
  }

  public static void GEN_REG_AR(Instruction c, Operand opr1, Operand opr2) {
    Reg_ar i = (Reg_ar) c;
    OperandReg reg = (OperandReg) opr1;
    OperandArrayAccess array = (OperandArrayAccess) opr2;
    OperandReg base = (OperandRegO) array.base;
    OperandReg index = (OperandReg) array.index;
    i.set(base.index, index != null ? index.index : -1, reg.index);
  }

  public static void GEN_INSTANCEOF(Instruction c, Operand opr1, Operand opr2, Operand opr3) {
    Instanceof i = (Instanceof) c;
    OperandRegI regI = (OperandRegI) opr1;
    OperandRegO regO = (OperandRegO) opr2;
    OperandSymO sym = (OperandSymO) opr3;
    i.set(sym.index, regO.index, regI.index);
  }

  public static void GEN_CHECKCAST(Instruction c, Operand opr1, Operand opr2) {
    Instanceof i = (Instanceof) c;
    OperandRegO regO = (OperandRegO) opr1;
    OperandSymO sym = (OperandSymO) opr2;
    i.set(sym.index, regO.index, 0);
  }

  public static void GEN_S24(Instruction c, Operand opr) {
    S24 i = (S24) c;
    OperandConstant s24 = (OperandConstant) opr;
    i.set(s24.getValueAsInt());
  }

  public static void GEN_SWITCH(Vector vcode, Switch_reg sswitch, int defAddr, BC171_lookupswitch ji, int line) {
    // add the default address
    Two16 tc1 = new Two16(line);
    tc1.v1 = Bytecode2TCCode.setGotoIndex(defAddr);
    sswitch.params[0] = tc1;

    int k = 1;
    // add the keys
    for (int i = 0; i < ji.values.length; i++) {
      I32 tc = new I32(line);
      tc.set(ji.values[i]);
      sswitch.params[k++] = tc;
    }

    // add the destionation address
    Two16 tc = null;
    for (int i = 0; i < ji.jumps.length; i++) {
      if ((i & 1) == 1) {
        tc.v2 = Bytecode2TCCode.setGotoIndex(ji.jumps[i]);
      } else {
        tc = new Two16(line);
        tc.v1 = Bytecode2TCCode.setGotoIndex(ji.jumps[i]);
        sswitch.params[k++] = tc;
      }
    }
  }

  public static void GEN_SWITCH(Vector vcode, Switch_reg sswitch, int defAddr, BC170_tableswitch ji, int line) {
    // add the default address
    Two16 tc1 = new Two16(line);
    tc1.v1 = Bytecode2TCCode.setGotoIndex(defAddr);
    sswitch.params[0] = tc1;

    int k = 1;
    // add the keys
    for (int i = ji.low; i <= ji.high; i++) {
      I32 tc = new I32(line);
      tc.set(i);
      sswitch.params[k++] = tc;
    }

    // add the destionation address
    Two16 tc = null;
    for (int i = 0; i < ji.jumps.length; i++) {
      if ((i & 1) == 1) {
        tc.v2 = Bytecode2TCCode.setGotoIndex(ji.jumps[i]);
      } else {
        tc = new Two16(line);
        tc.v1 = Bytecode2TCCode.setGotoIndex(ji.jumps[i]);
        sswitch.params[k++] = tc;
      }
    }
  }
}
