// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
      "releaseNativeHandle", "writePixelsFrameMetric"
  };
  private static final String[] SYMBOLS = {
      "tuiNIB_createEmptyNative_ii", "tuiNIB_snapshotNative", "tuiNIB_makeMutableNative",
      "tuiNIB_readPixelsNative_Iiiiii", "tuiNIB_releaseNativeHandle_l",
      "tuiNIB_writePixelsFrameMetric_i"
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
    for (String method : METHODS) {
      assertTrue(hasNativeMethod(converted, method), "missing native bridge " + method);
    }
  }

  @Test
  void nativeRegistrationIncludesEveryBackingBridge() throws Exception {
    Path vmRoot = Path.of("..", "TotalCrossVM");
    String declarations = Files.readString(vmRoot.resolve("src/nm/NativeMethods.txt"));
    String prototypes = Files.readString(vmRoot.resolve("src/nm/NativeMethodsPrototypes.txt"));
    String header = Files.readString(vmRoot.resolve("src/nm/NativeMethods.h"));
    String bridge = Files.readString(vmRoot.resolve("src/nm/ui/image_NativeImageBacking.c"));
    String registrations = Files.readString(vmRoot.resolve("src/init/nativeProcAddressesTC.c"));
    for (int i = 0; i < SYMBOLS.length; i++) {
      assertTrue(declarations.contains(CLASS_NAME + "|native"), "missing source declaration for " + METHODS[i]);
      assertTrue(prototypes.contains("TC_API void " + SYMBOLS[i] + "(NMParams p);"),
          "missing generated prototype for " + SYMBOLS[i]);
      assertTrue(header.contains("TC_API void " + SYMBOLS[i] + "(NMParams p);"),
          "missing native header declaration for " + SYMBOLS[i]);
      assertTrue(bridge.contains("TC_API void " + SYMBOLS[i] + "(NMParams p)"),
          "missing native bridge implementation for " + SYMBOLS[i]);
      assertTrue(registrations.contains("hashCode(\"" + SYMBOLS[i] + "\"), &" + SYMBOLS[i]),
          "missing native registration for " + SYMBOLS[i]);
    }
  }

  @Test
  void frameMetricNativeSignatureIsNotTruncated() throws Exception {
    String symbol = "tuiNIB_writePixelsFrameMetric_i";
    assertTrue(symbol.length() < 32, "frame metric native symbol reaches the limit");
    Path vmRoot = Path.of("..", "TotalCrossVM");
    String prototypes = Files.readString(vmRoot.resolve("src/nm/NativeMethodsPrototypes.txt"));
    String registrations = Files.readString(vmRoot.resolve("src/init/nativeProcAddressesTC.c"));
    assertTrue(prototypes.contains("TC_API void " + symbol + "(NMParams p);"),
        "generated frame metric prototype was truncated or renamed");
    assertTrue(registrations.contains("hashCode(\"" + symbol + "\"), &" + symbol),
        "frame metric registration does not match the generated symbol");
    assertFalse(registrations.contains("tuiNIB_writePixelsFrameMetricTes"),
        "frame metric registration still uses the truncated signature");
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
