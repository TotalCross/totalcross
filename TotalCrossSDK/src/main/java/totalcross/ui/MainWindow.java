// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000-2012 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import java.util.ArrayList;
import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.firebase.FirebaseManager;
import totalcross.firebase.FirebaseMessagingService;
import totalcross.firebase.iid.FirebaseInstanceId;
import totalcross.firebase.iid.FirebaseInstanceIdService;
import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.io.IOException;
import totalcross.res.Resources;
import totalcross.sys.CharacterConverter;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.effect.UIEffects;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.UpdateListener;
import totalcross.ui.event.WindowListener;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.unit.UIRobot;
import totalcross.util.Hashtable;
import totalcross.util.IOUtils;
import totalcross.util.Vector;
import totalcross.util.concurrent.Lock;
import totalcross.util.zip.CompressedStream;
import totalcross.util.zip.ZLibStream;
import totalcross.util.zip.ZipEntry;
import totalcross.util.zip.ZipStream;

/**
 * MainWindow is the main window of a UI-based application.
 * <p>
 * All TotalCross programs with an user-interface must have <b>one and only one</b> main window.
 * <p>
 * Here is an example showing a basic application:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    Edit edName;
 *    public void initUI()
 *    {
 *       ... initialization code ...
 *       add(new Label("Name:"), LEFT,TOP+2);
 *       add(edName = new Edit(""), AFTER,SAME-2);
 *    }
 * }
 * </pre>
 */

public class MainWindow extends Window implements totalcross.MainClass {
  protected TimerEvent firstTimer;

  static MainWindow mainWindowInstance;
  private TimerEvent startTimer;
  private static int lastMinInterval;
  private boolean initUICalled;
  private static int timeAvailable;

  static Font defaultFont;
  private static Thread mainThread;
  private Lock runnersLock = new Lock();
  private Vector runners = new Vector(1);
  private ArrayList<UpdateListener> updateListeners = new ArrayList<UpdateListener>();
  private long lastUpdateCallTimestamp = 0;

  /** Constructs a main window with no title and no border. */
  public MainWindow() {
    this(null, NO_BORDER);
  }

  /** Constructs a main window with the given title and border style.
   * @see #NO_BORDER
   * @see #RECT_BORDER
   * @see #ROUND_BORDER
   * @see #TAB_BORDER
   * @see #TAB_ONLY_BORDER
   * @since SuperWaba 2.0b4
   */
  public MainWindow(String title, byte style) // guich@112
  {
    super(title, style);

    setX = 0;
    setY = 0;
    setW = Settings.screenWidth;
    setH = Settings.screenHeight;
    setFont = this.font;
    Settings.scrollDistanceOnMouseWheelMove = fmH;

    boolean isAndroid = Settings.platform.equals(Settings.ANDROID);
    boolean isIphone = Settings.isIOS();
    if (isAndroid || isIphone) {
      Settings.unmovableSIP = true;
    }

    // guich@tc120_48
    if (Settings.fingerTouch) {
      Settings.touchTolerance = fmH / 2;
    }

    // update some settings
    setBackColor(UIColors.controlsBack = Color.WHITE); // guich@200b4_39 - guich@tc100: set the controlsBack to this color

    uitip = new ToolTip(null, "");

    if (mainWindowInstance == null) {
      mainWindowInstance = this;
      mainWindowCreate();
      zStack.push(this); // guich
      topMost = this;
    }
    canDrag = false; // we can't drag the mainwindow.

    byte[] bytes = Vm.getFile("tcapp.prop");
    if (bytes != null) {
      Settings.appProps = new Hashtable(new String(bytes));
    }
    FirebaseManager.getInstance().registerFirebaseInstanceIdService(initFirebaseInstanceIdService());
    FirebaseManager.getInstance().setMessagingService(initFirebaseMessagingService());
  }

  /**
   * Register your own FirebaseInstanceIdService when initializing the app
   * @return
   */
  protected FirebaseInstanceIdService initFirebaseInstanceIdService() {
    return null;
  }

