// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

import net.coobird.thumbnailator.Thumbnails;
import sun.awt.image.ToolkitImage;
import tc.tools.JarClassPathLoader;
import tc.tools.RegisterSDK;
import tc.tools.deployer.DeploySettings;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.Label;
import totalcross.ui.MainWindow;
import totalcross.ui.UIColors;
import totalcross.ui.Window;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.MultiTouchEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PressListener;
import totalcross.util.Hashtable;
import totalcross.util.IntHashtable;
import totalcross.util.zip.TCZ;

/*
 * Note: Everything that calls TotalCross code in these classes must be
 * synchronized with respect to the Applet uiLock object to allow TotalCross
 * programs to be single threaded. This is because of the multi-threaded
 * nature of Java and because timers use multiple threads.
 *
 * Because all calls into TotalCross are synchronized and users can't call this code,
 * they can't deadlock the program in any way. If we moved the synchronization
 * into TotalCross code, we would have the possibility of deadlock.
 */

/** Represents the applet or application used as a Java Container to make possible run TotalCross at the desktop. */

final public class Launcher extends java.applet.Applet implements WindowListener, KeyListener,
    java.awt.event.MouseListener, MouseWheelListener, MouseMotionListener, ComponentListener {
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

  private int toBpp = 24;
  private int toWidth = -1;
  private int toHeight = -1;
  private String className;
  private boolean appletInitialized; // guich@500_1
  private LauncherFrame frame;
  private int toUI = -1; // guich@573_6: since now we have 4 styles, select the target one directly.
  private double toScale = -1;
  private int toX = -1, toY = -1;
  private WinTimer winTimer;
  private boolean started; // guich@120
  private boolean destroyed; // guich@230_24
  private boolean settingsFilled;
  private int[] screenPixels = new int[0];
  private int lookupR[], lookupG[], lookupB[], lookupGray[];
  private int pal685[];
  private Class<?> _class; // used by the openInputStream method.
  protected MemoryImageSource screenMis;
  protected java.awt.Image screenImg;
  private AlertBox alert;
  private String frameTitle;
  private String crid4settings; // prevent from having two different crids for loading and storing the settings.
  private StringBuffer mmsb = new StringBuffer(32);
  private TCEventThread eventThread;
  private boolean isMainClass;
  private boolean isDemo;
  private String activationKey;
  private boolean fastScale;
  
  private double toScaleValue = -1;
  private double toDensityValue = 1;

  @SuppressWarnings("deprecation")
  public Launcher() {
    instance = this;
    addKeyListener(this);
    addMouseListener(this);
    addMouseWheelListener(this);
    addMouseMotionListener(this);
    try {
      Runtime.runFinalizersOnExit(true);
    } catch (Throwable t) {
    }
    //try {System.runFinalizersOnExit(true);} catch (Throwable t) {} // guich@300_31
    try {
      JarClassPathLoader.addFile(DeploySettings.etcDir + "libs/jna-4.2.2.jar");
      JarClassPathLoader.addFile(DeploySettings.etcDir + "libs/jna-platform-4.2.2.jar");
      JarClassPathLoader.addFile(DeploySettings.etcDir + "libs/slf4j-api-1.7.21.jar");
      JarClassPathLoader.addFile(DeploySettings.etcDir + "libs/appdirs-1.0.0.jar");
    } catch (java.io.IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void destroy() {
    if (mainWindow == null || destroyed) {
      return;
    }
    destroyed = true;
    eventThread.invokeInEventThread(true, new Runnable() {
      @Override
      public void run() {
        mainWindow.appEnding();
        System.runFinalization();
        storeSettings();
      }
    });
    winTimer.stopGracefully(); // timer must be running when appEnding is called
  }

  private void runtimeInstructions() {
    System.out.println("Current path: " + System.getProperty("user.dir"));
    System.out.println("TotalCross " + Settings.versionStr + "." + Settings.buildNumber);
    // print instructions
    System.out.println("===================================");
    System.out.println("Device key emulations:");
    System.out.println("F2 : TAKE SCREEN SHOT AND SAVE TO CURRENT FOLDER");
    System.out.println("F6 : MENU");
    System.out.println("F7 : BACK (ESCAPE)");
    System.out.println("F9 : CHANGE ORIENTATION");
    System.out.println("F11: OPEN KEYBOARD");
    System.out.println("===================================");
  }

  @Override
  @SuppressWarnings("static-access")
  final public void init() {
    boolean showInstructionsOnError = true;
    appletInitialized = true; // guich@500_1
    totalcross.sys.Settings.showDesktopMessages = true; // guich@500_1: redo the messages.
    try {
      alert = new AlertBox();
      // NOTE: getParameter() and size() don't work in a
      // java applet constructor, so we need to call them here
      if (!isApplication) {
        String arguments = getParameter("arguments");
        if (arguments == null) {
          throw new Exception(
              "Error: you must suply an 'arguments' property with all the argments to create the application");
        }
        String[] args = tokenizeString(arguments, ' ');
        parseArguments(args);
      }

      fillSettings();
      if (isApplication && !className.equals("tc.Help") && activationKey != null) {
        Class.forName("tc.tools.RegisterSDK").getConstructor(String.class).newInstance(activationKey);
      }

      try {
        _class = getClass(); // guich@500_1: we can use ourselves
        // if the user pass: tc/samples/ui/image/test/ImageTest.class, change to tc.samples.ui.image.test.ImageTest
        if (className.endsWith(".class")) {
          className = className.substring(0, className.length() - 6);
        }
        className = className.replace('/', '.');

        Class<?> c = Class.forName(className); // guich@200b2: applets dont let you specify the path. it must be set in the codebase param - guich@520_9: changed from Class. to getClass
        showInstructionsOnError = false;
        isMainClass = checkIfMainClass(c); // guich@tc122_4
        if (!isMainClass) {
          runtimeInstructions();
        }
        Object o = c.newInstance();
        if (o instanceof MainClass && !(o instanceof MainWindow)) {
          ((MainClass) o).appStarting(0);
          ((MainClass) o).appEnding();
          if (terminateIfMainClass) {
            System.exit(0); // currently we just exit after the constructor is called in a Non-GUI (headless) application
          } else {
            return;
          }
        }
        mainWindow = (MainWindow) o;
        // NOTE: java will call a partially constructed object if show() is called before all the objects are constructed
        if (isApplication) {
          frame = new LauncherFrame();
          if (activationKey == null) {
            new Activate().popup();
          }
          requestFocus();
        } else {
          setLayout(new java.awt.BorderLayout());
        }
        if (toUI != -1) {
          mainWindow.setUIStyle((byte) toUI);
        }
      } catch (LinkageError le) {
        System.out.println("Fatal Error when running applet: there is an error in the constructor of the class "
            + className + " and it could not be instantiated. Stack trace: ");
        le.printStackTrace();
        exit(0);
      } catch (ClassCastException cce) {
        System.out.println("Error: class " + className + " does not extend MainClass nor MainWindow!");
        cce.printStackTrace();
        exit(-1);
      } catch (ClassNotFoundException cnfe) {
        System.out.println("The MainWindow class specified was not found: " + className + "\n\nCommon causes are:");
        System.out
            .println(". The name is misspelled: java is case sensitive, so UIGadgets is not the same of uigadgets");
        if (className.indexOf('.') < 0) {
          System.out.println(
              ". The package name is incorrect: if you declared a class like: \n     package com.foo.bar;\n     public class "
                  + className + "\n  then you must specify com.foo.bar." + className
                  + " as the main class; only specifying " + className + " is not enough.");
        }
        System.out.println(
            ". Its location was not added to the classpath: if you're running from the prompt, be sure to add the path where your application is to the CLASSPATH argument. For example, if the class is in the current path, add a . specifying the current path: java -classpath .;tc.jar totalcross.Launcher "
                + className);
        exit(-1);
      }
    } catch (Exception ee) {
      String name = ee.getClass().getSimpleName();
      if (name.equals("RegisterSDKException")) {
        System.out.println("SDK registration returned: " + ee.getMessage());
        exit(-2);
      }
      if (showInstructionsOnError) {
        showInstructions();
      }
      ee.printStackTrace();
    } // guich@120
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

  private class LauncherFrame extends Frame {
    private Insets insets;

    public LauncherFrame() {
      setBackground(new java.awt.Color(getScreenColor(mainWindow.getBackColor())));
      setResizable(Settings.resizableWindow); // guich@570_54
      setLayout(null);
      add(instance);
      addNotify(); // without this, the insets will not be correctly set.
      insets = getInsets();
      if (insets == null) {
        insets = new Insets(0, 0, 0, 0);
      }
      setFrameSize(toWidth, toHeight, true);
      setLocation(toX, toY);
      super.setTitle(frameTitle != null ? frameTitle : mainWindow.getClass().getName());
      setVisible(true);
      addWindowListener(instance);
      addComponentListener(instance);
    }

    @Override
    public void update(java.awt.Graphics g) {
    }

    public void setFrameSize(int toWidth, int toHeight, boolean set) {
      if (set) {
        setSize((int) (toWidth * toScale) + insets.left + insets.right,
            (int) (toHeight * toScale) + insets.top + insets.bottom);
      }
      instance.setBounds(insets.left, insets.top, (int) (toWidth * toScale), (int) (toHeight * toScale));
    }
  };

  private class WinTimer extends java.lang.Thread {
    private int interval;
    private boolean shouldStop;

    @Override
    public void run() {
      // NOTE: because we have created an official event queue/thread, which now
      // resembles the device event queue much more closely, we must be
      // sure that all timers and TC threads are run in that event thread.  This
      // will ensure that such things as blinking cursors will continue to work
      // if there is a blocking modal dialog open.  This also means that TC JDK
      // threads will act much more like the device threads... in that, threads
      // will not run unless a message pump is running.
      while (!shouldStop) {
        boolean doTick = true;
        int millis = interval;
        if (millis <= 0) {
          // NOTE: Netscape navigator doesn't support interrupt()
          // so we sleep here less than we would normally need to
          // (1 second) if we're not doing anything to check if
          // the timer should start in case interrupt didn't work
          millis = 1 * 1000;
          doTick = false;
        }
        // guich@200b4_84: implement the simple thread
        long first = System.currentTimeMillis();
        while ((System.currentTimeMillis() - first) < millis) {
          try {
            sleep(millis);
            doTick = true; // guich@230_3
            break; // guich@230_3
          } catch (InterruptedException e) {
            doTick = false;
            break; // guich@230_4
          }
        }
        if (doTick && eventThread != null) {
          eventThread.invokeInEventThread(false, new Runnable() {
            @Override
            public void run() {
              synchronized (instance) // guich@510_2: synchronize the repaint with the timer
              {
                mainWindow._onTimerTick(true);
              }
            }
          });
        }
      }
    }

    void setInterval(int millis) {
      //System.out.println("setInterval "+millis);
      interval = millis < 10 ? 10 : millis; // guich@230_3
      interrupt();
    }

    void stopGracefully() {
      // NOTE: It's not a good idea to call stop() on threads since
      // it can cause the JVM to crash.
      shouldStop = true;
      interrupt();
    }
  }

  public boolean eventIsAvailable() {
    return eventThread.eventAvailable();
  }

  void startApp() {
    eventThread = new TCEventThread(mainWindow);
    if (!started) // guich@120 - make sure that the component is available for drawing when starting the application. called by paint.
    {
      try {
        eventThread.invokeInEventThread(true, new Runnable() {
          @Override
          public void run() {
            while (mainWindow == null) {
              Thread.yield();
            }
            mainWindow.appStarting(isDemo ? 80 : -1);
            if (isApplication) {
              new Thread() {
                @Override
                public void run() {
                  try {
                    new URL("http://www.superwaba.net/SDKRegistrationService/PingService?CHAVE=" + activationKey)
                        .openConnection().getInputStream().close();
                  } catch (Throwable e) {
                  }
                }
              }.start(); // keep track of TC usage
            }
          } // guich@200b4_107 - guich@570_3: check if mainWindow is not null to avoid problems when running on Linux. seems that the paint event is being generated before the start one.
        });
      } catch (Throwable e) {
        e.printStackTrace();
      }
      started = true;
    }
  }

  static void showInstructions() {
    System.out.println("Possible Arguments (in any order and case insensitive). Default is marked as *");
    System.out.println("   /scr WIDTHxHEIGHT     : sets the width and height resolution.");
    System.out.println("   /scr WIDTHxHEIGHTxBPP : sets the width, height and bits per pixel (8, 16, 24 or 32)");
    System.out.println("   /density <0.1 to 4>   : sets the screen pixel density");
    System.out.println("   /scr win32       : Windows 32          (same of /scr 240x320x24)");
    System.out.println("*  /scr android     : Android             (same of /scr 320x568x24)");
    System.out.println("   /scr iphone      : iPhone 8 resolution (same of /scr 750x1334x24 /density 2)");
    System.out.println("   /scr ipad        : iPad resolution     (same of /scr 1536x2048x24 /density 2)");
    System.out.println("   /pos x,y         : Sets the openning position of the application");
    System.out.println("   /uiStyle Flat    : Flat user interface style");
    System.out.println("*  /uiStyle Vista   : Vista user interface style");
    System.out.println("   /uiStyle Android : Android 4 user interface style");
    System.out.println("   /uiStyle Holo    : Android 5 user interface style");
    System.out.println("   /uiStyle Material: Material 6 user interface style");
    System.out.println("   /penlessDevice   : acts as a device that has no touchscreen.");
    System.out.println("   /fingerTouch     : acts as a device that uses a finger instead of a pen.");
    System.out.println("   /unmovablesip    : acts as a device whose SIP is unmovable (like in Android and iPhone).");
    System.out.println("   /geofocus        : enables geographical focus.");
    System.out.println("   /virtualKeyboard : shows the virtual keyboard when in an Edit or a MultiEdit");
    System.out.println("   /showmousepos    : shows the mouse position.");
    System.out.println("   /bpp 8           : emulates 8  bits per pixel screens (256 colors)");
    System.out.println("   /bpp 16          : emulates 16 bits per pixel screens (64K colors)");
    System.out.println("   /bpp 24          : emulates 24 bits per pixel screens (16M colors)");
    System.out.println("   /bpp 32          : emulates 32 bits per pixel screens (16M colors without transparency)");
    System.out.println("   /scale <0.1 to 8>: scales the screen, using by default a method that gives higher priority to image smoothness than scaling speed.");
    System.out.println("   /fastscale       : combined with scale, changes its default scaling method for one that gives higher priority to scaling speed than smoothness of the scaled image.");
    System.out.println("   /dataPath <path> : sets where the PDB and media files are stored");
    System.out.println("   /cmdLine <...>   : the rest of arguments-1 are passed as the command line");
    System.out.println("   /fontSize <size> : set the default font size to the one passed as parameter");
    System.out.println(
        "   /r <key>         : specify a registration key to be used to activate TotalCross when required. You may use %key%, where key is an environment variable");
    System.out.println("The class name that extends MainWindow must always be the last argument");
    System.out.println("Please notice that the Launcher automatically scales down the resolution to fit in the display, to disable this behavior you may include the argument scale with the value 1");
  }

  public static void main(String args[]) {
    if (args.length == 0 || args[0].equals("/help")) {
      if (args.length == 0) {
        showInstructions();
      }
      args = new String[] { "/scr", "480x620x32", "/fontsize", "16", "tc.Help" };
    }
    isApplication = true;
    Launcher app = new Launcher();
    app.parseArguments(args);
    app.init();
  }

  private int toInt(String s) // Convert.toInt can't be used here, otherwise, the settings will be set too early!
  {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return 0;
    }
  }

  private double toDouble(String s) {
    try {
      return Double.parseDouble(s);
    } catch (Exception e) {
      return 0;
    }
  }

  protected void parseArguments(String... args) {
    parseArguments(args[args.length - 1], Arrays.copyOf(args, args.length - 1));
  }

  protected void parseArguments(String clazz, String... args) {
    int n = args.length, i = 0;
    String newDataPath = null;
    try {
      className = clazz;
      for (i = 0; i < n; i++) {
        if (args[i].equalsIgnoreCase("/fontsize")) {
          userFontSize = toInt(args[++i]);
        } else if (args[i].equalsIgnoreCase("/dataPath")) {
          newDataPath = args[++i];
          System.out.println("Data path is " + newDataPath);
        } else if (args[i].equalsIgnoreCase("/scr")) /* /scr 320x320  or  /scr 320x320x8 */
        {
          String next = args[++i];
          if (next.equalsIgnoreCase("win32")) {
            toWidth = 240;
            toHeight = 320;
            toBpp = 24;
          } else if (next.equalsIgnoreCase("iPhone")) {
            toWidth = 750;
            toHeight = 1334;
            toBpp = 24;
            toDensityValue = 2;
          } else if (next.equalsIgnoreCase("ipad")) {
              toWidth = 1536;
              toHeight = 2048;
              toBpp = 24;
              toDensityValue = 2;
          } else if (next.equalsIgnoreCase("android")) {
            toWidth = 720;
            toHeight = 1184;
            toBpp = 24;
            toDensityValue = 2;
          } else {
            String[] scr = tokenizeString(next.toLowerCase(), 'x');
            if (scr.length == 1) {
              throw new Exception();
            }
            toWidth = toInt(scr[0]);
            toHeight = toInt(scr[1]);
            if (scr.length == 3) {
              toBpp = toInt(scr[2]);
            }
          }
          System.out.println("Screen is " + toWidth + "x" + toHeight + "x" + toBpp);
        } else if (args[i].equalsIgnoreCase("/r")) {
          activationKey = args[++i].toUpperCase();
          if (activationKey.startsWith("%")) {
            activationKey = System.getenv(activationKey.substring(1, activationKey.length() - 1));
          }
          if (activationKey == null || activationKey.length() != 24) {
            throw new RuntimeException("Invalid registration key: " + activationKey);
          }
        } else if (args[i].equalsIgnoreCase("/pos")) /* x,y */
        {
          String[] scr = tokenizeString(args[++i].toLowerCase(), ',');
          if (scr.length == 1) {
            throw new Exception();
          }
          toX = toInt(scr[0]);
          toY = toInt(scr[1]);
        } else if (args[i].equalsIgnoreCase("/cmdline")) {
          commandLine = "";
          while (++i < n) {
            commandLine += args[i] + " ";
          }
          commandLine = commandLine.trim();
          System.out.println("Command line is '" + commandLine + "'");
        } else if (args[i].equalsIgnoreCase("/uiStyle")) {
          String next = args[++i];
          if (next.equalsIgnoreCase("Flat")) {
            toUI = Settings.Flat;
          } else if (next.equalsIgnoreCase("Vista")) {
            toUI = Settings.Vista;
          } else if (next.equalsIgnoreCase("Android")) {
            toUI = Settings.Android;
          } else if (next.equalsIgnoreCase("Holo")) {
            toUI = Settings.Holo;
          } else if (next.equalsIgnoreCase("Material")) {
            toUI = Settings.Material;
          } else {
            throw new Exception();
          }
          System.out.println("UI style is " + toUI);
        } else if (args[i].equalsIgnoreCase("/penlessDevice")) // guich@573_20
        {
          Settings.keyboardFocusTraversable = true;
          System.out.println("Penless device is on");
        } else if (args[i].equalsIgnoreCase("/fingertouch")) // guich@573_20
        {
          Settings.fingerTouch = true;
          System.out.println("Finger touch is on");
        } else if (args[i].equalsIgnoreCase("/unmovablesip")) // guich@573_20
        {
          Settings.unmovableSIP = true;
          System.out.println("Unmovable SIP is on");
        } else if (args[i].equalsIgnoreCase("/geofocus")) // guich@tc114_31
        {
          Settings.geographicalFocus = Settings.keyboardFocusTraversable = true;
          System.out.println("Geographical focus is on");
        } else if (args[i].equalsIgnoreCase("/virtualKeyboard")) // bruno@tc110
        {
          Settings.virtualKeyboard = true;
          System.out.println("Virtual keyboard is on");
        } else if (args[i].equalsIgnoreCase("/bpp")) {
          toBpp = toInt(args[++i]);
          if (toBpp != 8 && toBpp != 16 && toBpp != 24 && toBpp != 32) {
            throw new Exception();
          }
          System.out.println("Bpp is " + toBpp);
        } else if (args[i].equalsIgnoreCase("/scale")) {
          final BigDecimal scaleDecimal = new BigDecimal(args[++i]);
          if (scaleDecimal.compareTo(BigDecimal.ZERO) < 0 || scaleDecimal.compareTo(BigDecimal.valueOf(8)) > 0) {
            throw new Exception();
          }
          toScaleValue = scaleDecimal.doubleValue();
          System.out.println("Scale is " + toScaleValue);
        } else if (args[i].equalsIgnoreCase("/fastscale")) {
            fastScale = true;
        } else if (args[i].equalsIgnoreCase("/showmousepos")) {
          Settings.showMousePosition = true;
        } else if (args[i].equalsIgnoreCase("/demo")) {
          isDemo = true;
        } else if (args[i].equalsIgnoreCase("/density")) {
            final BigDecimal screenDensityDecimal = new BigDecimal(args[++i]);
            if (screenDensityDecimal.compareTo(BigDecimal.ZERO) <= 0
                    || screenDensityDecimal.compareTo(BigDecimal.valueOf(4)) > 0) {
                throw new Exception();
            }
            toDensityValue = screenDensityDecimal.doubleValue();
        } else {
          throw new Exception();
        }
      }
    } catch (Exception e) {
      showInstructions();
      System.err.println("Invalid or incomplete argument at position " + i + ": " + args[i]);
      String s = "";
      for (i = 0; i < args.length; i++) {
        s += " " + args[i];
      }
      System.err.println(e.getMessage());
      System.err.println("Full command line:\n" + s.trim());
      exit(-1);
      return;
    }

    // verify the parameters
    if (toWidth == -1 || toHeight == -1) // if no width specified, use the lowest one
    {
      if (isApplication) {
        toWidth = 320;
        toHeight = 568;
      } else {
        toWidth = getSize().width;
        toHeight = getSize().height;
      }
    }
    
    Settings.screenDensity = toDensityValue;

    /*
     * Gets the display resolution and automatically scales down the Launcher to fit
     * in the display *only* if a scale value isn't provided on the command line
     */
    if (toScaleValue == -1) {
        final Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .getBounds();
        
        /**
         * Arbitrary value based on tests to avoid overlapping a docked task bar.
         */
        final double USEABLE_AREA = 0.88;
        final int viewportW = (int) (toWidth / toDensityValue);
        final int viewportH = (int) (toHeight / toDensityValue);
        final double maxRatio = Math.max((double) viewportW / r.width, (double) viewportH / r.height);
        if (maxRatio > USEABLE_AREA) {
            toScaleValue = USEABLE_AREA / maxRatio;
        }
    }
    toScale = Math.abs(toScaleValue) / toDensityValue;

    Settings.dataPath = newDataPath;
    if (isApplication && activationKey == null) {
      activationKey = RegisterSDK.getStoredActivationKey();
    }
    if (activationKey != null && activationKey.length() != 24) {
      System.err.println("The registration key has incorrect length: " + activationKey.length() + " but must have 24");
      System.exit(0);
    }
  }

  private String[] tokenizeString(String string, char c) {
    java.util.StringTokenizer st = new java.util.StringTokenizer(string, "" + c);
    String[] ret = new String[st.countTokens()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = st.nextToken();
    }
    return ret;
  }

  @Override
  public void start() {
    mainWindow = MainWindow.getMainWindow();
  }

  ///////// guich@200b2: to make the vm easier to port, i removed all methods from the TotalCross classes that uses the jdk classes /////////
  public void registerMainWindow(totalcross.ui.MainWindow main) {
    (winTimer = new WinTimer()).start(); // guich@510_2: start the timer only after we had added the others
  }

  public void setTimerInterval(int milliseconds) {
    winTimer.setInterval(milliseconds);
  }

  public void exit(int exitCode) {
    destroy(); // guich@230_24
    if (isApplication) {
      System.exit(exitCode);
    }
  }

  public void minimize() {
    if (frame != null) {
      frame.setExtendedState(Frame.ICONIFIED);
    }
  }

  public void restore() {
    if (frame != null) {
      frame.setExtendedState(Frame.NORMAL);
    }
  }

  @Override
  public void print(java.awt.Graphics g) {
  }

  @Override
  public boolean isFocusTraversable() // guich@512_1: inform that we want to handle tab
  {
    return true;
  }

  private int modifiers;

  private void updateModifiers(java.awt.event.KeyEvent event) {
    if (event.isShiftDown()) {
      keysPressed.put(SpecialKeys.SHIFT, 1);
      modifiers |= SpecialKeys.SHIFT;
    } else {
      keysPressed.put(SpecialKeys.SHIFT, 0);
      modifiers &= ~SpecialKeys.SHIFT;
    }
    if (event.isControlDown()) {
      keysPressed.put(SpecialKeys.CONTROL, 1);
      modifiers |= SpecialKeys.CONTROL;
    } else {
      keysPressed.put(SpecialKeys.CONTROL, 0);
      modifiers &= ~SpecialKeys.CONTROL;
    }
    if (event.isAltDown()) {
      keysPressed.put(SpecialKeys.ALT, 1);
      modifiers |= SpecialKeys.ALT;
    } else {
      keysPressed.put(SpecialKeys.ALT, 0);
      modifiers &= ~SpecialKeys.ALT;
    }
  }

  @Override
  public void keyPressed(final java.awt.event.KeyEvent event) {
    if (event.getKeyChar() == '1' && event.isControlDown()) {
      totalcross.ui.Window.onRobotKey();
    }
    updateModifiers(event);
    if (event.isActionKey()) {
      updateModifiers(event);
      int key = 0;

      switch (event.getKeyCode()) {
      case java.awt.event.KeyEvent.VK_HOME:
        key = SpecialKeys.HOME;
        break;
      case java.awt.event.KeyEvent.VK_END:
        key = SpecialKeys.END;
        break;
      case java.awt.event.KeyEvent.VK_UP:
        key = SpecialKeys.UP;
        break;
      case java.awt.event.KeyEvent.VK_DOWN:
        key = SpecialKeys.DOWN;
        break;
      case java.awt.event.KeyEvent.VK_LEFT:
        key = SpecialKeys.LEFT;
        break;
      case java.awt.event.KeyEvent.VK_RIGHT:
        key = SpecialKeys.RIGHT;
        break;
      case java.awt.event.KeyEvent.VK_INSERT:
        key = SpecialKeys.INSERT;
        break;
      case java.awt.event.KeyEvent.VK_ENTER:
        key = SpecialKeys.ENTER;
        break;
      case java.awt.event.KeyEvent.VK_TAB:
        key = SpecialKeys.TAB;
        break;
      case java.awt.event.KeyEvent.VK_BACK_SPACE:
        key = SpecialKeys.BACKSPACE;
        break;
      case java.awt.event.KeyEvent.VK_ESCAPE:
        key = SpecialKeys.ESCAPE;
        break;
      case java.awt.event.KeyEvent.VK_DELETE:
        key = SpecialKeys.DELETE;
        break;
      case java.awt.event.KeyEvent.VK_PAGE_UP:
        key = SpecialKeys.PAGE_UP;
        keysPressed.put(key, 1);
        keysPressed.put(java.awt.event.KeyEvent.VK_PAGE_DOWN, 0);
        break; // don't let down/up simultanealy
      case java.awt.event.KeyEvent.VK_PAGE_DOWN:
        key = SpecialKeys.PAGE_DOWN;
        keysPressed.put(key, 1);
        keysPressed.put(java.awt.event.KeyEvent.VK_PAGE_UP, 0);
        break;
      // guich@120 - emulate more keys
      case java.awt.event.KeyEvent.VK_F1:
        break;
      case java.awt.event.KeyEvent.VK_F2:
        takeScreenShot();
        break;
      case java.awt.event.KeyEvent.VK_F3:
        break;
      case java.awt.event.KeyEvent.VK_F4:
        break;
      case java.awt.event.KeyEvent.VK_F5:
        break;
      case java.awt.event.KeyEvent.VK_F6:
        key = SpecialKeys.MENU;
        break;
      case java.awt.event.KeyEvent.VK_F7:
        key = SpecialKeys.ESCAPE;
        break;
      case java.awt.event.KeyEvent.VK_F8:
        break;
      case java.awt.event.KeyEvent.VK_F10:
        break;
      case java.awt.event.KeyEvent.VK_F11:
        key = SpecialKeys.KEYBOARD_ABC;
        break;
      case java.awt.event.KeyEvent.VK_F12:
        break;
      case java.awt.event.KeyEvent.VK_F9:
        if (isApplication && !Settings.disableScreenRotation && Settings.screenWidth != Settings.screenHeight
            && eventThread != null) // guich@tc: changed orientation?
        {
          int t = toWidth;
          toWidth = toHeight;
          toHeight = t;
          screenResized(Settings.screenHeight, Settings.screenWidth, true);
          key = 0;
          ignoreNextResize = true;
        }
        break;
      default:
        key = 0;
        break;
      }
      if (key != 0 && eventThread != null) // sometimes, when debugging in applet, eventThread can be null
      {
        eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, key, 0, 0, modifiers, Vm.getTimeStamp());
      }
      if (showKeyCodes && eventThread != null) {
        final String msg = "Key code: " + (key == 0 ? event.getKeyCode() : key) + ", Modifier: " + modifiers;
        new Thread() {
          @Override
          public void run() {
            Vm.alert(msg);
          }
        }.start(); // must place this in a separate thread, or the vm dies
      }
    }
  }

  private void takeScreenShot() {
    try {
      totalcross.ui.image.Image img = MainWindow.getScreenShot();
      String name = totalcross.sys.Settings.appPath + new Time().getTimeLong() + ".png";
      totalcross.io.File f = new totalcross.io.File(name, totalcross.io.File.CREATE_EMPTY);
      img.createPng(f);
      f.close();
      System.out.println("Saved at " + name);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void screenResized(int w, int h, boolean setframe) {
    if (screenMis == null || (Settings.screenWidth == w && Settings.screenHeight == h)) {
      return;
    }
    Settings.screenWidth = w;
    Settings.screenHeight = h;
    frame.setFrameSize(w, h, setframe);
    screenMis = null; // force the creation of a new screen image
    eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, SpecialKeys.SCREEN_CHANGE, 0, 0, modifiers, Vm.getTimeStamp());
  }

  @Override
  public void transferFocus() // guich@512_1: handle the tab key.
  {
    super.transferFocus();
    if (eventThread != null) {
      eventThread.pushEvent(KeyEvent.SPECIAL_KEY_PRESS, SpecialKeys.TAB, 0, 0, modifiers, Vm.getTimeStamp());
    }
  }

  @Override
  public void keyReleased(java.awt.event.KeyEvent event) {
    updateModifiers(event);
    if (event.isActionKey()) {
      switch (event.getKeyCode()) {
      //            case java.awt.event.KeyEvent.VK_F1:        keysPressed.put(SpecialKeys.HARD1,0); break;
      //            case java.awt.event.KeyEvent.VK_F2:        keysPressed.put(SpecialKeys.HARD2,0); break;
      //            case java.awt.event.KeyEvent.VK_F3:        keysPressed.put(SpecialKeys.HARD3,0); break;
      //            case java.awt.event.KeyEvent.VK_F4:        keysPressed.put(SpecialKeys.HARD4,0); break;
      case java.awt.event.KeyEvent.VK_PAGE_UP:
        keysPressed.put(SpecialKeys.PAGE_UP, 0);
        break;
      case java.awt.event.KeyEvent.VK_PAGE_DOWN:
        keysPressed.put(SpecialKeys.PAGE_DOWN, 0);
        break;
      }
    }
  }

  @Override
  public void keyTyped(java.awt.event.KeyEvent event) {
    updateModifiers(event);
    if (!event.isActionKey() && eventThread != null) {
      int key = event.getKeyChar(), orig = key;
      switch (key) {
      case 8:
        key = SpecialKeys.BACKSPACE;
        break;
      case 10:
        key = SpecialKeys.ENTER;
        break;
      case 127:
        key = SpecialKeys.DELETE;
        break;
      case 27:
        key = SpecialKeys.ESCAPE;
        break; // guich@tc110_79
      }
      eventThread.pushEvent(orig < 32 ? KeyEvent.SPECIAL_KEY_PRESS : KeyEvent.KEY_PRESS, key, 0, 0, modifiers,
          Vm.getTimeStamp());
    }
  }

  boolean isRightButton;
  int startPY;

  @Override
  public void mousePressed(java.awt.event.MouseEvent event) {
    int px = (int) (event.getX() / toScale);
    int py = (int) (event.getY() / toScale);
    if (eventThread != null) {
      eventThread.pushEvent(PenEvent.PEN_DOWN, 0, px, py, modifiers, Vm.getTimeStamp());
    }
    if (isRightButton = (event.getButton() & 2) != 0) {
      eventThread.pushEvent(MultiTouchEvent.SCALE, 1, px, startPY = py, modifiers, Vm.getTimeStamp());
    }
  }

  @Override
  public void mouseReleased(java.awt.event.MouseEvent event) {
    int px = (int) (event.getX() / toScale);
    int py = (int) (event.getY() / toScale);
    if (eventThread != null) {
      eventThread.pushEvent(PenEvent.PEN_UP, 0, px, py, modifiers, Vm.getTimeStamp());
    }
    if ((event.getButton() & 2) != 0) {
      eventThread.pushEvent(MultiTouchEvent.SCALE, 2, px, py, modifiers, Vm.getTimeStamp());
    }
  }

  @Override
  public void mouseDragged(java.awt.event.MouseEvent event) {
    int px = (int) (event.getX() / toScale);
    int py = (int) (event.getY() / toScale);
    if (eventThread != null) // sometimes, when debugging in applet, eventThread can be null
    {
      if ((event.getButton() & 2) != 0 || isRightButton) {
        double scale = py < startPY ? 1.05 : 0.95;
        long l = Double.doubleToLongBits(scale);
        int x = (int) (l >>> 32);
        int y = (int) l;
        if (!eventThread.hasEvent(MultiTouchEvent.SCALE)) {
          eventThread.pushEvent(MultiTouchEvent.SCALE, 0, x, y, modifiers, Vm.getTimeStamp());
        }
      } else if (!eventThread.hasEvent(PenEvent.PEN_DRAG)) {
        eventThread.pushEvent(PenEvent.PEN_DRAG, 0, px, py, modifiers, Vm.getTimeStamp()); // guich@580_40: changed from 201 to 203; PenEvent.PEN_MOVE is deprecated
      }
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (eventThread != null) // sometimes, when debugging in applet, eventThread can be null
    {
      int ev = totalcross.ui.event.MouseEvent.MOUSE_WHEEL;
      if (!eventThread.hasEvent(ev)) {
        int px = (int) (e.getX() / toScale);
        int py = (int) (e.getY() / toScale);
        eventThread.pushEvent(ev,
            e.getWheelRotation() < 0 ? totalcross.ui.event.DragEvent.UP : totalcross.ui.event.DragEvent.DOWN, px, py,
            modifiers, Vm.getTimeStamp()); // guich@580_40: changed from 201 to 203; PenEvent.PEN_MOVE is deprecated
      }
    }
  }

  @Override
  public void windowClosing(java.awt.event.WindowEvent event) {
    if (Settings.closeButtonType == Settings.NO_BUTTON) {
      eventThread.pushEvent(totalcross.ui.event.KeyEvent.SPECIAL_KEY_PRESS, SpecialKeys.MENU, 0, 0, 0,
          Vm.getTimeStamp());
    } else {
      destroy();
      exit(0);
    }
  }

  @Override
  public void mouseEntered(java.awt.event.MouseEvent event) {
    if (frame != null && frame.getFocusOwner() != this && !destroyed) {
      requestFocus(); // guich@200b4: correct a bug that sometimes key events was not being sent anymore to the canvas.
    }
  }

  @Override
  public void mouseClicked(java.awt.event.MouseEvent event) {
  }

  @Override
  public void mouseExited(java.awt.event.MouseEvent event) {
  }

  @Override
  public void windowActivated(java.awt.event.WindowEvent event) {
  }

  @Override
  public void windowClosed(java.awt.event.WindowEvent event) {
  }

  @Override
  public void windowDeactivated(java.awt.event.WindowEvent event) {
  }

  @Override
  public void windowDeiconified(java.awt.event.WindowEvent event) {
    if (mainWindow != null) {
      mainWindow.onRestore();
    }
  }

  @Override
  public void windowIconified(java.awt.event.WindowEvent event) {
    if (mainWindow != null) {
      mainWindow.onMinimize();
    }
  }

  @Override
  public void windowOpened(java.awt.event.WindowEvent event) {
  }

  @Override
  public void mouseMoved(java.awt.event.MouseEvent event) {
    if (eventThread != null) {
      eventThread.pushEvent(totalcross.ui.event.MouseEvent.MOUSE_MOVE, 0, (int) (event.getX() / toScale),
          (int) (event.getY() / toScale), modifiers, Vm.getTimeStamp());
    }
    if (frame != null && Settings.showMousePosition) // guich@tc115_48
    {
      mmsb.setLength(0);
      if (frameTitle != null) {
        mmsb.append(frameTitle).append(" (");
      }
      int xx = (int) (event.getX() / toScale);
      int yy = (int) (event.getY() / toScale);
      int[] pixels = totalcross.ui.gfx.Graphics.mainWindowPixels;
      mmsb.append(xx).append(",").append(yy).append(" ")
          .append(totalcross.sys.Convert.unsigned2hex(pixels[yy * Settings.screenWidth + xx], 6));
      if (frameTitle != null) {
        mmsb.append(")");
      }
      frame.setTitle(mmsb.toString());
    }
  }

  @Override
  public void paint(java.awt.Graphics g) {
    if (!started) {
      startApp();
    } else {
      eventThread.invokeInEventThread(false, new Runnable() {
        @Override
        public void run() {
          try {
            totalcross.ui.Window.repaintActiveWindows();
          } catch (Exception e) {
            System.out.println("Exception in Launcher.paint");
            e.printStackTrace();
          }
        }
      });
    }
  }

  public void pumpEvents() {
    if (eventThread != null) {
      eventThread.pumpEvents();
    }
  }

  @Override
  public void update(java.awt.Graphics g) {
  }

  public void setNewMainWindow(MainWindow newInstance, String args) // called on Vm.exec
  {
    commandLine = args; // guich@200b3: added command line support for desktop classes.
    winTimer.stopGracefully(); // guich@120
    Window.destroyZStack();
    mainWindow = newInstance;
    mainWindow.initUI(); // ps: since we are being called from an app, we cannot use the synchronized method
  }

  /** Calls System.out.println. TotalCross system debugging uses this method. See also debug(String s). */
  public static void print(String s) {
    if (totalcross.sys.Settings.showDesktopMessages) {
      System.err.println(s);
    }
  }
  //// Graphics ////////////////////////////////////////////////////////////////////

  private void createColorPaletteLookupTables() {
    int i, r, g, b;
    lookupR = new int[256];
    lookupG = new int[256];
    lookupB = new int[256];
    lookupGray = new int[256];

    for (i = 0; i < 256; i++) {
      r = (i + 1) * 6 / 256;
      if (r > 0) {
        r--;
      }
      g = (i + 1) * 8 / 256;
      if (g > 0) {
        g--;
      }
      b = (i + 1) * 5 / 256;
      if (b > 0) {
        b--;
      }
      lookupR[i] = r * 40;
      lookupG[i] = g * 5;
      lookupB[i] = b + 16;
      lookupGray[i] = i / 0x11;
    }
    pal685 = totalcross.ui.gfx.Graphics.getPalette();
  }

  private int getScreenColor(int p) {
    int r = (p >> 16) & 0xFF;
    int g = (p >> 8) & 0xFF;
    int b = p & 0xFF;
    switch (toBpp) {
    case 8:
      if (lookupR == null) {
        createColorPaletteLookupTables();
      }
      return pal685[(g == r && g == b) ? lookupGray[r] : (lookupR[r] + lookupG[g] + lookupB[b])];
    case 16:
      return (((r) >> 3) << 19) | (((g) >> 2) << 10) | (((b >> 3) << 3));
    default:
      return p;
    }
  }

  public void updateScreen() {
    //int ini = totalcross.sys.Vm.getTimeStamp();
    int[] pixels = totalcross.ui.gfx.Graphics.mainWindowPixels;
    int n = Settings.screenWidth * Settings.screenHeight;
    if (toBpp >= 24) {
      screenPixels = pixels;
    } else if (screenPixels.length < n) {
      screenPixels = new int[n];
    }
    // convert to the target bpp on-the-fly
    switch (toBpp) {
    case 8: {
      if (lookupR == null) {
        createColorPaletteLookupTables();
      }
      int[] pal = pal685;
      int[] toR = lookupR;
      int[] toG = lookupG;
      int[] toB = lookupB;
      int[] toGray = lookupGray;
      while (--n >= 0) {
        int p = pixels[n];
        int r = (p >> 16) & 0xFF;
        int g = (p >> 8) & 0xFF;
        int b = p & 0xFF;
        screenPixels[n] = pal[(g == r && g == b) ? toGray[r] : (toR[r] + toG[g] + toB[b])];
      }
      break;
    }
    case 16: {
      while (--n >= 0) {
        screenPixels[n] = pixels[n] & 0xF8FCF8; // guich@tc100b4_2: use a direct and instead of a bunch of shifts. note: using a DirectColorModel(32,0xF80000,0x00FC00,0x0000F8,0) is 5x SLOWER than doing the mapping by ourselves.
      }
      break;
    }
    }
    int w = totalcross.sys.Settings.screenWidth;
    int h = totalcross.sys.Settings.screenHeight;
    if (screenMis == null) {
      screenMis = new MemoryImageSource(w, h, 
              GraphicsEnvironment.
              getLocalGraphicsEnvironment().getDefaultScreenDevice().
              getDefaultConfiguration().getColorModel(),
          screenPixels, 0, w);
      screenMis.setAnimated(true);
      screenMis.setFullBufferUpdates(true);
      screenImg = Toolkit.getDefaultToolkit().createImage(screenMis);
    }
    screenMis.newPixels();
    Graphics g = getGraphics();
    int ww = (int) (w * toScale);
    int hh = (int) (h * toScale);
    int shiftY = totalcross.ui.Window.shiftY;
    int shiftH = totalcross.ui.Window.shiftH;
    if ((shiftY + shiftH) > h) {
      totalcross.ui.Window.shiftY = shiftY = h - shiftH;
    }
    if (shiftY != 0) {
      g.setColor(new Color(UIColors.shiftScreenColor));
      int yy = (int) (shiftH * toScale);
      g.fillRect(0, yy, ww, hh - yy); // erase empty area
      g.setClip(0, 0, ww, yy); // limit drawing area
      g.translate(0, -(int) (shiftY * toScale));
    }
    if (toScale != 1) // guich@tc126_74 - guich@tc130 
    {
        if (fastScale) {
            g.drawImage(screenImg, 0, 0, ww, hh, 0, 0, w, h, this);
        } else {
            /*
             * Required to force ToolkitImage to create the BufferedImage object, otherwise
             * calling getBufferedImage returns null
             */
            ((ToolkitImage) screenImg).getWidth();
            
            try {
                g.drawImage(Thumbnails.of(((ToolkitImage) screenImg).getBufferedImage()).size(ww, hh).asBufferedImage(), 0, 0, this);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    } else if (g != null) {
      g.drawImage(screenImg, 0, 0, ww, hh, 0, 0, w, h, this); // this is faster than use img.getScaledInstance
    }
    if (shiftY != 0) {
      g.translate(0, (int) (shiftY * toScale));
      g.setClip(0, 0, ww, hh);
    }
    // make the emulator work like OpenGL: erase the screen to instruct the user that everything must be drawn always
    //java.util.Arrays.fill(pixels, getScreenColor(UIColors.shiftScreenColor));
  }

  //static int count;
  ///////////////////////        I/O        /////////////////////////////////////
  private File[] getClassPathDirectories() throws Exception {
    char dirSeparator = File.pathSeparatorChar;
    File[] classPath;
    String pathstr = System.getProperty("java.class.path");
    // Count the number of path separators
    int i = 0;
    int n = 0;
    int j = 0;
    while ((i = pathstr.indexOf(dirSeparator, i)) != -1) {
      n++;
      i++;
    }
    // Build the class path
    File[] path = new File[n + 1];
    int len = pathstr.length();
    for (i = n = 0; i < len; i = j + 1) {
      if ((j = pathstr.indexOf(dirSeparator, i)) == -1) {
        j = len;
      }
      if (i != j) {
        String p = pathstr.substring(i, j);
        File file = new File(p);
        if (!file.isDirectory()) {
          file = new File(getPathOf(p)); // add the parent path of the file
        }
        if (file.isDirectory()) {
          path[n++] = file;
        }
      }
    }
    // Trim class path to exact size
    classPath = new File[n];
    System.arraycopy(path, 0, classPath, 0, n);
    return classPath;
  }

  private InputStream readJavaInputStream(java.io.InputStream is) {
    if (is == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    byte[] buf = new byte[128];
    int len;
    while (true) {
      try {
        len = is.read(buf);
      } catch (java.io.IOException e) {
        break;
      }
      if (len > 0) {
        baos.write(buf, 0, len);
      } else {
        break;
      }
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }

  private String getPathOf(String pathAndFileName) {
    char[] chars = pathAndFileName.toCharArray();
    for (int i = chars.length - 1; i >= 0; i--) {
      if (chars[i] == '\\' || chars[i] == '/') {
        return new String(chars, 0, i);
      }
    }
    return ""; // no path
  }

  public String getDataPath() // guich@420_11 - this now is needed because the user may change the datapath anywhere in the program
  {
    String path = totalcross.sys.Settings.dataPath;
    if (path != null) {
      path = path.replace('\\', '/');
      if (!path.endsWith("/")) {
        path += "/";
        // don't check for folder to keep compatibility with win32 vm
        //java.io.File f = new java.io.File(newDataPath);
        //if (!f.isDirectory())
        //   System.out.println("ERROR: dataPath specified is not a directory or does not exist! "+newDataPath);
      }
    }
    return path;
  }

  private String getMainWindowPath() {
    if (MainWindow.getMainWindow() == null) {
      return null;
    }
    String main = MainWindow.getMainWindow().getClass().getName().replace('.', '/');
    return getPathOf(main) + "/";
  }

  /** used in some classes so they can correctly open files. now can open jar files. */
  public InputStream openInputStream(String path) {
    String sread = "\nopening for read " + path + "\n";
    String dataPath = getDataPath();
    InputStream stream = null;
    String mainpath = getMainWindowPath();
    try {
      try // guich@tc100: removed the nonGuiApp flag
      {
        sread += "#0 - the file given: " + path + "\n";
        stream = new FileInputStream(path); // guich@421_72
      } catch (Exception e) {
        stream = null;
      }
      if (stream == null && isApplication) {
        // search in the Settings.dataPath
        try {
          String p = isOk(dataPath) ? (dataPath + path) : path;
          sread += "#1 - dataPath: " + p + "\n";
          stream = new FileInputStream(p);
          htOpenedAt.put(path, getPathOf(p)); // guich@200b4_82 - jr: i changed getPathOf(path) to getPathOf(p)
        } catch (Exception e) {
          stream = null;
        }
        if (stream == null && mainpath != null) {
          try {
            String p = mainpath + path;
            sread += "#2 - MainWindow's path from current folder: " + p + "\n";
            stream = new FileInputStream(p);
            htOpenedAt.put(path, getPathOf(p)); // guich@200b4_82 - jr: i changed getPathOf(path) to getPathOf(p)
          } catch (Exception e) {
            stream = null;
          }
        }
        // search in the classpath
        if (stream == null) {
          sread += "#3 - classpath\n";
          File[] dirs = getClassPathDirectories();
          File f = null;
          for (int i = 0; i < dirs.length; i++) {
            try {
              f = new File(dirs[i], path);
              if (!f.isFile() && mainpath != null) {
                f = new File(dirs[i], mainpath + path); // guich@tc100: search in the path of the main window
              }
              if (f.isFile()) {
                String ff = getPathOf(f.getAbsolutePath());
                htOpenedAt.put(path, ff); // guich@200b4_82 - jr: changed dirs[i].getAbsolutePath - guich@tc112_20: using f.getAbsolutePath instead of dirs[i].getAbsolutePath
                break;
              } else {
                f = null; // guich@400_8: fixed problem when file was not found so the #3 can be tried below
              }
            } catch (Exception e) {
              f = null;
            }
          }
          if (f != null) {
            stream = new FileInputStream(f);
          }
        }
        if (stream == null && _class != null) // guich@400_6: now the resources can be read from the jar file
        {
          sread += "#4 - jar file\n";
          try {
            InputStream is = (InputStream) _class.getResourceAsStream("/" + path);
            if (is != null) {
              stream = readJavaInputStream(is);
            }
          } catch (Throwable tt) {
            if (tt.getMessage() != null) {
              System.out.println(tt.getMessage());
            }
          }
        }
        String sjar;
        if (stream == null && !path.endsWith(".class")
            && (sjar = getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).contains(".jar")) // guich@330 - let tc.Help work from inside a jar
        {
          sread += "#4b - " + sjar.substring(1) + "\n";
          try {
            URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
            ZipInputStream zIn = new ZipInputStream(url.openStream());
            String spath = "/" + path;
            for (java.util.zip.ZipEntry zEntry = zIn.getNextEntry(); zEntry != null; zEntry = zIn.getNextEntry()) {
              if (zEntry.getName().endsWith(spath)) {
                stream = readJavaInputStream(zIn);
                break;
              }
            }
            zIn.close();
          } catch (Throwable tt) {
            if (tt.getMessage() != null) {
              System.out.println(tt.getMessage());
            }
          }
        }
        if (stream == null && htAttachedFiles.size() > 0) // guich@tc100: load from attached libraries too
        {
          sread += "#5 - attached libraries\n";
          totalcross.io.ByteArrayStream bas = (totalcross.io.ByteArrayStream) htAttachedFiles.get(path.toLowerCase());
          if (bas != null) {
            stream = new ByteArrayInputStream(bas.getBuffer()); // buffer is the same size of the loaded file.
          }
        }
      } else if (stream == null) {
        URL url;
        // zero in the jar file (normal way)
        InputStream is = null;
        try {
          is = (InputStream) _class.getResourceAsStream("/" + path);
        } catch (Throwable tt) {
          if (tt.getMessage() != null) {
            System.out.println(tt.getMessage());
          }
        }
        sread += "#1 - resource: " + is + "\n"; // guich@200b4_59
        if (is != null) {
          stream = readJavaInputStream(is);
        }
        // first in the jar file
        // guich@200b4: using this in Internet makes the archive be fetched from the server at each call of this function.
        if (stream == null) {
          String archive = getParameter("archive");
          sread += "#2 - archive: " + archive + "\n";
          if (isOk(archive) && !archive.equals("null")) {
            String[] archives = tokenizeString(archive, ','); // guich@580_39: if there are more than one file, split them
            for (int i = 0; i < archives.length; i++) {
              archive = archives[i];
              if (archive.startsWith("null")) {
                archive = archive.substring(4);
              }
              URL codeBase = getCodeBase();
              url = new URL(codeBase + "/" + archive);
              try {
                ZipInputStream zIn = new ZipInputStream(url.openStream());
                java.util.zip.ZipEntry zEntry = zIn.getNextEntry();
                while (!zEntry.getName().equals(path)) {
                  zEntry = zIn.getNextEntry();
                  if (zEntry == null) {
                    throw new Exception("doh");
                  }
                }
                // guich@200b2: ok. the zIn.available() returns 1 and not the real size of the zip entry. so, here we read all into a byte stream
                stream = readJavaInputStream(zIn);
              } catch (Exception e) {
                if (!e.getMessage().equals("doh")) {
                  e.printStackTrace();/* doh didn't find it in the jar thing */
                }
              }
            }
          }
        }
        // second under the codebase
        if (stream == null) {
          try {
            URL codeBase = getCodeBase();
            String cb = codeBase.toString();
            char lastc = cb.charAt(cb.length() - 1);
            char firstc = path.charAt(0);
            if (lastc != '/' && firstc != '/') {
              cb += "/";
            }
            sread += "#3 - url: " + cb + path + "\n";
            url = new URL(cb + path);
            stream = url.openStream();
          } catch (FileNotFoundException ee) {
          } catch (Exception e) {
            e.printStackTrace();
            /* neither in the codebase */}
        }
        // third in the localhost
        if (stream == null) {
          try {
            sread += "#4- url: file://localhost/" + dataPath + path + "\n";
            url = new URL("file://localhost/" + dataPath + path); // guich@120
            stream = url.openStream();
          } catch (Exception e) {
          }
        }
        ;
        if (stream == null && htAttachedFiles.size() > 0) // guich@tc100: load from attached libraries too
        {
          sread += "#5 - attached libraries\n";
          totalcross.io.ByteArrayStream bas = (totalcross.io.ByteArrayStream) htAttachedFiles.get(path.toLowerCase());
          if (bas != null) {
            stream = new ByteArrayInputStream(bas.getBuffer()); // buffer is the same size of the loaded file.
          }
        }
      }
      if (stream == null) {
        print(sread + "file not found\n");
      }
    } catch (FileNotFoundException ee) {
      print("file not found");
    } catch (Exception e) // guich@120
    {
      if (isOk(e.getMessage())) {
        print("error in JavaBridge.openInputStream: " + e.getMessage());
      }
      return null;
    }
    return stream;
  }

  private OutputStream openOutputUrl(URL url) {
    try {
      URLConnection con = url.openConnection();
      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(false);
      return con.getOutputStream();
    } catch (Exception u) // try another way
    {
      try {
        String path = url + "";
        return new FileOutputStream(isOk(totalcross.sys.Settings.dataPath) ? (getDataPath() + path) : path);
      } catch (Exception ee) {
        return null;
      }
    }
  }

  /** used in some classes so they can correctly open files. used internally by readBytes. */
  public OutputStream openOutputStream(String path) {
    print("\nopening for write " + path);
    String dataPath = getDataPath();
    OutputStream stream = null;
    String readPath = (String) htOpenedAt.get(path); // guich@tc112_20
    try {
      try // guich@tc100: removed the nonGuiApp flag
      {
        String pp = isOk(dataPath) ? (dataPath + path)
            : isOk(readPath) ? totalcross.sys.Convert.appendPath(readPath, path) : path; // guich@tc112_20: use readPath if not null
        stream = new FileOutputStream(pp); // guich@421_11: added support for dataPath
      } catch (Exception e) {
        stream = null;
      }

      if (stream == null && isApplication) {
        // search in the place where it was read - guich@200b4_82
        if (readPath != null) {
          try {
            print("#1 - read path");
            stream = new FileOutputStream(new java.io.File(readPath, path));
            print("found in " + readPath);
          } catch (Exception e) {
            stream = null;
          }
        }
        if (stream != null) {
          return stream;
        }
        // search in the Settings.dataPath
        try {
          String p = isOk(dataPath) ? (dataPath + path) : path;
          print("#2 - Settings.dataPath");
          stream = new FileOutputStream(p);
          print("found in " + p);
        } catch (Exception e) {
          stream = null;
        }
        // search in the classpath
        if (stream == null) {
          print("#3 - classpath");
          File[] dirs = getClassPathDirectories();
          File f = null;
          for (int i = 0; i < dirs.length; i++) {
            try {
              f = new File(dirs[i], path);
              if (f.isFile()) {
                print("found in " + dirs[i]);
                break;
              }
            } catch (Exception e) {
              f = null;
            }
          }
          if (f == null) {
            print("could not find file in the classpath");
          } else {
            stream = new FileOutputStream(f);
          }
        }
      } else if (stream == null) {
        URL url;
        // first under the codebase
        if (stream == null) {
          try {
            URL codeBase = getCodeBase();
            print("#1- codeBase: " + codeBase);
            String cb = codeBase.toString();
            char lastc = cb.charAt(cb.length() - 1);
            char firstc = path.charAt(0);
            if (lastc != '/' && firstc != '/') {
              cb += "/";
            }
            url = new URL(cb + path);
            stream = openOutputUrl(url);
            print("found under codebase: " + url);
          } catch (Exception e) {
            e.printStackTrace();
            /* neither in the codebase */}
        }
        // third in the localhost
        if (stream == null) {
          try {
            print("#2- url: file://localhost/" + dataPath + path);
            url = new URL("file://localhost/" + dataPath + path); // guich@120
            stream = openOutputUrl(url);
            print("found under localhost: " + url);
          } catch (Exception e) {
          }
        }
        ;
      }
      if (stream == null) {
        print("file not found");
      }
    } catch (FileNotFoundException ee) {
      print("file not found");
    } catch (Exception e) {
      /*if (!msgShowed) */print("error in Vm.openOutputStream: " + e.getMessage());
      return null;
    } // guich@200
    return stream;
  }

  /** read the available bytes from the stream getted with openInputStream.
    * called by totalcross.ui.image.Image and totalcross.io.PDBFile
    */
  public byte[] readBytes(String path) {
    byte[] bytes = null;
    try {
      InputStream is = openInputStream(path);
      if (is != null) {
        int n = is.available();
        bytes = new byte[n];
        is.read(bytes);
        is.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bytes;
  }

  /** write the available bytes to the stream getted with openOutputStream.
    * called by totalcross.io.PDBFile
    */
  public boolean writeBytes(String path, byte[] buf, int len) {
    boolean ret = true;
    try {
      OutputStream os = openOutputStream(path);
      if (os != null) {
        if (buf != null) {
          os.write(buf, 0, len);
          os.close(); // pietj@330_1
        } else {
          print("ATT: you sent to stream.writeBytes a null buffer!");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      ret = false;
    }
    return ret;
  }

  /** return true is the string is valid. called by openInputStream and openOutputStream in this class. */
  private boolean isOk(String s) {
    return s != null && s.length() > 0;
  }

  String getDefaultCrid(String name) {
    if (name == null) {
      return null;
    }

    if (name.indexOf('.') != -1) {
      name = name.substring(name.lastIndexOf('.') + 1);
    }
    int i;
    int n = name.length();
    int hash = 0;
    byte[] creat = new byte[4];
    for (i = 0; i < n; i++) {
      hash += (byte) name.charAt(i);
    }
    for (i = 0; i < 4; i++) {
      creat[i] = (byte) ((hash % 26) + 'a');
      if ((hash & 64) > 0) {
        creat[i] += ('A' - 'a');
      }
      hash = hash / 2;
    }
    return new String(creat);
  }

  void storeSettings() {
    try {
      String crid = crid4settings;//totalcross.sys.Settings.applicationId;
      // first verify if the PDBFile is created but the String is null
      totalcross.sys.Settings.showDesktopMessages = false; // guich@340_49
      boolean saveSettings = totalcross.sys.Settings.appSettings != null || totalcross.sys.Settings.appSecretKey != null
          || totalcross.sys.Settings.appSettingsBin != null; // guich@570_9: also check if appSecretKey is null

      totalcross.io.PDBFile cat;

      if (!saveSettings) {
        try {
          cat = new totalcross.io.PDBFile("Settings4" + crid + ".TCVM." + crid, totalcross.io.PDBFile.READ_WRITE); // guich@241_17: changed READ_ONLY to READ_WRITE to fix "operation invalid" error
          cat.delete();
        } catch (totalcross.io.FileNotFoundException e) {
        }
      } else {
        cat = new totalcross.io.PDBFile("Settings4" + crid + ".TCVM." + crid, totalcross.io.PDBFile.CREATE);
        totalcross.io.ResizeRecord rs = new totalcross.io.ResizeRecord(cat, 256);
        totalcross.io.DataStream ds = new totalcross.io.DataStream(rs);

        try {
          cat.setRecordPos(1);
          cat.deleteRecord();
        } catch (totalcross.io.IOException e) {
        }
        try {
          cat.setRecordPos(0);
          cat.deleteRecord();
        } catch (totalcross.io.IOException e) {
        }
        rs.startRecord();
        // store the appSettings record
        ds.writeString(totalcross.sys.Settings.appSettings);
        ds.writeString(totalcross.sys.Settings.appSecretKey);
        rs.endRecord();
        // guich@573_16: store the bin in another record
        if (totalcross.sys.Settings.appSettingsBin != null) {
          int len = totalcross.sys.Settings.appSettingsBin.length;
          cat.addRecord(len);
          cat.writeBytes(totalcross.sys.Settings.appSettingsBin, 0, len);
        }
        cat.close();

      }
      totalcross.sys.Settings.showDesktopMessages = true;
    } catch (Throwable t) {
      System.out.println("Settings can't be stored: " + t.toString());
    }
  }

  private void getAppSettings() {
    String crid = crid4settings = totalcross.sys.Settings.applicationId;
    totalcross.sys.Settings.showDesktopMessages = false; // guich@340_49
    try {
      totalcross.io.PDBFile cat = new totalcross.io.PDBFile("Settings4" + crid + ".TCVM." + crid,
          totalcross.io.PDBFile.READ_WRITE);
      totalcross.io.DataStream ds = new totalcross.io.DataStream(cat);
      cat.setRecordPos(0);
      String s;
      s = ds.readString();
      if (!"".equals(s)) {
        totalcross.sys.Settings.appSettings = s;
      }
      try {
        s = ds.readString();
        if (!"".equals(s)) {
          totalcross.sys.Settings.appSecretKey = s;
        }
      } catch (Throwable t) {
        System.out.println("Reading an old settings file; no appSecretKey available.");
      }

      if (cat.getRecordCount() > 1) // guich@573_16
      {
        cat.setRecordPos(1);
        byte[] buf = new byte[cat.getRecordSize()];
        cat.readBytes(buf, 0, buf.length);
        totalcross.sys.Settings.appSettingsBin = buf;
      }

      cat.close();
    } catch (Throwable t) {
    }
    totalcross.sys.Settings.showDesktopMessages = true; // guich@340_49
  }

  private char getFirstSymbol(String s) {
    char[] c = s.toCharArray();
    for (int i = 0; i < c.length; i++) {
      if (c[i] != ' ' && !('0' <= c[i] && c[i] <= '9')) {
        return c[i];
      }
    }
    return ' ';
  }

  /** called by totalcross.Launcher.init() */
  public void fillSettings() {
    if (settingsFilled) {
      return;
    }
    settingsFilled = true;
    java.util.Calendar cal = java.util.Calendar.getInstance();
    // guich@340_34: since java can't provide us good methods to return these values, we use parse the return of some formatting methods
    cal.set(2002, 11, 25, 20, 0, 0); // guich@401_32
    java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT); // guich@401_32: fixed wrong results in some systems
    String d = df.format(cal.getTime());
    totalcross.sys.Settings.dateFormat = d.startsWith("25") ? totalcross.sys.Settings.DATE_DMY
        : d.startsWith("12") ? totalcross.sys.Settings.DATE_MDY : totalcross.sys.Settings.DATE_YMD;
    totalcross.sys.Settings.dateSeparator = getFirstSymbol(d);
    df = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT); // guich@401_32
    d = df.format(cal.getTime());

    totalcross.sys.Settings.is24Hour = d.toLowerCase().indexOf("am") == -1 && d.toLowerCase().indexOf("pm") == -1;
    totalcross.sys.Settings.timeSeparator = getFirstSymbol(d);
    //

    totalcross.sys.Settings.weekStart = (byte) (cal.getFirstDayOfWeek() - 1);
    settingsRefresh(false);

    java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols();
    totalcross.sys.Settings.thousandsSeparator = dfs.getGroupingSeparator();
    totalcross.sys.Settings.decimalSeparator = dfs.getDecimalSeparator();
    totalcross.sys.Settings.screenBPP = toBpp;
    try {
      totalcross.sys.Settings.screenWidthInDPI = totalcross.sys.Settings.screenHeightInDPI = Toolkit.getDefaultToolkit()
          .getScreenResolution();
    } catch (Throwable t) {
      totalcross.sys.Settings.screenWidthInDPI = 96;
    }
    totalcross.sys.Settings.romVersion = 0x02000000;
    totalcross.sys.Settings.uiStyle = totalcross.sys.Settings.Vista;
    totalcross.sys.Settings.screenWidth = toWidth;
    totalcross.sys.Settings.screenHeight = toHeight;
    totalcross.sys.Settings.onJavaSE = true;
    totalcross.sys.Settings.platform = Settings.JAVA;
    totalcross.sys.Settings.applicationId = getDefaultCrid(className); // dhaysmith@420_4
    totalcross.sys.Settings.deviceId = "Desktop"; // guich@568_2
    if (totalcross.sys.Settings.applicationId != null) {
      getAppSettings(); // guich@330_47
    }
    try {
      // Fill all paths
      String basePath = System.getProperty("user.dir");
      totalcross.sys.Settings.vmPath = basePath;
      totalcross.sys.Settings.appPath = basePath;
      // guich@tc112_21: commented - if (totalcross.sys.Settings.dataPath == null) totalcross.sys.Settings.dataPath = basePath; // flsobral@tc100b5_51: Settings.dataPath was being overwritten if set before the Launcher was initialized.

      if (totalcross.sys.Settings.appPath != null) // guich@582_17: make sure that it ends with a slash
      {
        if (totalcross.sys.Settings.appPath.indexOf('/') >= 0 && !totalcross.sys.Settings.appPath.endsWith("/")) {
          totalcross.sys.Settings.appPath += "/";
        } else if (totalcross.sys.Settings.appPath.indexOf('\\') >= 0
            && !totalcross.sys.Settings.appPath.endsWith("\\")) {
          totalcross.sys.Settings.appPath += "\\";
        }
      }
      totalcross.sys.Settings.userName = !isApplication ? null : java.lang.System.getProperty("user.name");
    } catch (SecurityException se) {
      totalcross.sys.Settings.userName = null;
    }
  }

  @SuppressWarnings("deprecation")
  public void settingsRefresh(boolean callStoreSettings) // guich@tc115_81
  {
    java.util.TimeZone tz = java.util.TimeZone.getDefault(); // guich@340_33
    Settings.daylightSavingsMinutes = tz.getDSTSavings() / 60000;
    Settings.daylightSavings = Settings.daylightSavingsMinutes != 0;
    Settings.timeZone = tz.getRawOffset() / (60 * 60000);
    Settings.timeZoneMinutes = tz.getRawOffset() / 60000;
    Settings.timeZoneStr = java.util.TimeZone.getDefault().getID();
    if (callStoreSettings) {
      try {
        storeSettings();
      } catch (Exception e) {
      }
    }
  }

  ////  font and font metrics //////////////////////////////////////////////////////
  static final int AA_NO = 0;
  static final int AA_4BPP = 1;
  static final int AA_8BPP = 2;
  private totalcross.util.Hashtable htLoadedFonts = new totalcross.util.Hashtable(31);
  static Hashtable htBaseFonts = new Hashtable(5); // 

  static totalcross.ui.font.Font getBaseFont(String name, boolean bold, int size, String suffix) {
    String key = name + "|" + bold + "|" + size + "|" + suffix;
    totalcross.ui.font.Font f = (totalcross.ui.font.Font) htBaseFonts.get(key);
    if (f == null) {
      int i;
      if (!name.endsWith("noaa")) {
        TCZ z = (TCZ) loadedTCZs.get((name + ".tcz").toLowerCase());
        if (z == null) {
          return null;
        }
        FontInfo fi = (FontInfo) z.bag;
        for (i = 0; i < fi.sizes.length - 1; i++) {
          if (size <= fi.sizes[i]) {
            size = fi.sizes[i];
            break;
          }
        }
      }

      int idx = Integer.parseInt(suffix.substring(suffix.indexOf('u') + 1));
      totalcross.ui.font.Font.baseChar = (char) idx;
      f = totalcross.ui.font.Font.getFont(name, bold, size);
      totalcross.ui.font.Font.baseChar = ' ';
      if (f != null) {
        f.removeFromCache();
        htBaseFonts.put(key, f);
      }
    }

    return f;
  }

  private UserFont loadUF(String fontName, String suffix) {
    try {
      if (totalcross.ui.font.Font.baseChar == ' ' && !fontName.endsWith("noaa")) // test if there's another 8bpp native font. - base font
      {
        boolean bold = suffix.charAt(1) == 'b';
        int size = Integer.parseInt(suffix.substring(2, suffix.indexOf('u')));
        totalcross.ui.font.Font base = getBaseFont(fontName, bold, size, suffix);
        if (base == null) {
          new UserFont(fontName, suffix); // load sizes
          base = getBaseFont(fontName, bold, size, suffix);
        }
        if (base != null) {
          return new UserFont(fontName, suffix, size, base);
        }
      }
      return new UserFont(fontName, suffix);
    } catch (Exception e) {
      String msg = "" + e.getMessage();
      if (!msg.startsWith("name") && !msg.endsWith("not found")) {
        if (Settings.onJavaSE) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  public UserFont getFont(totalcross.ui.font.Font f, char c) {
    UserFont uf = null;
    try {
      // verify if its in the cache.
      String fontName = f.name;
      int size = (int) (Math.max(f.size, totalcross.ui.font.Font.MIN_FONT_SIZE) * Settings.screenDensity); // guich@tc122_15: don't check for the maximum font size here
      size += ((int) (size * 0.15)) * 3;
      
      char faceType = c < 0x3000 && f.style == 1 ? 'b' : 'p';
      int uIndex = ((int) c >> 8) << 8;
      String suffix = "$" + faceType + size + "u" + uIndex;
      String key = fontName + suffix;
      uf = (UserFont) htLoadedFonts.get(key);
      if (uf != null) {
        return uf;
      }

      if (fontName.charAt(0) == '$') {
        print("Native fonts are not supported on Desktop");
      } else {
        // first, try to load the font itself using the current font pattern
        uf = loadUF(fontName, suffix);
        if (uf == null) {
          uf = loadUF(fontName, "$p" + size + "u" + uIndex); // guich@tc122_15: ... check only here
        }
        if (uf == null && f.size != totalcross.ui.font.Font.NORMAL_SIZE) {
          int t = f.size;
          while (uf == null && ++t <= 120) {
            uf = loadUF(fontName, "$p" + t + "u" + uIndex);
          }
          t = f.size;
          while (uf == null && --t >= 5) {
            uf = loadUF(fontName, "$p" + t + "u" + uIndex);
          }
        }
        if (uf == null) {
          uf = loadUF(fontName, "$" + faceType + totalcross.ui.font.Font.NORMAL_SIZE + "u" + uIndex);
        }
        if (uf == null && faceType != 'p') {
          uf = loadUF(fontName, "$p" + totalcross.ui.font.Font.NORMAL_SIZE + "u" + uIndex);
        }
      }

      // at last, use the default font
      if (uf == null) {
        uf = loadUF(totalcross.ui.font.Font.DEFAULT, suffix);
      }
      if (uf == null && fontName.charAt(0) != '$') {
        for (int i = totalcross.ui.font.Font.MIN_FONT_SIZE; i <= totalcross.ui.font.Font.MAX_FONT_SIZE; i++) {
          if ((uf = loadUF(fontName, "$p" + i + "u" + uIndex)) != null) {
            break;
          }
        }
      }
      if (uf == null) {
        for (int i = totalcross.ui.font.Font.MIN_FONT_SIZE; i <= totalcross.ui.font.Font.MAX_FONT_SIZE; i++) {
          if ((uf = loadUF(totalcross.ui.font.Font.DEFAULT, "$p" + i + "u" + uIndex)) != null) {
            break;
          }
        }
      }

      if (uf != null) {
        if (totalcross.ui.font.Font.baseChar == ' ') {
          htLoadedFonts.put(key, uf); // note that we will use the original key to avoid entering all exception handlers.
        }
        f.name = uf.fontName; // update the name, the font may have been replaced.
      } else if (htLoadedFonts.size() > 0) {
        return c == ' ' ? null : getFont(f, ' '); // probably the index was outside the available ranges at this font - guich@tc110_28: if space, just return null
      } else if (appletInitialized) // guich@500_1: when retroguard is loaded, Applet.init is never called, so we just skip here.
      {
        System.err.println("No fonts found! be sure to place the file " + totalcross.ui.font.Font.DEFAULT
            + ".tcz in the same directory from where you're running your application"
            + (isApplication ? " or put a reference to TotalCross3/etc folder in the classpath!"
                : "or in your applet's codebase or in a jar file!"));
        System.exit(2);
      }
    } catch (Exception e) {
      System.out.println("Launcher.getFont: " + e);
    }
    return uf;
  }

  /** Represents the internal font structure, read from a pdb file. used internally. */
  // created by guich@200b2
  public static class CharBits // pgr@402_50 - describe the bitmap for a given character
  {
    public int rowWIB; // width in bytes
    public byte[] charBitmapTable;
    public int offset; // offset relative to the bitmap table
    public int width;
    public int index;
    public totalcross.ui.image.Image img;
  }

  private static Hashtable loadedTCZs = new Hashtable(31);

  class FontInfo {
    totalcross.io.ByteArrayStream chunks[];
    int[] sizes;
  }

  public class UserFont {
    // 25/120 14/70 4/25 2/15
    public UserFont ubase;
    public totalcross.ui.image.Image[] nativeFonts; // stores the system font in some platforms
    public int antialiased; // AA_ flags
    public int firstChar; // ASCII code of first character
    public int lastChar; // ASCII code of last character
    public int spaceWidth; // width of the space char
    public int maxWidth; // width of font rectangle - unused
    public int maxHeight; // height of font rectangle
    public int owTLoc; // offset to offset/width table - unused
    public int ascent; // ascent
    public int descent; // descent
    public int rowWords; // row width of bit image / 2 - used only to compute rowWidthInBytes

    private int rowWidthInBytes;
    private byte[] bitmapTable;
    private int[] bitIndexTable;
    private String fontName;
    private int numberWidth;
    private int minusW;

    private UserFont(String fontName, String sufix, int size, totalcross.ui.font.Font base) throws Exception {
      UserFont ubase = (UserFont) base.hv_UserFont;
      this.ubase = ubase;
      this.maxHeight = size;
      this.rowWidthInBytes = ubase.rowWidthInBytes * maxHeight / ubase.maxHeight;
      this.bitIndexTable = new int[ubase.bitIndexTable.length];
      for (int i = 0; i < bitIndexTable.length; i++) {
        this.bitIndexTable[i] = ubase.bitIndexTable[i] * maxHeight / ubase.maxHeight;
      }
      this.nativeFonts = new totalcross.ui.image.Image[bitIndexTable.length];
      this.fontName = base.name;
      this.firstChar = ubase.firstChar;
      this.lastChar = ubase.lastChar;
      this.antialiased = ubase.antialiased;
      this.descent = ubase.descent * maxHeight / ubase.maxHeight;
      this.ascent = size - this.descent;
      this.numberWidth = ubase.numberWidth * maxHeight / ubase.maxHeight;
      this.spaceWidth = ubase.spaceWidth * maxHeight / ubase.maxHeight;
      this.minusW = ubase.minusW;
    }

    private UserFont(String fontName, String sufix) throws Exception {
      this.fontName = fontName;
      String fileName = fontName + ".tcz";
      TCZ z = (TCZ) loadedTCZs.get(fileName.toLowerCase());
      if (z == null) {
        InputStream is = openInputStream(fileName);
        if (is == null) {
          is = openInputStream("vm/" + fileName); // for the release sdk, there's no etc/fonts. the tcfont.tcz is located at dist/vm/tcfont.tcz
          if (is == null) {
            is = openInputStream("etc/fonts/" + fileName); // if looking for the default font when debugging, use etc/fonts
            if (is == null) {
              throw new Exception("file " + fileName + " not found"); // loaded = false
            }
          }
        }
        z = new TCZ(new IS2S(is));
        FontInfo fi = new FontInfo();
        int n = z.numberOfChunks;
        fi.chunks = new totalcross.io.ByteArrayStream[n];
        totalcross.util.IntVector sizes = new totalcross.util.IntVector(n / 2);
        for (int i = 0; i < n; i++) {
          int s = z.getNextChunkSize();
          fi.chunks[i] = new totalcross.io.ByteArrayStream(s);
          z.readNextChunk(fi.chunks[i]);
          // compute size - $p20u0
          String name = z.names[i];
          String ss = name.substring(name.lastIndexOf('$') + 2, name.lastIndexOf('u'));
          int size = toInt(ss);
          if (!sizes.contains(size)) {
            sizes.addElement(size);
          }
        }
        sizes.qsort();
        fi.sizes = sizes.toIntArray();
        z.bag = fi;
        loadedTCZs.put(fileName.toLowerCase(), z);
      }
      fontName += sufix;
      int index = z.findNamePosition(fontName.toLowerCase());
      if (index == -1) {
        throw new Exception("name " + fontName + " not found"); // loaded = false
      }

      totalcross.io.ByteArrayStream bas = ((FontInfo) z.bag).chunks[index];
      bas.reset();
      totalcross.io.DataStreamLE ds = new totalcross.io.DataStreamLE(bas);
      antialiased = ds.readUnsignedShort();
      firstChar = ds.readUnsignedShort();
      lastChar = ds.readUnsignedShort();
      spaceWidth = ds.readUnsignedShort();
      maxWidth = ds.readUnsignedShort();
      maxHeight = ds.readUnsignedShort();
      owTLoc = ds.readUnsignedShort();
      ascent = ds.readUnsignedShort();
      descent = ds.readUnsignedShort();
      rowWords = ds.readUnsignedShort();

      rowWidthInBytes = 2 * rowWords * (antialiased == AA_NO ? 1 : antialiased == AA_4BPP ? 4 : 8);
      int bitmapTableSize = (int) rowWidthInBytes * (int) maxHeight;

      bitmapTable = new byte[bitmapTableSize];
      ds.readBytes(bitmapTable);
      bitIndexTable = new int[lastChar - firstChar + 1 + 1];
      for (int i = 0; i < bitIndexTable.length; i++) {
        bitIndexTable[i] = ds.readUnsignedShort();
      }
      //
      minusW = antialiased == AA_8BPP && fontName.equals("TCFont") ? 1 : 0;
      if (firstChar <= '0' && '0' <= lastChar) {
        index = (int) '0' - (int) firstChar;
        numberWidth = bitIndexTable[index + 1] - bitIndexTable[index] - minusW;
      }
      if (antialiased == AA_8BPP) {
        nativeFonts = new totalcross.ui.image.Image[bitIndexTable.length];
      }
    }

    private totalcross.ui.image.Image getBaseCharImage(int index) throws totalcross.ui.image.ImageException // called only in ubase instances
    {
      if (bitmapTable == null && ubase != null) {
        return ubase.getBaseCharImage(index);
      }
      int offset = bitIndexTable[index];
      int width = bitIndexTable[index + 1] - offset - minusW;
      totalcross.ui.image.Image img = new totalcross.ui.image.Image(width, maxHeight);
      int[] pixels = img.getPixels();
      for (int y = 0, idx = 0; y < maxHeight; y++) {
        for (int x = 0; x < width; x++, idx++) {
          pixels[idx] = bitmapTable[y * rowWidthInBytes + x + offset] << 24;
        }
      }
      return img;
    }

    // Get the source x coordinate and width of the character
    public void setCharBits(char ch, CharBits bits) {
      if (firstChar <= ch && ch <= lastChar) {
        int index = (int) ch - (int) firstChar;
        bits.index = index;
        bits.rowWIB = rowWidthInBytes;
        bits.charBitmapTable = bitmapTable;
        bits.offset = bitIndexTable[index];
        bits.width = bitIndexTable[index + 1] - bits.offset - minusW;
        if (bits.width == 0) {
          bits.width += minusW;
        }
        if (ubase != null && ubase.nativeFonts != null) {
          try {
            if (ubase.nativeFonts[index] == null) {
              ubase.nativeFonts[index] = ubase.getBaseCharImage(index);
            }
            if (nativeFonts[index] == null) {
              nativeFonts[index] = ubase.nativeFonts[index].getHwScaledInstance(bits.width, maxHeight);
            }
            bits.img = nativeFonts[index];
            bits.rowWIB = bits.width;
          } catch (Exception e) {
            if (Settings.showDesktopMessages) {
              e.printStackTrace();
            }
            bits.width = spaceWidth;
            bits.offset = -1;
          }
        }
      } else {
        bits.width = spaceWidth;
        bits.offset = -1;
      }
    }
  }

  public int getCharWidth(totalcross.ui.font.Font f, char ch) // guich@tc122_16: moved to outside UserFont, because each char may be in a different UserFont
  {
    UserFont font = (UserFont) f.hv_UserFont;
    if (ch < font.firstChar || ch > font.lastChar) {
      f.hv_UserFont = font = Launcher.instance.getFont(f, ch);
    }
    if (ch == 160) {
      return font.numberWidth;
    }
    if (ch < ' ') {
      return (ch == '\t') ? font.spaceWidth * totalcross.ui.font.Font.TAB_SIZE : 0; // guich@tc100: handle tabs
    }
    int index = (int) ch - (int) font.firstChar;
    return (font.firstChar <= ch && ch <= font.lastChar)
        ? font.bitIndexTable[index + 1] - font.bitIndexTable[index] - font.minusW : font.spaceWidth;
  }

  private class AlertBox extends Frame implements java.awt.event.ActionListener {
    private java.awt.Button ok;
    private java.awt.TextArea ta;

    public AlertBox() {
      super("Alert");
      setLayout(new BorderLayout());
      add("Center", ta = new java.awt.TextArea());
      Panel p = new Panel();
      p.setLayout(new FlowLayout());
      p.add(ok = new java.awt.Button("Ok"));
      ok.addActionListener(this);
      add("South", p);
      pack();
      Dimension d = getToolkit().getScreenSize();
      setLocation(d.width / 3, d.height / 3);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent ae) {
      if (ae.getSource() == ok) {
        setVisible(false);
      }
    }

    public void setText(String s) {
      ta.setText(s);
    }
  }

  private class Activate extends Window {

    public Activate() {
      super("Activation Key required", ROUND_BORDER);
      this.fadeOtherWindows = true;
      this.canDrag = false;
    }

    @Override
    public void initUI() {
      Label lblKey = new Label("Activation Key");
      final Edit edKey = new Edit();
      edKey.setMaxLength(24);

      add(lblKey, CENTER, CENTER - 50);
      add(edKey, LEFT + 20, AFTER, FILL - 20, PREFERRED);

      Button btOk = new Button("  OK  ");
      add(btOk, CENTER, AFTER + 10);
      btOk.addPressListener(new PressListener() {

        @Override
        public void controlPressed(ControlEvent e) {
          String email = edKey.getText();
          if (email != null && email.length() > 0) {
            try {
              Class.forName("tc.tools.RegisterSDK").getConstructor(String.class, String.class).newInstance(email, null);
              
              new Thread() {
                  @Override
                  public void run() {
                    try {
                      new URL("http://www.superwaba.net/SDKRegistrationService/PingService?CHAVE=" + email)
                          .openConnection().getInputStream().close();
                    } catch (Throwable e) {
                    }
                  }
                }.start(); // keep track of TC usage
                
                Activate.this.unpop();
              
            } catch (Exception e1) {
            	
							e1.printStackTrace();

							MessageBox mb = new MessageBox("Error to validate KEY",
									"Try again or get a new KEY at www.totalcross.com", new String[] { "Close" });
							mb.setBackColor(totalcross.ui.gfx.Color.getRGB(250, 250, 250));
							mb.setForeColor(0x000000);
							mb.popup();
            }
          }else {
    			MessageBox mb = new MessageBox("Error to validate KEY", "Please insert a valid KEY or get a new one at www.totalcross.com", new String[]{"Close"});
      			mb.setBackColor(totalcross.ui.gfx.Color.getRGB(250, 250, 250));
    	        	mb.setForeColor(0x000000);
      			mb.popup();
          }

        }
      });

      add(new Label("Register at totalcross.com"), CENTER, BOTTOM - 20);
    }

    @Override
    protected void onPopup() {
      setRect(CENTER, CENTER, SCREENSIZE + 90, SCREENSIZE + 60);
    }
  }

  public void alert(String msg) {
    if (!started) {
      System.out.println("Alert: " + msg);
    } else {
      alert.setText(msg);
      alert.setVisible(true);
      while (alert.isVisible()) {
        try {
          Thread.sleep(10);
        } catch (Exception e) {
        }
      }
    }
  }

  /** Converts a java.io.InputStream into a totalcross.io.Stream */
  public static class IS2S extends totalcross.io.Stream {
    InputStream is;

    public IS2S(InputStream is) {
      this.is = is;
    }

    @Override
    public void close() {
      try {
        is.close();
      } catch (Exception e) {
      }
      is = null;
    }

    @Override
    public int readBytes(byte[] buf, int start, int count) {
      try {
        return is.read(buf, start, count);
      } catch (Exception e) {
        return -1;
      }
    }

    @Override
    public int writeBytes(byte[] buf, int start, int count) {
      return 0; // not supported
    }
  }

  public static class S2IS extends java.io.InputStream {
    private Stream s;
    private byte[] oneByte = new byte[1];
    private int left;
    private boolean closeUnderlying;

    public S2IS(Stream s) {
      this(s, -1, true);
    }

    public S2IS(Stream s, int max) {
      this(s, max, true);
    }

    public S2IS(Stream s, int max, boolean closeUnderlying) {
      this.s = s;
      this.left = max;
      this.closeUnderlying = closeUnderlying;
    }

    @Override
    public int read() throws java.io.IOException {
      if (left == 0) {
        return -1;
      }

      try {
        int r = s.readBytes(oneByte, 0, 1);

        if (left != -1 && r == 1) {
          left--;
        }

        return r > 0 ? ((int) oneByte[0] & 0xFF) : -1;
      } catch (IOException e) {
        throw new java.io.IOException(e.getMessage());
      }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws java.io.IOException {
      if (left == 0) {
        return -1;
      }

      try {
        if (left != -1 && len > left) {
          len = left;
        }

        int r = s.readBytes(buf, off, len);

        if (left != -1 && r > 0) {
          left -= r;
        }

        return r;
      } catch (IOException e) {
        throw new java.io.IOException(e.getMessage());
      }
    }

    @Override
    public void close() throws java.io.IOException {
      if (closeUnderlying) {
        try {
          s.close();
        } catch (IOException e) {
          throw new java.io.IOException(e.getMessage());
        }
      }
    }
  }

  public static class S2OS extends java.io.OutputStream {
    private Stream s;
    private byte[] oneByte = new byte[1];
    private int count;
    private boolean closeUnderlying;

    public S2OS(Stream s) {
      this(s, true);
    }

    public S2OS(Stream s, boolean closeUnderlying) {
      this.s = s;
      this.closeUnderlying = closeUnderlying;
    }

    public int count() {
      return count;
    }

    @Override
    public void write(int b) throws java.io.IOException {
      try {
        oneByte[0] = (byte) (b & 0xFF);

        int c = s.writeBytes(oneByte, 0, 1);
        if (c < 0) {
          throw new java.io.IOException("Unknown error when writing to stream");
        }
        count++;
      } catch (IOException e) {
        throw new java.io.IOException(e.getMessage());
      }
    }

    @Override
    public void write(byte[] b, int off, int len) throws java.io.IOException {
      try {
        int c = s.writeBytes(b, off, len);
        if (c < 0) {
          throw new java.io.IOException("Unknown error when writing to stream");
        }
        count += c;
      } catch (IOException e) {
        throw new java.io.IOException(e.getMessage());
      }
    }

    @Override
    public void close() throws java.io.IOException {
      if (closeUnderlying) {
        try {
          s.close();
        } catch (IOException e) {
          throw new java.io.IOException(e.getMessage());
        }
      }
    }
  }

  public void setTitle(String title) {
    if (isApplication) {
      frameTitle = title;
      if (frame != null) {
        frame.setTitle(title);
      }
    }
  }

  public void vibrate(final int millis) {
    if (isApplication && frame != null) {
      new Thread() {
        @Override
        public void run() {
          Point p = frame.getLocation();
          int x = p.x, y = p.y;

          int[] xPoints = { x - 3, x, x + 3, x, x + 3, x, x - 3, x };
          int[] yPoints = { y - 3, y, y + 3, y, y - 3, y, y + 3, y };
          int i = 0;
          int j = 0;

          int t = Vm.getTimeStamp();
          do {
            frame.setLocation(xPoints[i], yPoints[j]);

            i = ++i % xPoints.length;
            if (i == 0) {
              j = ++j % yPoints.length;
            }

            Thread.yield();// give some time for the other threads to execute
          } while (Vm.getTimeStamp() - t < millis);

          frame.setLocation(x, y); // restore original location
        }
      }.start();
    }
  }

  public void setSIP(int option, Control edit, boolean secret) {
  }

  public static void checkLitebaseAllowed() {
  }

  @Override
  public void componentHidden(ComponentEvent arg0) {
  }

  @Override
  public void componentMoved(ComponentEvent arg0) {
  }

  @Override
  public void componentShown(ComponentEvent arg0) {
  }

  boolean ignoreNextResize; // guich@tc168: ignore when using F9

  @Override
  public void componentResized(ComponentEvent ev) {
    if (ignoreNextResize) {
      ignoreNextResize = false;
      return;
    }
    int w = frame.getWidth() - frame.insets.left - frame.insets.right;
    int h = frame.getHeight() - frame.insets.top - frame.insets.bottom;
    w /= toScale; // guich@tc168: consider scale
    h /= toScale;
    if (w < toWidth || h < toHeight) {
      screenResized(w >= toWidth ? w : toWidth, h >= toHeight ? h : toHeight, true);
    } else {
      screenResized(w, h, false);
    }
  }
}
