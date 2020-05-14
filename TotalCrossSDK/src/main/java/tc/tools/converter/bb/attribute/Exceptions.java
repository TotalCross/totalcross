// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bb.attribute;

import tc.tools.converter.bb.JavaClass;
import tc.tools.converter.bb.JavaConstant;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Vector;

public class Exceptions implements AttributeInfo {
  private JavaClass jclass;

  public Vector exceptions;

  public Exceptions(JavaClass jclass) {
    this.jclass = jclass;
    exceptions = new Vector();
  }

  @Override
  public int length() {
    return 2 + (exceptions.size() * 2);
  }

  @Override
  public void load(DataStream ds) throws IOException {
    int count = ds.readUnsignedShort();
    exceptions.removeAllElements();
    for (int i = 0; i < count; i++) {
      exceptions.addElement(jclass.getConstant(ds.readUnsignedShort(), this));
    }
  }

  @Override
  public void save(DataStream ds) throws IOException {
    int count = exceptions.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ds.writeShort(jclass.getConstantIndex((JavaConstant) exceptions.items[i], this));
    }
  }
}
