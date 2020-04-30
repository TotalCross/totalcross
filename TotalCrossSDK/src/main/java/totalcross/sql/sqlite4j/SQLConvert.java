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

package totalcross.sql.sqlite4j;

import totalcross.sql.Timestamp;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.util.BigDecimal;
import totalcross.util.Date;
import totalcross.util.InvalidDateException;

class SQLConvert {
  private SQLConvert() {
  }

  static java.sql.Time time(totalcross.sys.Time x) {
    try {
      return x == null ? null : new java.sql.Time(x.getTime());
    } catch (InvalidDateException e) {
      return null;
    }
  }

  static totalcross.sys.Time time(String x) // 2015-01-14 17:28:09.708
  {
    try {
      return x == null || x.isEmpty() ? null : new Time(x, true, true, true, true, true, true, Settings.DATE_YMD);
    } catch (Exception e) {
      if (Settings.onJavaSE) {
        Vm.debug("Error converting time: \"" + x + "\"");
        e.printStackTrace();
      }
      return null;
    }
  }

  static java.sql.Date date(Date x) {
    return x == null ? null : new java.sql.Date(x.getTime());
  }

  static Date date(java.sql.Date x) {
    if (x != null) {
      try {
        Time t = new totalcross.sys.Time(x.getTime(), true);
        return new Date(t);
      } catch (Exception e) {
        if (Settings.onJavaSE) {
          e.printStackTrace();
        }
        return null;
      }
    }
    return null;
  }

  static java.sql.Timestamp timestamp(Timestamp x) {
    return new java.sql.Timestamp(x.getTime());
  }

  static Timestamp timestamp(java.sql.Timestamp x) {
    return new Timestamp(x.getTime());
  }

  public static java.math.BigDecimal bigdecimal(BigDecimal x) {
    return new java.math.BigDecimal(x.toPlainString());
  }

  public static BigDecimal bigdecimal(java.math.BigDecimal x) {
    try {
      return new BigDecimal(x.toPlainString());
    } catch (InvalidNumberException e) {
      return null;
    }
  }
}
