// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.gfx.Graphics;

/** Shared setup and reporting helpers for phase-2 raster benchmark apps. */
final class ImageRasterBenchmarkSupport {
  private static final long FNV_OFFSET = 0xcbf29ce484222325L;
  private static final long FNV_PRIME = 0x100000001b3L;

  private ImageRasterBenchmarkSupport() {
  }

  static void configure(String scenario, int targetFeature) {
    require("pre".equals(scenario) || "post-disabled".equals(scenario)
        || "post-enabled".equals(scenario), "invalid scenario");
    ImageOptimizationSettings.resetForTest();
    if (!"pre".equals(scenario)) {
      for (int feature = 0; feature < ImageOptimizationSettings.FEATURE_COUNT; feature++) {
        ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.DISABLED);
      }
      if ("post-enabled".equals(scenario)) {
        ImageOptimizationSettings.setState(targetFeature, ImageOptimizationSettings.ENABLED);
      }
    }
  }

  static byte[] resource(String path) {
    byte[] bytes = Vm.getFile(path);
    require(bytes != null && bytes.length > 0, "resource " + path);
    return bytes;
  }

  static byte[] opaquePng(int width, int height) throws Exception {
    Image image = new Image(width, height);
    Graphics graphics = image.getGraphics();
    require(graphics != null, "opaque PNG graphics");
    for (int y = 0; y < height; y += 32) {
      for (int x = 0; x < width; x += 32) {
        graphics.backColor = 0x102030 + ((x * 13 + y * 7) & 0x00D0D0D0);
        graphics.fillRect(x, y, Math.min(32, width - x), Math.min(32, height - y));
      }
    }
    ByteArrayStream stream = new ByteArrayStream(width * 4);
    image.createPng(stream);
    return copy(stream.getBuffer(), stream.getPos());
  }

  static Image materialize(byte[] encoded) throws Exception {
    Image image = new Image(encoded, encoded.length);
    require(image.getGraphics() != null, "decoded image graphics");
    return image;
  }

  static long fullPixelHash(Image image) {
    return fullPixelHash(image.getPixels());
  }

  static long fullPixelHash(int[] pixels) {
    require(pixels != null, "pixel output");
    long hash = FNV_OFFSET;
    for (int pixel : pixels) {
      hash ^= pixel & 0xFFFFFFFFL;
      hash *= FNV_PRIME;
    }
    return hash;
  }

  static long fullByteHash(byte[] bytes, int length) {
    require(bytes != null && length >= 0 && length <= bytes.length, "byte output");
    long hash = FNV_OFFSET;
    for (int i = 0; i < length; i++) {
      hash ^= bytes[i] & 0xFFL;
      hash *= FNV_PRIME;
    }
    return hash;
  }

  static String hashString(long hash) {
    return Integer.toHexString((int) (hash >>> 32))
        + Integer.toHexString((int) hash);
  }

  static byte[] copy(byte[] source, int length) {
    byte[] copy = new byte[length];
    Vm.arrayCopy(source, 0, copy, 0, length);
    return copy;
  }

  static String argument(String commandLine, String name, String fallback) {
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

  static int integerArgument(String commandLine, String name, int fallback) {
    try {
      return Integer.parseInt(argument(commandLine, name, String.valueOf(fallback)));
    } catch (NumberFormatException ignored) {
      return fallback;
    }
  }

  static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  static boolean finish(String fixture, String scenario, int samples, int completedSamples,
      String details, String error) {
    boolean pass = completedSamples == samples && error.length() == 0;
    System.out.println("fixture=" + fixture + ",scenario=" + scenario + ",samples=" + samples
        + ",completed_samples=" + completedSamples + "," + details + ",overallPass=" + pass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    return pass;
  }
}
