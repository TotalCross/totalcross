// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import java.awt.Color;
import java.awt.event.ComponentListener;
import java.awt.event.WindowListener;

import tc.simulator.awt.AwtWindow;
import tc.preview.PreviewSession;
import tc.preview.PreviewFrameSink;
import tc.simulator.awt.WindowConfiguration;
import totalcross.MainClass;
import totalcross.sys.Settings;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.MainWindow;

/**
 * Owns the Java desktop launcher lifecycle independently of the caller.
 * <p>
 * IDE tooling should prefer this runtime or {@link PreviewRunner} instead of
 * constructing {@link Launcher} directly. The current implementation still uses
 * the existing launcher internals so desktop simulator behavior remains stable
 * while window and rendering responsibilities are extracted incrementally.
 */
public class Launcher extends SimulatorCore implements PreviewSession {
  public class UserFont extends FontRegistry.UserFont {
    protected UserFont(String fontName, String suffix, int size, totalcross.ui.font.Font base) throws Exception {
      super(fontName, suffix, size, base);
    }

    protected UserFont(String fontName, String suffix) throws Exception {
      super(fontName, suffix);
    }
  }
  private SimulatorConfiguration config;
  private LaunchOptions parsedConfig;
  private PreviewFrameSink previewFrameSurface;
  private boolean previewMode;
  private ClassLoader appClassLoader;
  private Launcher launcher;

  public Launcher() {
    super();
  }

  Launcher(PreviewFrameSink previewSurface, boolean previewMode) {
    super(previewSurface, previewMode);
  }

  Launcher(PreviewFrameSink previewSurface, boolean previewMode, ClassLoader appClassLoader) {
    super(previewSurface, previewMode, appClassLoader);
  }

  public static Launcher startApplication(String mainWindowClass, String... args) {
    Launcher runtime = new Launcher();
    runtime.parseArguments(mainWindowClass, args);
    runtime.startApplication();
    return runtime;
  }

  public static Launcher startPreview(String mainWindowClass, PreviewFrameSink surface, ClassLoader appClassLoader,
      String... args) {
    Launcher runtime = new Launcher();
    runtime.setPreviewFrameSurface(surface);
    runtime.setAppClassLoader(appClassLoader);
    runtime.parseArguments(mainWindowClass, args);
    runtime.startPreview();
    return runtime;
  }

  public static Launcher startPreviewFrames(String mainWindowClass, PreviewFrameSink surface,
      ClassLoader appClassLoader, String... args) {
    Launcher runtime = new Launcher();
    runtime.setPreviewFrameSurface(surface);
    runtime.setAppClassLoader(appClassLoader);
    runtime.parseArguments(mainWindowClass, args);
    runtime.startPreview();
    return runtime;
  }

  public void parseArguments(String mainWindowClass, String... args) {
    this.config = new SimulatorConfiguration(mainWindowClass, args);
  }

  public void configure(SimulatorConfiguration config) {
    if (config == null) {
      throw new IllegalArgumentException("config cannot be null");
    }
    this.config = config;
    this.parsedConfig = null;
  }

  public void recordLauncherUsage() {
    // Anonymous telemetry is intentionally disabled until its service contract
    // is replaced with a maintained, opt-in endpoint.
  }

  void parseSimulatorArguments(Launcher launcher, SimulatorConfiguration config, boolean application, int fallbackWidth,
      int fallbackHeight) throws CommandLineParser.InvalidArgumentException {
    configure(config);
    applyParsedConfig(launcher, CommandLineParser.parse(config, application, fallbackWidth, fallbackHeight));
  }

  public void setPreviewFrameSurface(PreviewFrameSink previewFrameSurface) {
    this.previewFrameSurface = previewFrameSurface;
  }

  public void setAppClassLoader(ClassLoader appClassLoader) {
    this.appClassLoader = appClassLoader;
  }

  public void startApplication() {
    previewMode = false;
    start(false);
  }

  public void startPreview() {
    previewMode = true;
    start(true);
  }

  public void pumpEvents() {
    if (launcher != null) {
      launcher.pumpEvents();
    }
  }

