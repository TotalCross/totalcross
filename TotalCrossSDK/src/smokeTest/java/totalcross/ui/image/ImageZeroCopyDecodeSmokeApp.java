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
    String error = "";

    try {
      byte[] png = Vm.getFile("image-abi/tiny.png");
      byte[] jpeg = Vm.getFile("image-abi/lena512.jpg");
      require(png != null && jpeg != null, "fixtures");

      DecodeResult pngDisabled = decode(png, false);
      DecodeResult pngEnabled = decode(png, true);
      pngParity = samePixels(pngDisabled.pixels, pngEnabled.pixels);
      pngCounter = pngDisabled.copied > 0 && pngDisabled.zeroCopy == 0
          && pngEnabled.zeroCopy > 0 && pngEnabled.copied == 0
          && pngEnabled.finalBytes == (long) pngEnabled.width * pngEnabled.height * 4;

      DecodeResult jpegDisabled = decode(jpeg, false);
      DecodeResult jpegEnabled = decode(jpeg, true);
      jpegParity = samePixels(jpegDisabled.pixels, jpegEnabled.pixels);
      jpegCounter = jpegDisabled.copied > 0 && jpegDisabled.zeroCopy == 0
          && jpegEnabled.zeroCopy > 0 && jpegEnabled.copied == 0
          && jpegEnabled.finalBytes == (long) jpegEnabled.width * jpegEnabled.height * 4;

      configure(true);
      Image.resetImageOperationAccountingForTest();
      Image retryImage = new Image(png, png.length);
      Image.failNextNativeMaterializationForTest();
      boolean failed = false;
      try {
        retryImage.getPixels();
      } catch (TransientImageMaterializationException expected) {
        failed = true;
      }
      int[] retriedPixels = retryImage.getPixels();
      retry = failed && retriedPixels != null && Image.zeroCopyDecodeCountForTest() > 0;
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":"
          + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean pass = pngParity && jpegParity && pngCounter && jpegCounter && retry && error.length() == 0;
    System.out.println("fixture=ImageZeroCopyDecodeSmokeApp,pngParity=" + pngParity
        + ",jpegParity=" + jpegParity + ",pngCounter=" + pngCounter
        + ",jpegCounter=" + jpegCounter + ",retry=" + retry + ",overallPass=" + pass
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
