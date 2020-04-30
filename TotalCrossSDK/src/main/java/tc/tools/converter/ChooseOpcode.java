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

import tc.tools.converter.oper.OperandKind;

/*
 * Implementation of functions for selection of opcodes for the virtual machine instructions.
 *
 * If a function returns negative value, means that the caller needs to make an swap between
 * the operands. For example, if function chooseOpcodeMul is called, and its parameters, src1 and src2,
 * are a constant value of 12 bits and an int register, respectively, then ( - MUL_regI_regI_s12) is
 * returned. In the multiplication, first the register, then the constant.
 * The caller knows that will be necessary swap src1 and src2, and then change the signal of the
 * returned value to get correct opcode.
 *
 * If a function returns positive value, this already is the opcode selected.
 *
 * The caller also cannot forget to make the adequacy of the operands of the call to the selected operands
 * (defined in opcode). For example:
 *
 * Example 1:
 * Operands of the call: MUL OPR_regI, OPR_regI, OPR_s24 // regI = regI * s24;  s24 is a signed constant of 24 bits.
 * Opcode selected: MUL_regI_regI_regI
 * The caller must move s24 to regI.
 *
 * Example 2:
 * Operands of the call: MUL OPR_regI, OPR_regI, OPR_symI // regI = regI * symI;  symI is in the constant pool.
 * Opcode selected: MUL_regI_regI_regI
 * The caller must move symI to regI.
 *
 * Example 3:
 * Operands of the call: ADD OPR_regI, OPR_regI, OPR_s24 // regI = regI + s24;
 * Opcode selected: ADD_regI_regI_sym
 * The caller must move s24 to sym.
 *
 * To make adequacy of operands, use the function promoteOperand(...).
 */

