// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;

class RuntimeConfigurationMetadataTest {
  @Test
  void roundTripsEverySelectorDimensionWithStableVersionedBytes() {
    RuntimeSelector selector = RuntimeSelector.platform(Platform.WINDOWS, Platform.LINUX)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP))
        .and(RuntimeSelector.backend(GraphicsBackend.RASTER, GraphicsBackend.GPU))
        .or(RuntimeSelector.architecture(Architecture.ARM64));
    List<RuntimeSelector> selectors = Collections.singletonList(selector);
    List<RuntimeConfigurationMetadata.DeploymentTarget> noTargets = Collections.emptyList();

    byte[] first = RuntimeConfigurationMetadata.encodeForDeployment(selectors, noTargets);
    byte[] second = RuntimeConfigurationMetadata.encodeForDeployment(selectors, noTargets);
    List<RuntimeSelector> decoded = RuntimeConfigurationMetadata.decode(first);

    assertArrayEquals(first, second);
    assertEquals(2, first[4]);
    assertEquals(1, decoded.size());
    assertTrue(decoded.get(0).matches(environment("Win32", 2, true)));
    assertTrue(decoded.get(0).matches(environment("Linux", 4, false)));
    assertFalse(decoded.get(0).matches(environment("Android", 0, false)));
    assertThrows(UnsupportedOperationException.class, () -> decoded.add(RuntimeSelector.any()));
  }

  @Test
  void deploymentKeepsOnlyCorrelatedTargetBranches() {
    RuntimeSelector windowsDesktop = RuntimeSelector.platform(Platform.WINDOWS)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP));
    RuntimeSelector androidMobile = RuntimeSelector.platform(Platform.ANDROID)
        .and(RuntimeSelector.family(RuntimeFamily.MOBILE));
    RuntimeSelector impossibleWindowsMobile = RuntimeSelector.platform(Platform.WINDOWS)
        .and(RuntimeSelector.family(RuntimeFamily.MOBILE));
    List<RuntimeSelector> source = Arrays.asList(windowsDesktop.or(androidMobile), impossibleWindowsMobile);
    List<RuntimeConfigurationMetadata.DeploymentTarget> targets = Arrays.asList(
        new RuntimeConfigurationMetadata.DeploymentTarget(Platform.WINDOWS, RuntimeFamily.DESKTOP, null),
        new RuntimeConfigurationMetadata.DeploymentTarget(Platform.ANDROID, RuntimeFamily.MOBILE, null));

    List<RuntimeSelector> deployed = RuntimeConfigurationMetadata.decode(
        RuntimeConfigurationMetadata.encodeForDeployment(source, targets));

    assertEquals(1, deployed.size());
    assertTrue(deployed.get(0).matches(environment("Win32", 2, false)));
    assertTrue(deployed.get(0).matches(environment("Android", 4, true)));
    assertFalse(deployed.get(0).matches(environment("WindowsCE", 4, true)));
  }

  @Test
  void foldsOnlyKnownArchitectureAndPreservesItsSpecificity() {
    RuntimeSelector architecture = RuntimeSelector.architecture(Architecture.ARM32);
    RuntimeSelector allKnownDimensions = RuntimeSelector.platform(Platform.LINUX)
        .and(RuntimeSelector.family(RuntimeFamily.EMBEDDED))
        .and(RuntimeSelector.architecture(Architecture.ARM32));
    List<RuntimeConfigurationMetadata.DeploymentTarget> linuxArm32 = Collections.singletonList(
        new RuntimeConfigurationMetadata.DeploymentTarget(Platform.LINUX, RuntimeFamily.EMBEDDED,
            Architecture.ARM32));
    List<RuntimeSelector> deployed = RuntimeConfigurationMetadata.decode(
        RuntimeConfigurationMetadata.encodeForDeployment(Arrays.asList(architecture, allKnownDimensions),
            linuxArm32));

    assertEquals(2, deployed.size());
    RuntimeEnvironment target = RuntimeEnvironmentTestSupport.environment("Linux_ARM", 3, false);
    assertEquals(1, deployed.get(0).specificityFor(target));
    assertEquals(3, deployed.get(1).specificityFor(target));
    assertTrue(deployed.get(0).matches(target));
    assertTrue(deployed.get(1).matches(target));
  }

  @Test
  void preservesArchitectureAndBackendWhenDeploymentCannotProveThem() {
    List<RuntimeSelector> source = Arrays.asList(
        RuntimeSelector.architecture(Architecture.ARM64),
        RuntimeSelector.backend(GraphicsBackend.GPU));
    List<RuntimeConfigurationMetadata.DeploymentTarget> windows = Collections.singletonList(
        new RuntimeConfigurationMetadata.DeploymentTarget(Platform.WINDOWS, RuntimeFamily.DESKTOP, null));

    List<RuntimeSelector> deployed = RuntimeConfigurationMetadata.decode(
        RuntimeConfigurationMetadata.encodeForDeployment(source, windows));

    assertEquals(2, deployed.size());
    assertTrue(deployed.get(0).matches(environment("WindowsCE", 4, true)));
    assertTrue(deployed.get(1).matches(environment("WindowsCE", 0, true)));
  }

  @Test
  void markerOnlyDeclarationHasEmptyVersionedPayloadAndNoDeclarationHasNone() {
    List<RuntimeConfigurationMetadata.DeploymentTarget> noTargets = Collections.emptyList();
    assertNull(RuntimeConfigurationMetadata.encodeForDeployment(null, noTargets));
    List<RuntimeSelector> decoded = RuntimeConfigurationMetadata.decode(
        RuntimeConfigurationMetadata.encodeForDeployment(Collections.<RuntimeSelector>emptyList(), noTargets));
    assertTrue(decoded.isEmpty());
  }

  @Test
  void rejectsUnknownVersionsDimensionsValuesAndTrailingBytes() {
    byte[] valid = RuntimeConfigurationMetadata.encodeForDeployment(
        Collections.singletonList(RuntimeSelector.platform(Platform.WINDOWS)),
        Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList());

    byte[] unknownVersion = valid.clone();
    unknownVersion[4] = 3;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationMetadata.decode(unknownVersion)).getMessage().contains("unsupported metadata version"));
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationMetadata.decode(new byte[] { 0x54 })).getMessage().contains("truncated payload"));

    byte[] unknownDimension = valid.clone();
    unknownDimension[11] = 99;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationMetadata.decode(unknownDimension)).getMessage().contains("unknown dimension tag"));

    byte[] zeroDimension = valid.clone();
    zeroDimension[11] = 0;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationMetadata.decode(zeroDimension)).getMessage().contains("unknown dimension tag 0"));

    byte[] unknownValue = valid.clone();
    unknownValue[13] = 99;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationMetadata.decode(unknownValue)).getMessage().contains("unknown value tag"));

    byte[] zeroValue = valid.clone();
    zeroValue[13] = 0;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationMetadata.decode(zeroValue)).getMessage().contains("unknown value tag 0"));

    byte[] legacyZeroValue = new byte[] { 0x54, 0x43, 0x52, 0x43, 1, 0, 1, 0, 1, 1, 1, 1, 0 };
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationMetadata.decode(legacyZeroValue)).getMessage().contains("unknown value tag 0"));

    byte[] trailing = Arrays.copyOf(valid, valid.length + 1);
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> RuntimeConfigurationMetadata.decode(trailing)).getMessage().contains("trailing bytes"));
  }

  @Test
  void encoderUsesNonzeroIdsForEveryPublicEnvironmentFact() {
    List<RuntimeSelector> selectors = Arrays.asList(
        RuntimeSelector.platform(Platform.values()),
        RuntimeSelector.family(RuntimeFamily.values()),
        RuntimeSelector.backend(GraphicsBackend.values()),
        RuntimeSelector.architecture(Architecture.values()));
    byte[] encoded = RuntimeConfigurationMetadata.encodeForDeployment(selectors,
        Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList());

    int offset = 7;
    for (int selectorIndex = 0; selectorIndex < selectors.size(); selectorIndex++) {
      int clauseCount = unsignedShort(encoded, offset);
      offset += 2;
      assertEquals(1, clauseCount);
      int dimensionCount = encoded[offset++] & 0xff;
      offset++; // version 2 static-dimension mask
      for (int dimensionIndex = 0; dimensionIndex < dimensionCount; dimensionIndex++) {
        offset++; // dimension tag
        int valueCount = encoded[offset++] & 0xff;
        for (int valueIndex = 0; valueIndex < valueCount; valueIndex++) {
          assertTrue((encoded[offset++] & 0xff) > 0, "zero is reserved for unavailable native facts");
        }
      }
    }
    assertEquals(encoded.length, offset);
  }

  @Test
  void decodesVersionOneMetadataWithoutStaticSpecificity() {
    byte[] legacy = new byte[] { 0x54, 0x43, 0x52, 0x43, 1, 0, 1, 0, 1, 1, 1, 1, 1 };
    RuntimeSelector decoded = RuntimeConfigurationMetadata.decode(legacy).get(0);
    assertEquals(1, decoded.specificityFor(environment("Win32", 2, false)));
    assertTrue(decoded.matches(environment("Win32", 2, false)));
  }

  private static RuntimeEnvironment environment(String platform, int architecture, boolean isOpenGL) {
    return RuntimeEnvironmentTestSupport.environment(platform, architecture, isOpenGL);
  }

  private static int unsignedShort(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
  }
}
