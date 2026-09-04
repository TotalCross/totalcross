// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.net.Base64;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Deployed macOS smoke for deferred Image frame state and frame layout. */
public class ImageDeferredFrameStateSmokeApp extends MainWindow {
  private static final int FRAME_A = 0xFF102030;
  private static final int FRAME_B = 0xFF405060;

  @Override
  public void initUI() {
    boolean currentFrameDeferred = false;
    boolean currentFrameWrap = false;
    boolean cachedVariantReused = false;
    boolean cachedVariantFrameUpdated = false;
    boolean canonicalSelectedFrame = false;
    boolean cropCapturesSelectedFrame = false;
    boolean explicitFrameExtraction = false;
    boolean colorFrameSemantics = false;
    boolean fadeCallTimeFrame = false;
    boolean fadeOrdering = false;
    boolean fadeVisibleFrameOrdering = false;
    boolean hidpiFrameLayout = false;
    boolean croppedHidpiFrameLayout = false;
    boolean croppedFractionalFrameLayout = false;
    boolean exactFractionalScale11 = false;
    boolean exactFractionalScale125 = false;
    boolean exactFractionalScale175 = false;
    boolean croppedExactFractionalScale = false;
    boolean frameSelectedExactFractionalScale = false;
    boolean zeroWidthFrameCompatibility = false;
    boolean frameLayoutDeferred = false;
    boolean frameLayoutMetadata = false;
    boolean frameLayoutPixels = false;
    boolean frameLayoutRoundTrip = false;
    boolean frameLayoutRetryable = false;
    String error = "";

    try {
      byte[] frames = encodedFrames();
      Image source = new Image(frames);
      source.setCurrentFrame(-1);
      currentFrameDeferred = source.pipelineForSmoke() != null && source.pixels == null
          && source.getCurrentFrame() == 1;
      source.nextFrame();
      source.prevFrame();
      currentFrameWrap = source.getCurrentFrame() == 1;

      Image scaled = new Image(frames).getSmoothScaledInstance(4, 2);
      Image oneX = scaled.resolveForDrawing(1);
      Image twoX = scaled.resolveForDrawing(2);
      int firstFramePixel = oneX.getPixels()[0];
      Image selectedOneX = oneX;
      Image selectedTwoX = twoX;
      scaled.setCurrentFrame(1);
      cachedVariantReused = selectedOneX == scaled.resolveForDrawing(1)
          && selectedTwoX == scaled.resolveForDrawing(2);
      int oneFramePixel = oneX.getPixels()[0];
      int twoFramePixel = twoX.getPixels()[0];
      cachedVariantFrameUpdated = firstFramePixel != oneFramePixel && firstFramePixel != twoFramePixel;
      require(cachedVariantReused && cachedVariantFrameUpdated, "cached frame synchronization");

      Image canonical = new Image(frames);
      canonical.setCurrentFrame(1);
      Image expectedCanonical = new Image(frames).getFrameInstance(1);
      canonicalSelectedFrame = samePixels(expectedCanonical, canonical);
      require(canonicalSelectedFrame, "canonical selected frame");

      Image cropSource = new Image(frames);
      cropSource.setCurrentFrame(1);
      Image crop = cropSource.getClippedInstance(0, 0, 2, 1);
      cropSource.setCurrentFrame(0);
      Image expectedCrop = new Image(frames).getFrameInstance(1).getClippedInstance(0, 0, 2, 1);
      cropCapturesSelectedFrame = samePixels(expectedCrop, crop);
      explicitFrameExtraction = samePixels(new Image(frames).getFrameInstance(0),
          new Image(frames).getFrameInstance(0));
      require(cropCapturesSelectedFrame && explicitFrameExtraction, "frame extraction isolation");

      Image color = new Image(frames);
      color.setCurrentFrame(1);
      color.applyColor(0xFF804020);
      boolean colorReset = color.getCurrentFrame() == 0 && color.pipelineForSmoke() != null;
      Image fadeExpected = new Image(frames);
      fadeExpected.getPixels();
      fadeExpected.applyFade(137);
      Image fade = new Image(frames);
      fade.applyFade(137);
      boolean fadeSelected = samePixels(fadeExpected, fade) && fade.getCurrentFrame() == 0;
      Image transparent = new Image(frames);
      transparent.setCurrentFrame(1);
      transparent.setTransparentColor(0x102030);
      colorFrameSemantics = colorReset && fadeSelected && transparent.getCurrentFrame() == 1;
      require(colorFrameSemantics, "color frame semantics");

      Image fadeExpectedFirst = new Image(frames);
      fadeExpectedFirst.getPixels();
      fadeExpectedFirst.applyFade(137);
      fadeExpectedFirst.setCurrentFrame(1);
      Image fadeActualFirst = new Image(frames);
      fadeActualFirst.applyFade(137);
      fadeActualFirst.setCurrentFrame(1);
      fadeCallTimeFrame = samePixels(fadeExpectedFirst, fadeActualFirst) && fadeActualFirst.getCurrentFrame() == 1;

      Image fadeExpectedOrdering = new Image(frames);
      fadeExpectedOrdering.getPixels();
      fadeExpectedOrdering.setCurrentFrame(1);
      fadeExpectedOrdering.applyFade(137);
      fadeExpectedOrdering.setCurrentFrame(0);
      fadeExpectedOrdering.applyFade(137);
      fadeExpectedOrdering.setCurrentFrame(1);
      Image fadeActualOrdering = new Image(frames);
      fadeActualOrdering.setCurrentFrame(1);
      fadeActualOrdering.applyFade(137);
      fadeActualOrdering.setCurrentFrame(0);
      fadeActualOrdering.applyFade(137);
      fadeActualOrdering.setCurrentFrame(1);
      fadeOrdering = samePixels(fadeExpectedOrdering, fadeActualOrdering)
          && fadeActualOrdering.getCurrentFrame() == 1;
      fadeVisibleFrameOrdering = fadeCallTimeFrame && fadeOrdering;
      require(fadeVisibleFrameOrdering, "fade call-time ordering");

      Image layout = new Image(encodedStrip());
      layout.setFrameCount(2);
      frameLayoutDeferred = layout.pipelineForSmoke() != null && layout.pixels == null;
      frameLayoutMetadata = layout.getFrameCount() == 2 && layout.getWidth() == 2
          && "FC=2".equals(layout.comment);
      Image layoutVariant = layout.resolveForDrawing(1);
      int[] first = layoutVariant.getPixels().clone();
      layout.setCurrentFrame(1);
      boolean secondFrame = layout.resolveForDrawing(1) == layoutVariant
          && layoutVariant.getPixels()[0] != first[0];
      frameLayoutPixels = layoutVariant.getPixelWidth() == 2 && secondFrame;
      require(frameLayoutDeferred && frameLayoutMetadata && frameLayoutPixels, "frame layout");

      Image scaledLayout = new Image(encodedStrip()).getSmoothScaledInstance(3, 2);
      scaledLayout.setFrameCount(2);
      Image scaledLayoutVariant = scaledLayout.resolveForDrawing(2);
      frameLayoutMetadata = frameLayoutMetadata && scaledLayout.getWidth() == 1
          && scaledLayoutVariant.getWidth() == 1 && scaledLayoutVariant.getPixelWidth() == 2;

      Image roundTripSource = new Image(encodedStrip());
      roundTripSource.setFrameCount(2);
      ByteArrayStream output = new ByteArrayStream(256);
      roundTripSource.createPng(output);
      Image roundTrip = new Image(copy(output));
      EncodedImageSource roundTripEncoded = (EncodedImageSource) roundTrip.pipelineForSmoke().root();
      frameLayoutRoundTrip = roundTrip.getFrameCount() == 2 && roundTripEncoded.getIntrinsicWidth() == 5
          && "FC=2".equals(roundTripEncoded.getComment());
      require(frameLayoutRoundTrip, "frame layout round trip");

      hidpiFrameLayout = highDensityFrameLayout(2.0) && highDensityFrameLayout(1.5);
      require(hidpiFrameLayout, "high-density frame layout");
      croppedHidpiFrameLayout = croppedHighDensityFrameLayout(2.0);
      croppedFractionalFrameLayout = croppedHighDensityFrameLayout(1.5);
      require(croppedHidpiFrameLayout && croppedFractionalFrameLayout, "cropped high-density frame layout");
      exactFractionalScale11 = exactFractionalFrameLayout(1.1, 6, false);
      exactFractionalScale125 = exactFractionalFrameLayout(1.25, 6, false);
      exactFractionalScale175 = exactFractionalFrameLayout(1.75, 5, false);
      frameSelectedExactFractionalScale = exactFractionalScale11 && exactFractionalScale125
          && exactFractionalScale175;
      croppedExactFractionalScale = exactFractionalFrameLayout(1.1, 6, true)
          && exactFractionalFrameLayout(1.25, 6, true) && exactFractionalFrameLayout(1.75, 5, true);
      require(frameSelectedExactFractionalScale && croppedExactFractionalScale,
          "exact fractional frame layout");

      Image zeroExpected = new Image(1, 2);
      fill(zeroExpected, FRAME_A);
      zeroExpected.setFrameCount(2);
      Image zeroActual = new Image(encodedZeroStrip());
      zeroActual.setFrameCount(2);
      Image zeroResolved = zeroActual.resolveForDrawing(1);
      zeroActual.setCurrentFrame(1);
      zeroWidthFrameCompatibility = zeroActual.getWidth() == 0 && zeroActual.getPixelWidth() == 0
          && zeroResolved.getPixels().length == 0 && sameRows(zeroExpected, zeroActual, 1);
      require(zeroWidthFrameCompatibility, "zero-width frame compatibility");

      Image retry = new Image(encodedStrip());
      retry.setFrameCount(2);
      EncodedImageSource retryEncoded = (EncodedImageSource) retry.pipelineForSmoke().root();
      Image.failNextMaterializedFrameBufferAllocationForTest();
      try {
        retry.resolveForDrawing(1);
      } catch (TransientImageMaterializationException expected) {
        // The failed visible allocation must leave the deferred source retryable.
      }
      frameLayoutRetryable = retry.pipelineForSmoke() != null && retryEncoded.decodeFailure() == null
          && retry.resolveForDrawing(1) != null;
      require(frameLayoutRetryable, "retryable frame layout allocation");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = currentFrameDeferred && currentFrameWrap && cachedVariantReused
        && cachedVariantFrameUpdated && canonicalSelectedFrame && cropCapturesSelectedFrame
        && explicitFrameExtraction && colorFrameSemantics && fadeCallTimeFrame && fadeOrdering
        && fadeVisibleFrameOrdering && hidpiFrameLayout && croppedHidpiFrameLayout
        && croppedFractionalFrameLayout && exactFractionalScale11 && exactFractionalScale125
        && exactFractionalScale175 && croppedExactFractionalScale && frameSelectedExactFractionalScale
        && zeroWidthFrameCompatibility && frameLayoutDeferred && frameLayoutMetadata
        && frameLayoutPixels && frameLayoutRoundTrip && frameLayoutRetryable;
    System.out.println("fixture=ImageDeferredFrameStateSmokeApp,currentFrameDeferred=" + currentFrameDeferred
        + ",currentFrameWrap=" + currentFrameWrap + ",cachedVariantReused=" + cachedVariantReused
        + ",cachedVariantFrameUpdated=" + cachedVariantFrameUpdated + ",canonicalSelectedFrame="
        + canonicalSelectedFrame + ",cropCapturesSelectedFrame=" + cropCapturesSelectedFrame
        + ",explicitFrameExtraction=" + explicitFrameExtraction + ",colorFrameSemantics=" + colorFrameSemantics
        + ",fadeCallTimeFrame=" + fadeCallTimeFrame + ",fadeOrdering=" + fadeOrdering
        + ",fadeVisibleFrameOrdering=" + fadeVisibleFrameOrdering + ",hidpiFrameLayout=" + hidpiFrameLayout
        + ",croppedHidpiFrameLayout=" + croppedHidpiFrameLayout + ",croppedFractionalFrameLayout="
        + croppedFractionalFrameLayout + ",exactFractionalScale11=" + exactFractionalScale11
        + ",exactFractionalScale125=" + exactFractionalScale125 + ",exactFractionalScale175="
        + exactFractionalScale175 + ",croppedExactFractionalScale=" + croppedExactFractionalScale
        + ",frameSelectedExactFractionalScale=" + frameSelectedExactFractionalScale
        + ",zeroWidthFrameCompatibility=" + zeroWidthFrameCompatibility
        + ",frameLayoutDeferred=" + frameLayoutDeferred + ",frameLayoutMetadata=" + frameLayoutMetadata
        + ",frameLayoutPixels=" + frameLayoutPixels + ",frameLayoutRoundTrip=" + frameLayoutRoundTrip
        + ",frameLayoutRetryable=" + frameLayoutRetryable + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static byte[] encodedFrames() throws Exception {
    return Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAgAAAACCAYAAABllJ3tAAAADHRFWHRDb21tZW50AEZDPTLAxqe/AAAAFklEQVR4nGMQUDD4j4wdAhJQMAMhBQAwNxpx1IOH/QAAAABJRU5ErkJggg==");
  }

  private static byte[] encodedStrip() throws Exception {
    return Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAUAAAACCAYAAACQahZdAAAAIElEQVR4nGP4z8jE/J+JmeU/MwvrfxZWtv+sbOwM2AQB6OEKbzVpD/sAAAAASUVORK5CYII=");
  }

  private static byte[] encodedZeroStrip() throws Exception {
    return Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAD0lEQVR4nGP4L6BgwAAiABAdAr80JfcXAAAAAElFTkSuQmCC");
  }

  private static byte[] copy(ByteArrayStream stream) {
    byte[] result = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, result, 0, result.length);
    return result;
  }

  private static boolean samePixels(Image expected, Image actual) throws Exception {
    int[] first = expected.getPixels();
    int[] second = actual.getPixels();
    if (first.length != second.length) {
      return false;
    }
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) {
        return false;
      }
    }
    return true;
  }

