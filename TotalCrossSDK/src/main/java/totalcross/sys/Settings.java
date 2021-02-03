// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import java.io.File;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.ui.IVirtualKeyboard;
import totalcross.util.Hashtable;

/** this class provides some preferences from the device configuration and other Vm settings.
 * All settings are read-only, unless otherwise specified. Changing their values may cause
 * the VM to crash.
 */

public final class Settings {
  /**
   * Field that represents the version of the TotalCross Virtual Machine. The major version is
   * base 100. For example, version 1.0 has value 100. version 4 has a
   * version value of 400. A beta 0.81 VM will have version 81.
   */
  public static int version = 702;

  /** Field that represents the version in a string form, like "1.36". Only digits and dot is allowed or an exception will be throws during tc.Deploy. */
  public static String versionStr = "7.0.2";

  /** Current build number for the TotalCross SDK.
   * @since TotalCross 1.53 
   */
  public static int buildNumber = 000;

  /** Some properties you may want to use in the application. 
   * The file format must be:
   * <pre>
   * property=value&lt;enter&gt;
   * </pre>
   * This file must be named <code>tcapp.prop</code> and it is searched from the current folder up to the root folder.
   * 
   * To have a build number be incremented by tc.Deploy, just create the file somewhere in a parent folder from
   * where you run tc.Deploy for your application. 
   * The file will be inserted into the application's tcz and you will be able to retrieve the build number using
   * <code>Settings.appBuildNumber()</code>.
   * 
   * Note that the properties are always read-only and you can add other values to be used in your application.
   * @since TotalCross 3.1
   */
  public static Hashtable appProps;

  /** Returns the application's build number. You can make tc.Deploy use this automatically by
   * making the appVersion end with a dot. For example:
   * <pre>
   * static
   * {
   *    Settings.appVersion = "2.1."; // leading dot
   * }
   * // then at the application's constructor:
   * Settings.appVersion += Settings.appBuildNumber();
   * </pre> 
   * This way, when you run tc.Deploy, it will update the appVersion to include the build number in all platforms.
   * @see #appProps
   * @since TotalCross 3.1
   */
  public static String appBuildNumber() {
    String ret = Settings.appProps == null ? null : (String) Settings.appProps.get("build.number");
    if (ret == null) {
      ret = "0";
    }
    return ret;
  }

  /** Can be one of the following constants: DATE_MDY, DATE_DMY, DATE_YMD; where m = month, d = day and y = year
   * @see #DATE_DMY
   * @see #DATE_MDY
   * @see #DATE_YMD
   */
  public static byte dateFormat;
  /** The date char separator. */
  public static char dateSeparator;
  /** The week day start. 0 = Sunday, 6 = Saturday */
  public static byte weekStart;
  /** True if the time format is 24 hour format or if it is the AM/PM format */
  public static boolean is24Hour;
  /** The time char separator */
  public static char timeSeparator;
  /** The thousands separator for numbers */
  public static char thousandsSeparator;
  /** The decimal separator for numbers */
  public static char decimalSeparator;

  /** Field that represents the device's screen width */
  public static int screenWidth;
  /** Field that represents the device's screen height */
  public static int screenHeight;
  /** Field that represents the device's screen horizontal pixels density, in dots per inch (DPI). Note that this value can be incorrect in many devices. */
  public static int screenWidthInDPI;
  /** Field that represents the device's screen vertical pixels density, in dots per inch (DPI). Note that this value can be incorrect in many devices. */
  public static int screenHeightInDPI;
  /** Field that represents if the device supports color. 
   /** Field that represents the screen's number of bits per pixel.
   * @since TotalCross 1.0
   */
  public static int screenBPP;
  /** Field that defines if running in Java Standard Edition
   * (ie, in Eclipse or java in your desktop or even on an applet in a browser) instead of a handheld device. */
  public static boolean onJavaSE;
  /** Field that returns the ROM version of the device, like 0x02000000 or 0x03010000.
   * For Android, returns the version in decimal number (E.G.: 3.16 is returned as 316). 
   */
  public static int romVersion = 0x02000000;

  /** Underlying platform is Java. To be used with the <code>platform</code> member. */
  public static final String JAVA = "Java";
  /** Underlying platform is Windows CE. To be used with the <code>platform</code> member. */
  public static final String WINDOWSCE = "WindowsCE";
  /** Underlying platform is Pocket PC. To be used with the <code>platform</code> member. */
  public static final String POCKETPC = "PocketPC";
  /** Underlying platform is Windows Mobile. To be used with the <code>platform</code> member. */
  public static final String WINDOWSMOBILE = "WindowsMobile";
  /** Underlying platform is desktop Windows. To be used with the <code>platform</code> member. */
  public static final String WIN32 = "Win32";
    /**
     * Underlying platform is Windows Phone. To be used with the
     * <code>platform</code> member.
     * @deprecated
     */
  @Deprecated
  public static final String WINDOWSPHONE = "WindowsPhone";
  /** Underlying platform is Linux. To be used with the <code>platform</code> member. */
  public static final String LINUX = "Linux";
  /** Underlying platform is iPhone. To be used with the <code>platform</code> member. */
  public static final String IPHONE = "iPhone";
  /** Underlying platform is Android. To be used with the <code>platform</code> member. */
  public static final String ANDROID = "Android";
  /** Underlying platform is iPad. To be used with the <code>platform</code> member. */
  public static final String IPAD = "iPad";
  /** Underlying platform is Linux ARM. To be used with the <code>platform</code> member. */  
  public static final String LINUX_ARM = "Linux_ARM";

