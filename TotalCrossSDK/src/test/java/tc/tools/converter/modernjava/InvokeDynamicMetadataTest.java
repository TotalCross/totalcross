// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaBootstrapMethod;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaMethod;
import tc.tools.converter.java.JavaMethodHandle;

class InvokeDynamicMetadataTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void parserExposesLambdaMetafactoryBootstrapMetadata() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava8LambdaFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");

    JavaClass javaClass = new JavaClass(fixture.get().bytes, false);
    BC186_invokedynamic site = findInvokeDynamic(javaClass);

    assertNotNull(javaClass.bootstrapMethods);
    assertTrue(javaClass.bootstrapMethods.length > site.bootstrapMethodAttrIndex);
    JavaBootstrapMethod bootstrapMethod = javaClass.bootstrapMethods[site.bootstrapMethodAttrIndex];
    JavaMethodHandle bootstrapHandle = new JavaMethodHandle(javaClass.cp, bootstrapMethod.bootstrapMethodRef);

    assertEquals("java/lang/invoke/LambdaMetafactory", bootstrapHandle.getOwner(javaClass.cp));
    assertEquals("metafactory", bootstrapHandle.getName(javaClass.cp));
    assertEquals("run", site.name);
    assertEquals("(Ljava/lang/String;)Ljava/lang/Runnable;", site.descriptor);
    assertEquals(3, bootstrapMethod.bootstrapArguments.length);
  }

  private static BC186_invokedynamic findInvokeDynamic(JavaClass javaClass) {
    for (int i = 0; i < javaClass.methods.length; i++) {
      JavaMethod method = javaClass.methods[i];
      if (method.code == null || method.code.bcs == null) {
        continue;
      }
      for (int j = 0; j < method.code.bcs.length; j++) {
        if (method.code.bcs[j] instanceof BC186_invokedynamic) {
          return (BC186_invokedynamic) method.code.bcs[j];
        }
      }
    }
    throw new AssertionError("Expected compiled lambda fixture to contain invokedynamic");
  }
}
