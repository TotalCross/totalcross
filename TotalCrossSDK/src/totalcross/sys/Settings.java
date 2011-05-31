/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
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

/** this class provides some preferences from the device configuration and other Vm settings.
 * All settings are read-only, unless otherwise specified. Changing their values may cause
 * the VM to crash.
 */

public final class Settings
{
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

   /** <b>READ-ONLY</b> variable that represents the device's screen width */
   public static int screenWidth;
   /** <b>READ-ONLY</b> variable that represents the device's screen height */
   public static int screenHeight;
   /** <b>READ-ONLY</b> variable that represents the device's screen horizontal pixels density, in dots per inch (DPI). Note that this value can be incorrect in many devices. */
   public static int screenWidthInDPI;
   /** <b>READ-ONLY</b> variable that represents the device's screen vertical pixels density, in dots per inch (DPI). Note that this value can be incorrect in many devices. */
   public static int screenHeightInDPI;
   /** <b>READ-ONLY</b> variable that represents if the device supports color. 
    * @deprecated Now all devices support color. */
   public static boolean isColor;
   /** <b>READ-ONLY</b> variable that represents the screen's number of bits per pixel.
    * @since TotalCross 1.0
    */
   public static int screenBPP;
   /** <b>READ-ONLY</b> variable that returns the number of colors supported by the device. 
    * @deprecated Use screenBPP instead. */
   public static int maxColors;
   /** <b>READ-ONLY</b> variable that defines if running in Java Standard Edition
    * (ie, in Eclipse or java in your desktop or even on an applet in a browser) instead of a handheld device. */
   public static boolean onJavaSE;
   /** <b>READ-ONLY</b> variable that returns the ROM version of the device, like 0x02000000 or 0x03010000. In desktop, return the oldest version of the destination platform where TotalCross can run. */
   public static int romVersion = 0x02000000;
   
   /** Underlying platform is Java. To be used with the <code>platform</code> member. */
   public static final String JAVA          = "Java";
   /** Underlying platform is Palm OS. To be used with the <code>platform</code> member. */
   public static final String PALMOS        = "PalmOS";
   /** Underlying platform is Windows CE. To be used with the <code>platform</code> member. */
   public static final String WINDOWSCE     = "WindowsCE";
   /** Underlying platform is Pocket PC. To be used with the <code>platform</code> member. */
   public static final String POCKETPC      = "PocketPC";
   /** Underlying platform is Windows Mobile. To be used with the <code>platform</code> member. */
   public static final String WINDOWSMOBILE = "WindowsMobile";
   /** Underlying platform is desktop Windows. To be used with the <code>platform</code> member. */
   public static final String WIN32         = "Win32";
   /** Underlying platform is Symbian. To be used with the <code>platform</code> member. */
   public static final String SYMBIAN       = "Symbian";
   /** Underlying platform is Linux. To be used with the <code>platform</code> member. */
   public static final String LINUX         = "Linux";
   /** Underlying platform is iPhone. To be used with the <code>platform</code> member. */
   public static final String IPHONE        = "iPhone";
   /** Underlying platform is BlackBerry. To be used with the <code>platform</code> member. */
   public static final String BLACKBERRY    = "BlackBerry";
   /** Underlying platform is Android. To be used with the <code>platform</code> member. */
   public static final String ANDROID       = "Android";
   /** Underlying platform is iPad. To be used with the <code>platform</code> member. */   
   public static final String IPAD          = "iPad";
   
   /** <b>READ-ONLY</b> variable that returns the current platform name.
    * The possible return values are:
    * <ul>
    * <li> Java
    * <li> PalmOS
    * <li> WindowsCE
    * <li> PocketPC
    * <li> WindowsMobile
    * <li> Win32
    * <li> Symbian
    * <li> Linux
    * <li> iPhone
    * <li> BlackBerry
    * <li> Android
    * </ul>
    * You can also use the constants provided in this class instead of the String values.
    * @see #JAVA         
    * @see #PALMOS       
    * @see #WINDOWSCE    
    * @see #POCKETPC     
    * @see #WINDOWSMOBILE
    * @see #WIN32        
    * @see #SYMBIAN      
    * @see #LINUX        
    * @see #IPHONE       
    * @see #BLACKBERRY   
    * @see #ANDROID      
    * @see #isWindowsDevice()
    */
   public static String platform;
   
