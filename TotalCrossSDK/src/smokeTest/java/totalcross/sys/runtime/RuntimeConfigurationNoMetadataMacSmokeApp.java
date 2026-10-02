// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Vm;
import totalcross.ui.image.ImagePrefetchWorkerMode;
import totalcross.ui.image.ImageStorageProfile;
import totalcross.ui.MainWindow;

public final class RuntimeConfigurationNoMetadataMacSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    try {
      RuntimeEnvironment environment = RuntimeEnvironment.current();
      require(environment.platform() == Platform.MACOS, "platform=" + environment.platform());
      require(environment.runtimeFamily() == RuntimeFamily.DESKTOP, "family=" + environment.runtimeFamily());
      require(environment.architecture() == Architecture.ARM64, "architecture=" + environment.architecture());
      require(environment.isGraphicsBackendFinalized(), "graphics backend was not finalized");
      require(environment.graphicsBackend() == GraphicsBackend.RASTER,
          "backend=" + environment.graphicsBackend());
      require(Vm.getFile("tc.runtimeconfig") == null, "runtime metadata resource exists");
      require(Vm.getFile("tc.imageruntimeconfig") == null, "Image runtime metadata resource exists");
      require(RuntimeConfigurationStartup.current() == null, "configuration exists without metadata");
      ImageRuntimePolicy imagePolicy = ImageRuntimeConfigurationStartup.currentPolicy();
      require(imagePolicy.requestedStorageProfile() == ImageStorageProfile.STANDARD,
          "requestedStorage=" + imagePolicy.requestedStorageProfile());
      require(imagePolicy.effectiveStorageProfile() == ImageStorageProfile.STANDARD,
          "effectiveStorage=" + imagePolicy.effectiveStorageProfile());
      require(imagePolicy.rasterCore().zeroCopyDecode()
          && imagePolicy.rasterCore().opacityMetadata()
          && imagePolicy.rasterCore().opaqueWritePixels()
          && imagePolicy.rasterCore().rowReadback()
          && imagePolicy.rasterCore().directColorMaterialization()
          && imagePolicy.rasterCore().physicalIdentity(), "stable raster defaults were not enabled");
      require(!imagePolicy.rasterVariants().targetColorConversion()
          && !imagePolicy.rasterVariants().physicalVariantCache()
          && !imagePolicy.scrollRasterReuse().enabled()
          && !imagePolicy.imagePreparation().automaticPreparation(), "opt-in defaults were not disabled");
      require(imagePolicy.prefetchWorker() == ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD,
          "prefetch worker=" + imagePolicy.prefetchWorker());
      String report = RuntimeConfigurationReport.describe();
      require(report.contains("Image\n  storage:\n    requested: STANDARD\n    effective: STANDARD"),
          "Image default policy missing from report");
      System.out.println("runtime-config-no-metadata-pass platform=" + environment.platform()
          + " family=" + environment.runtimeFamily() + " architecture=" + environment.architecture()
          + " backend=" + environment.graphicsBackend() + " metadata=absent"
          + " imageStorageRequested=" + imagePolicy.requestedStorageProfile()
          + " imageStorageEffective=" + imagePolicy.effectiveStorageProfile()
          + " prefetchWorker=" + imagePolicy.prefetchWorker());
      System.out.flush();
      exit(0);
    } catch (Throwable failure) {
      System.out.println("runtime-config-no-metadata-fail error=" + failure.getMessage());
      System.out.flush();
      exit(1);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