  /** Field that returns the current platform name.
   * The possible return values are the constants described below.
   * @see #JAVA         
   * @see #WINDOWSCE    
   * @see #POCKETPC     
   * @see #WINDOWSMOBILE
   * @see #WIN32        
   * @see #IPAD
   * @see #LINUX        
   * @see #IPHONE       
   * @see #ANDROID      
   * @see #isWindowsCE()
   * @see #isIOS()
   * @see #LINUX_ARM
   */
  public static String platform;

  /**
   * Field that returns the username of the user running the Virtual Machine. Because of
   * Java's security model, this method will return null when called in a Java
   * applet. This method will also return null under most WinCE devices (that
   * will be fixed in a future release). In Windows 32, this will return the currently logged in user.
   */
  public static String userName;

  /** Application defined settings, stored as a byte array. If you set the value of this app to something
   * other than null, the VM will save it when exiting and load it when restarting. Under PalmOS, the value
   * is stored in the unsaved preferences database, which is not backed up during hot-sync.
   * Use this to save small amount of data, up to 2 or 4 kb maximum. At desktop, a file named
   * settings4crtr.pdb stores the appSettingsBin for the current running TotalCross programs.
   * Here's a sample of how to use it:
   * <pre>
   *  if (Settings.appSettingsBin == null)
   *  {
   *     add(new Label("empty"),CENTER,CENTER);
   *     ByteArrayStream bas = new ByteArrayStream(100);
   *     DataStream ds = new DataStream(bas);
   *     ds.writeDouble(1234.567);
   *     Settings.appSettingsBin = bas.getCopy();
   *  }
   *  else
   *  {
   *     ByteArrayStream bas = new ByteArrayStream(Settings.appSettingsBin);
   *     DataStream ds = new DataStream(bas);
   *     double d = ds.readDouble();
   *     add(new Label("d = "+Convert.toString(d,3)),CENTER,CENTER);
   *     Settings.appSettingsBin = null;
   *  }
   * </pre>
   * This property is saved only at application's exit; you can force an update by calling <code>Settings.refresh()</code>.
   * @see #refresh()
   */
  public static byte[] appSettingsBin;

  /** Application defined settings. If you set the value of this app to something other than null,
       the VM will save it when exiting and load it when restarting. Under PalmOS, the value is stored in the
       unsaved preferences database, which is not backed up during hot-sync.
       Use this to save small strings, up to 2 or 4 kb maximum.
       At desktop, a file named settings4crtr.pdb stores the appSettings for the current running TotalCross programs.
  
   * This property is saved only at application's exit; you can force an update by calling <code>Settings.refresh()</code>.
   * @see #refresh()
   */
  public static String appSettings;

  /** Application defined secret key. If you set the value of this app to something other than null,
       the VM will save it when exiting and load it when restarting. Under PalmOS, the value is stored in the
       saved preferences database, which is backed up during hotsync.
       Use this to save small strings, up to 2 or 4 kb maximum.
       At desktop, a file named settings4crtr.pdb stores the appSettings for the current running TotalCross programs.
       The String is stored in the saved preferences with a creator id different of your application's (but
       calculated based in it), so it will never be deleted and will be restored even after a hard-reset.
       <p>In Windows CE, the key is stored in the registry.
  
   * This property is saved only at application's exit; you can force an update by calling <code>Settings.refresh()</code>.
   * @see #refresh()
   */
  public static String appSecretKey;

  /** The application's ID. MUST BE CHANGED IN THE STATIC INITIALIZER! Used by the deployer and by the virtual machine.
   * 
   * In Palm OS, all data created is assigned to an application id. So when a program is uninstalled, all of its data
   * is also removed from device.
   * 
   * In Android, when the single package option is passed to tc.Deploy (/p), the application's package is changed to 
   * totalcross.app&lt;application id&gt;.
   * 
   * It must have 4 letters or digits, and the first character must be a letter.
   */
  public static String applicationId;

  // Not set by the VM

  /** Defines a Windows CE user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   * @deprecated Use Flat, Vista or Android. This user interface does not work on TotalCross 2.
   */
  @Deprecated
  public static final byte WinCE = 0;
  /** Defines a PalmOS user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   * @deprecated Use Flat, Vista or Android. This user interface does not work on TotalCross 2. 
   */
  @Deprecated
  public static final byte PalmOS = 1;
  /** Defines a FLAT user interface style, like the ones used in Pocket PC 2003. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   * @deprecated Use FLAT_UI instead
   */
  @Deprecated
  public static final byte Flat = 2;
  /** Defines a Windows Vista user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   * @deprecated Use VISTA_UI instead
   */
  @Deprecated
  public static final byte Vista = 3; // guich@573_6
  /** Defines an Android user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   * @deprecated Use ANDROID_UI instead
   */
  @Deprecated
  public static final byte Android = 4; // guich@tc130
  /** Defines an Holo user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   * @deprecated Use HOLO_UI instead
   */
  @Deprecated
  public static final byte Holo = 5; // guich@tc130