   /**
   * <b>READ-ONLY</b> variable that returns the username of the user running the Virtual Machine. Because of
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
    */
   public static byte[] appSettingsBin;

   /** Application defined settings. If you set the value of this app to something other than null,
       the VM will save it when exiting and load it when restarting. Under PalmOS, the value is stored in the
       unsaved preferences database, which is not backed up during hot-sync.
       Use this to save small strings, up to 2 or 4 kb maximum.
       At desktop, a file named settings4crtr.pdb stores the appSettings for the current running TotalCross programs.
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
     */
   public static String appSecretKey;

   /** The application's ID. MUST BE CHANGED IN THE STATIC INITIALIZER! Used by the deployer and by the virtual machine.
    */
   public static String applicationId;

   // Not set by the VM

   /** <b>READ-ONLY</b> variable that stores the current user interface style.
    * It must be set by calling Settings.setUIStyle.
    * @see #PalmOS
    * @see #WinCE
    * @see #Flat
    * @see #Vista
    * @see #Android
    */
   public static byte uiStyle;
   
   /**
   * <b>READ-ONLY</b> variable that represents the version of the TotalCross Virtual Machine. The major version is
   * base 100. For example, version 1.0 has value 100. version 4 has a
   * version value of 400. A beta 0.8 VM will have version 80.
   * ps: Waba 1.0G will return 1.01. TotalCross = 110 (1.1) and beyond.
   */   // not declared final to prevent compile time optimizations!
   public static int version = 127;
   
   /** <b>READ-ONLY</b> variable that represents the version in a string form, like "2.0b4r8" */
   public static String versionStr = "1.27";

   /** Defines a Windows CE user interface style. Used in the uiStyle member.
    * @see totalcross.ui.MainWindow#setUIStyle(byte)
    */
   public static final byte WinCE = 0;
   /** Defines a PalmOS user interface style. Used in the uiStyle member.
    * @see totalcross.ui.MainWindow#setUIStyle(byte)
    */
   public static final byte PalmOS = 1;
   /** Defines a FLAT user interface style, like the ones used in Pocket PC 2003. Used in the uiStyle member.
    * @see totalcross.ui.MainWindow#setUIStyle(byte)
    */
   public static final byte Flat = 2;
   /** Defines a Windows Vista user interface style. Used in the uiStyle member.
    * @see totalcross.ui.MainWindow#setUIStyle(byte)
    */
   public static final byte Vista = 3; // guich@573_6
   /** Defines an Android user interface style. Used in the uiStyle member.
    * @see totalcross.ui.MainWindow#setUIStyle(byte)
    */
   public static byte Android = 4; // guich@tc130

   /** Constant used in dateFormat: month day year */
   public static final byte DATE_MDY = 1;
   /** Constant used in dateFormat: day month year */
   public static final byte DATE_DMY = 2;
   /** Constant used in dateFormat: year month day */
   public static final byte DATE_YMD = 3;

   /** <b>READ-ONLY</b> variable that represents if the device is in daylight savings mode.
     * @since SuperWaba 3.4
     */
   public static boolean daylightSavings;

   /** <b>READ-ONLY</b> variable that represents the timezone used for this device. This is the number of hours
     * away from GMT (E.g.: for Brazil it will return -3).
     * @since SuperWaba 3.4
     */
   public static int timeZone;

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

   /** <b>READ-ONLY</b> variable that represents the ROM serial number for this device.
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
     * @since SuperWaba 4.21
     */
   public static String romSerialNumber;

   /** <b>READ-ONLY</b> variable that represents if the PDA has a password and the user choosen to hide the secret records
     * (via the Apps/Security/Current Privacy).
     * Returns true in all other cases.
     * Specific for Palm OS (Windows CE does not let the user hide records). This can be used to let the Operating System do the <i>login</i>
     * for a program that requires privacy.
     * @since SuperWaba 4.21
     */
   public static boolean showSecrets = true; // guich@421_35

