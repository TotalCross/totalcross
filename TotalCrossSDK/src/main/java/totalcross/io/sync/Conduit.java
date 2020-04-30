// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.io.sync;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.sys.Registry;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Control;
import totalcross.ui.Label;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;

/**
 * This class allows you to automatically synchronize files between your device and the desktop. Currently it supports
 * only WinCE and PalmOS devices, and desktops with Windows 2000+. It is also required that you have installed the
 * synchronization software provided by the device manufacturer. (Refer to the TotalCross Companion for more details)
 *
 * <p>
 * This is the conduit's MainWindow. Two methods will be called at different times:
 * <ul>
 * <li>doSync: when a synchronization begins.
 * <li>doConfig: when a setup (preferences) is asked by the user.
 * </ul>
 * <p>
 */
public abstract class Conduit extends totalcross.ui.MainWindow {
  public static final int TARGETING_WINCE = 1;
  public static final int TARGETING_PALMOS = 2;
  public static final int TARGETING_ALL = 127;
  /** Defines the target platform for synchronization.
   * Note that changing this value has no effects.
   * @see #TARGETING_WINCE
   * @see #TARGETING_PALMOS
   * @see #TARGETING_ALL
   */
  public int syncTarget; // guich@567_18: made public
  Object conduitHandle;
  protected String conduitName;
  protected String targetApplicationId;
  protected String targetAppPath;

  private Button btnRP, btnUP, btnRW, btnUW;

  /**
   * Creates a conduit with a title and a border. This is the only constructor that your MainWindow class may call.
   */
  public Conduit(String conduitName, String targetApplicationId, String targetAppPath, byte style) {
    super(conduitName, style);

    if (conduitName == null) {
      throw new NullPointerException("Argument 'conduitName' cannot be null");
    }
    if (targetApplicationId == null) {
      throw new NullPointerException("Argument 'targetApplicationId' cannot be null");
    }
    if (targetApplicationId.length() != 4) {
      throw new IllegalArgumentException("Illegal creator id");
    }

    this.conduitName = conduitName;
    this.targetApplicationId = targetApplicationId;
    this.targetAppPath = targetAppPath;
  }

  /**
   * A commandline parameter must be passed in order to invoke actions on this conduit:<br>
   * /r{target platform} register conduit.<br>
   * /u{target platform} unregister conduit.<br>
   * /s{target platform} synchronize.<br>
   * /c configure.<br>
   *
   * Where {target platform} may be one of the following:<br>
   * p - targets PalmOS.<br>
   * w - targets WinCE.<br>
   * a - targets all supported platforms.<br>
   */
  @Override
  public final void initUI() {
    String cmd = getCommandLine();

    if (cmd != null && cmd.length() == 3 && cmd.charAt(0) == '/') {
      init(cmd);
    } else // show an UI so the user can register/unregister
    {
      setBorderStyle(HORIZONTAL_GRADIENT);
      gradientTitleStartColor = 0x0000CC;
      gradientTitleEndColor = Color.CYAN;
      setTitle("Register/Unregister");
      Label l = new Label(
          "Please use the buttons below to register\nor unregister your conduit. Note that\nthis can also be done from the\ncommandline, with the options provided\nbetween parenthesis. The app exits after\nthe task finishes with success.");
      l.align = FILL;
      add(l, LEFT, TOP);
      l = new Label("Palm OS (requires Palm Desktop)");
      l.setHighlighted(true);
      Button.commonGap = 2;
      add(l, LEFT, AFTER + 2);
      add(l = new Label("  "), CENTER, AFTER);
      add(btnRP = new Button("Register (/rp)"), BEFORE, SAME, l);
      btnRP.appObj = "/rp";
      add(btnUP = new Button("Unregister (/up)"), AFTER, SAME, l);
      btnUP.appObj = "/up";
      l = new Label("Windows CE (requires ActiveSync)");
      l.setHighlighted(true);
      add(l, LEFT, AFTER + 2);
      add(l = new Label("  "), CENTER, AFTER);
      add(btnRW = new Button("Register (/rw)"), BEFORE, SAME, l);
      btnRW.appObj = "/rw";
      add(btnUW = new Button("Unregister (/uw)"), AFTER, SAME, l);
      btnUW.appObj = "/uw";
      Button.commonGap = 0;
      repaintNow();
    }
  }

  @Override
  public void onEvent(Event e) {
    if (e.type == ControlEvent.PRESSED && ((Control) e.target).appObj != null) {
      init((String) ((Control) e.target).appObj);
    }
  }

