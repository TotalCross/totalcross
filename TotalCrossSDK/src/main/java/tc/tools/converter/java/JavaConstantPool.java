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
package tc.tools.converter.java;

import totalcross.io.DataStream;

public final class JavaConstantPool {
  public int numConstants;
  public Object[] constants;

  public JavaConstantPool(DataStream ds) throws totalcross.io.IOException {
    totalcross.sys.Convert.setDefaultConverter("UTF8");
    numConstants = ds.readUnsignedShort();
    if (numConstants > 0) {
      byte b;
      constants = new Object[numConstants + 1]; // constants start from 1
      for (int i = 1; i < numConstants; i++) {
        switch (b = ds.readByte())
        // guich@400_16: moved to here since now we compute other things; also, ordered and inserted blank cases to let the compiler optimize it to a jump table
        {
        case 1: // utf8 - IDENTIFIER
          constants[i] = ds.readString();
          break;
        case 3: // integer
          constants[i] = new Integer(ds.readInt());
          break;
        case 4: // float
          constants[i] = new Float(ds.readFloat());
          break;
        case 5: // long
          constants[i] = new Long(ds.readLong());
          i++;
          break;
        case 6: // double
          constants[i] = new Double(ds.readDouble());
          i++;
          break;
        case 7: // Class
        case 8: // String
          constants[i] = new JavaConstantInfo(b, ds.readUnsignedShort());
          break;
        case 9: // field
        case 10: // method
        case 11: // interface method
        case 12: // name and type
          constants[i] = new JavaConstantInfo(b, ds.readUnsignedShort(), ds.readUnsignedShort());
          break;
        }
      }
    }
    totalcross.sys.Convert.setDefaultConverter("");
  }

  public String getString1(int idx) {
    if (idx == 0) {
      return null;
    }
    if (constants[idx] instanceof String) {
      return (String) constants[idx];
    }
    JavaConstantInfo ci = (JavaConstantInfo) constants[idx];
    return (String) constants[ci.index1];
  }

  public String getString2(int idx) {
    if (idx == 0) {
      return null;
    }
    JavaConstantInfo ci = (JavaConstantInfo) constants[idx];
    return (String) constants[ci.index2];
  }
}
