// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.MethodDeclarationResolver;
import tc.tools.converter.TCConstants;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.metadata.CompilationMetadata.CallSiteMetadata;
import tc.tools.converter.metadata.CompilationMetadata.ClassMetadata;
import tc.tools.converter.metadata.CompilationMetadata.MethodMetadata;
import tc.tools.converter.metadata.CompilationMetadata.NativeKind;
import tc.tools.converter.metadata.CompilationMetadata.OriginRange;
import tc.tools.converter.tclass.TCMethod;
import totalcross.util.Vector;

class CompilationMetadataCaptureTest {
  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void preservesSourceFactsAcrossLoweringWithoutChangingTcMethods() throws Exception {
    GlobalConstantPool.init();
    J2TC.resetCompilationMetadata();
    boolean previous = TCMethod.checkJavaCalls;
    TCMethod.checkJavaCalls = false;
    try {
      new J2TC(new JavaClass(fixtureClass(), false, true), true);
    } finally {
      TCMethod.checkJavaCalls = previous;
    }

    CompilationMetadata metadata = J2TC.getCompilationMetadata();
    assertEquals(1, metadata.classes.size());
    ClassMetadata type = metadata.classes.get(0);
    assertEquals("fixtures/MetadataFixture", type.originalName);
    assertEquals(type.originalName, type.effectiveName);
    assertEquals("MetadataFixture.java", type.sourceFile);
    assertEquals("<T:Ljava/lang/Object;>Ljava/lang/Object;", type.signature);
    assertEquals(Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, type.rawAccessFlags);
    assertEquals(1, type.fields.size());
    assertEquals("&I", type.fields.get(0).loweredType);
    assertEquals(Integer.valueOf(7), type.fields.get(0).constantValue);
    assertTrue(type.fields.get(0).tcFieldSymbol >= 0);

    MethodMetadata sample = method(type, "sample");
    assertEquals("(FLjava/util/LinkedHashMap;D)I", sample.javaDescriptor);
    assertEquals("F", sample.sourceParameterDescriptors.get(0));
    assertEquals("&D", sample.loweredParameterTypes.get(0));
    assertEquals("D", sample.sourceParameterDescriptors.get(2));
    assertEquals("&D", sample.loweredParameterTypes.get(2));
    assertEquals("java.util.LinkedHashMap", sample.loweredParameterTypes.get(1));
    assertEquals(NativeKind.NONE, sample.nativeKind);
    assertTrue(sample.tcMethodNameSymbol >= 0);

    CallSiteMetadata inherited = call(sample, "put");
    assertEquals(CompilationMetadata.InvokeKind.VIRTUAL, inherited.invokeKind);
    assertEquals("java/util/LinkedHashMap", inherited.symbolicOwner);
    assertEquals("java/util/HashMap", inherited.resolvedDeclarationOwner);
    assertEquals(MethodDeclarationResolver.resolve(inherited.symbolicOwner, inherited.name,
        inherited.javaDescriptor).declarationOwner, inherited.resolvedDeclarationOwner);
    assertEquals(TCConstants.CALL_virtual, inherited.loweredOpcode);
    assertTrue(inherited.tcStartSlot >= 0);
    assertTrue(inherited.tcEndSlotExclusive > inherited.tcStartSlot);

    OriginRange allocation = allocation(sample, "java/util/LinkedHashMap");
    assertTrue(allocation.tcStartSlot >= 0);
    assertTrue(allocation.tcEndSlotExclusive > allocation.tcStartSlot);
    CallSiteMetadata constructor = call(sample, "<init>");
    assertEquals("java/util/LinkedHashMap", constructor.symbolicOwner);
    assertEquals(constructor.symbolicOwner, constructor.resolvedDeclarationOwner);
    assertTrue(metadata.resolvedClassForNameRoots.contains("fixtures/MetadataTarget"));
    assertTrue(metadata.unresolvedDynamicClassLookup);
    CallSiteMetadata interfaceCall = call(method(type, "interfaceSize"), "size");
    assertEquals(CompilationMetadata.InvokeKind.INTERFACE, interfaceCall.invokeKind);
    assertEquals(TCConstants.CALL_normal, interfaceCall.loweredOpcode);
    assertEquals(NativeKind.JAVA_NATIVE, method(type, "nativeMethod").nativeKind);
    assertEquals(NativeKind.REPLACED_ON_DEPLOY, method(type, "replacedMethod").nativeKind);
    OriginRange promotedBranch = origin(method(type, "promotedBranch"), Opcodes.IF_ICMPEQ);
    assertTrue(promotedBranch.tcEndSlotExclusive - promotedBranch.tcStartSlot >= 2,
        "promoted branch instructions must retain their shared Java origin");
    J2TC.disableCompilationMetadata();
  }

  @Test
  void disabledCaptureRetainsDeploySemanticsWithoutMetadataOrOriginTags() throws Exception {
    GlobalConstantPool.init();
    J2TC.disableCompilationMetadata();
    J2TC.callForName = new Vector(4);
    boolean previous = TCMethod.checkJavaCalls;
    TCMethod.checkJavaCalls = false;
    J2TC conversion;
    try {
      conversion = new J2TC(new JavaClass(fixtureClass(), false), true);
    } finally {
      TCMethod.checkJavaCalls = previous;
    }

    assertTrue(J2TC.getCompilationMetadata().classes.isEmpty());
    assertTrue(J2TC.callForName.size() > 0, "Class.forName deploy discovery must remain active");
    for (TCMethod method : conversion.converted.methods) {
      if (method.insts == null) continue;
      for (int i = 0; i < method.insts.size(); i++) {
        tc.tools.converter.ir.Instruction.Instruction instruction =
            (tc.tools.converter.ir.Instruction.Instruction) method.insts.items[i];
        assertEquals(-1, instruction.javaPc);
        assertEquals(-1, instruction.javaOpcode);
      }
    }
  }