  /** Defines an user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   */
  public static final byte Material = 6; // guich@20170527

  /** Defines a FLAT user interface style, like the ones used in Pocket PC 2003. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   */
  public static final byte FLAT_UI = 2;
  /** Defines a Windows Vista user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   */
  public static final byte VISTA_UI = 3; // guich@573_6
  /** Defines an Android user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   */
  public static final byte ANDROID_UI = 4; // guich@tc130
  /** Defines an Holo user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   */
  public static final byte HOLO_UI = 5; // guich@tc130
  /** Defines an user interface style. Used in the uiStyle member.
   * @see totalcross.ui.MainWindow#setUIStyle(byte)
   */
  public static final byte MATERIAL_UI = 6; // guich@20170527

  /** Field that stores the current user interface style.
   * It must be set by calling Settings.setUIStyle.
   * @see #FLAT_UI
   * @see #VISTA_UI
   * @see #ANDROID_UI
   * @see #HOLO_UI
   * @see #MATERIAL_UI
   */
  public static byte uiStyle = VISTA_UI;

  /** Constant used in dateFormat: month day year */
  public static final byte DATE_MDY = 1;
  /** Constant used in dateFormat: day month year */
  public static final byte DATE_DMY = 2;
  /** Constant used in dateFormat: year month day */
  public static final byte DATE_YMD = 3;

  /** 
   * Field that represents if the device is in daylight savings mode.
   * @since SuperWaba 3.4
   * @deprecated Use daylightSavingsMinutes
   */
  @Deprecated
  public static boolean daylightSavings;

  /**
   * Daylight savings minutes to add; will be 0 when not in daylight savings.
   * To compute the difference to GMT+0, use:
   * <pre>
   * int dif = Settings.timeZoneMinutes + Settings.daylightSavingsMinutes;
   * int hours = dif / 60;
   * int minutes = Math.abs(dif % 60);
   * </pre>
   * @since TotalCross 2.0
   */
  public static int daylightSavingsMinutes;

  /**
   * Field that represents the timezone used for this device. This is the number of hours
   * away from GMT (E.g.: for Brazil it will return -3).
   * @since SuperWaba 3.4
   * @deprecated Use timeZoneMinutes
   */
  @Deprecated
  public static int timeZone;

  /**
   * Timezone in minutes. Some countries have a timezone difference in minutes, like Iran with 3:30.
   * In this example, timeZoneMinutes will be 210.
   * To compute the difference to GMT+0, use:
   * <pre>
   * int dif = Settings.timeZoneMinutes + Settings.daylightSavingsMinutes;
   * int hours = dif / 60;
   * int minutes = Math.abs(dif % 60);
   * </pre>
   * @since TotalCross 2.0
   */
  public static int timeZoneMinutes;

  /** True if this handheld has a virtual keyboard, I.E., like the soft input panel in windows ce devices or in the Tungsten T|X. */
  public static boolean virtualKeyboard;

  /**
   * Specifies the directory where pdbs should be read/written.
   * Used in desktop when:
   * <ol>
   * <li>Running under Win32 VM. Can be specified anywhere.
   * <li>Running under JDK, as application (java.exe). Can also be specified with the <code>/dataPath</code> commandline parameter.
   * </ol>
   * Some warnings:
   * <ul>
   * <li>If this member is not null, any paths specified for the file are replaced by this path.
   * <li>Slashes are normalized by the vm.
   * <li>The path existence is not verified on Win32.
   * <li>Changing this member also changes the place where local libraries are loaded (global libraries are loaded when the VM starts, before the app is initialized). So, before loading any library, change this member back to <code>null</code> (unless the library is also in the dataPath).
   * <li>The system ensures that the datapath ends with a slash, if you don't put one.
   * </ul>
   * @since SuperWaba 4.11 (Win32) and 2.0beta4 (JDK)
   */
  public static String dataPath;

  /** Field that represents the ROM serial number for this device.
   * <b>Important!</b>: don't rely only on this information to uniquely identify
   * a device! Its very easy to change this number, and it won't be available in
   * all devices.
   * It will contain <code>null</code> if unavailable, such as in
   * Windows CE 2.11 devices, Palm OS &lt; 3.0, and even some Palm OS 5 devices,
   * like Treo 6xx.
   * <p>Note: this was correctly tested; you can ensure that your Palm OS device
   * has or not a serial number pressing App/Info/Version: the serial number
   * will appear in the top; if nothing appears, it means that the device doesn't
   * has a serial number. If it appears but nothing is returned from here, it means
   * that the device has a non-standard function that retrieves the serial number,
   * and thus we don't support it.
   * 
   * In Android 3.0 or greater it will return the serial number; in Android 1.x and 2.x will return
   * a consistent number that MAY be the same across resets, but has no relation to the real serial number.
   * 
   * @since SuperWaba 4.21
   */
  public static String romSerialNumber;

  /** The macAddress currently in use.
   * Currently works only on Android.
   * 
   * @deprecated due to provide users with greater data protection
   */
  @Deprecated
  public static String macAddress;

