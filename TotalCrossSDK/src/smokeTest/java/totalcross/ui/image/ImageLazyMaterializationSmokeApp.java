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
  private static final int NATIVE_RED = 0xFF0000FF;
  private static final int NATIVE_BLUE = 0x0000FFFF;

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
    boolean transformFamiliesDeferred = false;
    boolean transformFamiliesResolve = false;
    boolean transformChainDeferred = false;
    boolean transformChainResolve = false;
    boolean encodedRootShared = false;
    boolean rasterSnapshotIsolated = false;
    boolean rotationFillRegression = false;
    boolean frameTransformPass = false;
    boolean drawTransformBarrier = false;
    boolean largeImageHashPass = false;
    boolean smoothScaleNoOpCompatibility = false;
    boolean rotationNoOpCompatibility = false;
    boolean transformEquivalence = false;
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

      Image nativeRetry = new Image(jpeg);
      boolean firstNativeAllocationFailed = false;
      Image.failNextNativeMaterializationForTest();
      try {
        nativeRetry.getPixels();
      } catch (Throwable expected) {
        firstNativeAllocationFailed = true;
      }
      int[] retriedNativePixels = nativeRetry.getPixels();
      nativeAllocationRetryable = firstNativeAllocationFailed && retriedNativePixels != null
          && retriedNativePixels.length == 2;
      require(nativeAllocationRetryable, "native allocation failure retry");

      byte[] transformBytes = Vm.getFile("image-abi/tiny.png");
      Image transformSource = new Image(transformBytes);
      Image replicated = transformSource.getScaledInstance(12, 10);
      Image smooth = transformSource.getSmoothScaledInstance(12, 10);
      Image touched = transformSource.getTouchedUpInstance((byte) 16, (byte) -8);
      Image faded = transformSource.getFadedInstance(0x00112233);
      Image alpha = transformSource.getAlphaInstance(-32);
      Image rotatedSquare = transformSource.getRotatedScaledInstance(100, 30, 0xFF123456);
      transformFamiliesDeferred = replicated.pixels == null && smooth.pixels == null
          && touched.pixels == null && faded.pixels == null && alpha.pixels == null
          && rotatedSquare.pixels == null;
      require(transformFamiliesDeferred, "encoded transform deferment");
      transformFamiliesResolve = replicated.getPixels() != null && smooth.getPixels() != null
          && touched.getPixels() != null && faded.getPixels() != null && alpha.getPixels() != null
          && rotatedSquare.getPixels() != null;
      require(transformFamiliesResolve, "encoded transform resolution");

      Image chainSource = new Image(transformBytes);
      Image chained = chainSource.getSmoothScaledInstance(20, 18)
          .getTouchedUpInstance((byte) 8, (byte) 0)
          .getRotatedScaledInstance(100, 30, 0xFF123456)
          .getScaledInstance(8, 8);
      transformChainDeferred = chained.pixels == null && chained.pipelineForSmoke() != null
          && chained.pipelineForSmoke().previous() != null
          && chained.pipelineForSmoke().previous().previous() != null;
      require(transformChainDeferred, "transform chain deferment");
      transformChainResolve = chained.getPixels() != null && chained.getPixelWidth() == 8
          && chained.getPixelHeight() == 8;
      require(transformChainResolve, "transform chain resolution");

      Image sharedSource = new Image(transformBytes);
      Image sharedFirst = sharedSource.getScaledInstance(10, 10);
      Image sharedSecond = sharedSource.getAlphaInstance(-16);
      encodedRootShared = sharedFirst.pipelineForSmoke().root() == sharedSecond.pipelineForSmoke().root();
      require(encodedRootShared, "encoded transform root sharing");

      Image mutable = new Image(4, 2);
      fill(mutable, NATIVE_RED);
      Image snapshot = mutable.getSmoothScaledInstance(2, 1);
      mutable.pixels[0] = NATIVE_BLUE;
      snapshot.getPixels();
      rasterSnapshotIsolated = rowContainsRgb(snapshot, 0xFF, 0x00, 0x00);
      require(rasterSnapshotIsolated, "raster transform snapshot");

      Image rotationSource = new Image(3, 2);
      fill(rotationSource, NATIVE_RED);
      Image deferredRotation = rotationSource.getRotatedScaledInstance(100, 45, 0xFF123456);
      boolean rotationDeferred = deferredRotation.pixels == null;
      rotationFillRegression = rotationDeferred && deferredRotation.getPixelWidth() == 4
          && deferredRotation.getPixelHeight() == 3
          && containsRgb(deferredRotation, 0x12, 0x34, 0x56);
      require(rotationFillRegression, "deferred rotation fill");

      Image frames = new Image(8, 2);
      fill(frames, NATIVE_RED);
      frames.setFrameCount(2);
      Image transformedFrames = frames.getScaledInstance(2, 1);
      frameTransformPass = transformedFrames.pixels == null && transformedFrames.getFrameCount() == 2
          && transformedFrames.getPixelWidth() == 2 && transformedFrames.getPixels() != null
          && transformedFrames.getFrameCount() == 2;
      require(frameTransformPass, "deferred frame transform");

      Image drawTransform = new Image(transformBytes).getSmoothScaledInstance(10, 10);
      target.drawImage(drawTransform, 5, 5);
      drawTransformBarrier = drawTransform.pixels != null;
      require(drawTransformBarrier, "draw transform barrier");

      Image large = new Image(65, 65);
      fill(large, NATIVE_RED);
      Image expectedReduced = new Image(65, 65);
      fill(expectedReduced, NATIVE_RED);
      int expectedLargeHash = expectedReduced.getScaledInstance(64, 64).hashCode();
      largeImageHashPass = expectedLargeHash != 0 && large.hashCode() == expectedLargeHash;
      require(largeImageHashPass, "large image hash reduction");

      Image smoothSource = new Image(4, 2);
      fill(smoothSource, NATIVE_RED);
      Image smoothSame = smoothSource.getSmoothScaledInstance(4, 2);
      Image logicalSmoothSource = Image.createLogical(2, 1, 2);
      fill(logicalSmoothSource, NATIVE_RED);
      Image logicalSmooth = logicalSmoothSource.getSmoothScaledInstance(4, 2);
      smoothScaleNoOpCompatibility = smoothSame == smoothSource && logicalSmooth != logicalSmoothSource
          && logicalSmooth.getPixels() != null;
      require(smoothScaleNoOpCompatibility, "smooth-scale no-op compatibility");

      Image rotationNoOpSource = new Image(3, 2);
      fill(rotationNoOpSource, NATIVE_RED);
      Image rotationSame = rotationNoOpSource.getRotatedScaledInstance(100, 0, 0xFF123456);
      Image logicalRotationSource = Image.createLogical(3, 2, 2);
      fill(logicalRotationSource, NATIVE_RED);
      Image logicalRotation = logicalRotationSource.getRotatedScaledInstance(100, 0, 0xFF123456);
      rotationNoOpCompatibility = rotationSame == rotationNoOpSource && logicalRotation != logicalRotationSource
          && logicalRotation.getPixels() != null;
      require(rotationNoOpCompatibility, "rotation no-op compatibility");

      transformEquivalence = equivalentWithMaterializedRoot(transformBytes);
      require(transformEquivalence, "deferred transform equivalence");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = pngConstructionLazy && pngSourceCopy && warningCompatibility && firstDrawMaterializes
        && repeatedBarrierReusesPixels && pathSourceStable && jpegConstructionLazy
        && structuralInvalid && payloadInvalidDeferred && nativeAllocationRetryable
        && transformFamiliesDeferred && transformFamiliesResolve && transformChainDeferred
        && transformChainResolve && encodedRootShared && rasterSnapshotIsolated
        && rotationFillRegression && frameTransformPass && drawTransformBarrier && largeImageHashPass
        && smoothScaleNoOpCompatibility && rotationNoOpCompatibility && transformEquivalence;
    System.out.println("fixture=ImageLazyMaterializationSmokeApp,pngConstructionLazy=" + pngConstructionLazy
        + ",pngSourceCopy=" + pngSourceCopy + ",firstDrawMaterializes=" + firstDrawMaterializes
        + ",warningCompatibility=" + warningCompatibility
        + ",repeatedBarrierReusesPixels=" + repeatedBarrierReusesPixels + ",pathSourceStable=" + pathSourceStable
        + ",jpegConstructionLazy=" + jpegConstructionLazy + ",structuralInvalid=" + structuralInvalid
        + ",payloadInvalidDeferred=" + payloadInvalidDeferred + ",nativeAllocationRetryable="
        + nativeAllocationRetryable + ",transformFamiliesDeferred=" + transformFamiliesDeferred
        + ",transformFamiliesResolve=" + transformFamiliesResolve + ",transformChainDeferred="
        + transformChainDeferred + ",transformChainResolve=" + transformChainResolve
        + ",encodedRootShared=" + encodedRootShared + ",rasterSnapshotIsolated=" + rasterSnapshotIsolated
        + ",rotationFillRegression=" + rotationFillRegression + ",frameTransformPass=" + frameTransformPass
        + ",drawTransformBarrier=" + drawTransformBarrier + ",largeImageHashPass=" + largeImageHashPass
        + ",smoothScaleNoOpCompatibility=" + smoothScaleNoOpCompatibility
        + ",rotationNoOpCompatibility=" + rotationNoOpCompatibility
        + ",transformEquivalence=" + transformEquivalence + ",overallPass=" + overallPass
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

  private static void fill(Image image, int pixel) {
    int[] pixels = image.getPixels();
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = pixel;
    }
  }

  private static boolean rowContainsRgb(Image image, int red, int green, int blue) {
    byte[] row = new byte[image.getPixelWidth() * 4];
    image.getPixelRow(row, 0);
    for (int i = 0; i < row.length; i += 4) {
      if ((row[i] & 0xFF) == red && (row[i + 1] & 0xFF) == green && (row[i + 2] & 0xFF) == blue) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsRgb(Image image, int red, int green, int blue) {
    for (int y = 0; y < image.getPixelHeight(); y++) {
      byte[] row = new byte[image.getPixelWidth() * 4];
      image.getPixelRow(row, y);
      for (int i = 0; i < row.length; i += 4) {
        if ((row[i] & 0xFF) == red && (row[i + 1] & 0xFF) == green && (row[i + 2] & 0xFF) == blue) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean equivalentWithMaterializedRoot(byte[] bytes) throws ImageException {
    return samePixels(new Image(bytes).getScaledInstance(12, 10), materialized(bytes).getScaledInstance(12, 10))
        && samePixels(new Image(bytes).getSmoothScaledInstance(12, 10),
            materialized(bytes).getSmoothScaledInstance(12, 10))
        && samePixels(new Image(bytes).getRotatedScaledInstance(100, 30, 0xFF123456),
            materialized(bytes).getRotatedScaledInstance(100, 30, 0xFF123456))
        && samePixels(new Image(bytes).getTouchedUpInstance((byte) 16, (byte) -8),
            materialized(bytes).getTouchedUpInstance((byte) 16, (byte) -8))
        && samePixels(new Image(bytes).getFadedInstance(0x00112233),
            materialized(bytes).getFadedInstance(0x00112233))
        && samePixels(new Image(bytes).getAlphaInstance(-32), materialized(bytes).getAlphaInstance(-32));
  }

  private static Image materialized(byte[] bytes) throws ImageException {
    Image image = new Image(bytes);
    image.getPixels();
    return image;
  }

  private static boolean samePixels(Image first, Image second) {
    if (first.getPixelWidth() != second.getPixelWidth() || first.getPixelHeight() != second.getPixelHeight()) {
      return false;
    }
    for (int y = 0; y < first.getPixelHeight(); y++) {
      byte[] firstRow = new byte[first.getPixelWidth() * 4];
      byte[] secondRow = new byte[second.getPixelWidth() * 4];
      first.getPixelRow(firstRow, y);
      second.getPixelRow(secondRow, y);
      for (int x = 0; x < firstRow.length; x++) {
        if (firstRow[x] != secondRow[x]) {
          return false;
        }
      }
    }
    return true;
  }
}