   /** <b>READ-ONLY</b> variable that represents if this device supports high or true color (or, in othe words, is not palletized).
     * @since SuperWaba 4.21
     * @deprecated Now all devices are high-color (16-bit)
     */
   public static boolean isHighColor; // guich@421_45

   /** <b>READ-ONLY</b> variable that represents if this device has a keypad only (many SmartPhones have keypads only).
    * On such devices, presses in the 1-9 and *# pops up the KeyPad class (used in phones without alpha keys).
    * You can set keypadOnly to enable/disable the the KeyPad popup.
    * The disabling occurs everytime a non-digit is pressed.
    * @since SuperWaba 5.7
    */
   public static boolean keypadOnly; // fdie@570_107 the device has a keypad only (no alphanum keyboard)

   /** Defines if the arrow keys will be used to change the focus using the keyboard. Note that some controls
    * will behave differently. It will be true for pen less devices, and it may be changed
    * if your application wants to use navigation keys to change the focus between the controls.
    * @since SuperWaba 5.5
    */
   public static boolean keyboardFocusTraversable; // guich@550_15

   /** <b>READ-ONLY</b> variable that represents a device id that can be used to identify the device's name.
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
   public static String appPath; // guich@581_1
   
   /** To be used in the closeButtonType. Will remove the x/ok button from screen on Windows CE devices.
    * If the device does not support removing the button, it will change to a MINIMIZE_BUTTON, which is the default on CE devices. 
    */
   public static final int NO_BUTTON = 0;
   /** To be used in the closeButtonType. An OK button is placed, and the application is closed when its pressed. */
   public static final int CLOSE_BUTTON = 1;
   /** To be used in the closeButtonType. A X button is placed, and the application is minimized when its pressed. */
   public static final int MINIMIZE_BUTTON  = 2;
   
   /** Set it at the application's static initializer.
    * If set to MINIMIZE_BUTTON, replaces the ok button in Windows CE devices, which closes the application,
    * by the x one, which just minimizes the application. The ok closes the application, and the
    * x minimizes it. By default, it is CLOSE_BUTTON, which closes the application.
    *
    * On BlackBerry, if this is set to <code>CLOSE_BUTTON</code>, the application is closed
    * when the END key is pressed; otherwise, it is sent to background (minimized). The default behavior
    * is sending the application to background (set to <code>MINIMIZE_BUTTON</code>).
    * @since TotalCross 1.11
    * @see #CLOSE_BUTTON
    * @see #MINIMIZE_BUTTON
    * @see #NO_BUTTON
    */
   public static int closeButtonType = CLOSE_BUTTON;

   /** Set it at the application's static initializer. Defines the Symbian UID, which is used when deploying to the Symbian platform.
    * @since TotalCross 1.0
    */
   public static String symbianUID;

   /** Set it at the application's static initializer. Makes the application full screen.
    * Only a simple assignment to true is supported; you cannot check the platform nor any other attribute of this class
    * because the static initializer is called BEFORE this class have its fields set. Also, some platforms set it at Deploying time.
    * Note that Android applications are always full screen.
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
    * @since TotalCross 1.0
    */
   public static String appVersion;

   /** Set it at the application's static initializer. Defines the company's information, which is used in some installations.
    * @since TotalCross 1.0
    */
   public static String companyInfo;

   /** Set it at the application's static initializer. Defines the user's or company's contact email, which is used in some installations.
    * @since TotalCross 1.0
    */
   public static String companyContact;

   /** Set it at the application's static initializer. Defines the application's description, which is used in some installations.
    * @since TotalCross 1.0
    */
   public static String appDescription;

   /** Set it at the application's static initializer. Defines the application's location, which is used in some installations.
    * @since TotalCross 1.0
    */
   public static String appLocation;

   /** Set it at the application's static initializer. Defines the application's category, which is used in some installations.
    * @since TotalCross 1.0
    */
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

   /** <b>READ-ONLY</b> variable that represents the number of the hidden volume (flash memory), or -1 if the device does not have it or if its not a Palm OS device
    * @since TotalCross 1.0
    */
   public static int nvfsVolume = -1;

