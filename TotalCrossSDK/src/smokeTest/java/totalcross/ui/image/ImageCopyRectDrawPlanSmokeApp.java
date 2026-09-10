// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Native smoke cases for deferred copyRect draw-plan execution. */
public class ImageCopyRectDrawPlanSmokeApp extends MainWindow {
  private static final int SOURCE_WIDTH = 80;
  private static final int SOURCE_HEIGHT = 60;
  private static final int TARGET_WIDTH = 160;
  private static final int TARGET_HEIGHT = 120;
  private static final int DEST_X = 31;
  private static final int DEST_Y = 17;

  @Override
  public void initUI() {
    boolean fullPass = false;
    boolean clippedPass = false;
    boolean noIntersectionPass = false;
    boolean framePass = false;
    boolean fallbackPass = false;
    boolean feature15DefaultPass = false;
    boolean feature15DisabledPass = false;
    String error = "";
    try {
      ImageRasterBenchmarkSupport.configureApplicationRasterFeatures("post-enabled", false, false);
      ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
          ImageOptimizationSettings.ENABLED);
      Image source = patternedSource(SOURCE_WIDTH, SOURCE_HEIGHT);
      Image deferred = source.getSmoothScaledInstance(SOURCE_WIDTH, SOURCE_HEIGHT);
      Image expectedDeferred = deferred.resolveForDrawing(1);
      fullPass = compareCopy(expectedDeferred, deferred, false, 0, 0, SOURCE_WIDTH, SOURCE_HEIGHT);
      clippedPass = compareCopy(expectedDeferred, deferred, true, 0, 0, SOURCE_WIDTH, SOURCE_HEIGHT);
      noIntersectionPass = noIntersection(deferred);
      framePass = frameCopy();
      fallbackPass = unsupportedFallback(source);
      feature15DefaultPass = identityFeatureState(true);
      feature15DisabledPass = identityFeatureState(false);
      ImageRasterBenchmarkSupport.require(fullPass, "full copyRect plan path");
      ImageRasterBenchmarkSupport.require(clippedPass, "clipped copyRect plan path");
      ImageRasterBenchmarkSupport.require(noIntersectionPass, "no-intersection copyRect");
      ImageRasterBenchmarkSupport.require(framePass, "frame copyRect plan path");
      ImageRasterBenchmarkSupport.require(fallbackPass, "unsupported copyRect fallback");
      ImageRasterBenchmarkSupport.require(feature15DefaultPass, "feature 15 default");
      ImageRasterBenchmarkSupport.require(feature15DisabledPass, "feature 15 disabled");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_').replace(',', '_');
    }
    boolean pass = fullPass && clippedPass && noIntersectionPass && framePass && fallbackPass
        && feature15DefaultPass && feature15DisabledPass && error.length() == 0;
    System.out.println("fixture=ImageCopyRectDrawPlanSmokeApp,full=" + fullPass
        + ",clipped=" + clippedPass + ",noIntersection=" + noIntersectionPass
        + ",frame=" + framePass + ",fallback=" + fallbackPass
        + ",feature15Default=" + feature15DefaultPass
        + ",feature15Disabled=" + feature15DisabledPass
        + ",directDrawPlans=" + Image.directDrawPlanExecutionCountForTest()
        + ",physicalIdentityHits=" + NativeImageBacking.physicalIdentityHitsForTest()
        + ",genericGeometryDraws=" + NativeImageBacking.genericGeometryDrawsForTest()
        + ",smoothResampleDraws=" + NativeImageBacking.smoothResampleDrawsForTest()
        + ",nativeGeometryMaterializations=" + Image.nativeGeometryMaterializationCountForTest()
        + ",overallPass=" + pass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(pass ? 0 : 1);
  }

  private static boolean compareCopy(Image expectedSource, Image deferredSource, boolean clip,
      int sourceX, int sourceY, int width, int height) throws Exception {
    Image expected = target();
    Image actual = target();
    Graphics expectedGraphics = expected.getGraphics();
    Graphics actualGraphics = actual.getGraphics();
    if (clip) {
      expectedGraphics.setClip(50, 20, 40, 30);
      actualGraphics.setClip(50, 20, 40, 30);
    }
    expectedGraphics.copyRect(expectedSource, sourceX, sourceY, width, height, DEST_X, DEST_Y);
    Image.resetImageOperationAccountingForTest();
    actualGraphics.copyRect(deferredSource, sourceX, sourceY, width, height, DEST_X, DEST_Y);
    boolean pixels = samePixels(expected.getPixels(), actual.getPixels());
    boolean direct = Image.directDrawPlanExecutionCountForTest() > 0
        && Image.nativeGeometryMaterializationCountForTest() == 0;
    boolean raster = NativeImageBacking.physicalIdentityHitsForTest() > 0
        && NativeImageBacking.writePixelsHitsForTest() > 0
        && NativeImageBacking.genericGeometryDrawsForTest() == 0
        && NativeImageBacking.smoothResampleDrawsForTest() == 0;
    return pixels && direct && raster;
  }

  private static boolean noIntersection(Image deferredSource) throws Exception {
    Image actual = target();
    NativeImageBacking targetBacking = (NativeImageBacking) actual.backing;
    long generation = targetBacking.generationForTest();
    int opacity = targetBacking.opacityForTest();
    long hash = ImageRasterBenchmarkSupport.fullPixelHash(actual);
    Image.resetImageOperationAccountingForTest();
    actual.getGraphics().copyRect(deferredSource, 0, 0, SOURCE_WIDTH, SOURCE_HEIGHT,
        TARGET_WIDTH + 20, TARGET_HEIGHT + 20);
    return hash == ImageRasterBenchmarkSupport.fullPixelHash(actual)
        && generation == targetBacking.generationForTest()
        && opacity == targetBacking.opacityForTest()
        && Image.directDrawPlanExecutionCountForTest() > 0;
  }

