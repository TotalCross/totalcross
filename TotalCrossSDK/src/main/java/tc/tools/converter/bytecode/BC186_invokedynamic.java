// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.bytecode;

import tc.tools.converter.TCValue;
import tc.tools.converter.java.JavaConstantInfo;
import tc.tools.converter.java.JavaMethod;

public class BC186_invokedynamic extends ByteCode {
  public int constantPoolIndex;
  public int bootstrapMethodAttrIndex;
  public int nameAndTypeIndex;
  public String name, descriptor, ret, signature;
  public String[] jargs;
  public TCValue[] args;
  private static StringBuffer retsb = new StringBuffer(30);

  public BC186_invokedynamic() {
    constantPoolIndex = readUInt16(pc + 1);
    JavaConstantInfo jci = (JavaConstantInfo) cp.constants[constantPoolIndex];
    bootstrapMethodAttrIndex = jci.index1;
    nameAndTypeIndex = jci.index2;
    name = cp.getString1(nameAndTypeIndex);
    descriptor = cp.getString2(nameAndTypeIndex);
    pcInc = 5;
    parseDescriptor();
    targetType = convertJavaType(ret);
  }

  private void parseDescriptor() {
    jargs = JavaMethod.splitParams(descriptor, retsb);
    ret = retsb.toString();
    signature = name + descriptor.substring(0, descriptor.length() - ret.length());
    if (jargs != null) {
      args = new TCValue[jargs.length];
      for (int j = 0; j < args.length; j++) {
        String p = jargs[j];
        int t = convertJavaType(p);
        args[j] = new TCValue();
        args[j].asInt = t;
        if (t == OBJECT) {
          args[j].asObj = p;
        }
      }
    }
  }

  @Override
  public void exec() {
  }
}