  /** Applies an IDE resize to the active desktop preview. */
  public void resizePreview(int width, int height, double density) {
    if (launcher == null) throw new IllegalStateException("Launcher has not been started");
    runPreviewCommand(() -> {
      Settings.screenDensity = density;
      Settings.screenWidth = width;
      Settings.screenHeight = height;
      if (launcher.hasWindowBackend()) launcher.setWindowSize(width, height, true);
      launcher.updateScreen();
    }, "preview resize");
  }

  private void runPreviewCommand(Runnable command, String operation) {
    if (launcher.eventLoop == null) {
      command.run();
      return;
    }
    if (!launcher.eventLoop.invoke(command, RuntimeState.PREVIEW_DESTROY_TIMEOUT_MILLIS)) {
      throw new IllegalStateException("Timed out waiting for TotalCross " + operation + ".");
    }
  }

  /** Injects a pointer transition from an external preview surface. */
  public void injectPreviewPointer(int x, int y, int button, boolean pressed) {
    if (launcher == null) throw new IllegalStateException("Launcher has not been started");
    launcher.dispatchPreviewPointer(x, y, button, pressed);
  }

  /** Injects a key transition from an external preview surface. */
  public void injectPreviewKey(int keyCode, boolean pressed, int modifiers) {
    if (launcher == null) throw new IllegalStateException("Launcher has not been started");
    launcher.dispatchPreviewKey(keyCode, pressed, modifiers);
  }

  void dispatchPreviewPointer(int x, int y, int button, boolean pressed) {
    super.injectPreviewPointer(x, y, button, pressed);
  }

  void dispatchPreviewKey(int keyCode, boolean pressed, int modifiers) {
    super.injectPreviewKey(keyCode, pressed, modifiers);
  }

  @Override
  public void resize(int width, int height, double density) {
    resizePreview(width, height, density);
  }

  @Override
  public void pointer(int x, int y, int button, boolean pressed) {
    injectPreviewPointer(x, y, button, pressed);
  }

  @Override
  public void key(int keyCode, boolean pressed, int modifiers) {
    injectPreviewKey(keyCode, pressed, modifiers);
  }

  public void stop() {
    if (launcher != null) {
      launcher.destroy();
      launcher = null;
    }
  }

  @Override
  public void close() {
    stop();
  }

  public void setNewMainWindow(MainWindow newInstance, String args) {
    if (launcher == null) {
      throw new IllegalStateException("Launcher has not been started");
    }
    launcher.replaceMainWindow(newInstance, args);
  }

   public void preparePreviewMainWindowReload() {
    if (launcher == null) {
      throw new IllegalStateException("Launcher has not been started");
    }
    launcher.preparePreviewMainWindowReload();
  }

   public void replaceMainWindow(MainWindow newInstance, String args) {
    if (launcher == null) {
      throw new IllegalStateException("Launcher has not been started");
    }
    launcher.replacePreviewMainWindow(newInstance, args);
  }

   public void showContainer(Container container) {
    if (launcher == null) {
      throw new IllegalStateException("Launcher has not been started");
    }
    launcher.showPreviewContainer(container);
  }

   public void showControl(Control control) {
    if (launcher == null) {
      throw new IllegalStateException("Launcher has not been started");
    }
    launcher.showPreviewControl(control);
  }

  void initializeSettings(Launcher launcher) {
    launcher.fillSettings();
  }

  void setParsedConfig(LaunchOptions parsedConfig) {
    this.parsedConfig = parsedConfig;
  }

  LaunchOptions getParsedConfig() {
    return parsedConfig;
  }

  void applyParsedConfig(Launcher launcher, LaunchOptions parsedConfig) {
    setParsedConfig(parsedConfig);
    applySettings(parsedConfig);
    launcher.applyParsedArguments(parsedConfig);
  }

