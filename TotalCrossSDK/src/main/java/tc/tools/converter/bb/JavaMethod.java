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

import tc.tools.converter.bb.attribute.Code;
import tc.tools.converter.bb.attribute.Exceptions;
import tc.tools.converter.bb.attribute.Synthetic;
import tc.tools.converter.bb.constant.Class;
import tc.tools.converter.bb.constant.UTF8;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Vector;

public class JavaMethod implements JavaClassStructure {
  public JavaClass jclass;
  public int accessFlags;
  public JavaConstant name;
  public JavaConstant descriptor;
  public Vector attributes;

  public static final int ACC_PUBLIC = 0x0001;
  public static final int ACC_PRIVATE = 0x0002;
  public static final int ACC_PROTECTED = 0x0004;
  public static final int ACC_STATIC = 0x0008;
  public static final int ACC_FINAL = 0x0010;
  public static final int ACC_SYNCHRONIZED = 0x0020;
  public static final int ACC_NATIVE = 0x0100;
  public static final int ACC_ABSTRACT = 0x0400;
  public static final int ACC_STRICT = 0x0800;

  public JavaMethod(JavaClass jclass) {
    this.jclass = jclass;
    attributes = new Vector();
  }

  @Override
  public String toString() {
    return getName() + getDescriptor();
  }

  public String getName() {
    return ((UTF8) name.info).value;
  }

  public String getDescriptor() {
    return ((UTF8) descriptor.info).value;
  }

  public JavaAttribute getCode() {
    JavaAttribute attribute;

    int count = attributes.size();
    for (int i = 0; i < count; i++) {
      attribute = (JavaAttribute) attributes.items[i];
      if (attribute.info instanceof Code) {
        return attribute;
      }
    }

    return null;
  }

  public boolean isPublic() {
    return (accessFlags & ACC_PUBLIC) == ACC_PUBLIC;
  }

  public boolean isPrivate() {
    return (accessFlags & ACC_PRIVATE) == ACC_PRIVATE;
  }

  public boolean isProtected() {
    return (accessFlags & ACC_PROTECTED) == ACC_PROTECTED;
  }

  public boolean isStatic() {
    return (accessFlags & ACC_STATIC) == ACC_STATIC;
  }

  public boolean isFinal() {
    return (accessFlags & ACC_FINAL) == ACC_FINAL;
  }

  public boolean isSynchronized() {
    return (accessFlags & ACC_SYNCHRONIZED) == ACC_SYNCHRONIZED;
  }

  public boolean isNative() {
    return (accessFlags & ACC_NATIVE) == ACC_NATIVE;
  }

  public boolean isAbstract() {
    return (accessFlags & ACC_ABSTRACT) == ACC_ABSTRACT;
  }

  public boolean isStrict() {
    return (accessFlags & ACC_STRICT) == ACC_STRICT;
  }

  public boolean isSynthetic() {
    for (int i = 0; i < attributes.size(); i++) {
      JavaAttribute attr = (JavaAttribute) attributes.items[i];
      if (attr.info instanceof Synthetic) {
        return true;
      }
    }

    return false;
  }

  public boolean isEquivalentTo(JavaMethod other) {
    if (other == null) {
      return false;
    }
    if (accessFlags != other.accessFlags) {
      return false;
    }

    Vector v1 = getExceptions();
    Vector v2 = other.getExceptions();

    if (v1 == null) {
      return v2 == null;
    }
    if (v2 == null) {
      return false;
    }
    if (v1.size() != v2.size()) {
      return false;
    }

    for (int i = 0; i < v1.size(); i++) {
      Class c1 = (Class) ((JavaConstant) v1.items[i]).info;
      String name = c1.getValueAsName().value;

      boolean found = false;
      for (int j = 0; j < v2.size(); j++) {
        Class c2 = (Class) ((JavaConstant) v2.items[i]).info;
        if (name.equals(c2.getValueAsName().value)) {
          found = true;
          break;
        }
      }

      if (!found) {
        return false;
      }
    }

    return true;
  }

  private Vector getExceptions() {
    for (int i = 0; i < attributes.size(); i++) {
      JavaAttribute attr = (JavaAttribute) attributes.items[i];
      if (attr.info instanceof Exceptions) {
        return ((Exceptions) attr.info).exceptions;
      }
    }

    return null;
  }

  @Override
  public int length() {
    int len = 8;
    int count = attributes.size();
    for (int i = 0; i < count; i++) {
      len += ((JavaAttribute) attributes.items[i]).length();
    }

    return len;
  }

  @Override
  public void load(DataStream ds) throws IOException {
    accessFlags = ds.readUnsignedShort();
    name = jclass.getConstant(ds.readUnsignedShort(), this);
    descriptor = jclass.getConstant(ds.readUnsignedShort(), this);

    int count = ds.readUnsignedShort();
    attributes.removeAllElements();
    for (int i = 0; i < count; i++) {
      JavaAttribute attribute = new JavaAttribute(jclass);
      attribute.load(ds);

      attributes.addElement(attribute);
    }
  }

  @Override
  public void save(DataStream ds) throws IOException {
    ds.writeShort(accessFlags);
    ds.writeShort(jclass.getConstantIndex(name, this));
    ds.writeShort(jclass.getConstantIndex(descriptor, this));

    int count = attributes.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      JavaAttribute attribute = (JavaAttribute) attributes.items[i];
      attribute.save(ds);
    }
  }
}
