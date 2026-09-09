// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Small warm-path comparison for materialized and deferred image draws. */
public class ImageWarmCopyRectBenchmarkApp extends MainWindow {
  private static final int SAMPLE_COUNT = 10;
  private static final int WARMUP_COUNT = 3;
  private static final int OPERATIONS_PER_SAMPLE = 256;
  private static final int IMAGE_SIZE = 64;

  @Override
  public void initUI() {
    String error = "";
    boolean overallPass = false;
    try {
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures("post-enabled", false, false);
      ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
          ImageOptimizationSettings.ENABLED);
      byte[] encoded = ImageRasterBenchmarkSupport.opaquePng(IMAGE_SIZE, IMAGE_SIZE);
      Image materialized = ImageRasterBenchmarkSupport.materialize(encoded);
      Image deferred = new Image(encoded, encoded.length).getSmoothScaledInstance(
          IMAGE_SIZE, IMAGE_SIZE);
      long expectedHash = runCase(materialized, "materialized", false);
      long deferredDrawHash = runCase(deferred, "deferred-drawImage", false);
      long deferredCopyHash = runCase(deferred, "deferred-copyRect", false);
      ImageRasterBenchmarkSupport.require(expectedHash == deferredDrawHash
          && expectedHash == deferredCopyHash, "full draw hashes differ");
      long clippedExpectedHash = runCase(materialized, "materialized", true);
      long clippedDrawHash = runCase(deferred, "deferred-drawImage", true);
      long clippedCopyHash = runCase(deferred, "deferred-copyRect", true);
      ImageRasterBenchmarkSupport.require(clippedExpectedHash == clippedDrawHash
          && clippedExpectedHash == clippedCopyHash, "partial draw hashes differ");
      overallPass = true;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_').replace(',', '_');
    }
    System.out.println("fixture=ImageWarmCopyRectBenchmarkApp,overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private long runCase(Image image, String operation, boolean clip) throws Exception {
    Image target = Image.createLogical(96, 96, 1);
    Graphics canvas = target.getGraphics();
    ImageRasterBenchmarkSupport.require(canvas != null, "target graphics");
    for (int warmup = 0; warmup < WARMUP_COUNT; warmup++) {
      runBatch(canvas, image, operation, clip);
    }
    long total = 0;
    long hash = 0;
    for (int sample = 0; sample < SAMPLE_COUNT; sample++) {
      long start = Vm.getTimeStamp();
      runBatch(canvas, image, operation, clip);
      total += Vm.getTimeStamp() - start;
      hash = ImageRasterBenchmarkSupport.fullPixelHash(target);
    }
    double average = total / (double) SAMPLE_COUNT;
    System.out.println("fixture=ImageWarmCopyRectBenchmarkApp,record=case"
        + ",operation=" + operation + ",clip=" + (clip ? "partial" : "full")
        + ",average_ms=" + average + ",operations=" + OPERATIONS_PER_SAMPLE
        + ",hash=" + ImageRasterBenchmarkSupport.hashString(hash)
        + ",materializations=" + Image.materializationCountForTest()
        + ",native_geometry_materializations=" + Image.nativeGeometryMaterializationCountForTest()
        + ",generic_geometry_draws=" + NativeImageBacking.genericGeometryDrawsForTest()
        + ",smooth_resample_draws=" + NativeImageBacking.smoothResampleDrawsForTest());
    canvas.clearClip();
    return hash;
  }

  private static void runBatch(Graphics canvas, Image image, String operation, boolean clip) {
    if (clip) {
      canvas.setClip(24, 24, 40, 40);
    } else {
      canvas.clearClip();
    }
    for (int index = 0; index < OPERATIONS_PER_SAMPLE; index++) {
      int x = 16 + ((index & 1) * 2);
      int y = 16 + (((index >>> 1) & 1) * 2);
      if ("deferred-drawImage".equals(operation)) {
        canvas.drawImage(image, x, y, true);
      } else {
        canvas.copyRect(image, 0, 0, IMAGE_SIZE, IMAGE_SIZE, x, y);
      }
    }
  }
}
