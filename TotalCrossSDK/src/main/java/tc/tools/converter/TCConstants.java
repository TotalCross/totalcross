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

public interface TCConstants extends tc.tools.converter.tclass.TClassConstants {
  public static final String[] bcTClassNames = { "NOP", "MOV_regI_regI", "MOV_regI_field", "MOV_regI_static",
      "MOV_regI_aru", "MOV_regI_arc", "MOV_regI_sym", "MOV_regI_s18", "MOV_regI_arlen", "MOV_regO_regO",
      "MOV_regO_field", "MOV_regO_static", "MOV_regO_aru", "MOV_regO_arc", "MOV_regO_sym", "MOV_reg64_reg64",
      "MOV_reg64_field", "MOV_reg64_static", "MOV_reg64_aru", "MOV_reg64_arc", "MOV_regD_sym", "MOV_regL_sym",
      "MOV_regD_s18", "MOV_regL_s18", "MOV_field_regI", "MOV_field_regO", "MOV_field_reg64", "MOV_static_regI",
      "MOV_static_regO", "MOV_static_reg64", "MOV_arc_regI", "MOV_arc_regO", "MOV_arc_reg64", "MOV_aru_regI",
      "MOV_aru_regO", "MOV_aru_reg64", "MOV_arc_regIb", "MOV_arc_reg16", "MOV_aru_regIb", "MOV_aru_reg16",
      "MOV_regIb_arc", "MOV_reg16_arc", "MOV_regIb_aru", "MOV_reg16_aru", "MOV_regO_null", "INC_regI",
      "ADD_regI_regI_regI", "ADD_regI_s12_regI", "ADD_regI_arc_s6", "ADD_regI_aru_s6", "ADD_regI_regI_sym",
      "ADD_regD_regD_regD", "ADD_regL_regL_regL", "ADD_aru_regI_s6", "SUB_regI_s12_regI", "SUB_regI_regI_regI",
      "SUB_regD_regD_regD", "SUB_regL_regL_regL", "MUL_regI_regI_s12", "MUL_regI_regI_regI", "MUL_regD_regD_regD",
      "MUL_regL_regL_regL", "DIV_regI_regI_s12", "DIV_regI_regI_regI", "DIV_regD_regD_regD", "DIV_regL_regL_regL",
      "MOD_regI_regI_s12", "MOD_regI_regI_regI", "MOD_regD_regD_regD", "MOD_regL_regL_regL", "SHR_regI_regI_s12",
      "SHR_regI_regI_regI", "SHR_regL_regL_regL", "SHL_regI_regI_s12", "SHL_regI_regI_regI", "SHL_regL_regL_regL",
      "USHR_regI_regI_s12", "USHR_regI_regI_regI", "USHR_regL_regL_regL", "AND_regI_regI_s12", "AND_regI_aru_s6",
      "AND_regI_regI_regI", "AND_regL_regL_regL", "OR_regI_regI_s12", "OR_regI_regI_regI", "OR_regL_regL_regL",
      "XOR_regI_regI_s12", "XOR_regI_regI_regI", "XOR_regL_regL_regL", "JEQ_regO_regO", "JEQ_regO_null",
      "JEQ_regI_regI", "JEQ_regL_regL", "JEQ_regD_regD", "JEQ_regI_s6", "JEQ_regI_sym", "JNE_regO_regO",
      "JNE_regO_null", "JNE_regI_regI", "JNE_regL_regL", "JNE_regD_regD", "JNE_regI_s6", "JNE_regI_sym",
      "JLT_regI_regI", "JLT_regL_regL", "JLT_regD_regD", "JLT_regI_s6", "JLE_regI_regI", "JLE_regL_regL",
      "JLE_regD_regD", "JLE_regI_s6", "JGT_regI_regI", "JGT_regL_regL", "JGT_regD_regD", "JGT_regI_s6", "JGE_regI_regI",
      "JGE_regL_regL", "JGE_regD_regD", "JGE_regI_s6", "JGE_regI_arlen", "DECJGTZ_regI", "DECJGEZ_regI", "TEST_regO",
      "JUMP_s24", "CONV_regI_regL", "CONV_regI_regD", "CONV_regIb_regI", "CONV_regIc_regI", "CONV_regIs_regI",
      "CONV_regL_regI", "CONV_regL_regD", "CONV_regD_regI", "CONV_regD_regL", "RETURN_regI", "RETURN_regO",
      "RETURN_reg64", "RETURN_void", "RETURN_s24I", "RETURN_null", "RETURN_s24D", "RETURN_s24L", "RETURN_symI",
      "RETURN_symO", "RETURN_symD", "RETURN_symL", "SWITCH", "NEWARRAY_len", "NEWARRAY_regI", "NEWARRAY_multi",
      "NEWOBJ", "THROW", "INSTANCEOF", "CHECKCAST", "CALL_normal", "CALL_virtual", "JUMP_regI", "MONITOR_Enter",
      "MONITOR_Exit" };

