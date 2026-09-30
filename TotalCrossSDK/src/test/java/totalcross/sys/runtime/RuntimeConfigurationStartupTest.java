// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Settings;

class RuntimeConfigurationStartupTest {
  private boolean previousOnJavaSE;
  private boolean previousIsOpenGL;
  private String previousPlatform;

  @BeforeEach
  void saveSettings() {
    previousOnJavaSE = Settings.onJavaSE;
    previousIsOpenGL = Settings.isOpenGL;
    previousPlatform = Settings.platform;
  }

  @AfterEach
  void restoreSettings() {
    Settings.onJavaSE = previousOnJavaSE;
    Settings.isOpenGL = previousIsOpenGL;
    Settings.platform = previousPlatform;
    RuntimeConfigurationStartup.initializeForSimulator(null);
  }

  @Test
  void resolvesMatchingSelectorsThroughTheSharedStartupResolver() {
    Settings.onJavaSE = true;
    String osName = System.getProperty("os.name");
    String osArch = System.getProperty("os.arch");
    RuntimeEnvironment host = RuntimeEnvironment.fromRuntimeSettings("Java", true, osName, osArch, 0, 0, 0);
    RuntimeConfigurationStartup.initializeForSimulator(Arrays.asList(
        RuntimeSelector.platform(host.platform()),
        RuntimeSelector.family(host.runtimeFamily()),
        RuntimeSelector.backend(GraphicsBackend.GPU)), osName, osArch);

    ResolvedRuntimeConfiguration current = RuntimeConfigurationStartup.current();
    assertNotNull(current);
    assertEquals(2, current.matchedRuleCount());
    assertEquals(host.platform(), current.environment().platform());
    assertNull(current.environment().graphicsBackend());
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.GPU);
    assertEquals(3, RuntimeConfigurationStartup.current().matchedRuleCount());
  }

  @Test
  void defersBackendSensitiveRulesUntilTheGraphicsBackendIsFinalized() {
    Settings.onJavaSE = true;
    String osName = System.getProperty("os.name");
    String osArch = System.getProperty("os.arch");
    RuntimeConfigurationStartup.initializeForSimulator(
        Collections.singletonList(RuntimeSelector.backend(GraphicsBackend.GPU)), osName, osArch);

    assertNotNull(RuntimeConfigurationStartup.current());
    assertEquals(0, RuntimeConfigurationStartup.current().matchedRuleCount());
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.GPU);
    ResolvedRuntimeConfiguration current = RuntimeConfigurationStartup.current();
    assertNotNull(current);
    assertEquals(GraphicsBackend.GPU, current.environment().graphicsBackend());
    assertEquals(1, current.matchedRuleCount());
    assertThrows(IllegalStateException.class,
        () -> RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER));
  }

  @Test
  void keepsMissingMetadataOnTheExistingPathAndAcceptsAnEmptyDeclaration() {
    RuntimeConfigurationStartup.initializeForSimulator(null);
    assertNull(RuntimeConfigurationStartup.current());

    RuntimeConfigurationStartup.initializeForSimulator(Collections.<RuntimeSelector>emptyList());
    ResolvedRuntimeConfiguration empty = RuntimeConfigurationStartup.current();
    assertNotNull(empty);
    assertEquals(0, empty.matchedRuleCount());
  }
}