  /**
   * Register a FireBaseMessagingService when initializing the app
   */
  protected FirebaseMessagingService initFirebaseMessagingService() { return  new FirebaseMessagingService();}

  //$START:REMOVE-ON-SDK-GENERATION$   
  private static void sendStats() {
    try {
      try (File appcr4shedFile = new File(Convert.appendPath(Settings.appPath, "appCr4shed"), File.READ_WRITE)) {
        appcr4shedFile.delete();
      }
      Settings.abortedOnLastRun = true;
      // if appCrashed doesn't exists, its because it exitted normally
      final byte[] dconbytes = readDebugConsole();
      new Thread() {// app crashed exists, send report
        @Override
        public void run() {
          try {
            boolean createdBugRep = Settings.ANDROID.equals(Settings.platform)
                && Vm.exec("bugreport", null, 0, true) == 1;
            StringBuffer sb = new StringBuffer(128);
            sb.append("classname='").append(MainWindow.getMainWindow().getClass().getName()).append("'")
                .append(",platform='").append(Settings.platform).append("'").append(",wres=")
                .append(Settings.screenWidth).append(",hres=").append(Settings.screenHeight).append(",fonth=")
                .append(Settings.deviceFontHeight).append(",tcver='").append(Settings.versionStr).append('.')
                .append(Settings.buildNumber).append("'").append(",romver=").append(Settings.romVersion)
                .append(",deviceid='").append(Settings.deviceId).append("'").append(",activkey='")
                .append(Settings.activationKey).append("'");
            if (Settings.bugreportUser != null) {
              sb.append(",bruser='").append(Settings.bugreportUser).append("'");
            }
            if (Settings.applicationId != null) {
              sb.append(",appid='").append(Settings.applicationId).append("'");
            }
            if (Settings.romSerialNumber != null) {
              sb.append(",serial='").append(Settings.romSerialNumber).append("'");
            }
            if (Settings.imei != null) {
              sb.append(",imei='").append(Settings.imei).append("'");
            }
            if (Settings.appVersion != null) {
              sb.append(",appver='").append(Settings.appVersion).append("'");
            }
            if (Settings.bugreportEmail != null) {
              sb.append(",bremail='").append(Settings.bugreportEmail).append("'");
            }
            if (Settings.companyInfo != null) {
              sb.append(",compinfo='").append(Settings.companyInfo.replace(',', ';')).append("'");
            }
            byte[] info = Convert.getBytes(sb);
            totalcross.io.ByteArrayStream bas = new totalcross.io.ByteArrayStream(256);
            ZLibStream z = new ZLibStream(bas, CompressedStream.DEFLATE);
            z.writeBytes(info);
            z.close();
            final byte[] infobytes = bas.toByteArray();
            final byte[] bugrbytes;
            if (createdBugRep) {
              try (File test = new File("/sdcard/IssueReport/bugreport.zip")) {
                if (test.exists()) {
                  try (File asRead = new File("/sdcard/IssueReport/bugreport.zip", File.READ_WRITE);
                      ByteArrayStream readFromFile = new ByteArrayStream(asRead.getSize());) {
                    IOUtils.copy(asRead.asInputStream(), readFromFile.asOutputStream(), 4096);
                    bugrbytes = readFromFile.toByteArray();
                    asRead.delete();
                  }
                } else {
                  bugrbytes = new byte[0];
                }
              }
            } else {
              bugrbytes = new byte[0];
            }
            //HttpStream
            totalcross.net.HttpStream.Options options = new totalcross.net.HttpStream.Options();
            options.openTimeOut = 30000;
            options.readTimeOut = options.writeTimeOut = 60000;
            options.httpType = totalcross.net.HttpStream.POST;
            //options.postHeaders.put("Content-Type","application/octet-stream");
            options.postHeaders.put("Info-u-len", String.valueOf(info.length));
            options.postHeaders.put("Info-c-len", String.valueOf(infobytes.length));
            options.postHeaders.put("Content-Length",
                String.valueOf(bugrbytes.length + infobytes.length + dconbytes.length));
            options.postHeaders.put("Bugr-len", String.valueOf(bugrbytes.length));
            options.postHeaders.put("DCon-len", String.valueOf(dconbytes.length));
            for (int i = 0; i < 200; i++) {
              try (totalcross.net.HttpStream httpStream = new totalcross.net.HttpStream(
                  new totalcross.net.URI("http://www.superwaba.net/SDKRegistrationService/BugReportService"), options) {
                @Override
                protected void writeResponseRequest(StringBuffer sb, Options options) throws totalcross.io.IOException {
                  String str = sb.toString();
                  byte[] bytes = ((CharacterConverter) Convert.charsetForName("ISO-8859-1")).chars2bytes(str.toCharArray(), 0, sb.length());
                  writeBytes(bytes, 0, bytes.length);
                  // content length
                  writeBytes(infobytes, 0, infobytes.length);
                  if (bugrbytes.length > 0) {
                    writeBytes(bugrbytes, 0, bugrbytes.length);
                  }
                  if (dconbytes.length > 0) {
                    writeBytes(dconbytes, 0, dconbytes.length);
                  }
                }
              }) {
                break;
              } catch (IOException e) {
                Vm.sleep(60000);
              }
            }
          } catch (Throwable t) {
            if (Settings.buildNumber == 0) {
              t.printStackTrace();
              // ignore
            }
          }
        }
      }.start();
    } catch (OutOfMemoryError oome) {
    } catch (Throwable t) {// FileNotFound
    }
    try (File appCr4shed = new File(Convert.appendPath(Settings.appPath, "appCr4shed"), File.CREATE_EMPTY)) {
    } catch (Throwable t) {
      t.printStackTrace();
    } // restarting app
  }

