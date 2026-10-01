// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;

/** Shared compact-storage smoke fixtures, independent pixel models, and accounting helpers. */
final class ImageCompactStorageSmokeSupport {
  static final int WIDTH = 8;
  static final int HEIGHT = 6;
  static final int COLOR_ROW_BYTES = WIDTH * 2;
  static final int GRAY_ROW_BYTES = WIDTH;
  static String compactDecodeFailureEvidence = "";
  static Image load(String name) throws Exception {
    byte[] bytes = Vm.getFile("image-compact/" + name);
    require(bytes != null && bytes.length > 0, "missing fixture " + name);
    return new Image(bytes, bytes.length);
  }

  static NativeImageBacking nativeBacking(Image image) {
    require(image.hasNativeBackingForSmoke(), "image did not materialize a native backing");
    require(image.backing instanceof NativeImageBacking, "image backing is not native");
    return (NativeImageBacking) image.backing;
  }

  static boolean checkFormat(NativeImageBacking backing, int format, int rowBytes, long bytes) {
    return backing.formatForTest() == format && backing.rowBytesForTest() == rowBytes
        && backing.backingBytesForTest() == bytes;
  }

  static int countCompact(NativeImageBacking... backings) {
    int count = 0;
    for (NativeImageBacking backing : backings) {
      if (backing.isCompact()) count++;
    }
    return count;
  }

  static boolean checkObservers(Image image, int format) throws Exception {
    NativeImageBacking backing = nativeBacking(image);
    int[] first = image.getPixels();
    int[] second = image.getPixels();
    byte[] row = new byte[WIDTH * 4];
    image.getPixelRow(row, 2);
    boolean rowParity = true;
    for (int x = 0; x < WIDTH; x++) {
      int pixel = first[2 * WIDTH + x];
      rowParity &= (row[x * 4] & 0xFF) == ((pixel >>> 16) & 0xFF)
          && (row[x * 4 + 1] & 0xFF) == ((pixel >>> 8) & 0xFF)
          && (row[x * 4 + 2] & 0xFF) == (pixel & 0xFF)
          && (row[x * 4 + 3] & 0xFF) == (pixel >>> 24);
    }
    ByteArrayStream encoded = new ByteArrayStream(1024);
    image.createPng(encoded);
    return same(first, second) && rowParity && encoded.getPos() > 0
        && backing.formatForTest() == format;
  }

