package tc.samples.app.store;

import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Label;
import totalcross.ui.MainWindow;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.util.Random;

/**
 * This is a sample of a Login page with a list of products. Very simple.
 */

public class TCProductStore extends MainWindow implements PSConstants {
  static {
    Settings.companyInfo = "TotalCross";
    Settings.appPackagePublisher = "53F995CF-1FB5-4EC3-84DD-A694BE4CFD1A";
    Settings.appPackageIdentifier = "1748TotalCross.ProductStore";
    Settings.iosCFBundleIdentifier = "com.totalcross.tcps";
    Settings.appVersion = "1.01";
    Settings.windowSize = Settings.WINDOWSIZE_480X640;
  }

  /** The path where the images are stored. */
  public static String imagePath;

  public TCProductStore() {
    setUIStyle(Settings.Holo); // change the user interface style
    setBackColor(MW_BACKCOLOR);
    Settings.uiAdjustmentsBasedOnFontHeight = true; // enable adjustments based on font, not pixels
    Settings.scrollDistanceOnMouseWheelMove = fmH * 10;
  }

  @Override
  public void initUI() {
    checkImages();
    new PSlogin().swapToTopmostWindow();
  }

  private void checkImages() {
    // create randomly colorized images on disk so they can be read by the image list
    Label l = null;
    try {
      imagePath = Settings.appPath + "/images/";
      Vm.debug("Image path: " + imagePath);

      File f = new File(imagePath);
      if (!f.exists()) {
        f.createDir();
      }

      f = new File(imagePath + "almof00.png");
      if (!f.exists()) {
        // show UI
        l = new Label("Preparing sample images...");
        l.setForeColor(Color.WHITE);
        add(l, CENTER, CENTER);
        repaintNow();
        // create and same images
        Random r = new Random();
        Image orig = new Image("img/almof.png").getSmoothScaledInstance(fmH * 3, fmH * 3);
        ByteArrayStream bas = new ByteArrayStream(40 * 1024);
        for (int i = 0; i < 100; i++) {
          bas.reset();
          Image c = orig.getCopy();
          c.applyColor2(Color.getRandomColor(r));
          c.createPng(bas);
          new File(imagePath + "almof" + (i < 10 ? "0" + i : i) + ".png", File.CREATE_EMPTY)
              .writeAndClose(bas.toByteArray());
        }
      }
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
    if (l != null) {
      remove(l);
    }
  }
}
