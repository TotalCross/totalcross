/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.sys;

import totalcross.*;
import totalcross.io.*;
import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.util.*;

import java.io.*;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.util.StringUtilities;

/**
 * Vm contains various system level methods.
 * <p>
 * This class contains methods to copy arrays, obtain a timestamp, sleep and get platform and version information, among
 * many other things.
 */

public final class Vm4B
{
   public static int[] keysBeingIntercepted;
   public final static String ERASE_DEBUG = "!erase debug!";
   public final static String ALTERNATIVE_DEBUG = "!alt_debug!";
   public static boolean disableDebug;
   
   private static Thread autoOffBlocker;
   private static boolean autoOffBlocked;
   private static Logger logger = Logger.getLogger("totalcross.sys");
   private static long startTS = System.currentTimeMillis();
   
   private static boolean sendToEventLog;
   private static File debugFile;
   private static long eventLogGUID;
   private static Object debugLock = new Object();
   private static StringBuffer sb = new StringBuffer();
   
   private Vm4B()
   {
   }

   public static boolean arrayCopy(Object srcArray, int srcStart, Object dstArray, int dstStart, int length)
   {
      if (length < 0 || srcArray == null || dstArray == null)
         return false;

      if (length > 0) // guich@tc110_80: Java throws AIOOBE if length is 0
         System.arraycopy(srcArray, srcStart, dstArray, dstStart, length);
      return true;
   }

   public static int getTimeStamp()
   {
      return (int)(System.currentTimeMillis() - startTS);
   }

   public static void setTime(Time t)
   {
   }

   public static void exitAndReboot()
   {
      Launcher4B.instance.rebootWhenExit = true;
      MainWindow.exit(0);
   }

   public static int exec(String command, String args, int launchCode, boolean wait)
   {
      if ("net_rim_bb_browser_daemon".equals(command)) //flsobral@tc126_37: better way to launch the browser on blackberry, which also allows urls containing the character '&'.
      {
         if (args.startsWith("url&"))
            args = args.substring(4);
         net.rim.blackberry.api.browser.Browser.getDefaultSession().displayPage(args);
         return 1;
      }
      else
      {
         try
         {
            ApplicationManager.getApplicationManager().launch(command + "?" + args);
            if (wait)
            {
               UiApplication app = UiApplication.getUiApplication();
               while (!app.isForeground())
               {
                  try
                  {
                     Thread.sleep(100);
                  }
                  catch (InterruptedException ex) {}
               }
            }
   
            return 1;
         }
         catch (ApplicationManagerException ex)
         {
            logger.throwing(Vm.class.getName(), "exec", ex);
            return -1;
         }
      }
   }

   public static void setAutoOff(boolean enabled)
   {
      if (enabled) // enable auto off
      {
         if (autoOffBlocked) // auto off is currently disabled
         {
            autoOffBlocked = false;
            autoOffBlocker.interrupt();
            try
            {
               autoOffBlocker.join();
            }
            catch (InterruptedException e) {}
         }
      }
      else // disable auto off
      {
         if (!autoOffBlocked) // auto off is currently enabled
         {
            autoOffBlocker = new Thread()
            {
               public void run()
               {
                  int timeout = Backlight.getTimeoutDefault();
                  Backlight.setTimeout(255); // change backlight timeout to 255 seconds
                  
                  while (autoOffBlocked)
                  {
                     Backlight.enable(true);
                     try
                     {
                        Thread.sleep(250000); // sleeps for 250 seconds
                     }
                     catch (InterruptedException e) { }
                  }
                  
                  Backlight.setTimeout(timeout);
               }
            };
            
            autoOffBlocked = true;
            autoOffBlocker.start();
         }
      }
   }

