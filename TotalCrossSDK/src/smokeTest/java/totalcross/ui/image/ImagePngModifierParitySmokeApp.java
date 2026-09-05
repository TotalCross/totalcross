// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Full-buffer eager/deferred parity diagnosis for the Lenna PNG fixture. */
public class ImagePngModifierParitySmokeApp extends MainWindow {
  private static final String SOURCE = "image-modifier-memory/lenna.png";
  private static final int SOURCE_BASELINE = -1;
  private static final int TOUCH_UP = 0;
  private static final int ROTATE_SCALE = 1;
  private static final int ROTATE_THEN_TOUCH_UP = 2;
  private static final int TOUCH_UP_THEN_ROTATE = 3;

  private static final class CaseResult {
    final String name;
    int pixelCount = -1;
    int width = -1;
    int height = -1;
    int deferredWidth = -1;
    int deferredHeight = -1;
    boolean dimensionsMatch;
    int rawDifferingPixels;
    int transparentOnlyDifferingPixels;
    int visibleSemanticDifferingPixels;
    int alphaDifferingPixels;
    int eagerAlphaZeroWithRgb;
    int deferredAlphaZeroWithRgb;
    int firstDifferingIndex = -1;
    int firstExpected;
    int firstActual;
    int deltaA;
    int deltaR;
    int deltaG;
    int deltaB;
    int maxAbsA;
    int maxAbsR;
    int maxAbsG;
    int maxAbsB;
    long rawEagerCrc32;
    long rawDeferredCrc32;
    long normalizedEagerCrc32;
    long normalizedDeferredCrc32;
    String error = "";

    CaseResult(String name) {
      this.name = name;
    }

    boolean semanticParityPass() {
      return dimensionsMatch && error.length() == 0 && visibleSemanticDifferingPixels == 0
          && alphaDifferingPixels == 0;
    }

    boolean exactParityPass() {
      return semanticParityPass() && rawDifferingPixels == 0;
    }
  }

  private static final class PixelOutput {
    final int[] pixels;
    final int width;
    final int height;

    PixelOutput(int[] pixels, int width, int height) {
      this.pixels = pixels;
      this.width = width;
      this.height = height;
    }
  }

