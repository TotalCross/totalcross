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

package totalcross.unit;

import totalcross.io.DataStreamLE;
import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.sys.SpecialKeys;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.ListBox;
import totalcross.ui.MainWindow;
import totalcross.ui.MultiListBox;
import totalcross.ui.Window;
import totalcross.ui.dialog.ControlBox;
import totalcross.ui.dialog.InputBox;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.Comparable;
import totalcross.util.Date;
import totalcross.util.ElementNotFoundException;
import totalcross.util.IntVector;
import totalcross.util.Random;
import totalcross.util.Vector;
import totalcross.util.concurrent.Lock;

/** This class permits the control of the User Interface, playing back events recorded
 * by the user.
 * 
 * The robot is comprised of some dialogs that are invoked using a special key defined by the application.
 * The special key running in Java SE (eclipse, netbeans) is control+1. The code below defines the 
 * Find special key to be used at the device (you can choose any special key you want):
 * <pre>
 * Vm.interceptSpecialKeys(new int[]{SpecialKeys.FIND});
 * Settings.deviceRobotSpecialKey = SpecialKeys.FIND;
 * </pre>
 * You should set it at the application's constructor. 
 *
 * When this key is pressed, it opens a window with three options: record, playback and cancel.
 * 
 * Clicking on the "record" button opens another window asking for the robot's name. Type the name
 * and press "Start record" to start recording. Do the events smoothly and slowly. When done, press the special
 * key again.
 * 
 * Clicking in the playback button opens a screen with a list of recorded robots.
 *  
 * After selecting the robots, press one of these buttons:
 * <ul>
 * <li> Play selected: plays the robots in the same order you selected (the order is recorded as you select).
 * <li> Play all: plays all robots in the list, in the order they appear.
 * <li> Play random: randomizes the selected robots order and reproduce that. A new window is opened asking you
 * to enter the number of times that exact sequence will be run.
 * <li> Dump contents: dumps the contents of the selected robots, so you can see the events that are played back.
 * <li> Delete selected: delete the selected robots.
 * </ul> 
 * Robots are saved in a file with ".robot" extension at the application's path. Since the robot saves absolute
 * pen events at a specific time, they are not portable among different device resolutions and/or devices with 
 * different processors.
 * 
 * You can start a specific robot passing its name as commandline to the application.
 * For example, in:
 * <ul>
 *  <li> Android: adb shell am start -a android.intent.action.MAIN -n totalcross.app.uigadgets/.UIGadgets -e cmdline test1.robot
 *  <li> Windows 32: UIGadgets.exe /cmdline test1.robot
 *  <li> J2SE: /cmdline flick.robot tc.samples.ui.gadgets.UIGadgets
 * </ul>
 * 
 * Note that no other commandline parameters must be passed to the application; the UIRobot expects that the only parameter
 * will be the robot's name.
 * 
 * In JavaSE (desktop), you can skip the robot start by launching the application and pressing the SHIFT key while the "Starting ..."  
 * MessageBox is displayed. This is useful if you want to record the robot again whithout having to remove it from the commandline.
 * The shift key also aborts the robot during its execution. 
 * 
 * When the robot finishes, it takes a screenshot of the application. When it plays back, it compares the 
 * screen with the saved one and sends one of these two UIRobotEvent to the MainWindow: ROBOT_SUCCEED (if comparison succeeds) or
 * ROBOT_FAILED (if comparison fails).
 * 
 * You can create a log file with the results by putting this in the onEvent of your MainWindow class:
 * <pre>
 *  if (event.type == UIRobotEvent.ROBOT_FAILED || event.type == UIRobotEvent.ROBOT_SUCCEED)
 *  {
 *     try
 *     {
 *        File f = new File(Settings.appPath+"/robot.log",File.CREATE_EMPTY);
 *        String s = event.type == UIRobotEvent.ROBOT_FAILED ? "FAILED" : "SUCCEED";
 *        f.writeBytes("Robot "+UIRobot.robotFileName+" "+s+". Running time: "+UIRobot.totalTime);
 *        f.close();
 *     }
 *     catch (Exception e) {e.printStackTrace();}
 *  }
 * </pre> 
 * 
 * @see totalcross.sys.SpecialKeys
 * @see UIRobotEvent
 */
public class UIRobot {
  private static final String TITLE = " User Interface Robot ";
  public static final int IDLE = 0;
  public static final int RECORDING = 1;
  public static final int PLAYBACK = 2;
  public static int status;

  private static MainWindow mw = MainWindow.getMainWindow();
  private int lastTS;
  private File flog;
  private DataStreamLE fds;
  private int counter;
  private boolean autolaunch;

