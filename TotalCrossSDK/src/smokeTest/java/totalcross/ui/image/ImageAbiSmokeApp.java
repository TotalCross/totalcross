// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package imageabi;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

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
        && pngRoundTripPass;
    byte[] commitBytes = Vm.getFile("image-abi/commit.txt");
    String commit = commitBytes == null ? "unknown" : new String(commitBytes).trim();
    System.out.println("fixture=ImageAbiSmokeApp,commit=" + commit + ",renderer="
        + (Settings.isOpenGL ? "opengl" : "software") + ",imageClass=totalcross.ui.image.Image"
        + ",constructorPass=" + constructorPass + ",decodePass=" + decodePass
        + ",logicalDimensionsPass=" + logicalDimensionsPass + ",physicalDimensionsPass=" + physicalDimensionsPass
        + ",framePass=" + framePass + ",colorMutationPass=" + colorMutationPass + ",resizePass=" + resizePass
        + ",textureUploadPass=" + textureUploadPass + ",textureRecreatePass=" + textureRecreatePass
        + ",pngRoundTripPass=" + pngRoundTripPass + ",overallPass=" + overallPass
        + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
