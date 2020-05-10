// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bb.constant;

import tc.tools.converter.bb.JavaClass;

public class NameAndType extends DoubleReference {
  public NameAndType(JavaClass jclass) {
    super(jclass);
  }

  public UTF8 getValue1AsName() {
    return (UTF8) value1.info;
  }

  public UTF8 getValue2AsDescriptor() {
    return (UTF8) value2.info;
  }
}
