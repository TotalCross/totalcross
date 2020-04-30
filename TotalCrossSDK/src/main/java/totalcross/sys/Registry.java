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

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.util.ElementNotFoundException;

/** This class can be used to create, delete, read, write and list registry values.
 * Currently only Windows and Palm OS are supported.
 * <br><br>
 * Note that these methods don't work at Java desktop.
 * <br><br>
 * Here's a full sample for Windows:
 * <pre>
   ListBox lb;
   private void log(String s)
   {
      lb.add(s);
      lb.selectLast();
   }

   public void initUI()
   {
      ByteArrayStream bas = new ByteArrayStream(10);
      DataStreamLE ds = new DataStreamLE(bas);

      lb = new ListBox();
      lb.enableHorizontalScroll();
      add(lb, LEFT,TOP,FILL,FILL);
      try
      {
         log("adding int 0x12345678");
         Registry.set(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","IntKey",0x12345678);
         log("added. now retrieving...");
         int ires = Registry.getInt(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","IntKey");
         log("int retrieved: "+Convert.unsigned2hex(ires,8));

         log("adding string \"Bárbara\"");
         Registry.set(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","StringKey","B�rbara");
         log("added. now retrieving...");
         String sres = Registry.getString(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","StringKey");
         log("string retrieved: "+sres);

         double dvalue = 1234.56789;
         log("adding blob double value: "+dvalue);
         ds.writeDouble(dvalue);
         Registry.set(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","BlobKey",bas.toByteArray());
         log("added. now retrieving...");
         byte[] bres = Registry.getBlob(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","BlobKey");
         bas.setBuffer(bres);
         double dres = ds.readDouble();
         log("double retrieved: "+dres);

         log("deleting string value");
         Registry.delete(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","StringKey");
         log("deleted. trying to retrieve...");
         try
         {
            sres = Registry.getString(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","StringKey");
            log("Failure! delete didn't work");
         }
         catch (ElementNotFoundException enfe)
         {
            log("Success: the deleted key was not found (as expected)");
         }

         log("deleting the whole key");
         Registry.delete(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test");
         log("deleted. trying to retrieve...");
         try
         {
            sres = Registry.getString(Registry.HKEY_CURRENT_USER, "Software\\TotalCross\\appSettings\\Test","IntKey");
            log("Failure! delete didn't work");
         }
         catch (ElementNotFoundException enfe)
         {
            log("Success: the deleted key was not found (as expected)");
         }
         log("Tests finished.");
      }
      catch (Exception e)
      {
         MessageBox.showException(e, true);
      }
   }
 * </pre>
 * And here's a full sample for Palm OS.
 * <pre>
   ListBox lb;
   private void log(String s)
   {
      lb.add(s);
      lb.selectLast();
   }

   public void initUI()
   {
      ByteArrayStream bas = new ByteArrayStream(10);
      DataStreamLE ds = new DataStreamLE(bas);

      lb = new ListBox();
      lb.enableHorizontalScroll();
      add(lb, LEFT,TOP,FILL,FILL);
      try
      {
         // retrieves the ROM version from the Feature manager
         int ret = Registry.getInt(Registry.FEATURE, "psys", "1");
         log("rom version: "+Convert.unsigned2hex(ret,8));

         log("adding int 0x12345678");
         Registry.set(Registry.UNSAVED_PREFERENCES, Settings.applicationId,"1",0x12345678);
         log("added. now retrieving...");
         int ires = Registry.getInt(Registry.UNSAVED_PREFERENCES, Settings.applicationId,"1");
         log("int retrieved: "+Convert.unsigned2hex(ires,8));

         log("adding string \"Bárbara\"");
         Registry.set(Registry.UNSAVED_PREFERENCES, Settings.applicationId,"2","B�rbara");
         log("added. now retrieving...");
         String sres = Registry.getString(Registry.UNSAVED_PREFERENCES, Settings.applicationId,"2");
         log("string retrieved: "+sres);

         double dvalue = 1234.56789;
         log("adding blob double value: "+dvalue);
         ds.writeDouble(dvalue);
         Registry.set(Registry.UNSAVED_PREFERENCES, Settings.applicationId,"3",bas.toByteArray());
         log("added. now retrieving...");
         byte[] bres = Registry.getBlob(Registry.UNSAVED_PREFERENCES, Settings.applicationId,"3");
         bas.setBuffer(bres);
         double dres = ds.readDouble();
         log("double retrieved: "+dres);

         log("deleting string value");
         Registry.delete(Registry.UNSAVED_PREFERENCES, Settings.applicationId,"2");
         log("deleted. trying to retrieve...");
         try
         {
            sres = Registry.getString(Registry.UNSAVED_PREFERENCES, Settings.applicationId,"2");
            log("Failure! delete didn't work");
         }
         catch (ElementNotFoundException enfe)
         {
            log("Success: the deleted key was not found (as expected)");
         }

         log("Tests finished.");
      }
      catch (Exception e)
      {
         MessageBox.showException(e, true);
      }
   }
 * </pre>
 * @since TotalCross 1.0
 */

