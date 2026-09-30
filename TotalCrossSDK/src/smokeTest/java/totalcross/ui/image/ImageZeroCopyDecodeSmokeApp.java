// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Focused macOS smoke for zero-copy decode parity and retry ownership. */
public class ImageZeroCopyDecodeSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean pngParity = false;
    boolean jpegParity = false;
    boolean pngCounter = false;
    boolean jpegCounter = false;
    boolean retry = false;
    boolean pngFailureCleanup = false;
    boolean jpegFailureCleanup = false;
    String error = "";
    String step = "start";

    try {
      byte[] png = Vm.getFile("image-abi/tiny.png");
      byte[] jpeg = Vm.getFile("image-abi/lena512.jpg");
      require(png != null && jpeg != null, "fixtures");

      step = "png-disabled";
      DecodeResult pngDisabled = decode(png, false);
      step = "png-enabled";
      DecodeResult pngEnabled = decode(png, true);
      pngParity = samePixels(pngDisabled.pixels, pngEnabled.pixels);
      pngCounter = pngDisabled.copied > 0 && pngDisabled.zeroCopy == 0
          && pngEnabled.zeroCopy > 0 && pngEnabled.copied == 0
          && pngEnabled.finalBytes == (long) pngEnabled.width * pngEnabled.height * 4;

      step = "jpeg-disabled";
      DecodeResult jpegDisabled = decode(jpeg, false);
      step = "jpeg-enabled";
      DecodeResult jpegEnabled = decode(jpeg, true);
      jpegParity = samePixels(jpegDisabled.pixels, jpegEnabled.pixels);
      jpegCounter = jpegDisabled.copied > 0 && jpegDisabled.zeroCopy == 0
          && jpegEnabled.zeroCopy > 0 && jpegEnabled.copied == 0
          && jpegEnabled.finalBytes == (long) jpegEnabled.width * jpegEnabled.height * 4;

      step = "retry";
      configure(true);
      Image.resetImageOperationAccountingForTest();
      Image retryImage = new Image(png, png.length);
      Image.failNextNativeMaterializationForTest();
      boolean failed = false;
      try {
        retryImage.getPixels();
      } catch (IllegalStateException expected) {
        failed = containsTransientFailure(expected);
      }
      int[] retriedPixels = retryImage.getPixels();
      retry = failed && retriedPixels != null && Image.zeroCopyDecodeCountForTest() > 0;

      step = "png-final-buffer-failure";
      pngFailureCleanup = failureAfterFinalBufferAllocation(png);
      step = "jpeg-final-buffer-failure";
      jpegFailureCleanup = failureAfterFinalBufferAllocation(jpeg);
    } catch (Throwable failure) {
      error = step + ":" + failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = pngParity && jpegParity && pngCounter && jpegCounter && retry
        && pngFailureCleanup && jpegFailureCleanup && error.length() == 0;
    System.out.println("fixture=ImageZeroCopyDecodeSmokeApp,pngParity=" + pngParity
        + ",jpegParity=" + jpegParity + ",pngCounter=" + pngCounter
        + ",jpegCounter=" + jpegCounter + ",retry=" + retry
        + ",pngFailureCleanup=" + pngFailureCleanup
        + ",jpegFailureCleanup=" + jpegFailureCleanup + ",overallPass=" + pass
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(pass ? 0 : 1);
  }

  private static DecodeResult decode(byte[] encoded, boolean zeroCopy) throws Exception {
    configure(zeroCopy);
    Image.resetImageOperationAccountingForTest();
    Image image = new Image(encoded, encoded.length);
    int[] pixels = image.getPixels();
    return new DecodeResult(pixels, image.getPixelWidth(), image.getPixelHeight(),
        Image.zeroCopyDecodeCountForTest(), Image.copiedDecodeCountForTest(),
        Image.decodeFinalBufferBytesForTest());
  }

  private static boolean failureAfterFinalBufferAllocation(byte[] encoded) throws Exception {
    configure(true);
    Image.resetImageOperationAccountingForTest();
    Image image = new Image(encoded, encoded.length);
    long liveBefore = NativeImageBacking.backingRecordsLiveForTest();
    Image.failNextZeroCopyDecodeAfterAllocationForTest();
    boolean failed = false;
    try {
      image.getPixels();
    } catch (IllegalStateException expected) {
      failed = containsTransientFailure(expected);
    }
    boolean noBacking = image.backing == null || !image.backing.isValid();
    boolean noLeak = NativeImageBacking.backingRecordsLiveForTest() == liveBefore;
    int[] retried = image.getPixels();
    return failed && noBacking && noLeak && retried != null
        && Image.zeroCopyDecodeCountForTest() > 0;
  }

  private static void configure(boolean zeroCopy) {
    ImageOptimizationSettings.resetForTest();
    for (int feature = 0; feature < ImageOptimizationSettings.FEATURE_COUNT; feature++) {
      ImageOptimizationSettings.setState(feature, ImageOptimizationSettings.DISABLED);
    }
    if (zeroCopy) {
      ImageOptimizationSettings.setState(ImageOptimizationSettings.DECODE_ZERO_COPY,
          ImageOptimizationSettings.ENABLED);
    }
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

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static boolean containsTransientFailure(Throwable failure) {
    for (Throwable current = failure; current != null; current = current.getCause()) {
      if (current instanceof TransientImageMaterializationException) {
        return true;
      }
    }
    return false;
  }

  private static final class DecodeResult {
    final int[] pixels;
    final int width;
    final int height;
    final int zeroCopy;
    final int copied;
    final long finalBytes;

    DecodeResult(int[] pixels, int width, int height, int zeroCopy, int copied, long finalBytes) {
      this.pixels = pixels;
      this.width = width;
      this.height = height;
      this.zeroCopy = zeroCopy;
      this.copied = copied;
      this.finalBytes = finalBytes;
    }
  }
}
