// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bb.constant;

import tc.tools.converter.bb.JavaClass;

public class MethodRef extends DoubleReference {
  public MethodRef(JavaClass jclass) {
    super(jclass);
  }

  public Class getValue1AsClass() {
    return (Class) value1.info;
  }

  public NameAndType getValue2AsNameAndType() {
    return (NameAndType) value2.info;
  }
}
