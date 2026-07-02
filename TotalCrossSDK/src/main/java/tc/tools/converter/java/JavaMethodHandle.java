// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.java;

public final class JavaMethodHandle {
  public static final int REF_GET_FIELD = 1;
  public static final int REF_GET_STATIC = 2;
  public static final int REF_PUT_FIELD = 3;
  public static final int REF_PUT_STATIC = 4;
  public static final int REF_INVOKE_VIRTUAL = 5;
  public static final int REF_INVOKE_STATIC = 6;
  public static final int REF_INVOKE_SPECIAL = 7;
  public static final int REF_NEW_INVOKE_SPECIAL = 8;
  public static final int REF_INVOKE_INTERFACE = 9;

  public final int referenceKind;
  public final int referenceIndex;

  public JavaMethodHandle(JavaConstantPool cp, int constantPoolIndex) {
    JavaConstantInfo info = (JavaConstantInfo) cp.constants[constantPoolIndex];
    if (info.type != 15) {
      throw new IllegalArgumentException("Constant pool entry " + constantPoolIndex + " is not a MethodHandle");
    }
    referenceKind = info.index1;
    referenceIndex = info.index2;
  }

  public String getOwner(JavaConstantPool cp) {
    return cp.getString1(reference(cp).index1);
  }

  public String getName(JavaConstantPool cp) {
    return cp.getString1(reference(cp).index2);
  }

  public String getDescriptor(JavaConstantPool cp) {
    return cp.getString2(reference(cp).index2);
  }

  private JavaConstantInfo reference(JavaConstantPool cp) {
    return (JavaConstantInfo) cp.constants[referenceIndex];
  }
}
