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
package tc.tools.converter.bytecode;

import tc.tools.converter.JConstants;
import tc.tools.converter.TCValue;
import tc.tools.converter.java.JavaCode;
import tc.tools.converter.java.JavaConstantPool;

public class ByteCode implements JConstants {
  /** The bytecode index. */
  public int bc;
  /** The number of increments to the stack this instruction does. */
  public int stackInc = 1;
  /** The number of increments to the pc this instruction does. */
  public int pcInc = 1;
  /** The pc where this instruction starts. */
  public int pcInMethod;
  /** The position of this instruction in the method. */
  public int posInMethod;
  /** The type of the bytecode or, if its a conversion, the resulting type. */
  public int targetType;
  /** The JavaCode of this bytecode. */
  public JavaCode jc;

  /** Current code being interpreted. Must be assigned by the interpreter. */
  public static byte[] code;
  /** Current program counter being interpreted. Must be incremented by the interpreter. */
  public static int pc;
  /** The constant pool for this method */
  public static JavaConstantPool cp;

  // used during execution

  /** The current stack */
  public static TCValue[] stack;
  /** The current stack pointer */
  public static int stackPtr;
  /** The local variables */
  public static TCValue[] local;

  private static Class<?>[] bcClasses;

  public ByteCode getNext() {
    return jc.bcs[posInMethod + 1];
  }

  public ByteCode getPrev() {
    return jc.bcs[posInMethod - 1];
  }

  public ByteCode() {
    pcInMethod = pc;
  }

