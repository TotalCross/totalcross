// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.modernjava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.java.JavaClassFileVersion;
import tc.tools.converter.java.JavaConstantInfo;

class ConstantPoolModernTagsTest {
  @BeforeAll
  static void initByteCodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void parserReadsDynamicModuleAndPackageConstantPoolTags() throws Exception {
    JavaClass javaClass = new JavaClass(ModernJavaClassFileFixtures.classFileWithModernConstantPoolTags(
        "fixtures/ModernConstantPoolTags", JavaClassFileVersion.JAVA_26), false);

    assertEquals("fixtures/ModernConstantPoolTags", javaClass.className);
    assertConstantTag(javaClass, 11, 19);
    assertConstantTag(javaClass, 13, 20);
    assertConstantTag(javaClass, 17, 17);
  }

  private static void assertConstantTag(JavaClass javaClass, int constantPoolIndex, int expectedTag) {
    assertTrue(javaClass.cp.constants[constantPoolIndex] instanceof JavaConstantInfo);
    JavaConstantInfo info = (JavaConstantInfo) javaClass.cp.constants[constantPoolIndex];
    assertEquals(expectedTag, info.type);
  }
}
