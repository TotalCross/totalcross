// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** macOS workload for target-color conversion controls. */
public class ImageRasterTargetColorBenchmarkApp extends MainWindow {
  private static final int DEFAULT_SAMPLES = 60;
  private static final int DRAWS_PER_SAMPLE = 1024;

  @Override
  public void initUI() {
    String scenario = ImageRasterBenchmarkSupport.argument(getCommandLine(), "scenario", "pre");
    String target = ImageRasterBenchmarkSupport.argument(getCommandLine(), "target", "rgba");
    int samples = ImageRasterBenchmarkSupport.integerArgument(getCommandLine(), "samples", DEFAULT_SAMPLES);
    int completedSamples = 0;
    String error = "";
    String expectedHash = null;
    try {
      ImageRasterBenchmarkSupport.require("rgba".equals(target) || "bgra".equals(target)
          || "rgb565".equals(target) || "translucent".equals(target)
          || "mutation".equals(target) || "replace".equals(target), "invalid target");
      ImageRasterBenchmarkSupport.require(samples > 0 && samples <= 200, "invalid samples");
      ImageRasterBenchmarkSupport.require((!"mutation".equals(target) && !"replace".equals(target))
          || samples == 1, "single-sample target requires one sample");
      ImageRasterBenchmarkSupport.configure(scenario,
          ImageOptimizationSettings.RASTER_TARGET_COLORTYPE_CONVERSION);
      Image source = Image.createLogical(100, 100, 2);
      fill(source);
      Image transformed = source.getSmoothScaledInstance(100, 100);
      if ("translucent".equals(target)) {
        transformed.alphaMask = 127;
      }
      if ("replace".equals(target)) {
        Image.resetImageOperationAccountingForTest();
        Image bgraDestination = Image.createTestRaster(100, 100, 2,
            NativeImageBacking.TEST_COLOR_BGRA8888);
        drawBatch(requireGraphics(bgraDestination), transformed, 2);
        String bgraHash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(bgraDestination));
        Image rgb565Destination = Image.createTestRaster(100, 100, 2,
            NativeImageBacking.TEST_COLOR_RGB565);
        drawBatch(requireGraphics(rgb565Destination), transformed, 2);
        String rgb565Hash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(rgb565Destination));
        ImageRasterBenchmarkSupport.require("post-enabled".equals(scenario)
            ? NativeImageBacking.targetColorAttemptsForTest() == 4
                && NativeImageBacking.targetColorMaterializationsForTest() == 2
                && NativeImageBacking.targetColorHitsForTest() == 0
                && NativeImageBacking.targetColorFallbacksForTest() == 2
                && NativeImageBacking.targetColorConvertedBytesForTest() == 240000
            : NativeImageBacking.targetColorAttemptsForTest() == 0,
            "replacement target color counters");
        expectedHash = bgraHash + "/" + rgb565Hash;
        completedSamples = 1;
      } else {
        Image destination = Image.createTestRaster(100, 100, 2, colorType(target));
        Graphics canvas = requireGraphics(destination);
        int[] canonicalSourcePixels = source.getPixels();
        if ("mutation".equals(target)) {
          drawBatch(canvas, transformed, 2);
          Image.resetImageOperationAccountingForTest();
          transformed.mutateDeferredRootForTest(destination.getContentScale());
          long start = Vm.getTimeStamp();
          drawBatch(canvas, transformed, 2);
          long elapsed = Vm.getTimeStamp() - start;
          String hash = ImageRasterBenchmarkSupport.hashString(
              ImageRasterBenchmarkSupport.fullPixelHash(destination));
          ImageRasterBenchmarkSupport.require("post-enabled".equals(scenario)
              ? NativeImageBacking.targetColorAttemptsForTest() == 2
                  && NativeImageBacking.targetColorMaterializationsForTest() == 1
                  && NativeImageBacking.targetColorHitsForTest() == 0
                  && NativeImageBacking.targetColorFallbacksForTest() == 1
                  && NativeImageBacking.targetColorConvertedBytesForTest() == 160000
              : NativeImageBacking.targetColorAttemptsForTest() == 0,
              "mutation target color counters");
          ImageRasterBenchmarkSupport.require(same(canonicalSourcePixels, source.getPixels()),
              "mutation canonical source readback");
          expectedHash = hash;
          System.out.println("sample=1,elapsed_ms=" + elapsed + ",target=mutation,draws=2,pixel_hash=" + hash
              + ",target_color_attempts=" + NativeImageBacking.targetColorAttemptsForTest()
              + ",target_color_materializations=" + NativeImageBacking.targetColorMaterializationsForTest()
              + ",target_color_hits=" + NativeImageBacking.targetColorHitsForTest()
              + ",target_color_fallbacks=" + NativeImageBacking.targetColorFallbacksForTest()
              + ",target_color_converted_bytes=" + NativeImageBacking.targetColorConvertedBytesForTest());
          completedSamples = 1;
        } else {
      Image.resetImageOperationAccountingForTest();
      for (int warmup = 0; warmup < 3; warmup++) {
        drawBatch(canvas, transformed);
      }
      for (int sample = 1; sample <= samples; sample++) {
        long start = Vm.getTimeStamp();
        drawBatch(canvas, transformed);
        long elapsed = Vm.getTimeStamp() - start;
        String hash = ImageRasterBenchmarkSupport.hashString(
            ImageRasterBenchmarkSupport.fullPixelHash(destination));
        if (expectedHash == null) {
          expectedHash = hash;
        } else {
          ImageRasterBenchmarkSupport.require(expectedHash.equals(hash), "target color hash drift");
        }
        long expectedDraws = (long) (sample + 3) * DRAWS_PER_SAMPLE;
        assertCounters(scenario, target, expectedDraws);
        System.out.println("sample=" + sample + ",elapsed_ms=" + elapsed + ",target=" + target
            + ",draws=" + DRAWS_PER_SAMPLE + ",pixel_hash=" + hash
            + ",target_color_attempts=" + NativeImageBacking.targetColorAttemptsForTest()
            + ",target_color_materializations=" + NativeImageBacking.targetColorMaterializationsForTest()
            + ",target_color_hits=" + NativeImageBacking.targetColorHitsForTest()
            + ",target_color_fallbacks=" + NativeImageBacking.targetColorFallbacksForTest()
            + ",target_color_converted_bytes=" + NativeImageBacking.targetColorConvertedBytesForTest());
        System.out.flush();
        completedSamples = sample;
      }
        ImageRasterBenchmarkSupport.require(same(canonicalSourcePixels, source.getPixels()),
            "target canonical source readback");
      }
      }
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    long reportedDraws = "mutation".equals(target) ? 2 : "replace".equals(target) ? 4 : DRAWS_PER_SAMPLE;
    boolean pass = ImageRasterBenchmarkSupport.finish("ImageRasterTargetColorBenchmarkApp", scenario,
        samples, completedSamples, "target=" + target + ",draws=" + reportedDraws
            + ",pixel_hash=" + String.valueOf(expectedHash)
            + ",target_color_attempts=" + NativeImageBacking.targetColorAttemptsForTest()
            + ",target_color_materializations=" + NativeImageBacking.targetColorMaterializationsForTest()
            + ",target_color_hits=" + NativeImageBacking.targetColorHitsForTest()
            + ",target_color_fallbacks=" + NativeImageBacking.targetColorFallbacksForTest()
            + ",target_color_converted_bytes=" + NativeImageBacking.targetColorConvertedBytesForTest(), error);
    exit(pass ? 0 : 1);
  }

  private static void fill(Image image) throws Exception {
    Graphics graphics = image.getGraphics();
    for (int y = 0; y < 100; y += 10) {
      for (int x = 0; x < 100; x += 10) {
        graphics.foreColor = 0xFF000000 | ((x * 23) & 0xFF) << 16
            | ((y * 29) & 0xFF) << 8 | ((x * 7 + y) & 0xFF);
        graphics.fillRect(x, y, Math.min(10, 100 - x), Math.min(10, 100 - y));
      }
    }
  }

  private static void drawBatch(Graphics canvas, Image image) {
    drawBatch(canvas, image, DRAWS_PER_SAMPLE);
  }

  private static void drawBatch(Graphics canvas, Image image, int draws) {
    for (int i = 0; i < draws; i++) {
      canvas.drawImage(image, 0, 0, false);
    }
  }

  private static Graphics requireGraphics(Image image) {
    Graphics graphics = image.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "target graphics");
    return graphics;
  }

  private static boolean same(int[] first, int[] second) {
    if (first == null || second == null || first.length != second.length) {
      return false;
    }
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) {
        return false;
      }
    }
    return true;
  }

  private static void assertCounters(String scenario, String target, long expectedDraws) {
    long attempts = NativeImageBacking.targetColorAttemptsForTest();
    long materializations = NativeImageBacking.targetColorMaterializationsForTest();
    long hits = NativeImageBacking.targetColorHitsForTest();
    long fallbacks = NativeImageBacking.targetColorFallbacksForTest();
    long convertedBytes = NativeImageBacking.targetColorConvertedBytesForTest();
    if (!"post-enabled".equals(scenario) || "rgba".equals(target)) {
      ImageRasterBenchmarkSupport.require(attempts == 0 && materializations == 0 && hits == 0
          && fallbacks == 0 && convertedBytes == 0, "disabled or control target color counters");
      return;
    }
    if ("translucent".equals(target)) {
      ImageRasterBenchmarkSupport.require(attempts == expectedDraws && materializations == 0
          && hits == 0 && fallbacks == expectedDraws && convertedBytes == 0,
          "translucent target color counters");
    } else {
      ImageRasterBenchmarkSupport.require(attempts == expectedDraws && materializations == 1
          && hits == expectedDraws - 2 && fallbacks == 1
          && convertedBytes == ("rgb565".equals(target) ? 80000 : 160000),
          "target color counters");
    }
  }

  private static int colorType(String target) {
    if ("bgra".equals(target) || "translucent".equals(target) || "replace".equals(target)
        || "mutation".equals(target)) {
      return NativeImageBacking.TEST_COLOR_BGRA8888;
    }
    if ("rgb565".equals(target)) {
      return NativeImageBacking.TEST_COLOR_RGB565;
    }
    return NativeImageBacking.TEST_COLOR_RGBA8888;
  }
}