  private void init(String cmd) {
    MessageBox mb = null;
    try {
      String lcmd = cmd.toLowerCase();
      syncTarget = 0;
      if (lcmd.charAt(2) == 'p') {
        syncTarget = TARGETING_PALMOS;
      } else if (lcmd.charAt(2) == 'w') {
        syncTarget = TARGETING_WINCE;
      } else if (lcmd.charAt(2) == 'a') {
        syncTarget = TARGETING_ALL;
      }

      if (!Vm.attachNativeLibrary("TCSync")) {
        throw new RuntimeException(
            "Could not load native library\nTCSync.dll.\n\nPlease add its location\nto the path.");
      }

      if (syncTarget != 0) {
        switch (lcmd.charAt(1)) {
        case 'r': // register
        {
          mb = new MessageBox("Message",
              (syncTarget == TARGETING_PALMOS) ? "Registering the conduit\nfor PalmOS and\nrestarting hotsync..."
                  : "Registering the conduit\nfor WinCE...",
              null); // guich@511_4
          mb.popupNonBlocking();
          int error = register(2) ? 0 : 1;
          if (error == 0) {
            setSyncingEnabled(true);
            onRegister();
          }
          exit(error);
          return;
        }
        case 'u': // unregister
        {
          mb = new MessageBox("Message",
              (syncTarget == TARGETING_PALMOS) ? "Unregistering the conduit\nfor PalmOS and\nand restarting hotsync..."
                  : "Unregistering the conduit\nfor WinCE...",
              null); // guich@511_4
          mb.popupNonBlocking();
          int ret = unregister() ? 0 : 1;
          if (ret == 0) {
            onUnregister();
          }
          exit(ret);
          return;
        }
        case 's': // sync
        {
          repaintNow();
          if (initSync()) {
            doSync();
          }
          finishSync(); //flsobral@tc114_46: always clean up and exit after initSync.
          exit(0);
          return;
        }
        case 'c': // config
        {
          repaintNow();
          doConfig();
          return;
        } // no break here!
        }
      }
    } catch (Exception e) {
      if (mb != null) {
        mb.unpop();
        mb = null;
      }
      new MessageBox("Error", e.getMessage()).popup();
    }
    if (mb != null) {
      mb.unpop();
    }
  }

  @ReplacedByNativeOnDeploy
  private boolean initSync() {
    return true;
  }

  @ReplacedByNativeOnDeploy
  private boolean finishSync() {
    return true;
  }

  /**
   * When synchronizing Palm OS devices, logs the given text on the HotSync log.<br>
   * When synchronizing Windows CE devices, the text is logged on the debug console instead.
   */
  @ReplacedByNativeOnDeploy
  public static void log(String text) {
    Vm.debug(text);
  }

  /**
   * This method lets the conduit manager <i>take a breath</i>. This is important, because the connection between the
   * device and the desktop might be lost during long operations.
   */
  @ReplacedByNativeOnDeploy
  public static void yield() {
    Vm.sleep(10);
  }

  /**
   * Called when the synchronization is started. The whole synchronization done from this method.
   */
  protected abstract void doSync();

  /** Called when the user wants to configure this conduit. */
  protected abstract void doConfig();

  /**
   * This method must be used so that a conduit will be called when the synchronization occurs. Note that you cannot
   * call it directly, use the command line argument instead. Here is a sample:
   *
   * <pre>
   * TIConduit.exe /rp
   * </pre>
   *
   * <p>
   * Note that, to the conduit be called by HotSync, the corresponding program must be installed on the PDA.
   */
  @ReplacedByNativeOnDeploy
  private boolean register(int priority) {
    return true;
  }

  /**
   * This method must be called to remove a the conduit from the synchronization. Note that you cannot call it
   * directly, use the command line argument instead. Here is a sample:
   *
   * <pre>
   * TIConduit.exe /up
   * </pre>
   *
   */
  @ReplacedByNativeOnDeploy
  private boolean unregister() {
    return true;
  }

  private static String getClassName() {
    String name = getMainWindow().getClass().toString();
    int dot = name.lastIndexOf('.');
    if (dot < 0) {
      dot = name.lastIndexOf('/'); // desktop
    }
    if (dot >= 0) {
      name = name.substring(dot + 1);
    }
    return name;
  }

  /**
   * Enables or disables the conduit execution.
   * <p>
   * Note that disabling a conduit is NOT the same as unregistering.
   */
  public static void setSyncingEnabled(boolean enable) {
    try {
      Registry.set(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\EnableSync", getClassName(), enable ? 1 : 0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Returns whether the conduit is enabled or not. */
  public static boolean isSyncingEnabled() {
    try {
      return Registry.getInt(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\EnableSync", getClassName()) == 1;
    } catch (Exception e) {
      return false;
    }
  }

  /** Sets the conduit's position and size when it is opened.
   * You can remove the window from screen by using a x value like 10000.
   * Default values are -2,-2,240,320.
   * You should call this method in the onRegister method, inherited by your conduit.
   * <br>This is a sample that sets the screen size to be the same of "/scr palmhi" when running on the desktop:
   * <pre>
   * setConduitRect(-2,-2,320,320,false);
   * </pre>
   * @param x Absolute position on screen, or -1 to use default, or -2 to center.
   * @param y Absolute position on screen, or -1 to use default, or -2 to center.
   * @param w The width of the window (usually 240 or 320)
   * @param h The height of the window (usually 320)
   * @param config If true, you are passing the bounds for when the config is being called; if false, is the bounds used when synchronizing.
   * @since TotalCross 1.01
   */
  public static void setConduitRect(int x, int y, int w, int h, boolean config) // guich@tc110_27
  {
    try {
      Registry.set(Registry.HKEY_CURRENT_USER,
          config ? "Software\\TotalCross\\ConduitCfgRect" : "Software\\TotalCross\\ConduitSyncRect", getClassName(),
          x + "," + y + "," + w + "," + h);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Called when the conduit is registered. Placeholder, does nothing by default.
   * @since TotalCross 1.01
   */
  protected void onRegister() // guich@tc110_26
  {
  }

  /** Called when the conduit is unregistered. Placeholder, does nothing by default.
   * @since TotalCross 1.01
   */
  protected void onUnregister() // guich@tc110_26
  {
  }
}