   public MainWindow createMainWindow(String className, ClassLoader classLoader, boolean terminateIfMainClass)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    String normalizedClassName = normalizeMainWindowClassName(className);
    Class<?> mainClass = Class.forName(normalizedClassName, true, classLoader);
    boolean mainClassOnly = checkIfMainClass(mainClass);
    if (!mainClassOnly) {
      runtimeInstructions();
    }
    Object instance = mainClass.newInstance();
    if (instance instanceof MainClass && !(instance instanceof MainWindow)) {
      ((MainClass) instance).appStarting(0);
      ((MainClass) instance).appEnding();
      if (terminateIfMainClass) {
        System.exit(0);
      }
      return null;
    }
    return (MainWindow) instance;
  }

  AwtWindow startWindowBackend(Launcher launcher, String title, Color background,
      WindowListener windowListener, ComponentListener componentListener) {
    if (parsedConfig == null) {
      throw new IllegalStateException("Launcher arguments must be parsed before starting the window backend");
    }
    WindowConfiguration config = new WindowConfiguration(parsedConfig.width, parsedConfig.height, parsedConfig.scale, title,
        parsedConfig.x, parsedConfig.y, parsedConfig.fullscreen, Settings.resizableWindow, background, windowListener,
        componentListener);
    AwtWindow backend = new AwtWindow(launcher);
    backend.start(config);
    return backend;
  }

  Launcher getSimulatorCore() {
    return launcher;
  }

  boolean isPreviewMode() {
    return previewMode;
  }

  String getMainWindowClass() {
    return config == null ? null : config.getMainWindowClass();
  }

  String[] getLauncherArgs() {
    return config == null ? new String[0] : config.getLauncherArgs();
  }

  static String normalizeMainWindowClassName(String className) {
    if (className.endsWith(".class")) {
      className = className.substring(0, className.length() - 6);
    }
    return className.replace('/', '.');
  }

  private void start(boolean preview) {
    if (config == null) {
      throw new IllegalStateException("parseArguments must be called before start");
    }
    Launcher.isApplication = true;
    recordLauncherUsage();
    launcher = new Launcher(null, preview, appClassLoader);
    launcher.setPreviewFrameSink(previewFrameSurface);
    launcher.setRuntime(this);
    launcher.parseApplicationArguments(config.getMainWindowClass(), config.getLauncherArgs());
    launcher.init();
    if (preview) {
      launcher.startApp();
      launcher.pumpEvents();
      // A headless consumer has no AWT paint callback to trigger the first frame.
      launcher.updateScreen();
    }
  }

  private void applySettings(LaunchOptions parsedConfig) {
    Launcher.userFontSize = parsedConfig.userFontSize;
    if (parsedConfig.keyboardFocusTraversable) {
      Settings.keyboardFocusTraversable = true;
    }
    if (parsedConfig.fingerTouch) {
      Settings.fingerTouch = true;
    }
    if (parsedConfig.unmovableSIP) {
      Settings.unmovableSIP = true;
    }
    if (parsedConfig.geographicalFocus) {
      Settings.geographicalFocus = true;
    }
    if (parsedConfig.virtualKeyboard) {
      Settings.virtualKeyboard = true;
    }
    if (parsedConfig.showMousePosition) {
      Settings.showMousePosition = true;
    }
    if (parsedConfig.showDebugMessages) {
      Settings.showDebugMessages = true;
    }
    Settings.screenDensity = parsedConfig.densityValue;
    Settings.dataPath = parsedConfig.dataPath;
  }

  private static boolean checkIfMainClass(Class<?> c) {
    Class<?>[] interfaces = c.getInterfaces();
    if (interfaces != null) {
      for (int i = 0; i < interfaces.length; i++) {
        if (interfaces[i].getName().equals("totalcross.MainClass")) {
          return true;
        }
      }
    }
    return false;
  }

  private static void runtimeInstructions() {
    System.out.println("Current path: " + System.getProperty("user.dir"));
    System.out.println("TotalCross " + Settings.versionStr + "." + Settings.buildNumber);
    System.out.println("===================================");
    System.out.println("Device key emulations:");
    System.out.println("F2 : TAKE SCREEN SHOT AND SAVE TO CURRENT FOLDER");
    System.out.println("F6 : MENU");
    System.out.println("F7 : BACK (ESCAPE)");
    System.out.println("F9 : CHANGE ORIENTATION");
    System.out.println("F11: OPEN KEYBOARD");
    System.out.println("===================================");
  }

  public static void main(String[] args) {
    if (args == null || args.length == 0 || "/help".equalsIgnoreCase(args[0])) {
      if (args == null || args.length == 0) {
        ApplicationLoader.showInstructions();
      }
      if (java.awt.GraphicsEnvironment.isHeadless()) {
        return;
      }
      args = new String[] { "/scr", "480x620x32", "/fontsize", "16", "tc.Help" };
    }
    isApplication = true;
    startApplication(args[args.length - 1], java.util.Arrays.copyOf(args, args.length - 1));
  }
}