  private static boolean sameRows(Image expected, Image actual) throws Exception {
    return sameRows(expected, actual, expected.getPixelWidth() * expected.getFrameCount());
  }

  private static boolean sameRows(Image expected, Image actual, int fullWidth) throws Exception {
    byte[] first = new byte[fullWidth * 4];
    byte[] second = new byte[fullWidth * 4];
    for (int y = 0; y < expected.getPixelHeight(); y++) {
      expected.getPixelRow(first, y);
      actual.getPixelRow(second, y);
      for (int i = 0; i < first.length; i++) {
        if (first[i] != second[i]) {
          return false;
        }
      }
    }
    return true;
  }

  private static boolean highDensityFrameLayout(double contentScale) throws Exception {
    Image expected = Image.createLogical(4, 2, contentScale);
    fillStrip(expected);
    expected.setFrameCount(2);
    Image source = Image.createLogical(4, 2, contentScale);
    fillStrip(source);
    Image actual = source.getFrameInstance(0);
    actual.setFrameCount(2);
    boolean metadata = actual.pipelineForSmoke() != null && actual.pixels == null
        && actual.getPixelWidth() == expected.getPixelWidth()
        && actual.getWidth() == expected.getWidth();
    boolean first = samePixels(expected, actual);
    expected.setCurrentFrame(1);
    actual.setCurrentFrame(1);
    boolean second = samePixels(expected, actual);
    require(metadata && first && second, "high-density frame layout");
    return true;
  }

