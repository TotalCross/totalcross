// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import totalcross.sys.RuntimeFamily;
import totalcross.sys.runtime.RuntimeConfigurationStartup;
import totalcross.sys.runtime.ImageRuntimeConfigurationStartup;
import totalcross.sys.runtime.ImageRuntimeOptions;
import totalcross.sys.runtime.ImageRuntimePolicy;
import totalcross.sys.runtime.ImageRuntimeConfigurationMetadata;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeRule;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.sys.runtime.RuntimeFeatureState;
import totalcross.sys.runtime.RuntimeConfigurationFeatureBridge.FeatureRule;
import tc.tools.converter.runtimeconfig.ImageRuntimeConfigurationParser;
import totalcross.sys.GraphicsBackend;
import totalcross.ui.image.ImageRuntimeRule;
import totalcross.ui.image.ImagePrefetchWorkerMode;
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

  @Test
  void simulatorAndDecodedDeploymentRulesProduceTheSameFivePropertyPolicy() throws Exception {
    String fixtureName = ImageAnnotatedFixture.class.getName();
    RuntimeConfigurationSimulator.initialize(getClass().getClassLoader(), fixtureName);
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
    ImageRuntimePolicy simulated = ImageRuntimeConfigurationStartup.currentPolicy();

    RuntimeConfigurationStartup.initializeForSimulator(null, "MacOS", "aarch64");
    List<FeatureRule<ImageRuntimeOptions>> parsed = ImageRuntimeConfigurationParser.parse(
        classBytes(ImageAnnotatedFixture.class), fixtureName);
    byte[] metadata = ImageRuntimeConfigurationMetadata.encodeForDeployment(parsed,
        Collections.emptyList());
    List<FeatureRule<ImageRuntimeOptions>> deployed = ImageRuntimeConfigurationMetadata.decode(metadata);
    ImageRuntimeConfigurationStartup.initializeForSimulator(deployed);
    RuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend(GraphicsBackend.RASTER);
    ImageRuntimeConfigurationStartup.finalizeSimulatorGraphicsBackend();
    ImageRuntimePolicy decoded = ImageRuntimeConfigurationStartup.currentPolicy();

    assertEquals(ImageStorageProfile.COMPACT, decoded.requestedStorageProfile());
    assertTrue(decoded.rasterVariants().targetColorConversion());
    assertTrue(decoded.rasterVariants().physicalVariantCache());
    assertTrue(decoded.scrollRasterReuse().enabled());
    assertEquals(ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER, decoded.prefetchWorker());
    assertEquals(simulated.requestedStorageProfile(), decoded.requestedStorageProfile());
    assertEquals(simulated.effectiveStorageProfile(), decoded.effectiveStorageProfile());
    assertEquals(simulated.rasterVariants().targetColorConversion(),
        decoded.rasterVariants().targetColorConversion());
    assertEquals(simulated.rasterVariants().physicalVariantCache(),
        decoded.rasterVariants().physicalVariantCache());
    assertEquals(simulated.scrollRasterReuse().enabled(), decoded.scrollRasterReuse().enabled());
    assertEquals(simulated.prefetchWorker(), decoded.prefetchWorker());
    assertEquals(simulated.matchedRuleNames(), decoded.matchedRuleNames());
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
      storage = ImageStorageProfile.COMPACT,
      targetColorConversion = RuntimeFeatureState.ENABLED,
      physicalVariantCache = RuntimeFeatureState.ENABLED,
      scrollRasterReuse = RuntimeFeatureState.ENABLED,
      prefetchWorker = ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER)
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

  private static byte[] classBytes(Class<?> type) throws Exception {
    String resource = "/" + type.getName().replace('.', '/') + ".class";
    try (InputStream input = type.getResourceAsStream(resource);
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      int count;
      while ((count = input.read(buffer)) != -1) {
        output.write(buffer, 0, count);
      }
      return output.toByteArray();
    }
  }
}
