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

package totalcross.lang;

import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;

public class Long4D extends Number4D implements Comparable<Long4D> {
  public static final Class<Long> TYPE = Long.class;
  long v;

  public Long4D(long v) {
    this.v = v;
  }

  public Long4D(String s) throws NumberFormatException {
    v = parseLong(s);
  }

  @Override
  public long longValue() {
    return v;
  }

  @Override
  public boolean equals(Object o) {
    return o != null && o instanceof Long4D && ((Long4D) o).v == this.v;
  }

  @Override
  public int hashCode() {
    return (int) (v ^ (v >>> 32));
  }

  public String toString(long l) {
    return Convert.toString(l);
  }

  @Override
  public String toString() {
    return String.valueOf(v);
  }

  public Long4D valueOf(long l) {
    return new Long4D(l);
  }

  public static Long4D valueOf(String s) throws NumberFormatException {
    try {
      return new Long4D(Convert.toLong(s));
    } catch (InvalidNumberException ine) {
      throw new NumberFormatException(ine.getMessage());
    }
  }

  public static long parseLong(String s) throws NumberFormatException {
    try {
      return Convert.toLong(s);
    } catch (InvalidNumberException ine) {
      throw new NumberFormatException(ine.getMessage());
    }
  }

  @Override
  public int intValue() {
    return (int) v;
  }

  @Override
  public double doubleValue() {
    return (double) v;
  }

  @Override
  public int compareTo(Long4D o) {
    return this.v < o.v ? -1 : (this.v == o.v ? 0 : +1);
  }
  
  public static int compare(long a, long b) {
	if (a < b) {
		return -1;
	} else if (a > b) {
		return 1;
	} else {
		return 0;
	}
  }

  public static String toString(long i, int radix) {
    return Convert.toString(i, radix);
  }
}
