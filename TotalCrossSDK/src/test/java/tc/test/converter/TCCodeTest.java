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

import tc.tools.converter.tclass.TCCode;
import totalcross.unit.TestCase;

public class TCCodeTest extends TestCase {
  @Override
  public void testRun() {
    TCCode c = new TCCode(0);

    c.u32__u32(0);
    assertEquals(0, c.u32__u32()); //c.clear();
    c.u32__u32(0x12345678);
    assertEquals(0x12345678, c.u32__u32()); //c.clear();

    c.op(255);
    assertEquals(255, c.op()); //c.clear();
    c.s24__desloc(0);
    assertEquals(0, c.s24__desloc()); //c.clear();

    c.s24__desloc(-1234567);
    assertEquals(-1234567, c.s24__desloc()); //c.clear();
    c.s24__desloc(1234567);
    assertEquals(1234567, c.s24__desloc()); //c.clear();

    c.reg_reg__reg0(63);
    assertEquals(63, c.reg_reg__reg0()); //c.clear();
    c.reg_reg__reg0(0);
    assertEquals(0, c.reg_reg__reg0()); //c.clear();
    c.reg_reg__reg1(63);
    assertEquals(63, c.reg_reg__reg1()); //c.clear();
    c.reg_reg__reg1(0);
    assertEquals(0, c.reg_reg__reg1()); //c.clear();

    c.field_reg__this(63);
    assertEquals(63, c.field_reg__this()); //c.clear();
    c.field_reg__this(0);
    assertEquals(0, c.field_reg__this()); //c.clear();
    c.field_reg__reg(63);
    assertEquals(63, c.field_reg__reg()); //c.clear();
    c.field_reg__reg(0);
    assertEquals(0, c.field_reg__reg()); //c.clear();
    c.field_reg__sym(4095);
    assertEquals(4095, c.field_reg__sym()); //c.clear();
    c.field_reg__sym(0);
    assertEquals(0, c.field_reg__sym()); //c.clear();

    c.mtd__this(63);
    assertEquals(63, c.mtd__this()); //c.clear();
    c.mtd__this(0);
    assertEquals(0, c.mtd__this()); //c.clear();
    c.mtd__retOrParam(63);
    assertEquals(63, c.mtd__retOrParam()); //c.clear();
    c.mtd__retOrParam(0);
    assertEquals(0, c.mtd__retOrParam()); //c.clear();
    c.mtd__sym(4095);
    assertEquals(4095, c.mtd__sym()); //c.clear();
    c.mtd__sym(0);
    assertEquals(0, c.mtd__sym()); //c.clear();

    c.static_reg__reg(63);
    assertEquals(63, c.static_reg__reg()); //c.clear();
    c.static_reg__reg(0);
    assertEquals(0, c.static_reg__reg()); //c.clear();
    c.static_reg__sym(65535);
    assertEquals(65535, c.static_reg__sym()); //c.clear();
    c.static_reg__sym(0);
    assertEquals(0, c.static_reg__sym()); //c.clear();

    c.reg_ar__base(63);
    assertEquals(63, c.reg_ar__base()); //c.clear();
    c.reg_ar__base(0);
    assertEquals(0, c.reg_ar__base()); //c.clear();
    c.reg_ar__reg(63);
    assertEquals(63, c.reg_ar__reg()); //c.clear();
    c.reg_ar__reg(0);
    assertEquals(0, c.reg_ar__reg()); //c.clear();
    c.reg_ar__idx(63);
    assertEquals(63, c.reg_ar__idx()); //c.clear();
    c.reg_ar__idx(0);
    assertEquals(0, c.reg_ar__idx()); //c.clear();

    c.reg_sym__sym(65535);
    assertEquals(65535, c.reg_sym__sym()); //c.clear();
    c.reg_sym__sym(0);
    assertEquals(0, c.reg_sym__sym()); //c.clear();
    c.reg_sym__reg(63);
    assertEquals(63, c.reg_sym__reg()); //c.clear();
    c.reg_sym__reg(0);
    assertEquals(0, c.reg_sym__reg()); //c.clear();

    c.s18_reg__s18(-131072);
    assertEquals(-131072, c.s18_reg__s18()); //c.clear();
    c.s18_reg__s18(131071);
    assertEquals(131071, c.s18_reg__s18()); //c.clear();
    c.s18_reg__reg(63);
    assertEquals(63, c.s18_reg__reg()); //c.clear();
    c.s18_reg__reg(0);
    assertEquals(0, c.s18_reg__reg()); //c.clear();

    // OPERATIONS

    c.reg_reg_reg__reg0(63);
    assertEquals(63, c.reg_reg_reg__reg0()); //c.clear();
    c.reg_reg_reg__reg0(0);
    assertEquals(0, c.reg_reg_reg__reg0()); //c.clear();
    c.reg_reg_reg__reg1(63);
    assertEquals(63, c.reg_reg_reg__reg1()); //c.clear();
    c.reg_reg_reg__reg1(0);
    assertEquals(0, c.reg_reg_reg__reg1()); //c.clear();
    c.reg_reg_reg__reg2(63);
    assertEquals(63, c.reg_reg_reg__reg2()); //c.clear();
    c.reg_reg_reg__reg2(0);
    assertEquals(0, c.reg_reg_reg__reg2()); //c.clear();

    c.reg_reg_s12__reg0(63);
    assertEquals(63, c.reg_reg_s12__reg0()); //c.clear();
    c.reg_reg_s12__reg0(0);
    assertEquals(0, c.reg_reg_s12__reg0()); //c.clear();
    c.reg_reg_s12__reg1(63);
    assertEquals(63, c.reg_reg_s12__reg1()); //c.clear();
    c.reg_reg_s12__reg1(0);
    assertEquals(0, c.reg_reg_s12__reg1()); //c.clear();
    c.reg_reg_s12__s12(-2048);
    assertEquals(-2048, c.reg_reg_s12__s12()); //c.clear();
    c.reg_reg_s12__s12(2047);
    assertEquals(2047, c.reg_reg_s12__s12()); //c.clear();

    c.reg_s6_ar__reg(63);
    assertEquals(63, c.reg_s6_ar__reg()); //c.clear();
    c.reg_s6_ar__reg(0);
    assertEquals(0, c.reg_s6_ar__reg()); //c.clear();
    c.reg_s6_ar__base(63);
    assertEquals(63, c.reg_s6_ar__base()); //c.clear();
    c.reg_s6_ar__base(0);
    assertEquals(0, c.reg_s6_ar__base()); //c.clear();
    c.reg_s6_ar__idx(63);
    assertEquals(63, c.reg_s6_ar__idx()); //c.clear();
    c.reg_s6_ar__idx(0);
    assertEquals(0, c.reg_s6_ar__idx()); //c.clear();
    c.reg_s6_ar__s6(-32);
    assertEquals(-32, c.reg_s6_ar__s6()); //c.clear();
    c.reg_s6_ar__s6(31);
    assertEquals(31, c.reg_s6_ar__s6()); //c.clear();

    c.reg_reg_sym__reg0(63);
    assertEquals(63, c.reg_reg_sym__reg0()); //c.clear();
    c.reg_reg_sym__reg0(0);
    assertEquals(0, c.reg_reg_sym__reg0()); //c.clear();
    c.reg_reg_sym__sym(4095);
    assertEquals(4095, c.reg_reg_sym__sym()); //c.clear();
    c.reg_reg_sym__sym(0);
    assertEquals(0, c.reg_reg_sym__sym()); //c.clear();
    c.reg_reg_sym__reg1(63);
    assertEquals(63, c.reg_reg_sym__reg1()); //c.clear();
    c.reg_reg_sym__reg1(0);
    assertEquals(0, c.reg_reg_sym__reg1()); //c.clear();

    c.reg_s6_ar__reg(63);
    assertEquals(63, c.reg_s6_ar__reg()); //c.clear();
    c.reg_s6_ar__reg(0);
    assertEquals(0, c.reg_s6_ar__reg()); //c.clear();
    c.reg_s6_ar__s6(-32);
    assertEquals(-32, c.reg_s6_ar__s6()); //c.clear();
    c.reg_s6_ar__s6(31);
    assertEquals(31, c.reg_s6_ar__s6()); //c.clear();
    c.reg_s6_ar__base(63);
    assertEquals(63, c.reg_s6_ar__base()); //c.clear();
    c.reg_s6_ar__base(0);
    assertEquals(0, c.reg_s6_ar__base()); //c.clear();
    c.reg_s6_ar__idx(63);
    assertEquals(63, c.reg_s6_ar__idx()); //c.clear();
    c.reg_s6_ar__idx(0);
    assertEquals(0, c.reg_s6_ar__idx()); //c.clear();

    c.reg_sym_sdesloc__reg(63);
    assertEquals(63, c.reg_sym_sdesloc__reg()); //c.clear();
    c.reg_sym_sdesloc__reg(0);
    assertEquals(0, c.reg_sym_sdesloc__reg()); //c.clear();
    c.reg_sym_sdesloc__sym(4095);
    assertEquals(4095, c.reg_sym_sdesloc__sym()); //c.clear();
    c.reg_sym_sdesloc__sym(0);
    assertEquals(0, c.reg_sym_sdesloc__sym()); //c.clear();
    c.reg_sym_sdesloc__desloc(-32);
    assertEquals(-32, c.reg_sym_sdesloc__desloc()); //c.clear();
    c.reg_sym_sdesloc__desloc(31);
    assertEquals(31, c.reg_sym_sdesloc__desloc()); //c.clear();

    c.reg_arl_s12__regI(63);
    assertEquals(63, c.reg_arl_s12__regI()); //c.clear();
    c.reg_arl_s12__regI(0);
    assertEquals(0, c.reg_arl_s12__regI()); //c.clear();
    c.reg_arl_s12__base(63);
    assertEquals(63, c.reg_arl_s12__base()); //c.clear();
    c.reg_arl_s12__base(0);
    assertEquals(0, c.reg_arl_s12__base()); //c.clear();
    c.reg_arl_s12__desloc(-2048);
    assertEquals(-2048, c.reg_arl_s12__desloc()); //c.clear();
    c.reg_arl_s12__desloc(2047);
    assertEquals(2047, c.reg_arl_s12__desloc()); //c.clear();

    c.reg__reg(63);
    assertEquals(63, c.reg__reg()); //c.clear();
    c.reg__reg(0);
    assertEquals(0, c.reg__reg()); //c.clear();

    c.sym__sym(65535);
    assertEquals(65535, c.sym__sym()); //c.clear();
    c.sym__sym(0);
    assertEquals(0, c.sym__sym()); //c.clear();

    // others
    c.newarray__regO(0);
    assertEquals(0, c.newarray__regO()); //c.clear();
    c.newarray__regO(63);
    assertEquals(63, c.newarray__regO()); //c.clear();
    c.newarray__sym(0);
    assertEquals(0, c.newarray__sym()); //c.clear();
    c.newarray__sym(4095);
    assertEquals(4095, c.newarray__sym()); //c.clear();
    c.newarray__lenOrRegIOrDims(0);
    assertEquals(0, c.newarray__lenOrRegIOrDims()); //c.clear();
    c.newarray__lenOrRegIOrDims(63);
    assertEquals(63, c.newarray__lenOrRegIOrDims()); //c.clear();
  }

}
