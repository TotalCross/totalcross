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
package tc.tools.converter.tclass;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.TCConstants;
import totalcross.util.IntVector;

public final class TCCode implements TCConstants {
  public int val;
  public int len = 1; // method, switch and newarray_multi parameters must have len > 1
  public int line; // line number in the original source file.
  private static final int T_u32 = 1;
  private static final int T_i32 = 2;
  private static final int T_x24 = 3;
  private static final int T_x16x16 = 4;
  private static final int T_x8x16 = 5;
  private static final int T_x8x8x8 = 6;
  private static final int T_x12x6x6 = 7;
  private static final int T_x6x18 = 8;
  private static final int T_x6x6x12 = 9;
  private static final int T_x6xS6x12 = 10;
  private static final int T_x6x6x6x6 = 11;

  private int type;
  static int printAsX8x8x8x8Count;
  static int printSwitchDefaultAddress;
  static int printSwitchKeyCount;
  static int printSwitchTargetAddressCount;

  public TCCode(int line) {
    this.line = line;
  }

  public TCCode(int v, int line) {
    op(v);
    this.line = line;
  }

  private static StringBuffer sbt = new StringBuffer(50);

  @Override
  public String toString() {
    try {
      sbt.setLength(0);
      if (printAsX8x8x8x8Count > 0) {
        printAsX8x8x8x8Count--;
        sbt.append(op()).append(",").append(x8x8x8_1()).append(",").append(x8x8x8_2()).append(",").append(x8x8x8_3());
      } else if (printSwitchDefaultAddress == 1) {
        sbt.append(x16x16_1());
        printSwitchDefaultAddress = 0;
      } else if (printSwitchKeyCount > 0) {
        sbt.append(i32__i32());
        printSwitchKeyCount--;
      } else if (printSwitchTargetAddressCount > 0) {
        sbt.append(x16x16_1()).append(",").append(x16x16_2());
        printSwitchTargetAddressCount--;
      } else {
        sbt.append(TCConstants.bcTClassNames[op()]).append(" ");
        switch (type) {
        case T_u32:
          sbt.append(u32__u32());
          break;
        case T_i32:
          sbt.append(i32__i32());
          break;
        case T_x24:
          sbt.append(x24_1());
          break;
        case T_x16x16:
          sbt.append(x16x16_1()).append(",").append(x16x16_2());
          break;
        case T_x8x16:
          sbt.append(x8x16_1()).append(",").append(op() == INC_regI ? x8x16_s2() : x8x16_2());
          break;
        case T_x8x8x8:
          sbt.append(x8x8x8_1()).append(",").append(x8x8x8_2()).append(",").append(x8x8x8_3());
          break;
        case T_x12x6x6:
          sbt.append(x12x6x6_1()).append(",").append(x12x6x6_2()).append(",").append(x12x6x6_3());
          break;
        case T_x6x18:
          sbt.append(x6x18_1()).append(",").append(x6x18_2());
          break;
        case T_x6x6x12:
          sbt.append(x6x6x12_1()).append(",").append(x6x6x12_2()).append(",").append(x6x6x12_s3());
          break;
        case T_x6xS6x12:
          sbt.append(x6x6x12_1()).append(",").append(x6x6x12_s2()).append(",").append(x6x6x12_s3());
          break;
        case T_x6x6x6x6:
          sbt.append(x6x6x6x6_1()).append(",").append(x6x6x6x6_2()).append(",").append(x6x6x6x6_3()).append(",")
              .append(x6x6x6x6_4());
          break;
        }
        switch (op()) {
        case CALL_normal:
        case CALL_virtual: {
          int sym = mtd__sym();
          sbt.append(" ").append(GlobalConstantPool.getMtdName(sym));
          if (len > 1) {
            sbt.append(" - parameters are next ").append(len - 1).append(" instructions");
            printAsX8x8x8x8Count = len - 1;
          }
          break;
        }
        case NEWARRAY_multi:
          if (len > 1) {
            sbt.append(" - sizes are next ").append(len - 1).append(" instructions");
            printAsX8x8x8x8Count = len - 1;
          }
          break;
        case SWITCH: {
          sbt.append(" - Informations of Switch Table are next ").append(len - 1).append(" instructions");
          printSwitchDefaultAddress = 1;
          printSwitchKeyCount = x8x16_2();
          printSwitchTargetAddressCount = (printSwitchKeyCount + 1) / 2;
          break;
        }
        case MOV_field_regI:
        case MOV_field_regO:
        case MOV_field_reg64:
        case MOV_regI_field:
        case MOV_regO_field:
        case MOV_reg64_field: {
          int sym = field_reg__sym();
          int extf = GlobalConstantPool.getInstanceField(sym);
          int cl = (extf >> 16) & 0xFFFF;
          int fi = extf & 0xFFFF;
          sbt.append(" ").append(GlobalConstantPool.getClassName(cl)).append(".")
              .append(GlobalConstantPool.getMethodFieldName(fi));
          break;
        }
        case MOV_static_regI:
        case MOV_static_regO:
        case MOV_static_reg64:
        case MOV_regI_static:
        case MOV_regO_static:
        case MOV_reg64_static: {
          int sym = static_reg__sym();
          int extf = GlobalConstantPool.getStaticField(sym);
          int cl = (extf >> 16) & 0xFFFF;
          int fi = extf & 0xFFFF;
          sbt.append(" ").append(GlobalConstantPool.getClassName(cl)).append(".")
              .append(GlobalConstantPool.getMethodFieldName(fi));
          break;
        }
        }
      }
      sbt.append(" [" + line + "]");
      return sbt.toString();
    } catch (ArrayIndexOutOfBoundsException e) {
      return "";
    }
  }