public class ChooseOpcode implements TCConstants {
  public static int chooseOp(int prefix) {
    switch (prefix) {
    case pref_ARRAYLEN:
      return MOV_regI_arlen;
    case pref_SWITCH:
      return SWITCH;
    case pref_NEWOBJ:
      return NEWOBJ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  public static int chooseOp(int prefix, int opr) {
    switch (prefix) {
    case pref_JUMP:
      return JUMP_s24;
    case pref_NEWARRAY:
      return opr == opr_s6I ? NEWARRAY_len : NEWARRAY_regI;
    case pref_RETURNI:
      return Return(opr_regI, opr); // target, source
    case pref_RETURNO:
      return Return(opr_regO, opr);
    case pref_RETURND:
      return Return(opr_regD, opr);
    case pref_RETURNL:
      return Return(opr_regL, opr);
    }
    return INSTRUCTION_NOT_FOUND;
  }

  public static int chooseOp(int prefix, int opr1, int opr2) {
    switch (prefix) {
    case pref_MOV:
      return mov(opr1, opr2);
    case pref_CONV:
      return conv(opr1, opr2);
    case pref_JEQ:
      return jeq(opr1, opr2);
    case pref_JNE:
      return jne(opr1, opr2);
    case pref_JLT:
      return jlt(opr1, opr2);
    case pref_JGT:
      return jgt(opr1, opr2);
    case pref_JLE:
      return jle(opr1, opr2);
    case pref_JGE:
      return jge(opr1, opr2);
    case pref_CHECKCAST:
      return CHECKCAST;
    case pref_NEWOBJ:
      return NEWOBJ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  public static int chooseOp(int prefix, int opr1, int opr2, int opr3) {
    switch (prefix) {
    case pref_ADD:
      return add(opr1, opr2, opr3);
    case pref_SUB:
      return sub(opr1, opr2, opr3);
    case pref_MUL:
      return mul(opr1, opr2, opr3);
    case pref_AND:
      return and(opr1, opr2, opr3);
    case pref_DIV:
      return div(opr1, opr2, opr3);
    case pref_MOD:
      return mod(opr1, opr2, opr3);
    case pref_SHL:
      return shl(opr1, opr2, opr3);
    case pref_SHR:
      return shr(opr1, opr2, opr3);
    case pref_USHR:
      return ushr(opr1, opr2, opr3);
    case pref_OR:
      return or(opr1, opr2, opr3);
    case pref_XOR:
      return xor(opr1, opr2, opr3);
    case pref_NEWARRAY:
      return chooseOp(prefix, opr3);
    case pref_INSTANCEOF:
      return INSTANCEOF;
    case pref_JEQ:
      return jeq(opr1, opr2);
    case pref_JNE:
      return jne(opr1, opr2);
    case pref_JLT:
      return jlt(opr1, opr2);
    case pref_JGT:
      return jgt(opr1, opr2);
    case pref_JLE:
      return jle(opr1, opr2);
    case pref_JGE:
      return jge(opr1, opr2);
    }
    return INSTRUCTION_NOT_FOUND;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////    MOV MOV MOV MOV MOV MOV MOV MOV MOV MOV MOV MOV MOV MOV MOV /////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //   Given operands target and src (source) of a MOV operation, choose and return the opcode.
  //   target and src are one of opr_XXX in TCConstants.java
  public static int mov(int target, int src) {
    switch (target) {
    /////////////////////////// MOV_regI_? /////////////////////////////
    case opr_regI: {
      switch (src) {
      case opr_regIb:
      case opr_regIc:
      case opr_regIs:
      case opr_reg16:
      case opr_regI:
        return MOV_regI_regI;
      case opr_fieldI:
        return MOV_regI_field;
      case opr_staticI:
        return MOV_regI_static;
      case opr_aruI:
        return MOV_regI_aru;
      case opr_arcI:
        return MOV_regI_arc;
      case opr_arlen:
        return MOV_regI_arlen;
      default: {
        if (OperandKind.isConstantFitIn18Bits(src)) {
          return MOV_regI_s18;
        }
        if (OperandKind.isSymI_s24_s32(src)) {
          return MOV_regI_sym;
        }
        if (OperandKind.isTypeD(src)) {
          return CONV_regI_regD;
        }
        if (OperandKind.isTypeL(src)) {
          return CONV_regI_regL;
        }
      }
      }
      break;
    }

    /////////////////////////// MOV_regD_? /////////////////////////////
    case opr_regD: {
      switch (src) {
      case opr_regD:
        return MOV_reg64_reg64;
      case opr_fieldD:
        return MOV_reg64_field;
      case opr_staticD:
        return MOV_reg64_static;
      case opr_aruD:
        return MOV_reg64_aru;
      case opr_arcD:
        return MOV_reg64_arc;
      case opr_symD:
        return MOV_regD_sym;
      default: {
        if (OperandKind.isConstantFitIn18Bits(src)) {
          return MOV_regD_s18;
        }
        if (OperandKind.isConstantNotFitIn18Bits(src)) {
          return MOV_regD_sym;
        }
        if (OperandKind.isTypeIExceptConstants(src)) {
          return CONV_regD_regI;
        }
        if (OperandKind.isSuffixL(src)) {
          return CONV_regD_regL;
        }
      }
      }
      break;
    }

    /////////////////////////// MOV_regL_? /////////////////////////////
    case opr_regL: {
      switch (src) {
      case opr_regL:
        return MOV_reg64_reg64;
      case opr_fieldL:
        return MOV_reg64_field;
      case opr_staticL:
        return MOV_reg64_static;
      case opr_aruL:
        return MOV_reg64_aru;
      case opr_arcL:
        return MOV_reg64_arc;
      case opr_symL:
        return MOV_regL_sym;
      default: {
        if (OperandKind.isConstantFitIn18Bits(src)) {
          return MOV_regL_s18;
        }
        if (OperandKind.isConstantNotFitIn18Bits(src)) {
          return MOV_regL_sym;
        }
        if (OperandKind.isTypeIExceptConstants(src)) {
          return CONV_regL_regI;
        }
        if (OperandKind.isTypeD(src)) {
          return CONV_regL_regD;
        }
      }
      }
      break;
    }

    /////////////////////////// MOV_regO_? /////////////////////////////
    case opr_regO: {
      switch (src) {
      case opr_regO:
        return MOV_regO_regO;
      case opr_fieldO:
        return MOV_regO_field;
      case opr_staticO:
        return MOV_regO_static;
      case opr_aruO:
        return MOV_regO_aru;
      case opr_arcO:
        return MOV_regO_arc;
      case opr_symO:
        return MOV_regO_sym;
      case opr_null:
        return MOV_regO_null;
      default:
        ;
      }
      break;
    }

    /////////////////////////// MOV_extFieldI_? /////////////////////////////
    case opr_fieldI: {
      if (OperandKind.isNumerical(src)) {
        return MOV_field_regI;
      }
      break;
    }

    /////////////////////////// MOV_extFieldD_? /////////////////////////////
    case opr_fieldD: {
      if (OperandKind.isNumerical(src)) {
        return MOV_field_reg64;
      }
      break;
    }

    /////////////////////////// MOV_extFieldL_? /////////////////////////////
    case opr_fieldL: {
      if (OperandKind.isNumerical(src)) {
        return MOV_field_reg64;
      }
      break;
    }

    /////////////////////////// MOV_extFieldO_? /////////////////////////////
    case opr_fieldO: {
      if (OperandKind.isTypeO(src)) {
        return MOV_field_regO;
      }
      break;
    }

    /////////////////////////// MOV_extStaticI_? /////////////////////////////
    case opr_staticI: {
      if (OperandKind.isNumerical(src)) {
        return MOV_static_regI;
      }
      break;
    }

    /////////////////////////// MOV_extStaticD_? /////////////////////////////
    case opr_staticD: {
      if (OperandKind.isNumerical(src)) {
        return MOV_static_reg64;
      }
      break;
    }

    /////////////////////////// MOV_extStaticL_? /////////////////////////////
    case opr_staticL: {
      if (OperandKind.isNumerical(src)) {
        return MOV_static_reg64;
      }
      break;
    }

    /////////////////////////// MOV_extStaticO_? /////////////////////////////
    case opr_staticO: {
      if (OperandKind.isTypeO(src)) {
        return MOV_static_regO;
      }
      break;
    }

    /////////////////////////// MOV_regIb_? /////////////////////////////
    case opr_regIb: {
      switch (src) {
      case opr_aruI:
        return MOV_regIb_aru;
      case opr_arcI:
        return MOV_regIb_arc;
      default:
        if (OperandKind.isTypeIExceptARU_I_ARC_I(src)) {
          return CONV_regIb_regI;
        }
      }
      break;
    }

    //////////////////// MOV_regIs_? or MOV_reg16_? //////////////////////
    case opr_regIs:
    case opr_reg16: {
      switch (src) {
      case opr_arcI:
        return MOV_reg16_arc;
      case opr_aruI:
        return CONV_regIs_regI;
      default:
        if (OperandKind.isTypeIExceptARU_I_ARC_I(src)) {
          return CONV_regIs_regI;
        }
      }
      break;
    }

    /////////////////////////// MOV_regIc_? /////////////////////////////
    case opr_regIc: {
      switch (src) {
      case opr_aruI:
        return MOV_reg16_aru;
      case opr_arcI:
        return MOV_reg16_arc;
      default:
        if (OperandKind.isTypeIExceptARU_I_ARC_I(src)) {
          return CONV_regIc_regI;
        }
      }
      break;
    }

    /////////////////////////// MOV_aruI_? /////////////////////////////
    case opr_aruI: {
      switch (src) {
      case opr_regIb:
        return MOV_aru_regIb;
      case opr_regIc:
        return MOV_aru_reg16;
      case opr_regIs:
        return MOV_aru_regI;
      default:
        if (OperandKind.isNumericalExceptRegIx(src)) {
          return MOV_aru_regI;
        }
      }
      break;
    }

    /////////////////////////// MOV_aruIb_? /////////////////////////////
    case opr_aruIb:
      return MOV_aru_regIb;

    /////////////////// MOV_aruIc_? or MOV_aruIs_? //////////////////////
    case opr_aruIc:
    case opr_aruIs:
      return MOV_aru_reg16;

    /////////////////////////// MOV_aruD_? /////////////////////////////
    case opr_aruD: {
      if (OperandKind.isNumerical(src)) {
        return MOV_aru_reg64;
      }
      break;
    }

    /////////////////////////// MOV_aruL_? /////////////////////////////
    case opr_aruL: {
      if (OperandKind.isNumerical(src)) {
        return MOV_aru_reg64;
      }
      break;
    }

    /////////////////////////// MOV_aruO_? /////////////////////////////
    case opr_aruO: {
      if (OperandKind.isTypeO(src)) {
        return MOV_aru_regO;
      }
      break;
    }

    /////////////////////////// MOV_arcI_? /////////////////////////////
    case opr_arcI: {
      switch (src) {
      case opr_regIb:
        return MOV_arc_regIb;
      case opr_regIs:
        return MOV_arc_reg16;
      case opr_regIc:
        return MOV_arc_reg16;
      default:
        if (OperandKind.isNumericalExceptRegIx(src)) {
          return MOV_arc_regI;
        }
      }
      break;
    }

    /////////////////////////// MOV_arcIb_? /////////////////////////////
    case opr_arcIb:
      return MOV_arc_regIb;

    /////////////////// MOV_arcIc_? or MOV_arcIs_? //////////////////////
    case opr_arcIc:
    case opr_arcIs:
      return MOV_arc_reg16;

    /////////////////////////// MOV_arcD_? /////////////////////////////
    case opr_arcD: {
      if (OperandKind.isNumerical(src)) {
        return MOV_arc_reg64;
      }
      break;
    }

    /////////////////////////// MOV_arcL_? /////////////////////////////
    case opr_arcL: {
      if (OperandKind.isNumerical(src)) {
        return MOV_arc_reg64;
      }
      break;
    }

    /////////////////////////// MOV_arcO_? /////////////////////////////
    case opr_arcO: {
      if (OperandKind.isTypeO(src)) {
        return MOV_arc_regO;
      }
      break;
    }
    default:
      ;
    }

    return INSTRUCTION_NOT_FOUND;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////   / CONV CONV CONV CONV CONV CONV CONV CONV CONV CONV CONV CONV CONV CONV CONV /////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //   Given operands target and src (source) of a CONV operation, choose and return the opcode.
  private static int conv(int target, int src) {
    switch (target) {
    /////////////////////////// CONV_regI_? /////////////////////////////
    case opr_regI: {
      if (OperandKind.isTypeI(src)) {
        return mov(target, src);
      }
      if (OperandKind.isTypeL(src)) {
        return CONV_regI_regL;
      }
      if (OperandKind.isTypeD(src)) {
        return CONV_regI_regD;
      }
      break;
    }

    /////////////////////////// CONV_regL_? /////////////////////////////
    case opr_regL: {
      if (OperandKind.isTypeI(src)) {
        return CONV_regL_regI;
      }
      if (OperandKind.isTypeL(src)) {
        return mov(target, src);
      }
      if (OperandKind.isTypeD(src)) {
        return CONV_regL_regD;
      }
      break;
    }

    /////////////////////////// CONV_regD_? /////////////////////////////
    case opr_regD: {
      if (OperandKind.isTypeI(src)) {
        return CONV_regD_regI;
      }
      if (OperandKind.isTypeL(src)) {
        return CONV_regD_regL;
      }
      if (OperandKind.isTypeD(src)) {
        return mov(target, src);
      }
      break;
    }

    /////////////////////////// CONV_regIb_? /////////////////////////////
    case opr_regIb: {
      if (OperandKind.isTypeI(src)) {
        return CONV_regIb_regI;
      }
      if (OperandKind.isTypeL(src)) {
        return CONV_regIb_regI;
      }
      if (OperandKind.isTypeD(src)) {
        return CONV_regIb_regI;
      }
      break;
    }

    /////////////////////////// CONV_regIc_? /////////////////////////////
    case opr_regIc: {
      if (OperandKind.isTypeI(src)) {
        return CONV_regIc_regI;
      }
      if (OperandKind.isTypeL(src)) {
        return CONV_regIc_regI;
      }
      if (OperandKind.isTypeD(src)) {
        return CONV_regIc_regI;
      }
      break;
    }

    /////////////////////////// CONV_regIs_? /////////////////////////////
    case opr_regIs: {
      if (OperandKind.isTypeI(src)) {
        return CONV_regIs_regI;
      }
      if (OperandKind.isTypeL(src)) {
        return CONV_regIs_regI;
      }
      if (OperandKind.isTypeD(src)) {
        return CONV_regIs_regI;
      }
      break;
    }
    default:
      ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////    ADD ADD ADD ADD ADD ADD ADD ADD ADD ADD ADD ADD ADD ADD ADD /////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //   Given operands target and sources (src1 and src2) of an ADD operation, choose and return the opcode.
  private static int add(int target, int src1, int src2) {
    switch (target) {
    /////////////////////////// ADD_regI_?_? /////////////////////////////
    case opr_regIb:
    case opr_regIc:
    case opr_regIs:
    case opr_reg16:
    case opr_regI: {
      //specific cases
      if (src1 == opr_arcI && src2 == opr_s6I) {
        return ADD_regI_arc_s6;
      }
      if (src1 == opr_s6I && src2 == opr_arcI) {
        return (-ADD_regI_arc_s6); //swap src1 <-> src2
      }
      if (src1 == opr_aruI && src2 == opr_s6I) {
        return ADD_regI_aru_s6;
      }
      if (src1 == opr_s6I && src2 == opr_aruI) {
        return (-ADD_regI_aru_s6); //swap src1 <-> src2
      }
      if (src2 == opr_s6I || src2 == opr_s12I) {
        return ADD_regI_s12_regI;
      }
      if (src1 == opr_s6I || src1 == opr_s12I) {
        return (-ADD_regI_s12_regI); //swap src1 <-> src2
      }

      switch (src2) {
      case opr_s18I:
      case opr_s24I:
      case opr_s32I:
      case opr_s16I:
      case opr_u16:
      case opr_symI:
        return ADD_regI_regI_sym;
      default:
        ;
      }
      switch (src1) {
      case opr_s18I:
      case opr_s24I:
      case opr_s32I:
      case opr_s16I:
      case opr_u16:
      case opr_symI:
        return (-ADD_regI_regI_sym); //swap
      default:
        ;
      }

      //general case
      return ADD_regI_regI_regI;
    }

    /////////////////////////// ADD_regD_?_? /////////////////////////////
    case opr_regD: {
      return ADD_regD_regD_regD;
    }

    /////////////////////////// ADD_regL_?_? /////////////////////////////
    case opr_regL: {
      return ADD_regL_regL_regL;
    }

    /////////////////////////// ADD_aru_?_? /////////////////////////////
    case opr_aruI:
    case opr_aruD:
    case opr_aruL:
    case opr_aruO: {
      if (src2 == opr_s6I) {
        return ADD_aru_regI_s6;
      }
      if (src1 == opr_s6I) {
        return (-ADD_aru_regI_s6); //swap
      }

      break;
    }

    default:
      ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////    SUB SUB SUB SUB SUB SUB SUB SUB SUB SUB SUB SUB SUB SUB SUB /////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //   Given operands target and sources (src1 and src2) of an SUB operation, choose and return the opcode.
  private static int sub(int target, int src1, int src2) {
    switch (target) {
    /////////////////////////// SUB_regI_?_? /////////////////////////////
    case opr_regIb:
    case opr_regIc:
    case opr_regIs:
    case opr_reg16:
    case opr_regI: {
      //specific cases
      if (src1 == opr_s6I || src1 == opr_s12I) {
        return SUB_regI_s12_regI;
        //if (src2 == opr_s6 || src2 == opr_s12)  return _SUB_regI_regI_s12; //special opcode
      }

      //general case
      return SUB_regI_regI_regI;
    }

    /////////////////////////// SUB_regD_?_? /////////////////////////////
    case opr_regD: {
      return SUB_regD_regD_regD;
    }

    /////////////////////////// SUB_regL_?_? /////////////////////////////
    case opr_regL: {
      return SUB_regL_regL_regL;
    }
    default:
      ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////    MUL MUL MUL MUL MUL MUL MUL MUL MUL MUL MUL MUL MUL MUL MUL /////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //

  //   Given operands target and sources (src1 and src2) of an MUL operation, choose and return the opcode.
  private static int mul(int target, int src1, int src2) {
    switch (target) {
    /////////////////////////// MUL_regI_?_? /////////////////////////////
    case opr_regIb:
    case opr_regIc:
    case opr_regIs:
    case opr_reg16:
    case opr_regI: {
      //specific cases
      if (src2 == opr_s6I || src2 == opr_s12I) {
        return MUL_regI_regI_s12;
      }
      if (src1 == opr_s6I || src1 == opr_s12I) {
        return (-MUL_regI_regI_s12); //swap
      }

      //general case
      return MUL_regI_regI_regI;
    }

    /////////////////////////// MUL_regD_?_? /////////////////////////////
    case opr_regD: {
      return MUL_regD_regD_regD;
    }

    /////////////////////////// MUL_regL_?_? /////////////////////////////
    case opr_regL: {
      return MUL_regL_regL_regL;
    }
    default:
      ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////DIV DIV DIV DIV DIV DIV DIV DIV DIV DIV DIV DIV DIV DIV DIV /////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////MOD MOD MOD MOD MOD MOD MOD MOD MOD MOD MOD MOD MOD MOD MOD /////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  private static int div(int target, int src1, int src2) {
    return div_mod(pref_DIV, target, src1, src2);
  }

  private static int mod(int target, int src1, int src2) {
    return div_mod(pref_MOD, target, src1, src2);
  }

  private static int div_mod(int prefix, int target, int src1, int src2) {
    switch (target) {
    case opr_regI: {
      if (src2 == opr_s6I || src2 == opr_s12I) {
        return (prefix == pref_DIV ? DIV_regI_regI_s12 : MOD_regI_regI_s12);
      }

      return (prefix == pref_DIV ? DIV_regI_regI_regI : MOD_regI_regI_regI);
    }
    case opr_regD: {
      return (prefix == pref_DIV ? DIV_regD_regD_regD : MOD_regD_regD_regD);
    }
    case opr_regL: {
      return (prefix == pref_DIV ? DIV_regL_regL_regL : MOD_regL_regL_regL);
    }
    default:
      ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////SHL SHL SHL SHL SHL SHL SHL SHL SHL SHL SHL SHL SHL SHL SHL /////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////SHR SHR SHR SHR SHR SHR SHR SHR SHR SHR SHR SHR SHR SHR SHR /////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  ///////////// USHR USHR USHR USHR USHR USHR USHR USHR USHR USHR USHR USHR USHR USHR USHR ///////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  private static int shl(int target, int src1, int src2) {
    return shift(pref_SHL, target, src1, src2);
  }

  private static int shr(int target, int src1, int src2) {
    return shift(pref_SHR, target, src1, src2);
  }

  private static int ushr(int target, int src1, int src2) {
    return shift(pref_USHR, target, src1, src2);
  }

  private static int shift(int prefix, int target, int src1, int src2) {
    switch (target) {
    case opr_regI: {
      if (src2 == opr_s6I || src2 == opr_s12I) {
        return (prefix == pref_SHL ? SHL_regI_regI_s12 : prefix == pref_SHR ? SHR_regI_regI_s12 : USHR_regI_regI_s12);
      }

      return (prefix == pref_SHL ? SHL_regI_regI_regI : prefix == pref_SHR ? SHR_regI_regI_regI : USHR_regI_regI_regI);
    }
    case opr_regL: {
      return (prefix == pref_SHL ? SHL_regL_regL_regL : prefix == pref_SHR ? SHR_regL_regL_regL : USHR_regL_regL_regL);
    }
    default:
      ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////    AND AND AND AND AND AND AND AND AND AND AND AND AND AND AND /////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //   Given operands target and sources (src1 and src2) of an AND operation, choose and return the opcode.
  private static int and(int target, int src1, int src2) {
    switch (target) {
    /////////////////////////// AND_regI_?_? /////////////////////////////
    case opr_regIs:
    case opr_reg16:
    case opr_regIc:
    case opr_regIb:
    case opr_regI: {
      //specific cases
      if (src1 == opr_aruI && src2 == opr_s6I) {
        return AND_regI_aru_s6;
      }
      if (src1 == opr_s6I && src2 == opr_aruI) {
        return (-AND_regI_aru_s6); //swap src1 <-> src2
      }
      if (src2 == opr_s6I || src2 == opr_s12I) {
        return AND_regI_regI_s12;
      }
      if (src1 == opr_s6I || src1 == opr_s12I) {
        return (-AND_regI_regI_s12); //swap
      }

      //general case
      return AND_regI_regI_regI;
    }

    /////////////////////////// AND_regL_?_? /////////////////////////////
    case opr_regL: {
      return AND_regL_regL_regL;
    }
    default:
      ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////OR OR OR OR OR OR OR OR OR OR OR OR OR OR OR ////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////// XOR XOR XOR XOR XOR XOR XOR XOR XOR XOR XOR XOR XOR XOR XOR ////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  private static int or(int target, int src1, int src2) {
    return or_xor(pref_OR, target, src1, src2);
  }

  private static int xor(int target, int src1, int src2) {
    return or_xor(pref_XOR, target, src1, src2);
  }

  private static int or_xor(int prefix, int target, int src1, int src2) {
    switch (target) {
    case opr_regI: {
      if (src2 == opr_s6I || src2 == opr_s12I) {
        return (prefix == pref_OR ? OR_regI_regI_s12 : XOR_regI_regI_s12);
      }
      if (src1 == opr_s6I || src1 == opr_s12I) {
        return (prefix == pref_OR ? (-OR_regI_regI_s12) : (-XOR_regI_regI_s12));
      }

      return (prefix == pref_OR ? OR_regI_regI_regI : XOR_regI_regI_regI);
    }
    case opr_regL: {
      return (prefix == pref_OR ? OR_regL_regL_regL : XOR_regL_regL_regL);
    }
    default:
      ;
    }
    return INSTRUCTION_NOT_FOUND;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////    JLE JLE JLE JLE JLE JLE JLE JLE JLE JLE JLE JLE JLE JLE JLE /////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //

  private static int jle(int src1, int src2) {
    if (src2 == opr_s6I) {
      if (OperandKind.isTypeI(src1)) {
        return JLE_regI_s6;
      }
    }
    if (src1 == opr_s6I) {
      if (OperandKind.isTypeI(src2)) {
        return (-JGE_regI_s6);
      }
    }
    if (OperandKind.isTypeI(src1)) {
      if (OperandKind.isTypeL(src2)) {
        return JLE_regL_regL;
      }
      if (OperandKind.isTypeD(src2)) {
        return JLE_regD_regD;
      }

      return JLE_regI_regI;
    }
    if (OperandKind.isTypeL(src1)) {
      if (OperandKind.isTypeD(src2)) {
        return JLE_regD_regD;
      }

      return JLE_regL_regL;
    }
    if (OperandKind.isTypeD(src1)) {
      return JLE_regD_regD;
    }

    return INSTRUCTION_NOT_FOUND;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////    JGE JGE JGE JGE JGE JGE JGE JGE JGE JGE JGE JGE JGE JGE JGE /////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //

  private static int jge(int src1, int src2) {
    if (src2 == opr_s6I) {
      if (OperandKind.isTypeI(src1)) {
        return JGE_regI_s6;
      }
    }
    if (src1 == opr_s6I) {
      if (OperandKind.isTypeI(src2)) {
        return (-JLE_regI_s6);
      }
    }
    if (src2 == opr_arlen) {
      if (OperandKind.isTypeI(src1)) {
        return JGE_regI_arlen;
      }
    }
    if (OperandKind.isTypeI(src1)) {
      if (OperandKind.isTypeL(src2)) {
        return JGE_regL_regL;
      }
      if (OperandKind.isTypeD(src2)) {
        return JGE_regD_regD;
      }

      return JGE_regI_regI;
    }
    if (OperandKind.isTypeL(src1)) {
      if (OperandKind.isTypeD(src2)) {
        return JGE_regD_regD;
      }

      return JGE_regL_regL;
    }
    if (OperandKind.isTypeD(src1)) {
      return JGE_regD_regD;
    }

    return INSTRUCTION_NOT_FOUND;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////    JEQ JEQ JEQ JEQ JEQ JEQ JEQ JEQ JEQ JEQ JEQ JEQ JEQ JEQ JEQ /////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////    JNE JNE JNE JNE JNE JNE JNE JNE JNE JNE JNE JNE JNE JNE JNE /////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  private static int jeq(int src1, int src2) {
    return jeq_jne(pref_JEQ, src1, src2);
  }

  private static int jne(int src1, int src2) {
    return jeq_jne(pref_JNE, src1, src2);
  }

  private static int jeq_jne(int prefix, int src1, int src2) {
    if (src2 == opr_s6I) {
      if (OperandKind.isTypeI(src1)) {
        return (prefix == pref_JEQ ? JEQ_regI_s6 : JNE_regI_s6);
      }
    }
    if (src1 == opr_s6I) {
      if (OperandKind.isTypeI(src2)) {
        return (prefix == pref_JEQ ? (-JEQ_regI_s6) : (-JNE_regI_s6));
      }
    }
    if (src2 == opr_symI || OperandKind.isConstantI(src2)) {
      if (OperandKind.isTypeI(src1)) {
        return (prefix == pref_JEQ ? JEQ_regI_sym : JNE_regI_sym);
      }
    }
    if (src1 == opr_symI || OperandKind.isConstantI(src1)) {
      if (OperandKind.isTypeI(src2)) {
        return (prefix == pref_JEQ ? (-JEQ_regI_sym) : (-JNE_regI_sym));
      }
    }
    if (src2 == opr_null) {
      if (OperandKind.isTypeO(src1)) {
        return (prefix == pref_JEQ ? JEQ_regO_null : JNE_regO_null);
      }
    }
    if (src1 == opr_null) {
      if (OperandKind.isTypeO(src2)) {
        return (prefix == pref_JEQ ? (-JEQ_regO_null) : (-JNE_regO_null));
      }
    }
    if (OperandKind.isTypeI(src1)) {
      if (OperandKind.isTypeL(src2)) {
        return (prefix == pref_JEQ ? JEQ_regL_regL : JNE_regL_regL);
      }
      if (OperandKind.isTypeD(src2)) {
        return (prefix == pref_JEQ ? JEQ_regD_regD : JNE_regD_regD);
      }

      return (prefix == pref_JEQ ? JEQ_regI_regI : JNE_regI_regI);
    }
    if (OperandKind.isTypeL(src1)) {
      if (OperandKind.isTypeD(src2)) {
        return (prefix == pref_JEQ ? JEQ_regD_regD : JNE_regD_regD);
      }

      return (prefix == pref_JEQ ? JEQ_regL_regL : JNE_regL_regL);
    }
    if (OperandKind.isTypeD(src1)) {
      return (prefix == pref_JEQ ? JEQ_regD_regD : JNE_regD_regD);
    }
    if (OperandKind.isTypeO(src1)) {
      return (prefix == pref_JEQ ? JEQ_regO_regO : JNE_regO_regO);
    }

    return INSTRUCTION_NOT_FOUND;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////    JLT JLT JLT JLT JLT JLT JLT JLT JLT JLT JLT JLT JLT JLT JLT /////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  //////////////////    JGT JGT JGT JGT JGT JGT JGT JGT JGT JGT JGT JGT JGT JGT JGT /////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////   //
  private static int jlt(int src1, int src2) {
    return jlt_jgt(pref_JLT, src1, src2);
  }

  private static int jgt(int src1, int src2) {
    return jlt_jgt(pref_JGT, src1, src2);
  }

  private static int jlt_jgt(int prefix, int src1, int src2) {
    if (src2 == opr_s6I) {
      if (OperandKind.isTypeI(src1)) {
        return (prefix == pref_JLT ? JLT_regI_s6 : JGT_regI_s6);
      }
    }
    if (src1 == opr_s6I) {
      if (OperandKind.isTypeI(src2)) {
        return (prefix == pref_JLT ? (-JGT_regI_s6) : (-JLT_regI_s6));
      }
    }
    if (OperandKind.isTypeI(src1)) {
      if (OperandKind.isTypeL(src2)) {
        return (prefix == pref_JLT ? JLT_regL_regL : JGT_regL_regL);
      }
      if (OperandKind.isTypeD(src2)) {
        return (prefix == pref_JLT ? JLT_regD_regD : JGT_regD_regD);
      }

      return (prefix == pref_JLT ? JLT_regI_regI : JGT_regI_regI);
    }
    if (OperandKind.isTypeL(src1)) {
      if (OperandKind.isTypeD(src2)) {
        return (prefix == pref_JLT ? JLT_regD_regD : JGT_regD_regD);
      }

      return (prefix == pref_JLT ? JLT_regL_regL : JGT_regL_regL);
    }
    if (OperandKind.isTypeD(src1)) {
      return (prefix == pref_JLT ? JLT_regD_regD : JGT_regD_regD);
    }

    return INSTRUCTION_NOT_FOUND;
  }

  //   Given target and source operands of a RETURN operation, choose and return the opcode.
  private static int Return(int target, int src) {
    switch (src) {
    case opr_null:
    case opr_s6I:
    case opr_s12I:
    case opr_u16:
    case opr_s16I:
    case opr_s18I:
    case opr_s24I: {
      switch (target) {
      case opr_regI:
        return RETURN_s24I;
      case opr_regO:
        return RETURN_null;
      case opr_regD:
        return RETURN_s24D;
      case opr_regL:
        return RETURN_s24L;
      }
      break;
    }

    case opr_symI:
    case opr_s32I: {
      switch (target) {
      case opr_regI:
        return RETURN_symI;
      case opr_regD:
        return RETURN_symD;
      case opr_regL:
        return RETURN_symL;
      }
      break;
    }

    case opr_symL:
    case opr_s64L:
    case opr_s64D: {
      switch (target) {
      case opr_regD:
        return RETURN_symD;
      case opr_regL:
        return RETURN_symL;
      }
      break;
    }

    case opr_symO:
      return RETURN_symO;
    case opr_symD:
      return RETURN_symD;
    }

    switch (target) {
    case opr_regIs:
    case opr_reg16:
    case opr_regIc:
    case opr_regIb:
    case opr_regI:
      return RETURN_regI;

    case opr_regD:
    case opr_regL:
      return RETURN_reg64;

    case opr_regO:
      return RETURN_regO;

    default:
      System.out.println("ERROR: return not implemented for type " + target);
    }

    return INSTRUCTION_NOT_FOUND;
  }
}
