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
abstract class RuntimeState extends SimulatorSupport {
  public void destroy() {
    if (mainWindow == null || destroyed) {
      return;
    }
    destroyed = true;
    boolean ended = eventLoop == null || eventLoop.invoke(new Runnable() {
      @Override
      public void run() {
        mainWindow.appEnding();
        System.runFinalization();
        storeSettings();
      }
    }, previewMode ? PREVIEW_DESTROY_TIMEOUT_MILLIS : 0);
    if (!ended) {
      System.err.println("Timed out waiting for TotalCross preview appEnding during reload.");
    }
    if (winTimer != null) {
      winTimer.shutdown(); // timer must be running when appEnding is called
    }
    if (eventLoop != null) {
      eventLoop.shutdown();
      eventLoop = null;
    }
    stopWindowBackend();
  }

  @SuppressWarnings("static-access")
  final public void init() {
    boolean showInstructionsOnError = true;
    appletInitialized = true; // guich@500_1
    totalcross.sys.Settings.showDesktopMessages = true; // guich@500_1: redo the messages.
    try {
      if (shouldCreateAlertBox(previewMode, java.awt.GraphicsEnvironment.isHeadless())) {
        alert = new AlertBox();
      }
      // NOTE: applet parameters are supplied by LauncherApplet after construction,
      // so they can only be parsed during init.
      if (!isApplication) {
        String arguments = appletArguments;
        if (arguments == null) {
          throw new Exception(
              "Error: you must suply an 'arguments' property with all the argments to create the application");
        }
        String[] args = tokenizeString(arguments, ' ');
        parseArguments(args);
        getRuntime().recordLauncherUsage();
      }

      getRuntime().initializeSettings((Launcher) this);

      try {
        _class = getClass(); // guich@500_1: we can use ourselves
        // if the user pass: tc/samples/ui/image/test/ImageTest.class, change to tc.samples.ui.image.test.ImageTest
        className = Launcher.normalizeMainWindowClassName(className);
        mainWindow = getRuntime().createMainWindow(className, getAppClassLoader(), terminateIfMainClass);
        showInstructionsOnError = false;
        if (mainWindow == null) {
          return;
        }
        // NOTE: java will call a partially constructed object if show() is called before all the objects are constructed
        if (isApplication && !previewMode) {
          createWindowBackend();
          requestFocus();
        } else if (!previewMode) {
          setLayout(new java.awt.BorderLayout());
        }
        if (toUI != -1) {
          mainWindow.setUIStyle((byte) toUI);
        }
      } catch (LinkageError le) {
        if (previewMode) {
          throw previewInitializationFailure(le);
        }
        System.out.println("Fatal Error when running applet: there is an error in the constructor of the class "
            + className + " and it could not be instantiated. Stack trace: ");
        le.printStackTrace();
        exit(0);
      } catch (ClassCastException cce) {
        if (previewMode) {
          throw previewInitializationFailure(cce);
        }
        System.out.println("Error: class " + className + " does not extend MainClass nor MainWindow!");
        cce.printStackTrace();
        exit(-1);
      } catch (ClassNotFoundException cnfe) {
        if (previewMode) {
          throw previewInitializationFailure(cnfe);
        }
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
    } catch (PreviewInitializationException pie) {
      throw pie;
    } catch (Exception ee) {
      if (previewMode) {
        throw previewInitializationFailure(ee);
      }
      if (showInstructionsOnError) {
        ApplicationLoader.showInstructions();
      }
      ee.printStackTrace();
    } catch (Error error) {
      if (previewMode) {
        throw previewInitializationFailure(error);
      }
      throw error;
    } // guich@120
  }

  private PreviewInitializationException previewInitializationFailure(Throwable cause) {
    return new PreviewInitializationException(className, cause);
  }

  static boolean shouldCreateAlertBox(boolean previewMode, boolean headless) {
    return !previewMode && !headless;
  }

  private static final class PreviewInitializationException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private PreviewInitializationException(String className, Throwable cause) {
      super("Failed to initialize preview MainWindow: " + className, cause);
    }
  }

  protected ClassLoader getAppClassLoader() {
    return appClassLoader == null ? getClass().getClassLoader() : appClassLoader;
  }

  protected Launcher getRuntime() {
    if (runtime == null) {
      runtime = new Launcher();
    }
    return runtime;
  }

  protected void createWindowBackend() {
    windowBackend = getRuntime().startWindowBackend(instance,
        frameTitle != null ? frameTitle : mainWindow.getClass().getName(),
        new java.awt.Color(getScreenColor(mainWindow.getBackColor())), instance, instance);
  }

  protected boolean hasWindowBackend() {
    return windowBackend != null;
  }

  protected void stopWindowBackend() {
    if (windowBackend != null) {
      windowBackend.stop();
      windowBackend = null;
    }
  }

  protected void setWindowSize(int width, int height, boolean resizeFrame) {
    windowBackend.setContentSize(width, height, resizeFrame);
  }

  protected void setWindowTitle(String title) {
    windowBackend.setTitle(title);
  }

  protected void minimizeWindow() {
    windowBackend.setExtendedState(Frame.ICONIFIED);
  }

  protected void restoreWindow() {
    windowBackend.setExtendedState(Frame.NORMAL);
  }

  protected boolean windowFocusOwnerIsNotThis() {
    return windowBackend.getFocusOwner() != this;
  }

  protected Point getWindowLocation() {
    return windowBackend.getLocation();
  }

  protected void setWindowLocation(int x, int y) {
    windowBackend.setLocation(x, y);
  }

  protected int getWindowContentWidth() {
    return windowBackend.getContentWidth();
  }

  protected int getWindowContentHeight() {
    return windowBackend.getContentHeight();
  }

  protected class WinTimer extends java.lang.Thread {
    protected int interval;
    protected boolean shouldStop;

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
        if (doTick && eventLoop != null) {
          eventLoop.post(new Runnable() {
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

    void shutdown() {
      // NOTE: It's not a good idea to call stop() on threads since
      // it can cause the JVM to crash.
      shouldStop = true;
      interrupt();
    }
  }

  public boolean eventIsAvailable() {
    return eventLoop != null && eventLoop.hasPendingEvents();
  }

  void startApp() {
    eventLoop = new EventLoop(mainWindow);
    if (!started) // guich@120 - make sure that the component is available for drawing when starting the application. called by paint.
    {
      try {
        eventLoop.invoke(new Runnable() {
          @Override
          public void run() {
            while (mainWindow == null) {
              Thread.yield();
            }
            mainWindow.appStarting(isDemo ? 80 : -1);
          } // guich@200b4_107 - guich@570_3: check if mainWindow is not null to avoid problems when running on Linux. seems that the paint event is being generated before the start one.
        });
      } catch (Throwable e) {
        e.printStackTrace();
      }
      started = true;
    }
  }

}
