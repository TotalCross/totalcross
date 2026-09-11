// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

import static totalcross.ui.image.ImageNativeColorFilterSmokeSupport.*;

/** Deployed macOS smoke for native color mutations and mixed pipelines. */
public class ImageNativeColorFilterSmokeApp extends MainWindow {
  private static int[] applyColor2Pixels(Image image, int color, boolean direct) throws Exception {
    ImageOptimizationSettings.resetForTest();
    if (direct) {
      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_DIRECT_COLOR_MATERIALIZATION,
          ImageOptimizationSettings.ENABLED);
    }
    image.applyColor2(color);
    return image.getPixels();
  }

  @Override
  public void initUI() {
    boolean nativeBacking = false;
    boolean frameScopedFade = false;
    boolean alpha = false;
    boolean faded = false;
    boolean applyColor2 = false;
    boolean directColorParity = false;
    boolean applyColor2Cache = false;
    boolean touchUp = false;
    boolean applyColor = false;
    boolean exactColors = false;
    boolean mixedPipeline = false;
    boolean touchUpThreshold = false;
    boolean drawColorStages = false;
    boolean drawInPlaceStages = false;
    boolean drawPrefixStages = false;
    boolean encodedSourceCache = false;
    boolean drawPlanGeneration = false;
    boolean drawAndSave = false;
    int drawDirectCount = -1;
    int drawColorReadbackCount = -1;
    String error = "";
    try {
      Image source = sourceImage();
      nativeBacking = source.hasNativeBackingForSmoke();
      require(nativeBacking, "source native backing");

      Image fadedFrame = frameStripImage();
      fadedFrame.setFrameCount(2);
      fadedFrame.setCurrentFrame(1);
      fadedFrame.applyFade(128);
      int[] frameOne = fadedFrame.getPixels();
      fadedFrame.setCurrentFrame(0);
      int[] frameZero = fadedFrame.getPixels();
      frameScopedFade = sameArray(frameOne, fadedFramePixels(1, 128))
          && sameArray(frameZero, fadedFramePixels(0, 255))
          && fadedFrame.hasNativeBackingForSmoke();
      require(frameScopedFade, "frame-scoped fade");

      Image alphaResult = sourceImage().getAlphaInstance(-40);
      require(alphaResult.hasNativeBackingForSmoke(),
          "alpha native result backing");
      int[] alphaPixels = alphaResult.getPixels();
      alpha = alphaPixels[0] == 0xD7102030 && alphaPixels[3] == 0xD7E0F000;
      require(alpha, "alpha mapping");

      Image fadedResult = sourceImage().getFadedInstance(0xFF204060);
      require(fadedResult.hasNativeBackingForSmoke(),
          "fade native result backing");
      int[] fadedPixels = fadedResult.getPixels();
      faded = fadedPixels[0] == 0xFF183048 && fadedPixels[3] == 0xFF809830;
      require(faded, "fade mapping");

      Image colorImage = sourceImage();
      colorImage.applyColor(0xFF804020);
      int[] colorPixels = colorImage.getPixels();
      applyColor = colorPixels[0] == applyColor(SOURCE[0], 0xFF804020)
          && (colorPixels[0] & 0xFF000000) == (SOURCE[0] & 0xFF000000);
      require(applyColor, "applyColor mapping");

      Image color2Image = sourceImage();
      color2Image.applyColor2(0xAA4080C0);
      int[] color2Pixels = color2Image.getPixels();
      applyColor2 = color2Pixels[0] == applyColor2(SOURCE[0], 0xAA4080C0)
          && color2Pixels[3] == applyColor2(SOURCE[3], 0xAA4080C0);
      require(applyColor2, "applyColor2 mapping");

      int[] disabledNormal = applyColor2Pixels(alphaImage(), 0x204080C0, false);
      int[] enabledNormal = applyColor2Pixels(alphaImage(), 0x204080C0, true);
      int[] disabledAlpha = applyColor2Pixels(alphaImage(), 0xAA4080C0, false);
      int[] enabledAlpha = applyColor2Pixels(alphaImage(), 0xAA4080C0, true);
      directColorParity = sameArray(disabledNormal, enabledNormal)
          && sameArray(disabledAlpha, enabledAlpha);
      require(directColorParity, "direct applyColor2 parity");
      ImageOptimizationSettings.resetForTest();

      Image changed = sourceImage();
      changed.changeColors(SOURCE[0], 0xFFAABBCC);
      int[] changedPixels = changed.getPixels();
      Image transparent = sourceImage();
      transparent.setTransparentColor(SOURCE[0] & 0x00FFFFFF);
      int[] transparentPixels = transparent.getPixels();
      Image opaque = sourceImage();
      opaque.setTransparentColor(-1);
      int[] opaquePixels = opaque.getPixels();
      exactColors = changedPixels[0] == 0xFFAABBCC
          && changedPixels[1] == SOURCE[1]
          && transparentPixels[0] == (SOURCE[0] & 0x00FFFFFF)
          && (transparentPixels[0] >>> 24) == 0
          && (transparentPixels[1] >>> 24) == 0xFF
          && opaquePixels[0] == SOURCE[0];
      require(exactColors, "exact color mutations");

      Image mixed = sourceImage().getClippedInstance(0, 0, WIDTH, HEIGHT)
          .getSmoothScaledInstance(8, 2);
      mixed.applyFade(128);
      mixed.changeColors(fade(SOURCE[0], 128), 0xFF010203);
      Image mixedResult = mixed.getRotatedScaledInstance(100, 90, 0).getAlphaInstance(-20);
      int[] mixedPixels = mixedResult.getPixels();
      ByteArrayStream mixedEncoded = new ByteArrayStream(512);
      mixedResult.createPng(mixedEncoded);
      Image mixedSaved = new Image(mixedEncoded.getBuffer(), mixedEncoded.getPos());
      Image mixedDestination = new Image(mixedResult.getWidth(), mixedResult.getHeight());
      mixedDestination.getGraphics().drawImage(mixedResult, 0, 0);
      mixedPipeline = sameArray(mixedPixels, mixedSaved.getPixels())
          && sameArray(mixedPixels, mixedDestination.getPixels());
      require(mixedPipeline, "mixed native pipeline");

      Image chained = sourceImage();
      chained.applyColor(0xFF804020);
      chained.applyColor2(0xAA4080C0);
      chained.setTransparentColor(0x010203);
      ByteArrayStream chainedEncoded = new ByteArrayStream(512);
      chained.createPng(chainedEncoded);
      Image chainedSaved = new Image(chainedEncoded.getBuffer(), chainedEncoded.getPos());
      Image chainedDestination = new Image(WIDTH, HEIGHT);
      chainedDestination.getGraphics().drawImage(chained, 0, 0);
      drawAndSave = sameArray(chained.getPixels(), chainedSaved.getPixels())
          && sameArray(chained.getPixels(), chainedDestination.getPixels());

      Image touched = sourceImage().getTouchedUpInstance((byte) 20, (byte) -10);
      require(touched.hasNativeBackingForSmoke(),
          "touch-up native result backing");
      int[] touchedPixels = touched.getPixels();
      require(touchedPixels.length == SOURCE.length, "touch-up output dimensions");
      touchUp = true;

      int[] expectedBrightness = { 0xFFDFEF00, 0xFFE0F000, 0xFFE1F100, 0xFFE3F300 };
      for (int brightness = -1; brightness <= 2; brightness++) {
        Image threshold = sourceImage().getTouchedUpInstance((byte) brightness, (byte) 0);
        require(threshold.getPixels()[3] == expectedBrightness[brightness + 1],
            "touch-up brightness=" + brightness);
      }
      Image thresholdContrast = sourceImage().getTouchedUpInstance((byte) 1, (byte) 1);
      touchUpThreshold = thresholdContrast.getPixels()[3] == 0xFFE2F200;
      require(touchUpThreshold, "touch-up brightness threshold with contrast");

      ByteArrayStream drawEncoded = new ByteArrayStream(512);
      sourceImage().createPng(drawEncoded);
      byte[] drawBytes = drawEncoded.getBuffer();
      int drawLength = drawEncoded.getPos();
      Image expectedTouch = new Image(drawBytes, drawLength)
          .getTouchedUpInstance((byte) 20, (byte) -10);
      Image expectedFade = new Image(drawBytes, drawLength)
          .getFadedInstance(0xFF204060);
      Image expectedAlpha = new Image(drawBytes, drawLength)
          .getAlphaInstance(-40);
      Image expectedTouchThenRotate = new Image(drawBytes, drawLength)
          .getTouchedUpInstance((byte) 20, (byte) -10)
          .getRotatedScaledInstance(100, 90, 0xFF102030);
      Image expectedRotateThenTouch = new Image(drawBytes, drawLength)
          .getRotatedScaledInstance(100, 90, 0xFF102030)
          .getTouchedUpInstance((byte) 20, (byte) -10);
      int[] expectedTouchPixels = expectedTouch.getPixels();
      int[] expectedFadePixels = expectedFade.getPixels();
      int[] expectedAlphaPixels = expectedAlpha.getPixels();
      int[] expectedTouchThenRotatePixels = expectedTouchThenRotate.getPixels();
      int[] expectedRotateThenTouchPixels = expectedRotateThenTouch.getPixels();
      Image expectedApplyColor = new Image(drawBytes, drawLength);
      expectedApplyColor.applyColor(0xFF804020);
      int[] expectedApplyColorPixels = expectedApplyColor.getPixels();
      Image expectedApplyFade = new Image(drawBytes, drawLength);
      expectedApplyFade.applyFade(137);
      int[] expectedApplyFadePixels = expectedApplyFade.getPixels();
      Image expectedApplyColor2 = new Image(drawBytes, drawLength);
      expectedApplyColor2.applyColor2(0xAA4080C0);
      int[] expectedApplyColor2Pixels = expectedApplyColor2.getPixels();
      Image expectedCapturedFrame = deferredFrameStripImage();
      expectedCapturedFrame.setCurrentFrame(1);
      expectedCapturedFrame.applyFade(128);
      int[] expectedCapturedFramePixels = expectedCapturedFrame.getPixels();
      Image expectedUncapturedFrame = deferredFrameStripImage();
      expectedUncapturedFrame.setCurrentFrame(0);
      int[] expectedUncapturedFramePixels = expectedUncapturedFrame.getPixels();
      Image expectedColorBeforeSmooth = new Image(drawBytes, drawLength)
          .getTouchedUpInstance((byte) 20, (byte) -10)
          .getSmoothScaledInstance(3, 2);
      Image expectedSmoothThenColor = new Image(drawBytes, drawLength)
          .getSmoothScaledInstance(3, 2)
          .getTouchedUpInstance((byte) 20, (byte) -10);
      Image expectedTwoRotations = new Image(drawBytes, drawLength)
          .getRotatedScaledInstance(100, 90, 0xFF102030)
          .getRotatedScaledInstance(100, 90, 0xFF405060);
      int[] expectedColorBeforeSmoothPixels = expectedColorBeforeSmooth.getPixels();
      int[] expectedSmoothThenColorPixels = expectedSmoothThenColor.getPixels();
      int[] expectedTwoRotationsPixels = expectedTwoRotations.getPixels();

      Image.resetImageOperationAccountingForTest();
      Image encodedCacheRoot = new Image(drawBytes, drawLength);
      Image encodedCacheScale = encodedCacheRoot.getScaledInstance(2, 1);
      Image encodedCacheRotate = encodedCacheRoot.getRotatedScaledInstance(100, 90, 0xFF102030);
      drawToPixels(encodedCacheScale);
      int sourceDecodeAfterFirstSibling = Image.fullDecodeInvocationCountForTest();
      drawToPixels(encodedCacheRotate);
      int sourceDecodeAfterSecondSibling = Image.fullDecodeInvocationCountForTest();
      encodedSourceCache = sourceDecodeAfterFirstSibling == 1 && sourceDecodeAfterSecondSibling == 1;
      require(encodedSourceCache, "encoded source backing cache");

      Image pinch = new Image(createJpeg(1024, 768)).getSmoothScaledInstance(205, 154);
      EncodedImageSource pinchSource = (EncodedImageSource) pinch.pipelineForSmoke().root();
      Image low = pinch.resolveForDrawing(1);
      Image high = pinch.resolveForDrawing(2);
      Image lowAgain = pinch.resolveForDrawing(1);
      ImageDrawPlan lowPlan = (ImageDrawPlan) pinch.drawPlanForDrawing(1);
      drawPlanGeneration = low.getPixelWidth() == 205 && high.getPixelWidth() == 410
          && lowAgain.getPixelWidth() == 205 && lowAgain != low
          && pinchSource.decodedDenominator() == 2 && pinchSource.decodedGeneration() == 2
          && lowPlan.sourceDecodeGeneration == pinchSource.decodedGeneration();
      require(drawPlanGeneration, "draw plan source generation");

      Image actualTouch = new Image(drawBytes, drawLength)
          .getTouchedUpInstance((byte) 20, (byte) -10);
      Image actualFade = new Image(drawBytes, drawLength)
          .getFadedInstance(0xFF204060);
      Image actualAlpha = new Image(drawBytes, drawLength)
          .getAlphaInstance(-40);
      Image actualTouchThenRotate = new Image(drawBytes, drawLength)
          .getTouchedUpInstance((byte) 20, (byte) -10)
          .getRotatedScaledInstance(100, 90, 0xFF102030);
      Image actualRotateThenTouch = new Image(drawBytes, drawLength)
          .getRotatedScaledInstance(100, 90, 0xFF102030)
          .getTouchedUpInstance((byte) 20, (byte) -10);
      int[] actualTouchPixels = drawToPixels(actualTouch);
      int[] actualFadePixels = drawToPixels(actualFade);
      int[] actualAlphaPixels = drawToPixels(actualAlpha);
      int[] actualTouchThenRotatePixels = drawToPixels(actualTouchThenRotate);
      int[] actualRotateThenTouchPixels = drawToPixels(actualRotateThenTouch);
      Image actualCapturedFrame = deferredFrameStripImage();
      actualCapturedFrame.setCurrentFrame(1);
      actualCapturedFrame.applyFade(128);
      Image actualUncapturedFrame = deferredFrameStripImage();
      actualUncapturedFrame.setCurrentFrame(1);
      actualUncapturedFrame.applyFade(128);
      actualUncapturedFrame.setCurrentFrame(0);
      int[] actualApplyColorPixels = drawToPixels(applyColorImage(drawBytes, drawLength));
      int[] actualApplyFadePixels = drawToPixels(applyFadeImage(drawBytes, drawLength));
      int[] actualCapturedFramePixels = drawToPixels(actualCapturedFrame);
      int[] actualUncapturedFramePixels = drawToPixels(actualUncapturedFrame);
      drawDirectCount = Image.directDrawPlanExecutionCountForTest();
      drawColorReadbackCount = Image.nativeColorReadbackCountForTest();
      drawColorStages = sameArray(actualTouchPixels, expectedTouchPixels)
          && sameArray(actualFadePixels, expectedFadePixels)
          && sameArray(actualAlphaPixels, expectedAlphaPixels)
          && sameArray(actualTouchThenRotatePixels, expectedTouchThenRotatePixels)
          && sameArray(actualRotateThenTouchPixels, expectedRotateThenTouchPixels);
      drawInPlaceStages = sameArray(actualApplyColorPixels, expectedApplyColorPixels)
          && sameArray(actualApplyFadePixels, expectedApplyFadePixels)
          && sameArray(actualCapturedFramePixels, expectedCapturedFramePixels)
          && sameArray(actualUncapturedFramePixels, expectedUncapturedFramePixels)
          && drawDirectCount > 0 && drawColorReadbackCount == 0;
      int prefixReadbackBefore = Image.nativeColorReadbackCountForTest();
      Image actualColorBeforeSmooth = new Image(drawBytes, drawLength)
          .getTouchedUpInstance((byte) 20, (byte) -10)
          .getSmoothScaledInstance(3, 2);
      int[] actualColorBeforeSmoothPixels = drawToPixels(actualColorBeforeSmooth);
      int prefixReadbackAfterFirst = Image.nativeColorReadbackCountForTest();
      int[] actualColorBeforeSmoothPixelsAgain = drawToPixels(actualColorBeforeSmooth);
      int prefixReadbackAfterSecond = Image.nativeColorReadbackCountForTest();
      Image actualSmoothThenColor = new Image(drawBytes, drawLength)
          .getSmoothScaledInstance(3, 2)
          .getTouchedUpInstance((byte) 20, (byte) -10);
      Image actualTwoRotations = new Image(drawBytes, drawLength)
          .getRotatedScaledInstance(100, 90, 0xFF102030)
          .getRotatedScaledInstance(100, 90, 0xFF405060);
      drawPrefixStages = sameArray(actualColorBeforeSmoothPixels, expectedColorBeforeSmoothPixels)
          && sameArray(actualColorBeforeSmoothPixelsAgain, expectedColorBeforeSmoothPixels)
          && sameArray(drawToPixels(actualSmoothThenColor), expectedSmoothThenColorPixels)
          && sameArray(drawToPixels(actualTwoRotations), expectedTwoRotationsPixels)
          && prefixReadbackAfterFirst == prefixReadbackBefore + 1
          && prefixReadbackAfterSecond == prefixReadbackAfterFirst;
      drawDirectCount = Image.directDrawPlanExecutionCountForTest();
      Image repeatedApplyColor2 = new Image(drawBytes, drawLength);
      repeatedApplyColor2.applyColor2(0xAA4080C0);
      int color2ReadbackBeforeFirstDraw = Image.nativeColorReadbackCountForTest();
      int[] actualApplyColor2Pixels = drawToPixels(repeatedApplyColor2);
      int color2ReadbackAfterFirstDraw = Image.nativeColorReadbackCountForTest();
      int[] actualApplyColor2PixelsAgain = drawToPixels(repeatedApplyColor2);
      int color2ReadbackAfterSecondDraw = Image.nativeColorReadbackCountForTest();
      applyColor2Cache = sameArray(actualApplyColor2Pixels, expectedApplyColor2Pixels)
          && sameArray(actualApplyColor2PixelsAgain, expectedApplyColor2Pixels)
          && color2ReadbackAfterFirstDraw > color2ReadbackBeforeFirstDraw
          && color2ReadbackAfterSecondDraw == color2ReadbackAfterFirstDraw;
      drawColorStages = drawColorStages && drawInPlaceStages;
      require(drawColorStages, "draw-time color stages direct=" + drawDirectCount
          + ",readback=" + drawColorReadbackCount);

      ByteArrayStream encoded = new ByteArrayStream(512);
      touched.createPng(encoded);
      Image saved = new Image(encoded.getBuffer(), encoded.getPos());
      Image destination = new Image(WIDTH, HEIGHT);
      Graphics destinationGraphics = destination.getGraphics();
      destinationGraphics.drawImage(touched, 0, 0);
      drawAndSave = drawAndSave && sameArray(touchedPixels, saved.getPixels())
          && sameArray(touchedPixels, destination.getPixels());
      require(drawAndSave, "direct draw and saved output");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = nativeBacking && frameScopedFade && alpha && faded && applyColor && applyColor2
        && directColorParity
        && touchUp && exactColors && mixedPipeline && touchUpThreshold && drawColorStages && drawPrefixStages
        && applyColor2Cache && encodedSourceCache && drawAndSave;
    overallPass = overallPass && drawPlanGeneration;
    System.out.println("fixture=ImageNativeColorFilterSmokeApp,nativeBacking=" + nativeBacking
        + ",frameScopedFade=" + frameScopedFade + ",alpha=" + alpha + ",faded=" + faded
        + ",applyColor=" + applyColor + ",applyColor2=" + applyColor2
        + ",directColorParity=" + directColorParity
        + ",touchUp=" + touchUp + ",exactColors=" + exactColors
        + ",mixedPipeline=" + mixedPipeline + ",touchUpThreshold=" + touchUpThreshold
        + ",drawColorStages=" + drawColorStages + ",drawInPlaceStages=" + drawInPlaceStages
        + ",drawPrefixStages=" + drawPrefixStages
        + ",applyColor2Cache=" + applyColor2Cache
        + ",encodedSourceCache=" + encodedSourceCache
        + ",drawPlanGeneration=" + drawPlanGeneration
        + ",drawAndSave=" + drawAndSave
        + ",drawDirectCount=" + drawDirectCount + ",drawColorReadbackCount=" + drawColorReadbackCount
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

}
