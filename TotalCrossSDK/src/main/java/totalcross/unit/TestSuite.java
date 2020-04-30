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

import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Grid;
import totalcross.ui.Label;
import totalcross.ui.ListBox;
import totalcross.ui.MainWindow;
import totalcross.ui.MenuBar;
import totalcross.ui.MenuItem;
import totalcross.ui.ProgressBar;
import totalcross.ui.SpinList;
import totalcross.ui.Window;
import totalcross.ui.dialog.ControlBox;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;
import totalcross.util.IntHashtable;
import totalcross.util.Vector;

/** JUnit implementation for TotalCross, to be used in the device (or in the desktop).
 * It simulates the same output that you get when using JUnit under Eclipse.
 * To use it, you must extend this class and add the test cases using the <code>addTestCase</code> method.
 * See an example in the Litebase SDK, in samples/sys/testcases folder.
 * <br><br>
 * You can also create a test suite without this user interface. Here's a sample:
 * <pre>
   public class Testes extends TestSuite
   {
      public Testes()
      {
         super("UI Tests");
         Settings.showDebugTimestamp = false;
         addTestCase(Teste1.class);
         addTestCase(TesteChamado509.class);
         runTests();
         Settings.showDebugTimestamp = true;
      }
   }
 * </pre> 
 */

public abstract class TestSuite extends MainWindow {
  private ProgressBar bar;
  private Label lErrors, lElapsed;
  private static Label lMsg, lMem;
  private Button btn;
  private Vector v = new Vector(10);
  private static Vector vDump = new Vector(1000);
  private static ListBox lbDump;
  protected MenuBar mbar;
  private static MenuItem miDump, miLoop;
  private TestChooser chooser;
  private static IntHashtable ihtForeColors;
  private static IntHashtable ihtBackColors;
  private static final int green = 0x00F000;
  private static final int red = 0xF00000;
  private static final int errorFore = red;
  private static final int errorBack = 0xF8F800;
  private int errors, loopCount;
  private static boolean hasUI;
  protected static boolean assertionFailed;
  protected static final int OUTPUT_MSG = 0;
  protected static final int OUTPUT_TEST_FAILED = 1;
  protected static final int OUTPUT_ASSERT_FAILED = 2;

  public TestSuite(String title) {
    super(title, TAB_ONLY_BORDER);
  }

  /** Add a new testcase. */
  public void addTestCase(Class<?> c) {
    v.addElement(c);
  }

  static void output(String s, int type) {
    if (hasUI) {
      vDump.addElement(s);
      switch (type) {
      case OUTPUT_TEST_FAILED:
        ihtBackColors.put(vDump.size() - 1, errorBack);
      case OUTPUT_ASSERT_FAILED:
        ihtForeColors.put(vDump.size() - 1, errorFore);
        break;
      }
    }
    if (!hasUI || miDump.isChecked) {
      Vm.debug(s);
    }
  }

  static void status(String s) {
    if (hasUI) {
      lMsg.setText(s);
      lMsg.repaintNow();
    }
  }

  private void updateMsg() {
    byte[] s = Settings.appSettingsBin; // guich@580_9: use appSettingsBin instead of appSecretKey because AllTests use the last
    int skip = 0;
    if (s != null) {
      for (int i = s.length - 1; i >= 0; i--) {
        skip += 1 - s[i];
      }
    }
    lMsg.setText(skip == 0 ? "" : ("Skipping " + skip + " tests"));
  }

