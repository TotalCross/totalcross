// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.Java8LambdaLowering;
import tc.tools.converter.TCConstants;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.metadata.CompilationMetadata;
import tc.tools.converter.metadata.CompilationMetadata.CallSiteMetadata;
import tc.tools.converter.metadata.CompilationMetadata.ClassMetadata;
import tc.tools.converter.metadata.CompilationMetadata.MethodMetadata;
import tc.tools.converter.metadata.CompilationMetadata.SyntheticOrigin;
import tc.tools.converter.tclass.TCMethod;

class CompilationMetadataSyntheticTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void preservesLambdaSiteAndGeneratedAdapterOrigin() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava8LambdaFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");

    GlobalConstantPool.init();
    Java8LambdaLowering.beginConversionRun();
    J2TC.resetCompilationMetadata();
    boolean previous = TCMethod.checkJavaCalls;
    TCMethod.checkJavaCalls = false;
    try {
      new J2TC(new JavaClass(fixture.get().bytes, false, true), true);
    } finally {
      TCMethod.checkJavaCalls = previous;
    }

    CompilationMetadata metadata = J2TC.getCompilationMetadata();
    ClassMetadata owner = metadata.classes.get(0);
    assertEquals(1, owner.syntheticOrigins.size());
    SyntheticOrigin origin = owner.syntheticOrigins.get(0);
    assertEquals(CompilationMetadata.SyntheticKind.LAMBDA, origin.kind);
    assertEquals("fixtures/CompiledJava8Lambda", origin.owner);
    assertTrue(origin.generatedClass.startsWith("fixtures/CompiledJava8Lambda$$TC$$Lambda$"));
    assertTrue(origin.factoryMethod.startsWith("$$tc_lambda_factory$"));
    assertEquals("()V", origin.samDescriptor);
    assertEquals("java/lang/String", origin.captureDescriptors.get(0).substring(1,
        origin.captureDescriptors.get(0).length() - 1));
    assertFalse(origin.implementationDescriptor.isEmpty());

    MethodMetadata method = method(owner, "runnable");
    CallSiteMetadata call = method.callSites.get(0);
    assertEquals(CompilationMetadata.InvokeKind.DYNAMIC_LAMBDA, call.invokeKind);
    assertEquals("run", call.name);
    assertEquals("(Ljava/lang/String;)Ljava/lang/Runnable;", call.javaDescriptor);
    assertEquals(TCConstants.CALL_normal, call.loweredOpcode);
    assertTrue(call.tcStartSlot >= 0);
    assertTrue(call.tcEndSlotExclusive > call.tcStartSlot);
  }

  @Test
  void classifiesStringConcatLowering() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava11StringConcatFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 11");
    CompilationMetadata metadata = convert(fixture.get());
    assertTrue(hasSynthetic(metadata.classes.get(0), CompilationMetadata.SyntheticKind.STRING_CONCAT));
    assertTrue(hasInvoke(metadata.classes.get(0), CompilationMetadata.InvokeKind.DYNAMIC_STRING_CONCAT));
    assertValidRange(invoke(metadata.classes.get(0), CompilationMetadata.InvokeKind.DYNAMIC_STRING_CONCAT));
  }

  @Test
  void classifiesRecordObjectMethodLowering() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava17RecordFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 17");
    CompilationMetadata metadata = convert(fixture.get());
    assertTrue(hasSynthetic(metadata.classes.get(0), CompilationMetadata.SyntheticKind.RECORD_OBJECT_METHOD));
    assertTrue(hasInvoke(metadata.classes.get(0), CompilationMetadata.InvokeKind.DYNAMIC_RECORD));
    assertValidRange(invoke(metadata.classes.get(0), CompilationMetadata.InvokeKind.DYNAMIC_RECORD));
  }

  private static CompilationMetadata convert(ModernJavaClassFileFixture fixture) throws Exception {
    GlobalConstantPool.init();
    Java8LambdaLowering.beginConversionRun();
    J2TC.resetCompilationMetadata();
    boolean previous = TCMethod.checkJavaCalls;
    TCMethod.checkJavaCalls = false;
    try {
      new J2TC(new JavaClass(fixture.bytes, false, true), true);
      return J2TC.getCompilationMetadata();
    } finally {
      TCMethod.checkJavaCalls = previous;
    }
  }

  private static boolean hasSynthetic(ClassMetadata owner, CompilationMetadata.SyntheticKind kind) {
    for (SyntheticOrigin origin : owner.syntheticOrigins) {
      if (origin.kind == kind) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasInvoke(ClassMetadata owner, CompilationMetadata.InvokeKind kind) {
    return invoke(owner, kind) != null;
  }

  private static CallSiteMetadata invoke(ClassMetadata owner, CompilationMetadata.InvokeKind kind) {
    for (MethodMetadata method : owner.methods) {
      for (CallSiteMetadata call : method.callSites) {
        if (call.invokeKind == kind) {
          return call;
        }
      }
    }
    return null;
  }

  private static void assertValidRange(CallSiteMetadata call) {
    assertTrue(call != null && call.tcStartSlot >= 0 && call.tcEndSlotExclusive > call.tcStartSlot);
  }

  private static MethodMetadata method(ClassMetadata owner, String name) {
    for (MethodMetadata method : owner.methods) {
      if (name.equals(method.originalName)) {
        return method;
      }
    }
    throw new AssertionError("Missing method: " + name);
  }
}
