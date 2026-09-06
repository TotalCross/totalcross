// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Isolated and combined Phase-3 compact-format benchmark workload. */
public class ImageCompactFormatsBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int SOURCE_DRAWS = 32;
  private static final int PROMOTION_DRAWS = 8;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "post-disabled");
    String workload = ImageRasterBenchmarkSupport.argument(getCommandLine(), "workload", "combined-disabled");
    boolean phase2Stack = "true".equals(ImageRasterBenchmarkSupport.argument(
        getCommandLine(), "phase2", "false"));
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures = null;
    String inputHashes = "unavailable";
    try {
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200,
          "samples must be between 1 and 200");
      ImageRasterBenchmarkSupport.require(isWorkload(workload), "unsupported workload " + workload);
      fixtures = selectedFixtures(workload);
      inputHashes = inputHashes(fixtures);
      int[][] references = references(fixtures);
      ImageCompactFormatsBenchmarkSupport.configure(scenario, workload, phase2Stack);
      for (int warmup = 0; warmup < 3; warmup++) {
        runWorkload(fixtures, workload);
      }
      Image.resetImageOperationAccountingForTest();
      String expectedHash = null;
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        WorkloadResult result = runWorkload(fixtures, workload);
        long elapsed = ImageCompactFormatsBenchmarkSupport.elapsedMillis(start);
        String outputHash = hashResult(result);
        if (expectedHash == null) {
          expectedHash = outputHash;
        } else {
          ImageRasterBenchmarkSupport.require(expectedHash.equals(outputHash),
              "output hash drift");
        }
        QualitySummary quality = quality(result, references, workload);
        requireExpectedFormats(result, scenario, workload);
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed
            + ",workload=" + workload + ",phase2_stack=" + phase2Stack
            + ",input_hashes=" + inputHashes + ",output_hash=" + outputHash
            + ",source_formats=" + join(result.formats)
            + ",source_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("backingBytesLiveForTest")
            + ",rgba8888_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("rgba8888BackingBytesForTest")
            + ",rgb565_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("rgb565BackingBytesForTest")
            + ",gray8_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("gray8BackingBytesForTest")
            + ",argb4444_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("argb4444BackingBytesForTest")
            + ",compact_decodes=" + ImageCompactFormatsBenchmarkSupport.metric("compactDirectDecodeCountForTest")
            + ",compact_decode_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("compactDirectDecodeBytesForTest")
            + ",temporary_rgba_decode_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("temporaryRgbaDecodeBytesForTest")
            + ",compact_readbacks=" + ImageCompactFormatsBenchmarkSupport.metric("compactReadbackCountForTest")
            + ",row_scratch_peak_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("compactRowScratchPeakBytesForTest")
            + ",promotion_attempts=" + ImageCompactFormatsBenchmarkSupport.metric("promotionAttemptsForTest")
            + ",promotion_successes=" + ImageCompactFormatsBenchmarkSupport.metric("promotionSuccessesForTest")
            + ",promotion_failures=" + ImageCompactFormatsBenchmarkSupport.metric("promotionFailuresForTest")
            + ",promotion_bytes=" + ImageCompactFormatsBenchmarkSupport.metric("promotionBytesForTest")
            + ",rgba_quality_max=" + quality.rgbaMax
            + ",rgba_quality_rmse=" + quality.rgbaRmse
            + ",model_quality_max=" + quality.modelMax
            + ",model_quality_rmse=" + quality.modelRmse);
        System.out.flush();
        completedSamples = sample;
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = ImageRasterBenchmarkSupport.finish("ImageCompactFormatsBenchmarkApp", scenario,
        samples, completedSamples,
        "workload=" + workload + ",phase2_stack=" + phase2Stack + ",input_hashes=" + inputHashes
            + ",format_probe=" + ImageCompactFormatsBenchmarkSupport.formatProbeAvailable()
            + ",promotion_attempts=" + ImageCompactFormatsBenchmarkSupport.metric("promotionAttemptsForTest")
            + ",compact_decodes=" + ImageCompactFormatsBenchmarkSupport.metric("compactDirectDecodeCountForTest"),
        error);
    exit(pass ? 0 : 1);
  }

  private static boolean isWorkload(String workload) {
    return "rgb565".equals(workload) || "gray8".equals(workload)
        || "argb4444".equals(workload) || "combined-disabled".equals(workload)
        || "combined-enabled".equals(workload) || "promotion".equals(workload)
        || "writepixels".equals(workload);
  }

  private static ImageCompactFormatsBenchmarkSupport.Fixture[] selectedFixtures(String workload) {
    ImageCompactFormatsBenchmarkSupport.Fixture[] all = ImageCompactFormatsBenchmarkSupport.fixtures();
    if ("rgb565".equals(workload) || "writepixels".equals(workload)) {
      return new ImageCompactFormatsBenchmarkSupport.Fixture[] { all[0], all[1] };
    }
    if ("gray8".equals(workload)) {
      return new ImageCompactFormatsBenchmarkSupport.Fixture[] { all[2], all[3] };
    }
    if ("argb4444".equals(workload)) {
      return new ImageCompactFormatsBenchmarkSupport.Fixture[] { all[4] };
    }
    if ("promotion".equals(workload)) {
      return new ImageCompactFormatsBenchmarkSupport.Fixture[] { all[0], all[2], all[4] };
    }
    return all;
  }

  private static int[][] references(ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures)
      throws Exception {
    int[][] references = new int[fixtures.length][];
    for (int i = 0; i < fixtures.length; i++) {
      references[i] = ImageCompactFormatsBenchmarkSupport.decodeReference(fixtures[i].bytes);
    }
    return references;
  }

  private static WorkloadResult runWorkload(
      ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures, String workload) throws Exception {
    int[][] pixels = new int[fixtures.length][];
    String[] formats = new String[fixtures.length];
    for (int i = 0; i < fixtures.length; i++) {
      Image image = ImageCompactFormatsBenchmarkSupport.materialize(fixtures[i].bytes);
      formats[i] = ImageCompactFormatsBenchmarkSupport.format(image);
      pixels[i] = image.getPixels();
      if ("promotion".equals(workload)) {
        if (image.getGraphics() == null) {
          throw new IllegalStateException("promotion graphics unavailable");
        }
        for (int draw = 0; draw < PROMOTION_DRAWS; draw++) {
          image.getGraphics().fillRect(0, 0, 1, 1);
        }
        image.getGraphics().fillRect(0, 0, 1, 1);
      } else {
        ImageCompactFormatsBenchmarkSupport.drawToRgbaTarget(image,
            "writepixels".equals(workload) ? SOURCE_DRAWS * 4 : SOURCE_DRAWS);
      }
      if ("combined-disabled".equals(workload) || "combined-enabled".equals(workload)) {
        // Encoding is an observer and must not promote the compact source.
        ImageCompactFormatsBenchmarkSupport.encodePng(image);
      }
    }
    return new WorkloadResult(pixels, formats);
  }

  private static void requireExpectedFormats(WorkloadResult result, String scenario, String workload) {
    if (!"post-enabled".equals(scenario)
        || !ImageCompactFormatsBenchmarkSupport.formatProbeAvailable()) {
      return;
    }
    for (int i = 0; i < result.formats.length; i++) {
      String expected = expectedFormat(result.formats[i], workload, i);
      if (expected != null && !expected.equals(result.formats[i])) {
        throw new IllegalStateException("format precedence expected=" + expected
            + " actual=" + result.formats[i] + " workload=" + workload);
      }
    }
  }

  private static String expectedFormat(String actual, String workload, int index) {
    if ("rgb565".equals(workload) || "writepixels".equals(workload)) {
      return ImageCompactFormatsBenchmarkSupport.RGB565;
    }
    if ("gray8".equals(workload)) {
      return ImageCompactFormatsBenchmarkSupport.GRAY8;
    }
    if ("argb4444".equals(workload)) {
      return ImageCompactFormatsBenchmarkSupport.ARGB4444;
    }
    if ("combined-enabled".equals(workload)) {
      if (index <= 1) {
        return index == 0 ? ImageCompactFormatsBenchmarkSupport.RGB565
            : ImageCompactFormatsBenchmarkSupport.RGB565;
      }
      if (index <= 3) {
        return ImageCompactFormatsBenchmarkSupport.GRAY8;
      }
      return ImageCompactFormatsBenchmarkSupport.ARGB4444;
    }
    return null;
  }

  private static QualitySummary quality(WorkloadResult result, int[][] references, String workload) {
    int rgbaMax = 0;
    double rgbaRmse = 0;
    int modelMax = 0;
    double modelRmse = 0;
    for (int i = 0; i < result.pixels.length; i++) {
      ImageCompactFormatsBenchmarkSupport.Quality rgba =
          ImageCompactFormatsBenchmarkSupport.quality(result.pixels[i], references[i]);
      int[] model = references[i];
      if (result.formats[i].equals(ImageCompactFormatsBenchmarkSupport.RGB565)) {
        model = ImageCompactFormatsBenchmarkSupport.rgb565Reference(model);
      } else if (result.formats[i].equals(ImageCompactFormatsBenchmarkSupport.GRAY8)) {
        model = ImageCompactFormatsBenchmarkSupport.gray8Reference(model);
      } else if (result.formats[i].equals(ImageCompactFormatsBenchmarkSupport.ARGB4444)) {
        model = ImageCompactFormatsBenchmarkSupport.argb4444Reference(model);
      }
      ImageCompactFormatsBenchmarkSupport.Quality modelQuality =
          ImageCompactFormatsBenchmarkSupport.quality(result.pixels[i], model);
      rgbaMax = Math.max(rgbaMax, rgba.maxError);
      rgbaRmse = Math.max(rgbaRmse, rgba.rmse);
      modelMax = Math.max(modelMax, modelQuality.maxError);
      modelRmse = Math.max(modelRmse, modelQuality.rmse);
    }
    return new QualitySummary(rgbaMax, rgbaRmse, modelMax, modelRmse);
  }

  private static String inputHashes(ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures) {
    StringBuilder result = new StringBuilder();
    for (ImageCompactFormatsBenchmarkSupport.Fixture fixture : fixtures) {
      if (result.length() > 0) {
        result.append('|');
      }
      result.append(fixture.name).append(':').append(fixture.inputHash);
    }
    return result.toString();
  }

  private static String hashResult(WorkloadResult result) {
    long hash = 0xcbf29ce484222325L;
    for (int[] pixels : result.pixels) {
      hash ^= ImageCompactFormatsBenchmarkSupport.quality(pixels, pixels).maxError;
      hash *= 0x100000001b3L;
      hash ^= ImageRasterBenchmarkSupport.fullPixelHash(pixels);
      hash *= 0x100000001b3L;
    }
    return ImageRasterBenchmarkSupport.hashString(hash);
  }

  private static String join(String[] values) {
    StringBuilder result = new StringBuilder();
    for (String value : values) {
      if (result.length() > 0) {
        result.append('|');
      }
      result.append(value);
    }
    return result.toString();
  }

  private static final class WorkloadResult {
    final int[][] pixels;
    final String[] formats;

    WorkloadResult(int[][] pixels, String[] formats) {
      this.pixels = pixels;
      this.formats = formats;
    }
  }

  private static final class QualitySummary {
    final int rgbaMax;
    final double rgbaRmse;
    final int modelMax;
    final double modelRmse;

    QualitySummary(int rgbaMax, double rgbaRmse, int modelMax, double modelRmse) {
      this.rgbaMax = rgbaMax;
      this.rgbaRmse = rgbaRmse;
      this.modelMax = modelMax;
      this.modelRmse = modelRmse;
    }
  }
}
