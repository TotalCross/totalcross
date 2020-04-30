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

public class Byte4D extends Number4D {
  public static final Class<Byte> TYPE = Byte.class;
  byte v;

  public Byte4D(byte v) {
    this.v = v;
  }

  @Override
  public byte byteValue() {
    return v;
  }

  @Override
  public boolean equals(Object o) {
    return o != null && o instanceof Byte4D && ((Byte4D) o).v == this.v;
  }

  @Override
  public int hashCode() {
    return v;
  }

  @Override
  public String toString() {
    return String.valueOf(v);
  }

  public static Byte4D valueOf(byte b) {
    return new Byte4D(b);
  }

  public static Byte4D valueOf(String s) throws NumberFormatException {
    try {
      return new Byte4D((byte) Convert.toInt(s));
    } catch (InvalidNumberException ine) {
      throw new NumberFormatException(ine.getMessage());
    }
  }

  @Override
  public int intValue() {
    return v;
  }

  @Override
  public long longValue() {
    return v;
  }

  @Override
  public double doubleValue() {
    return v;
  }
}