  @Override
  public void initUI() {
    boolean diagnosticCompleted = false;
    boolean plusOneControlPass = false;
    boolean allCasesSemanticParity = false;
    int fixtureWidth = -1;
    int fixtureHeight = -1;
    CaseResult[] results = new CaseResult[6];
    String error = "";

    try {
      byte[] encoded = Vm.getFile(SOURCE);
      require(encoded != null && encoded.length > 0, "encoded fixture");
      Image probe = new Image(encoded);
      fixtureWidth = probe.getPixelWidth();
      fixtureHeight = probe.getPixelHeight();

      results[0] = compareCase("SOURCE_BASELINE", encoded, SOURCE_BASELINE, 0);
      results[1] = compareCase("TOUCH_UP_+16", encoded, TOUCH_UP, 16);
      results[2] = compareCase("ROTATE_SCALE_20_0", encoded, ROTATE_SCALE, 0);
      results[3] = compareCase("ROTATE_SCALE_THEN_TOUCH_UP_+16", encoded,
          ROTATE_THEN_TOUCH_UP, 16);
      results[4] = compareCase("TOUCH_UP_THEN_ROTATE_SCALE_+16", encoded,
          TOUCH_UP_THEN_ROTATE, 16);
      results[5] = compareCase("ROTATE_SCALE_THEN_TOUCH_UP_+1_CONTROL", encoded,
          ROTATE_THEN_TOUCH_UP, 1);
      plusOneControlPass = results[5].exactParityPass();
      diagnosticCompleted = true;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    for (CaseResult result : results) {
      if (result != null) {
        System.out.println(formatResult(result));
      }
    }
    allCasesSemanticParity = diagnosticCompleted;
    for (CaseResult result : results) {
      if (result == null || !result.semanticParityPass()) {
        allCasesSemanticParity = false;
      }
    }
    boolean overallPass = diagnosticCompleted && allCasesSemanticParity && plusOneControlPass;
    System.out.println("fixture=ImagePngModifierParitySmokeApp,fixturePath=" + SOURCE
        + ",fixtureFormat=PNG,fixtureWidth=" + fixtureWidth + ",fixtureHeight=" + fixtureHeight
        + ",diagnosticCompleted=" + diagnosticCompleted + ",plusOneControlPass="
        + plusOneControlPass + ",allCasesSemanticParity=" + allCasesSemanticParity
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static CaseResult compareCase(String name, byte[] encoded, int operation, int brightness) {
    CaseResult result = new CaseResult(name);
    try {
      PixelOutput eager = eagerOutput(encoded, operation, brightness);
      PixelOutput deferred = deferredOutput(encoded, operation, brightness);
      result.width = eager.width;
      result.height = eager.height;
      result.deferredWidth = deferred.width;
      result.deferredHeight = deferred.height;
      result.dimensionsMatch = eager.width == deferred.width && eager.height == deferred.height;
      if (!result.dimensionsMatch) {
        result.error = "output_dimensions_mismatch:eager=" + eager.width + "x" + eager.height
            + ":deferred=" + deferred.width + "x" + deferred.height;
      }
      result.pixelCount = Math.min(eager.pixels.length, deferred.pixels.length);
      result.rawEagerCrc32 = crc32(eager.pixels, false);
      result.rawDeferredCrc32 = crc32(deferred.pixels, false);
      result.normalizedEagerCrc32 = crc32(eager.pixels, true);
      result.normalizedDeferredCrc32 = crc32(deferred.pixels, true);
      comparePixels(result, eager.pixels, deferred.pixels);
    } catch (Throwable failure) {
      result.error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    return result;
  }

  private static PixelOutput eagerOutput(byte[] encoded, int operation, int brightness)
      throws Exception {
    Image root = new Image(encoded);
    root.getPixels();
    return output(transform(root, operation, brightness));
  }

  private static PixelOutput deferredOutput(byte[] encoded, int operation, int brightness)
      throws Exception {
    Image root = new Image(encoded);
    Image deferred = transform(root, operation, brightness);
    return drawToPixels(deferred);
  }

  private static Image transform(Image root, int operation, int brightness) throws Exception {
    switch (operation) {
      case SOURCE_BASELINE:
        return root;
      case TOUCH_UP:
        return root.getTouchedUpInstance((byte) brightness, (byte) 0);
      case ROTATE_SCALE:
        return root.getRotatedScaledInstance(20, 0, 0);
      case ROTATE_THEN_TOUCH_UP:
        return root.getRotatedScaledInstance(20, 0, 0)
            .getTouchedUpInstance((byte) brightness, (byte) 0);
      case TOUCH_UP_THEN_ROTATE:
        return root.getTouchedUpInstance((byte) brightness, (byte) 0)
            .getRotatedScaledInstance(20, 0, 0);
      default:
        throw new IllegalArgumentException("unknown operation " + operation);
    }
  }

  private static PixelOutput output(Image image) throws Exception {
    return new PixelOutput(image.getPixels(), image.getPixelWidth(), image.getPixelHeight());
  }

  private static PixelOutput drawToPixels(Image image) throws Exception {
    Image target = new Image(image.getPixelWidth(), image.getPixelHeight());
    target.getGraphics().drawImage(image, 0, 0, true);
    return output(target);
  }

  private static void comparePixels(CaseResult result, int[] eager, int[] deferred) {
    if (eager.length != deferred.length) {
      String lengthError = "pixel_buffer_length_mismatch:" + eager.length + ":" + deferred.length;
      result.error = result.error.length() == 0 ? lengthError : result.error + "|" + lengthError;
    }
    int count = Math.min(eager.length, deferred.length);
    for (int index = 0; index < count; index++) {
      int expected = eager[index];
      int actual = deferred[index];
      if (channel(expected, 24) == 0 && (channel(expected, 16) != 0
          || channel(expected, 8) != 0 || channel(expected, 0) != 0)) {
        result.eagerAlphaZeroWithRgb++;
      }
      if (channel(actual, 24) == 0 && (channel(actual, 16) != 0
          || channel(actual, 8) != 0 || channel(actual, 0) != 0)) {
        result.deferredAlphaZeroWithRgb++;
      }
      if (expected == actual) {
        continue;
      }
      result.rawDifferingPixels++;
      int expectedAlpha = channel(expected, 24);
      int actualAlpha = channel(actual, 24);
      if (expectedAlpha == 0 && actualAlpha == 0) {
        result.transparentOnlyDifferingPixels++;
      } else {
        result.visibleSemanticDifferingPixels++;
      }
      if (expectedAlpha != actualAlpha) {
        result.alphaDifferingPixels++;
      }
      if (result.firstDifferingIndex < 0) {
        result.firstDifferingIndex = index;
        result.firstExpected = expected;
        result.firstActual = actual;
        result.deltaA = channel(actual, 24) - channel(expected, 24);
        result.deltaR = channel(actual, 16) - channel(expected, 16);
        result.deltaG = channel(actual, 8) - channel(expected, 8);
        result.deltaB = channel(actual, 0) - channel(expected, 0);
      }
      result.maxAbsA = Math.max(result.maxAbsA, Math.abs(channel(actual, 24) - channel(expected, 24)));
      result.maxAbsR = Math.max(result.maxAbsR, Math.abs(channel(actual, 16) - channel(expected, 16)));
      result.maxAbsG = Math.max(result.maxAbsG, Math.abs(channel(actual, 8) - channel(expected, 8)));
      result.maxAbsB = Math.max(result.maxAbsB, Math.abs(channel(actual, 0) - channel(expected, 0)));
    }
  }

  private static String formatResult(CaseResult result) {
    String firstX = result.firstDifferingIndex < 0 ? "N/A"
        : String.valueOf(result.firstDifferingIndex % result.width);
    String firstY = result.firstDifferingIndex < 0 ? "N/A"
        : String.valueOf(result.firstDifferingIndex / result.width);
    String firstIndex = result.firstDifferingIndex < 0 ? "N/A"
        : String.valueOf(result.firstDifferingIndex);
    String expected = result.firstDifferingIndex < 0 ? "N/A" : hex(result.firstExpected);
    String actual = result.firstDifferingIndex < 0 ? "N/A" : hex(result.firstActual);
    String delta = result.firstDifferingIndex < 0 ? "N/A"
        : "A=" + result.deltaA + "/R=" + result.deltaR + "/G=" + result.deltaG
            + "/B=" + result.deltaB;
    return "case=" + result.name + ",pixelCount=" + result.pixelCount
        + ",rawDifferingPixels=" + result.rawDifferingPixels
        + ",transparentOnlyDifferingPixels=" + result.transparentOnlyDifferingPixels
        + ",visibleSemanticDifferingPixels=" + result.visibleSemanticDifferingPixels
        + ",alphaDifferingPixels=" + result.alphaDifferingPixels
        + ",firstDifferingX=" + firstX
        + ",firstDifferingY=" + firstY + ",firstDifferingIndex=" + firstIndex
        + ",expectedArgb=" + expected + ",actualArgb=" + actual
        + ",eagerOutputDimensions=" + result.width + "x" + result.height
        + ",deferredOutputDimensions=" + result.deferredWidth + "x" + result.deferredHeight
        + ",dimensionsMatch=" + result.dimensionsMatch
        + ",eagerAlphaZeroWithRgb=" + result.eagerAlphaZeroWithRgb
        + ",deferredAlphaZeroWithRgb=" + result.deferredAlphaZeroWithRgb
        + ",perChannelDelta=" + delta + ",maxAbsDelta=A=" + result.maxAbsA
        + "/R=" + result.maxAbsR + "/G=" + result.maxAbsG + "/B=" + result.maxAbsB
        + ",rawEagerCrc32=" + hex(result.rawEagerCrc32) + ",rawDeferredCrc32="
        + hex(result.rawDeferredCrc32) + ",normalizedEagerCrc32="
        + hex(result.normalizedEagerCrc32) + ",normalizedDeferredCrc32="
        + hex(result.normalizedDeferredCrc32) + ",semanticParityPass="
        + result.semanticParityPass() + ",exactParityPass=" + result.exactParityPass()
        + (result.error.length() == 0 ? "" : ",error=" + result.error);
  }

  private static int channel(int pixel, int shift) {
    return (pixel >>> shift) & 0xff;
  }

  private static long crc32(int[] pixels, boolean normalizeTransparent) {
    long crc = 0xffffffffL;
    for (int pixel : pixels) {
      if (normalizeTransparent && channel(pixel, 24) == 0) {
        pixel = 0;
      }
      for (int shift = 24; shift >= 0; shift -= 8) {
        crc ^= (pixel >>> shift) & 0xff;
        for (int bit = 0; bit < 8; bit++) {
          crc = (crc & 1) == 0 ? crc >>> 1 : (crc >>> 1) ^ 0xedb88320L;
        }
      }
    }
    return (~crc) & 0xffffffffL;
  }

  private static String hex(long value) {
    return "0x" + Convert.unsigned2hex((int) value, 8);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