  // Opcodes
  public static final int BREAK = 0;
  public static final int MOV_regI_regI = 1;
  public static final int MOV_regI_field = 2;
  public static final int MOV_regI_static = 3;
  public static final int MOV_regI_aru = 4;
  public static final int MOV_regI_arc = 5;
  public static final int MOV_regI_sym = 6;
  public static final int MOV_regI_s18 = 7;
  public static final int MOV_regI_arlen = 8;
  public static final int MOV_regO_regO = 9;
  public static final int MOV_regO_field = 10;
  public static final int MOV_regO_static = 11;
  public static final int MOV_regO_aru = 12;
  public static final int MOV_regO_arc = 13;
  public static final int MOV_regO_sym = 14;
  public static final int MOV_reg64_reg64 = 15;
  public static final int MOV_reg64_field = 16;
  public static final int MOV_reg64_static = 17;
  public static final int MOV_reg64_aru = 18;
  public static final int MOV_reg64_arc = 19;
  public static final int MOV_regD_sym = 20;
  public static final int MOV_regL_sym = 21;
  public static final int MOV_regD_s18 = 22;
  public static final int MOV_regL_s18 = 23;
  public static final int MOV_field_regI = 24;
  public static final int MOV_field_regO = 25;
  public static final int MOV_field_reg64 = 26;
  public static final int MOV_static_regI = 27;
  public static final int MOV_static_regO = 28;
  public static final int MOV_static_reg64 = 29;
  public static final int MOV_arc_regI = 30;
  public static final int MOV_arc_regO = 31;
  public static final int MOV_arc_reg64 = 32;
  public static final int MOV_aru_regI = 33;
  public static final int MOV_aru_regO = 34;
  public static final int MOV_aru_reg64 = 35;
  public static final int MOV_arc_regIb = 36;
  public static final int MOV_arc_reg16 = 37;
  public static final int MOV_aru_regIb = 38;
  public static final int MOV_aru_reg16 = 39;
  public static final int MOV_regIb_arc = 40;
  public static final int MOV_reg16_arc = 41;
  public static final int MOV_regIb_aru = 42;
  public static final int MOV_reg16_aru = 43;
  public static final int MOV_regO_null = 44;
  public static final int INC_regI = 45;
  public static final int ADD_regI_regI_regI = 46;
  public static final int ADD_regI_s12_regI = 47;
  public static final int ADD_regI_arc_s6 = 48;
  public static final int ADD_regI_aru_s6 = 49;
  public static final int ADD_regI_regI_sym = 50;
  public static final int ADD_regD_regD_regD = 51;
  public static final int ADD_regL_regL_regL = 52;
  public static final int ADD_aru_regI_s6 = 53;
  public static final int SUB_regI_s12_regI = 54;
  public static final int SUB_regI_regI_regI = 55;
  public static final int SUB_regD_regD_regD = 56;
  public static final int SUB_regL_regL_regL = 57;
  public static final int MUL_regI_regI_s12 = 58;
  public static final int MUL_regI_regI_regI = 59;
  public static final int MUL_regD_regD_regD = 60;
  public static final int MUL_regL_regL_regL = 61;
  public static final int DIV_regI_regI_s12 = 62;
  public static final int DIV_regI_regI_regI = 63;
  public static final int DIV_regD_regD_regD = 64;
  public static final int DIV_regL_regL_regL = 65;
  public static final int MOD_regI_regI_s12 = 66;
  public static final int MOD_regI_regI_regI = 67;
  public static final int MOD_regD_regD_regD = 68;
  public static final int MOD_regL_regL_regL = 69;
  public static final int SHR_regI_regI_s12 = 70;
  public static final int SHR_regI_regI_regI = 71;
  public static final int SHR_regL_regL_regL = 72;
  public static final int SHL_regI_regI_s12 = 73;
  public static final int SHL_regI_regI_regI = 74;
  public static final int SHL_regL_regL_regL = 75;
  public static final int USHR_regI_regI_s12 = 76;
  public static final int USHR_regI_regI_regI = 77;
  public static final int USHR_regL_regL_regL = 78;
  public static final int AND_regI_regI_s12 = 79;
  public static final int AND_regI_aru_s6 = 80;
  public static final int AND_regI_regI_regI = 81;
  public static final int AND_regL_regL_regL = 82;
  public static final int OR_regI_regI_s12 = 83;
  public static final int OR_regI_regI_regI = 84;
  public static final int OR_regL_regL_regL = 85;
  public static final int XOR_regI_regI_s12 = 86;
  public static final int XOR_regI_regI_regI = 87;
  public static final int XOR_regL_regL_regL = 88;
  public static final int JEQ_regO_regO = 89;
  public static final int JEQ_regO_null = 90;
  public static final int JEQ_regI_regI = 91;
  public static final int JEQ_regL_regL = 92;
  public static final int JEQ_regD_regD = 93;
  public static final int JEQ_regI_s6 = 94;
  public static final int JEQ_regI_sym = 95;
  public static final int JNE_regO_regO = 96;
  public static final int JNE_regO_null = 97;
  public static final int JNE_regI_regI = 98;
  public static final int JNE_regL_regL = 99;
  public static final int JNE_regD_regD = 100;
  public static final int JNE_regI_s6 = 101;
  public static final int JNE_regI_sym = 102;
  public static final int JLT_regI_regI = 103;
  public static final int JLT_regL_regL = 104;
  public static final int JLT_regD_regD = 105;
  public static final int JLT_regI_s6 = 106;
  public static final int JLE_regI_regI = 107;
  public static final int JLE_regL_regL = 108;
  public static final int JLE_regD_regD = 109;
  public static final int JLE_regI_s6 = 110;
  public static final int JGT_regI_regI = 111;
  public static final int JGT_regL_regL = 112;
  public static final int JGT_regD_regD = 113;
  public static final int JGT_regI_s6 = 114;
  public static final int JGE_regI_regI = 115;
  public static final int JGE_regL_regL = 116;
  public static final int JGE_regD_regD = 117;
  public static final int JGE_regI_s6 = 118;
  public static final int JGE_regI_arlen = 119;
  public static final int DECJGTZ_regI = 120;
  public static final int DECJGEZ_regI = 121;
  public static final int TEST_regO = 122;
  public static final int JUMP_s24 = 123;
  public static final int CONV_regI_regL = 124;
  public static final int CONV_regI_regD = 125;
  public static final int CONV_regIb_regI = 126;
  public static final int CONV_regIc_regI = 127;
  public static final int CONV_regIs_regI = 128;
  public static final int CONV_regL_regI = 129;
  public static final int CONV_regL_regD = 130;
  public static final int CONV_regD_regI = 131;
  public static final int CONV_regD_regL = 132;
  public static final int RETURN_regI = 133;
  public static final int RETURN_regO = 134;
  public static final int RETURN_reg64 = 135;
  public static final int RETURN_void = 136;
  public static final int RETURN_s24I = 137;
  public static final int RETURN_null = 138;
  public static final int RETURN_s24D = 139;
  public static final int RETURN_s24L = 140;
  public static final int RETURN_symI = 141;
  public static final int RETURN_symO = 142;
  public static final int RETURN_symD = 143;
  public static final int RETURN_symL = 144;
  public static final int SWITCH = 145;
  public static final int NEWARRAY_len = 146;
  public static final int NEWARRAY_regI = 147;
  public static final int NEWARRAY_multi = 148;
  public static final int NEWOBJ = 149;
  public static final int THROW = 150;
  public static final int INSTANCEOF = 151;
  public static final int CHECKCAST = 152;
  public static final int CALL_normal = 153;
  public static final int CALL_virtual = 154;
  public static final int JUMP_regI = 155;
  public static final int MONITOR_Enter = 156;
  public static final int MONITOR_Exit = 157;
  public static final int MONITOR_Enter2 = 158;
  public static final int MONITOR_Exit2 = 159;

