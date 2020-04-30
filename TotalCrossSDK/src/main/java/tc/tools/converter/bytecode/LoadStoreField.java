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

import tc.tools.converter.java.JavaConstantInfo;

public class LoadStoreField extends ByteCode {
  public String fieldName, fieldTypeClass;
  public String classInstance;
  /** Class name where this field is declared */
  public String className;

  public LoadStoreField(int idx) {
    JavaConstantInfo jci = (JavaConstantInfo) cp.constants[idx];
    int classIndex = jci.index1;
    int nameAndTypeIndex = jci.index2;
    className = cp.getString1(classIndex);
    fieldName = cp.getString1(nameAndTypeIndex);
    fieldTypeClass = cp.getString2(nameAndTypeIndex);
    targetType = convertJavaType(fieldTypeClass);
    pcInc = 3;
  }
}
