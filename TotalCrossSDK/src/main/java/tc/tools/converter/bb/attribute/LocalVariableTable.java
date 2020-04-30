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
package tc.tools.converter.bb.attribute;

import tc.tools.converter.bb.JavaClass;
import tc.tools.converter.bb.JavaClassStructure;
import tc.tools.converter.bb.JavaConstant;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Vector;

public class LocalVariableTable implements AttributeInfo {
  private JavaClass jclass;

  public Vector variables;

  public LocalVariableTable(JavaClass jclass) {
    this.jclass = jclass;
    variables = new Vector();
  }

  @Override
  public int length() {
    return 2 + (variables.size() * 10);
  }

  @Override
  public void load(DataStream ds) throws IOException {
    int count = ds.readUnsignedShort();
    variables.removeAllElements();
    for (int i = 0; i < count; i++) {
      LocalVariable variable = new LocalVariable(this);
      variable.load(ds);
      variables.addElement(variable);
    }
  }

  @Override
  public void save(DataStream ds) throws IOException {
    int count = variables.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ((LocalVariable) variables.items[i]).save(ds);
    }
  }

  public static class LocalVariable implements JavaClassStructure {
    public LocalVariableTable table;
    public int startPC;
    public int length;
    public JavaConstant name;
    public JavaConstant descriptor;
    public int index;

    public LocalVariable(LocalVariableTable table) {
      this.table = table;
    }

    @Override
    public int length() {
      return 10;
    }

    /**
     * @param ds
     */
    @Override
    public void load(DataStream ds) throws IOException {
      startPC = ds.readUnsignedShort();
      length = ds.readUnsignedShort();
      name = table.jclass.getConstant(ds.readUnsignedShort(), this);
      descriptor = table.jclass.getConstant(ds.readUnsignedShort(), this);
      index = ds.readUnsignedShort();
    }

    /**
     * @param ds
     */
    @Override
    public void save(DataStream ds) throws IOException {
      ds.writeShort(startPC);
      ds.writeShort(length);
      ds.writeShort(table.jclass.getConstantIndex(name, this));
      ds.writeShort(table.jclass.getConstantIndex(descriptor, this));
      ds.writeShort(index);
    }
  }
}
