// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.metadata.CompilationMetadata.CallSiteMetadata;
import tc.tools.converter.metadata.CompilationMetadata.MethodMetadata;
import tc.tools.converter.tclass.TCMethod;

class MethodDeclarationResolverTest {
  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void resolvesProgramClassesAndInterfacesWithoutChangingSymbolicOwner() throws Exception {
    MethodDeclarationResolver.beginConversionRun();
    register(programType("fixtures/ProgramBase", "java/lang/Object", null, "inherited"));
    register(programType("fixtures/ProgramChild", "fixtures/ProgramBase", null, null));
    MethodDeclarationResolver.Resolution inherited =
        MethodDeclarationResolver.resolve("fixtures/ProgramChild", "inherited", "()V");
    assertEquals("fixtures/ProgramChild", inherited.symbolicOwner);
    assertEquals("fixtures/ProgramBase", inherited.declarationOwner);

    register(programType("fixtures/ProgramInterface", "java/lang/Object", new String[0], "defaultMethod"));
    register(programType("fixtures/ProgramImpl", "java/lang/Object",
        new String[] { "fixtures/ProgramInterface" }, null));
    assertEquals("fixtures/ProgramInterface",
        MethodDeclarationResolver.resolve("fixtures/ProgramImpl", "defaultMethod", "()V").declarationOwner);
  }

  @Test
  void resolvesMappedDeviceDeclarationsAndKeepsConstructorsOnTheirOwner() {
    MethodDeclarationResolver.beginConversionRun();
    MethodDeclarationResolver.Resolution properties = MethodDeclarationResolver.resolve("java/util/Properties",
        "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    assertEquals("java/util/Properties", properties.symbolicOwner);
    assertEquals("java/util/Hashtable", properties.declarationOwner);
    assertTrue(properties.deviceMemberFound);

    MethodDeclarationResolver.Resolution linked = MethodDeclarationResolver.resolve("java/util/LinkedHashMap",
        "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    assertEquals("java/util/LinkedHashMap", linked.symbolicOwner);
    assertEquals("java/util/HashMap", linked.declarationOwner);

    MethodDeclarationResolver.Resolution constructor =
        MethodDeclarationResolver.resolve("java/util/Properties", "<init>", "()V");
    assertEquals("java/util/Properties", constructor.declarationOwner);
  }

  @Test
  void leavesUnavailableSourceFactsUnresolvedInsteadOfUsingHostJdk() {
    MethodDeclarationResolver.beginConversionRun();
    MethodDeclarationResolver.Resolution hostOnly =
        MethodDeclarationResolver.resolve("java/lang/Thread", "onSpinWait", "()V");
    assertNull(hostOnly.declarationOwner);
    assertTrue(hostOnly.deviceClassFound);
  }

  @Test
  void followsOnlyMappedDeviceHierarchyForInheritedApiMethods() {
    assertMappedDeclaration("java/io/ByteArrayInputStream", "close", "()V", "java/io/InputStream");
    assertMappedDeclaration("java/io/IOException", "printStackTrace", "()V", "java/lang/Throwable");
    assertMappedDeclaration("java/io/UnsupportedEncodingException", "initCause",
        "(Ljava/lang/Throwable;)Ljava/lang/Throwable;", "java/lang/Throwable");
    assertMappedDeclaration("java/util/LinkedList", "iterator", "()Ljava/util/Iterator;",
        "java/util/AbstractSequentialList");
    assertMappedDeclaration("java/lang/InternalError", "initCause",
        "(Ljava/lang/Throwable;)Ljava/lang/Throwable;", "java/lang/Throwable");
    assertMappedDeclaration("java/nio/charset/UnsupportedCharsetException", "getMessage", "()Ljava/lang/String;",
        "java/lang/Throwable");
    assertMappedDeclaration("javax/crypto/SecretKeyFactorySpi", "engineGenerateSecret",
        "(Ljava/security/spec/KeySpec;)Ljavax/crypto/SecretKey;", "javax/crypto/SecretKeyFactorySpi");
  }

  @Test
  void tcmUsesTheSameProgramDeclarationResult() throws Exception {
    GlobalConstantPool.init();
    J2TC.resetCompilationMetadata();
    register(programType("fixtures/ProgramBase", "java/lang/Object", null, "inherited"));
    register(programType("fixtures/ProgramChild", "fixtures/ProgramBase", null, null));
    JavaClass caller = new JavaClass(caller(), false, true);
    boolean previous = TCMethod.checkJavaCalls;
    TCMethod.checkJavaCalls = false;
    try {
      new J2TC(caller, true);
    } finally {
      TCMethod.checkJavaCalls = previous;
    }

    MethodMetadata method = J2TC.getCompilationMetadata().classes.get(0).methods.get(0);
    CallSiteMetadata call = method.callSites.get(0);
    MethodDeclarationResolver.Resolution shared = MethodDeclarationResolver.resolve(call.symbolicOwner, call.name,
        call.javaDescriptor);
    assertEquals("fixtures/ProgramChild", call.symbolicOwner);
    assertEquals(shared.declarationOwner, call.resolvedDeclarationOwner);
    assertEquals("fixtures/ProgramBase", call.resolvedDeclarationOwner);
    J2TC.disableCompilationMetadata();
  }

  private static void register(JavaClass type) {
    MethodDeclarationResolver.registerProgramClass(type);
  }

  private static void assertMappedDeclaration(String owner, String name, String descriptor, String declaration) {
    MethodDeclarationResolver.Resolution result = MethodDeclarationResolver.resolve(owner, name, descriptor);
    assertEquals(owner, result.symbolicOwner);
    assertEquals(declaration, result.declarationOwner);
    assertTrue(result.deviceMemberFound);
  }

  private static JavaClass programType(String name, String superName, String[] interfaces, String methodName)
      throws Exception {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    int access = Opcodes.ACC_PUBLIC;
    if (interfaces != null && interfaces.length == 0) access |= Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
    writer.visit(Opcodes.V1_8, access, name, null, superName, interfaces);
    if (methodName != null) {
      MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC, methodName, "()V", null, null);
      if ((access & Opcodes.ACC_INTERFACE) == 0 || "defaultMethod".equals(methodName)) {
        method.visitCode();
        method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 1);
      }
      method.visitEnd();
    }
    writer.visitEnd();
    return new JavaClass(writer.toByteArray(), false, true);
  }

  private static byte[] caller() {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "fixtures/ProgramCaller", null, "java/lang/Object", null);
    MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "call",
        "(Lfixtures/ProgramChild;)V", null, null);
    method.visitCode();
    method.visitVarInsn(Opcodes.ALOAD, 0);
    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "fixtures/ProgramChild", "inherited", "()V", false);
    method.visitInsn(Opcodes.RETURN);
    method.visitMaxs(0, 0);
    method.visitEnd();
    writer.visitEnd();
    return writer.toByteArray();
  }
}