  /** Field that represents if the PDA has a password and the user choosen to hide the secret records
   * (via the Apps/Security/Current Privacy).
   * Returns true in all other cases.
   * Specific for Palm OS (Windows CE does not let the user hide records). This can be used to let the Operating System do the <i>login</i>
   * for a program that requires privacy.
   * @since SuperWaba 4.21
   * @deprecated This was only used in Palm OS, which is now unsupported
   */
  @Deprecated
  public static boolean showSecrets = true; // guich@421_35

  /** Defines if the arrow keys will be used to change the focus using the keyboard. Note that some controls
   * will behave differently. It will be true for pen less devices, and it may be changed
   * if your application wants to use navigation keys to change the focus between the controls.
   * @since SuperWaba 5.5
   */
  public static boolean keyboardFocusTraversable; // guich@550_15

  /** Field that represents a device id that can be used to identify the device's name.
   * For Palm OS devices, see: http://www.mobilegeographics.com/dev/devices.php
   * and http://homepage.mac.com/alvinmok/palm/codenames.html.
   * For Pocket PC devices, it will return the device's name.
   * For Win32 VM, it will return the computer name.
   * For iPod & iPhone devices, values are : "i386" on simulator, "iPod1,1" on iPod Touch, "iPhone1,1" on iPhone,
   *     "iPhone1,2" on iPhone 3G, and "iPhone2,1" on iPhone 3GS, as listed in the "Model identifier" row at
   *     http://en.wikipedia.org/w/index.php?title=List_of_iPhone_and_iPod_Touch_models&oldid=319641529
   * If the device can't be identified, it will contain <code>null</code>.
   */
  public static String deviceId; // guich@568_2

  /** The main path used to derive <code>tempPath</code> and <code>appPath</code> and also
   * to store TotalCross global files. This is a read-only property: changing it will have
   * no effect.
   * @since TotalCross 1.0
   */
  public static String vmPath;

  /** The path from where the application is running from. In Palm OS devices, it will be null.
   * This is the default location where the PDBFiles are created. This is a read-only property:
   * changing it will have no effect.
   * @see #dataPath
   * @since SuperWaba 5.81
   */
  public static String appPath = initAppPath();

  static String initAppPath() {
    String basePath = null;
    try {
      basePath = System.getProperty("user.dir");
      if (basePath != null && basePath.indexOf(File.separatorChar) >= 0 && !basePath.endsWith(File.separator)) {
        basePath += File.separator;
      }
    } catch (SecurityException se) {
    }
    return basePath;
  }

  static String initAppPath4D() {
    return null;
  }

  /** To be used in the closeButtonType. Will remove the x/ok button from screen on Windows CE devices. 
   * In Windows 32, the X button will still be visible, but clicking on it will not close the application;
   * instead, the SpcialKeys.MENU key event will be sent to the application.
   */
  public static final int NO_BUTTON = 0;
  /** To be used in the closeButtonType. An OK button is placed, and the application is closed when its pressed. */
  public static final int CLOSE_BUTTON = 1;
  /** To be used in the closeButtonType. A X button is placed, and the application is minimized when its pressed. */
  public static final int MINIMIZE_BUTTON = 2;

  /** Set it at the application's static initializer.
   * If set to MINIMIZE_BUTTON, replaces the ok button in Windows CE devices, which closes the application,
   * by the x one, which just minimizes the application. The ok closes the application, and the
   * x minimizes it. By default, it is CLOSE_BUTTON, which closes the application.
   *
   * @since TotalCross 1.11
   * @see #CLOSE_BUTTON
   * @see #MINIMIZE_BUTTON
   * @see #NO_BUTTON
   */
  public static int closeButtonType = CLOSE_BUTTON;

  /** Set it at the application's static initializer. Makes the application full screen.
   * Only a simple assignment to true is supported; you cannot check the platform nor any other attribute of this class
   * because the static initializer is called BEFORE this class have its fields set. Also, some platforms set it at Deploying time.
   * Note that Android applications have problems with the keyboard when its fullscreen.
   * @since TotalCross 1.0
   * @see #fullScreenPlatforms
   */
  public static boolean isFullScreen;

  /** Set this to a list of platforms that will set the fullscreen mode. If this member is null, all platforms will have
   * full screen set to the isFullScreen member. Here's a sample, to use full screen on windows devices (but not windows desktop):
   * <pre>
   * static
      {
         Settings.isFullScreen = true;
         Settings.fullScreenPlatforms = Settings.WINDOWSCE+","+Settings.WINDOWSMOBILE;
      }
   * </pre>
   * You can use any separator. The only platform that does not work with this is JAVA.
   * 
   * You must provide a String like the one above, concatenating the platform Strings in a single line. 
   * Using a StringBuffer or anything else may result in incorrect results.
   * 
   * @see #isFullScreen
   * @since TotalCross 1.2
   */
  public static String fullScreenPlatforms; // guich@tc120_59

  /** Set it at the application's static initializer. Defines the current application's version, which will be applied to the pdbs.
   * 
   * If you turn on automatic build number increment (with appProps) and the appVersion ends with a dot, during deploy the
   * build number will be appended to the appVersion. For example, if appVersion is "2." and the current build number is 103, 
   * deploy will change the appVersion to "2.103" during deploy. To see the version that will appear, use at your application's
   * constructo: <code>Settings.appVersion += Settings.appBuildNumber();</code>
   * @since TotalCross 1.0
   */
  public static String appVersion;