   /** <b>READ-ONLY</b> variable that represents the smartphone IMEI (if this device is a GSM, UMTS or IDEN smartphone), or null if there's none.
    * @since TotalCross 1.0
    */
   public static String imei;

   /** <b>READ-ONLY</b> variable that represents the smartphone ESN (if this device is a CDMA smartphone) or null if there's none.
    * @since TotalCross 1.0
    */
   public static String esn;

   /** <b>READ-ONLY</b> variable that represents the serial number of the GSM chip or null if there's none. 
    * Works for Windows Mobile and Android.
    * @since TotalCross 1.27
    */
   public static String iccid;

   /** If set to true, the application will ignore the event "Close application" issued by the operating system.
    *  This is specially useful to avoid that an incoming call exits the application on Palm OS.
    *  You must provide a way to terminate the application by calling the exit method.
    *  @see totalcross.ui.MainWindow#exit(int)
    *  @since TotalCross 1.0
    */
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
   
   /** Flag to indicate whether the current TotalCross application is running in the background
    * (minimized) or not.
    * @since TotalCross 1.2
    * @deprecated This can be controlled inside each application as necessary, by observing the following events:
    * {@link totalcross.ui.MainWindow#onMinimize()} and {@link totalcross.ui.MainWindow#onRestore()}.
    */
   public static boolean isMinimized; // bruno@tc122_30

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
    * Currently this value is true for iPhone and Android platforms, and the Blackberry Storm.
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
    
   /** Set to true to enable the transition effects when a window is shown and hidden.
    * @see totalcross.ui.Container#nextTransitionEffect
    * @see totalcross.ui.Container#transitionEffect
    * @since TotalCross 1.2 
    */
   public static boolean enableWindowTransitionEffects;
   
   /** Set to true to debug all events that are received at Window._postEvent. */
   public static boolean debugEvents; // guich@tc125_14

   /**
    * <b>READ-ONLY</b> unique identifier available for registered applications after the TotalCross VM is activated.<br>
    * Value defaults to "NOT AVAILABLE" when running on DEMO.
    * 
    * @since TotalCross 1.25
    */
   public static String activationId = "NOT AVAILABLE";
   
   /** Returns true if the current platform is Windows Mobile or Pocket PC. Note that Windows Desktop (aka WIN32)
    * returns false. */
   public static boolean isWindowsDevice()
   {
      return POCKETPC.equals(platform) || WINDOWSCE.equals(platform) || WINDOWSMOBILE.equals(platform);
   }

   /** Refresh some fields thay may have been updated since the program 
   started.
   * The currently refreshed fields are:
   * <ul>
   * <li> daylightSavings
   * <li> timeZone
   * <li> timeZoneStr
   * </ul>
   * @since TotalCross 1.15
   */
   public static void refresh()
   {
      totalcross.Launcher.instance.settingsRefresh();
   }
   native public static void refresh4D();
   
   /** Set to true to enable the vibration when a MessageBox appears on screen (only on device).
    * @since TotalCross 1.22
    */
   public static boolean vibrateMessageBox; // guich@tc122_51
   
   /** Set to true to disable screen rotation.
    * @since TotalCross 1.27
    */
   public static boolean disableScreenRotation; // guich@tc126_2
 
   /** Set to true to move an Edit or MultiEdit to the top of the screen (inside a SIPBox) 
    * if the application is running in a platform
    * that does not support moving the Soft Input Panel to the top. Otherwise, the SIP will be placed
    * on top of the Edit.
    * You must set this in the MainWindow's constructor, never in the static block.
    * @since TotalCross 1.27
    */
   public static boolean useSIPBox; // guich@tc126_21
   
   /** The size in pixels of the device's system font. */
   public static int deviceFontHeight;

   /** Set to true to put the cursor at the end of the Edit and MultiEdit when focus was set to the control 
    * (default is at the start).
    * @since TotalCross 1.3
    */
   public static boolean moveCursorToEndOnFocus;
   
	// this class can't be instantiated
	private Settings()
	{
	}
}