  /** Adds the opcode to the given IntVector. If the opcode
   * has more than one int, this method must be overridden to
   * ensure that all ints are added.
   */
  public void addTo(IntVector iv) {
    iv.addElement(val);
  }

  // format: little endian
  // bit   31 .... .... .... .... .... .... .... .... 0
  // x12x6x6: cccc ccbb bbbb aaaa aaaa aaaa oooo oooo    (a = first 12 bits, b = 2nd 6 bits, c = 3rd 6 bits, o = opcode)
  /** Sets a signed/unsigned bit field.
   * @param v The value to store.
   * @param mask The mask representing the bits that will be used to store the value.
   * @param desloc How many shifts to right that makes the value go to bit 0.
   */
  private void set(int v, int mask, int desloc, int bits) {
    val = (val & ~mask) | ((v << desloc) & mask);
    // check if the number can fit in the available bits
    //int u = v & 0xFFFFFFFF; // make it unsigned
    //if (u >= (1 << bits))
    //   try {throw new Converter("Error! "+u+" can't fit in "+bits+" bits!");} catch (Exception e) {e.printStackTrace();}
  }

  /** Gets a signed value */
  private int gets(int mask, int leftDesloc, int rightDesloc) {
    int x = val << leftDesloc;
    return (x >> rightDesloc) & (mask >> rightDesloc);
  }

  private int gets(int mask, int desloc) {
    return (val >> desloc) & (mask >> desloc);
  }

  /** gets an unsigned value */
  private int getu(int mask, int desloc) {
    return (val >>> desloc) & (mask >> desloc);
  }

  // u32
  //   uint32 u32;
  public void u32__u32(int v) {
    val = v;
    type = T_u32;
  }

  public long u32__u32() {
    return ((long) val) & 0xFFFFFFFFL;
  }

  // i32
  //   int32 i32;
  public void i32__i32(int v) {
    val = v;
    type = T_i32;
  }

  public int i32__i32() {
    return val;
  }

  // all the methods below must use these ones to handle OP
  public void op(int v) {
    set(v, 0x000000FF, 0, 8);
  }

  public int op() {
    return getu(0x000000FF, 0);
  }