  /** Set it at the application's static initializer; should not contain spaces. Defines the company's information, which is used in iOS, Windows CE.
   * In iOS is used to form the bundle suffix id. 
   * @since TotalCross 1.0
   */
  public static String companyInfo;

  /** Set it at the application's static initializer. Defines the user's or company's contact email.
   * @since TotalCross 1.0
   */
  public static String companyContact;

  /** Set it at the application's static initializer. Defines the application's description.
   * @since TotalCross 1.0
   */
  public static String appDescription;

  /** Set it at the application's static initializer. Defines the application's package identifier.
   */
  public static String appPackageIdentifier;

  /** Set it at the application's static initializer. Defines the application's package publisher.
   */
  public static String appPackagePublisher;

  /** Set it at the application's static initializer. Defines the application's package id, which is used in iOS. */
  public static String iosCFBundleIdentifier;

  /** @deprecated No longer used.
   * @since TotalCross 1.0
   */
  @Deprecated
  public static String appLocation;

  /** @deprecated No longer used.
   * @since TotalCross 1.0
   */
  @Deprecated
  public static String appCategory;

  /** Set it at the application's static initializer. Specifies a URI to a WebService to be used by the VM activation process.<br>
   * See the TotalCross Companion for more information about this feature.  
   * @since TotalCross 1.15 
   */
  public static String activationServerURI;

  /** Set it at the application's static initializer. Specifies a namespace for the WebService provided by the user.<br>
   * See the TotalCross Companion for more information about this feature. 
   * @since TotalCross 1.15 
   */
  public static String activationServerNamespace;

  /** Set to false to hide all messages that are sent to the console when running at the desktop.
   * Note that doing so may hide important messages! Do it with caution.
   * @since TotalCross 1.0
   */
  public static boolean showDesktopMessages = true;
  /** set true if you wanna see all the debug messages
   * 
   *
   */
  public static boolean showDebugMessages=false;
  /**
   * Field that represents the smartphone IMEI (if this device is a GSM, UMTS or
   * IDEN smartphone), or null if there's none.
   * 
   * @since TotalCross 1.0
   * @deprecated This field was deprecated and it may be undefined or hold an
   *             invalid value.
   */
  @Deprecated
  public static String imei;

  /**
   * Field that represents the smartphone IMEIs; used in phones with more than one
   * line.
   * 
   * @since TotalCross 1.0
   * @deprecated This field was deprecated and it may be undefined or hold an
   *             invalid value.
   */
  @Deprecated
  public static String[] imeis;

  /**
   * Field that represents the smartphone ESN (if this device is a CDMA
   * smartphone) or null if there's none.
   * 
   * @since TotalCross 1.0
   * @deprecated This field was deprecated and it may be undefined or hold an
   *             invalid value.
   */
  @Deprecated
  public static String esn;

  /**
   * Field that represents the serial number of the GSM chip or null if there's
   * none. Works for Windows Mobile and Android.
   * 
   * @since TotalCross 1.27
   * @deprecated This field was deprecated and it may be undefined or hold an
   *             invalid value.
   */
  @Deprecated
  public static String iccid;

  /** If set to true, the application will ignore the event "Close application" issued by the operating system.
   *  This is specially useful to avoid that an incoming call exits the application on Palm OS.
   *  You must provide a way to terminate the application by calling the exit method.
   *  @see totalcross.ui.MainWindow#exit(int)
   *  @since TotalCross 1.0
   *  @deprecated Not used in any platform
   */
  @Deprecated
  public static boolean dontCloseApplication;

  /**
   * When directional keys are used to change focus, the next control to receive focus will be determined based on
   * their physical location on the screen. Also, when geographicalFocus is true, the highlight/action key model
   * will not be used. Instead, controls will be highlighted and given focus immediately.
   */
  public static boolean geographicalFocus; // kmeehl@tc100

  /**
   * Setting this field to true allows the execution of multiple instances of the same application.
   * When the application is started, it first checks if there is a running instance of the same application. If so, the
   * running instance is moved to the foreground and the starting application exits.
   * This only works for Win32 and WinCE.
   * @since TotalCross 1.0
   */
  public static boolean multipleInstances;

  /** Set to false to disable the default circular navigation when using the arrows: it will stop at the first or the last item.
   * It applies to: ListBox, MenuBar, ComboBoxEditable, Grid, MenuBarDropDown.
   * @since TotalCross 1.13
   */
  public static boolean circularNavigation = true; // guich@tc113_41

  /** Number of times that the GC ran. Updated by the VM during the application execution.
   * @since TotalCross 1.14
   */
  public static int gcCount;

  /** How much time the GC took in all runs. Updated by the VM during the application execution.
   * @since TotalCross 1.14
   */
  public static int gcTime;

  /** Number of memory chunks (blocks of memory that store Objects) created. Updated by the VM during the application execution.
   * @since TotalCross 1.14
   */
  public static int chunksCreated;

  /** Time when the user last interacted with the device using the keyboard, pen, trackball, etc.
   * @since TotalCross 1.14
   */
  public static int lastInteractionTime;