  /** Set to true to abort the run of the current UIRobot. Useful if you set a breakpoint
   * at a code and want to abort the run.
   * 
   * You can also abort the UIRobot by pressing the shift key during execution and at the starting message box.
   */
  public static boolean abort;

  /** The filename of the running robot. */
  public static String robotFileName;
  /** The amount of time since the robot started. */
  public static int totalTime;

  private static String[] recordedRobots;

  /** Constructs a new UIRobot and opens the user interface. 
   */
  public UIRobot() throws Exception {
    autolaunch = false;
    if (status != IDLE) {
      throw new Exception("Already running");
    }
    // get from user if he wants to record or playback
    switch (showMessage("Please select the action:", new String[] { "Record", "Playback", "Cancel" }, 0, false)) {
    case 0:
      record();
      break;
    case 1:
      playback();
      break;
    }
  }

  /** Constructs a new UIRobot and starts the playback of the given file. */
  public UIRobot(String robotFileName) throws Exception {
    autolaunch = true;
    recordedRobots = new String[] { robotFileName };
    play(new int[] { 0 }, 1, false, 1);
  }

  private void record() throws Exception {
    InputBox ib = new InputBox(TITLE, "Please type the robot name:", "", new String[] { "Start record", "Cancel" });
    ib.setBackForeColors(Color.ORANGE, 1);
    ib.buttonKeys = new int[] { SpecialKeys.ENTER, SpecialKeys.ESCAPE };
    ib.popup();
    if (ib.getPressedButtonIndex() == 1) {
      throw new Exception("Cancelled");
    }
    String name = ib.getValue() + ".robot";
    flog = new File(robotFileName = Settings.appPath + "/" + name, File.CREATE_EMPTY);
    fds = new DataStreamLE(flog);
    lastTS = Vm.getTimeStamp();
    status = RECORDING;
  }

  private void playback() throws Exception {
    if (recordedRobots == null) {
      fillListOfRecordedRobots();
    }
    if (recordedRobots == null) {
      showMessage("No robots found.", null, 1500, false);
      throw new Exception("No robots");
    }
    MultiListBox lb = new MultiListBox(recordedRobots);
    lb.setOrderIsImportant(true);
    ControlBox cb = new ControlBox(TITLE, "Select the robots in the\nsequence you want to run.", lb, Control.FILL,
        Control.FIT,
        new String[] { "Play selected", "Play all", "Play random", "Dump contents", "Delete selected", "Cancel" }, 2);
    cb.setBackForeColors(Color.ORANGE, 1);
    cb.popup();
    IntVector order = lb.getSelectedIndexes();
    int n = order.size();
    int sel = cb.getPressedButtonIndex();
    switch (sel) {
    case 0: // play selected
      play(order.items, n, false, 1);
      break;
    case 1: // play all
      play(null, recordedRobots.length, false, 1);
      break;
    case 2: // play random
    {
      // get the number of repetitions
      InputBox ib = new InputBox(TITLE, "Type the number of runs", "1");
      ib.getEdit().setValidChars(Edit.numbersSet);
      ib.setBackForeColors(Color.ORANGE, 1);
      ib.popup();
      if (ib.getPressedButtonIndex() == 1) {
        throw new Exception("Cancelled");
      }
      String countStr = ib.getValue();
      int repeat;
      try {
        repeat = Convert.toInt(countStr);
      } catch (InvalidNumberException ine) {
        showMessage("Invalid number, operation cancelled.", null, 1500, true);
        throw new Exception("Cancelled");
      }
      // fills an array with all indexes and them swap them randomly
      int[] s = order.items;
      Random r = new Random();
      for (int i = n * 3; --i >= 0;) {
        int idx1 = r.between(0, n - 1);
        int idx2 = r.between(0, n - 1);
        if (idx1 != idx2) {
          int temp = s[idx1];
          s[idx1] = s[idx2];
          s[idx2] = temp;
        }
      }
      play(s, n, false, repeat);
      break;
    }
    case 3: // dump
      play(order.items, n, true, 1);
      break;
    case 4: // delete
      if (showMessage("Do you want to delete the selected robots?", new String[] { "No", "Yes" }, 0, true) == 1) {
        for (int i = 0; i < n; i++) {
          try {
            String item = recordedRobots[order.items[i]];
            String fileName = item.substring(0, item.indexOf(' '));
            new File(Settings.appPath + "/" + fileName, 1).delete();
          } catch (Exception ee) {
            ee.printStackTrace();
          }
        }
        fillListOfRecordedRobots();
      }
      break;
    }
  }