public class Registry {
  /** Windows-specific value to be used in the hk parameter. */
  public static final int HKEY_CLASSES_ROOT = 0x80000000;
  /** Windows-specific value to be used in the hk parameter. */
  public static final int HKEY_CURRENT_USER = 0x80000001;
  /** Windows-specific value to be used in the hk parameter. */
  public static final int HKEY_LOCAL_MACHINE = 0x80000002;
  /** Windows-specific value to be used in the hk parameter. */
  public static final int HKEY_USERS = 0x80000003;

  /** PalmOS-specific value to be used in the hk parameter. Note that the Features database is read-only,
   * and trying to write to it will throw an exception. */
  public static final int FEATURE = 1;
  /** PalmOS-specific value to be used in the hk parameter. */
  public static final int SAVED_PREFERENCES = 2;
  /** PalmOS-specific value to be used in the hk parameter. */
  public static final int UNSAVED_PREFERENCES = 3;

  /** Returns an integer at the given location.
   * @param hk In Windows, can be HKEY_CLASSES_ROOT, HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE or HKEY_USERS.
   *           In Palm OS, can be FEATURE, SAVED_PREFERENCES or UNSAVED_PREFERENCES.
   * @param key In Windows, its the full path to the key (E.G.: "Software\\TotalCross\\appSettings\\Test".
   *            In Palm OS, its the application id (E.G.: Settings.applicationId).
   * @param value In Windows, is the value to be retrieved (E.G.: "IntData").
   *              In Palm OS, is an integer representing the number of the preference (from 0 to 32767).
   * @throws ElementNotFoundException if the hk, key or value was not found.
   * @throws Exception if there was an error when retrieving the data.
   */
  @ReplacedByNativeOnDeploy
  public static int getInt(int hk, String key, String value)
      throws totalcross.util.ElementNotFoundException, Exception {
    return 0;
  }

  /** Returns a String at the given location. In Palm OS you cannot retrieve a string FEATURE.
   * @param hk In Windows, can be HKEY_CLASSES_ROOT, HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE or HKEY_USERS.
   *           In Palm OS, can be FEATURE, SAVED_PREFERENCES or UNSAVED_PREFERENCES.
   * @param key In Windows, its the full path to the key (E.G.: "Software\\TotalCross\\appSettings\\Test".
   *            In Palm OS, its the application id (E.G.: Settings.applicationId).
   * @param value In Windows, is the value to be retrieved (E.G.: "IntData").
   *              In Palm OS, is an integer representing the number of the preference (from 0 to 32767).
   * @throws ElementNotFoundException if the hk, key or value was not found.
   * @throws Exception if there was an error when retrieving the data.
   */
  @ReplacedByNativeOnDeploy
  public static String getString(int hk, String key, String value)
      throws totalcross.util.ElementNotFoundException, Exception {
    return null;
  }

