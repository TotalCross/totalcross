// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.metadata.CompilationMetadata.ClassMetadata;
import tc.tools.converter.metadata.CompilationMetadataCollector;

class CompilationMetadataClassFactsTest {
  @TempDir
  Path workDir;

  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void preservesRecordAndPermittedSubclassFacts() throws Exception {
    assumeTrue(ToolProvider.getSystemJavaCompiler() != null, "A JDK with javac is required");
    Optional<ModernJavaClassFileFixture> record = ModernJavaClassFileFixtures.compileJava17RecordFixture(workDir);
    Optional<ModernJavaClassFileFixture> sealed = ModernJavaClassFileFixtures.compileJava17SealedFixture(workDir);
    Optional<List<ModernJavaClassFileFixture>> nest = ModernJavaClassFileFixtures.compileJava11NestmateFixture(workDir);
    assumeTrue(record.isPresent() && sealed.isPresent() && nest.isPresent(),
        "Current javac cannot target Java 11 and Java 17");

    CompilationMetadataCollector collector = new CompilationMetadataCollector();
    JavaClass recordClass = new JavaClass(record.get().bytes, false);
    JavaClass sealedClass = new JavaClass(sealed.get().bytes, false);
    collector.captureClass(recordClass, recordClass.className);
    collector.captureClass(sealedClass, sealedClass.className);
    for (ModernJavaClassFileFixture fixture : nest.get()) {
      JavaClass nested = new JavaClass(fixture.bytes, false);
      collector.captureClass(nested, nested.className);
    }
    List<ClassMetadata> classes = collector.snapshot().classes;

    ClassMetadata recordMetadata = find(classes, "fixtures/CompiledJava17Record");
    assertEquals(2, recordMetadata.recordComponents.size());
    assertEquals("name", recordMetadata.recordComponents.get(0).name);
    assertEquals("Ljava/lang/String;", recordMetadata.recordComponents.get(0).descriptor);
    assertEquals("count", recordMetadata.recordComponents.get(1).name);
    assertTrue(recordMetadata.sourceFile.endsWith(".java"));

    ClassMetadata sealedMetadata = find(classes, "fixtures/CompiledJava17Sealed");
    assertEquals(1, sealedMetadata.permittedSubclasses.size());
    assertEquals("fixtures/CompiledJava17Sealed$Allowed", sealedMetadata.permittedSubclasses.get(0));

    ClassMetadata outer = find(classes, "fixtures/CompiledJava11Nestmates");
    ClassMetadata inner = find(classes, "fixtures/CompiledJava11Nestmates$Inner");
    assertTrue(outer.nestMembers.contains(inner.originalName));
    assertEquals(outer.originalName, inner.nestHost);
  }

  private static ClassMetadata find(List<ClassMetadata> classes, String name) {
    for (ClassMetadata metadata : classes) {
      if (name.equals(metadata.originalName)) {
        return metadata;
      }
    }
    throw new AssertionError("Missing class metadata: " + name);
  }
}