  // these are the basic methods, called by the others:
  // remember that they all have the first 8 bits as the opcode. so, 8 16 means 8 8 16
  // 24
  public void x24_1(int v) {
    set(v, 0xFFFFFF00, 8, 24);
    type = T_x24;
  }

  public int x24_1() {
    return gets(0xFFFFFF00, 8);
  }

  // 16 16
  public void x16x16_1(int v) {
    set(v, 0x0000FFFF, 0, 16);
    type = T_x16x16;
  }

  public void x16x16_2(int v) {
    set(v, 0xFFFF0000, 16, 16);
  }

  public int x16x16_1() {
    return gets(0x0000FFFF, 0);
  }

  public int x16x16_2() {
    return gets(0xFFFF0000, 16);
  }

  // 8 16
  public void x8x16_1(int v) {
    set(v, 0x0000FF00, 8, 8);
    type = T_x8x16;
  } // stored as x16x8 for better performance

  public void x8x16_2(int v) {
    set(v, 0xFFFF0000, 16, 16);
  }

  public int x8x16_1() {
    return getu(0x0000FF00, 8);
  }

  public int x8x16_2() {
    return getu(0xFFFF0000, 16);
  }

  public int x8x16_s2() {
    return gets(0xFFFF0000, 16);
  }

  // 8 8 8
  public void x8x8x8_1(int v) {
    set(v, 0x0000FF00, 8, 8);
    type = T_x8x8x8;
  }

  public void x8x8x8_2(int v) {
    set(v, 0x00FF0000, 16, 8);
  }

  public void x8x8x8_3(int v) {
    set(v, 0xFF000000, 24, 8);
  }

  public int x8x8x8_1() {
    return getu(0x0000FF00, 8);
  }

  public int x8x8x8_2() {
    return getu(0x00FF0000, 16);
  }

  public int x8x8x8_3() {
    return getu(0xFF000000, 24);
  }

  // 12 6 6
  public void x12x6x6_1(int v) {
    set(v, 0x000FFF00, 8, 12);
    type = T_x12x6x6;
  }

  public void x12x6x6_2(int v) {
    set(v, 0x03F00000, 20, 6);
  }

  public void x12x6x6_3(int v) {
    set(v, 0xFC000000, 26, 6);
  }

  public int x12x6x6_1() {
    return getu(0x000FFF00, 8);
  }

  public int x12x6x6_2() {
    return getu(0x03F00000, 20);
  }

  public int x12x6x6_3() {
    return getu(0xFC000000, 26);
  }

  public int x12x6x6_s3() {
    return gets(0xFC000000, 26);
  }

  // 6 18
  public void x6x18_1(int v) {
    set(v, 0x00003F00, 8, 6);
    type = T_x6x18;
  }

  public void x6x18_2(int v) {
    set(v, 0xFFFFC000, 14, 18);
  }

  public int x6x18_1() {
    return gets(0x00003F00, 8);
  }

  public int x6x18_2() {
    return gets(0xFFFFC000, 14);
  }

  // 6 6 12
  public void x6x6x12_1(int v) {
    set(v, 0x00003F00, 8, 6);
    type = T_x6x6x12;
  }

  public void x6x6x12_2(int v) {
    set(v, 0x000FC000, 14, 6);
  }

  public void x6x6x12_3(int v) {
    set(v, 0xFFF00000, 20, 12);
  }

  public int x6x6x12_1() {
    return getu(0x00003F00, 8);
  }

  public int x6x6x12_2() {
    return getu(0x000FC000, 14);
  }

  public int x6x6x12_3() {
    return getu(0xFFF00000, 20);
  }

  public int x6x6x12_s2() {
    return gets(0xFC000000, 12, 26);
  }

  public int x6x6x12_s3() {
    return gets(0xFFF00000, 20);
  }

  // 6 6 6 6
  public void x6x6x6x6_1(int v) {
    set(v, 0x0003F00, 8, 6);
    type = T_x6x6x6x6;
  }

  public void x6x6x6x6_2(int v) {
    set(v, 0x000FC000, 14, 6);
  }

