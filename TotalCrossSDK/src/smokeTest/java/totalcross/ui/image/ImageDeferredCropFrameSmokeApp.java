// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.net.Base64;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for deferred Image frame extraction and clipping. */
public class ImageDeferredCropFrameSmokeApp extends MainWindow {
  private static final int FRAME_A = 0xFF102030;
  private static final int FRAME_B = 0xFF405060;

  @Override
  public void initUI() {
    boolean frameExtractionLazy = false;
    boolean frameNormalization = false;
    boolean getCopyLazy = false;
    boolean cropLazy = false;
    boolean cropPixels = false;
    boolean cropCapturesCurrentFrame = false;
    boolean cropMetadataCompatibility = false;
    boolean rasterSnapshotIsolation = false;
    boolean naturalScaleCrop = false;
    boolean scaledCrop = false;
    boolean fractionalScale15 = false;
    boolean fractionalScale25 = false;
    boolean alphaMaskCompatibility = false;
    boolean hwScaleCompatibility = false;
    boolean targetedAfterSmooth = false;
    boolean fallbackBeforeSmooth = false;
    String error = "";

    try {
      byte[] fixture = encodedFrames();
      Image source = new Image(fixture);
      Image selected = source.getFrameInstance(-1);
      frameExtractionLazy = source.pipelineForSmoke() != null
          && selected.pipelineForSmoke() != null;
      require(frameExtractionLazy, "lazy frame extraction");
      int lastPixel = selected.getPixels()[0];
      int firstPixel = new Image(fixture).getFrameInstance(99).getPixels()[0];
      frameNormalization = lastPixel != firstPixel;
      require(frameNormalization, "frame normalization");

      Image copy = new Image(fixture).getCopy();
      getCopyLazy = copy.pipelineForSmoke() != null;
      Image expectedCopy = new Image(fixture).getFrameInstance(0);
      getCopyLazy = getCopyLazy && copy.getPixels()[0] == expectedCopy.getPixels()[0];
      require(getCopyLazy, "lazy getCopy");

      Image actualCrop = new Image(fixture).getClippedInstance(1, 0, 2, 2);
      cropLazy = actualCrop.pipelineForSmoke() != null;
      require(cropLazy, "lazy crop");
      Image expectedCrop = eagerCrop(new Image(fixture), 1, 0, 2, 2);
      cropPixels = samePixels(expectedCrop, actualCrop);
      require(cropPixels, "crop pixels actual=" + values(actualCrop.getPixels()) + " expected="
          + values(expectedCrop.getPixels()) + " logical=" + actualCrop.getWidth() + "x"
          + actualCrop.getHeight() + " physical=" + actualCrop.getPixelWidth() + "x"
          + actualCrop.getPixelHeight() + " scale=" + actualCrop.getContentScale());

      Image currentFrameSource = new Image(fixture);
      currentFrameSource.setCurrentFrame(1);
      Image currentFrameCrop = currentFrameSource.getClippedInstance(0, 0, 2, 2);
      currentFrameSource.setCurrentFrame(0);
      Image expectedCurrentFrameCrop = eagerCrop(new Image(fixture).getFrameInstance(1), 0, 0, 2, 2);
      cropCapturesCurrentFrame = samePixels(expectedCurrentFrameCrop, currentFrameCrop)
          && currentFrameCrop.getFrameCount() == 1;
      require(cropCapturesCurrentFrame, "crop current frame snapshot");

      Image metadataSource = new Image(fixture);
      metadataSource.hwScaleW = 0.5;
      metadataSource.hwScaleH = 0.75;
      metadataSource.alphaMask = 73;
      metadataSource.transparentColor = 0x123456;
      metadataSource.useAlpha = true;
      Image metadataCrop = metadataSource.getClippedInstance(0, 0, 2, 1);
      cropMetadataCompatibility = metadataCrop.getPath() == null && metadataCrop.hwScaleW == 1
          && metadataCrop.hwScaleH == 1 && metadataCrop.alphaMask == 255
          && metadataCrop.transparentColor == 0xFFFFFF && !metadataCrop.useAlpha;
      require(cropMetadataCompatibility, "crop metadata defaults");

      Image raster = new Image(4, 3);
      fill(raster, 0xFF203040);
      int callTimePixel = raster.getPixels()[0];
      Image rasterCopy = raster.getFrameInstance(0);
      raster.getPixels()[0] = 0xFFFFFFFF;
      rasterSnapshotIsolation = rasterCopy.backing == null && callTimePixel == rasterCopy.getPixels()[0];
      require(rasterSnapshotIsolation, "raster snapshot isolation");

      Image logical = Image.createLogical(3, 2, 2);
      fillPattern(logical);
      Image naturalDeferred = logical.getClippedInstance(1, 0, 2, 1);
      Image natural = naturalDeferred.resolveForDrawing(3);
      naturalScaleCrop = natural.getContentScale() == 2 && natural.getPixelWidth() == 4
          && natural.getPixelHeight() == 2
          && sameArray(natural.getPixels(), croppedPattern(2, 0, 4));
      require(naturalScaleCrop, "natural crop scale actual=" + values(natural.getPixels()) + " expected="
          + values(croppedPattern(2, 0, 4)) + " dimensions=" + natural.getPixelWidth() + "x"
          + natural.getPixelHeight() + " scale=" + natural.getContentScale());

      Image scaledSource = Image.createLogical(4, 4, 2);
      fillQuadrants(scaledSource);
      Image scaled = scaledSource.getSmoothScaledInstance(6, 6).getClippedInstance(1, 1, 1, 1);
      Image scaledResult = scaled.resolveForDrawing(2);
      scaledCrop = scaledResult.getContentScale() == 2 && scaledResult.getPixelWidth() == 2
          && scaledResult.getPixelHeight() == 2 && scaledResult.getWidth() == 1
          && scaledResult.getHeight() == 1 && allPixelsEqual(scaledResult.getPixels(), FRAME_A);
      require(scaledCrop, "scaled crop bounds actual=" + values(scaledResult.getPixels()) + " dimensions="
          + scaledResult.getPixelWidth() + "x" + scaledResult.getPixelHeight() + " logical="
          + scaledResult.getWidth() + "x" + scaledResult.getHeight() + " scale="
          + scaledResult.getContentScale());

      fractionalScale15 = fractionalScaleCrop(1.5);
      require(fractionalScale15, "fractional scale 1.5");
      fractionalScale25 = fractionalScaleCrop(2.5);
      require(fractionalScale25, "fractional scale 2.5");

      Image alphaSource = Image.createLogical(3, 2, 2);
      fillPattern(alphaSource);
      alphaSource.alphaMask = 127;
      Image alphaActual = alphaSource.getClippedInstance(1, 0, 2, 1).resolveForDrawing(1);
      Image alphaExpected = eagerCropWithScale(alphaSource, 1, 0, 2, 1, 2);
      int[] alphaActualPixels = alphaActual.getPixels();
      int[] alphaExpectedPixels = alphaExpected.getPixels();
      alphaMaskCompatibility = sameArray(alphaExpectedPixels, alphaActualPixels);
      require(alphaMaskCompatibility, "alpha mask compatibility");

      Image hwSource = Image.createLogical(3, 2, 2);
      fillPattern(hwSource);
      hwSource.hwScaleW = 0.5;
      hwSource.hwScaleH = 0.75;
      Image hwActual = hwSource.getClippedInstance(1, 0, 2, 1).resolveForDrawing(1);
      Image hwExpected = eagerCropWithScale(hwSource, 1, 0, 2, 1, 2);
      hwScaleCompatibility = samePixels(hwExpected, hwActual);
      require(hwScaleCompatibility, "hardware scale compatibility");

      byte[] jpeg = createJpeg(1024, 768);
      Image.resetTargetedDecodeInvocationCountForTest();
      Image smoothThenCrop = new Image(jpeg).getSmoothScaledInstance(64, 48).getClippedInstance(0, 0, 32, 24);
      Image smoothCropResult = smoothThenCrop.resolveForDrawing(1);
      targetedAfterSmooth = Image.targetedDecodeInvocationCountForTest() == 1
          && smoothCropResult.getPixelWidth() == 32 && smoothCropResult.getPixelHeight() == 24;
      require(targetedAfterSmooth, "JPEG targeted decode after smooth scale");

      Image.resetTargetedDecodeInvocationCountForTest();
      Image cropThenSmooth = new Image(jpeg).getClippedInstance(0, 0, 128, 96).getSmoothScaledInstance(64, 48);
      Image fallbackResult = cropThenSmooth.resolveForDrawing(1);
      fallbackBeforeSmooth = Image.targetedDecodeInvocationCountForTest() == 0
          && fallbackResult.getPixelWidth() == 64 && fallbackResult.getPixelHeight() == 48;
      require(fallbackBeforeSmooth, "JPEG fallback before smooth scale");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = frameExtractionLazy && frameNormalization && getCopyLazy && cropLazy && cropPixels
        && cropCapturesCurrentFrame && cropMetadataCompatibility && rasterSnapshotIsolation && naturalScaleCrop
        && scaledCrop && fractionalScale15 && fractionalScale25 && alphaMaskCompatibility && hwScaleCompatibility
        && targetedAfterSmooth && fallbackBeforeSmooth;
    System.out.println("fixture=ImageDeferredCropFrameSmokeApp,frameExtractionLazy=" + frameExtractionLazy
        + ",frameNormalization=" + frameNormalization + ",getCopyLazy=" + getCopyLazy + ",cropLazy=" + cropLazy
        + ",cropPixels=" + cropPixels + ",cropCapturesCurrentFrame=" + cropCapturesCurrentFrame
        + ",cropMetadataCompatibility=" + cropMetadataCompatibility + ",rasterSnapshotIsolation="
        + rasterSnapshotIsolation + ",naturalScaleCrop=" + naturalScaleCrop + ",scaledCrop=" + scaledCrop
        + ",fractionalScale15=" + fractionalScale15 + ",fractionalScale25=" + fractionalScale25
        + ",alphaMaskCompatibility=" + alphaMaskCompatibility + ",hwScaleCompatibility="
        + hwScaleCompatibility
        + ",targetedAfterSmooth=" + targetedAfterSmooth + ",fallbackBeforeSmooth=" + fallbackBeforeSmooth
        + ",overallPass=" + overallPass + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static byte[] encodedFrames() throws Exception {
    return Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAgAAAACCAYAAABllJ3tAAAADHRFWHRDb21tZW50AEZDPTLAxqe/AAAAFklEQVR4nGMQUDD4j4wdAhJQMAMhBQAwNxpx1IOH/QAAAABJRU5ErkJggg==");
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

  private static Image eagerCrop(Image source, int x, int y, int w, int h) throws Exception {
    source.getPixels();
    Image result = new Image(w, h);
    new Graphics(result).copyImageRect(source, x, y, w, h, true);
    return result;
  }

  private static Image eagerCropWithScale(Image source, int x, int y, int w, int h, double contentScale)
      throws Exception {
    source.getPixels();
    Image result = Image.createLogical(w, h, contentScale);
    new Graphics(result).copyImageRect(source, x, y, w, h, true);
    return result;
  }

  private static boolean fractionalScaleCrop(double contentScale) throws Exception {
    Image source = Image.createLogical(3, 2, contentScale);
    fillPattern(source);
    int sourceX = (int) Math.round(contentScale);
    int physicalWidth = (int) Math.ceil(contentScale);
    int physicalHeight = (int) Math.ceil(contentScale);
    Image actual = source.getClippedInstance(1, 0, 1, 1).resolveForDrawing(1);
    int[] expected = new int[physicalWidth * physicalHeight];
    int[] sourcePixels = source.getPixels();
    for (int row = 0; row < physicalHeight; row++) {
      Vm.arrayCopy(sourcePixels, row * source.getPixelWidth() + sourceX, expected, row * physicalWidth,
          physicalWidth);
    }
    int[] actualPixels = actual.getPixels();
    boolean widthPass = actual.getPixelWidth() == physicalWidth;
    boolean heightPass = actual.getPixelHeight() == physicalHeight;
    boolean pixelsPass = sameArray(actualPixels, expected);
    return widthPass && heightPass && pixelsPass;
  }

  private static boolean sameArray(int[] first, int[] second) {
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

  private static String values(int[] pixels) {
    String result = "[";
    for (int i = 0; i < pixels.length; i++) {
      if (i > 0) {
        result += ",";
      }
      result += Integer.toHexString(pixels[i]);
    }
    return result + "]";
  }

  private static boolean allPixelsEqual(int[] pixels, int expected) {
    for (int pixel : pixels) {
      if (pixel != expected) {
        return false;
      }
    }
    return true;
  }

  private static void fillPattern(Image image) {
    Graphics graphics = beginPhysicalFill(image);
    int width = image.getPixelWidth();
    int height = image.getPixelHeight();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        graphics.foreColor = patternPixel(x, y);
        graphics.setPixel(x, y);
      }
    }
    endPhysicalFill(image, graphics);
  }

  private static int[] croppedPattern(int x, int y, int physicalWidth) {
    int[] result = new int[physicalWidth * 2];
    for (int row = 0; row < 2; row++) {
      for (int column = 0; column < physicalWidth; column++) {
        result[row * physicalWidth + column] = patternPixel(x + column, y + row);
      }
    }
    return result;
  }

  private static int patternPixel(int x, int y) {
    return 0xFF000000 | ((x + 1) << 16) | ((y + 1) << 8) | (x + y + 1);
  }

  private static void fillQuadrants(Image image) {
    Graphics graphics = beginPhysicalFill(image);
    int width = image.getPixelWidth();
    int halfWidth = width / 2;
    int halfHeight = image.getPixelHeight() / 2;
    for (int y = 0; y < image.getPixelHeight(); y++) {
      for (int x = 0; x < width; x++) {
        graphics.foreColor = x < halfWidth && y < halfHeight ? FRAME_A
            : x >= halfWidth && y < halfHeight ? FRAME_B : 0xFF708090;
        graphics.setPixel(x, y);
      }
    }
    endPhysicalFill(image, graphics);
  }

  private static void fill(Image image, int pixel) {
    Graphics graphics = beginPhysicalFill(image);
    graphics.foreColor = pixel;
    for (int y = 0; y < image.getPixelHeight(); y++) {
      for (int x = 0; x < image.getPixelWidth(); x++) {
        graphics.setPixel(x, y);
      }
    }
    endPhysicalFill(image, graphics);
  }

  private static Graphics beginPhysicalFill(Image image) {
    Graphics graphics = image.getGraphics();
    graphics.setScales(1, 1);
    graphics.refresh(0, 0, image.getPixelWidth(), image.getPixelHeight(), 0, 0, null);
    return graphics;
  }

  private static void endPhysicalFill(Image image, Graphics graphics) {
    graphics.setScales(image.getContentScale(), 1);
    graphics.refresh(0, 0, image.getWidth(), image.getHeight(), 0, 0, null);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static byte[] createJpeg(int width, int height) throws Exception {
    Image image = new Image(width, height);
    fill(image, 0xFF204060);
    ByteArrayStream stream = new ByteArrayStream(width * height);
    image.createJpg(stream, 80);
    byte[] result = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, result, 0, result.length);
    return result;
  }
}
