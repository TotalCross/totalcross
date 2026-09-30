// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import totalcross.sys.RuntimeFamily;
import totalcross.sys.runtime.RuntimeConfigurationStartup;
import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeRule;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.sys.GraphicsBackend;
import totalcross.ui.image.ImageRuntimeRule;
import totalcross.ui.image.ImageStorageProfile;

class RuntimeConfigurationSimulatorTest {
  private static final String INITIALIZED_PROPERTY = "totalcross.runtimeConfigurationFixture.initialized";

  @AfterEach
  void cleanup() {
    System.clearProperty(INITIALIZED_PROPERTY);
    RuntimeConfigurationStartup.initializeForSimulator(null);
    ImageRuntimeConfigurationStartup.initializeForSimulator(null);
  }

  @Test
  void parsesClassResourceWithoutInitializingTheApplicationClass() throws Exception {
    System.clearProperty(INITIALIZED_PROPERTY);
    String fixtureName = AnnotatedFixture.class.getName();

    RuntimeConfigurationSimulator.initialize(getClass().getClassLoader(), fixtureName);

    assertNull(System.getProperty(INITIALIZED_PROPERTY));
    Object current = startupResult();
    assertNotNull(current);
    assertEquals(1, matchedRuleCount(current));

    Class.forName(fixtureName, true, getClass().getClassLoader());
    assertEquals("loaded", System.getProperty(INITIALIZED_PROPERTY));
  }

  @Test
  void leavesUnannotatedJava17ClassesOnTheNoMetadataPath() throws Exception {
    RuntimeConfigurationSimulator.initialize(getClass().getClassLoader(), PlainFixture.class.getName());

    assertNull(startupResult());
  }

  @Test
  void parsesImageRulesBeforeInitializationAndResolvesAfterRasterBackendFinalizes() throws Exception {
    System.clearProperty(INITIALIZED_PROPERTY);
    RuntimeConfigurationSimulator.initialize(getClass().getClassLoader(), ImageAnnotatedFixture.class.getName());

    assertNull(System.getProperty(INITIALIZED_PROPERTY));
    assertEquals(ImageStorageProfile.STANDARD,
        ImageRuntimeConfigurationStartup.currentPolicy().requestedStorageProfile());

    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
    assertEquals(ImageStorageProfile.COMPACT,
        ImageRuntimeConfigurationStartup.currentPolicy().requestedStorageProfile());
    assertEquals(ImageStorageProfile.STANDARD,
        ImageRuntimeConfigurationStartup.currentPolicy().effectiveStorageProfile());
    Class.forName(ImageAnnotatedFixture.class.getName(), true, getClass().getClassLoader());
    assertEquals("loaded", System.getProperty(INITIALIZED_PROPERTY));
  }

  @RuntimeConfiguration
  @RuntimeRule(when = @RuntimeWhen(allOf = { @RuntimeCondition(family = RuntimeFamily.DESKTOP) }))
  static class AnnotatedFixture {
    static {
      System.setProperty(INITIALIZED_PROPERTY, "loaded");
    }
  }

  static class PlainFixture {
  }

  @RuntimeConfiguration
  @ImageRuntimeRule(when = @RuntimeWhen(allOf = { @RuntimeCondition(backend = GraphicsBackend.RASTER) }),
      storage = ImageStorageProfile.COMPACT)
  static class ImageAnnotatedFixture {
    static {
      System.setProperty(INITIALIZED_PROPERTY, "loaded");
    }
  }

  private static Object startupResult() throws Exception {
    java.lang.reflect.Method current = RuntimeConfigurationStartup.class.getDeclaredMethod("current");
    current.setAccessible(true);
    return current.invoke(null);
  }

  private static int matchedRuleCount(Object result) throws Exception {
    java.lang.reflect.Method count = result.getClass().getDeclaredMethod("matchedRuleCount");
    count.setAccessible(true);
    return ((Integer) count.invoke(result)).intValue();
  }
}