  private static boolean frameCopy() throws Exception {
    Image frames = patternedSource(SOURCE_WIDTH * 2, SOURCE_HEIGHT);
    frames.setFrameCount(2);
    Image expectedFrame = frames.getFrameInstance(1);
    expectedFrame.getPixels();
    Image deferredFrame = patternedSource(SOURCE_WIDTH * 2, SOURCE_HEIGHT);
    deferredFrame.setFrameCount(2);
    deferredFrame = deferredFrame.getFrameInstance(1);
    Image expected = target();
    Image actual = target();
    expected.getGraphics().copyRect(expectedFrame, 0, 0, SOURCE_WIDTH, SOURCE_HEIGHT, DEST_X, DEST_Y);
    Image.resetImageOperationAccountingForTest();
    actual.getGraphics().copyRect(deferredFrame, 0, 0, SOURCE_WIDTH, SOURCE_HEIGHT, DEST_X, DEST_Y);
    return samePixels(expected.getPixels(), actual.getPixels())
        && Image.directDrawPlanExecutionCountForTest() > 0
        && NativeImageBacking.physicalIdentityHitsForTest() > 0
        && NativeImageBacking.smoothResampleDrawsForTest() == 0;
  }

  private static boolean unsupportedFallback(Image source) throws Exception {
    Image expectedSource = source.getRotatedScaledInstance(100, 45, 0).resolveForDrawing(2);
    Image deferredSource = source.getRotatedScaledInstance(100, 45, 0);
    Image expected = target();
    Image actual = target();
    int width = SOURCE_WIDTH;
    int height = SOURCE_HEIGHT;
    expected.getGraphics().copyRect(expectedSource, 0, 0, width, height, 5, 5);
    Image.resetImageOperationAccountingForTest();
    actual.getGraphics().copyRect(deferredSource, 0, 0, width, height, 5, 5);
    int[] expectedPixels = expected.getPixels();
    int[] actualPixels = actual.getPixels();
    return samePixels(expectedPixels, actualPixels)
        && Image.directDrawPlanExecutionCountForTest() == 0
        && NativeImageBacking.physicalIdentityAttemptsForTest() == 0
        && Image.nativeGeometryMaterializationCountForTest() > 0;
  }

  private static boolean identityFeatureState(boolean defaultEnabled) throws Exception {
    ImageOptimizationSettings.resetForTest();
    ImageOptimizationSettings.setState(ImageOptimizationSettings.DIAGNOSTIC_ACCOUNTING,
        ImageOptimizationSettings.ENABLED);
    if (!defaultEnabled) {
      ImageOptimizationSettings.setState(ImageOptimizationSettings.RASTER_PHYSICAL_IDENTITY_FOLDING,
          ImageOptimizationSettings.DISABLED);
    }
    Image source = patternedSource(SOURCE_WIDTH, SOURCE_HEIGHT);
    Image deferred = source.getSmoothScaledInstance(SOURCE_WIDTH, SOURCE_HEIGHT);
    Image expectedSource = deferred.resolveForDrawing(2);
    Image expected = target();
    Image actual = target();
    expected.getGraphics().copyRect(expectedSource, 0, 0, SOURCE_WIDTH, SOURCE_HEIGHT, DEST_X, DEST_Y);
    Image.resetImageOperationAccountingForTest();
    actual.getGraphics().copyRect(deferred, 0, 0, SOURCE_WIDTH, SOURCE_HEIGHT, DEST_X, DEST_Y);
    boolean parity = samePixels(expected.getPixels(), actual.getPixels());
    if (defaultEnabled) {
      return parity && Image.directDrawPlanExecutionCountForTest() > 0
          && Image.nativeGeometryMaterializationCountForTest() == 0
          && NativeImageBacking.physicalIdentityHitsForTest() > 0
          && NativeImageBacking.writePixelsHitsForTest() > 0
          && NativeImageBacking.genericGeometryDrawsForTest() == 0;
    }
    return parity && Image.directDrawPlanExecutionCountForTest() > 0
        && Image.nativeGeometryMaterializationCountForTest() == 0
        && NativeImageBacking.physicalIdentityAttemptsForTest() == 0
        && NativeImageBacking.genericGeometryDrawsForTest() > 0
        && NativeImageBacking.smoothResampleDrawsForTest() > 0;
  }

  private static Image target() throws Exception {
    Image image = Image.createLogical(TARGET_WIDTH, TARGET_HEIGHT, 2);
    Graphics graphics = image.getGraphics();
    graphics.backColor = 0x102030;
    graphics.fillRect(0, 0, TARGET_WIDTH, TARGET_HEIGHT);
    return image;
  }

  private static Image patternedSource(int width, int height) throws Exception {
    Image image = Image.createLogical(width, height, 2);
    Graphics graphics = image.getGraphics();
    for (int y = 0; y < height; y += 10) {
      for (int x = 0; x < width; x += 10) {
        graphics.foreColor = 0xFF000000 | ((x * 17) & 0xFF) << 16
            | ((y * 19) & 0xFF) << 8 | ((x + y) & 0xFF);
        graphics.fillRect(x, y, Math.min(10, width - x), Math.min(10, height - y));
      }
    }
    return image;
  }

  private static boolean samePixels(int[] first, int[] second) {
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
}
