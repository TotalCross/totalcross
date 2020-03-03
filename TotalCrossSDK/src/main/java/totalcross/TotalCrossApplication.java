package totalcross;

import tc.Help;
import totalcross.ui.MainWindow;

/**
 * This class represents a runnable Totalcross Java Application. It supports the
 * same arguments supported by {@link totalcross.Launcher}. See the example
 * bellow:
 * 
 * <pre>
 *
 * import totalcross.TotalCrossApplication;
 * import totalcross.sample.main.TCSample;
 * 
 * public class TCSampleApplication {
 * 
 *   public static void main(String[] args) {
 *     TotalCrossApplication.run(TCSample.class, "/scr", "360x568");
 *   }
 * }
 * </pre>
 */
public class TotalCrossApplication {

  private TotalCrossApplication() {
  }

  /**
   * Runs a TotalCross application on Java using the totalcross.Launcher, it
   * receives classes that extends {@link totalcross.ui.MainWindow} and the
   * {@link totalcross.Launcher} arguments.
   * 
   * @param clazz
   *          the class that extends MainWindow
   * @param args
   *          Launcher arguments
   * @see totalcross.ui.MainWindow
   * @see totalcross.Launcher
   */
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
