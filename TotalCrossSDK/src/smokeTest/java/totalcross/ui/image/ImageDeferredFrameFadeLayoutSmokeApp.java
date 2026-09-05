// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for frame-scoped APPLY_FADE after deferred FRAME_LAYOUT. */
public class ImageDeferredFrameFadeLayoutSmokeApp extends MainWindow {
  private static final int FRAME_ZERO = 0xFFFF0000;
  private static final int FRAME_ONE = 0xFF0000FF;
  private static final int FADE = 128;

  @Override
  public void initUI() {
    boolean deferredFrameZero = false;
    boolean deferredFrameOne = false;
    boolean eagerFrameZero = false;
    boolean eagerFrameOne = false;
    boolean eagerParity = false;
    boolean fadeScoped = false;
    boolean fadeThenSelectZero = false;
    boolean fadeThenSelectOne = false;
    boolean rootFadeThenSelectZero = false;
    boolean rootFadeThenSelectOne = false;
    boolean selectFadeSelectZero = false;
    boolean selectFadeSelectOne = false;
    boolean fadeThenLayout = false;
    boolean selectSelectZero = false;
    boolean selectSelectOne = false;
    boolean selectScaleSelectZero = false;
    boolean layoutSelectZero = false;
    boolean layoutSelectOne = false;
    String error = "";

    try {
      byte[] encoded = singleFrameStrip();
      Image eager = new Image(encoded);
      eager.getPixels();
      eager.setFrameCount(2);
      eager.setCurrentFrame(1);
      eager.applyFade(FADE);
      eager.setCurrentFrame(0);
      int[] expectedZero = drawToPixels(eager);
      eager.setCurrentFrame(1);
      int[] expectedOne = drawToPixels(eager);
      eagerFrameZero = expectedZero[0] == FRAME_ZERO;
      eagerFrameOne = expectedOne[0] == 0xFF000080;

      Image deferred = new Image(encoded);
      require(deferred.getFrameCount() == 1, "single-frame encoded source");
      deferred.setFrameCount(2);
      deferred.setCurrentFrame(1);
      deferred.applyFade(FADE);
      deferred.setCurrentFrame(0);
      int[] actualZero = drawToPixels(deferred);
      deferred.setCurrentFrame(1);
      int[] actualOne = drawToPixels(deferred);
      deferredFrameZero = actualZero[0] == FRAME_ZERO;
      deferredFrameOne = actualOne[0] == 0xFF000080;
      eagerParity = same(actualZero, expectedZero) && same(actualOne, expectedOne);
      fadeScoped = deferredFrameZero && deferredFrameOne && eagerParity;
      require(fadeScoped, "frame-scoped deferred fade");

      fadeThenSelectZero = fadeThenSelect(encoded, 0, FRAME_ZERO);
      fadeThenSelectOne = fadeThenSelect(encoded, 1, 0xFF000080);
      require(fadeThenSelectZero && fadeThenSelectOne, "fade then frame selection");

      byte[] encodedFrames = twoFrameStrip();
      rootFadeThenSelectZero = fadeThenSelect(encodedFrames, 0, FRAME_ZERO);
      rootFadeThenSelectOne = fadeThenSelect(encodedFrames, 1, 0xFF000080);
      require(rootFadeThenSelectZero && rootFadeThenSelectOne, "root fade then frame selection");

      selectFadeSelectZero = selectFadeSelect(encodedFrames, 0, 0xFF000080);
      selectFadeSelectOne = selectFadeSelect(encodedFrames, 1, 0xFF000080);
      require(selectFadeSelectZero && selectFadeSelectOne, "frame selection fade frame selection");

      fadeThenLayout = fadeThenLayout(encoded, 0xFF800000);
      require(fadeThenLayout, "fade then frame layout");

      selectSelectZero = selectSelect(encodedFrames, 0);
      selectSelectOne = selectSelect(encodedFrames, 1);
      require(selectSelectZero && selectSelectOne, "repeated frame selection");

      selectScaleSelectZero = selectScaleSelect(encodedFrames);
      require(selectScaleSelectZero, "scaled repeated frame selection");

      layoutSelectZero = layoutSelect(encoded, 0);
      layoutSelectOne = layoutSelect(encoded, 1);
      require(layoutSelectZero && layoutSelectOne, "frame layout selection");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = deferredFrameZero && deferredFrameOne && eagerFrameZero && eagerFrameOne
        && eagerParity && fadeScoped && fadeThenSelectZero && fadeThenSelectOne
        && rootFadeThenSelectZero && rootFadeThenSelectOne && selectFadeSelectZero
        && selectFadeSelectOne && fadeThenLayout && selectSelectZero && selectSelectOne
        && selectScaleSelectZero && layoutSelectZero && layoutSelectOne;
    System.out.println("fixture=ImageDeferredFrameFadeLayoutSmokeApp,deferredFrameZero=" + deferredFrameZero
        + ",deferredFrameOne=" + deferredFrameOne + ",eagerFrameZero=" + eagerFrameZero
        + ",eagerFrameOne=" + eagerFrameOne + ",eagerParity=" + eagerParity + ",fadeScoped="
        + fadeScoped + ",fadeThenSelectZero=" + fadeThenSelectZero + ",fadeThenSelectOne="
        + fadeThenSelectOne + ",rootFadeThenSelectZero=" + rootFadeThenSelectZero
        + ",rootFadeThenSelectOne=" + rootFadeThenSelectOne + ",overallPass=" + overallPass
        + ",selectFadeSelectZero=" + selectFadeSelectZero + ",selectFadeSelectOne="
        + selectFadeSelectOne + ",fadeThenLayout=" + fadeThenLayout
        + ",selectSelectZero=" + selectSelectZero + ",selectSelectOne=" + selectSelectOne
        + ",selectScaleSelectZero=" + selectScaleSelectZero + ",layoutSelectZero="
        + layoutSelectZero + ",layoutSelectOne=" + layoutSelectOne
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static byte[] singleFrameStrip() throws Exception {
    Image image = new Image(8, 2);
    Graphics graphics = image.getGraphics();
    graphics.backColor = FRAME_ZERO & 0x00FFFFFF;
    graphics.fillRect(0, 0, 4, 2);
    graphics.backColor = FRAME_ONE & 0x00FFFFFF;
    graphics.fillRect(4, 0, 4, 2);
    ByteArrayStream stream = new ByteArrayStream(512);
    image.createPng(stream);
    byte[] result = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, result, 0, result.length);
    return result;
  }

  private static byte[] twoFrameStrip() throws Exception {
    Image image = new Image(8, 2);
    Graphics graphics = image.getGraphics();
    graphics.backColor = FRAME_ZERO & 0x00FFFFFF;
    graphics.fillRect(0, 0, 4, 2);
    graphics.backColor = FRAME_ONE & 0x00FFFFFF;
    graphics.fillRect(4, 0, 4, 2);
    image.setFrameCount(2);
    ByteArrayStream stream = new ByteArrayStream(512);
    image.createPng(stream);
    byte[] result = new byte[stream.getPos()];
    Vm.arrayCopy(stream.getBuffer(), 0, result, 0, result.length);
    return result;
  }

  private static int[] drawToPixels(Image image) throws Exception {
    Image target = new Image(image.getPixelWidth(), image.getPixelHeight());
    target.getGraphics().drawImage(image, 0, 0, true);
    return target.getPixels();
  }

  private static boolean fadeThenSelect(byte[] encoded, int selectedFrame, int expectedPixel)
      throws Exception {
    Image eager = new Image(encoded);
    eager.getPixels();
    eager.setFrameCount(2);
    eager.setCurrentFrame(1);
    eager.applyFade(FADE);
    eager.setCurrentFrame(selectedFrame);

    Image deferred = new Image(encoded);
    deferred.setFrameCount(2);
    deferred.setCurrentFrame(1);
    deferred.applyFade(FADE);
    Image selected = deferred.getFrameInstance(selectedFrame);
    int[] actual = drawToPixels(selected);
    int[] expected = eager.getPixels();
    return actual.length == 8 && actual[0] == expectedPixel && same(actual, expected);
  }

  private static boolean selectFadeSelect(byte[] encoded, int finalFrame, int expectedPixel)
      throws Exception {
    Image eagerRoot = new Image(encoded);
    eagerRoot.getPixels();
    eagerRoot.setFrameCount(2);
    eagerRoot.setCurrentFrame(1);
    Image eagerSelected = eagerRoot.getFrameInstance(1);
    eagerSelected.getPixels();
    eagerSelected.applyFade(FADE);
    eagerSelected.setCurrentFrame(finalFrame);
    int[] expected = eagerSelected.getPixels();

    Image deferred = new Image(encoded);
    deferred.setFrameCount(2);
    Image selected = deferred.getFrameInstance(1);
    selected.applyFade(FADE);
    Image finalSelection = selected.getFrameInstance(finalFrame);
    int[] actual = drawToPixels(finalSelection);
    boolean sequence = hasOperationSequence(finalSelection, ImagePipeline.FRAME_SELECT,
        ImagePipeline.APPLY_FADE, ImagePipeline.FRAME_SELECT);
    return actual.length == 8 && actual[0] == expectedPixel && same(actual, expected) && sequence;
  }

  private static boolean fadeThenLayout(byte[] encoded, int expectedPixel) throws Exception {
    Image eager = new Image(encoded);
    eager.getPixels();
    eager.applyFade(FADE);
    eager.setFrameCount(2);
    int[] expected = eager.getPixels();

    Image deferred = new Image(encoded);
    deferred.applyFade(FADE);
    deferred.setFrameCount(2);
    int[] actual = drawToPixels(deferred);
    return actual.length == 8 && actual[0] == expectedPixel && same(actual, expected)
        && hasOperationSequence(deferred, ImagePipeline.APPLY_FADE, ImagePipeline.FRAME_LAYOUT);
  }

  private static boolean selectSelect(byte[] encoded, int finalFrame) throws Exception {
    Image eagerRoot = new Image(encoded);
    eagerRoot.getPixels();
    eagerRoot.setFrameCount(2);
    Image eagerSelected = eagerRoot.getFrameInstance(1);
    int[] expected = eagerSelected.getPixels();

    Image deferred = new Image(encoded);
    deferred.setFrameCount(2);
    Image selected = deferred.getFrameInstance(1);
    Image finalSelection = selected.getFrameInstance(finalFrame);
    int[] actual = drawToPixels(finalSelection);
    return actual.length == expected.length && same(actual, expected)
        && hasOperationSequence(finalSelection, ImagePipeline.FRAME_SELECT,
            ImagePipeline.FRAME_SELECT) && finalSelection.drawPlanForDrawing(1) == null;
  }

  private static boolean selectScaleSelect(byte[] encoded) throws Exception {
    Image eagerRoot = new Image(encoded);
    eagerRoot.getPixels();
    eagerRoot.setFrameCount(2);
    Image eagerSelected = eagerRoot.getFrameInstance(1);
    eagerSelected.getPixels();
    Image eagerScaled = eagerSelected.getScaledInstance(2, 2);
    int[] expected = eagerScaled.getPixels();

    Image deferred = new Image(encoded);
    deferred.setFrameCount(2);
    Image selected = deferred.getFrameInstance(1);
    Image scaled = selected.getScaledInstance(2, 2);
    Image finalSelection = scaled.getFrameInstance(0);
    int[] actual = drawToPixels(finalSelection);
    return actual.length == expected.length && same(actual, expected)
        && hasOperationSequence(finalSelection, ImagePipeline.FRAME_SELECT, ImagePipeline.SCALE,
            ImagePipeline.FRAME_SELECT) && finalSelection.drawPlanForDrawing(1) == null;
  }

  private static boolean layoutSelect(byte[] encoded, int finalFrame) throws Exception {
    Image eager = new Image(encoded);
    eager.getPixels();
    eager.setFrameCount(2);
    eager.setCurrentFrame(finalFrame);
    int[] expected = eager.getPixels();

    Image deferred = new Image(encoded);
    deferred.setFrameCount(2);
    Image selected = deferred.getFrameInstance(finalFrame);
    int[] actual = drawToPixels(selected);
    return actual.length == expected.length && same(actual, expected)
        && hasOperationSequence(selected, ImagePipeline.FRAME_LAYOUT, ImagePipeline.FRAME_SELECT)
        && selected.drawPlanForDrawing(1) == null;
  }

  private static boolean hasOperationSequence(Image image, int... operations) {
    ImagePipeline node = image.pipelineForSmoke();
    for (int i = operations.length - 1; i >= 0; i--) {
      if (node == null || node.previous() == null || node.operationType() != operations[i]) {
        return false;
      }
      node = node.previous();
    }
    return true;
  }

  private static boolean same(int[] first, int[] second) {
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
}
