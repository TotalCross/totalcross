// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCCode;
import tc.tools.converter.tclass.TCLineNumber;
import tc.tools.converter.tclass.TCMethod;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStreamLE;

class SparseLineNumberTableTest {
  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void preservesUnknownAndSparseLineRegions() throws Exception {
    GlobalConstantPool.init();
    TCClass converted = new J2TC(new JavaClass(fixtureClass(), false), true).converted;

    assertLineSequence(converted, "noTable", 0);
    assertLineSequence(converted, "atZero", 10);
    assertLineSequence(converted, "startsAfterZero", 0, 20);
    TCMethod multiple = assertLineSequence(converted, "multipleSparse", 0, 30, 40);

    assertStructurallyValid(multiple);
    ByteArrayStream serialized = new ByteArrayStream(256);
    multiple.write(new DataStreamLE(serialized));
    assertEquals(0, multiple.lineNumbers[0].lineNumber,
        "serialization must not rewrite unknown source lines to a later entry");
  }

  private static TCMethod assertLineSequence(TCClass converted, String name, int... expected) {
    TCMethod method = findMethod(converted, name);
    int[] distinct = new int[method.code.length];
    int count = 0;
    int previous = Integer.MIN_VALUE;
    for (TCCode code : method.code) {
      if (code.line != previous) {
        distinct[count++] = code.line;
        previous = code.line;
      }
    }
    int[] actual = new int[count];
    System.arraycopy(distinct, 0, actual, 0, count);
    assertArrayEquals(expected, actual, name);
    return method;
  }

  private static void assertStructurallyValid(TCMethod method) {
    int previousPc = -1;
    for (TCLineNumber line : method.lineNumbers) {
      assertTrue(line.startPC > previousPc, "line PCs must increase");
      assertTrue(line.startPC < method.code.length, "line PC must address emitted code");
      previousPc = line.startPC;
    }
  }

  private static TCMethod findMethod(TCClass converted, String name) {
    for (TCMethod method : converted.methods) {
      if (name.equals(GlobalConstantPool.getMethodFieldName(method.cpName))) {
        return method;
      }
    }
    throw new AssertionError("Converted method not found: " + name);
  }

  private static byte[] fixtureClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "fixtures/SparseLines", null, "java/lang/Object", null);
    addMethod(writer, "noTable", 0, 0);
    addMethod(writer, "atZero", 10, 0);
    addMethod(writer, "startsAfterZero", 20, 1);
    addMethod(writer, "multipleSparse", 30, 2);
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static void addMethod(ClassWriter writer, String name, int firstLine, int shape) {
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, "(I)I", null, null);
    method.visitCode();
    if (shape == 0 && firstLine != 0) {
      markLine(method, firstLine);
    }
    method.visitIincInsn(0, 1);
    if (shape > 0) {
      markLine(method, firstLine);
      method.visitIincInsn(0, 1);
    }
    if (shape > 1) {
      method.visitIincInsn(0, 1);
      markLine(method, 40);
      method.visitIincInsn(0, 1);
      method.visitIincInsn(0, 1);
    }
    method.visitVarInsn(Opcodes.ILOAD, 0);
    method.visitInsn(Opcodes.IRETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
  }

  private static void markLine(MethodVisitor method, int line) {
    Label label = new Label();
    method.visitLabel(label);
    method.visitLineNumber(line, label);
  }
}
