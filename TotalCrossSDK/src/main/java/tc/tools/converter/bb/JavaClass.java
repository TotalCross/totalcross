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
package tc.tools.converter.bb;

import tc.tools.converter.bb.constant.Class;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Vector;

public class JavaClass implements JavaClassStructure {
  public int minorVersion;
  public int majorVersion;
  public Vector constantPool;
  public int accessFlags;
  public JavaConstant thisClass;
  public JavaConstant superClass;
  public Vector interfaces;
  public Vector fields;
  public Vector methods;
  public Vector attributes;

  public static final int MAGIC_NUMBER = 0xCAFEBABE;

  public static final int ACC_PUBLIC = 0x0001;
  public static final int ACC_FINAL = 0x0010;
  public static final int ACC_SUPER = 0x0020;
  public static final int ACC_INTERFACE = 0x0200;
  public static final int ACC_ABSTRACT = 0x0400;

  public static final int PLATFORM_DESKTOP = 1;
  public static final int PLATFORM_DEVICE = 2;
  public static final int PLATFORM_BLACKBERRY = 4;
  public static final int PLATFORM_ALL = 7;

  public JavaClass() {
    constantPool = new Vector();
    interfaces = new Vector();
    fields = new Vector();
    methods = new Vector();
    attributes = new Vector();
  }

  @Override
  public String toString() {
    return getClassName();
  }

  public String getClassName() {
    return ((Class) thisClass.info).getValueAsName().value;
  }

  public JavaConstant getConstant(byte index1, byte index2, JavaClassStructure caller)
      throws IndexOutOfBoundsException {
    return getConstant((((int) index1 << 8) & 0xFF00) | ((int) index2 & 0xFF), caller);
  }

  public JavaConstant getConstant(int index, JavaClassStructure caller) throws IndexOutOfBoundsException {
    if (index <= 0 || index > constantPool.size()) {
      throw new IndexOutOfBoundsException(
          "Invalid constant pool index: " + index + "; class: " + this + ", caller: " + caller);
    }

    return (JavaConstant) constantPool.items[index];
  }

  public int getConstantIndex(JavaConstant constant, JavaClassStructure caller) throws IndexOutOfBoundsException {
    int index = constantPool.indexOf(constant);
    if (index <= 0 || index > constantPool.size()) {
      throw new IndexOutOfBoundsException(
          "Invalid constant pool index: " + index + "; class: " + this + ", caller: " + caller);
    }

    return index;
  }

  @Override
  public int length() {
    int len = 24 + (interfaces.size() * 2);

    int count = constantPool.size();
    for (int i = 0; i < count; i++) {
      JavaConstant constant = (JavaConstant) constantPool.items[i];
      len += constant.length();
      i += constant.slots() - 1;
    }

    count = fields.size();
    for (int i = 0; i < count; i++) {
      len += ((JavaField) fields.items[i]).length();
    }

    count = methods.size();
    for (int i = 0; i < count; i++) {
      len += ((JavaMethod) methods.items[i]).length();
    }

    count = attributes.size();
    for (int i = 0; i < count; i++) {
      len += ((JavaAttribute) attributes.items[i]).length();
    }

    return len;
  }

  @Override
  public void load(DataStream ds) throws IOException {
    if (ds.readInt() != MAGIC_NUMBER) {
      return;
    }

    minorVersion = ds.readUnsignedShort();
    majorVersion = ds.readUnsignedShort();

    int count = ds.readUnsignedShort() - 1;
    constantPool.removeAllElements();
    for (int i = 0; i <= count; i++) {
      constantPool.addElement(new JavaConstant(this));
    }
    for (int i = 1; i <= count; i++) {
      JavaConstant constant = (JavaConstant) constantPool.items[i];
      constant.load(ds);

      i += constant.slots() - 1;
    }

    accessFlags = ds.readUnsignedShort();
    thisClass = getConstant(ds.readUnsignedShort(), this);
    superClass = getConstant(ds.readUnsignedShort(), this);

    count = ds.readUnsignedShort();
    interfaces.removeAllElements();
    for (int i = 0; i < count; i++) {
      interfaces.addElement(getConstant(ds.readUnsignedShort(), this));
    }

    count = ds.readUnsignedShort();
    fields.removeAllElements();
    for (int i = 0; i < count; i++) {
      JavaField field = new JavaField(this);
      field.load(ds);

      fields.addElement(field);
    }

    count = ds.readUnsignedShort();
    methods.removeAllElements();
    for (int i = 0; i < count; i++) {
      JavaMethod method = new JavaMethod(this);
      method.load(ds);

      methods.addElement(method);
    }

    count = ds.readUnsignedShort();
    attributes.removeAllElements();
    for (int i = 0; i < count; i++) {
      JavaAttribute attribute = new JavaAttribute(this);
      attribute.load(ds);

      attributes.addElement(attribute);
    }
  }

  @Override
  public void save(DataStream ds) throws IOException {
    ds.writeInt(MAGIC_NUMBER);
    ds.writeShort(minorVersion);
    ds.writeShort(majorVersion);

    int count = constantPool.size() - 1;
    ds.writeShort(count + 1);
    for (int i = 1; i <= count; i++) {
      JavaConstant constant = (JavaConstant) constantPool.items[i];
      constant.save(ds);

      i += constant.slots() - 1;
    }

    ds.writeShort(accessFlags);
    ds.writeShort(getConstantIndex(thisClass, this));
    ds.writeShort(getConstantIndex(superClass, this));

    count = interfaces.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ds.writeShort(getConstantIndex((JavaConstant) interfaces.items[i], this));
    }

    count = fields.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ((JavaField) fields.items[i]).save(ds);
    }

    count = methods.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ((JavaMethod) methods.items[i]).save(ds);
    }

    count = attributes.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ((JavaAttribute) attributes.items[i]).save(ds);
    }
  }
}