  /** Set this flag to false (default is true) to don't display the memory errors when the program exits.
   * The possible errors are:
   * <ul>
   * <li> Memory error: out of memory when allocating
   * <li> Memory leak: memory was not freed properly
   * </ul>
   * It is highly recommended that you keep this flag on while debugging your programs, but you may not
   * want to let it on when deploying your application to your costumers.
   * <br>Note that the log messages will still be dumped to the console. 
   * @since TotalCross 1.14
   */
  public static boolean showMemoryMessagesAtExit = true; // guich@tc114_44

  /** Shows the mouse position in the Window's title when running as Java SE application.
   * This greatly helps the creation of UIRobot's user interface unit tests (using absolute coordinates to simulate the events).
   * @since TotalCross 1.15
   */
  public static boolean showMousePosition;

  /** Makes the generation of user interface tests much easier by using the built-in 
   * User Interface Robot. 
   * <ul>
   * <li> To enable it at device, you must assign a special key that will open
   * the interface.
   * <li> To enable it at desktop (Java SE), you must press control+1.
   * </ul>
   * For example, if you want to set the SpecialKeys.FIND to be the one that will open the UIRobot, do:
   * <pre>
   * Vm.interceptSpecialKeys(new int[]{SpecialKeys.FIND});
   * Settings.deviceRobotSpecialKey = SpecialKeys.FIND;
   * </pre>
   * @since TotalCross 1.3
   */
  public static int deviceRobotSpecialKey;

  /** Set to false to don't display the timestamp before each Vm.debug output.
   * @since TotalCross 1.15
   */
  public static boolean showDebugTimestamp = true; // guich@tc115_50

  /**
   * Textual description of the time zone currently used by the device. This value is completely
   * platform dependent, and may be localized for the platform current settings.
   * 
   * @since TotalCross 1.15
   */
  public static String timeZoneStr; //flsobral@tc115_54: added field Settings.timeZoneStr

  /** Defines platforms that the touchscreen is used MOSTLY with the finger.
   * We say <i>mostly</i> because there are special pens that can be used with iPhone; however, we consider this an exception, not the rule.
   * Currently this value is true for iPhone and Android platforms.
   * <br><br>
   * When fingerTouch is true, all controls that can scroll, like ListBox, Grid, ScrollContainer, MultiEdit, etc, 
   * will have the flick and drag enabled and the ScrollBar will be replaced by the ScrollPosition.
   * @since TotalCross 1.2
   */
  public static boolean fingerTouch; // guich@tc120_32

  /** Defines a touch tolerance to find the closest control. Used in fingerTouch devices.
   * You can disable it setting this property to 0.
   * @since TotalCross 1.2
   */
  public static int touchTolerance; // guich@tc120_48

  /** Default fadeOtherWindows value used in the totalcross.ui.dialog windows. 
   * @since TotalCross 1.2 */
  public static boolean fadeOtherWindows = true;

  /** Set to true to debug all events that are received at Window._postEvent. */
  public static boolean debugEvents; // guich@tc125_14

  /**
   * <b>READ-ONLY</b> unique identifier available for registered applications after the TotalCross VM is activated.<br>
   * Value defaults to "NOT AVAILABLE" when running on DEMO, and "NO ACTIVATION" when using the new licensing model.
   * 
   * @since TotalCross 1.25
   */
  public static String activationId = "NOT AVAILABLE";

  /** Returns true if the current platform is Windows Mobile or Windows Phone. Note that Windows Desktop (aka WIN32)
   * returns false.
   */
  public static boolean isWindowsDevice() {
    return POCKETPC.equals(platform) || WINDOWSCE.equals(platform) || WINDOWSMOBILE.equals(platform);
  }

  /** Returns true if the current platform is Windows Mobile or Windows Phone. Note that Windows Desktop (aka WIN32)
   * returns false.
   */
  public static boolean isWindowsCE() {
    return POCKETPC.equals(platform) || WINDOWSCE.equals(platform) || WINDOWSMOBILE.equals(platform);
  }

  /** Returns true if this is an iPad or an iPhone.
   * @since TotalCross 1.53
   */
  public static boolean isIOS() {
    return IPAD.equals(platform) || IPHONE.equals(platform);
  }

  /** Refresh some fields thay may have been updated since the program 
   started.
   * The currently refreshed fields are:
   * <ul>
   * <li> daylightSavings
   * <li> timeZone
   * <li> timeZoneStr
   * </ul>
   * Also saves the appSettings properties, which otherwise is saved only at program's end.
   * @since TotalCross 1.15
   */
  @ReplacedByNativeOnDeploy
  public static void refresh() {
    totalcross.Launcher.instance.settingsRefresh(true);
  }

  /** Set to true to enable the vibration when a MessageBox appears on screen (only on device).
   * @since TotalCross 1.22
   */
  public static boolean vibrateMessageBox; // guich@tc122_51

  /** Set to true to disable screen rotation. Works only in JavaSE.
   * 
   * If you need this feature on the device, override the screenResized method in your MainWindow
   * and add something like:
   * <pre>
      public void screenResized()
      {
         if (Settings.isLandscape())
         {
            // make sure that the MessageBox takes the whole screen
            MessageBox mb = new MessageBox("Attention","This program must be run in portrait mode.\nPlease rotate the device.",null)
            {
               public void setRect(int x, int y, int w, int h)
               {
                  super.setRect(x,y,Settings.screenWidth,Settings.screenHeight);
               }
            };
            mb.transitionEffect = TRANSITION_NONE;
            mb.popupNonBlocking();
            while (Settings.isLandscape())
               pumpEvents();
            mb.unpop();
         }
         else super.screenResized();
      }
   * </pre>
   * @since TotalCross 1.27
   */
  public static boolean disableScreenRotation; // guich@tc126_2

