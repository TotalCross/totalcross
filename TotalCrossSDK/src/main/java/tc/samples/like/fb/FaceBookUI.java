package tc.samples.like.fb;

import tc.samples.like.fb.db.FBDB;
import tc.samples.like.fb.ui.FBButtonBar;
import tc.samples.like.fb.ui.FBImages;
import tc.samples.like.fb.ui.FBMenu;
import tc.samples.like.fb.ui.FBNewUser;
import tc.samples.like.fb.ui.FBPosts;
import tc.samples.like.fb.ui.FBTitleBar;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.ui.Container;
import totalcross.ui.MainWindow;
import totalcross.ui.TabbedContainer;
import totalcross.ui.Toast;
import totalcross.ui.Window;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.KeyListener;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class FaceBookUI extends MainWindow implements FBConstants {
  static {
    Settings.companyContact = "registro@totalcross.com";
    Settings.companyInfo = "TotalCross";
    //Settings.appPackagePublisher = "53F995CF-1FB5-4EC3-84DD-A694BE4CFD1A";
    //Settings.appPackageIdentifier = "1748TotalCross.TotalCrossAPI";
    Settings.iosCFBundleIdentifier = "com.totalcross.fbui";
    Settings.windowSize = Settings.WINDOWSIZE_480X640;
  }

  private static FBPosts posts;
  private static FBButtonBar bar;
  private static TabbedContainer tc;

  public static Image defaultPhoto;
  public static String defaultUser;

  public FaceBookUI() {
    Settings.uiAdjustmentsBasedOnFontHeight = true;
    setUIStyle(Settings.Android);
    setBackColor(CONTENTH);
  }

  @Override
  public void initUI() {
    try {
      Toast.backColor = Color.YELLOW;
      Toast.foreColor = Color.BLACK;

      FBImages.load(fmH);
      FBDB.db.loadActiveUser();
      add(new FBTitleBar(), LEFT, TOP, FILL, FONTSIZE + 250);
      add(bar = new FBButtonBar(), LEFT, AFTER, FILL, SAME);
      String[] tits = { "1", "2", "3", "4", "5" };
      tc = new TabbedContainer(tits);
      tc.setBackColor(CONTENTH);
      tc.setType(TabbedContainer.TABS_NONE);
      add(tc, LEFT, AFTER, FILL, FILL);
      tc.setContainer(0, posts = new FBPosts());
      tc.setContainer(4, new FBMenu());
      Container c = tc.getContainer(1);
      c.add(new FBNewUser(posts), CENTER, AFTER + 25, PARENTSIZE + 96, PREFERRED);

      if (defaultUser == null) {
        tc.setActiveTab(1);
      }

      Window.keyHook = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void actionkeyPressed(KeyEvent e) {
        }

        @Override
        public void specialkeyPressed(KeyEvent e) {
          if (e.key == SpecialKeys.ESCAPE) {
            e.consumed = true;
            MainWindow.exit(0);
          }
        }
      };
    } catch (Throwable t) {
      MessageBox.showException(t, true);
      exit(0);
    }
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case ControlEvent.PRESSED:
      if (e.target == tc) {
        bar.setPressed(tc.getActiveTab());
      } else if (e.target == bar) {
        tc.setActiveTab(bar.last);
      }
    }
  }
}
