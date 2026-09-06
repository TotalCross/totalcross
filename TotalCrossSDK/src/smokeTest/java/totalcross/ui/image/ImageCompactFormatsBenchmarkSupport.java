// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.ui.gfx.Graphics;

/** Shared Phase-3 fixtures, configuration, independent quality oracles, and shims. */
final class ImageCompactFormatsBenchmarkSupport {
  static final String RGB565 = "RGB565";
  static final String GRAY8 = "GRAY8";
  static final String ARGB4444 = "ARGB4444";
  static final String RGBA8888 = "RGBA8888";
  private ImageCompactFormatsBenchmarkSupport() {
  }

  static final class Fixture {
    final String name;
    final byte[] bytes;
    final String inputHash;

    Fixture(String name, byte[] bytes) {
      this.name = name;
      this.bytes = bytes;
      this.inputHash = ImageRasterBenchmarkSupport.hashString(
          ImageRasterBenchmarkSupport.fullByteHash(bytes, bytes.length));
    }
  }

  static Fixture[] fixtures() {
    return new Fixture[] {
        fixture("rgb565-jpeg", "image-opt-phase3/rgb565-source.jpg"),
        fixture("rgb565-png", "image-opt-phase3/rgb565-source.png"),
        fixture("gray8-jpeg", "image-opt-phase3/gray8-source.jpg"),
        fixture("gray8-png", "image-opt-phase3/gray8-source.png"),
        fixture("argb4444-png", "image-opt-phase3/argb4444-source.png")
    };
  }

  private static Fixture fixture(String name, String path) {
    return new Fixture(name, ImageRasterBenchmarkSupport.resource(path));
  }