  /** Set to true to move an Edit or MultiEdit to the top of the screen 
   * if the application is running in a platform
   * that does not support moving the Soft Input Panel to the top. Otherwise, the SIP (Soft Input Panel) will be placed
   * on top of the Edit.
   * You must set this in the MainWindow's constructor, never in the static block.
   * @see #SIPBottomLimit
   * @see totalcross.ui.UIColors#shiftScreenColor
   * @since TotalCross 1.3
   */
  public static boolean unmovableSIP; // guich@tc126_21

  /** The size in pixels of the device's system font. */
  public static int deviceFontHeight;

  /** Set to true to put the cursor at the end of the Edit and MultiEdit when focus was set to the control 
   * (default is at the end).
   * @since TotalCross 1.3
   */
  public static boolean moveCursorToEndOnFocus = true;

  /** The limit that will make the Soft Input Panel be placed at bottom. 
   * If the control's absolute rect is &lt; this value,
   * the SIP will stay at the bottom of the screen (otherwise, it will be moved to the top).
   * 
   * Before TotalCross 1.3, this value used to be the half of the screen, but since in some new Windows
   * Mobile the SIP is very tall (specially in landscape mode), we added this field so you can change it if desired.
   * 
   * For example, to set to 5 times the font's height, do:
   * <pre>
   * Settings.SIPBottomLimit = 5 * fmH;
   * </pre>
   * Setting it to -1 (default value) will use half the current screen height.
   * 
   * This field is used in Windows CE devices only.
   * @since TotalCross 1.3
   */
  public static int SIPBottomLimit = -1;

  /** Set to true to make the extra adjustment values used in the relative positioning be a percentage 
   * of the control's font height.
   * 
   * In modern devices, a single pixel can have different sizes in inches (or, in other words, the devices
   * have different screen densities). So, something like PREFERRED+4 in a 320x480 device with 160 DPI (DIPS
   * PER INCH - or pixels per inch) will have the half size of a device with the same resolution but 320 DPI.
   * 
   * Since the font sizes change according to the DPI and not the resolution, its good to change the relative
   * positioning to use a percentage of the font's height instead of absolute pixels.
   * 
   * By setting this flag to true will make the adjustment a PERCENTAGE of the font's height.
   * 
   * So, you can use something like PREFERRED+50 (50% of font's height), SAME+150 (150% of font's height),
   * and so on.
   * 
   * It is possible to disable the adjustment for a single Control, Container or Window, using 
   * Control.uiAdjustmentsBasedOnFontHeightIsSupported.
   * @see totalcross.ui.Control#uiAdjustmentsBasedOnFontHeightIsSupported
   */
  public static boolean uiAdjustmentsBasedOnFontHeight;

  /** Set to true to post a PRESSED event when an item is programatically selected or changed.
   * 
   * Works with the following controls: Edit, MultiEdit, Check, Radio, ComboBox, ListBox, SpinList.
   * 
   * Usually this event only occurs when the user selects an item, not when
   * you call setSelectedItem/Index
   * @since TotalCross 1.5
   */
  public static boolean sendPressEventOnChange;

  /** Set to true to make the program's window resizable in Windows and desktop.
   * @since TotalCross 1.53
   */
  public static boolean resizableWindow;

  /** Used in the windowSize field. */
  public static final int WINDOWSIZE_320X480 = 1;
  /** Used in the windowSize field. */
  public static final int WINDOWSIZE_480X640 = 2;
  /** Used in the windowSize field. */
  public static final int WINDOWSIZE_600X800 = 3;

  /** Defines the window size when running in a desktop computer (the default is 240x320).
   * Must be set in the static initializer.
   * If used, the window will be centered on screen with the given resolution.
   * @since TotalCross 1.53
   * @see #WINDOWSIZE_320X480
   * @see #WINDOWSIZE_480X640
   * @see #WINDOWSIZE_600X800
   * @see #resizableWindow
   */
  public static int windowSize;

  /** Used in the windowFont field; sets the size to 12. */
  public static final int WINDOWFONT_12 = 0;
  /** Used in the windowFont field; sets the size to the one defined by user. */
  public static final int WINDOWFONT_DEFAULT = 1;

  /** Defines the window font size when running in a desktop computer.
   * Must be set in the static initializer.
   * @deprecated Use at the application's constructor: if (Settings.platform.equals(Settings.WIN32)) setDefaultFont(Font.getFont(false,NN)); where NN is the desired font size
  
   * @since TotalCross 1.53
   * @see #WINDOWFONT_12
   * @see #WINDOWFONT_DEFAULT
   * @see #windowSize
   * @see #resizableWindow
   */
  @Deprecated
  public static int windowFont;

  /**
   * Returns the line number of the device. Note that if the phone is off it may
   * return null. It can be null also if the device uses a non-standard API. Works
   * only on Android, since iOS does not allow to get it programatically. For
   * dual-sim devices, returns only the first line number.
   * 
   * @since TotalCross 1.7 / 2.0
   * @deprecated This field was deprecated and it may be undefined or hold an
   *             invalid value.
   */
  @Deprecated
  public static String lineNumber;

