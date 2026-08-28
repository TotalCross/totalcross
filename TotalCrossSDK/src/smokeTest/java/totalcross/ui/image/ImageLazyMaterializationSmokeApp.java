// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for lazy encoded Image construction and barriers. */
public class ImageLazyMaterializationSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean pngConstructionLazy = false;
    boolean pngSourceCopy = false;
    boolean firstDrawMaterializes = false;
    boolean repeatedBarrierReusesPixels = false;
    boolean warningCompatibility = false;
    boolean pathSourceStable = false;
    boolean jpegConstructionLazy = false;
    boolean structuralInvalid = false;
    boolean payloadInvalidDeferred = false;
    boolean nativeAllocationRetryable = false;
    String error = "";

    try {
      byte[] pngBytes = Vm.getFile("image-abi/tiny.png");
      require(pngBytes != null && pngBytes.length > 0, "tiny PNG resource");

      Image copied = new Image(pngBytes);
      pngBytes[findIdatData(pngBytes)] ^= 1;
      int copiedWidth = copied.getPixelWidth();
      int[] copiedPixels = copied.getPixels();
      pngSourceCopy = copiedWidth == 36 && copiedPixels != null && copiedPixels.length == 36 * 36;
      pngConstructionLazy = copiedWidth == 36;
      require(pngSourceCopy, "copied PNG source");

      byte[] warningBytes = addInvalidSbit(Vm.getFile("image-abi/tiny.png"));
      Image warningImage = new Image(warningBytes);
      warningCompatibility = warningImage.getPixels() != null;
      require(warningCompatibility, "nonfatal PNG warning compatibility");

      Image drawn = new Image(Vm.getFile("image-abi/tiny.png"));
      Graphics target = getGraphics();
      require(target != null, "main window graphics");
      target.drawImage(drawn, 3, 3);
      int[] firstPixels = drawn.getPixels();
      firstDrawMaterializes = firstPixels != null && firstPixels.length == 36 * 36;
      int[] secondPixels = drawn.getPixels();
      repeatedBarrierReusesPixels = firstPixels == secondPixels;
      require(firstDrawMaterializes && repeatedBarrierReusesPixels, "draw/pixel barriers");

      Image pathImage = new Image("image-abi/tiny.png");
      pathSourceStable = pathImage.getPixelWidth() == 36 && pathImage.getPixelHeight() == 36
          && pathImage.getPixels().length == 36 * 36;
      require(pathSourceStable, "TCZ path source");

      Image writable = new Image(2, 1);
      ByteArrayStream jpegStream = new ByteArrayStream(512);
      writable.createJpg(jpegStream, 80);
      byte[] jpeg = new byte[jpegStream.getPos()];
      Vm.arrayCopy(jpegStream.getBuffer(), 0, jpeg, 0, jpeg.length);
      Image jpegImage = new Image(jpeg);
      int jpegWidth = jpegImage.getPixelWidth();
      int jpegHeight = jpegImage.getPixelHeight();
      ByteArrayStream jpegOutput = new ByteArrayStream(512);
      jpegImage.createJpg(jpegOutput, 80);
      jpegConstructionLazy = jpegWidth == 2 && jpegHeight == 1 && jpegOutput.getPos() > 0;
      require(jpegConstructionLazy, "JPEG construction/materialization");

      try {
        new Image(new byte[] { 1, 2, 3 });
      } catch (ImageException expected) {
        structuralInvalid = true;
      }
      require(structuralInvalid, "structural invalid source");

      byte[] corruptBytes = Vm.getFile("image-abi/tiny.png");
      int corruptIdat = findIdatData(corruptBytes);
      corruptBytes[corruptIdat] = 0;
      int corruptLength = readInt(corruptBytes, corruptIdat - 8);
      writeInt(corruptBytes, corruptIdat + corruptLength,
          crc(corruptBytes, corruptIdat - 4, corruptLength + 4));
      Image corrupt = new Image(corruptBytes);
      corrupt.getPixelWidth();
      try {
        corrupt.getPixels();
      } catch (IllegalStateException expected) {
        payloadInvalidDeferred = true;
      }
      require(payloadInvalidDeferred, "payload-invalid source deferred failure");

      Image nativeRetry = new Image(Vm.getFile("image-abi/tiny.png"));
      boolean firstNativeAllocationFailed = false;
      Image.failNextNativeMaterializationForTest();
      try {
        nativeRetry.getPixels();
      } catch (Throwable expected) {
        firstNativeAllocationFailed = true;
      }
      int[] retriedNativePixels = nativeRetry.getPixels();
      nativeAllocationRetryable = firstNativeAllocationFailed && retriedNativePixels != null
          && retriedNativePixels.length == 36 * 36;
      require(nativeAllocationRetryable, "native allocation failure retry");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = pngConstructionLazy && pngSourceCopy && warningCompatibility && firstDrawMaterializes
        && repeatedBarrierReusesPixels && pathSourceStable && jpegConstructionLazy
        && structuralInvalid && payloadInvalidDeferred && nativeAllocationRetryable;
    System.out.println("fixture=ImageLazyMaterializationSmokeApp,pngConstructionLazy=" + pngConstructionLazy
        + ",pngSourceCopy=" + pngSourceCopy + ",firstDrawMaterializes=" + firstDrawMaterializes
        + ",warningCompatibility=" + warningCompatibility
        + ",repeatedBarrierReusesPixels=" + repeatedBarrierReusesPixels + ",pathSourceStable=" + pathSourceStable
        + ",jpegConstructionLazy=" + jpegConstructionLazy + ",structuralInvalid=" + structuralInvalid
        + ",payloadInvalidDeferred=" + payloadInvalidDeferred + ",nativeAllocationRetryable="
        + nativeAllocationRetryable + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static int findIdatData(byte[] bytes) {
    int position = 8;
    while (position + 12 <= bytes.length) {
      int length = readInt(bytes, position);
      if (length > 0 && bytes[position + 4] == 'I' && bytes[position + 5] == 'D'
          && bytes[position + 6] == 'A' && bytes[position + 7] == 'T') {
        return position + 8;
      }
      position += length + 12;
    }
    throw new IllegalStateException("PNG has no IDAT");
  }

  private static byte[] addInvalidSbit(byte[] source) {
    int data = findIdatData(source);
    int chunk = data - 8;
    byte[] sbit = new byte[] { 9, 9, 9 };
    byte[] result = new byte[source.length + sbit.length + 12];
    Vm.arrayCopy(source, 0, result, 0, chunk);
    writeInt(result, chunk, sbit.length);
    result[chunk + 4] = 's';
    result[chunk + 5] = 'B';
    result[chunk + 6] = 'I';
    result[chunk + 7] = 'T';
    Vm.arrayCopy(sbit, 0, result, chunk + 8, sbit.length);
    writeInt(result, chunk + 8 + sbit.length, crc(result, chunk + 4, sbit.length + 4));
    Vm.arrayCopy(source, chunk, result, chunk + sbit.length + 12, source.length - chunk);
    return result;
  }

  private static int crc(byte[] bytes, int position, int length) {
    int value = 0xffffffff;
    for (int i = 0; i < length; i++) {
      value ^= bytes[position + i] & 0xff;
      for (int bit = 0; bit < 8; bit++) {
        value = (value >>> 1) ^ ((value & 1) == 0 ? 0 : 0xedb88320);
      }
    }
    return value ^ 0xffffffff;
  }

  private static int readInt(byte[] bytes, int position) {
    return ((bytes[position] & 0xff) << 24) | ((bytes[position + 1] & 0xff) << 16)
        | ((bytes[position + 2] & 0xff) << 8) | (bytes[position + 3] & 0xff);
  }

  private static void writeInt(byte[] bytes, int position, int value) {
    bytes[position] = (byte) (value >> 24);
    bytes[position + 1] = (byte) (value >> 16);
    bytes[position + 2] = (byte) (value >> 8);
    bytes[position + 3] = (byte) value;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