  public static void initClasses() throws ClassNotFoundException {
    if (bcClasses == null) // init only once
    {
      bcClasses = new Class[bcClassNames.length];
      // by directly assigning the real class, we can safely obfuscate these classes
      bcClasses[0] = tc.tools.converter.bytecode.BC000_nop.class;
      bcClasses[1] = tc.tools.converter.bytecode.BC001_aconst_null.class;
      bcClasses[2] = tc.tools.converter.bytecode.BC002_iconst_m1.class;
      bcClasses[3] = tc.tools.converter.bytecode.BC003_iconst_0.class;
      bcClasses[4] = tc.tools.converter.bytecode.BC004_iconst_1.class;
      bcClasses[5] = tc.tools.converter.bytecode.BC005_iconst_2.class;
      bcClasses[6] = tc.tools.converter.bytecode.BC006_iconst_3.class;
      bcClasses[7] = tc.tools.converter.bytecode.BC007_iconst_4.class;
      bcClasses[8] = tc.tools.converter.bytecode.BC008_iconst_5.class;
      bcClasses[9] = tc.tools.converter.bytecode.BC009_lconst_0.class;
      bcClasses[10] = tc.tools.converter.bytecode.BC010_lconst_1.class;
      bcClasses[11] = tc.tools.converter.bytecode.BC011_fconst_0.class;
      bcClasses[12] = tc.tools.converter.bytecode.BC012_fconst_1.class;
      bcClasses[13] = tc.tools.converter.bytecode.BC013_fconst_2.class;
      bcClasses[14] = tc.tools.converter.bytecode.BC014_dconst_0.class;
      bcClasses[15] = tc.tools.converter.bytecode.BC015_dconst_1.class;
      bcClasses[16] = tc.tools.converter.bytecode.BC016_bipush.class;
      bcClasses[17] = tc.tools.converter.bytecode.BC017_sipush.class;
      bcClasses[18] = tc.tools.converter.bytecode.BC018_ldc.class;
      bcClasses[19] = tc.tools.converter.bytecode.BC019_ldc_w.class;
      bcClasses[20] = tc.tools.converter.bytecode.BC020_ldc2_w.class;
      bcClasses[21] = tc.tools.converter.bytecode.BC021_iload.class;
      bcClasses[22] = tc.tools.converter.bytecode.BC022_lload.class;
      bcClasses[23] = tc.tools.converter.bytecode.BC023_fload.class;
      bcClasses[24] = tc.tools.converter.bytecode.BC024_dload.class;
      bcClasses[25] = tc.tools.converter.bytecode.BC025_aload.class;
      bcClasses[26] = tc.tools.converter.bytecode.BC026_iload_0.class;
      bcClasses[27] = tc.tools.converter.bytecode.BC027_iload_1.class;
      bcClasses[28] = tc.tools.converter.bytecode.BC028_iload_2.class;
      bcClasses[29] = tc.tools.converter.bytecode.BC029_iload_3.class;
      bcClasses[30] = tc.tools.converter.bytecode.BC030_lload_0.class;
      bcClasses[31] = tc.tools.converter.bytecode.BC031_lload_1.class;
      bcClasses[32] = tc.tools.converter.bytecode.BC032_lload_2.class;
      bcClasses[33] = tc.tools.converter.bytecode.BC033_lload_3.class;
      bcClasses[34] = tc.tools.converter.bytecode.BC034_fload_0.class;
      bcClasses[35] = tc.tools.converter.bytecode.BC035_fload_1.class;
      bcClasses[36] = tc.tools.converter.bytecode.BC036_fload_2.class;
      bcClasses[37] = tc.tools.converter.bytecode.BC037_fload_3.class;
      bcClasses[38] = tc.tools.converter.bytecode.BC038_dload_0.class;
      bcClasses[39] = tc.tools.converter.bytecode.BC039_dload_1.class;
      bcClasses[40] = tc.tools.converter.bytecode.BC040_dload_2.class;
      bcClasses[41] = tc.tools.converter.bytecode.BC041_dload_3.class;
      bcClasses[42] = tc.tools.converter.bytecode.BC042_aload_0.class;
      bcClasses[43] = tc.tools.converter.bytecode.BC043_aload_1.class;
      bcClasses[44] = tc.tools.converter.bytecode.BC044_aload_2.class;
      bcClasses[45] = tc.tools.converter.bytecode.BC045_aload_3.class;
      bcClasses[46] = tc.tools.converter.bytecode.BC046_iaload.class;
      bcClasses[47] = tc.tools.converter.bytecode.BC047_laload.class;
      bcClasses[48] = tc.tools.converter.bytecode.BC048_faload.class;
      bcClasses[49] = tc.tools.converter.bytecode.BC049_daload.class;
      bcClasses[50] = tc.tools.converter.bytecode.BC050_aaload.class;
      bcClasses[51] = tc.tools.converter.bytecode.BC051_baload.class;
      bcClasses[52] = tc.tools.converter.bytecode.BC052_caload.class;
      bcClasses[53] = tc.tools.converter.bytecode.BC053_saload.class;
      bcClasses[54] = tc.tools.converter.bytecode.BC054_istore.class;
      bcClasses[55] = tc.tools.converter.bytecode.BC055_lstore.class;
      bcClasses[56] = tc.tools.converter.bytecode.BC056_fstore.class;
      bcClasses[57] = tc.tools.converter.bytecode.BC057_dstore.class;
      bcClasses[58] = tc.tools.converter.bytecode.BC058_astore.class;
      bcClasses[59] = tc.tools.converter.bytecode.BC059_istore_0.class;
      bcClasses[60] = tc.tools.converter.bytecode.BC060_istore_1.class;
      bcClasses[61] = tc.tools.converter.bytecode.BC061_istore_2.class;
      bcClasses[62] = tc.tools.converter.bytecode.BC062_istore_3.class;
      bcClasses[63] = tc.tools.converter.bytecode.BC063_lstore_0.class;
      bcClasses[64] = tc.tools.converter.bytecode.BC064_lstore_1.class;
      bcClasses[65] = tc.tools.converter.bytecode.BC065_lstore_2.class;
      bcClasses[66] = tc.tools.converter.bytecode.BC066_lstore_3.class;
      bcClasses[67] = tc.tools.converter.bytecode.BC067_fstore_0.class;
      bcClasses[68] = tc.tools.converter.bytecode.BC068_fstore_1.class;
      bcClasses[69] = tc.tools.converter.bytecode.BC069_fstore_2.class;
      bcClasses[70] = tc.tools.converter.bytecode.BC070_fstore_3.class;
      bcClasses[71] = tc.tools.converter.bytecode.BC071_dstore_0.class;
      bcClasses[72] = tc.tools.converter.bytecode.BC072_dstore_1.class;
      bcClasses[73] = tc.tools.converter.bytecode.BC073_dstore_2.class;
      bcClasses[74] = tc.tools.converter.bytecode.BC074_dstore_3.class;
      bcClasses[75] = tc.tools.converter.bytecode.BC075_astore_0.class;
      bcClasses[76] = tc.tools.converter.bytecode.BC076_astore_1.class;
      bcClasses[77] = tc.tools.converter.bytecode.BC077_astore_2.class;
      bcClasses[78] = tc.tools.converter.bytecode.BC078_astore_3.class;
      bcClasses[79] = tc.tools.converter.bytecode.BC079_iastore.class;
      bcClasses[80] = tc.tools.converter.bytecode.BC080_fastore.class;
      bcClasses[81] = tc.tools.converter.bytecode.BC081_lastore.class;
      bcClasses[82] = tc.tools.converter.bytecode.BC082_dastore.class;
      bcClasses[83] = tc.tools.converter.bytecode.BC083_aastore.class;
      bcClasses[84] = tc.tools.converter.bytecode.BC084_bastore.class;
      bcClasses[85] = tc.tools.converter.bytecode.BC085_castore.class;
      bcClasses[86] = tc.tools.converter.bytecode.BC086_sastore.class;
      bcClasses[87] = tc.tools.converter.bytecode.BC087_pop.class;
      bcClasses[88] = tc.tools.converter.bytecode.BC088_pop2.class;
      bcClasses[89] = tc.tools.converter.bytecode.BC089_dup.class;
      bcClasses[90] = tc.tools.converter.bytecode.BC090_dup_x1.class;
      bcClasses[91] = tc.tools.converter.bytecode.BC091_dup_x2.class;
      bcClasses[92] = tc.tools.converter.bytecode.BC092_dup2.class;
      bcClasses[93] = tc.tools.converter.bytecode.BC093_dup2_x1.class;
      bcClasses[94] = tc.tools.converter.bytecode.BC094_dup2_x2.class;
      bcClasses[95] = tc.tools.converter.bytecode.BC095_swap.class;
      bcClasses[96] = tc.tools.converter.bytecode.BC096_iadd.class;
      bcClasses[97] = tc.tools.converter.bytecode.BC097_ladd.class;
      bcClasses[98] = tc.tools.converter.bytecode.BC098_fadd.class;
      bcClasses[99] = tc.tools.converter.bytecode.BC099_dadd.class;
      bcClasses[100] = tc.tools.converter.bytecode.BC100_isub.class;
      bcClasses[101] = tc.tools.converter.bytecode.BC101_lsub.class;
      bcClasses[102] = tc.tools.converter.bytecode.BC102_fsub.class;
      bcClasses[103] = tc.tools.converter.bytecode.BC103_dsub.class;
      bcClasses[104] = tc.tools.converter.bytecode.BC104_imul.class;
      bcClasses[105] = tc.tools.converter.bytecode.BC105_lmul.class;
      bcClasses[106] = tc.tools.converter.bytecode.BC106_fmul.class;
      bcClasses[107] = tc.tools.converter.bytecode.BC107_dmul.class;
      bcClasses[108] = tc.tools.converter.bytecode.BC108_idiv.class;
      bcClasses[109] = tc.tools.converter.bytecode.BC109_ldiv.class;
      bcClasses[110] = tc.tools.converter.bytecode.BC110_fdiv.class;
      bcClasses[111] = tc.tools.converter.bytecode.BC111_ddiv.class;
      bcClasses[112] = tc.tools.converter.bytecode.BC112_irem.class;
      bcClasses[113] = tc.tools.converter.bytecode.BC113_lrem.class;
      bcClasses[114] = tc.tools.converter.bytecode.BC114_frem.class;
      bcClasses[115] = tc.tools.converter.bytecode.BC115_drem.class;
      bcClasses[116] = tc.tools.converter.bytecode.BC116_ineg.class;
      bcClasses[117] = tc.tools.converter.bytecode.BC117_lneg.class;
      bcClasses[118] = tc.tools.converter.bytecode.BC118_fneg.class;
      bcClasses[119] = tc.tools.converter.bytecode.BC119_dneg.class;
      bcClasses[120] = tc.tools.converter.bytecode.BC120_ishl.class;
      bcClasses[121] = tc.tools.converter.bytecode.BC121_lshl.class;
      bcClasses[122] = tc.tools.converter.bytecode.BC122_ishr.class;
      bcClasses[123] = tc.tools.converter.bytecode.BC123_lshr.class;
      bcClasses[124] = tc.tools.converter.bytecode.BC124_iushr.class;
      bcClasses[125] = tc.tools.converter.bytecode.BC125_lushr.class;
      bcClasses[126] = tc.tools.converter.bytecode.BC126_iand.class;
      bcClasses[127] = tc.tools.converter.bytecode.BC127_land.class;
      bcClasses[128] = tc.tools.converter.bytecode.BC128_ior.class;
      bcClasses[129] = tc.tools.converter.bytecode.BC129_lor.class;
      bcClasses[130] = tc.tools.converter.bytecode.BC130_ixor.class;
      bcClasses[131] = tc.tools.converter.bytecode.BC131_lxor.class;
      bcClasses[132] = tc.tools.converter.bytecode.BC132_iinc.class;
      bcClasses[133] = tc.tools.converter.bytecode.BC133_i2l.class;
      bcClasses[134] = tc.tools.converter.bytecode.BC134_i2f.class;
      bcClasses[135] = tc.tools.converter.bytecode.BC135_i2d.class;
      bcClasses[136] = tc.tools.converter.bytecode.BC136_l2i.class;
      bcClasses[137] = tc.tools.converter.bytecode.BC137_l2f.class;
      bcClasses[138] = tc.tools.converter.bytecode.BC138_l2d.class;
      bcClasses[139] = tc.tools.converter.bytecode.BC139_f2i.class;
      bcClasses[140] = tc.tools.converter.bytecode.BC140_f2l.class;
      bcClasses[141] = tc.tools.converter.bytecode.BC141_f2d.class;
      bcClasses[142] = tc.tools.converter.bytecode.BC142_d2i.class;
      bcClasses[143] = tc.tools.converter.bytecode.BC143_d2l.class;
      bcClasses[144] = tc.tools.converter.bytecode.BC144_d2f.class;
      bcClasses[145] = tc.tools.converter.bytecode.BC145_i2b.class;
      bcClasses[146] = tc.tools.converter.bytecode.BC146_i2c.class;
      bcClasses[147] = tc.tools.converter.bytecode.BC147_i2s.class;
      bcClasses[148] = tc.tools.converter.bytecode.BC148_lcmp.class;
      bcClasses[149] = tc.tools.converter.bytecode.BC149_fcmpl.class;
      bcClasses[150] = tc.tools.converter.bytecode.BC150_fcmpg.class;
      bcClasses[151] = tc.tools.converter.bytecode.BC151_dcmpl.class;
      bcClasses[152] = tc.tools.converter.bytecode.BC152_dcmpg.class;
      bcClasses[153] = tc.tools.converter.bytecode.BC153_ifeq.class;
      bcClasses[154] = tc.tools.converter.bytecode.BC154_ifne.class;
      bcClasses[155] = tc.tools.converter.bytecode.BC155_iflt.class;
      bcClasses[156] = tc.tools.converter.bytecode.BC156_ifge.class;
      bcClasses[157] = tc.tools.converter.bytecode.BC157_ifgt.class;
      bcClasses[158] = tc.tools.converter.bytecode.BC158_ifle.class;
      bcClasses[159] = tc.tools.converter.bytecode.BC159_if_icmpeq.class;
      bcClasses[160] = tc.tools.converter.bytecode.BC160_if_icmpne.class;
      bcClasses[161] = tc.tools.converter.bytecode.BC161_if_icmplt.class;
      bcClasses[162] = tc.tools.converter.bytecode.BC162_if_icmpge.class;
      bcClasses[163] = tc.tools.converter.bytecode.BC163_if_icmpgt.class;
      bcClasses[164] = tc.tools.converter.bytecode.BC164_if_icmple.class;
      bcClasses[165] = tc.tools.converter.bytecode.BC165_if_acmpeq.class;
      bcClasses[166] = tc.tools.converter.bytecode.BC166_if_acmpne.class;
      bcClasses[167] = tc.tools.converter.bytecode.BC167_goto.class;
      bcClasses[168] = tc.tools.converter.bytecode.BC168_jsr.class;
      bcClasses[169] = tc.tools.converter.bytecode.BC169_ret.class;
      bcClasses[170] = tc.tools.converter.bytecode.BC170_tableswitch.class;
      bcClasses[171] = tc.tools.converter.bytecode.BC171_lookupswitch.class;
      bcClasses[172] = tc.tools.converter.bytecode.BC172_ireturn.class;
      bcClasses[173] = tc.tools.converter.bytecode.BC173_lreturn.class;
      bcClasses[174] = tc.tools.converter.bytecode.BC174_freturn.class;
      bcClasses[175] = tc.tools.converter.bytecode.BC175_dreturn.class;
      bcClasses[176] = tc.tools.converter.bytecode.BC176_areturn.class;
      bcClasses[177] = tc.tools.converter.bytecode.BC177_return.class;
      bcClasses[178] = tc.tools.converter.bytecode.BC178_getstatic.class;
      bcClasses[179] = tc.tools.converter.bytecode.BC179_putstatic.class;
      bcClasses[180] = tc.tools.converter.bytecode.BC180_getfield.class;
      bcClasses[181] = tc.tools.converter.bytecode.BC181_putfield.class;
      bcClasses[182] = tc.tools.converter.bytecode.BC182_invokevirtual.class;
      bcClasses[183] = tc.tools.converter.bytecode.BC183_invokespecial.class;
      bcClasses[184] = tc.tools.converter.bytecode.BC184_invokestatic.class;
      bcClasses[185] = tc.tools.converter.bytecode.BC185_invokeinterface.class;
      bcClasses[187] = tc.tools.converter.bytecode.BC187_new.class;
      bcClasses[188] = tc.tools.converter.bytecode.BC188_newarray.class;
      bcClasses[189] = tc.tools.converter.bytecode.BC189_anewarray.class;
      bcClasses[190] = tc.tools.converter.bytecode.BC190_arraylength.class;
      bcClasses[191] = tc.tools.converter.bytecode.BC191_athrow.class;
      bcClasses[192] = tc.tools.converter.bytecode.BC192_checkcast.class;
      bcClasses[193] = tc.tools.converter.bytecode.BC193_instanceof.class;
      bcClasses[194] = tc.tools.converter.bytecode.BC194_monitorenter.class;
      bcClasses[195] = tc.tools.converter.bytecode.BC195_monitorexit.class;
      bcClasses[196] = tc.tools.converter.bytecode.BC196_wide.class;
      bcClasses[197] = tc.tools.converter.bytecode.BC197_multinewarray.class;
      bcClasses[198] = tc.tools.converter.bytecode.BC198_if_null.class;
      bcClasses[199] = tc.tools.converter.bytecode.BC199_if_nonnull.class;
      bcClasses[200] = tc.tools.converter.bytecode.BC200_goto_w.class;
      bcClasses[201] = tc.tools.converter.bytecode.BC201_jsr_w.class;
      bcClasses[202] = tc.tools.converter.bytecode.BC202_breakpoint.class;
    }
  }

