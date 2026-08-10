// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import tc.tools.converter.tclass.TCMethod;

class ExceptionHandlerStackTest {
  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void convertsValidHandlerThatUsesExceptionBeforeStoringIt() throws Exception {
    byte[] bytes = fixtureClass();
    Class<?> verified = new FixtureLoader().define(bytes);
    assertDoesNotThrow(() -> verified.getMethod("handle", Throwable.class).invoke(null, new Throwable("test")));

    GlobalConstantPool.init();
    TCClass converted = new J2TC(new JavaClass(bytes, false), true).converted;
    TCMethod method = findMethod(converted, "handle");
    assertNotNull(method.exceptionHandlers);
    assertNotNull(method.code);
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
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "fixtures/HandlerStack", null, "java/lang/Object", null);
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "handle",
        "(Ljava/lang/Throwable;)V", null, null);
    Label start = new Label();
    Label end = new Label();
    Label handler = new Label();
    method.visitTryCatchBlock(start, end, handler, "java/lang/Throwable");
    method.visitCode();
    method.visitLabel(start);
    method.visitVarInsn(Opcodes.ALOAD, 0);
    method.visitInsn(Opcodes.ATHROW);
    method.visitLabel(end);
    method.visitLabel(handler);
    method.visitInsn(Opcodes.DUP);
    method.visitVarInsn(Opcodes.ASTORE, 1);
    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable", "getMessage",
        "()Ljava/lang/String;", false);
    method.visitInsn(Opcodes.POP);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static final class FixtureLoader extends ClassLoader {
    Class<?> define(byte[] bytes) {
      return defineClass("fixtures.HandlerStack", bytes, 0, bytes.length);
    }
  }
}
