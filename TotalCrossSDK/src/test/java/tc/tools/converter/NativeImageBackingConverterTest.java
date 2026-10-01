// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tc.tools.converter.bytecode.ByteCode;
import tc.tools.converter.java.JavaClass;
import tc.tools.converter.tclass.TCClass;
import tc.tools.converter.tclass.TCMethod;

class NativeImageBackingConverterTest {
  private static final String CLASS_NAME = "totalcross/ui/image/NativeImageBacking";
  private static final String[] METHODS = {
      "createEmptyNative", "snapshotNative", "makeMutableNative", "readPixelsNative",
      "readRgbaRowNative", "releaseNativeHandle"
  };
  private static final String[] SYMBOLS = {
      "tuiNIB_createEmptyNative_ii", "tuiNIB_snapshotNative", "tuiNIB_makeMutableNative",
      "tuiNIB_readPixelsNative_Iiiiii", "tuiNIB_readRgbaRowNative_Bii", "tuiNIB_releaseNativeHandle_l"
  };

  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void convertsOpaqueBackingFieldsAndBridges() throws Exception {
    J2TC.htAddedClasses.clear();
    J2TC.htExcludedClasses.clear();
    GlobalConstantPool.init();
    TCClass converted;
    try (InputStream stream = Class.forName("totalcross.ui.image.NativeImageBacking")
        .getResourceAsStream("NativeImageBacking.class")) {
      assertNotNull(stream, "NativeImageBacking.class resource");
      converted = new J2TC(new JavaClass(stream.readAllBytes(), false), true).converted;
    }

    assertNotNull(converted);
    assertEquals("nativeHandle", GlobalConstantPool.getMethodFieldName(converted.value64InstanceFields[0].cpName));
    assertEquals("width", GlobalConstantPool.getMethodFieldName(converted.int32InstanceFields[0].cpName));
    assertEquals("height", GlobalConstantPool.getMethodFieldName(converted.int32InstanceFields[1].cpName));
    TCClass backing = convertBackingBase();
    assertEquals("mutationGeneration",
        GlobalConstantPool.getMethodFieldName(backing.value64InstanceFields[0].cpName));
    assertEquals("opacityState", GlobalConstantPool.getMethodFieldName(backing.int32InstanceFields[0].cpName));
    assertEquals("mutableStorageEscaped", GlobalConstantPool.getMethodFieldName(backing.int32InstanceFields[1].cpName));
    for (String method : METHODS) {
      assertTrue(hasNativeMethod(converted, method), "missing native bridge " + method);
    }
  }

  private static TCClass convertBackingBase() throws Exception {
    Class<?> backingClass = Class.forName("totalcross.ui.image.ImageBacking");
    try (InputStream stream = backingClass.getResourceAsStream("ImageBacking.class")) {
      assertNotNull(stream, "ImageBacking.class resource");
      return new J2TC(new JavaClass(stream.readAllBytes(), false), true).converted;
    }
  }

  @Test
  void nativeRegistrationIncludesEveryBackingBridge() throws Exception {
    Path vmRoot = Path.of("..", "TotalCrossVM");
    String declarations = Files.readString(vmRoot.resolve("src/nm/NativeMethods.txt"));
    String prototypes = Files.readString(vmRoot.resolve("src/nm/NativeMethodsPrototypes.txt"));
    String header = Files.readString(vmRoot.resolve("src/nm/NativeMethods.h"));
    String registrations = Files.readString(vmRoot.resolve("src/init/nativeProcAddressesTC.c"));
    for (int i = 0; i < SYMBOLS.length; i++) {
      assertTrue(declarations.contains(CLASS_NAME + "|native"), "missing source declaration for " + METHODS[i]);
      assertTrue(prototypes.contains("TC_API void " + SYMBOLS[i] + "(NMParams p);"),
          "missing generated prototype for " + SYMBOLS[i]);
      assertTrue(header.contains("TC_API void " + SYMBOLS[i] + "(NMParams p);"),
          "missing native header declaration for " + SYMBOLS[i]);
      assertTrue(registrations.contains("hashCode(\"" + SYMBOLS[i] + "\"), &" + SYMBOLS[i]),
          "missing native registration for " + SYMBOLS[i]);
    }
  }

  @Test
  void nativeFieldOffsetsIncludeMutableStorageEscapeState() throws Exception {
    String fields = Files.readString(Path.of("..", "TotalCrossVM", "src", "nm", "instancefields.h"));
    assertTrue(fields.contains("#define ImageBacking_mutableStorageEscaped(o) FIELD_I32(o, 1)"));
    assertTrue(fields.contains("#define NativeImageBacking_width(o)        FIELD_I32(o, 2)"));
    assertTrue(fields.contains("#define NativeImageBacking_height(o)       FIELD_I32(o, 3)"));
    assertTrue(fields.contains("#define RasterImageBacking_width(o)              FIELD_I32(o, 2)"));
    assertTrue(fields.contains("#define RasterImageBacking_height(o)             FIELD_I32(o, 3)"));
    assertTrue(fields.contains("#define RasterImageBacking_frameCount(o)         FIELD_I32(o, 4)"));
    assertTrue(fields.contains("#define RasterImageBacking_widthOfAllFrames(o)   FIELD_I32(o, 5)"));
  }

  private static boolean hasNativeMethod(TCClass converted, String name) {
    for (TCMethod method : converted.methods) {
      if (name.equals(GlobalConstantPool.getMethodFieldName(method.cpName))) {
        return method.flags.isNative && method.code == null;
      }
    }
    return false;
  }
}
