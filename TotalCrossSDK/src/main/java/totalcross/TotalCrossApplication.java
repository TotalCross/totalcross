package totalcross;

import tc.Help;
import totalcross.ui.MainWindow;

public class TotalCrossApplication {
  private TotalCrossApplication() {

  }

  public static void run(Class<? extends MainWindow> clazz, String... args) {
    if (clazz == null || (args.length > 0 && args[0].equals("/help"))) {
      clazz = Help.class;
      args = new String[] { "/scr", "android", "/fontsize", "20", "/fingertouch" };
    }
    Launcher.isApplication = true;

    Launcher app = new Launcher();
    app.parseArguments(clazz.getCanonicalName(), args);
    app.init();
  }
}
