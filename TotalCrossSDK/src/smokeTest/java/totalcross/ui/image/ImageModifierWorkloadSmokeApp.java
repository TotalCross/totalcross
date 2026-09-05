// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Deployed macOS smoke for the ImageModifier live-slider pipeline orderings. */
public class ImageModifierWorkloadSmokeApp extends MainWindow {
  private static final String DEFAULT_SOURCE = "image-abi/lena1960.jpg";
  private static final String TRANSPARENT_SOURCE = "image-abi/tiny.png";
  private static final int ROTATE_FILL = 0xFF102030;

  private static final int[] BRIGHTNESS_SESSION_A = {
      8, 16, 24, 32, 24, 16, 8, 0, -8, -16, -24, -32, -24, -16, -8, 0
  };
  private static final int[] BRIGHTNESS_SESSION_B = {
      -16, -32, -48, -32, -16, 0, 16, 32, 48, 32, 16, 0, 1, 24, 40, 24, 8, 0
  };
  private static final int[] CONTRAST_SESSION_A = {
      8, 16, 24, 32, 24, 16, 8, 0, -8, -16, -24, -32, -24, -16, -8, 0
  };
  private static final int[] CONTRAST_SESSION_B = {
      -24, -48, -72, -48, -24, 0, 24, 48, 72, 48, 24, 0, 16, 32, 16, 0
  };
  private static final int[] ROTATE_SESSION_A = {
      8, 16, 24, 32, 40, 32, 24, 16, 8, 0, -8, -16, -24, -32, -40, -32,
      -24, -16, -8, 0, 12, 24, 36, 48, 36, 24, 12, 0
  };
  private static final int[] ROTATE_SESSION_B = {
      -12, -24, -36, -48, -36, -24, -12, 0, 15, 30, 45, 30, 15, 0,
      -15, -30, -45, -30, -15, 0
  };
  private static final int[] SCALE_SESSION_A = {
      24, 28, 32, 36, 40, 36, 32, 28, 24, 20, 16, 12, 16, 20, 24, 28,
      32, 36, 40, 32, 24, 20
  };
  private static final int[] SCALE_SESSION_B = {
      18, 16, 14, 12, 10, 12, 14, 16, 18, 22, 26, 30, 34, 30, 26, 22, 18, 20
  };

  private static final class SliderState {
    int rotate;
    int scale = 20;
    int brightness;
    int contrast;
  }

