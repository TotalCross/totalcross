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

public class StoreArray extends ByteCode {
  public int stackIncIni, objPos, indPos, srcPos;

  public StoreArray(int stackIncIni, int objPos, int indPos, int srcPos, int type) {
    this.stackIncIni = stackIncIni;
    this.objPos = objPos;
    this.indPos = indPos;
    this.srcPos = srcPos;
    this.targetType = type;
    super.stackInc = 0;
  }

  @Override
  public void exec() {
    stackPtr += stackIncIni;
    Object o = stack[stackPtr + objPos];
    int ind = stack[stackPtr + indPos].asInt;
    switch (targetType) {
    case INT: {
      int[] a = (int[]) o;
      a[ind] = stack[stackPtr + srcPos].asInt;
      break;
    }
    case FLOAT: {
      float[] a = (float[]) o;
      a[ind] = (float) stack[stackPtr + srcPos].asDouble;
      break;
    }
    case DOUBLE: {
      double[] a = (double[]) o;
      a[ind] = stack[stackPtr + srcPos].asDouble;
      break;
    }
    case LONG: {
      long[] a = (long[]) o;
      a[ind] = stack[stackPtr + srcPos].asLong;
      break;
    }
    case OBJECT: {
      Object[] a = (Object[]) o;
      a[ind] = stack[stackPtr + srcPos].asObj;
      break;
    }
    case SHORT: {
      short[] a = (short[]) o;
      a[ind] = (short) stack[stackPtr + srcPos].asInt;
      break;
    }
    case CHAR: {
      char[] a = (char[]) o;
      a[ind] = (char) stack[stackPtr + srcPos].asInt;
      break;
    }
    case BYTE: {
      byte[] a = (byte[]) o;
      a[ind] = (byte) stack[stackPtr + srcPos].asInt;
      break;
    }
    }
  }
}
