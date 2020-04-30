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

package totalcross.sys;

import totalcross.ui.Window;
import totalcross.util.Hashtable;

/**
 * Vm contains various system level methods.
 * <p>
 * This class contains methods to copy arrays, obtain a timestamp,
 * sleep and get platform and version information, among many other things.
 */

public final class Vm4D {
  public final static String ERASE_DEBUG = "!erase debug!";
  public final static String ALTERNATIVE_DEBUG = "!alt_debug!";
  private static Hashtable htLoadedNatLibs = new Hashtable(5);
  public static boolean disableDebug; // guich@552_30

  private Vm4D() {
  }

  native public static boolean arrayCopy(Object srcArray, int srcStart, Object dstArray, int dstStart, int length);

  native public static int getTimeStamp();

  native public static void setTime(totalcross.sys.Time t);

  native public static void exitAndReboot();

  native public static int exec(String command, String args, int launchCode, boolean wait);

  native public static void setAutoOff(boolean enabled);

  native public static void sleep(int millis);

  native public static int getFreeMemory();

  native public static void gc(); // guich@200b4_159

  native public static void interceptSpecialKeys(int[] keys);

  native public static boolean isKeyDown(int key); // guich@200b4_7

  native public static void debug(String s);

  native public static void alert(String s);

  native public static void clipboardCopy(String s);

  native public static String clipboardPaste();

  native public static boolean attachLibrary(String name);

  native public static boolean privateAttachNativeLibrary(String name);

  native public static byte[] getFile(String name); // guich@402_62

  native public static int getRemainingBattery(); // guich@421_18

  native public static void tweak(int param, boolean set); // guich@582_3

  native public static String getStackTrace(Throwable t); // guic@582_6

  native public static void showKeyCodes(boolean on);

  native public static boolean turnScreenOn(boolean on);

  native public static void vibrate(int millis);

  native public static void preallocateArray(Object sample, int length);

  public static final int TWEAK_AUDIBLE_GC = 1;
  public static final int TWEAK_DUMP_MEM_STATS = 2;
  public static final int TWEAK_MEM_PROFILER = 3;
  public static final int TWEAK_DISABLE_GC = 4;
  public static final int TWEAK_TRACE_CREATED_CLASSOBJS = 5;
  public static final int TWEAK_TRACE_LOCKED_OBJS = 6;
  public static final int TWEAK_TRACE_OBJECTS_LEFT_BETWEEN_2_GCS = 7;
  public static final int TWEAK_TRACE_METHODS = 8;

  public static boolean attachNativeLibrary(String name) {
    if (htLoadedNatLibs.exists(name)) {
      return true;
    }

    if (privateAttachNativeLibrary(name)) {
      htLoadedNatLibs.put(name, name);
      return true;
    }
    return false;
  }

  public static void warning(String s) // guich@421_4
  {
    debug("Warning! " + s);
  }

  public static void printStackTrace() {
    try {
      throw new Exception("Stack trace");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void printStackTrace(String msg) {
    try {
      throw new Exception(msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void safeSleep(int millis) {
    int cur = getTimeStamp();
    int end = cur + millis;
    while (cur <= end) {
      millis = end - cur;
      int s = millis > 10 ? 10 : millis;
      sleep(s);
      //if (Event.isAvailable()) // always call pumpEvents, otherwise a thread that use this method will not be able to update the screen
      Window.pumpEvents();
      cur = getTimeStamp();
    }
  }

  public static native int identityHashCode(Object object);

  /** used internally for enum */
  static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
    arrayCopy(src, srcPos, dest, destPos, length);
  }

  public static String getStackTrace() {
    try {
      throw new Exception();
    } catch (Exception e) {
      return getStackTrace(e);
    }
  }
}