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
import tc.simulator.awt.AwtWindow;
import tc.preview.PreviewSession;
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
abstract class ApplicationLoader extends RuntimeState {
  static void showInstructions() {
    System.out.println("Possible Arguments (in any order and case insensitive). Default is marked as *");
    System.out.println("   /scr WIDTHxHEIGHT     : sets the width and height resolution.");
    System.out.println("   /scr WIDTHxHEIGHTxBPP : sets the width, height and bits per pixel (8, 16, 24 or 32)");
    System.out.println("   /density <0.1 to 4>   : sets the screen pixel density");
    System.out.println("   /scr win32       : Windows 32            (same of /scr 240x320x24)");
    System.out.println("*  /scr android     : Android               (same of /scr 360x592x24  /density 2)");
    System.out.println("   /scr iPhone      : iPhone 15 resolution  (same of /scr 393x852x24  /density 3  /safeAreaPortrait 59,0,34,0 /safeAreaLandscape 0,59,21,59)");
    System.out.println("   /scr iPhoneSE    : iPhone SE resolution  (same of /scr 375x667x24  /density 2)");
    System.out.println("   /scr ipad        : iPad resolution       (same of /scr 768x1024x24 /density 2)");
    System.out.println("   /fullscreen      : Use full-screen window");
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
    System.out.println("   /safeAreaPortrait  top,left,bottom,right : sets a margin from the device borders to simulate devices with notch on portrait");
    System.out.println("   /safeAreaLandscape top,left,bottom,right : sets a margin from the device borders to simulate devices with notch on landscape");
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
    Launcher.startApplication(args[args.length - 1], Arrays.copyOf(args, args.length - 1));
  }

  protected int toInt(String s) // Convert.toInt can't be used here, otherwise, the settings will be set too early!
  {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return 0;
    }
  }

  protected void parseArguments(String... args) {
    parseApplicationArguments(args[args.length - 1], Arrays.copyOf(args, args.length - 1));
  }

  protected void parseApplicationArguments(String clazz, String... args) {
    SimulatorConfiguration config = new SimulatorConfiguration(clazz, args);
    try {
      getRuntime().parseSimulatorArguments((Launcher) this, config, isApplication, getSize().width, getSize().height);
    } catch (CommandLineParser.InvalidArgumentException e) {
      showInstructions();
      System.err.println("Invalid or incomplete argument at position " + e.getIndex() + ": " + e.getArgument());
      System.err.println(e.getMessage());
      System.err.println("Full command line:\n" + e.getFullCommandLine());
      exit(-1);
      return;
    }
  }

  void applyParsedArguments(LaunchOptions result) {
    className = result.className;
    toWidth = result.width;
    toHeight = result.height;
    toBpp = result.bpp;
    commandLine = result.commandLine;
    toUI = result.uiStyle;
    toScaleValue = result.scaleValue;
    toDensityValue = result.densityValue;
    toScale = result.scale;
    fastScale = result.fastScale;
    isDemo = result.demo;
    toInsetsPortrait = result.insetsPortrait;
    toInsetsLandscape = result.insetsLandscape;
  }

  protected String[] tokenizeString(String string, char c) {
    java.util.StringTokenizer st = new java.util.StringTokenizer(string, "" + c);
    String[] ret = new String[st.countTokens()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = st.nextToken();
    }
    return ret;
  }

  public void start() {
    mainWindow = MainWindow.getMainWindow();
  }

  void setAppletArguments(String appletArguments) {
    this.appletArguments = appletArguments;
  }

  void setAppletCodeBase(URL appletCodeBase) {
    this.appletCodeBase = appletCodeBase;
  }

  void setAppletParameter(String name, String value) {
    if (name != null && value != null) {
      appletParameters.put(name, value);
    }
  }

  protected String getLauncherParameter(String name) {
    return appletParameters.get(name);
  }

  protected URL getLauncherCodeBase() throws java.net.MalformedURLException {
    return appletCodeBase == null ? new File(".").toURI().toURL() : appletCodeBase;
  }

  void setRuntime(Launcher runtime) {
    this.runtime = runtime;
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
    if (hasWindowBackend()) {
      minimizeWindow();
    }
  }

  public void restore() {
    if (hasWindowBackend()) {
      restoreWindow();
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

  protected int modifiers;

  protected void updateModifiers(java.awt.event.KeyEvent event) {
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

}
