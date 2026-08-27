// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.ui.MainWindow;

/** Deployed macOS smoke for native encoded-source capture and metadata. */
public class EncodedImageSourceSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean byteCapture = false;
    boolean streamCapture = false;
    boolean pathCapture = false;
    boolean pngValidation = false;
    boolean fcMetadataParity = false;
    boolean jpegValidation = false;
    boolean lifetime = false;
    String error = "";

    try {
      byte[] framedPng = png(300, 40, "FC=3", new byte[] { 1, 2, 3 });
      EncodedImageSource bytes = EncodedImageSource.fromBytes(framedPng);
      byteCapture = bytes.hasNativeBackingForSmoke() && !bytes.hasJavaBackingForSmoke()
          && bytes.getFormat() == ImageEncodedStructure.Format.PNG
          && bytes.getEncodedLength() == framedPng.length
          && bytes.getIntrinsicWidth() == 300 && bytes.getIntrinsicHeight() == 40
          && bytes.getLogicalWidth() == 100 && bytes.getLogicalHeight() == 40
          && bytes.getFrameCount() == 3 && "FC=3".equals(bytes.getComment());
      require(byteCapture, "byte capture metadata/backing");

      fcMetadataParity = fc("FC=1", 300, 1, 300)
          && fc("FC=3", 300, 3, 100)
          && fc("FC=2147483647", 300, Integer.MAX_VALUE, 0)
          && rejects(png(300, 40, "FC=0", new byte[] { 1 }))
          && rejects(png(300, 40, "FC=-2", new byte[] { 1 }))
          && rejects(png(300, 40, "FC=2147483648", new byte[] { 1 }))
          && fc("FC=not-a-number", 300, 1, 300);
      require(fcMetadataParity, "FC metadata parity");

      EncodedImageSource stream = EncodedImageSource.fromStream(new ByteArrayStream(framedPng));
      streamCapture = stream.hasNativeBackingForSmoke() && !stream.hasJavaBackingForSmoke()
          && stream.getEncodedLength() == framedPng.length && stream.getLogicalWidth() == 100;
      require(streamCapture, "stream capture metadata/backing");

      byte[] pathPng = totalcross.sys.Vm.getFile("image-abi/tiny.png");
      EncodedImageSource path = EncodedImageSource.fromPath("image-abi/tiny.png");
      pathCapture = pathPng != null && path.hasNativeBackingForSmoke() && !path.hasJavaBackingForSmoke()
          && path.getFormat() == ImageEncodedStructure.Format.PNG
          && path.getEncodedLength() == pathPng.length
          && path.getIntrinsicWidth() == 36 && path.getIntrinsicHeight() == 36;
      require(pathCapture, "TCZ path capture metadata/backing");

      byte[] badCrc = framedPng.clone();
      badCrc[badCrc.length - 5] ^= 1;
      byte[] truncated = new byte[framedPng.length - 3];
      System.arraycopy(framedPng, 0, truncated, 0, truncated.length);
      pngValidation = rejects(badCrc) && rejects(truncated);
      require(pngValidation, "PNG CRC/truncation validation");

      Image pixel = new Image(2, 1);
      ByteArrayStream jpegStream = new ByteArrayStream(512);
      pixel.createJpg(jpegStream, 80);
      byte[] jpeg = new byte[jpegStream.getPos()];
      System.arraycopy(jpegStream.getBuffer(), 0, jpeg, 0, jpeg.length);
      EncodedImageSource jpegSource = EncodedImageSource.fromBytes(jpeg);
      byte[] shortJpeg = new byte[jpeg.length - 1];
      System.arraycopy(jpeg, 0, shortJpeg, 0, shortJpeg.length);
      jpegValidation = jpegSource.hasNativeBackingForSmoke()
          && jpegSource.getFormat() == ImageEncodedStructure.Format.JPEG
          && jpegSource.getIntrinsicWidth() == 2 && jpegSource.getIntrinsicHeight() == 1
          && rejects(shortJpeg);
      require(jpegValidation, "JPEG header/marker validation");

      bytes.releaseForSmoke();
      bytes.releaseForSmoke();
      lifetime = !bytes.hasNativeBackingForSmoke();
      require(lifetime, "idempotent native bag release");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = byteCapture && streamCapture && pathCapture && pngValidation
        && fcMetadataParity && jpegValidation && lifetime;
    System.out.println("fixture=EncodedImageSourceSmokeApp,byteCapture=" + byteCapture
        + ",streamCapture=" + streamCapture + ",pathCapture=" + pathCapture
        + ",pngValidation=" + pngValidation + ",fcMetadataParity=" + fcMetadataParity
        + ",jpegValidation=" + jpegValidation
        + ",lifetime=" + lifetime + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static boolean rejects(byte[] bytes) {
    try {
      EncodedImageSource.fromBytes(bytes);
      return false;
    } catch (ImageException expected) {
      return true;
    }
  }

  private static boolean fc(String comment, int width, int frameCount, int logicalWidth) throws ImageException {
    EncodedImageSource source = EncodedImageSource.fromBytes(png(width, 40, comment, new byte[] { 1 }));
    return source.getFrameCount() == frameCount && source.getLogicalWidth() == logicalWidth
        && comment.equals(source.getComment());
  }

  private static byte[] png(int width, int height, String comment, byte[] idat) {
    int textLength = 8 + comment.length();
    int total = 8 + 12 + 13 + 12 + textLength + 12 + idat.length + 12;
    byte[] result = new byte[total];
    int p = 0;
    result[p++] = (byte) 0x89; result[p++] = 'P'; result[p++] = 'N'; result[p++] = 'G';
    result[p++] = 13; result[p++] = 10; result[p++] = 26; result[p++] = 10;
    p = chunk(result, p, "IHDR", new byte[] { (byte) (width >> 24), (byte) (width >> 16),
        (byte) (width >> 8), (byte) width, (byte) (height >> 24), (byte) (height >> 16),
        (byte) (height >> 8), (byte) height, 8, 6, 0, 0, 0 });
    byte[] text = new byte[textLength];
    byte[] keyword = "Comment".getBytes();
    System.arraycopy(keyword, 0, text, 0, keyword.length);
    text[keyword.length] = 0;
    byte[] commentBytes = comment.getBytes();
    System.arraycopy(commentBytes, 0, text, keyword.length + 1, commentBytes.length);
    p = chunk(result, p, "tEXt", text);
    p = chunk(result, p, "IDAT", idat);
    chunk(result, p, "IEND", new byte[0]);
    return result;
  }

  private static int chunk(byte[] result, int p, String name, byte[] data) {
    putInt(result, p, data.length); p += 4;
    byte[] type = name.getBytes();
    System.arraycopy(type, 0, result, p, 4); p += 4;
    System.arraycopy(data, 0, result, p, data.length);
    p += data.length;
    putInt(result, p, crc(result, p - data.length - 4, data.length + 4));
    return p + 4;
  }

  private static int crc(byte[] bytes, int p, int length) {
    int value = 0xffffffff;
    for (int i = 0; i < length; i++) {
      value ^= bytes[p + i] & 0xff;
      for (int bit = 0; bit < 8; bit++) {
        value = (value >>> 1) ^ ((value & 1) == 0 ? 0 : 0xedb88320);
      }
    }
    return value ^ 0xffffffff;
  }

  private static void putInt(byte[] bytes, int p, int value) {
    bytes[p] = (byte) (value >> 24); bytes[p + 1] = (byte) (value >> 16);
    bytes[p + 2] = (byte) (value >> 8); bytes[p + 3] = (byte) value;
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new IllegalStateException(message);
  }
}
