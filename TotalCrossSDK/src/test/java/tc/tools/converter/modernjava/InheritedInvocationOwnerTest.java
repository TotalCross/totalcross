// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.TCConstants;
import tc.tools.converter.TCValue;
import tc.tools.converter.bb.InvalidClassException;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.ir.Instruction.Call;
import tc.tools.converter.ir.Instruction.Instruction;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCMethod;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStreamLE;

class InheritedInvocationOwnerTest {
  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void validatesInheritedDeclarationWithoutRewritingVirtualCallOwner() throws Exception {
    byte[] fixtureBytes = fixtureClass();
    Class<?> verified = new FixtureLoader().define(fixtureBytes);
    java.util.Properties properties = new java.util.Properties();
    verified.getMethod("inheritedPut", java.util.Properties.class).invoke(null, properties);
    assertEquals("value", properties.get("key"), "fixture must execute as valid JVM bytecode");

    TCClass converted = convertFixture();
    TCMethod inherited = findMethod(converted, "inheritedPut");
    Call call = findCall(inherited);
    int[] reference = (int[]) ((TCValue) GlobalConstantPool.getMtdRef(call.sym)).asObj;

    assertEquals("java.util.Properties", GlobalConstantPool.getClassName(reference[0]));
    assertEquals(TCConstants.CALL_virtual, call.opcode);
    inherited.write(new DataStreamLE(new ByteArrayStream(256)));
  }

  @Test
  void keepsPreciseFailureForUnresolvedInheritedMember() throws Exception {
    TCMethod missing = findMethod(convertFixture(), "missing");
    assertThrows(InvalidClassException.class,
        () -> missing.write(new DataStreamLE(new ByteArrayStream(256))));
  }

  private static TCClass convertFixture() throws Exception {
    GlobalConstantPool.init();
    boolean previous = TCMethod.checkJavaCalls;
    TCMethod.checkJavaCalls = false;
    try {
      return new J2TC(new JavaClass(fixtureClass(), false), true).converted;
    } finally {
      TCMethod.checkJavaCalls = previous;
    }
  }

  private static Call findCall(TCMethod method) {
    for (int i = 0; i < method.insts.size(); i++) {
      Instruction instruction = (Instruction) method.insts.items[i];
      if (instruction instanceof Call) {
        return (Call) instruction;
      }
    }
    throw new AssertionError("Converted call not found");
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
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "fixtures/InheritedOwner", null, "java/lang/Object", null);
    addCall(writer, "inheritedPut", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
    addCall(writer, "missing", "missingMethod", "()V", false);
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static void addCall(ClassWriter writer, String methodName, String targetName, String descriptor,
      boolean arguments) {
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName,
        "(Ljava/util/Properties;)V", null, null);
    method.visitCode();
    method.visitVarInsn(Opcodes.ALOAD, 0);
    if (arguments) {
      method.visitLdcInsn("key");
      method.visitLdcInsn("value");
    }
    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Properties", targetName, descriptor, false);
    if (arguments) {
      method.visitInsn(Opcodes.POP);
    }
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
  }

  private static final class FixtureLoader extends ClassLoader {
    Class<?> define(byte[] bytes) {
      return defineClass("fixtures.InheritedOwner", bytes, 0, bytes.length);
    }
  }
}