  private static MethodMetadata method(ClassMetadata type, String name) {
    for (MethodMetadata method : type.methods) {
      if (name.equals(method.originalName)) {
        return method;
      }
    }
    throw new AssertionError("Missing method metadata: " + name);
  }

  private static CallSiteMetadata call(MethodMetadata method, String name) {
    for (CallSiteMetadata call : method.callSites) {
      if (name.equals(call.name)) {
        return call;
      }
    }
    throw new AssertionError("Missing call metadata: " + name);
  }

  private static OriginRange allocation(MethodMetadata method, String type) {
    for (OriginRange origin : method.origins) {
      if (type.equals(origin.allocationType)) {
        return origin;
      }
    }
    throw new AssertionError("Missing allocation metadata: " + type);
  }

  private static OriginRange origin(MethodMetadata method, int javaOpcode) {
    for (OriginRange origin : method.origins) {
      if (origin.javaOpcode == javaOpcode) {
        return origin;
      }
    }
    throw new AssertionError("Missing origin for opcode: " + javaOpcode);
  }

  private static byte[] fixtureClass() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "fixtures/MetadataFixture",
        "<T:Ljava/lang/Object;>Ljava/lang/Object;", "java/lang/Object", null);
    writer.visitSource("MetadataFixture.java", null);
    FieldVisitor field = writer.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "VALUE", "I",
        null, Integer.valueOf(7));
    field.visitEnd();

    MethodVisitor sample = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "sample",
        "(FLjava/util/LinkedHashMap;D)I", null, null);
    sample.visitCode();
    sample.visitLdcInsn("fixtures.MetadataTarget");
    sample.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
        "(Ljava/lang/String;)Ljava/lang/Class;", false);
    sample.visitInsn(Opcodes.POP);
    sample.visitTypeInsn(Opcodes.NEW, "java/util/LinkedHashMap");
    sample.visitInsn(Opcodes.DUP);
    sample.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/LinkedHashMap", "<init>", "()V", false);
    sample.visitInsn(Opcodes.POP);
    sample.visitVarInsn(Opcodes.ALOAD, 1);
    sample.visitLdcInsn("key");
    sample.visitLdcInsn("value");
    sample.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/LinkedHashMap", "put",
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
    sample.visitInsn(Opcodes.POP);
    sample.visitInsn(Opcodes.ICONST_1);
    sample.visitInsn(Opcodes.IRETURN);
    sample.visitMaxs(0, 0);
    sample.visitEnd();

    MethodVisitor dynamic = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "dynamicForName",
        "(Ljava/lang/String;)V", null, null);
    dynamic.visitCode();
    dynamic.visitVarInsn(Opcodes.ALOAD, 0);
    dynamic.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
        "(Ljava/lang/String;)Ljava/lang/Class;", false);
    dynamic.visitInsn(Opcodes.POP);
    dynamic.visitInsn(Opcodes.RETURN);
    dynamic.visitMaxs(0, 0);
    dynamic.visitEnd();

    MethodVisitor promotedBranch = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "promotedBranch",
        "(I)I", null, null);
    promotedBranch.visitCode();
    Label equal = new Label();
    promotedBranch.visitVarInsn(Opcodes.ILOAD, 0);
    promotedBranch.visitIntInsn(Opcodes.BIPUSH, 100);
    promotedBranch.visitJumpInsn(Opcodes.IF_ICMPEQ, equal);
    for (int i = 0; i < 40; i++) {
      promotedBranch.visitIincInsn(0, 1);
    }
    promotedBranch.visitInsn(Opcodes.ICONST_0);
    promotedBranch.visitInsn(Opcodes.IRETURN);
    promotedBranch.visitLabel(equal);
    promotedBranch.visitInsn(Opcodes.ICONST_1);
    promotedBranch.visitInsn(Opcodes.IRETURN);
    promotedBranch.visitMaxs(0, 0);
    promotedBranch.visitEnd();

    MethodVisitor interfaceSize = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "interfaceSize",
        "(Ljava/util/List;)I", null, null);
    interfaceSize.visitCode();
    interfaceSize.visitVarInsn(Opcodes.ALOAD, 0);
    interfaceSize.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "size", "()I", true);
    interfaceSize.visitInsn(Opcodes.IRETURN);
    interfaceSize.visitMaxs(0, 0);
    interfaceSize.visitEnd();

    MethodVisitor nativeMethod = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE,
        "nativeMethod", "()V", null, null);
    nativeMethod.visitEnd();
    MethodVisitor replaced = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "replacedMethod", "()V",
        null, null);
    AnnotationVisitor annotation = replaced.visitAnnotation(
        "Lcom/totalcross/annotations/ReplacedByNativeOnDeploy;", false);
    assertNotNull(annotation);
    annotation.visitEnd();
    replaced.visitCode();
    replaced.visitInsn(Opcodes.RETURN);
    replaced.visitMaxs(0, 0);
    replaced.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }
}
