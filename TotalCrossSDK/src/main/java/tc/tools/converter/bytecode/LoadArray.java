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

package tc.tools.converter.bytecode;

public class LoadArray extends ByteCode {
  public int stackIncIni, objPos, indPos, resultPos;

  public LoadArray(int stackIncIni, int objPos, int indPos, int resultPos, int type) {
    this.stackIncIni = stackIncIni;
    this.objPos = objPos;
    this.indPos = indPos;
    this.resultPos = resultPos;
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
      stack[stackPtr + resultPos].asInt = a[ind];
      break;
    }
    case FLOAT: {
      float[] a = (float[]) o;
      stack[stackPtr + resultPos].asDouble = a[ind];
      break;
    }
    case DOUBLE: {
      double[] a = (double[]) o;
      stack[stackPtr + resultPos].asDouble = a[ind];
      break;
    }
    case LONG: {
      long[] a = (long[]) o;
      stack[stackPtr + resultPos].asLong = a[ind];
      break;
    }
    case OBJECT: {
      Object[] a = (Object[]) o;
      stack[stackPtr + resultPos].asObj = a[ind];
      break;
    }
    case SHORT: {
      short[] a = (short[]) o;
      stack[stackPtr + resultPos].asInt = a[ind];
      break;
    }
    case CHAR: {
      char[] a = (char[]) o;
      stack[stackPtr + resultPos].asInt = a[ind];
      break;
    }
    case BYTE: {
      byte[] a = (byte[]) o;
      stack[stackPtr + resultPos].asInt = a[ind];
      break;
    }
    }
  }
}
