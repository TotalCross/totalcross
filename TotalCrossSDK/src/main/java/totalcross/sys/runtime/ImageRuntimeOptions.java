// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import totalcross.ui.image.ImagePrefetchWorkerMode;
import totalcross.ui.image.ImageStorageProfile;

/** Internal immutable set of optional assignments carried by one Image runtime rule. */
public final class ImageRuntimeOptions {
  private final ImageStorageProfile storage;
  private final RuntimeFeatureState targetColorConversion;
  private final RuntimeFeatureState physicalVariantCache;
  private final RuntimeFeatureState scrollRasterReuse;
  private final ImagePrefetchWorkerMode prefetchWorker;

  public ImageRuntimeOptions(ImageStorageProfile storage, RuntimeFeatureState targetColorConversion,
      RuntimeFeatureState physicalVariantCache, RuntimeFeatureState scrollRasterReuse,
      ImagePrefetchWorkerMode prefetchWorker) {
    if (storage == null || targetColorConversion == null || physicalVariantCache == null
        || scrollRasterReuse == null || prefetchWorker == null) {
      throw new IllegalArgumentException("Image runtime options must use non-null typed values");
    }
    this.storage = storage;
    this.targetColorConversion = targetColorConversion;
    this.physicalVariantCache = physicalVariantCache;
    this.scrollRasterReuse = scrollRasterReuse;
    this.prefetchWorker = prefetchWorker;
  }

  public static ImageRuntimeOptions storageOnly(ImageStorageProfile storage) {
    return new ImageRuntimeOptions(storage, RuntimeFeatureState.DEFAULT, RuntimeFeatureState.DEFAULT,
        RuntimeFeatureState.DEFAULT, ImagePrefetchWorkerMode.DEFAULT);
  }

  public ImageStorageProfile storage() { return storage; }
  public RuntimeFeatureState targetColorConversion() { return targetColorConversion; }
  public RuntimeFeatureState physicalVariantCache() { return physicalVariantCache; }
  public RuntimeFeatureState scrollRasterReuse() { return scrollRasterReuse; }
  public ImagePrefetchWorkerMode prefetchWorker() { return prefetchWorker; }

  public boolean hasExplicitAssignment() {
    return storage != ImageStorageProfile.DEFAULT
        || targetColorConversion != RuntimeFeatureState.DEFAULT
        || physicalVariantCache != RuntimeFeatureState.DEFAULT
        || scrollRasterReuse != RuntimeFeatureState.DEFAULT
        || prefetchWorker != ImagePrefetchWorkerMode.DEFAULT;
  }
}
