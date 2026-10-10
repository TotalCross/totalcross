// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

/** Shared assertions against the Java-derived native bridge model. */
final class NativeBridgeTestSupport {
  private NativeBridgeTestSupport() {
  }

  static Set<String> symbolsFor(Class<?> type) throws Exception {
    String resourceName = type.getSimpleName() + ".class";
    try (InputStream stream = type.getResourceAsStream(resourceName)) {
      assertNotNull(stream, type.getName() + " class resource");
      Set<String> symbols = new LinkedHashSet<String>();
      for (NativeBridgeModel.Entry entry : NativeBridgeModel.fromClassBytes(stream.readAllBytes())) {
        symbols.add(entry.symbol);
      }
      return symbols;
    }
  }
}
