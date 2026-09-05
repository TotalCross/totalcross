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
    boolean evictionReload = false;
    boolean retainedBacking = false;
    int targetedDecodeCount = -1;
    int fullDecodeCount = -1;
    String error = "";

    try {
      byte[] jpeg = createJpeg();
      Image root = new Image(jpeg);
      EncodedImageSource source = (EncodedImageSource) root.pipelineForSmoke().root();
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
          && compareWithForcedReference(jpeg, 205, 154, 4, quality20Image);
      Image quality35Image = qualityRoot.getSmoothScaledInstance(358, 269).resolveForDrawing(1);
      quality35 = qualitySource.decodedDenominator() == 2
          && qualitySource.decodedWidth() >= 358 && compareWithForcedReference(jpeg, 358, 269, 2, quality35Image);
      Image quality60Image = qualityRoot.getSmoothScaledInstance(614, 461).resolveForDrawing(1);
      quality60 = qualitySource.decodedDenominator() == 1
          && qualitySource.decodedWidth() >= 614 && compareWithForcedReference(jpeg, 614, 461, 1, quality60Image);
      Image quality100Image = qualityRoot.getSmoothScaledInstance(SOURCE_WIDTH, SOURCE_HEIGHT)
          .resolveForDrawing(1);
      quality100 = qualitySource.decodedDenominator() == 1
          && quality100Image.getPixelWidth() == SOURCE_WIDTH
          && quality100Image.getPixelHeight() == SOURCE_HEIGHT
          && compareWithForcedReference(jpeg, SOURCE_WIDTH, SOURCE_HEIGHT, 1, quality100Image);
      noAvoidableUpscale = quality35 && quality60;

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
          && noAvoidableUpscale && quality20 && quality35 && quality60 && quality100 && retainedBacking
          && evictionReload, "JPEG pinch workload");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = smallestSufficientTier && upwardPromotion && repeatAndDownwardReuse
        && zoomOutAfterFull && noAvoidableUpscale && quality20 && quality35 && quality60 && quality100
        && retainedBacking && evictionReload;
    System.out.println("fixture=ImageJpegPinchSmokeApp,smallestSufficientTier=" + smallestSufficientTier
        + ",upwardPromotion=" + upwardPromotion + ",repeatAndDownwardReuse=" + repeatAndDownwardReuse
        + ",zoomOutAfterFull=" + zoomOutAfterFull + ",noAvoidableUpscale=" + noAvoidableUpscale
        + ",quality20=" + quality20 + ",quality35=" + quality35 + ",quality60=" + quality60
        + ",quality100=" + quality100 + ",retainedBacking=" + retainedBacking + ",evictionReload="
        + evictionReload + ",targetedDecodeCount=" + targetedDecodeCount + ",fullDecodeCount="
        + fullDecodeCount + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static int displayDimension(int original, double fraction) {
    return (int) Math.round(original * fraction);
  }

  private static boolean compareWithForcedReference(byte[] jpeg, int width, int height, int denominator,
      Image selected) throws Exception {
    Image direct = Image.decodeJpegAtDenominatorForTest(jpeg, denominator)
        .getSmoothScaledInstance(width, height);
    int[] selectedPixels = selected.getPixels();
    Image target = new Image(width, height);
    target.getGraphics().drawImage(direct, 0, 0, true);
    int[] directPixels = target.getPixels();
    if (selectedPixels.length != directPixels.length) {
      return false;
    }
    int[] samplePoints = {0, width / 3, width - 1, (height / 2) * width + width / 2,
        (height - 1) * width + width / 4};
    for (int point : samplePoints) {
      if (selectedPixels[point] != directPixels[point]) {
        return false;
      }
    }
    return true;
  }

  private static byte[] createJpeg() throws Exception {
    Image image = new Image(SOURCE_WIDTH, SOURCE_HEIGHT);
    Graphics graphics = image.getGraphics();
    for (int y = 0; y < SOURCE_HEIGHT; y += 32) {
      for (int x = 0; x < SOURCE_WIDTH; x += 32) {
        graphics.foreColor = 0xFF000000 | (((x * 7 + y * 3) & 0xFF) << 16)
            | (((x * 5 + y * 11) & 0xFF) << 8) | ((x * 13 + y * 17) & 0xFF);
        graphics.fillRect(x, y, 32, 32);
      }
    }
    ByteArrayStream stream = new ByteArrayStream(SOURCE_WIDTH * SOURCE_HEIGHT);
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