  public void x6x6x6x6_3(int v) {
    set(v, 0x03F00000, 20, 6);
  }

  public void x6x6x6x6_4(int v) {
    set(v, 0xFC000000, 26, 6);
  }

  public int x6x6x6x6_1() {
    return getu(0x0003F00, 8);
  }

  public int x6x6x6x6_2() {
    return getu(0x000FC000, 14);
  }

  public int x6x6x6x6_3() {
    return getu(0x03F00000, 20);
  }

  public int x6x6x6x6_4() {
    return getu(0xFC000000, 26);
  }

  public int x6x6x6x6_s4() {
    return gets(0xFC000000, 26);
  }

  // s24:
  //     int32 desloc: 24;
  public void s24__desloc(int v) {
    x24_1(v);
  }

  public int s24__desloc() {
    return x24_1();
  }

  // op:
  //    uint32 op: 8;
  //    uint32 rest: 24;
  // use the op(v) and op()

  // inc:
  //    uint32 op: 8;
  //    uint32 reg: 8;
  //    int32 s16: 16; // inc value
  public void inc__reg(int v) {
    x8x16_1(v);
  }

  public void inc__s16(int v) {
    x8x16_2(v);
  }

  public int inc__reg() {
    return x8x16_1();
  }

  public int inc__s16() {
    return x8x16_s2();
  }

  // params:
  //   uint32 param1: 8;
  //   uint32 param2: 8;
  //   uint32 param3: 8;
  //   uint32 param4: 8;
  public void params__param1(int v) {
    op(v);
  }

  public void params__param2(int v) {
    x8x8x8_1(v);
  }

  public void params__param3(int v) {
    x8x8x8_2(v);
  }

  public void params__param4(int v) {
    x8x8x8_3(v);
  }

  public int params__param1() {
    return op();
  }

  public int params__param2() {
    return x8x8x8_1();
  }

  public int params__param3() {
    return x8x8x8_2();
  }

  public int params__param4() {
    return x8x8x8_3();
  }

  // reg_reg; // reg <-> reg
  //   uint32 op   : 8;
  //   uint32 reg0 : 8; // 6
  //   uint32 reg1 : 8; // 6
  public void reg_reg__reg0(int v) {
    x8x8x8_1(v);
  }

  public void reg_reg__reg1(int v) {
    x8x8x8_2(v);
  }

  public int reg_reg__reg0() {
    return x8x8x8_1();
  }

  public int reg_reg__reg1() {
    return x8x8x8_2();
  }

  // static_reg; // reg <-> sym[sym]
  //   uint32 op   : 8;
  //   uint32 reg  : 8; // 6
  //   uint32 sym  : SYM16; // cp.extRef - index in constant pool for the full definition of the field (class and name)
  public void static_reg__reg(int v) {
    x8x16_1(v);
  }

  public void static_reg__sym(int v) {
    x8x16_2(v);
  }

  public int static_reg__reg() {
    return x8x16_1();
  }

  public int static_reg__sym() {
    return x8x16_2();
  }

  //field_reg; // reg <-> regO[sym] (regO explicit)
  //   uint32 op   : 8;
  //   uint32 sym  : SYM12; // class.instanceFields
  //   uint32 this : 6;  // in which regO is the "this" for the external class
  //   uint32 reg  : 6;
  public void field_reg__sym(int v) {
    x12x6x6_1(v);
  }

  public void field_reg__this(int v) {
    x12x6x6_2(v);
  }

  public void field_reg__reg(int v) {
    x12x6x6_3(v);
  }

  public int field_reg__sym() {
    return x12x6x6_1();
  }

  public int field_reg__this() {
    return x12x6x6_2();
  }

  public int field_reg__reg() {
    return x12x6x6_3();
  }

  // mtd; // method call: the slots may be: the instance, the return value, the parameters
  //   uint32 op   : 8;
  //   uint32 sym  : SYM12; // class.methods
  //   uint32 this : 6;  // this instance, if any
  //   uint32 retOrParam : 6;  // may be the return reg or the first parameter
  public void mtd__sym(int v) {
    x12x6x6_1(v);
  }

