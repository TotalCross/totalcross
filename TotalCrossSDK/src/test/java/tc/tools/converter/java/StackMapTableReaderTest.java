// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.metadata.CompilationMetadata;
import tc.tools.converter.metadata.CompilationMetadataCollector;

class StackMapTableReaderTest {
  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void parsesCompactChopAppendFullObjectAndUninitializedFrames() throws Exception {
    byte[] bytes = validFixture();
    Class<?> fixture = new FixtureLoader().define(bytes);
    assertDoesNotThrow(() -> fixture.getMethod("same", float.class, double.class, long.class, Object.class)
        .invoke(null, 1.0f, 2.0d, 3L, new Object()));
    assertDoesNotThrow(() -> fixture.getMethod("sameOne").invoke(null));
    assertDoesNotThrow(() -> fixture.getMethod("appendChop").invoke(null));
    assertDoesNotThrow(() -> fixture.getMethod("full", float.class, double.class, long.class, Object.class)
        .invoke(null, 1.0f, 2.0d, 3L, new Object()));
    assertDoesNotThrow(() -> fixture.getMethod("objectFrame", Object.class).invoke(null, new Object()));
    assertDoesNotThrow(() -> fixture.getMethod("uninitialized").invoke(null));

    JavaClass parsed = new JavaClass(bytes, false);
    assertEquals(1, method(parsed, "same").code.stackMapFrames.length);
    assertEquals(1, method(parsed, "sameOne").code.stackMapFrames[0].stack.length);
    assertEquals(1, method(parsed, "appendChop").code.stackMapFrames[0].locals.length);
    assertEquals(0, method(parsed, "appendChop").code.stackMapFrames[1].locals.length);

    EnumSet<JavaVerificationType.Kind> kinds = EnumSet.noneOf(JavaVerificationType.Kind.class);
    for (JavaMethod method : parsed.methods) {
      if (method.code == null || method.code.stackMapFrames == null) {
        continue;
      }
      for (JavaStackMapFrame frame : method.code.stackMapFrames) {
        addKinds(kinds, frame.locals);
        addKinds(kinds, frame.stack);
      }
    }
    assertTrue(kinds.contains(JavaVerificationType.Kind.FLOAT));
    assertTrue(kinds.contains(JavaVerificationType.Kind.DOUBLE));
    assertTrue(kinds.contains(JavaVerificationType.Kind.LONG));
    assertTrue(kinds.contains(JavaVerificationType.Kind.OBJECT));
    assertTrue(kinds.contains(JavaVerificationType.Kind.UNINITIALIZED));

    CompilationMetadataCollector collector = new CompilationMetadataCollector();
    collector.captureClass(parsed, parsed.className);
    CompilationMetadata.MethodMetadata full = metadataMethod(collector.snapshot(), "full");
    assertEquals("FLOAT", full.verificationFrames.get(0).locals.get(0).kind);
    assertEquals("DOUBLE", full.verificationFrames.get(0).locals.get(1).kind);
    assertEquals("LONG", full.verificationFrames.get(0).locals.get(2).kind);
  }

  @Test
  void reportsMalformedFrameTypeWithMethodContext() throws Exception {
    totalcross.io.IOException error = assertThrows(totalcross.io.IOException.class,
        () -> new JavaClass(malformedFixture(), false));
    assertTrue(error.getMessage().startsWith("Malformed StackMapTable in fixtures/MalformedFrames.run()"));
    assertTrue(error.getMessage().contains("reserved frame type 200"));
  }

  private static void addKinds(EnumSet<JavaVerificationType.Kind> kinds, JavaVerificationType[] values) {
    for (JavaVerificationType value : values) {
      kinds.add(value.kind);
    }
  }

  private static JavaMethod method(JavaClass owner, String name) {
    for (JavaMethod method : owner.methods) {
      if (name.equals(method.name)) {
        return method;
      }
    }
    throw new AssertionError("Missing method: " + name);
  }

  private static CompilationMetadata.MethodMetadata metadataMethod(CompilationMetadata metadata, String name) {
    for (CompilationMetadata.MethodMetadata method : metadata.classes.get(0).methods) {
      if (name.equals(method.originalName)) {
        return method;
      }
    }
    throw new AssertionError("Missing method metadata: " + name);
  }

