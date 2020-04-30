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

import totalcross.sys.Convert;

public class TCValue implements JConstants, tc.tools.converter.tclass.TClassConstants {
  public int type;

  public int asInt;
  public long asLong;
  public double asDouble;
  public Object asObj;
  // fields used on TempConstantPool
  public String asStr;
  public int index = -1;

  public void copyFrom(TCValue val) {
    asInt = val.asInt;
    asLong = val.asLong;
    asDouble = val.asDouble;
    asObj = val.asObj;
    type = val.type;
  }

  public void clear() {
    type = asInt = 0;
    asLong = 0;
    asDouble = 0;
    asObj = null;
  }

  @Override
  public String toString() {
    //      return typeAsString[type]+"(I:"+asInt+",L:"+asLong+",D:"+asDouble+",O:\""+(asObj==null?"":asObj)+"\"";
    if (index >= 0) {
      switch (type) {
      case POOL_I32:
        return Convert.toString(asInt);
      case POOL_I64:
        return Convert.toString(asLong);
      case POOL_DBL:
        return Convert.toString(asDouble);
      default:
        return asStr;
      }
    } else {
      switch (type) {
      case VOID:
        return "void";
      case INT:
        return asInt + "I";
      case FLOAT:
        return asDouble + "F";
      case DOUBLE:
        return asDouble + "D";
      case LONG:
        return asLong + "L";
      case OBJECT:
        return "\"" + asObj + "\"";
      case SHORT:
        return asInt + "S";
      case CHAR:
        return asInt + "C";
      case BYTE:
        return asInt + "B";
      case BOOLEAN:
        return asInt == 0 ? "false" : "true";
      default:
        return "Undefined";
      }
    }
  }

  public TCValue() {
  }

  public TCValue(int val) {
    set(val);
  }

  public void set(int val) {
    type = POOL_I32;
    asInt = val;
  }

  public TCValue(long val) {
    set(val);
  }

  public void set(long val) {
    type = POOL_I64;
    asLong = val;
  }

  public TCValue(double val) {
    set(val);
  }

  public void set(double val) {
    type = POOL_DBL;
    asDouble = val;
  }

  public TCValue(String val, int type) {
    set(val, type);
  }

  public void set(String val, int type) {
    this.type = type;
    asStr = val;
  }

  @Override
  public int hashCode() {
    switch (type) {
    case POOL_I32:
      return asInt;
    case POOL_I64:
      return (int) asLong;
    case POOL_DBL:
      return (int) Double.doubleToLongBits(asDouble);
    default:
      return asStr.hashCode();
    }
  }

  @Override
  public boolean equals(Object other) {
    try {
      TCValue o = (TCValue) other;
      switch (type) {
      case POOL_I32:
        return o.asInt == asInt;
      case POOL_I64:
        return o.asLong == asLong;
      case POOL_DBL:
        return o.asDouble == asDouble;
      default:
        return o.asStr.equals(asStr);
      }
    } catch (ClassCastException cce) {
      if (other instanceof String) {
        return ((String) other).equals(asStr);
      }
      return false;
    }
  }
}
