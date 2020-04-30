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
import tc.tools.converter.J2TC;
import totalcross.io.DataStreamLE;

public class TCField {
  // The access flags of this field (isPublic, isPrivate, isObject, isArray, etc)
  public TCFieldFlags flags = new TCFieldFlags();
  // The name's index in the constant pool
  public int /*uint16*/ cpName;
  // The fully qualified class name
  public int /*uint16*/ cpType;
  // The index to access the field in the constant pool (class name + field name)
  public int /*uint16*/ cpField;

  public void write(DataStreamLE ds) throws totalcross.io.IOException {
    if (J2TC.dump) {
      System.out.println("Field: " + flags.toString() + GlobalConstantPool.getType(cpType) + " "
          + GlobalConstantPool.getMethodFieldName(cpName));
    }
    flags.write(ds);
    ds.writeShort(cpName);
    ds.writeShort(cpType);
  }
}
