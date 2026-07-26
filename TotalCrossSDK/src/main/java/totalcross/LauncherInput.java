// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Dave Slaughter
// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross;

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
import totalcross.preview.AppletPreviewSurface;
import totalcross.preview.AwtWindowBackend;
import totalcross.preview.PreviewRuntime;
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
abstract class LauncherInput extends LauncherArguments {

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

  protected void takeScreenShot() {
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

  protected void screenResized(int w, int h, boolean setframe) {
    if (screenImg == null || (Settings.screenWidth == w && Settings.screenHeight == h)) {
      return;
    }
    Settings.screenWidth = w;
    Settings.screenHeight = h;
    setWindowSize(w, h, setframe);
    screenImg = null; // force the creation of a new screen image
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
    if (hasWindowBackend() && windowFocusOwnerIsNotThis() && !destroyed) {
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
    if (hasWindowBackend() && Settings.showMousePosition) // guich@tc115_48
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
      setWindowTitle(mmsb.toString());
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
    if (runtime != null) {
      runtime.setNewMainWindow(newInstance, args);
    } else {
      replaceMainWindow(newInstance, args);
    }
  }

  void replaceMainWindow(MainWindow newInstance, String args)
  {
    commandLine = args; // guich@200b3: added command line support for desktop classes.
    if (winTimer != null) {
      winTimer.stopGracefully(); // guich@120
      winTimer = null;
    }
    Window.destroyZStack();
    mainWindow = newInstance;
    mainWindow.initUI(); // ps: since we are being called from an app, we cannot use the synchronized method
  }

  void preparePreviewMainWindowReload() {
    if (winTimer != null) {
      winTimer.stopGracefully();
      winTimer = null;
    }
    MainWindow.resetPreviewState();
  }

  void replacePreviewMainWindow(MainWindow newInstance, String args) {
    commandLine = args;
    mainWindow = newInstance;
    if (eventThread != null) {
      eventThread.setMainClass(newInstance);
      boolean started = eventThread.invokeInEventThread(true, new Runnable() {
        @Override
        public void run() {
          mainWindow.appStarting(isDemo ? 80 : -1);
        }
      }, PREVIEW_DESTROY_TIMEOUT_MILLIS);
      if (!started) {
        System.err.println("Timed out waiting for TotalCross preview appStarting during reload.");
      }
    } else {
      mainWindow.appStarting(isDemo ? 80 : -1);
    }
  }

  void showPreviewContainer(final Container container) {
    runInPreviewEventThread(new Runnable() {
      @Override
      public void run() {
        mainWindow.swap(container);
      }
    }, "container preview");
  }

  void showPreviewControl(final Control control) {
    runInPreviewEventThread(new Runnable() {
      @Override
      public void run() {
        mainWindow.removeAll();
        mainWindow.add(control, Control.CENTER, Control.CENTER, Control.PREFERRED, Control.PREFERRED);
      }
    }, "control preview");
  }

  protected void runInPreviewEventThread(Runnable runnable, String operation) {
    if (eventThread != null) {
      boolean completed = eventThread.invokeInEventThread(true, runnable, PREVIEW_DESTROY_TIMEOUT_MILLIS);
      if (!completed) {
        System.err.println("Timed out waiting for TotalCross " + operation + ".");
      }
    } else {
      runnable.run();
    }
  }


}