  @Override
  public void initUI() {
    String fixture = fixtureFromCommandLine();
    boolean brightnessContrast = false;
    boolean transparentFill = false;
    boolean exactPixels = false;
    boolean brightnessPlusOne = false;
    boolean siblingDecode = false;
    boolean noPersistentSliderBacking = false;
    boolean boundedCaches = false;
    boolean rotateScaleWorkload = false;
    boolean fillAfterTouchUp = false;
    boolean fusedDraw = false;
    boolean brightnessTrajectory = false;
    boolean contrastTrajectory = false;
    boolean rotateTrajectory = false;
    boolean scaleTrajectory = false;
    boolean repeatedAdjustmentSessions = false;
    int fullDecodeCount = -1;
    int targetedDecodeCount = -1;
    int directDrawCount = -1;
    int nativeColorReadbackCount = -1;
    int sliderEvents = -1;
    int drawCount = -1;
    int fixtureWidth = -1;
    int fixtureHeight = -1;
    String fixtureFormat = "unknown";
    String finalDecodeTier = "N/A";
    String error = "";

    try {
      byte[] encoded = Vm.getFile(fixture);
      byte[] transparentEncoded = Vm.getFile(TRANSPARENT_SOURCE);
      require(encoded != null && encoded.length > 0, "encoded fixture");
      require(transparentEncoded != null && transparentEncoded.length > 0, "transparent fixture");

      Image expectedRoot = new Image(encoded);
      expectedRoot.getPixels();
      Image expected = expectedRoot.getRotatedScaledInstance(20, 0, 0)
          .getTouchedUpInstance((byte) 16, (byte) 0);
      int[] expectedPixels = expected.getPixels();
      int[] expectedBrightnessOnePixels = expectedRoot.getRotatedScaledInstance(20, 0, 0)
          .getTouchedUpInstance((byte) 1, (byte) 0).getPixels();
      int[] transparentPixels = new Image(transparentEncoded)
          .getTouchedUpInstance((byte) -32, (byte) -16).getPixels();

      Image.resetImageOperationAccountingForTest();
      Image root = new Image(encoded);
      fixtureWidth = root.getPixelWidth();
      fixtureHeight = root.getPixelHeight();
      EncodedImageSource encodedSource = (EncodedImageSource) root.pipelineForSmoke().root();
      fixtureFormat = encodedSource.getFormat().name();
      SliderState state = new SliderState();
      boolean[] parity = {false};
      boolean[] brightnessOne = {false};
      boolean[] fill = {false};

      Image rotated = root.getRotatedScaledInstance(state.scale, state.rotate, 0);
      require(rotated.pipelineForSmoke() != null, "rotated prefix deferred");
      int brightnessSteps = drawColorTrajectory(rotated, state, BRIGHTNESS_SESSION_A, true,
          expectedPixels, expectedBrightnessOnePixels, parity, brightnessOne);
      int contrastSteps = drawColorTrajectory(rotated, state, CONTRAST_SESSION_A, false,
          expectedPixels, expectedBrightnessOnePixels, parity, brightnessOne);
      Image rotatedAgain = root.getRotatedScaledInstance(state.scale, state.rotate, 0);
      brightnessSteps += drawColorTrajectory(rotatedAgain, state, BRIGHTNESS_SESSION_B, true,
          expectedPixels, expectedBrightnessOnePixels, parity, brightnessOne);
      contrastSteps += drawColorTrajectory(rotatedAgain, state, CONTRAST_SESSION_B, false,
          expectedPixels, expectedBrightnessOnePixels, parity, brightnessOne);

      Image contrasted = root.getTouchedUpInstance((byte) state.brightness, (byte) state.contrast);
      require(contrasted.pipelineForSmoke() != null, "contrasted prefix deferred");
      int rotateSteps = drawGeometryTrajectory(contrasted, state, ROTATE_SESSION_A, true, fill);
      int scaleSteps = drawGeometryTrajectory(contrasted, state, SCALE_SESSION_A, false, fill);
      Image contrastedAgain = root.getTouchedUpInstance((byte) state.brightness, (byte) state.contrast);
      rotateSteps += drawGeometryTrajectory(contrastedAgain, state, ROTATE_SESSION_B, true, fill);
      scaleSteps += drawGeometryTrajectory(contrastedAgain, state, SCALE_SESSION_B, false, fill);

      ImagePipeline rootPipeline = root.pipelineForSmoke();
      ImagePipeline rotatedPipeline = rotated.pipelineForSmoke();
      ImagePipeline contrastedPipeline = contrasted.pipelineForSmoke();
      sliderEvents = brightnessSteps + contrastSteps + rotateSteps + scaleSteps;
      drawCount = sliderEvents;
      fullDecodeCount = Image.fullDecodeInvocationCountForTest();
      targetedDecodeCount = Image.targetedDecodeInvocationCountForTest();
      directDrawCount = Image.directDrawPlanExecutionCountForTest();
      nativeColorReadbackCount = Image.nativeColorReadbackCountForTest();
      if ("JPEG".equals(fixtureFormat)) {
        finalDecodeTier = String.valueOf(encodedSource.decodedDenominator());
      }
      brightnessTrajectory = crossesZero(BRIGHTNESS_SESSION_A)
          && crossesZero(BRIGHTNESS_SESSION_B) && hasBacktracking(BRIGHTNESS_SESSION_A);
      contrastTrajectory = crossesZero(CONTRAST_SESSION_A)
          && crossesZero(CONTRAST_SESSION_B) && hasBacktracking(CONTRAST_SESSION_B);
      rotateTrajectory = crossesZero(ROTATE_SESSION_A)
          && crossesZero(ROTATE_SESSION_B) && hasBacktracking(ROTATE_SESSION_A);
      scaleTrajectory = hasBacktracking(SCALE_SESSION_A) && hasBacktracking(SCALE_SESSION_B)
          && min(SCALE_SESSION_A) < 20 && max(SCALE_SESSION_A) > 20;
      repeatedAdjustmentSessions = brightnessSteps > 20 && contrastSteps > 20
          && rotateSteps > 20 && scaleSteps > 20;
      siblingDecode = fullDecodeCount == 1;
      noPersistentSliderBacking = rootPipeline != null && rotatedPipeline != null
          && contrastedPipeline != null && rotatedAgain.pipelineForSmoke() != null
          && contrastedAgain.pipelineForSmoke() != null;
      boundedCaches = rotatedPipeline.cachedVariantCountForSmoke() <= 2
          && contrastedPipeline.cachedVariantCountForSmoke() <= 2;
      transparentFill = hasAlpha(transparentPixels, 0);
      exactPixels = parity[0];
      brightnessPlusOne = brightnessOne[0];
      rotateScaleWorkload = rotateSteps > 0 && scaleSteps > 0;
      fillAfterTouchUp = fill[0];
      fusedDraw = directDrawCount >= brightnessSteps + contrastSteps + rotateSteps + scaleSteps
          && nativeColorReadbackCount == 0;
      brightnessContrast = brightnessSteps > 0 && contrastSteps > 0;
      require(brightnessContrast && transparentFill && exactPixels && brightnessPlusOne && siblingDecode,
          "brightness/contrast workload");
      require(noPersistentSliderBacking && boundedCaches && brightnessTrajectory && contrastTrajectory,
          "slider cache ownership");
      require(rotateScaleWorkload && fillAfterTouchUp && fusedDraw && rotateTrajectory
          && scaleTrajectory && repeatedAdjustmentSessions, "rotate/scale workload");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = brightnessContrast && transparentFill && exactPixels && brightnessPlusOne
        && siblingDecode && noPersistentSliderBacking && boundedCaches && rotateScaleWorkload
        && fillAfterTouchUp && fusedDraw && brightnessTrajectory && contrastTrajectory
        && rotateTrajectory && scaleTrajectory && repeatedAdjustmentSessions;
    String fullJpegDecodes = "JPEG".equals(fixtureFormat) ? String.valueOf(fullDecodeCount) : "N/A";
    String targetedJpegDecodes = "JPEG".equals(fixtureFormat) ? String.valueOf(targetedDecodeCount) : "N/A";
    System.out.println("fixture=ImageModifierWorkloadSmokeApp,fixturePath=" + fixture
        + ",fixtureFormat=" + fixtureFormat + ",fixtureWidth=" + fixtureWidth
        + ",fixtureHeight=" + fixtureHeight + ",sliderEvents=" + sliderEvents
        + ",draws=" + drawCount + ",imageCreated=" + Image.imageCreatedCountForTest()
        + ",imagePipelineCreated=" + Image.imagePipelineCreatedCountForTest()
        + ",imageDrawPlanCreated=" + Image.imageDrawPlanCreatedCountForTest()
        + ",drawPlanCacheHits=" + Image.imageDrawPlanCacheHitCountForTest()
        + ",fullDecodeCount=" + fullDecodeCount + ",targetedDecodeCount=" + targetedDecodeCount
        + ",fullJpegDecodes=" + fullJpegDecodes + ",targetedJpegDecodes=" + targetedJpegDecodes
        + ",finalDecodeTier=" + finalDecodeTier
        + ",nativeBackingCreated=" + NativeImageBacking.backingRecordsCreatedForTest()
        + ",nativeBackingLive=" + NativeImageBacking.backingRecordsLiveForTest()
        + ",nativeBackingPeakLive=" + NativeImageBacking.backingRecordsPeakLiveForTest()
        + ",nativeBackingBytesLive=" + NativeImageBacking.backingBytesLiveForTest()
        + ",nativeBackingBytesPeakLive=" + NativeImageBacking.backingBytesPeakLiveForTest()
        + ",geometryMaterializations=" + Image.nativeGeometryMaterializationCountForTest()
        + ",nativeColorReadbacks=" + Image.nativeColorReadbackCountForTest()
        + ",backingReadbacks=" + Image.backingReadbackCountForTest()
        + ",directDrawPlanExecutions=" + directDrawCount
        + ",brightnessContrast=" + brightnessContrast
        + ",transparentFill=" + transparentFill + ",exactPixels=" + exactPixels
        + ",brightnessPlusOne=" + brightnessPlusOne + ",siblingDecode=" + siblingDecode
        + ",noPersistentSliderBacking=" + noPersistentSliderBacking + ",boundedCaches=" + boundedCaches
        + ",rotateScale=" + rotateScaleWorkload + ",fillAfterTouchUp=" + fillAfterTouchUp
        + ",fusedDraw=" + fusedDraw + ",brightnessTrajectory=" + brightnessTrajectory
        + ",contrastTrajectory=" + contrastTrajectory + ",rotateTrajectory=" + rotateTrajectory
        + ",scaleTrajectory=" + scaleTrajectory + ",repeatedAdjustmentSessions="
        + repeatedAdjustmentSessions + ",fullDecodeCount=" + fullDecodeCount + ",directDrawCount="
        + directDrawCount + ",nativeColorReadbackCount=" + nativeColorReadbackCount + ",overallPass="
        + overallPass + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static String fixtureFromCommandLine() {
    String commandLine = getCommandLine();
    return commandLine == null || commandLine.length() == 0 ? DEFAULT_SOURCE : commandLine;
  }

  private static int drawColorTrajectory(Image base, SliderState state, int[] values,
      boolean brightnessSlider, int[] expectedPixels, int[] expectedBrightnessOnePixels,
      boolean[] parity, boolean[] brightnessOne) throws Exception {
    int steps = 0;
    for (int value : values) {
      if (brightnessSlider && state.brightness == value || !brightnessSlider && state.contrast == value) {
        continue;
      }
      if (brightnessSlider) {
        state.brightness = value;
      } else {
        state.contrast = value;
      }
      Image displayed = base.getTouchedUpInstance((byte) state.brightness, (byte) state.contrast);
      int[] actual = drawToPixels(displayed);
      require(actual.length == displayed.getPixelWidth() * displayed.getPixelHeight(),
          "touch-up output dimensions");
      if (state.scale == 20 && state.rotate == 0 && state.brightness == 16 && state.contrast == 0) {
        parity[0] = sameSamples(actual, expectedPixels);
      }
      if (brightnessSlider && state.scale == 20 && state.rotate == 0 && state.brightness == 1
          && state.contrast == 0) {
        brightnessOne[0] = sameSamples(actual, expectedBrightnessOnePixels);
      }
      steps++;
    }
    return steps;
  }

  private static int drawGeometryTrajectory(Image base, SliderState state, int[] values,
      boolean rotateSlider, boolean[] fill) throws Exception {
    int steps = 0;
    for (int value : values) {
      if (rotateSlider && state.rotate == value || !rotateSlider && state.scale == value) {
        continue;
      }
      if (rotateSlider) {
        state.rotate = value;
      } else {
        state.scale = value;
      }
      Image displayed = base.getRotatedScaledInstance(state.scale, state.rotate, ROTATE_FILL);
      int[] actual = drawToPixels(displayed);
      require(actual.length == displayed.getPixelWidth() * displayed.getPixelHeight(),
          "rotate output dimensions");
      if (state.rotate == 45) {
        fill[0] = hasPixel(actual, ROTATE_FILL);
      }
      steps++;
    }
    return steps;
  }

  private static int[] drawToPixels(Image image) throws Exception {
    Image target = new Image(image.getPixelWidth(), image.getPixelHeight());
    target.getGraphics().drawImage(image, 0, 0, true);
    return target.getPixels();
  }

  private static boolean sameSamples(int[] first, int[] second) {
    if (first == null || second == null || first.length != second.length || first.length == 0) {
      return false;
    }
    int[] samplePoints = {0, first.length / 3, first.length / 2, first.length - 1};
    for (int point : samplePoints) {
      if (first[point] != second[point]) {
        return false;
      }
    }
    return true;
  }

  private static boolean crossesZero(int[] values) {
    return min(values) < 0 && max(values) > 0;
  }

  private static boolean hasBacktracking(int[] values) {
    for (int i = 1; i < values.length - 1; i++) {
      if ((values[i] > values[i - 1] && values[i + 1] < values[i])
          || (values[i] < values[i - 1] && values[i + 1] > values[i])) {
        return true;
      }
    }
    return false;
  }

  private static int min(int[] values) {
    int result = values[0];
    for (int value : values) {
      result = Math.min(result, value);
    }
    return result;
  }

  private static int max(int[] values) {
    int result = values[0];
    for (int value : values) {
      result = Math.max(result, value);
    }
    return result;
  }

  private static boolean hasAlpha(int[] pixels, int alpha) {
    for (int pixel : pixels) {
      if ((pixel >>> 24) == alpha) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasPixel(int[] pixels, int expected) {
    for (int pixel : pixels) {
      if (pixel == expected) {
        return true;
      }
    }
    return false;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