  /** Returns true if the device is currently in landscale (screenWidth > screenHeight). */
  public static boolean isLandscape() {
    return screenWidth > screenHeight;
  }

  /**
   * Returns true if this is an open gl platform (IOS or Android).
   * 
   * @since TotalCross 1.68
   */
  public static boolean isOpenGL;

  /** An optional value for the backspace key. Android 4.4.2 has a bug that prevents the backspace from working well;
   * this bug is fixed in 4.4.3. The workaround is to define a unused key that will work as the backspace one.
   * Defaults to the î key, used only if romVersion is 442.
   */
  public static int optionalBackspaceKey = Settings.romVersion == 442 ? '\u00EE' : 0;

  /** Set to false to disable the scroll optimization using images. This optimization
   * greatly improves performance but uses more memory.
   */
  public static boolean optimizeScroll = Settings.isOpenGL;

  // this class can't be instantiated
  private Settings() {
  }

  /** Dumb field to keep compilation compatibility with TC 1 */
  public static String PALMOS = "PalmOS";
  /** Dumb field to keep compilation compatibility with TC 1 */
  public static String BLACKBERRY = "BlackBerry";
  /** Dumb field to keep compilation compatibility with TC 1 */
  public static int nvfsVolume = -1;
  /** Dumb field to keep compilation compatibility with TC 1 */
  public static boolean useNewFont;
  /** Dumb field to keep compilation compatibility with TC 1 */
  public static boolean isMinimized;
  /** Dumb field to keep compilation compatibility with TC 1 */
  public static boolean keypadOnly;

  /** Set to 0 to disable the automatic scroll of the ScrollContainer under the
   * mouse position when the mouse wheel changes.
   */
  public static int scrollDistanceOnMouseWheelMove;

  /** An email that will be used to send bug reports when an unhandled exception is caught
   * on your application. Note that you may receive dozens of emails per day. The bug report
   * is sent after an application crash if, and only if, the application is called within 3 minutes
   * after the crash, otherwise, Android may roll out the logs. Note that we always receive a copy 
   * of the same message, even if you don't set this email, so we will be aware of the problems. The 
   * bug report takes from 5 to 20 seconds to be generated and sent from application's startup time, 
   * so if your application crashes too early, the report will halt the exit process until its effectively
   * sent, and a black screen will appear in the meanwhile.
   * 
   * You must set this in your application's static initializer, preferrably at the first line. E.G.:
   * <pre>
   * static
   * {
   *    Settings.bugreportEmail = "bugreports@mycompany.com";
   *    ...
   * }
   * </pre>
   * Currently this is supported only on Android.
   * 
   * @since TotalCross 3.1
   */
  public static String bugreportEmail;

  /** Set this to the username of your application to be able to filter at the bug report service.
   * IMPORTANT: this must be assigned by your application at the constructor of the application, or it will
   * be ignored in the crash report, because this one is sent very early.  
   * @since TotalCross 3.1
   */
  public static String bugreportUser;

  /** The activation key used during deploy */
  public static String activationKey;

  /** No longer used; put the 'google-services.json' so that the deploy will send it to the app, and it will*/
  @Deprecated
  public static String pushTokenAndroid;

  /** The due date of the iOS certificate. You can use it to inform your costumers when its time to update the software. */
  public static Time iosCertDate;

  /** Field set to true if the program have aborted on last run. */
  public static boolean abortedOnLastRun;

  public static IVirtualKeyboard customKeyboard;

  /** Set to false to disable the virtual keyboard on all Edits and MultiEdits at once
   * @since TotalCross 4.0
   */
  public static boolean enableVirtualKeyboard = true;

  /** Set to false to disable UI positional errors that are shown in Java SE */
  public static boolean showUIErrors = true;
  
  /**
   * On Android it has the value of Settings.Secure.ANDROID_ID.
   * Refer to the Android documentation for more details about this identifier:
   * https://developer.android.com/reference/android/provider/Settings.Secure.html#ANDROID_ID
   */
  public static final String ANDROID_ID = null;
  
  /**
   * The logical density of the display, used as a scaling factor for density
   * independent pixels (dp) and scaleable pixels (sp).
   * 
   * On WinCE based devices the screen density is set to 0.75 if the largest
   * screen component (width or height) is smaller than 240 pixels.
   * 
   * https://material.io/guidelines/layout/units-measurements.htm
   */
  public static double screenDensity = 1;
  /**
   * Minimal interval between two Update events
   */
  public static final int minimalUpdateInterval = 16;
  public static int getAnimationMaximumFps() {
     return (int)(1000 / (float)minimalUpdateInterval);
  }

    /**
     * Apps that target Android 6.0 (API level 23) or higher automatically
     * participate in Auto Backup. Set this value to enable or disable backup. The
     * default value is true but to make your intentions clear, we recommend
     * explicitly setting the attribute.
     * 
     * You can disable backups by setting allowBackup to false. You might want to do
     * this if your app can recreate its state through some other mechanism or if
     * your app deals with sensitive information that Android shouldn't back up.
     */
    public static boolean allowBackup = true;
}
