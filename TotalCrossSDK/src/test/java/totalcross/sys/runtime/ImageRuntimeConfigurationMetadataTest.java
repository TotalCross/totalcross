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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import totalcross.sys.Architecture;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.ui.image.ImagePrefetchWorkerMode;
import totalcross.ui.image.ImageStorageProfile;

class ImageRuntimeConfigurationMetadataTest {
  @Test
  void roundTripsStorageRulesWithStableVersionTwoTags() {
    RuntimeSelector selector = RuntimeSelector.platform(Platform.MACOS)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP));
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Arrays.asList(
        rule("standard", selector, ImageStorageProfile.STANDARD),
        rule("compact", selector, ImageStorageProfile.COMPACT));
    byte[] encoded = ImageRuntimeConfigurationMetadata.encodeForDeployment(rules,
        Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList());
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> decoded =
        ImageRuntimeConfigurationMetadata.decode(encoded);

    assertArrayEquals(new byte[] { 0x54, 0x43, 0x49, 0x43 }, Arrays.copyOf(encoded, 4));
    assertEquals(2, encoded[4] & 0xff);
    assertEquals(2, unsignedShort(encoded, 5));
    assertEquals(0x01, storageTag(encoded, 0));
    assertEquals(0x02, storageTag(encoded, 1));
    assertEquals(ImageStorageProfile.STANDARD, decoded.get(0).requestedValue().storage());
    assertEquals(ImageStorageProfile.COMPACT, decoded.get(1).requestedValue().storage());
    assertTrue(decoded.get(0).selector().matches(RuntimeEnvironmentTestSupport.environment("MacOS", 4, false)));
    assertFalse(decoded.get(0).selector().matches(RuntimeEnvironmentTestSupport.environment("Linux", 4, false)));
  }

  @Test
  void roundTripsEveryPropertyAndSparseRulesWithExplicitPresenceBits() {
    RuntimeSelector selector = RuntimeSelector.platform(Platform.MACOS);
    ImageRuntimeOptions all = new ImageRuntimeOptions(ImageStorageProfile.COMPACT,
        RuntimeFeatureState.ENABLED, RuntimeFeatureState.DISABLED, RuntimeFeatureState.ENABLED,
        ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER);
    ImageRuntimeOptions sparse = new ImageRuntimeOptions(ImageStorageProfile.DEFAULT,
        RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.ENABLED,
        ImagePrefetchWorkerMode.DEFAULT);
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules = Arrays.asList(
        optionRule("all", selector, all), optionRule("sparse", selector, sparse));
    byte[] encoded = ImageRuntimeConfigurationMetadata.encodeForDeployment(rules,
        Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList());
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> decoded =
        ImageRuntimeConfigurationMetadata.decode(encoded);

    assertEquals(0x1f, presenceMask(encoded, 0));
    assertEquals(0x08, presenceMask(encoded, 1));
    int valuesOffset = 12 + readInt(encoded, 7);
    assertEquals(Arrays.asList(2, 1, 2, 1, 2), Arrays.asList(encoded[valuesOffset] & 0xff,
        encoded[valuesOffset + 1] & 0xff, encoded[valuesOffset + 2] & 0xff,
        encoded[valuesOffset + 3] & 0xff, encoded[valuesOffset + 4] & 0xff));
    assertEquals(2, decoded.size());
    assertEquals(ImageStorageProfile.COMPACT, decoded.get(0).requestedValue().storage());
    assertEquals(RuntimeFeatureState.ENABLED, decoded.get(0).requestedValue().targetColorConversion());
    assertEquals(RuntimeFeatureState.DISABLED, decoded.get(0).requestedValue().physicalVariantCache());
    assertEquals(RuntimeFeatureState.ENABLED, decoded.get(0).requestedValue().scrollRasterReuse());
    assertEquals(ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER,
        decoded.get(0).requestedValue().prefetchWorker());
    assertEquals(ImageStorageProfile.DEFAULT, decoded.get(1).requestedValue().storage());
    assertEquals(RuntimeFeatureState.DEFAULT, decoded.get(1).requestedValue().targetColorConversion());
    assertEquals(RuntimeFeatureState.DEFAULT, decoded.get(1).requestedValue().physicalVariantCache());
    assertEquals(RuntimeFeatureState.ENABLED, decoded.get(1).requestedValue().scrollRasterReuse());
    assertEquals(ImagePrefetchWorkerMode.DEFAULT, decoded.get(1).requestedValue().prefetchWorker());
  }

  @Test
  void decodesLegacyVersionOneStorageRulesWithNewPropertiesUnassigned() {
    byte[] encodedV2 = ImageRuntimeConfigurationMetadata.encodeForDeployment(
        Collections.singletonList(rule("legacy", RuntimeSelector.platform(Platform.MACOS),
            ImageStorageProfile.COMPACT)), Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList());
    byte[] encodedV1 = asVersionOne(encodedV2);
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> decoded =
        ImageRuntimeConfigurationMetadata.decode(encodedV1);

    assertEquals(1, encodedV1[4] & 0xff);
    assertEquals(ImageStorageProfile.COMPACT, decoded.get(0).requestedValue().storage());
    assertEquals(RuntimeFeatureState.DEFAULT, decoded.get(0).requestedValue().targetColorConversion());
    assertEquals(RuntimeFeatureState.DEFAULT, decoded.get(0).requestedValue().physicalVariantCache());
    assertEquals(RuntimeFeatureState.DEFAULT, decoded.get(0).requestedValue().scrollRasterReuse());
    assertEquals(ImagePrefetchWorkerMode.DEFAULT, decoded.get(0).requestedValue().prefetchWorker());
  }

  @Test
  void prunesImpossibleRulesAndPreservesSpecificityAcrossRetainedSelectors() {
    List<RuntimeConfigurationMetadata.DeploymentTarget> macArm64 = Collections.singletonList(
        new RuntimeConfigurationMetadata.DeploymentTarget(Platform.MACOS, RuntimeFamily.DESKTOP, Architecture.ARM64));
    RuntimeSelector architecture = RuntimeSelector.architecture(Architecture.ARM64);
    RuntimeSelector specific = RuntimeSelector.platform(Platform.MACOS)
        .and(RuntimeSelector.family(RuntimeFamily.DESKTOP))
        .and(RuntimeSelector.architecture(Architecture.ARM64));
    RuntimeSelector impossible = RuntimeSelector.platform(Platform.WINDOWS)
        .and(RuntimeSelector.family(RuntimeFamily.MOBILE));
    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> source = Arrays.asList(
        rule("architecture", architecture, ImageStorageProfile.COMPACT),
        optionRule("specific", specific, new ImageRuntimeOptions(ImageStorageProfile.STANDARD,
            RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.ENABLED,
            ImagePrefetchWorkerMode.DEFAULT)),
        rule("impossible", impossible, ImageStorageProfile.COMPACT));

    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> decoded =
        ImageRuntimeConfigurationMetadata.decode(ImageRuntimeConfigurationMetadata.encodeForDeployment(source,
            macArm64));
    RuntimeEnvironment target = RuntimeEnvironmentTestSupport.environment("MacOS", 4, false);

    assertEquals(2, decoded.size());
    assertEquals(1, decoded.get(0).selector().specificityFor(target));
    assertEquals(3, decoded.get(1).selector().specificityFor(target));
    assertEquals(ImageStorageProfile.STANDARD, decoded.get(1).requestedValue().storage());
    assertEquals(RuntimeFeatureState.ENABLED, decoded.get(1).requestedValue().scrollRasterReuse());
    assertNull(ImageRuntimeConfigurationMetadata.encodeForDeployment(
        Collections.singletonList(rule("pruned", impossible, ImageStorageProfile.COMPACT)), macArm64));
  }

  @Test
  void rejectsMalformedEnvelopesAndNestedSelectorPayloads() {
    byte[] valid = ImageRuntimeConfigurationMetadata.encodeForDeployment(
        Collections.singletonList(rule("compact", RuntimeSelector.platform(Platform.MACOS),
            ImageStorageProfile.COMPACT)), Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList());
    int payloadLength = readInt(valid, 7);
    int presenceOffset = 11 + payloadLength;
    int tagOffset = presenceOffset + 1;

    byte[] wrongMagic = valid.clone();
    wrongMagic[0] = 0;
    assertThrows(IllegalArgumentException.class, () -> ImageRuntimeConfigurationMetadata.decode(wrongMagic));

    byte[] wrongVersion = valid.clone();
    wrongVersion[4] = 3;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(wrongVersion)).getMessage().contains("unsupported metadata version"));

    byte[] invalidLength = valid.clone();
    invalidLength[7] = 0x7f;
    invalidLength[8] = (byte) 0xff;
    invalidLength[9] = (byte) 0xff;
    invalidLength[10] = (byte) 0xff;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(invalidLength)).getMessage().contains("invalid selector payload length"));

    byte[] malformedSelector = valid.clone();
    malformedSelector[11] = 0;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(malformedSelector)).getMessage().contains("invalid selector payload"));

    byte[] zeroTag = valid.clone();
    zeroTag[presenceOffset] = 0;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(zeroTag)).getMessage().contains("empty presence mask"));

    byte[] unknownTag = valid.clone();
    unknownTag[tagOffset] = 3;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(unknownTag)).getMessage().contains("unknown storage tag"));

    byte[] unknownPresenceBits = valid.clone();
    unknownPresenceBits[presenceOffset] = 0x21;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(unknownPresenceBits)).getMessage()
        .contains("unknown presence bits"));

    byte[] featureRule = ImageRuntimeConfigurationMetadata.encodeForDeployment(
        Collections.singletonList(optionRule("target", RuntimeSelector.platform(Platform.MACOS),
            new ImageRuntimeOptions(ImageStorageProfile.DEFAULT, RuntimeFeatureState.ENABLED,
                RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT, ImagePrefetchWorkerMode.DEFAULT))),
        Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList());
    int featurePresenceOffset = 11 + readInt(featureRule, 7);
    byte[] unknownFeatureTag = featureRule.clone();
    unknownFeatureTag[featurePresenceOffset + 1] = 3;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(unknownFeatureTag)).getMessage()
        .contains("unknown target-color conversion tag"));

    byte[] workerRule = ImageRuntimeConfigurationMetadata.encodeForDeployment(
        Collections.singletonList(optionRule("worker", RuntimeSelector.platform(Platform.MACOS),
            new ImageRuntimeOptions(ImageStorageProfile.DEFAULT, RuntimeFeatureState.DEFAULT,
                RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
                ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD))),
        Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList());
    int workerPresenceOffset = 11 + readInt(workerRule, 7);
    byte[] unknownWorkerTag = workerRule.clone();
    unknownWorkerTag[workerPresenceOffset + 1] = 3;
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(unknownWorkerTag)).getMessage()
        .contains("unknown prefetch worker tag"));

    byte[] trailing = Arrays.copyOf(valid, valid.length + 1);
    assertTrue(assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(trailing)).getMessage().contains("trailing bytes"));
    assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(Arrays.copyOf(valid, valid.length - 1)));
    assertEquals("TCIC", new String(valid, 0, 4, StandardCharsets.US_ASCII));

    byte[] temporaryMagic = valid.clone();
    temporaryMagic[3] = 'R';
    assertEquals("TCIR", new String(temporaryMagic, 0, 4, StandardCharsets.US_ASCII));
    IllegalArgumentException temporaryMagicFailure = assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.decode(temporaryMagic));
    assertTrue(temporaryMagicFailure.getMessage().contains("unexpected metadata signature"));
  }

  @Test
  void rejectsAnAllDefaultRuleDuringEncoding() {
    ImageRuntimeOptions empty = new ImageRuntimeOptions(ImageStorageProfile.DEFAULT,
        RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
        ImagePrefetchWorkerMode.DEFAULT);
    IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
        () -> ImageRuntimeConfigurationMetadata.encodeForDeployment(
            Collections.singletonList(optionRule("empty", RuntimeSelector.platform(Platform.MACOS), empty)),
            Collections.<RuntimeConfigurationMetadata.DeploymentTarget>emptyList()));
    assertTrue(failure.getMessage().contains("must explicitly assign at least one option"));
  }

  private static RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions> rule(String name,
      RuntimeSelector selector, ImageStorageProfile profile) {
    return new RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>(name, selector,
        ImageRuntimeOptions.storageOnly(profile));
  }

  private static RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions> optionRule(String name,
      RuntimeSelector selector, ImageRuntimeOptions options) {
    return new RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>(name, selector, options);
  }

  private static byte[] asVersionOne(byte[] encodedV2) {
    byte[] encodedV1 = new byte[encodedV2.length - 1];
    int presenceOffset = 11 + readInt(encodedV2, 7);
    System.arraycopy(encodedV2, 0, encodedV1, 0, presenceOffset);
    encodedV1[4] = 1;
    System.arraycopy(encodedV2, presenceOffset + 1, encodedV1, presenceOffset,
        encodedV2.length - presenceOffset - 1);
    return encodedV1;
  }

  private static int presenceMask(byte[] encoded, int index) {
    int offset = 7;
    for (int i = 0; i < index; i++) {
      offset += 4 + readInt(encoded, offset) + 1 + valueCount(encoded, offset);
    }
    return encoded[offset + 4 + readInt(encoded, offset)] & 0xff;
  }

  private static int valueCount(byte[] encoded, int ruleOffset) {
    int selectorLength = readInt(encoded, ruleOffset);
    int mask = encoded[ruleOffset + 4 + selectorLength] & 0xff;
    return Integer.bitCount(mask);
  }

  private static int storageTag(byte[] encoded, int index) {
    int offset = 7;
    for (int i = 0; i < index; i++) {
      offset += 4 + readInt(encoded, offset) + 2;
    }
    int length = readInt(encoded, offset);
    return encoded[offset + 5 + length] & 0xff;
  }

  private static int unsignedShort(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
  }

  private static int readInt(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xff) << 24) | ((bytes[offset + 1] & 0xff) << 16)
        | ((bytes[offset + 2] & 0xff) << 8) | (bytes[offset + 3] & 0xff);
  }
}