  protected void runTests() {
    hasUI = btn != null;
    totalcross.sys.Vm.setAutoOff(false);
    int n = v.size();
    errors = 0;
    if (hasUI) {
      btn.setEnabled(false);
      repaintNow();
      // clear the output views
      if (miDump.isChecked) {
        Vm.debug(Vm.ERASE_DEBUG);
      }
      vDump.removeAllElements();
      lbDump.removeAll();
      ihtForeColors = new IntHashtable(10);
      ihtBackColors = new IntHashtable(10);
      bar.setForeColor(green);
      bar.setValue(0);
      bar.repaintNow();
      lErrors.setText("0/" + n);
      updateMem();
    }
    int start = Vm.getTimeStamp();
    for (int i = 0; i < n; i++) {
      Class<?> c = (Class<?>) v.items[i];
      output("=============================", OUTPUT_MSG);
      if (!canRun(i)) {
        output("SKIPPING " + c, OUTPUT_MSG);
      } else {
        boolean error = false;
        output("RUNNING " + c, OUTPUT_MSG);
        // show the test in the msg area. aligning to the right makes it always show the class name, and maybe cut the package.
        if (hasUI) {
          lMsg.align = RIGHT;
          lMsg.setText(c.toString());
          lMsg.repaintNow();
          lMsg.align = LEFT;
        }

        int ustart = Vm.getTimeStamp();
        try // guich@tc110_3: this strange code is to deal with Blackberry's odd behaviour on "catch Throwable".
        {
          try {
            updateMem();
            Object o = c.newInstance();
            ((TestCase) o).run(); // guich@565_6: don't start the tests on the constructor
            error = false;
          } catch (Throwable e) // was another exception but AssertionFailedException thrown?
          {
            error = true;
            showException(e);
          }
        } catch (Error err) {
        } catch (Exception ee) {
        }
        updateMem();
        if (assertionFailed) {
          error = true;
        }

        if (error) {
          if (errors == 0 && hasUI) {
            bar.setForeColor(red);
          }
          errors++;
          if (hasUI) {
            lErrors.setText(errors + "/" + n);
            lErrors.repaintNow();
          }
          output("###### TEST FAILED ######", OUTPUT_TEST_FAILED);
        } else {
          int uend = Vm.getTimeStamp();
          output("Test elapsed " + (uend - ustart) + "ms", OUTPUT_MSG);
          output("###### TEST PASSED", OUTPUT_MSG);
        }
      }
      if (hasUI) {
        bar.setValue(i + 1);
      }
      TestCase.learning = false; // reset after each testcase so that one do not change other's behaviour
    }
    int end = Vm.getTimeStamp();
    if (hasUI) {
      updateMem();
      updateMsg(); // if the tests have issued any "status"
      lElapsed.setText(Convert.toString(end - start)); // guich@560_27
      lElapsed.repaintNow();
      // now show all in the listbox
      lbDump.add(vDump.toObjectArray());
      lbDump.ihtForeColors = ihtForeColors.size() > 0 ? ihtForeColors : null;
      lbDump.ihtBackColors = ihtBackColors.size() > 0 ? ihtBackColors : null;
      btn.setEnabled(true);
      lbDump.requestFocus(); // let user navigate using scroll buttons
    }
    totalcross.sys.Vm.setAutoOff(true);
  }

  /** If you override this method, you must call
   *    super.initUI(); this call must be the
   *    first statement
   */
  @Override
  public void initUI() //rnovais@570_77 : Now is not final anymore
  {
    add(new Label(" free"), RIGHT, 0, PREFERRED, PREFERRED - 4);
    add(lMem = new Label("99999999", RIGHT), BEFORE, 0, PREFERRED, PREFERRED - 4);
    lMem.setText("memory");
    add(new Label("Errors: "), LEFT, TOP + 3);
    add(lErrors = new Label("999/999"), AFTER, SAME);
    add(new Label("ms"), RIGHT, SAME);
    add(lElapsed = new Label("99999999", RIGHT), BEFORE - 1, SAME); // guich@550_26
    add(new Label("Elapsed: "), BEFORE, SAME);
    lElapsed.setText("0");

    add(bar = new ProgressBar(), CENTER, AFTER + 3);
    add(btn = new Button(" Start tests "), RIGHT, BOTTOM);
    add(lbDump = new ListBox());
    lbDump.enableHorizontalScroll();
    lbDump.setRect(LEFT, AFTER + 2, FILL, FIT, bar);
    add(lMsg = new Label(""));
    lMsg.setRect(LEFT, BOTTOM, FILL - btn.getPreferredWidth() - 2, PREFERRED);
    updateMsg();

    bar.setBackForeColors(Color.DARK, Color.WHITE);
    bar.textColor = Color.WHITE;
    bar.max = v.size();
    bar.suffix = "/" + v.size();
    lErrors.setText("0/" + v.size());
    if (Settings.appSettings == null || Settings.appSettings.length() < 3 || Settings.appSettings.equals("yes")) {
      Settings.appSettings = "nn10"; // default
    }
    String s = Settings.appSettings;
    try {
      loopCount = Convert.toInt(s.substring(2));
    } catch (InvalidNumberException ine) {
    }

    MenuItem[] menu0 = { new MenuItem("File"), new MenuItem("Select tests"),
        miDump = new MenuItem("Dump to console", s.charAt(0) == 'y'), new MenuItem(),
        miLoop = new MenuItem("Loop " + loopCount + "x", s.charAt(1) == 'y'), new MenuItem("Select loop count"),
        new MenuItem(), new MenuItem("Exit"), };
    bar.prefix = miLoop.isChecked ? "0/" + loopCount + " - " : "";

    setMenuBar(mbar = new MenuBar(new MenuItem[][] { menu0 })); // if a new vertical menu is added, AllTests must be changed!
    mbar.setBackForeColors(Color.BLUE, Color.WHITE);
    mbar.setCursorColor(0x6464FF);
    mbar.setBorderStyle(NO_BORDER);
    mbar.setPopColors(0x0078FF, Color.CYAN, -1);
    btn.setBackColor(Color.GREEN);

    updateMem();
    if ("/autorun".equals(getCommandLine())) {
      runTests(); // guich@570_90: now is safe to run from here, because initUI is already started from a thread.
    } else {
      btn.requestFocus();
    }
  }

