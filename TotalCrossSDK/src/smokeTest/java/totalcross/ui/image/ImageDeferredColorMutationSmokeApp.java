// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Deployed macOS smoke for deferred in-place Image color mutations. */
public class ImageDeferredColorMutationSmokeApp extends MainWindow {
  private static final int COLOR = 0xFF804020;
  private static final int COLOR2 = 0xFF4080C0;
  private static final int FADE = 137;
  private static final int TRANSPARENT = 0;
  private static final int CHANGE_FROM = 0xFF000000;
  private static final int CHANGE_TO = 0xFF20A040;

  @Override
  public void initUI() {
    boolean constructionLazy = false;
    boolean applyColorDeferred = false;
    boolean applyColor2Deferred = false;
    boolean applyFadeDeferred = false;
    boolean changeColorsDeferred = false;
    boolean transparentColorDeferred = false;
    boolean chainEquivalence = false;
    boolean multiFrameCompatibility = false;
    boolean cacheInvalidated = false;
    boolean eagerAliasCompatibility = false;
    boolean targetedAfterSmooth = false;
    boolean fallbackBeforeSmooth = false;
    String error = "";

    try {
      byte[] png = Vm.getFile("image-abi/tiny.png");
      require(png != null && png.length > 0, "tiny PNG resource");
      Image constructed = new Image(png);
      constructionLazy = constructed.pipelineForSmoke() != null && constructed.pixels == null
          && constructed.getPixelWidth() == 36 && constructed.getPixelHeight() == 36;
      require(constructionLazy, "deferred construction");

      applyColorDeferred = matchesEager(png, 1);
      applyColor2Deferred = matchesEager(png, 2);
      applyFadeDeferred = matchesEager(png, 3);
      changeColorsDeferred = matchesEager(png, 4);
      transparentColorDeferred = matchesEager(png, 5);
      require(applyColorDeferred && applyColor2Deferred && applyFadeDeferred && changeColorsDeferred
          && transparentColorDeferred, "deferred mutation equivalence");

      Image expectedChain = new Image(png);
      expectedChain.getPixels();
      expectedChain.applyFade(FADE);
      expectedChain.applyColor(COLOR);
      expectedChain.changeColors(CHANGE_FROM, CHANGE_TO);
      expectedChain.setTransparentColor(TRANSPARENT);
      expectedChain.applyColor2(COLOR2);
      Image actualChain = new Image(png);
      actualChain.applyFade(FADE);
      actualChain.applyColor(COLOR);
      actualChain.changeColors(CHANGE_FROM, CHANGE_TO);
      actualChain.setTransparentColor(TRANSPARENT);
      actualChain.applyColor2(COLOR2);
      chainEquivalence = actualChain.pipelineForSmoke() != null
          && samePixels(expectedChain, actualChain);
      require(chainEquivalence, "mutation order");

      Image cachedSource = new Image(24, 20);
      fill(cachedSource, 0xFF204060);
      Image cached = cachedSource.getSmoothScaledInstance(18, 18);
      Image oldVariant = cached.resolveForDrawing(1);
      int[] oldPixels = oldVariant.getPixels().clone();
      cached.applyColor(0xFFFFFFFF);
      Image newVariant = cached.resolveForDrawing(1);
      cacheInvalidated = cached.pipelineForSmoke().cachedVariantCountForSmoke() == 1
          && oldVariant != newVariant && sameArray(oldPixels, oldVariant.getPixels())
          && !sameArray(oldPixels, newVariant.getPixels());
      require(cacheInvalidated, "mutation cache invalidation");

      Image eagerAlias = new Image(4, 2);
      int[] alias = eagerAlias.getPixels();
      eagerAlias.applyColor(COLOR);
      eagerAliasCompatibility = eagerAlias.pipelineForSmoke() == null && alias == eagerAlias.getPixels();
      require(eagerAliasCompatibility, "materialized alias compatibility");

      multiFrameCompatibility = multiFrameMatches(1) && multiFrameMatches(2) && multiFrameMatches(3)
          && multiFrameMatches(4) && multiFrameMatches(5);
      require(multiFrameCompatibility, "multi-frame compatibility");

      byte[] jpeg = createJpeg(1024, 768);
      Image.resetTargetedDecodeInvocationCountForTest();
      Image smoothThenColor = new Image(jpeg).getSmoothScaledInstance(64, 48);
      smoothThenColor.applyColor(COLOR);
      Image smoothResult = smoothThenColor.resolveForDrawing(1);
      targetedAfterSmooth = Image.targetedDecodeInvocationCountForTest() == 1
          && smoothResult.getPixelWidth() == 64 && smoothResult.getPixelHeight() == 48;
      require(targetedAfterSmooth, "JPEG targeted decode after smooth scale");

      Image.resetTargetedDecodeInvocationCountForTest();
      Image colorThenSmooth = new Image(jpeg);
      colorThenSmooth.applyColor(COLOR);
      Image fallbackResult = colorThenSmooth.getSmoothScaledInstance(64, 48).resolveForDrawing(1);
      fallbackBeforeSmooth = Image.targetedDecodeInvocationCountForTest() == 0
          && fallbackResult.getPixelWidth() == 64 && fallbackResult.getPixelHeight() == 48;
      require(fallbackBeforeSmooth, "JPEG full decode before smooth scale");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = constructionLazy && applyColorDeferred && applyColor2Deferred && applyFadeDeferred
        && changeColorsDeferred && transparentColorDeferred && chainEquivalence && multiFrameCompatibility
        && cacheInvalidated && eagerAliasCompatibility && targetedAfterSmooth && fallbackBeforeSmooth;
    System.out.println("fixture=ImageDeferredColorMutationSmokeApp,constructionLazy=" + constructionLazy
        + ",applyColorDeferred=" + applyColorDeferred + ",applyColor2Deferred=" + applyColor2Deferred
        + ",applyFadeDeferred=" + applyFadeDeferred + ",changeColorsDeferred=" + changeColorsDeferred
        + ",transparentColorDeferred=" + transparentColorDeferred + ",chainEquivalence=" + chainEquivalence
        + ",multiFrameCompatibility=" + multiFrameCompatibility + ",cacheInvalidated=" + cacheInvalidated
        + ",eagerAliasCompatibility=" + eagerAliasCompatibility + ",targetedAfterSmooth="
        + targetedAfterSmooth + ",fallbackBeforeSmooth=" + fallbackBeforeSmooth + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static boolean matchesEager(byte[] encoded, int operation) throws Exception {
    Image expected = new Image(encoded);
    expected.getPixels();
    apply(expected, operation);
    Image actual = new Image(encoded);
    apply(actual, operation);
    boolean deferred = actual.pipelineForSmoke() != null && actual.pixels == null;
    return deferred && samePixels(expected, actual);
  }

  private static boolean multiFrameMatches(int operation) throws Exception {
    Image expected = framed().getSmoothScaledInstance(2, 1);
    expected.getPixels();
    apply(expected, operation);
    Image actual = framed().getSmoothScaledInstance(2, 1);
    apply(actual, operation);
    return actual.pipelineForSmoke() != null && actual.getCurrentFrame() == 0
        && sameFrames(expected, actual);
  }

  private static void apply(Image image, int operation) {
    switch (operation) {
    case 1:
      image.applyColor(COLOR);
      break;
    case 2:
      image.applyColor2(COLOR2);
      break;
    case 3:
      image.applyFade(FADE);
      break;
    case 4:
      image.changeColors(CHANGE_FROM, CHANGE_TO);
      break;
    case 5:
      image.setTransparentColor(TRANSPARENT);
      break;
    default:
      throw new IllegalArgumentException("operation=" + operation);
    }
  }

  private static Image framed() throws Exception {
    Image image = new Image(8, 2);
    int[] first = image.getPixels();
    for (int i = 0; i < first.length; i++) {
      first[i] = 0xFF000000;
    }
    image.setFrameCount(2);
    image.setCurrentFrame(1);
    int[] second = image.getPixels();
    for (int i = 0; i < second.length; i++) {
      second[i] = 0xFF0000FF;
    }
    image.setCurrentFrame(0);
    return image;
  }

  private static boolean sameFrames(Image expected, Image actual) throws Exception {
    if (expected.getFrameCount() != actual.getFrameCount()) {
      return false;
    }
    for (int frame = 0; frame < expected.getFrameCount(); frame++) {
      expected.setCurrentFrame(frame);
      actual.setCurrentFrame(frame);
      if (!samePixels(expected, actual)) {
        return false;
      }
    }
    return true;
  }

  private static boolean samePixels(Image expected, Image actual) throws Exception {
    return expected.getPixelWidth() == actual.getPixelWidth()
        && expected.getPixelHeight() == actual.getPixelHeight()
        && sameArray(expected.getPixels(), actual.getPixels());
  }

  private static boolean sameArray(int[] first, int[] second) {
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

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static void fill(Image image, int pixel) {
    int[] pixels = image.getPixels();
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = pixel;
    }
  }

  private static byte[] createJpeg(int width, int height) throws Exception {
    Image image = new Image(width, height);
    fill(image, 0xFF204060);
    ByteArrayStream stream = new ByteArrayStream(width * height);
    image.createJpg(stream, 80);
    byte[] jpeg = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, jpeg, 0, jpeg.length);
    return jpeg;
  }
}
