// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import tc.tools.JarClassPathLoader;
import tc.tools.deployer.DeploySettings;
import totalcross.io.IOException;
import totalcross.io.RandomAccessStream;
import totalcross.io.Stream;
import tc.simulator.awt.AppletPreviewSurface;
import tc.simulator.awt.AwtRenderSurface;
import tc.simulator.awt.AwtWindow;
import tc.preview.PreviewFrameSink;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.ui.Control;
import totalcross.ui.Container;
import totalcross.ui.MainWindow;
import totalcross.ui.Window;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.PenEvent;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.zip.TCZ;

@SuppressWarnings({"deprecation", "removal"})
abstract class SimulatorSupport extends FontRegistry {
  public static Launcher instance;
  public static boolean isApplication;
  public static boolean terminateIfMainClass = true;
  public String commandLine = "";
  public int threadCount;
  public Hashtable htOpenedAt = new Hashtable(31); // guich@200b4_82
  public IntHashtable keysPressed = new IntHashtable(129);
  public MainWindow mainWindow;
  public boolean showKeyCodes;
  public Hashtable htAttachedFiles = new Hashtable(5); // guich@566_28
  public static int userFontSize = -1;

  protected int toBpp = 24;
  protected int toWidth = -1;
  protected int toHeight = -1;

  protected String className;
  protected boolean appletInitialized; // guich@500_1
  protected AwtWindow windowBackend;
  protected int toUI = -1; // guich@573_6: since now we have 4 styles, select the target one directly.
  protected double toScale = -1;
  protected RuntimeState.WinTimer winTimer;
  protected boolean started; // guich@120
  protected boolean destroyed; // guich@230_24
  protected boolean settingsFilled;
  protected int[] screenPixels = new int[0];
  protected int lookupR[], lookupG[], lookupB[], lookupGray[];
  protected int pal685[];
  protected Class<?> _class; // used by the openInputStream method.
  protected BufferedImage screenImg;
  protected AlertBox alert;
  protected String frameTitle;
  protected String crid4settings; // prevent from having two different crids for loading and storing the settings.
  protected StringBuffer mmsb = new StringBuffer(32);
  EventLoop eventLoop;
  protected static final long PREVIEW_DESTROY_TIMEOUT_MILLIS = 10000;
  protected boolean isDemo;
  protected boolean fastScale;
  protected boolean previewMode;
  protected AwtRenderSurface previewSurface;
  protected ClassLoader appClassLoader;
  protected tc.simulator.Launcher runtime;
  protected String appletArguments;
  protected URL appletCodeBase;
  protected final Map<String, String> appletParameters = new HashMap<String, String>();

  protected double toScaleValue = -1;
  protected double toDensityValue = 1;
  public totalcross.ui.Insets toInsetsPortrait;
  public totalcross.ui.Insets toInsetsLandscape;
  protected SimulatorSupport() {
    instance = (Launcher) this;
  }

  protected void initializeLauncher() {
    if (!previewMode) {
      addKeyListener(this);
      addMouseListener(this);
      addMouseWheelListener(this);
      addMouseMotionListener(this);
    }
    try {
      File libsFile = new File(DeploySettings.distDir, "libs");
      JarClassPathLoader.addJar(libsFile, "jna");
      JarClassPathLoader.addJar(libsFile, "jna-platform");
      JarClassPathLoader.addJar(libsFile, "slf4j-api");
      JarClassPathLoader.addJar(libsFile, "appdirs");
      JarClassPathLoader.addJar(libsFile, "thumbnailator");
    } catch (java.io.IOException e) {
      e.printStackTrace();
    }
  }

  protected boolean ignoreNextResize;
  protected void parseArguments(String... args) { }
  protected void storeSettings() { }
  static void showInstructions() { }
  protected int getScreenColor(int pixel) { return pixel; }
  protected String[] tokenizeString(String value, char separator) { return value.split(java.util.regex.Pattern.quote(String.valueOf(separator))); }
  protected PreviewFrameSink getPreviewFrameSink() { return null; }
  public abstract void exit(int exitCode);

}
