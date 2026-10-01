// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.Arrays;

import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for lazy JPEG scaling factories and captured source lifetime. */
public class ImageLazyJpegFactorySmokeApp extends MainWindow {
  private static final String REPLACED_PATH = "p5-lazy-jpeg-replaced.jpg";
  private static final String DELETED_PATH = "p5-lazy-jpeg-deleted.jpg";
  private static final String CORRUPT_PATH = "p5-lazy-jpeg-corrupt.jpg";
  private static final String RETRY_PATH = "p5-lazy-jpeg-retry.jpg";
  private static final int RED = 0xFFFF0000;
  private static final int BLUE = 0xFF0000FF;
  private static final int GREEN = 0xFF00AA00;

  @Override
  public void initUI() {
    boolean bestFitLazy = false;
    boolean bestFitDraw = false;
    boolean explicitRatioLazy = false;
    boolean explicitRatioDraw = false;
    boolean replacementCapture = false;
    boolean deletionCapture = false;
    boolean deterministicCache = false;
    boolean transientRetry = false;
    boolean publicJavaRoute = false;
    boolean privateNativeDecode = false;
    int bestFitDecodeCalls = 0;
    int explicitDecodeCalls = 0;
    int bestFitSample = 0;
    int bestFitPixelCount = 0;
    String error = "";

    try {
      byte[] redJpeg = jpeg(RED);
      byte[] blueJpeg = jpeg(BLUE);
      byte[] greenJpeg = jpeg(GREEN);
      int[] expectedRed = Image.decodeJpegAtDenominatorForTest(redJpeg, 4).getPixels().clone();
      int[] expectedBlue = Image.decodeJpegAtDenominatorForTest(blueJpeg, 4).getPixels().clone();

      Image.resetImageOperationAccountingForTest();
      write(REPLACED_PATH, redJpeg);
      Image bestFit = Image.getJpegBestFit(REPLACED_PATH, 16, 8);
      bestFitLazy = isLazy(bestFit, ImageDecodePolicy.Mode.BEST_FIT, 16, 8)
          && Image.targetedDecodeInvocationCountForTest() == 0 && Image.fullDecodeInvocationCountForTest() == 0;
      require(bestFitLazy, "best-fit returns deferred image");

      write(REPLACED_PATH, blueJpeg);
      Image explicit = Image.getJpegScaled(REPLACED_PATH, 1, 4);
      explicitRatioLazy = isLazy(explicit, ImageDecodePolicy.Mode.EXPLICIT_RATIO, 16, 8)
          && Image.targetedDecodeInvocationCountForTest() == 0 && Image.fullDecodeInvocationCountForTest() == 0;
      require(explicitRatioLazy, "explicit ratio returns deferred image");
      write(REPLACED_PATH, greenJpeg);

      Graphics graphics = getGraphics();
      require(graphics != null, "window graphics");
      graphics.drawImage(bestFit, 2, 2);
      Image bestPixels = bestFit.resolveForDrawing(1);
      int[] bestPixelsData = bestPixels.getPixels();
      bestFitPixelCount = bestPixelsData.length;
      bestFitSample = bestPixelsData.length == 0 ? 0 : bestPixelsData[bestPixelsData.length / 2];
      bestFitDraw = Arrays.equals(expectedRed, bestPixelsData) && bestPixels.getPixelWidth() == 16
          && bestPixels.getPixelHeight() == 8;
      bestFitDecodeCalls = Image.targetedDecodeInvocationCountForTest();
      require(bestFitDraw, "best-fit later draw/materialization");

      Image.resetImageOperationAccountingForTest();
      graphics.drawImage(explicit, 24, 2);
      Image explicitPixels = explicit.resolveForDrawing(1);
      int[] explicitPixelsData = explicitPixels.getPixels();
      explicitRatioDraw = Arrays.equals(expectedBlue, explicitPixelsData) && explicitPixels.getPixelWidth() == 16
          && explicitPixels.getPixelHeight() == 8;
      explicitDecodeCalls = Image.targetedDecodeInvocationCountForTest();
      require(explicitRatioDraw, "explicit ratio later draw/materialization");

      replacementCapture = bestFitDraw && explicitRatioDraw;
      require(replacementCapture, "replacement keeps both captured byte sequences");

      write(DELETED_PATH, blueJpeg);
      Image deletedSource = Image.getJpegScaled(DELETED_PATH, 1, 4);
      delete(DELETED_PATH);
      deletionCapture = Arrays.equals(expectedBlue, deletedSource.getPixels()) && deletedSource.getWidth() == 16
          && deletedSource.getHeight() == 8;
      require(deletionCapture, "deleted path does not affect captured source");

      write(CORRUPT_PATH, corruptJpegEntropy(redJpeg));
      Image corrupt = Image.getJpegScaled(CORRUPT_PATH, 1, 4);
      EncodedImageSource corruptSource = (EncodedImageSource) corrupt.pipelineForSmoke().root();
      ImageException first = materializationFailure(corrupt);
      ImageException second = materializationFailure(corrupt);
      deterministicCache = first != null && first == second && corruptSource.decodeFailure() == first;
      require(deterministicCache, "deterministic JPEG failure is cached");

      write(RETRY_PATH, redJpeg);
      Image.resetImageOperationAccountingForTest();
      Image retry = Image.getJpegScaled(RETRY_PATH, 1, 4);
      EncodedImageSource retrySource = (EncodedImageSource) retry.pipelineForSmoke().root();
      Image.failNextNativeMaterializationForTest();
      boolean firstAttemptFailed = false;
      try {
        retry.getPixels();
      } catch (Throwable expected) {
        firstAttemptFailed = isTransientFailure(expected);
      }
      int[] retriedPixels = retry.getPixels();
      transientRetry = firstAttemptFailed && retrySource.decodeFailure() == null
          && retriedPixels != null && retriedPixels.length == 16 * 8 && Arrays.equals(expectedRed, retriedPixels);
      require(transientRetry, "transient native decode failure retries");

      publicJavaRoute = bestFitLazy && explicitRatioLazy
          && bestFit.pipelineForSmoke() != null && explicit.pipelineForSmoke() != null;
      privateNativeDecode = !Settings.onJavaSE && bestFitDecodeCalls > 0 && explicitDecodeCalls > 0
          && Image.targetedDecodeInvocationCountForTest() >= 2 && bestFit.hasNativeBackingForSmoke()
          && explicit.hasNativeBackingForSmoke() && retry.hasNativeBackingForSmoke();
      require(publicJavaRoute, "public factories execute the deferred Java path");
      require(privateNativeDecode, "private native bridge materializes captured JPEG bytes");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    } finally {
      deleteIfExists(REPLACED_PATH);
      deleteIfExists(DELETED_PATH);
      deleteIfExists(CORRUPT_PATH);
      deleteIfExists(RETRY_PATH);
    }

    boolean overallPass = bestFitLazy && bestFitDraw && explicitRatioLazy && explicitRatioDraw
        && replacementCapture && deletionCapture && deterministicCache && transientRetry
        && publicJavaRoute && privateNativeDecode;
    System.out.println("fixture=ImageLazyJpegFactorySmokeApp,bestFitLazy=" + bestFitLazy
        + ",bestFitDraw=" + bestFitDraw + ",explicitRatioLazy=" + explicitRatioLazy
        + ",explicitRatioDraw=" + explicitRatioDraw + ",replacementCapture=" + replacementCapture
        + ",deletionCapture=" + deletionCapture + ",deterministicCache=" + deterministicCache
        + ",transientRetry=" + transientRetry + ",publicJavaRoute=" + publicJavaRoute
        + ",privateNativeDecode=" + privateNativeDecode + ",overallPass=" + overallPass
        + ",bestFitPixels=" + bestFitPixelCount + ",bestFitSample=0x" + Integer.toHexString(bestFitSample)
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static boolean isLazy(Image image, ImageDecodePolicy.Mode mode, int width, int height) {
    ImagePipeline pipeline = image.pipelineForSmoke();
    return pipeline != null && image.getWidth() == width && image.getHeight() == height
        && image.getPixelWidth() == width && image.getPixelHeight() == height
        && pipeline.decodePolicy().mode() == mode;
  }

  private static byte[] jpeg(int color) throws Exception {
    Image image = new Image(64, 32);
    Graphics imageGraphics = image.getGraphics();
    imageGraphics.foreColor = color;
    imageGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    ByteArrayStream output = new ByteArrayStream(8192);
    image.createJpg(output, 90);
    byte[] encoded = new byte[output.getPos()];
    System.arraycopy(output.getBuffer(), 0, encoded, 0, encoded.length);
    return encoded;
  }

  private static void write(String path, byte[] bytes) throws Exception {
    File file = new File(path, File.CREATE_EMPTY);
    file.writeBytes(bytes, 0, bytes.length);
    file.close();
  }

  private static void delete(String path) throws Exception {
    File file = new File(path, File.DONT_OPEN);
    file.delete();
  }

  private static void deleteIfExists(String path) {
    try {
      delete(path);
    } catch (Throwable ignored) {
      // The smoke may already have deleted this file or may have failed before creating it.
    }
  }

  private static ImageException materializationFailure(Image image) {
    try {
      image.resolveForDrawing(1);
      return null;
    } catch (ImageException failure) {
      return failure;
    }
  }

  private static boolean isTransientFailure(Throwable failure) {
    for (Throwable current = failure; current != null; current = current.getCause()) {
      if (current instanceof TransientImageMaterializationException) {
        return true;
      }
    }
    return false;
  }

  private static byte[] corruptJpegEntropy(byte[] source) {
    int sos = -1;
    for (int i = 0; i + 1 < source.length; i++) {
      if ((source[i] & 0xFF) == 0xFF && (source[i + 1] & 0xFF) == 0xDA) {
        sos = i;
        break;
      }
    }
    if (sos < 0) {
      throw new IllegalStateException("JPEG SOS marker missing");
    }
    int segmentLength = ((source[sos + 2] & 0xFF) << 8) | (source[sos + 3] & 0xFF);
    int entropy = sos + 2 + segmentLength;
    byte[] invalidTail = {
        (byte) 0xFF, (byte) 0xC3, 0, 8, 8, 0, 1, 0, 1, 1, (byte) 0xFF, (byte) 0xD9
    };
    byte[] result = new byte[entropy + invalidTail.length];
    System.arraycopy(source, 0, result, 0, entropy);
    System.arraycopy(invalidTail, 0, result, entropy, invalidTail.length);
    return result;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
