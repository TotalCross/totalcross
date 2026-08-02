// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.preview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class PreviewContractTest {
  @Test
  void exposesOnlyWorkerLifecycleCommands() {
    Set<String> methods = Arrays.stream(PreviewSession.class.getDeclaredMethods())
        .map(Method::getName).collect(Collectors.toSet());
    assertEquals(Set.of("pumpEvents", "resize", "pointer", "key", "close"), methods);
    assertEquals(5, PreviewSession.class.getDeclaredMethods().length);
    assertTrue(Modifier.isPublic(PreviewBootstrap.class.getModifiers()));
    assertTrue(Modifier.isPublic(PreviewFrame.class.getModifiers()));
    assertTrue(Modifier.isPublic(PreviewFrameSink.class.getModifiers()));
  }
}
