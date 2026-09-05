// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.MainWindow;

/** Deployed macOS regression smoke for native diagnostic accounting state. */
public class ImageDiagnosticAccountingGateSmokeApp extends MainWindow {
  private static final int WIDTH = 16;
  private static final int HEIGHT = 16;

  @Override
  public void initUI() {
    boolean overallPass = false;
    String error = "";
    try {
      disableAllFeatures();
      Image.resetImageOperationAccountingForTest();
      ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
          ImageOptimizationSettings.DISABLED);
      Image.clearImageOperationAccountingCountersForTest();
      exerciseNativeBacking();
      require(Image.imagePipelineCreatedCountForTest() == 0, "disabled Java accounting");
      require(Image.backingReadbackCountForTest() == 0, "disabled readback accounting");
      require(NativeImageBacking.backingRecordsCreatedForTest() == 0,
          "disabled native create accounting");
      require(NativeImageBacking.backingRecordsReleasedForTest() == 0,
          "disabled native release accounting");
      require(NativeImageBacking.backingRecordsLiveForTest() == 0,
          "disabled native live accounting");

      ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
          ImageOptimizationSettings.ENABLED);
      Image.clearImageOperationAccountingCountersForTest();
      Image.recordImagePipelineCreatedForTest();
      exerciseNativeBacking();
      require(Image.imagePipelineCreatedCountForTest() == 1, "enabled Java accounting");
      require(Image.backingReadbackCountForTest() == 1, "enabled readback accounting");
      require(NativeImageBacking.backingRecordsCreatedForTest() == 1,
          "enabled native create accounting");
      require(NativeImageBacking.backingRecordsReleasedForTest() == 1,
          "enabled native release accounting");
      overallPass = true;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    System.out.println("fixture=ImageDiagnosticAccountingGateSmokeApp,overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private static void disableAllFeatures() {
    ImageOptimizationSettings.resetForTest();
    for (int feature = 0; feature < ImageOptimizationSettings.FEATURE_COUNT; feature++) {
      ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.DISABLED);
    }
  }

  private static void exerciseNativeBacking() throws Exception {
    NativeImageBacking backing = NativeImageBacking.createEmpty(WIDTH, HEIGHT);
    try {
      backing.readVisiblePixels(WIDTH, HEIGHT, 0);
    } finally {
      backing.release();
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