  private static byte[] readDebugConsole() {
    String name = Convert.appendPath(Settings.appPath, "DebugConsole.txt");
    byte[] ret = new byte[0];
    try (File debug = new File(name, File.READ_ONLY)) {
      if (debug.getSize() == 0) {
        return new byte[0];
      }

      totalcross.io.ByteArrayStream bas = new totalcross.io.ByteArrayStream(debug.getSize() / 10);
      try (ZipStream zstream = new ZipStream(bas, CompressedStream.DEFLATE)) {
        zstream.putNextEntry(new ZipEntry("dc.z"));
        IOUtils.copy(debug.asInputStream(), zstream.asOutputStream(), 4096);
        zstream.closeEntry();
      }
      ret = bas.toByteArray();
    } catch (OutOfMemoryError oome) {
    } catch (FileNotFoundException fnfe) {
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (ret != null) {// if we read the file, erase it to prevent sending again with the same data
      Vm.debug(Vm.ERASE_DEBUG);
    }
    return ret;
  }
  //$END:REMOVE-ON-SDK-GENERATION$

  /** Returns true if this is the main thread.
   * @since TotalCross 2.0
   */
  public static boolean isMainThread() {
    return mainThread == Thread.currentThread();
  }

  void mainWindowCreate() {
    totalcross.Launcher.instance.registerMainWindow(this);
  }

  void mainWindowCreate4D() {
  } // not needed at device

  /** Sets the default font used in all controls created. To change the default font, assign it to this member in the MainWindow constructor,
   * making it the FIRST LINE in the constructor; you'll not be able to use super(title,border): change by setBorderStyle and setTitle, after
   * the defaultFont assignment. Example:
   * <pre>
   * public MyApp()
   * {
   *    MainWindow.setDefaultFont(Font.getFont(false, Font.NORMAL_SIZE+2));
   *    setBorderStyle(TAB_ONLY_BORDER);
   *    setTitle("My application");
   * }
   * </pre>
   * @since TotalCross 1.0 beta3
   */
  public static void setDefaultFont(Font newFont) {
    defaultFont = newFont;
    mainWindowInstance.setFont(newFont);
    uitip.setFont(newFont); // guich@tc100b5_58
    mainWindowInstance.setTitleFont(newFont.asBold()); // guich@tc125_4
    mainWindowInstance.titleGap = 0;

    // guich@tc120_48
    if (Settings.fingerTouch) {
      Settings.touchTolerance = newFont.fm.height / 2;
    }
  }

  /** Returns the default font.
   * @since TotalCross 1.0 beta3
   */
  public static Font getDefaultFont() {
    return defaultFont;
  }

  /** Changes the user interface style to the given one.
   * This method must be called in the MainWindow's constructor, and only once. E.g.:
   * <pre>
   * public class Foo extends MainWindow
   * {
   *    public Foo()
   *    {
   *       super("Hi bar",TAB_ONLY_BORDER);
   *       setUIStyle(totalcross.sys.Settings.Flat);
   * </pre>
   * Changing to Android style will also set Settings.fingerTouch to true.
   * If you don't like such behaviour in non finger devices, set this property to false after calling setUIStyle.
   *  
   * @see totalcross.sys.Settings#Flat
   * @see totalcross.sys.Settings#Vista
   * @see totalcross.sys.Settings#Android
   * @see totalcross.sys.Settings#Holo
   * @see totalcross.sys.Settings#Material
   * @since SuperWaba 5.05
   */
  public void setUIStyle(byte style) {
    Settings.uiStyle = style;
    if (style >= Settings.Android) {
      Settings.fingerTouch = true;
    }
    if (style == Settings.Material) {
      UIEffects.defaultEffect = UIEffects.Effects.MATERIAL;
    }
    Control.uiStyleChanged();
    Resources.uiStyleChanged();
    if (uiAndroid) {
      androidBorderThickness = Settings.screenWidth <= 320 ? 1 : Settings.screenWidth <= 640 ? 2 : 3;
      borderGaps[ROUND_BORDER] = androidBorderThickness == 3 ? 3 : 2;
    }
  }

  /**
   * Notifies the application that it should stop executing and exit. It will
   * exit after executing any pending events. If the underlying system supports
   * it, the exitCode passed is returned to the program that started the app.
   * Note: On AppletViewer/Browser the exitCode is useless.
   * Note 2: On Android, you can exit softly by using SOFT_EXIT as the exit code. 
   * <p>If you want your code to be called when the VM exits, extend the onExit method.
   * @see #onExit
   */
  @ReplacedByNativeOnDeploy
  public static final void exit(int exitCode) {
    totalcross.Launcher.instance.exit(exitCode);
  }

  /**
   * Notifies the application that it should be minimized, that is, transfered
   * to the background. Whenever the application is minimized, the following call back function
   * will be called: {@link #onMinimize()}. Note: On Android, calling {@link #minimize()} will
   * pause the application execution and it can only be restored manually by the user. This method is
   * also supported on Windows 32.
   * @see #onMinimize
   * @see #onRestore
   * @since TotalCross 1.10
   */
  @ReplacedByNativeOnDeploy
  public static void minimize() // bruno@tc110_89
  {
    totalcross.Launcher.instance.minimize();
  }

  /**
   * Notifies the application that it should be restored, that is, transfered
   * to the foreground. 
   * Whenever the application is restored, the following call back function will be called:
   * {@link #onRestore()}. Note: This method is supported on Android but the user must restore
   * the application manually. This method is also supported on Windows 32.
   * @since TotalCross 1.10
   */
  @ReplacedByNativeOnDeploy
  public static void restore() // bruno@tc110_89
  {
    totalcross.Launcher.instance.restore();
  }

  /** 
   * Returns the instance of the current main window. You can use it to get access to methods of the <code>MainWindow</code> class from outside the class. 
   * It is also possible to cast the returned class to the class that is extending <code>MainWindow</code> (this is a normal Java behavior). 
   * So, if UiGadgets is running, it is correct to do:
   * <pre>
   * UIGadgets instance = (UIGadgets)MainWindow.getMainWindow();
   * </pre>
   */
  public static MainWindow getMainWindow() {
    return mainWindowInstance;
  }

  /**
   * Adds a timer to a control. This method is protected, the public
   * method to add a timer to a control is the addTimer() method in
   * the Control class. The Timer event will be issued to the target every millis milliseconds.
   */
  protected TimerEvent addTimer(Control target, int millis) {
    TimerEvent t = new TimerEvent();
    addTimer(t, target, millis);
    return t;
  }

  /**
   * Adds the timer t to the target control. This method is protected, the public
   * method to add a timer to a control is the addTimer() method in
   * the Control class. The Timer event will be issued to the target every millis milliseconds.
   */
  protected void addTimer(TimerEvent t, Control target, int millis) {
    addTimer(t, target, millis, true);
  }

  /**
   * Adds the timer t to the target control. This method is protected, the public
   * method to add a timer to a control is the addTimer() method in
   * the Control class. The Timer event will be issued to the target every millis milliseconds.
   */
  protected void addTimer(TimerEvent te, Control target, int millis, boolean append) {
    te.target = target;
    te.millis = millis;
    te.lastTick = Vm.getTimeStamp();
    if (firstTimer == null) // first timer to be added
    {
      te.next = null;
      firstTimer = te;
    } else if (append) // appending timer to the end of the list
    {
      TimerEvent last = null;
      for (TimerEvent t = firstTimer; t != null; t = t.next) {
        if (t == te) {
          // already inserted? get out!
          return;
        }
        last = t;
      }
      if (last != null) {
        last.next = te;
      }
      te.next = null;
    } else // inserting timer to the beginning of the list
    {
      te.next = firstTimer;
      firstTimer = te;
    }
    setTimerInterval(1); // forces a call to _onTimerTick inside the TC Event Thread
  }

  /**
   * Removes the given timer from the timers queue. This method returns true if the timer was found
   * and removed and false if the given timer could not be found.
   * The <code>target</code> member is set to null.
   */
  @Override
  public boolean removeTimer(TimerEvent timer) {
    if (timer == null) {
      return false;
    }
    TimerEvent t = firstTimer;
    TimerEvent prev = null;
    while (t != timer) {
      if (t == null) {
        return false;
      }
      prev = t;
      t = t.next;
    }
    if (prev == null) {
      firstTimer = t.next;
    } else {
      prev.next = t.next;
    }

    // not already removed?
    if (timer.target != null) {
      setTimerInterval(1); // forces a call to _onTimerTick inside the TC Event Thread
    }
    timer.target = null; // guich@tc120_46
    return true;
  }

  /** Removes any timers that belongs to this window or whose paren't is null */
  void removeTimers(Window win) {
    boolean changed;
    do {
      changed = false;
      TimerEvent t = firstTimer;
      while (t != null) {
        Control c = (Control) t.target;
        Window w = c.getParentWindow();
        if (w == null || w == win) {
          changed = true;
          removeTimer(t);
          break;
        }
        t = t.next;
      }
    } while (changed);
  }

  /** Called by the VM when the application is starting. Setups a
   * timer that will call initUI after the event loop is started.
   * Never call this method directly; this method is not private
   * to prevent the compiler from removing it during optimization.
   * The timeAvail parameter is passed by the vm to show how much
   * time the user have to keep testing the demo vm. Even if this
   * value is not shown to the user, it is internally computed and 
   * the vm will exit when the counter reaches 0.
   */
  @Override
  final public void appStarting(int timeAvail) // guich@200b4_107 - guich@tc126_46: added timeAvail parameter to show MessageBox from inside here.
  {
    mainThread = Thread.currentThread();
    timeAvailable = timeAvail;
    gfx = new Graphics(this); // revalidate the pixels
    startTimer = addTimer(1); // guich@567_17
  }

  static boolean quittingApp;

  /** Called by the system so we can finish things correctly.
   * Never call this method directly; this method is not private
   * to prevent the compiler from removing it during optimization.
   */
  @Override
  final public void appEnding() // guich@200final_11: fixed when switching apps not calling killThreads.
  {
    if (!Settings.onJavaSE) {
      try {
        new File(Convert.appendPath(Settings.appPath, "appCr4shed"), File.READ_WRITE).delete();
      } catch (Throwable t) {
      } // finished fine
    }
    quittingApp = true;
    // guich@tc100: do this at device side - if (resetSipToBottom) setStatePosition(0, Window.VK_BOTTOM); // fixes a problem of the window of the sip not correctly being returned to the bottom

    // guich@tc126_46: don't call app's onExit if time expired, since initUI was not called.
    if (initUICalled) {
      onExit(); // guich@200b4_85
    }
  }

  final protected void _onMinimize() {
    if (!Settings.onJavaSE) {
      try {
        new File(Convert.appendPath(Settings.appPath, "appCr4shed"), File.READ_WRITE).delete();
      } catch (Throwable t) {
      } // finished fine
    }
    onMinimize();
  }

  final protected void _onRestore() {
    if (!Settings.onJavaSE) {
      try {
        new File(Convert.appendPath(Settings.appPath, "appCr4shed"), File.CREATE_EMPTY).close();
      } catch (Throwable t) {
      } // restarting app
    }
    onRestore();
  }

  /**
   * Called just before an application exits.
   * When this is called, all threads are already killed.
   * You should return from this method as soon as possible, because the OS can kill the application if
   * it takes too much to return.
   * 
   * Note that on Windows Phone this method is NEVER called.
   */
  public void onExit() {
  }

  /**
   * Called just after the application is minimized. 
   * 
   * If the user press the home key and then forces the application to stop (by going to the Settings / Applications), then
   * all Litebase tables may be corrupted (actually, no data is lost, but a TableNotClosedException will be issued). So, its a good
   * thing to call LitebaseConnection.closeAll in your litebase instances and recover them in the onRestore method.
   * <br><br>
   * When the onMinimize is called, the screen will only be able to be updated after it resumes (in other words,
   * calling repaint or repaintNow from the onMinimize method has no effect).
   * 
   * On Windows Phone, the onMinimize is called and, if the user don't call the application again
   * within 10 seconds, the application is KILLED without notifications. So, you should save all your application's state
   * in this method and restore it in the onRestore method.
   * @see #minimize()
   * @since TotalCross 1.10
   */
  public void onMinimize() // bruno@tc110_89 - bruno@tc122_31: now supported on wince and win32
  {
  }

  /**
   * Called just after the application is restored. 
   * 
   * @see #onRestore()
   * @since TotalCross 1.10
   */
  public void onRestore() // bruno@tc110_89 - bruno@tc122_31: now supported on wince and win32
  {
  }

  private void startProgram() {
    //$START:REMOVE-ON-SDK-GENERATION$
    if (!Settings.onJavaSE && (Settings.applicationId == null
        || (!Settings.applicationId.equals("HmG4") && !Settings.applicationId.equals("DetM")))) {
      sendStats();
    }
    //$END:REMOVE-ON-SDK-GENERATION$
    initUICalled = Window.needsPaint = true;
    initUI();
    Window.needsPaint = Graphics.needsUpdate = true; // required by device
    started = true; // guich@567_17: moved this from appStarting to here to let popup check if the screen was already painted
    repaintActiveWindows();
    // start a robot if one is passed as parameter
    String cmd = getCommandLine();
    if (cmd != null && cmd.endsWith(".robot")) {
      try {
        new UIRobot(cmd + " (cmdline)");
      } catch (Exception e) {
        MessageBox.showException(e, true);
      }
    }
    if (cmd != null && cmd.equals("/pushnotification")) {
      postPushNotifications();
    }
    
    // The application always has to have a Timer for the Update to work
    this.addTimer(1000);
  }

  /**
   * Called by the VM to process timer interrupts. This method is not private
   * to prevent the compiler from removing it during optimization.
   */
  @Override
  final public void _onTimerTick(boolean canUpdate) {
    if (startTimer != null) // guich@567_17
    {
      TimerEvent t = startTimer;
      startTimer = null; // removeTimer calls again onTimerTick, so we have to null out this before calling it
      removeTimer(t);
      if (timeAvailable >= 0 && Settings.platform.equals(Settings.WIN32)
          && (Settings.romSerialNumber == null || Settings.romSerialNumber.length() == 0)) {
        new MessageBox("Fatal Error",
            "Failed to retrieve a unique device identification to activate the TotalCross VM. Please check your network settings and activate any disabled networks.")
                .popup();
        exit(0);
        return;
      }
      //$START:REMOVE-ON-SDK-GENERATION$
      if (timeAvailable == -999998) {
        Settings.activationId = "NO ACTIVATION";
        startProgram();
      } else if (timeAvailable == -999999)
        try {
          timeAvailable = -1;
          Window w = (Window) Class.forName("ras.ui.ActivationWindow").newInstance();
          w.addWindowListener(new WindowListener() {

            @Override
            public void windowClosed(ControlEvent e) {
              startProgram();
            }
          });
          w.popupNonBlocking();
        } catch (Exception e) {
          Vm.alert("Fatal error: " + e.getMessage() + ". Exiting...");
          exit(1);
          return;
        } else 
        //$END:REMOVE-ON-SDK-GENERATION$
        startProgram();
    }
    int minInterval = 0;
    TimerEvent timer = firstTimer;

    
    long timestamp = Vm.getTimeStamp();
    int elapsedMilliseconds = (int)(timestamp - lastUpdateCallTimestamp);
    if (elapsedMilliseconds >= Settings.minimalUpdateInterval) {
       // Reverse-for allows for the safe removal of updateListeners during the update event
       for (int i = updateListeners.size() - 1; i >= 0; i--) {
          updateListeners.get(i).updateListenerTriggered(elapsedMilliseconds);
       }
       lastUpdateCallTimestamp = timestamp;
    }
    
    
    while (timer != null) {
      if (timer.target == null) // aleady removed but still in the queue?
      {
        TimerEvent t = timer.next;
        removeTimer(timer);
        timer = t != null ? t.next : null;
        continue;
      }
      int now = Vm.getTimeStamp(); // guich@tc100b4
      int diff = now - timer.lastTick;
      if (diff < 0) {
        diff += (1 << 30); // wrap around - max stamp is (1 << 30)
      }
      int interval;
      if (diff >= timer.millis) {
        // post TIMER event
        timer.triggered = true; // guich@220_39
        ((Control) timer.target).postEvent(timer);
        timer.triggered = false;
        timer.lastTick = now;
        interval = timer.millis;
      } else {
        interval = timer.millis - diff;
      }
      if (interval < minInterval || minInterval == 0) {
        minInterval = interval;
      }
      timer = timer.next;
    }

    // guich@tc100: call only if there's a timer to run
    if (minInterval > 0 || lastMinInterval > 0) {
      setTimerInterval(lastMinInterval = minInterval);
    }
    // run everything that needs to run on main thread
    Object[] runners = getRunners();
    if (runners != null) {
      Control.enableUpdateScreen = false; // we'll update the screen below
      for (int i = 0; i < runners.length; i++) {
        ((Runnable) runners[i]).run();
      }
      Control.enableUpdateScreen = true;
    }

    // guich@200b4_1: corrected the infinit repaint on popup windows
    if (Window.needsPaint) {
      repaintActiveWindows(); // already calls updateScreen
    } else if (canUpdate && Graphics.needsUpdate) {
      // guich@tc100: make sure that any pending screen update is committed. - if not called from addTimer/removeTimer (otherwise, an open combobox will flicker)
      safeUpdateScreen();
    }
  }

  @ReplacedByNativeOnDeploy
  void setTimerInterval(int n) {
    totalcross.Launcher.instance.setTimerInterval(n < Settings.minimalUpdateInterval ? n : Settings.minimalUpdateInterval);
  }

  /** Returns the command line passed by the application that called this application in the Vm.exec method.
   * 
   * In Android, you can start an application using adb:
   * <pre>
   * adb shell am start -a android.intent.action.MAIN -n totalcross.app.uigadgets/.UIGadgets -e cmdline "Hello world"
   * </pre>
   * In the sample above, we're starting UIGadgets. Your app should be: totalcross.app.yourMainWindowClass/.yourMainWindowClass
   *
   * Note: When you click on the application's icon, there's no command line.
   */
  @ReplacedByNativeOnDeploy
  final public static String getCommandLine() // guich@tc120_8: now is static
  {
    return totalcross.Launcher.instance.commandLine;
  }

  /** This method can't be called for a MainWindow */
  @Override
  public void setRect(int x, int y, int width, int height, Control relative, boolean screenChanged) // guich@567_19
  {
    // no messages, please. just ignore
  }

  /** Takes a screen shot of the current screen. Since TotalCross 3.06, it uses Control.takeScreenShot. 
   * Here's a sample:
   * <pre>
   * Image img = MainWindow.getScreenShot();
   * File f = new File(Settings.appPath + "/screen.png", File.CREATE_EMPTY);
   * img.createPng(f);
   * f.close();
   * </pre>
   * Note that the font varies from device to device and even to desktop. So, if you want to compare a device's
   * screen shot with one taken at desktop, be sure to set the default font in both to the same, like using
   * <code>setDefaultFont(Font.getFont(false,20))</code>.
   * 
   * @since TotalCross 1.3
   */
  public static Image getScreenShot() {
    try {
      // get main window
      mainWindowInstance.takeScreenShot();
      Image img = mainWindowInstance.offscreen;
      mainWindowInstance.releaseScreenShot();
      // now paint other windows
      int lastFade = 1000;
      for (int j = 0, n = zStack.size(); j < n; j++) {
        if (((Window) zStack.items[j]).fadeOtherWindows) {
          lastFade = j;
        }
      }
      for (int i = 0, n = zStack.size(); i < n; i++) // repaints every window, from the nearest with the MainWindow size to last parent
      {
        if (i == lastFade) {
          img.applyFade(fadeValue);
        }
        if (i > 0 && zStack.items[i] != null) {
          Window w = (Window) zStack.items[i];
          Graphics g = img.getGraphics();
          g.translate(w.x, w.y);
          w.paint2shot(g, true);
        }
      }
      //img.lockChanges();
      return img;
    } catch (Throwable e) {
    }
    return null;
  }

  // stuff to let a thread update the screen
  private Object[] getRunners() {
    Object[] o = null;
    synchronized (runnersLock) {
      try {
        int n = runners.size();
        if (n != 0) {
          o = runners.toObjectArray();
          runners = new Vector(1);
        }
      } catch (Exception e) {
      }
    }
    return o;
  }
  
  
  /** Adds a listener for Update events. 
   * @see totalcross.ui.event.UpdateListener 
   */ 
  public void addUpdateListener(UpdateListener listener) { 
     updateListeners.add(listener); 
  } 
  /** Removes a listener for Update events 
   * @see totalcross.ui.event.UpdateEventListener 
   * @since TotalCross 5.00 
   */ 
  public void removeUpdateListener(UpdateListener listener) { 
     updateListeners.remove(listener); 
  } 

  /** The same of <code>runOnMainThread(r, true)</code>. 
   * @see #runOnMainThread(Runnable, boolean) 
   * Note that this
   * */
  public void runOnMainThread(Runnable r) {
    runOnMainThread(r, true);
  }

  /** Runs the given code in the main thread. As of TotalCross 2.0, a thread cannot update the screen.
   * So, asking the code to be called in the main thread solves the problem. Of course, this call is asynchronous, ie,
   * the thread may run again before the screen is updated. This method is thread-safe.
   * If singleInstance is true and the array contains an element of this class, it is replaced by the given one.
   * Note that passing as false may result in memory leak.
   * Sample:
   * <pre>
   *  new Thread()
         {
            public void run()
            {
               while (true)
               {
                  Vm.sleep(1000);
                  MainWindow.getMainWindow().runOnMainThread(new Runnable()
                  {
                     public void run()
                     {
                        log("babi "+ ++contador);
                     }
                  });
               }
            }
         }.start();
   * </pre>
   * @since TotalCross 2.1
   */
  public void runOnMainThread(Runnable r, boolean singleInstance) {
    synchronized (runnersLock) {
      try {
        if (singleInstance && runners.size() > 0) {
          String origname = r.getClass().getName() + "@";
          for (int i = 0, n = runners.size(); i < n; i++) {
            if (runners.items[i].toString().startsWith(origname)) {
              runners.removeElementAt(i);
              break;
            }
          }
        }
        runners.addElement(r);
        setTimerInterval(1);
      } catch (Exception e) {
      }
    }
    Thread.yield();
  }
}