  private Vector threadPool = new Vector(10);
  private Lock tpLock;

  private PostThread popThread() {
    try {
      synchronized (tpLock) {
        return (PostThread) threadPool.pop();
      }
    } catch (ElementNotFoundException enfe) {
      PostThread t = new PostThread();
      t.start();
      return t;
    }
  }

  private void pushThread(PostThread t) {
    synchronized (tpLock) {
      threadPool.push(t);
    }
  }

  private class PostThread extends Thread {
    boolean running;
    int type, key, x, y, mods;
    Lock l;

    public PostThread() {
      l = new Lock();
    }

    public void set(int type, int key, int x, int y, int mods) {
      this.key = key;
      this.x = x;
      this.y = y;
      this.mods = mods;
      synchronized (l) {
        this.type = type;
      }
    }

    public void kill() {
      running = false;
    }

    @Override
    public void run() {
      running = true;
      while (running) {
        int type;
        synchronized (l) {
          type = this.type;
        }
        if (type == 0) {
          Thread.yield();
        } else {
          mw._postEvent(type, key, x, y, mods, 0);
          this.type = 0;
          pushThread(this);
        }
      }
    }
  }

  private void play(final int[] items, final int n, final boolean dump, final int repeat) {
    if (tpLock == null) {
      tpLock = new Lock();
    }
    new Thread() {
      @Override
      public void run() {
        String fileName = "";
        try {
          ListBox lb = null;
          ControlBox cb = null;
          if (!dump) {
            status = PLAYBACK;
          } else {
            lb = new ListBox();
            lb.enableHorizontalScroll();
            cb = new ControlBox(TITLE, "Robot dump", lb, Control.FILL, Control.FIT, new String[] { "Ok" });
            cb.setBackForeColors(Color.ORANGE, 1);
          }
          abort = false;
          for (int r = 1; !abort && r <= repeat && (dump || status == PLAYBACK); r++) {
            for (int i = 0; i < n && (dump || status == PLAYBACK); i++) {
              abort = false;
              String item = recordedRobots[items == null ? i : items[i]];
              fileName = item.substring(0, item.indexOf(' '));
              File f = new File(fileName.indexOf('/') <= 0 ? Settings.appPath + "/" + fileName : fileName,
                  File.READ_WRITE);
              robotFileName = f.getPath();
              DataStreamLE ds = new DataStreamLE(f);
              String st = "Starting " + fileName;
              if (repeat > 1) {
                st += " (run " + r + " of " + repeat + ")";
              }
              Vm.debug(st);
              if (!dump) {
                showMessage(st, null, 1500, false);
                if (Vm.isKeyDown(SpecialKeys.SHIFT)) {
                  abort = true;
                }
              } else {
                lb.add(st);
              }
              totalTime = 0;
              for (int j = 0; !abort && (dump || status == PLAYBACK); j++) {
                int type = ds.readInt();
                int key = ds.readInt();
                int x = ds.readInt();
                int y = ds.readInt();
                int mods = ds.readInt();
                int delay = ds.readInt();
                totalTime += delay;
                if (!dump && delay > 0) {
                  Vm.sleep(delay);
                }
                if (Vm.isKeyDown(SpecialKeys.SHIFT)) {
                  abort = true;
                  break;
                }
                if (dump || Settings.onJavaSE) {
                  String s = dumpEvent(j, type, key, x, y, mods, delay);
                  if (!dump && Settings.onJavaSE) {
                    Vm.debug(s);
                  }
                  if (lb != null) {
                    lb.add(s);
                  }
                }
                if (type == UIRobotEvent.ROBOT_EOF) {
                  break;
                }
                if (!dump) {
                  PostThread pt = popThread();
                  pt.set(type, key, x, y, mods);
                }
              }
              f.close();
              if (!dump && !abort) {
                Vm.sleep(1000);
                Window.repaintActiveWindows();
                try {
                  boolean ok = compareScreenShots(fileName);
                  showMessage("Finished " + fileName + ".\nScreenshot comparison " + (ok ? "succeed" : "FAILED"), null,
                      ok ? 1500 : 2500, !ok);
                } catch (FileNotFoundException fnfe) {
                  showMessage("Finished " + fileName + ".\nScreenshot not found", null, 1500, false);
                  Vm.debug("One of the image files was not found during robot test comparison.");
                }
              } else if (dump) {
                lb.add("====================");
              }
            }
          }
          if (dump) {
            cb.popup();
          } else {
            // kill all tasks in the thread pool
            for (int i = threadPool.size(); --i >= 0;) {
              ((PostThread) threadPool.items[i]).kill();
            }
            threadPool.removeAllElements();
            if (!abort) {
              Vm.sleep(500); // give a time so all can get killed
            } else if (Settings.onJavaSE) {
              Vm.debug("UIRobot ABORTED");
            }
            status = IDLE;
            abort = false;
          }
        } catch (Exception e) {
          e.printStackTrace();
          status = IDLE;
          robotFailed(fileName, "Exception thrown: " + e);
        }
        if (autolaunch) {
          recordedRobots = null;
        }
      }
    }.start();
  }