  private static boolean croppedHighDensityFrameLayout(double contentScale) throws Exception {
    Image expectedSource = Image.createLogical(4, 2, contentScale);
    fillStrip(expectedSource);
    Image expected = expectedSource.getClippedInstance(0, 0, 4, 2);
    expected.getPixels();
    expected.setFrameCount(2);

    Image source = Image.createLogical(4, 2, contentScale);
    fillStrip(source);
    Image actual = source.getClippedInstance(0, 0, 4, 2);
    actual.setFrameCount(2);
    ImagePipeline layout = actual.pipelineForSmoke();
    boolean metadata = layout != null && actual.pixels == null && actual.getPixelWidth() == expected.getPixelWidth()
        && actual.getWidth() == expected.getWidth() && actual.getContentScale() == expected.getContentScale()
        && layout.widthOfAllFrames() == expected.getPixelWidth() * 2;
    boolean first = samePixels(expected, actual);
    expected.setCurrentFrame(1);
    actual.setCurrentFrame(1);
    boolean second = samePixels(expected, actual);
    boolean canonical = actual.pipelineForSmoke() == null;
    require(metadata && first && second && canonical, "cropped high-density frame layout");
    return true;
  }

  private static boolean exactFractionalFrameLayout(double contentScale, int logicalWidth, boolean cropped)
      throws Exception {
    int fullPhysicalWidth = (int) Math.ceil(logicalWidth * contentScale);
    Image expectedSource = Image.createLogical(logicalWidth, 2, contentScale);
    fillStrip(expectedSource);
    Image expected = cropped ? expectedSource.getClippedInstance(0, 0, logicalWidth, 2)
        : expectedSource.getFrameInstance(0);
    boolean roundedSource = expectedSource.getPixelWidth() == fullPhysicalWidth;
    int fullWidth = expected.getPixelWidth();
    expected.getPixels();
    expected.setFrameCount(2);

    Image source = Image.createLogical(logicalWidth, 2, contentScale);
    fillStrip(source);
    boolean actualRoundedSource = source.getPixelWidth() == fullPhysicalWidth;
    Image actual = cropped ? source.getClippedInstance(0, 0, logicalWidth, 2) : source.getFrameInstance(0);
    actual.setFrameCount(2);
    ImagePipeline layout = actual.pipelineForSmoke();
    boolean metadata = layout != null && actual.pixels == null && actual.getFrameCount() == 2
        && actual.getContentScale() == contentScale && actual.getPixelWidth() == fullWidth / 2
        && actual.getWidth() == (int) Math.ceil(actual.getPixelWidth() / contentScale)
        && layout.widthOfAllFrames() == fullWidth && roundedSource && actualRoundedSource;
    boolean first = samePixels(expected, actual);
    expected.setCurrentFrame(1);
    actual.setCurrentFrame(1);
    boolean second = samePixels(expected, actual);
    boolean stable = actual.getContentScale() == contentScale && actual.getPixelWidth() == expected.getPixelWidth()
        && actual.getWidth() == expected.getWidth();
    return metadata && first && second && stable && actual.pipelineForSmoke() == null;
  }

  private static void fillStrip(Image image) {
    int width = image.getPixelWidth();
    int[] pixels = image.getPixels();
    for (int y = 0; y < image.getPixelHeight(); y++) {
      for (int x = 0; x < width; x++) {
        pixels[y * width + x] = 0xFF000000 | ((x + 1) << 16) | ((y + 2) << 8) | x + 3;
      }
    }
  }

  private static void fill(Image image, int pixel) {
    int[] pixels = image.getPixels();
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = pixel;
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
