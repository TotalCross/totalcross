// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCField;

/** Locks the field prefix addressed by TotalCrossVM/src/nm/instancefields.h. */
class ImageFieldAbiTest {
  private static final String[] I32_FIELDS = {
      "surfaceType", "width", "height", "frameCount", "currentFrame", "widthOfAllFrames",
      "transparentColor", "useAlpha", "alphaMask", "lastAccess", "textureId", "logicalWidth", "logicalHeight"
  };

  private static final String[] OBJECT_FIELDS = {
      "pixels", "pixelsOfAllFrames", "comment", "gfx", "changed", "instanceCount"
  };

  private static final String[] VALUE64_FIELDS = { "hwScaleW", "hwScaleH", "contentScale" };

  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void deployedImageReplacementPreservesNativeFieldIndices() throws Exception {
    J2TC.htAddedClasses.clear();
    J2TC.htExcludedClasses.clear();
    GlobalConstantPool.init();

    TCClass converted = convertReplacement();
    assertNotNull(converted);
    assertEquals("totalcross/ui/image/Image", converted.className);
    assertAbiPrefix(converted);
  }

  private static TCClass convertReplacement() throws Exception {
    try (InputStream stream = totalcross.ui.image.Image4D.class
        .getResourceAsStream("Image4D.class")) {
      assertNotNull(stream, "Image4D.class resource");
      return new J2TC(new JavaClass(stream.readAllBytes(), false)).converted;
    }
  }

  static void assertAbiPrefix(TCClass converted) {
    assertFieldPrefix("I32", I32_FIELDS, converted.int32InstanceFields);
    assertFieldPrefix("object", OBJECT_FIELDS, converted.objectInstanceFields);
    assertFieldPrefix("value64", VALUE64_FIELDS, converted.value64InstanceFields);
  }

  private static void assertFieldPrefix(String category, String[] expected, TCField[] actual) {
    assertNotNull(actual, category + " fields");
    for (int index = 0; index < expected.length; index++) {
      String actualName = index < actual.length ? fieldName(actual[index]) : "<missing>";
      assertEquals(expected[index], actualName,
          category + " field index " + index + ": expected " + expected[index] + ", actual " + actualName);
    }
  }

  private static String fieldName(TCField field) {
    return GlobalConstantPool.getMethodFieldName(field.cpName);
  }
}
