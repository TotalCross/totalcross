// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import totalcross.ui.image.ImagePrefetchWorkerMode;
import totalcross.ui.image.ImageStorageProfile;

/** Internal versioned codec for Image runtime configuration TCZ metadata. */
public final class ImageRuntimeConfigurationMetadata {
  private static final int MAGIC = 0x54434943;
  private static final int VERSION_1 = 1;
  private static final int VERSION = 2;

  private static final int STORAGE_PRESENT = 1 << 0;
  private static final int TARGET_COLOR_CONVERSION_PRESENT = 1 << 1;
  private static final int PHYSICAL_VARIANT_CACHE_PRESENT = 1 << 2;
  private static final int SCROLL_RASTER_REUSE_PRESENT = 1 << 3;
  private static final int PREFETCH_WORKER_PRESENT = 1 << 4;
  private static final int KNOWN_PRESENCE_BITS = STORAGE_PRESENT | TARGET_COLOR_CONVERSION_PRESENT
      | PHYSICAL_VARIANT_CACHE_PRESENT | SCROLL_RASTER_REUSE_PRESENT | PREFETCH_WORKER_PRESENT;

  private static final int STANDARD_TAG = 0x01;
  private static final int COMPACT_TAG = 0x02;
  private static final int ENABLED_TAG = 0x01;
  private static final int DISABLED_TAG = 0x02;
  private static final int LEGACY_PER_ENTRY_THREAD_TAG = 0x01;
  private static final int SEMAPHORE_PROCESS_WORKER_TAG = 0x02;

  private ImageRuntimeConfigurationMetadata() {
  }

  /** Encodes retained Image rules, returning {@code null} when deployment prunes every rule. */
  public static byte[] encodeForDeployment(
      List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules,
      List<RuntimeConfigurationMetadata.DeploymentTarget> targets) {
    if (rules == null) {
      return null;
    }
    List<EncodedRule> retained = new ArrayList<EncodedRule>(rules.size());
    for (RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions> rule : rules) {
      if (rule == null) {
        throw new IllegalArgumentException("Image runtime configuration rules cannot contain null");
      }
      ImageRuntimeOptions options = rule.requestedValue();
      if (!options.hasExplicitAssignment()) {
        throw new IllegalArgumentException("Image runtime configuration rule '" + rule.name()
            + "' must explicitly assign at least one option");
      }
      byte[] selectorPayload = RuntimeConfigurationFeatureBridge.encodeSingleSelectorForDeployment(
          rule.selector(), targets);
      if (selectorPayload != null) {
        retained.add(new EncodedRule(selectorPayload, presenceMask(options), options));
      }
    }
    if (retained.isEmpty()) {
      return null;
    }
    if (retained.size() > 0xffff) {
      throw new IllegalArgumentException("Image runtime configuration has too many rules");
    }

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    writeInt(output, MAGIC);
    output.write(VERSION);
    writeShort(output, retained.size());
    for (EncodedRule rule : retained) {
      writeInt(output, rule.selectorPayload.length);
      output.write(rule.selectorPayload, 0, rule.selectorPayload.length);
      output.write(rule.presenceMask);
      writeValues(output, rule.presenceMask, rule.options);
    }
    return output.toByteArray();
  }

  /** Decodes Image rules and rejects malformed, unsupported, or non-canonical envelopes. */
  public static List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> decode(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      throw invalid("metadata is empty");
    }
    Reader input = new Reader(bytes);
    if (input.readInt() != MAGIC) {
      throw invalid("unexpected metadata signature");
    }
    int version = input.readUnsignedByte();
    if (version != VERSION_1 && version != VERSION) {
      throw invalid("unsupported metadata version " + version);
    }
    int count = input.readUnsignedShort();
    if (count == 0) {
      throw invalid("must contain at least one retained rule");
    }

