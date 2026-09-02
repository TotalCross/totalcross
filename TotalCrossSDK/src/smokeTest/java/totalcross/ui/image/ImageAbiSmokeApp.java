// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package imageabi;

import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;

/** Native macOS smoke fixture for the unified Image field ABI and lifecycle. */
public class ImageAbiSmokeApp extends MainWindow {
  private static final int JAVA_RED = 0xFFFF0000;
  private static final int JAVA_BLUE = 0xFF0000FF;
  private static final int JAVA_GRAY = 0xFF202020;
  // Native Pixel is stored in the VM's byte order in the Java int array.
  private static final int NATIVE_RED = 0xFF0000FF;
  private static final int NATIVE_BLUE = 0x0000FFFF;
  private static final int NATIVE_GRAY = 0x202020FF;
  private static final int FRAME_A = 0xFF102030;
  private static final int FRAME_B = 0xFF405060;

  @Override
  public void initUI() {
    boolean constructorPass = false;
    boolean decodePass = false;
    boolean logicalDimensionsPass = false;
    boolean physicalDimensionsPass = false;
    boolean framePass = false;
    boolean colorMutationPass = false;
    boolean resizePass = false;
    boolean textureUploadPass = false;
    boolean textureRecreatePass = false;
    boolean pngRoundTripPass = false;
    boolean replicateScalePass = false;
    boolean smoothScalePass = false;
    boolean rotationPass = false;
    boolean touchUpPass = false;
    boolean fadePass = false;
    boolean alphaPass = false;
    boolean hwScaleCopyPass = false;
    boolean jpegBestFitPass = false;
    boolean jpegScaledPass = false;
    boolean jpegBestFitScalePass = false;
    boolean jpegOddDimensionPass = false;
    boolean jpegArgumentValidationPass = false;
    String error = "";

    try {
      Image fixed = new Image(7, 5);
      constructorPass = fixed.getWidth() == 7 && fixed.getHeight() == 5 && fixed.getContentScale() == 1
          && fixed.getPixelWidth() == 7 && fixed.getPixelHeight() == 5;
      require(constructorPass, "fixed image dimensions");

      Image logical = Image.createLogical(7, 5, 2);
      logicalDimensionsPass = logical.getWidth() == 7 && logical.getHeight() == 5
          && logical.getContentScale() == 2;
      physicalDimensionsPass = logical.getPixelWidth() == 14 && logical.getPixelHeight() == 10;
      require(logicalDimensionsPass && physicalDimensionsPass, "logical image dimensions");

      byte[] encoded = Vm.getFile("image-abi/tiny.png");
      require(encoded != null && encoded.length > 0, "tiny PNG resource");
      Image pathImage = new Image("image-abi/tiny.png");
      Image streamImage = new Image(new ByteArrayStream(encoded));
      Image byteImage = new Image(encoded, encoded.length);
      decodePass = pathImage.getPixelWidth() == 36 && pathImage.getPixelHeight() == 36
          && streamImage.getPixelWidth() == pathImage.getPixelWidth()
          && streamImage.getPixelHeight() == pathImage.getPixelHeight()
          && byteImage.getPixelWidth() == pathImage.getPixelWidth()
          && byteImage.getPixelHeight() == pathImage.getPixelHeight()
          && pathImage.getClass().getName().equals("totalcross.ui.image.Image");
      require(decodePass, "PNG decode paths");

      byte[] jpegBytes = Vm.getFile("image-abi/back3.jpg");
      require(jpegBytes != null && jpegBytes.length > 0, "JPEG resource");
      File jpegFile = new File("back3-file.jpg", File.CREATE_EMPTY);
      jpegFile.writeBytes(jpegBytes, 0, jpegBytes.length);
      jpegFile.close();

      Image bestFit = Image.getJpegBestFit("image-abi/back3.jpg", 200, 113);
      jpegBestFitPass = hasDimensions(bestFit, 200, 113);
      require(jpegBestFitPass, "JPEG best-fit logical/pixel dimensions");

      Image scaledTcz = Image.getJpegScaled("image-abi/back3.jpg", 1, 2);
      Image scaledFile = Image.getJpegScaled("back3-file.jpg", 1, 2);
      Image scaledThreeQuarters = Image.getJpegScaled("image-abi/back3.jpg", 3, 4);
      jpegScaledPass = hasDimensions(scaledTcz, 800, 450) && hasDimensions(scaledFile, 800, 450)
          && hasDimensions(scaledThreeQuarters, 1200, 675);
      require(jpegScaledPass, "JPEG scaled logical/pixel dimensions");

      jpegBestFitScalePass = hasDimensions(Image.getJpegBestFit("image-abi/back3.jpg", 200, 113), 200, 113)
          && hasDimensions(Image.getJpegBestFit("image-abi/back3.jpg", 400, 225), 400, 225)
          && hasDimensions(Image.getJpegBestFit("image-abi/back3.jpg", 800, 450), 800, 450)
          && hasDimensions(Image.getJpegBestFit("image-abi/back3.jpg", 1600, 900), 1600, 900)
          && hasDimensions(Image.getJpegBestFit("image-abi/back3.jpg", 201, 114), 400, 225)
          && hasDimensions(Image.getJpegBestFit("image-abi/back3.jpg", 401, 226), 800, 450)
          && hasDimensions(Image.getJpegBestFit("back3-file.jpg", 801, 451), 1600, 900);
      require(jpegBestFitScalePass, "JPEG best-fit libjpeg scale boundaries");

      Image oddJpegSource = new Image(1601, 901);
      File oddJpegFile = new File("odd-jpeg.jpg", File.CREATE_EMPTY);
      oddJpegSource.createJpg(oddJpegFile, 90);
      oddJpegFile.close();
      jpegOddDimensionPass = hasDimensions(Image.getJpegBestFit("odd-jpeg.jpg", 201, 113), 201, 113)
          && hasDimensions(Image.getJpegBestFit("odd-jpeg.jpg", 200, 200), 201, 113)
          && hasDimensions(Image.getJpegBestFit("odd-jpeg.jpg", 402, 200), 401, 226)
          && hasDimensions(Image.getJpegScaled("odd-jpeg.jpg", 1, 8), 201, 113)
          && hasDimensions(Image.getJpegScaled("odd-jpeg.jpg", 3, 4), 1201, 676);
      require(jpegOddDimensionPass, "JPEG odd dimensions use libjpeg ceiling");

      jpegArgumentValidationPass = rejectsJpegBestFit("image-abi/back3.jpg", 0, 113)
          && rejectsJpegBestFit("image-abi/back3.jpg", 200, -1)
          && rejectsJpegScaled("image-abi/back3.jpg", 0, 1)
          && rejectsJpegScaled("image-abi/back3.jpg", 1, 0)
          && rejectsJpegScaled("image-abi/back3.jpg", -1, 1)
          && rejectsJpegScaled("image-abi/back3.jpg", 1, -1)
          && rejectsJpegScaled("image-abi/back3.jpg", Integer.MIN_VALUE, 1);
      require(jpegArgumentValidationPass, "JPEG invalid arguments");

      Image mutable = new Image(2, 2);
      int[] mutablePixels = mutable.getPixels();
      for (int i = 0; i < mutablePixels.length; i++) {
        mutablePixels[i] = NATIVE_RED;
      }
      mutable.changeColors(JAVA_RED, JAVA_BLUE);
      int changedPixel = mutablePixels[0];
      colorMutationPass = changedPixel == NATIVE_BLUE;
      mutable.changeColors(JAVA_BLUE, JAVA_GRAY);
      int grayPixel = mutablePixels[0];
      mutable.applyColor(0x00202020);
      byte[] mutatedRow = new byte[8];
      mutable.getPixelRow(mutatedRow, 0);
      colorMutationPass = colorMutationPass && grayPixel == NATIVE_GRAY
          && mutatedRow[0] > 32 && mutatedRow[0] < 64 && mutatedRow[1] == mutatedRow[0]
          && mutatedRow[2] == mutatedRow[0] && (mutatedRow[3] & 0xFF) == 0xFF;
      require(colorMutationPass, "native pixel mutations changed=" + changedPixel + ",final=" + mutablePixels[0]
          + ",row=" + (mutatedRow[0] & 0xFF) + "/" + (mutatedRow[1] & 0xFF) + "/"
          + (mutatedRow[2] & 0xFF) + "/" + (mutatedRow[3] & 0xFF));

      Image scaled = pathImage.getSmoothScaledInstance(7, 5);
      resizePass = scaled.getWidth() == 7 && scaled.getHeight() == 5
          && scaled.getPixelWidth() == 7 && scaled.getPixelHeight() == 5;
      require(resizePass, "smooth resize dimensions");

      Image transformSource = new Image(4, 2);
      fill(transformSource, NATIVE_RED);
      Image replicated = transformSource.getScaledInstance(8, 4);
      replicateScalePass = replicated.getPixelWidth() == 8 && replicated.getPixelHeight() == 4
          && rowContainsRgb(replicated, 0xFF, 0x00, 0x00);
      require(replicateScalePass, "replicate scale dimensions=" + replicated.getPixelWidth() + "x"
          + replicated.getPixelHeight() + ",row=" + rowSummary(replicated));

      Image smoothlyScaled = transformSource.getSmoothScaledInstance(2, 1);
      smoothScalePass = smoothlyScaled.getPixelWidth() == 2 && smoothlyScaled.getPixelHeight() == 1;
      require(smoothScalePass, "smooth scale");

      Image rotationSource = new Image(3, 2);
      fill(rotationSource, NATIVE_RED);
      Image rotated = rotationSource.getRotatedScaledInstance(100, 45, 0xFF123456);
      rotationPass = rotated.getPixelWidth() == 4 && rotated.getPixelHeight() == 3
          && containsRgb(rotated, 0x12, 0x34, 0x56);
      require(rotationPass, "non-square rotation dimensions/fill=" + rotated.getPixelWidth() + "x"
          + rotated.getPixelHeight() + ",pixels=" + imageSummary(rotated));

      byte[] sourceRow = row(transformSource);
      Image touchedUp = transformSource.getTouchedUpInstance((byte) 32, (byte) 0);
      touchUpPass = touchedUp.getPixelWidth() == 4 && touchedUp.getPixelHeight() == 2
          && differs(sourceRow, row(touchedUp));
      require(touchUpPass, "touch-up transformation");

      Image faded = transformSource.getFadedInstance(0x00112233);
      fadePass = faded.getPixelWidth() == 4 && faded.getPixelHeight() == 2
          && differs(sourceRow, row(faded));
      require(fadePass, "fade transformation");

      Image alpha = transformSource.getAlphaInstance(-64);
      byte[] alphaRow = row(alpha);
      alphaPass = alpha.getPixelWidth() == 4 && alpha.getPixelHeight() == 2
          && (alphaRow[3] & 0xFF) == 0xBF;
      require(alphaPass, "alpha transformation");

      Image hwScaleSource = new Image(4, 2);
      hwScaleSource.setHwScaleFixedAspectRatio(8, false);
      Image hwScaleDerived = hwScaleSource.getScaledInstance(2, 1);
      hwScaleCopyPass = hwScaleDerived.getWidth() == 4 && hwScaleDerived.getHeight() == 2;
      require(hwScaleCopyPass, "hardware scale on derived image");

      Image frames = new Image(8, 2);
      int[] framePixels = frames.getPixels();
      for (int i = 0; i < framePixels.length; i++) {
        framePixels[i] = (i % 8) < 4 ? FRAME_A : FRAME_B;
      }
      frames.setFrameCount(2);
      frames.setCurrentFrame(1);
      Image frame = frames.getFrameInstance(1);
      framePass = frames.getFrameCount() == 2 && frames.getCurrentFrame() == 1
          && frame.getPixelWidth() == 4 && frame.getPixelHeight() == 2
          && frame.getPixels()[0] == FRAME_B;
      require(framePass, "frame state and copy count=" + frames.getFrameCount() + ",current="
          + frames.getCurrentFrame() + ",width=" + frame.getPixelWidth() + ",height=" + frame.getPixelHeight()
          + ",pixel=" + frame.getPixels()[0]);

      frames.applyChanges();
      textureUploadPass = true;
      Graphics target = getGraphics();
      require(target != null, "main window graphics");
      target.drawImage(frames, 2, 2);
      frames.freeTexture();
      target.drawImage(frames, 10, 2);
      textureRecreatePass = true;

      ByteArrayStream png = new ByteArrayStream(256);
      mutable.createPng(png);
      Image roundTrip = new Image(png.getBuffer(), png.getPos());
      pngRoundTripPass = roundTrip.getPixelWidth() == mutable.getPixelWidth()
          && roundTrip.getPixelHeight() == mutable.getPixelHeight() && roundTrip.equals(mutable);
      require(pngRoundTripPass, "PNG round trip");
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = constructorPass && decodePass && logicalDimensionsPass && physicalDimensionsPass
        && framePass && colorMutationPass && resizePass && textureUploadPass && textureRecreatePass
        && pngRoundTripPass && replicateScalePass && smoothScalePass && rotationPass && touchUpPass
        && fadePass && alphaPass && hwScaleCopyPass && jpegBestFitPass && jpegScaledPass
        && jpegBestFitScalePass && jpegOddDimensionPass && jpegArgumentValidationPass;
    byte[] commitBytes = Vm.getFile("image-abi/commit.txt");
    String commit = commitBytes == null ? "unknown" : new String(commitBytes).trim();
    System.out.println("fixture=ImageAbiSmokeApp,commit=" + commit + ",renderer="
        + (Settings.isOpenGL ? "opengl" : "software") + ",imageClass=totalcross.ui.image.Image"
        + ",constructorPass=" + constructorPass + ",decodePass=" + decodePass
        + ",logicalDimensionsPass=" + logicalDimensionsPass + ",physicalDimensionsPass=" + physicalDimensionsPass
        + ",framePass=" + framePass + ",colorMutationPass=" + colorMutationPass + ",resizePass=" + resizePass
        + ",textureUploadPass=" + textureUploadPass + ",textureRecreatePass=" + textureRecreatePass
        + ",pngRoundTripPass=" + pngRoundTripPass + ",replicateScalePass=" + replicateScalePass
        + ",smoothScalePass=" + smoothScalePass + ",rotationPass=" + rotationPass
        + ",touchUpPass=" + touchUpPass + ",fadePass=" + fadePass + ",alphaPass=" + alphaPass
        + ",hwScaleCopyPass=" + hwScaleCopyPass + ",jpegBestFitPass=" + jpegBestFitPass
        + ",jpegScaledPass=" + jpegScaledPass + ",jpegBestFitScalePass=" + jpegBestFitScalePass
        + ",jpegOddDimensionPass=" + jpegOddDimensionPass
        + ",jpegArgumentValidationPass=" + jpegArgumentValidationPass
        + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }

  private static boolean hasDimensions(Image image, int width, int height) {
    return image != null && image.getWidth() == width && image.getHeight() == height
        && image.getPixelWidth() == width && image.getPixelHeight() == height
        && image.getContentScale() == 1;
  }

  private static boolean rejectsJpegBestFit(String path, int targetWidth, int targetHeight) {
    try {
      Image.getJpegBestFit(path, targetWidth, targetHeight);
      return false;
    } catch (ImageException expected) {
      return true;
    } catch (Exception unexpected) {
      return false;
    }
  }

  private static boolean rejectsJpegScaled(String path, int numerator, int denominator) {
    try {
      Image.getJpegScaled(path, numerator, denominator);
      return false;
    } catch (ImageException expected) {
      return true;
    } catch (Exception unexpected) {
      return false;
    }
  }

  private static void fill(Image image, int pixel) {
    int[] pixels = image.getPixels();
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = pixel;
    }
  }

  private static byte[] row(Image image) {
    return row(image, 0);
  }

  private static byte[] row(Image image, int y) {
    byte[] row = new byte[image.getPixelWidth() * 4];
    image.getPixelRow(row, y);
    return row;
  }

  private static boolean rowContainsRgb(Image image, int red, int green, int blue) {
    byte[] row = row(image);
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

  private static boolean differs(byte[] first, byte[] second) {
    if (first.length != second.length) {
      return true;
    }
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) {
        return true;
      }
    }
    return false;
  }

  private static String rowSummary(Image image) {
    return rowSummary(image, 0);
  }

  private static String rowSummary(Image image, int y) {
    byte[] row = row(image, y);
    StringBuilder summary = new StringBuilder();
    for (int i = 0; i < Math.min(row.length, 16); i++) {
      if (i > 0) {
        summary.append('/');
      }
      summary.append(row[i] & 0xFF);
    }
    return summary.toString();
  }

  private static String imageSummary(Image image) {
    StringBuilder summary = new StringBuilder();
    for (int y = 0; y < image.getPixelHeight(); y++) {
      if (y > 0) {
        summary.append('|');
      }
      summary.append(rowSummary(image, y));
    }
    return summary.toString();
  }
}
