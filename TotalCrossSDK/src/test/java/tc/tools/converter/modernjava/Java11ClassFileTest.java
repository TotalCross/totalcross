// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.converter.ConverterException;
import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaClassFileVersion;

class Java11ClassFileTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void parserReadsModuleInfoMetadata() throws Exception {
    JavaClass javaClass = new JavaClass(ModernJavaClassFileFixtures.moduleInfoClassFile("fixtures.module",
        JavaClassFileVersion.JAVA_11), false);

    assertEquals(JavaClassFileVersion.JAVA_11, javaClass.majorVersion);
    assertEquals("module-info", javaClass.className);
    assertEquals("", javaClass.superClass);
    assertEquals("fixtures.module", javaClass.moduleName);
  }

  @Test
  void converterAcceptsModuleInfoAsMetadataOnly() throws Exception {
    JavaClass javaClass = new JavaClass(ModernJavaClassFileFixtures.moduleInfoClassFile("fixtures.module",
        JavaClassFileVersion.JAVA_11), false);
    GlobalConstantPool.init();

    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void parserReadsNestmateMetadataFromJavacOutput() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<List<ModernJavaClassFileFixture>> fixture =
        ModernJavaClassFileFixtures.compileJava11NestmateFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 11");

    JavaClass outer = new JavaClass(fixture.get().get(0).bytes, false);
    JavaClass inner = new JavaClass(fixture.get().get(1).bytes, false);

    assertEquals(JavaClassFileVersion.JAVA_11, outer.majorVersion);
    assertEquals(JavaClassFileVersion.JAVA_11, inner.majorVersion);
    assertNotNull(outer.nestMembers);
    assertEquals("fixtures/CompiledJava11Nestmates$Inner", outer.nestMembers[0]);
    assertEquals("fixtures/CompiledJava11Nestmates", inner.nestHost);
  }

  @Test
  void parserRejectsLdcDynamicConstantWithClearDiagnostic() throws Exception {
    ConverterException error = assertThrows(ConverterException.class, () -> new JavaClass(
        ModernJavaClassFileFixtures.classFileWithLdcDynamicConstant("fixtures/DynamicConstantLoad",
            JavaClassFileVersion.JAVA_11),
        false));

    assertTrue(error.getMessage().contains("Unsupported CONSTANT_Dynamic"));
    assertTrue(error.getMessage().contains("constant pool index 14"));
  }
}
