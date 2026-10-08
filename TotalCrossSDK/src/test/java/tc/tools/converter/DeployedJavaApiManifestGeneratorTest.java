// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class DeployedJavaApiManifestGeneratorTest {
  @Test
  void derivesSupportedSystemMethodFromResolver() {
    MethodDeclarationResolver.beginConversionRun();
    List<String> entries = DeployedJavaApiManifestGenerator.entriesFor(System.class);
    assertTrue(entries.stream().anyMatch(entry -> entry.startsWith("java/lang/System#nanoTime()J\t")));
  }

  @Test
  void omitsHostOnlyThreadMethod() {
    MethodDeclarationResolver.beginConversionRun();
    List<String> entries = DeployedJavaApiManifestGenerator.entriesFor(Thread.class);
    assertTrue(entries.stream().noneMatch(entry -> entry.startsWith("java/lang/Thread#onSpinWait()V\t")));
  }
}
