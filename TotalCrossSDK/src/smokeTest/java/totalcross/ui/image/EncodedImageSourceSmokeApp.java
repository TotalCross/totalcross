// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.io.IllegalArgumentIOException;
import totalcross.ui.MainWindow;

/** Deployed smoke for native encoded-source capture and metadata. */
public class EncodedImageSourceSmokeApp extends MainWindow {
  private static final char[] HEX_DIGITS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };
  private static String tinyPngFnv1a = "not-read";

  static {
    // Android deployment patches fixed-width package descriptors in classes.dex.
    totalcross.sys.Settings.applicationId = "test";
  }

  @Override
  public void initUI() {
    if (totalcross.sys.Settings.onJavaSE) {
      runJavaSEFilesystemSmoke();
      return;
    }

    boolean byteCapture = false;
    boolean streamCapture = false;
    boolean pathCapture = false;
    boolean pngValidation = false;
    boolean fcMetadataParity = false;
    boolean jpegValidation = false;
    boolean lifetime = false;
    boolean runtimeFixturesNotPackaged = false;
    boolean filesystemPng = false;
    boolean filesystemJpeg = false;
    boolean filesystemPngFile = false;
    boolean filesystemPngPath = false;
    boolean filesystemPngPathImageException = false;
    String filesystemPngError = "";
    boolean filesystemJpegFile = false;
    boolean filesystemJpegPath = false;
    boolean filesystemJpegPathImageException = false;
    String filesystemJpegError = "";
    boolean tczOnlyPng = false;
    boolean tczOnlyJpeg = false;
    boolean tczPrecedence = false;
    boolean tczInvalidNoFallback = false;
    boolean tczEmptyNoFallback = false;
    boolean missingFile = false;
    boolean invalidFilesystemFile = false;
    boolean absoluteFilesystemPath = false;
    boolean deviceFilesystemPath = false;
    boolean deviceFilesystemPathSupported = false;
    boolean devicePathImageException = false;
    String devicePathError = "";
    boolean pathLengthBoundary = false;
    String error = "";
    tinyPngFnv1a = "not-read";

    smokePhase("start");
    try {
      discardRuntimeFile("runtime-image-string-fallback.png");
      discardRuntimeFile("runtime-image-string-fallback.jpg");
      runtimeFixturesNotPackaged = totalcross.sys.Vm.getFile("runtime-image-string-fallback.png") == null
          && totalcross.sys.Vm.getFile("runtime-image-string-fallback.jpg") == null;
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("runtime-fixtures-checked=" + runtimeFixturesNotPackaged);

    smokePhase("filesystem-parity-start");
    FilesystemResult pngResult = safeFilesystemRoundTrip("runtime-image-string-fallback.png", false);
    smokePhase("filesystem-png-result=" + pngResult.parity + ",error=" + pngResult.error);
    filesystemPngFile = pngResult.fileLoaded;
    filesystemPngPath = pngResult.pathLoaded;
    filesystemPngPathImageException = pngResult.pathImageException;
    filesystemPngError = pngResult.error;
    filesystemPng = pngResult.parity;
    FilesystemResult jpegResult = safeFilesystemRoundTrip("runtime-image-string-fallback.jpg", true);
    smokePhase("filesystem-jpeg-result=" + jpegResult.parity + ",error=" + jpegResult.error);
    filesystemJpegFile = jpegResult.fileLoaded;
    filesystemJpegPath = jpegResult.pathLoaded;
    filesystemJpegPathImageException = jpegResult.pathImageException;
    filesystemJpegError = jpegResult.error;
    filesystemJpeg = jpegResult.parity;
    smokePhase("filesystem-parity-summary=png:" + filesystemPng + ",jpeg:" + filesystemJpeg);

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
      smokePhase("encoded-source-validation-complete");

    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }

    smokePhase("tcz-only-png-start");
    try {
      tczOnlyPng = tczImageParity("image-abi/tiny.png");
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("tcz-only-png-result=" + tczOnlyPng);
    try {
      tczOnlyJpeg = tczImageParity("image-abi/lena512.jpg");
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("tcz-only-jpeg-result=" + tczOnlyJpeg);
    try {
      tczPrecedence = tczPrecedenceOverPhysicalFile();
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    try {
      tczInvalidNoFallback = packagedInvalidDoesNotFallback("image-abi/invalid.png", false);
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    try {
      tczEmptyNoFallback = packagedInvalidDoesNotFallback("image-abi/empty.png", true);
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("tcz-images-checked");

    try {
      missingFile = missingImageUsesFileError();
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("missing-file-checked=" + missingFile);
    try {
      invalidFilesystemFile = invalidFilesystemImageFails();
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("invalid-file-checked=" + invalidFilesystemFile);
    try {
      absoluteFilesystemPath = safeFilesystemRoundTrip(
          totalcross.sys.Settings.appPath + "runtime-image-string-fallback-absolute.png", false).parity;
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("absolute-path-checked=" + absoluteFilesystemPath);
    try {
      deviceFilesystemPathSupported = supportsDevicePath();
      if (deviceFilesystemPathSupported) {
        FilesystemResult deviceResult = safeFilesystemRoundTrip(
            "device/runtime-image-string-fallback-device.png", false);
        deviceFilesystemPath = deviceResult.parity;
        devicePathImageException = deviceResult.pathImageException;
        devicePathError = deviceResult.error;
      } else {
        deviceFilesystemPath = true;
      }
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("device-path-supported=" + deviceFilesystemPathSupported);
    try {
      pathLengthBoundary = pathLengthBoundaryIsSafe();
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }
    smokePhase("path-length-checked=" + pathLengthBoundary);
    try {
      lifetime = lifetimeCheck(png(300, 40, "FC=3", new byte[] { 1, 2, 3 }));
    } catch (Throwable failure) {
      error = appendFailure(error, failure);
    }

    boolean overallPass = byteCapture && streamCapture && pathCapture && pngValidation
        && fcMetadataParity && jpegValidation && tczOnlyPng && tczOnlyJpeg && tczPrecedence
        && tczInvalidNoFallback && tczEmptyNoFallback && filesystemPng && filesystemJpeg
        && missingFile && invalidFilesystemFile && absoluteFilesystemPath
        && deviceFilesystemPath && pathLengthBoundary && lifetime;
    overallPass &= runtimeFixturesNotPackaged;
    System.out.println("fixture=EncodedImageSourceSmokeApp,byteCapture=" + byteCapture
        + ",streamCapture=" + streamCapture + ",pathCapture=" + pathCapture
        + ",pngValidation=" + pngValidation + ",fcMetadataParity=" + fcMetadataParity
        + ",jpegValidation=" + jpegValidation
        + ",filesystemPng=" + filesystemPng + ",filesystemJpeg=" + filesystemJpeg
        + ",filesystemPngFile=" + filesystemPngFile + ",filesystemPngPath=" + filesystemPngPath
        + ",filesystemPngPathImageException=" + filesystemPngPathImageException
        + ",filesystemPngError=" + filesystemPngError.replace(' ', '_')
        + ",tczOnlyPng=" + tczOnlyPng + ",tczOnlyJpeg=" + tczOnlyJpeg
        + ",tczPrecedence=" + tczPrecedence + ",tczInvalidNoFallback=" + tczInvalidNoFallback
        + ",tczEmptyNoFallback=" + tczEmptyNoFallback + ",missingFile=" + missingFile
        + ",invalidFilesystemFile=" + invalidFilesystemFile
        + ",absoluteFilesystemPath=" + absoluteFilesystemPath
        + ",deviceFilesystemPath=" + deviceFilesystemPath
        + ",deviceFilesystemPathSupported=" + deviceFilesystemPathSupported
        + ",devicePathImageException=" + devicePathImageException
        + ",devicePathError=" + devicePathError.replace(' ', '_')
        + ",pathLengthBoundary=" + pathLengthBoundary
        + ",tczTinyPngFnv1a=" + tinyPngFnv1a
        + ",filesystemJpegFile=" + filesystemJpegFile + ",filesystemJpegPath=" + filesystemJpegPath
        + ",filesystemJpegPathImageException=" + filesystemJpegPathImageException
        + ",filesystemJpegError=" + filesystemJpegError.replace(' ', '_')
        + ",runtimeFixturesNotPackaged=" + runtimeFixturesNotPackaged
        + ",lifetime=" + lifetime + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private void runJavaSEFilesystemSmoke() {
    boolean runtimeFixturesNotPackaged = false;
    FilesystemResult pngResult = new FilesystemResult();
    FilesystemResult jpegResult = new FilesystemResult();
    boolean missingFile = false;
    boolean invalidFilesystemFile = false;
    boolean absoluteFilesystemPath = false;
    boolean deviceFilesystemPath = false;
    boolean deviceFilesystemPathSupported = false;
    boolean devicePathImageException = false;
    String devicePathError = "";
    boolean pathLengthBoundary = false;
    String error = "";
    try {
      discardRuntimeFile("runtime-image-string-fallback.png");
      discardRuntimeFile("runtime-image-string-fallback.jpg");
      runtimeFixturesNotPackaged = totalcross.sys.Vm.getFile("runtime-image-string-fallback.png") == null
          && totalcross.sys.Vm.getFile("runtime-image-string-fallback.jpg") == null;
      pngResult = filesystemRoundTrip("runtime-image-string-fallback.png", false);
      jpegResult = filesystemRoundTrip("runtime-image-string-fallback.jpg", true);
      missingFile = missingImageUsesFileError();
      invalidFilesystemFile = invalidFilesystemImageFails();
      absoluteFilesystemPath = filesystemRoundTrip(
          totalcross.sys.Settings.appPath + "runtime-image-string-fallback-absolute.png", false).parity;
      deviceFilesystemPathSupported = supportsDevicePath();
      if (deviceFilesystemPathSupported) {
        FilesystemResult deviceResult = filesystemRoundTrip("device/runtime-image-string-fallback-device.png", false);
        deviceFilesystemPath = deviceResult.parity;
        devicePathImageException = deviceResult.pathImageException;
        devicePathError = deviceResult.error;
      } else {
        deviceFilesystemPath = true;
      }
      pathLengthBoundary = pathLengthBoundaryIsSafe();
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean overallPass = runtimeFixturesNotPackaged && pngResult.fileLoaded && pngResult.pathLoaded
        && pngResult.parity && jpegResult.fileLoaded && jpegResult.pathLoaded && jpegResult.parity
        && missingFile && invalidFilesystemFile && absoluteFilesystemPath
        && deviceFilesystemPath && pathLengthBoundary;
    System.out.println("fixture=EncodedImageSourceSmokeApp,javaSE=true"
        + ",runtimeFixturesNotPackaged=" + runtimeFixturesNotPackaged
        + ",filesystemPngFile=" + pngResult.fileLoaded + ",filesystemPngPath=" + pngResult.pathLoaded
        + ",filesystemPng=" + pngResult.parity + ",filesystemPngError=" + pngResult.error.replace(' ', '_')
        + ",filesystemJpegFile=" + jpegResult.fileLoaded + ",filesystemJpegPath=" + jpegResult.pathLoaded
        + ",filesystemJpeg=" + jpegResult.parity + ",filesystemJpegError=" + jpegResult.error.replace(' ', '_')
        + ",missingFile=" + missingFile + ",invalidFilesystemFile=" + invalidFilesystemFile
        + ",absoluteFilesystemPath=" + absoluteFilesystemPath
        + ",deviceFilesystemPath=" + deviceFilesystemPath
        + ",deviceFilesystemPathSupported=" + deviceFilesystemPathSupported
        + ",devicePathImageException=" + devicePathImageException
        + ",devicePathError=" + devicePathError.replace(' ', '_')
        + ",pathLengthBoundary=" + pathLengthBoundary
        + ",overallPass=" + overallPass + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static boolean tczImageParity(String path) throws Exception {
    smokePhase("tcz-parity-read-start=" + path);
    byte[] encoded = totalcross.sys.Vm.getFile(path);
    if (encoded == null) {
      return false;
    }
    String checksum = hex32(fnv1a(encoded));
    if ("image-abi/tiny.png".equals(path)) {
      tinyPngFnv1a = checksum;
      if (!"cdf05a32".equals(checksum)) {
        smokePhase("tcz-parity-checksum-mismatch=" + path + ",expected=cdf05a32,actual=" + checksum);
        return false;
      }
    }
    smokePhase("tcz-parity-read-length=" + path + ",bytes=" + encoded.length + ",fnv1a=" + checksum);
    try (File physical = new File(path)) {
      if (physical.exists()) {
        return false;
      }
    }
    EncodedImageSource source = EncodedImageSource.fromPath(path);
    Image fromPath = null;
    Image fromBytes = null;
    try {
      smokePhase("tcz-parity-path-image-start=" + path);
      fromPath = new Image(path);
      smokePhase("tcz-parity-path-image-created=" + path);
      fromBytes = new Image(encoded);
      smokePhase("tcz-parity-bytes-image-created=" + path);
      if (fromPath.getPixelWidth() != fromBytes.getPixelWidth()
          || fromPath.getPixelHeight() != fromBytes.getPixelHeight()) {
        return false;
      }
      smokePhase("tcz-parity-pixels-bytes-start=" + path);
      int[] bytePixels = fromBytes.getPixels();
      smokePhase("tcz-parity-pixels-bytes-created=" + path);
      smokePhase("tcz-parity-pixels-path-start=" + path);
      int[] pathPixels = fromPath.getPixels();
      smokePhase("tcz-parity-pixels-path-created=" + path);
      boolean samePixels = pathPixels.length == bytePixels.length;
      for (int i = 0; samePixels && i < pathPixels.length; i++) {
        samePixels = pathPixels[i] == bytePixels[i];
      }
      return source.hasNativeBackingForSmoke() && !source.hasJavaBackingForSmoke() && samePixels;
    } finally {
      source.releaseForSmoke();
      releaseEncodedSource(fromPath);
      releaseEncodedSource(fromBytes);
    }
  }

  private static String failureChain(Throwable failure) {
    StringBuffer message = new StringBuffer();
    while (failure != null) {
      if (message.length() > 0) {
        message.append("->");
      }
      message.append(failure.getClass().getName()).append(':');
      if (failure.getMessage() != null) {
        message.append(failure.getMessage().replace(' ', '_'));
      }
      failure = failure.getCause();
    }
    return message.toString();
  }

  private static int fnv1a(byte[] bytes) {
    int hash = 0x811c9dc5;
    for (int i = 0; i < bytes.length; i++) {
      hash = (hash ^ (bytes[i] & 0xff)) * 0x01000193;
    }
    return hash;
  }

  private static String hex32(int value) {
    char[] digits = new char[8];
    for (int i = 7; i >= 0; i--) {
      digits[i] = HEX_DIGITS[value & 0x0f];
      value >>>= 4;
    }
    return new String(digits);
  }

  private static FilesystemResult safeFilesystemRoundTrip(String path, boolean jpeg) {
    try {
      return filesystemRoundTrip(path, jpeg);
    } catch (Throwable failure) {
      FilesystemResult result = new FilesystemResult();
      result.error = failureChain(failure).replace(' ', '_');
      return result;
    }
  }

  private static String appendFailure(String existing, Throwable failure) {
    String current = failureChain(failure).replace(' ', '_');
    return existing.length() == 0 ? current : existing + "|" + current;
  }

  private static boolean tczPrecedenceOverPhysicalFile() throws Exception {
    String path = "image-abi/tiny.png";
    byte[] packaged = totalcross.sys.Vm.getFile(path);
    if (packaged == null) {
      return false;
    }
    ensureDirectory("image-abi");
    writeRuntimeFile(path, encodeRuntimeImage(false));
    Image fromPath = null;
    Image fromFile = null;
    try {
      fromPath = new Image(path);
      try (File file = new File(path, File.READ_ONLY)) {
        fromFile = new Image(file);
      }
      return fromPath.getPixelWidth() == 36 && fromPath.getPixelHeight() == 36
          && fromFile.getPixelWidth() == 9 && fromFile.getPixelHeight() == 7
          && !sameDecodedImage(fromPath, fromFile);
    } finally {
      releaseEncodedSource(fromPath);
      releaseEncodedSource(fromFile);
      discardRuntimeFile(path);
    }
  }

  private static boolean packagedInvalidDoesNotFallback(String path, boolean empty) throws Exception {
    byte[] packaged = totalcross.sys.Vm.getFile(path);
    if (packaged == null || (empty && packaged.length != 0) || (!empty && packaged.length == 0)) {
      return false;
    }
    ensureDirectory("image-abi");
    writeRuntimeFile(path, encodeRuntimeImage(false));
    boolean rejected = false;
    try {
      Image unexpectedFallback = new Image(path);
      releaseEncodedSource(unexpectedFallback);
    } catch (ImageException expected) {
      rejected = true;
    } finally {
      discardRuntimeFile(path);
    }
    return rejected;
  }

  private static boolean missingImageUsesFileError() throws Exception {
    String path = "runtime-image-string-fallback-missing.png";
    discardRuntimeFile(path);
    try {
      new Image(path);
      return false;
    } catch (FileNotFoundException expected) {
      return true;
    }
  }

  private static boolean invalidFilesystemImageFails() throws Exception {
    String path = "runtime-image-string-fallback-invalid.png";
    try {
      writeRuntimeFile(path, new byte[] { 1, 2, 3, 4, 5, 6 });
      try {
        new Image(path);
        return false;
      } catch (ImageException expected) {
        return true;
      }
    } finally {
      discardRuntimeFile(path);
    }
  }

  private static boolean pathLengthBoundaryIsSafe() throws Exception {
    boolean length255 = pathHasFileError(repeatedPath(255), FileNotFoundException.class);
    smokePhase("path-255-result=" + length255);
    boolean length256 = pathHasFileError(repeatedPath(256), IllegalArgumentIOException.class);
    smokePhase("path-256-result=" + length256);
    boolean length300 = pathHasFileError(repeatedPath(300), IllegalArgumentIOException.class);
    smokePhase("path-300-result=" + length300);
    return length255 && length256 && length300;
  }

  private static boolean supportsDevicePath() {
    String path = "device/runtime-image-string-fallback-device-probe.tmp";
    try {
      try (File probe = new File(path, File.CREATE_EMPTY)) {
        probe.writeBytes(new byte[] { 1 });
      }
      discardRuntimeFile(path);
      return true;
    } catch (Throwable unsupported) {
      discardRuntimeFile(path);
      return false;
    }
  }

  private static boolean lifetimeCheck(byte[] encoded) throws ImageException {
    EncodedImageSource source = EncodedImageSource.fromBytes(encoded);
    boolean captured = source.hasNativeBackingForSmoke();
    source.releaseForSmoke();
    source.releaseForSmoke();
    return captured && !source.hasNativeBackingForSmoke();
  }

  private static boolean pathHasFileError(String path, Class expected) throws Exception {
    smokePhase("path-error-start-length=" + path.length());
    try {
      new Image(path);
      smokePhase("path-error-unexpected-success-length=" + path.length());
      return false;
    } catch (Throwable failure) {
      boolean matches = expected.isInstance(failure);
      smokePhase("path-error-done-length=" + path.length() + ",type=" + failure.getClass().getName()
          + ",expected=" + matches);
      return matches;
    }
  }

  private static void smokePhase(String phase) {
    System.err.println("EncodedImageSourceSmoke phase=" + phase);
    System.err.flush();
  }

  private static String repeatedPath(int length) {
    StringBuffer path = new StringBuffer(length);
    for (int i = 0; i < length; i++) {
      path.append('x');
    }
    return path.toString();
  }

  private static void ensureDirectory(String path) throws Exception {
    try (File directory = new File(path)) {
      if (!directory.exists()) {
        directory.createDir();
      }
    }
  }

  private static void writeRuntimeFile(String path, byte[] content) throws Exception {
    try (File file = new File(path, File.CREATE_EMPTY)) {
      file.writeBytes(content);
    }
  }

  private static byte[] encodeRuntimeImage(boolean jpeg) throws Exception {
    Image image = new Image(9, 7);
    try {
      int[] pixels = image.getPixels();
      for (int y = 0; y < image.getPixelHeight(); y++) {
        for (int x = 0; x < image.getPixelWidth(); x++) {
          pixels[y * image.getPixelWidth() + x] = 0xff000000
              | ((x * 37 & 0xff) << 16) | ((y * 53 & 0xff) << 8) | ((x * 11 + y * 17) & 0xff);
        }
      }
      ByteArrayStream output = new ByteArrayStream(1024);
      if (jpeg) {
        image.createJpg(output, 92);
      } else {
        image.createPng(output);
      }
      byte[] encoded = new byte[output.getPos()];
      System.arraycopy(output.getBuffer(), 0, encoded, 0, encoded.length);
      return encoded;
    } finally {
      releaseEncodedSource(image);
    }
  }

  private static boolean sameDecodedImage(Image left, Image right) throws Exception {
    if (left.getPixelWidth() != right.getPixelWidth() || left.getPixelHeight() != right.getPixelHeight()) {
      return false;
    }
    int[] leftPixels = left.getPixels();
    int[] rightPixels = right.getPixels();
    if (leftPixels.length != rightPixels.length) {
      return false;
    }
    for (int i = 0; i < leftPixels.length; i++) {
      if (leftPixels[i] != rightPixels[i]) {
        return false;
      }
    }
    return true;
  }

  private static FilesystemResult filesystemRoundTrip(String path, boolean jpeg) throws Exception {
    FilesystemResult result = new FilesystemResult();
    smokePhase("filesystem-start=" + (jpeg ? "jpeg" : "png"));
    String phase = "construct-image";
    Image generated = null;
    Image fromFile = null;
    Image fromPath = null;
    try {
      generated = new Image(9, 7);
      phase = "fill-pixels";
      int[] generatedPixels = generated.getPixels();
      for (int y = 0; y < generated.getPixelHeight(); y++) {
        for (int x = 0; x < generated.getPixelWidth(); x++) {
          generatedPixels[y * generated.getPixelWidth() + x] = 0xff000000
              | ((x * 37 & 0xff) << 16) | ((y * 53 & 0xff) << 8) | ((x * 11 + y * 17) & 0xff);
        }
      }
      phase = "write-image";
      try (File output = new File(path, File.CREATE_EMPTY)) {
        if (jpeg) {
          generated.createJpg(output, 92);
        } else {
          generated.createPng(output);
        }
      }
      smokePhase("filesystem-written=" + (jpeg ? "jpeg" : "png"));
      phase = "read-Image-File";
      try (File input = new File(path, File.READ_ONLY)) {
        fromFile = new Image(input);
      }
      phase = "decode-Image-File";
      int[] filePixels = fromFile.getPixels();
      result.fileLoaded = fromFile.getPixelWidth() > 0 && fromFile.getPixelHeight() > 0
          && filePixels.length > 0;
      smokePhase("filesystem-file-decoded=" + (jpeg ? "jpeg" : "png"));
      phase = "read-Image-String";
      try {
        fromPath = new Image(path);
      } catch (ImageException expectedOnUnfixedNativeRuntime) {
        result.pathImageException = true;
        return result;
      }
      result.pathLoaded = true;
      smokePhase("filesystem-path-loaded=" + (jpeg ? "jpeg" : "png"));
      phase = "compare-dimensions";
      if (fromFile.getPixelWidth() != fromPath.getPixelWidth()
          || fromFile.getPixelHeight() != fromPath.getPixelHeight()) {
        return result;
      }
      phase = "decode-Image-String";
      int[] pathPixels = fromPath.getPixels();
      if (filePixels.length != pathPixels.length) {
        return result;
      }
      phase = "compare-pixels";
      for (int i = 0; i < filePixels.length; i++) {
        if (filePixels[i] != pathPixels[i]) {
          return result;
        }
      }
      result.parity = true;
      smokePhase("filesystem-parity=" + (jpeg ? "jpeg" : "png"));
      return result;
    } catch (Throwable failure) {
      result.error = phase + ":" + failure.getClass().getName() + ":" + failure.getMessage();
      return result;
    } finally {
      releaseEncodedSource(fromFile);
      releaseEncodedSource(fromPath);
      discardRuntimeFile(path);
    }
  }

  private static void discardRuntimeFile(String path) {
    try {
      File file = new File(path);
      if (file.exists()) {
        file.delete();
      }
    } catch (Exception ignored) {
      // The fixture reports file creation or loading failures through assertions.
    }
  }

  private static class FilesystemResult {
    boolean fileLoaded;
    boolean pathLoaded;
    boolean pathImageException;
    boolean parity;
    String error = "";
  }

  private static void releaseEncodedSource(Image image) {
    if (image != null) {
      ImagePipeline pipeline = image.pipelineForSmoke();
      if (pipeline != null && pipeline.root() instanceof EncodedImageSource) {
        ((EncodedImageSource) pipeline.root()).releaseForSmoke();
      }
    }
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