  public void mtd__this(int v) {
    x12x6x6_2(v);
  }

  public void mtd__retOrParam(int v) {
    x12x6x6_3(v);
  }

  public int mtd__sym() {
    return x12x6x6_1();
  }

  public int mtd__this() {
    return x12x6x6_2();
  }

  public int mtd__retOrParam() {
    return x12x6x6_3();
  }

  // reg_ar; // reg <-> regO[regI]
  //   uint32 op   : 8;
  //   uint32 base : 8; // 6 - regO
  //   uint32 idx  : 8; // 6 - regI
  //   uint32 reg  : 8; // 6
  public void reg_ar__base(int v) {
    x8x8x8_1(v);
  }

  public void reg_ar__idx(int v) {
    x8x8x8_2(v);
  }

  public void reg_ar__reg(int v) {
    x8x8x8_3(v);
  }

  public int reg_ar__base() {
    return x8x8x8_1();
  }

  public int reg_ar__idx() {
    return x8x8x8_2();
  }

  public int reg_ar__reg() {
    return x8x8x8_3();
  }

  // reg_sym; // reg <-> sym
  //   uint32 op    : 8;
  //   uint32 reg   : 8; // 6
  //   uint32 sym   : SYM16; // cp.i32 / cp.obj / cp.dbl / cp.i64 / cp.identObj
  public void reg_sym__reg(int v) {
    x8x16_1(v);
  }

  public void reg_sym__sym(int v) {
    x8x16_2(v);
  }

  public int reg_sym__reg() {
    return x8x16_1();
  }

  public int reg_sym__sym() {
    return x8x16_2();
  }

  // s18_reg; // reg <- s18
  //   uint32 op    : 8;
  //   uint32 reg   : 6;
  //   int32 s18    : 18;
  public void s18_reg__reg(int v) {
    x6x18_1(v);
  }

  public void s18_reg__s18(int v) {
    x6x18_2(v);
  }

  public int s18_reg__reg() {
    return x6x18_1();
  }

  public int s18_reg__s18() {
    return x6x18_2();
  }

  // reg_reg_reg; // reg <- reg op reg
  //   uint32 op    : 8;
  //   uint32 reg0  : 8; // 6
  //   uint32 reg1  : 8; // 6
  //   uint32 reg2  : 8; // 6
  public void reg_reg_reg__reg0(int v) {
    x8x8x8_1(v);
  }

  public void reg_reg_reg__reg1(int v) {
    x8x8x8_2(v);
  }

  public void reg_reg_reg__reg2(int v) {
    x8x8x8_3(v);
  }

  public int reg_reg_reg__reg0() {
    return x8x8x8_1();
  }

  public int reg_reg_reg__reg1() {
    return x8x8x8_2();
  }

  public int reg_reg_reg__reg2() {
    return x8x8x8_3();
  }

  // reg_reg_s12; // reg <- reg op s12 (or s12 op reg)
  //   uint32 op    : 8;
  //   uint32 reg0  : 6;
  //   uint32 reg1  : 6;
  //   int32 s12    : 12; // signed values must go last!
  public void reg_reg_s12__reg0(int v) {
    x6x6x12_1(v);
  }

  public void reg_reg_s12__reg1(int v) {
    x6x6x12_2(v);
  }

  public void reg_reg_s12__s12(int v) {
    x6x6x12_3(v);
  }

  public int reg_reg_s12__reg0() {
    return x6x6x12_1();
  }

  public int reg_reg_s12__reg1() {
    return x6x6x12_2();
  }

  public int reg_reg_s12__s12() {
    return x6x6x12_s3();
  }

  // reg_s6_ar; // reg <-> base[idx] op s6
  //   uint32 op    : 8;
  //   uint32 reg   : 6;
  //   uint32 base  : 6; // regO
  //   uint32 idx   : 6; // regI
  //   int32  s6    : 6; // signed values must go last!
  public void reg_s6_ar__reg(int v) {
    x6x6x6x6_1(v);
  }

