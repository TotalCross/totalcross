// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Deployed macOS smoke for JPEG adaptive decode and ImageModifier-like redraws. */
public class ImageJpegModifierSmokeApp extends MainWindow {
  private static final String SOURCE = "image-abi/lena1960.jpg";
  private static final String SMALL_SOURCE = "image-abi/lena512.jpg";
  private static final int[][] SIZES = {{180, 180}, {320, 320}, {640, 640}, {1280, 1280}};

  @Override
  public void initUI() {
    boolean loaded = false;
    boolean modifierDraw = false;
    boolean presentationReuse = false;
    boolean adaptivePromotion = false;
    boolean downwardReuse = false;
    boolean directDraw = false;
    boolean exactModifierPixels = false;
    boolean smallLenaParity = false;
    boolean noMaterialization = false;
    boolean structuralReuse = false;
    int targetedDecodeCount = -1;
    int fullDecodeCount = -1;
    int tier = -1;
    int structuralPlanBuilds = -1;
    int capabilityArrayAllocations = -1;
    int cacheHits = -1;
    String error = "";

    try {
      byte[] jpeg = Vm.getFile(SOURCE);
      byte[] smallJpeg = Vm.getFile(SMALL_SOURCE);
      Image smallExpected = new Image(smallJpeg).getSmoothScaledInstance(96, 96)
          .getTouchedUpInstance((byte) -7, (byte) 11).resolveForDrawing(1);
      Image smallActual = new Image(smallJpeg).getSmoothScaledInstance(96, 96)
          .getTouchedUpInstance((byte) -7, (byte) 11);
      smallLenaParity = sameSamples(drawToPixels(smallActual), drawToPixels(smallExpected));
      require(smallLenaParity, "small Lena parity");
      Image reference = new Image(jpeg).getSmoothScaledInstance(SIZES[1][0], SIZES[1][1])
          .getTouchedUpInstance((byte) 9, (byte) -5).resolveForDrawing(1);
      int[] referencePixels = drawToPixels(reference);
      Image.resetImageOperationAccountingForTest();
      Image root = new Image(jpeg);
      EncodedImageSource source = (EncodedImageSource) root.pipelineForSmoke().root();
      loaded = source.getIntrinsicWidth() == 1960 && source.getIntrinsicHeight() == 1960
          && root.getPixelWidth() == 1960 && root.getPixelHeight() == 1960;
      require(loaded, "JPEG dimensions");

      Image modifier = root.getSmoothScaledInstance(SIZES[0][0], SIZES[0][1])
          .getTouchedUpInstance((byte) 18, (byte) -12);
      int[] first = drawToPixels(modifier);
      int firstChecksum = checksum(first);
      int buildsBeforeRepeat = Image.imageDrawPlanCreatedCountForTest();
      int capabilitiesBeforeRepeat = Image.imageDrawPlanCapabilitiesAllocatedCountForTest();
      for (int i = 0; i < 20; i++) {
        require(checksum(drawToPixels(modifier)) == firstChecksum, "stable modifier redraw");
      }
      modifierDraw = first.length == SIZES[0][0] * SIZES[0][1] && firstChecksum != 0;

      modifier.alphaMask = 160;
      int[] alphaPixels = drawToPixels(modifier);
      modifier.alphaMask = 255;
      int[] restoredPixels = drawToPixels(modifier);
      presentationReuse = alphaPixels.length == restoredPixels.length
          && (alphaPixels[0] >>> 24) == 160 && checksum(restoredPixels) == firstChecksum;
      presentationReuse = presentationReuse
          && Image.imageDrawPlanCreatedCountForTest() == buildsBeforeRepeat
          && Image.imageDrawPlanCapabilitiesAllocatedCountForTest() == capabilitiesBeforeRepeat;

      Image[] pinchStates = new Image[SIZES.length];
      int previousTier = Integer.MAX_VALUE;

      for (int i = 0; i < SIZES.length; i++) {
        pinchStates[i] = root.getSmoothScaledInstance(SIZES[i][0], SIZES[i][1])
            .getTouchedUpInstance((byte) (i * 9), (byte) (-i * 5));
        int[] statePixels = drawToPixels(pinchStates[i]);
        if (i == 1) {
          exactModifierPixels = sameSamples(statePixels, referencePixels);
        }
        int currentTier = source.decodedDenominator();
        require(currentTier > 0 && currentTier <= previousTier, "monotonic JPEG tier");
        previousTier = currentTier;
      }
      targetedDecodeCount = Image.targetedDecodeInvocationCountForTest();
      fullDecodeCount = Image.fullDecodeInvocationCountForTest();
      tier = source.decodedDenominator();
      adaptivePromotion = targetedDecodeCount > 0 && fullDecodeCount == 1 && tier == 1;

      int fullDecodeBeforeDownward = Image.fullDecodeInvocationCountForTest();
      int targetedDecodeBeforeDownward = Image.targetedDecodeInvocationCountForTest();
      int stableAfterPromotion = checksum(drawToPixels(pinchStates[1]));
      downwardReuse = source.decodedDenominator() == 1
          && Image.fullDecodeInvocationCountForTest() == fullDecodeCount
          && Image.targetedDecodeInvocationCountForTest() == targetedDecodeCount
          && stableAfterPromotion == checksum(drawToPixels(pinchStates[1]))
          && fullDecodeBeforeDownward == fullDecodeCount
          && targetedDecodeBeforeDownward == targetedDecodeCount;

      structuralReuse = Image.imageDrawPlanCreatedCountForTest() >= buildsBeforeRepeat
          && Image.imageDrawPlanCapabilitiesAllocatedCountForTest()
              == Image.imageDrawPlanCreatedCountForTest()
          && Image.imageDrawPlanCacheHitCountForTest() >= 20;
      structuralPlanBuilds = Image.imageDrawPlanCreatedCountForTest();
      capabilityArrayAllocations = Image.imageDrawPlanCapabilitiesAllocatedCountForTest();
      cacheHits = Image.imageDrawPlanCacheHitCountForTest();
      noMaterialization = Image.nativeGeometryMaterializationCountForTest() == 0
          && Image.nativeColorReadbackCountForTest() == 0
          && Image.presentationOnlyPlanRecreationCountForTest() == 0;
      directDraw = Image.directDrawPlanExecutionCountForTest() >= 20 + SIZES.length * 2;
      modifierDraw = modifierDraw && source.decodedWidth() >= SIZES[3][0]
          && source.decodedHeight() >= SIZES[3][1];
      require(modifierDraw && presentationReuse && adaptivePromotion && downwardReuse
          && exactModifierPixels && noMaterialization && structuralReuse && directDraw,
          "JPEG modifier workload");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = loaded && modifierDraw && presentationReuse && adaptivePromotion
        && downwardReuse && directDraw && exactModifierPixels && noMaterialization && structuralReuse;
    System.out.println("fixture=ImageJpegModifierSmokeApp,loaded=" + loaded + ",modifierDraw=" + modifierDraw
        + ",presentationReuse=" + presentationReuse + ",adaptivePromotion=" + adaptivePromotion
        + ",downwardReuse=" + downwardReuse + ",directDraw=" + directDraw
        + ",exactModifierPixels=" + exactModifierPixels + ",smallLenaParity=" + smallLenaParity
        + ",noMaterialization=" + noMaterialization
        + ",structuralReuse=" + structuralReuse + ",targetedDecodeCount=" + targetedDecodeCount
        + ",fullDecodeCount=" + fullDecodeCount + ",tier=" + tier + ",structuralPlanBuilds="
        + structuralPlanBuilds + ",capabilityArrayAllocations=" + capabilityArrayAllocations
        + ",cacheHits=" + cacheHits + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static int[] drawToPixels(Image image) throws Exception {
    Image target = new Image(image.getPixelWidth(), image.getPixelHeight());
    target.getGraphics().drawImage(image, 0, 0, true);
    return target.getPixels();
  }

  private static int checksum(int[] pixels) {
    int result = 1;
    for (int pixel : pixels) {
      result = 31 * result + pixel;
    }
    return result;
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

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
