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

class GraphicsRasterWriteConverterTest {
  @BeforeAll
  static void initializeBytecodes() throws Exception {
    ByteCode.initClasses();
  }

  @Test
  void publicSetRgbKeepsItsSignatureAndPrivateBridgeIsNative() throws Exception {
    J2TC.htAddedClasses.clear();
    J2TC.htExcludedClasses.clear();
    GlobalConstantPool.init();
    TCClass converted;
    try (InputStream stream = totalcross.ui.gfx.Graphics.class.getResourceAsStream("Graphics.class")) {
      assertNotNull(stream, "Graphics.class resource");
      converted = new J2TC(new JavaClass(stream.readAllBytes(), false), true).converted;
    }

    assertNotNull(converted);
    TCMethod publicSetRgb = findMethod(converted, "setRGB");
    TCMethod nativeBridge = findMethod(converted, "setRGBNative");
    assertNotNull(publicSetRgb, "public setRGB contract");
    assertNotNull(nativeBridge, "private native setRGB bridge");
    assertTrue(!publicSetRgb.flags.isNative && nativeBridge.flags.isNative && nativeBridge.code == null);
  }

  @Test
  void javaDerivedNativeBridgeContainsThePrivateSetRgbBridgeOnly() throws Exception {
    String symbol = "tugG_setRGBNative_Iiiiiib";
    assertTrue(NativeBridgeTestSupport.symbolsFor(totalcross.ui.gfx.Graphics.class).contains(symbol));

    String implementation = Files.readString(
        Path.of("..", "TotalCrossVM", "src", "nm", "ui", "gfx_Graphics.c"));
    assertTrue(implementation.contains("TC_API void " + symbol + "(NMParams p)"));
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