  public static final int INSTRUCTION_NOT_FOUND = 255;

  // Operands
  public static final int opr_regI = 0;
  public static final int opr_regO = 1;
  public static final int opr_regD = 2;
  public static final int opr_regL = 3;
  public static final int opr_fieldI = 4;
  public static final int opr_fieldD = 5;
  public static final int opr_fieldL = 6;
  public static final int opr_fieldO = 7;
  public static final int opr_staticI = 8;
  public static final int opr_staticD = 9;
  public static final int opr_staticL = 10;
  public static final int opr_staticO = 11;
  public static final int opr_symI = 12;
  public static final int opr_symD = 13;
  public static final int opr_symL = 14;
  public static final int opr_symO = 15;
  public static final int opr_regIb = 16;
  public static final int opr_regIs = 17;
  public static final int opr_regIc = 18;
  public static final int opr_reg16 = 19;
  public static final int opr_s6I = 20;
  public static final int opr_s6D = 21;
  public static final int opr_s6L = 22;
  public static final int opr_s12I = 23;
  public static final int opr_s12D = 24;
  public static final int opr_s12L = 25;
  public static final int opr_s16I = 26;
  public static final int opr_s16D = 27;
  public static final int opr_s16L = 28;
  public static final int opr_s18I = 29;
  public static final int opr_s18D = 30;
  public static final int opr_s18L = 31;
  public static final int opr_s24I = 32;
  public static final int opr_s24D = 33;
  public static final int opr_s24L = 34;
  public static final int opr_s32I = 35;
  public static final int opr_s32D = 36;
  public static final int opr_s32L = 37;
  public static final int opr_s64L = 38;
  public static final int opr_s64D = 39;
  public static final int opr_u16 = 40;
  public static final int opr_aruI = 41;
  public static final int opr_aruIb = 42;
  public static final int opr_aruIc = 43;
  public static final int opr_aruIs = 44;
  public static final int opr_aruD = 45;
  public static final int opr_aruL = 46;
  public static final int opr_aruO = 47;
  public static final int opr_arcI = 48;
  public static final int opr_arcIb = 49;
  public static final int opr_arcIc = 50;
  public static final int opr_arcIs = 51;
  public static final int opr_arcD = 52;
  public static final int opr_arcL = 53;
  public static final int opr_arcO = 54;
  public static final int opr_arlen = 55;
  public static final int opr_null = 56;
  public static final int opr_void = 57;
  public static final int opr_cmp = 58; // special operand kind. Used in java instructions: LCMP, FCMPL, FCMPG, DCMPL, DCMPG

