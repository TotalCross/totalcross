// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.File;
import totalcross.sys.Vm;
import totalcross.ui.MainWindow;

/** Minimal deployed control for comparing Image(File) and Image(byte[]) on Linux. */
public final class LinuxPngDecodeComparisonApp extends MainWindow {
  private static final String PNG_FIXTURE = "comparison/tiny.png";
  private static final String JPEG_FIXTURE = "comparison/control.jpg";

  @Override
  public void initUI() {
    byte[] png = Vm.getFile(PNG_FIXTURE);
    byte[] jpeg = Vm.getFile(JPEG_FIXTURE);
    if (png == null || jpeg == null) {
      System.out.println("fixture=LinuxPngDecodeComparisonApp,assets=missing,png=" + (png != null)
          + ",jpeg=" + (jpeg != null) + ",overallPass=false");
      System.out.flush();
      MainWindow.exit(0);
      return;
    }

    boolean pngWritten = writeFixture("comparison-png.png", png);
    boolean jpegWritten = writeFixture("comparison-control.jpg", jpeg);
    System.out.println("fixture=LinuxPngDecodeComparisonApp,assets=loaded,pngLength=" + png.length
        + ",jpegLength=" + jpeg.length + ",pngWritten=" + pngWritten + ",jpegWritten=" + jpegWritten);

    boolean pngFile = runCase("png-file", "Image(File)", png, "comparison-png.png", pngWritten);
    boolean pngBytes = runCase("png-bytes", "Image(byte[])", png, null, true);
    boolean jpegFile = runCase("jpeg-file", "Image(File)", jpeg, "comparison-control.jpg", jpegWritten);
    boolean jpegBytes = runCase("jpeg-bytes", "Image(byte[])", jpeg, null, true);
    boolean overallPass = pngFile && pngBytes && jpegFile && jpegBytes;
    System.out.println("comparison=complete,pngFileDecode=" + pngFile + ",pngBytesDecode=" + pngBytes
        + ",jpegFileDecode=" + jpegFile + ",jpegBytesDecode=" + jpegBytes
        + ",overallPass=" + overallPass);
    System.out.flush();
    MainWindow.exit(0);
  }

  private static boolean writeFixture(String path, byte[] bytes) {
    File file = null;
    try {
      file = new File(path, File.CREATE_EMPTY);
      int offset = 0;
      while (offset < bytes.length) {
        int written = file.writeBytes(bytes, offset, bytes.length - offset);
        if (written <= 0) {
          throw new IllegalStateException("file write stopped at " + offset);
        }
        offset += written;
      }
      return true;
    } catch (Throwable failure) {
      System.out.println("fixtureWrite=failed,path=" + path + ",exception=" + describe(failure));
      return false;
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (Throwable failure) {
          System.out.println("fixtureClose=failed,path=" + path + ",exception=" + describe(failure));
        }
      }
    }
  }

  private static boolean runCase(String name, String constructor, byte[] bytes, String path, boolean ready) {
    File file = null;
    Image image = null;
    boolean constructed = false;
    boolean decoded = false;
    int width = -1;
    int height = -1;
    int pixelCount = -1;
    String exception = "none";
    try {
      if (!ready) {
        throw new IllegalStateException("filesystem fixture write failed");
      }
      if (path == null) {
        image = new Image(bytes);
      } else {
        file = new File(path, File.READ_ONLY);
        image = new Image(file);
      }
      constructed = true;
    } catch (Throwable failure) {
      exception = describe(failure);
    }

    if (constructed) {
      try {
        width = image.getPixelWidth();
        height = image.getPixelHeight();
        int[] pixels = image.getPixels();
        pixelCount = pixels == null ? 0 : pixels.length;
        decoded = pixels != null && pixels.length > 0;
        if (!decoded) {
          exception = "pixels-null";
        }
      } catch (Throwable failure) {
        exception = describe(failure);
      }
    }

    if (file != null) {
      try {
        file.close();
      } catch (Throwable failure) {
        exception = exception.equals("none") ? describe(failure) : exception + "_close=" + describe(failure);
      }
    }

    System.out.println("case=" + name + ",constructor=" + constructor + ",construct=" + constructed
        + ",decode=" + decoded + ",width=" + width + ",height=" + height
        + ",pixelCount=" + pixelCount + ",exception=" + exception);
    return constructed && decoded;
  }

  private static String describe(Throwable failure) {
    String message = failure.getMessage();
    String value = failure.getClass().getName() + (message == null ? "" : ":" + message);
    return value.replace(' ', '_').replace(',', '_').replace('\n', '_').replace('\r', '_');
  }
}