  public void reg_s6_ar__base(int v) {
    x6x6x6x6_2(v);
  }

  public void reg_s6_ar__idx(int v) {
    x6x6x6x6_3(v);
  }

  public void reg_s6_ar__s6(int v) {
    x6x6x6x6_4(v);
  }

  public int reg_s6_ar__reg() {
    return x6x6x6x6_1();
  }

  public int reg_s6_ar__base() {
    return x6x6x6x6_2();
  }

  public int reg_s6_ar__idx() {
    return x6x6x6x6_3();
  }

  public int reg_s6_ar__s6() {
    return x6x6x6x6_s4();
  }

  // reg_reg_sym; // reg <- reg op sym
  //   uint32 op    : 8;
  //   uint32 sym   : SYM12; // cp.i32
  //   uint32 reg0  : 6;
  //   uint32 reg1  : 6;
  public void reg_reg_sym__sym(int v) {
    x12x6x6_1(v);
  }

  public void reg_reg_sym__reg0(int v) {
    x12x6x6_2(v);
  }

  public void reg_reg_sym__reg1(int v) {
    x12x6x6_3(v);
  }

  public int reg_reg_sym__sym() {
    return x12x6x6_1();
  }

  public int reg_reg_sym__reg0() {
    return x12x6x6_2();
  }

  public int reg_reg_sym__reg1() {
    return x12x6x6_3();
  }

  // reg_s6_desloc; // if (reg op s6) ip += desloc
  //   uint32 op    : 8;
  //   uint32 reg   : 6;
  //   int32  s6    : 6;  // signed values must go last!
  //   int32  desloc: 12;
  public void reg_s6_desloc__reg(int v) {
    x6x6x12_1(v);
    type = T_x6xS6x12;
  }

  public void reg_s6_desloc__s6(int v) {
    x6x6x12_2(v);
  }

  public void reg_s6_desloc__desloc(int v) {
    x6x6x12_3(v);
  }

  public int reg_s6_desloc__reg() {
    return x6x6x12_1();
  }

  public int reg_s6_desloc__s6() {
    return x6x6x12_s2();
  }

  public int reg_s6_desloc__desloc() {
    return x6x6x12_s3();
  }

  // reg_desloc; // if (reg op s6) ip += desloc
  //   uint32 op    : 8;
  //   uint32 reg   : 8; // 6
  //   int32  desloc: 16;
  public void reg_desloc__reg(int v) {
    x8x16_1(v);
  }

  public void reg_desloc__desloc(int v) {
    x8x16_2(v);
  }

  public int reg_desloc__reg() {
    return x8x16_1();
  }

  public int reg_desloc__desloc() {
    return x8x16_2();
  }

  // reg_sym_sdesloc; // if (reg op sym) ip += desloc
  //   uint32 op    : 8;
  //   uint32 sym   : SYM12; // cp.i32
  //   uint32 reg   : 6;
  //   int32 desloc : 6;
  public void reg_sym_sdesloc__sym(int v) {
    x12x6x6_1(v);
  }

  public void reg_sym_sdesloc__reg(int v) {
    x12x6x6_2(v);
  }

  public void reg_sym_sdesloc__desloc(int v) {
    x12x6x6_3(v);
  }

  public int reg_sym_sdesloc__sym() {
    return x12x6x6_1();
  }

  public int reg_sym_sdesloc__reg() {
    return x12x6x6_2();
  }

  public int reg_sym_sdesloc__desloc() {
    return x12x6x6_s3();
  }

  // reg_arl_s12; // if (regI <= base.length) ip += desloc
  //   uint32 op    : 8;
  //   uint32 regI  : 6;
  //   uint32 base  : 6; // regO
  //   int32  desloc: 12; // signed values must go last!
  public void reg_arl_s12__regI(int v) {
    x6x6x12_1(v);
  }

  public void reg_arl_s12__base(int v) {
    x6x6x12_2(v);
  }

