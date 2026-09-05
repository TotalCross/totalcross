// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Correctness, ownership, observer, promotion, and retry smoke for Phase 3. */
public class ImageCompactFormatsSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean selection = false;
    boolean observer = false;
    boolean promotion = false;
    boolean decodeFailureRetry = false;
    boolean promotionFailureRetry = false;
    boolean writePixelsParity = false;
    boolean formatProbe = ImageCompactFormatsBenchmarkSupport.formatProbeAvailable();
    String error = "";
    try {
      ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures =
          ImageCompactFormatsBenchmarkSupport.fixtures();
      ImageCompactFormatsBenchmarkSupport.configure("post-enabled", "combined-enabled", true);
      Image.resetImageOperationAccountingForTest();
      Image[] images = new Image[fixtures.length];
      for (int i = 0; i < fixtures.length; i++) {
        images[i] = ImageCompactFormatsBenchmarkSupport.materialize(fixtures[i].bytes);
      }
      selection = checkSelection(images);
      long promotionAttemptsBefore = ImageCompactFormatsBenchmarkSupport.metric("promotionAttemptsForTest");
      for (Image image : images) {
        int[] first = image.getPixels();
        int[] second = image.getPixels();
        ImageRasterBenchmarkSupport.require(ImageRasterBenchmarkSupport.fullPixelHash(first)
            == ImageRasterBenchmarkSupport.fullPixelHash(second), "observer pixel drift");
        ImageCompactFormatsBenchmarkSupport.encodePng(image);
      }
      observer = promotionAttemptsBefore
          == ImageCompactFormatsBenchmarkSupport.metric("promotionAttemptsForTest")
          && checkSelection(images);
      writePixelsParity = checkDrawParity(images[0]);
      decodeFailureRetry = checkDecodeFailureRetry(fixtures[0]);
      promotion = checkPromotions(fixtures);
      promotionFailureRetry = checkPromotionFailureRetry(fixtures[1]);
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = error.length() == 0 && selection && observer && promotion
        && decodeFailureRetry && promotionFailureRetry && writePixelsParity;
    System.out.println("fixture=ImageCompactFormatsSmokeApp,formatProbe=" + formatProbe
        + ",selection=" + selection + ",observerNonPromotion=" + observer
        + ",promotion=" + promotion + ",decodeFailureRetry=" + decodeFailureRetry
        + ",promotionFailureRetry=" + promotionFailureRetry
        + ",writePixelsParity=" + writePixelsParity
        + ",backingBytesLive=" + ImageCompactFormatsBenchmarkSupport.metric("backingBytesLiveForTest")
        + ",promotionAttempts=" + ImageCompactFormatsBenchmarkSupport.metric("promotionAttemptsForTest")
        + ",promotionSuccesses=" + ImageCompactFormatsBenchmarkSupport.metric("promotionSuccessesForTest")
        + ",promotionFailures=" + ImageCompactFormatsBenchmarkSupport.metric("promotionFailuresForTest")
        + ",overallPass=" + pass + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(pass ? 0 : 1);
  }

  private static boolean checkSelection(Image[] images) {
    if (!ImageCompactFormatsBenchmarkSupport.formatProbeAvailable()) {
      return true;
    }
    String[] expected = {
        ImageCompactFormatsBenchmarkSupport.RGB565,
        ImageCompactFormatsBenchmarkSupport.RGB565,
        ImageCompactFormatsBenchmarkSupport.GRAY8,
        ImageCompactFormatsBenchmarkSupport.GRAY8,
        ImageCompactFormatsBenchmarkSupport.ARGB4444
    };
    for (int i = 0; i < images.length; i++) {
      if (!expected[i].equals(ImageCompactFormatsBenchmarkSupport.format(images[i]))) {
        return false;
      }
    }
    return true;
  }

  private static boolean checkPromotions(
      ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures) throws Exception {
    if (!ImageCompactFormatsBenchmarkSupport.formatProbeAvailable()) {
      return true;
    }
    for (int index : new int[] { 0, 2, 4 }) {
      Image image = ImageCompactFormatsBenchmarkSupport.materialize(fixtures[index].bytes);
      String before = ImageCompactFormatsBenchmarkSupport.format(image);
      Graphics graphics = image.getGraphics();
      if (graphics == null || !ImageCompactFormatsBenchmarkSupport.RGBA8888.equals(
          ImageCompactFormatsBenchmarkSupport.format(image))) {
        return false;
      }
      graphics.backColor = 0x00445566;
      graphics.fillRect(0, 0, 1, 1);
      if (before.equals(ImageCompactFormatsBenchmarkSupport.format(image))) {
        return false;
      }
    }
    return true;
  }

  private static boolean checkDecodeFailureRetry(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture) throws Exception {
    if (!ImageCompactFormatsBenchmarkSupport.metricProbeAvailable(
        "failNextCompactDecodeAfterAllocationForTest")) {
      return true;
    }
    long beforeBytes = ImageCompactFormatsBenchmarkSupport.metric("backingBytesLiveForTest");
    ImageCompactFormatsBenchmarkSupport.invokeStaticRequired("totalcross.ui.image.Image",
        "failNextCompactDecodeAfterAllocationForTest");
    boolean failed = false;
    try {
      ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    } catch (Throwable expected) {
      failed = true;
    }
    long afterBytes = ImageCompactFormatsBenchmarkSupport.metric("backingBytesLiveForTest");
    Image retry = ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    return failed && beforeBytes == afterBytes && retry.getPixels() != null;
  }

  private static boolean checkPromotionFailureRetry(
      ImageCompactFormatsBenchmarkSupport.Fixture fixture) throws Exception {
    if (!ImageCompactFormatsBenchmarkSupport.metricProbeAvailable("failNextPromotionForTest")) {
      return true;
    }
    Image image = ImageCompactFormatsBenchmarkSupport.materialize(fixture.bytes);
    String before = ImageCompactFormatsBenchmarkSupport.format(image);
    ImageCompactFormatsBenchmarkSupport.invokeStaticRequired(
        "totalcross.ui.image.NativeImageBacking", "failNextPromotionForTest");
    boolean failed = false;
    try {
      Graphics graphics = image.getGraphics();
      if (graphics == null) {
        failed = true;
      }
    } catch (Throwable expected) {
      failed = true;
    }
    boolean preserved = before.equals(ImageCompactFormatsBenchmarkSupport.format(image));
    Graphics retry = image.getGraphics();
    return failed && preserved && retry != null
        && ImageCompactFormatsBenchmarkSupport.RGBA8888.equals(
            ImageCompactFormatsBenchmarkSupport.format(image));
  }

  private static boolean checkDrawParity(Image source) throws Exception {
    Image target = new Image(source.getPixelWidth(), source.getPixelHeight());
    Graphics graphics = target.getGraphics();
    if (graphics == null) {
      return false;
    }
    graphics.drawImage(source, 0, 0, false);
    int[] actual = target.getPixels();
    int[] expected = source.getPixels();
    if (actual.length != expected.length) {
      return false;
    }
    for (int i = 0; i < actual.length; i++) {
      if (actual[i] != expected[i]) {
        return false;
      }
    }
    return true;
  }
}