  /** Returns a String at the given location. In Palm OS you cannot retrieve a string FEATURE.
   * This can be used to read long and double types, just create a ByteArrayStream, attach a DataStreamLE to it and call
   * readDouble/readLong.
   * @param hk In Windows, can be HKEY_CLASSES_ROOT, HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE or HKEY_USERS.
   *           In Palm OS, can be FEATURE, SAVED_PREFERENCES or UNSAVED_PREFERENCES.
   * @param key In Windows, its the full path to the key (E.G.: "Software\\TotalCross\\appSettings\\Test".
   *            In Palm OS, its the application id (E.G.: Settings.applicationId).
   * @param value In Windows, is the value to be retrieved (E.G.: "IntData").
   *              In Palm OS, is an integer representing the number of the preference (from 0 to 32767).
   * @throws ElementNotFoundException if the hk, key or value was not found.
   * @throws Exception if there was an error when retrieving the data.
   */
  @ReplacedByNativeOnDeploy
  public static byte[] getBlob(int hk, String key, String value)
      throws totalcross.util.ElementNotFoundException, Exception {
    return null;
  }

  /** Sets an integer value at the given location. If the key or value doesn't exists, it is created.
   * @param hk In Windows, can be HKEY_CLASSES_ROOT, HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE or HKEY_USERS.
   *           In Palm OS, can be FEATURE, SAVED_PREFERENCES or UNSAVED_PREFERENCES.
   * @param key In Windows, its the full path to the key (E.G.: "Software\\TotalCross\\appSettings\\Test".
   *            In Palm OS, its the application id (E.G.: Settings.applicationId).
   * @param value In Windows, is the value to be retrieved (E.G.: "IntData").
   *              In Palm OS, is an integer representing the number of the preference (from 0 to 32767).
   * @param data The data to be written.
   * @throws Exception if there was an error when setting the data.
   */
  @ReplacedByNativeOnDeploy
  public static void set(int hk, String key, String value, int data) throws Exception {
  }

  /** Sets a String value at the given location. If the key or value doesn't exists, it is created. Note that the String is created
   * as ASCII, not as UNICODE. To store a UNICODE string, use a byte array (blob).
   * @param hk In Windows, can be HKEY_CLASSES_ROOT, HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE or HKEY_USERS.
   *           In Palm OS, can be FEATURE, SAVED_PREFERENCES or UNSAVED_PREFERENCES.
   * @param key In Windows, its the full path to the key (E.G.: "Software\\TotalCross\\appSettings\\Test".
   *            In Palm OS, its the application id (E.G.: Settings.applicationId).
   * @param value In Windows, is the value to be retrieved (E.G.: "IntData").
   *              In Palm OS, is an integer representing the number of the preference (from 0 to 32767).
   * @param data The data to be written. In Palm OS, its limited to 64KB.
   * @throws Exception if there was an error when setting the data.
   */
  @ReplacedByNativeOnDeploy
  public static void set(int hk, String key, String value, String data) throws Exception {
  }

  /** Sets a byte array (blob) value at the given location. If the key or value doesn't exists, it is created.
   * @param hk In Windows, can be HKEY_CLASSES_ROOT, HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE or HKEY_USERS.
   *           In Palm OS, can be FEATURE, SAVED_PREFERENCES or UNSAVED_PREFERENCES.
   * @param key In Windows, its the full path to the key (E.G.: "Software\\TotalCross\\appSettings\\Test".
   *            In Palm OS, its the application id (E.G.: Settings.applicationId).
   * @param value In Windows, is the value to be retrieved (E.G.: "IntData").
   *              In Palm OS, is an integer representing the number of the preference (from 0 to 32767).
   * @param data The data to be written. In Palm OS, its limited to 64KB.
   * @throws Exception if there was an error when setting the data.
   */
  @ReplacedByNativeOnDeploy
  public static void set(int hk, String key, String value, byte[] data) throws Exception {
  }

  /** Deletes a specific value. */
  @ReplacedByNativeOnDeploy
  public static boolean delete(int hk, String key, String value) {
    return true;
  }

  /** Deletes a key with all its values. Does not work on Palm OS, which requires that you pass the value number too. */
  public static boolean delete(int hk, String key) {
    return delete(hk, key, null);
  }

  /** Lists all children keys of the given key. Works only in Windows platforms. Note that only
   * keys are listed, and not values nor data.
   * @param hk In Windows, can be HKEY_CLASSES_ROOT, HKEY_CURRENT_USER, HKEY_LOCAL_MACHINE or HKEY_USERS.
   * @param key In Windows, its the full path to the key (E.G.: "Software\\TotalCross\\appSettings\\Test".
   */
  @ReplacedByNativeOnDeploy
  public static String[] list(int hk, String key) {
    return null;
  }
}