  private static TCValue[] allocTCValues(TCValue[] vs, int max) {
    if (vs == null || max > vs.length) {
      // have to alloc?
      vs = new TCValue[max];
      for (int i = 0; i < max; i++) {
        vs[i] = new TCValue();
      }
    } else {
      // just zero it
      for (int i = 0; i < max; i++) {
        vs[i].clear();
      }
    }
    return vs;
  }

  public static void initLocal(int max) {
    local = allocTCValues(local, max);
  }

  public static void initStack(int max) {
    stack = allocTCValues(stack, max);
    stackPtr = 0;
  }

  public static ByteCode getInstance(int idx) {
    try {
      return (ByteCode) bcClasses[idx].newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  protected static int readInt16(int p) {
    return (short) ((code[p] & 0xFF) << 8) | (code[p + 1] & 0xFF);
  }

  protected static int readInt32(int p) {
    return (code[p + 3] & 0xFF) | ((code[p + 2] & 0xFF) << 8) | ((code[p + 1] & 0xFF) << 16)
        | ((code[p + 0] & 0xFF) << 24);
  }

  protected static int readUInt16(int p) {
    return ((code[p] & 0xFF) << 8) | (code[p + 1] & 0xFF);
  }

  protected static int readUInt8(int p) {
    return code[p] & 0xFF;
  }

  public static int convertJavaType(String t) {
    switch (t.charAt(0)) {
    case 'I':
      return INT;
    case 'J':
      return LONG;
    case 'Z':
      return BOOLEAN;
    case 'B':
      return BYTE;
    case 'C':
      return CHAR;
    case 'S':
      return SHORT;
    case 'F':
      return FLOAT;
    case 'D':
      return DOUBLE;
    case 'V':
      return VOID;
    default:
      return OBJECT;
    }
  }

  protected void exec() {
  }

  public void execute() {
    exec();
    stackPtr += stackInc;
    pc += pcInc;
  }

  private static StringBuffer sbuf = new StringBuffer(30);

  @Override
  public String toString() {
    sbuf.setLength(0);
    return sbuf.append(posInMethod).append("  ").append(pcInMethod).append(": ").append(bcClassNames[bc].substring(6))
        .toString();
  }
}