  private String formatTime(int t) {
    int ms = t % 1000;
    t /= 1000;
    int s = t % 60;
    t /= 60;
    int m = t % 60;
    t /= 60;
    int h = t % 24;
    StringBuffer sb = new StringBuffer(20);
    if (h > 0) {
      sb.append(h).append('h');
    }
    if (h > 0 || m > 0) {
      sb.append(m).append('m');
    }
    if (h > 0 || m > 0 || s > 0) {
      sb.append(s).append('s');
    }
    if (ms < 100) {
      sb.append('0');
    }
    if (ms < 10) {
      sb.append('0');
    }
    sb.append(ms).append("ms");
    return sb.toString();
  }

  private void robotFailed(String fileName, String reason) {
    Vm.debug("robot " + fileName + " failed");
    UIRobotEvent ev = new UIRobotEvent(UIRobotEvent.ROBOT_FAILED, fileName, reason);
    MainWindow.getMainWindow().postEvent(ev);
    if (!ev.consumed && listeners != null) {
      for (int i = listeners.size(); --i >= 0 && !ev.consumed;) {
        ((UIRobotListener) listeners.items[i]).robotFailed(ev);
      }
    }
  }

  private void robotSucceed(String fileName) {
    Vm.debug("robot " + fileName + " succeed");
    UIRobotEvent ev = new UIRobotEvent(UIRobotEvent.ROBOT_SUCCEED, fileName, null);
    MainWindow.getMainWindow().postEvent(ev);
    if (!ev.consumed && listeners != null) {
      for (int i = listeners.size(); --i >= 0 && !ev.consumed;) {
        ((UIRobotListener) listeners.items[i]).robotSucceed(ev);
      }
    }
  }

  private void saveScreenShot(String fileName, boolean recording) throws IOException, ImageException {
    fileName = getScreenShotName(fileName, recording);
    File f = new File(fileName, File.CREATE_EMPTY);
    Image img = MainWindow.getScreenShot();
    img.createPng(f);
    f.close();
  }

  private String getScreenShotName(String fileName, boolean recording) {
    return fileName.substring(0, fileName.length() - 6) + (recording ? "_rec" : "_play") + ".png";
  }

  private static byte[] cmpbuf1, cmpbuf2;

  private boolean compareScreenShots(String fileName) throws FileNotFoundException, IOException, ImageException {
    saveScreenShot(fileName, false);
    // compare the recorded and playback images
    if (cmpbuf1 == null) {
      cmpbuf1 = new byte[2048];
      cmpbuf2 = new byte[2048];
    }
    String rec = getScreenShotName(fileName, true);
    String ply = getScreenShotName(fileName, false);
    File frec = new File(rec, File.READ_WRITE);
    File fply = new File(ply, File.READ_WRITE);
    byte[] cmp1 = cmpbuf1;
    byte[] cmp2 = cmpbuf2;
    int sr = frec.getSize();
    int sp = fply.getSize();
    boolean same = sr == sp;
    while (same) {
      int r1 = frec.readBytes(cmp1, 0, cmp1.length);
      int r2 = fply.readBytes(cmp2, 0, cmp2.length);
      if (r1 <= 0) {
        break;
      }
      if (r1 != r2) {
        same = false;
      } else {
        while (--r1 >= 0) {
          if (cmp1[r1] != cmp2[r1]) {
            same = false;
          }
        }
      }
    }
    frec.close();
    fply.close();
    if (same) {
      robotSucceed(fileName);
    } else {
      robotFailed(fileName, "Screenshots don't match");
    }
    return same;
  }

