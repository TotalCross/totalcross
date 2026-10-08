// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NativeBridgeSymbolEncoderTest {
  @Test
  void preservesLegacyEncodingAndThirtyTwoCharacterLimit() {
    assertEquals("jlS_nanoTime",
        NativeBridgeSymbolEncoder.encode("java/lang/System", "nanoTime", "()J"));
    assertEquals("tuiNIB_createFromArgbPixelsNativ",
        NativeBridgeSymbolEncoder.encode("totalcross/ui/image/NativeImageBacking",
            "createFromArgbPixelsNative", "([III)J"));
  }
}
