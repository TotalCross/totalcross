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

public interface JConstants {
  public static final String[] bcClassNames = { "BC000_nop", "BC001_aconst_null", "BC002_iconst_m1", "BC003_iconst_0",
      "BC004_iconst_1", "BC005_iconst_2", "BC006_iconst_3", "BC007_iconst_4", "BC008_iconst_5", "BC009_lconst_0",
      "BC010_lconst_1", "BC011_fconst_0", "BC012_fconst_1", "BC013_fconst_2", "BC014_dconst_0", "BC015_dconst_1",
      "BC016_bipush", "BC017_sipush", "BC018_ldc", "BC019_ldc_w", "BC020_ldc2_w", "BC021_iload", "BC022_lload",
      "BC023_fload", "BC024_dload", "BC025_aload", "BC026_iload_0", "BC027_iload_1", "BC028_iload_2", "BC029_iload_3",
      "BC030_lload_0", "BC031_lload_1", "BC032_lload_2", "BC033_lload_3", "BC034_fload_0", "BC035_fload_1",
      "BC036_fload_2", "BC037_fload_3", "BC038_dload_0", "BC039_dload_1", "BC040_dload_2", "BC041_dload_3",
      "BC042_aload_0", "BC043_aload_1", "BC044_aload_2", "BC045_aload_3", "BC046_iaload", "BC047_laload",
      "BC048_faload", "BC049_daload", "BC050_aaload", "BC051_baload", "BC052_caload", "BC053_saload", "BC054_istore",
      "BC055_lstore", "BC056_fstore", "BC057_dstore", "BC058_astore", "BC059_istore_0", "BC060_istore_1",
      "BC061_istore_2", "BC062_istore_3", "BC063_lstore_0", "BC064_lstore_1", "BC065_lstore_2", "BC066_lstore_3",
      "BC067_fstore_0", "BC068_fstore_1", "BC069_fstore_2", "BC070_fstore_3", "BC071_dstore_0", "BC072_dstore_1",
      "BC073_dstore_2", "BC074_dstore_3", "BC075_astore_0", "BC076_astore_1", "BC077_astore_2", "BC078_astore_3",
      "BC079_iastore", "BC080_fastore", "BC081_lastore", "BC082_dastore", "BC083_aastore", "BC084_bastore",
      "BC085_castore", "BC086_sastore", "BC087_pop", "BC088_pop2", "BC089_dup", "BC090_dup_x1", "BC091_dup_x2",
      "BC092_dup2", "BC093_dup2_x1", "BC094_dup2_x2", "BC095_swap", "BC096_iadd", "BC097_ladd", "BC098_fadd",
      "BC099_dadd", "BC100_isub", "BC101_lsub", "BC102_fsub", "BC103_dsub", "BC104_imul", "BC105_lmul", "BC106_fmul",
      "BC107_dmul", "BC108_idiv", "BC109_ldiv", "BC110_fdiv", "BC111_ddiv", "BC112_irem", "BC113_lrem", "BC114_frem",
      "BC115_drem", "BC116_ineg", "BC117_lneg", "BC118_fneg", "BC119_dneg", "BC120_ishl", "BC121_lshl", "BC122_ishr",
      "BC123_lshr", "BC124_iushr", "BC125_lushr", "BC126_iand", "BC127_land", "BC128_ior", "BC129_lor", "BC130_ixor",
      "BC131_lxor", "BC132_iinc", "BC133_i2l", "BC134_i2f", "BC135_i2d", "BC136_l2i", "BC137_l2f", "BC138_l2d",
      "BC139_f2i", "BC140_f2l", "BC141_f2d", "BC142_d2i", "BC143_d2l", "BC144_d2f", "BC145_i2b", "BC146_i2c",
      "BC147_i2s", "BC148_lcmp", "BC149_fcmpl", "BC150_fcmpg", "BC151_dcmpl", "BC152_dcmpg", "BC153_ifeq", "BC154_ifne",
      "BC155_iflt", "BC156_ifge", "BC157_ifgt", "BC158_ifle", "BC159_if_icmpeq", "BC160_if_icmpne", "BC161_if_icmplt",
      "BC162_if_icmpge", "BC163_if_icmpgt", "BC164_if_icmple", "BC165_if_acmpeq", "BC166_if_acmpne", "BC167_goto",
      "BC168_jsr", "BC169_ret", "BC170_tableswitch", "BC171_lookupswitch", "BC172_ireturn", "BC173_lreturn",
      "BC174_freturn", "BC175_dreturn", "BC176_areturn", "BC177_return", "BC178_getstatic", "BC179_putstatic",
      "BC180_getfield", "BC181_putfield", "BC182_invokevirtual", "BC183_invokespecial", "BC184_invokestatic",
      "BC185_invokeinterface", "BC000_nop", // NOT DEFINED BY THE JAVA SPECIFICATION
      "BC187_new", "BC188_newarray", "BC189_anewarray", "BC190_arraylength", "BC191_athrow", "BC192_checkcast",
      "BC193_instanceof", "BC194_monitorenter", "BC195_monitorexit", "BC196_wide", "BC197_multinewarray",
      "BC198_if_null", "BC199_if_nonnull", "BC200_goto_w", "BC201_jsr_w", "BC202_breakpoint", };

