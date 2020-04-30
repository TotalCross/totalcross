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
package tc.tools.converter.java;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import totalcross.io.DataStream;

public final class JavaField {
  public String name, type;
  public Object constantValue;
  public boolean isPublic, isPrivate, isProtected, isStatic, isFinal, isVolatile, isTransient, is64bit;

  public JavaField(DataStream ds, JavaConstantPool cp) throws totalcross.io.IOException {
    // access flags
    int f = ds.readUnsignedShort();
    isPublic = (f & 0x1) != 0;
    isPrivate = (f & 0x2) != 0;
    isProtected = (f & 0x4) != 0;
    isStatic = (f & 0x8) != 0;
    isFinal = (f & 0x10) != 0;
    isVolatile = (f & 0x40) != 0;
    isTransient = (f & 0x80) != 0;

    name = (String) cp.constants[ds.readUnsignedShort()];
    type = (String) cp.constants[ds.readUnsignedShort()];
    is64bit = type.charAt(0) == 'D' || type.charAt(0) == 'J'; // double or long
    // read the attributes
    int n = ds.readUnsignedShort();
    for (int i = 0; i < n; i++) {
      String attr = (String) cp.constants[ds.readUnsignedShort()];
      int len = ds.readInt();
      if (attr.equals("ConstantValue") && len == 2) {
        constantValue = cp.constants[ds.readUnsignedShort()];
      } else {
        ds.skipBytes(len);
      }
    }
  }

  public JavaField(FieldNode fieldNode) {
    isPublic = ((fieldNode.access & Opcodes.ACC_PUBLIC) != 0);
    isPrivate = ((fieldNode.access & Opcodes.ACC_PRIVATE) != 0);
    isProtected = ((fieldNode.access & Opcodes.ACC_PROTECTED) != 0);
    isStatic = ((fieldNode.access & Opcodes.ACC_STATIC) != 0);
    isFinal = ((fieldNode.access & Opcodes.ACC_FINAL) != 0);
    isVolatile = ((fieldNode.access & Opcodes.ACC_VOLATILE) != 0);
    isTransient = ((fieldNode.access & Opcodes.ACC_TRANSIENT) != 0);

    name = fieldNode.name;

    type = fieldNode.desc;

    is64bit = fieldNode.desc.charAt(0) == 'D' || fieldNode.desc.charAt(0) == 'J';
    constantValue = fieldNode.value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (isPrivate) {
      sb.append("private ");
    }
    if (isPublic) {
      sb.append("public ");
    }
    if (isProtected) {
      sb.append("protected ");
    }
    if (isStatic) {
      sb.append("static ");
    }
    if (isFinal) {
      sb.append("final ");
    }
    if (isVolatile) {
      sb.append("volatile ");
    }
    if (isTransient) {
      sb.append("transient ");
    }

    sb.append(type);
    sb.append(' ').append(name);
    if (constantValue != null) {
      sb.append(" = ").append(constantValue);
    }
    sb.append(";");

    return sb.toString();
  }
}