   public static void sleep(int millis)
   {
      try
      {
         java.lang.Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {
      }
   }

   public static int getFreeMemory()
   {
      return (int) Runtime.getRuntime().freeMemory();
   }

   public static void gc()
   {
      Runtime.getRuntime().gc();
   }

   public static void interceptSpecialKeys(int[] keys)
   {
      keysBeingIntercepted = keys; // for desktop
   }

   public static boolean isKeyDown(int key)
   {
      try
      {
         return Launcher.instance.keysPressed.get(key) == 1;
      }
      catch (ElementNotFoundException e)
      {
         return false;
      }
   }
   
   private static String tsep = " - ";
   private static String crlf = "\r\n";
   
   private static byte[] tsepBytes = tsep.getBytes();
   private static byte[] crlfBytes = crlf.getBytes();
   
   public static void debug(String s)
   {
      synchronized (debugLock)
      {
         if (disableDebug)
            return;
         if (s == null)
            throw new NullPointerException("Argument 's' cannot have a null value");
         
         if (ALTERNATIVE_DEBUG.equals(s))
            sendToEventLog = !sendToEventLog;
         else if (sendToEventLog)
            debugOnEventLog(s);
         else
            debugOnFile(s);
      }
   }
   
   private static void debugOnFile(String s)
   {
      try
      {
         if (debugFile == null) // debug file not created yet
         {
            debugFile = new File(Convert.appendPath(Settings.vmPath, "DebugConsole.txt"), File.CREATE);
            debugFile.setPos(debugFile.getSize()); // move to the end of the file
         }
         if (!ERASE_DEBUG.equals(s)) // common debug message
         {
            // guich@tc123_35: write the parts to the file without concatenating them into a single String 
            if (Settings.showDebugTimestamp)
            {
               debugFile.writeBytes(String.valueOf(getTimeStamp()));
               debugFile.writeBytes(tsepBytes);
            }
            debugFile.writeBytes(s == null ? "null" : s);
            debugFile.writeBytes(crlfBytes);
            debugFile.flush();
         }
         else // erase flag
            debugFile.setSize(0);
      }
      catch (IOException e) {}
   }
   
   private static void debugOnEventLog(String s)
   {
      if (eventLogGUID == 0)
      {
         eventLogGUID = StringUtilities.stringHashToLong(Settings.applicationId);
         if (!EventLogger.register(eventLogGUID, "TotalCross", EventLogger.VIEWER_STRING))
            eventLogGUID = 0;
      }
      if (eventLogGUID != 0 && !ERASE_DEBUG.equals(s))
      {
         sb.setLength(0);
         if (Settings.showDebugTimestamp)
         {
            sb.append(String.valueOf(getTimeStamp()));
            sb.append(tsep);
         }
         sb.append(s);
         EventLogger.logEvent(eventLogGUID, Convert.getBytes(sb));
      }
   }

   public static void alert(String s)
   {
      if (s == null)
         throw new NullPointerException("Argument 's' cannot have a null value");
      Launcher4B.instance.alert(s);
   }

   public static void warning(String s)
   {
      debug("Warning! " + s);
   }

   public static void clipboardCopy(String s)
   {
      net.rim.device.api.system.Clipboard.getClipboard().put(s);
   }

   public static String clipboardPaste()
   {
      return net.rim.device.api.system.Clipboard.getClipboard().get().toString();
   }

   public static boolean attachLibrary(String name)
   {
      return false;
   }

   public static boolean attachNativeLibrary(String name)
   {
      return false;
   }

   public static totalcross.util.Hashtable htAttachedFiles = new totalcross.util.Hashtable(13); // guich@566_28

   public static byte[] getFile(String name)
   {
      ByteArrayStream bas = (ByteArrayStream) htAttachedFiles.get(name.toLowerCase());
      if (bas != null)
         return bas.getBuffer();
      else
      {
         if (!name.startsWith("/"))
            name = "/" + name;

         InputStream is = null;
         try
         {
            is = Class.forName("Stub").getResourceAsStream(name);
         }
         catch (ClassNotFoundException ex) { }

         if (is == null)
            return null;

         try
         {
            byte[] b = new byte[is.available()];
            is.read(b);
            is.close();

            return b;
         }
         catch (java.io.IOException e)
         {
            return null;
         }
      }
   }

   public static int getRemainingBattery()
   {
      return DeviceInfo.getBatteryLevel();
   }

   public static final int TWEAK_AUDIBLE_GC     = 1;
   public static final int TWEAK_DUMP_MEM_STATS = 2;
   public static final int TWEAK_MEM_PROFILER   = 3;
   public static final int TWEAK_DISABLE_GC = 4;

   public static void tweak(int param, boolean set)
   {
   }

   public static String getStackTrace(Throwable t)
   {
      return t.toString();
   }

   public static void showKeyCodes(boolean on)
   {
      Launcher.instance.showKeyCodes = on;
   }

   public static boolean turnScreenOn(boolean on)
   {
      Backlight.enable(on);
      return Backlight.isEnabled() == on;
   }

   public static void vibrate(int millis)
   {
      Alert.startVibrate(millis <= 25500 ? millis : 25500); // max duration is 25500 ms
   }
   
   public static void printStackTrace()
   {
      try {throw new Exception("Stack trace");} catch (Throwable e) {e.printStackTrace();}
   }

   public static void safeSleep(int millis)
   {
      int cur = getTimeStamp();
      int end = cur + millis;
      while (cur <= end)
      {
         millis = end - cur;
         int s = millis > 100 ? 100 : millis;
         try {java.lang.Thread.sleep(s);} catch (InterruptedException e) {}
         if (Event.isAvailable())
            Window.pumpEvents();
         cur = getTimeStamp();
      }
   }
}
