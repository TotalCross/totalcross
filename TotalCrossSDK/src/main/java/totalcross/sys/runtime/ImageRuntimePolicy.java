// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import totalcross.ui.image.ImageStorageProfile;

/** Internal immutable resolved Image policy snapshot published during startup. */
public final class ImageRuntimePolicy {
  private static final String COMPACT_UNAVAILABLE_REASON = "compact storage is not available in P1";
  private static final RasterCorePolicy DEFAULT_RASTER_CORE = new RasterCorePolicy(true, true, true, true, true, true);
  private static final RasterVariantPolicy DEFAULT_RASTER_VARIANTS = new RasterVariantPolicy(false, false);
  private static final ScrollRasterReusePolicy DEFAULT_SCROLL_REUSE = new ScrollRasterReusePolicy(false);
  private static final ImagePreparationPolicy DEFAULT_PREPARATION = new ImagePreparationPolicy(false);
  private static final PrefetchWorkerPolicy DEFAULT_PREFETCH_WORKER = PrefetchWorkerPolicy.LEGACY_PER_ENTRY_THREAD;

  private final ImageStorageProfile requestedStorageProfile;
  private final ImageStorageProfile effectiveStorageProfile;
  private final String storageReason;
  private final List<String> matchedRuleNames;
  private final RasterCorePolicy rasterCore;
  private final RasterVariantPolicy rasterVariants;
  private final ScrollRasterReusePolicy scrollRasterReuse;
  private final ImagePreparationPolicy imagePreparation;
  private final PrefetchWorkerPolicy prefetchWorker;

  private ImageRuntimePolicy(ImageStorageProfile requestedStorageProfile,
      ImageStorageProfile effectiveStorageProfile, String storageReason, List<String> matchedRuleNames) {
    if (requestedStorageProfile == null || effectiveStorageProfile == null || matchedRuleNames == null) {
      throw new IllegalArgumentException("an Image policy requires requested and effective storage values");
    }
    this.requestedStorageProfile = requestedStorageProfile;
    this.effectiveStorageProfile = effectiveStorageProfile;
    this.storageReason = storageReason;
    this.matchedRuleNames = Collections.unmodifiableList(new ArrayList<String>(matchedRuleNames));
    this.rasterCore = DEFAULT_RASTER_CORE;
    this.rasterVariants = DEFAULT_RASTER_VARIANTS;
    this.scrollRasterReuse = DEFAULT_SCROLL_REUSE;
    this.imagePreparation = DEFAULT_PREPARATION;
    this.prefetchWorker = DEFAULT_PREFETCH_WORKER;
  }

  static ImageRuntimePolicy defaults() {
    return new ImageRuntimePolicy(ImageStorageProfile.STANDARD, ImageStorageProfile.STANDARD, null,
        Collections.<String>emptyList());
  }

  /** Resolves the request against P1 support; P4 owns COMPACT storage capability support. */
  static ImageRuntimePolicy forRequestedStorage(ImageStorageProfile requestedStorageProfile,
      List<String> matchedRuleNames) {
    // P1 has no compact backing format; later capability work can relax this downgrade.
    ImageStorageProfile effectiveStorageProfile = ImageStorageProfile.STANDARD;
    String reason = requestedStorageProfile == effectiveStorageProfile ? null : COMPACT_UNAVAILABLE_REASON;
    return new ImageRuntimePolicy(requestedStorageProfile, effectiveStorageProfile, reason, matchedRuleNames);
  }

  /** Returns the rule-selected Image storage request. Internal feature access only. */
  public ImageStorageProfile requestedStorageProfile() {
    return requestedStorageProfile;
  }

  /** Returns the storage profile supported by this runtime. Internal feature access only. */
  public ImageStorageProfile effectiveStorageProfile() {
    return effectiveStorageProfile;
  }

  /** Returns a reason when the runtime downgraded the requested storage profile. */
  public String storageReason() {
    return storageReason;
  }

  /** Returns diagnostic names of matching Image rules. */
  public List<String> matchedRuleNames() {
    return matchedRuleNames;
  }

  public RasterCorePolicy rasterCore() {
    return rasterCore;
  }

  public RasterVariantPolicy rasterVariants() {
    return rasterVariants;
  }

  public ScrollRasterReusePolicy scrollRasterReuse() {
    return scrollRasterReuse;
  }

  public ImagePreparationPolicy imagePreparation() {
    return imagePreparation;
  }

  public PrefetchWorkerPolicy prefetchWorker() {
    return prefetchWorker;
  }

