// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.event.SizeChangeEvent;

/** Small deployed smoke for native generated and decoded Image backing. */
public class ImageNativeMaterializationSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    boolean pass = false;
    String failure = "";
    try {
      Image generated = new Image(32, 24);
      require(generated.hasNativeBackingForSmoke(), "generated native backing");
      Graphics generatedGraphics = generated.getGraphics();
      require(generatedGraphics != null, "generated graphics");
      generatedGraphics.backColor = Color.WHITE;
      generatedGraphics.fillRect(0, 0, generated.getWidth(), generated.getHeight());
      generatedGraphics.foreColor = Color.BLACK;
      generatedGraphics.drawRect(2, 2, 28, 20);
      require((generatedGraphics.getPixel(0, 0) & 0xFFFFFF) == Color.WHITE,
          "generated pixel readback");
      byte[] generatedRow = new byte[generated.getPixelWidth() * 4];
      generated.getPixelRow(generatedRow, 4);
      require((generatedRow[4 * 4] & 0xFF) == 255 && (generatedRow[4 * 4 + 3] & 0xFF) == 255,
          "generated row readback");

      ByteArrayStream encoded = new ByteArrayStream(1024);
      generated.createPng(encoded);
      Image decodedPng = new Image(encoded.getBuffer(), encoded.getPos());
      require(decodedPng.getPixelWidth() == 32 && decodedPng.getPixelHeight() == 24,
          "PNG dimensions");
      require(decodedPng.hasNativeBackingForSmoke(), "PNG native backing");
      int[] pngSnapshot = decodedPng.getPixels();
      pngSnapshot[4] = 0;
      require(decodedPng.hasNativeBackingForSmoke(), "PNG snapshot does not replace backing");
      byte[] decodedRow = new byte[decodedPng.getPixelWidth() * 4];
      decodedPng.getPixelRow(decodedRow, 4);
      int decodedRed = decodedRow[4 * 4] & 0xFF;
      int decodedAlpha = decodedRow[4 * 4 + 3] & 0xFF;
      require(decodedRed == 255 && decodedAlpha == 255,
          "PNG alpha readback red=" + decodedRed + ",alpha=" + decodedAlpha);

      byte[] jpeg = Vm.getFile("image-abi/back3.jpg");
      require(jpeg != null && jpeg.length > 0, "JPEG resource");
      Image decodedJpeg = new Image(jpeg, jpeg.length);
      require(decodedJpeg.hasNativeBackingForSmoke(), "JPEG native backing");
      Image targetedJpeg = Image.getJpegBestFit("image-abi/back3.jpg", 200, 113);
      require(targetedJpeg.getPixelWidth() == 200 && targetedJpeg.getPixelHeight() == 113
          && targetedJpeg.hasNativeBackingForSmoke(), "targeted JPEG native backing");

      Image retry = new Image(encoded.getBuffer(), encoded.getPos());
      Image.failNextNativeMaterializationForTest();
      boolean failed = false;
      try {
        retry.getPixelRow(new byte[32 * 4], 0);
      } catch (IllegalStateException expected) {
        failed = true;
      }
      require(failed && retry.pipelineForSmoke() != null, "native decode retry state");
      retry.getPixelRow(new byte[32 * 4], 0);
      require(retry.hasNativeBackingForSmoke(), "native decode retry success");

      Image large = new Image(500, 500);
      Graphics largeGraphics = large.getGraphics();
      require(largeGraphics != null, "large generated graphics");
      largeGraphics.backColor = Color.WHITE;
      largeGraphics.fillRect(0, 0, large.getWidth(), large.getHeight());
      Image snapshotScale = large.getSmoothScaledInstance(89, 89);
      largeGraphics.backColor = Color.BLACK;
      largeGraphics.fillRect(0, 0, large.getWidth(), large.getHeight());
      require(snapshotScale.hasNativeBackingForSmoke(), "native snapshot scale backing");
      byte[] snapshotRow = new byte[89 * 4];
      snapshotScale.getPixelRow(snapshotRow, 0);
      require((snapshotRow[0] & 0xFF) == 255 && (snapshotRow[1] & 0xFF) == 255
          && (snapshotRow[2] & 0xFF) == 255, "native snapshot isolation");
      ByteArrayStream largeEncoded = new ByteArrayStream(4096);
      large.createPng(largeEncoded);
      ImageControl control = new ImageControl(new Image(largeEncoded.getBuffer(), largeEncoded.getPos()));
      control.scaleToFit = true;
      Graphics screenGraphics = getGraphics();
      for (int i = 0; i < 3; i++) {
        control.setRect(0, 0, 89, 89);
        control.onEvent(new SizeChangeEvent(control, 89, 89));
        Image resized = control.getImage();
        require(resized != null && resized.getPixelWidth() == 89 && resized.getPixelHeight() == 89,
            "ImageControl resize dimensions");
        require(resized.hasNativeBackingForSmoke(), "ImageControl native backing");
        screenGraphics.drawImage(resized, 0, 0, true);
      }
      pass = true;
    } catch (Throwable failureCause) {
      failure = failureCause.toString();
    }
    System.out.println("fixture=ImageNativeMaterializationSmokeApp,renderer="
        + (Settings.isOpenGL ? "opengl" : "software") + ",pass=" + pass
        + (failure.length() == 0 ? "" : ",failure=" + failure.replace(' ', '_')));
    exit(pass ? 0 : 1);
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
