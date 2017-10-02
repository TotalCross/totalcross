/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.converter;

/*import tc.tools.converter.bytecode.*;
import tc.tools.converter.tclass.TCCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaCode;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.oper.*;
import totalcross.io.File;
import totalcross.util.*;*/
import tc.tools.converter.JConstants;
import tc.tools.converter.TCConstants;
import totalcross.unit.TestCase;

public class Bytecode2TCCodeTest extends TestCase implements JConstants, TCConstants {
  @Override
  public void testRun() {
  }

  /*private static final double DOUBLE_ERROR_PRECISION = 1e-3d;
   public static Vector vcodeTest = new Vector(500);
   private JavaMethod[] startTest(String fileName) throws Exception
   {
      Bytecode2TCCode.indexOfCurrentBytecode = 0;
      vcodeTest.removeAllElements();
      ByteCode.initClasses();
      GlobalConstantPool.init();
      File f = new File("P:/TotalCross3/classes/tc/test/converter/testfiles/"+fileName, File.READ_WRITE);
  
      JavaClass jc = new JavaClass(f, false);
      new J2TC(jc);
  
      OperandReg.init(jc.methods[0].params, jc.methods[0].isStatic);
      return jc.methods;
   }
  
   /*private void BC001() throws Exception
   {
      JavaCode jcode = startTest("BC001.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(5);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
  
         switch (op)
         {
            case ACONST_NULL:
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               assertEquals(opr_null, stack.pop().kind);
               break;
         }
      }
   }
  
   private void BC002to015() throws Exception
   {
      JavaCode jcode = startTest("BC002to015.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
  
         switch (op)
         {
            case ICONST_M1: //2
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandConstant oci = (OperandConstant)stack.pop();
               assertEquals(opr_s6, oci.kind);
               assertEquals(-1, oci.getValueAsInt());
               break;
            }
            case ICONST_0: //3
            case LCONST_0: //9
            case FCONST_0: //11
            case DCONST_0: //14
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandConstant oci = (OperandConstant)stack.pop();
               assertEquals(opr_s6, oci.kind);
               assertEquals(0, oci.getValueAsInt());
               break;
            }
            case ICONST_1: //4
            case LCONST_1: //10
            case FCONST_1: //12
            case DCONST_1: //15
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandConstant oci = (OperandConstant)stack.pop();
               assertEquals(opr_s6, oci.kind);
               assertEquals(1, oci.getValueAsInt());
               break;
            }
            case ICONST_2: //5
            case FCONST_2: //13
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandConstant oci = (OperandConstant)stack.pop();
               assertEquals(opr_s6, oci.kind);
               assertEquals(2, oci.getValueAsInt());
               break;
            }
            case ICONST_3: //6
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandConstant oci = (OperandConstant)stack.pop();
               assertEquals(opr_s6, oci.kind);
               assertEquals(3, oci.getValueAsInt());
               break;
            }
            case ICONST_4: //7
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandConstant oci = (OperandConstant)stack.pop();
               assertEquals(opr_s6, oci.kind);
               assertEquals(4, oci.getValueAsInt());
               break;
            }
            case ICONST_5: //8
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandConstant oci = (OperandConstant)stack.pop();
               assertEquals(opr_s6, oci.kind);
               assertEquals(5, oci.getValueAsInt());
               break;
            }
         }
      }
   }
  
   private void BC016to017() throws Exception
   {
      JavaCode jcode = startTest("BC016to017.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
  
         switch (op)
         {
            case BIPUSH: //16
            case SIPUSH: //17
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandConstant oci = (OperandConstant)stack.pop();
               int x = oci.getValueAsInt();
  
               if (x >= -32 && x <= 31)
                  assertEquals(opr_s6, oci.kind);
               else
               if (x >= -2048 && x <= 2047)
                  assertEquals(opr_s12, oci.kind);
               else
                  assertEquals(opr_s16, oci.kind);
               break;
            }
         }
      }
   }
  
   private void BC018to020() throws Exception
   {
      JavaCode jcode = startTest("BC018to020.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         Operand opr = null;
         TCValue v;
  
         switch (op)
         {
            case LDC: //18
            case LDC_W: //19
            case LDC2_W: //20
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               opr = stack.pop();
            }
         }
  
         switch (op)
         {
            case LDC: //18
            case LDC_W: //19
            {
               if (op == LDC)
               {
                  BC018_ldc ji = (BC018_ldc)i;
                  v = ji.val;
               }
               else
               {
                  BC019_ldc_w ji = (BC019_ldc_w)i;
                  v = ji.val;
               }
  
               if (v.type == INT)
               {
                  OperandConstant opr2 = (OperandConstant) opr;
                  assertEquals(opr.isConstantInt(), true);
                  assertEquals(opr2.getValueAsInt(), v.asInt);
               }
               else
               if (v.type == DOUBLE) // this constant is a float
               {
                  OperandSymD opr2 = (OperandSymD) opr;
                  assertEquals(opr.isSym(), true);
                  assertEquals(v.asDouble, GlobalConstantPool.getD(opr2.index), DOUBLE_ERROR_PRECISION);
               }
               else // constant string
               {
                  OperandSymO opr2 = (OperandSymO) opr;
                  assertEquals(opr.isSym(), true);
                  assertEquals(v.asObj, GlobalConstantPool.getStr(opr2.index));
               }
               break;
            }
  
            case LDC2_W: //20
            {
               BC020_ldc2_w ji = (BC020_ldc2_w)i;
               v = ji.val;
               if (v.type == LONG)
               {
                  OperandConstant opr2 = (OperandConstant) opr;
                  assertEquals(opr.isConstantLong(), true);
                  assertEquals(opr2.getValueAsLong(), v.asLong);
               }
               else
               if (v.type == DOUBLE)
               {
                  OperandSymD opr2 = (OperandSymD) opr;
                  assertEquals(opr.isSym(), true);
                  assertEquals(v.asDouble, GlobalConstantPool.getD(opr2.index),DOUBLE_ERROR_PRECISION);
               }
            }
         }
      }
   }
  
   private void BC021_026to029() throws Exception
   {
      JavaCode jcode = startTest("BC021_026to029.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case ILOAD: //21
            case ILOAD_0: //26
            case ILOAD_1: //27
            case ILOAD_2: //28
            case ILOAD_3: //29
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandReg reg = (OperandReg)stack.pop();
               assertEquals(reg.kind, opr_regI);
  
               LoadLocal ji = (LoadLocal)i;
               OperandReg reg2 = new OperandRegI(ji.localIdx);
               assertEquals(reg.index, reg2.index);
               break;
            }
         }
      }
   }
  
   private void BC022_030to033() throws Exception
   {
      JavaCode jcode = startTest("BC022_030to033.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case LLOAD: //22
            case LLOAD_0: //30
            case LLOAD_1: //31
            case LLOAD_2: //32
            case LLOAD_3: //33
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandReg reg = (OperandReg)stack.pop();
               assertEquals(reg.kind, opr_regL);
  
               LoadLocal ji = (LoadLocal)i;
               OperandReg reg2 = new OperandRegL(ji.localIdx);
               assertEquals(reg.index, reg2.index);
               break;
            }
         }
      }
   }
  
   private void BC023_034to037() throws Exception
   {
      JavaCode jcode = startTest("BC023_034to037.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case FLOAD: //23
            case FLOAD_0: //34
            case FLOAD_1: //35
            case FLOAD_2: //36
            case FLOAD_3: //37
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandReg reg = (OperandReg)stack.pop();
               assertEquals(reg.kind, opr_regD);
  
               LoadLocal ji = (LoadLocal)i;
               OperandReg reg2 = new OperandRegD32(ji.localIdx);
               assertEquals(reg.index, reg2.index);
               break;
            }
         }
      }
   }
  
   private void BC024_038to041() throws Exception
   {
      JavaCode jcode = startTest("BC024_038to041.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case DLOAD: //24
            case DLOAD_0: //38
            case DLOAD_1: //39
            case DLOAD_2: //40
            case DLOAD_3: //41
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandReg reg = (OperandReg)stack.pop();
               assertEquals(reg.kind, opr_regD);
  
               LoadLocal ji = (LoadLocal)i;
               OperandReg reg2 = new OperandRegD64(ji.localIdx);
               assertEquals(reg.index, reg2.index);
               break;
            }
         }
      }
   }
  
   private void BC025_042to045() throws Exception
   {
      JavaCode jcode = startTest("BC025_042to045.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case ALOAD: //25
            case ALOAD_0: //42
            case ALOAD_1: //43
            case ALOAD_2: //44
            case ALOAD_3: //45
            {
               Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               OperandReg reg = (OperandReg)stack.pop();
               assertEquals(reg.kind, opr_regO);
  
               LoadLocal ji = (LoadLocal)i;
               OperandReg reg2 = new OperandRegO(ji.localIdx);
               assertEquals(reg.index, reg2.index);
               break;
            }
         }
      }
   }
  
   private void BC087to095() throws Exception
   {
      JavaCode jcode = startTest("BC087to095.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      //OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case POP: //87
            case POP2: //88
            case DUP: //89
            case DUP_X1: //90
            case DUP_X2: //91
            case DUP2: //92
            case DUP2_X1: //93
            case DUP2_X2: //94
            case SWAP: //95
               break;
         }
      }
   }
  
   private void BC054_059to062() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC054_059to062.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case ISTORE:   //54
            case ISTORE_0: //59
            case ISTORE_1: //60
            case ISTORE_2: //61
            case ISTORE_3: //62
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               switch (++n)
               {
                  // see file BC054_059to062.java
                  case 1: // a = -1;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 0);
                     assertEquals(tccode.s18_reg__s18(), -1);
                     break;
                  case 2: // b = 0;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 1);
                     assertEquals(tccode.s18_reg__s18(), 0);
                     break;
                  case 3: // c = -32;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 2);
                     assertEquals(tccode.s18_reg__s18(), -32);
                     break;
                  case 4: // d = 31;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 3);
                     assertEquals(tccode.s18_reg__s18(), 31);
                     break;
                  case 5: // e = -2048;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), -2048);
                     break;
                  case 6: // e = 2047;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), 2047);
                     break;
                  case 7: // e = -32768;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), -32768);
                     break;
                  case 8: // e = 32767;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), 32767);
                     break;
                  case 9: // e = -131072;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), -131072);
                     break;
                  case 10: // e = 131071;
                     assertEquals(tccode.op(), MOV_regI_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), 131071);
                     break;
                  case 11: // e = -8388608;
                     assertEquals(tccode.op(), MOV_regI_sym);
                     assertEquals(tccode.reg_sym__reg(), 4);
                     assertEquals(-8388608, GlobalConstantPool.getI(tccode.reg_sym__sym()));
                     break;
                  case 12: // e = 8388607;
                     assertEquals(tccode.op(), MOV_regI_sym);
                     assertEquals(tccode.reg_sym__reg(), 4);
                     assertEquals(8388607, GlobalConstantPool.getI(tccode.reg_sym__sym()));
                     break;
                  case 13: // e = -2147483648;
                     assertEquals(tccode.op(), MOV_regI_sym);
                     assertEquals(tccode.reg_sym__reg(), 4);
                     assertEquals(-2147483648, GlobalConstantPool.getI(tccode.reg_sym__sym()));
                     break;
                  case 14: // e = 2147483647;
                     assertEquals(tccode.op(), MOV_regI_sym);
                     assertEquals(tccode.reg_sym__reg(), 4);
                     assertEquals(2147483647, GlobalConstantPool.getI(tccode.reg_sym__sym()));
                     break;
                  case 15: // a = b;
                     assertEquals(tccode.op(), MOV_regI_regI);
                     assertEquals(tccode.reg_reg__reg0(), 0);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 16: // d = a;
                     assertEquals(tccode.op(), MOV_regI_regI);
                     assertEquals(tccode.reg_reg__reg0(), 3);
                     assertEquals(tccode.reg_reg__reg1(), 0);
                     break;
                  case 17: // e = d;
                     assertEquals(tccode.op(), MOV_regI_regI);
                     assertEquals(tccode.reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 18: // e = a + b;
                     assertEquals(tccode.op(), MOV_regI_regI);
                     assertEquals(tccode.reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
               }
               break;
            }
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC055_063to066() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC055_063to066.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case LSTORE:   //55
            case LSTORE_0: //63
            case LSTORE_1: //64
            case LSTORE_2: //65
            case LSTORE_3: //66
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               switch (++n)
               {
                  // see file BC055_063to066.java
                  case 1: // a = -131072;
                     assertEquals(tccode.op(), MOV_regL_s18);
                     assertEquals(tccode.s18_reg__reg(), 0);
                     assertEquals(tccode.s18_reg__s18(), -131072);
                     break;
                  case 2: // b = 131071;
                     assertEquals(tccode.op(), MOV_regL_s18);
                     assertEquals(tccode.s18_reg__reg(), 1);
                     assertEquals(tccode.s18_reg__s18(), 131071);
                     break;
                  case 3: // c = -2147483648;
                     assertEquals(tccode.op(), MOV_reg64_sym);
                     assertEquals(tccode.reg_sym__reg(), 2);
                     assertEquals(GlobalConstantPool.getL(tccode.reg_sym__sym()), -2147483648);
                     break;
                  case 4: // d = 2147483647;
                     assertEquals(tccode.op(), MOV_reg64_sym);
                     assertEquals(tccode.reg_sym__reg(), 3);
                     assertEquals(GlobalConstantPool.getL(tccode.reg_sym__sym()), 2147483647);
                     break;
                  case 5: // e = -9223372036854775808L;
                     assertEquals(tccode.op(), MOV_reg64_sym);
                     assertEquals(tccode.reg_sym__reg(), 4);
                     assertEquals(GlobalConstantPool.getL(tccode.reg_sym__sym()), -9223372036854775808L);
                     break;
                  case 6: // e = 9223372036854775807L;
                     assertEquals(tccode.op(), MOV_reg64_sym);
                     assertEquals(tccode.reg_sym__reg(), 4);
                     assertEquals(GlobalConstantPool.getL(tccode.reg_sym__sym()), 9223372036854775807L);
                     break;
                  case 7: // a = b;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 0);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 8: // d = a;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 3);
                     assertEquals(tccode.reg_reg__reg1(), 0);
                     break;
                  case 9: // e = d;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
               }
               break;
            }
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC056_067to070() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC056_067to070.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case FSTORE:   //56
            case FSTORE_0: //67
            case FSTORE_1: //68
            case FSTORE_2: //69
            case FSTORE_3: //70
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               switch (++n)
               {
                  // see file BC056_067to070.java
                  case 1: // a = -131072;
                     assertEquals(tccode.op(), MOV_regD_s18);
                     assertEquals(tccode.s18_reg__reg(), 0);
                     assertEquals(tccode.s18_reg__s18(), -131072);
                     break;
                  case 2: // b = 131071;
                     assertEquals(tccode.op(), MOV_regD_s18);
                     assertEquals(tccode.s18_reg__reg(), 1);
                     assertEquals(tccode.s18_reg__s18(), 131071);
                     break;
                  case 3: // c = -0.5f;
                     assertEquals(tccode.op(), MOV_reg64_sym);
                     assertEquals(tccode.reg_sym__reg(), 2);
                     assertEquals(GlobalConstantPool.getD(tccode.reg_sym__sym()), -0.5f, DOUBLE_ERROR_PRECISION);
                     break;
                  case 4: // d = 1.5f;
                     assertEquals(tccode.op(), MOV_reg64_sym);
                     assertEquals(tccode.reg_sym__reg(), 3);
                     assertEquals(GlobalConstantPool.getD(tccode.reg_sym__sym()), 1.5f,DOUBLE_ERROR_PRECISION);
                     break;
                  case 5: // e = -131072.0f;
                     assertEquals(tccode.op(), MOV_regD_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), -131072);
                     break;
                  case 6: // e = 131071.0f;
                     assertEquals(tccode.op(), MOV_regD_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), 131071);
                     break;
                  case 7: // a = b;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 0);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 8: // d = a;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 3);
                     assertEquals(tccode.reg_reg__reg1(), 0);
                     break;
                  case 9: // e = d;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
               }
               break;
            }
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC057_071to074() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC057_071to074.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case DSTORE:   //57
            case DSTORE_0: //71
            case DSTORE_1: //72
            case DSTORE_2: //73
            case DSTORE_3: //74
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               switch (++n)
               {
                  // see file BC057_071to074.java
                  case 1: // a = -131072;
                     assertEquals(tccode.op(), MOV_regD_s18);
                     assertEquals(tccode.s18_reg__reg(), 0);
                     assertEquals(tccode.s18_reg__s18(), -131072);
                     break;
                  case 2: // b = 131071;
                     assertEquals(tccode.op(), MOV_regD_s18);
                     assertEquals(tccode.s18_reg__reg(), 1);
                     assertEquals(tccode.s18_reg__s18(), 131071);
                     break;
                  case 3: // c = -0.5;
                     assertEquals(tccode.op(), MOV_reg64_sym);
                     assertEquals(tccode.reg_sym__reg(), 2);
                     assertEquals(GlobalConstantPool.getD(tccode.reg_sym__sym()), -0.5, DOUBLE_ERROR_PRECISION);
                     break;
                  case 4: // d = 1.5;
                     assertEquals(tccode.op(), MOV_reg64_sym);
                     assertEquals(tccode.reg_sym__reg(), 3);
                     assertEquals(GlobalConstantPool.getD(tccode.reg_sym__sym()), 1.5,DOUBLE_ERROR_PRECISION);
                     break;
                  case 5: // e = -131072.0;
                     assertEquals(tccode.op(), MOV_regD_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), -131072);
                     break;
                  case 6: // e = 131071.0;
                     assertEquals(tccode.op(), MOV_regD_s18);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), 131071);
                     break;
                  case 7: // a = b;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 0);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 8: // d = a;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 3);
                     assertEquals(tccode.reg_reg__reg1(), 0);
                     break;
                  case 9: // e = d;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
               }
               break;
            }
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC058_075to078() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC058_075to078.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case ASTORE:   //58
            case ASTORE_0: //75
            case ASTORE_1: //76
            case ASTORE_2: //77
            case ASTORE_3: //78
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC058_075to078.java
               switch (++n)
               {
                  case 1: // o1 = null;
                     assertEquals(tccode.op(), MOV_regO_null);
                     assertEquals(tccode.s18_reg__reg(), 1);
                     assertEquals(tccode.s18_reg__s18(), 0);
                     break;
                  case 2: // o2 = null;
                     assertEquals(tccode.op(), MOV_regO_null);
                     assertEquals(tccode.s18_reg__reg(), 2);
                     assertEquals(tccode.s18_reg__s18(), 0);
                     break;
                  case 3: // o3 = null;
                     assertEquals(tccode.op(), MOV_regO_null);
                     assertEquals(tccode.s18_reg__reg(), 3);
                     assertEquals(tccode.s18_reg__s18(), 0);
                     break;
                  case 4: // o4 = null;
                     assertEquals(tccode.op(), MOV_regO_null);
                     assertEquals(tccode.s18_reg__reg(), 4);
                     assertEquals(tccode.s18_reg__s18(), 0);
                     break;
                  case 5: // o2 = o1;
                     assertEquals(tccode.op(), MOV_regO_regO);
                     assertEquals(tccode.reg_reg__reg0(), 2);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 6: // o4 = o2;
                     assertEquals(tccode.op(), MOV_regO_regO);
                     assertEquals(tccode.reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg__reg1(), 2);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC096to132() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC096to132.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IADD:  //96
            case ISUB:  //100
            case IMUL:  //104
            case IDIV:  //108
            case IREM:  //112
            case INEG:  //116
            case ISHL:  //120
            case ISHR:  //122
            case IUSHR: //124
            case IAND:  //126
            case IOR:   //128
            case IXOR:  //130
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC096to132.java
               switch (++n)
               {
                  case 1: // i1 = i2 + i3;
                     assertEquals(tccode.op(), ADD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 3);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 2: // i1 = i2 - i3;
                     assertEquals(tccode.op(), SUB_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 5);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 3: // i1 = i2 * i3;
                     assertEquals(tccode.op(), MUL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 6);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 4: // i1 = i2 / i3;
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 7);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 5: // i1 = i2 + 2047;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 8);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 6: // i1 = 2047 + i2;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 9);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 7: // i1 = i2 + 2048;
                     assertEquals(tccode.op(), ADD_regI_regI_sym);
                     assertEquals(tccode.reg_reg_sym__reg0(), 10);
                     assertEquals(tccode.reg_reg_sym__reg1(), 1);
                     assertEquals(GlobalConstantPool.getI(tccode.reg_reg_sym__sym()), 2048);
                     break;
                  case 8: // i1 = 2048 + i2;
                     assertEquals(tccode.op(), ADD_regI_regI_sym);
                     assertEquals(tccode.reg_reg_sym__reg0(), 11);
                     assertEquals(tccode.reg_reg_sym__reg1(), 1);
                     assertEquals(GlobalConstantPool.getI(tccode.reg_reg_sym__sym()), 2048);
                     break;
                  case 9: // i1 = i2 - 2047;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 12);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), -2047);
                     break;
                  case 10: // i1 = i2 - 2048;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 13);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 11: // i1 = i2 - 2049;
                     assertEquals(tccode.op(), ADD_regI_regI_sym);
                     assertEquals(tccode.reg_reg_sym__reg0(), 14);
                     assertEquals(tccode.reg_reg_sym__reg1(), 1);
                     assertEquals(GlobalConstantPool.getI(tccode.reg_reg_sym__sym()), -2049);
                     break;
                  case 12: // i1 = i2 - 2147483647;
                     assertEquals(tccode.op(), ADD_regI_regI_sym);
                     assertEquals(tccode.reg_reg_sym__reg0(), 15);
                     assertEquals(tccode.reg_reg_sym__reg1(), 1);
                     assertEquals(GlobalConstantPool.getI(tccode.reg_reg_sym__sym()), -2147483647);
                     break;
                  case 13: // i1 = i2 - (-2147483648);
                     assertEquals(tccode.op(), SUB_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 16);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 17);
                     break;
                  case 14: // i1 = 2047 - i2;
                     assertEquals(tccode.op(), SUB_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 18);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 15: // i1 = -2048 - i2;
                     assertEquals(tccode.op(), SUB_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 19);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 16: // i1 = i1 + i2 + i3; -------> (i1 + i2)
                     assertEquals(tccode.op(), ADD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 17: // i1 = i1 + i2 + i3; -------> ((i1 + i2) + i3)
                     assertEquals(tccode.op(), ADD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 21);
                     assertEquals(tccode.reg_reg_reg__reg1(), 20);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 18: // i1 = i2 * 2047;
                     assertEquals(tccode.op(), MUL_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 22);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 19: // i1 = 2047 * i2;
                     assertEquals(tccode.op(), MUL_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 23);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 20: // i1 = i2 * 2048;
                     assertEquals(tccode.op(), MUL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 24);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 25);
                     break;
                  case 21: // i1 = 2048 * i2;
                     assertEquals(tccode.op(), MUL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 26);
                     assertEquals(tccode.reg_reg_reg__reg1(), 27);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 22: // i1 = i2 / 2047;
                     assertEquals(tccode.op(), DIV_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 28);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 23: // i1 = 2047 / i2;
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 29);
                     assertEquals(tccode.reg_reg_reg__reg1(), 30);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 24: // i1 = i2 / 2048;
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 31);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 32);
                     break;
                  case 25: // i1 = 2048 / i2;
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 33);
                     assertEquals(tccode.reg_reg_reg__reg1(), 34);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 26: // i1 = i2 % i3;
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 35);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 27: // i1 = i2 % 2047;
                     assertEquals(tccode.op(), MOD_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 36);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 28: // i1 = 2047 % i2;
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 37);
                     assertEquals(tccode.reg_reg_reg__reg1(), 38);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 29: // i1 = i2 % 2048;
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 39);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 40);
                     break;
                  case 30: // i1 = - i2;
                     assertEquals(tccode.op(), SUB_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 41);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 0);
                     break;
                  case 31: // i1 = i2 << i3;
                     assertEquals(tccode.op(), SHL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 42);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 32: // i1 = i2 << 2047;
                     assertEquals(tccode.op(), SHL_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 43);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 33: // i1 = 2047 << i2;
                     assertEquals(tccode.op(), SHL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 44);
                     assertEquals(tccode.reg_reg_reg__reg1(), 45);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 34: // i1 = i2 << 2048;
                     assertEquals(tccode.op(), SHL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 46);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 47);
                     break;
                  case 35: // i1 = i2 >> i3;
                     assertEquals(tccode.op(), SHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 48);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 36: // i1 = i2 >> 2047;
                     assertEquals(tccode.op(), SHR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 49);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 37: // i1 = 2047 >> i2;
                     assertEquals(tccode.op(), SHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 50);
                     assertEquals(tccode.reg_reg_reg__reg1(), 51);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 38: // i1 = i2 >> 2048;
                     assertEquals(tccode.op(), SHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 52);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 53);
                     break;
                  case 39: // i1 = i2 >>> i3;
                     assertEquals(tccode.op(), USHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 54);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 40: // i1 = i2 >>> 2047;
                     assertEquals(tccode.op(), USHR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 55);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 41: // i1 = 2047 >>> i2;
                     assertEquals(tccode.op(), USHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 56);
                     assertEquals(tccode.reg_reg_reg__reg1(), 57);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 42: // i1 = i2 >>> 2048;
                     assertEquals(tccode.op(), USHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 58);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 59);
                     break;
                  case 43: // i1 = i2 & i3;
                     assertEquals(tccode.op(), AND_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 60);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 44: // i1 = i2 & 2047;
                     assertEquals(tccode.op(), AND_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 61);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 45: // i1 = 2047 & i2;
                     assertEquals(tccode.op(), AND_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 62);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 46: // i1 = i2 & 2048;
                     assertEquals(tccode.op(), AND_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 63);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 64);
                     //OperandReg.init(); // restart register indexes
                     break;
                  case 47: // i1 = i2 | i3;
                     assertEquals(tccode.op(), OR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 65);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 48: // i1 = i2 | 2047;
                     assertEquals(tccode.op(), OR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 66);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 49: // i1 = 2047 | i2;
                     assertEquals(tccode.op(), OR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 67);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 50: // i1 = i2 | 2048;
                     assertEquals(tccode.op(), OR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 68);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 69);
                     break;
                  case 51: // i1 = i2 ^ i3;
                     assertEquals(tccode.op(), XOR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 70);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 52: // i1 = i2 ^ 2047;
                     assertEquals(tccode.op(), XOR_regI_regI_s12);
                     //assertEquals(tccode.reg_reg_s12__reg0(), 71); // TODO
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 53: // i1 = 2047 ^ i2;
                     assertEquals(tccode.op(), XOR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 72);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 54: // i1 = i2 ^ 2048;
                     assertEquals(tccode.op(), XOR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 73);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 74);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC096to099() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC096to099.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IADD: //96
            case LADD: //97
            case FADD: //98
            case DADD: //99
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC096to099.java
               switch (++n)
               {
                  // type: int
                  case 1: // i1 = i2 + i3;
                     assertEquals(tccode.op(), ADD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 3);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 2: // i1 = i2 + 2047;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 4);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 3: // i1 = 2047 + i2;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 5);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 4: // i1 = i2 + 2048;
                     assertEquals(tccode.op(), ADD_regI_regI_sym);
                     assertEquals(tccode.reg_reg_sym__reg0(), 6);
                     assertEquals(tccode.reg_reg_sym__reg1(), 1);
                     assertEquals(GlobalConstantPool.getI(tccode.reg_reg_sym__sym()), 2048);
                     break;
                  case 5: // i1 = 2048 + i2;
                     assertEquals(tccode.op(), ADD_regI_regI_sym);
                     assertEquals(tccode.reg_reg_sym__reg0(), 7);
                     assertEquals(tccode.reg_reg_sym__reg1(), 1);
                     assertEquals(GlobalConstantPool.getI(tccode.reg_reg_sym__sym()), 2048);
                     break;
                  case 6: // i1 = i1 + i2 + i3; -------> (i1 + i2)
                     assertEquals(tccode.op(), ADD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 8);
                     assertEquals(tccode.reg_reg_reg__reg1(), 0);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 7: // i1 = i1 + i2 + i3; -------> ((i1 + i2) + i3)
                     assertEquals(tccode.op(), ADD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
  
                  // type: long
                  case 8: // l1 = l2 + l3;
                     assertEquals(tccode.op(), ADD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 9: // l1 = l2 + 2047;
                     assertEquals(tccode.op(), ADD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 11);
                     break;
                  case 10: // l1 = 2047 + l2;
                     assertEquals(tccode.op(), ADD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg_reg__reg1(), 13);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 11: // l1 = l2 + 2048;
                     assertEquals(tccode.op(), ADD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 14);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 15);
                     break;
                  case 12: // l1 = 2048 + l2;
                     assertEquals(tccode.op(), ADD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 16);
                     assertEquals(tccode.reg_reg_reg__reg1(), 17);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 13: // l1 = l1 + l2 + l3; -------> (l1 + l2)
                     assertEquals(tccode.op(), ADD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 18);
                     assertEquals(tccode.reg_reg_reg__reg1(), 0);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 14: // l1 = l1 + l2 + l3; -------> ((l1 + l2) + l3)
                     assertEquals(tccode.op(), ADD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 19);
                     assertEquals(tccode.reg_reg_reg__reg1(), 18);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
  
                  // type: float
                  case 15: // f1 = f2 + f3;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 16: // f1 = f2 + 2047;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 21);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 22);
                     break;
                  case 17: // f1 = 2047 + f2;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 23);
                     assertEquals(tccode.reg_reg_reg__reg1(), 24);
                     assertEquals(tccode.reg_reg_reg__reg2(), 4);
                     break;
                  case 18: // f1 = f2 + 2048;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 25);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 26);
                     break;
                  case 19: // f1 = 2048 + f2;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 27);
                     assertEquals(tccode.reg_reg_reg__reg1(), 28);
                     assertEquals(tccode.reg_reg_reg__reg2(), 4);
                     break;
                  case 20: // f1 = f1 + f2 + f3; -------> (f1 + f2)
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 29);
                     assertEquals(tccode.reg_reg_reg__reg1(), 3);
                     assertEquals(tccode.reg_reg_reg__reg2(), 4);
                     break;
                  case 21: // f1 = f1 + f2 + f3; -------> ((f1 + f2) + f3)
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 30);
                     assertEquals(tccode.reg_reg_reg__reg1(), 29);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
  
                  // type: double
                  case 22: // d1 = d2 + d3;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 31);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 23: // d1 = d2 + 2047;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 32);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 33);
                     break;
                  case 24: // d1 = 2047 + d2;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 34);
                     assertEquals(tccode.reg_reg_reg__reg1(), 35);
                     assertEquals(tccode.reg_reg_reg__reg2(), 7);
                     break;
                  case 25: // d1 = d2 + 2048;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 36);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 37);
                     break;
                  case 26: // d1 = 2048 + d2;
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 38);
                     assertEquals(tccode.reg_reg_reg__reg1(), 39);
                     assertEquals(tccode.reg_reg_reg__reg2(), 7);
                     break;
                  case 27: // d1 = d1 + d2 + d3; -------> (d1 + d2)
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 40);
                     assertEquals(tccode.reg_reg_reg__reg1(), 6);
                     assertEquals(tccode.reg_reg_reg__reg2(), 7);
                     break;
                  case 28: // d1 = d1 + d2 + d3; -------> ((d1 + d2) + d3)
                     assertEquals(tccode.op(), ADD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 41);
                     assertEquals(tccode.reg_reg_reg__reg1(), 40);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC100to103() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC100to103.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case ISUB: //100
            case LSUB: //101
            case FSUB: //102
            case DSUB: //103
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC100to103.java
               switch (++n)
               {
                  // type: int
                  case 1: // i1 = i2 - i3;
                     assertEquals(tccode.op(), SUB_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 3);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 2: // i1 = i2 - 2047;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 4);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), -2047);
                     break;
                  case 3: // i1 = 2047 - i2;
                     assertEquals(tccode.op(), SUB_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 5);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 4: // i1 = i2 - 2048;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 6);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 5: // i1 = 2048 - i2;
                     assertEquals(tccode.op(), SUB_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 7);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 6: // i1 = i1 - i2 - i3; -------> (i1 - i2)
                     assertEquals(tccode.op(), SUB_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg_reg__reg1(), 0);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 7: // i1 = i1 - i2 - i3; -------> ((i1 - i2) - i3)
                     assertEquals(tccode.op(), SUB_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 9);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
  
                  // type: long
                  case 8: // l1 = l2 - l3;
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 9: // l1 = l2 - 2047;
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 11);
                     break;
                  case 10: // l1 = 2047 - l2;
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg_reg__reg1(), 13);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 11: // l1 = l2 - 2048;
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 14);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 15);
                     break;
                  case 12: // l1 = 2048 - l2;
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 16);
                     assertEquals(tccode.reg_reg_reg__reg1(), 17);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 13: // l1 = l1 - l2 - l3; -------> (l1 - l2)
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 18);
                     assertEquals(tccode.reg_reg_reg__reg1(), 0);
                     assertEquals(tccode.reg_reg_reg__reg2(), 1);
                     break;
                  case 14: // l1 = l1 - l2 - l3; -------> ((l1 - l2) - l3)
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 19);
                     assertEquals(tccode.reg_reg_reg__reg1(), 18);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
  
                  // type: float
                  case 15: // f1 = f2 - f3;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 16: // f1 = f2 - 2047;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 21);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 22);
                     break;
                  case 17: // f1 = 2047 - f2;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 23);
                     assertEquals(tccode.reg_reg_reg__reg1(), 24);
                     assertEquals(tccode.reg_reg_reg__reg2(), 4);
                     break;
                  case 18: // f1 = f2 - 2048;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 25);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 26);
                     break;
                  case 19: // f1 = 2048 - f2;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 27);
                     assertEquals(tccode.reg_reg_reg__reg1(), 28);
                     assertEquals(tccode.reg_reg_reg__reg2(), 4);
                     break;
                  case 20: // f1 = f1 - f2 - f3; -------> (f1 - f2)
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 29);
                     assertEquals(tccode.reg_reg_reg__reg1(), 3);
                     assertEquals(tccode.reg_reg_reg__reg2(), 4);
                     break;
                  case 21: // f1 = f1 - f2 - f3; -------> ((f1 - f2) - f3)
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 30);
                     assertEquals(tccode.reg_reg_reg__reg1(), 29);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
  
                  // type: double
                  case 22: // d1 = d2 - d3;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 31);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 23: // d1 = d2 - 2047;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 32);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 33);
                     break;
                  case 24: // d1 = 2047 - d2;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 34);
                     assertEquals(tccode.reg_reg_reg__reg1(), 35);
                     assertEquals(tccode.reg_reg_reg__reg2(), 7);
                     break;
                  case 25: // d1 = d2 - 2048;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 36);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 37);
                     break;
                  case 26: // d1 = 2048 - d2;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 38);
                     assertEquals(tccode.reg_reg_reg__reg1(), 39);
                     assertEquals(tccode.reg_reg_reg__reg2(), 7);
                     break;
                  case 27: // d1 = d1 - d2 - d3; -------> (d1 - d2)
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 40);
                     assertEquals(tccode.reg_reg_reg__reg1(), 6);
                     assertEquals(tccode.reg_reg_reg__reg2(), 7);
                     break;
                  case 28: // d1 = d1 - d2 - d3; -------> ((d1 - d2) - d3)
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 41);
                     assertEquals(tccode.reg_reg_reg__reg1(), 40);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC104to107() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC104to107.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IMUL: //104
            case LMUL: //105
            case FMUL: //106
            case DMUL: //107
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC104to107.java
               switch (++n)
               {
                  // type: int
                  case 1: // i1 = i2 * i3;
                     assertEquals(tccode.op(), MUL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 2: // i1 = i2 * 2047;
                     assertEquals(tccode.op(), MUL_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 5);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 3: // i1 = 2047 * i2;
                     assertEquals(tccode.op(), MUL_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 6);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 4: // i1 = i2 * 2048;
                     assertEquals(tccode.op(), MUL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 7);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 5: // i1 = 2048 * i2;
                     assertEquals(tccode.op(), MUL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg_reg__reg1(), 10);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 6: // i1 = i1 * i2 * i3; -------> (i1 * i2)
                     assertEquals(tccode.op(), MUL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 11);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 7: // i1 = i1 * i2 * i3; -------> ((i1 * i2) * i3)
                     assertEquals(tccode.op(), MUL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg_reg__reg1(), 11);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
  
                  // type: long
                  case 8: // l1 = l2 * l3;
                     assertEquals(tccode.op(), MUL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 9: // l1 = l2 * 2047;
                     assertEquals(tccode.op(), MUL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 11);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 12);
                     break;
                  case 10: // l1 = 2047 * l2;
                     assertEquals(tccode.op(), MUL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 13);
                     assertEquals(tccode.reg_reg_reg__reg1(), 14);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 11: // l1 = l2 * 2048;
                     assertEquals(tccode.op(), MUL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 15);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 16);
                     break;
                  case 12: // l1 = 2048 * l2;
                     assertEquals(tccode.op(), MUL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 17);
                     assertEquals(tccode.reg_reg_reg__reg1(), 18);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 13: // l1 = l1 * l2 * l3; -------> (l1 * l2)
                     assertEquals(tccode.op(), MUL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 19);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 14: // l1 = l1 * l2 * l3; -------> ((l1 * l2) * l3)
                     assertEquals(tccode.op(), MUL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg_reg__reg1(), 19);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
  
                  // type: float
                  case 15: // f1 = f2 * f3;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 21);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 6);
                     break;
                  case 16: // f1 = f2 * 2047;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 22);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 23);
                     break;
                  case 17: // f1 = 2047 * f2;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 24);
                     assertEquals(tccode.reg_reg_reg__reg1(), 25);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 18: // f1 = f2 * 2048;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 26);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 27);
                     break;
                  case 19: // f1 = 2048 * f2;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 28);
                     assertEquals(tccode.reg_reg_reg__reg1(), 29);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 20: // f1 = f1 * f2 * f3; -------> (f1 * f2)
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 30);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 21: // f1 = f1 * f2 * f3; -------> ((f1 * f2) * f3)
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 31);
                     assertEquals(tccode.reg_reg_reg__reg1(), 30);
                     assertEquals(tccode.reg_reg_reg__reg2(), 6);
                     break;
  
                  // type: double
                  case 22: // d1 = d2 * d3;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 32);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
                  case 23: // d1 = d2 * 2047;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 33);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 34);
                     break;
                  case 24: // d1 = 2047 * d2;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 35);
                     assertEquals(tccode.reg_reg_reg__reg1(), 36);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 25: // d1 = d2 * 2048;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 37);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 38);
                     break;
                  case 26: // d1 = 2048 * d2;
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 39);
                     assertEquals(tccode.reg_reg_reg__reg1(), 40);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 27: // d1 = d1 * d2 * d3; -------> (d1 * d2)
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 41);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 28: // d1 = d1 * d2 * d3; -------> ((d1 * d2) * d3)
                     assertEquals(tccode.op(), MUL_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 42);
                     assertEquals(tccode.reg_reg_reg__reg1(), 41);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC108to111() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC108to111.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IDIV: //108
            case LDIV: //109
            case FDIV: //110
            case DDIV: //111
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC108to111.java
               switch (++n)
               {
                  // type: int
                  case 1: // i1 = i2 / i3;
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 2: // i1 = i2 / 2047;
                     assertEquals(tccode.op(), DIV_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 5);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 3: // i1 = 2047 / i2;
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 6);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 4: // i1 = i2 / 2048;
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 8);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
                  case 5: // i1 = 2048 / i2;
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 11);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 6: // i1 = i1 / i2 / i3; -------> (i1 / i2)
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 7: // i1 = i1 / i2 / i3; -------> ((i1 / i2) / i3)
                     assertEquals(tccode.op(), DIV_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 13);
                     assertEquals(tccode.reg_reg_reg__reg1(), 12);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
  
                  // type: long
                  case 8: // l1 = l2 / l3;
                     assertEquals(tccode.op(), DIV_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 9: // l1 = l2 / 2047;
                     assertEquals(tccode.op(), DIV_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 11);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 12);
                     break;
                  case 10: // l1 = 2047 / l2;
                     assertEquals(tccode.op(), DIV_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 13);
                     assertEquals(tccode.reg_reg_reg__reg1(), 14);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 11: // l1 = l2 / 2048;
                     assertEquals(tccode.op(), DIV_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 15);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 16);
                     break;
                  case 12: // l1 = 2048 / l2;
                     assertEquals(tccode.op(), DIV_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 17);
                     assertEquals(tccode.reg_reg_reg__reg1(), 18);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 13: // l1 = l1 / l2 / l3; -------> (l1 / l2)
                     assertEquals(tccode.op(), DIV_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 19);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 14: // l1 = l1 / l2 / l3; -------> ((l1 / l2) / l3)
                     assertEquals(tccode.op(), DIV_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg_reg__reg1(), 19);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
  
                  // type: float
                  case 15: // f1 = f2 / f3;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 21);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 6);
                     break;
                  case 16: // f1 = f2 / 2047;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 22);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 23);
                     break;
                  case 17: // f1 = 2047 / f2;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 24);
                     assertEquals(tccode.reg_reg_reg__reg1(), 25);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 18: // f1 = f2 / 2048;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 26);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 27);
                     break;
                  case 19: // f1 = 2048 / f2;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 28);
                     assertEquals(tccode.reg_reg_reg__reg1(), 29);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 20: // f1 = f1 / f2 / f3; -------> (f1 / f2)
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 30);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 21: // f1 = f1 / f2 / f3; -------> ((f1 / f2) / f3)
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 31);
                     assertEquals(tccode.reg_reg_reg__reg1(), 30);
                     assertEquals(tccode.reg_reg_reg__reg2(), 6);
                     break;
  
                  // type: double
                  case 22: // d1 = d2 / d3;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 32);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
                  case 23: // d1 = d2 / 2047;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 33);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 34);
                     break;
                  case 24: // d1 = 2047 / d2;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 35);
                     assertEquals(tccode.reg_reg_reg__reg1(), 36);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 25: // d1 = d2 / 2048;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 37);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 38);
                     break;
                  case 26: // d1 = 2048 / d2;
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 39);
                     assertEquals(tccode.reg_reg_reg__reg1(), 40);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 27: // d1 = d1 / d2 / d3; -------> (d1 / d2)
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 41);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 28: // d1 = d1 / d2 / d3; -------> ((d1 / d2) / d3)
                     assertEquals(tccode.op(), DIV_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 42);
                     assertEquals(tccode.reg_reg_reg__reg1(), 41);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC112to115() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC112to115.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IREM: //112
            case LREM: //113
            case FREM: //114
            case DREM: //115
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC112to115.java
               switch (++n)
               {
                  // type: int
                  case 1: // i1 = i2 % i3;
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 2: // i1 = i2 % 2047;
                     assertEquals(tccode.op(), MOD_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 5);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 3: // i1 = 2047 % i2;
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 6);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 4: // i1 = i2 % 2048;
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 8);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
                  case 5: // i1 = 2048 % i2;
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 11);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 6: // i1 = i1 % i2 % i3; -------> (i1 % i2)
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 7: // i1 = i1 % i2 % i3; -------> ((i1 % i2) % i3)
                     assertEquals(tccode.op(), MOD_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 13);
                     assertEquals(tccode.reg_reg_reg__reg1(), 12);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
  
                  // type: long
                  case 8: // l1 = l2 % l3;
                     assertEquals(tccode.op(), MOD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 9: // l1 = l2 % 2047;
                     assertEquals(tccode.op(), MOD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 11);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 12);
                     break;
                  case 10: // l1 = 2047 % l2;
                     assertEquals(tccode.op(), MOD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 13);
                     assertEquals(tccode.reg_reg_reg__reg1(), 14);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 11: // l1 = l2 % 2048;
                     assertEquals(tccode.op(), MOD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 15);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 16);
                     break;
                  case 12: // l1 = 2048 % l2;
                     assertEquals(tccode.op(), MOD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 17);
                     assertEquals(tccode.reg_reg_reg__reg1(), 18);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 13: // l1 = l1 % l2 % l3; -------> (l1 % l2)
                     assertEquals(tccode.op(), MOD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 19);
                     assertEquals(tccode.reg_reg_reg__reg1(), 1);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 14: // l1 = l1 % l2 % l3; -------> ((l1 % l2) % l3)
                     assertEquals(tccode.op(), MOD_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg_reg__reg1(), 19);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
  
                  // type: float
                  case 15: // f1 = f2 % f3;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 21);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 6);
                     break;
                  case 16: // f1 = f2 % 2047;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 22);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 23);
                     break;
                  case 17: // f1 = 2047 % f2;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 24);
                     assertEquals(tccode.reg_reg_reg__reg1(), 25);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 18: // f1 = f2 % 2048;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 26);
                     assertEquals(tccode.reg_reg_reg__reg1(), 5);
                     assertEquals(tccode.reg_reg_reg__reg2(), 27);
                     break;
                  case 19: // f1 = 2048 % f2;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 28);
                     assertEquals(tccode.reg_reg_reg__reg1(), 29);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 20: // f1 = f1 % f2 % f3; -------> (f1 % f2)
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 30);
                     assertEquals(tccode.reg_reg_reg__reg1(), 4);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 21: // f1 = f1 % f2 % f3; -------> ((f1 % f2) % f3)
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 31);
                     assertEquals(tccode.reg_reg_reg__reg1(), 30);
                     assertEquals(tccode.reg_reg_reg__reg2(), 6);
                     break;
  
                  // type: double
                  case 22: // d1 = d2 % d3;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 32);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
                  case 23: // d1 = d2 % 2047;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 33);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 34);
                     break;
                  case 24: // d1 = 2047 % d2;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 35);
                     assertEquals(tccode.reg_reg_reg__reg1(), 36);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 25: // d1 = d2 % 2048;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 37);
                     assertEquals(tccode.reg_reg_reg__reg1(), 8);
                     assertEquals(tccode.reg_reg_reg__reg2(), 38);
                     break;
                  case 26: // d1 = 2048 % d2;
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 39);
                     assertEquals(tccode.reg_reg_reg__reg1(), 40);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 27: // d1 = d1 % d2 % d3; -------> (d1 % d2)
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 41);
                     assertEquals(tccode.reg_reg_reg__reg1(), 7);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 28: // d1 = d1 % d2 % d3; -------> ((d1 % d2) % d3)
                     assertEquals(tccode.op(), MOD_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 42);
                     assertEquals(tccode.reg_reg_reg__reg1(), 41);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC116to119() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC116to119.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case INEG: //116
            case LNEG: //117
            case FNEG: //118
            case DNEG: //119
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC116to119.java
               switch (++n)
               {
                  // type: int
                  case 1: // i1 = -i2;
                     assertEquals(tccode.op(), SUB_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 4);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 0);
                     break;
                  case 2: // i1 = -(i1+i2);
                     assertEquals(tccode.op(), SUB_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 6);
                     assertEquals(tccode.reg_reg_s12__reg1(), 5);
                     assertEquals(tccode.reg_reg_s12__s12(), 0);
                     break;
  
                  // type: long
                  case 3: // l1 = -l2;
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 11);
                     assertEquals(tccode.reg_reg_reg__reg2(), 2);
                     break;
                  case 4: // l1 = -(l1+l2);
                     assertEquals(tccode.op(), SUB_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 13);
                     assertEquals(tccode.reg_reg_reg__reg1(), 14);
                     assertEquals(tccode.reg_reg_reg__reg2(), 12);
                     break;
  
                  // type: float
                  case 5: // f1 = -f2;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 15);
                     assertEquals(tccode.reg_reg_reg__reg1(), 16);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 6: // f1 = -(f1+f2);
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 18);
                     assertEquals(tccode.reg_reg_reg__reg1(), 19);
                     assertEquals(tccode.reg_reg_reg__reg2(), 17);
                     break;
  
                  // type: double
                  case 7: // d1 = -d2;
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg_reg__reg1(), 21);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 8: // d1 = -(d1+d2);
                     assertEquals(tccode.op(), SUB_regD_regD_regD);
                     assertEquals(tccode.reg_reg_reg__reg0(), 23);
                     assertEquals(tccode.reg_reg_reg__reg1(), 24);
                     assertEquals(tccode.reg_reg_reg__reg2(), 22);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC120to125() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC120to125.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case ISHL:  //120
            case LSHL:  //121
            case ISHR:  //122
            case LSHR:  //123
            case IUSHR: //124
            case LUSHR: //125
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC120to125.java
               switch (++n)
               {
                  case 1: // i1 = i2 << i3;
                     assertEquals(tccode.op(), SHL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 2: // i1 = i2 << 2047;
                     assertEquals(tccode.op(), SHL_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 5);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 3: // i1 = i2 << -2048;
                     assertEquals(tccode.op(), SHL_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 6);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 4: // i1 = i2 << 2048;
                     assertEquals(tccode.op(), SHL_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 7);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 5: // l1 = l2 << i1;
                     assertEquals(tccode.op(), SHL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 5);
                     break;
                  case 6: // l1 = l2 << 2047;
                     assertEquals(tccode.op(), SHL_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 6);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 7);
                     break;
                  case 7: // i1 = i2 >> i3;
                     assertEquals(tccode.op(), SHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 8: // i1 = i2 >> 2047;
                     assertEquals(tccode.op(), SHR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 10);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 9: // i1 = i2 >> -2048;
                     assertEquals(tccode.op(), SHR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 11);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 10: // i1 = i2 >> 2048;
                     assertEquals(tccode.op(), SHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 13);
                     break;
                  case 11: // l1 = l2 >> i1;
                     assertEquals(tccode.op(), SHR_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 8);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
                  case 12: // l1 = l2 >> 2047;
                     assertEquals(tccode.op(), SHR_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 11);
                     break;
                  case 13: // i1 = i2 >>> i3;
                     assertEquals(tccode.op(), USHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 14);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 14: // i1 = i2 >>> 2047;
                     assertEquals(tccode.op(), USHR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 15);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 15: // i1 = i2 >>> -2048;
                     assertEquals(tccode.op(), USHR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 16);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 16: // i1 = i2 >>> 2048;
                     assertEquals(tccode.op(), USHR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 17);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 18);
                     break;
                  case 17: // l1 = l2 >>> i1;
                     assertEquals(tccode.op(), USHR_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 13);
                     break;
                  case 18: // l1 = l2 >>> 2047;
                     assertEquals(tccode.op(), USHR_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 14);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 15);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC126to131() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC126to131.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IAND: //126
            case LAND: //127
            case IOR:  //128
            case LOR:  //129
            case IXOR: //130
            case LXOR: //131
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC126to131.java
               switch (++n)
               {
                  case 1: // i1 = i2 & i3;
                     assertEquals(tccode.op(), AND_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 2: // i1 = i2 & 2047;
                     assertEquals(tccode.op(), AND_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 5);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 3: // i1 = i2 & -2048;
                     assertEquals(tccode.op(), AND_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 6);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 4: // i1 = i2 & 2048;
                     assertEquals(tccode.op(), AND_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 7);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 8);
                     break;
                  case 5: // l1 = l2 & l3;
                     assertEquals(tccode.op(), AND_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 4);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 6: // l1 = l2 & 2047;
                     assertEquals(tccode.op(), AND_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 5);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 6);
                     break;
                  case 7: // i1 = i2 | i3;
                     assertEquals(tccode.op(), OR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 8: // i1 = i2 | 2047;
                     assertEquals(tccode.op(), OR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 10);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 9: // i1 = i2 | -2048;
                     assertEquals(tccode.op(), OR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 11);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 10: // i1 = i2 | 2048;
                     assertEquals(tccode.op(), OR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 13);
                     break;
                  case 11: // l1 = l2 | l3;
                     assertEquals(tccode.op(), OR_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 7);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 12: // l1 = l2 | 2047;
                     assertEquals(tccode.op(), OR_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 8);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 9);
                     break;
                  case 13: // i1 = i2 ^ i3;
                     assertEquals(tccode.op(), XOR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 14);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 14: // i1 = i2 ^ 2047;
                     assertEquals(tccode.op(), XOR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 15);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 2047);
                     break;
                  case 15: // i1 = i2 ^ -2048;
                     assertEquals(tccode.op(), XOR_regI_regI_s12);
                     assertEquals(tccode.reg_reg_s12__reg0(), 16);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), -2048);
                     break;
                  case 16: // i1 = i2 ^ 2048;
                     assertEquals(tccode.op(), XOR_regI_regI_regI);
                     assertEquals(tccode.reg_reg_reg__reg0(), 17);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 18);
                     break;
                  case 17: // l1 = l2 ^ l3;
                     assertEquals(tccode.op(), XOR_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 3);
                     break;
                  case 18: // l1 = l2 ^ 2047;
                     assertEquals(tccode.op(), XOR_regL_regL_regL);
                     assertEquals(tccode.reg_reg_reg__reg0(), 11);
                     assertEquals(tccode.reg_reg_reg__reg1(), 2);
                     assertEquals(tccode.reg_reg_reg__reg2(), 12);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC132() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC132.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IINC: //132
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC132.java
               switch (++n)
               {
                  case 1: // x++;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 1);
                     assertEquals(tccode.reg_reg_s12__reg1(), 1);
                     assertEquals(tccode.reg_reg_s12__s12(), 1);
                     break;
                  case 2: // y--;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 2);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), -1);
                     break;
                  case 3: // --z;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 3);
                     assertEquals(tccode.reg_reg_s12__reg1(), 3);
                     assertEquals(tccode.reg_reg_s12__s12(), -1);
                     break;
                  case 4: // ++y;
                     assertEquals(tccode.op(), ADD_regI_s12_regI);
                     assertEquals(tccode.reg_reg_s12__reg0(), 2);
                     assertEquals(tccode.reg_reg_s12__reg1(), 2);
                     assertEquals(tccode.reg_reg_s12__s12(), 1);
                     break;
               }
            }
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC133to147() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC133to147.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case I2L: //133
            case I2F: //134
            case I2D: //135
            case L2I: //136
            case L2F: //137
            case L2D: //138
            case F2I: //139
            case F2L: //140
            case F2D: //141
            case D2I: //142
            case D2L: //143
            case D2F: //144
            case I2B: //145
            case I2C: //146
            case I2S: //147
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC133to147.java
               switch (++n)
               {
                  case 1: // b1 = (byte) c1;
                     assertEquals(tccode.op(), CONV_regIb_regI);
                     assertEquals(tccode.reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 2: // b1 = (byte) s1;
                     assertEquals(tccode.op(), CONV_regIb_regI);
                     assertEquals(tccode.reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 3: // b1 = (byte) i1;
                     assertEquals(tccode.op(), CONV_regIb_regI);
                     assertEquals(tccode.reg_reg__reg0(), 11);
                     assertEquals(tccode.reg_reg__reg1(), 7);
                     break;
                  case 4: // b1 = (byte) l1; ----> convert from long to int
                     assertEquals(tccode.op(), CONV_regI_regL);
                     assertEquals(tccode.reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 5: // b1 = (byte) l1; ----> convert int to byte
                     assertEquals(tccode.op(), CONV_regIb_regI);
                     assertEquals(tccode.reg_reg__reg0(), 13);
                     assertEquals(tccode.reg_reg__reg1(), 12);
                     break;
                  case 6: // b1 = (byte) f1; ----> convert from float to int
                     assertEquals(tccode.op(), CONV_regI_regD);
                     assertEquals(tccode.reg_reg__reg0(), 14);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 7: // b1 = (byte) f1; ----> convert int to byte
                     assertEquals(tccode.op(), CONV_regIb_regI);
                     assertEquals(tccode.reg_reg__reg0(), 15);
                     assertEquals(tccode.reg_reg__reg1(), 14);
                     break;
                  case 8: // b1 = (byte) d1; ----> convert from double to int
                     assertEquals(tccode.op(), CONV_regI_regD);
                     assertEquals(tccode.reg_reg__reg0(), 16);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 9: // b1 = (byte) d1; ----> convert int to byte
                     assertEquals(tccode.op(), CONV_regIb_regI);
                     assertEquals(tccode.reg_reg__reg0(), 17);
                     assertEquals(tccode.reg_reg__reg1(), 16);
                     break;
                  case 10: // c1 = (char) b1;
                     assertEquals(tccode.op(), CONV_regIc_regI);
                     assertEquals(tccode.reg_reg__reg0(), 18);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 11: // c1 = (char) s1;
                     assertEquals(tccode.op(), CONV_regIc_regI);
                     assertEquals(tccode.reg_reg__reg0(), 19);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 12: // c1 = (char) i1;
                     assertEquals(tccode.op(), CONV_regIc_regI);
                     assertEquals(tccode.reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg__reg1(), 7);
                     break;
                  case 13: // c1 = (char) l1; ----> convert from long to int
                     assertEquals(tccode.op(), CONV_regI_regL);
                     assertEquals(tccode.reg_reg__reg0(), 21);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 14: // c1 = (char) l1; ----> convert int to char
                     assertEquals(tccode.op(), CONV_regIc_regI);
                     assertEquals(tccode.reg_reg__reg0(), 22);
                     assertEquals(tccode.reg_reg__reg1(), 21);
                     break;
                  case 15: // c1 = (char) f1; ----> convert from float to int
                     assertEquals(tccode.op(), CONV_regI_regD);
                     assertEquals(tccode.reg_reg__reg0(), 23);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 16: // c1 = (char) f1; ----> convert int to char
                     assertEquals(tccode.op(), CONV_regIc_regI);
                     assertEquals(tccode.reg_reg__reg0(), 24);
                     assertEquals(tccode.reg_reg__reg1(), 23);
                     break;
                  case 17: // c1 = (char) d1; ----> convert from double to int
                     assertEquals(tccode.op(), CONV_regI_regD);
                     assertEquals(tccode.reg_reg__reg0(), 25);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 18: // c1 = (char) d1; ----> convert int to char
                     assertEquals(tccode.op(), CONV_regIc_regI);
                     assertEquals(tccode.reg_reg__reg0(), 26);
                     assertEquals(tccode.reg_reg__reg1(), 25);
                     break;
                  case 19: // s1 = (short) b1;
                     assertEquals(tccode.op(), CONV_regIs_regI);
                     assertEquals(tccode.reg_reg__reg0(), 27);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 20: // s1 = (short) c1;
                     assertEquals(tccode.op(), CONV_regIs_regI);
                     assertEquals(tccode.reg_reg__reg0(), 28);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 21: // s1 = (short) i1;
                     assertEquals(tccode.op(), CONV_regIs_regI);
                     assertEquals(tccode.reg_reg__reg0(), 29);
                     assertEquals(tccode.reg_reg__reg1(), 7);
                     break;
                  case 22: // s1 = (short) l1; ----> convert from long to int
                     assertEquals(tccode.op(), CONV_regI_regL);
                     assertEquals(tccode.reg_reg__reg0(), 30);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 23: // s1 = (short) l1; ----> convert int to short
                     assertEquals(tccode.op(), CONV_regIs_regI);
                     assertEquals(tccode.reg_reg__reg0(), 31);
                     assertEquals(tccode.reg_reg__reg1(), 30);
                     break;
                  case 24: // s1 = (short) f1; ----> convert from float to int
                     assertEquals(tccode.op(), CONV_regI_regD);
                     assertEquals(tccode.reg_reg__reg0(), 32);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 25: // s1 = (short) f1; ----> convert int to short
                     assertEquals(tccode.op(), CONV_regIs_regI);
                     assertEquals(tccode.reg_reg__reg0(), 33);
                     assertEquals(tccode.reg_reg__reg1(), 32);
                     break;
                  case 26: // s1 = (short) d1; ----> convert from double to int
                     assertEquals(tccode.op(), CONV_regI_regD);
                     assertEquals(tccode.reg_reg__reg0(), 34);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 27: // s1 = (short) d1; ----> convert int to short
                     assertEquals(tccode.op(), CONV_regIs_regI);
                     assertEquals(tccode.reg_reg__reg0(), 35);
                     assertEquals(tccode.reg_reg__reg1(), 34);
                     break;
                  case 28: // i1 = (int) l1;
                     assertEquals(tccode.op(), CONV_regI_regL);
                     assertEquals(tccode.reg_reg__reg0(), 36);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 29: // i1 = (int) f1;
                     assertEquals(tccode.op(), CONV_regI_regD);
                     assertEquals(tccode.reg_reg__reg0(), 37);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 30: // i1 = (int) d1;
                     assertEquals(tccode.op(), CONV_regI_regD);
                     assertEquals(tccode.reg_reg__reg0(), 38);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 31: // l1 = (long) b1;
                     assertEquals(tccode.op(), CONV_regL_regI);
                     assertEquals(tccode.reg_reg__reg0(), 7);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 32: // l1 = (long) c1;
                     assertEquals(tccode.op(), CONV_regL_regI);
                     assertEquals(tccode.reg_reg__reg0(), 8);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 33: // l1 = (long) s1;
                     assertEquals(tccode.op(), CONV_regL_regI);
                     assertEquals(tccode.reg_reg__reg0(), 9);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 34: // l1 = (long) i1;
                     assertEquals(tccode.op(), CONV_regL_regI);
                     assertEquals(tccode.reg_reg__reg0(), 10);
                     assertEquals(tccode.reg_reg__reg1(), 7);
                     break;
                  case 35: // l1 = (long) f1;
                     assertEquals(tccode.op(), CONV_regL_regD);
                     assertEquals(tccode.reg_reg__reg0(), 11);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 36: // l1 = (long) d1;
                     assertEquals(tccode.op(), CONV_regL_regD);
                     assertEquals(tccode.reg_reg__reg0(), 12);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 37: // f1 = (float) b1;
                     assertEquals(tccode.op(), CONV_regD_regI);
                     assertEquals(tccode.reg_reg__reg0(), 13);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 38: // f1 = (float) c1;
                     assertEquals(tccode.op(), CONV_regD_regI);
                     assertEquals(tccode.reg_reg__reg0(), 14);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 39: // f1 = (float) s1;
                     assertEquals(tccode.op(), CONV_regD_regI);
                     assertEquals(tccode.reg_reg__reg0(), 15);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 40: // f1 = (float) i1;
                     assertEquals(tccode.op(), CONV_regD_regI);
                     assertEquals(tccode.reg_reg__reg0(), 16);
                     assertEquals(tccode.reg_reg__reg1(), 7);
                     break;
                  case 41: // f1 = (float) l1;
                     assertEquals(tccode.op(), CONV_regD_regL);
                     assertEquals(tccode.reg_reg__reg0(), 17);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 42: // f1 = (float) d1;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 18);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
  
                  case 43: // d1 = (double) b1;
                     assertEquals(tccode.op(), CONV_regD_regI);
                     assertEquals(tccode.reg_reg__reg0(), 19);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 44: // d1 = (double) c1;
                     assertEquals(tccode.op(), CONV_regD_regI);
                     assertEquals(tccode.reg_reg__reg0(), 20);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
                  case 45: // d1 = (double) s1;
                     assertEquals(tccode.op(), CONV_regD_regI);
                     assertEquals(tccode.reg_reg__reg0(), 21);
                     assertEquals(tccode.reg_reg__reg1(), 5);
                     break;
                  case 46: // d1 = (double) i1;
                     assertEquals(tccode.op(), CONV_regD_regI);
                     assertEquals(tccode.reg_reg__reg0(), 22);
                     assertEquals(tccode.reg_reg__reg1(), 7);
                     break;
                  case 47: // d1 = (double) l1;
                     assertEquals(tccode.op(), CONV_regD_regL);
                     assertEquals(tccode.reg_reg__reg0(), 23);
                     assertEquals(tccode.reg_reg__reg1(), 1);
                     break;
                  case 48: // d1 = (double) f1;
                     assertEquals(tccode.op(), MOV_reg64_reg64);
                     assertEquals(tccode.reg_reg__reg0(), 24);
                     assertEquals(tccode.reg_reg__reg1(), 3);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC187to189() throws Exception
   {
      JavaCode jcode = startTest("BC187to189.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case NEW: //187
            case NEWARRAY: //188
            case ANEWARRAY: //189
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC187to189.java
               switch (++n)
               {
                  case 1: // arBool = new boolean[2];
                     assertEquals(tccode.op(), NEWARRAY_len);
                     assertEquals(tccode.newarray__regO(), 2);
                     assertEquals(tccode.newarray__sym(), 12);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 2);
                     break;
                  case 2: // arChar = new char[4];
                     assertEquals(tccode.op(), NEWARRAY_len);
                     assertEquals(tccode.newarray__regO(), 4);
                     assertEquals(tccode.newarray__sym(), 14);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 4);
                     break;
                  case 3: // arFloat = new float[8];
                     assertEquals(tccode.op(), NEWARRAY_len);
                     assertEquals(tccode.newarray__regO(), 6);
                     assertEquals(tccode.newarray__sym(), 18);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 8);
                     break;
                  case 4: // arDbl = new double[16];
                     assertEquals(tccode.op(), NEWARRAY_len);
                     assertEquals(tccode.newarray__regO(), 8);
                     assertEquals(tccode.newarray__sym(), 19);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 16);
                     break;
                  case 5: // arByte = new byte[32];
                     assertEquals(tccode.op(), NEWARRAY_regI);
                     assertEquals(tccode.newarray__regO(), 10);
                     assertEquals(tccode.newarray__sym(), 13);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 1);
                     break;
                  case 6: // arShort = new short[64];
                     assertEquals(tccode.op(), NEWARRAY_regI);
                     assertEquals(tccode.newarray__regO(), 12);
                     assertEquals(tccode.newarray__sym(), 15);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 2);
                     break;
                  case 7: // arInt = new int[128];
                     assertEquals(tccode.op(), NEWARRAY_regI);
                     assertEquals(tccode.newarray__regO(), 14);
                     assertEquals(tccode.newarray__sym(), 16);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 3);
                     break;
                  case 8: // arLong = new long[256];
                     assertEquals(tccode.op(), NEWARRAY_regI);
                     assertEquals(tccode.newarray__regO(), 16);
                     assertEquals(tccode.newarray__sym(), 17);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 4);
                     break;
                  case 9: // arInt = new int[len];
                     assertEquals(tccode.op(), NEWARRAY_regI);
                     assertEquals(tccode.newarray__regO(), 18);
                     assertEquals(tccode.newarray__sym(), 16);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 5);
                     break;
                  case 10: // Object arObj1 = new Object[4];
                     assertEquals(tccode.op(), NEWARRAY_len);
                     assertEquals(tccode.newarray__regO(), 19);
                     assertEquals(tccode.newarray__sym(), 11);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 4);
                     break;
                  case 11: // BC187to189 arObj2 = new BC187to189[4];
                     assertEquals(tccode.op(), NEWARRAY_len);
                     assertEquals(tccode.newarray__regO(), 21);
                     assertEquals(tccode.newarray__sym(), 33);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 4);
                     break;
               }
               break;
            }
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC190() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC190.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case ARRAYLENGTH: //190
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC190.java
               switch (++n)
               {
                  case 1: // int arInt[] = new int[4]; int len = arInt.length;
                     assertEquals(tccode.op(), MOV_regI_arlen);
                     assertEquals(tccode.reg_ar__base(), 3);
                     assertEquals(tccode.reg_ar__reg(), 2);
                     break;
                  case 2: // arInt = new int[32]; len = arInt.length;
                     assertEquals(tccode.op(), MOV_regI_arlen);
                     assertEquals(tccode.reg_ar__base(), 3);
                     assertEquals(tccode.reg_ar__reg(), 4);
                     break;
                  case 3: // arInt = new int[len]; len = arInt.length;
                     assertEquals(tccode.op(), MOV_regI_arlen);
                     assertEquals(tccode.reg_ar__base(), 3);
                     assertEquals(tccode.reg_ar__reg(), 5);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC046to053() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC046to053.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IALOAD: //46
            case LALOAD: //47
            case FALOAD: //48
            case DALOAD: //49
            case AALOAD: //50
            case BALOAD: //51
            case CALOAD: //52
            case SALOAD: //53
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC046to053.java
               switch (++n)
               {
                  case 1: // boolean b1 = arBool[1];
                     assertEquals(tccode.op(), MOV_regI_arc);
                     assertEquals(tccode.reg_ar__reg(), 2);
                     assertEquals(tccode.reg_ar__base(), 3);
                     assertEquals(tccode.reg_ar__idx(), 1);
                     break;
                  case 2: // char c1 = arChar[1];
                     assertEquals(tccode.op(), MOV_regI_arc);
                     assertEquals(tccode.reg_ar__reg(), 5);
                     assertEquals(tccode.reg_ar__base(), 5);
                     assertEquals(tccode.reg_ar__idx(), 4);
                     break;
                  case 3: // byte B1 = arByte[1];
                     assertEquals(tccode.op(), MOV_regI_arc);
                     assertEquals(tccode.reg_ar__reg(), 8);
                     assertEquals(tccode.reg_ar__base(), 7);
                     assertEquals(tccode.reg_ar__idx(), 7);
                     break;
                  case 4: // short s1 = arShort[1];
                     assertEquals(tccode.op(), MOV_regI_arc);
                     assertEquals(tccode.reg_ar__reg(), 11);
                     assertEquals(tccode.reg_ar__base(), 9);
                     assertEquals(tccode.reg_ar__idx(), 10);
                     break;
                  case 5: // int i1 = arInt[0];
                     assertEquals(tccode.op(), MOV_regI_arc);
                     assertEquals(tccode.reg_ar__reg(), 14);
                     assertEquals(tccode.reg_ar__base(), 11);
                     assertEquals(tccode.reg_ar__idx(), 13);
                     break;
                  case 6: // i1 = arInt[3];
                     assertEquals(tccode.op(), MOV_regI_arc);
                     assertEquals(tccode.reg_ar__reg(), 17);
                     assertEquals(tccode.reg_ar__base(), 11);
                     assertEquals(tccode.reg_ar__idx(), 16);
                     break;
                  case 7: // i1 = arInt[i1];
                     assertEquals(tccode.op(), MOV_regI_arc);
                     assertEquals(tccode.reg_ar__reg(), 18);
                     assertEquals(tccode.reg_ar__base(), 11);
                     assertEquals(tccode.reg_ar__idx(), 15);
                     break;
                  case 8: // l1 = arLong[1];
                     assertEquals(tccode.op(), MOV_reg64_arc);
                     assertEquals(tccode.reg_ar__reg(), 1);
                     assertEquals(tccode.reg_ar__base(), 13);
                     assertEquals(tccode.reg_ar__idx(), 19);
                     break;
                  case 9: // f1 = arFloat[1];
                     assertEquals(tccode.op(), MOV_reg64_arc);
                     assertEquals(tccode.reg_ar__reg(), 3);
                     assertEquals(tccode.reg_ar__base(), 15);
                     assertEquals(tccode.reg_ar__idx(), 20);
                     break;
                  case 10: // d1 = arDbl[1];
                     assertEquals(tccode.op(), MOV_reg64_arc);
                     assertEquals(tccode.reg_ar__reg(), 5);
                     assertEquals(tccode.reg_ar__base(), 17);
                     assertEquals(tccode.reg_ar__idx(), 21);
                     break;
                  case 11: // o1 = arObj[1];
                     assertEquals(tccode.op(), MOV_regO_arc);
                     assertEquals(tccode.reg_ar__reg(), 20);
                     assertEquals(tccode.reg_ar__base(), 19);
                     assertEquals(tccode.reg_ar__idx(), 22);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC079to086() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC079to086.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case IASTORE: //79
            case FASTORE: //80
            case LASTORE: //81
            case DASTORE: //82
            case AASTORE: //83
            case BASTORE: //84
            case CASTORE: //85
            case SASTORE: //86
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC079to086.java
               switch (++n)
               {
                  case 1: // arBool[1] = b1;
                     assertEquals(tccode.op(), MOV_arc_regI);
                     assertEquals(tccode.reg_ar__reg(), 1);
                     assertEquals(tccode.reg_ar__base(), 4);
                     assertEquals(tccode.reg_ar__idx(), 6);
                     break;
                  case 2: // arChar[1] = c1;
                     assertEquals(tccode.op(), MOV_arc_regI);
                     assertEquals(tccode.reg_ar__reg(), 2);
                     assertEquals(tccode.reg_ar__base(), 6);
                     assertEquals(tccode.reg_ar__idx(), 7);
                     break;
                  case 3: // arByte[1] = B1;
                     assertEquals(tccode.op(), MOV_arc_regI);
                     assertEquals(tccode.reg_ar__reg(), 3);
                     assertEquals(tccode.reg_ar__base(), 8);
                     assertEquals(tccode.reg_ar__idx(), 8);
                     break;
                  case 4: // arShort[1] = s1;
                     assertEquals(tccode.op(), MOV_arc_regI);
                     assertEquals(tccode.reg_ar__reg(), 4);
                     assertEquals(tccode.reg_ar__base(), 10);
                     assertEquals(tccode.reg_ar__idx(), 9);
                     break;
                  case 5: // arInt[1] = i1;
                     assertEquals(tccode.op(), MOV_arc_regI);
                     assertEquals(tccode.reg_ar__reg(), 5);
                     assertEquals(tccode.reg_ar__base(), 12);
                     assertEquals(tccode.reg_ar__idx(), 10);
                     break;
                  case 6: // arLong[1] = l1;
                     assertEquals(tccode.op(), MOV_arc_reg64);
                     assertEquals(tccode.reg_ar__reg(), 1);
                     assertEquals(tccode.reg_ar__base(), 14);
                     assertEquals(tccode.reg_ar__idx(), 11);
                     break;
                  case 7: // arFloat[1] = f1;
                     assertEquals(tccode.op(), MOV_arc_reg64);
                     assertEquals(tccode.reg_ar__reg(), 2);
                     assertEquals(tccode.reg_ar__base(), 16);
                     assertEquals(tccode.reg_ar__idx(), 12);
                     break;
                  case 8: // arDbl[1] = d1;
                     assertEquals(tccode.op(), MOV_arc_reg64);
                     assertEquals(tccode.reg_ar__reg(), 3);
                     assertEquals(tccode.reg_ar__base(), 18);
                     assertEquals(tccode.reg_ar__idx(), 13);
                     break;
                  case 9: // arObj[1] = 01;
                     assertEquals(tccode.op(), MOV_arc_regO);
                     assertEquals(tccode.reg_ar__reg(), 2);
                     assertEquals(tccode.reg_ar__base(), 20);
                     assertEquals(tccode.reg_ar__idx(), 14);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC178to181() throws Exception  // this test is not completed
   {
      JavaCode jcode = startTest("BC178to181.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case GETSTATIC: //178
            case PUTSTATIC: //179
            case GETFIELD:  //180
            case PUTFIELD:  //181
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC178to181.java
               switch (++n)
               {
                  case 1: // boolean b1 = fb1;
                     assertEquals(tccode.op(), MOV_regI_field);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 1);
                     break;
                  case 2: // char c1 = fc1;
                     assertEquals(tccode.op(), MOV_regI_field);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__reg(), 3);
                     break;
                  case 3: // byte B1 = fB1;
                     assertEquals(tccode.op(), MOV_regI_field);
                     assertEquals(tccode.field_reg__this(), 2);
                     assertEquals(tccode.field_reg__reg(), 5);
                     break;
                  case 4: // short s1 = fs1;
                     assertEquals(tccode.op(), MOV_regI_field);
                     assertEquals(tccode.field_reg__this(), 3);
                     assertEquals(tccode.field_reg__reg(), 7);
                     break;
                  case 5: // int i1 = fi1;
                     assertEquals(tccode.op(), MOV_regI_field);
                     assertEquals(tccode.field_reg__this(), 4);
                     assertEquals(tccode.field_reg__reg(), 9);
                     break;
                  case 6: // long l1 = fl1;
                     assertEquals(tccode.op(), MOV_reg64_field);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 1);
                     break;
                  case 7: // float f1 = ff1;
                     assertEquals(tccode.op(), MOV_reg64_field);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__reg(), 3);
                     break;
                  case 8: // double d1 = fd1;
                     assertEquals(tccode.op(), MOV_reg64_field);
                     assertEquals(tccode.field_reg__this(), 2);
                     assertEquals(tccode.field_reg__reg(), 5);
                     break;
                  case 9: // Object o1 = fo1;
                     assertEquals(tccode.op(), MOV_regO_field);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 2);
                     break;
                  case 10: // fb1 = b1;
                     assertEquals(tccode.op(), MOV_field_regI);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 2);
                     break;
                  case 11: // fc1 = c1;
                     assertEquals(tccode.op(), MOV_field_regI);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__reg(), 4);
                     break;
                  case 12: // fB1 = B1;
                     assertEquals(tccode.op(), MOV_field_regI);
                     assertEquals(tccode.field_reg__this(), 2);
                     assertEquals(tccode.field_reg__reg(), 6);
                     break;
                  case 13: // fs1 = s1;
                     assertEquals(tccode.op(), MOV_field_regI);
                     assertEquals(tccode.field_reg__this(), 3);
                     assertEquals(tccode.field_reg__reg(), 8);
                     break;
                  case 14: // fi1 = i1;
                     assertEquals(tccode.op(), MOV_field_regI);
                     assertEquals(tccode.field_reg__this(), 4);
                     assertEquals(tccode.field_reg__reg(), 10);
                     break;
                  case 15: // fl1 = l1;
                     assertEquals(tccode.op(), MOV_field_reg64);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 2);
                     break;
                  case 16: // ff1 = f1;
                     assertEquals(tccode.op(), MOV_field_reg64);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__reg(), 4);
                     break;
                  case 17: // fd1 = d1;
                     assertEquals(tccode.op(), MOV_field_reg64);
                     assertEquals(tccode.field_reg__this(), 2);
                     assertEquals(tccode.field_reg__reg(), 6);
                     break;
                  case 18: // fo1 = o1;
                     assertEquals(tccode.op(), MOV_field_regO);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 3);
                     break;
                  case 19: // b1 = static_b1;
                     assertEquals(tccode.op(), MOV_regI_static);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 11);
                     break;
                  case 20: // c1 = static_c1;
                     assertEquals(tccode.op(), MOV_regI_static);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__reg(), 12);
                     break;
                  case 21: // B1 = static_B1;
                     assertEquals(tccode.op(), MOV_regI_static);
                     assertEquals(tccode.field_reg__this(), 2);
                     assertEquals(tccode.field_reg__reg(), 13);
                     break;
                  case 22: // s1 = static_s1;
                     assertEquals(tccode.op(), MOV_regI_static);
                     assertEquals(tccode.field_reg__this(), 3);
                     assertEquals(tccode.field_reg__reg(), 14);
                     break;
                  case 23: // i1 = static_i1;
                     assertEquals(tccode.op(), MOV_regI_static);
                     assertEquals(tccode.field_reg__this(), 4);
                     assertEquals(tccode.field_reg__reg(), 15);
                     break;
                  case 24: // l1 = static_l1;
                     assertEquals(tccode.op(), MOV_reg64_static);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 7);
                     break;
                  case 25: // f1 = static_f1;
                     assertEquals(tccode.op(), MOV_reg64_static);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__reg(), 8);
                     break;
                  case 26: // d1 = static_d1;
                     assertEquals(tccode.op(), MOV_reg64_static);
                     assertEquals(tccode.field_reg__this(), 2);
                     assertEquals(tccode.field_reg__reg(), 9);
                     break;
                  case 27: // o1 = static_o1;
                     assertEquals(tccode.op(), MOV_regO_static);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 4);
                     break;
                  case 28: // static_b1 = b1;
                     assertEquals(tccode.op(), MOV_static_regI);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 2);
                     break;
                  case 29: // static_c1 = c1;
                     assertEquals(tccode.op(), MOV_static_regI);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__reg(), 4);
                     break;
                  case 30: // static_B1 = B1;
                     assertEquals(tccode.op(), MOV_static_regI);
                     assertEquals(tccode.field_reg__this(), 2);
                     assertEquals(tccode.field_reg__reg(), 6);
                     break;
                  case 31: // static_s1 = s1;
                     assertEquals(tccode.op(), MOV_static_regI);
                     assertEquals(tccode.field_reg__this(), 3);
                     assertEquals(tccode.field_reg__reg(), 8);
                     break;
                  case 32: // static_i1 = i1;
                     assertEquals(tccode.op(), MOV_static_regI);
                     assertEquals(tccode.field_reg__this(), 4);
                     assertEquals(tccode.field_reg__reg(), 10);
                     break;
                  case 33: // static_l1 = l1;
                     assertEquals(tccode.op(), MOV_static_reg64);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 2);
                     break;
                  case 34: // static_f1 = f1;
                     assertEquals(tccode.op(), MOV_static_reg64);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__reg(), 4);
                     break;
                  case 35: // static_d1 = d1;
                     assertEquals(tccode.op(), MOV_static_reg64);
                     assertEquals(tccode.field_reg__this(), 2);
                     assertEquals(tccode.field_reg__reg(), 6);
                     break;
                  case 36: // static_o1 = o1;
                     assertEquals(tccode.op(), MOV_static_regO);
                     assertEquals(tccode.field_reg__this(), 0);
                     assertEquals(tccode.field_reg__reg(), 3);
                     break;
                  case 37: // i1 = super.ext_fi1;
                     assertEquals(tccode.op(), MOV_regI_field);
                     assertEquals(tccode.field_reg__reg(), 16);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__sym(), 1);
                     break;
                  case 38: // l1 = super.ext_fl1;
                     assertEquals(tccode.op(), MOV_reg64_field);
                     assertEquals(tccode.field_reg__reg(), 10);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__sym(), 2);
                     break;
                  case 39: // d1 = super.ext_fd1;
                     assertEquals(tccode.op(), MOV_reg64_field);
                     assertEquals(tccode.field_reg__reg(), 11);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__sym(), 3);
                     break;
                  case 40: // o1 = super.ext_fo1;
                     assertEquals(tccode.op(), MOV_regO_field);
                     assertEquals(tccode.field_reg__reg(), 5);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__sym(), 4);
                     break;
                  case 41: // ext_fi1 = i1;
                     assertEquals(tccode.op(), MOV_field_regI);
                     assertEquals(tccode.field_reg__reg(), 10);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__sym(), 5);
                     break;
                  case 42: // ext_fl1 = l1;
                     assertEquals(tccode.op(), MOV_field_reg64);
                     assertEquals(tccode.field_reg__reg(), 2);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__sym(), 6);
                     break;
                  case 43: // ext_fd1 = d1;
                     assertEquals(tccode.op(), MOV_field_reg64);
                     assertEquals(tccode.field_reg__reg(), 6);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__sym(), 7);
                     break;
                  case 44: // ext_fo1 = o1;
                     assertEquals(tccode.op(), MOV_field_regO);
                     assertEquals(tccode.field_reg__reg(), 3);
                     assertEquals(tccode.field_reg__this(), 1);
                     assertEquals(tccode.field_reg__sym(), 8);
                     break;
                  case 45: // i1 = thisObj.fi1;
                     assertEquals(tccode.op(), MOV_regI_field);
                     assertEquals(tccode.field_reg__reg(), 17);
                     assertEquals(tccode.field_reg__this(), 7);
                     assertEquals(tccode.field_reg__sym(), 9);
                     break;
                  case 46: // thisObj.fd1 = d1;
                     assertEquals(tccode.op(), MOV_field_reg64);
                     assertEquals(tccode.field_reg__reg(), 6);
                     assertEquals(tccode.field_reg__this(), 7);
                     assertEquals(tccode.field_reg__sym(), 10);
                     break;
                  case 47: // i1 = field2.fi1;
                     assertEquals(tccode.op(), MOV_regI_static);
                     assertEquals(tccode.static_reg__reg(), 18);
                     assertEquals(tccode.static_reg__sym(), 1);
                     break;
                  case 48: // l1 = field2.fl1;
                     assertEquals(tccode.op(), MOV_reg64_static);
                     assertEquals(tccode.static_reg__reg(), 12);
                     assertEquals(tccode.static_reg__sym(), 2);
                     break;
                  case 49: // d1 = field2.fd1;
                     assertEquals(tccode.op(), MOV_reg64_static);
                     assertEquals(tccode.static_reg__reg(), 13);
                     assertEquals(tccode.static_reg__sym(), 3);
                     break;
                  case 50: // o1 = field2.fo1;
                     assertEquals(tccode.op(), MOV_regO_static);
                     assertEquals(tccode.static_reg__reg(), 8);
                     assertEquals(tccode.static_reg__sym(), 4);
                     break;
                  case 51: // field2.fi1 = i1;
                     assertEquals(tccode.op(), MOV_static_regI);
                     assertEquals(tccode.static_reg__reg(), 10);
                     assertEquals(tccode.static_reg__sym(), 1);
                     break;
                  case 52: // field2.fl1 = l1;
                     assertEquals(tccode.op(), MOV_static_reg64);
                     assertEquals(tccode.static_reg__reg(), 14);
                     assertEquals(tccode.static_reg__sym(), 2);
                     break;
                  case 53: // field2.fd1 = d1;
                     assertEquals(tccode.op(), MOV_static_reg64);
                     assertEquals(tccode.static_reg__reg(), 6);
                     assertEquals(tccode.static_reg__sym(), 3);
                     break;
                  case 54: // field2.fo1 = o1;
                     assertEquals(tccode.op(), MOV_static_regO);
                     assertEquals(tccode.static_reg__reg(), 3);
                     assertEquals(tccode.static_reg__sym(), 4);
                     break;
               }
               break;
            }
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC197() throws Exception
   {
      JavaCode jcode = startTest("BC197.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case MULTIANEWARRAY: //197
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC197.java
               switch (++n)
               {
                  case 1: // boolean v1[][] = new boolean[1][2];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 2);
                     assertEquals(tccode.newarray__sym(), 34);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 2);
                     TCCode t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     break;
                  case 2: // int v2[][][] = new int[1][2][3];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 4);
                     assertEquals(tccode.newarray__sym(), 35);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 3);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     assertEquals(t.dims__dim3()-64, 3);
                     break;
                  case 3: // int v3[][][][] = new int[1][2][3][4];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 6);
                     assertEquals(tccode.newarray__sym(), 36);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 4);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     assertEquals(t.dims__dim3()-64, 3);
                     assertEquals(t.dims__dim4()-64, 4);
                     break;
                  case 4: // long v4[][][][][] = new long[1][2][3][4][5];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 8);
                     assertEquals(tccode.newarray__sym(), 37);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 5);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-2];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     assertEquals(t.dims__dim3()-64, 3);
                     assertEquals(t.dims__dim4()-64, 4);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 5);
                     break;
                  case 5: // double v5[][][][][][][][][] = new long[1][2][3][4][5][6][7][8][9];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 10);
                     assertEquals(tccode.newarray__sym(), 38);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 9);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-3];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     assertEquals(t.dims__dim3()-64, 3);
                     assertEquals(t.dims__dim4()-64, 4);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-2];
                     assertEquals(t.dims__dim1()-64, 5);
                     assertEquals(t.dims__dim2()-64, 6);
                     assertEquals(t.dims__dim3()-64, 7);
                     assertEquals(t.dims__dim4()-64, 8);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 9);
                     break;
                  case 6: // Object v6[][] = new Object[1][2];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 12);
                     assertEquals(tccode.newarray__sym(), 39);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 2);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     break;
                  case 7: // BC197 v7[][] = new BC197[1][2];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 14);
                     assertEquals(tccode.newarray__sym(), 40);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 2);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     break;
                  case 8: // int v8[][][] = new int[193][191][192];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 16);
                     assertEquals(tccode.newarray__sym(), 35);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 3);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1(), 1); // register
                     assertEquals(t.dims__dim2()-64, 191);
                     assertEquals(t.dims__dim3(), 2); // register
                     break;
                  case 9: // long v9[][][][][] = new long[1][2][][][];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 18);
                     assertEquals(tccode.newarray__sym(), 37);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 2);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     break;
                  case 10: // v9[1][1] = new long[1][2][3];
                     assertEquals(tccode.op(), NEWARRAY_multi);
                     assertEquals(tccode.newarray__regO(), 21);
                     assertEquals(tccode.newarray__sym(), 41);
                     assertEquals(tccode.newarray__lenOrRegIOrDims(), 3);
                     t = (TCCode) vcodeTest.items[vcodeTest.size()-1];
                     assertEquals(t.dims__dim1()-64, 1);
                     assertEquals(t.dims__dim2()-64, 2);
                     assertEquals(t.dims__dim3()-64, 3);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC193() throws Exception
   {
      JavaCode jcode = startTest("BC193.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case JINSTANCEOF: //193
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC193.java
               switch (++n)
               {
                  case 1: // c1 instanceof BC193
                     assertEquals(tccode.op(), INSTANCEOF);
                     assertEquals(tccode.instanceof__regI(), 1);
                     assertEquals(tccode.instanceof__regO(), 3);
                     assertEquals(tccode.instanceof__sym(), 33);
                     break;
                  case 2: // c2 instanceof BC193_A
                     assertEquals(tccode.op(), INSTANCEOF);
                     assertEquals(tccode.instanceof__regI(), 2);
                     assertEquals(tccode.instanceof__regO(), 5);
                     assertEquals(tccode.instanceof__sym(), 35);
                     break;
                  case 3: // c3 instanceof BC193
                     assertEquals(tccode.op(), INSTANCEOF);
                     assertEquals(tccode.instanceof__regI(), 3);
                     assertEquals(tccode.instanceof__regO(), 7);
                     assertEquals(tccode.instanceof__sym(), 33);
                     break;
                  case 4: // c4 instanceof BC193_B
                     assertEquals(tccode.op(), INSTANCEOF);
                     assertEquals(tccode.instanceof__regI(), 4);
                     assertEquals(tccode.instanceof__regO(), 9);
                     assertEquals(tccode.instanceof__sym(), 36);
                     break;
                  case 5: // c4 instanceof BC193_A
                     assertEquals(tccode.op(), INSTANCEOF);
                     assertEquals(tccode.instanceof__regI(), 5);
                     assertEquals(tccode.instanceof__regO(), 9);
                     assertEquals(tccode.instanceof__sym(), 35);
                     break;
                  case 6: // c1 instanceof BC193_C
                     assertEquals(tccode.op(), INSTANCEOF);
                     assertEquals(tccode.instanceof__regI(), 6);
                     assertEquals(tccode.instanceof__regO(), 3);
                     assertEquals(tccode.instanceof__sym(), 34);
                     break;
  
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC187() throws Exception
   {
      JavaCode jcode = startTest("BC187.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            case NEW: //187
            {
               TCCode tccode = Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
               // see file BC187.java
               switch (++n)
               {
                  case 1: // BC187 c1 = new BC187();
                     assertEquals(tccode.op(), NEWOBJ);
                     assertEquals(tccode.reg_sym__reg(), 2);
                     assertEquals(tccode.reg_sym__sym(), 33);
                     break;
                  case 2: // BC187_A c2 = new BC187_A();
                     assertEquals(tccode.op(), NEWOBJ);
                     assertEquals(tccode.reg_sym__reg(), 4);
                     assertEquals(tccode.reg_sym__sym(), 34);
                     break;
               }
               break;
            }
  
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC159to166() throws Exception
   {
      JavaCode jcode = startTest("BC159to166.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         Bytecode2TCCode.convert(bc, bc[j], stack, vcodeTest, false);
      }
  
      Bytecode2TCCode.updateBranchs(vcodeTest);
  
      for (int j = 0; j < vcodeTest.size(); j++)
      {
         TCCode tccode = (TCCode) vcodeTest.items[j];
         int op = tccode.op();
         switch (op)
         {
            case JEQ_regO_regO:
            case JNE_regO_regO:
            case JEQ_regO_null:
            case JNE_regO_null:
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
            case JGE_regD_regD:
            case JEQ_regI_s6:
            case JNE_regI_s6:
            case JLT_regI_s6:
            case JLE_regI_s6:
            case JGT_regI_s6:
            case JGE_regI_s6:
            case JEQ_regI_sym:
            case JNE_regI_sym:
            case JGE_regI_arlen:
            case JUMP_s24:
            {
               switch (++n)
               {
                  case 1: // if (a == b)
                     assertEquals(tccode.op(), JNE_regI_regI);
                     assertEquals(tccode.reg_reg_s12__s12(), 4);
                     TCCode tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.op(), ADD_regI_regI_regI);
                     tc2 = (TCCode) vcodeTest.items[j + tccode.reg_reg_s12__s12()];
                     assertEquals(tc2.op(), SUB_regI_regI_regI);
                     break;
                  case 2: // goto exit of "if (a == b)"
                     tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.op(), SUB_regI_regI_regI);
                     tc2 = (TCCode) vcodeTest.items[j + tccode.s24__desloc()];
                     assertEquals(tc2.op(), MUL_regI_regI_regI);
                     break;
               }
            }
         }
      }
   }
  
   private void BC148to158() throws Exception
   {
      JavaCode jcode = startTest("BC148to158.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         Bytecode2TCCode.convert(bc, bc[j], stack, vcodeTest, false);
      }
  
      Bytecode2TCCode.updateBranchs(vcodeTest);
  
      for (int j = 0; j < vcodeTest.size(); j++)
      {
         TCCode tccode = (TCCode) vcodeTest.items[j];
         int op = tccode.op();
         switch (op)
         {
            case JEQ_regO_regO:
            case JNE_regO_regO:
            case JEQ_regO_null:
            case JNE_regO_null:
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
            case JGE_regD_regD:
            case JEQ_regI_s6:
            case JNE_regI_s6:
            case JLT_regI_s6:
            case JLE_regI_s6:
            case JGT_regI_s6:
            case JGE_regI_s6:
            case JEQ_regI_sym:
            case JNE_regI_sym:
            case JGE_regI_arlen:
            case JUMP_s24:
            {
               switch (++n)
               {
                  case 1: // if (a == b)
                     assertEquals(tccode.op(), JNE_regL_regL);
                     assertEquals(tccode.reg_reg_s12__s12(), 4);
                     TCCode tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.op(), ADD_regL_regL_regL);
                     tc2 = (TCCode) vcodeTest.items[j + tccode.reg_reg_s12__s12()];
                     assertEquals(tc2.op(), SUB_regL_regL_regL);
                     break;
                  case 2: // goto exit of "if (a == b)"
                     tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.op(), SUB_regL_regL_regL);
                     tc2 = (TCCode) vcodeTest.items[j + tccode.s24__desloc()];
                     assertEquals(tc2.op(), MUL_regL_regL_regL);
                     break;
               }
            }
         }
      }
   }
  
   private void BC198to199() throws Exception
   {
      JavaCode jcode = startTest("BC198to199.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      int n = 0;
      for (int j = 0; j < bc.length; j++)
      {
         Bytecode2TCCode.convert(bc, bc[j], stack, vcodeTest, false);
      }
  
      Bytecode2TCCode.updateBranchs(vcodeTest);
  
      for (int j = 0; j < vcodeTest.size(); j++)
      {
         TCCode tccode = (TCCode) vcodeTest.items[j];
         int op = tccode.op();
         switch (op)
         {
            case JEQ_regO_null:
            case JNE_regO_null:
            case JUMP_s24:
            {
               switch (++n)
               {
                  case 1: // if (o1 == null)
                     assertEquals(tccode.op(), JNE_regO_null);
                     assertEquals(tccode.reg_reg_s12__s12(), 2);
                     TCCode tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.op(), MOV_regO_null);
                     assertEquals(tc2.s18_reg__reg(), 3);
                     assertEquals(tc2.s18_reg__s18(), 0);
                     break;
                  case 2: // if (o1 != null)
                     assertEquals(tccode.op(), JEQ_regO_null);
                     assertEquals(tccode.reg_reg_s12__s12(), 2);
                     tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.op(), MOV_regO_null);
                     assertEquals(tc2.s18_reg__reg(), 4);
                     assertEquals(tc2.s18_reg__s18(), 0);
                     break;
               }
            }
         }
      }
   }
  
   private void BC168to169() throws Exception
   {
      JavaCode jcode = startTest("BC168to169.class")[0].code;
      ByteCode[] bc = jcode.bcs;
      OperandStack stack = new OperandStack(100);
      for (int j = 0; j < bc.length; j++)
      {
         ByteCode i = bc[j];
         int op = i.bc;
         switch (op)
         {
            default: Bytecode2TCCode.convert(bc, i, stack, vcodeTest, false);
         }
      }
   }
  
   private void BC172to177() throws Exception
   {
      JavaMethod[] jm = startTest("BC172to177.class");
  
      for (int k=0; k<jm.length; k++)
      {
         ByteCode[] bc = jm[k].code.bcs;
         OperandStack stack = new OperandStack(100);
         OperandReg.init(jm[k].params, jm[k].isStatic);
         for (int j = 0; j < bc.length; j++)
         {
            Bytecode2TCCode.convert(bc, bc[j], stack, vcodeTest, false);
         }
      }
  
      int n = 0;
      for (int j = 0; j < vcodeTest.size(); j++)
      {
         TCCode tccode = (TCCode) vcodeTest.items[j];
         int op = tccode.op();
         switch (op)
         {
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
            case RETURN_symL:
            {
               switch (++n)
               {
                  case 1:
                     assertEquals(tccode.op(), RETURN_void);
                     break;
                  case 2:
                     assertEquals(tccode.op(), RETURN_void);
                     break;
                  case 3:
                     assertEquals(tccode.op(), RETURN_regI);
                     break;
                  case 4:
                     assertEquals(tccode.op(), RETURN_regO);
                     break;
                  case 5:
                     assertEquals(tccode.op(), RETURN_regO);
                     break;
                  case 6:
                     assertEquals(tccode.op(), RETURN_reg64);
                     break;
                  case 7:
                     assertEquals(tccode.op(), RETURN_reg64);
                     break;
                  case 8:
                     assertEquals(tccode.op(), RETURN_reg64);
                     break;
                  case 9:
                     assertEquals(tccode.op(), RETURN_regI);
                     break;
                  case 10:
                     assertEquals(tccode.op(), RETURN_regI);
                     break;
                  case 11:
                     assertEquals(tccode.op(), RETURN_regI);
                     break;
                  case 12:
                     assertEquals(tccode.op(), RETURN_s24I);
                     break;
                  case 13:
                     assertEquals(tccode.op(), RETURN_s24D);
                     break;
                  case 14:
                     assertEquals(tccode.op(), RETURN_null);
                     break;
               }
            }
         }
      }
   }
  
   private void BC182to183() throws Exception
   {
      JavaMethod[] jm = startTest("BC182to183.class");
  
      for (int k=0; k<jm.length; k++)
      {
         ByteCode[] bc = jm[k].code.bcs;
         OperandStack stack = new OperandStack(100);
         OperandReg.init(jm[k].params, jm[k].isStatic);
         for (int j = 0; j < bc.length; j++)
         {
            Bytecode2TCCode.convert(bc, bc[j], stack, vcodeTest, false);
         }
      }
  
      int n = 0;
      for (int j = 0; j < vcodeTest.size(); j++)
      {
         TCCode tccode = (TCCode) vcodeTest.items[j];
         int op = tccode.op();
         switch (op)
         {
            case CALL_normal:
            case CALL_virtual:
            {
               switch (++n)
               {
                  case 1: // thisClass = new BC182to183();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 1);
                     assertEquals(tccode.mtd__this(), 2);
                     break;
                  case 2: // thisClass = new BC182to183(1, 2, 3);
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 2);
                     assertEquals(tccode.mtd__this(), 4);
                     assertEquals(tccode.mtd__retOrParam(), 1);
                     TCCode tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.params__param1(), 2);
                     assertEquals(tc2.params__param2(), 3);
                     break;
                  case 3: // thisClass = new BC182to183(1, 2, 3, 4, 5, 6);
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 3);
                     assertEquals(tccode.mtd__this(), 5);
                     assertEquals(tccode.mtd__retOrParam(), 4);
                     tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.params__param1(), 5);
                     assertEquals(tc2.params__param2(), 6);
                     assertEquals(tc2.params__param3(), 7);
                     assertEquals(tc2.params__param4(), 8);
                     tc2 = (TCCode) vcodeTest.items[j + 2];
                     assertEquals(tc2.params__param1(), 9);
                     break;
                  case 4: // superclass1 = new BC182to183();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 1);
                     assertEquals(tccode.mtd__this(), 6);
                     break;
                  case 5: // superclass2 = new BC182to183();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 1);
                     assertEquals(tccode.mtd__this(), 8);
                     break;
                  case 6: // superclass2 = new A();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 10);
                     break;
                  case 7: // superclass1 = new A();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 11);
                     break;
                  case 8: // superclass2 = new B();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 5);
                     assertEquals(tccode.mtd__this(), 12);
                     break;
                  case 9: // m1(1, 2, 3, 4);
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 1);
                     assertEquals(tccode.mtd__retOrParam(), 10);
                     tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.params__param1(), 11);
                     assertEquals(tc2.params__param2(), 12);
                     assertEquals(tc2.params__param3(), 13);
                     break;
                  case 10: // int x = m2(1, 2);
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 5);
                     assertEquals(tccode.mtd__this(), 1);
                     assertEquals(tccode.mtd__retOrParam(), 14);
                     tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.params__param1(), 15);
                     assertEquals(tc2.params__param2(), 16);
                     break;
                  case 11: // overloading();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 7);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 12: // ma1();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 6);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 13: // mb1();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 7);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 14: // super.overloading();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 8);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 15: // ma2(1);
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 9);
                     assertEquals(tccode.mtd__this(), 1);
                     assertEquals(tccode.mtd__retOrParam(), 18);
                     break;
                  case 16: // ma3(1, 2, 3, 4);
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 10);
                     assertEquals(tccode.mtd__this(), 1);
                     assertEquals(tccode.mtd__retOrParam(), 19);
                     tc2 = (TCCode) vcodeTest.items[j + 1];
                     assertEquals(tc2.params__param1(), 20);
                     assertEquals(tc2.params__param2(), 21);
                     assertEquals(tc2.params__param3(), 22);
                     break;
                  case 17: // thisClass.m3();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 11);
                     assertEquals(tccode.mtd__this(), 3);
                     break;
               }
            }
         }
      }
   }
  
   private void BC184() throws Exception
   {
      JavaMethod[] jm = startTest("BC184.class");
  
      for (int k=0; k<jm.length; k++)
      {
         ByteCode[] bc = jm[k].code.bcs;
         OperandStack stack = new OperandStack(100);
         OperandReg.init(jm[k].params, jm[k].isStatic);
         for (int j = 0; j < bc.length; j++)
         {
            Bytecode2TCCode.convert(bc, bc[j], stack, vcodeTest, false);
         }
      }
  
      int n = 0;
      for (int j = 0; j < vcodeTest.size(); j++)
      {
         TCCode tccode = (TCCode) vcodeTest.items[j];
         int op = tccode.op();
         switch (op)
         {
            case CALL_normal:
            {
               switch (++n)
               {
                  // The first three can be ignored.
                  case 4: // sm1();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 2);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 5: // this.sm2();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 3);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 6: // smOverloading();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 7: // this.smOverloading();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 8: // BC184.smOverloading();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 9: // thisClass.smOverloading();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 1);
                     break;
                  case 10: // super.smA1();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 3);
                     break;
                  case 11: // smA2();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     break;
                  case 12: // super.smOverloading();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 5);
                     break;
                  case 13: // this.smA2();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     break;
                  case 14: // superClass.smA1();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 3);
                     break;
                  case 15: // BC184_A.smA2();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 6);
                     break;
                  case 16: // BC184_A.smOverloading();
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 5);
                     break;
               }
            }
         }
      }
   }
  
   private void BC185() throws Exception
   {
      JavaMethod[] jm = startTest("BC185.class");
  
      for (int k=0; k<jm.length; k++)
      {
         ByteCode[] bc = jm[k].code.bcs;
         OperandStack stack = new OperandStack(100);
         OperandReg.init(jm[k].params, jm[k].isStatic);
         for (int j = 0; j < bc.length; j++)
         {
            Bytecode2TCCode.convert(bc, bc[j], stack, vcodeTest, false);
         }
      }
  
      int n = 0;
      for (int j = 0; j < vcodeTest.size(); j++)
      {
         TCCode tccode = (TCCode) vcodeTest.items[j];
         int op = tccode.op();
         switch (op)
         {
            case CALL_normal:
            {
               switch (++n)
               {
                  // The first four CALL_normal can be ignored.
                  case 5: // superInterface.Overloading(x);
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 5);
                     assertEquals(tccode.mtd__retOrParam(), 1);
                     break;
                  case 6: // superInterface.Overloading(x) [**** again - intentionally ****];
                     assertEquals(tccode.op(), CALL_normal);
                     assertEquals(tccode.mtd__sym(), 4);
                     assertEquals(tccode.mtd__this(), 5);
                     assertEquals(tccode.mtd__retOrParam(), 1);
                     break;
               }
            }
         }
      }
   }
  
   private void BC170() throws Exception
   {
      JavaMethod[] jm = startTest("BC170.class");
   }
  
   private void BC171() throws Exception
   {
      JavaMethod[] jm = startTest("BC171.class");
   }
  
   public void testRun()
   {
      try
      {
         // complexity 1
         BC001();
         BC002to015();
         BC016to017();
         BC018to020();
         BC021_026to029();
         BC022_030to033();
         BC023_034to037();
         BC024_038to041();
         BC025_042to045();
         BC087to095();
  
         // complexity 2
         BC054_059to062();
         BC055_063to066();
         BC056_067to070();
         BC057_071to074();
         BC058_075to078();
         BC096to099();
         BC100to103();
         BC104to107();
         BC108to111();
         BC112to115();
         BC116to119();
         BC120to125();
         BC126to131();
         BC132();
         BC133to147();
         BC190();
  
         // complexity 3
         BC187to189();
         BC046to053();
         BC079to086();
         BC178to181();
         BC187();
         BC193();
         BC197();
         BC148to158();
         BC159to166();
         BC198to199();
  
         // complexity 4
         BC168to169();
         BC172to177();
  
         // complexity 5
         BC182to183();
         BC184();
         BC185();
         BC170();
         BC171();
      }
      catch (AssertionFailedError afe)
      {
         throw afe;
      }
      catch (Exception e)
      {
         if (!totalcross.sys.Settings.onDevice)
            e.printStackTrace();
         fail(e+"");
      }
   }*/
}
