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
    boolean writePixelsFallback = false;
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
      writePixelsParity = checkDrawParity(images[0]) && checkDrawParity(images[2]);
      writePixelsFallback = checkTranslucentFallback(images[4]);
      decodeFailureRetry = checkDecodeFailureRetry(fixtures);
      promotion = checkPromotions(fixtures);
      promotionFailureRetry = checkPromotionFailureRetry(fixtures);
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = error.length() == 0 && selection && observer && promotion
        && decodeFailureRetry && promotionFailureRetry && writePixelsParity
        && writePixelsFallback;
    System.out.println("fixture=ImageCompactFormatsSmokeApp,formatProbe=" + formatProbe
        + ",selection=" + selection + ",observerNonPromotion=" + observer
        + ",promotion=" + promotion + ",decodeFailureRetry=" + decodeFailureRetry
        + ",promotionFailureRetry=" + promotionFailureRetry
        + ",writePixelsParity=" + writePixelsParity
        + ",writePixelsFallback=" + writePixelsFallback
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
      int[] beforePixels = image.getPixels();
      long attemptsBefore = ImageCompactFormatsBenchmarkSupport.metric("promotionAttemptsForTest");
      Graphics graphics = image.getGraphics();
      int[] afterPromotionPixels = image.getPixels();
      long attemptsAfterPromotion = ImageCompactFormatsBenchmarkSupport.metric(
          "promotionAttemptsForTest");
      if (graphics == null || !ImageCompactFormatsBenchmarkSupport.RGBA8888.equals(
          ImageCompactFormatsBenchmarkSupport.format(image))
          || !samePixels(beforePixels, afterPromotionPixels)
          || attemptsAfterPromotion != attemptsBefore + 1) {
        System.out.println("promotionCheck index=" + index + ",before=" + before
            + ",after=" + ImageCompactFormatsBenchmarkSupport.format(image)
            + ",attemptsBefore=" + attemptsBefore + ",attemptsAfter=" + attemptsAfterPromotion
            + ",pixelMismatch=" + firstMismatch(beforePixels, afterPromotionPixels));
        return false;
      }
      graphics.backColor = 0x00445566;
      graphics.fillRect(0, 0, 1, 1);
      if (before.equals(ImageCompactFormatsBenchmarkSupport.format(image))
          || !ImageCompactFormatsBenchmarkSupport.RGBA8888.equals(
              ImageCompactFormatsBenchmarkSupport.format(image))
          || ImageCompactFormatsBenchmarkSupport.metric("promotionAttemptsForTest")
              != attemptsAfterPromotion) {
        return false;
      }
    }
    return true;
  }

  private static boolean checkDecodeFailureRetry(
      ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures) throws Exception {
    if (!ImageCompactFormatsBenchmarkSupport.metricProbeAvailable(
        "failNextCompactDecodeAfterAllocationForTest")) {
      return true;
    }
    String[] expectedFormats = {
        ImageCompactFormatsBenchmarkSupport.RGB565,
        ImageCompactFormatsBenchmarkSupport.RGB565,
        ImageCompactFormatsBenchmarkSupport.GRAY8,
        ImageCompactFormatsBenchmarkSupport.GRAY8,
        ImageCompactFormatsBenchmarkSupport.ARGB4444
    };
    for (int index = 0; index < fixtures.length; index++) {
      ImageCompactFormatsBenchmarkSupport.configure("post-enabled", "combined-enabled", true);
      Image referenceImage = ImageCompactFormatsBenchmarkSupport.materialize(fixtures[index].bytes);
      int[] expectedPixels = referenceImage.getPixels();
      long beforeBytes = ImageCompactFormatsBenchmarkSupport.metric("backingBytesLiveForTest");
      ImageCompactFormatsBenchmarkSupport.invokeStaticRequired("totalcross.ui.image.Image",
          "failNextCompactDecodeAfterAllocationForTest");
      boolean failed = false;
      try {
        ImageCompactFormatsBenchmarkSupport.materialize(fixtures[index].bytes);
      } catch (Throwable expected) {
        failed = true;
      }
      long afterBytes = ImageCompactFormatsBenchmarkSupport.metric("backingBytesLiveForTest");
      Image retry = ImageCompactFormatsBenchmarkSupport.materialize(fixtures[index].bytes);
      if (!failed || beforeBytes != afterBytes
          || !expectedFormats[index].equals(ImageCompactFormatsBenchmarkSupport.format(retry))
          || !samePixels(expectedPixels, retry.getPixels())) {
        System.out.println("decodeFailureCheck index=" + index + ",failed=" + failed
            + ",beforeBytes=" + beforeBytes + ",afterBytes=" + afterBytes
            + ",format=" + ImageCompactFormatsBenchmarkSupport.format(retry)
            + ",pixelMismatch=" + firstMismatch(expectedPixels, retry.getPixels()));
        return false;
      }
    }
    return true;
  }

  private static boolean checkPromotionFailureRetry(
      ImageCompactFormatsBenchmarkSupport.Fixture[] fixtures) throws Exception {
    if (!ImageCompactFormatsBenchmarkSupport.metricProbeAvailable("failNextPromotionForTest")) {
      return true;
    }
    for (int index : new int[] { 0, 2, 4 }) {
      Image image = ImageCompactFormatsBenchmarkSupport.materialize(fixtures[index].bytes);
      String before = ImageCompactFormatsBenchmarkSupport.format(image);
      int[] beforePixels = image.getPixels();
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
      boolean preserved = before.equals(ImageCompactFormatsBenchmarkSupport.format(image))
          && samePixels(beforePixels, image.getPixels());
      Graphics retry = image.getGraphics();
      if (!failed || !preserved || retry == null
          || !ImageCompactFormatsBenchmarkSupport.RGBA8888.equals(
              ImageCompactFormatsBenchmarkSupport.format(image))) {
        System.out.println("promotionFailureCheck index=" + index + ",failed=" + failed
            + ",preserved=" + preserved + ",format="
            + ImageCompactFormatsBenchmarkSupport.format(image));
        return false;
      }
    }
    return true;
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

  private static boolean checkTranslucentFallback(Image source) throws Exception {
    long fallbacksBefore = ImageCompactFormatsBenchmarkSupport.metric("writePixelsFallbacksForTest");
    Image target = new Image(source.getPixelWidth(), source.getPixelHeight());
    Graphics graphics = target.getGraphics();
    if (graphics == null) {
      return false;
    }
    graphics.drawImage(source, 0, 0, false);
    target.getPixels();
    return ImageCompactFormatsBenchmarkSupport.metric("writePixelsFallbacksForTest")
        > fallbacksBefore;
  }

  private static boolean samePixels(int[] first, int[] second) {
    if (first == null || second == null || first.length != second.length) {
      return false;
    }
    for (int index = 0; index < first.length; index++) {
      if (first[index] != second[index]) {
        return false;
      }
    }
    return true;
  }

  private static int firstMismatch(int[] first, int[] second) {
    if (first == null || second == null || first.length != second.length) {
      return -1;
    }
    for (int index = 0; index < first.length; index++) {
      if (first[index] != second[index]) {
        return index;
      }
    }
    return -1;
  }
}
