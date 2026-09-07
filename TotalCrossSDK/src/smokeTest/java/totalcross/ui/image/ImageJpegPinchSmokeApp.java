// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for JPEG pinch-to-zoom backing promotion and eviction. */
public class ImageJpegPinchSmokeApp extends MainWindow {
  private static final int SOURCE_WIDTH = 1024;
  private static final int SOURCE_HEIGHT = 768;

  @Override
  public void initUI() {
    boolean smallestSufficientTier = false;
    boolean upwardPromotion = false;
    boolean repeatAndDownwardReuse = false;
    boolean zoomOutAfterFull = false;
    boolean noAvoidableUpscale = false;
    boolean quality20 = false;
    boolean quality35 = false;
    boolean quality60 = false;
    boolean quality100 = false;
    boolean metadataInvariant = false;
    boolean smoothFamilies = false;
    boolean directDrawParity = false;
    boolean oddDimensions = false;
    boolean boundaryDenominators = false;
    boolean nearestRotateFullDecode = false;
    boolean evictionReload = false;
    boolean retainedBacking = false;
    int directDrawCount = -1;
    int targetedDecodeCount = -1;
    int fullDecodeCount = -1;
    String error = "";

    try {
      byte[] jpeg = createJpeg();
      Image root = new Image(jpeg);
      EncodedImageSource source = (EncodedImageSource) root.pipelineForSmoke().root();
      Image.resetImageOperationAccountingForTest();

      Image metadataBase = new Image(jpeg);
      Image metadataFirst = metadataBase.getSmoothScaledInstance(102, 77);
      ImageDrawPlan freshPlan = (ImageDrawPlan) metadataFirst.drawPlanForDrawing(1);
      Image metadataCached = metadataBase.getSmoothScaledInstance(102, 77);
      ImageDrawPlan cachedPlan = (ImageDrawPlan) metadataCached.drawPlanForDrawing(1);
      metadataInvariant = freshPlan != null && cachedPlan != null
          && freshPlan.root.getPixelWidth() == 128 && freshPlan.root.getPixelHeight() == 96
          && freshPlan.root.getWidth() == SOURCE_WIDTH && freshPlan.root.getHeight() == SOURCE_HEIGHT
          && freshPlan.root.getContentScale() == 0.125 && freshPlan.rootContentScale == 0.125
          && cachedPlan.root.getContentScale() == freshPlan.root.getContentScale()
          && samePixels(freshPlan.root.getPixels(), cachedPlan.root.getPixels());
      require(metadataInvariant, "targeted metadata invariant");
      Image.resetImageOperationAccountingForTest();

      double[] fractions = {
          .10, .125, .20, .25, .30, .49, .50, .55, .80, 1.00,
          .70, .40, .20, .10, .35, .60
      };
      int[] expectedDenominators = {8, 8, 4, 4, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1};
      int[] expectedGenerations = {1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4};
      Image full = null;
      int decodeCountAtFull = -1;
      for (int i = 0; i < fractions.length; i++) {
        int width = displayDimension(SOURCE_WIDTH, fractions[i]);
        int height = displayDimension(SOURCE_HEIGHT, fractions[i]);
        Image result = root.getSmoothScaledInstance(width, height).resolveForDrawing(1);
        require(result.getPixelWidth() == width && result.getPixelHeight() == height,
            "pinch output dimensions i=" + i);
        require(source.decodedDenominator() == expectedDenominators[i]
            && source.decodedGeneration() == expectedGenerations[i],
            "pinch tier i=" + i + " denominator=" + source.decodedDenominator()
                + " generation=" + source.decodedGeneration());
        require(source.decodedWidth() >= width && source.decodedHeight() >= height,
            "pinch backing coverage i=" + i);
        if (i == 0) {
          smallestSufficientTier = source.decodedDenominator() == 8;
        }
        if (i == 7) {
          decodeCountAtFull = Image.targetedDecodeInvocationCountForTest()
              + Image.fullDecodeInvocationCountForTest();
          upwardPromotion = Image.targetedDecodeInvocationCountForTest() == 3
              && Image.fullDecodeInvocationCountForTest() == 1;
        }
        if (i == 9) {
          full = result;
        }
        if (i >= 10) {
          repeatAndDownwardReuse = Image.targetedDecodeInvocationCountForTest() == 3
              && Image.fullDecodeInvocationCountForTest() == 1;
        }
      }
      targetedDecodeCount = Image.targetedDecodeInvocationCountForTest();
      fullDecodeCount = Image.fullDecodeInvocationCountForTest();
      zoomOutAfterFull = decodeCountAtFull == targetedDecodeCount + fullDecodeCount
          && source.decodedDenominator() == 1;

      Image qualityRoot = new Image(jpeg);
      EncodedImageSource qualitySource = (EncodedImageSource) qualityRoot.pipelineForSmoke().root();
      Image quality20Image = qualityRoot.getSmoothScaledInstance(205, 154).resolveForDrawing(1);
      quality20 = qualitySource.decodedDenominator() == 4
          && compareWithFullReference(jpeg, 205, 154, 0, quality20Image);
      Image quality35Image = qualityRoot.getSmoothScaledInstance(358, 269).resolveForDrawing(1);
      quality35 = qualitySource.decodedDenominator() == 2
          && qualitySource.decodedWidth() >= 358 && compareWithFullReference(jpeg, 358, 269, 0, quality35Image);
      Image quality60Image = qualityRoot.getSmoothScaledInstance(614, 461).resolveForDrawing(1);
      quality60 = qualitySource.decodedDenominator() == 1
          && qualitySource.decodedWidth() >= 614 && compareWithFullReference(jpeg, 614, 461, 0, quality60Image);
      Image quality100Image = qualityRoot.getSmoothScaledInstance(SOURCE_WIDTH, SOURCE_HEIGHT)
          .resolveForDrawing(1);
      quality100 = qualitySource.decodedDenominator() == 1
          && quality100Image.getPixelWidth() == SOURCE_WIDTH
          && quality100Image.getPixelHeight() == SOURCE_HEIGHT
          && compareWithFullReference(jpeg, SOURCE_WIDTH, SOURCE_HEIGHT, 0, quality100Image);
      noAvoidableUpscale = quality35 && quality60;

      Image familyAlpha = new Image(jpeg).getSmoothScaledInstance(205, 154).getAlphaInstance(-40)
          .resolveForDrawing(1);
      Image familyCrop = new Image(jpeg).getClippedInstance(0, 0, 512, 384)
          .getSmoothScaledInstance(205, 154).resolveForDrawing(1);
      Image familyTwice = new Image(jpeg).getSmoothScaledInstance(410, 308)
          .getSmoothScaledInstance(205, 154).resolveForDrawing(1);
      Image familyHardware = new Image(jpeg).getHwScaledInstance(205, 154).resolveForDrawing(1);
      smoothFamilies = compareWithFullReference(jpeg, 205, 154, 1, familyAlpha)
          && compareWithFullReference(jpeg, 205, 154, 2, familyCrop)
          && compareWithFullReference(jpeg, 205, 154, 3, familyTwice)
          && compareWithFullReference(jpeg, 205, 154, 0, familyHardware);
      require(smoothFamilies, "smooth JPEG family parity");

      int directBefore = Image.directDrawPlanExecutionCountForTest();
      Image directCandidate = new Image(jpeg).getSmoothScaledInstance(205, 154);
      Image directTarget = new Image(205, 154);
      directTarget.getGraphics().drawImage(directCandidate, 0, 0, true);
      directDrawCount = Image.directDrawPlanExecutionCountForTest();
      directDrawParity = directDrawCount > directBefore
          && compareWithFullReference(jpeg, 205, 154, 0, directTarget);
      require(directDrawParity, "direct draw JPEG parity");

      byte[] oddJpeg = createJpeg(161, 121);
      Image oddRoot = new Image(oddJpeg);
      EncodedImageSource oddSource = (EncodedImageSource) oddRoot.pipelineForSmoke().root();
      int[] oddWidths = {20, 40, 80, 81};
      int[] oddHeights = {15, 30, 60, 61};
      int[] oddDenominators = {8, 4, 2, 1};
      oddDimensions = true;
      for (int i = 0; i < oddWidths.length; i++) {
        Image oddResult = oddRoot.getSmoothScaledInstance(oddWidths[i], oddHeights[i])
            .resolveForDrawing(1);
        oddDimensions = oddDimensions && oddResult.getPixelWidth() == oddWidths[i]
            && oddResult.getPixelHeight() == oddHeights[i]
            && oddSource.decodedDenominator() == oddDenominators[i]
            && oddSource.decodedWidth() == (161 + oddDenominators[i] - 1) / oddDenominators[i]
            && oddSource.decodedHeight() == (121 + oddDenominators[i] - 1) / oddDenominators[i];
      }
      boundaryDenominators = oddDimensions;
      require(oddDimensions, "odd JPEG dimensions and denominator boundaries");

      Image.resetImageOperationAccountingForTest();
      new Image(jpeg).getScaledInstance(205, 154).resolveForDrawing(1);
      boolean nearestFull = Image.targetedDecodeInvocationCountForTest() == 0
          && Image.fullDecodeInvocationCountForTest() == 1;
      Image.resetImageOperationAccountingForTest();
      new Image(jpeg).getRotatedScaledInstance(100, 37, 0xFF123456).resolveForDrawing(1);
      boolean rotateFull = Image.targetedDecodeInvocationCountForTest() == 0
          && Image.fullDecodeInvocationCountForTest() == 1;
      nearestRotateFullDecode = nearestFull && rotateFull;
      require(nearestRotateFullDecode, "nearest and rotate full decode");

      long generationBeforeEviction = source.decodedGeneration();
      int targetedBeforeEviction = Image.targetedDecodeInvocationCountForTest();
      int fullBeforeEviction = Image.fullDecodeInvocationCountForTest();
      source.evictDecodedBacking();
      require(source.decodedDenominator() == 0 && source.decodedGeneration() == generationBeforeEviction + 1,
          "explicit JPEG eviction");
      retainedBacking = full != null && full.getPixels().length == SOURCE_WIDTH * SOURCE_HEIGHT;
      Image afterEviction = root.getSmoothScaledInstance(102, 77).resolveForDrawing(1);
      evictionReload = afterEviction.getPixelWidth() == 102 && afterEviction.getPixelHeight() == 77
          && source.decodedDenominator() == 8
          && Image.targetedDecodeInvocationCountForTest() == targetedBeforeEviction + 1
          && Image.fullDecodeInvocationCountForTest() == fullBeforeEviction;
      require(smallestSufficientTier && upwardPromotion && repeatAndDownwardReuse && zoomOutAfterFull
          && noAvoidableUpscale && quality20 && quality35 && quality60 && quality100 && metadataInvariant
          && smoothFamilies && directDrawParity && oddDimensions && boundaryDenominators
          && nearestRotateFullDecode && retainedBacking && evictionReload, "JPEG pinch workload");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = smallestSufficientTier && upwardPromotion && repeatAndDownwardReuse
        && zoomOutAfterFull && noAvoidableUpscale && quality20 && quality35 && quality60 && quality100
        && metadataInvariant && smoothFamilies && directDrawParity && oddDimensions
        && boundaryDenominators && nearestRotateFullDecode && retainedBacking && evictionReload;
    System.out.println("fixture=ImageJpegPinchSmokeApp,smallestSufficientTier=" + smallestSufficientTier
        + ",upwardPromotion=" + upwardPromotion + ",repeatAndDownwardReuse=" + repeatAndDownwardReuse
        + ",zoomOutAfterFull=" + zoomOutAfterFull + ",noAvoidableUpscale=" + noAvoidableUpscale
        + ",quality20=" + quality20 + ",quality35=" + quality35 + ",quality60=" + quality60
        + ",quality100=" + quality100 + ",metadataInvariant=" + metadataInvariant + ",smoothFamilies="
        + smoothFamilies + ",directDrawParity=" + directDrawParity + ",oddDimensions=" + oddDimensions
        + ",boundaryDenominators=" + boundaryDenominators + ",nearestRotateFullDecode="
        + nearestRotateFullDecode + ",retainedBacking=" + retainedBacking + ",evictionReload="
        + evictionReload + ",directDrawCount=" + directDrawCount + ",targetedDecodeCount="
        + targetedDecodeCount + ",fullDecodeCount=" + fullDecodeCount + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static int displayDimension(int original, double fraction) {
    return (int) Math.round(original * fraction);
  }

  private static boolean compareWithFullReference(byte[] jpeg, int width, int height, int kind,
      Image selected) throws Exception {
    Image full = new Image(jpeg);
    full.getPixels();
    Image reference;
    switch (kind) {
    case 1:
      reference = full.getSmoothScaledInstance(width, height).getAlphaInstance(-40).resolveForDrawing(1);
      break;
    case 2:
      reference = full.getClippedInstance(0, 0, 512, 384).getSmoothScaledInstance(width, height)
          .resolveForDrawing(1);
      break;
    case 3:
      reference = full.getSmoothScaledInstance(410, 308).getSmoothScaledInstance(width, height)
          .resolveForDrawing(1);
      break;
    default:
      reference = full.getSmoothScaledInstance(width, height).resolveForDrawing(1);
      break;
    }
    return similarPixels(selected.getPixels(), reference.getPixels());
  }

  private static boolean similarPixels(int[] selected, int[] reference) {
    if (selected.length != reference.length) {
      return false;
    }
    int maxChannelDifference = 0;
    for (int i = 0; i < selected.length; i++) {
      int selectedPixel = selected[i];
      int referencePixel = reference[i];
      maxChannelDifference = Math.max(maxChannelDifference,
          Math.max(Math.abs(((selectedPixel >> 16) & 0xFF) - ((referencePixel >> 16) & 0xFF)),
              Math.max(Math.abs(((selectedPixel >> 8) & 0xFF) - ((referencePixel >> 8) & 0xFF)),
                  Math.abs((selectedPixel & 0xFF) - (referencePixel & 0xFF)))));
    }
    if (maxChannelDifference > 64) {
      return false;
    }
    int[] samplePoints = {0, selected.length / 3, selected.length / 2, selected.length - 1};
    for (int point : samplePoints) {
      int selectedPixel = selected[point];
      int referencePixel = reference[point];
      if (Math.abs(((selectedPixel >> 16) & 0xFF) - ((referencePixel >> 16) & 0xFF)) > 64
          || Math.abs(((selectedPixel >> 8) & 0xFF) - ((referencePixel >> 8) & 0xFF)) > 64
          || Math.abs((selectedPixel & 0xFF) - (referencePixel & 0xFF)) > 64) {
        return false;
      }
    }
    return true;
  }

  private static boolean samePixels(int[] first, int[] second) {
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

  private static byte[] createJpeg() throws Exception {
    return createJpeg(SOURCE_WIDTH, SOURCE_HEIGHT);
  }

  private static byte[] createJpeg(int width, int height) throws Exception {
    Image image = new Image(width, height);
    Graphics graphics = image.getGraphics();
    for (int y = 0; y < height; y += 32) {
      for (int x = 0; x < width; x += 32) {
        graphics.foreColor = 0xFF000000 | (((x * 7 + y * 3) & 0xFF) << 16)
            | (((x * 5 + y * 11) & 0xFF) << 8) | ((x * 13 + y * 17) & 0xFF);
        graphics.fillRect(x, y, 32, 32);
      }
    }
    ByteArrayStream stream = new ByteArrayStream(width * height);
    image.createJpg(stream, 82);
    byte[] jpeg = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, jpeg, 0, jpeg.length);
    return jpeg;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