  // prefixs of opcodes
  public static final int pref_BREAK = 0;
  public static final int pref_MOV = 1;
  public static final int pref_ADD = 2;
  public static final int pref_SUB = 3;
  public static final int pref_MUL = 4;
  public static final int pref_DIV = 5;
  public static final int pref_MOD = 6;
  public static final int pref_SHR = 7;
  public static final int pref_SHL = 8;
  public static final int pref_USHR = 9;
  public static final int pref_AND = 10;
  public static final int pref_OR = 11;
  public static final int pref_XOR = 12;
  public static final int pref_JEQ = 13;
  public static final int pref_JNE = 14;
  public static final int pref_JLT = 15;
  public static final int pref_JLE = 16;
  public static final int pref_JGT = 17;
  public static final int pref_JGE = 18;
  public static final int pref_CONV = 19;

  public static final int pref_RETURNV = 20;
  public static final int pref_RETURNI = 21;
  public static final int pref_RETURNO = 22;
  public static final int pref_RETURND = 23;
  public static final int pref_RETURNL = 24;

  public static final int pref_JUMP = 25;
  public static final int pref_SWITCH = 26;
  public static final int pref_SYNCSTART = 27;
  public static final int pref_SYNCEND = 28;
  public static final int pref_NEWARRAY = 29;
  public static final int pref_NEWMULTIARRAY = 30;
  public static final int pref_ARRAYLEN = 31;
  public static final int pref_NEWOBJ = 32;
  public static final int pref_THROW = 33;
  public static final int pref_INSTANCEOF = 34;
  public static final int pref_CHECKCAST = 35;
  public static final int pref_CALL = 36;
  public static final int pref_TEST = 37;
  public static final int pref_INC = 38;

  public static final int type_Int = 0;
  public static final int type_Obj = 1;
  public static final int type_Double = 2;
  public static final int type_Long = 3;
  public static final int type_Void = 4;
  public static final int type_Constant = 5; // used in register allocation

  public static final int PHYSICALREGCOUNT = 64;
}
