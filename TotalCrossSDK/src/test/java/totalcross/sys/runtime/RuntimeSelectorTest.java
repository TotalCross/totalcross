// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;

class RuntimeSelectorTest {
  @Test
  void anySelectorMatchesEveryEnvironment() {
    assertTrue(RuntimeSelector.any().matches(environment("Android", 4, true)));
    assertTrue(RuntimeSelector.any().matches(environment("Win32", 1, false)));
  }

  @Test
  void matchesEachTypedEnvironmentDimension() {
    RuntimeEnvironment environment = environment("Android", 4, true);

    assertTrue(RuntimeSelector.platform(Platform.ANDROID).matches(environment));
    assertTrue(RuntimeSelector.family(RuntimeFamily.MOBILE).matches(environment));
    assertTrue(RuntimeSelector.backend(GraphicsBackend.GPU).matches(environment));
    assertTrue(RuntimeSelector.architecture(Architecture.ARM64).matches(environment));
    assertFalse(RuntimeSelector.platform(Platform.WINDOWS).matches(environment));
  }

  @Test
  void combinesIndependentDimensionsAndDisjunctions() {
    RuntimeEnvironment windows = environment("Win32", 2, false);
    RuntimeSelector desktopRaster = RuntimeSelector.allOf(RuntimeSelector.family(RuntimeFamily.DESKTOP),
        RuntimeSelector.backend(GraphicsBackend.RASTER));
    RuntimeSelector windowsOrMobile = RuntimeSelector.anyOf(RuntimeSelector.platform(Platform.WINDOWS),
        RuntimeSelector.family(RuntimeFamily.MOBILE));

    assertTrue(desktopRaster.matches(windows));
    assertTrue(windowsOrMobile.matches(windows));
    assertFalse(windowsOrMobile.matches(environment("Linux", 2, false)));
  }

  @Test
  void missingFactsDoNotMatchConstrainedDimensionsButMatchAny() {
    RuntimeEnvironment environment = RuntimeEnvironment.fromRuntimeSettings("unsupported", false, null, null, 0, 0,
        0);

    assertFalse(environment.isGraphicsBackendFinalized());
    assertTrue(RuntimeSelector.any().matches(environment));
    assertFalse(RuntimeSelector.platform(Platform.WINDOWS).matches(environment));
    assertFalse(RuntimeSelector.family(RuntimeFamily.DESKTOP).matches(environment));
    assertFalse(RuntimeSelector.backend(GraphicsBackend.RASTER).matches(environment));
    assertFalse(RuntimeSelector.architecture(Architecture.X86).matches(environment));

    RuntimeEnvironment unknownFamily = RuntimeEnvironment.fromRuntimeSettings("WindowsFuture", false, null, null, 1,
        0, 0);
    assertTrue(RuntimeSelector.platform(Platform.WINDOWS).matches(unknownFamily));
    assertFalse(RuntimeSelector.family(RuntimeFamily.DESKTOP).matches(unknownFamily));
  }

  @Test
  void selectorFactoriesRejectAbsentValues() {
    assertThrows(IllegalArgumentException.class, () -> RuntimeSelector.platform((Platform) null));
    assertThrows(IllegalArgumentException.class, () -> RuntimeSelector.family((RuntimeFamily) null));
    assertThrows(IllegalArgumentException.class, () -> RuntimeSelector.backend((GraphicsBackend) null));
    assertThrows(IllegalArgumentException.class, () -> RuntimeSelector.architecture((Architecture) null));
  }

  private static RuntimeEnvironment environment(String platform, int architecture, boolean isOpenGL) {
    return RuntimeEnvironmentTestSupport.environment(platform, architecture, isOpenGL);
  }
}
