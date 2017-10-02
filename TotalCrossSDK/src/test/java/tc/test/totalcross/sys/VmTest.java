/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package tc.test.totalcross.sys;

import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.unit.TestCase;

public class VmTest extends TestCase {
  // methods not tested - suggestions of how to test them?
  // public static void exitAndReboot()
  // public static int exec(String command, String args, int launchCode,
  // boolean wait)
  // public static void interceptSystemKeys(int keys)
  // public static void debug(String s)
  // public static void warning(String s)
  // public static boolean attachNativeLibrary(String name)
  // public static int getSystemKeysPressed()
  // public static int setDeviceAutoOff(int seconds) - idea: turn off, wait 5
  // minutes (above usual auto-off timer) and ask the user if the device has
  // turned off.

  private void attachLibrary_getFile() {
    /*      String libName = "MyTextLibrary.Test";
      String name = libName + ".SWAX";
      String textName = "sample.txt"; // all lower
      String text = "This is a line of text that will be read using getFile.";
    
      // create our "library"
      try
      {
         PDBFile cat = new PDBFile(name, PDBFile.READ_WRITE);
         cat = new PDBFile(name, PDBFile.CREATE);
         ResizeRecord rs = new ResizeRecord(cat, 256);
         DataStream ds = new DataStream(rs);
         rs.startRecord();
         ds.writeString(textName);
         ds.writeCString(text);
         rs.endRecord();
         cat.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      // attach it to the currently running process
      assertTrue(Vm.attachLibrary(libName));
    
      // now get the file
      byte[] txt = Vm.getFile(textName);
      assertNotNull(txt);
      assertEquals(text, new String(txt, 0, txt.length - 1));
     */ }

  private void getRemainingBattery() {
    assertGreater(Vm.getRemainingBattery(), 0);
  }

  private void clipboardPaste_clipboardCopy() {
    String s = "Verinha";
    Vm.clipboardCopy(s);
    String o = Vm.clipboardPaste();
    assertEquals(s, o);
  }

  private void gc_getFreeMemory() {
    Vm.gc();
    // allocate some memory at start
    byte[][] big = new byte[5][];
    for (int i = 0; i < 5; i++) {
      big[i] = new byte[50000];
    }
    // get the amount of memory
    int before = Vm.getFreeMemory();
    // let the gc free big's memory
    big = null;
    Vm.gc();
    int after = Vm.getFreeMemory();
    assertGreaterOrEqual(after, before);
  }

  private void sleep_getTimeStamp() {
    int e = -Vm.getTimeStamp();
    Vm.sleep(1000);
    e += Vm.getTimeStamp();
    assertBetween(900, e, 1100);
  }

  private void copyArray() {
    String[] strs = { "bobby", "daddy", "mommy" };
    int[] ints = { 0, 1, 2 };
    String[] dstr = new String[3];
    assertTrue(Vm.arrayCopy(strs, 0, dstr, 0, strs.length));
    assertEquals(dstr[0], strs[0]);
    assertEquals(dstr[1], strs[1]);
    assertEquals(dstr[2], strs[2]);
    assertTrue(Vm.arrayCopy(strs, 2, dstr, 0, 1));
    assertEquals(strs[2], dstr[0]);
    try {
      assertFalse(Vm.arrayCopy(ints, 0, strs, 0, 3));
      fail("Arrays should be incompatible");
    } catch (ArrayStoreException ase) {
    }
    try {
      Vm.arrayCopy(strs, 0, dstr, 0, 4);
      fail("ArrayIndexOutOfBoundsException was expected.");
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      // ok
    }
  }

  @Override
  public void testRun() {
    copyArray();
    sleep_getTimeStamp();
    if (!Settings.platform.equals(Settings.ANDROID)) {
      clipboardPaste_clipboardCopy();
    }
    gc_getFreeMemory();
    getRemainingBattery();
    attachLibrary_getFile();
  }
}
