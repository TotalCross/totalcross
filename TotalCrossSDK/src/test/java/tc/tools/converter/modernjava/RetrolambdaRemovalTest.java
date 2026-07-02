// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import tc.tools.converter.bytecode.BC186_invokedynamic;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaMethod;

class RetrolambdaRemovalTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void deployerConvertsDirectJava8LambdaClassWithoutRetrolambda() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture =
        ModernJavaClassFileFixtures.compileJava8RetrolambdaRemovalFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 8");

    JavaClass javaClass = new JavaClass(fixture.get().bytes, false);
    JavaClass[] adapters = Java8LambdaLowering.generateAdapterClasses(javaClass);

    assertEquals(52, fixture.get().metadata().majorVersion);
    assertTrue(fixture.get().compiledWithJavac);
    assertTrue(hasInvokeDynamic(javaClass));
    assertEquals(5, adapters.length);

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(javaClass, true));
    for (int i = 0; i < adapters.length; i++) {
      final JavaClass adapter = adapters[i];
      assertFalse(hasInvokeDynamic(adapter));
      GlobalConstantPool.init();
      assertDoesNotThrow(() -> new J2TC(adapter, true));
    }
  }

  private static boolean hasInvokeDynamic(JavaClass javaClass) {
    for (int i = 0; i < javaClass.methods.length; i++) {
      JavaMethod method = javaClass.methods[i];
      if (method.code == null || method.code.bcs == null) {
        continue;
      }
      for (int j = 0; j < method.code.bcs.length; j++) {
        if (method.code.bcs[j] instanceof BC186_invokedynamic) {
          return true;
        }
      }
    }
    return false;
  }
}
