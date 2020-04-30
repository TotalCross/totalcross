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
package tc.tools.converter.bb.constant;

import tc.tools.converter.bb.JavaClass;

public class Class extends Reference {
  public Class(JavaClass jclass) {
    super(jclass);
  }

  public UTF8 getValueAsName() {
    return (UTF8) value.info;
  }
}
