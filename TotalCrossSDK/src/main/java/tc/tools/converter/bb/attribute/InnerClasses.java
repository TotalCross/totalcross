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

public class InnerClasses implements AttributeInfo {
  private JavaClass jclass;

  public Vector classes;

  public InnerClasses(JavaClass jclass) {
    this.jclass = jclass;
    classes = new Vector();
  }

  @Override
  public int length() {
    return 2 + (classes.size() * 8);
  }

  @Override
  public void load(DataStream ds) throws IOException {
    int count = ds.readUnsignedShort();
    classes.removeAllElements();
    for (int i = 0; i < count; i++) {
      Classes innerClasses = new Classes();
      innerClasses.load(ds);
      classes.addElement(innerClasses);
    }
  }

  @Override
  public void save(DataStream ds) throws IOException {
    int count = classes.size();
    ds.writeShort(count);
    for (int i = 0; i < count; i++) {
      ((Classes) classes.items[i]).save(ds);
    }
  }

  public class Classes implements JavaClassStructure {
    public JavaConstant innerClass;
    public JavaConstant outerClass;
    public JavaConstant innerName;
    public int innerClassAccessFlags;

    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_INTERFACE = 0x0200;
    public static final int ACC_ABSTRACT = 0x0400;

    public boolean isPublic() {
      return (innerClassAccessFlags & ACC_PUBLIC) == ACC_PUBLIC;
    }

    public boolean isPrivate() {
      return (innerClassAccessFlags & ACC_PRIVATE) == ACC_PRIVATE;
    }

    public boolean isProtected() {
      return (innerClassAccessFlags & ACC_PROTECTED) == ACC_PROTECTED;
    }

    public boolean isStatic() {
      return (innerClassAccessFlags & ACC_STATIC) == ACC_STATIC;
    }

    public boolean isFinal() {
      return (innerClassAccessFlags & ACC_FINAL) == ACC_FINAL;
    }

    public boolean isInterface() {
      return (innerClassAccessFlags & ACC_INTERFACE) == ACC_INTERFACE;
    }

    public boolean isAbstract() {
      return (innerClassAccessFlags & ACC_ABSTRACT) == ACC_ABSTRACT;
    }

    @Override
    public int length() {
      return 8;
    }

    @Override
    public void load(DataStream ds) throws IOException {
      int index = ds.readUnsignedShort();
      innerClass = index == 0 ? null : jclass.getConstant(index, this);

      index = ds.readUnsignedShort();
      outerClass = index == 0 ? null : jclass.getConstant(index, this);

      index = ds.readUnsignedShort();
      innerName = index == 0 ? null : jclass.getConstant(index, this);

      innerClassAccessFlags = ds.readUnsignedShort();
    }

    @Override
    public void save(DataStream ds) throws IOException {
      ds.writeShort(innerClass == null ? 0 : jclass.getConstantIndex(innerClass, this));
      ds.writeShort(outerClass == null ? 0 : jclass.getConstantIndex(outerClass, this));
      ds.writeShort(innerName == null ? 0 : innerName == null ? 0 : jclass.getConstantIndex(innerName, this));
      ds.writeShort(innerClassAccessFlags);
    }
  }
}