  static void configure(String scenario, String workload, boolean phase2Stack) {
    ImageRasterBenchmarkSupport.require("pre".equals(scenario)
        || "post-disabled".equals(scenario) || "post-enabled".equals(scenario),
        "invalid scenario");
    ImageOptimizationSettings.resetForTest();
    if ("pre".equals(scenario)) {
      if (phase2Stack && workload.contains("combined-enabled")) {
        for (int feature = ImageOptimizationSettings.DECODE_ZERO_COPY;
            feature <= ImageOptimizationSettings.RASTER_DIRECT_COLOR_MATERIALIZATION; feature++) {
          ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.ENABLED);
        }
      }
      return;
    }
    for (int feature = ImageOptimizationSettings.DECODE_ZERO_COPY;
        feature <= ImageOptimizationSettings.STORAGE_ARGB4444; feature++) {
      ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.DISABLED);
    }
    if (phase2Stack) {
      for (int feature = ImageOptimizationSettings.DECODE_ZERO_COPY;
          feature <= ImageOptimizationSettings.RASTER_DIRECT_COLOR_MATERIALIZATION; feature++) {
        ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.ENABLED);
      }
    }
    if (!"post-enabled".equals(scenario)) {
      return;
    }
    if (workload.contains("rgb565") || workload.contains("writepixels")
        || workload.contains("promotion")) {
      ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_RGB565,
          ImageOptimizationSettings.ENABLED);
    }
    if (workload.contains("gray8") || workload.contains("promotion")) {
      ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_GRAY8,
          ImageOptimizationSettings.ENABLED);
    }
    if (workload.contains("argb4444") || workload.contains("promotion")) {
      ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_ARGB4444,
          ImageOptimizationSettings.ENABLED);
    }
    if (workload.contains("combined")) {
      ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_RGB565,
          ImageOptimizationSettings.ENABLED);
      ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_GRAY8,
          ImageOptimizationSettings.ENABLED);
      ImageOptimizationSettings.setState(ImageOptimizationSettings.STORAGE_ARGB4444,
          ImageOptimizationSettings.ENABLED);
    }
  }

  /** Materializes an encoded source without calling getGraphics on the source. */
  static Image materialize(byte[] encoded) throws Exception {
    Image image = new Image(encoded, encoded.length);
    return ImageCompactFormatsNativeHooks.materialize(image);
  }

  static int[] decodeReference(byte[] encoded) throws Exception {
    ImageOptimizationSettings.resetForTest();
    return materialize(encoded).getPixels();
  }

  static void drawToRgbaTarget(Image source, int draws) throws Exception {
    Image target = new Image(source.getPixelWidth(), source.getPixelHeight());
    Graphics graphics = target.getGraphics();
    ImageRasterBenchmarkSupport.require(graphics != null, "RGBA target graphics");
    for (int i = 0; i < draws; i++) {
      graphics.drawImage(source, 0, 0, false);
    }
    target.getPixels();
  }

  static byte[] encodePng(Image image) throws Exception {
    ByteArrayStream stream = new ByteArrayStream(image.getPixelWidth() * 4);
    image.createPng(stream);
    return ImageRasterBenchmarkSupport.copy(stream.getBuffer(), stream.getPos());
  }

  static String format(Image image) {
    return ImageCompactFormatsNativeHooks.format(image);
  }

  static boolean formatProbeAvailable() {
    return ImageCompactFormatsNativeHooks.formatProbeAvailable();
  }

  static boolean metricProbeAvailable(String method) {
    return ImageCompactFormatsNativeHooks.metricProbeAvailable(method);
  }

  static long metric(String method) {
    return ImageCompactFormatsNativeHooks.metric(method);
  }

  static void invokeStaticRequired(String className, String method) throws Exception {
    ImageCompactFormatsNativeHooks.invokeStaticRequired(className, method);
  }

  static Quality quality(int[] actual, int[] expected) {
    ImageRasterBenchmarkSupport.require(actual != null && expected != null
        && actual.length == expected.length, "quality arrays differ");
    long squared = 0;
    int max = 0;
    int count = 0;
    for (int i = 0; i < actual.length; i++) {
      int a = actual[i];
      int e = expected[i];
      max = Math.max(max, Math.abs(((a >>> 24) & 0xFF) - ((e >>> 24) & 0xFF)));
      squared += square(((a >>> 24) & 0xFF) - ((e >>> 24) & 0xFF));
      count++;
      if (((e >>> 24) & 0xFF) != 0) {
        max = Math.max(max, Math.abs(((a >>> 16) & 0xFF) - ((e >>> 16) & 0xFF)));
        max = Math.max(max, Math.abs(((a >>> 8) & 0xFF) - ((e >>> 8) & 0xFF)));
        max = Math.max(max, Math.abs((a & 0xFF) - (e & 0xFF)));
        squared += square(((a >>> 16) & 0xFF) - ((e >>> 16) & 0xFF));
        squared += square(((a >>> 8) & 0xFF) - ((e >>> 8) & 0xFF));
        squared += square((a & 0xFF) - (e & 0xFF));
        count += 3;
      }
    }
    return new Quality(max, Math.sqrt((double) squared / Math.max(1, count)));
  }

  static Quality compositeQuality(int[] actual, int[] expected, int background) {
    long squared = 0;
    int max = 0;
    int count = actual.length * 3;
    for (int i = 0; i < actual.length; i++) {
      int actualAlpha = alpha(actual[i]);
      int expectedAlpha = alpha(expected[i]);
      int actualRed = composite(red(actual[i]), actualAlpha, background);
      int actualGreen = composite(green(actual[i]), actualAlpha, background);
      int actualBlue = composite(blue(actual[i]), actualAlpha, background);
      int expectedRed = composite(red(expected[i]), expectedAlpha, background);
      int expectedGreen = composite(green(expected[i]), expectedAlpha, background);
      int expectedBlue = composite(blue(expected[i]), expectedAlpha, background);
      int redError = actualRed - expectedRed;
      int greenError = actualGreen - expectedGreen;
      int blueError = actualBlue - expectedBlue;
      max = Math.max(max, Math.max(Math.abs(redError),
          Math.max(Math.abs(greenError), Math.abs(blueError))));
      squared += square(redError) + square(greenError) + square(blueError);
    }
    return new Quality(max, Math.sqrt((double) squared / Math.max(1, count)));
  }

  private static int composite(int channel, int alpha, int background) {
    return (channel * alpha + background * (255 - alpha) + 127) / 255;
  }

  private static long square(int value) {
    return (long) value * value;
  }

  static int[] rgb565Reference(int[] rgba) {
    int[] result = new int[rgba.length];
    for (int i = 0; i < rgba.length; i++) {
      int pixel = rgba[i];
      int red = expand5((red(pixel) * 31 + 127) / 255);
      int green = expand6((green(pixel) * 63 + 127) / 255);
      int blue = expand5((blue(pixel) * 31 + 127) / 255);
      result[i] = argb(255, red, green, blue);
    }
    return result;
  }

  static int[] gray8Reference(int[] rgba) {
    int[] result = new int[rgba.length];
    for (int i = 0; i < rgba.length; i++) {
      int value = red(rgba[i]);
      result[i] = argb(255, value, value, value);
    }
    return result;
  }

  static int[] argb4444Reference(int[] rgba) {
    int[] result = new int[rgba.length];
    for (int i = 0; i < rgba.length; i++) {
      int pixel = rgba[i];
      int alpha = quantize4(alpha(pixel));
      int premulRed = quantize4((red(pixel) * alpha + 7) / 15);
      int premulGreen = quantize4((green(pixel) * alpha + 7) / 15);
      int premulBlue = quantize4((blue(pixel) * alpha + 7) / 15);
      int unpremulRed = alpha == 0 ? 0 : Math.min(255, (premulRed * 255 + alpha / 2) / alpha);
      int unpremulGreen = alpha == 0 ? 0 : Math.min(255, (premulGreen * 255 + alpha / 2) / alpha);
      int unpremulBlue = alpha == 0 ? 0 : Math.min(255, (premulBlue * 255 + alpha / 2) / alpha);
      result[i] = argb(alpha * 17, unpremulRed, unpremulGreen, unpremulBlue);
    }
    return result;
  }

  private static int quantize4(int value) {
    return (value * 15 + 127) / 255;
  }

  private static int expand5(int value) {
    return (value * 255 + 15) / 31;
  }

  private static int expand6(int value) {
    return (value * 255 + 31) / 63;
  }

  private static int alpha(int pixel) {
    return (pixel >>> 24) & 0xFF;
  }

  private static int red(int pixel) {
    return (pixel >>> 16) & 0xFF;
  }

  private static int green(int pixel) {
    return (pixel >>> 8) & 0xFF;
  }

  private static int blue(int pixel) {
    return pixel & 0xFF;
  }

  private static int argb(int alpha, int red, int green, int blue) {
    return (alpha << 24) | (red << 16) | (green << 8) | blue;
  }

  static final class Quality {
    final int maxError;
    final double rmse;

    Quality(int maxError, double rmse) {
      this.maxError = maxError;
      this.rmse = rmse;
    }
  }

  static long elapsedMillis(long start) {
    return totalcross.sys.Vm.getTimeStamp() - start;
  }
}