  private static byte[] validFixture() {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "fixtures/StackMapFrames", null, "java/lang/Object", null);
    addSame(writer);
    addSameOne(writer);
    addAppendChop(writer);
    addFull(writer);
    addObject(writer);
    addUninitialized(writer);
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static void addSame(ClassWriter writer) {
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "same",
        "(FDJLjava/lang/Object;)V", null, null);
    method.visitCode();
    Label target = new Label();
    method.visitJumpInsn(Opcodes.GOTO, target);
    method.visitLabel(target);
    method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(0, 6);
    method.visitEnd();
  }

  private static void addSameOne(ClassWriter writer) {
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "sameOne", "()V", null, null);
    method.visitCode();
    Label target = new Label();
    method.visitInsn(Opcodes.ACONST_NULL);
    method.visitJumpInsn(Opcodes.GOTO, target);
    method.visitLabel(target);
    method.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { Opcodes.NULL });
    method.visitInsn(Opcodes.POP);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(1, 0);
    method.visitEnd();
  }

  private static void addAppendChop(ClassWriter writer) {
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "appendChop", "()V", null,
        null);
    method.visitCode();
    method.visitInsn(Opcodes.ICONST_0);
    method.visitVarInsn(Opcodes.ISTORE, 0);
    Label appended = new Label();
    method.visitJumpInsn(Opcodes.GOTO, appended);
    method.visitLabel(appended);
    method.visitFrame(Opcodes.F_APPEND, 1, new Object[] { Opcodes.INTEGER }, 0, null);
    Label chopped = new Label();
    method.visitJumpInsn(Opcodes.GOTO, chopped);
    method.visitLabel(chopped);
    method.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(1, 1);
    method.visitEnd();
  }

  private static void addFull(ClassWriter writer) {
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "full",
        "(FDJLjava/lang/Object;)V", null, null);
    method.visitCode();
    Label target = new Label();
    method.visitJumpInsn(Opcodes.GOTO, target);
    method.visitLabel(target);
    method.visitFrame(Opcodes.F_FULL, 4,
        new Object[] { Opcodes.FLOAT, Opcodes.DOUBLE, Opcodes.LONG, "java/lang/Object" }, 0, null);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(0, 6);
    method.visitEnd();
  }

  private static void addObject(ClassWriter writer) {
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "objectFrame",
        "(Ljava/lang/Object;)V", null, null);
    method.visitCode();
    method.visitVarInsn(Opcodes.ALOAD, 0);
    Label target = new Label();
    method.visitJumpInsn(Opcodes.GOTO, target);
    method.visitLabel(target);
    method.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Object" });
    method.visitInsn(Opcodes.POP);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(1, 1);
    method.visitEnd();
  }

  private static void addUninitialized(ClassWriter writer) {
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "uninitialized", "()V", null,
        null);
    method.visitCode();
    Label allocation = new Label();
    method.visitLabel(allocation);
    method.visitTypeInsn(Opcodes.NEW, "java/lang/Object");
    method.visitInsn(Opcodes.DUP);
    Label target = new Label();
    method.visitJumpInsn(Opcodes.GOTO, target);
    method.visitLabel(target);
    method.visitFrame(Opcodes.F_FULL, 0, null, 2, new Object[] { allocation, allocation });
    method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    method.visitInsn(Opcodes.POP);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(2, 0);
    method.visitEnd();
  }

  private static byte[] malformedFixture() {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "fixtures/MalformedFrames", null, "java/lang/Object", null);
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()V", null, null);
    method.visitCode();
    method.visitInsn(Opcodes.RETURN);
    method.visitAttribute(new RawStackMapAttribute());
    method.visitMaxs(0, 0);
    method.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }

  private static final class RawStackMapAttribute extends Attribute {
    RawStackMapAttribute() {
      super("StackMapTable");
    }

    @Override
    public boolean isCodeAttribute() {
      return true;
    }

    @Override
    protected ByteVector write(ClassWriter writer, byte[] code, int codeLength, int maxStack, int maxLocals) {
      return new ByteVector().putShort(1).putByte(200);
    }
  }

  private static final class FixtureLoader extends ClassLoader {
    Class<?> define(byte[] bytes) {
      return defineClass("fixtures.StackMapFrames", bytes, 0, bytes.length);
    }
  }
}
