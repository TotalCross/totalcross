// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import java.util.Arrays;

import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;

/** Measures full or half-size decode for one fresh native benchmark process. */
public final class ImageDecodeBenchmarkApp extends MainWindow {
  private static final int EXPECTED_IMAGES = 663;

  private String variant;
  private String source;
  private String order;
  private String mode;
  private int round;
  private int targetedJpegDenominator;
  private String orderFile;

  public ImageDecodeBenchmarkApp() {
    super("", Window.NO_BORDER);
    setDeviceTitle("image-decode-benchmark");
    String applicationRoot = ImageRasterBenchmarkSupport.argument(
        getCommandLine(), "app-root", null);
    if (applicationRoot != null && applicationRoot.length() > 0) {
      Settings.appPath = applicationRoot;
    }
  }

  @Override
  public void initUI() {
    super.initUI();
    try {
      runBenchmark();
    } catch (Throwable failure) {
      System.out.println("DECODE_PROCESS,overallPass=false,error=" + csv(safeMessage(failure)));
      System.out.flush();
      exit(1);
    }
  }

  private void runBenchmark() throws Exception {
    variant = requiredArgument("variant");
    source = requiredArgument("source");
    order = requiredArgument("order");
    mode = requiredArgument("decode-mode");
    orderFile = requiredArgument("order-file");
    round = integerArgument("round");
    ImageRasterBenchmarkSupport.require("filesystem".equals(source) || "tcz".equals(source),
        "source must be filesystem or tcz");
    ImageRasterBenchmarkSupport.require("sequential".equals(order) || "random".equals(order),
        "order must be sequential or random");
    ImageRasterBenchmarkSupport.require("full".equals(mode) || "half".equals(mode),
        "decode-mode must be full or half");
    ImageRasterBenchmarkSupport.require(round > 0, "round must be positive");

    String[] imageIds = readOrder(orderFile);
    ImageRasterBenchmarkSupport.require(imageIds.length == EXPECTED_IMAGES,
        "expected 663 image IDs but found " + imageIds.length);
    String[] sortedIds = imageIds.clone();
    Arrays.sort(sortedIds);
    for (int index = 1; index < sortedIds.length; index++) {
      ImageRasterBenchmarkSupport.require(!sortedIds[index].equals(sortedIds[index - 1]),
          "duplicate image ID: " + sortedIds[index]);
    }

    Image.resetTargetedDecodeInvocationCountForTest();
    targetedJpegDenominator = 0;
    int failed = 0;
    for (String imageId : imageIds) {
      if (!measure(imageId)) {
        failed++;
      }
    }
    System.out.println("DECODE_PROCESS,variant=" + variant
        + ",source=" + source + ",order=" + order + ",mode=" + mode
        + ",round=" + round + ",processed=" + imageIds.length
        + ",failed=" + failed
        + ",targeted_jpeg_decode_count=" + Image.targetedDecodeInvocationCountForTest()
        + ",targeted_jpeg_decode_denominator=" + targetedJpegDenominator
        + ",constructor=Image-byte-array,overallPass=" + (failed == 0));
    System.out.flush();
    exit(failed == 0 ? 0 : 1);
  }

  private boolean measure(String imageId) {
    int targetedBefore = Image.targetedDecodeInvocationCountForTest();
    long t0 = System.nanoTime();
    long t1 = t0;
    long t2 = t0;
    byte[] encoded = null;
    int inputWidth = 0;
    int inputHeight = 0;
    int outputWidth = 0;
    int outputHeight = 0;
    String status = "ok";
    try {
      encoded = acquire(imageId);
      t1 = System.nanoTime();
      Image image = new Image(encoded, encoded.length);
      inputWidth = image.getWidth();
      inputHeight = image.getHeight();
      Image decoded;
      if ("half".equals(mode)) {
        int width = Math.max(1, inputWidth / 2);
        int height = Math.max(1, inputHeight / 2);
        decoded = forceDecode(image.getSmoothScaledInstance(width, height));
      } else {
        decoded = forceDecode(image);
      }
      outputWidth = decoded.getWidth();
      outputHeight = decoded.getHeight();
      t2 = System.nanoTime();
    } catch (Throwable failure) {
      if (t1 == t0) {
        t1 = System.nanoTime();
      }
      t2 = System.nanoTime();
      status = "error";
      System.out.println("DECODE_ERROR,image_id=" + csv(imageId)
          + ",message=" + csv(safeMessage(failure)));
    }
    int targetedCount = Math.max(0,
        Image.targetedDecodeInvocationCountForTest() - targetedBefore);
    String format = encoded == null ? "unknown" : detectFormat(encoded);
    int denominator = "jpeg".equals(format) ? 1 : 0;
    targetedJpegDenominator += denominator;
    long acquireNs = t1 - t0;
    long decodeNs = t2 - t1;
    long totalNs = t2 - t0;
    System.out.println("DECODE_ROW," + csv(variant) + "," + csv(source) + ","
        + csv(order) + "," + csv(mode) + "," + round + "," + csv(imageId)
        + "," + (encoded == null ? 0 : encoded.length) + "," + csv(format)
        + "," + inputWidth + "," + inputHeight + "," + outputWidth + "," + outputHeight
        + "," + acquireNs + "," + decodeNs + "," + totalNs + "," + status
        + "," + targetedCount + "," + denominator);
    return "ok".equals(status);
  }