    List<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>> rules =
        new ArrayList<RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>>(count);
    for (int i = 0; i < count; i++) {
      long payloadLength = input.readUnsignedInt();
      if (payloadLength > Integer.MAX_VALUE || payloadLength > input.remaining() - 1L) {
        throw invalid("rule " + i + " has an invalid selector payload length");
      }
      byte[] selectorPayload = input.readBytes((int) payloadLength);
      RuntimeSelector selector;
      try {
        selector = RuntimeConfigurationFeatureBridge.decodeSingleSelector(selectorPayload);
      } catch (IllegalArgumentException e) {
        throw invalid("rule " + i + " contains an invalid selector payload: " + e.getMessage());
      }
      ImageRuntimeOptions options = version == VERSION_1
          ? ImageRuntimeOptions.storageOnly(storageProfile(input.readUnsignedByte(), i))
          : readOptions(input, i);
      rules.add(new RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>("image-rule-" + i,
          selector, options));
    }
    if (input.remaining() != 0) {
      throw invalid("contains trailing bytes");
    }
    return Collections.unmodifiableList(rules);
  }

  private static int presenceMask(ImageRuntimeOptions options) {
    int mask = 0;
    if (options.storage() != ImageStorageProfile.DEFAULT) {
      mask |= STORAGE_PRESENT;
    }
    if (options.targetColorConversion() != RuntimeFeatureState.DEFAULT) {
      mask |= TARGET_COLOR_CONVERSION_PRESENT;
    }
    if (options.physicalVariantCache() != RuntimeFeatureState.DEFAULT) {
      mask |= PHYSICAL_VARIANT_CACHE_PRESENT;
    }
    if (options.scrollRasterReuse() != RuntimeFeatureState.DEFAULT) {
      mask |= SCROLL_RASTER_REUSE_PRESENT;
    }
    if (options.prefetchWorker() != ImagePrefetchWorkerMode.DEFAULT) {
      mask |= PREFETCH_WORKER_PRESENT;
    }
    if (mask == 0) {
      throw new IllegalArgumentException("Image runtime configuration rule has an empty presence mask");
    }
    return mask;
  }

  private static void writeValues(ByteArrayOutputStream output, int mask, ImageRuntimeOptions options) {
    if ((mask & STORAGE_PRESENT) != 0) {
      output.write(storageTag(options.storage()));
    }
    if ((mask & TARGET_COLOR_CONVERSION_PRESENT) != 0) {
      output.write(featureStateTag(options.targetColorConversion()));
    }
    if ((mask & PHYSICAL_VARIANT_CACHE_PRESENT) != 0) {
      output.write(featureStateTag(options.physicalVariantCache()));
    }
    if ((mask & SCROLL_RASTER_REUSE_PRESENT) != 0) {
      output.write(featureStateTag(options.scrollRasterReuse()));
    }
    if ((mask & PREFETCH_WORKER_PRESENT) != 0) {
      output.write(prefetchWorkerTag(options.prefetchWorker()));
    }
  }

  private static ImageRuntimeOptions readOptions(Reader input, int ruleIndex) {
    int mask = input.readUnsignedByte();
    if ((mask & ~KNOWN_PRESENCE_BITS) != 0) {
      throw invalid("rule " + ruleIndex + " has unknown presence bits 0x" + Integer.toHexString(mask));
    }
    if (mask == 0) {
      throw invalid("rule " + ruleIndex + " has an empty presence mask");
    }
    ImageStorageProfile storage = (mask & STORAGE_PRESENT) != 0
        ? storageProfile(input.readUnsignedByte(), ruleIndex) : ImageStorageProfile.DEFAULT;
    RuntimeFeatureState targetColorConversion = (mask & TARGET_COLOR_CONVERSION_PRESENT) != 0
        ? featureState(input.readUnsignedByte(), ruleIndex, "target-color conversion") : RuntimeFeatureState.DEFAULT;
    RuntimeFeatureState physicalVariantCache = (mask & PHYSICAL_VARIANT_CACHE_PRESENT) != 0
        ? featureState(input.readUnsignedByte(), ruleIndex, "physical variant cache") : RuntimeFeatureState.DEFAULT;
    RuntimeFeatureState scrollRasterReuse = (mask & SCROLL_RASTER_REUSE_PRESENT) != 0
        ? featureState(input.readUnsignedByte(), ruleIndex, "scroll raster reuse") : RuntimeFeatureState.DEFAULT;
    ImagePrefetchWorkerMode prefetchWorker = (mask & PREFETCH_WORKER_PRESENT) != 0
        ? prefetchWorker(input.readUnsignedByte(), ruleIndex) : ImagePrefetchWorkerMode.DEFAULT;
    return new ImageRuntimeOptions(storage, targetColorConversion, physicalVariantCache,
        scrollRasterReuse, prefetchWorker);
  }

  private static int storageTag(ImageStorageProfile storage) {
    if (storage == ImageStorageProfile.STANDARD) {
      return STANDARD_TAG;
    }
    if (storage == ImageStorageProfile.COMPACT) {
      return COMPACT_TAG;
    }
    throw new IllegalArgumentException("unsupported Image storage profile " + storage);
  }

  private static ImageStorageProfile storageProfile(int tag, int ruleIndex) {
    if (tag == STANDARD_TAG) {
      return ImageStorageProfile.STANDARD;
    }
    if (tag == COMPACT_TAG) {
      return ImageStorageProfile.COMPACT;
    }
    throw invalid("rule " + ruleIndex + " has unknown storage tag 0x" + Integer.toHexString(tag));
  }

  private static int featureStateTag(RuntimeFeatureState state) {
    if (state == RuntimeFeatureState.ENABLED) {
      return ENABLED_TAG;
    }
    if (state == RuntimeFeatureState.DISABLED) {
      return DISABLED_TAG;
    }
    throw new IllegalArgumentException("DEFAULT cannot be encoded as an Image feature assignment");
  }

  private static RuntimeFeatureState featureState(int tag, int ruleIndex, String property) {
    if (tag == ENABLED_TAG) {
      return RuntimeFeatureState.ENABLED;
    }
    if (tag == DISABLED_TAG) {
      return RuntimeFeatureState.DISABLED;
    }
    throw invalid("rule " + ruleIndex + " has unknown " + property + " tag 0x" + Integer.toHexString(tag));
  }

  private static int prefetchWorkerTag(ImagePrefetchWorkerMode worker) {
    if (worker == ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD) {
      return LEGACY_PER_ENTRY_THREAD_TAG;
    }
    if (worker == ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER) {
      return SEMAPHORE_PROCESS_WORKER_TAG;
    }
    throw new IllegalArgumentException("DEFAULT cannot be encoded as an Image prefetch worker assignment");
  }

  private static ImagePrefetchWorkerMode prefetchWorker(int tag, int ruleIndex) {
    if (tag == LEGACY_PER_ENTRY_THREAD_TAG) {
      return ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD;
    }
    if (tag == SEMAPHORE_PROCESS_WORKER_TAG) {
      return ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER;
    }
    throw invalid("rule " + ruleIndex + " has unknown prefetch worker tag 0x" + Integer.toHexString(tag));
  }

  private static void writeShort(ByteArrayOutputStream output, int value) {
    output.write((value >>> 8) & 0xff);
    output.write(value & 0xff);
  }

  private static void writeInt(ByteArrayOutputStream output, int value) {
    output.write((value >>> 24) & 0xff);
    output.write((value >>> 16) & 0xff);
    output.write((value >>> 8) & 0xff);
    output.write(value & 0xff);
  }

  private static IllegalArgumentException invalid(String message) {
    return new IllegalArgumentException("Invalid Image runtime configuration metadata: " + message);
  }

  private static final class EncodedRule {
    private final byte[] selectorPayload;
    private final int presenceMask;
    private final ImageRuntimeOptions options;

    private EncodedRule(byte[] selectorPayload, int presenceMask, ImageRuntimeOptions options) {
      this.selectorPayload = selectorPayload;
      this.presenceMask = presenceMask;
      this.options = options;
    }
  }

  private static final class Reader {
    private final byte[] bytes;
    private int position;

    private Reader(byte[] bytes) {
      this.bytes = bytes;
    }

    private int readUnsignedByte() {
      ensureAvailable(1);
      return bytes[position++] & 0xff;
    }

    private int readUnsignedShort() {
      return (readUnsignedByte() << 8) | readUnsignedByte();
    }

    private int readInt() {
      return (readUnsignedByte() << 24) | (readUnsignedByte() << 16)
          | (readUnsignedByte() << 8) | readUnsignedByte();
    }

    private long readUnsignedInt() {
      return readInt() & 0xffffffffL;
    }

    private byte[] readBytes(int length) {
      ensureAvailable(length);
      byte[] result = new byte[length];
      System.arraycopy(bytes, position, result, 0, length);
      position += length;
      return result;
    }

    private int remaining() {
      return bytes.length - position;
    }

    private void ensureAvailable(int length) {
      if (length < 0 || length > remaining()) {
        throw invalid("metadata is truncated");
      }
    }
  }
}