  private static String getLineNumber(Throwable e) {
    // convert the stack trace into String
    String s = Vm.getStackTrace(e);
    if (s == null || s.length() == 0) {
      return null;
    }
    // split into lines
    String lines[] = Convert.tokenizeString(s, '\n');
    // skip the ones containing totalcross(.unit)
    for (int i = 0; i < lines.length; i++) {
      String l = lines[i];
      if (Settings.onJavaSE && l.startsWith(" at ")) {
        l = l.substring(4);
      }
      if (!l.startsWith("totalcross.") && !l.startsWith("litebase.") && (l.indexOf(' ') >= 0 || l.indexOf(':') >= 0)) {
        return l;
      }
    }
    return null;
  }

  static void showException(Throwable e) {
    String msg = e.getMessage();
    if (msg == null) {
      msg = "";
    }
    if (!(e instanceof AssertionFailedError)) {
      msg += " (" + e.getClass() + ")";
    }
    String at = getLineNumber(e);
    if (at != null) {
      msg += " at " + at;
    } else {
      msg = "#" + TestCase.assertionCounter + ": " + msg;
    }
    output(msg, OUTPUT_ASSERT_FAILED);
    e.printStackTrace();
  }

  /** Updates the label with the available memory. */
  public static void updateMem() {
    if (hasUI) {
      lMem.setText(Convert.toString(Vm.getFreeMemory()));
      lMem.repaintNow();
    }
  }

  /** If you override this method, you must call
   *    super.onEvent(); this call must be the
   *    first statement
   */
  @Override
  public void onEvent(Event e) //rnovais@570_77 : Now is not final anymore
  {
    try {
      switch (e.type) {
      case ControlEvent.PRESSED:
        if (e.target == btn) {
          if (!miLoop.isChecked) {
            bar.prefix = "";
            runTests();
          } else {
            errors = 0;
            for (int loop = 0; loop < loopCount && errors == 0; loop++) {
              bar.prefix = (loop + 1) + "/" + loopCount + " - ";
              runTests();
            }
          }
        } else if (e.target == mbar) {
          switch (mbar.getSelectedIndex()) {
          case 1:
            if (chooser == null) {
              chooser = new TestChooser();
            }
            chooser.popupNonBlocking();
            break;
          case 2: // dump
          case 4: // loop
            updateSettings();
            break;
          case 5:
            SpinList sl = new SpinList(new String[] { "[1,100]" }, !Settings.fingerTouch);
            sl.setSelectedIndex(loopCount - 1);
            sl.timerInterval = 100;
            ControlBox cb = new ControlBox("Loop count", "Select the number of\ntimes the tests will run", sl,
                new String[] { "Ok" });
            cb.popup();
            loopCount = sl.getSelectedIndex() + 1; // index 0 = 1x
            miLoop.caption = "Loop " + loopCount + "x";
            updateSettings();
            break;
          case 7:
            exit(0);
            break;
          }
        }
        break;
      }
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }

  private void updateSettings() {
    Settings.appSettings = (miDump.isChecked ? "y" : "n") + (miLoop.isChecked ? "y" : "n") + loopCount;
    bar.prefix = miLoop.isChecked ? "0/" + loopCount + " - " : "";
    Window.needsPaint = true;
  }

  private boolean canRun(int i) {
    byte[] s = Settings.appSettingsBin;
    return s == null || (s.length > i && s[i] == 1);
  }

  class TestChooser extends Window // guich@567_8
  {
    Grid grid;
    Button btn;

    public TestChooser() {
      super("Choose the tests", RECT_BORDER);
      setRect(CENTER, CENTER, Settings.screenWidth - 10, Settings.screenHeight * 4 / 5);
      setBackColor(0xA0FFEC);
    }

    @Override
    public void initUI() {
      add(btn = new Button("Close"), RIGHT - 3, BOTTOM - 1);
      add(grid = new Grid(new String[] { " # ", "Class name" }, true));
      grid.setRect(LEFT + 3, TOP + 3, FILL - 3, FIT - 3);

      int n = v.size();
      for (int i = 0; i < n; i++) {
        grid.add(new String[] { Convert.toString(i + 1), prepareName(v.items[i].toString()) });
      }
    }

    @Override
    public void onPopup() {
      int n = v.size();
      for (int i = 0; i < n; i++) {
        grid.setChecked(i, canRun(i));
      }
    }

    private String prepareName(String c) {
      int dot = c.lastIndexOf('.');
      if (dot > 0) {
        dot = c.lastIndexOf('.', dot - 1);
      }
      if (dot > 0) {
        c = c.substring(dot + 1);
      }
      if (c.startsWith("class ")) {
        c = c.substring(6);
      }
      return c;
    }

    @Override
    public void onEvent(Event e) {
      if (e.type == ControlEvent.PRESSED && e.target == btn) {
        int n = v.size();
        byte[] c = new byte[n];
        int checked = 0;
        for (int i = 0; i < n; i++) {
          if (grid.isChecked(i)) {
            try {
              String num = grid.getCellText(i, 1); // get the real index, because the user may have sorted the grid
              c[Convert.toInt(num) - 1] = (byte) 1;
              checked++;
            } catch (InvalidNumberException ine) {
            }
          }
        }
        Settings.appSettingsBin = checked == n ? null : c; // if everyone is checked, then we just store null - run everybody
        updateMsg();
        unpop();
      }
    }
  }
}