  private byte[] acquire(String imageId) throws Exception {
    if ("tcz".equals(source)) {
      String resourcePath = "decode/" + variant + "/" + imageId;
      byte[] encoded = Vm.getFile(resourcePath);
      ImageRasterBenchmarkSupport.require(encoded != null && encoded.length > 0,
          "TCZ resource not found: " + resourcePath);
      return encoded;
    }
    return readFilesystemBytes("corpus/" + variant + "/" + imageId);
  }

  private static Image forceDecode(Image image) throws Exception {
    return image.resolveForDrawing(1);
  }

  private static byte[] readFilesystemBytes(String path) throws Exception {
    File file = new File(path, File.READ_ONLY);
    try {
      int size = file.getSize();
      ImageRasterBenchmarkSupport.require(size > 0, "empty file: " + path);
      byte[] bytes = new byte[size];
      int offset = 0;
      while (offset < size) {
        int read = file.readBytes(bytes, offset, size - offset);
        ImageRasterBenchmarkSupport.require(read > 0, "short read: " + path);
        offset += read;
      }
      return bytes;
    } finally {
      file.close();
    }
  }

  private String[] readOrder(String path) throws Exception {
    byte[] bytes = readFilesystemBytes(path);
    String text = new String(bytes, 0, bytes.length, "UTF-8");
    int count = 0;
    for (int index = 0; index < text.length(); index++) {
      if (text.charAt(index) == '\n') {
        count++;
      }
    }
    if (text.length() > 0 && text.charAt(text.length() - 1) != '\n') {
      count++;
    }
    String[] ids = new String[count];
    int start = 0;
    int next = 0;
    for (int index = 0; index <= text.length(); index++) {
      if (index == text.length() || text.charAt(index) == '\n') {
        int end = index;
        if (end > start && text.charAt(end - 1) == '\r') {
          end--;
        }
        if (end > start) {
          ids[next++] = text.substring(start, end);
        }
        start = index + 1;
      }
    }
    ImageRasterBenchmarkSupport.require(next == ids.length, "invalid image order file");
    return ids;
  }

  private String requiredArgument(String name) {
    String value = ImageRasterBenchmarkSupport.argument(getCommandLine(), name, null);
    ImageRasterBenchmarkSupport.require(value != null && value.length() > 0,
        "missing --" + name + "=<value>");
    return value;
  }

  private int integerArgument(String name) {
    String value = requiredArgument(name);
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException failure) {
      throw new IllegalArgumentException(name + " must be an integer: " + value);
    }
  }

  private static String detectFormat(byte[] bytes) {
    if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
        && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
      return "jpeg";
    }
    if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P'
        && bytes[2] == 'N' && bytes[3] == 'G' && bytes[4] == 13 && bytes[5] == 10
        && bytes[6] == 26 && bytes[7] == 10) {
      return "png";
    }
    if (bytes.length >= 6 && (startsWith(bytes, "GIF87a") || startsWith(bytes, "GIF89a"))) {
      return "gif";
    }
    if (bytes.length >= 2 && bytes[0] == 'B' && bytes[1] == 'M') {
      return "bmp";
    }
    if (bytes.length >= 4 && ((bytes[0] == 'I' && bytes[1] == 'I'
        && bytes[2] == 42 && bytes[3] == 0)
        || (bytes[0] == 'M' && bytes[1] == 'M' && bytes[2] == 0 && bytes[3] == 42))) {
      return "tiff";
    }
    if (bytes.length >= 12 && startsWith(bytes, "RIFF")
        && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
      return "webp";
    }
    return "unknown";
  }

  private static boolean startsWith(byte[] bytes, String value) {
    if (bytes.length < value.length()) {
      return false;
    }
    for (int index = 0; index < value.length(); index++) {
      if (bytes[index] != value.charAt(index)) {
        return false;
      }
    }
    return true;
  }

  private static String csv(String value) {
    if (value.indexOf(',') < 0 && value.indexOf('"') < 0
        && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
      return value;
    }
    return '"' + value.replace("\"", "\"\"") + '"';
  }

  private static String safeMessage(Throwable failure) {
    String message = failure.getMessage();
    return message == null ? failure.getClass().getSimpleName() : message;
  }
}
