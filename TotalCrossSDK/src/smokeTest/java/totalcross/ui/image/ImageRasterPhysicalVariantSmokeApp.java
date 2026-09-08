// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Focused macOS smoke cases for the bounded physical raster variant slot. */
public class ImageRasterPhysicalVariantSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "post-enabled");
    String testCase = ImageRasterBenchmarkSupport.argument(getCommandLine(), "case", "repeat");
    int completedSamples = 0;
    String error = "";
    String hash = null;
    try {
      ImageRasterBenchmarkSupport.require("repeat".equals(testCase) || "identity".equals(testCase)
          || "replace".equals(testCase) || "mutation".equals(testCase) || "crop".equals(testCase)
          || "alpha".equals(testCase) || "hwscale".equals(testCase)
          || "rotation".equals(testCase) || "combined".equals(testCase)
          || "decode".equals(testCase) || "compact-combined".equals(testCase)
          || "compact-bgra-combined".equals(testCase), "invalid case");
      if ("compact-combined".equals(testCase)) {
        ImageRasterBenchmarkSupport.configureAllRasterFeatures(scenario);
        if ("post-enabled".equals(scenario)) {
          ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_RGB565,
              ImageOptimizationSettings.ENABLED);
        }
      } else if ("compact-bgra-combined".equals(testCase)) {
        ImageCompactFormatsBenchmarkSupport.configurePhase2WithStorage(scenario,
            ImageOptimizationSettings.STORAGE_RGB565);
      } else {
        ImageRasterBenchmarkSupport.configure(scenario,
            ImageOptimizationSettings.RASTER_PHYSICAL_VARIANT_CACHE);
      }
      if ("identity".equals(testCase) && "post-enabled".equals(scenario)) {
        ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING,
            ImageOptimizationSettings.ENABLED);
      }
      if ("combined".equals(testCase) && "post-enabled".equals(scenario)) {
        ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION,
            ImageOptimizationSettings.ENABLED);
      }
      Image source;
      if ("decode".equals(testCase)) {
        byte[] encoded = ImageRasterBenchmarkSupport.opaquePng(200, 200);
        source = new Image(encoded, encoded.length);
      } else if ("compact-combined".equals(testCase)
          || "compact-bgra-combined".equals(testCase)) {
        source = ImageCompactFormatsBenchmarkSupport.materialize(
            ImageCompactFormatsBenchmarkSupport.fixtures()[1].bytes);
      } else {
        source = source("identity".equals(testCase));
      }
      Image image;
      Image destination;
      if ("crop".equals(testCase)) {
        image = source.getClippedInstance(10, 10, 100, 100);
        destination = Image.createLogical(100, 100, 2);
      } else if ("rotation".equals(testCase)) {
        image = source.getRotatedScaledInstance(50, 90, 0);
        destination = Image.createLogical(200, 200, 2);
      } else if ("combined".equals(testCase)) {
        image = source.getSmoothScaledInstance(100, 100);
        destination = Image.createTestRaster(100, 100, 2,
            NativeImageBacking.TEST_COLOR_BGRA8888);
      } else if ("compact-combined".equals(testCase)) {
        image = source.getSmoothScaledInstance(100, 100);
        destination = Image.createTestRaster(100, 100, 2,
            NativeImageBacking.TEST_COLOR_RGB565);
      } else if ("compact-bgra-combined".equals(testCase)) {
        image = source.getSmoothScaledInstance(100, 100);
        destination = Image.createTestRaster(100, 100, 2,
            NativeImageBacking.TEST_COLOR_BGRA8888);
      } else {
        image = source.getSmoothScaledInstance(100, 100);
        destination = Image.createLogical(100, 100, 2);
      }
      Graphics canvas = destination.getGraphics();
      ImageRasterBenchmarkSupport.require(canvas != null, "destination graphics");
      Image.resetImageOperationAccountingForTest();
      long start = Vm.getTimeStamp();
      if ("replace".equals(testCase)) {
        Image alternate = image.getSmoothScaledInstance(96, 96);
        Graphics alternateCanvas = Image.createLogical(96, 96, 2).getGraphics();
        drawBatch(canvas, image, 2);
        drawBatch(canvas, image, 1);
        drawBatch(alternateCanvas, alternate, 2);
        drawBatch(alternateCanvas, alternate, 1);
      } else if ("mutation".equals(testCase)) {
        drawBatch(canvas, image, 2);
        Image.resetImageOperationAccountingForTest();
        image.mutateDeferredRootForTest(destination.getContentScale());
        drawBatch(canvas, image, 2);
      } else if ("decode".equals(testCase)) {
        drawBatch(canvas, image, 2);
        Image.resetImageOperationAccountingForTest();
        Object root = image.pipelineForSmoke().root();
        ImageRasterBenchmarkSupport.require(root instanceof EncodedImageSource,
            "encoded physical variant root");
        ((EncodedImageSource) root).evictDecodedBacking();
        drawBatch(canvas, image, 2);
      } else {
        if ("compact-bgra-combined".equals(testCase)) {
          drawBatch(canvas, image, 3);
        } else if ("alpha".equals(testCase)) {
          image.alphaMask = 127;
        } else if ("hwscale".equals(testCase)) {
          image.hwScaleW = 0.75;
        }
        drawBatch(canvas, image, 3);
      }
      long elapsed = Vm.getTimeStamp() - start;
      hash = ImageRasterBenchmarkSupport.hashString(
          ImageRasterBenchmarkSupport.fullPixelHash(destination));
      assertCounters(scenario, testCase);
      System.out.println("sample=1,elapsed_ms=" + elapsed + ",case=" + testCase
          + ",pixel_hash=" + hash + counters());
      completedSamples = 1;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterPhysicalVariantSmokeApp", scenario,
        1, completedSamples, "case=" + testCase + ",pixel_hash=" + String.valueOf(hash)
            + counters(), error);
    exit(pass ? 0 : 1);
  }

  private static Image source(boolean identity) throws Exception {
    int size = identity ? 100 : 200;
    Image image = Image.createLogical(size, size, 2);
    Graphics graphics = image.getGraphics();
    for (int y = 0; y < size; y += 16) {
      for (int x = 0; x < size; x += 16) {
        graphics.foreColor = 0xFF000000 | ((x * 11) & 0xFF) << 16
            | ((y * 13) & 0xFF) << 8 | ((x + y * 3) & 0xFF);
        graphics.fillRect(x, y, Math.min(16, size - x), Math.min(16, size - y));
      }
    }
    return image;
  }

  private static void drawBatch(Graphics canvas, Image image, int draws) {
    for (int i = 0; i < draws; i++) {
      canvas.drawImage(image, 0, 0, false);
    }
  }

  private static String counters() {
    return ",physical_variant_lookups=" + NativeImageBacking.physicalVariantLookupsForTest()
        + ",physical_variant_hits=" + NativeImageBacking.physicalVariantHitsForTest()
        + ",physical_variant_misses=" + NativeImageBacking.physicalVariantMissesForTest()
        + ",physical_variant_materializations="
        + NativeImageBacking.physicalVariantMaterializationsForTest()
        + ",physical_variant_evictions=" + NativeImageBacking.physicalVariantEvictionsForTest()
        + ",physical_variant_bytes=" + NativeImageBacking.physicalVariantBytesForTest();
  }

  private static void assertCounters(String scenario, String testCase) {
    long lookups = NativeImageBacking.physicalVariantLookupsForTest();
    long hits = NativeImageBacking.physicalVariantHitsForTest();
    long misses = NativeImageBacking.physicalVariantMissesForTest();
    long materializations = NativeImageBacking.physicalVariantMaterializationsForTest();
    long evictions = NativeImageBacking.physicalVariantEvictionsForTest();
    long bytes = NativeImageBacking.physicalVariantBytesForTest();
    if (!"post-enabled".equals(scenario)) {
      ImageRasterBenchmarkSupport.require(lookups == 0 && hits == 0 && misses == 0
          && materializations == 0 && evictions == 0 && bytes == 0,
          "disabled physical variant counters" + counters());
    } else if ("identity".equals(testCase) || "alpha".equals(testCase)
        || "hwscale".equals(testCase) || "rotation".equals(testCase)) {
      ImageRasterBenchmarkSupport.require(lookups == 0 && materializations == 0,
          "ineligible physical variant counters" + counters());
    } else if ("replace".equals(testCase)) {
      ImageRasterBenchmarkSupport.require(lookups == 6 && hits == 2 && misses == 4
          && materializations == 2 && evictions == 1 && bytes == 307456,
          "replaced physical variant counters" + counters());
    } else if ("mutation".equals(testCase)) {
      ImageRasterBenchmarkSupport.require(lookups == 2 && hits == 0 && misses == 2
          && materializations == 1 && evictions == 0 && bytes == 160000,
          "mutated physical variant counters" + counters());
    } else if ("decode".equals(testCase)) {
      ImageRasterBenchmarkSupport.require(lookups == 2 && hits == 0 && misses == 2
          && materializations == 1 && evictions == 0 && bytes == 160000,
          "decode-generation physical variant counters" + counters());
    } else if ("compact-combined".equals(testCase)) {
      ImageRasterBenchmarkSupport.require(lookups == 3 && hits == 1 && misses == 2
          && materializations == 1 && evictions == 0 && bytes == 80000,
          "compact combined physical variant counters" + counters());
      ImageRasterBenchmarkSupport.require(NativeImageBacking.targetColorAttemptsForTest() == 0
          && NativeImageBacking.targetColorMaterializationsForTest() == 0
          && NativeImageBacking.targetColorHitsForTest() == 0
          && NativeImageBacking.targetColorConvertedBytesForTest() == 0,
          "compact combined target color counters");
    } else if ("compact-bgra-combined".equals(testCase)) {
      ImageRasterBenchmarkSupport.require(lookups == 3 && hits == 1 && misses == 2
          && materializations == 1 && evictions == 0 && bytes == 160000,
          "compact BGRA combined physical variant counters" + counters());
      ImageRasterBenchmarkSupport.require(NativeImageBacking.targetColorAttemptsForTest() == 0
          && NativeImageBacking.targetColorMaterializationsForTest() == 0
          && NativeImageBacking.targetColorHitsForTest() == 0
          && NativeImageBacking.targetColorConvertedBytesForTest() == 0,
          "compact BGRA combined target color counters");
    } else if ("combined".equals(testCase)) {
      ImageRasterBenchmarkSupport.require(lookups == 3 && hits == 1 && misses == 2
          && materializations == 1 && evictions == 0 && bytes == 160000,
          "combined physical variant counters" + counters());
      ImageRasterBenchmarkSupport.require(NativeImageBacking.targetColorAttemptsForTest() == 3
          && NativeImageBacking.targetColorMaterializationsForTest() == 0
          && NativeImageBacking.targetColorHitsForTest() == 0
          && NativeImageBacking.targetColorFallbacksForTest() == 3
          && NativeImageBacking.targetColorConvertedBytesForTest() == 0,
          "combined target color counters");
    } else {
      ImageRasterBenchmarkSupport.require(lookups == 3 && hits == 1 && misses == 2
          && materializations == 1 && evictions == 0 && bytes == 160000,
          "physical variant counters" + counters());
    }
  }
}
