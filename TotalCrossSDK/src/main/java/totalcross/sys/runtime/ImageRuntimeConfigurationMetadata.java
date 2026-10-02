// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import totalcross.ui.image.ImageStorageProfile;

/** Internal versioned codec for Image runtime configuration TCZ metadata. */
public final class ImageRuntimeConfigurationMetadata {
  private static final int MAGIC = 0x54434943;
  private static final int VERSION = 1;
  private static final int STANDARD_TAG = 0x01;
  private static final int COMPACT_TAG = 0x02;

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
      byte[] selectorPayload = RuntimeConfigurationFeatureBridge.encodeSingleSelectorForDeployment(
          rule.selector(), targets);
      if (selectorPayload != null) {
        ImageRuntimeOptions options = rule.requestedValue();
        if (options.storage() == ImageStorageProfile.DEFAULT
            || options.targetColorConversion() != RuntimeFeatureState.DEFAULT
            || options.physicalVariantCache() != RuntimeFeatureState.DEFAULT
            || options.scrollRasterReuse() != RuntimeFeatureState.DEFAULT
            || options.prefetchWorker() != totalcross.ui.image.ImagePrefetchWorkerMode.DEFAULT) {
          throw new IllegalArgumentException("Image runtime metadata v1 only supports explicit storage rules");
        }
        retained.add(new EncodedRule(selectorPayload, storageTag(options.storage())));
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
      output.write(rule.storageTag);
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
    if (version != VERSION) {
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
      ImageStorageProfile storage = storageProfile(input.readUnsignedByte(), i);
      rules.add(new RuntimeConfigurationFeatureBridge.FeatureRule<ImageRuntimeOptions>("image-rule-" + i,
          selector, ImageRuntimeOptions.storageOnly(storage)));
    }
    if (input.remaining() != 0) {
      throw invalid("contains trailing bytes");
    }
    return Collections.unmodifiableList(rules);
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
    private final int storageTag;

    private EncodedRule(byte[] selectorPayload, int storageTag) {
      this.selectorPayload = selectorPayload;
      this.storageTag = storageTag;
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
