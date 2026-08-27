// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCField;
import tc.tools.converter.tclass.TCMethod;

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
  void directImageConversionPreservesNativeFieldIndices() throws Exception {
    J2TC.htAddedClasses.clear();
    J2TC.htExcludedClasses.clear();
    GlobalConstantPool.init();

    TCClass converted = convertDirectImage();
    assertNotNull(converted);
    assertEquals("totalcross/ui/image/Image", converted.className);
    assertAbiPrefix(converted);
    assertEquals("master", fieldName(converted.objectInstanceFields[6]));
    assertEquals("path", fieldName(converted.objectInstanceFields[7]));
    assertEquals("pipeline", fieldName(converted.objectInstanceFields[8]));
    assertNativeMethods(converted, "imageLoad", "imageParse", "setCurrentFrameNative", "applyChangesNative",
        "changeColorsNative", "getPixelRowNative", "setTransparentColorNative", "freeTextureNative", "createJpgNative",
        "applyColorNative", "nativeEqualsNative", "applyColor2Native", "applyFadeNative", "decodeEncodedSource",
        "nativeResizeJpeg", "getJpegBestFit", "getJpegScaled", "getModifiedNative");
  }

  private static TCClass convertDirectImage() throws Exception {
    try (InputStream stream = totalcross.ui.image.Image.class
        .getResourceAsStream("Image.class")) {
      assertNotNull(stream, "Image.class resource");
      return new J2TC(new JavaClass(stream.readAllBytes(), false), true).converted;
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

  private static void assertNativeMethods(TCClass converted, String... names) {
    for (String name : names) {
      boolean found = false;
      for (TCMethod method : converted.methods) {
        if (name.equals(GlobalConstantPool.getMethodFieldName(method.cpName)) && method.flags.isNative) {
          assertNull(method.code, "native method " + name + " must not retain executable Java code");
          found = true;
        }
      }
      assertTrue(found, "converted method " + name + " must be native");
    }
  }

  private static TCMethod findMethod(TCClass converted, String name) {
    for (TCMethod method : converted.methods) {
      if (name.equals(GlobalConstantPool.getMethodFieldName(method.cpName))) {
        return method;
      }
    }
    return null;
  }
}