  public void reg_arl_s12__desloc(int v) {
    x6x6x12_3(v);
  }

  public int reg_arl_s12__regI() {
    return x6x6x12_1();
  }

  public int reg_arl_s12__base() {
    return x6x6x12_2();
  }

  public int reg_arl_s12__desloc() {
    return x6x6x12_s3();
  }

  // reg
  //   uint32 op    : 8;
  //   uint32 reg   : 8; // 6
  public void reg__reg(int v) {
    x8x8x8_1(v);
  }

  public int reg__reg() {
    return x8x8x8_1();
  }

  // sym
  //   uint32 op    : 8;
  //   uint32 unused: 8;
  //   uint32 sym   : SYM16; // cp.i32 / cp.obj / cp.dbl / cp.i64
  public void sym__sym(int v) {
    x8x16_2(v);
  }

  public int sym__sym() {
    return x8x16_2();
  }

  // newarray / newmultiarray
  //   uint32 op    : 8;
  //   uint32 sym   : SYM12; // cp.identCls or cp.identPrimitive
  //   uint32 regO  : 6;
  //   uint32 lenOrRegIOrDims : 6; // array len or reg number or dimensions
  //
  //   See, below (dims_xxx), the form store the dimensions.
  public void newarray__sym(int v) {
    x12x6x6_1(v);
  }

  public void newarray__regO(int v) {
    x12x6x6_2(v);
  }

  public void newarray__lenOrRegIOrDims(int v) {
    x12x6x6_3(v);
  }

  public int newarray__sym() {
    return x12x6x6_1();
  }

  public int newarray__regO() {
    return x12x6x6_2();
  }

  public int newarray__lenOrRegIOrDims() {
    return x12x6x6_3();
  }

  // dimensions:
  //   uint32 dim1: 8;
  //   uint32 dim2: 8;
  //   uint32 dim3: 8;
  //   uint32 dim4: 8;
  //
  //   When newmultiarray is used, the next instructions store the length of each dimension.
  //   The dimensions must be moved to registers (regI). Each instruction of dimensions can
  //   store 4 dimensions (8 bits for each register).
  public void dims__dim1(int v) {
    op(v);
  }

  public void dims__dim2(int v) {
    x8x8x8_1(v);
  }

  public void dims__dim3(int v) {
    x8x8x8_2(v);
  }

  public void dims__dim4(int v) {
    x8x8x8_3(v);
  }

  public int dims__dim1() {
    return op();
  }

  public int dims__dim2() {
    return x8x8x8_1();
  }

  public int dims__dim3() {
    return x8x8x8_2();
  }

  public int dims__dim4() {
    return x8x8x8_3();
  }

  // switch_reg;
  //   uint32 op    : 8;
  //   uint32 key   : 8;  // 6 - regI
  //   uint32 n     : 16; // number of keys
  public void switch_reg__key(int v) {
    x8x16_1(v);
  }

  public void switch_reg__n(int v) {
    x8x16_2(v);
  }

  public int switch_reg__key() {
    return x8x16_1();
  }

  public int switch_reg__n() {
    return x8x16_2();
  }

  // instanceof
  //   uint32 op    : 8;
  //   uint32 sym   : SYM12; // cp.identCls
  //   uint32 regI  : 6;
  //   uint32 regO  : 6;
  public void instanceof__sym(int v) {
    x12x6x6_1(v);
  }

  public void instanceof__regI(int v) {
    x12x6x6_2(v);
  }

  public void instanceof__regO(int v) {
    x12x6x6_3(v);
  }

  public int instanceof__sym() {
    return x12x6x6_1();
  }

  public int instanceof__regI() {
    return x12x6x6_2();
  }

  public int instanceof__regO() {
    return x12x6x6_3();
  }

  public void two16__v1(int v) {
    x16x16_1(v);
  }

  public void two16__v2(int v) {
    x16x16_2(v);
  }

  public int two16__v1() {
    return x16x16_1();
  }

  public int two16__v2() {
    return x16x16_2();
  }

