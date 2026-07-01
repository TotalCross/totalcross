// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaClassFileVersion;

class ClassFileVersionTest {
  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void parserAcceptsRoadmapClassFileMajorVersions() throws Exception {
    for (Map.Entry<Integer, Integer> entry : ModernJavaClassFileFixtures.ROADMAP_MAJOR_VERSIONS.entrySet()) {
      int javaRelease = entry.getKey().intValue();
      int major = entry.getValue().intValue();
      JavaClass javaClass = new JavaClass(
          ModernJavaClassFileFixtures.minimalClassFile("fixtures/ParserJava" + javaRelease, major, 0), false);

      assertEquals(major, javaClass.majorVersion);
      assertEquals("fixtures/ParserJava" + javaRelease, javaClass.className);
    }
  }

  @Test
  void parserRejectsPreviewClassFiles() throws Exception {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new JavaClass(
        ModernJavaClassFileFixtures.minimalClassFile("fixtures/PreviewClass", JavaClassFileVersion.JAVA_21,
            JavaClassFileVersion.PREVIEW_MINOR_VERSION),
        false));

    assertTrue(error.getMessage().contains("preview class file version"));
  }

  @Test
  void parserRejectsClassFilesAboveSupportedMaximum() throws Exception {
    int unsupportedMajor = JavaClassFileVersion.maxSupportedMajor() + 1;
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new JavaClass(
        ModernJavaClassFileFixtures.minimalClassFile("fixtures/FutureClass", unsupportedMajor, 0), false));

    assertTrue(error.getMessage().contains("supports up to " + JavaClassFileVersion.maxSupportedMajor()));
  }

  @Test
  void parserSkipsUnknownClassAttributes() throws Exception {
    JavaClass javaClass = new JavaClass(ModernJavaClassFileFixtures.classFileWithUnknownClassAttribute(
        "fixtures/UnknownAttributeClass", JavaClassFileVersion.JAVA_26), false);

    assertEquals(JavaClassFileVersion.JAVA_26, javaClass.majorVersion);
    assertEquals("fixtures/UnknownAttributeClass", javaClass.className);
  }
}