  static int[] expectedOpaqueSource() {
    int[] expected = new int[WIDTH * HEIGHT];
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        int red = (x * 31 + y * 7) & 255;
        int green = (y * 43 + x * 5) & 255;
        int blue = ((x + y) * 23 + 11) & 255;
        expected[y * WIDTH + x] = 0xFF000000 | red << 16 | green << 8 | blue;
      }
    }
    return expected;
  }

  static int[] expectedOpaqueRgb565() {
    int[] source = expectedOpaqueSource();
    for (int i = 0; i < source.length; i++) {
      int p = source[i];
      int r5 = (((p >>> 16) & 255) * 31 + 127) / 255;
      int g6 = (((p >>> 8) & 255) * 63 + 127) / 255;
      int b5 = ((p & 255) * 31 + 127) / 255;
      int red = (r5 << 3) | (r5 >>> 2);
      int green = (g6 << 2) | (g6 >>> 4);
      int blue = (b5 << 3) | (b5 >>> 2);
      source[i] = 0xFF000000 | red << 16 | green << 8 | blue;
    }
    return source;
  }

  static int[] expectedArgb4444Readback() {
    int[] source = expectedAlphaSource();
    for (int i = 0; i < source.length; i++) {
      int pixel = source[i];
      int alpha = pixel >>> 24;
      int alpha4 = (alpha * 15 + 127) / 255;
      int expandedAlpha = alpha4 * 17;
      int red = unpremultiplyArgb4444((pixel >>> 16) & 255, alpha, alpha4);
      int green = unpremultiplyArgb4444((pixel >>> 8) & 255, alpha, alpha4);
      int blue = unpremultiplyArgb4444(pixel & 255, alpha, alpha4);
      source[i] = expandedAlpha << 24 | red << 16 | green << 8 | blue;
    }
    return source;
  }

  static int unpremultiplyArgb4444(int channel, int alpha, int alpha4) {
    if (alpha4 == 0) {
      return 0;
    }
    int premultiplied4 = (channel * alpha * 15 + 255 * 255 / 2) / (255 * 255);
    return Math.min(255, (premultiplied4 * 255 + alpha4 / 2) / alpha4);
  }

  static int[] expectedAlphaSource() {
    int[] expected = new int[WIDTH * HEIGHT];
    int[] alphas = {0, 32, 64, 96, 128, 160, 224, 255};
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        int alpha = alphas[x];
        int red = x == 0 ? 255 : (x * 29) & 255;
        int green = x == 0 ? 0 : (y * 37 + x * 3) & 255;
        int blue = x == 0 ? 255 : (255 - x * 21 - y * 5) & 255;
        expected[y * WIDTH + x] = alpha << 24 | red << 16 | green << 8 | blue;
      }
    }
    return expected;
  }

  static int[] expectedGraySource() {
    int[] expected = new int[WIDTH * HEIGHT];
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        int gray = 18 + x * 24 + y * 7;
        expected[y * WIDTH + x] = 0xFF000000 | gray << 16 | gray << 8 | gray;
      }
    }
    return expected;
  }

  static int[] compositeOnWhite(int[] pixels) {
    int[] result = pixels.clone();
    for (int i = 0; i < pixels.length; i++) {
      int alpha = pixels[i] >>> 24;
      int red = (pixels[i] >>> 16) & 255;
      int green = (pixels[i] >>> 8) & 255;
      int blue = pixels[i] & 255;
      red = (red * alpha + 255 * (255 - alpha) + 127) / 255;
      green = (green * alpha + 255 * (255 - alpha) + 127) / 255;
      blue = (blue * alpha + 255 * (255 - alpha) + 127) / 255;
      result[i] = 0xFF000000 | red << 16 | green << 8 | blue;
    }
    return result;
  }

  static boolean exactGray(int[] actual, int[] expected) {
    return java.util.Arrays.equals(actual, expected);
  }

  static boolean grayscaleChannels(int[] pixels) {
    for (int pixel : pixels) {
      if (((pixel >>> 16) & 255) != ((pixel >>> 8) & 255)
          || ((pixel >>> 8) & 255) != (pixel & 255) || (pixel >>> 24) != 255) {
        return false;
      }
    }
    return true;
  }

  static int compositingError(int[] expected, int[] actual) {
    int maxError = 0;
    for (int i = 0; i < expected.length; i++) {
      int expectedAlpha = expected[i] >>> 24;
      int actualAlpha = actual[i] >>> 24;
      for (int shift = 0; shift <= 16; shift += 8) {
        int expectedChannel = (expected[i] >>> shift) & 255;
        int actualChannel = (actual[i] >>> shift) & 255;
        for (int background : new int[] {0, 255}) {
          int expectedComposite = (expectedChannel * expectedAlpha
              + background * (255 - expectedAlpha) + 127) / 255;
          int actualComposite = (actualChannel * actualAlpha
              + background * (255 - actualAlpha) + 127) / 255;
          maxError = Math.max(maxError, Math.abs(expectedComposite - actualComposite));
        }
      }
    }
    return maxError;
  }

  static int maxChannelError(int[] first, int[] second) {
    int maxError = 0;
    for (int i = 0; i < first.length; i++) {
      for (int shift = 0; shift <= 24; shift += 8) {
        maxError = Math.max(maxError,
            Math.abs(((first[i] >>> shift) & 255) - ((second[i] >>> shift) & 255)));
      }
    }
    return maxError;
  }

  static double rootMeanSquareChannelError(int[] first, int[] second) {
    long squaredError = 0;
    int channelCount = first.length * 3;
    for (int i = 0; i < first.length; i++) {
      for (int shift = 0; shift <= 16; shift += 8) {
        int error = ((first[i] >>> shift) & 255) - ((second[i] >>> shift) & 255);
        squaredError += (long) error * error;
      }
    }
    return Math.sqrt((double) squaredError / channelCount);
  }

  static long metric(int metric, int format) {
    return NativeImageBacking.metricForTest(metric, format);
  }

  static boolean compactDecodeFailureRetry(String fixture) throws Exception {
    NativeImageBacking.resetBackingAccountingForTest();
    long liveBefore = NativeImageBacking.backingRecordsLiveForTest();
    Image image = load(fixture);
    EncodedImageSource source = (EncodedImageSource) image.pipelineForSmoke().root();
    Image.failCompactCandidateForTest();
    boolean transientFailure = false;
    boolean materializedOnFirstAttempt = false;
    try {
      materializedOnFirstAttempt = image.hasNativeBackingForSmoke();
    } catch (IllegalStateException failure) {
      transientFailure = failure.getCause() instanceof TransientImageMaterializationException;
    }
    long createdAfterFirstAttempt = NativeImageBacking.backingRecordsCreatedForTest();
    long releasedAfterFirstAttempt = NativeImageBacking.backingRecordsReleasedForTest();
    long liveAfterFirstAttempt = NativeImageBacking.backingRecordsLiveForTest();
    boolean transientFailureClean = transientFailure && createdAfterFirstAttempt == 1
        && releasedAfterFirstAttempt == 1 && liveAfterFirstAttempt == liveBefore
        && image.pipelineForSmoke() != null;
    boolean fallbackRetryClean = materializedOnFirstAttempt && createdAfterFirstAttempt >= 2
        && releasedAfterFirstAttempt == 1
        && liveAfterFirstAttempt == liveBefore + createdAfterFirstAttempt - releasedAfterFirstAttempt;
    boolean failedCandidateReleased = transientFailureClean || fallbackRetryClean;
    if (!failedCandidateReleased || source.decodeFailure() != null
        || !image.hasNativeBackingForSmoke()) {
      compactDecodeFailureEvidence += fixture + "=failed:thrown=" + transientFailure
          + ",materialized=" + materializedOnFirstAttempt + ",created=" + createdAfterFirstAttempt
          + ",released=" + releasedAfterFirstAttempt + ";";
      return false;
    }
    NativeImageBacking backing = nativeBacking(image);
    long createdAfterRetry = NativeImageBacking.backingRecordsCreatedForTest();
    long releasedAfterRetry = NativeImageBacking.backingRecordsReleasedForTest();
    boolean retrySucceeded = backing.isCompact() && releasedAfterRetry == 1
        && NativeImageBacking.backingRecordsLiveForTest()
            == liveBefore + createdAfterRetry - releasedAfterRetry;
    compactDecodeFailureEvidence += fixture + "="
        + (transientFailure ? "explicit-retry" : "fallback-retry") + ":"
        + createdAfterRetry + "/" + releasedAfterRetry + ";";
    return retrySucceeded;
  }

  static boolean same(int[] first, int[] second) {
    return java.util.Arrays.equals(first, second);
  }

  static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
  static void captureAccounting(Metrics metrics, int compactDecodeFixtureCount) {
      metrics.liveGrayBytes = metric(NativeImageBacking.TEST_METRIC_LIVE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_GRAY8);
      metrics.peakGrayBytes = metric(NativeImageBacking.TEST_METRIC_PEAK_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_GRAY8);
      metrics.liveRgb565Bytes = metric(NativeImageBacking.TEST_METRIC_LIVE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGB565);
      metrics.peakRgb565Bytes = metric(NativeImageBacking.TEST_METRIC_PEAK_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGB565);
      metrics.liveArgb4444Bytes = metric(NativeImageBacking.TEST_METRIC_LIVE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_ARGB4444);
      metrics.peakArgb4444Bytes = metric(NativeImageBacking.TEST_METRIC_PEAK_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_ARGB4444);
      metrics.liveRgbaBytes = metric(NativeImageBacking.TEST_METRIC_LIVE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGBA8888);
      metrics.peakRgbaBytes = metric(NativeImageBacking.TEST_METRIC_PEAK_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGBA8888);
      metrics.grayDecodeCount = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT,
          NativeImageBacking.FORMAT_GRAY8);
      metrics.grayDecodeBytes = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_GRAY8);
      metrics.rgb565DecodeCount = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT,
          NativeImageBacking.FORMAT_RGB565);
      metrics.rgb565DecodeBytes = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_RGB565);
      metrics.argb4444DecodeCount = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT,
          NativeImageBacking.FORMAT_ARGB4444);
      metrics.argb4444DecodeBytes = metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT,
          NativeImageBacking.FORMAT_ARGB4444);
      metrics.compactReadbackCount = metric(NativeImageBacking.TEST_METRIC_COMPACT_READBACK_COUNT, 0);
      metrics.rowScratchPeakBytes = metric(NativeImageBacking.TEST_METRIC_ROW_SCRATCH_PEAK_BYTES, 0);
      metrics.fullRgbaDecodeTempBytes = metric(NativeImageBacking.TEST_METRIC_FULL_RGBA_DECODE_TEMP_BYTES, 0);
      metrics.promotionAttempts = metric(NativeImageBacking.TEST_METRIC_PROMOTION_ATTEMPTS, 0);
      metrics.promotionSuccesses = metric(NativeImageBacking.TEST_METRIC_PROMOTION_SUCCESSES, 0);
      metrics.promotionFailures = metric(NativeImageBacking.TEST_METRIC_PROMOTION_FAILURES, 0);
      metrics.promotionBytes = metric(NativeImageBacking.TEST_METRIC_PROMOTION_BYTES, 0);
      long directDecodeCount = metrics.grayDecodeCount + metrics.rgb565DecodeCount + metrics.argb4444DecodeCount;
      long directDecodeBytes = metrics.grayDecodeBytes + metrics.rgb565DecodeBytes + metrics.argb4444DecodeBytes;
      metrics.compactMetrics = directDecodeCount == compactDecodeFixtureCount + 3
          && directDecodeBytes == 3L * GRAY_ROW_BYTES * HEIGHT
              + 7L * COLOR_ROW_BYTES * HEIGHT
          && metrics.grayDecodeCount == 3 && metrics.grayDecodeBytes == 3L * GRAY_ROW_BYTES * HEIGHT
          && metrics.rgb565DecodeCount == 4 && metrics.rgb565DecodeBytes == 4L * COLOR_ROW_BYTES * HEIGHT
          && metrics.argb4444DecodeCount == 3 && metrics.argb4444DecodeBytes == 3L * COLOR_ROW_BYTES * HEIGHT
          && metrics.liveGrayBytes >= metrics.grayDecodeBytes && metrics.liveRgb565Bytes >= metrics.rgb565DecodeBytes
          && metrics.liveArgb4444Bytes >= metrics.argb4444DecodeBytes && metrics.liveRgbaBytes >= WIDTH * HEIGHT * 4
          && metrics.peakGrayBytes >= metrics.grayDecodeBytes && metrics.peakRgb565Bytes >= metrics.rgb565DecodeBytes
          && metrics.peakArgb4444Bytes >= metrics.argb4444DecodeBytes && metrics.peakRgbaBytes >= metrics.liveRgbaBytes
          && metrics.peakRgb565Bytes >= metrics.liveRgb565Bytes && metrics.peakGrayBytes >= metrics.liveGrayBytes
          && metrics.peakArgb4444Bytes >= metrics.liveArgb4444Bytes
          && metrics.compactReadbackCount > 0 && metrics.rowScratchPeakBytes == WIDTH * 4
          && metrics.fullRgbaDecodeTempBytes == 0
          && metrics.promotionAttempts == metrics.promotionSuccesses + metrics.promotionFailures
          && metrics.promotionFailures == 1 && metrics.promotionSuccesses >= 3
          && metrics.promotionBytes == metrics.promotionSuccesses * WIDTH * HEIGHT * 4;
      require(metrics.compactMetrics, "compact storage lifecycle metrics were incomplete or inconsistent");

  }

  static long compactDecodeCount() {
    return metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT, NativeImageBacking.FORMAT_RGB565)
        + metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT, NativeImageBacking.FORMAT_GRAY8)
        + metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT, NativeImageBacking.FORMAT_ARGB4444);
  }

  static long compactDecodeBytes() {
    return metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT, NativeImageBacking.FORMAT_RGB565)
        + metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT, NativeImageBacking.FORMAT_GRAY8)
        + metric(NativeImageBacking.TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT, NativeImageBacking.FORMAT_ARGB4444);
  }

  static final class Metrics {
    boolean compactMetrics;
    long liveGrayBytes = -1, peakGrayBytes = -1;
    long liveRgb565Bytes = -1, peakRgb565Bytes = -1;
    long liveArgb4444Bytes = -1, peakArgb4444Bytes = -1;
    long liveRgbaBytes = -1, peakRgbaBytes = -1;
    long grayDecodeCount = -1, grayDecodeBytes = -1;
    long rgb565DecodeCount = -1, rgb565DecodeBytes = -1;
    long argb4444DecodeCount = -1, argb4444DecodeBytes = -1;
    long compactReadbackCount = -1, rowScratchPeakBytes = -1;
    long fullRgbaDecodeTempBytes = -1;
    long promotionAttempts = -1, promotionSuccesses = -1, promotionFailures = -1;
    long promotionBytes = -1;
    int promotionReadbackError = -1, promotionFormat = -1, promotionElapsedMillis = -1;
    long promotionBytesBefore = -1, promotionBytesAfter = -1;
    boolean promotionOpacityPreserved, promotionGenerationAdvanced;
    boolean promotionVariantsCleared, cachedSiblingPreserved;
  }

}