  /**
   * Formats the resolved typed policy, including defaults reserved for future
   * consumers. A field is operational only once its owning feature
   * implementation consumes that policy.
   */
  String describeSection() {
    StringBuilder output = new StringBuilder(320);
    output.append("  storage:\n");
    output.append("    requested: ").append(requestedStorageProfile.name()).append('\n');
    output.append("    effective: ").append(effectiveStorageProfile.name()).append('\n');
    if (storageReason != null) {
      output.append("    reason: ").append(storageReason).append('\n');
    }
    output.append("\n  rasterCore:\n");
    output.append("    zeroCopyDecode: ").append(enabled(rasterCore.zeroCopyDecode)).append('\n');
    output.append("    opacityMetadata: ").append(enabled(rasterCore.opacityMetadata)).append('\n');
    output.append("    opaqueWritePixels: ").append(enabled(rasterCore.opaqueWritePixels)).append('\n');
    output.append("    rowReadback: ").append(enabled(rasterCore.rowReadback)).append('\n');
    output.append("    directColorMaterialization: ")
        .append(enabled(rasterCore.directColorMaterialization)).append('\n');
    output.append("    physicalIdentity: ").append(enabled(rasterCore.physicalIdentity)).append('\n');
    output.append("\n  rasterVariants:\n");
    output.append("    targetColorConversion: ").append(enabled(rasterVariants.targetColorConversion)).append('\n');
    output.append("    physicalVariantCache: ").append(enabled(rasterVariants.physicalVariantCache)).append('\n');
    output.append("\n  scrollRasterReuse: ").append(enabled(scrollRasterReuse.enabled)).append('\n');
    output.append("\n  imagePreparation:\n");
    output.append("    automaticPreparation: ").append(enabled(imagePreparation.automaticPreparation)).append('\n');
    output.append("\n  prefetchWorker: ").append(prefetchWorker.name()).append('\n');
    return output.toString();
  }

  private static String enabled(boolean enabled) {
    return enabled ? "enabled" : "disabled";
  }

  /** Typed raster-core defaults consumed by P2/P3. */
  public static final class RasterCorePolicy {
    private final boolean zeroCopyDecode;
    private final boolean opacityMetadata;
    private final boolean opaqueWritePixels;
    private final boolean rowReadback;
    private final boolean directColorMaterialization;
    private final boolean physicalIdentity;

    private RasterCorePolicy(boolean zeroCopyDecode, boolean opacityMetadata, boolean opaqueWritePixels,
        boolean rowReadback, boolean directColorMaterialization, boolean physicalIdentity) {
      this.zeroCopyDecode = zeroCopyDecode;
      this.opacityMetadata = opacityMetadata;
      this.opaqueWritePixels = opaqueWritePixels;
      this.rowReadback = rowReadback;
      this.directColorMaterialization = directColorMaterialization;
      this.physicalIdentity = physicalIdentity;
    }

    public boolean zeroCopyDecode() { return zeroCopyDecode; }
    public boolean opacityMetadata() { return opacityMetadata; }
    public boolean opaqueWritePixels() { return opaqueWritePixels; }
    public boolean rowReadback() { return rowReadback; }
    public boolean directColorMaterialization() { return directColorMaterialization; }
    public boolean physicalIdentity() { return physicalIdentity; }
  }

  /** Typed raster-variant defaults consumed by P3. */
  public static final class RasterVariantPolicy {
    private final boolean targetColorConversion;
    private final boolean physicalVariantCache;

    private RasterVariantPolicy(boolean targetColorConversion, boolean physicalVariantCache) {
      this.targetColorConversion = targetColorConversion;
      this.physicalVariantCache = physicalVariantCache;
    }

    public boolean targetColorConversion() { return targetColorConversion; }
    public boolean physicalVariantCache() { return physicalVariantCache; }
  }

  /** Typed scroll-raster-reuse default consumed by P7. */
  public static final class ScrollRasterReusePolicy {
    private final boolean enabled;

    private ScrollRasterReusePolicy(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean enabled() { return enabled; }
  }

  /** Typed image-preparation default consumed by P8. */
  public static final class ImagePreparationPolicy {
    private final boolean automaticPreparation;

    private ImagePreparationPolicy(boolean automaticPreparation) {
      this.automaticPreparation = automaticPreparation;
    }

    public boolean automaticPreparation() { return automaticPreparation; }
  }

  /** Typed prefetch-worker default consumed by P9. */
  public enum PrefetchWorkerPolicy {
    LEGACY_PER_ENTRY_THREAD
  }
}