  public int getSymbol() {
    switch (op()) {
    case NEWOBJ:
    case MOV_regI_sym:
    case MOV_regO_sym:
    case MOV_regD_sym:
    case MOV_regL_sym:
      return reg_sym__sym();
    case MOV_regI_field:
    case MOV_regO_field:
    case MOV_reg64_field:
    case MOV_field_regI:
    case MOV_field_regO:
    case MOV_field_reg64:
      return field_reg__sym();
    case MOV_regI_static:
    case MOV_regO_static:
    case MOV_reg64_static:
    case MOV_static_regI:
    case MOV_static_regO:
    case MOV_static_reg64:
      return static_reg__sym();
    case ADD_regI_regI_sym:
      return reg_reg_sym__sym();
    case JEQ_regI_sym:
    case JNE_regI_sym:
      return reg_sym_sdesloc__sym();
    case INSTANCEOF:
    case CHECKCAST:
      return instanceof__sym();
    case RETURN_symI:
    case RETURN_symO:
    case RETURN_symD:
    case RETURN_symL:
      return sym__sym();
    case NEWARRAY_len:
    case NEWARRAY_regI:
    case NEWARRAY_multi:
      return newarray__sym();
    case CALL_normal:
    case CALL_virtual:
      return mtd__sym();
    default:
      return -1;
    }
  }

  public void setSymbol(int sym) {
    switch (op()) {
    case NEWOBJ:
    case MOV_regI_sym:
    case MOV_regO_sym:
    case MOV_regD_sym:
    case MOV_regL_sym:
      reg_sym__sym(sym);
      break;
    case MOV_regI_field:
    case MOV_regO_field:
    case MOV_reg64_field:
    case MOV_field_regI:
    case MOV_field_regO:
    case MOV_field_reg64:
      field_reg__sym(sym);
      break;
    case MOV_regI_static:
    case MOV_regO_static:
    case MOV_reg64_static:
    case MOV_static_regI:
    case MOV_static_regO:
    case MOV_static_reg64:
      static_reg__sym(sym);
      break;
    case ADD_regI_regI_sym:
      reg_reg_sym__sym(sym);
      break;
    case JEQ_regI_sym:
    case JNE_regI_sym:
      reg_sym_sdesloc__sym(sym);
      break;
    case INSTANCEOF:
    case CHECKCAST:
      instanceof__sym(sym);
      break;
    case RETURN_symI:
    case RETURN_symO:
    case RETURN_symD:
    case RETURN_symL:
      sym__sym(sym);
      break;
    case NEWARRAY_len:
    case NEWARRAY_regI:
    case NEWARRAY_multi:
      newarray__sym(sym);
      break;
    case CALL_normal:
    case CALL_virtual:
      mtd__sym(sym);
      break;
    }
  }

  public int getSymbolType() {
    switch (op()) {
    case CHECKCAST:
    case INSTANCEOF:
    case NEWARRAY_len:
    case NEWARRAY_multi:
    case NEWARRAY_regI:
    case NEWOBJ:
      return POOL_SYM;
    case ADD_regI_regI_sym:
    case JEQ_regI_sym:
    case JNE_regI_sym:
    case MOV_regI_sym:
    case RETURN_symI:
      return POOL_I32;
    /*         case MOV_field_reg64:
       case MOV_field_regI:
       case MOV_field_regO:
       case MOV_regO_field:
       case MOV_reg64_field:
       case MOV_regI_field:
          return POOL_IF;
       case MOV_reg64_static:
       case MOV_regI_static:
       case MOV_regO_static:
       case MOV_static_reg64:
       case MOV_static_regI:
       case MOV_static_regO:
          return POOL_SF;
       case CALL_normal:
       case CALL_virtual:
          return POOL_MTD;
     */ case MOV_regD_sym:
    case RETURN_symD:
      return POOL_DBL;
    case MOV_regL_sym:
    case RETURN_symL:
      return POOL_I64;
    case MOV_regO_sym:
    case RETURN_symO:
      return POOL_STR;
    }
    return -1;
  }
}
