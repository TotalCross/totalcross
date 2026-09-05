// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** macOS benchmark workload for complete diagnostic accounting gating. */
public class ImageDiagnosticAccountingGateBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int CACHED_DRAWS = 256;
  private static final int CHURN_OPERATIONS = 32;
  private static final int TARGET_WIDTH = 320;
  private static final int TARGET_HEIGHT = 240;
  private static final int PROBE_WIDTH = 16;
  private static final int PROBE_HEIGHT = 16;

  @Override
  public void initUI() {
    String scenario = argument(getCommandLine(), "scenario", "pre");
    int samples = integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    boolean overallPass = false;
    String error = "";
    int completedSamples = 0;

    try {
      require("pre".equals(scenario) || "post-disabled".equals(scenario)
          || "post-enabled".equals(scenario), "invalid scenario");
      require(samples > 0 && samples <= 200, "samples must be between 1 and 200");
      configureScenario();
      boolean diagnosticEnabled = "post-enabled".equals(scenario);
      byte[] fixture = encodedFixture();
      Image.resetImageOperationAccountingForTest();
      setDiagnosticState(diagnosticEnabled);
      Image root = new Image(fixture);
      Image cached = root.getSmoothScaledInstance(160, 120);
      Image target = new Image(TARGET_WIDTH, TARGET_HEIGHT);
      Graphics surface = target.getGraphics();
      require(surface != null, "benchmark target graphics");

      for (int warmup = 0; warmup < 3; warmup++) {
        runBatch(root, cached, surface, warmup);
      }

      Image.resetImageOperationAccountingForTest();
      setDiagnosticState(diagnosticEnabled);
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        runBatch(root, cached, surface, sample);
        long elapsed = Vm.getTimeStamp() - start;
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed
            + ",cached_draws=" + CACHED_DRAWS + ",churn_operations=" + CHURN_OPERATIONS
            + ",image_created=" + Image.imageCreatedCountForTest()
            + ",image_finalized=" + Image.imageFinalizedCountForTest()
            + ",pipeline_created=" + Image.imagePipelineCreatedCountForTest()
            + ",draw_plan_created=" + Image.imageDrawPlanCreatedCountForTest()
            + ",draw_plan_cache_hits=" + Image.imageDrawPlanCacheHitCountForTest()
            + ",draw_plan_capabilities=" + Image.imageDrawPlanCapabilitiesAllocatedCountForTest()
            + ",presentation_plan_recreations=" + Image.presentationOnlyPlanRecreationCountForTest()
            + ",full_decode=" + Image.fullDecodeInvocationCountForTest()
            + ",targeted_decode=" + Image.targetedDecodeInvocationCountForTest()
            + ",native_geometry=" + Image.nativeGeometryMaterializationCountForTest()
            + ",native_color_readback=" + Image.nativeColorReadbackCountForTest()
            + ",direct_draw=" + Image.directDrawPlanExecutionCountForTest()
            + ",backing_readback=" + Image.backingReadbackCountForTest()
            + ",native_backing_created=" + NativeImageBacking.backingRecordsCreatedForTest()
            + ",native_backing_released=" + NativeImageBacking.backingRecordsReleasedForTest()
            + ",native_backing_live=" + NativeImageBacking.backingRecordsLiveForTest()
            + ",native_backing_peak_live=" + NativeImageBacking.backingRecordsPeakLiveForTest()
            + ",native_backing_bytes_live=" + NativeImageBacking.backingBytesLiveForTest()
            + ",native_backing_peak_bytes=" + NativeImageBacking.backingBytesPeakLiveForTest());
        System.out.flush();
        completedSamples = sample;
      }
      overallPass = completedSamples == samples;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    System.out.println("fixture=ImageDiagnosticAccountingGateBenchmarkApp,scenario=" + scenario
        + ",samples=" + samples + ",completed_samples=" + completedSamples
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overallPass ? 0 : 1);
  }

  private static void configureScenario() {
    ImageOptimizationSettings.resetForTest();
    for (int feature = 0; feature < ImageOptimizationSettings.FEATURE_COUNT; feature++) {
      ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.DISABLED);
    }
  }

  private static void setDiagnosticState(boolean enabled) {
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        enabled ? ImageOptimizationSettings.ENABLED : ImageOptimizationSettings.DISABLED);
  }

  private static void runBatch(Image root, Image cached, Graphics surface, int sample) throws Exception {
    for (int draw = 0; draw < CACHED_DRAWS; draw++) {
      surface.drawImage(cached, (draw & 3) * 8, ((draw >>> 2) & 3) * 8, true);
    }
    for (int operation = 0; operation < CHURN_OPERATIONS; operation++) {
      int scale = 90 + ((sample * 17 + operation * 11) % 71);
      int angle = -180 + ((sample * 23 + operation * 37) % 361);
      Image leaf = root.getRotatedScaledInstance(scale, angle, 0xFF102030)
          .getTouchedUpInstance((byte) ((operation & 15) - 8), (byte) ((sample & 15) - 8));
      surface.drawImage(leaf, (operation & 7) * 12, ((operation >>> 3) & 7) * 12, true);
    }
    NativeImageBacking probe = NativeImageBacking.createEmpty(PROBE_WIDTH, PROBE_HEIGHT);
    try {
      probe.readVisiblePixels(PROBE_WIDTH, PROBE_HEIGHT, 0);
    } finally {
      probe.release();
    }
  }

  private static byte[] encodedFixture() throws Exception {
    Image fixture = new Image(96, 72);
    Graphics graphics = fixture.getGraphics();
    for (int y = 0; y < 72; y += 8) {
      for (int x = 0; x < 96; x += 8) {
        graphics.backColor = 0x00102030 + ((x * 13 + y * 7) & 0x00FFFFFF);
        graphics.fillRect(x, y, 8, 8);
      }
    }
    ByteArrayStream stream = new ByteArrayStream(8192);
    fixture.createPng(stream);
    byte[] encoded = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, encoded, 0, encoded.length);
    return encoded;
  }

  private static String argument(String commandLine, String name, String fallback) {
    String prefix = "--" + name + "=";
    if (commandLine != null) {
      String[] parts = commandLine.trim().split("\\s+");
      for (String part : parts) {
        if (part.startsWith(prefix)) {
          return part.substring(prefix.length());
        }
      }
    }
    return fallback;
  }

  private static int integerArgument(String commandLine, String name, int fallback) {
    try {
      return Integer.parseInt(argument(commandLine, name, String.valueOf(fallback)));
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
