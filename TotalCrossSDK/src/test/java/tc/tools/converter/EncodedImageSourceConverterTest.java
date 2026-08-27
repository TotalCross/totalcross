// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter;

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

/** Ensures deployed encoded-source bridges are converted as native methods. */
class EncodedImageSourceConverterTest {
  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void convertsPrivateNativeBagBridges() throws Exception {
    J2TC.htAddedClasses.clear();
    J2TC.htExcludedClasses.clear();
    GlobalConstantPool.init();
    TCClass converted;
    try (InputStream stream = Class.forName("totalcross.ui.image.EncodedImageSource")
        .getResourceAsStream("EncodedImageSource.class")) {
      assertNotNull(stream, "EncodedImageSource.class resource");
      converted = new J2TC(new JavaClass(stream.readAllBytes(), false), true).converted;
    }
    assertNotNull(converted);
    assertTrue(hasNativeMethod(converted, "captureNative"));
    assertTrue(hasNativeMethod(converted, "captureNativePath"));
    assertTrue(hasNativeMethod(converted, "releaseNativeBag"));
  }

  @Test
  void nativeRegistrationUsesTheDeclaredSourceOfTruth() throws Exception {
    Path vmRoot = Path.of("..", "TotalCrossVM");
    String declarations = Files.readString(vmRoot.resolve("src/nm/NativeMethods.txt"));
    String prototypes = Files.readString(vmRoot.resolve("src/nm/NativeMethodsPrototypes.txt"));
    String header = Files.readString(vmRoot.resolve("src/nm/NativeMethods.h"));
    String registrations = Files.readString(vmRoot.resolve("src/init/nativeProcAddressesTC.c"));
    String[] symbols = { "tuiEIS_captureNative_Bi", "tuiEIS_captureNativePath_s", "tuiEIS_releaseNativeBag" };
    String[] methods = { "captureNative", "captureNativePath", "releaseNativeBag" };
    for (int i = 0; i < symbols.length; i++) {
      assertTrue(declarations.contains("totalcross/ui/image/EncodedImageSource|native private void " + methods[i]),
          "missing source-of-truth declaration for " + methods[i]);
      assertTrue(prototypes.contains("TC_API void " + symbols[i] + "(NMParams p);"),
          "missing generated prototype for " + symbols[i]);
      assertTrue(header.contains("TC_API void " + symbols[i] + "(NMParams p);"),
          "missing native header declaration for " + symbols[i]);
      assertTrue(registrations.contains("hashCode(\"" + symbols[i] + "\"), &" + symbols[i]),
          "missing native registration for " + symbols[i]);
    }
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
