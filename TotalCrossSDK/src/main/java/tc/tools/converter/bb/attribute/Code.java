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

import tc.tools.converter.bb.JavaAttribute;
import tc.tools.converter.bb.JavaClass;
import tc.tools.converter.bb.JavaClassStructure;
import tc.tools.converter.bb.JavaConstant;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Vector;

public class Code implements AttributeInfo {
  private JavaClass jclass;

  public int maxStack;
  public int maxLocals;
  public byte[] code;
  public Vector exceptions;
  public Vector attributes;

  public Code(JavaClass jclass) {
    this.jclass = jclass;
    exceptions = new Vector();
    attributes = new Vector();
  }

  @Override
  public int length() {
    int len = 12 + code.length + (exceptions.size() * 8);
    int count = attributes.size();
    for (int i = 0; i < count; i++) {
      len += ((JavaAttribute) attributes.items[i]).length();
    }

    return len;
  }

  public LineNumberTable[] getLineNumberTables() {
    Vector v = new Vector();
    getAttributes(LineNumberTable.class, v);

    if (v.size() == 0) {
      return null;
    } else {
      LineNumberTable[] result = new LineNumberTable[v.size()];
      v.copyInto(result);
      return result;
    }
  }

  public LocalVariableTable[] getLocalVariableTables() {
    Vector v = new Vector();
    getAttributes(LocalVariableTable.class, v);

    if (v.size() == 0) {
      return null;
    } else {
      LocalVariableTable[] result = new LocalVariableTable[v.size()];
      v.copyInto(result);
      return result;
    }
  }

  private void getAttributes(Class<?> infoType, Vector v) {
    JavaAttribute attribute;

    int count = attributes.size();
    for (int i = 0; i < count; i++) {
      attribute = (JavaAttribute) attributes.items[i];
      if (infoType.isInstance(attribute.info)) {
        v.addElement(attribute.info);
      }
    }
  }

  @Override
  public void load(DataStream ds) throws IOException {
    maxStack = ds.readUnsignedShort();
    maxLocals = ds.readUnsignedShort();

    int count = ds.readInt();
    code = new byte[count];
    ds.readBytes(code);

    count = ds.readUnsignedShort();
    exceptions.removeAllElements();
    for (int i = 0; i < count; i++) {
      Exception exception = new Exception();
      exception.load(ds);
      exceptions.addElement(exception);
    }

    count = ds.readUnsignedShort();
    attributes.removeAllElements();
    for (int i = 0; i < count; i++) {
      JavaAttribute attribute = new JavaAttribute(jclass);
      attribute.load(ds);
      attributes.addElement(attribute);
    }
  }

  @Override
  public void save(DataStream ds) throws IOException {
    ds.writeShort(maxStack);
    ds.writeShort(maxLocals);
    ds.writeInt(code.length);
    ds.writeBytes(code);

    int count = exceptions.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ((Exception) exceptions.items[i]).save(ds);
    }

    count = attributes.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ((JavaAttribute) attributes.items[i]).save(ds);
    }
  }

  public class Exception implements JavaClassStructure {
    public int startPC;
    public int endPC;
    public int handlerPC;
    public JavaConstant catchType;

    @Override
    public int length() {
      return 8;
    }

    @Override
    public void load(DataStream ds) throws IOException {
      startPC = ds.readUnsignedShort();
      endPC = ds.readUnsignedShort();
      handlerPC = ds.readUnsignedShort();

      int index = ds.readUnsignedShort();
      catchType = index == 0 ? null : jclass.getConstant(index, this);
    }

    @Override
    public void save(DataStream ds) throws IOException {
      ds.writeShort(startPC);
      ds.writeShort(endPC);
      ds.writeShort(handlerPC);
      ds.writeShort(catchType == null ? 0 : jclass.getConstantIndex(catchType, this));
    }
  }
}
