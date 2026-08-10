// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

import java.util.ArrayList;
import java.util.List;

import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;

final class StackMapTableReader {
  private StackMapTableReader() {
  }

  static JavaStackMapFrame[] read(byte[] bytes, JavaConstantPool cp, JavaMethod method)
      throws totalcross.io.IOException {
    try {
      DataStream in = new DataStream(new ByteArrayStream(bytes));
      int count = in.readUnsignedShort();
      List<JavaVerificationType> locals = initialLocals(method);
      JavaStackMapFrame[] frames = new JavaStackMapFrame[count];
      int offset = -1;
      for (int i = 0; i < count; i++) {
        int frameType = in.readUnsignedByte();
        int delta;
        List<JavaVerificationType> stack = new ArrayList<JavaVerificationType>();
        if (frameType <= 63) {
          delta = frameType;
        } else if (frameType <= 127) {
          delta = frameType - 64;
          stack.add(readType(in, cp));
        } else if (frameType == 247) {
          delta = in.readUnsignedShort();
          stack.add(readType(in, cp));
        } else if (frameType >= 248 && frameType <= 250) {
          delta = in.readUnsignedShort();
          int removed = 251 - frameType;
          if (removed > locals.size()) {
            throw malformed(method, "chop frame removes more locals than available");
          }
          for (int j = 0; j < removed; j++) {
            locals.remove(locals.size() - 1);
          }
        } else if (frameType == 251) {
          delta = in.readUnsignedShort();
        } else if (frameType >= 252 && frameType <= 254) {
          delta = in.readUnsignedShort();
          for (int j = 0; j < frameType - 251; j++) {
            locals.add(readType(in, cp));
          }
        } else if (frameType == 255) {
          delta = in.readUnsignedShort();
          locals.clear();
          int localCount = in.readUnsignedShort();
          for (int j = 0; j < localCount; j++) {
            locals.add(readType(in, cp));
          }
          int stackCount = in.readUnsignedShort();
          for (int j = 0; j < stackCount; j++) {
            stack.add(readType(in, cp));
          }
        } else {
          throw malformed(method, "reserved frame type " + frameType);
        }
        offset += delta + 1;
        frames[i] = new JavaStackMapFrame(offset, copy(locals), copy(stack));
      }
      return frames;
    } catch (totalcross.io.IOException e) {
      if (e.getMessage() != null && e.getMessage().startsWith("Malformed StackMapTable")) {
        throw e;
      }
      throw malformed(method, e.getMessage() == null ? "truncated attribute" : e.getMessage());
    } catch (RuntimeException e) {
      throw malformed(method, e.getMessage() == null ? "invalid attribute" : e.getMessage());
    }
  }

  private static List<JavaVerificationType> initialLocals(JavaMethod method) {
    List<JavaVerificationType> locals = new ArrayList<JavaVerificationType>();
    if (!method.isStatic) {
      locals.add(method.name.equals("<init>")
          ? JavaVerificationType.simple(JavaVerificationType.Kind.UNINITIALIZED_THIS)
          : JavaVerificationType.object(method.classOfMethod.className));
    }
    if (method.params != null) {
      for (int i = 0; i < method.params.length; i++) {
        locals.add(fromDescriptor(method.params[i]));
      }
    }
    return locals;
  }

  private static JavaVerificationType fromDescriptor(String descriptor) {
    switch (descriptor.charAt(0)) {
    case 'F':
      return JavaVerificationType.simple(JavaVerificationType.Kind.FLOAT);
    case 'D':
      return JavaVerificationType.simple(JavaVerificationType.Kind.DOUBLE);
    case 'J':
      return JavaVerificationType.simple(JavaVerificationType.Kind.LONG);
    case 'L':
      return JavaVerificationType.object(descriptor.substring(1, descriptor.length() - 1));
    case '[':
      return JavaVerificationType.object(descriptor);
    default:
      return JavaVerificationType.simple(JavaVerificationType.Kind.INTEGER);
    }
  }

  private static JavaVerificationType readType(DataStream in, JavaConstantPool cp)
      throws totalcross.io.IOException {
    switch (in.readUnsignedByte()) {
    case 0:
      return JavaVerificationType.simple(JavaVerificationType.Kind.TOP);
    case 1:
      return JavaVerificationType.simple(JavaVerificationType.Kind.INTEGER);
    case 2:
      return JavaVerificationType.simple(JavaVerificationType.Kind.FLOAT);
    case 3:
      return JavaVerificationType.simple(JavaVerificationType.Kind.DOUBLE);
    case 4:
      return JavaVerificationType.simple(JavaVerificationType.Kind.LONG);
    case 5:
      return JavaVerificationType.simple(JavaVerificationType.Kind.NULL);
    case 6:
      return JavaVerificationType.simple(JavaVerificationType.Kind.UNINITIALIZED_THIS);
    case 7:
      return JavaVerificationType.object(cp.getString1(in.readUnsignedShort()));
    case 8:
      return JavaVerificationType.uninitialized(in.readUnsignedShort());
    default:
      throw malformed(null, "invalid verification type");
    }
  }

  private static JavaVerificationType[] copy(List<JavaVerificationType> values) {
    return values.toArray(new JavaVerificationType[values.size()]);
  }

  private static totalcross.io.IOException malformed(JavaMethod method, String detail) {
    String owner = method == null ? "<unknown>" : method.classOfMethod.className + "." + method.signature;
    return new totalcross.io.IOException("Malformed StackMapTable in " + owner + ": " + detail);
  }
}
