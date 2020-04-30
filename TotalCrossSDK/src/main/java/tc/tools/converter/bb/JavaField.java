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

import tc.tools.converter.bb.constant.UTF8;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.util.Vector;

public class JavaField implements JavaClassStructure {
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
  public static final int ACC_VOLATILE = 0x0040;
  public static final int ACC_TRANSIENT = 0x0080;

  public JavaField(JavaClass jclass) {
    this.jclass = jclass;
    attributes = new Vector();
  }

  @Override
  public String toString() {
    return getName();
  }

  public String getName() {
    return ((UTF8) name.info).value;
  }

  public String getDescriptor() {
    return ((UTF8) descriptor.info).value;
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

  public boolean isVolatile() {
    return (accessFlags & ACC_VOLATILE) == ACC_VOLATILE;
  }

  public boolean isTransient() {
    return (accessFlags & ACC_TRANSIENT) == ACC_TRANSIENT;
  }

  public boolean isEquivalentTo(JavaField other) {
    if (other == null) {
      return false;
    }
    if (accessFlags != other.accessFlags) {
      return false;
    }
    return true;
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