  private String dumpEvent(int j, int type, int key, int x, int y, int mods, int delay) {
    switch (type) {
    case UIRobotEvent.ROBOT_EOF:
      return "ROBOT FINISHED" + " @ " + delay + "ms - " + formatTime(totalTime);
    case PenEvent.PEN_DOWN:
      return "PEN_DOWN  " + x + "," + y + " @ " + delay + "ms - " + formatTime(totalTime);
    case PenEvent.PEN_UP:
      return "PEN_UP    " + x + "," + y + " @ " + delay + "ms - " + formatTime(totalTime);
    case PenEvent.PEN_DRAG:
      return "PEN_DRAG  " + x + "," + y + " @ " + delay + "ms - " + formatTime(totalTime);
    case KeyEvent.KEY_PRESS:
      return "KEY_PRESS " + (key < 10 ? "  " : key < 100 ? " " : "") + key + " '" + (char) key + "'"
          + (mods == 0 ? " @ " : " - " + mods + " @ ") + delay + "ms - " + formatTime(totalTime);
    case KeyEvent.SPECIAL_KEY_PRESS:
      return "SPECIAL_KEY_PRESS " + key + (mods == 0 ? " @ " : " (" + mods + ") @ ") + delay + "ms - "
          + formatTime(totalTime);
    }
    return "";
  }

  private int showMessage(String msg, String[] btns, int delay, boolean error) {
    MessageBox mb = delay > 0 ? new MessageBox(TITLE, msg, null)
        : btns == null ? new MessageBox(TITLE, msg) : new MessageBox(TITLE, msg, btns);
    mb.setBackForeColors(error ? Color.interpolate(Color.RED, Color.ORANGE) : Color.ORANGE, 1);
    if (delay == 0) {
      mb.popup();
    } else {
      mb.popupNonBlocking();
      Vm.sleep(delay);
      mb.unpop();
    }
    return mb.getPressedButtonIndex();
  }

  /* Called from the Window class to record the event posted */
  public void onEvent(int type, int key, int x, int y, int modifiers) {
    switch (type) {
    case PenEvent.PEN_DOWN:
    case PenEvent.PEN_UP:
    case PenEvent.PEN_DRAG:
    case KeyEvent.KEY_PRESS:
    case KeyEvent.SPECIAL_KEY_PRESS:
    case UIRobotEvent.ROBOT_EOF:
      // handle these events
      break;
    default:
      // skip all others
      return;
    }
    try {
      if (flog == null) {
        return;
      }

      int timestamp = Vm.getTimeStamp();
      int elapsed = timestamp - lastTS;
      lastTS = timestamp;
      // int type, int key, int x, int y, int modifiers, int timeStamp
      fds.writeInt(type);
      fds.writeInt(key);
      fds.writeInt(x);
      fds.writeInt(y);
      fds.writeInt(modifiers);
      fds.writeInt(elapsed);
      if (Settings.onJavaSE) {
        Vm.debug((counter++) + " " + type + " " + key + " " + x + " " + y + " " + modifiers + " " + elapsed);
      }
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  public void stop() throws Exception {
    if (status == RECORDING) {
      onEvent(UIRobotEvent.ROBOT_EOF, 0, 0, 0, 0);
      flog.close();
      flog = null;
      saveScreenShot(robotFileName, true);
      fillListOfRecordedRobots();
      showMessage("Finished recording", null, 2000, false);
    }
    status = IDLE;
  }

  private static class StrTime implements Comparable {
    String s;
    long l;

    StrTime(String s, Time t) {
      this.s = s;
      l = t.getTimeLong();
    }

    @Override
    public int compareTo(Object other) throws ClassCastException {
      StrTime st = (StrTime) other;
      long res = l - st.l;
      return res > 0 ? 1 : res < 0 ? -1 : 0;
    }

  }

  private void fillListOfRecordedRobots() throws Exception {
    String[] list = new File(Settings.appPath).listFiles();
    recordedRobots = null;
    if (list != null && list.length > 0) {
      Vector v = new Vector(10);
      for (int i = 0; i < list.length; i++) {
        if (list[i].endsWith(".robot")) {
          File f = new File(Settings.appPath + "/" + list[i], File.READ_WRITE);
          Time t = f.getTime(File.TIME_MODIFIED);
          f.close();
          v.addElement(new StrTime(list[i] + " (" + new Date(t) + " " + t + ")", t));
        }
      }
      int n = v.size();
      if (n > 0) {
        v.qsort();
        recordedRobots = new String[n];
        while (--n >= 0) {
          recordedRobots[n] = ((StrTime) v.items[n]).s;
        }
      }
    }
  }

  Vector listeners;

  /** Adds a listener for UIRobot events.
   * @see totalcross.unit.UIRobotListener
   */
  public void addUIRobotListener(UIRobotListener listener) {
    if (listeners == null) {
      listeners = new Vector(2);
    }
    listeners.addElement(listener);
  }

  /** Removes a listener for UIRobot events.
   * @see totalcross.unit.UIRobotListener
   */
  public void removeUIRobotListener(UIRobotListener listener) {
    listeners.removeElement(listener);
  }

}