  public static final int NOP = 0;
  public static final int ACONST_NULL = 1;
  public static final int ICONST_M1 = 2;
  public static final int ICONST_0 = 3;
  public static final int ICONST_1 = 4;
  public static final int ICONST_2 = 5;
  public static final int ICONST_3 = 6;
  public static final int ICONST_4 = 7;
  public static final int ICONST_5 = 8;
  public static final int LCONST_0 = 9;
  public static final int LCONST_1 = 10;
  public static final int FCONST_0 = 11;
  public static final int FCONST_1 = 12;
  public static final int FCONST_2 = 13;
  public static final int DCONST_0 = 14;
  public static final int DCONST_1 = 15;
  public static final int BIPUSH = 16;
  public static final int SIPUSH = 17;
  public static final int LDC = 18;
  public static final int LDC_W = 19;
  public static final int LDC2_W = 20;
  public static final int ILOAD = 21;
  public static final int LLOAD = 22;
  public static final int FLOAD = 23;
  public static final int DLOAD = 24;
  public static final int ALOAD = 25;
  public static final int ILOAD_0 = 26;
  public static final int ILOAD_1 = 27;
  public static final int ILOAD_2 = 28;
  public static final int ILOAD_3 = 29;
  public static final int LLOAD_0 = 30;
  public static final int LLOAD_1 = 31;
  public static final int LLOAD_2 = 32;
  public static final int LLOAD_3 = 33;
  public static final int FLOAD_0 = 34;
  public static final int FLOAD_1 = 35;
  public static final int FLOAD_2 = 36;
  public static final int FLOAD_3 = 37;
  public static final int DLOAD_0 = 38;
  public static final int DLOAD_1 = 39;
  public static final int DLOAD_2 = 40;
  public static final int DLOAD_3 = 41;
  public static final int ALOAD_0 = 42;
  public static final int ALOAD_1 = 43;
  public static final int ALOAD_2 = 44;
  public static final int ALOAD_3 = 45;
  public static final int IALOAD = 46;
  public static final int LALOAD = 47;
  public static final int FALOAD = 48;
  public static final int DALOAD = 49;
  public static final int AALOAD = 50;
  public static final int BALOAD = 51;
  public static final int CALOAD = 52;
  public static final int SALOAD = 53;
  public static final int ISTORE = 54;
  public static final int LSTORE = 55;
  public static final int FSTORE = 56;
  public static final int DSTORE = 57;
  public static final int ASTORE = 58;
  public static final int ISTORE_0 = 59;
  public static final int ISTORE_1 = 60;
  public static final int ISTORE_2 = 61;
  public static final int ISTORE_3 = 62;
  public static final int LSTORE_0 = 63;
  public static final int LSTORE_1 = 64;
  public static final int LSTORE_2 = 65;
  public static final int LSTORE_3 = 66;
  public static final int FSTORE_0 = 67;
  public static final int FSTORE_1 = 68;
  public static final int FSTORE_2 = 69;
  public static final int FSTORE_3 = 70;
  public static final int DSTORE_0 = 71;
  public static final int DSTORE_1 = 72;
  public static final int DSTORE_2 = 73;
  public static final int DSTORE_3 = 74;
  public static final int ASTORE_0 = 75;
  public static final int ASTORE_1 = 76;
  public static final int ASTORE_2 = 77;
  public static final int ASTORE_3 = 78;
  public static final int IASTORE = 79;
  public static final int LASTORE = 80;
  public static final int FASTORE = 81;
  public static final int DASTORE = 82;
  public static final int AASTORE = 83;
  public static final int BASTORE = 84;
  public static final int CASTORE = 85;
  public static final int SASTORE = 86;
  public static final int POP = 87;
  public static final int POP2 = 88;
  public static final int DUP = 89;
  public static final int DUP_X1 = 90;
  public static final int DUP_X2 = 91;
  public static final int DUP2 = 92;
  public static final int DUP2_X1 = 93;
  public static final int DUP2_X2 = 94;
  public static final int SWAP = 95;
  public static final int IADD = 96;
  public static final int LADD = 97;
  public static final int FADD = 98;
  public static final int DADD = 99;
  public static final int ISUB = 100;
  public static final int LSUB = 101;
  public static final int FSUB = 102;
  public static final int DSUB = 103;
  public static final int IMUL = 104;
  public static final int LMUL = 105;
  public static final int FMUL = 106;
  public static final int DMUL = 107;
  public static final int IDIV = 108;
  public static final int LDIV = 109;
  public static final int FDIV = 110;
  public static final int DDIV = 111;
  public static final int IREM = 112;
  public static final int LREM = 113;
  public static final int FREM = 114;
  public static final int DREM = 115;
  public static final int INEG = 116;
  public static final int LNEG = 117;
  public static final int FNEG = 118;
  public static final int DNEG = 119;
  public static final int ISHL = 120;
  public static final int LSHL = 121;
  public static final int ISHR = 122;
  public static final int LSHR = 123;
  public static final int IUSHR = 124;
  public static final int LUSHR = 125;
  public static final int IAND = 126;
  public static final int LAND = 127;
  public static final int IOR = 128;
  public static final int LOR = 129;
  public static final int IXOR = 130;
  public static final int LXOR = 131;
  public static final int IINC = 132;
  public static final int I2L = 133;
  public static final int I2F = 134;
  public static final int I2D = 135;
  public static final int L2I = 136;
  public static final int L2F = 137;
  public static final int L2D = 138;
  public static final int F2I = 139;
  public static final int F2L = 140;
  public static final int F2D = 141;
  public static final int D2I = 142;
  public static final int D2L = 143;
  public static final int D2F = 144;
  public static final int I2B = 145;
  public static final int I2C = 146;
  public static final int I2S = 147;
  public static final int LCMP = 148;
  public static final int FCMPL = 149;
  public static final int FCMPG = 150;
  public static final int DCMPL = 151;
  public static final int DCMPG = 152;
  public static final int IFEQ = 153;
  public static final int IFNE = 154;
  public static final int IFLT = 155;
  public static final int IFGE = 156;
  public static final int IFGT = 157;
  public static final int IFLE = 158;
  public static final int IF_ICMPEQ = 159;
  public static final int IF_ICMPNE = 160;
  public static final int IF_ICMPLT = 161;
  public static final int IF_ICMPGE = 162;
  public static final int IF_ICMPGT = 163;
  public static final int IF_ICMPLE = 164;
  public static final int IF_ACMPEQ = 165;
  public static final int IF_ACMPNE = 166;
  public static final int GOTO = 167;
  public static final int JSR = 168;
  public static final int RET = 169;
  public static final int TABLESWITCH = 170;
  public static final int LOOKUPSWITCH = 171;
  public static final int IRETURN = 172;
  public static final int LRETURN = 173;
  public static final int FRETURN = 174;
  public static final int DRETURN = 175;
  public static final int ARETURN = 176;
  public static final int RETURN = 177;
  public static final int GETSTATIC = 178;
  public static final int PUTSTATIC = 179;
  public static final int GETFIELD = 180;
  public static final int PUTFIELD = 181;
  public static final int INVOKEVIRTUAL = 182;
  public static final int INVOKESPECIAL = 183;
  public static final int INVOKESTATIC = 184;
  public static final int INVOKEINTERFACE = 185;
  public static final int NEW = 187;
  public static final int NEWARRAY = 188;
  public static final int ANEWARRAY = 189;
  public static final int ARRAYLENGTH = 190;
  public static final int ATHROW = 191;
  public static final int JCHECKCAST = 192;
  public static final int JINSTANCEOF = 193;
  public static final int MONITORENTER = 194;
  public static final int MONITOREXIT = 195;
  public static final int WIDE = 196;
  public static final int MULTIANEWARRAY = 197;
  public static final int IFNULL = 198;
  public static final int IFNONNULL = 199;
  public static final int GOTO_W = 200;
  public static final int JSR_W = 201;
  public static final int BREAKPOINT = 202;

  /** The types that will be collected */
  public static final int NULL = 0;
  public static final int VOID = 1;
  public static final int BOOLEAN = 2;
  public static final int BYTE = 3;
  public static final int CHAR = 4;
  public static final int SHORT = 5;
  public static final int INT = 6;
  public static final int LONG = 7;
  public static final int FLOAT = 8;
  public static final int DOUBLE = 9;
  public static final int STRING = 10;
  public static final int OBJECT = 11;

  public static final String[] typeAsString = { "NULL", "VOID", "BOOLEAN", "BYTE", "CHAR", "SHORT", "INT", "LONG",
      "FLOAT", "DOUBLE", "STRING", "OBJECT" };
}
