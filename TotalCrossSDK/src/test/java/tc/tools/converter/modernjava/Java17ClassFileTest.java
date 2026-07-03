// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.converter.GlobalConstantPool;
import tc.tools.converter.J2TC;
import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaClassFileVersion;

class Java17ClassFileTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void convertsOrdinaryJava17ClassFile() throws Exception {
    JavaClass javaClass = compileSimpleJava17Class();

    assertEquals(JavaClassFileVersion.JAVA_17, javaClass.majorVersion);

    GlobalConstantPool.init();
    assertDoesNotThrow(() -> new J2TC(javaClass, true));
  }

  @Test
  void parserReadsRecordMetadataFromJavacOutput() throws Exception {
    JavaClass javaClass = compileJava17RecordClass();

    assertEquals(JavaClassFileVersion.JAVA_17, javaClass.majorVersion);
    assertEquals("java/lang/Record", javaClass.superClass);
    assertNotNull(javaClass.recordComponents);
    assertEquals(2, javaClass.recordComponents.length);
    assertEquals("name", javaClass.recordComponents[0].name);
    assertEquals("Ljava/lang/String;", javaClass.recordComponents[0].descriptor);
    assertEquals("count", javaClass.recordComponents[1].name);
    assertEquals("I", javaClass.recordComponents[1].descriptor);
  }

  @Test
  void parserReadsPermittedSubclassesFromJavacOutput() throws Exception {
    JavaClass javaClass = compileJava17SealedClass();

    assertEquals(JavaClassFileVersion.JAVA_17, javaClass.majorVersion);
    assertNotNull(javaClass.permittedSubclasses);
    assertEquals(1, javaClass.permittedSubclasses.length);
    assertEquals("fixtures/CompiledJava17Sealed$Allowed", javaClass.permittedSubclasses[0]);
  }

  private JavaClass compileSimpleJava17Class() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture =
        ModernJavaClassFileFixtures.compileSimpleFixture(workDir, ModernJavaClassFileFixtures.JAVA_17);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 17");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass compileJava17RecordClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava17RecordFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 17");
    return new JavaClass(fixture.get().bytes, false);
  }

  private JavaClass compileJava17SealedClass() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required for javac fixture tests");
    Optional<ModernJavaClassFileFixture> fixture = ModernJavaClassFileFixtures.compileJava17SealedFixture(workDir);
    assumeTrue(fixture.isPresent(), "Current javac cannot target Java 17");
    return new JavaClass(fixture.get().bytes, false);
  }
}
