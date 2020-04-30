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
package tc.tools.converter.oper;

import tc.tools.converter.TCConstants;

public class OperandKind implements TCConstants {
  public static final boolean isConstant(int opr) {
    switch (opr) {
    case opr_s6I:
    case opr_s6D:
    case opr_s6L:
    case opr_s12I:
    case opr_s12D:
    case opr_s12L:
    case opr_s16I:
    case opr_s16D:
    case opr_s16L:
    case opr_u16:
    case opr_s18I:
    case opr_s18D:
    case opr_s18L:
    case opr_s24I:
    case opr_s24D:
    case opr_s24L:
    case opr_s32I:
    case opr_s32D:
    case opr_s32L:
    case opr_s64L:
    case opr_s64D:
      return true;
    }

    return false;
  }

  public static final boolean isConstantI(int opr) {
    switch (opr) {
    case opr_s6I:
    case opr_s12I:
    case opr_s16I:
    case opr_u16:
    case opr_s18I:
    case opr_s24I:
    case opr_s32I:
      return true;
    }

    return false;
  }

  public static final boolean isConstantFitIn18Bits(int opr) {
    switch (opr) {
    case opr_s6I:
    case opr_s6D:
    case opr_s6L:
    case opr_s12I:
    case opr_s12D:
    case opr_s12L:
    case opr_s16I:
    case opr_s16D:
    case opr_s16L:
    case opr_u16:
    case opr_s18I:
    case opr_s18D:
    case opr_s18L:
      return true;
    }

    return false;
  }

  public static final boolean isConstantNotFitIn18Bits(int opr) {
    switch (opr) {
    case opr_s24I:
    case opr_s24D:
    case opr_s24L:
    case opr_s32I:
    case opr_s32D:
    case opr_s32L:
    case opr_s64L:
    case opr_s64D:
      return true;
    }

    return false;
  }

  public static final boolean isConstantFitIn16Bits(int opr) {
    switch (opr) {
    case opr_s6I:
    case opr_s6D:
    case opr_s6L:
    case opr_s12I:
    case opr_s12D:
    case opr_s12L:
    case opr_s16I:
    case opr_s16D:
    case opr_s16L:
    case opr_u16:
      return true;
    }

    return false;
  }

  public static final boolean isNumerical(int opr) {
    switch (opr) {
    case opr_regI:
    case opr_regD:
    case opr_regL:
    case opr_fieldI:
    case opr_fieldD:
    case opr_fieldL:
    case opr_staticI:
    case opr_staticD:
    case opr_staticL:
    case opr_symI:
    case opr_symD:
    case opr_symL:
    case opr_regIb:
    case opr_regIs:
    case opr_regIc:
    case opr_aruI:
    case opr_aruD:
    case opr_aruL:
    case opr_arcI:
    case opr_arcD:
    case opr_arcL:
    case opr_arlen:
      return true;
    }

    if (isConstant(opr)) {
      return true;
    }

    return false;
  }

  public static final boolean isNumericalExceptRegIx(int opr) {
    switch (opr) {
    case opr_regI:
    case opr_regD:
    case opr_regL:
    case opr_fieldI:
    case opr_fieldD:
    case opr_fieldL:
    case opr_staticI:
    case opr_staticD:
    case opr_staticL:
    case opr_symI:
    case opr_symD:
    case opr_symL:
    case opr_aruI:
    case opr_aruD:
    case opr_aruL:
    case opr_arcI:
    case opr_arcD:
    case opr_arcL:
    case opr_arlen:
      return true;
    }

    if (isConstant(opr)) {
      return true;
    }

    return false;
  }

  public static final boolean isSuffixI(int opr) {
    switch (opr) {
    case opr_regI:
    case opr_fieldI:
    case opr_staticI:
    case opr_symI:
    case opr_regIb:
    case opr_regIs:
    case opr_regIc:
    case opr_aruI:
    case opr_arcI:
      return true;
    }

    return false;
  }

  public static final boolean isSuffixD(int opr) {
    switch (opr) {
    case opr_regD:
    case opr_fieldD:
    case opr_staticD:
    case opr_symD:
    case opr_aruD:
    case opr_arcD:
    case opr_s64D:
      return true;
    }

    return false;
  }

  public static final boolean isSuffixL(int opr) {
    switch (opr) {
    case opr_regL:
    case opr_fieldL:
    case opr_staticL:
    case opr_symL:
    case opr_aruL:
    case opr_arcL:
      return true;
    }

    return false;
  }

  public static final boolean isSuffixO(int opr) {
    switch (opr) {
    case opr_regO:
    case opr_fieldO:
    case opr_staticO:
    case opr_symO:
    case opr_aruO:
    case opr_arcO:
      return true;
    }

    return false;
  }

  public static final boolean isTypeI(int opr) {
    switch (opr) {
    case opr_s6I:
    case opr_s12I:
    case opr_s16I:
    case opr_u16:
    case opr_s18I:
    case opr_s24I:
    case opr_s32I:
    case opr_arlen:
      return true;
    }

    if (isSuffixI(opr)) {
      return true;
    }

    return false;
  }

  public static final boolean isTypeD(int opr) {
    switch (opr) {
    case opr_s6D:
    case opr_s12D:
    case opr_s16D:
    case opr_s18D:
    case opr_s24D:
    case opr_s32D:
      return true;
    }

    return isSuffixD(opr);
  }

  public static final boolean isTypeL(int opr) {
    switch (opr) {
    case opr_s6L:
    case opr_s12L:
    case opr_s16L:
    case opr_s18L:
    case opr_s24L:
    case opr_s32L:
    case opr_s64L:
      return true;
    }
    if (isSuffixL(opr)) {
      return true;
    }

    return false;
  }

  public static final boolean isTypeO(int opr) {
    if (opr == opr_null || isSuffixO(opr)) {
      return true;
    }

    return false;
  }

  public static final boolean isTypeIExceptARU_I_ARC_I(int opr) {
    switch (opr) {
    case opr_regI:
    case opr_fieldI:
    case opr_staticI:
    case opr_symI:
    case opr_regIb:
    case opr_regIs:
    case opr_regIc:
    case opr_s6I:
    case opr_s12I:
    case opr_s18I:
    case opr_s24I:
    case opr_s32I:
    case opr_s16I:
    case opr_u16:
    case opr_arlen:
      return true;
    }

    return false;
  }

  public static final boolean isTypeIExceptConstants(int opr) {
    if (opr == opr_arlen || isSuffixI(opr)) {
      return true;
    }

    return false;
  }

  public static final boolean isSymI_s24_s32(int opr) {
    switch (opr) {
    case opr_symI:
    case opr_s24I:
    case opr_s32I:
      return true;
    }

    return false;
  }

  public static final boolean isSymL_s64(int opr) {
    switch (opr) {
    case opr_symL:
    case opr_s64L:
      return true;
    }

    return false;
  }

  public static final boolean isType64Bits(int opr) {
    return (isTypeL(opr) || isTypeD(opr));
  }
}
